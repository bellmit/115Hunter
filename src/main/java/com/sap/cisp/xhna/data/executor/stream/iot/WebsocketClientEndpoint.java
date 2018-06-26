package com.sap.cisp.xhna.data.executor.stream.iot;

import java.io.IOException;
import java.net.URI;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.cliffc.high_scale_lib.NonBlockingHashSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ChatServer Client
 *
 */
@ClientEndpoint
public class WebsocketClientEndpoint {
    private static Logger log = LoggerFactory.getLogger(WebsocketClientEndpoint.class);
    Session userSession = null;
    private MessageHandler messageHandler;
    public static int CONNECTION_TIMEOUT_LIMIT = 65;
    public static int CONNECTION_TIMEOUT = 65;
    private URI endpointURI;
    protected DateTime lastSeen;
    protected static Set<WebsocketClientEndpoint> streams = new NonBlockingHashSet<>();
    public static boolean detectDeadConnection = true;

    // Monitor and try to reconnect
    // !!!! Alarm end point is error prone. May create duplicated sessions.
    static {
        new Thread(new Runnable() {
            public void run() {
                while (detectDeadConnection) {
                    long now = DateTime.now().getMillis();
                    for (WebsocketClientEndpoint data : streams) {
                        if (data.lastSeen != null) {
                            if (now - data.lastSeen.getMillis() >=
                                    TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT_LIMIT)) {
                                log.warn("@@@@@ There is no data seen on WebsocketClientEndpoint {} for a while. Reconnect it.", data.getUserSession().getId());
                                data.closeAndReconnect();
                            }
                        }
                    }
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT));
                    } catch (InterruptedException e) {
                        LoggerFactory.getLogger(getClass()).info("Interrupted while waiting to check conn");
                    }
                }
            }
        }).start();
    }
    
    public WebsocketClientEndpoint(URI endpointURI) {
        try {
            this.endpointURI = endpointURI;
            connect();
            streams.add(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private void connect() throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        try {
            container.connectToServer(this, endpointURI);
        } catch (DeploymentException e) {
            log.error("Connect exception.", e);
            throw e;
        } catch (IOException e) {
            log.error("Connect exception.", e);
            throw e;
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
        log.info("opening websocket");
        this.userSession = userSession;
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        log.info("closing websocket. Session : {}; Reason : {}", userSession, reason);
        closeAndReconnect();
    }

    private void closeAndReconnect(){
        try {
            this.userSession.close();
        } catch (IOException e1) {
            log.error("!!! Failed to close the websocket session.", e1);
        }
        this.userSession = null;
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT_LIMIT));
        } catch (InterruptedException ignored) {
            log.info("Sleep interrupted, reconnecting");
        }
        try {
            connect();
        } catch (Exception e) {
            log.error("!!! Failed to reconnect the websocket endpoint.", e);
        }
    }
    
    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage(String message) {
        if (this.messageHandler != null) {
            lastSeen = DateTime.now();
            this.messageHandler.handleMessage(message);
        }
    }

    /**
     * register message handler
     *
     * @param message
     */
    public void addMessageHandler(MessageHandler msgHandler) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param user
     * @param message
     */
    public synchronized void  sendMessage(String message) {
        try {
            if(this.userSession != null && this.userSession.getBasicRemote() != null) {
                this.userSession.getBasicRemote().sendText(message);
            } else {
                throw new IOException("Remote Endpoint is null.");
            }
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            log.error("Exception caught during sending message.", e);
        }
    }
    
    public Session getUserSession() {
        return userSession;
    }

    public void setUserSession(Session userSession) {
        this.userSession = userSession;
    }

    /**
     * Message handler.
     *
     */
    public static interface MessageHandler {

        public void handleMessage(String message);
    }
}
