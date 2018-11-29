package eroc.io.randx.service;

import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.pojo.Buffer;

import java.util.concurrent.CopyOnWriteArraySet;

public interface PlayService {


    void openGame(CopyOnWriteArraySet<WebSocketServer> wss);

    void drawCard(Buffer.DrawRequest dr);

    void drawLeftCards(CopyOnWriteArraySet<WebSocketServer> wss);

    void returnCards(Buffer.ReturnRequest rr);

}
