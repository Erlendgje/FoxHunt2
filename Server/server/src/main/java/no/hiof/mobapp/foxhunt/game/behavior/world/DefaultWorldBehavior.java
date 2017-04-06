package no.hiof.mobapp.foxhunt.game.behavior.world;

import no.hiof.mobapp.foxhunt.game.BoundingBox;
import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.IHunter;
import no.hiof.mobapp.foxhunt.game.LatLongCoord;
import no.hiof.mobapp.foxhunt.game.World;
import no.hiof.mobapp.foxhunt.game.WorldBehavior;
import no.hiof.mobapp.foxhunt.game.objects.Food;
import no.hiof.mobapp.foxhunt.game.objects.Fox;
import no.hiof.mobapp.foxhunt.game.objects.Obstacle;

public class DefaultWorldBehavior extends WorldBehavior
{
	/**
	 * 
	 */
	public DefaultWorldBehavior(World currentWorld) {
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
    	//System.out.println("Number of game objects: " + world.getGameObjects().size());
    	//return;
    	
        int movingFox = 0;
        int gobjectToEat = 0;
        
        // Loop through each gameObject
        while (movingFox < world.gameObjects.size()) {
            
            GameObject gobject = this.world.gameObjects.elementAt(movingFox);
            
            if (gobject instanceof Fox && ((Fox)gobject).isCaught())
            {
            	// If we have a fox that is caught we try to respawn it.
            	// It will only spawn if the the time it's been away from the game and if longer than a set time
            	((Fox)gobject).reSpawn();
            	
            	movingFox++;
            	continue;
            }
            
            // Obstacles and food doesn't move.
            if ((gobject instanceof Obstacle) || (gobject instanceof Food))
            {
            	movingFox++;
            	continue;
            }
            
            // Ask the game object to move in the general Heading calculated from the
            // game rules
            gobject.move(deltaTime, generalHeading(gobject));
            
            
            
            
            // Loop through every other fox to see if we are at the same location.
            // A predator will eat other foxes.
            // Any fox will eat food.
            // A fox/predator will only eat one object per turn.
            
            gobjectToEat = 0;
            while (gobjectToEat < world.gameObjects.size()) {
                GameObject otherGameObject = world.gameObjects.elementAt(gobjectToEat);
                
                // Are both foxes at the same location (and it isn't the same fox)
                if ((movingFox != gobjectToEat)) {
                    
                    // If this fox has the property(interface) of IHunter and the other gameObject is a normal fox
                    if (((gobject instanceof IHunter) && otherGameObject instanceof Fox )) {

                    	if ( !((Fox)otherGameObject).isCaught() && (gobject.getDistance(otherGameObject) <= ((IHunter)gobject).getKillRange()))
                    	{
                    		System.out.println(gobject.getConfigValue("name") + " caught " + otherGameObject.getConfigValue("name"));
                    		// The hunter is updated as having caught the prey
                    		((IHunter)gobject).haveCaught(otherGameObject);
                    		
                    		// The prey(fox) is updated as being caught
                    		((Fox)otherGameObject).setCaught(true);
                    		break;
                    	}                 
                    }
                }
                gobjectToEat++;
            }
            movingFox++;
        }
        
        return;
    }

