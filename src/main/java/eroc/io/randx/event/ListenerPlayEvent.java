package eroc.io.randx.event;

import eroc.io.randx.service.PlayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 监听实现类
 */
@Component
public class ListenerPlayEvent implements ApplicationListener<PlayEvent> {


    @Autowired
    private PlayService playService;


    /**
     *
     *
     * @param playEvent
     */
    @Override
    public void onApplicationEvent(PlayEvent playEvent) {
    }
}
