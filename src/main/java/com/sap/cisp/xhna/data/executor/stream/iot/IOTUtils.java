package com.sap.cisp.xhna.data.executor.stream.iot;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IOTUtils {
    private static Logger logger = LoggerFactory.getLogger(IOTUtils.class);
    private static InputStream inputStream = null;
    private static Document _document = null;
    protected static ConcurrentHashMap<String, JSONObject> sensorCache = new ConcurrentHashMap<String, JSONObject>();
    

    public IOTUtils() {

    }


    public static void updateSensorCacheByStation(String stationId, JSONObject json) {
        sensorCache.put(stationId, json);
    }
    
    // Issue: timestamp, how to keep consistent with sensor reporting?
    public static JSONObject getSensorCacheDataByStation(String stationId) {
        JSONObject json = sensorCache.get(stationId);
        try {
            if (json.get("timestamp") == null
                    || json.get("timestamp").toString().isEmpty()) {
                json.remove("timestamp");
                //2 seconds ago?
                json.put("timestamp", System.currentTimeMillis() - 2000);
            }
        } catch (JSONException e) {
            logger.error("JSONException to set timestamp.", e);
        }
        return json;
    }

    public static Document getDocument(String xmlFileName) {
        inputStream = IOTUtils.class.getClassLoader().getResourceAsStream(xmlFileName);
        try {
            logger.info("**************** Get the resource file: "
                    + inputStream.available() + " path: "
                    + inputStream.toString());
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            logger.error("Cannot read the input file.", e1);
        }
        _document = null;

        SAXReader reader = new SAXReader();
        try {
            _document = reader.read(inputStream);
        } catch (DocumentException e) {
            logger.error("[Data Source XML] xml parse DocumentException is error.");
            logger.error(e.getMessage());
            return null;
        }
        return _document;
    }

    public static Element getRootElement(String xmlFileName) {
        Document _document = getDocument(xmlFileName);
        if (null == _document) {
            logger.error("[Data Source XML] xml document is null.");
            return null;
        }
        Element rootElement = _document.getRootElement();
        return rootElement;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Map<String, String>> getServerURI(
            String stationConfigXML) {
        Map<String, Map<String, String>> uriMap = new HashMap<String, Map<String, String>>();
        try {
            // iot
            Element rootElement = getRootElement(stationConfigXML);
            if (null == rootElement) {
                logger.error("[Station Config XML] root element is null.");
                return null;
            }

            // servers
            Iterator<Element> iteratorSource = ((Element) rootElement
                    .elementIterator("sources").next()).elementIterator();
            Map<String, String> sourceMap = new HashMap<String, String>();
            while (iteratorSource.hasNext()) {
                // server
                Element ele = iteratorSource.next();
                String type = ele.attributeValue("type");
                String uri = ele.attributeValue("uri");
                sourceMap.putIfAbsent(type, uri);
            }
            uriMap.put("source", sourceMap);

            Iterator<Element> iteratorDest = ((Element) rootElement
                    .elementIterator("destinations").next()).elementIterator();
            Map<String, String> destMap = new HashMap<String, String>();
            while (iteratorDest.hasNext()) {
                // server
                Element ele = iteratorDest.next();
                String type = ele.attributeValue("type");
                String uri = ele.attributeValue("uri");
                destMap.putIfAbsent(type, uri);
            }
            uriMap.put("destination", destMap);
        } catch (Exception e) {
            logger.error("Exception", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return uriMap;
    }

    /**
     * Get the data source configuration for schema/table name.
     * @param stationConfigXML
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getDataSource(
            String stationConfigXML) {
        Map<String, String> dataSourceMap = new HashMap<String, String>();
        try {
            // iotRouter
            Element rootElement = getRootElement(stationConfigXML);
            if (null == rootElement) {
                logger.error("[Station Config XML] root element is null.");
                return null;
            }

            // data source
            String schema = ((Element) rootElement
                    .elementIterator("datasource").next()).attributeValue("schema");
            String enable = ((Element) rootElement
                    .elementIterator("datasource").next()).attributeValue("enable");
            dataSourceMap.putIfAbsent("schema", schema);
            dataSourceMap.putIfAbsent("enable", enable);
            Iterator<Element> iteratorSource = ((Element) rootElement
                    .elementIterator("datasource").next()).elementIterator();
            while (iteratorSource.hasNext()) {
                // table
                Element ele = iteratorSource.next();
                String type = ele.attributeValue("type");
                String name = ele.attributeValue("name");
                dataSourceMap.putIfAbsent(type, name);
            }
        } catch (Exception e) {
            logger.error("Exception", e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return dataSourceMap;
    }
    
    public static enum StateEnum {
        Green("green"), Yellow("orange"), Red("red");

        private final String state;

        private StateEnum(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return state;
        }

        public String getState() {
            return state;
        }
    }
}
