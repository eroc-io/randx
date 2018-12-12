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
import eroc.io.randx.utils.SHA256;
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

    // All hand information
    private static List<PlayStatus> pss = new ArrayList<>();


    /**
     * Get all the table information
     *
     * @param wss The currently established websocket request, if null, is sent to all players
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
     * Open the table
     *
     * @param msg Buffer.OpenRequest
     * @return Table deckId
     */
    @Override
    public Buffer.OpenResponse initPlay(byte[] msg) {
        Buffer.OpenResponse.Builder oresp = Buffer.OpenResponse.newBuilder();
        try {
            Buffer.OpenRequest open = Buffer.OpenRequest.parseFrom(msg);
            int deckNo = open.getDeckNo();
            for(PlayStatus ps : pss) {
                // If you have already opened the table and return directly to the deckId
                if (ps.getDeckNo() == deckNo) {
                    return oresp.setDeckId(ByteString.copyFrom(ps.getDeckId().getBytes())).build();
                }
            }

            // Open the table
            PlayStatus playStatus = new PlayStatus(open);
            playStatus.setDeckDealer(new DeckDealer());
            pss.add(playStatus);
            oresp.setDeckId(ByteString.copyFrom(playStatus.getDeckId().getBytes()));
        } catch (InvalidProtocolBufferException e) {
            oresp.setErrMsg(Error.getMsg(90000));
            e.printStackTrace();
        }
        return oresp.build();
    }

    /**
     * Join the game and start the game when the number of players reaches the upper limit
     *
     * @param msg Buffer.JoinRequest
     * @param wss The currently requested websocket
     * @return Buffer.StartResponse
     */
    public synchronized Buffer.StartResponse joinGame(byte[] msg, WebSocketServer wss) {
        Buffer.StartResponse.Builder jresp = Buffer.StartResponse.newBuilder();
        try {
            Buffer.JoinRequest jmsg = Buffer.JoinRequest.parseFrom(msg);
            String deckId = jmsg.getDeckId().toString("utf-8");
            for(PlayStatus ps : pss) {
                // Find the current hand
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    List<Player> players = ps.getPlayers();
                    Integer nump = ps.getNumPlayers();
                    String uid = wss.getUid();

                    // Insufficient number of players ,join game
                    if (nump > players.size()) {
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
                        player.setUid(uid);// set uid
                        player.setSeat(i);// seat number
                        players.add(player);
                        int empty = nump - players.size();// available seat

                        // Notify the current player to join the message
                        List<Buffer.JoinNotification> js = ps.getJoinNotifyBuilder();
                        Buffer.JoinResponse joinResponse = Buffer.JoinResponse.newBuilder().setNumber(i).setEmptySeat(empty).addAllJoinNotify(js).build();
                        WebSocketServer.sendInfo(TypeUtils.getMsg(joinResponse.toByteArray(), (byte) 7), uid);

                        // Notify other players of the player's joining information
                        Buffer.JoinNotification.Builder joinNotification = Buffer.JoinNotification.newBuilder().setJoinSeat(i).setJoinpk(ByteString.copyFrom(TypeUtils.bufferPk(pk)));
                        js.add(joinNotification.build());
                        byte[] m = TypeUtils.getMsg(joinNotification.setEmptySeat(empty).build().toByteArray(), (byte) 8);
                        for(Player p : players) {
                            String oid = p.getUid();
                            if (!oid.equalsIgnoreCase(uid)) {
                                WebSocketServer.sendInfo(m, oid);
                            }
                        }
                        // Notice to the owner of the hall to update this table information
                        sendHallMessage(null);
                    }


                    // The player limit has been reached and the game is started.
                    if (nump == players.size()) {
                        DeckDealer deckDealer = ps.getDeckDealer();
                        Object[] o = deckDealer.resetOrStart();
                        ps.setDsk((byte[]) o[0]);
                        ps.setDpk((byte[]) o[1]);
                        byte[][] pks = new byte[nump][];
                        for(int i = 0; i < nump; i++) {
                            pks[i] = players.get(i).getPk();
                        }
                        Object[] obj = deckDealer.openGame(ps.getNumCards(), ps.getNumDecks(), pks);
                        // 设置抽牌顺序
                        List<String> index = ps.getIndex();
                        for(Integer i = 0; i < ps.getRounds(); i++) {
                            for(Player player : players) {
                                index.add(player.getUid());
                            }
                        }
                        jresp.setSalt(ByteString.copyFrom((byte[]) obj[0])).setDpk(ByteString.copyFrom((byte[]) obj[1]));
                        byte[] m = TypeUtils.getMsg(jresp.build().toByteArray(), (byte) 1);
                        for(Player player : players) {
                            WebSocketServer.sendInfo(m, player.getUid());// 发送开始游戏信息
                        }
                    } else {
                        jresp.setErrMsg(Error.getMsg(10001));
                    }
                } else {
                    throw new Exception("未找到该牌桌");
                }

            }

        } catch (InvalidProtocolBufferException e) {
            jresp.setErrMsg(Error.getMsg(90000));
        } catch (Exception e) {
            jresp.setErrMsg(e.getMessage());
            e.printStackTrace();
        }
        return jresp.build();
    }


    /**
     * 斗地主抽牌,抽17轮，剩3张
     */
    public synchronized Buffer.DrawResponse drawCard(byte[] dreq, WebSocketServer wss) {
        Buffer.DrawResponse.Builder dresp = Buffer.DrawResponse.newBuilder();
        try {
            Buffer.DrawRequest dr = Buffer.DrawRequest.parseFrom(dreq);
            String deckId = dr.getDeckId().toString("utf-8");
            for(PlayStatus ps : pss) {
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    List<WebSocketServer> wsl = ps.getWss();
                    // 缓存抽牌请求
                    if (null == wsl || !wsl.contains(wss)) {
                        wss.setDrawRequest(dreq);
                        wsl.add(wss);
                    }
                    // 判断人数是否凑齐
                    if (ps.getPlayers().size() != ps.getNumPlayers()) {
                        Buffer.DrawResponse build = dresp.setErrMsg(Error.getMsg(10004)).build();
                        WebSocketServer.sendInfo(TypeUtils.getMsg(build.toByteArray(), (byte) 2), wss.getUid());
                        return null;
                    }
                    // 当前牌局
                    List<String> index = ps.getIndex();
                    if (index.size() <= 0) {
                        Buffer.DrawResponse build = dresp.setErrMsg(Error.getMsg(10003)).build();
                        for(Player player : ps.getPlayers()) {
                            WebSocketServer.sendInfo(TypeUtils.getMsg(build.toByteArray(), (byte) 2), player.getUid());
                        }
                        return null;
                    }
                    // 轮到哪位玩家抽牌
                    String inUid = index.get(0);
                    for(WebSocketServer ws : wsl) {
                        String uid = ws.getUid();
                        if (uid.equalsIgnoreCase(inUid)) {
                            byte[] drawRequest = ws.getDrawRequest();
                            Buffer.DrawRequest drawReq = Buffer.DrawRequest.parseFrom(drawRequest);
                            // 抽牌
                            DeckDealer deckDealer = ps.getDeckDealer();
                            byte[] sign = CryptoUtils.rsGenSign(drawReq.getSig().toByteArray());
                            byte[] pk = TypeUtils.formatPK(drawReq.getPk().toByteArray());
                            Object[] obj = deckDealer.drawCard(pk, sign);
                            Buffer.DrawResponse.Builder card = (Buffer.DrawResponse.Builder) obj[0];
                            Buffer.DrawNotification.Builder notify = (Buffer.DrawNotification.Builder) obj[1];
                            Buffer.DrawResponse c = card.build();
                            Buffer.DrawNotification n = notify.build();
                            // 存入数据库
                            Proofs proofs = new Proofs();
                            proofs.setProof((String) obj[3]);
                            proofs.setDeckId(deckId);
                            proofs.setPk(Base64.getEncoder().encodeToString(pk));
                            this.proofsDao.insertSelective(proofs);
                            WebSocketServer.sendInfo(TypeUtils.getMsg(c.toByteArray(), (byte) 2), uid);
                            index.remove(0);
                            wsl.remove(ws);
                            byte[] msg = TypeUtils.getMsg(n.toByteArray(), (byte) 3);
                            // 获取其他用户id
                            for(Player player : ps.getPlayers()) {
                                String oid = player.getUid();
                                if (!oid.equalsIgnoreCase(uid)) {
                                    WebSocketServer.sendInfo(msg, oid);
                                } else {
                                    // 存储玩家最新盐
                                    player.getSalt().add((byte[]) obj[2]);
                                }
                            }
                            break;
                        }
                    }
                } else {
                    throw new Exception("未找到该牌桌");
                }
            }
        } catch (InvalidProtocolBufferException e) {
            dresp.setErrMsg(Error.getMsg(90000));
            e.printStackTrace();
        } catch (Exception e) {
            dresp.setErrMsg(e.getMessage());
            e.printStackTrace();
        }
        return dresp.build();
    }


    /**
     * 抽取剩余牌
     *
     * @param
     */
    public synchronized Buffer.DrawLeftNotification drawLeftCards(byte[] dleftReq, WebSocketServer wss) {
        Buffer.DrawLeftNotification.Builder lresp = Buffer.DrawLeftNotification.newBuilder();
        try {
            Buffer.DrawLeftRequest dl = Buffer.DrawLeftRequest.parseFrom(dleftReq);
            String deckId = dl.getDeckId().toString("utf-8");
            for(PlayStatus ps : pss) {
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    List<WebSocketServer> wssl = ps.getWss();
                    Integer nump = ps.getNumPlayers();
                    byte[] sign = CryptoUtils.rsGenSign(dl.getSig().toByteArray());
                    if (wssl.size() < nump) {
                        if (!wssl.contains(wss)) {
                            wssl.add(wss);
                        }
                        // 存储玩家pk和签名
                        byte[] leftpk = TypeUtils.formatPK(dl.getPk().toByteArray());
                        for(Player player : ps.getPlayers()) {
                            if (Arrays.equals(player.getPk(), leftpk)) {
                                player.setSign(sign);
                                player.setPk(leftpk);
                            }
                        }
                    }
                    if (wssl.size() == nump) {
                        // 抽剩余牌
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
                        byte[] msg = TypeUtils.getMsg(deckDealer.drawLeftCards(pks, ss, null).build().toByteArray(), (byte) 4);
                        for(Player player : players) {
                            WebSocketServer.sendInfo(msg, player.getUid());
                        }
                        wssl.clear();
                    }
                } else {
                    throw new Exception("未找到该牌桌");
                }
            }
        } catch (InvalidProtocolBufferException e) {
            lresp.setErrMsg(Error.getMsg(90000));
            e.printStackTrace();
        } catch (Exception e) {
            lresp.setErrMsg(e.getMessage());
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
        Buffer.ReturnResponse.Builder rresp = Buffer.ReturnResponse.newBuilder();
        try {
            Buffer.ReturnRequest rr = Buffer.ReturnRequest.parseFrom(returnReq);
            String deckId = rr.getDeckId().toString("utf-8");
            for(PlayStatus ps : pss) {
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    // 还牌
                    Buffer.EciesBody ec = rr.getCardsCipher();
                    byte[] cards = CryptoUtils.ECDHDecrypt(ps.getDsk(), ec);
                    int l = cards.length;
                    if (l % 32 == 0 && l != 0) {
                        // 取出牌信息
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
                        // 通知其他人还牌信息
                        for(Player player : ps.getPlayers()) {
                            String oid = player.getUid();
                            if (!oid.equalsIgnoreCase(uid)) {
                                WebSocketServer.sendInfo(TypeUtils.getMsg(notify.toByteArray(), (byte) 6), oid);
                            }
                        }
                    } else {
                        rresp.setErrMsg(Error.getMsg(10002));
                    }
                } else {
                    throw new Exception("未找到该牌桌");
                }
            }
        } catch (InvalidProtocolBufferException e) {
            rresp.setErrMsg(Error.getMsg(90000));
            e.printStackTrace();
        } catch (Exception e) {
            rresp.setErrMsg(e.getMessage());
            e.printStackTrace();
        }
        return rresp.build();
    }


    /**
     * 出牌
     *
     * @param disCards
     */
    public void disCard(byte[] disCards, WebSocketServer wss) {
        Buffer.DisCard.Builder rresp = Buffer.DisCard.newBuilder();
        try {
            Buffer.DisCard dc = Buffer.DisCard.parseFrom(disCards);
            String deckId = dc.getDeckId().toString("utf-8");
            for(PlayStatus ps : pss) {
                if (ps.getDeckId().equalsIgnoreCase(deckId)) {
                    ByteString ppk = dc.getPk();
                    String pk = Base64.getEncoder().encodeToString(TypeUtils.formatPK(ppk.toByteArray()));
                    ByteString salts = dc.getSalt();
                    byte[] cards = salts.toByteArray();
                    int l = cards.length;
                    if (l % 32 == 0 && l != 0) {
                        // 取出牌信息
                        List<byte[]> cs = new ArrayList<>();
                        byte[] card = new byte[32];
                        int n = l / 32;
                        for(int i = 0; i < n; i++) {
                            System.arraycopy(cards, i * 32, card, 0, 32);
                            cs.add(card);
                        }
                        boolean b = true;
                        for(byte[] c : cs) {
                            Proofs proof = new Proofs();
                            proof.setDeckId(deckId);
                            proof.setPk(pk);
                            proof.setProof(Base64.getEncoder().encodeToString(SHA256.getSHA256Bytes(c)));
                            List<Proofs> select = this.proofsDao.select(proof);
                            if (null == select || 0 == select.size()) {
                                b = false;
                            }
                        }
                        if (b) {
                            // 通知其他人出牌信息
                            String uid = wss.getUid();
                            Buffer.DisCardsNotify notify = Buffer.DisCardsNotify.newBuilder().setPk(ppk).setSalt(salts).build();
                            for(Player player : ps.getPlayers()) {
                                String oid = player.getUid();
                                if (!oid.equalsIgnoreCase(uid)) {
                                    WebSocketServer.sendInfo(TypeUtils.getMsg(notify.toByteArray(), (byte) 10), oid);
                                }
                            }
                        } else {
                            throw new Exception("抽牌证明验证未通过");
                        }
                    } else {
                        throw new Exception("出牌salts不对应");
                    }
                } else {
                    throw new Exception("未找到该牌桌");
                }
            }
        } catch (InvalidProtocolBufferException e) {
//            rresp.setErrMsg(Error.getMsg(90000));
            e.printStackTrace();
        } catch (Exception e) {
//            rresp.setErrMsg(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 玩家退出桌局
     *
     * @param wss
     */
    public synchronized void leavePlay(WebSocketServer wss) {
        for(PlayStatus ps : pss) {
            List<WebSocketServer> wss1 = ps.getWss();
            if (wss1.contains(wss)) {
                wss1.remove(wss);
            }
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
