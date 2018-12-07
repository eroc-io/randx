package eroc.io.randx.pojo;

import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.service.DeckDealer;
import eroc.io.randx.utils.UUIDUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 牌局状态信息
 */
public class PlayStatus {

    /**
     * 开桌
     *
     * @param open
     */
    public PlayStatus(Buffer.OpenRequest open) {
        this.deckNo = open.getDeckNo();
        this.numCards = open.getNumCards();
        this.numDecks = open.getNumDecks();
        this.numPlayers = open.getNumPlayers();
        this.rounds = open.getRounds();
        this.deckId = UUIDUtils.getUUID();
        this.seatSort = new byte[numPlayers][];
    }


    private Integer deckNo;//桌号

    private String deckId;//id

    private DeckDealer deckDealer;//牌局

    private Integer numCards;//一副牌的数量

    private Integer numDecks;//几副牌

    private Integer numPlayers;//玩家数

    private Integer rounds;//抽几轮牌

    private List<Player> players = new ArrayList<>();//本桌玩家

    private List<String> index = new ArrayList<>();//抽牌顺序

    private List<byte[]> signs = new ArrayList<>();//还牌

    private byte[] dsk;//私钥

    private byte[] dpk;//公钥

    private List<WebSocketServer> wss = new ArrayList<>();//缓存区

    private List<Buffer.JoinNotification> joinNotifyBuilder = new ArrayList<>();//已加入玩家信息

    private byte[][] seatSort;//玩家座位顺序,pk:format之后的


    public byte[][] getSeatSort() {
        return seatSort;
    }

    public void setSeatSort(byte[][] seatSort) {
        this.seatSort = seatSort;
    }

    public Integer getDeckNo() {
        return deckNo;
    }

    public void setDeckNo(Integer deckNo) {
        this.deckNo = deckNo;
    }

    public DeckDealer getDeckDealer() {
        return deckDealer;
    }

    public void setDeckDealer(DeckDealer deckDealer) {
        this.deckDealer = deckDealer;
    }

    public String getDeckId() {
        return deckId;
    }

    public void setDeckId(String deckId) {
        this.deckId = deckId;
    }

    public Integer getNumCards() {
        return numCards;
    }

    public void setNumCards(Integer numCards) {
        this.numCards = numCards;
    }

    public Integer getNumDecks() {
        return numDecks;
    }

    public void setNumDecks(Integer numDecks) {
        this.numDecks = numDecks;
    }

    public Integer getNumPlayers() {
        return numPlayers;
    }

    public void setNumPlayers(Integer numPlayers) {
        this.numPlayers = numPlayers;
    }

    public Integer getRounds() {
        return rounds;
    }

    public void setRounds(Integer rounds) {
        this.rounds = rounds;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<String> getIndex() {
        return index;
    }

    public void setIndex(List<String> index) {
        this.index = index;
    }

    public List<byte[]> getSigns() {
        return signs;
    }

    public void setSigns(List<byte[]> signs) {
        this.signs = signs;
    }

    public byte[] getDsk() {
        return dsk;
    }

    public void setDsk(byte[] dsk) {
        this.dsk = dsk;
    }

    public byte[] getDpk() {
        return dpk;
    }

    public void setDpk(byte[] dpk) {
        this.dpk = dpk;
    }

    public List<WebSocketServer> getWss() {
        return wss;
    }

    public void setWss(List<WebSocketServer> wss) {
        this.wss = wss;
    }

    public List<Buffer.JoinNotification> getJoinNotifyBuilder() {
        return joinNotifyBuilder;
    }

    public void setJoinNotifyBuilder(List<Buffer.JoinNotification> joinNotifyBuilder) {
        this.joinNotifyBuilder = joinNotifyBuilder;
    }
}
