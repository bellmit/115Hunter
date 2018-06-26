package com.sap.cisp.xhna.data.task;

import com.sap.cisp.xhna.data.TaskType;
import com.sap.cisp.xhna.data.db.HANAService;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public abstract class AbstractTask implements ITask {
    private static final long serialVersionUID = 5464800840447139445L;
    private TaskType type;
    private TaskParam param;
    private String mediaName;
    protected String taskId;
    protected HANAService hanaService = new HANAService();

    public TaskType getTaskType() {
        return type;
    }

    public TaskParam getParam() {
        return param;
    }

    public String getTaskId() {
        return taskId;
    }

    public AbstractTask(TaskType type, String mediaName, TaskParam param) {
        super();
        this.type = type;
        this.param = param;
        this.mediaName = mediaName;
        if (param != null)
            this.taskId = param.getString("task_id");
        changeTaskStatusToQueue();
    }

    private void changeTaskStatusToQueue() {
    	if (taskId == null)
            return;
        hanaService.updateTaskStatus(taskId, "QUEUE");
	}
    
    public void resetTaskStatus(){
    	if (taskId == null)
            return;
    	String state=param.getString("state");
        hanaService.updateTaskStatus(taskId, state);
    }

	public String getMediaName() {
        return mediaName;
    }

    public void startTask() {
        changeTaskStatusToRunning();
    }

    public void changeTaskStatusToRunning() {
        if (taskId == null)
            return;
        hanaService.updateTaskStatus(taskId, "RUNNING");
    }

    public void changeTaskStatusToStopped() {
        if (taskId == null)
            return;
        hanaService.updateTaskStatus(taskId, "STOPPED");
    }

    public void completeTask() {
        if (taskId == null)
            return;
        String endTime = param.getEndTime();
        // String dateFormat="yyyy-MM-dd HH:mm:ss";
        hanaService.updateTaskDataPart(taskId, endTime);
    }

    @Override
    public String toString() {
        return "AbstractTask [type=" + type + ", param=" + param
                + ", mediaName=" + mediaName + "]";
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + this.taskId.hashCode();
        result = 37 * result + this.type.hashCode();
        result = 37 * result + this.mediaName.hashCode();
        result = 37 * result + this.param.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // a quick test to see if the objects are identical
        if (this == obj)
            return true;

        // must return false if the explicit parameter is null
        if (obj == null)
            return false;

        if (getClass() != obj.getClass()) {
            return false;
        }

        AbstractTask p = (AbstractTask) obj;

        if (this.taskId.equalsIgnoreCase(p.taskId) && (this.type == p.type)
                && (this.mediaName.equalsIgnoreCase(p.mediaName))
                && this.param.equals(p.param)) {
            return true;
        } else {
            return false;
        }
    }

}
