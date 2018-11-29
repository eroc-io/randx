package eroc.io.randx.controller;

import eroc.io.randx.pojo.Buffer;
import eroc.io.randx.service.PlayService;
import eroc.io.randx.utils.TypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 斗地主controller
 */
@ServerEndpoint("/ws")
@Component
public class WebSocketServer {

    @Autowired
    private PlayService playService;

    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();
    //记录当前在线连接数
    private static int onlineCount = 0;
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    //当玩家公钥
    private byte[] pk;
    //签名
    private byte[] sign;
    //牌桌编号，牌桌id
    private Map<Integer, byte[]> deck;


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        WebSocketServer ws = new WebSocketServer();
        ws.setSession(session);
        webSocketSet.add(ws);     //加入set中
        addOnlineCount();           //在线数加1
        int i = getOnlineCount();
        System.out.println("有新连接加入！当前在线人数为" + i);
        //发布监听
//            applicationContext.publishEvent(new PlayEvent(this, i, webSocketSet));
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
        byte b = message[0];
        int l = message.length - 1;
        byte[] msg = new byte[l];
        System.arraycopy(message, 1, msg, 0, l);
        if (b == 0) {//OpenRequest

        } else if (b == 1) {//JoinReaquest

        } else if (b == 2) {//drawReaquest
            playService.drawCard(Buffer.DrawRequest.parseFrom(msg));
        } else if (b == 3) {//drawLeftRequest
            //取剩余牌，所有人的公钥和签名
//            for(WebSocketServer ws : webSocketSet) {
//                if (TypeUtils.bytesToHexString(ws.getPk()).equalsIgnoreCase(TypeUtils.bytesToHexString(player.getPk(0).toByteArray()))) {
//                    byte[] sign = CryptoUtils.generateSign(player.getR(0), player.getS(0));
//                    ws.setSign(sign);
//                }
//            }
//            playService.drawLeftCards(webSocketSet);
        } else if (b == 4) {//return request
//            for(WebSocketServer ws : webSocketSet) {
//                if (TypeUtils.bytesToHexString(ws.getPk()).equalsIgnoreCase(TypeUtils.bytesToHexString(player.getPk(0).toByteArray()))) {
//                    playService.returnCards(player);
//                }
//            }
        } else {
            this.sendMessage("操作异常".getBytes());
        }
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


    /**
     * 群发自定义消息
     */
    public static void sendInfo(byte[] message) throws IOException {
        for(WebSocketServer item : webSocketSet) {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 给指定的用户发送消息
     *
     * @param msg
     */
    public static void sendInfoSpecific(byte[] msg, byte[] pk) {
        String spk = TypeUtils.bytesToHexString(pk);
        for(WebSocketServer item : webSocketSet) {
            try {
                String ipk = TypeUtils.bytesToHexString(item.getPk());
                if (ipk.equals(spk)) {
                    item.sendMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
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

    public byte[] getPk() {
        return pk;
    }

    public void setPk(byte[] pk) {
        this.pk = pk;
    }

    public byte[] getSign() {
        return sign;
    }

    public void setSign(byte[] sign) {
        this.sign = sign;
    }
}
