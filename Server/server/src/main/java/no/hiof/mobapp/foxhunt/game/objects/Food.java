/*
 * File Name: Food.cs
 */

package no.hiof.mobapp.foxhunt.game.objects;

import no.hiof.mobapp.foxhunt.game.GameObject;

/**
 * This food class represents a food item on the map that all foxes, including predators want to eat.
 *
 * @author              joink
 */
public class Food extends GameObject {
        static int FOOD_GROUPID = -2;

    public Food()
    {
        super(FOOD_GROUPID);
    }

    /**
     * The food class overrides the move function to do nothing.
     */
    public void move(double dT, int newHeading)
    {
        // food does not move
    }
}
