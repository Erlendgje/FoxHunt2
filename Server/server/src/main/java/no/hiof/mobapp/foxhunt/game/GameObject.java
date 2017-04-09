package no.hiof.mobapp.foxhunt.game;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;


public abstract class GameObject implements IDatabaseObject{
    final static int DEFAULT_SEPARATE = 20;
    final static int DEFAULT_DETECT = 40;
    final static double DEFAULT_SPEED = 8;
    
    
    public int dbObjectID = -1;
    public int dbSimulationID = -1;
    
    
    private World world;
    
    // We add all queued updates in a batch so this object can be static
    public static PreparedStatement dbUpdateState;
    
	// The range at which inherited moving gameobjects will separate from other objects with the same groupID.
    // With low values flocks/packs/groups will be tighter, and i.e the foxes in a group will not separate to avoid
    // obstacles or other foxes. With higher values, the group will be spread apart and be more likely split in two 
    // when faced with an obstacle.
    public double separationRange = DEFAULT_SEPARATE;
    
    // The range at which game objects can detect the inherited instances like foxes, food, predators, and obstacles
    public double detectionRange = DEFAULT_DETECT;

    // The current location of this fox.
    protected LatLongCoord location = new LatLongCoord(50,11);
    
	protected GameObjectBehavior behavior;
	
    // The direction that the fox is facing in degrees.
    private int currentTheta;
    
	// The groupID of this gameObject.
    protected int groupID;
    
    // The current speed of the gameObject.
    private double currentSpeed;
    
    // maximum turning radius in degrees.
    private int maxTurnTheta;

    protected boolean hidden = false;
    
    protected HashMap<String, String> config;
    
