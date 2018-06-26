package com.sap.cisp.xhna.data.finder;

public class DefaultTaskFinder extends AbstractTaskFinder {

    @Override
    public void find() {
        addTasks(TaskFinderHelper.findAllTasks());
    }

}