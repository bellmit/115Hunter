package com.sap.cisp.xhna.data.task;

import com.sap.cisp.xhna.data.TaskType;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class WebPageTask extends AbstractTask {

    private static final long serialVersionUID = 8316489303132175709L;

    public WebPageTask(TaskType type, String mediaName, TaskParam param) {
        super(type, mediaName, param);
    }

}
