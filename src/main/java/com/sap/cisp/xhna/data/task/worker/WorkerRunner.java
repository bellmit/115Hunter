package com.sap.cisp.xhna.data.task.worker;

import net.johnewart.gearman.client.NetworkGearmanWorker;
import net.johnewart.gearman.common.interfaces.GearmanFunction;
import net.johnewart.gearman.common.interfaces.GearmanWorker;
import net.johnewart.gearman.net.Connection;

import java.util.Map;
import java.util.Set;

public class WorkerRunner implements Runnable {
    private GearmanWorker worker;
    protected Map<String, GearmanFunction> functions;
    protected Set<Connection> connections;

    public WorkerRunner() {
        
    }
    public WorkerRunner(Set<Connection> connections,
            Map<String, GearmanFunction> functions) {
        this.functions = functions;
        this.connections = connections;
    }

    @Override
    public void run() {
        NetworkGearmanWorker.Builder builder = new NetworkGearmanWorker.Builder();

        for (Connection c : connections) {
            builder.withConnection(c);
        }

        worker = builder.build();

        for (Map.Entry<String, GearmanFunction> entry : functions.entrySet()) {
            worker.registerCallback(entry.getKey(), entry.getValue());
        }
        worker.doWork();
    }

    public void stop() {
        worker.stopWork();
    }
}