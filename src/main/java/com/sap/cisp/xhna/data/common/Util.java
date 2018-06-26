package com.sap.cisp.xhna.data.common;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.sap.cisp.xhna.data.config.DataSource;

public class Util {
    private static Logger logger = LoggerFactory.getLogger(Util.class);

    public static String DateConversion(String unformatted_date,
            String media_name) {
        DateFormat df = null;
        Date date = null;
        if (unformatted_date == null)
            return "";
        try {
            if ("twitter".equalsIgnoreCase(media_name)) {
                df = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy",
                        Locale.US);
            } else if ("gplus".equalsIgnoreCase(media_name)
                    || "youtube".equalsIgnoreCase(media_name)) {
                unformatted_date = unformatted_date.substring(0,
                        unformatted_date.length() - 1) + "+0000";
                df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            } else if ("facebook".equalsIgnoreCase(media_name)) {
                df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

            }
            date = df.parse(unformatted_date);
        } catch (ParseException e) {
            return "";
        }
        DateFormat std = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return std.format(date);
    }

    public static String EscapeString(String str) {
        if (str == null)
            return "";
        String s = str;
        s = s.replaceAll("\\n", " ");
        s = s.replaceAll("\\t", "\\\\t");
        s = s.replaceAll("\\r", " ");
        s = s.replaceAll("\\u201c", "\\\\\"");
        s = s.replaceAll("\\u201d", "\\\\\"");
        s = s.replaceAll("&quot;", "\\\\\"");
        s = s.replaceAll("\\u2018", "'");
        s = s.replaceAll("\\u2019", "'");
        s = s.replaceAll("&#39;", "'");
        s = s.replaceAll("&amp;", "&");
        return s;
    }

    public static String getOutpath(String inputpath, String before, String end) {
        int startidx = inputpath.lastIndexOf(before);
        String s = inputpath.substring(0, startidx);
        String temp = end;
        s += temp;
        s += inputpath
                .substring(startidx + before.length(), inputpath.length());
        return s;
    }

    public static String constructJson(String media, String type,
            String key_type, String key, String start_time, String end_time) {
        HashMap<String, Object> crawl_mapping = new HashMap<String, Object>();
        crawl_mapping.put("appID", "com.sap.cisp.xhna.data");
        if ("SocialArticle".equals(type)) {
            HashMap<String, Object> post_mapping = new HashMap<String, Object>();
            post_mapping.put("media", media);
            post_mapping.put("ByAccount".equals(key_type) ? "account"
                    : "keyword", key);
            post_mapping.put("postTimeFrom", start_time);
            post_mapping.put("postTimeTo", end_time);
            crawl_mapping.put("post", post_mapping);
        }
        ;
        JSON j = (JSON) JSON.toJSON(crawl_mapping);
        return j.toJSONString();
    }

    public static Document getDocument(String xmlFileName) {
        File file = new File(xmlFileName);
        Document _document = null;
        if (!file.exists()) {
            logger.error(xmlFileName + " file not exists!");
            return null;
        }
        SAXReader reader = new SAXReader();
        try {
            _document = reader.read(file);
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

    public static List<Map<String, String>> getLRUCacheConfig(
            String LRUCacheConfigXML) {
        List<Map<String, String>> paramsList = new ArrayList<Map<String, String>>();
        Element rootElement = Util.getRootElement(LRUCacheConfigXML);
        if (null == rootElement) {
            logger.error("[LRU Cache Config XML] root element is null.");
            return null;
        }

        @SuppressWarnings("unchecked")
        Iterator<Element> iterator = rootElement.elementIterator();
        while (iterator.hasNext()) {
            Map<String, String> params = new HashMap<String, String>();
            Element ele = iterator.next();
            String cacheName = ele.attributeValue("name");
            params.put("cacheName", cacheName);

            @SuppressWarnings("unchecked")
            Iterator<Element> it = ele.elementIterator();
            while (it.hasNext()) {
                Element el = it.next();
                String name = el.attributeValue("name");
                if ("size".equalsIgnoreCase(name)) {
                    params.put("size", el.attributeValue("value"));
                } else if ("initialSize".equalsIgnoreCase(name)) {
                    params.put("initialSize", el.attributeValue("value"));
                } else {
                    // ignore this property
                }
            }
            paramsList.add(params);
        }
        return paramsList;
    }

    public static List<DataSource> getDataSources(String DataSourceXML) {
        List<DataSource> dataSourceList = new ArrayList<DataSource>();
        Element rootElement = Util.getRootElement(DataSourceXML);
        if (null == rootElement) {
            logger.error("[Data Source XML] root element is null.");
            return null;
        }

        @SuppressWarnings("unchecked")
        Iterator<Element> iterator = rootElement.elementIterator();
        while (iterator.hasNext()) {
            DataSource dataSource = new DataSource();
            Element ele = iterator.next();
            String dbconnname = ele.attributeValue("id");
            dataSource.setId(dbconnname);

            @SuppressWarnings("unchecked")
            Iterator<Element> it = ele.elementIterator();
            while (it.hasNext()) {
                Element el = it.next();
                String name = el.attributeValue("name");
                if ("driverClassName".equalsIgnoreCase(name)) {
                    dataSource.setDriver(el.attributeValue("value"));
                } else if ("url".equalsIgnoreCase(name)) {
                    dataSource.setUrl(el.attributeValue("value"));
                } else if ("username".equalsIgnoreCase(name)) {
                    dataSource.setUserName(el.attributeValue("value"));
                } else if ("password".equalsIgnoreCase(name)) {
                    dataSource.setPassword(el.attributeValue("value"));
                } else {
                    // ignore this property
                }
            }
            dataSourceList.add(dataSource);
        }
        return dataSourceList;
    }

    public static DataSource getDBSByName(String DataSourceXML, String dbsname) {
        for (DataSource ds : getDataSources(DataSourceXML)) {
            if (ds.getId().equalsIgnoreCase(dbsname)) {
                return ds;
            }
        }
        return null;
    }

    public static void createFolderByFilePath(String filepath) {
        File f = new File(filepath);
        if (!f.getParentFile().exists()) {
            if(f.getParentFile().mkdirs()) {
                logger.debug("Create folder by file {} sucessfully.", f);
            }
        }
    }

    public static void main(String[] args) {
        // System.out.println(DateConversion("Mon Dec 15 06:38:43 +0000 2014","Twitter"));
    	String crawl_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
    	System.out.println(crawl_time);
    }
}
