# 115Hunter
This is data crawler service for social media.

How to use?
-------------------------------------------------------------------------------
If you use SINGLETON work mode(It's by default, no need extra configuration).
-------------------------------------------------------------------------------
1. Build the tar.gz artifact 
   mvn install
2. Upload the com.sap.cisp.xhna.data-0.0.1-SNAPSHOT-bin.tar.gz  to Linux server
3. Log On server as root:
       cd <Your path>
       tar zxvf  com.sap.cisp.xhna.data-0.0.1-SNAPSHOT-bin.tar.gz
4.  cd com.sap.cisp.xhna.data-0.0.1-SNAPSHOT
5.  chmod +x datacrawlerd.sh
6.  Install data crawler service:
       ./datacrawlerd.sh install
7.  Start data crawler service:
       rcdatacrawler start
    Alternatively, you can:
       service datacrawler start
8. Check the service status:
       rcdatacrawler status
    Alternatively, you can:
       service datacrawler status
9. To stop the service:
        rcdatacrawler stop
    Alternatively, you can:
       service datacrawler stop
10. To run the service on console for debug purpose:
       rcdatacrawler console
       
       
-------------------------------------------------------------------------------
If you use distribution deployment, which may need to start server and worker(s).
-------------------------------------------------------------------------------
Step A) Start server process
1. Build the tar.gz artifact 
   mvn install
2. Upload the com.sap.cisp.xhna.data-0.0.1-SNAPSHOT-bin.tar.gz  to Linux server
3. Log On server as root:
       cd <Your path>
       tar zxvf  com.sap.cisp.xhna.data-0.0.1-SNAPSHOT-bin.tar.gz
4.  cd com.sap.cisp.xhna.data-0.0.1-SNAPSHOT
5.  chmod +x datacrawlerd.sh
6a. Install data crawler service:
       ./datacrawlerd.sh install
6b. Change the configuration
    cd configurations
    vi Main.properties
      Gearman_Host=127.0.0.1
      Gearman_Port=4730
      WorkMode=2
    save the change.  
7.  Start data crawler service:
       rcdatacrawler start
    Alternatively, you can:
       service datacrawler start
8. Check the service status:
       rcdatacrawler status
    Alternatively, you can:
       service datacrawler status
9. To stop the service:
        rcdatacrawler stop
    Alternatively, you can:
       service datacrawler stop
10. To run the service on console for debug purpose:
       rcdatacrawler console
--------------------------------------------------------------------------------      
Step B) Start worker(s) process
* Step 1 - 3 is optional if you use the same Linux server to start worker process, 
  please copy the whole folder from step A) to another location.

1. Build the tar.gz artifact 
   mvn install
2. Upload the com.sap.cisp.xhna.data-0.0.1-SNAPSHOT-bin.tar.gz  to Linux server
3. Log On server as root:
       cd <Your path>
       tar zxvf  com.sap.cisp.xhna.data-0.0.1-SNAPSHOT-bin.tar.gz
4.  cd com.sap.cisp.xhna.data-0.0.1-SNAPSHOT
5.  chmod +x datacrawlerd.sh
6.  Change the configuration
    cd configurations
    vi Main.properties
      Gearman_Host=<The Linux server IP in Step A)>
      Gearman_Port=4730
      WorkMode=0
    save the change.  
7.  Start data crawler service:
       ./datacrawlerd.sh startworker
8. Check the service status:
      ps -ef | grep com.sap
9. To stop the service:
       ./datacrawlerd.sh stopworker
----------------------------------------------------------------------------------

For more usage, You can type "rcdatacralwer -h"
Usage: /usr/local/sbin/rcdatacrawler {console|start|stop|restart|status|install|uninstall}

Note:
Please pay attention that your compiler JDK version should not be higher than the JRE version on server.
