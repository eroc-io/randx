package eroc.io.randx.service;

import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.pojo.Buffer;

import java.util.concurrent.CopyOnWriteArraySet;

public interface PlayService {

    Buffer.OpenResponse initPlay(byte[] msg);

    Buffer.JoinResponse joinGame(byte[] msg, WebSocketServer wss);

    Buffer.DrawResponse drawCard(byte[] dr, WebSocketServer wss);

    void drawLeftCards(CopyOnWriteArraySet<WebSocketServer> wss);

    void returnCards(Buffer.ReturnRequest rr);

}
