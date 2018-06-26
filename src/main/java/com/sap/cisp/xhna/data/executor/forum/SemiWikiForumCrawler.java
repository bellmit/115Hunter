package com.sap.cisp.xhna.data.executor.forum;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Stopwatch;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.common.Util;
import com.sap.cisp.xhna.data.common.cache.LRUCacheUtils;
import com.sap.cisp.xhna.data.config.ConfigStorage;
import com.sap.cisp.xhna.data.config.DataSource;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutorBuilder;
import com.sap.cisp.xhna.data.model.ForumPostInfo;
import com.sap.cisp.xhna.data.storage.ColumnInfo;
import com.sap.cisp.xhna.data.storage.Connections;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;
import com.sun.syndication.feed.synd.SyndEntry;

@SuppressWarnings("rawtypes")
public class SemiWikiForumCrawler extends TaskBarrierExecutor implements
        IForumCrawler {
    private static Logger logger = LoggerFactory
            .getLogger(SemiWikiForumCrawler.class);
    private static final String className = "SemiWikiForumCrawler";
    Map<String, Object> ctx;

    private static final Pattern patternDate = Pattern
            .compile("[\\s]*[0-9]{2}-[0-9]{2}-[0-9]{4}");
    private static final Pattern patternTime = Pattern.compile(
            "[0-9]{2}:[0-9]{2}\\s(?:AM|PM)", Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);
    public static final Pattern patternRelativeWeek = Pattern.compile(
            "[\\s]*[1-4]{1}\\s(?:Week|Weeks)\\sAgo", Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);
    public static final Pattern patternRelativeDay = Pattern.compile(
            "[\\s]*[0-9]{1,2}\\s(?:Day|Days)\\sAgo", Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);
    public static final Pattern patternRelativeHour = Pattern.compile(
            "[\\s]*[0-9]{1,2}\\s(?:Hour|Hours)\\sAgo", Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);
    public static final Pattern patternThreadPostPage = Pattern.compile(
            "(?:(^https://www.semiwiki.com/forum/.*-[0-9]*-[0-9]*.html))",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern patternOnclickText = Pattern.compile(
            "(?:(<a onclick=.*>.*</a>))", Pattern.CASE_INSENSITIVE
                    | Pattern.DOTALL);
    public static final Pattern patternBR = Pattern.compile("(?:(<br>))",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern patternListText = Pattern.compile(
            "(?:(<ul>.*</ul>))", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    public static final Pattern patternImgTag = Pattern.compile(
            "(?:(<img src=.*>))", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final String rootUrl = "https://www.semiwiki.com/forum/login.php?do=login";
    private static Map<String, String> loginCookies;
    static {
        try {
            initCookies();
        } catch (DataCrawlException e) {
            logger.error("Failed to initialize cookies.", e);
        }
    }

    private static void initCookies() throws DataCrawlException {
        Connection.Response res = null;
        try {
            res = Jsoup.connect(rootUrl)
                    .data("user Name", "likesemiwiki", "Password", "12345678")
                    .method(Method.POST).execute();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            logger.error("Cannot connect to the root url {}", rootUrl, e);
        }
        if (res != null)
            loginCookies = res.cookies();
        if (loginCookies == null) {
            throw new DataCrawlException(
                    "Cannot initialize the SemiWiki Cookies.");
        }
    }

    public SemiWikiForumCrawler(Map<String, Object> ctx) {
        super();
        this.ctx = ctx;
    }

    public SemiWikiForumCrawler() {
    }

    public SemiWikiForumCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, SyndEntry entry,
            ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, url, entry, parentExecutor);
    }

    public SemiWikiForumCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, String url, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, url, parentExecutor);
    }

    public SemiWikiForumCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes, List<String> urlList,
            ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, urlList, parentExecutor);
    }

    public SemiWikiForumCrawler(CyclicBarrier barrier, String methodName,
            Class[] parameterTypes,
            List<Map.Entry<String, SyndEntry>> entryList,
            boolean isEntryNeeded, ITaskExecutor parentExecutor) {
        super(barrier, methodName, parameterTypes, entryList, isEntryNeeded,
                parentExecutor);
    }

    // 1 CNN
    public void getSemiWikiForums(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        // google guava stopwatch, to caculate the execution duration
        Stopwatch stopwatch = Stopwatch.createStarted();
        getSemiWikiForumList(ctx, parentExecutor);
        stopwatch.stop();
        long nanos = stopwatch.elapsed(TimeUnit.SECONDS);
        logger.info("Crawl SemiWiki Forums elapsed Time(Seconds) {}", nanos);
    }

    public void getSemiWikiForumList(Map<String, Object> ctx,
            ITaskExecutor parentExecutor) throws Exception {
        TaskParam params = ((ITask) ctx.get("task")).getParam();
        String rootUrl = params.getRss();
        // String[] urlList = { rssUrl };
        rootUrl = "https://www.semiwiki.com/forum/login.php?do=login";
        ArrayList<String> allLinkList = new ArrayList<String>();
        int totalSize = 0;
        try {
            if(loginCookies == null) {
                throw new DataCrawlException("Failed to initialize the login cookies.");
            }
            ArrayList<String> forumList = getForumList(rootUrl, loginCookies);
//             forumList.clear();
            // // forumList.add("https://www.semiwiki.com/forum/f2/");
//             forumList.add("https://www.semiwiki.com/forum/f283/");
            for (String forumUrl : forumList) {
                ArrayList<String> pageUrlList = new ArrayList<String>();
                ArrayList<String> threadLinkList = new ArrayList<String>();
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
                            getPostUrlList(threadLink, loginCookies,
                                    allPostLinkList, parentExecutor);
                            break;
                        } catch (Exception e) {
                            if (retryNum < RETRY_THRESHOLD) {
                                retryNum++;
                                logger.error(
                                        "Cannot get the post url list for the thread url {}. Continue with retry {} times.",
                                        threadLink, retryNum, e);
                                Thread.sleep(30000);
                                initCookies();
                                continue;
                            }
                            logger.error(
                                    "Cannot get the post url list for the thread url {} with retry {} times.",
                                    threadLink, retryNum, e);
                            break;
                        }
                    }
                }
                logger.info("### The forum url {} with thread/post size {}",
                        forumUrl, allPostLinkList.size());
                totalSize = totalSize + allPostLinkList.size();
                allLinkList.addAll(allPostLinkList);
            }
            logger.info("###### The total url {} with thread/post size",
                    totalSize);
            ctx.put("methodName", "crawlSemiWikiFromumThreadPost");
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
                    "Caught Exception during get semiWiki post link urls.", e);
            throw e;
        } catch (Exception e) {
            logger.error(
                    "Caught Exception during get semiWiki post link urls.", e);
            throw e;
        }

    }

    public String crawlSemiWikiFromumThreadPost(String url,
            Map<String, Object> ctx, ITaskExecutor parentExecutor)
            throws Exception {
        StringBuilder keywordsBuilder = new StringBuilder();
        boolean isTopicPage = false;
        String threadTitle = "";
        String topicId = "";
        String topicAuthor = "";
        String topicAuthorUrl = "";
        String source = "SemiWiki.com";
        StringBuilder topicContent = new StringBuilder();
        String topicPostDate = "";
        String topicLastModifiedDate = "";
        Post topicPost = new Post();

        try {
            // Only crawl one page in this method
            ArrayList<String> postUrlList = new ArrayList<String>();
            postUrlList.add(url);

            for (String postUrl : postUrlList) {
                String topicUrl = "";
                Matcher postMatcher = patternThreadPostPage.matcher(postUrl);
                if (postMatcher.find()) {
                    // This is a post page
                    topicUrl = postUrl.substring(0, postUrl.lastIndexOf("-"))
                            + ".html";
                    isTopicPage = false;
                    logger.debug(
                            "*** This is a post url. Get the topic url --> {}",
                            topicUrl);
                } else {
                    isTopicPage = true;
                    topicUrl = postUrl;
                }

                if (!topicUrl.isEmpty()) {
                    Document doc = Jsoup.connect(topicUrl)
                            .timeout(Integer.MAX_VALUE).cookies(loginCookies)
                            // .cookie("__asc", "d55226f014fd999e9ea95d04a16")
                            .get();
                    // logger.debug("Get thread topic page content : {}", doc);
                    // Extract the page title
                    Elements metaTitle = doc.select("h1[class=pagetitle]");
                    if (!metaTitle.isEmpty()) {
                        threadTitle = metaTitle.first().html();
                        logger.debug("*** Thread title --> {}", threadTitle);
                    }

                    // Extract the Tags(keywords) in div
                    Elements metaContent = doc.select("div[data-role=content]");
                    if (!metaContent.isEmpty()) {
                        Elements tagDivs = metaContent.first().select("div");
                        if (!tagDivs.isEmpty()) {
                            // The third div layer...
                            Element tagDiv = tagDivs.get(2);
                            Elements tagsList = tagDiv.select("a[href]");

                            for (Element tagItem : tagsList) {
                                keywordsBuilder.append(tagItem.html()).append(
                                        ",");
                            }
                            if (keywordsBuilder.length() > 1)
                                keywordsBuilder.deleteCharAt(keywordsBuilder
                                        .length() - 1);
                            logger.info(
                                    "*** Get the Tags(keywords) string -> {}",
                                    keywordsBuilder.toString());
                        }
                    }

                    // Extract the topic id: currently is the first page url,
                    // and first post id
                    Elements metaTopicId = doc
                            .select("li[class=postbit postbitim postcontainer]");
                    if (!metaTopicId.isEmpty()) {
                        topicId = metaTopicId.first().attr("id");
                        logger.debug("*** The Topic ID -> {}", topicId);

                        // Extract the topic author, author url, content, date
                        Elements metaTopicAuthorUrl = metaTopicId.first()
                                .select("a[class=postuseravatarlink]");
                        if (!metaTopicAuthorUrl.isEmpty()) {
                            topicAuthorUrl = metaTopicAuthorUrl.first().attr(
                                    "href");
                            logger.debug("*** Author link : {} ",
                                    topicAuthorUrl);
                        }
                        Elements metaTopicAuthor = metaTopicId.first().select(
                                "span[class=xsaid]");
                        if (!metaTopicAuthor.isEmpty()) {
                            if (!metaTopicAuthor.first().select("a[href]")
                                    .isEmpty()) {
                                topicAuthor = metaTopicAuthor.first()
                                        .select("a[href]").first().html();
                                logger.debug("*** Author name : {}",
                                        topicAuthor);
                            }
                        }
                        Elements metaTopicPostDate = metaTopicId.first()
                                .select("span[class=postdate old]");
                        if (!metaTopicPostDate.isEmpty()) {
                            if (!metaTopicPostDate.first()
                                    .select("span[class=date]").isEmpty()) {
                                // need to transfer the format
                                String topicPostDateStr = metaTopicPostDate
                                        .first().select("span[class=date]")
                                        .html().trim();
                                topicPostDate = transferRelativeDate(topicPostDateStr);
                            } else {
                                topicPostDate = "";
                            }
                            logger.debug("*** Topic post date : {}",
                                    topicPostDate);
                        }

                        // Since it's optional, need to check it's real edit
                        // time for topic
                        Elements metaTopicLastModifiedDate = metaTopicId
                                .first()
                                .select("blockquote[class=postcontent lastedited]");
                        if (!metaTopicLastModifiedDate.isEmpty()) {
                            String dateText = metaTopicLastModifiedDate.first()
                                    .html().trim();
                            logger.debug("*** Get modified day --> Test{}Test",
                                    dateText.substring(
                                            dateText.indexOf("; ") + 1,
                                            dateText.indexOf(" at")));
                            String lastModifiedDay = transferRelativeDate(dateText
                                    .substring(dateText.indexOf("; ") + 1,
                                            dateText.indexOf(" at")));
                            String lastModifiedTime = "00:00 AM";
                            Elements metaTopicLastModifiedTime = metaTopicLastModifiedDate
                                    .first().select("span[class=time]");
                            if (!metaTopicLastModifiedTime.isEmpty()) {
                                String lastModifiedTimeStr = metaTopicLastModifiedTime
                                        .first().html().trim();
                                Matcher matcherTime = patternTime
                                        .matcher(lastModifiedTimeStr);
                                if (matcherTime.matches()) {
                                    lastModifiedTime = matcherTime.group();
                                    topicLastModifiedDate = transferModifiedTimeFormat(lastModifiedDay
                                            + " " + lastModifiedTime);
                                }
                            }
                            logger.debug("*** Topic Last modified date: {}",
                                    topicLastModifiedDate);
                        } else {
                            // last modified time is post date
                            topicLastModifiedDate = "";
                        }

                        Elements metaTopicText = metaTopicId.first().select(
                                "blockquote[class=postcontent restore]");
                        if (!metaTopicText.isEmpty()) {
                            String topicContentText = parsePostContent(metaTopicText
                                    .first());
                            logger.debug("*** Topic text : {}",
                                    topicContentText);
                            topicContent.append(topicContentText);
                        }
                    }
                    // Save the topic
                    if (isTopicPage) {
                        topicPost.setId(topicId);
                        topicPost.setAuthor(topicAuthor);
                        topicPost.setAuthorUrl(topicAuthorUrl);
                        topicPost.setContent(topicContent.toString());
                        topicPost.setReplyToId("");
                        topicPost.setSource(source);
                        topicPost.setTitle(threadTitle);
                        topicPost.setUrl(postUrl);
                        topicPost.setKeywords(keywordsBuilder.toString());
                        topicPost.setPostDate(topicPostDate);
                        topicPost.setLastModifiedDate(topicLastModifiedDate);
                        if (parentExecutor != null
                                && !parentExecutor.isCanceled()) {
                            topicPost.save(ctx);
                        }
                        logger.debug("*** Topic object to save --> {}",
                                JSON.toJSONString(topicPost));
                    }
                }
                // crawl posts of a topic
                // Extract the post id: currently is the first page url,
                // and first post id
                Document doc = Jsoup.connect(postUrl)
                        .timeout(Integer.MAX_VALUE).cookies(loginCookies)
                        // .cookie("__asc", "d55226f014fd999e9ea95d04a16")
                        .get();
                // logger.debug("Get thread post content : {}", doc);
                ArrayList<String> postIdList = new ArrayList<String>();
                HashMap<String, String> lastModifiedDateMap = new HashMap<String, String>();
                Elements metaPostId = doc
                        .select("li[class=postbit postbitim postcontainer]");
                if (!metaPostId.isEmpty()) {
                    if (isTopicPage)
                        metaPostId.remove(0); // remove the topic since it has
                                              // been
                                              // handled
                    if (!metaPostId.isEmpty()) {
                        for (Element postIdElement : metaPostId) {
                            postIdList.add(postIdElement.attr("id"));
                            logger.debug("*** The Post ID -> {}",
                                    postIdElement.attr("id"));

                            // Since it's optional, need to check it's real edit
                            // time for topic
                            String postLastmodifiedDate = "";
                            Elements metaPostLastModifiedDate = postIdElement
                                    .select("blockquote[class=postcontent lastedited]");
                            if (!metaPostLastModifiedDate.isEmpty()) {
                                String dateText = metaPostLastModifiedDate
                                        .first().html().trim();
                                logger.debug(
                                        "*** Get modified day --> Test{}Test",
                                        dateText.substring(
                                                dateText.indexOf("; ") + 1,
                                                dateText.indexOf(" at")));
                                String lastModifiedDay = transferRelativeDate(dateText
                                        .substring(dateText.indexOf("; ") + 1,
                                                dateText.indexOf(" at")));
                                String lastModifiedTime = "00:00 AM";
                                Elements metaPostLastModifiedTime = metaPostLastModifiedDate
                                        .first().select("span[class=time]");
                                if (!metaPostLastModifiedTime.isEmpty()) {
                                    String lastModifiedTimeStr = metaPostLastModifiedTime
                                            .first().html().trim();
                                    Matcher matcherTime = patternTime
                                            .matcher(lastModifiedTimeStr);
                                    if (matcherTime.matches()) {
                                        lastModifiedTime = matcherTime.group();
                                        postLastmodifiedDate = transferModifiedTimeFormat(lastModifiedDay
                                                + " " + lastModifiedTime);
                                    }
                                }
                                logger.debug("*** Post Last modified date: {}",
                                        postLastmodifiedDate);
                                if (!postLastmodifiedDate.isEmpty()) {
                                    lastModifiedDateMap.put(
                                            postIdElement.attr("id"),
                                            postLastmodifiedDate);
                                }
                            }

                        }
                    }

                }
                // Extract the post author, author url, content, date
                ArrayList<String> postAuthorList = new ArrayList<String>();
                ArrayList<String> postAuthorUrlList = new ArrayList<String>();
                ArrayList<String> postDateList = new ArrayList<String>();
                Elements metaPostAuthorUrl = doc
                        .select("a[class=postuseravatarlink]");
                if (!metaPostAuthorUrl.isEmpty()) {
                    if (isTopicPage)
                        metaPostAuthorUrl.remove(0); // remove the topic since
                                                     // it
                                                     // has been handled
                    if (!metaPostAuthorUrl.isEmpty()) {
                        for (Element postAuthonUrl : metaPostAuthorUrl) {
                            postAuthorUrlList.add(postAuthonUrl.attr("href"));
                            logger.debug("*** Post Author link : {} ",
                                    postAuthonUrl.attr("href"));
                        }
                    }
                }
                Elements metaPostAuthor = doc.select("span[class=xsaid]");
                if (!metaPostAuthor.isEmpty()) {
                    if (isTopicPage)
                        metaPostAuthor.remove(0); // remove the topic since it
                                                  // has been handled
                    if (!metaPostAuthor.isEmpty()) {
                        for (Element postAuthor : metaPostAuthor) {
                            if (!postAuthor.select("a[href]").isEmpty()) {
                                postAuthorList.add(postAuthor.select("a[href]")
                                        .first().html());
                                logger.debug("*** Author name : {}", postAuthor
                                        .select("a[href]").first().html());
                            }
                        }
                    }
                }

                Elements metaPostDate = doc.select("span[class=postdate old]");
                if (!metaPostDate.isEmpty()) {
                    if (isTopicPage)
                        metaPostDate.remove(0); // remove the topic since it
                                                // has been handled
                    if (!metaPostDate.isEmpty()) {
                        for (Element postDateElement : metaPostDate) {
                            String postDate = "";
                            if (!postDateElement.select("span[class=date]")
                                    .isEmpty()) {
                                // need to transfer the format
                                String postDateStr = postDateElement
                                        .select("span[class=date]").html()
                                        .trim();
                                postDate = transferRelativeDate(postDateStr);
                                postDateList.add(postDate);
                                logger.debug("*** Post date : {}", postDate);
                            }
                        }
                    }

                }
                ArrayList<String> postContentList = new ArrayList<String>();
                Elements metaPostText = doc
                        .select("blockquote[class=postcontent restore]");
                if (!metaPostText.isEmpty()) {
                    if (isTopicPage)
                        metaPostText.remove(0); // remove the topic since it
                                                // has been handled
                    if (!metaPostText.isEmpty()) {
                        for (Element postContent : metaPostText) {
                            String postContentText = parsePostContent(postContent);
                            logger.debug("*** Post text : {}", postContentText);
                            postContentList.add(postContentText);
                        }
                    }
                }
                // create post object for each post to save
                for (int i = 0; i < postIdList.size(); i++) {
                    Post postObj = new Post();
                    postObj.setId(postIdList.get(i));
                    if (postAuthorList.get(i) != null) {
                        postObj.setAuthor(postAuthorList.get(i));
                    } else {
                        postObj.setAuthor("");
                    }
                    if (postAuthorUrlList.get(i) != null) {
                        postObj.setAuthorUrl(postAuthorUrlList.get(i));
                    } else {
                        postObj.setAuthorUrl("");
                    }
                    if (postContentList.get(i) != null) {
                        postObj.setContent(postContentList.get(i));
                    } else {
                        postObj.setContent("");
                    }
                    if (postDateList.get(i) != null) {
                        postObj.setPostDate(postDateList.get(i));
                    } else {
                        postObj.setPostDate("");
                    }
                    if (lastModifiedDateMap.containsKey(postIdList.get(i))) {
                        postObj.setLastModifiedDate(lastModifiedDateMap
                                .get(postIdList.get(i)));
                    } else {
                        // last modified time is ""
                        postObj.setLastModifiedDate("");
                    }
                    postObj.setTitle(threadTitle);
                    postObj.setKeywords(keywordsBuilder.toString());
                    postObj.setSource(source);
                    postObj.setReplyToId(topicId);
                    postObj.setUrl(postUrl);

                    if (parentExecutor != null && !parentExecutor.isCanceled()) {
                        postObj.save(ctx);
                    }
                    logger.debug("*** Post object to save --> {}",
                            JSON.toJSONString(postObj));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        return topicPost.toString();
    }

    private String parsePostContent(Element postContent) {
        String content = Util.EscapeString(postContent.text());
        logger.debug("### The topic/post content length --> {}",
                content.length());
        return content;
    }

    private String transferModifiedTimeFormat(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm aa");
        SimpleDateFormat sdf24 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = null;
        String modifiedTime = "";
        try {
            date = sdf.parse(time);
            if (date != null) {
                modifiedTime = sdf24.format(date);
            }
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            logger.error("Cannot parse the modified time to date.", e);
        }
        return modifiedTime;
    }

    private String transferRelativeDate(String topicPostDateStr) {
        Matcher matcherWeek = patternRelativeWeek.matcher(topicPostDateStr);
        Matcher matcherDay = patternRelativeDay.matcher(topicPostDateStr);
        Matcher matcherHour = patternRelativeHour.matcher(topicPostDateStr);
        Matcher matcherDate = patternDate.matcher(topicPostDateStr);
        String topicPostDate = "";
        if (matcherWeek.find()) {
            logger.debug("*** Match the relative week -> {}",
                    matcherWeek.group());
            topicPostDate = transferRelativeWeek(matcherWeek.group());
        } else if (matcherDay.matches()) {
            logger.debug("*** Match the relative day -> {}", matcherDay.group());
            topicPostDate = transferRelativeDay(matcherDay.group());
        } else if (matcherHour.matches()) {
            logger.debug("*** Match the relative hour -> {}",
                    matcherHour.group());
            topicPostDate = transferRelativeHour(matcherHour.group());
        } else if (matcherDate.matches()) {
            logger.debug("*** Match the date -> {}", matcherDate.group());
            topicPostDate = matcherDate.group().substring(
                    matcherDate.group().lastIndexOf("-") + 1)
                    + "-"
                    + matcherDate.group().substring(0,
                            matcherDate.group().lastIndexOf("-"));
        } else {
            logger.debug("*** None of patterns matches -> {}", topicPostDateStr);
        }
        return topicPostDate;
    }

    private String transferRelativeHour(String group) {
        Pattern patternNumber = Pattern.compile("[0-9]{1,2}");
        Matcher matcher = patternNumber.matcher(group);
        if (!matcher.find())
            return "";
        int HourNum = Integer.parseInt(matcher.group());
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        currentCal.add(Calendar.HOUR, -HourNum);
        return sdf.format(currentCal.getTime());
    }

    private String transferRelativeDay(String group) {
        Pattern patternNumber = Pattern.compile("[0-9]{1,2}");
        Matcher matcher = patternNumber.matcher(group);
        if (!matcher.find())
            return "";
        int dateNum = Integer.parseInt(matcher.group());
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        currentCal.add(Calendar.DATE, -dateNum);
        return sdf.format(currentCal.getTime());
    }

    private String transferRelativeWeek(String group) {
        Pattern patternNumber = Pattern.compile("[1-4]{1}");
        Matcher matcher = patternNumber.matcher(group);
        if (!matcher.find())
            return "";
        int dateNum = Integer.parseInt(matcher.group()) * 7;
        Calendar currentCal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        currentCal.add(Calendar.DATE, -dateNum);
        return sdf.format(currentCal.getTime());
    }

    @Override
    public ArrayList<String> getForumList(String rootUrl,
            Map<String, String> loginCookies) throws Exception {

        // __asc d55226f014fd999e9ea95d04a16
        // And this is the easiest way I've found to remain in session
        Document doc = Jsoup
                .connect("https://www.semiwiki.com/forum/forum.php")
                .cookies(loginCookies)
                // .cookie("__asc", "d55226f014fd999e9ea95d04a16")
                .get();
        // logger.info("Get the rss feed with cookie: {}", doc);

        Elements forumListE = doc.select("div[data-role=content]");
        logger.info("forumList size : {}", forumListE.size());
        ArrayList<String> forumList = new ArrayList<String>();
        // Get the forum list
        // Issue: sub forum
        if (!forumListE.isEmpty()) {
            Elements links = forumListE.select("a[href]");
            logger.info("forum size : {}", links.size());
            for (Element link : links) {
                logger.info(" * a: {}  ", link.attr("abs:href"));
                forumList.add(link.attr("abs:href"));
            }
        }
        return forumList;
    }

    @Override
    public void getPageUrlList(String forumUrl,
            Map<String, String> loginCookies, ArrayList<String> pageUrlList)
            throws Exception {
        Document docForum = null;
        int retryNum = 0;
        while (true) {
            try {
                docForum = Jsoup.connect(forumUrl).cookies(loginCookies).get();
                break;
            } catch (Exception e) {
                if (retryNum < RETRY_THRESHOLD) {
                    retryNum++;
                    logger.error(
                            "Cannot get the page url list for the forum url {}. Continue with retry {} times.",
                            forumUrl, retryNum, e);
                    Thread.sleep(30000);
                    initCookies();
                    continue;
                }
                logger.error(
                        "Cannot get the page url list for the forum url {} with retry {} times.",
                        forumUrl, retryNum, e);
                throw e;
            }
        }
        // logger.info("Get the forum list  with cookie: {}", docForum);
        // Get all pages url
        Elements pageUrlListE = docForum.select("div[class=pagenav]");
        int maxPageNum = 0;
        if (!pageUrlListE.isEmpty()) {
            maxPageNum = Integer.parseInt(pageUrlListE.first().attr(
                    "data-totalpages"));
            logger.info("maxPageNum size : {}", maxPageNum);
            for (int i = 1; i <= maxPageNum; i++) {
                pageUrlList.add(String.format(forumUrl + "i%s.html", i));
            }
            logger.info("Page url list -> {}", pageUrlList);
        } else {
            // two reason:
            // 1- It's a parent forum with subforum;
            // 2- The forum(sub-forum) has only one page, need to keep them to
            // get thread list
            logger.info(
                    "*** Keep the forum url with empty page nav!, forum -> {}",
                    forumUrl);
            pageUrlList.add(forumUrl);
        }
    }

    @Override
    public void getThreadUrlList(String pageUrl,
            Map<String, String> loginCookies, ArrayList<String> threadLinkList)
            throws Exception {
        Document docForumPage = null;
        int retryNum = 0;
        while (true) {
            try {
                docForumPage = Jsoup.connect(pageUrl).cookies(loginCookies)
                        .get();
                break;
            } catch (Exception e) {
                if (retryNum < RETRY_THRESHOLD) {
                    retryNum++;
                    logger.error(
                            "Cannot get the thread url list for the page url {}. Continue with retry {} times.",
                            pageUrl, retryNum, e);
                    Thread.sleep(30000);
                    initCookies();
                    continue;
                }
                logger.error(
                        "Cannot get the thread url list for the page url {} with retry {} times.",
                        pageUrl, retryNum, e);
                throw e;
            }
        }
        Elements threadList = docForumPage.select("div[id=threadlist]");
        ArrayList<String> threadValidLinkList = new ArrayList<String>();
        if (!threadList.isEmpty()) {
            Elements threadLinks = threadList.select("a[href]");

            for (Element link : threadLinks) {
                if (link.attr("abs:href").contains(
                        "https://www.semiwiki.com/forum/member")
                        || link.attr("abs:href").contains("-new-post.html"))
                    continue;
                // logger.info(" * page {}, a: {}  ", pageUrl,
                // link.attr("abs:href"));
                threadValidLinkList.add(link.attr("abs:href"));
            }
            logger.info("Page : {}, valid threadLinks size : {}", pageUrl,
                    threadValidLinkList.size());
        }
        threadLinkList.addAll(threadValidLinkList);
    }

    @Override
    public void getPostUrlList(String threadUrl,
            Map<String, String> loginCookies, ArrayList<String> threadLinkList,
            ITaskExecutor parentExecutor) throws Exception {
        logger.info("The thread link  -> {}", threadUrl);
        // For each thread, need to get all the post via sub urls
        // Issue: 标点符号,如'
        // https://www.semiwiki.com/forum/f2/will-china’s-$100b-investment-pay-off-6602.html
        // https://www.semiwiki.com/forum/f2/2013-year-review-Â-semiconductor-equipment-materials-market-outlook-3973.html
        // https://www.semiwiki.com/forum/f2/atmelÂs-tech-tour-heads-sf-3883.html
        // https://www.semiwiki.com/forum/f2/eppur-si-muove-yet-moves-Â-cloud-beyond-1454.html

        // Issue: use separate cache
        // Issue: new post on the old post page, have to wait for a new post
        // page available
        // Issue: content size > 5000, this case also occurs when
        // abstractExtracter cannot work normally, use NCLOB,done
        // Issue: abstractExtracter cannot work normally, use special parsing as
        // fix, done
        // Issue: 表情图片需要过滤, done
        try {
            Document docThreadPage = Jsoup.connect(threadUrl)
                    .cookies(loginCookies).get();
            Elements threadUrlListE = docThreadPage
                    .select("div[class=pagenav]");
            ArrayList<String> threadUrlList = new ArrayList<String>();
            int maxPageNum = 0;
            if (!LRUCacheUtils.checkIfTraditionalUrlCached(threadUrl)
                    || !parentExecutor.isCacheable()) {
                logger.debug("Crawl the thread url {}.", threadUrl);
                threadUrlList.add(threadUrl);
            } else {
                // Here may need more logic, new posts is a question
                // mark?
                logger.info("The thread url {} has been crawled before.",
                        threadUrl);
                threadUrlList.clear();
                return;
            }

            if (!threadUrlListE.isEmpty()) {
                maxPageNum = Integer.parseInt(threadUrlListE.first().attr(
                        "data-totalpages"));
                logger.info("maxPageNum size : {}", maxPageNum);
                for (int i = 2; i <= maxPageNum; i++) {
                    String threadPageUrl = String.format(
                            threadUrl.substring(0, threadUrl.indexOf(".html"))
                                    + "-%s.html", i);
                    if (!LRUCacheUtils
                            .checkIfTraditionalUrlCached(threadPageUrl)
                            || !parentExecutor.isCacheable()) {
                        logger.debug("Crawl the Post thread url {}.",
                                threadPageUrl);
                        threadUrlList.add(threadPageUrl);
                    } else {
                        // Here may need more logic, update time is a question
                        // mark?
                        logger.info("The url {} has been crawled before.",
                                threadPageUrl);
                    }
                }
            }
            logger.info("Thread url list -> {}", threadUrlList);
            threadLinkList.addAll(threadUrlList);
        } catch (Exception e) {
            logger.error("Connect thread link {} error.", threadUrl, e);
            throw e;
        }

    }

    private String[] addInfo(String[] info, String... additionals) {
        String[] res = new String[info.length + additionals.length];
        System.arraycopy(info, 0, res, 0, info.length);
        for (int i = 0; i < additionals.length; ++i) {
            res[info.length + i] = additionals[i];
        }
        return res;
    }

    private synchronized void writeDB(List<String[]> strucute_data)
            throws Exception {
        String schema = "FORUM";
        String table = "FORUM_POSTS";

        DataSource ds = ConfigStorage.DS_INFO;
        String DriverClassName = ds.getDriver();
        String url = ds.getUrl();
        String username = ds.getUserName();
        String password = ds.getPassword();

        try {
            Class.forName(DriverClassName);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            logger.error("No JDBC Driver found!");
            throw e;
        }
        Connections connections = new Connections(DriverClassName, url,
                username, password);
        java.sql.Connection conn = connections.getConnection();
        ColumnInfo column_info = new ColumnInfo(conn, schema, table,
                strucute_data);
        column_info.insertData();
        logger.info("Write Table [{}.{}] Successed!", schema, table);
    }

    @Override
    public void save(Map<String, Object> ctx, List<String> data)
            throws Exception {
        if (parentExecutor.isCanceled() || parentExecutor.isTestFlagOn())
            return;
        // write to HANA
        if (data == null || data.isEmpty()) {
            logger.warn("No data Needs to be Persisted!");
            return;
        }
        String key = ((ITask) ctx.get("task")).getParam().getString("task_key");
        List<String[]> output = new ArrayList<String[]>();
        for (String row : data) {
            ForumPostInfo di = new ForumPostInfo(row);
            logger.info("*** Construct the data line to insert: {}",
                    (Object[]) addInfo(di.getAttributeValueList(), key));
            output.add(addInfo(di.getAttributeValueList(), key));
        }
        writeDB(output);
    }

    public static void main(String... args) throws Exception {
    }

}