    private boolean taken = false;
    

    
	@SuppressWarnings("unchecked")
	public static GameObject buildFromName(String objectName)
	{
		
		//System.out.print("Trying to create game object: " + objectName);
			Class<GameObject> goClass;
			try {
				goClass = (Class<GameObject>) Class.forName("no.hiof.mobapp.foxhunt.game.objects." + objectName).asSubclass(GameObject.class);
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return null;
			}
			//Constructor<?> c = goClass.getConstructor(no.hiof.mobapp.foxhunt.game.World.class);
			//GameObject goCon = (GameObject)c.newInstance(world);
			
			//System.out.println(". Success");
			try {
				return goClass.newInstance();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
	}
	/**
     * Creates a default gameobject at a specific location belonging to a group
     *
     * @param  lon Starting lon coordinate of the G.O.
     * @param  lat Starting lat coordinate of the G.O.
     * @param  theta Starting angle of the G.O - the direction the G.O is facing in degrees
     * @param  groupID The gameobjects group
     */
    public GameObject(double lon, double lat, int theta, int groupID) {
        location.lon = lon;
        location.lat = lat;
        currentTheta = theta;
        this.groupID = groupID;
    }
    
    /**
     * This constructor sets a random direction for the gameObject.
     *
     * @param  groupID The gameobjects group
     */
    public GameObject(int groupID) {
    	this.groupID = groupID;
    	this.currentTheta = (int)(Math.random() * 360);
    	
    	this.config = new HashMap<String, String>();
    	/*
        // call the other constructor.
        this( mapBB.c1.lon + (Math.random() * mapBB.width()), 
        		mapBB.c1.lat + (Math.random() * mapBB.height()), 
        		(int)(Math.random() * 360),
        		groupID);
        		*/
    }
    
	/* (non-Javadoc)
	 * @see no.hiof.mobapp.foxhunt.gamebehavior.IDatabaseObject#initFromDatabase(java.sql.Connection)
	 */
	public void initFromDatabase(Connection con) {
		//System.out.println("GameObject->InitFromDatabase()");
		
		try
		{
			Statement s = con.createStatement();
			ResultSet r = s.executeQuery (
					"SELECT *" +
					"FROM ("+
					"		("+
					"		SELECT objectId, property, value"+
					"		FROM SimulationObjectProperty"+
					"		WHERE SimulationObjectProperty.objectId=" + dbObjectID +
					"		AND SimulationObjectProperty.SimulationID=" + dbSimulationID +
					"		)"+
					"		UNION ("+
					"		SELECT objectId, property, value"+
					"		FROM ObjectProperty"+
					"		WHERE ObjectProperty.objectId =" + dbObjectID +
					"		)"+
					"		) AS propertytable"+
					"		GROUP BY property;");
			
			while (r.next()) {
				this.config.put(r.getString("property"), r.getString("value"));
				//System.out.println("GameObject->Config[" + r.getString("property") + ": " + r.getString("value")+"]");
			}
			
			createPreparedStatement(con);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		String behaviorName = config.containsKey("behavior") ? config.get("behavior") : "DefaultGameObjectBehavior";
		setBehavior(GameObjectBehavior.buildFromName(behaviorName, this));
		
		if (config.containsKey("lat") && config.containsKey("lon"))
		{
			setLocation(Double.parseDouble(config.get("lat")), Double.parseDouble(config.get("lon")));
		}
		
		if (config.containsKey("groupID"))
		{
			this.groupID = Integer.parseInt(config.get("groupID"));
		}
		
		if (config.containsKey("speed"))
		{
			setSpeed(Double.parseDouble(config.get("speed")));
		}
		
		if (config.containsKey("turnTheta"))
		{
			setMaxTurnTheta(Integer.parseInt(config.get("turnTheta")));
		}
		
		if (config.containsKey("detectionRange"))
		{
			detectionRange = Double.parseDouble(config.get("detectionRange"));
		}
		
		if (config.containsKey("separationRange"))
		{
			separationRange = Double.parseDouble(config.get("separationRange"));
		}
	}
	
	
	public void updateLog(long timeStamp, int simulationID, int objectID, String property, String value)
	{
		try {		
			// General for this object
			
			// Set time stamp
			dbUpdateState.setLong(1, timeStamp);
			
			// Set the sim- and object-id
			dbUpdateState.setInt(2, simulationID); // Simulation ID
			dbUpdateState.setInt(3, objectID);
			
			// Latitude Longitude
			dbUpdateState.setString(4, property); // TODO: Verify out of memory.
			dbUpdateState.setString(5, value);
			
			// Add the query to the batch
			dbUpdateState.addBatch();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void createPreparedStatement(Connection con) {
		// Create the update statement
		//dbUpdateState= con.prepareStatement("UPDATE Object SET lon= ? , lat= ? , hidden= ?  WHERE id= ? ");
		try {
			dbUpdateState= con.prepareStatement("INSERT INTO SimulationObjectLog (Timestamp, SimulationID, ObjectID, property, value) VALUES ( ? , ? , ? , ? , ? );");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateDatabase(Connection con) {

		if (World.doLogging == false){
			return;
		}
		
		long currentTime = System.currentTimeMillis();
		
		// Latitude Longitude
		updateLog(currentTime, this.dbSimulationID, dbObjectID, "lon", Double.toString(location.lon));
		updateLog(currentTime, this.dbSimulationID, dbObjectID, "lat", Double.toString(location.lat));
		
		
		// Hidden or not
		//updateLog(currentTime, this.dbSimulationID, dbObjectID, "hidden", (isHidden() ? "1":"0"));		
	}
    
    /**
	 * @param behavior the behavior to set
	 */
	public void setBehavior(GameObjectBehavior behavior) {
		this.behavior = behavior;
	}

	/**
     * Causes the GameObject to attempt to face a new direction.
     * Based on maxTurnTheta, the GameObject may not be able to complete the turn.
     *
     * @param  dT steptime
     * @param  newHeading The direction in degrees that the fox should turn toward.
     */
    public void move(double dT, LatLongCoord target) {
    	behavior.move(dT, target);
    }
     
	/**
     * Causes the GameObject to attempt to face a new direction.
     * Based on maxTurnTheta, the GameObject may not be able to complete the turn.
     *
     * @param  dT steptime
     */
    public void move(double dT) {
    	behavior.move(dT);
    }
     
    
    /**
     * Get the distance in pixels between this fox and another
     *
     * @param  otherFox The other fox to measure the distance between
     * @return The distance to the other fox
     */
    public double getDistance(GameObject otherObject) {
    	return location.getDistance(otherObject.location);
    }
    
    public int getBearing(GameObject otherObject) {
    	return location.getBearing(otherObject.location);
    }
    
    /**
     * Get the distance in pixels between this fox and a point
     *
     * @param p The point to measure the distance between
     * @return The distance between this fox and the point
     */
    public int getDistance(LatLongCoord gpsc) {
        double dX = gpsc.lon - location.lon;
        double dY = gpsc.lat - location.lat;
        
        return (int)Math.sqrt( Math.pow( dX, 2 ) + Math.pow( dY, 2 ));
    }
 
    /**
     * Get the current direction that the fox is facing
     *
     * @return The Maximum Theta for this fox
     */
    public int getMaxTurnTheta() {
        return maxTurnTheta;
    }
    
    /**
     * Set the maximum turn capability of the fox for each movement.
     *
     * @param  theta The new maximum turning theta in degrees
     */
    public void setMaxTurnTheta(int theta)
    {
        maxTurnTheta = theta;
    }
    
    /**
     * Get the current direction of this fox
     *
     * @return  The direction that this fox is facing
     */
    public int getTheta() {
        return currentTheta;
    }
    
    /**
	 * @param theta the theta to set
	 */
	public void setTheta(int theta) {
		this.currentTheta = theta;
	}

    /**
     * Get the current location of this fox
     *
     * @return  The location of this fox
     */
    public LatLongCoord getLocation() {
        return location;
    }

    /**
	 * @param location the location to set
	 */
	public void setLocation(LatLongCoord location) {
		this.location = location;
	}
	
    /**
	 * @param location the location to set (no new object)
	 */
	public void setLocation(Double lat, Double lon) {
		this.location.lat = lat;
		this.location.lon = lon;
	}
	
	

    /**
     * Set the current speed of the fox
     *
     * @param  speed The new speed for the fox
     */
    public void setSpeed( double speed ) {
        currentSpeed = speed;
    }
    
    /**
	 * @return the currentSpeed
	 */
	public double getSpeed() {
		return currentSpeed;
	}

	/**
     * Get the groupID
     *
     * @return  The the groupID
     */
    public int getGroupID() {
        return groupID;
    }
    
	/**
     * Get a configuration string
     *
     * @return  The the groupID of this fox
     */
    public String getConfigValue(String key) {
		return config.get(key);
	}
    
	/**
	 * @return the hidden
	 */
	public boolean isHidden() {
		return hidden;
	}
	
	public boolean isTaken(){
		return taken;
	}
	
	public void setTaken(boolean taken){
		this.taken = taken;
	}
	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	public String toString()
	{
		return ""+this.dbObjectID;
	}

	/**
	 * @return the world
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * @param world the world to set
	 */
	public void setWorld(World world) {
		this.world = world;
	}
}
