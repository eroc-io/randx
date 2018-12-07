package eroc.io.randx.event;

import eroc.io.randx.controller.WebSocketServer;
import org.springframework.context.ApplicationEvent;

import java.util.concurrent.CopyOnWriteArraySet;

public class PlayEvent extends ApplicationEvent {

    private Integer count;

    private CopyOnWriteArraySet<WebSocketServer> wss;

    public PlayEvent(Object source, Integer count, CopyOnWriteArraySet<WebSocketServer> wss) {
        super(source);
        this.count = count;
        this.wss = wss;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public CopyOnWriteArraySet<WebSocketServer> getWss() {
        return wss;
    }

    public void setWss(CopyOnWriteArraySet<WebSocketServer> wss) {
        this.wss = wss;
    }
}
