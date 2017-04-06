/**
 * 
 */
package no.hiof.mobapp.foxhunt.game;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

import no.hiof.mobapp.foxhunt.game.objects.Hunter;

/**
 * @author joink
 *
 */
public class World implements IDatabaseObject {
	
    // This is the list of foxes in the map
    public Vector<GameObject> gameObjects;
    
    // Mapping between objectID and the gameObject
    private HashMap<Integer, GameObject> gameObjectsMap;
    
    // Mapping between hunterID and the hunters gameObject
    private HashMap<String, GameObject> gameObjectsIDMap;
        
    // This is the bounding box (polygon in the future) that the game takes place
    private static BoundingBox map = new BoundingBox(50, 11, 60, 12);

    WorldBehavior worldBehavior;
    
    int simID;
    
    long lastDbUpdate = System.currentTimeMillis();
    
    int dbUpdateInterval = 1000; // Update interval in ms
    
    public static PreparedStatement dbLogSelect;
    
    public static boolean doLogging = true;
    
	/**
	 */
	public World(int simID) {
		this.gameObjects = new Vector<GameObject>(10);
		this.gameObjectsMap = new HashMap<Integer, GameObject>(15);
		this.gameObjectsIDMap = new HashMap<String, GameObject>(15);
		this.simID = simID;
	}
    
    /**
     * Add a gameObject, i.e. like a fox, to the world.
     *
     * @param  gObject The gameObject to add
     */
    public void addGameObject(GameObject gObject) {
        gameObjects.addElement(gObject);
        
        gameObjectsMap.put(gObject.dbObjectID, gObject);
        
        gameObjectsIDMap.put(Integer.toString(gObject.dbObjectID), gObject);
        
        // Hunters are placed in a hashmap as well. (HunterID->Hunters GameObject)
        //if (gObject instanceof Hunter)
        //{
        //	hunters.put(Integer.toString(gObject.dbObjectID), (Hunter)gObject);
        //}
    }
    	
    public Vector<GameObject> getGameObjects()
    {
    	return gameObjects;
    }
    
    public Hunter getHunter(String hunterID) {
		return (Hunter) gameObjectsIDMap.get(hunterID);
	}
    
    public GameObject getGameObjectByID(String ID) {
		return gameObjectsIDMap.get(ID);
	}

	/**
     * Remove the first gameObject belonging to a specific group. The group ID is given as an argument.
     *
     * @param  groupID The group ID of the gameObject to remove
     */
    synchronized void removeGameObject(int groupID) {
        for (int i=0; i < gameObjects.size(); i++) {
            GameObject gobject = gameObjects.elementAt(i);
            if (gobject.getGroupID() == groupID) {
                gameObjects.removeElementAt(i);
                break;
            }
        }
    }
    
    public static void setMapSize(BoundingBox newSize) {
        map = newSize;
    }
    
    public void setGameWorldBehaviour(WorldBehavior worldBehavior)
    {
    	this.worldBehavior = worldBehavior;
    }
    
    public WorldBehavior getGameWorldBehaviour()
    {
    	return this.worldBehavior;
    }
    
    public void update(double deltaTime)
    {
    	if (this.worldBehavior == null)
    	{
    		System.out.println("World->Behavior - Points to null. No world behavior will be performed");
    		return;
    	}
    	worldBehavior.move(deltaTime);
    }
    
	public static void createPreparedStatement(Connection con) {
		// Create the update statement
		//dbUpdateState= con.prepareStatement("UPDATE Object SET lon= ? , lat= ? , hidden= ?  WHERE id= ? ");
		try {
			dbLogSelect= con.prepareStatement("SELECT `Timestamp`, `SimulationID`, `ObjectID`, `property`, `value` FROM `SimulationObjectLog` WHERE `SimulationID` = ? AND `TimeStamp` >= ? AND `TimeStamp` < ? ;");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
    public void replay(long fromTime, long toTime)
    {
    	// Query the databaselog from: fromTime to: toTime
    	// Parse through the log and update the objects in the world
    	
    	System.out.println("Updating world with values from db in interval: " + fromTime + " -> " + toTime + " being: " + (toTime-fromTime));
    	
    	try {
    		dbLogSelect.setInt(1, this.simID);
			dbLogSelect.setLong(2, fromTime);
			dbLogSelect.setLong(3, toTime);
			System.out.println("DB-Query: "+dbLogSelect);
			
			ResultSet rs = dbLogSelect.executeQuery();
			
			while (rs.next()) {
				System.out.println("TS: " + rs.getLong(1) + " SimID: " + rs.getInt(2) + " ObjectID: " + rs.getInt(3) + " K:" + rs.getString(4) + " V:" + rs.getString(5));
				
				int objectId = rs.getInt(3);
				//String objectString = rs.getString(3);
				
				String key = rs.getString(4);
				
				if (key.equals("lat")) {
					gameObjectsMap.get(objectId).getLocation().lat = rs.getDouble(5);
				} else if (key.equals("lon")) {
					gameObjectsMap.get(objectId).getLocation().lon = rs.getDouble(5);
				} else if (key.equals("caught")) {
					// The objectID (being a hunter) caught the object with id rs.getInt(5)
					((IHunter) gameObjectsMap.get(objectId)).haveCaught(gameObjectsMap.get(rs.getInt(5)));
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
	public void initFromDatabase(Connection con) {
		// TODO Auto-generated method stub
	}

	public void updateDatabase(Connection con) {
		// If we don't need to do any logging we can just return as none of the object will
		// have data for us
		if (this.doLogging == false)
		{
			return;
		}
		
		// If we are below our update interval return and wait a bit more.
		if ( (System.currentTimeMillis() - lastDbUpdate) <= dbUpdateInterval )
		{
			return;
		}
				
		lastDbUpdate = System.currentTimeMillis();
		
    	for (int i=0; i < gameObjects.size(); i++) {
    		gameObjects.elementAt(i).updateDatabase(con);
    	}
    	
    	try {
			GameObject.dbUpdateState.executeBatch();
    	} catch (com.mysql.jdbc.exceptions.MySQLNonTransientConnectionException e) {
    		// The connection has gone down (and hopefully recreated, we recreate the prepared statement
    		GameObject.createPreparedStatement(con);
		} catch (SQLException e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BoundingBox getMap() {
		return map;
	}
    
    
}
