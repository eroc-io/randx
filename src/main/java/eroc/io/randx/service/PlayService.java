package eroc.io.randx.service;

import eroc.io.randx.controller.WebSocketServer;

import java.util.concurrent.CopyOnWriteArraySet;

public interface PlayService {


    void openGame(CopyOnWriteArraySet<WebSocketServer> wss);

}
