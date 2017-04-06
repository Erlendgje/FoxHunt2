/**
 * 
 */
package no.hiof.mobapp.foxhunt.game.behavior.individual;

import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.GameObjectBehavior;
import no.hiof.mobapp.foxhunt.game.LatLongCoord;

/**
 * @author joink
 *
 */
public class DefaultGameObjectBehavior extends GameObjectBehavior {

	/**
	 * 
	 */
	public DefaultGameObjectBehavior(GameObject thisGameObject) {
		gobject = thisGameObject;
	}

	//@Override
	public void setGameObject(GameObject thisGameObject) {
		gobject = thisGameObject;
	}
	
	/* (non-Javadoc)
	 * @see no.hiof.mobapp.foxhunt.gamebehavior.GameObjectBehavior#move(double)
	 */
	//@Override
	public void move(double deltaTime, LatLongCoord target) {

		int newHeading = gobject.getLocation().getBearing(target);
	// Introduce some randomness into the movements
	// This gets a random value between -0.5 and 0.5 and then multiplies it with 30. [-15, +15]
	newHeading += (Math.random() - 0.5) * 30;

        // determine if it is better to turn left or right for the new heading
        int left = (newHeading - gobject.getTheta() + 360) % 360;
        int right = (gobject.getTheta() - newHeading + 360) % 360;
        
        // after deciding which way to turn, find out if we can turn that much
        int thetaChange = 0;
        if (left < right) {
            // if left > than the max turn, then we can't fully adopt the new heading
            thetaChange = Math.min((int)(deltaTime*(double)gobject.getMaxTurnTheta()), left);
        }
        else {
            // right turns are negative degrees
            thetaChange = -Math.min((int)(deltaTime*(double)gobject.getMaxTurnTheta()), right);
        }
        
        // Make the turn
        gobject.setTheta((gobject.getTheta()+ thetaChange + 360) % 360);
        
        gobject.getLocation().moveDistance(deltaTime*gobject.getSpeed(), gobject.getTheta());
        
        // If the gameObject takes a trip outside the bounds of our field we force it back inside
        gobject.getLocation().clipCoordinates(gobject.getWorld().getMap());
	}

	@Override
	public void move(double deltaTime) {
		// TODO Auto-generated method stub
		System.out.println("OBJECT DOES NOT MOVE - BEHAVIORTYPE NOT IMPLEMENTED");
	}

}
