package pupket.togedogserver.global.socketio.config;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;

public class SocketDisconnectListener implements DisconnectListener {
    @Override
    public void onDisconnect(SocketIOClient client) {
    }
}
