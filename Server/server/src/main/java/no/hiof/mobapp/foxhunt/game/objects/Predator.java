package no.hiof.mobapp.foxhunt.game.objects;

import java.util.Enumeration;
import java.util.Vector;

import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.IHunter;

/**
 * The predator is a type of fox that tries to eat the other foxes.
 *
 * @author      joink
 */
public class Predator extends Fox implements IHunter {
        static int PREDATOR_GROUPID = -666;
        static int KILL_RANGE = 7;

        public int killrange = KILL_RANGE;

        private Vector<GameObject> caughtObjects = new Vector<GameObject>(5);
    // How many foxes this predator must eat before being removed from the map.
    int currentHunger;

    /**
     * Constructor for the predator.
     */
    public Predator()
    {
                super(PREDATOR_GROUPID);
        setSpeed( DEFAULT_FOX_SPEED );
        setMaxTurnTheta( DEFAULT_FOX_THETA );
    }

    /**
     * Set the hunger value
     *
     * @param  hunger How many foxes should this predator eat before being removed from the map.
     */
    public void setHunger( int hunger ) {
        currentHunger = hunger;
    }

    /**
     * Set the hunger value
     *
     * @return  The hunger value for this predator.
     */
    public int getHunger() {
        return currentHunger;
    }

    /**
     * Reduces the hunger value by one.
     */
    public void eatFox() {
        currentHunger--;
    }

        public double getKillRange() {
                // TODO Auto-generated method stub
                return killrange;
        }

        public void haveCaught(GameObject go) {
                caughtObjects.add(go);
        }

        public Enumeration<GameObject> getCaughtObjects()
        {
                return caughtObjects.elements();
        }

        public int getCaughtTotal() {
                // TODO Auto-generated method stub
                return 0;
        }
}
