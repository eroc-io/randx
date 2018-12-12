package eroc.io.randx.controller;

import eroc.io.randx.pojo.Buffer;
import eroc.io.randx.service.PlayService;
import eroc.io.randx.utils.TypeUtils;
import eroc.io.randx.utils.UUIDUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/ws")
@Component
public class WebSocketServer {


    private static ApplicationContext applicationContext;

    private PlayService playService;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketServer.applicationContext = applicationContext;
    }

    // Store the MyWebSocket object corresponding to each client
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    // Record the current number of online connections
    private static int onlineCount = 0;

    // Connection session with the client
    private Session session;

    private String uid;

    // Cache onmessage request
    private byte[] drawRequest;


    /**
     * Connection establishment successfully called methodConnection establishment successfully called method
     *
     * @param session Connection session with the client
     */
    @OnOpen
    public void onOpen(Session session) {
        playService = applicationContext.getBean(PlayService.class);
        this.session = session;
        this.uid = UUIDUtils.getUUID();
        webSocketSet.add(this);
        addOnlineCount();
        playService.sendHallMessage(this);
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
    }


    /**
     * Method called after receiving a client message
     *
     * @param message The message sent by the client
     * @param session Connection session with the client
     */
    @OnMessage
    public void onMessage(byte[] message, Session session) throws IOException {
        byte b = message[0];
        int l = message.length - 1;
        byte[] msg = new byte[l];
        System.arraycopy(message, 1, msg, 0, l);
        if (b == 0) {
            //OpenRequest
            Buffer.OpenResponse oresp = playService.initPlay(msg);
            sendMessage(TypeUtils.getMsg(oresp.toByteArray(), (byte) 0));
        } else if (b == 1) {
            //JoinReaquest
            Buffer.StartResponse jresp = playService.joinGame(msg, this);
            if (!StringUtils.isBlank(jresp.getErrMsg())) {
                sendMessage(TypeUtils.getMsg(jresp.toByteArray(), (byte) 1));
            }
        } else if (b == 2) {
            //drawReaquest
            Buffer.DrawResponse dresp = playService.drawCard(msg, this);
            if (null != dresp && !StringUtils.isBlank(dresp.getErrMsg())) {
                sendMessage(TypeUtils.getMsg(dresp.toByteArray(), (byte) 2));
            }
        } else if (b == 3) {
            //drawLeftRequest
            Buffer.DrawLeftNotification dln = playService.drawLeftCards(msg, this);
            if (!StringUtils.isBlank(dln.getErrMsg())) {
                sendMessage(TypeUtils.getMsg(dln.toByteArray(), (byte) 4));
            }
        } else if (b == 4) {
            //return request
            Buffer.ReturnResponse returnResponse = playService.returnCards(msg, this);
            if (!StringUtils.isBlank(returnResponse.getErrMsg())) {
                sendMessage(TypeUtils.getMsg(returnResponse.toByteArray(), (byte) 5));
            }
        } else if (b == 5) {
            //DisCard request
            playService.disCard(msg, this);
        }
    }


    /**
     * sendInfo
     *
     * @param message Sent message
     * @param uid     Uid is empty to send messages to everyone, not empty to send messages to the specified client
     */
    public static void sendInfo(byte[] message, String uid) throws IOException {
        if (0 == webSocketSet.size()) {
            return;
        }
        for(WebSocketServer item : webSocketSet) {
            try {
                if (uid == null) {
                    item.sendMessage(message);
                } else if (item.uid == uid) {
                    item.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Connection close call method
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);
        playService.leavePlay(this);
        subOnlineCount();
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * Called when an error occurs
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.out.println("发生错误");
        error.printStackTrace();
    }


    public void sendMessage(byte[] message) throws IOException {
        OutputStream sendStream = this.session.getBasicRemote().getSendStream();
        sendStream.write(message);
        sendStream.flush();
        sendStream.close();
    }


    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount++;
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount--;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public byte[] getDrawRequest() {
        return drawRequest;
    }

    public void setDrawRequest(byte[] drawRequest) {
        this.drawRequest = drawRequest;
    }
}