package no.hiof.mobapp.foxhunt.server;

import java.io.EOFException;
import java.io.Serializable;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import no.hiof.mobapp.foxhunt.game.*;

public class StatBuilder implements Runnable, Serializable
{
	/**
	 * 
	 */
	// Mapping between objectID and the gameObject
	private HashMap<Integer, LatLongCoord> gameObjectsTmp;
    private HashMap<Integer, LatLongCoord> gameObjects;
    private TreeMap<Integer, Double> gameObjectsCumDist;
    
    
    
	public static enum eMode { REPLAY };
	
	private static final long serialVersionUID = 2148876645646392439L;
	//transient public World world;
	transient private Connection con = null;
	boolean running = true;
	
	public static PreparedStatement dbLogSelect;
	
	private eMode mode = eMode.REPLAY;
	
	
	int simulationID = -1;

	public StatBuilder() {
		this.gameObjects = new HashMap<Integer, LatLongCoord>(15);
		this.gameObjectsTmp = new HashMap<Integer, LatLongCoord>(15);
		gameObjectsCumDist = new TreeMap<Integer, Double>();
	}
	
	public void connect()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
	    	String url = "jdbc:mysql://localhost:3306/wmp";

		    con = DriverManager.getConnection(url,"root", "");
    
		    //System.out.println("Simulation::Creating New Database Connection");
			//System.out.println("URL: " + url);
			//System.out.println("Connection: " + con);
			
			// If the link went down all of the prepared statements needs to be recreated
			GameObject.createPreparedStatement(con); // For now we only have prepared statements in the gameobject for logging purposes
			
			createPreparedStatement(con);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public String getSimulationProperty(String property)  {
		
		 return getSimulationProperty(property, simulationID);
	}
	
    public String getSimulationProperty(String property, int simID) {
		try {
			Statement stmt = con.createStatement();
			String query = "SELECT value FROM SimulationProperty WHERE SimulationID='" + simID + "' AND property='" + property + "';";
			//System.out.println(query);
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				//System.out.println(rs.getString(1));
				return rs.getString(1);
			}
		} catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			return getSimulationProperty(property, simID);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
    
    public void removeSimulationProperty(String property, int simID) {
		try {
			Statement stmt = con.createStatement();
			String query = "DELETE FROM SimulationProperty WHERE SimulationID='" + simID + "' AND property='" + property + "';";
			//System.out.println(query);
			stmt.executeUpdate(query);

		} catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			removeSimulationProperty(property, simID);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public void setSimulationProperty(String property, String value) {
    	setSimulationProperty(property, value, this.simulationID);
    }
    
    public void setSimulationProperty(String property, String value, int simID) {
		try {
			Statement stmt = con.createStatement();
			String query = "REPLACE INTO SimulationProperty (SimulationID, property, value) VALUES ("+ simID +", '"+ property +"' , '"+ value +"');";
			System.out.println(query);
			stmt.executeUpdate(query);

		} catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			setSimulationProperty(property, value, simID);
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			System.exit(100);
		} 

		return;
	}
    
    
    public class SimulationObject implements Serializable{
        /**
		 * 
		 */
		private static final long serialVersionUID = 4465164780397432719L;
		
		int _id = -1;
        String _name;

        public SimulationObject(int _id, String _name) {
            this._id = _id;
            this._name = _name;
        }
        
        public SimulationObject() {
        }
        
        public int getID() { return _id; }

        public String getName() { return this._name; }
        public void setName(String _name)
        {
        	this._name = _name;
        	//if (_id != -1) setSimulationProperty("name", _name);
        }
    	/**
    	 * @see java.lang.Object#equals(java.lang.Object)
    	 */
    	public boolean equals(Object obj)
    	{
    		if (obj == this)
    		{
    			return true;
    		}
    		if (obj == null)
    		{
    			return false;
    		}
    		if (obj instanceof SimulationObject)
    		{
    			SimulationObject other = (SimulationObject)obj;
    			return other.getID() == this._id;
    		}
    		else
    		{
    			return false;
    		}
    	}
    }
    
    public class DBObject implements Serializable{
        /**
		 * 
		 */
		private static final long serialVersionUID = 3365164780397432719L;
		
