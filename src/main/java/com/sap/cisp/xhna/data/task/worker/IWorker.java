package com.sap.cisp.xhna.data.task.worker;

import net.johnewart.gearman.common.interfaces.GearmanFunction;

public interface IWorker extends GearmanFunction, Runnable {

    /*
     * Register the function with a corresponding actual worker Dispatch the
     * fuction work with data
     */
    public byte[] dispatch(String function, byte[] data) throws Exception;

}
