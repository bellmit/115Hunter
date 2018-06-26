package com.sap.cisp.xhna.data.executor.forum;

import java.util.ArrayList;
import java.util.Map;

import com.sap.cisp.xhna.data.executor.ITaskExecutor;

public interface IForumCrawler {
    public ArrayList<String> getForumList(String rootUrl, Map<String, String> loginCookies) throws Exception;
    public void getPageUrlList(String forumUrl, Map<String, String> loginCookies, ArrayList<String> pageUrlList) throws Exception;
    public void getThreadUrlList(String pageUrl, Map<String, String> loginCookies, ArrayList<String> threadLinkList) throws Exception;
    public void getPostUrlList(String threadUrl, Map<String, String> loginCookies, ArrayList<String> threadLinkList, ITaskExecutor parentExecutor) throws Exception;

}
