package no.hiof.mobapp.foxhunt.game.objects;

import java.sql.Connection;
import java.util.Enumeration;
import java.util.Vector;

import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.IHunter;
import no.hiof.mobapp.foxhunt.game.World;

/**
 * The predator is a type of fox that tries to eat the other foxes.
 *
 * @author      joink
 */
public class Hunter extends GameObject implements IHunter {
        final static int HUNTER_GROUPID = -555;
        final static int CATCH_RANGE = 9; // Distance in meters
    final static int DEFAULT_HUNTER_THETA = 180; // Can turn 180 degrees in 1 second

        public int catchrange = CATCH_RANGE;
        
        private boolean taken = false;

        private Vector<GameObject> caughtObjects = new Vector<GameObject>(5);

    /**
     * Constructor for the hunter.
     */
    public Hunter()
    {
                super(HUNTER_GROUPID);
        setMaxTurnTheta( DEFAULT_HUNTER_THETA );

    }

        @Override
        public void initFromDatabase(Connection con) {
                // TODO Auto-generated method stub
                config.put("behavior", "DummyGameObjectBehavior");
                config.put("speed", "3.3");
                super.initFromDatabase(con);

                if (config.containsKey("catchRadius"))
                {
                        catchrange = Integer.parseInt(config.get("catchRadius"));
                }
        }

        public double getKillRange() {
                // TODO Auto-generated method stub
                return catchrange;
        }

        public void haveCaught(GameObject go) {
                //if (!caughtObjects.contains(go)) {
                        caughtObjects.add(go);

                        if (World.doLogging == true) {
                                long caughtTime = System.currentTimeMillis();
                                System.out.println(caughtObjects.toString());
                                updateLog(caughtTime, this.dbSimulationID, dbObjectID, "caught", go.toString());
                        }
                //}
        }

        public Enumeration<GameObject> getCaughtObjects()
        {
                return caughtObjects.elements();
        }

        public int getCaughtTotal() {
                // TODO Auto-generated method stub
                return caughtObjects.size();
        }
        
        public boolean getTaken(){
        	return taken;
        }
}
