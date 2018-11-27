package eroc.io.randx.service.impl;

import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.pojo.Buffer;
import eroc.io.randx.service.DeckDealer;
import eroc.io.randx.service.PlayService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class PlayServiceImpl implements PlayService {


    private static DeckDealer deckDealer;

    /**
     * 开始游戏
     */
    public void openGame(CopyOnWriteArraySet<WebSocketServer> wss) {
        List<byte[]> pks = new ArrayList<>();
        for(WebSocketServer webSocketServer : wss) {
            pks.add(webSocketServer.getPk().toByteArray());
        }
        try {
            deckDealer = new DeckDealer();
            Buffer.transtion game = deckDealer.openGame(54, 1, pks);
            //将game信息发送给玩家
            WebSocketServer.sendInfo(game.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
