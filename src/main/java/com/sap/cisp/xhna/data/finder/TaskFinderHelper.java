package com.sap.cisp.xhna.data.finder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.db.HANAService;
import com.sap.cisp.xhna.data.task.ITask;

public class TaskFinderHelper {
    private static HANAService hanaService = new HANAService();
    private static Logger logger = LoggerFactory
            .getLogger(TaskFinderHelper.class);

    public static List<ITask> findAllTasks() {
        // TODO Auto-generated method stub
        List<ITask> tasksList = new ArrayList<ITask>();
        tasksList.addAll(TaskFinderHelper.findSocialTasks());
        tasksList.addAll(TaskFinderHelper.findSocialAccountArticleTasks());
        tasksList.addAll(TaskFinderHelper.findTraditionalTasks());
        return tasksList;
    }

    private static List<ITask> findSocialAccountArticleTasks() {
    	logger.info("start to find SocialAccountArticleTasks");
        List<ITask> tasksList = new ArrayList<ITask>();
        List<Map<String, String>> mapList = hanaService.listSocialAccountArticleTasksMaps();
        tasksList.addAll(TaskFactory.getInstance().createTasksFromMapsList(
                mapList));
        logger.info("find {} SocialAccountArticleTasks", tasksList.size());
        return tasksList;
	}

	private static List<ITask> findSocialTasks() {
        logger.info("start to find SocialTasks");
        List<ITask> tasksList = new ArrayList<ITask>();
        List<Map<String, String>> mapList = hanaService.listSocialTasksMaps();
        tasksList.addAll(TaskFactory.getInstance().createTasksFromMapsList(
                mapList));
        logger.info("find {} SocialTasks", tasksList.size());
        return tasksList;
    }
	
	private static List<ITask> findTraditionalTasks() {
        logger.info("start to find TraditionalTasks");
        List<ITask> tasksList = new ArrayList<ITask>();
        List<Map<String, String>> mapList = hanaService.listTraditionalTasksMaps();
        tasksList.addAll(TaskFactory.getInstance().createTasksFromMapsList(
                mapList));
        logger.info("find {} TraditionalTasks", tasksList.size());
        return tasksList;
    }
}
