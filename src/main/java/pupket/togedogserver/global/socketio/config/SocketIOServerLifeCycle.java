package pupket.togedogserver.global.socketio.config;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class SocketIOServerLifeCycle {

    private final SocketIOServer server;

    public SocketIOServerLifeCycle(SocketIOServer server) {
        this.server = server;
    }

    @PostConstruct
    public void start() {
        server.start();
    }

    @PreDestroy
    public void stop() {
        server.stop();
    }

}
