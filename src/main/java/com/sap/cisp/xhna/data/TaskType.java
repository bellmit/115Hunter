package com.sap.cisp.xhna.data;

public enum TaskType {

    SocialMedia_ArticleData_ByKeyword("SocialArticleKeyword"),
    SocialMedia_ArticleData_ByAccount("SocialArticleAccount"), 
    SocialMedia_AccountData("SocialAccount"), 
    //帖子附属信息任务
    SocialMedia_Account_Article("SocialAccountArticle"),
    TraditionalMedia_ArticleData_ByWebPage("TraditionalArticleWebPage"), 
    TraditionalMedia_ArticleData_ByRSS("TraditionalArticleRSS"), 
    Test("Test"), TraditionalMedia_ArticleData_ByRSS_Trace("TraditionalArticleRSSTrace"),
    TraditionalMedia_ArticleData_ByWebPage_Trace("TraditionalArticleWebPageTrace"),
    Forum_ArticleData("ForumArticle"),
    SocialMedia_Datasift_ByKeyword("DatasiftStreamKeyword"),
    SocialMedia_Datasift_ByAccount("DatasiftStreamAccount");

    private String name;

    public String getName() {
        return name;
    }

    private TaskType(String name) {
        this.name = name;
    }

}
