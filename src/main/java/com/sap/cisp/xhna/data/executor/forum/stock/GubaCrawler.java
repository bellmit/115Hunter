/*2015-11-16 this file is from SemiWikiForumCrawler*/

package com.sap.cisp.xhna.data.executor.forum.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.embedded.RedisExecProvider;
import redis.embedded.RedisServer;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.common.cache.LRUCacheUtils;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutorBuilder;
import com.sap.cisp.xhna.data.executor.forum.IForumCrawler;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sap.cisp.xhna.data.config.ConfigInstance;

public class GubaCrawler extends TaskBarrierExecutor implements IForumCrawler{
    private static Logger logger = LoggerFactory
            .getLogger(GubaCrawler.class);
    
    private static final String className = "GubaCrawler";
    /*the loginCookies is no use in this file, for same interface as other*/
    private static Map<String, String> loginCookies;
    private int connect_times = 3;
    private int connect_timeout = 30000;
    
    private static Jedis jedis = null;
    private static boolean first_stock = true;
    public GubaCrawler() {
    }
    
    public GubaCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, List<String> urlList,
            ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, urlList, parentExecutor);
    }
    
    public GubaCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, ITaskExecutor parentExecutor) {    	
        super(barrier, methodName, parameterTypes, url, parentExecutor);
    }
    
    public void getGubaPosts(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();
        JedisPool pool = null;
        RedisServer redisServer = null;
        if(ConfigInstance.GUBA_REDIS_ON)
        {
        	/*connect to redis server*/        	
            try
            {    
            	RedisExecProvider customProvider = RedisExecProvider.defaultProvider();
            	redisServer = RedisServer.builder()
      				  .redisExecProvider(customProvider)
      				  .port(6379)
      				  .setting("databases 3")
      				  .setting("save 900 1")
      				  .setting("save 300 10")
      				  .setting("save 60 10000")
      				  .setting("rdbcompression yes")
      				  .setting("dbfilename guba.rdb")
      				  //.setting("dir C:/redis/")
      				  .setting("dir /root/redis/")
      				  //.setting("auto-aof-rewrite-percentage 100")
      				  //.setting("auto-aof-rewrite-min-size 64mb")
      				  //.setting("appendonly yes")
      				  .build();
            	redisServer.start();
            	logger.info("redisServer.start");
            	pool = new JedisPool(ConfigInstance.GUBA_REDIS_SERVER, ConfigInstance.GUBA_REDIS_PORT);
            	jedis = pool.getResource();
            }
            catch(Exception e)
        	{
            	logger.error("getGubaPosts can't connect to Redis server {}:{}", ConfigInstance.GUBA_REDIS_SERVER, ConfigInstance.GUBA_REDIS_PORT, e);
                throw e;
        	}
        }
        getGubaForumList(ctx, parentExecutor);
        if(ConfigInstance.GUBA_REDIS_ON)
        {
        	if (jedis != null)
        	{
        		pool.returnResource(jedis);
        		redisServer.stop();
        		logger.info("redisServer.stop");
        	}
        } 
        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info("Crawl Guba stock forums elapsed Time(Seconds) {}", nanos);
    }
    
    public void getGubaForumList(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {

        String rootUrl = "http://guba.eastmoney.com/remenba.aspx?type=1";
        ArrayList<String> allLinkList = new ArrayList<String>();
        int totalSize = 0;        
        try {
        	/*forumList such as 'http://guba.eastmoney.com/list,600000.html'*/
            ArrayList<String> forumList = getForumList(rootUrl, loginCookies);

            for (String forumUrl : forumList) {
            	/*pageUrl such as 'http://guba.eastmoney.com/list,600000_2.html'*/
                ArrayList<String> pageUrlList = new ArrayList<String>();
                /*threadLinkList such as 'http://guba.eastmoney.com/news,600000,201804097.html'*/
                ArrayList<String> threadLinkList = new ArrayList<String>();
                /*PostLinkList   such as 'http://guba.eastmoney.com/news,600000,201804097_2.html'*/
                ArrayList<String> allPostLinkList = new ArrayList<String>();
                
                getPageUrlList(forumUrl, loginCookies, pageUrlList);
                
                for (String pageUrl : pageUrlList) {
                    getThreadUrlList(pageUrl, loginCookies, threadLinkList);
                }
                
                for (String threadLink : threadLinkList) {
                    // Crawl the whole thread posts together to make correlation
                    int retryNum = 0;
                    while (true) {
                        try {
                            getPostUrlList(threadLink, loginCookies, allPostLinkList, parentExecutor);
                            break;
                        } catch (Exception e) {
                            if (retryNum < connect_times) {
                                retryNum++;
                                logger.error(
                                        "Cannot get the post url list for the thread url {}. Continue with retry {} times.",
                                        threadLink, retryNum, e);
                                Thread.sleep(30000);
                                continue;
                            }
                            logger.error(
                                    "Cannot get the post url list for the thread url {} with retry {} times.",
                                    threadLink, retryNum, e);
                            break;
                        }
                    }
                }

                logger.info("### The Guba urls {} with post size {}", forumUrl, allPostLinkList.size());
                totalSize = totalSize + allPostLinkList.size();
                allLinkList.addAll(allPostLinkList);
            }
            logger.info("###### The total url {} with post size", allLinkList.size());
            ctx.put("methodName", "crawlGubaPost");
            ctx.put("parameterTypes", new Class[] { String.class, Map.class,
                    ITaskExecutor.class });
            ctx.put("className", className);
            if (parentExecutor != null && !parentExecutor.isCanceled()
                    && !allLinkList.isEmpty()) {
                CyclicBarrier parentBarrier = new CyclicBarrier(2);
                TaskBarrierExecutorBuilder.getInstance()
                        .buildWebPageTaskBarrier(allLinkList, ctx,
                                parentExecutor, parentBarrier);
                parentBarrier.await();
            }
        } catch (IOException e) {
            logger.error(
                    "Caught Exception during get Guba post link urls.", e);
            throw e;
        } catch (Exception e) {
            logger.error(
                    "Caught Exception during get Guba post link urls.", e);
            throw e;
        }
    }
    
    public ArrayList<String> getForumList(String rootUrl,
            Map<String, String> loginCookies) throws Exception {
    	/*get all stocks urls*/
    	ArrayList<String> forumList = new ArrayList<String>();
    	try
    	{
    		Document doc = Jsoup.connect(rootUrl).timeout(connect_timeout).get();
        	Elements eles = doc.getElementsByClass("ngbggulbody");
        	Element ele = eles.first().child(0).child(0);
        	Elements nums = ele.getElementsByTag("a");
        	
        	int start = Math.min(ConfigInstance.GUBA_START_STOCK, nums.size()) - 1;
        	int end = Math.min(ConfigInstance.GUBA_END_STOCK, nums.size());
        	logger.info("stock number:" + nums.size() + "/start stock:" + start + "/end stock:" + end + "/pages:" + ConfigInstance.GUBA_PAGES_ONE_STOCK);

        	for(Element num : nums.subList(start, end))
        	{   
        		String destUrl = "http://guba.eastmoney.com/list," + num.text().substring(1, 7) + ".html";
        		forumList.add(destUrl);
        	}
    	}
    	catch(Exception e)
    	{
    		logger.error("getForumList:", e);
            throw e;
    	}
    	
        return forumList;
    }

    @Override
    /*get all post url of this stock*/
    public void getPageUrlList(String forumUrl,
            Map<String, String> loginCookies, ArrayList<String> pageUrlList)
            throws Exception {
        Document docForum = null;
        int retryNum = 0;
        while (true) {
            try {
                docForum = Jsoup.connect(forumUrl).timeout(connect_timeout).get();
                break;
            } catch (Exception e) {
                if (retryNum < connect_times) {
                    retryNum++;
                    logger.error(
                            "Cannot get the page url list for the Guba url {}. Continue with retry {} times.",
                            forumUrl, retryNum, e);
                    Thread.sleep(30000);

                    continue;
                }
                logger.error(
                        "Cannot get the page url list for the Guba url {} with retry {} times.",
                        forumUrl, retryNum, e);
                throw e;
            }
        }

        // Get all pages url
        Elements pageUrlListE = docForum.select("span[class=pagernums]");

        String page_info = pageUrlListE.first().attr("data-pager");
        
        String[] page_info_array = page_info.split("\\|");
        /*calucate the total pages*/
        int total_post_number = Integer.parseInt(page_info_array[1]);
        /*int post_number_page = Integer.parseInt(page_info_array[2]);
        int maxPageNum = total_post_number / post_number_page;
        if((total_post_number % post_number_page) != 0)
        {
        	maxPageNum++;
        }*/
        
        int crawl_pages = total_post_number / (80 * 100);
        if(crawl_pages < 5)
        {
        	crawl_pages = 5;
        }
        logger.info(forumUrl + " pages numer:" + crawl_pages);
        String pre_url = forumUrl.replace(".html", "");
        for (int i = 1; i <= crawl_pages; i++) 
        {
            pageUrlList.add(String.format(pre_url + "_%d.html", i));
        }
        //logger.info("Page url list -> {}", pageUrlList);       
    }

    @Override
    public void getThreadUrlList(String pageUrl,
            Map<String, String> loginCookies, ArrayList<String> threadLinkList)
            throws Exception {
        Document docForumPage = null;
        int retryNum = 0;
        logger.info("pageUrl input : {}", pageUrl);
        while (true) {
            try {
                docForumPage = Jsoup.connect(pageUrl).timeout(connect_timeout).get();
                break;
            } catch (Exception e) {
                if (retryNum < connect_times) {
                    retryNum++;
                    logger.error(
                            "Cannot get the thread url list for the page url {}. Continue with retry {} times.",
                            pageUrl, retryNum, e);
                    Thread.sleep(30000);
                    continue;
                }
                logger.error(
                        "Cannot get the thread url list for the page url {} with retry {} times.",
                        pageUrl, retryNum, e);
                throw e;
            }
        }
        
        Elements threadLists = docForumPage.select("div[class=articleh]");
        ArrayList<String> threadValidLinkList = new ArrayList<String>();
        if (!threadLists.isEmpty()) {
        	for (Element link : threadLists)
        	{
        		/*not crawl top articles*/
				Elements ems = link.getElementsByTag("em");
				if(first_stock == false && !ems.isEmpty() && ems.first().hasClass("settop"))
				{
        			continue;
        		}	
        		
        		Elements threadLink = link.select("a[href]");
        		String url = threadLink.first().attr("href");
        		        		        		        		
        		if(url.startsWith("/"))
        		{
        			url = "http://guba.eastmoney.com" + url;
        		}
        		else
        		{
        			url = "http://guba.eastmoney.com/" + url;
        		}
        		//logger.info("http://guba.eastmoney.com" + threadLink.first().attr("href"));        		       		
        		       		
        		if(ConfigInstance.GUBA_REDIS_ON)
        		{
        			/*check redis history records*/
            		if(jedis != null)
            		{
            			if(jedis.exists(url.replace(".html", "").replace("http://guba.eastmoney.com/news,", "")))
            			{
            				logger.info("{} has been crawled before", url);
            				continue;
            			}
            		}
        		}
        		threadValidLinkList.add(url);
        	}

            logger.info("Page : {}, valid threadLinks size : {}", pageUrl, threadValidLinkList.size());
        }
        threadLinkList.addAll(threadValidLinkList); 
        first_stock = false;
    }

    @Override
    public void getPostUrlList(String threadUrl,
            Map<String, String> loginCookies, ArrayList<String> threadLinkList,
            ITaskExecutor parentExecutor) throws Exception {  
    	logger.info("The thread link  -> {}", threadUrl);
    	ArrayList<String> threadUrlList = new ArrayList<String>();
        try {
            Document docThreadPage = Jsoup.connect(threadUrl).timeout(connect_timeout).get();
         // Get all pages url
            Elements pageUrlListE = docThreadPage.select("span[class=pagernums]");
            /*just one page*/
            if(pageUrlListE.isEmpty())
            {
            	if (!LRUCacheUtils
                        .checkIfTraditionalUrlCached(threadUrl)
                        || !parentExecutor.isCacheable()) {
                    logger.debug("Crawl the Post thread 1 url {}.",
                    		threadUrl);
                    threadUrlList.add(threadUrl);
                } else {
                    logger.info("The url {} has been crawled before.",
                    		threadUrl);
                }
            }
            else
            {
            	String page_info = pageUrlListE.first().attr("data-page");
                
                String[] page_info_array = page_info.split("\\|");
                /*calucate the total pages*/
                int total_post_number = Integer.parseInt(page_info_array[1]);
                int post_number_page = Integer.parseInt(page_info_array[2]);
                int maxPageNum = total_post_number / post_number_page;
                if((total_post_number % post_number_page) != 0)
                {
                	maxPageNum++;
                }
                //logger.info("maxPageNum size : {}", maxPageNum);
                String pre_url = threadUrl.replace(".html", "");
                for (int i = 1; i <= maxPageNum; i++) 
                {
                	String threadPageUrl = String.format(pre_url + "_%d.html", i);
                	if (!LRUCacheUtils
                            .checkIfTraditionalUrlCached(threadPageUrl)
                            || !parentExecutor.isCacheable()) {
                        logger.debug("Crawl the Post thread urls {}.",
                                threadPageUrl);
                        threadUrlList.add(threadPageUrl);
                    } else {
                        logger.info("The url {} has been crawled before.",
                                threadPageUrl);
                    }
                }
            }
           
            //logger.info("Thread url list -> {}", threadUrlList);
            threadLinkList.addAll(threadUrlList);
        } catch (Exception e) {
            logger.error("Connect thread link {} error.", threadUrl, e);
            throw e;
        }
    }
    
    public String crawlGubaPost(String url, Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws Exception 
    {
        String title = "";
        String postId = "";
        String author = "";
        String authorId = "";
        StringBuilder content = new StringBuilder();
        String postDate = "";
        String replyCount = "";
        String replyToId = "";
        String forwardCount = "";
        String stockName = "";
        GubaPost topicPost = new GubaPost();
        
        /*postId*/
    	String [] tempArray = url.split(",");            	
    	postId = tempArray[2].substring(0, 9);
    	
        try 
        {            
        	Document doc = null;
            int retryNum = 0;
            while (true) {
                try {
                    doc = Jsoup.connect(url).timeout(connect_timeout).get();
                    break;
                } catch (Exception e) {
                    if (retryNum < connect_times) {
                        retryNum++;
                        logger.error(
                                "crawlGubaPost Cannot get the page url list for the Guba url {}. Continue with retry {} times.",
                                url, retryNum, e);
                        Thread.sleep(30000);

                        continue;
                    }
                    logger.error(
                            "crawlGubaPost Cannot get the page url list for the Guba url {} with retry {} times.",
                            url, retryNum, e);
                    throw e;
                }
             }
            
        	/*stockName*/
        	Element stockName_div = doc.getElementById("stockname");
        	stockName = stockName_div.text();
        	
        	Element title_div = doc.getElementById("zwconttbt");
            
            /*this page includes original post or only includes replies*/
            if(title_div != null)
            {
            	/*title*/
            	title = title_div.text();           	
            	/*author and authorId*/
            	Element author_div = doc.getElementById("zwconttbn");
            	author = author_div.child(0).child(0).text();
            	authorId = author_div.child(0).child(0).attr("data-popper");
            	/*content*/
            	Element content_div = doc.getElementById("zw_body");
            	if(content_div != null)
            	{
            		Elements ps = content_div.getElementsByTag("p");
                	
                	for(Element p : ps)
                	{
                		Elements pChilds = p.children();
                		if(pChilds.isEmpty())
                		{
                			content.append(p.text());
                		}        		
                	}
            	}
            	else
            	{
            		Element con = doc.getElementById("zwconbody");
            		Elements ps = con.getElementsByTag("p");
            		if(ps.isEmpty())
            		{
            			content.append(con.child(0).text());
            		}
            		else
            		{
            			for(Element p : ps)
                    	{
                    		Elements pChilds = p.children();
                    		if(pChilds.isEmpty())
                    		{
                    			content.append(p.text());
                    		}        		
                    	}
            		}     
            	}
            	
            	/*postDate*/
            	Elements times = doc.getElementsByClass("zwfbtime");
            	String time_text = times.first().text();

            	Pattern pattern = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}");
    			Matcher matcher = pattern.matcher(time_text);
    			if (matcher.find()) {
    				postDate = matcher.group();
    			}
    			/*replyCount*/
    			Element tab = doc.getElementById("zwcontab");
    			if(tab != null)
    			{
    				String temp_reply = tab.child(0).child(0).child(0).text();

        			Pattern pattern1 = Pattern.compile("(\\d+)");
        			Matcher matcher1 = pattern1.matcher(temp_reply);
        			if (matcher1.find()) {
        				replyCount = matcher1.group();
        			}
    			}
    			
    	        /*forwardCount*/
    			Element forward_zone = doc.getElementById("zfnums");
    			if(forward_zone != null)
    			{
    				forwardCount = forward_zone.text();
    			}
    			
    			
    			topicPost.setStockName(stockName);
    			topicPost.setId(postId);
    			topicPost.setTitle(title);
    			topicPost.setAuthor(author);
    			topicPost.setAuthorId(authorId);
    			topicPost.setContent(content.toString());
    			topicPost.setPostDate(postDate);
    			topicPost.setReplyCount(replyCount);
    			topicPost.setReplyToId("");
    			topicPost.setUrl(url);
    			topicPost.setForwardCount(forwardCount);
    			if (parentExecutor != null
                        && !parentExecutor.isCanceled()) {
                    topicPost.save(ctx);
                }
                //logger.debug("*** Topic object to save --> {}", JSON.toJSONString(topicPost));    			
                
                Element reply_zone = doc.getElementById("zwlist");
    			Elements replies = reply_zone.getElementsByClass("clearfix");
                for(Element reply : replies)
                {
                	String reply_autor = "";
                	String reply_autorId = "";
                	String reply_postDate = "";
                	String reply_content = "";
                	String reply_id = "";
                    /*reply_id*/
                	reply_id = reply.attr("id");
                	Elements autor_span = reply.getElementsByClass("zwnick");
                	/*reply_autor*/
                	reply_autor = autor_span.first().child(0).text();
                	reply_autorId = autor_span.first().child(0).attr("data-popper");
                	/*reply_postDate*/
                	Elements time_span = reply.getElementsByClass("zwlitime");
                	String temp_time = time_span.first().text();
                	Pattern pattern2 = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}");
        			Matcher matcher2 = pattern2.matcher(temp_time);
        			if (matcher2.find()) {
        				reply_postDate = matcher2.group();
        			}
        			/*reply_content*/
        			Elements content_span = reply.getElementsByClass("stockcodec");
        			reply_content = content_span.first().text();

        			GubaPost replyPost = new GubaPost();
        			replyPost.setStockName(stockName);
                	replyPost.setId(reply_id);
                	replyPost.setTitle("");
                	replyPost.setAuthor(reply_autor);
                	replyPost.setAuthorId(reply_autorId);
                	replyPost.setContent(reply_content);
                	replyPost.setPostDate(reply_postDate);
                	replyPost.setUrl(url);
                	replyPost.setReplyToId(postId);
                	if (parentExecutor != null
                            && !parentExecutor.isCanceled()) {
                		replyPost.save(ctx);
                    }
                    //logger.debug("*** reply object to save --> {}", JSON.toJSONString(replyPost));
                }
                
                if(ConfigInstance.GUBA_REDIS_ON)
                {                	
        	        if(jedis != null)
            		{
        	        	String str = url.replace("http://guba.eastmoney.com/news,", "").replace(".html", "").replace("_1", "");
        	        	logger.info("{} put into redis.", str);
        	        	jedis.mset(str, "");            			            			
            		}
                }               
            }
            else
            {
            	Element reply_zone = doc.getElementById("zwlist");
    			Elements replies = reply_zone.getElementsByClass("clearfix");
                for(Element reply : replies)
                {
                	String reply_autor = "";
                	String reply_autorId = "";
                	String reply_postDate = "";
                	String reply_content = "";
                	String reply_id = "";
                    /*reply_id*/
                	reply_id = reply.attr("id");
                	Elements autor_span = reply.getElementsByClass("zwnick");
                	/*reply_autor*/
                	reply_autor = autor_span.first().child(0).text();
                	reply_autorId = autor_span.first().child(0).attr("data-popper");
                	/*reply_postDate*/
                	Elements time_span = reply.getElementsByClass("zwlitime");
                	String temp_time = time_span.first().text();
                	Pattern pattern2 = Pattern.compile("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}");
        			Matcher matcher2 = pattern2.matcher(temp_time);
        			if (matcher2.find()) {
        				reply_postDate = matcher2.group();
        			}
        			/*reply_content*/
        			Elements content_span = reply.getElementsByClass("stockcodec");
        			reply_content = content_span.first().text();

        			GubaPost replyPost = new GubaPost();
        			replyPost.setStockName(stockName);
                	replyPost.setId(reply_id);
                	replyPost.setTitle("");
                	replyPost.setAuthor(reply_autor);
                	replyPost.setAuthorId(reply_autorId);
                	replyPost.setContent(reply_content);
                	replyPost.setPostDate(reply_postDate);
                	replyPost.setUrl(url);
                	replyPost.setReplyToId(postId);
                	if (parentExecutor != null
                            && !parentExecutor.isCanceled()) {
                		replyPost.save(ctx);
                    }
                    //logger.debug("***only reply object to save --> {}", JSON.toJSONString(replyPost));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("", e);
            throw e;
        }

        return topicPost.toString();
    }


}
