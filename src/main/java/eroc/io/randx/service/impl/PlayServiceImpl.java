package eroc.io.randx.service.impl;

import com.google.common.primitives.Bytes;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.dao.ProofsDao;
import eroc.io.randx.exception.Error;
import eroc.io.randx.pojo.Buffer;
import eroc.io.randx.pojo.PlayStatus;
import eroc.io.randx.pojo.Player;
import eroc.io.randx.pojo.Proofs;
import eroc.io.randx.service.DeckDealer;
import eroc.io.randx.service.PlayService;
import eroc.io.randx.utils.CryptoUtils;
import eroc.io.randx.utils.TypeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
public class PlayServiceImpl implements PlayService {

    @Autowired
    private ProofsDao proofsDao;

    //所有牌局信息
    private static List<PlayStatus> pss = new ArrayList<>();


    /**
     * 获取所有牌桌信息
     *
     * @param wss
     */
    @Override
    public void sendHallMessage(WebSocketServer wss) {
        if (0 == pss.size()) {
            return;
        }
        Buffer.HallResponse.Builder hall = Buffer.HallResponse.newBuilder();
        for(PlayStatus playStatus : pss) {
            Buffer.DeckMsg.Builder deck = Buffer.DeckMsg.newBuilder();
            deck.setDeckNo(playStatus.getDeckNo());
            byte[][] seatSort = playStatus.getSeatSort();
            int emptyNum = 0;
            List<Byte> seats = new ArrayList<>();
            int l = seatSort.length;
            for(int i = 0; i < l; i++) {
                if (null == seatSort[i]) {
                    emptyNum++;
                } else {
                    seats.add((byte) (i + 1));
                }
            }
            deck.setEmptyNum(emptyNum);
            deck.setSeat(ByteString.copyFrom(Bytes.toArray(seats)));
            hall.addDeck(deck.build());
        }
        byte[] msg = TypeUtils.getMsg(hall.build().toByteArray(), (byte) 9);
        try {
            if (wss != null) {
                WebSocketServer.sendInfo(msg, wss.getUid());
            } else {
                WebSocketServer.sendInfo(msg, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开桌
     */
    @Override
    public Buffer.OpenResponse initPlay(byte[] msg) {
        String errmsg = null;
        Buffer.OpenResponse.Builder oresp = Buffer.OpenResponse.newBuilder();
        try {
            Buffer.OpenRequest open = Buffer.OpenRequest.parseFrom(msg);
            int deckNo = open.getDeckNo();
            for(PlayStatus ps : pss) {//判断是否开桌
                if (ps.getDeckNo() == deckNo) {
                    return oresp.setDeckId(ByteString.copyFrom(ps.getDeckId().getBytes())).build();
                }
            }
            PlayStatus playStatus = new PlayStatus(open);//开桌
            playStatus.setDeckDealer(new DeckDealer());
            pss.add(playStatus);
            oresp.setDeckId(ByteString.copyFrom(playStatus.getDeckId().getBytes()));
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
    public synchronized Buffer.StartResponse joinGame(byte[] msg, WebSocketServer wss) {
        String errmsg;
        Buffer.StartResponse.Builder jresp = Buffer.StartResponse.newBuilder();
        try {
            Buffer.JoinRequest jmsg = Buffer.JoinRequest.parseFrom(msg);
            String deckId = jmsg.getDeckId().toString("utf-8");//牌桌id
            for(PlayStatus ps : pss) {
                //当前牌局
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    List<Player> players = ps.getPlayers();
                    Integer nump = ps.getNumPlayers();
                    String uid = wss.getUid();
                    int psize = players.size();
                    if (nump > psize) {
                        //加入游戏
                        Player player = new Player();
                        byte[] pk = TypeUtils.formatPK(jmsg.getPk().toByteArray());
                        byte[][] seatSort = ps.getSeatSort();
                        int i, l = seatSort.length;
                        for(i = 1; i <= l; i++) {
                            if (null == seatSort[i - 1]) {
                                seatSort[i - 1] = pk;
                                break;
                            }
                        }
                        player.setPk(pk);
                        player.setUid(uid);//加入uid
                        player.setSeat(i);//座位号
                        players.add(player);
                        int empty = nump - players.size();//空座数
                        //通知当前玩家
                        List<Buffer.JoinNotification> js = ps.getJoinNotifyBuilder();
                        Buffer.JoinResponse joinResponse = Buffer.JoinResponse.newBuilder().setNumber(i).setEmptySeat(empty).addAllJoinNotify(js).build();
                        WebSocketServer.sendInfo(TypeUtils.getMsg(joinResponse.toByteArray(), (byte) 7), uid);
                        //通知其他玩家
                        Buffer.JoinNotification.Builder joinNotification = Buffer.JoinNotification.newBuilder().setJoinSeat(i).setJoinpk(ByteString.copyFrom(TypeUtils.bufferPk(pk)));
                        js.add(joinNotification.build());
                        byte[] m = TypeUtils.getMsg(joinNotification.setEmptySeat(empty).build().toByteArray(), (byte) 8);
                        for(Player p : players) {
                            String oid = p.getUid();
                            if (!oid.equalsIgnoreCase(uid)) {
                                WebSocketServer.sendInfo(m, oid);
                            }
                        }
                        sendHallMessage(null);
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
                        byte[] m = TypeUtils.getMsg(jresp.build().toByteArray(), (byte) 1);
                        for(Player player : players) {
                            WebSocketServer.sendInfo(m, player.getUid());//发送开始游戏信息
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
            String deckId = dr.getDeckId().toString("utf-8");
            for(PlayStatus ps : pss) {
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    List<WebSocketServer> wsl = ps.getWss();
                    //缓存抽牌请求
                    if (null == wsl || !wsl.contains(wss)) {
                        wss.setDrawRequest(dreq);
                        wsl.add(wss);
                    }
                    //当前牌局
                    List<String> index = ps.getIndex();
                    if (index.size() <= 0) {
                        Buffer.DrawResponse build = dresp.setErrMsg("抽牌结束").build();
                        for(Player player : ps.getPlayers()) {
                            WebSocketServer.sendInfo(TypeUtils.getMsg(build.toByteArray(), (byte) 2), player.getUid());
                        }
                        return null;
                    }
                    //轮到哪位玩家抽牌
                    String inUid = index.get(0);
                    for(WebSocketServer ws : wsl) {
                        String uid = ws.getUid();
                        if (uid.equalsIgnoreCase(inUid)) {
                            byte[] drawRequest = ws.getDrawRequest();
                            Buffer.DrawRequest drawReq = Buffer.DrawRequest.parseFrom(drawRequest);
                            //抽牌
                            DeckDealer deckDealer = ps.getDeckDealer();
                            byte[] sign = CryptoUtils.rsGenSign(drawReq.getSig().toByteArray());
                            byte[] pk = TypeUtils.formatPK(drawReq.getPk().toByteArray());
                            Object[] obj = deckDealer.drawCard(pk, sign);
                            Buffer.DrawResponse.Builder card = (Buffer.DrawResponse.Builder) obj[0];
                            Buffer.DrawNotification.Builder notify = (Buffer.DrawNotification.Builder) obj[1];
                            Buffer.DrawResponse c = card.build();
                            Buffer.DrawNotification n = notify.build();
                            //存入数据库
                            Proofs proofs = new Proofs();
                            proofs.setProof(n.getProof().toString("utf-8"));
                            proofs.setDeckId(deckId);
                            proofs.setPk(Base64.getEncoder().encodeToString(pk));
                            this.proofsDao.insertSelective(proofs);
                            WebSocketServer.sendInfo(TypeUtils.getMsg(c.toByteArray(), (byte) 2), uid);
                            index.remove(0);
                            wsl.remove(ws);
                            byte[] msg = TypeUtils.getMsg(n.toByteArray(), (byte) 3);
                            //获取其他用户id
                            for(Player player : ps.getPlayers()) {
                                String oid = player.getUid();
                                if (!oid.equalsIgnoreCase(uid)) {
                                    WebSocketServer.sendInfo(msg, oid);
                                } else {
                                    //缓存玩家抽牌信息
                                    player.getSalt().add((byte[]) obj[2]);
                                }
                            }
                            break;
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
            String deckId = dl.getDeckId().toString("utf-8");
            for(PlayStatus ps : pss) {
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    List<byte[]> signs = ps.getSigns();
                    Integer nump = ps.getNumPlayers();
                    byte[] sign = CryptoUtils.rsGenSign(dl.getSig().toByteArray());
                    if (signs.size() < nump) {
                        signs.add(sign);
                        //存储玩家pk和签名
                        byte[] leftpk = TypeUtils.formatPK(dl.getPk().toByteArray());
                        for(Player player : ps.getPlayers()) {
                            if (Arrays.equals(player.getPk(), leftpk)) {
                                player.setSign(sign);
                                player.setPk(leftpk);
                            }
                        }
                    }
                    if (signs.size() == nump) {
                        //抽剩余牌
                        DeckDealer deckDealer = ps.getDeckDealer();
                        byte[][] pks = new byte[nump][];
                        byte[][] ss = new byte[nump][];
                        List<Player> players = ps.getPlayers();
                        Integer seat;
                        for(Player player : players) {
                            seat = player.getSeat() - 1;
                            pks[seat] = player.getPk();
                            ss[seat] = player.getSign();
                        }
                        byte[] msg = TypeUtils.getMsg(deckDealer.drawLeftCards(pks, ss).build().toByteArray(), (byte) 4);
                        for(Player player : players) {
                            WebSocketServer.sendInfo(msg, player.getUid());
                        }
                        signs.clear();
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            errmsg = Error.getMsg(90000);
            lresp.setErrMsg(errmsg);
            e.printStackTrace();
        } catch (Exception e) {
            errmsg = e.getMessage();
            lresp.setErrMsg(errmsg);
            e.printStackTrace();
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
            String deckId = rr.getDeckId().toString("utf-8");
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
                        byte[] pk = TypeUtils.formatPK(rr.getPk().toByteArray());
                        byte[] sign = CryptoUtils.rsGenSign(rr.getSig().toByteArray());
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
                        errmsg = Error.getMsg(10002);
                        rresp.setErrMsg(errmsg);
                    }
                }
            }
        } catch (InvalidProtocolBufferException e) {
            errmsg = Error.getMsg(90000);
            rresp.setErrMsg(errmsg);
            e.printStackTrace();
        } catch (Exception e) {
            errmsg = e.getMessage();
            rresp.setErrMsg(errmsg);
            e.printStackTrace();
        }
        return rresp.build();
    }


//    public void verifyCards(byte[] pk, Buffer.EciesBody eciesBody) {
//
//    }

    /**
     * 玩家退出桌局
     *
     * @param wss
     */
    public synchronized void leavePlay(WebSocketServer wss) {
        for(PlayStatus ps : pss) {
            List<Player> players = ps.getPlayers();
            byte[][] seatSort = ps.getSeatSort();
            for(Player player : players) {
                if (player.getUid().equalsIgnoreCase(wss.getUid())) {
                    for(int i = 0; i < seatSort.length; i++) {
                        if (Arrays.equals(seatSort[i], player.getPk())) {
                            seatSort[i] = null;
                        }
                    }
                    players.remove(player);
                    sendHallMessage(null);
                    break;
                }
            }
        }
    }


}
