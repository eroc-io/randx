package eroc.io.randx;

import eroc.io.randx.controller.WebSocketServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ServletComponentScan
@MapperScan(basePackages = "eroc.io.randx.dao")
public class RandxApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(RandxApplication.class, args);
        WebSocketServer.setApplicationContext(run);

    }
}
