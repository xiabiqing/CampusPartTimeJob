package fun.xiabiqing.config; // 你的包名，必须和其他配置类一致

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

// 注解1：标记这是一个Spring配置类
@Configuration
public class WebSocketConfig {
    // 注解2：向Spring容器中注册一个Bean对象
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        // 返回WebSocket核心对象：作用是「自动扫描并注册后续的聊天服务类」
        return new ServerEndpointExporter();
    }
}