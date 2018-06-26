package com.sap.cisp.xhna.data.common;
import javax.lang.model.SourceVersion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method; 

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Processes {

    private static Logger logger = LoggerFactory.getLogger(Processes.class);

    private static final PidHelper PID_HELPER;

    static {
        // Comparing with the string value to avoid a strong dependency on JDK 9
        if (SourceVersion.latest().toString().equals( "RELEASE_9" )) {
            PID_HELPER = PidHelper.JDK_9;
        }
        else {
            PID_HELPER = PidHelper.LEGACY;
        }
    }

    public Processes() {
        // no instance
    }

    public static String processId() {
        return PID_HELPER.getPid();
    }
    
    public static final String getPID() {
        String pid = System.getProperty("pid");
        if (pid == null) {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            String processName = runtimeMXBean.getName();
            if (processName.indexOf('@') != -1) {
                pid = processName.substring(0, processName.indexOf('@'));
            } else {
                pid = processId();
            }
            System.setProperty("pid", pid);
        }
        logger.debug("PID-> {}", pid);
        return pid;
    }

    private static String unixLikeProcessId() {
        String pid = null;

        String[] cmd = new String[] { "/bin/sh", "-c", "echo $$ $PPID" };

        InputStream is = null;
        ByteArrayOutputStream baos = null;
        try {
            byte[] buf = new byte[1024];
            Process exec = Runtime.getRuntime().exec(cmd);
            is = exec.getInputStream();
            baos = new ByteArrayOutputStream();
            while (is.read(buf) != -1) {
                baos.write(buf);
            }
            String ppids = baos.toString();
            pid = ppids.split(" ")[1];
        } catch (Exception e) {
            logger.error("Exception",e);
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    logger.error("Exception",e);
                }
            }
            if(baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    logger.error("Exception",e);
                }
            }
        }
        return pid;
    }

     
    private enum PidHelper {

        JDK_9 {
            @Override
            String getPid() {
                try {
                    // Invoking via reflection to avoid a strong dependency on JDK 9
                    Method getPid = Processes.class.getMethod("getPid");
                    return (String) getPid.invoke(getPid, (Object[])null);
                }
                catch (Exception e) {
                    logger.error("Exception",e);
                    return null;
                }
            }
        },
        LEGACY {
            @Override
            String getPid() {
                String pid=unixLikeProcessId();
                if (pid==null) {
                    logger.error("Cannot get process pid.");
                }
                return pid;
            }
        };

        abstract String getPid();
    }
    
    public static void main(String[] args) {
        Processes.getPID();
        while(true) {
            ;
        }
    }
}