		int _id = -1;
        String _className;
        String _name;

        public DBObject(int _id, String _className, String _name) {
            this._id = _id;
            this._className = _className;
            this._name = _name;
        }
        
        public DBObject() {
        }
        
        public int getID() { return _id; }

        public String getName() { return this._name; }
        public void setName(String _name)
        {
        	this._name = _name;
        	if (_id != -1) setSimulationObjectProperty(_id, "name", _name);
        }

        public String getClassName() { return _className; }
        public void setClassName(String _className) {
        	this._className = _className;
        	if (_id != -1)  setSimulationObjectProperty(_id, "ClassName", _name);
        }
        
        public void setSimProperty(String property, String value)
        {
        	if (_id != -1)  setSimulationObjectProperty(_id, property, value);
        }
        
        public String getSimProperty(String property)
        {
        	if (_id != -1)  return getSimulationObjectProperty(_id, property);
        	return null;
        }
        
        public void addToSimulation()
        {
        	addSimulationObject(_id);
        }
        
    }
    
    
    public List<DBObject> getSimulationObjects()
    {
    	Vector<DBObject> results = new Vector<DBObject>(10);
    	
    	//HashMap<Integer, String> results = new HashMap<Integer,String>(10);
    	
		try
		{
			Statement s2 = con.createStatement();
			ResultSet r2 = s2.executeQuery (					
					"select * FROM SimulationObject as so LEFT JOIN "+
						" (SELECT * FROM ("+
						
								"(SELECT objectId, property, value "+
								" FROM SimulationObjectProperty "+
								" WHERE SimulationObjectProperty.property='ClassName' )"+
								" UNION "+
						
								"(SELECT objectId, property, value "+
								" FROM ObjectProperty "+
								" WHERE ObjectProperty.property ='ClassName' )"+
						") AS propertytable "+
						" GROUP BY objectId) as sop "+
					" ON so.objectId=sop.objectId "+
					" WHERE so.SimulationID = " + this.simulationID +";");
			
			while ( r2.next() )
			{
				String name = getSimulationObjectProperty(r2.getInt("objectId"), "name");
				
				results.add(new DBObject( r2.getInt("objectId"), r2.getString("value"), name)  );
			}
			
			s2 = null;
			r2.close();

		} catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			return getSimulationObjects();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return (List<DBObject>)results;
    }
    
    
    public String getSimulationObjectProperty(int objectId, String property) {
    	
    	String result = null;
    	
		try
		{
			Statement s = con.createStatement();
			
			String query = "select * FROM SimulationObject as so LEFT JOIN "
					+ " (SELECT * FROM (" +

					"(SELECT objectId, property, value "
					+ " FROM SimulationObjectProperty "
					+ " WHERE SimulationObjectProperty.property='" + property + "'"
					+ " AND SimulationObjectProperty.SimulationID=" + this.simulationID
					+ " )" + " UNION " +

					"(SELECT objectId, property, value "
					+ " FROM ObjectProperty "
					+ " WHERE ObjectProperty.property ='" + property + "' )"
					+ ") AS propertytable " + " GROUP BY objectId) as sop "
					+ " ON so.objectId=sop.objectId "
					+ " WHERE so.SimulationID = " + this.simulationID
					+ " AND so.objectId=" + objectId + ";";
			
			//System.out.println(query);
			ResultSet r = s.executeQuery (query);
    	
			while ( r.next() )
			{
				result = r.getString("value");
			}
			
			s = null;
			r.close();

		} catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			return getSimulationObjectProperty(objectId, property);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
    	
    	return result;
    }
    
    public void setSimulationObjectProperty(int objectId, String property, String value){
    	setSimulationObjectProperty(objectId, property, value, this.simulationID);
	}
    
    public void setSimulationObjectProperty(int objectId, String property, String value, int simID)  {
		try {
			Statement stmt = con.createStatement();
			String query = "REPLACE INTO SimulationObjectProperty (SimulationID, objectId, property, value) VALUES ("+ simID +", "+ objectId +", '"+ property +"' , '"+ value +"');";
			//System.out.println(query);
			stmt.executeQuery(query);

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			setSimulationObjectProperty(objectId, property, value, simID);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}

		return;
	}
    
