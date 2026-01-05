package fun.xiabiqing.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 文档基础信息
                .info(new Info()
                        .title("校园兼职系统接口文档") // 文档标题
                        .description("基于Knife4j 4.5的接口文档，包含用...等模块接口") // 文档描述
                        .version("V1.0.0") // 接口版本
                        // 联系人信息
                        .contact(new Contact()
                                .name("xiabiqing")
                                .email("2632493933@qq.com")
                                .url("https://github.com/xiabiqing"))
                );
    }
}