    /** 
     * This function determines the direction a Fox will turn towards for this step.
     * The fox looks at every other fox and obstacle on the map to see if it is
     * within the detection range. Predators will move toward foxes. Foxes will
     * avoid gameObjects of a different group (Predators, Obstacles, Foxes of a different "herd").
     *
     * @param  fox The fox to get the heading for
     */
    // TODO: There are quite some "magic constants" here. Pull these out into variables and let them be set
    // by the user.
    private LatLongCoord generalHeading(GameObject gobject)
    {           
        // Sum the location of all foxes that are within our detection range
        LatLongCoord target = new LatLongCoord(gobject.getLocation().lat, gobject.getLocation().lon);
        
        // A gameobject have some momentum
        // TODO: If more realism is needed calculate the from the objects mass and current speed.
        target.moveDistance(1.5, gobject.getTheta());
        
        // Number of foxes that are within the detection range
        int numGameObjects = 0;
        
        // Loop thorough each fox to see if it is within our detection range
        for (int i=0; i < world.gameObjects.size(); i++) {
            GameObject otherObject = world.gameObjects.elementAt(i);
            
            // get distance to the other Fox.
            double distance = gobject.getDistance(otherObject);
            
            /**
             *  Don't check against our self, only act if we are between 0 and detection range and don't do
             *  anything if the other object or ourself is hidden
             */
            if (!gobject.equals(otherObject) && distance > 0 && distance <= gobject.detectionRange && 
            		!gobject.isHidden() && !otherObject.isHidden())
            {
            	// If the other object is caught skip it
            	if (otherObject instanceof Fox && ((Fox)otherObject).isCaught())
            	{
            		continue;
            	}
            	
            	/**
            	 * If the gameObjects are of the same group we have attraction
            	 */
                if (gobject.getGroupID() == (otherObject.getGroupID())) {
                	
                	//System.out.print("Fox see a friend - ");
                	double force = Math.pow((1 - distance/gobject.separationRange), 2);
                	
                	// If we are too close to the other
                	if (distance < gobject.separationRange)
                	{
                		//System.out.println("And moves a bit away");
                		target.moveDistance(2.5*force, otherObject.getBearing(gobject));
                	} else {
                		//System.out.println("And moves a bit towards it");
                		target.moveDistance(3*force, gobject.getBearing(otherObject));
                	}
                }
                /**
                 * If this fox is of a hunter type, and the other game object is a type of fox (prey, predator)
                 * then there is attraction.
                 */
                else if ((gobject instanceof IHunter) && (otherObject instanceof Fox)) {
                	//System.out.println("See Prey - Moving towards it");
                	double force = Math.pow((1 - distance/gobject.detectionRange), 2);
                	target.moveDistance(20*force, gobject.getBearing(otherObject));
                }
                /**
                 * If the other gameObject is food, then there is attraction, for both
                 * foxes and predators.
                 */
                else if ((gobject instanceof Fox) && (otherObject instanceof Food)) {
                	target.moveDistance(5, gobject.getBearing(otherObject));
                }
                /**
                 * If the gameObject is an obstacle,
                 * the gameObject is repulsed with a force that is weighted
                 * according to a distance square rule multiplied by a factor.
                 */
                else if ((otherObject instanceof Obstacle)){
                	//System.out.println("Eeep. Something unknown. Avoiding!");
                	double force = Math.pow((1 - distance/gobject.separationRange), 2);
                	target.moveDistance(-2*force, gobject.getBearing(otherObject));
                }
                /**
                 * If the gameObject is of a different group or type,
                 * it is repulsed with a force that is weighted
                 * according to a distance square rule.
                 */
                else {
                	//System.out.println("Eeep. Something unknown. Avoiding!");
                	double force = Math.pow((1 - distance/gobject.detectionRange), 2);
                	target.moveDistance(-15*force, gobject.getBearing(otherObject));
                }
                numGameObjects++;
            }
        }
        
        // Fence avoidance
        
        // Get the two closest edges of the map and calculate the repulsion from them given 
        // that they are inside the SeperationRange and detectionRange
        // Earlier try of using just a single point gave the possibility of getting the fox stuck
        // in corners
        
        
        LatLongCoord[] intCoords = world.getMap().closestEdgeIntersectionCoordinates(gobject.getLocation());
        
        double distFromFenceVertical = intCoords[0].getDistance(gobject.getLocation());
        double distFromFenceHorizontal = intCoords[1].getDistance(gobject.getLocation());
        
        
        //if (gobject instanceof IHunter)
        //		System.out.println("DistV: " + distFromFenceVertical + ", DistH: " + distFromFenceHorizontal);
        
        // Work with the vertical fence first
        if (distFromFenceVertical < gobject.separationRange && distFromFenceVertical < gobject.detectionRange)
        {
        	double force = Math.pow((1 - distFromFenceVertical/gobject.detectionRange), 2);
        	//System.out.println("Too close to v-fence. Distance to fence is: "+ distFromFenceVertical + " and force: "+force);
        	
        	int avoidHeading = intCoords[0].getBearing(gobject.getLocation());
        	
        	//System.out.println("Current heading(" + gobject.getTheta() + "): " + (int)((1.0-force)*gobject.getTheta()) + " - AvoidHeading(" + avoidHeading + "): " + (int)(force*avoidHeading) );
        	//avoidHeading = (int)((1.0-force)*gobject.getTheta() +  (int)(force*avoidHeading));
        	
        	//System.out.println("Moving: " + 17*force + " with heading: " + avoidHeading);
        	target.moveDistance(10*force, avoidHeading);
        	
        	numGameObjects++;
        }
        
        // Work with the horizontal fence first
        if (distFromFenceHorizontal < gobject.separationRange && distFromFenceHorizontal < gobject.detectionRange)
        {
        	double force = Math.pow((1 - distFromFenceHorizontal/gobject.detectionRange), 2);
        	//System.out.println("Too close to h-fence. Distance to fence is: "+ distFromFenceHorizontal + " and force: "+force);
        	//System.out.println("(" + gobject.location.lat + ", " + gobject.location.lon + ") bearing to (" + closestFencePoints.c2.lat + ", " + closestFencePoints.c2.lon + ")");
            
        	int avoidHeading = intCoords[1].getBearing(gobject.getLocation());
        	//int avoidHeading = gobject.location.getBearing(closestFencePoints.c2);
        	//gobject.location.getBearing(closestFencePoints.c1);
        	//System.out.println("Fence-normal avoidance: " + avoidHeading);
        	
        	
        	//System.out.println("Current heading(" + gobject.getTheta() + "): " + (int)((1.0-force)*gobject.getTheta()) + " - AvoidHeading(" + avoidHeading + "): " + (int)(force*avoidHeading) );
        	//avoidHeading = (int)((1.0-force)*gobject.getTheta() +  (int)(force*avoidHeading));
        	
        	//System.out.println("Moving: " + 17*force + " with heading: " + avoidHeading);
        	target.moveDistance(10*force, avoidHeading);
        	
        	numGameObjects++;
        }

        //int targetTheta =  gobject.getLocation().getBearing(target); //target.getBearing(gobject.location);
        
        //System.out.println("New theta: " + targetTheta);
        //System.out.println("---------");
        return target; // angle for Fox to steer towards
    }
}
