package puregero.multipaper.server.handlers;

import puregero.multipaper.server.DataOutputSender;
import puregero.multipaper.server.ServerConnection;
import puregero.multipaper.server.handlers.Handler;
import puregero.multipaper.server.locks.AdvancementsLock;
import puregero.multipaper.server.locks.PlayerLock;

import java.io.DataInputStream;
import java.io.IOException;

public class LockAdvancementsHandler implements Handler {
    @Override
    public void handle(ServerConnection connection, DataInputStream in, DataOutputSender out) throws IOException {
        String uuid = in.readUTF();

        AdvancementsLock.lock(connection.getBungeeCordName(), uuid);
    }
}
