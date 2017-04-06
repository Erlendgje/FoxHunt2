/**
 * 
 */
package no.hiof.mobapp.foxhunt.game.behavior.individual;

import no.hiof.mobapp.foxhunt.game.BoundingBox;
import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.GameObjectBehavior;
import no.hiof.mobapp.foxhunt.game.IStamina;
import no.hiof.mobapp.foxhunt.game.LatLongCoord;
import no.hiof.mobapp.foxhunt.game.objects.Obstacle;
import no.hiof.mobapp.foxhunt.game.objects.TagPlayer;

/**
 * @author joink
 *
 */
public class TagPlayerAndHunter extends GameObjectBehavior {

	/**
	 * 
	 */
	public TagPlayerAndHunter(GameObject thisGameObject) {
		gobject = thisGameObject;
	}

	//@Override
	public void setGameObject(GameObject thisGameObject) {
		gobject = thisGameObject;
	}
	
	public void move(double deltaTime)
	{
		move(deltaTime, generalHeading(this.gobject));
	}
	
	/* (non-Javadoc)
	 * @see no.hiof.mobapp.foxhunt.gamebehavior.GameObjectBehavior#move(double)
	 */
	//@Override
	public void move(double deltaTime, LatLongCoord target) {
		
		int newHeading = gobject.getLocation().getBearing(target);
		
	// Introduce some randomness into the movements
	// This gets a randon value between -0.5 and 0.5 and then multiplies it with 90. [-45, +45]
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
        
        if (gobject instanceof IStamina) {
        	((IStamina)gobject).rebuildStamina(deltaTime);
        	
        	double dist = (gobject.getLocation().getDistance(target))/gobject.separationRange;
        	if (dist > 1.0)
        	{
        		dist = 1.0;
        	}
        	double distanceFactor = Math.pow((dist), 2);
        	//System.out.println("Distance is: " + gobject.getLocation().getDistance(target) + " - Factor is: " + distanceFactor);
        	gobject.setSpeed(((IStamina)gobject).getStaminaSpeed(distanceFactor, deltaTime));
        }
        
        gobject.getLocation().moveDistance(deltaTime*gobject.getSpeed(), gobject.getTheta());
        
        // If the gameObject takes a trip outside the bounds of our field we force it back inside
        gobject.getLocation().clipCoordinates(gobject.getWorld().getMap());
	}
	
