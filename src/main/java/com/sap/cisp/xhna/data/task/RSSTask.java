package com.sap.cisp.xhna.data.task;

import com.sap.cisp.xhna.data.TaskType;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class RSSTask extends AbstractTask {

    private static final long serialVersionUID = 8334752622661569177L;

    public RSSTask(TaskType type, String mediaName, TaskParam param) {
        super(type, mediaName, param);
    }

}
