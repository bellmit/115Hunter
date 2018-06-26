package com.sap.cisp.xhna.data.task.worker;

import org.gearman.Gearman;
import org.gearman.GearmanClient;
import org.gearman.GearmanJobEvent;
import org.gearman.GearmanJobPriority;
import org.gearman.GearmanJobReturn;
import org.gearman.GearmanServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.config.ConfigInstance;

/**
 * Remote distributed job sumbit helper
 * 
 * @author hluan
 *
 */
public class JobSubmitClientHelperOfficialVersion {
    public static final String GEARMAN_HOST = ConfigInstance
            .getValue("Gearman_Host");; // gearman server
    public static final int PORT = 4730;
    public static final Gearman gearman = Gearman.createGearman();
    public static final GearmanClient clientGet = gearman.createGearmanClient();
    public static final GearmanClient clientRelease = gearman
            .createGearmanClient();
    public static final GearmanClient clientOther = gearman
            .createGearmanClient();
    public static GearmanClient client = null;
    GearmanServer server = null; // creat the first server
    public static Logger logger = LoggerFactory
            .getLogger(JobSubmitClientHelperOfficialVersion.class);
    private String host = GEARMAN_HOST;
    private int port = PORT;

    public static JobSubmitClientHelperOfficialVersion getInstance() {
        return JobClientSubmitHelperHolder.helper;
    }

    private static class JobClientSubmitHelperHolder {
        public static JobSubmitClientHelperOfficialVersion helper = new JobSubmitClientHelperOfficialVersion();
    }

    private JobSubmitClientHelperOfficialVersion() {
        client = clientOther;
    }

    public JobSubmitClientHelperOfficialVersion init(String host, int port) {
        this.host = host;
        this.port = port;
        server = createGearmanServer(host, port);
        clientGet.addServer(server);
        clientRelease.addServer(server);
        clientOther.addServer(server);
        return this;
    }

    public JobSubmitClientHelperOfficialVersion init(ClientTypeEnum clientType) {
        switch (clientType) {
        case GET:
            client = clientGet;
            break;
        case RELEASE:
            client = clientRelease;
            break;
        default:
            client = clientOther;
        }
        return this;
    }

    public JobSubmitClientHelperOfficialVersion init() {
        try {
            port = Integer.parseInt(ConfigInstance.getValue("Gearman_Port"));
        } catch (NumberFormatException e) {
            logger.error("Failed  to parse gearman port. Use default {}", PORT,
                    e);
        }
        server = createGearmanServer(host, port);
        clientGet.addServer(server);
        clientRelease.addServer(server);
        clientOther.addServer(server);
        return this;
    }

    public GearmanServer createGearmanServer(String host, int port) {
        return gearman.createGearmanServer(host, port);
    }

    public byte[] submitJob(String function, byte[] data,
            GearmanJobPriority priority) throws InterruptedException {
        byte[] result = new byte[0];
        if (client.getServerCount() == 0) {
            init();
        }
        System.out.println(">> server count for client  : "
                + client.getServerCount());
        GearmanJobReturn jobReturn = null;
        jobReturn = client.submitJob(function, data, priority);

        while (!jobReturn.isEOF()) {
            GearmanJobEvent event = jobReturn.poll();
            switch (event.getEventType()) {
            case GEARMAN_JOB_SUCCESS:
                result = event.getData();
                break;
            case GEARMAN_SUBMIT_FAIL:
                System.out.println("### Job Submit failed");
            case GEARMAN_JOB_FAIL:
                System.err.println(event.getEventType() + ": "
                        + new String(event.getData()));
            default:
            }
        }
        return result;
    }

    public byte[] submitJob(String function, byte[] data)
            throws InterruptedException {
        if (client.getServerCount() == 0) {
            init();
        }

        return submitJob(function, data, GearmanJobPriority.NORMAL_PRIORITY);
    }

    public void submitBackgroundJob(String function, byte[] data)
            throws InterruptedException {
        if (client.getServerCount() == 0) {
            init();
        }
        client.submitBackgroundJob(function, data);
    }

    public void shutdown() {
        gearman.shutdown();
    }

    public static enum ClientTypeEnum {
        GET(0), RELEASE(1), OTHER(2);

        private final int index;

        private ClientTypeEnum(int index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return Integer.toString(index);
        }

        public int getInt() {
            return index;
        }
    }
}
