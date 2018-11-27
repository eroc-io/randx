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

    private Integer num = 3;

    @Autowired
    private PlayService playService;


    /**
     * 实现开始游戏
     *
     * @param playEvent
     */
    @Override
    public void onApplicationEvent(PlayEvent playEvent) {
        if (playEvent.getCount().equals(num)) {
            //开始游戏
            playService.openGame(playEvent.getWss());

        }
    }
}
