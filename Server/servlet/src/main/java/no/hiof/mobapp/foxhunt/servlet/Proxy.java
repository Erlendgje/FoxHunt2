package no.hiof.mobapp.foxhunt.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.World;
import no.hiof.mobapp.foxhunt.server.IDBProxy;
import no.hiof.mobapp.foxhunt.server.Simulation;
//import no.hiof.mobapp.foxhunt.server.Simulation.DBObject;
//import no.hiof.mobapp.foxhunt.server.Simulation.SimulationObject;
import no.hiof.mobapp.foxhunt.server.Simulation.eMode;

import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;

public abstract class Proxy extends Model implements IDBProxy {
	
	transient private WicketApplication wa;
	transient private Connection con = null;
	
	transient public Simulation sim;
	transient public Thread tsim;
	
	public eMode simulationMode = eMode.SIMULATION;
	
	//public Simulation.SimulationObject simulationObject;
	public SimulationObject selectSimulationObject;
	
	//public int simulationID = -1;
	public int GROUPID = -1;
	public String defaultWorldBehavior = "DefaultWorldBehavior";
	
	// Models to fetch data from the simulation
	public Model simulationObjectsModel = new Model()
	{	
		public Object getObject()
		  {
			  return getSimulations();
		  }
	};
	
	public  LoadableDetachableModel mod = new LoadableDetachableModel() { 
		protected Object load() {
			return wa.proxy.getSimulations();
		}
	};
	
	public void setWAP(WicketApplication wa) {
		this.wa = wa;
		
	}
	
	public void init() {
		
		selectSimulationObject = getSimulationObjectByID(-1, simulationObjectsModel);
	}
	
	public SimulationObject getSimulationObjectByID(int id, Model list)
	{
		// If id = -1 return the last simulation object
		Iterator<SimulationObject> it = ((List<SimulationObject>)list.getObject()).iterator();
		
		while (it.hasNext())
		{
			SimulationObject cur = it.next();
			
			if (id == -1 && !it.hasNext())
			{
				System.out.println("getSimObjectByID returns: " + cur.getID());
				return cur;
			}
			
			if (cur.getID() == id)
			{
				System.out.println("getSimObjectByID returns: " + cur.getID());
				return cur;
			}
		}
		
		System.out.println("getSimObjectByID returns: null!!");
		return null;
	}
	
	public void createSimulation() {
		// Set the correct mounts for the simulation
		checkSimAndFix();
		
		// Get the simulation
		selectSimulationObject = getSimulationObjectByID(sim.getSimulationID(), simulationObjectsModel);
	}
	
	public int dbCreateNewSimulation(String name)
	{
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = null;

			String query = "INSERT INTO Simulation (name) VALUES ('"+ name +"');";
			//System.out.println(query);
			stmt.executeUpdate(query);

		    int autoIncKeyFromApi = -1;

		    rs = stmt.getGeneratedKeys();

		    if (rs.next()) {
		        autoIncKeyFromApi = rs.getInt(1);
				setSimulationProperty("LastInit", Long.toString(System.currentTimeMillis()), autoIncKeyFromApi);
				setSimulationProperty("Logging", "true", autoIncKeyFromApi);
				setSimulationProperty("ShowGPSPos", "false", autoIncKeyFromApi);

		        return autoIncKeyFromApi;
		    } else {
		        // throw an exception from here
		    }
		    
		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			return dbCreateNewSimulation(name);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
		return -1;
	}
	
	public void simStop()
	{
		if (tsim == null || sim == null) {
			// If either the simulation thread or the simulation is null the simulation is dead and we don't need to stop it.
			return;
			/*
			// Since we are actually dependent of having a simulation in the backend to query we call checkSimAndFix to fix this.
			checkSimAndFix();
			*/
		}
		
		// Is the thread running if so we can assume that the simulation is set and we can signal the stop flag in it.
		if (tsim.isAlive())
		{
			sim.stop();
		}
	}
	
	public void simSpawnThread()
	{
		// Spawn a new thread for a future run
		tsim = new Thread(sim);
	}
	
