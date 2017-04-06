package no.hiof.mobapp.foxhunt.server;

import java.io.EOFException;
import java.io.Serializable;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import no.hiof.mobapp.foxhunt.game.*;

public class Simulation implements Runnable, Serializable
{
	public static enum eMode { SIMULATION, REPLAY };
	private eMode mode = eMode.SIMULATION;
	
	public World world;
	
	boolean running = true;
	
	int simulationID = -1;

	IDBProxy dbp = null;
	private Connection con = null;
    
    public int getSimulationID() {
		return simulationID;
	}

	public void setSimulationID(int simulationID) {
		this.simulationID = simulationID;
	}

	/*
	public void initSimulation()
    {
    	initSimulation(simulationID); // Default is 1
    }
    */
	public void setDBC(Connection c) {
		
		con = c;
	}
	
	public void setDBP(IDBProxy p) {
		
		dbp = p;
	}
	
	public void initSimulation(int simID)
	{
		BoundingBox bbox;
		String fence = dbp.getSimulationProperty("FENCE",0);
		
		if (fence == null) {
			double xmin = Double.parseDouble(dbp.getSimulationProperty("FENCE_LONMIN",0));
			double xmax = Double.parseDouble(dbp.getSimulationProperty("FENCE_LONMAX",0));
			double ymin = Double.parseDouble(dbp.getSimulationProperty("FENCE_LATMIN",0));
			double ymax = Double.parseDouble(dbp.getSimulationProperty("FENCE_LATMAX",0));
			
			// Set up simulation
			bbox = new BoundingBox(xmin, ymin, xmax, ymax);
		} else {
			Vector<LatLongCoord> tmpBound = new Vector<LatLongCoord>(4);
			
			String[] points = fence.split(",");
			System.out.println("Points should be 4*2=8. The actual value is: " + points.length);
			for (int i = 0; i < points.length; i+=2) {
				System.out.println("Adding point ("+ i/2 + ") consisting of ("+ points[i]+ "," + points[i+1] + ")");
				tmpBound.add(new LatLongCoord(points[i], points[i+1]));
			}
			
			bbox = new BoundingBox(tmpBound.toArray(new LatLongCoord[] {}));
		}
		/*
		 * 59.29284,11.05811 , 59.29244,11.05909 , 59.29207,11.05854 , 59.29241,11.05752
		 */
		// New Simulation World Object - This holds all the game objects and simulation properties
		world = new World(simID);
		
		World.setMapSize(bbox);
        
		
		// Check the db to see if what we should set logging to
		
		String log = dbp.getSimulationProperty("Logging",simID);
		
		if (log != null) {
			World.doLogging = Boolean.parseBoolean(log);
		}
		
        //world.setGameWorldBehaviour(new DefaultWorldBehavior(world));
		
		
		String behaviorName = dbp.getSimulationProperty("Behavior",simID);
		if (behaviorName == null) {
			behaviorName = "DefaultWorldBehavior";
		}
		
		
		world.setGameWorldBehaviour(WorldBehavior.buildFromName(behaviorName, world));
        System.out.println("New World, Behavior: " + behaviorName);
        // Populate the world with gameObjects from the database
                  
		try
		{
			//Statement s1 = con.createStatement();
			//s1.executeUpdate("DELETE FROM Found;");
			
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
					String className = r2.getString("value");
					//System.out.println("A object from class: " + className + " will be created");

					gobject = GameObject.buildFromName(className);
				
					gobject.dbObjectID = r2.getInt("objectId");
					gobject.dbSimulationID = simID;
					
					gobject.setWorld(world);
					
					gobject.initFromDatabase(con);
					
		            //if (r2.getInt(5) != 1) {
		            	world.addGameObject(gobject);
		            	String tmp_bev = gobject.getConfigValue("behavior");
		            	System.out.println("World->Add(" +className + "){Behavior: " + ((tmp_bev != null) ? tmp_bev : "Default") + "}");
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
			//connect();
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
    
    public void replayObjects(long fromTime, long toTime)
    {
		world.replay(fromTime, toTime);
		//world.updateDatabase(con);
    }

	public void run() {
		
		running = true;
		long timeLast;
		long timeDelta;
		
		switch (mode)
		{
		case SIMULATION:
			
			dbp.setSimulationProperty("LastRun", Long.toString(System.currentTimeMillis()),this.simulationID );
			dbp.removeSimulationProperty("LastStopped", this.simulationID);
			
			timeLast = System.currentTimeMillis();
			timeDelta = timeLast;
			
			while (running) {
				timeDelta = System.currentTimeMillis() - timeLast;
				timeLast = System.currentTimeMillis();
				
				updateObjects(timeDelta);
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			dbp.setSimulationProperty("LastStopped", Long.toString(System.currentTimeMillis()),  this.simulationID );
			
			break;
			
		case REPLAY:
			// TODO: Check for and/or, catch null exception
			long timeFrom = Long.parseLong(dbp.getSimulationProperty("LastRun"), this.simulationID);
			long lastStopped = Long.parseLong(dbp.getSimulationProperty("LastStopped"), this.simulationID);
			
			timeLast = System.currentTimeMillis();
			timeDelta = timeLast;
			
			double factor = 1.0;
			
			while (running) {
				timeDelta = System.currentTimeMillis() - timeLast;
				timeLast = System.currentTimeMillis();
				
				replayObjects(timeFrom, timeFrom+timeDelta);
				
				timeFrom += timeDelta;
				//updateObjects(timeDelta);
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			break;
		}
		
		
	}
	
	public void init() {
		//connect();
		initSimulation(this.simulationID);
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
		Simulation s = new Simulation();
		s.setMode(eMode.REPLAY);
		s.setSimulationID(1);
		s.init();
		//s.connect();
		
		//
		//s.initSimulation();
		//s.randomizeFoxesAndUpdate();
		
		s.run();
	}
}
