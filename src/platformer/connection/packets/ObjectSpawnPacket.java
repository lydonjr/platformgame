package platformer.connection.packets;

import platformer.GameUtil;
import platformer.MainClient;
import platformer.connection.Communicator;
import platformer.connection.Packet;
import platformer.world.WorldObj;
import platformer.world.entity.PlayerEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ObjectSpawnPacket extends Packet {
    private WorldObj obj;

    public ObjectSpawnPacket(WorldObj obj) {
        this.obj = obj;
    }

    public ObjectSpawnPacket() {
    }

    public WorldObj getObj() {
        return obj;
    }

    @Override
    protected void breakdown(OutputStream out) throws IOException {
        GameUtil.write(out, obj);
    }

    @Override
    protected void buildPacket(InputStream in) throws IOException, ClassNotFoundException {
        obj = GameUtil.read(in);
    }

    @Override
    public void applyPacket(Communicator communicator, Socket socket) {
        MainClient.WORLD.addObjectToWorld(obj);

        // if this entity is the client's player entity, assign players entity
        if (obj.getObjectId() == MainClient.PLAYER_ID) {
            assert MainClient.PLAYER != null : "Cannot re-assign player object";
            MainClient.PLAYER = (PlayerEntity) obj;
        }
//        else Platform.runLater(() -> MainClient.root.getChildren().add(obj.getShape()));
    }
}