	public void checkSimAndFix()
	{
		// Check to see if the selected Simulation Object exist,
		// if not this means there does not exist any simulations in db
		// As a service to the user we create the first one
		if (selectSimulationObject == null) {
			simNew();
			return;
		}
		
		// For some reason the simulation is not created, create it.
		if (sim == null)
		{
			// Create new simulation
			sim = new Simulation();
			
			// Set DB
			sim.setDBC(con);
			
			// Set the databaseProxy to use
			sim.setDBP(this);
			
			// Set the new simulationID
			sim.setSimulationID(selectSimulationObject.getID());

			// Initialize the simulation
			sim.init();
		}
		
		// For some reason the simulation thread is not created, create it.
		if (tsim == null || !tsim.isAlive())
		{
			// The old thread is dead or used, create a new and start it
			simSpawnThread();
		}
		
		//current_behavior = sim.world.getGameWorldBehaviour().getClass().getSimpleName();
		//System.out.println("Behavior name: " + current_behavior);
		/*
		unmount("/getStateSmall");
		unmount("/getState");
		unmount("/getConfig");
		
		if (current_behavior.equals(configFoxhunt.defaultBehavior))
		{
			setConfig(configFoxhunt, this);
		} else /* if .. */ 
		/*{
			setConfig(configTag, this);
		}
*/
	}
	
	public void simNullify() {
		sim = null;
		tsim = null;
	}
	
	public void simStart() {
		// Check that the state of the simulation and threads are ok before continuing
		checkSimAndFix();
		
		// The simulation is already running, don't try to run two simulations at once.
		if (tsim.isAlive())
		{
			return;
		}
		
		// Set simulation mode to REPLAY and not SIMULATION
		sim.setMode(simulationMode);
		
		// All the previous checks have passed, we can now start the simulation.
		tsim.start();
	}
	
	public void simRepopulate() {
		
		// Stop current simulation
		simStop();
		
		removeSimulationObjectsAndProperties(selectSimulationObject.getID());
		System.out.println("wa: " + wa);
		populateWith(wa.currentConfig.defaultLargePopulation);
		System.out.println("wa.currentConfig: " + wa.currentConfig);
		
		// Nullify the current simulation (to force a refresh)
		simNullify();
		
		// Force refresh
		checkSimAndFix();
	}
	
	public void simRepopulateSmall() {
		
		// Stop current simulation
		simStop();
		
		removeSimulationObjectsAndProperties(selectSimulationObject.getID());
		populateWith(wa.currentConfig.defaultSmallPopulation);
		
		// Nullify the current simulation (to force a refresh)
		simNullify();
		
		// Force refresh
		checkSimAndFix();
	}
	
	public void simNew() {
		// Stop current simulation
		simStop();
		
		selectSimulationObject = getSimulationObjectByID(dbCreateNewSimulation("Dummy"), simulationObjectsModel);
		
		
		// Nullify the current simulation (to force a refresh)
		simNullify();
		
		// Force refresh
		checkSimAndFix();
		
		// Add some default properties (from here as they are not related to the simulation, but the mobile client
		setSimulationProperty("ShowOtherHunters", "true", sim.getSimulationID());
		setSimulationProperty("ShowPoints", "true", sim.getSimulationID());
		
		// Since the new simulation is empty populate it with the default population (foxes, hunters)
		simRepopulate();
		
		
	}
	
	public void simDelete() {
		// Stop current simulation
		simStop();
		
		// If we for some reason haven't got a selected/current simulation don't try to delete it
		// Simulation 1 should never be deleted
		
		if (selectSimulationObject == null || selectSimulationObject.getID() == 1)
		{
			return;
		}
		
		// Delete from database
		removeSimulationObjectsAndProperties(selectSimulationObject.getID());
		removeSimulation(selectSimulationObject.getID());
		
		
		selectSimulationObject = getSimulationObjectByID(1, simulationObjectsModel);
		
		// Nullify the current simulation (to force a refresh)
		simNullify();
		
		// Force refresh
		checkSimAndFix();
		
	}
	
	/**
	 * 
	 */
	public void connect()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
	    	String url = "jdbc:mysql://" + WicketApplication.DBURL;

		    con = DriverManager.getConnection(url,WicketApplication.DBUser, WicketApplication.DBPass);
    
		    System.out.println("Creating New Database Connection");
			System.out.println("URL: " + url);
			System.out.println("Connection: " + con);
			
