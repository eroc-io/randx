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

/**
 * 斗地主controller
 */
@ServerEndpoint("/ws")
@Component
public class WebSocketServer {

//    @Resource
//    private PlayService playService;

    //此处是解决无法注入的关键
    private static ApplicationContext applicationContext;
    //你要注入的service或者dao
    private PlayService playService;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketServer.applicationContext = applicationContext;
    }

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();
    //记录当前在线连接数
    private static int onlineCount = 0;
    //与客户端的连接会话
    private Session session;

    private String uid;

    private byte[] drawRequest;


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        playService = applicationContext.getBean(PlayService.class);
        this.session = session;
        this.uid = UUIDUtils.getUUID();
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        playService.sendHallMessage(this);
        System.out.println("有新连接加入！当前在线人数为" + getOnlineCount());
        //发布监听
//      applicationContext.publishEvent(new PlayEvent(this, i, webSocketSet));
    }


    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
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
        }
    }


    /**
     * 发送消息
     */
    public static void sendInfo(byte[] message, String uid) throws IOException {
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
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        playService.leavePlay(this);
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 发生错误时调用
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