package eroc.io.randx.service;

import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.pojo.Buffer;

public interface PlayService {

    void sendHallMessage(WebSocketServer wss);

    Buffer.OpenResponse initPlay(byte[] msg);

    Buffer.StartResponse joinGame(byte[] msg, WebSocketServer wss);

    Buffer.DrawResponse drawCard(byte[] dr, WebSocketServer wss);

    Buffer.DrawLeftNotification drawLeftCards(byte[] dleftReq, WebSocketServer wss);

    Buffer.ReturnResponse returnCards(byte[] rr, WebSocketServer wss);

    void leavePlay(WebSocketServer wss);

    void disCard(byte[] disCards, WebSocketServer wss);

    void catchCard(byte[] catchReq, WebSocketServer wss);
}
