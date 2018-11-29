package eroc.io.randx.service.impl;

import com.google.protobuf.ByteString;
import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.dao.ProofsDao;
import eroc.io.randx.pojo.Buffer;
import eroc.io.randx.service.DeckDealer;
import eroc.io.randx.service.PlayService;
import eroc.io.randx.utils.CryptoUtils;
import eroc.io.randx.utils.TypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class PlayServiceImpl implements PlayService {

    @Autowired
    private ProofsDao proofsDao;

    private static DeckDealer deckDealer;

    private static List<byte[]> pks;

    private static Integer cardNum = 54;

    private static Integer deckNum = 1;

    private static Integer round;

    private static Integer r = 0;

    private static List<Integer> index;

    /**
     * 开始游戏
     */
    public synchronized void openGame(CopyOnWriteArraySet<WebSocketServer> wss) {
        pks = new ArrayList<>();
        for(WebSocketServer webSocketServer : wss) {
            pks.add(webSocketServer.getPk());
        }
        //判断轮到哪个玩家抽牌
        index = new ArrayList<>();
        round = (cardNum * deckNum) / pks.size() - 1;
        for(Integer i = 0; i < round; i++) {
            for(int j = 0; j < 3; j++) {
                index.add(j);
            }
        }
        try {
            deckDealer = new DeckDealer();
            Buffer.JoinResponse game = deckDealer.openGame(cardNum, deckNum, pks);
            //将game信息发送给玩家
            WebSocketServer.sendInfo(game.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 斗地主抽牌,抽17轮，剩3张
     */
    public synchronized void drawCard(Buffer.DrawRequest dr) {
        try {
            if (index.size() == 0) {
                WebSocketServer.sendInfo("抽牌结束".getBytes());
                return;
            }
            Integer i = index.get(0);
            String inpk = TypeUtils.bytesToHexString(pks.get(i));
            byte[] pkb = dr.getPk().toByteArray();
            String pk = TypeUtils.bytesToHexString(pkb);
            if (inpk.equals(pk)) {
                byte[] sign = dr.getSig().toByteArray();
                Object[] o = deckDealer.drawCard(pkb, sign);
                Buffer.DrawResponse card = (Buffer.DrawResponse) o[0];
                Buffer.DrawNotification notify = (Buffer.DrawNotification) o[1];
                WebSocketServer.sendInfoSpecific(card.toByteArray(),pkb);
                //给其他人发送消息
                index.remove(0);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 抽取剩余牌
     *
     * @param
     */
    public synchronized void drawLeftCards(CopyOnWriteArraySet<WebSocketServer> wss) {
        List<byte[]> pks = new ArrayList<>();
        List<byte[]> signs = new ArrayList<>();
        for(WebSocketServer ws : wss) {
            signs.add(ws.getSign());
            pks.add(ws.getPk());
        }
        try {
            Buffer.DrawLeftNotification dln = deckDealer.drawLeftCards(pks, signs);
            WebSocketServer.sendInfo(dln.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 还牌
     *
     * @param
     */
    @Override
    public void returnCards(Buffer.ReturnRequest rr) {
        try {
            byte[] pk = rr.getPk().toByteArray();
            byte[] sign = rr.getSig().toByteArray();
            Buffer.EciesBody cc = rr.getCardsCipher();
//            List<ByteString> salts = transtion.getSaltList();
//            List<byte[]> cs = new ArrayList<>();
//            for(ByteString salt : salts) {
//                cs.add(salt.toByteArray());
//            }
            Object[] o = deckDealer.returnCards(pk, sign, cc);
            Buffer.ReturnResponse resp = (Buffer.ReturnResponse) o[0];
            Buffer.ReturnNotification notify = (Buffer.ReturnNotification) o[1];
            WebSocketServer.sendInfoSpecific(resp.toByteArray(),pk);
            WebSocketServer.sendInfo(notify.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
