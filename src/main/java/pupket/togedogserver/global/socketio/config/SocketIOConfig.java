package pupket.togedogserver.global.socketio.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOConfig {

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(9092);

        // CORS 허용 설정 (모든 출처에 대해 허용)
        config.setOrigin("*");

        return new SocketIOServer(config);
    }

}
