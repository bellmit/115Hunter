package com.sap.cisp.xhna.data.db;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cisp.xhna.data.TaskType;

@SuppressWarnings("rawtypes")
public class QuerySqls {
	private static Map<String,String> sqls=new HashMap<String,String>(); 
	private static Logger logger=LoggerFactory.getLogger(QuerySqls.class);
	static{
		SAXReader reader=new SAXReader();
		try {
			Document document=reader.read(new File("configurations/querySqls.xml"));
			Element root=document.getRootElement();
			for ( Iterator i = root.elementIterator(); i.hasNext(); ) {
	            Element element = (Element) i.next();
	            sqls.put(element.attributeValue("name"), element.getTextTrim());
	        }
		} catch (DocumentException e) {
			logger.error("DocumentException",e);
		}
	}
	
	public static String getSql(String queryName){
		return sqls.get(queryName);
	}

	public static String getUpdateTaskLastCrawlTimeSql(TaskType type) {
		String sql=null;
		switch(type){
		case SocialMedia_ArticleData_ByKeyword:
			sql=getSql("updateKeywordArticleTaskLastCrawlTime");
			break;
		case SocialMedia_ArticleData_ByAccount:
			sql=getSql("updateAccountAriticleTaskLastCrawlTime");
			break;
		case SocialMedia_AccountData:
			break;
		case TraditionalMedia_ArticleData_ByWebPage:
			sql=getSql("updateWebPageTaskLastCrawlTime");
			break;
		case TraditionalMedia_ArticleData_ByRSS:
			sql=getSql("updateRSSLastCrawlTime");
			break;
		default:
			sql="";
			break;
		}
		return sql;
	}
	
}