    public void addSimulationObject(int objectId) {
		try {
			Statement stmt = con.createStatement();

			String query = "REPLACE INTO `SimulationObject` (`SimulationID`, `objectId`) VALUES " +
			"("+ this.simulationID+", "+objectId +");";
			
			//System.out.println(query);
			stmt.executeQuery(query);

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			addSimulationObject(objectId);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
    	
    }
    
    public void removeSimulationObject(int objectId) {
		try {
			Statement stmt = con.createStatement();

			String query = "DELETE FROM `SimulationObject` WHERE SimulationID="+ this.simulationID +" AND objectId="+ objectId +";";
			
			System.out.println(query);
			stmt.executeUpdate(query);

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			removeSimulationObject(objectId);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
    	
    }
    
    public void removeSimulation(int simulationId)
    {
		try {
			Statement stmt = con.createStatement();

			String query = "DELETE FROM `Simulation` WHERE id="+ this.simulationID +";";
			
			System.out.println(query);
			stmt.executeUpdate(query);

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			removeSimulation(simulationId);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
    	
    }
    
    public void removeSimulationObjectsAndProperties(int simulationId)
    {
		try {
			Statement stmt = con.createStatement();

			String query = "DELETE FROM `SimulationObjectProperty` WHERE SimulationID="+ this.simulationID +";";
			
			System.out.println(query);
			stmt.executeUpdate(query);
			
			query = "DELETE FROM `SimulationObject` WHERE SimulationID="+ this.simulationID +";";
			
			System.out.println(query);
			stmt.executeUpdate(query);
			

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			removeSimulationObjectsAndProperties(simulationId);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
    	
    }
    
    public void removeSimulationObjectProperties(int objectId)
    {
		try {
			Statement stmt = con.createStatement();

			String query = "DELETE FROM `SimulationObjectProperty` WHERE SimulationID="+ this.simulationID +" AND objectId="+ objectId +";";
			
			System.out.println(query);
			stmt.executeUpdate(query);

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			removeSimulationObjectProperties(objectId);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
    	
    }

    public String getObjectProperty(String id, String property)
    {
        try
              {
                        Statement stmt = con.createStatement ();
                        String query = "SELECT value FROM ObjectProperty WHERE id='"+id+"' AND property='"+property+"';";
                         ResultSet rs = stmt.executeQuery (query);

                        while ( rs.next() )
                        {
                                return rs.getString(1);
            }
                }catch (com.mysql.jdbc.CommunicationsException e) {
        			// Link went down, re-establish
        			connect();
        			// Try to do the query again
        			return getObjectProperty(id, property);
        			
        		}
                
                catch(Exception e)
                {
                        e.printStackTrace();
                }

        return null;
    }

    public List<SimulationObject> getSimulations()
    {
    	Vector<SimulationObject> results = new Vector<SimulationObject>(10);
    	
    	//HashMap<Integer, String> results = new HashMap<Integer,String>(10);
    	
		try
		{
			Statement s2 = con.createStatement();
			ResultSet r2 = s2.executeQuery (					
					"select * FROM Simulation ORDER BY ID ASC;");
			
			while ( r2.next() )
			{	
				results.add(new SimulationObject(r2.getInt("id"), r2.getString("name")));
			}
			
			s2 = null;
			r2.close();

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			return getSimulations();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return (List<SimulationObject>)results;
    }
    
    public int getSimulationID() {
		return simulationID;
	}

	public void setSimulationID(int simulationID) {
		this.simulationID = simulationID;
	}

	public void initSimulation()
    {
		// Check if simulationID is -1, this means we want to init the last created simulation
		if (simulationID == -1)
		{
			List<SimulationObject> sims = getSimulations();
			
			this.simulationID = sims.get(sims.size()-1).getID();
		}
		
    	//initSimulation(simulationID); // Default is 1
    }
    
	/*
	public void initSimulation(int simID)
	{
		double xmin = Double.parseDouble(getSimulationProperty("FENCE_LONMIN",0));
		double xmax = Double.parseDouble(getSimulationProperty("FENCE_LONMAX",0));
		double ymin = Double.parseDouble(getSimulationProperty("FENCE_LATMIN",0));
		double ymax = Double.parseDouble(getSimulationProperty("FENCE_LATMAX",0));
		
		// Set up simulation		
		BoundingBox bbox = new BoundingBox(xmin, ymin, xmax, ymax);
		
		// New Simulation World Object - This holds all the game objects and simulation properties
		world = new World(simID);
		
		World.setMapSize(bbox);
        
		
		// Check the db to see if what we should set logging to
		
		String log = getSimulationProperty("Logging",simID);
		
		if (log != null) {
			World.doLogging = Boolean.parseBoolean(log);
		}
		
        //world.setGameWorldBehaviour(new DefaultWorldBehavior(world));
		
		String behaviorName = "DefaultWorldBehavior";
		
		world.setGameWorldBehaviour(WorldBehavior.buildFromName(behaviorName, world));
        System.out.println("Simulation::PopulatingWord::Start");
        // Populate the world with gameObjects from the database
                  
		try
		{
			Statement s1 = con.createStatement();
			s1.executeUpdate("DELETE FROM Found;");
			
			
			Statement s2 = con.createStatement();
			ResultSet r2 = s2.executeQuery (					
					"select * FROM SimulationObject as so LEFT JOIN "+
						" (SELECT * FROM ("+
						
								"(SELECT objectId, property, value "+
								" FROM SimulationObjectProperty "+
								" WHERE SimulationObjectProperty.property='ClassName' )"+
								" UNION "+
						
								"(SELECT objectId, property, value "+
								" FROM ObjectProperty "+
								" WHERE ObjectProperty.property ='ClassName' )"+
						") AS propertytable "+
						" GROUP BY objectId) as sop "+
					" ON so.objectId=sop.objectId "+
					" WHERE so.SimulationID = " + simID +";");

			
			//"SELECT * FROM Object LEFT JOIN ObjectProperty ON Object.id=ObjectProperty.objectId WHERE ObjectProperty.property='ClassName';");
			
			while ( r2.next() ) 
			{
				GameObject gobject;
		        try {
					// We need to create the gameObject of the class specified in the ObjectProperty.property className property
					String className = "no.hiof.mobapp.foxhunt.gamebehavior." + r2.getString("value");
					//System.out.println("A object from class: " + className + " will be created");

					gobject = (GameObject)Class.forName(className).newInstance();
				
					gobject.dbObjectID = r2.getInt("objectId");
					gobject.dbSimulationID = simID;
					
					gobject.setWorld(world);
					
					gobject.initFromDatabase(con);
					
		            //if (r2.getInt(5) != 1) {
		            	world.addGameObject(gobject);
		            	System.out.println("World->Add(" +className + ")");
		            //}
		        }
		        catch (Exception e) {
		            e.printStackTrace();
		        }	
			}
			s2 = null;
			r2.close();

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			initSimulation(simID);
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("Simulation::PopulatingWord::Finished");
	}
    
    
    public void updateObjects(long deltaMillis)
	{
		double deltaSeconds = (double)deltaMillis / 1000.0;
				
		world.update(deltaSeconds);
		world.updateDatabase(con);
	}
	*/ 
    
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
	
    public void replayObjects(long fromTime, long toTime)
    {
    	// Query the databaselog from: fromTime to: toTime
    	// Parse through the log and update the objects in the world
    	
    	//System.out.println("Updating world with values from db in interval: " + fromTime + " -> " + toTime + " being: " + (toTime-fromTime));
    	
    	try {
    		dbLogSelect.setInt(1, this.simulationID);
			dbLogSelect.setLong(2, fromTime);
			dbLogSelect.setLong(3, toTime);
			//System.out.println("DB-Query: "+dbLogSelect);
			
			ResultSet rs = dbLogSelect.executeQuery();
			
			while (rs.next()) {
				//System.out.println("TS: " + rs.getLong(1) + " SimID: " + rs.getInt(2) + " ObjectID: " + rs.getInt(3) + " K:" + rs.getString(4) + " V:" + rs.getString(5));
				
				int objectId = rs.getInt(3);
				//String objectString = rs.getString(3);
				
				if (objectId < 100 || objectId > 105) {
					continue;
				}
				
				String key = rs.getString(4);
				
				if (key.equals("lat")) {
					if (rs.getDouble(5) == 50.0) {
						continue;
					}
					if (!gameObjectsTmp.containsKey(objectId))
					{
						gameObjectsTmp.put(objectId, new LatLongCoord(rs.getDouble(5), rs.getDouble(5)));
					}
					gameObjectsTmp.get(objectId).lat = rs.getDouble(5);
				} else if (key.equals("lon")) {
					if (rs.getDouble(5) == 11.0) {
						continue;
					}
					gameObjectsTmp.get(objectId).lon = rs.getDouble(5);
					if (!gameObjects.containsKey(objectId))
					{
						gameObjects.put(objectId, new LatLongCoord(gameObjectsTmp.get(objectId).lat, gameObjectsTmp.get(objectId).lon));
					} else {
						if (!gameObjectsCumDist.containsKey(objectId)) {
							gameObjectsCumDist.put(objectId, 0.0);
						}
						gameObjectsCumDist.put(objectId, gameObjectsCumDist.get(objectId) + gameObjects.get(objectId).getDistance(gameObjectsTmp.get(objectId)));
						//System.out.println(this.simulationID + ",\t" + objectId + ",\t" + gameObjectsCumDist.get(objectId) + "\t" + gameObjects.get(objectId).getDistance(gameObjectsTmp.get(objectId)) + "\t" + gameObjects.get(objectId).lat + "\t" + gameObjects.get(objectId).lon);
						//gameObjects.remove(objectId);
						gameObjects.get(objectId).lat = gameObjectsTmp.get(objectId).lat;
						gameObjects.get(objectId).lon = gameObjectsTmp.get(objectId).lon;
					}
					
				}
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

	public void run() {
		
		running = true;
		long timeLast;
		long timeDelta;
		
		switch (mode)
		{			
		case REPLAY:
			// TODO: Check for and/or, catch null exception
			//System.out.println("SimID: " + this.simulationID + " - Run: " + getSimulationProperty("LastRun") + " - Stopped: " + getSimulationProperty("LastStopped"));
			long timeFrom = Long.parseLong(getSimulationProperty("LastRun"));
			long lastStopped = Long.parseLong(getSimulationProperty("LastStopped"));
			
			timeLast = System.currentTimeMillis();
			timeDelta = timeLast;
			
			double factor = 100.0;
			
			while (running) {
				timeDelta = System.currentTimeMillis() - timeLast;
				
				timeDelta *= factor;
				
				timeLast = System.currentTimeMillis();
				
				replayObjects(timeFrom, timeFrom+timeDelta);
				
				if (timeFrom >= lastStopped) {
					stop();
					
					printCumulativeDistances();
				}
				timeFrom += timeDelta;
				//updateObjects(timeDelta);
				/*
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					e.printStackTrace();
				}
				*/
			}
			
			break;
		}
		
		
	}
	
	public void printCumulativeDistances() {
		for (SortedMap.Entry<Integer, Double> entry: gameObjectsCumDist.entrySet())  
		{  
			int key = entry.getKey();  
			Double value = entry.getValue();
			//int length = new Integer(value.toString());
			
			System.out.println(this.simulationID + ",\t" + key + ",\t" + value.intValue());
		}  
	}
	public void init() {
		connect();
		initSimulation();
	}
	
	synchronized public void stop()
	{
		running = false;
	}
	
	public synchronized boolean getState()
	{
		return running;
	}
		
	/**
	 * @return the mode
	 */
	public eMode getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(eMode mode) {
		this.mode = mode;
	}
	
	public static void main(String argv[]) {
		for (int i = 128; i <= 134; i++)
		{
			if (i == 133)
			{
				continue;
			}
			//System.out.println(new LatLongCoord(1.0,1.0).getDistance(new LatLongCoord(10.0,2.0)));
			//System.exit(0);
			StatBuilder s = new StatBuilder();
			s.setMode(eMode.REPLAY);
			s.setSimulationID(i);
			s.init();
			s.run();
		}
	}
}
