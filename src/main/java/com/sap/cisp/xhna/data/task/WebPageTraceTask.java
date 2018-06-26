package com.sap.cisp.xhna.data.task;

import com.sap.cisp.xhna.data.TaskType;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class WebPageTraceTask extends AbstractTask {

    private static final long serialVersionUID = 8316489303132175709L;

    public WebPageTraceTask(TaskType type, String mediaName, TaskParam param) {
        super(type, mediaName, param);
    }

}
