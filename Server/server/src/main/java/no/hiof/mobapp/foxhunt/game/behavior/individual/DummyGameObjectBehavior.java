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
public class DummyGameObjectBehavior extends GameObjectBehavior {

	/**
	 * 
	 */
	public DummyGameObjectBehavior(GameObject thisGameObject) {
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
		// Dummy - do nothing
	}
	
	@Override
	public void move(double deltaTime) {
		// Dummy - do nothing
	}

}
