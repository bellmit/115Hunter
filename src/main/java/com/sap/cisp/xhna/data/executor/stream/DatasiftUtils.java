package com.sap.cisp.xhna.data.executor.stream;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.SerializationUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskManager;
import com.sap.cisp.xhna.data.common.Processes;
import com.sap.cisp.xhna.data.config.ConfigInstance;
import com.sap.cisp.xhna.data.task.worker.JobSubmitClientHelperOfficialVersion;
import com.sap.cisp.xhna.data.task.worker.TaskManagementUtils;
import com.sap.cisp.xhna.data.task.worker.main.TaskWorkerMain;
import com.datasift.client.DataSiftClient;
import com.datasift.client.DataSiftConfig;
import com.datasift.client.core.Stream;
import com.datasift.client.core.Validation;
import com.datasift.client.stream.DataSiftMessage;
import com.datasift.client.stream.DeletedInteraction;
import com.datasift.client.stream.ErrorListener;
import com.datasift.client.stream.Interaction;
import com.datasift.client.stream.StreamEventListener;
import com.datasift.client.stream.StreamSubscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.BooleanNode;

public class DatasiftUtils {
    private static Logger logger = LoggerFactory.getLogger(DatasiftUtils.class);
    // The map to save the mapping between csdl string and datasift subscription
    // object, used to unsubscribe
    private static final ConcurrentHashMap<String, Subscription> subscriptionCache = new ConcurrentHashMap<String, Subscription>();
    private static DataSiftClient activeClient = null;
    private static DataSiftConfig config = null;
    private static final Stack<DataSiftClient> clientStack = new Stack<DataSiftClient>();
    private static final Map<DataSiftClient, Semaphore> clientStreamPermitMap = new ConcurrentHashMap<DataSiftClient, Semaphore>();
    // The map to save the mapping between csdl string and datasift client, used
    // to release client/stream semaphore
    private static final Map<String, DataSiftClient> csdlClientPermitMap = new ConcurrentHashMap<String, DataSiftClient>();
    private static String workerIdentifier = "";
    private static final Map<String, String> csdlClientWorkerIdentifierMap = new ConcurrentHashMap<String, String>();
    // worker list on server side
    private static final List<String> workerList = new ArrayList<String>();

