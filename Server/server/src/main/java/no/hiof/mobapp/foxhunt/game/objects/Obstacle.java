package no.hiof.mobapp.foxhunt.game.objects;

import no.hiof.mobapp.foxhunt.game.GameObject;

/**
 * This obstacle class represents an item on the map that all foxes try to avoid. Obstacles do not move.
 *
 * @author      joink
 */
public class Obstacle extends GameObject {
        static int OBSTACLE_GROUPID = -1;
    /**
     * This is the constructor for the obstacle.
     */
    public Obstacle()
    {
        super (OBSTACLE_GROUPID);
    }

    /**
     * The obstacle class overrides the move function to do nothing.
     *
     */
    public void move(double dT, int newHeading)
    {
        // obstacles do not move
    }
}

