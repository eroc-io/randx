package eroc.io.randx.utils;

import eroc.io.randx.pojo.BasicParam;
import eroc.io.randx.pojo.GameType;
import eroc.io.randx.pojo.OtherParam;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class XMLReader {


    /**
     * 读取用户设置的xml文件
     *
     * @return document xml文档对象
     * @throws DocumentException
     * @throws IOException
     */
    public static Document readerXML(String name) throws DocumentException, IOException {
        SAXReader sr = new SAXReader();
        InputStream is = XMLReader.class.getClassLoader().getResourceAsStream(name);
        Document doc = sr.read(is);
        is.close();
        return doc;
    }


    /**
     * 解析xml文件,获取游戏类型
     *
     * @param code 游戏类型编号
     * @return GameType
     * @throws DocumentException
     * @throws IOException
     */
    public static GameType getGameType(String code) throws DocumentException, IOException {
        //解析record节点
        List<Element> games = readerXML("gameType.xml").getRootElement().elements("game");
        for(Element game : games) {
            if (game.attribute("code").getValue().equals(code)) {
                GameType gt = new GameType();
                gt.setCode(code);
                gt.setName(game.attributeValue("name"));
                BasicParam bp = new BasicParam();
                Element basicparam = game.element("basicparam");
                bp.setNumCards(Integer.valueOf(basicparam.attributeValue("numCards")));
                bp.setNumDecks(Integer.valueOf(basicparam.attributeValue("numDecks")));
                bp.setNumPlayers(Integer.valueOf(basicparam.attributeValue("numPlayers")));
                bp.setRounds(Integer.valueOf(basicparam.attributeValue("rounds")));
                gt.setBasicParam(bp);
                Element otherparam = game.element("otherparam");
                if (null != otherparam) {
                    OtherParam op = new OtherParam();
                    if (null != otherparam.attribute("drawLeftCardsNum"))
                        op.setDrawLeftCardsNum(Integer.valueOf(otherparam.attributeValue("drawLeftCardsNum")));
                    if (null != otherparam.attribute("returnCardsNum"))
                        op.setReturnCardsNum(Integer.valueOf(otherparam.attributeValue("returnCardsNum")));
                    gt.setOtherParam(op);
                }
                return gt;
            }
        }
        return null;
    }


    public static void main(String[] args) throws IOException, DocumentException {
        GameType gameType = getGameType("0003");
        System.out.println(gameType.getOtherParam().getDrawLeftCardsNum());
    }
}