    /*
     * ^ #start of the line ( # start of group #1 [01]?\\d\\d? # Can be one or
     * two digits. If three digits appear, it must start either 0 or 1 # e.g
     * ([0-9], [0-9][0-9],[0-1][0-9][0-9]) | # ...or 2[0-4]\\d # start with 2,
     * follow by 0-4 and end with any digit (2[0-4][0-9]) | # ...or 25[0-5] #
     * start with 2, follow by 5 and ends with 0-5 (25[0-5]) ) # end of group #2
     * \. # follow by a dot "." .... # repeat with 3 times (3x) $ #end of the
     * line
     */
    private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    public static final Pattern patternIPAdress = Pattern.compile(
            IPADDRESS_PATTERN, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    static {
        if(ConfigInstance.DATASIFT_USERNAME != null && ConfigInstance.DATASIFT_APIKEY != null) {
            config = new DataSiftConfig(ConfigInstance.DATASIFT_USERNAME,
                    ConfigInstance.DATASIFT_APIKEY);
            if (ConfigInstance.USE_PROXY) {
                config.proxy(ConfigInstance.PROXY_ADDR, ConfigInstance.PROXY_PORT);
            }
        } else {
            logger.error("Cannot create the Datasift config. Please check the property file to ensure datasift user name and api key have been configured properly.");
        }
        
        workerIdentifier = getIP() + "@" + Processes.getPID();
        logger.info("Worker Identifier : {}", workerIdentifier);
    }

    private DatasiftUtils() {
    };

    public static DatasiftUtils getInstance() {
        return DatasiftUtilHolder.instance;
    }

    private static class DatasiftUtilHolder {
        public static DatasiftUtils instance = new DatasiftUtils();
    }

    private static DataSiftClient generateDataSiftConnection() {
        if(config == null) {
            logger.error("Cannot generate datasift client due to config is null.");
            return null;
        }
        DataSiftClient client = new DataSiftClient(config);
        client.liveStream().onError(new ErrorHandler());
        client.liveStream().onStreamEvent(new DeleteHandler());
        return client;
    }

    public static String getWorkerIdentifier() {
        return workerIdentifier;
    }

    public static String getWorkerIdentifierByCSDL(String csdl) {
        return csdlClientWorkerIdentifierMap.get(csdl);
    }

    /**
     * register worker and get client semaphore. Invoked on server side.
     * 
     * @param workerIdentifier
     * @return true/false
     */
    public static boolean registerWorkerIdentifierOnServer(
            String workerIdentifier) {
        synchronized (workerList) {
            if (ConfigInstance.DATASIFT_CONNECTION_SEMAPHORE.tryAcquire()) {
                logger.info(
                        "Get permit on server. Register the worker {} successfully.",
                        workerIdentifier);
                return workerList.add(workerIdentifier);
            } else {
                logger.error("Cannot get permit on server for worker {}.",
                        workerIdentifier);
                return false;
            }
        }
    }

    /**
     * deregister worker and release client semaphore. Invoked on server side.
     * 
     * @param workerIdentifier
     * @return
     */
    public static boolean deregisterWorkerIdentifierOnServer(
            String workerIdentifier) {
        synchronized (workerList) {
            if (workerList.remove(workerIdentifier)) {
                ConfigInstance.DATASIFT_CONNECTION_SEMAPHORE.release();
                logger.info(
                        "Release permit on server. Deregister the worker {} successfully.",
                        workerIdentifier);
                return true;
            } else {
                logger.error("Cannot find the worker {} registered on server.",
                        workerIdentifier);
                return false;
            }
        }
    }

    /**
     * register csdl with worker identifier. Invoked on server side.
     * 
     * @param csdl
     * @param workerIdentifier
     * @return true/false
     */
    public static boolean registerCsdlOnServer(String csdl,
            String workerIdentifier) {
        // if csdl already be registered before, return false, no permit for
        // duplication.
        return setWorkerIdentifierForCSDL(csdl, workerIdentifier) == null;
    }

    /**
     * deregister csdl with worker identifier. Invoked on server side.
     * 
     * @param csdl
     * @return true/false
     */
    public static boolean deregisterCsdlOnServer(String csdl) {
        // if csdl never be registered before, return false.
        // if csdl registered worker identifier is inconsistent, return false.
        return (removeWorkerIdentifierForCSDL(csdl) != null);
    }

    public static String setWorkerIdentifierForCSDL(String csdl,
            String workerIdentifer) {
        return csdlClientWorkerIdentifierMap.putIfAbsent(csdl, workerIdentifer);
    }

    public static String removeWorkerIdentifierForCSDL(String csdl) {
        return csdlClientWorkerIdentifierMap.remove(csdl);
    }

    /**
     * Subscribe a datasift stream for the csdl.
     * 
     * @param csdl
     * @param queue
     * @return
     */
    public static synchronized boolean subscribeStreamWithCSDL(String csdl,
            BlockingQueue<Interaction> queue) {
        if (subscriptionCache.contains(csdl)) {
            logger.info("The stream with csdl {} has been subscribed already.",
                    csdl);
            return true;
        }
        boolean isPermit = false;
        // Local handling for non worker-only work mode, server-only will not be here since it's invoked in executor
        if (ConfigInstance.getCurrentWorkMode() != TaskManager.WorkMode.WORKER_ONLY) {
            isPermit = getSubscribePermitFromLocal(csdl);
        } else {
            isPermit = getSubscribePermitFromServer(csdl);
        }
        if (isPermit) {
            Stream stream = activeClient.compile(csdl).sync();
            Subscription sub = new Subscription(stream, activeClient, queue);
            logger.debug("Acquired the permit to subscribe csdl {}.", csdl);
            activeClient.liveStream().subscribe(sub);
            subscriptionCache.putIfAbsent(csdl, sub);
            logger.debug("Subscribe csdl {} successfully.", csdl);
            return true;
        } else {
            // Need to send a POISON message to exit the blocking queue in
            // executor thread
            logger.error(
                    "Cannot get permit for the new subscription with csdl {}. Stop the queue with POISON message.",
                    csdl);
            queue.add(new Interaction(BooleanNode.getFalse()));
            return false;
        }
    }

    /**
     * Subscribe dummy stream for test
     * 
     * @param csdl
     * @param queue
     */
    public static void subscirbeDummyStreamWithCSDL(String csdl,
            BlockingQueue<Interaction> queue) {
        int i = 0;
        while (true) {
            queue.add(new Interaction(BooleanNode.getTrue()));
            i++;
            if (i > 2000) {
                queue.add(new Interaction(BooleanNode.getFalse()));
                break;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Unsubcribe the stream csdl.
     * @param csdl
     * @return
     */
    public static boolean unsubscribeStreamWithCSDL(String csdl) {
        // Local handling for non worker-only work mode: singleton, worker-server
        if (ConfigInstance.getCurrentWorkMode() != TaskManager.WorkMode.WORKER_ONLY &&
                ConfigInstance.getCurrentWorkMode() != TaskManager.WorkMode.SERVER_ONLY) {
            return releaseSubscriptionFromLocal(csdl);
        } else {
            return releaseSubscriptionFromServer(csdl);
        }
    }

    public static synchronized boolean getSubscribePermitFromLocal(String csdl) {
        if (activeClient == null) {
            if (!ConfigInstance.DATASIFT_CONNECTION_SEMAPHORE.tryAcquire()) {
                return false;
            }
            DataSiftClient newClient = generateDataSiftConnection();
            if(newClient == null) {
                return false;
            }
            clientStack.add(newClient);
            activeClient = clientStack.peek();
            //Need to validate the csdl to avoid errors
            Validation validation = activeClient.validate(csdl).sync();
            if(!validation.isValid())  {
                logger.error("The csdl {} is invalid!", csdl);
                return false;
            }
            clientStreamPermitMap
                    .putIfAbsent(
                            clientStack.peek(),
                            new Semaphore(
                                    ConfigInstance.DATASIFTCLIENT_STREAM_PERMITS_NUMBER));
            if (!clientStreamPermitMap.get(activeClient).tryAcquire()) {
                return false;
            }
            csdlClientPermitMap.putIfAbsent(csdl, clientStack.peek());
            return true;
        } else {
            //Need to validate the csdl to avoid errors
            Validation validation = activeClient.validate(csdl).sync();
            if(!validation.isValid())  {
                logger.error("The csdl {} is invalid!", csdl);
                return false;
            }
            if (!clientStreamPermitMap.get(activeClient).tryAcquire()) {
                // The current active client is full of stream, maximum
                // 200/connection
                // generate new connection
                if (!ConfigInstance.DATASIFT_CONNECTION_SEMAPHORE.tryAcquire()) {
                    return false;
                }
                DataSiftClient newClient = generateDataSiftConnection();
                if(newClient == null) {
                    return false;
                }
                clientStack.add(newClient);
                activeClient = clientStack.peek();
                clientStreamPermitMap
                        .putIfAbsent(
                                clientStack.peek(),
                                new Semaphore(
                                        ConfigInstance.DATASIFTCLIENT_STREAM_PERMITS_NUMBER));
                if (!clientStreamPermitMap.get(activeClient).tryAcquire()) {
                    return false;
                }
                csdlClientPermitMap.putIfAbsent(csdl, clientStack.peek());
                return true;
            } else {
                csdlClientPermitMap.putIfAbsent(csdl, clientStack.peek());
                return true;
            }
        }
    }

    public static synchronized boolean releaseSubscriptionFromLocal(String csdl) {
        logger.info(
                "--> Release the client stream semaphore permit with csdl locally: {}",
                csdl);
        if (subscriptionCache.containsKey(csdl)) {
            DataSiftClient client = subscriptionCache.get(csdl)
                    .getActiveClient();
            int retryNum = 0;
            while(true) {
                try {
                    client.liveStream().unsubscribe(
                            subscriptionCache.get(csdl).stream());
                    break;
                } catch(Exception e) {
                    if(retryNum < 3) {
                        retryNum ++;
                        logger.error("Exception occured during unsubscribe datasift stream. Retry ({}) time.", retryNum, e);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            logger.error("Interrupted.", e);
                        }
                        continue;
                    } else {
                        logger.error("Cannot unsubscribe datasift stream with retry ({}) times. Shutdown the client.", retryNum);
                        client.shutdown();
                        break;
                    }
                }
            }
            // put a POISON object in queue to notify the consumer to stop
            subscriptionCache.get(csdl).unsubscribe();
            if (csdlClientPermitMap.get(csdl) != null
                    && clientStreamPermitMap.get(csdlClientPermitMap.get(csdl)) != null) {
                clientStreamPermitMap.get(csdlClientPermitMap.get(csdl))
                        .release();
            } else {
                logger.error("Cannot release the semaphore permit. Datasift client or client semaphore is null.");
            }
            subscriptionCache.remove(csdl);
            logger.debug("Remove the subscription from cache {}.",
                    subscriptionCache.containsKey(csdl) ? "Failed"
                            : "Successfully");
            return true;
        } else {
            logger.warn("The subscription with csdl {} does not exist.", csdl);
            return false;
        }
    }

    /**
     * release subscription on worker from server. Step 1: Find the worker
     * identifier(IP@PID) by csdl. Step 2: send request to IP@PID function to
     * unsubscribe csdl on worker side. Step 3: If worker unsubscribed
     * successfully, remove the mapping between csdl and worker identifier.
     * 
     * @param csdl
     * @return true/false
     */
    public static synchronized boolean releaseSubscriptionFromServer(String csdl) {
        // Step 1: Find the worker identifier
        // Step 2: send request to IP@PID function to unsubscribe csdl on worker
        // side
        String workerToRequest = csdlClientWorkerIdentifierMap.get(csdl);
        if (workerToRequest == null) {
            logger.error(
                    "Cannot procced to unsubscribe. There is no worker registered for the csdl {}.",
                    csdl);
            return false;
        }
        HashMap<String, String> workerFunctionMap = new HashMap<String, String>();
        // function name
        workerFunctionMap
                .put("0",
                        TaskWorkerMain.WorkerSpecificFunctionEnum.UNSUBSCRIBE_STREAM_CSDL
                                .toString());
        // function parameter
        workerFunctionMap.put("1", csdl);
        byte[] data = SerializationUtils.serialize(workerFunctionMap);
        logger.debug(
                "<-- Send unsubscription request  to worker {} for csdl: {}, data size {} bytes.",
                workerToRequest, csdl, data.length);
        byte[] jobReturn = null;
        try {
            jobReturn = JobSubmitClientHelperOfficialVersion
                    .getInstance()
                    .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER)
                    .submitJob(workerToRequest, data);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("<-- Send unsubscription request remotely error.", e);
        }
        boolean result = false;
        if (jobReturn != null && jobReturn.length != 0) {
            result = (boolean) SerializationUtils.deserialize(jobReturn);
        }
        // Step 3: If worker unsubscribed successfully, remove the mapping
        // between csdl and worker identifier
        if (result) {
            logger.debug("Remote unsubscription successfully. Proceed to deregister csdl {} on server.", csdl);
            return deregisterCsdlOnServer(csdl);
        } else {
            return false;
        }
    }

    /**
     * Get subscription permit from server. Invoked on worker side. Step 1:
     * register the datasift client with worker identifier if there is no
     * client. Step 2: acquire stream semaphore from client. Step 3: register
     * csdl with worker identifier on server.
     * 
     * @param csdl
     * @return true/false
     */
    public static synchronized boolean getSubscribePermitFromServer(String csdl) {
        // get permit remotely
        if (activeClient == null) {
            if (!registerDatasiftClientWithWorkerIdentifier()) {
                return false;
            }
            DataSiftClient newClient = generateDataSiftConnection();
            if(newClient == null) {
                return false;
            }
            clientStack.add(newClient);
            activeClient = clientStack.peek();
            //Need to validate the csdl to avoid errors
            Validation validation = activeClient.validate(csdl).sync();
            if(!validation.isValid())  {
                logger.error("The csdl {} is invalid!", csdl);
                return false;
            }
            clientStreamPermitMap.putIfAbsent(activeClient, new Semaphore(
                    ConfigInstance.DATASIFTCLIENT_STREAM_PERMITS_NUMBER));
            if (!clientStreamPermitMap.get(activeClient).tryAcquire()) {
                return false;
            }
            csdlClientPermitMap.putIfAbsent(csdl, clientStack.peek());
        } else {
            //Need to validate the csdl to avoid errors
            Validation validation = activeClient.validate(csdl).sync();
            if(!validation.isValid())  {
                logger.error("The csdl {} is invalid!", csdl);
                return false;
            }
            if (!clientStreamPermitMap.get(activeClient).tryAcquire()) {
                // The current active client is full of stream, maximum
                // 200/connection
                // generate new connection
                if (!registerDatasiftClientWithWorkerIdentifier()) {
                    return false;
                }
                DataSiftClient newClient = generateDataSiftConnection();
                if(newClient == null) {
                    return false;
                }
                clientStack.add(newClient);
                activeClient = clientStack.peek();
                clientStreamPermitMap.putIfAbsent(activeClient, new Semaphore(
                        ConfigInstance.DATASIFTCLIENT_STREAM_PERMITS_NUMBER));
                if (!clientStreamPermitMap.get(activeClient).tryAcquire()) {
                    return false;
                }
            }
            csdlClientPermitMap.putIfAbsent(csdl, clientStack.peek());
        }
        // Need to cache the csdl and worker identifier on server for
        // deregisteration
        if (registerCSDLWithWorkerIdentifier(csdl)) {
            return true;
        } else {
            // if this csdl has been registered before, release the semaphore,
            // no permit.
            clientStreamPermitMap.get(activeClient).release();
            return false;
        }
    }

    /**
     * register a datasift client with a worker identifier.
     * 
     * @return true - register on server successfully false - register on server
     *         failed
     * 
     */
    public static synchronized boolean registerDatasiftClientWithWorkerIdentifier() {
        byte[] data = SerializationUtils.serialize(workerIdentifier);
        logger.debug(
                "--> Apply subscription permit for worker identifier: {}, data size {} bytes.",
                workerIdentifier, data.length);
        byte[] jobReturn = null;
        try {
            jobReturn = JobSubmitClientHelperOfficialVersion
                    .getInstance()
                    .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER)
                    .submitJob(
                            TaskManagementUtils.FunctionEnum.REGISTER_WORKER_IDENTIFIER
                                    .toString(), data);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("--> register worker identifier remotely error.", e);
        }
        boolean result = false;
        if (jobReturn != null && jobReturn.length != 0) {
            result = (boolean) SerializationUtils.deserialize(jobReturn);
        }
        return result;
    }

    /**
     * deregister a datasift client with a worker identifier to release client
     * semaphore on server.
     * 
     * @return true/false
     */
    public static synchronized boolean deregisterDatasiftClientWithWorkerIdentifier() {
        byte[] data = SerializationUtils.serialize(workerIdentifier);
        logger.debug(
                "<-- Relase subscription permit for worker identifier: {}, data size {} bytes.",
                workerIdentifier, data.length);
        byte[] jobReturn = null;
        try {
            jobReturn = JobSubmitClientHelperOfficialVersion
                    .getInstance()
                    .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER)
                    .submitJob(
                            TaskManagementUtils.FunctionEnum.DEREGISTER_WORKER_IDENTIFIER
                                    .toString(), data);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error("<-- deregister worker identifier remotely error.", e);
        }
        boolean result = false;
        if (jobReturn != null && jobReturn.length != 0) {
            result = (boolean) SerializationUtils.deserialize(jobReturn);
        }
        return result;
    }

