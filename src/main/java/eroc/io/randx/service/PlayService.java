package eroc.io.randx.service;

import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.pojo.Buffer;

import java.util.concurrent.CopyOnWriteArraySet;

public interface PlayService {


    void openGame(CopyOnWriteArraySet<WebSocketServer> wss);

    void drawCard(Buffer.transtion transtion);

    void drawLeftCards(Buffer.transtion transtion);

}
