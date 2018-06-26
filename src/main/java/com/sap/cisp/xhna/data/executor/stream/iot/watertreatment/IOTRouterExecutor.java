package com.sap.cisp.xhna.data.executor.stream.iot.watertreatment;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import com.sap.cisp.xhna.data.TaskExecutorPool;
import com.sap.cisp.xhna.data.TaskFactory;
import com.sap.cisp.xhna.data.TaskThreadFactory;
import com.sap.cisp.xhna.data.config.ConfigStorage;
import com.sap.cisp.xhna.data.config.DataSource;
import com.sap.cisp.xhna.data.executor.AbstractTaskExecutor;
import com.sap.cisp.xhna.data.executor.stream.BatchWriter;
import com.sap.cisp.xhna.data.executor.stream.iot.IModelInfo;
import com.sap.cisp.xhna.data.executor.stream.iot.IOTUtils;
import com.sap.cisp.xhna.data.executor.stream.iot.WebsocketClientEndpoint;
import com.sap.cisp.xhna.data.storage.ColumnInfo;
import com.sap.cisp.xhna.data.storage.Connections;
import com.sap.cisp.xhna.data.task.ITask;
import com.sap.cisp.xhna.data.task.param.TaskParam;

public class IOTRouterExecutor extends AbstractTaskExecutor {
    private static Logger log = LoggerFactory
            .getLogger(IOTRouterExecutor.class);
    private static Map<String, String> sourceUriMap = null;
    private static Map<String, String> destUriMap = null;
    private static Map<String, String> dataSourceMap = null;
    private static final Map<String, WebsocketClientEndpoint> sourceClientMap = new ConcurrentHashMap<String, WebsocketClientEndpoint>();
    private static final Map<String, WebsocketClientEndpoint> destClientMap = new ConcurrentHashMap<String, WebsocketClientEndpoint>();
    protected BlockingQueue<String> alarmQueue = new LinkedBlockingQueue<String>();
    protected BlockingQueue<String> sensorDataQueue = new LinkedBlockingQueue<String>();
    private BatchWriter alarmBatchWriter = null;
    private BatchWriter sensorBatchWriter = null;
    private BatchWriter stationBatchWriter = null;
    private BatchWriter sluiceBatchWriter = null;
    private final SimpleDateFormat sdf = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");
    protected static ScheduledThreadPoolExecutor iotSchedulerService = new ScheduledThreadPoolExecutor(
            5);
    private ExecutorService taskExecutorPool = null;
    private volatile boolean isSourceEndPointsReady = false;
    private volatile boolean isDestEndPointsReady = false;

    public void init() {
        ThreadFactory factory = new TaskThreadFactory();
        taskExecutorPool = new TaskExecutorPool(100, 100, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100),
                factory);
        Map<String, Map<String, String>> uriMap = IOTUtils
                .getServerURI("iotRouter.xml");
        // source: from PI
        sourceUriMap = uriMap.get("source");
        // destination: Web server(UI)
        destUriMap = uriMap.get("destination");

