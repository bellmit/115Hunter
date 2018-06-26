package com.sap.cisp.xhna.data;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import net.johnewart.gearman.engine.queue.factories.MemoryJobQueueFactory;
import net.johnewart.gearman.server.config.GearmanServerConfiguration;
import net.johnewart.gearman.server.config.ServerConfiguration;
import net.johnewart.gearman.server.net.ServerListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.task.worker.main.IWorkerAgent;

public class TaskManager {

    private static ServerRunner runner = null;
    private static Logger logger = LoggerFactory.getLogger(TaskManager.class);
    static ExecutorService exe = Executors.newCachedThreadPool();

    /**
     * Create a Gearman instance. This method should only be called on server
     * node, not worker-only node. To avoid packet lost on worker-only side.
     * 
     * @param executor
     * @param worker
     * @throws IOException
     */
    public static void createGearman(ScheduledThreadPoolExecutor executor,
            IWorkerAgent worker) throws IOException {
        // Start Gearman server
        int port = Integer.parseInt(ConfigInstance.getValue("Gearman_Port"));
        String hostname = ConfigInstance.getValue("Gearman_Host");
        GearmanServerConfiguration primaryConfig = new GearmanServerConfiguration();
        primaryConfig.setHostName(hostname);
        primaryConfig.setPort(port);
        primaryConfig.setJobQueueFactory(new MemoryJobQueueFactory(
                primaryConfig.getMetricRegistry()));
        logger.info(
                "--> Initiate Gearman server with config: Host {}, Port {}",
                primaryConfig.getHostName(), primaryConfig.getPort());
        runner = new ServerRunner(primaryConfig);
        executor.execute(runner);
        logger.info("--> Start Worker thread {}", worker.getClass());
        executor.execute(worker);
    }

    public static void shutdown() {
        if (runner != null)
            runner.stop();
    }

    /**
     * Specifies the work mode.
     */
    public static enum WorkMode {
        /**
         * Only work as worker, no Gearman server running. Can not find task
         * from DB directly
         */
        WORKER_ONLY,

        /**
         * Find task from DB and route all tasks to Gearman server. Work as
         * either server or worker
         */
        WORKER_AND_SERVER,

        /**
         * Only find task from DB and route to Gearman server, do not have a
         * worker to process task
         */
        SERVER_ONLY,

        /**
         * Can find task from DB and execute locally Can process job submitted
         * via Gearman client.
         */
        SINGLETON
    }

    public static WorkMode getWorkModeByProperty(int mode) {
        switch (mode) {
        case 0:
            return WorkMode.WORKER_ONLY;
        case 1:
            return WorkMode.WORKER_AND_SERVER;
        case 2:
            return WorkMode.SERVER_ONLY;
        case 3:
            return WorkMode.SINGLETON;
        default:
            break;
        }
        return WorkMode.SINGLETON;
    }

}

class ServerRunner implements Runnable {

    private final ServerListener server;

    public ServerRunner(ServerConfiguration serverConfig) {
        server = new ServerListener(serverConfig);
    }

    @Override
    public void run() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

}
