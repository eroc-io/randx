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
                        Object[] o = deckDealer.resetOrStart();
                        ps.setDsk((byte[]) o[0]);
                        ps.setDpk((byte[]) o[1]);
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
            errmsg = e.getMessage();
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
                    if (wss.getUid().equalsIgnoreCase(inUid)) {
                        //抽牌
                        DeckDealer deckDealer = ps.getDeckDealer();
                        byte[] sign = CryptoUtils.rsGnSign(dr.getSig().toByteArray());
                        byte[] pk = CryptoUtils.formatPK(dr.getPk().toByteArray());
                        Object[] obj = deckDealer.drawCard(pk, sign);
                        Buffer.DrawResponse.Builder card = (Buffer.DrawResponse.Builder) obj[0];
                        Buffer.DrawNotification.Builder notify = (Buffer.DrawNotification.Builder) obj[1];
                        WebSocketServer.sendInfo(TypeUtils.getMsg(card.build().toByteArray(), (byte) 2), uid);
                        index.remove(0);
                        //获取其他用户id
                        for(Player player : ps.getPlayers()) {
                            String oid = player.getUid();
                            if (!oid.equalsIgnoreCase(uid)) {
                                WebSocketServer.sendInfo(TypeUtils.getMsg(notify.build().toByteArray(), (byte) 3), oid);
                            }
                        }
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            errmsg = Error.getMsg(90000);
            dresp.setErrMsg(errmsg);
//            e.printStackTrace();
        } catch (Exception e) {
            errmsg = e.getMessage();
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
    public synchronized Buffer.DrawLeftNotification drawLeftCards(byte[] dleftReq, WebSocketServer wss) {
        String errmsg;
        Buffer.DrawLeftNotification.Builder lresp = Buffer.DrawLeftNotification.newBuilder();
        try {
            Buffer.DrawLeftRequest dl = Buffer.DrawLeftRequest.parseFrom(dleftReq);
            String deckId = dl.getDeckId().toString();
            for(PlayStatus ps : pss) {
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    List<byte[]> signs = ps.getSigns();
                    Integer nump = ps.getNumPlayers();
                    byte[] sign = CryptoUtils.rsGnSign(dl.getSig().toByteArray());
                    if (signs.size() < nump) {
                        signs.add(sign);
                        //存储sign
                        for(Player player : ps.getPlayers()) {
                            if (player.getUid().equalsIgnoreCase(wss.getUid())) {
                                player.setPk(CryptoUtils.formatPK(dl.getPk().toByteArray()));
                                player.setSign(sign);
                            }
                        }
                    }
                    if (signs.size() == nump) {
                        //抽剩余牌
                        DeckDealer deckDealer = ps.getDeckDealer();
                        List<byte[]> pks = new ArrayList<>();
                        List<byte[]> ss = new ArrayList<>();
                        List<Player> players = ps.getPlayers();
                        for(Player player : players) {
                            pks.add(player.getPk());
                            ss.add(player.getSign());
                        }
                        Buffer.DrawLeftNotification.Builder dln = deckDealer.drawLeftCards(pks, ss);
                        for(Player player : players) {
                            WebSocketServer.sendInfo(TypeUtils.getMsg(dln.build().toByteArray(), (byte) 4), player.getUid());
                        }
                        signs.clear();
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            errmsg = Error.getMsg(90000);
            lresp.setErrMsg(errmsg);
//            e.printStackTrace();
        } catch (Exception e) {
            errmsg = e.getMessage();
            lresp.setErrMsg(errmsg);
//            e.printStackTrace();
        }
        return lresp.build();
    }


    /**
     * 还牌
     *
     * @param
     */
    @Override
    public Buffer.ReturnResponse returnCards(byte[] returnReq, WebSocketServer wss) {
        String errmsg;
        Buffer.ReturnResponse.Builder rresp = Buffer.ReturnResponse.newBuilder();
        try {
            Buffer.ReturnRequest rr = Buffer.ReturnRequest.parseFrom(returnReq);
            String deckId = rr.getDeckId().toString();
            for(PlayStatus ps : pss) {
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    //还牌
                    Buffer.EciesBody ec = rr.getCardsCipher();
                    byte[] cards = CryptoUtils.ECDHDecrypt(ps.getDsk(), ec);
                    int l = cards.length;
                    if (l % 32 == 0 && l != 0) {
                        //取出牌信息
                        List<byte[]> cs = new ArrayList<>();
                        byte[] card = new byte[32];
                        int n = l / 32;
                        for(int i = 0; i < n; i++) {
                            System.arraycopy(cards, i * 32, card, 0, 32);
                            cs.add(card);
                        }
                        DeckDealer deckDealer = ps.getDeckDealer();
                        byte[] pk = CryptoUtils.formatPK(rr.getPk().toByteArray());
                        byte[] sign = CryptoUtils.rsGnSign(rr.getSig().toByteArray());
                        Object[] obj = deckDealer.returnCards(pk, sign, cs);
                        Buffer.ReturnResponse resp = (Buffer.ReturnResponse) obj[0];
                        Buffer.ReturnNotification notify = (Buffer.ReturnNotification) obj[1];
                        String uid = wss.getUid();
                        WebSocketServer.sendInfo(TypeUtils.getMsg(resp.toByteArray(), (byte) 5), uid);
                        //通知其他人还牌信息
                        for(Player player : ps.getPlayers()) {
                            String oid = player.getUid();
                            if (!oid.equalsIgnoreCase(uid)) {
                                WebSocketServer.sendInfo(TypeUtils.getMsg(notify.toByteArray(), (byte) 6), oid);
                            }
                        }
                    } else {
                        errmsg = "还牌信息有误，请检查后重新操作";
                        rresp.setErrMsg(errmsg);
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            errmsg = Error.getMsg(90000);
            rresp.setErrMsg(errmsg);
//            e.printStackTrace();
        } catch (Exception e) {
            errmsg = e.getMessage();
            rresp.setErrMsg(errmsg);
//            e.printStackTrace();
        }


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

        return rresp.build();

    }
}
