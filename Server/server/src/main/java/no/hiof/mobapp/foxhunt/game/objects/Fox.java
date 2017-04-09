package no.hiof.mobapp.foxhunt.game.objects;

import java.sql.Connection;
import java.sql.PreparedStatement;

import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.World;

/**
 * Foxes try to flock with foxes of the same group and avoid foxes of other groups, predators, and obstacles.
 *
 * @author      joink
 */
public class Fox extends GameObject
{
        final static int DEFAULT_GROUPID = 1;
        final static double DEFAULT_FOX_SPEED = 5; // m/s
    final static int DEFAULT_FOX_THETA = 35; // Can turn 90 degrees in one second

    boolean caught = false;
    long caughtTime;

    static PreparedStatement dbIsCaught;

        public Fox()
        {
                super(DEFAULT_GROUPID);
        setSpeed( DEFAULT_FOX_SPEED );
        setMaxTurnTheta( DEFAULT_FOX_THETA );
        }

        public Fox(double lon, double lat, int theta, int groupID) {
                super(lon, lat, theta, groupID);
                // TODO Auto-generated constructor stub
        }

        public Fox(int groupID) {
                super(groupID);
                // TODO Auto-generated constructor stub
        }

        public void initFromDatabase(Connection con) {
                super.initFromDatabase(con);
        }

        public void reSpawn()
        {
                if (isCaught()){
                        if (System.currentTimeMillis() - caughtTime >= 30000){
                                System.out.println("Respawning " + this.getConfigValue("name"));
                                randomizePlacement();
                                setCaught(false);
                        }
                }
        }

        public void randomizePlacement()
        {

                //double newlon = getWorld().getMap().c1.lon + Math.random()*(getWorld().getMap().c2.lon - getWorld().getMap().c1.lon);
                //double newlat = getWorld().getMap().c1.lat + Math.random()*(getWorld().getMap().c2.lat -getWorld().getMap().c1.lat);

                //getLocation().lat = newlat;
                //getLocation().lon = newlon;

                setLocation(getWorld().getMap().getRandomPointInside());
        }
        /**
         * @return the caught
         */
        public boolean isCaught() {
                return caught;
        }

        /**
         * @param caught the caught to set
         */

        public void setCaught(boolean caught) {
            if (this.caught != caught) {
                    caughtTime = System.currentTimeMillis();
                    this.caught = caught;

                    if (World.doLogging == true) {
                            updateLog(caughtTime, this.dbSimulationID, dbObjectID, "iscaught", "true");
                    }
            }
    }
}
