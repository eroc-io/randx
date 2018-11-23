package eroc.io.randx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ServletComponentScan
@MapperScan(basePackages = "eroc.io.randx.dao")
public class RandxApplication {

    public static void main(String[] args) {
        SpringApplication.run(RandxApplication.class, args);
    }
}
