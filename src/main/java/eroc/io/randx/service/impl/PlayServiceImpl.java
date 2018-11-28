package eroc.io.randx.service.impl;

import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.pojo.Buffer;
import eroc.io.randx.service.DeckDealer;
import eroc.io.randx.service.PlayService;
import eroc.io.randx.utils.CryptoUtils;
import eroc.io.randx.utils.TypeUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class PlayServiceImpl implements PlayService {


    private static DeckDealer deckDealer;

    private static List<byte[]> pks = new ArrayList<>();

    private static Integer cardNum = 54;

    private static Integer deckNum = 1;

    private static Integer round = (cardNum * deckNum) / pks.size() - 1;

    private static Integer r = 0;

    private static List<Integer> index;

    /**
     * 开始游戏
     */
    public synchronized void openGame(CopyOnWriteArraySet<WebSocketServer> wss) {
        for(WebSocketServer webSocketServer : wss) {
            pks.add(webSocketServer.getPk().toByteArray());
        }
        //判断轮到哪个玩家抽牌
        index = new ArrayList<>();
        for(Integer i = 0; i < round; i++) {
            for(int j = 0; j < 3; j++) {
                index.add(j);
            }
        }
        try {
            deckDealer = new DeckDealer();
            Buffer.transtion game = deckDealer.openGame(cardNum, deckNum, pks);
            //将game信息发送给玩家
            WebSocketServer.sendInfo(game.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 斗地主抽牌,抽17轮，剩3张
     */
    public synchronized void drawCard(Buffer.transtion transtion) {
        try {
            if (index.size() == 0) {
                WebSocketServer.sendInfo("抽牌结束".getBytes());
                return;
            }
            Integer i = index.get(0);
            String inpk = TypeUtils.bytesToHexString(pks.get(i));
            byte[] pkb = transtion.getPk(0).toByteArray();
            String pk = TypeUtils.bytesToHexString(transtion.getPk(0).toByteArray());
            if (inpk.equals(pk)) {
                byte[] sign = CryptoUtils.generateSign(transtion.getR(0).toString(), transtion.getS(0).toString());
                byte[] encrypt = deckDealer.drawCard(pkb, sign);
                index.remove(0);
                WebSocketServer.sendInfoSpecific(encrypt, pkb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public synchronized void drawLeftCards(Buffer.transtion transtion) {

    }


}