    /**
     * register the csdl with worker identifier on server side.
     * 
     * @param csdl
     * @return true/false
     */
    public static synchronized boolean registerCSDLWithWorkerIdentifier(String csdl) {
        HashMap<String, String> csdlMap = new HashMap<String, String>();
        csdlMap.put("0", csdl);
        csdlMap.put("1", workerIdentifier);
        byte[] data = SerializationUtils.serialize(csdlMap);
        logger.debug(
                "--> Register subscription for csdl: {},  with the worker identifier: {}, data size {} bytes.",
                csdl, workerIdentifier, data.length);
        byte[] jobReturn = null;
        try {
            jobReturn = JobSubmitClientHelperOfficialVersion
                    .getInstance()
                    .init(JobSubmitClientHelperOfficialVersion.ClientTypeEnum.OTHER)
                    .submitJob(
                            TaskManagementUtils.FunctionEnum.REGISTER_CSDL_WITH_WORKER_IDENTIFIER
                                    .toString(), data);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            logger.error(
                    "--> register csdl with worker identifier remotely error.",
                    e);
        }
        boolean result = false;
        if (jobReturn != null && jobReturn.length != 0) {
            result = (boolean) SerializationUtils.deserialize(jobReturn);
        }
        return result;
    }

    public static JSONObject parseInteraction(Interaction i) {
        ObjectMapper mapper = new ObjectMapper();
        JSONObject obj = null;
        try {
            obj = new JSONObject(mapper.writeValueAsString(i.getData()));
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            logger.error("Parse interaction error.", e);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            logger.error("Parse interaction error.", e);
        }
        return obj;
    }

