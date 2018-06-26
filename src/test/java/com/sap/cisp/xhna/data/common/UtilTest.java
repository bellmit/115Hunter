package com.sap.cisp.xhna.data.common;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

public class UtilTest {
    public static Logger logger = LoggerFactory.getLogger(UtilTest.class);
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testEscapeString() {
        //test “”(\u201c and \u201d) replacement
        String str = "FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its “sexual” connotations.";
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("description", str);
        String result = Util.EscapeString(jsonObj.toString());
        assertEquals("{\"description\":\"FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its \\\"sexual\\\" connotations.\"}", result);
        logger.debug("result -> {}", result);
        //test "" replacement
        String str1 = "FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its \"sexual\" connotations.";
        JSONObject jsonObj1 = new JSONObject();
        jsonObj1.put("description", str1);
        String result1 = Util.EscapeString(jsonObj1.toString());
        assertEquals("{\"description\":\"FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its \\\"sexual\\\" connotations.\"}", result1);
        logger.debug("result1 -> {}", result1);
        //test &quot; replacement
        String str2 = "FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its &quot;sexual&quot; connotations.";
        JSONObject jsonObj2 = new JSONObject();
        jsonObj2.put("description", str2);
        String result2 = Util.EscapeString(jsonObj2.toString());
        assertEquals("{\"description\":\"FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its \\\"sexual\\\" connotations.\"}", result2);
        logger.debug("result2 -> {}", result2);
        //test ''(\u2018 and \u2019) replacement
        String str3 = "FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its \u2018sexual\u2019 connotations.";
        JSONObject jsonObj3 = new JSONObject();
        jsonObj3.put("description", str3);
        String result3 = Util.EscapeString(jsonObj3.toString());
        assertEquals("{\"description\":\"FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its 'sexual' connotations.\"}", result3);
        logger.debug("result3 -> {}", result3);
        //test &#39;(') replacement
        String str4 = "FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its &#39;sexual&#39; connotations.";
        JSONObject jsonObj4 = new JSONObject();
        jsonObj4.put("description", str4);
        String result4 = Util.EscapeString(jsonObj4.toString());
        assertEquals("{\"description\":\"FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its 'sexual' connotations.\"}", result4);
        logger.debug("result4 -> {}", result4);
        //test &amp; (&) replacement
        String str5 = "FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its &amp;sexual&amp; connotations.";
        JSONObject jsonObj5 = new JSONObject();
        jsonObj5.put("description", str5);
        String result5 = Util.EscapeString(jsonObj5.toString());
        assertEquals("{\"description\":\"FEMINISTS have succeeded in getting a Friday night club night at a top university renamed because of its &sexual& connotations.\"}", result5);
        logger.debug("result5 -> {}", result5);
        String str6 = "Turkey is increasingly being pulled into the Syrian vortex.\nOct. 11, 2015 6:23 p.m. ET\nA pair of suicide bombings ripped through a peace rally in Ankara on Saturday morning, killing at least 95 marchers and injuring hundreds. It was the deadliest terror attack in Turkey’s modern history and— further evidence that a country that is supposed to be an anchor of Middle East stability is increasingly vulnerable to regional furies and its own domestic discontents.\nNo group had taken credit for the bombings by our deadline Sunday evening. Turkish security forces believe the attack was carried out either by Islamic...\nTo Read the Full Story, Subscribe or Sign In\nPopular on WSJ\n";
        JSONObject jsonObj6 = new JSONObject();
        jsonObj6.put("description", str6);
        String result6 = jsonObj6.toString() ;// Util.EscapeString(jsonObj6.toString());
        logger.debug("result6 -> {}", result6);
    }

}
