package com.sap.cisp.xhna.data.executor.forum.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sap.cisp.xhna.data.common.DataCrawlException;
import com.sap.cisp.xhna.data.executor.ITaskExecutor;
import com.sap.cisp.xhna.data.executor.barrier.TaskBarrierExecutor;
import com.sap.cisp.xhna.data.executor.forum.IForumCrawler;
import com.sap.cisp.xhna.data.executor.forum.SemiWikiForumCrawler;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class GubaAccountInfoCrawler extends TaskBarrierExecutor implements IForumCrawler {
	private static Logger logger = LoggerFactory.getLogger(GubaAccountInfoCrawler.class);

	private static String loginUrl = "http://passport.eastmoney.com/ajax/lg.aspx";
	private String accountPageUrlFormat = "http://iguba.eastmoney.com/%s/";
	private String postPageUrlFormat = "http://guba.eastmoney.com/%s";

	private static String pageUrlFormat = "http://guba.eastmoney.com/list,600000_%d.html";
	private static String pagingUrl = "http://iguba.eastmoney.com/action.aspx?action=tafans&page=%d&uid=%s&rnd=%d";

	private static Map<String, String> loginCookies;
	private Set<String> accountIdSet = new HashSet<String>();

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
			Double r = Math.random();
			res = Jsoup.connect(loginUrl)
					.data("vcode", "", "u", "mdifferent@163.com", "p", "q1w2e3r4", "r", r.toString())
					.method(Method.POST).execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("Cannot connect to the root url {}", loginUrl, e);
		}
		if (res != null)
			loginCookies = res.cookies();
		if (loginCookies == null) {
			throw new DataCrawlException("Cannot initialize the Forum Cookies.");
		}
	}

	public void getGubaAccountInfo(Map<String, Object> ctx, ITaskExecutor parentExecutor) throws Exception {
		// TaskParam params = ((ITask) ctx.get("task")).getParam();
		try {
			if (loginCookies == null) {
				throw new DataCrawlException("Failed to initialize the login cookies.");
			}
			logger.info("Begin to get forum url list:");
			ArrayList<String> forumList = getForumList("http://guba.eastmoney.com/remenba.aspx", loginCookies);
			for (String forumUrl : forumList) {
				forumUrl = String.format("http://guba.eastmoney.com/%s", forumUrl);
				ArrayList<String> pageUrlList = new ArrayList<String>();
				ArrayList<String> threadLinkList = new ArrayList<String>();
				logger.info("Begin to get page url list:");
				getPageUrlList(forumUrl, loginCookies, pageUrlList);
				logger.info("Begin to get thread url list:");
				for (String pageUrl : pageUrlList) {
					//System.out.println(pageUrl);
					getThreadUrlList(pageUrl, loginCookies, threadLinkList);
				}
				pageUrlList.clear();
				ArrayList<String> allPostLinkList = new ArrayList<String>();
				logger.info("Begin to get post url list:");
				for (String threadLink : threadLinkList) {
					//System.out.println(threadLink);
					getPostUrlList(threadLink, loginCookies, allPostLinkList, null);
				}
				threadLinkList.clear();
				logger.info("Begin to get account list in reply:");
				for (String postLink : allPostLinkList) {
					//System.out.println(postLink);
					getReplyAccounts(postLink, loginCookies);
				}
				logger.debug("Account count: {}", accountIdSet.size());
				allPostLinkList.clear();
				logger.info("Begin to get account info:");
				for (String uid : accountIdSet) {
					System.out.println("Get info of account:" + uid);
					getAccountInfo(uid, loginCookies).save(ctx);       
				}
			}
			logger.info("Crawler task finished!");
		} catch (IOException e) {
			logger.error("Caught Exception during get semiWiki post link urls.", e);
			throw e;
		} catch (Exception e) {
			logger.error("Caught Exception during get semiWiki post link urls.", e);
			throw e;
		}
	}

	/* 获取每一个论坛的URL */
	@Override
	public ArrayList<String> getForumList(String rootUrl, Map<String, String> loginCookies) throws Exception {
		ArrayList<String> forumList = new ArrayList<String>();
		
		Document docForum = getDocOfUrl(rootUrl, loginCookies);
		Elements marketList = docForum.select("div[class=ngbglistdiv]");
		marketList.forEach(element -> {
			Elements stockList = element.select("ul[class=ngblistul2] li a");
			stockList.forEach(stockElement -> {
				String stockUrl = stockElement.attr("href");
				//logger.debug(stockUrl);
				if (!stockUrl.startsWith("http"))
					forumList.add(stockUrl);
			});
		});
		//forumList.forEach(f -> System.out.println(f));
		//forumList.add("list,600000.html");		//TODO for test
		return forumList;
	}

	/* 获取论坛每一页的URL */
	@Override
	public void getPageUrlList(String forumUrl, Map<String, String> loginCookies, ArrayList<String> pageUrlList)
			throws Exception {
		Document docForum = getDocOfUrl(forumUrl, loginCookies);

		Elements postCountElement = docForum.select("div[class=pager]");
		if (postCountElement != null && !postCountElement.isEmpty()) {
			String postCountStr = postCountElement.toString();
			int beginIndex = postCountStr.indexOf("数");
			int endIndex = postCountStr.indexOf("篇");
			int postCount = Integer.parseInt(postCountStr.substring(beginIndex + 1, endIndex).trim());
			int pageCount = (postCount % 80 == 0) ? (postCount / 80) : (postCount / 80) + 1;
			//if (pageCount > 1)
			//	pageCount = 1;													//TODO for test
			logger.info("maxPageNum size : {}", pageCount);
			for (int i = 1; i <= pageCount; i++) {
				pageUrlList.add(String.format(pageUrlFormat, i));
			}
		} else {
			// two reason:
			// 1- It's a parent forum with subforum;
			// 2- The forum(sub-forum) has only one page, need to keep them to
			// get thread list
			logger.info("*** Keep the forum url with empty page nav!, forum -> {}", forumUrl);
			pageUrlList.add(forumUrl);
		}
	}

	/* 获取每一个帖子的URL */
	@Override
	public void getThreadUrlList(String pageUrl, Map<String, String> loginCookies, ArrayList<String> threadLinkList)
			throws Exception {
		Document docForum = getDocOfUrl(pageUrl, loginCookies);
		Elements postCountElement = docForum.select("span[class=l3] a[href]");
		postCountElement.forEach(element -> {
			String pageId = element.attr("href");
			if (pageId.startsWith("/"))
				pageId = pageId.substring(1);
			threadLinkList.add(String.format(postPageUrlFormat, pageId));
		});
		/*
		ListIterator<Element> it = postCountElement.listIterator();
		while (it.hasNext()) {
			Element titleNode = it.next();
			String pageId = titleNode.attr("href");
			if (pageId.startsWith("/"))
				pageId = pageId.substring(1);
			threadLinkList.add(String.format(postPageUrlFormat, pageId));
		}*/
	}

	/* 获取每一个thread的所有account的URL */
	@Override
	public void getPostUrlList(String threadUrl, Map<String, String> loginCookies, ArrayList<String> allPostLinkList,
			ITaskExecutor parentExecutor) throws Exception {
		
		Document docForum = getDocOfUrl(threadUrl, loginCookies);
		// 发布者的id
		Elements postCountElement = docForum.select("div[id=zwconttbn] strong a[href]");
		String publisherId = postCountElement.attr("data-popper");
		if (publisherId.isEmpty())
			System.out.println(threadUrl);
		else
			accountIdSet.add(publisherId);
		// 回复分页
		Elements replyTotalTab = docForum.select("div[id=zwcontab] ul li[class=on] a[href]");
		if (replyTotalTab.select("span span[class=ftreplycount]").size() > 0)
			return;
		String totalReplyStr = replyTotalTab.text().trim();
		if (totalReplyStr == null || totalReplyStr.isEmpty())
			return;
		//logger.debug(threadUrl);
		int beginIndex = totalReplyStr.indexOf("（") + 1;
		int endIndex = totalReplyStr.indexOf("）");
		int totalReply = Integer.parseInt(totalReplyStr.substring(beginIndex, endIndex));
		int replyPageCount = (totalReply % 30 == 0) ? (totalReply / 30) : (totalReply / 30 + 1);
		//if (replyPageCount > 1)
		//	replyPageCount = 1;													//TODO for test
		for (int i = 1; i <= replyPageCount; ++i) {
			int pointIndex = threadUrl.lastIndexOf(".html");
			String newUrl = threadUrl.substring(0, pointIndex) + '_' + i + ".html";
			allPostLinkList.add(newUrl);
		}
	}

	public void getReplyAccounts(String postPageUrl, Map<String, String> loginCookies) throws Exception {
		Document docForum = getDocOfUrl(postPageUrl, loginCookies);
		Elements replyAccountElement = docForum.select("span[class=zwnick] a[href]");
		ListIterator<Element> it = replyAccountElement.listIterator();
		while (it.hasNext()) {
			Element titleNode = it.next();
			String userId = titleNode.attr("data-popper");
			accountIdSet.add(userId);
		}
	}

	/* 解析用户界面，获取account基本信息 */
	public GubaAccountInfo getAccountInfo(String uid, Map<String, String> loginCookies) throws Exception {
		
		String accountPageUrl = String.format(accountPageUrlFormat, uid);
		Document docForum = getDocOfUrl(accountPageUrl, loginCookies);
		// 头像
		String imgUrl = docForum.select("div[class=photo] img[src]").attr("src").trim();
		String name = docForum.select("div[class=taname]").text().trim();
		// 统计信息
		Elements tanums = docForum.select("div[class=tanums] table tbody tr td a em");
		String stockCount = tanums.get(0).text().trim();
		String followingCount = tanums.get(1).text().trim();
		String fansCount = tanums.get(2).text().trim();
		//
		String stars = docForum.select("span[class=stars]").attr("data-influence").trim();
		String logonDate = docForum.select("div[id=influence] span[style=color:#999;]").text().trim();
		if (!logonDate.isEmpty())
			logonDate = logonDate.substring(1, logonDate.length() - 1);
		
		Elements sumfw = docForum.select("div[class=sumfw] span");
		String totalVisit = "";
		String todayVisit = "";
		if (sumfw.size() > 0) {
			totalVisit = sumfw.get(0).text().trim();
			totalVisit = totalVisit.substring(0, totalVisit.length()-1);
			todayVisit = sumfw.get(1).text().trim();
			todayVisit = todayVisit.substring(0, todayVisit.length()-1);
		}
		
		GubaAccountInfo account = new GubaAccountInfo(uid, name, imgUrl, stockCount, followingCount, fansCount);
		account.setTodayVisit(todayVisit);
		account.setTotalVisit(totalVisit);
		if (stars != null)
			account.setInfluence(stars);
		else
			account.setInfluence("");
		if (logonDate != null)
			account.setLogonDate(logonDate);
		else
			account.setLogonDate("");
		

		logger.debug(JSON.toJSONString(account));
		return account;
	}

	private Document getDocOfUrl(String pageUrl, Map<String, String> loginCookies) throws Exception {
		Document docForum = null;
		int retryNum = 0;
		while (true) {
			try {
				docForum = Jsoup.connect(pageUrl).cookies(loginCookies).get();
				break;
			} catch (Exception e) {
				if (retryNum < RETRY_THRESHOLD) {
					retryNum++;
					logger.error("Cannot get the page url list for the forum url {}. Continue with retry {} times.",
							pageUrl, retryNum, e);
					Thread.sleep(30000);
					initCookies();
					continue;
				}
				logger.error("Cannot get the page url list for the forum url {} with retry {} times.", pageUrl,
						retryNum, e);
				throw e;
			}
		}
		return docForum;
	}

	public static void main(String args[]) {
		GubaAccountInfoCrawler c = new GubaAccountInfoCrawler();
		ArrayList<String> nameUrlList = new ArrayList<String>();
		try {
			c.getGubaAccountInfo(null, null);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
