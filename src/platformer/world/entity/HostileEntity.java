package platformer.world.entity;

import platformer.world.Location;
import platformer.world.World;

public class HostileEntity extends LivingEntity {

    private boolean targetAcquired;
    private Entity target;

    public HostileEntity(Location location, World world, int maxHealth) {
        super(location, world, maxHealth);
    }

    public HostileEntity(Location location, World world) {
        super(location, world);
    }

    @Override
    public void update() {

        if (getHealth() == 0) {
            alive = false;
        }

        //TODO - lock onto closest target

        //Get closest target by looking at all players, the closest one will be the one targeted.
        //Target must be min distance away (will figure this out once we have a working game to test distances)


    }
}
