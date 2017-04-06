package no.hiof.mobapp.foxhunt.game.behavior.world;

import no.hiof.mobapp.foxhunt.game.BoundingBox;
import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.IHunter;
import no.hiof.mobapp.foxhunt.game.ITagHunter;
import no.hiof.mobapp.foxhunt.game.LatLongCoord;
import no.hiof.mobapp.foxhunt.game.World;
import no.hiof.mobapp.foxhunt.game.WorldBehavior;
import no.hiof.mobapp.foxhunt.game.objects.TagPlayer;
import no.hiof.mobapp.foxhunt.game.objects.Obstacle;

public class Tag extends WorldBehavior
{
	/**
	 * 
	 */
	public Tag(World currentWorld) {
		this.world = currentWorld;
	}

	//@Override
	public void setWorld(World currentWorld) {
		this.world = currentWorld;
	}
	
    /**
     * Tells each Fox in the Vector to move in the direction of the generalHeading for the group
     *
     */
	//@Override
    synchronized public void move(double deltaTime) {
        int movingObject = 0;
        int otherObject = 0;
        
        // Loop through each gameObject
        while (movingObject < world.gameObjects.size()) {
            
            GameObject gobject = this.world.gameObjects.elementAt(movingObject);
            
            // Don't do anything for obstacles
            if (gobject instanceof Obstacle)
            {
            	movingObject++;
            	continue;
            }
            
            // Call the individual behavior for the game object
            gobject.move(deltaTime);
            
            
            // Reset the iteration counter
            otherObject = 0;
            
            // Loop through every other gameobject to see if we are at the same location.
            while (otherObject < world.gameObjects.size()) {
                GameObject otherGameObject = world.gameObjects.elementAt(otherObject);
                
                // Are both objects close enough (and it isn't the same object)
                if ((movingObject != otherObject)) {
                    
                    // If both gameObjects are TagPlayers
                    if (((gobject instanceof TagPlayer) && otherGameObject instanceof TagPlayer )) {
                    	
                    	if ( ((TagPlayer)gobject).isHunter() && !((TagPlayer)otherGameObject).isHunter() && 
                    			(gobject.getDistance(otherGameObject) <= ((ITagHunter)gobject).getKillRange()) &&
                    			!((TagPlayer)gobject).isLastCaughtBy(otherGameObject)
                    		)
                    	{
                    		System.out.println(gobject.getConfigValue("name") + " caught " + otherGameObject.getConfigValue("name"));
                    		
                    		// The current object/player is now a common player
                    		((TagPlayer)gobject).setHunter(false);
                    		
                    		// The other object/player is now the hunter
                    		((TagPlayer)otherGameObject).setHunter(true);
                    		
                    		((TagPlayer)otherGameObject).setLastCaughtBy(gobject);
                    		break;
                    	}                 
                    }
                }
                otherObject++;
            }
            movingObject++;
        }
        
        return;
    }
}
