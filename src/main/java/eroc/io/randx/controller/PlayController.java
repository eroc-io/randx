package eroc.io.randx.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PlayController {

    @RequestMapping("/")
    public String index() {
        return "card";
    }


}
