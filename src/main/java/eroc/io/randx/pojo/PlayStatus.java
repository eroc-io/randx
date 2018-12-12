package eroc.io.randx.pojo;

import eroc.io.randx.controller.WebSocketServer;
import eroc.io.randx.service.DeckDealer;
import eroc.io.randx.utils.UUIDUtils;
import eroc.io.randx.utils.XMLReader;
import org.dom4j.DocumentException;

import java.io.IOException;
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
    public PlayStatus(Buffer.OpenRequest open) throws IOException, DocumentException {
        this.deckNo = open.getDeckNo();
        GameType gameType = XMLReader.getGameType(open.getCode());
        BasicParam basicParam = gameType.getBasicParam();
        this.numCards = basicParam.getNumCards();
        this.numDecks = basicParam.getNumDecks();
        this.numPlayers = basicParam.getNumPlayers();
        this.rounds = basicParam.getRounds();
        this.deckId = UUIDUtils.getUUID();
        this.seatSort = new byte[numPlayers][];
        OtherParam otherParam = gameType.getOtherParam();
        this.drawLeftCardsNum = null != otherParam.getDrawLeftCardsNum() ? otherParam.getDrawLeftCardsNum() : null;
        this.returnCardsNum = null != otherParam.getReturnCardsNum() ? otherParam.getReturnCardsNum() : null;
    }


    private Integer deckNo; // 桌号

    private String deckId;  // id

    private DeckDealer deckDealer;  // 牌局

    private Integer numCards;   // 一副牌的数量

    private Integer numDecks;   // 几副牌

    private Integer numPlayers;     // 玩家数

    private Integer rounds;     // 抽几轮牌

    private Integer drawLeftCardsNum;   // 每次查看剩余牌的数量

    private Integer returnCardsNum;     // 还牌的数量

    private List<Player> players = new ArrayList<>();   // 本桌玩家

    private List<String> index = new ArrayList<>(); // 抽牌顺序

    private byte[] dsk;//私钥

    private byte[] dpk;//公钥

    private List<WebSocketServer> wss = new ArrayList<>();//缓存玩家抽牌请求

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

    public Integer getDrawLeftCardsNum() {
        return drawLeftCardsNum;
    }

    public void setDrawLeftCardsNum(Integer drawLeftCardsNum) {
        this.drawLeftCardsNum = drawLeftCardsNum;
    }

    public Integer getReturnCardsNum() {
        return returnCardsNum;
    }

    public void setReturnCardsNum(Integer returnCardsNum) {
        this.returnCardsNum = returnCardsNum;
    }
}

