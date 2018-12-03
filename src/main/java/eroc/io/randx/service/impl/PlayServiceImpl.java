package eroc.io.randx.service.impl;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.dao.ProofsDao;
import eroc.io.randx.exception.Error;
import eroc.io.randx.pojo.Buffer;
import eroc.io.randx.pojo.PlayStatus;
import eroc.io.randx.pojo.Player;
import eroc.io.randx.service.DeckDealer;
import eroc.io.randx.service.PlayService;
import eroc.io.randx.utils.CryptoUtils;
import eroc.io.randx.utils.TypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class PlayServiceImpl implements PlayService {

    @Autowired
    private ProofsDao proofsDao;

    //所有牌局信息
    private static List<PlayStatus> pss = new ArrayList<>();


    /**
     * 开桌，一桌游戏只开桌一次
     */
    @Override
    public Buffer.OpenResponse initPlay(byte[] msg) {
        String errmsg;
        Buffer.OpenResponse.Builder oresp = Buffer.OpenResponse.newBuilder();
        try {
            Buffer.OpenRequest open = Buffer.OpenRequest.parseFrom(msg);
            int deckNo = open.getDeckNo();
            boolean op = true;
            for(PlayStatus ps : pss) {//判断是否开桌
                if (ps.getDeckNo() == deckNo) {
                    op = false;
                }
            }
            if (op) {
                PlayStatus playStatus = new PlayStatus(open);//开桌
                DeckDealer deckDealer = new DeckDealer();
                String deckId = playStatus.getDeckId();
                playStatus.setDeckDealer(deckDealer);
                pss.add(playStatus);
                oresp.setDeckId(ByteString.copyFrom(deckId.getBytes()));
            }
        } catch (InvalidProtocolBufferException e) {
            errmsg = Error.getMsg(90000);
            oresp.setErrMsg(errmsg);
//            e.printStackTrace();
        }
        return oresp.build();
    }

    /**
     * 加入游戏
     */
    public synchronized Buffer.JoinResponse joinGame(byte[] msg, WebSocketServer wss) {
        String errmsg;
        Buffer.JoinResponse.Builder jresp = Buffer.JoinResponse.newBuilder();
        try {
            Buffer.JoinRequest jmsg = Buffer.JoinRequest.parseFrom(msg);
            String deckId = jmsg.getDeckId().toString();//牌桌id
            for(PlayStatus ps : pss) {//当前牌局
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    List<Player> players = ps.getPlayers();
                    Integer nump = ps.getNumPlayers();
                    if (nump > players.size()) {
                        //加入游戏
                        Player player = new Player();
                        player.setPk(CryptoUtils.formatPK(jmsg.getPk().toByteArray()));
                        player.setUid(wss.getUid());//加入uid
                        players.add(player);
                    }
                    if (nump == players.size()) {
                        //开始游戏
                        DeckDealer deckDealer = ps.getDeckDealer();
                        deckDealer.resetOrStart();
                        List<byte[]> pks = new ArrayList<>();
                        for(Player player : players) {
                            pks.add(player.getPk());
                        }
                        Object[] obj = deckDealer.openGame(ps.getNumCards(), ps.getNumDecks(), pks);
                        //设置抽牌顺序
                        List<String> index = ps.getIndex();
                        for(Integer i = 0; i < ps.getRounds(); i++) {
                            for(Player player : players) {
                                index.add(player.getUid());
                            }
                        }
                        jresp.setSalt(ByteString.copyFrom((byte[]) obj[0])).setDpk(ByteString.copyFrom((byte[]) obj[1]));
                        for(Player player : players) {
                            WebSocketServer.sendInfo(TypeUtils.getMsg(jresp.build().toByteArray(), (byte) 1), player.getUid());//发送开始游戏信息
                        }
                    } else {
                        jresp.setErrMsg(Error.getMsg(10001));
                    }
                }
            }

        } catch (InvalidProtocolBufferException e) {
            errmsg = Error.getMsg(90000);
            jresp.setErrMsg(errmsg);
//            e.printStackTrace();
        } catch (Exception e) {
            errmsg = Error.getMsg(10000);
            jresp.setErrMsg(errmsg);
//            e.printStackTrace();
        }
        return jresp.build();
    }

    /**
     * 斗地主抽牌,抽17轮，剩3张
     */
    public synchronized Buffer.DrawResponse drawCard(byte[] dreq, WebSocketServer wss) {
        String errmsg;
        Buffer.DrawResponse.Builder dresp = Buffer.DrawResponse.newBuilder();
        try {
            Buffer.DrawRequest dr = Buffer.DrawRequest.parseFrom(dreq);
            String deckId = dr.getDeckId().toString();
            for(PlayStatus ps : pss) {
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    //当前牌局
                    List<String> index = ps.getIndex();
                    if (index.size() <= 0) {
                        dresp.setErrMsg("抽牌结束");
                    }
                    //轮到哪位玩家抽牌
                    String inUid = index.get(0);
                    String uid = wss.getUid();
//                    String hexPk = TypeUtils.bytesToHexString(pk);
                    if (wss.getUid().equalsIgnoreCase(inUid)) {
                        //抽牌
                        List<String> otherId = new ArrayList<>();
                        DeckDealer deckDealer = ps.getDeckDealer();
                        byte[] sign = CryptoUtils.rsGnSign(dr.getSig().toByteArray());
                        byte[] pk = CryptoUtils.formatPK(dr.getPk().toByteArray());
                        Object[] obj = deckDealer.drawCard(pk, sign);
                        Buffer.DrawResponse.Builder card = (Buffer.DrawResponse.Builder) obj[0];
                        Buffer.DrawNotification.Builder notify = (Buffer.DrawNotification.Builder) obj[1];
                        //获取其他用户id
                        for(Player player : ps.getPlayers()) {
                            if (!player.getUid().equalsIgnoreCase(uid)) {
                                otherId.add(player.getUid());
                            }
                        }
                        WebSocketServer.sendInfo(TypeUtils.getMsg(card.build().toByteArray(), (byte) 2), uid);
                        index.remove(0);
                        for(String oid : otherId) {
                            WebSocketServer.sendInfo(TypeUtils.getMsg(notify.build().toByteArray(), (byte) 3), oid);
                        }
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            errmsg = Error.getMsg(90000);
            dresp.setErrMsg(errmsg);
//            e.printStackTrace();
        } catch (Exception e) {
            errmsg = Error.getMsg(10000);
            dresp.setErrMsg(errmsg);
//            e.printStackTrace();
        }
        return dresp.build();
    }


    /**
     * 抽取剩余牌
     *
     * @param
     */
    public synchronized void drawLeftCards(CopyOnWriteArraySet<WebSocketServer> wss) {



//        List<byte[]> pks = new ArrayList<>();
//        List<byte[]> signs = new ArrayList<>();
//        for(WebSocketServer ws : wss) {
//            signs.add(ws.getSign());
//            pks.add(ws.getPk());
//        }
//        try {
//            Buffer.DrawLeftNotification dln = deckDealer.drawLeftCards(pks, signs);
//            WebSocketServer.sendInfo(dln.toByteArray());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    /**
     * 还牌
     *
     * @param
     */
    @Override
    public void returnCards(Buffer.ReturnRequest rr) {
//        try {
//            byte[] pk = rr.getPk().toByteArray();
//            byte[] sign = rr.getSig().toByteArray();
//            Buffer.EciesBody cc = rr.getCardsCipher();
////            List<ByteString> salts = transtion.getSaltList();
////            List<byte[]> cs = new ArrayList<>();
////            for(ByteString salt : salts) {
////                cs.add(salt.toByteArray());
////            }
//            Object[] o = deckDealer.returnCards(pk, sign, cc);
//            Buffer.ReturnResponse resp = (Buffer.ReturnResponse) o[0];
//            Buffer.ReturnNotification notify = (Buffer.ReturnNotification) o[1];
//            WebSocketServer.sendInfoSpecific(resp.toByteArray(), pk);
//            WebSocketServer.sendInfo(notify.toByteArray());
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }
}