    public static void shutdown() {
        logger.info("Shutdown all the DataSiftClient instances. Number : {}",
                clientStack.size());
        for (DataSiftClient client : clientStack) {
            client.shutdown();
            // release client semaphore on server for each datasift client
            deregisterDatasiftClientWithWorkerIdentifier();
            // release all stream semaphores for each client
            clientStreamPermitMap.get(client).release(
                    ConfigInstance.DATASIFTCLIENT_STREAM_PERMITS_NUMBER);
        }
        clientStack.clear();
        clientStreamPermitMap.clear();
    }

    /**
     * Validate ip address with regular expression
     * 
     * @param ip
     *            ip address for validation
     * @return true valid ip address, false invalid ip address
     */
    public static boolean validate(final String ip) {
        Matcher matcher = patternIPAdress.matcher(ip);
        return matcher.matches();
    }

    public static String getIP() {
        Enumeration<NetworkInterface> netInterfaces = null;
        String ipAddress = null;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                logger.debug("DisplayName: {}", ni.getDisplayName());
                logger.debug("Name: {}", ni.getName());
                Enumeration<InetAddress> ips = ni.getInetAddresses();
                while (ips.hasMoreElements()) {
                    ipAddress = ips.nextElement().getHostAddress();
                    if (validate(ipAddress)
                            && !ipAddress.equalsIgnoreCase("127.0.0.1")) {
                        logger.debug("IP: {}", ipAddress);
                        return ipAddress;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Exception", e);
        }
        return ipAddress;
    }

    public static class Subscription extends StreamSubscription {
        DataSiftClient activeClient;
        BlockingQueue<Interaction> queue;

        public DataSiftClient getActiveClient() {
            return activeClient;
        }

        public Subscription(Stream stream) {
            super(stream);
        }

        public Subscription(Stream stream, DataSiftClient activeClient) {
            this(stream);
            this.activeClient = activeClient;
        }

        public Subscription(Stream stream, DataSiftClient activeClient,
                BlockingQueue<Interaction> queue) {
            this(stream, activeClient);
            this.queue = queue;
        }

        public void onDataSiftLogMessage(DataSiftMessage di) {
            logger.debug((di.isError() ? "Error" : di.isInfo() ? "Info"
                    : "Warning") + ":\n" + di);
        }

        public void onMessage(Interaction i) {
            logger.debug("INTERACTION:\n {}", i);
            logger.debug("INTERACTION TO JSON: {}", parseInteraction(i));
            try {
                queue.put(i);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void unsubscribe() {
            logger.debug("Unsubscribe the stream and stop the queue with POISON message.");
            BooleanNode poisonFlag = BooleanNode.getFalse();
            Interaction poison = new Interaction(poisonFlag);
            queue.add(poison);
        }
    }

    // Delete handler
    public static class DeleteHandler extends StreamEventListener {
        public void onDelete(DeletedInteraction di) {
            // You must delete the interaction to stay compliant
            logger.error("DELETED:\n " + di);
        }
    }

    // Error handler
    //handle exceptions that can't necessarily be linked to a specific stream
    public static class ErrorHandler extends ErrorListener {
        public void exceptionCaught(Throwable t) {
            t.printStackTrace();
            logger.error("Caught exception in livestream error handler.", t);
            // TODO: do something useful..!
        }
    }
}
