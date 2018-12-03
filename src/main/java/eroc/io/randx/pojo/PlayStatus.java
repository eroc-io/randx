package eroc.io.randx.pojo;

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
    }


    private Integer deckNo;//桌号

    private String deckId;//id

    private DeckDealer deckDealer;//牌局

    private Integer numCards;//一副牌的数量

    private Integer numDecks;//几副牌

    private Integer numPlayers;//玩家数

    private Integer rounds;//抽几轮牌

    private List<Player> players = new ArrayList<>();//本桌玩家

//    private static Integer r = 0;

    private List<String> index = new ArrayList<>();








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
}