			// If the link went down all of the prepared statements needs to be recreated
			GameObject.createPreparedStatement(con);
			World.createPreparedStatement(con);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
    public boolean DBInstalled() {
		try {
			Statement stmt = con.createStatement();
			//String query = "SELECT case WHEN EXISTS((SELECT * FROM information_schema.tables WHERE table_name = SimulationObject)) THEN 1 ELSE 0;";
			//String query = "SELECT value FROM SimulationProperty WHERE SimulationID='" + simID + "' AND property='" + property + "';";
			//String query = "SELECT COUNT(*) FROM information_schema.tables  WHERE table_schema = '" + WicketApplication.DBURL + "' AND table_name = 'another_test';";
			String query = "SELECT COUNT(*) FROM information_schema.tables  WHERE table_name = 'SimulationObject';";
			//System.out.println(query);
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				return rs.getBoolean(1);
			}
		} catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			return DBInstalled();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
    
    public void DBInstall() {
    	// Read SQL Setupfile (DDL)
		try {
		     try {
		       System.out.println ("Current dir : " + new File (".").getCanonicalPath());
		       }
		     catch(Exception e) {
		       e.printStackTrace();
		       }

			SQLScript s = new SQLScript(new BufferedReader(new FileReader("dbstructure.sql")), con);
			SQLScript d = new SQLScript(new BufferedReader(new FileReader("dbdata.sql")), con);
			
			s.loadScript();
			s.execute();
			
			d.loadScript();
			d.execute();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
    	


	public String getSimulationProperty(String property)  {
		
		 return getSimulationProperty(property, selectSimulationObject.getID());
	}
	
    public String getSimulationProperty(String property, int simID) {
		try {
			Statement stmt = con.createStatement();
			String query = "SELECT value FROM SimulationProperty WHERE SimulationID='" + simID + "' AND property='" + property + "';";
			//System.out.println(query);
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
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
    	setSimulationProperty(property, value, selectSimulationObject.getID());
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
					" WHERE so.SimulationID = " + selectSimulationObject.getID() +";");
			
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
					+ " AND SimulationObjectProperty.SimulationID=" + selectSimulationObject.getID()
					+ " )" + " UNION " +

					"(SELECT objectId, property, value "
					+ " FROM ObjectProperty "
					+ " WHERE ObjectProperty.property ='" + property + "' )"
					+ ") AS propertytable " + " GROUP BY objectId) as sop "
					+ " ON so.objectId=sop.objectId "
					+ " WHERE so.SimulationID = " + selectSimulationObject.getID()
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
    	setSimulationObjectProperty(objectId, property, value, selectSimulationObject.getID());
	}
    
    public void setSimulationObjectProperty(int objectId, String property, String value, int simID)  {
		try {
			Statement stmt = con.createStatement();
			String query = "REPLACE INTO SimulationObjectProperty (SimulationID, objectId, property, value) VALUES ("+ simID +", "+ objectId +", '"+ property +"' , '"+ value +"');";
			System.out.println(query);
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
			"("+ selectSimulationObject.getID()+", "+objectId +");";
			
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

			String query = "DELETE FROM `SimulationObject` WHERE SimulationID="+ selectSimulationObject.getID() +" AND objectId="+ objectId +";";
			
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
    
    public void removeSimulation(int simulationID)
    {
		try {
			Statement stmt = con.createStatement();

			String query = "DELETE FROM `Simulation` WHERE id="+ simulationID +";";
			
			System.out.println(query);
			stmt.executeUpdate(query);

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			removeSimulation(simulationID);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
    	
    }
    
    public void removeSimulationObjectsAndProperties(int simulationID)
    {
		try {
			Statement stmt = con.createStatement();

			String query = "DELETE FROM `SimulationObjectProperty` WHERE SimulationID="+ simulationID +";";
			
			System.out.println(query);
			stmt.executeUpdate(query);
			
			query = "DELETE FROM `SimulationObject` WHERE SimulationID="+ simulationID +";";
			
			System.out.println(query);
			stmt.executeUpdate(query);
			

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			removeSimulationObjectsAndProperties(simulationID);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
    	
    }
    
    public void removeSimulationObjectProperties(int objectId)
    {
		try {
			Statement stmt = con.createStatement();

			String query = "DELETE FROM `SimulationObjectProperty` WHERE SimulationID="+ selectSimulationObject.getID() +" AND objectId="+ objectId +";";
			
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
    
	/**
	 * Update the database and set the default foxes for the simulation
	 */
	public void populateWith(int[] objectArray)
	{
		try {
			Statement stmt = con.createStatement();

			String query = "REPLACE INTO `SimulationObject` (`SimulationID`, `objectId`) VALUES ";
			
			for (int i = 0; i < objectArray.length; i++)
			{
				query += "("+ selectSimulationObject.getID()+", " + objectArray[i] + ") ";
				query += (i == objectArray.length-1) ? "" : ",";
			}
			query += ";";
			
			System.out.println(query);
			stmt.executeUpdate(query);

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			populateWith(objectArray);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
	}
	
	/**
	 * Update the database and set the default foxes for the simulation
	 */
	public void populateWithDefaults()
	{
		try {
			Statement stmt = con.createStatement();

			String query = "REPLACE INTO `SimulationObject` (`SimulationID`, `objectId`) VALUES " +
			"("+ selectSimulationObject.getID()+", 1), "+
			"("+ selectSimulationObject.getID()+", 2), "+
			"("+ selectSimulationObject.getID()+", 3), "+
			"("+ selectSimulationObject.getID()+", 4), "+
			"("+ selectSimulationObject.getID()+", 5), "+
			"("+ selectSimulationObject.getID()+", 6), "+
			"("+ selectSimulationObject.getID()+", 7), "+
			"("+ selectSimulationObject.getID()+", 8), "+
			"("+ selectSimulationObject.getID()+", 9), "+
			"("+ selectSimulationObject.getID()+", 10), "+
			"("+ selectSimulationObject.getID()+", 11)," +
			"("+ selectSimulationObject.getID()+", 12), "+
			"("+ selectSimulationObject.getID()+", 13), "+
			"("+ selectSimulationObject.getID()+", 14), "+
			"("+ selectSimulationObject.getID()+", 15), "+
			"("+ selectSimulationObject.getID()+", 16), "+
			"("+ selectSimulationObject.getID()+", 17), "+
			"("+ selectSimulationObject.getID()+", 18)," +
			"("+ selectSimulationObject.getID()+", 100), "+
			"("+ selectSimulationObject.getID()+", 101), "+
			"("+ selectSimulationObject.getID()+", 102), "+
			"("+ selectSimulationObject.getID()+", 103), "+
			"("+ selectSimulationObject.getID()+", 104), "+
			"("+ selectSimulationObject.getID()+", 500);";
			
			//System.out.println(query);
			stmt.executeUpdate(query);

		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			populateWithDefaults();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(100);
		}
	}
	
	public void randomizeFoxesAndUpdate()
	{
		double xmin = Double.parseDouble(getSimulationProperty("FENCE_LONMIN"));
		double xmax = Double.parseDouble(getSimulationProperty("FENCE_LONMAX"));
		double ymin = Double.parseDouble(getSimulationProperty("FENCE_LATMIN"));
		double ymax = Double.parseDouble(getSimulationProperty("FENCE_LATMAX"));
		
		Statement s;
		try {
			s = con.createStatement();

			ResultSet r = s.executeQuery ("SELECT * FROM Object LEFT JOIN ObjectProperty ON Object.id=ObjectProperty.objectId " +
					"WHERE ObjectProperty.property='ClassName' AND " +
					"ObjectProperty.value='Fox' OR ObjectProperty.value='Predator'");
			
			while ( r.next() ) 
			{
				Statement s2 = con.createStatement();
				System.out.println("Randomzing placement for object with ID: " + r.getInt("id"));
				
				double newlon = xmin + Math.random()*(xmax-xmin);
				double newlat = ymin + Math.random()*(ymax-ymin);
				
				s2.executeUpdate("UPDATE ObjectProperty SET value=" + newlat + " WHERE property='lat' AND objectId="+r.getInt("id"));
				s2.executeUpdate("UPDATE ObjectProperty SET value=" + newlon + " WHERE property='lon' AND objectId="+r.getInt("id"));
				
				s2.close();
				s2 = null;
			}
			s = null;
			r.close();
		}catch (com.mysql.jdbc.CommunicationsException e) {
			// Link went down, re-establish
			connect();
			// Try to do the query again
			randomizeFoxesAndUpdate();
			
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public World getWorld() {
		// TODO: Check stuff
		if (this.sim == null)
			return null;
		
		return this.sim.world;
	}
}
