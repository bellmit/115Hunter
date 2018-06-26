package com.sap.cisp.xhna.data.task.worker;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.config.ConfigInstance;

import net.johnewart.gearman.client.NetworkGearmanClient;
import net.johnewart.gearman.common.JobStatus;
import net.johnewart.gearman.common.events.GearmanClientEventListener;
import net.johnewart.gearman.constants.JobPriority;
import net.johnewart.gearman.exceptions.NoServersAvailableException;
import net.johnewart.gearman.exceptions.WorkException;

/**
 * Obsolete class: It's error prone. There is potential work fail exception after long run.
 * 
 * Remote distributed job submit helper
 *
 */
public class JobSubmitClientHelper {
    public static final String GEARMAN_HOST = ConfigInstance
            .getValue("Gearman_Host"); // gearman server
    public static final int PORT = 4730;
    public static Logger logger = LoggerFactory
            .getLogger(JobSubmitClientHelper.class);
    private NetworkGearmanClient client;

    private static GearmanClientEventListener eventListener = new GearmanClientEventListener() {
        @Override
        public void handleWorkData(String jobHandle, byte[] data) {
            logger.debug("Received data update for job {}", jobHandle);
        }

        @Override
        public void handleWorkWarning(String jobHandle, byte[] warning) {
            logger.debug("Received warning for job {}", jobHandle);
        }

        @Override
        public void handleWorkStatus(String jobHandle, JobStatus jobStatus) {
            logger.debug("Received status update for job {}", jobHandle);
            logger.debug("Status: {} / {}", jobStatus.getNumerator(),
                    jobStatus.getDenominator());
        }
    };

    public static JobSubmitClientHelper getInstance() {
        return JobClientSubmitHelperHolder.helper;
    }

    private static class JobClientSubmitHelperHolder {
        public static JobSubmitClientHelper helper = new JobSubmitClientHelper();
    }

    private JobSubmitClientHelper() {
        try {
            int port = PORT;

            port = Integer.parseInt(ConfigInstance.getValue("Gearman_Port"));

            client = new NetworkGearmanClient(GEARMAN_HOST, port);
//            client.addHostToList(GEARMAN_HOST, port);
            logger.debug("Gearman client connect to Host {}, Port {}",
                    GEARMAN_HOST, port);
            client.registerEventListener(eventListener);
        } catch (NumberFormatException e) {
            logger.error("Failed  to parse gearman port. Use default {}", PORT,
                    e);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Failed  to init NetworkGearmanClient", e);
        }
    }

    public byte[] submitJob(String function, byte[] data, JobPriority priority)
            throws NoServersAvailableException, WorkException {
        byte[] result = new byte[0];
        try {
            result = client.submitJob(function, data, priority);
        } catch (NoServersAvailableException e) {
            e.printStackTrace();
            logger.error("Gearman server is unavailable.", e);
            throw e;
        } catch (WorkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("Gearman Work exception.", e);
            throw e;
        }
        return result;
    }

    public byte[] submitJob(String function, byte[] data)
            throws NoServersAvailableException, WorkException {
        return submitJob(function, data, JobPriority.NORMAL);
    }

    public String submitJobInBackground(String function, byte[] data,
            JobPriority priority) throws NoServersAvailableException {
        String result = "";
        try {
            result = client.submitJobInBackground(function, data, priority);
        } catch (NoServersAvailableException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public String submitJobInBackground(String function, byte[] data)
            throws NoServersAvailableException {
        return submitJobInBackground(function, data, JobPriority.NORMAL);
    }

    public void shutdown() {
        client.shutdown();
    }
}
