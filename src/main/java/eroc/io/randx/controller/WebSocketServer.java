package eroc.io.randx.controller;

import com.google.protobuf.ByteString;
import eroc.io.randx.event.PlayEvent;
import eroc.io.randx.pojo.Buffer;
import org.springframework.beans.factory.annotation.Autowired;
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
@ServerEndpoint("/deckdealer")
@Component
public class WebSocketServer {

    @Autowired
    private ApplicationContext applicationContext;


    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的
    private static int onlineCount = 0;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //当玩家公钥
    private ByteString pk;


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, byte[] player) {
        try {
            WebSocketServer ws = new WebSocketServer();
            Buffer.transtion p = Buffer.transtion.parseFrom(player);
            ws.setPk(p.getPk());
            ws.setSession(session);
            webSocketSet.add(ws);     //加入set中
            addOnlineCount();           //在线数加1
            int i = getOnlineCount();
            System.out.println("有新连接加入！当前在线人数为" + i);
            //发布监听
            applicationContext.publishEvent(new PlayEvent(this, i, webSocketSet));
            Buffer.transtion.Builder out = Buffer.transtion.newBuilder();
            String msg = "连接成功,当前已有" + i + "个玩家准备就绪";
            sendMessage(out.setMsg(msg).build().toByteArray());
        } catch (IOException e) {
            System.out.println("websocket IO异常");
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        System.out.println("有一连接关闭！当前在线人数为" + getOnlineCount());
    }


    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(byte[] message, Session session) throws IOException {
        System.out.println("来自客户端的消息:" + message);
//        群发消息
        sendInfo(message);
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
//        this.session.getBasicRemote().sendText(message);
        //this.session.getAsyncRemote().sendText(message);
    }


    /**
     * 群发自定义消息
     */
    public static void sendInfo(byte[] message) throws IOException {
        for(WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                continue;
            }
        }
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

    public ByteString getPk() {
        return pk;
    }

    public void setPk(ByteString pk) {
        this.pk = pk;
    }
}
