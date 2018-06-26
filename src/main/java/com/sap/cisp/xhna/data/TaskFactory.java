package com.sap.cisp.xhna.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sap.cisp.xhna.data.model.databasemapping.MediaKey;
import com.sap.cisp.xhna.data.task.AccountArticleTask;
import com.sap.cisp.xhna.data.task.AccountInfoTask;
import com.sap.cisp.xhna.data.task.ArticleInfoTask;
import com.sap.cisp.xhna.data.task.ForumArticleTask;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.KeywordArticleTask;
import com.sap.cisp.xhna.data.task.RSSTask;
import com.sap.cisp.xhna.data.task.TestTask;
import com.sap.cisp.xhna.data.task.WebPageTask;
import com.sap.cisp.xhna.data.task.WebPageTraceTask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class TaskFactory {

    private static TaskFactory instance = null;

    private TaskFactory() {
    }

    public static synchronized TaskFactory getInstance() {
        if (instance == null) {
            instance = new TaskFactory();
        }

        return instance;
    }

    public ITask createTask(TaskType type, String media, TaskParam param) {
        switch (type) {
        case SocialMedia_ArticleData_ByKeyword:
        case SocialMedia_Datasift_ByKeyword:
            return new KeywordArticleTask(type, media, param);
        case SocialMedia_ArticleData_ByAccount:
        case SocialMedia_Datasift_ByAccount:
            return new AccountArticleTask(type, media, param);
        case SocialMedia_AccountData:
            return new AccountInfoTask(type, media, param);
        case SocialMedia_Account_Article:
        	return new ArticleInfoTask(type, media, param);
        case TraditionalMedia_ArticleData_ByWebPage:
            return new WebPageTask(type, media, param);
        case TraditionalMedia_ArticleData_ByWebPage_Trace:
            return new WebPageTraceTask(type, media, param);
        case TraditionalMedia_ArticleData_ByRSS:
            return new RSSTask(type, media, param);
        case TraditionalMedia_ArticleData_ByRSS_Trace:
            return new RSSTask(type, media, param);
        case Forum_ArticleData:
        	return new ForumArticleTask(type, media, param);
        case Test:
            return new TestTask(type, media, param);
        default:
            return null;
        }

    }

    public List<? extends ITask> createTasksFromMapsList(
            List<Map<String, String>> mapList) {
        // TODO Auto-generated method stub
        List<ITask> tasksList = new ArrayList<ITask>();
        for (int i = 0; i < mapList.size(); i++) {
            Map<String, String> map = mapList.get(i);
            tasksList.add(createTaskFromMap(map));
        }
        return tasksList;
    }

    public ITask createTaskFromMap(Map<String, String> map) {
        // TODO Auto-generated method stub
        String type = map.get("type");
        TaskType taskType = null;
        switch (type) {
        case "SocialArticleKeyword":
            taskType = TaskType.SocialMedia_ArticleData_ByKeyword;
            break;
        case "SocialArticleAccount":
            taskType = TaskType.SocialMedia_ArticleData_ByAccount;
            break;
        case "SocialAccount":
            taskType = TaskType.SocialMedia_AccountData;
            break;
        case "SocialAccountArticle":
        	taskType=TaskType.SocialMedia_Account_Article;
        	break;
        case "TraditionalArticleWebPage":
            taskType = TaskType.TraditionalMedia_ArticleData_ByWebPage;
            break;
        case "TraditionalArticleWebPageTrace":
            taskType = TaskType.TraditionalMedia_ArticleData_ByWebPage_Trace;
            break;
        case "TraditionalArticleRSS":
            taskType = TaskType.TraditionalMedia_ArticleData_ByRSS;
            break;
        case "TraditionalArticleRSSTrace":
        	taskType = TaskType.TraditionalMedia_ArticleData_ByRSS_Trace;
        	break;
        case "ForumArticle":
        	taskType = TaskType.Forum_ArticleData;
        	break;
        default:
            taskType = TaskType.Test;
        }
        String media = map.get("media");
        MediaKey mediaKey = ApplicationContext.getMediaKey(media, type);
        TaskParam param = new TaskParam(map, mediaKey);
        return createTask(taskType, media, param);
    }

}
