package com.sap.cisp.xhna.data.executor.receiver;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.task.worker.JobSubmitClientHelper;

/**
 * This Remote Receiver aims to submit jobs to Gearman server and fulfill the
 * job via crawl worker.
 * 
 *
 */
public class RemoteReceiver implements Receiver {
    private static Logger logger = LoggerFactory
            .getLogger(RemoteReceiver.class);
    private byte[] data = null;

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    private String function;

    public RemoteReceiver() {

    }

    public RemoteReceiver(String function, byte[] data) {
        this.data = data;
        this.function = function;
    }

    @Override
    public Object action() throws Throwable {
        // TODO Auto-generated method stub
        byte[] result = null;
        result = JobSubmitClientHelper.getInstance().submitJob(function, data);
        logger.debug("Submit task remotely result : {}", SerializationUtils.deserialize(result));
        return SerializationUtils.deserialize(result);
    }
}