    /** 
     * This function determines the direction a Fox will turn towards for this step.
     * The fox looks at every other fox and obstacle on the map to see if it is
     * within the detection range. Predator will move toward foxes. Foxes will
     * avoid gameObjects of a differen group (Predators, Obstacles, Foxes of a different "herd").
     *
     * @param  fo
     *	The fox to get the heading for
     */
    private LatLongCoord generalHeading(GameObject gobject)
    {   
        // Sum the location of all foxes that are within our detection range
        LatLongCoord target = new LatLongCoord(gobject.getLocation().lat, gobject.getLocation().lon);
        
        // The default direction for this game object is the one it is moving to right now
        target.moveDistance(1.5, gobject.getTheta());
        
        // Number of foxes that are within the detection range
        int numGameObjects = 0;
        
        // Loop thorough each fox to see if it is within our detection range
        for (int i=0; i < gobject.getWorld().gameObjects.size(); i++) {
            GameObject otherObject = gobject.getWorld().gameObjects.elementAt(i);
            
            // get distance to the other object.
            double distance = gobject.getDistance(otherObject);
            
            /**
             *  Don't check against our self, only act if we are between 0 and detection range and don't do
             *  anything if the other object or ourself is hidden
             */
            if (!gobject.equals(otherObject) && distance > 0 && distance <= gobject.detectionRange && 
            		!gobject.isHidden() && !otherObject.isHidden())
            {
            	// If both we and the other object is a hunter - skip the otherObject - hunters don't hunt hunters.
            	if	(	(gobject instanceof TagPlayer && ((TagPlayer)gobject).isHunter()) && 
            			(otherObject instanceof TagPlayer && ((TagPlayer)otherObject).isHunter())
            		)
            	{
            		continue;
            	}
            	
            	// If the other player is the one we were last caught by, don't catch it.. Against game rules.
            	if	(	(gobject instanceof TagPlayer && ((TagPlayer)gobject).isHunter()) && 
            			((TagPlayer)gobject).isLastCaughtBy(otherObject)
            		)
            	{
            		continue;
            	}
            	
                /**
                 * If this player is a hunter, and the other game object is a type of fox (prey, predator)
                 * then there is attraction.
                 */
                else if ( ((TagPlayer)gobject).isHunter() && (otherObject instanceof TagPlayer)) {
                	//System.out.println("See Prey - Moving towards it");
                	
                	// Be more agressive
                	double dist = distance/(gobject.detectionRange*2);
                	
                	double force = Math.pow((1-dist), 2);
                	target.moveDistance(40*force, gobject.getBearing(otherObject));
                }
                /**
                 * If the gameObjects are an obstacle,
                 * the gameObject is repulsed with a force that is weighted
                 * according to a distance square rule multiplied by a factor.
                 */
                else if ((otherObject instanceof Obstacle)){
                	//System.out.println("Eeep. Something unknown. Avoiding!");
                	double force = Math.pow((1 - distance/gobject.separationRange), 2);
                	target.moveDistance(-2*force, gobject.getBearing(otherObject));
                }
                /**
                 * If the gameObject are of a different group or type,
                 * the gameObject is repulsed with a force that is weighted
                 * according to a distance square rule.
                 */
                else {
                	//System.out.println("Eeep. Something unknown. Avoiding!");
                	double force = Math.pow((1 - distance/gobject.detectionRange), 2);
                	target.moveDistance(-40*force, gobject.getBearing(otherObject));
                }
                numGameObjects++;
            }
        }
        
        // Fence avoidance
        
        // Get the two closest edges of the map and calculate the repulsion from them given 
        // that they are inside the SeperationRange and detectionRange
        // Earlier try of using just a single point gave the possibility of getting the fox stuck
        // in corners
        
        BoundingBox closestFencePoints = gobject.getWorld().getMap().closestEdgeCoordinates(gobject.getLocation());
        
        double distFromFenceVertical = closestFencePoints.c1.getDistance(gobject.getLocation());
        double distFromFenceHorizontal = closestFencePoints.c2.getDistance(gobject.getLocation());
        
        //System.out.println("DistV: " + distFromFenceVertical + ", DistH: " + distFromFenceHorizontal);
        
        // Work with the vertical fence first
        if (distFromFenceVertical < gobject.separationRange && distFromFenceVertical < gobject.detectionRange)
        {
        	double force = Math.pow((1 - distFromFenceVertical/gobject.separationRange), 2);
        	//System.out.println("Too close to v-fence. Distance to fence is: "+ distFromFenceVertical + " and force: "+force);
        	
        	int avoidHeading = closestFencePoints.c1.getBearing(gobject.getLocation());
        	
        	//System.out.println("Current heading(" + gobject.getTheta() + "): " + (int)((1.0-force)*gobject.getTheta()) + " - AvoidHeading(" + avoidHeading + "): " + (int)(force*avoidHeading) );
        	//avoidHeading = (int)((1.0-force)*gobject.getTheta() +  (int)(force*avoidHeading));
        	
        	//System.out.println("Moving: " + 17*force + " with heading: " + avoidHeading);
        	target.moveDistance(force, avoidHeading);
        	
        	numGameObjects++;
        }
        
        // Work with the horizontal fence first
        if (distFromFenceHorizontal < gobject.separationRange && distFromFenceHorizontal < gobject.detectionRange)
        {
        	double force = Math.pow((1 - distFromFenceHorizontal/gobject.separationRange), 2);
        	//System.out.println("Too close to h-fence. Distance to fence is: "+ distFromFenceHorizontal + " and force: "+force);
        	//System.out.println("(" + gobject.location.lat + ", " + gobject.location.lon + ") bearing to (" + closestFencePoints.c2.lat + ", " + closestFencePoints.c2.lon + ")");
            
        	int avoidHeading = closestFencePoints.c2.getBearing(gobject.getLocation());
        	//int avoidHeading = gobject.location.getBearing(closestFencePoints.c2);
        	//gobject.location.getBearing(closestFencePoints.c1);
        	//System.out.println("Fence-normal avoidance: " + avoidHeading);
        	
        	
        	//System.out.println("Current heading(" + gobject.getTheta() + "): " + (int)((1.0-force)*gobject.getTheta()) + " - AvoidHeading(" + avoidHeading + "): " + (int)(force*avoidHeading) );
        	//avoidHeading = (int)((1.0-force)*gobject.getTheta() +  (int)(force*avoidHeading));
        	
        	//System.out.println("Moving: " + 17*force + " with heading: " + avoidHeading);
        	target.moveDistance(force, avoidHeading);
        	
        	numGameObjects++;
        }
        
        //int targetTheta =  gobject.getLocation().getBearing(target); //target.getBearing(gobject.location);
        
        //System.out.println("New theta: " + targetTheta);
        //System.out.println("---------");
        return target; // angle for Fox to steer towards
    }
}
