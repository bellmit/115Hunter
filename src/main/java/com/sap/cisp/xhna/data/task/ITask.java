package com.sap.cisp.xhna.data.task;

import java.io.Serializable;

import com.sap.cisp.xhna.data.TaskType;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public interface ITask extends Serializable {

    public TaskType getTaskType();

    public String getMediaName();

    public TaskParam getParam();

    public String getTaskId();

    public void completeTask();

    public void startTask();
    
    public void resetTaskStatus();

    public void changeTaskStatusToRunning();

    public void changeTaskStatusToStopped();
}
