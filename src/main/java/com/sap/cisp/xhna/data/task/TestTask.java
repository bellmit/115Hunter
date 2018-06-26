package com.sap.cisp.xhna.data.task;

import com.sap.cisp.xhna.data.TaskType;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class TestTask extends AbstractTask {

    private static final long serialVersionUID = -1277132965346146386L;

    public TestTask(TaskType type, String mediaName, TaskParam param) {
        super(type, mediaName, param);
    }

    @Override
    public void completeTask() {
        // TODO Auto-generated method stub

    }

    @Override
    public void startTask() {
        // TODO Auto-generated method stub

    }

}