        dataSourceMap = IOTUtils.getDataSource("iotRouter.xml");
        while (!isCanceled) {
            try {
                for (Entry<String, String> entry : sourceUriMap.entrySet()) {
                    log.info(
                            "Create websocket client for source with uri {}, type: {}",
                            entry.getValue(), entry.getKey());
                    taskExecutorPool.execute(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            WebsocketClientEndpoint clientEndPoint = null;
                            try {
                                if (sourceClientMap.get(entry.getKey()) == null) {
                                    clientEndPoint = new WebsocketClientEndpoint(
                                            new URI(entry.getValue()));
                                    sourceClientMap.putIfAbsent(entry.getKey(),
                                            clientEndPoint);

                                }
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                log.error(
                                        "Cannot initialize the client end point for entry {}.",
                                        entry.getValue());
                            }
                        }
                    });
                }
                for (Entry<String, String> entry : destUriMap.entrySet()) {
                    log.info(
                            "Create websocket client for destination with uri {}, type: {}",
                            entry.getValue(), entry.getKey());
                    taskExecutorPool.execute(new Runnable() {

                        @Override
                        public void run() {
                            WebsocketClientEndpoint clientEndPoint = null;
                            try {
                                if (destClientMap.get(entry.getKey()) == null) {
                                    clientEndPoint = new WebsocketClientEndpoint(
                                            new URI(entry.getValue()));
                                    destClientMap.putIfAbsent(entry.getKey(),
                                            clientEndPoint);
                                }
                            } catch (Exception e) {
                                log.error(
                                        "Cannot initialize the client end point for entry {}.",
                                        entry.getValue());
                            }
                        }
                    });
                }
                // add listener
                // Only add message handler once
                // ???? synchronized?
                synchronized (sourceClientMap) {
                    if (sourceClientMap.size() == sourceUriMap.size()) {
                        for (Entry<String, WebsocketClientEndpoint> entry : sourceClientMap
                                .entrySet()) {
                            if (entry.getKey().equalsIgnoreCase("alarmagent")) {
                                entry.getValue()
                                        .addMessageHandler(
                                                new WebsocketClientEndpoint.MessageHandler() {
                                                    public void handleMessage(
                                                            String message) {
                                                        log.info(
                                                                "##### From alarmagent source : {}; Message: {}",
                                                                entry.getKey(),
                                                                message);
                                                        JSONObject json = null;
                                                        try {
                                                            json = new JSONObject(
                                                                    message);
                                                            // replace time
                                                            // stamp
                                                            // for alarm message
                                                            if (json.getString("type") != null
                                                                    && json.getString(
                                                                            "type")
                                                                            .equalsIgnoreCase(
                                                                                    "alarm")) {
                                                                if (json.get("timestamp") == null
                                                                        || json.get(
                                                                                "timestamp")
                                                                                .toString()
                                                                                .isEmpty()) {
                                                                    json.remove("timestamp");
                                                                    json.put(
                                                                            "timestamp",
                                                                            System.currentTimeMillis());
                                                                }
                                                            } else {
                                                                if (json.getString("type") != null
                                                                        && json.getString(
                                                                                "type")
                                                                                .equalsIgnoreCase(
                                                                                        "heartbeat")) {
                                                                    return;
                                                                }
                                                            }
                                                        } catch (Exception e) {
                                                            log.error(
                                                                    "Cannot parse the alarm JSON message from gateway.",
                                                                    e);
                                                            return;
                                                        }
                                                        log.info(
                                                                "**** Forward the message to web socket server. Type: {}; Message: {}",
                                                                json.get("type"),
                                                                json.toString());
                                                        destClientMap
                                                                .get(entry
                                                                        .getKey())
                                                                .sendMessage(
                                                                        json.toString());
                                                        // save to DB
                                                        // Keep no time stamp,
                                                        // since
                                                        // Alarm will generate
                                                        // formatted date
                                                        try {
                                                            alarmQueue
                                                                    .put(message);
                                                        } catch (InterruptedException e) {
                                                            log.error(
                                                                    "Exception caught in thread sleep",
                                                                    e);
                                                        }
                                                    }
                                                });
                            } else if (entry.getKey().equalsIgnoreCase(
                                    "sensordata")) {
                                entry.getValue()
                                        .addMessageHandler(
                                                new WebsocketClientEndpoint.MessageHandler() {
                                                    public void handleMessage(
                                                            String message) {
                                                        log.info(
                                                                "##### From sensordata source : {}; Message: {}",
                                                                entry.getKey(),
                                                                message);
                                                        JSONObject json = null;
                                                        try {
                                                            json = new JSONObject(
                                                                    message);
                                                            if (json.get("timestamp") == null
                                                                    || json.get(
                                                                            "timestamp")
                                                                            .toString()
                                                                            .isEmpty()) {
                                                                json.remove("timestamp");
                                                                json.put(
                                                                        "timestamp",
                                                                        System.currentTimeMillis());
                                                            }
                                                        } catch (Exception e) {
                                                            log.error(
                                                                    "Cannot parse the sensor data JSON message from gateway.",
                                                                    e);
                                                            return;
                                                        }
                                                        destClientMap
                                                                .get(entry
                                                                        .getKey())
                                                                .sendMessage(
                                                                        json.toString());
                                                    }
                                                });
                            } else if (entry.getKey().equalsIgnoreCase(
                                    "gateway")) {
                                entry.getValue()
                                        .addMessageHandler(
                                                new WebsocketClientEndpoint.MessageHandler() {
                                                    public void handleMessage(
                                                            String message) {
                                                        log.info(
                                                                "##### From gateway source : {}; Message: {}",
                                                                entry.getKey(),
                                                                message);
                                                        // save to DB
                                                        JSONObject json = new JSONObject(
                                                                message);
                                                        JSONArray dataStreams = json
                                                                .getJSONArray("data");
                                                        String timeStamp = sdf
                                                                .format(Calendar
                                                                        .getInstance()
                                                                        .getTime());
                                                        for (int i = 0; i < dataStreams
                                                                .length(); i++) {
                                                            JSONObject jsonSave = new JSONObject();
                                                            jsonSave.put(
                                                                    "channelCode",
                                                                    ((JSONObject) dataStreams
                                                                            .get(i))
                                                                            .get("sensor_id"));
                                                            jsonSave.put(
                                                                    "timeStamp",
                                                                    timeStamp);
                                                            jsonSave.put(
                                                                    "value",
                                                                    ((JSONObject) dataStreams
                                                                            .get(i))
                                                                            .get("value"));
                                                            try {
                                                                sensorDataQueue
                                                                        .put(jsonSave
                                                                                .toString());
                                                            } catch (InterruptedException e) {
                                                                log.error(
                                                                        "Exception caught in thread sleep",
                                                                        e);
                                                            }
                                                            // Update the cache
                                                            // for faster
                                                            // display on UI
                                                            IOTUtils.updateSensorCacheByStation(
                                                                    json.getString("station_id"),
                                                                    json);
                                                        }
                                                    }
                                                });
                            }
                        }
                        isSourceEndPointsReady = true;
                    }
                }
                // Only add message handler once
                // ???? synchronized?
                synchronized (destClientMap) {
                    if (destClientMap.size() == destUriMap.size()) {
                        for (Entry<String, WebsocketClientEndpoint> entry : destClientMap
                                .entrySet()) {
                            if (entry.getKey().equalsIgnoreCase("alarmagent")) {
                                entry.getValue()
                                        .addMessageHandler(
                                                new WebsocketClientEndpoint.MessageHandler() {
                                                    public void handleMessage(
                                                            String message) {
                                                        log.info(
                                                                "##### From alarmagent destination : {}; Message: {}",
                                                                entry.getKey(),
                                                                message);
                                                        // !!!! Do not simply
                                                        // forward the message,
                                                        // only command message
                                                        // is
                                                        // necessary
                                                        JSONObject json = null;
                                                        try {
                                                            json = new JSONObject(
                                                                    message);

                                                            try {
                                                                if (json.getString("type") != null
                                                                        && json.getString(
                                                                                "type")
                                                                                .equalsIgnoreCase(
                                                                                        "heartbeat")) {
                                                                    return;
                                                                }
                                                            } catch (Exception e) {
                                                                log.error("Not a heartbeat message.");
                                                            }

                                                            // Put the id length
                                                            // to 10 for some
                                                            // reservation
                                                            // commands.
                                                            if (json.getInt("command") > 10
                                                                    || json.getInt("command") < 1
                                                                    || json.length() != 1) {
                                                                // ignore
                                                                return;
                                                            }
                                                        } catch (Exception e) {
                                                            log.error(
                                                                    "Cannot parse the string to JSON object. Message ->",
                                                                    message);
                                                            return;
                                                        }
                                                        logger.info(
                                                                "*** Forward the command  {} to gateway.",
                                                                json.getInt("command"));
                                                        sourceClientMap
                                                                .get(entry
                                                                        .getKey())
                                                                .sendMessage(
                                                                        message);
                                                    }
                                                });
                            } else if (entry.getKey().equalsIgnoreCase(
                                    "sensordata")) {
                                entry.getValue()
                                        .addMessageHandler(
                                                new WebsocketClientEndpoint.MessageHandler() {
                                                    public void handleMessage(
                                                            String message) {
                                                        log.info(
                                                                "##### From sensordata destination : {}; Message: {}",
                                                                entry.getKey(),
                                                                message);
                                                        // !!!! Do not simply
                                                        // forward the message,
                                                        // only station id
                                                        // message
                                                        // is necessary
                                                        JSONObject json = null;
                                                        try {
                                                            json = new JSONObject(
                                                                    message);
                                                            try {
                                                                if (json.getString("type") != null
                                                                        && json.getString(
                                                                                "type")
                                                                                .equalsIgnoreCase(
                                                                                        "heartbeat")) {
                                                                    return;
                                                                }
                                                            } catch (Exception e) {
                                                                log.error("Not a heartbeat message.");
                                                            }

                                                            if (json.getInt("station_id") < 1
                                                                    || json.getInt("station_id") > 13
                                                                    || json.length() != 1) {
                                                                // ignore
                                                                return;
                                                            }
                                                        } catch (Exception e) {
                                                            log.error(
                                                                    "Cannot parse the string to JSON object. Message ->",
                                                                    message);
                                                            return;
                                                        }
                                                        logger.info(
                                                                "*** Forward the station id  {} to gateway.",
                                                                json.getInt("station_id"));
                                                        sourceClientMap
                                                                .get(entry
                                                                        .getKey())
                                                                .sendMessage(
                                                                        message);
                                                        // send the sensor data
                                                        // in cache to UI first
                                                        // to display faster
                                                        JSONObject jsonCache = IOTUtils
                                                                .getSensorCacheDataByStation(Integer
                                                                        .toString(json
                                                                                .getInt("station_id")));
                                                        if (jsonCache != null) {
                                                            logger.info("*** Send the cached sensor data for station: "
                                                                    + json.getInt("station_id"));
                                                            // send message must
                                                            // be thread-safe!
                                                            destClientMap
                                                                    .get(entry
                                                                            .getKey())
                                                                    .sendMessage(
                                                                            jsonCache
                                                                                    .toString());
                                                        }
                                                    }
                                                });
                            }
                        }
                        isDestEndPointsReady = true;
                    }

                }
                if (isDestEndPointsReady && isSourceEndPointsReady) {
                    log.info("Initialization of all end points are successfully!");
                    break;
                } else {
                    log.warn("Initialization of all end points are partially done. Will retry later.");
                }
            } catch (Exception ex) {
                log.error(
                        "Exception caught in iot Router initialization. Please retry later.",
                        ex);
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("Exception caught in thread sleep", e);
            }
        }
    }

    @Override
    public List<String> execute(Map<String, Object> ctx)
            throws InterruptedException, Exception {
        List<String> results = new ArrayList<String>();
        /* Reserved interface for future task setting */
        // ITask task = (ITask) ctx.get("task");
        // TaskParam taskParam = task.getParam();
        // String url = taskParam.getUrl();
        // log.debug("Get the url: {}", url);

        // Initialize the websocket client endpoint
        iotSchedulerService.execute(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });

        alarmBatchWriter = new BatchWriter(ctx, dataTypeEnum.Alarm.getType(),
                this, 60L, 60L);
        sensorBatchWriter = new BatchWriter(ctx, dataTypeEnum.Sensor.getType(),
                this, 60L, 60L);
        stationBatchWriter = new BatchWriter(ctx,
                dataTypeEnum.Station.getType(), this, 60L, 60L);
        sluiceBatchWriter = new BatchWriter(ctx, dataTypeEnum.Sluice.getType(),
                this, 60L, 60L);

        iotSchedulerService.execute(new Runnable() {
            @Override
            public void run() {
                while (!isCanceled) {
                    String message = null;
                    try {
                        // Block until queue is not null
                        message = alarmQueue.take();
                    } catch (InterruptedException e) {
                        logger.error("Interrupted datasift keyword executor.",
                                e);
                    }
                    if (message != null) {
                        JSONObject json = new JSONObject(message);
                        try {
                            if (json.getString("type") != null
                                    && json.getString("type").equalsIgnoreCase(
                                            "alarm")) {
                                alarmBatchWriter.addResult(message);
                            } else if (json.getString("type") != null
                                    && json.getString("type").equalsIgnoreCase(
                                            "station")) {
                                stationBatchWriter.addResult(message);
                            } else {
                                sluiceBatchWriter.addResult(message);
                            }
                        } catch (Exception e) {
                            log.error(
                                    "Cannot parse the JSON Object from alarm queue",
                                    e);
                        }
                    }
                }
            }
        });

        iotSchedulerService.execute(new Runnable() {
            @Override
            public void run() {
                while (!isCanceled) {
                    String message = null;
                    try {
                        // Block until queue is not null
                        message = sensorDataQueue.take();
                    } catch (InterruptedException e) {
                        logger.error("Interrupted datasift keyword executor.",
                                e);
                    }
                    if (message != null) {
                        sensorBatchWriter.addResult(message);
                    }
                }
            }
        });

        return isCanceled ? null : results;
    }

    @Override
    public void cancel(boolean mayInterruptIfRunning) throws Exception {
        // Poison message to the queue to avoid blocking?
        if (alarmBatchWriter != null) {
            alarmBatchWriter.shutdown();
        }
        if (sensorBatchWriter != null) {
            sensorBatchWriter.shutdown();
        }
        if (stationBatchWriter != null) {
            stationBatchWriter.shutdown();
        }
        if (sluiceBatchWriter != null) {
            sluiceBatchWriter.shutdown();
        }
        for (Entry<String, WebsocketClientEndpoint> entry : sourceClientMap
                .entrySet()) {
            Set<Session> sessions = entry.getValue().getUserSession()
                    .getOpenSessions();
            for (Session session : sessions) {
                session.close();
            }
        }
        for (Entry<String, WebsocketClientEndpoint> entry : destClientMap
                .entrySet()) {
            Set<Session> sessions = entry.getValue().getUserSession()
                    .getOpenSessions();
            for (Session session : sessions) {
                session.close();
            }
        }
        super.cancel(mayInterruptIfRunning);
    }

    private String[] addInfo(String[] info, String... additionals) {
        String[] res = new String[info.length + additionals.length];
        System.arraycopy(info, 0, res, 0, info.length);
        for (int i = 0; i < additionals.length; ++i) {
            res[info.length + i] = additionals[i];
        }
        return res;
    }

    private synchronized void writeDB(String schema, String table,
            List<String[]> structure_data) throws Exception {
        DataSource ds = ConfigStorage.DS_INFO;
        String DriverClassName = ds.getDriver();
        String url = ds.getUrl();
        String username = ds.getUserName();
        String password = ds.getPassword();

        try {
            Class.forName(DriverClassName);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            logger.error("No JDBC Driver found!");
            throw e;
        }
        Connections connections = new Connections(DriverClassName, url,
                username, password);
        java.sql.Connection conn = connections.getConnection();
        ColumnInfo column_info = new ColumnInfo(conn, schema, table,
                structure_data);
        column_info.insertData();
        logger.info("Write Table [{}.{}] Successed!", schema, table);
    }

    @Override
    public void save(Map<String, Object> ctx, List<String> data)
            throws Exception {
        // do nothing
        return;
    }

    public void save(String type, List<String> data) throws Exception {
        if (isCanceled() || isTestFlagOn()
                || !dataSourceMap.get("enable").equalsIgnoreCase("true"))
            return;
        // write to HANA
        if (data == null || data.isEmpty()) {
            logger.warn("No data Needs to be Persisted!");
            return;
        }
        List<String[]> output = new ArrayList<String[]>();
        IModelInfo di = null;
        dataTypeEnum typeEnum = getTypeByName(type);
        for (String row : data) {
            if (row.contains("joined") || row.contains("disconnected")) {
                continue;
            }
            switch (typeEnum) {
            case Alarm:
                di = new Alarm(row);
                break;
            case Sensor:
                di = new SensorData(row);
                break;
            case Station:
                di = new StationState(row);
                break;
            case Sluice:
                di = new SluiceState(row);
                break;
            default:
                break;
            }
            if (di == null) {
                logger.error("Invalid data type. Should not be here.");
                return;
            }
            logger.info(
                    "*** Construct the data line to insert. Type: {}, Line: {}",
                    typeEnum.getType(),
                    (Object[]) addInfo(di.getAttributeValueList()));
            output.add(addInfo(di.getAttributeValueList()));
        }
        writeDB(dataSourceMap.get("schema"),
                dataSourceMap.get(typeEnum.getType()), output);
    }

    public static dataTypeEnum getTypeByName(String name) {
        switch (name) {
        case "alarm":
            return dataTypeEnum.Alarm;
        case "sensor":
            return dataTypeEnum.Sensor;
        case "station":
            return dataTypeEnum.Station;
        case "sluice":
            return dataTypeEnum.Sluice;
        default:
            break;
        }
        return dataTypeEnum.None;
    }

    public static enum dataTypeEnum {
        Alarm("alarm"), Sensor("sensor"), Station("station"), Sluice("sluice"), None(
                "none");

        private final String type;

        private dataTypeEnum(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return type;
        }

        public String getType() {
            return type;
        }
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        try {
            logger.info("*** Start the IOTRouter ***");
            Map<String, Object> ctx = new HashMap<String, Object>();
            Map<String, String> params = new HashMap<String, String>();
            params.put("task_key", "IOT_WATERTREATMENT");
            TaskParam param = new TaskParam(params, null);
            ITask task = TaskFactory
                    .getInstance()
                    .createTask(
                            com.sap.cisp.xhna.data.TaskType.SocialMedia_ArticleData_ByKeyword,
                            "IOTRouter", param);
            ctx.put("task", task);

            IOTRouterExecutor exe = new IOTRouterExecutor();
            // exe.setTestFlagOn(true);
            ThreadFactory factory = new TaskThreadFactory();
            ExecutorService taskExecutorPool = new TaskExecutorPool(10, 10, 0L,
                    TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                    factory);
            exe.setTask(task);
            taskExecutorPool.submit(exe);
        } catch (Exception e) {
            log.error("Exception: ", e);
        }
    }
}