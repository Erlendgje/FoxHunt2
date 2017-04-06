package no.hiof.mobapp.foxhunt.servlet;

import java.sql.*;
import java.util.*;

//import no.hiof.mobapp.foxhunt.servlet.wicket;
import no.hiof.mobapp.foxhunt.game.*;
import no.hiof.mobapp.foxhunt.server.Simulation;
import no.hiof.mobapp.foxhunt.server.Simulation.eMode;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebApplication;

class GameConfig {
	protected String gameName;
	protected int gameGroup;
	protected String worldBehavior;
	protected Class getConfig;
	protected Class getStateSmall;
	protected Class getState;
	protected Class proxy;
	
	protected String worldBehaviors[];
	protected String individualBehaviors[];
	
	protected String gameObjects[];
	
	protected int defaultSmallPopulation[];
	protected int defaultLargePopulation[];
	
	public GameConfig (String gameName, int gameGroup, int smallPop[], int largePop[], Class proxy, Class config, Class stateSmall, Class state)
	{
		this.gameName = gameName;
		this.gameGroup = gameGroup;
		getConfig = config;
		getStateSmall = stateSmall;
		getState = state;
		this.proxy = proxy;
		
		defaultSmallPopulation = smallPop;
		defaultLargePopulation = largePop;
	}
	
	
}

/*
mountBookmarkablePage("/getConfig", GetConfig.class);
mountBookmarkablePage("/getStateSmall", GetStateSmall.class);
mountBookmarkablePage("/getState", GetState.class);
} else {
mountBookmarkablePage("/getConfig", GetConfigTag.class);
mountBookmarkablePage("/getStateSmall", GetStateTagSmall.class);
mountBookmarkablePage("/getState", GetStateTag.class);
*/

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start class.
 * 
 * @see wicket.myproject.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	GameConfig configFoxhunt = new GameConfig("Foxhunt", 0, 
			new int[] {1,2,3,4,100,101,102,103,104,500}, 
			new int[] {1,2,3,4,100,101,102,103,104,500},
			ProxyFoxHunt.class, GetConfig.class, GetStateSmall.class, GetState.class);
	GameConfig configTag = configFoxhunt;
		/*
		new GameConfig("Tag", 1,
			new int[] {200,201,202,203,204},
			new int[] {200,201,202,203,204},  
			ProxyFoxHunt.class, GetConfigTag.class, GetStateTagSmall.class, GetStateTag.class);
	*/
	
	// A proxy to the original simulation
	public GameConfig currentConfig= configFoxhunt;
	
	public Proxy proxy; 
	
	static public String DBURL = "localhost:3306/wmp";
	static public String DBUser = "root";
	static public String DBPass = "";
	boolean needDBConfig = false;
	
    /**
     * Constructor
     */
	public WicketApplication()
	{
		// Database settings for the Simulation Engine
		//Simulation.DBURL = "localhost:3306/wmp";
		//Simulation.DBUser = "wmp";
		//Simulation.DBPass = "wmp5!";
		configFoxhunt.worldBehaviors = new String[] { "DefaultWordBehavior" };
		configFoxhunt.individualBehaviors = new String[] { "DefaultGameObjectBehavior", "DummyGameObjectBehavior"};
		configFoxhunt.gameObjects = new String[] { "Fox", "Predator", "Hunter", "Food", "Obstacle"};
		
		configTag.worldBehaviors = new String[] { "Tag" };
		configTag.individualBehaviors = new String[] { "TagPlayerAndHunter" };
		configTag.gameObjects = new String[] { "TagPlayer", "Obstacle" };
		 
		setConfig(configFoxhunt);

	}
	
	public void setConfig(GameConfig newConfig)
	{
		currentConfig = newConfig;
		effectuateConfig();
		proxy.connect();
		
		if (proxy.DBInstalled()) {
			needDBConfig = false;
			proxy.init();
			proxy.setWAP(this);

		} else {
			needDBConfig = true;
			System.out.println("Showing Installation Page instead of regular homepage");
		}
	}
	
	public void effectuateConfig()
	{
		try {
			proxy = (Proxy) currentConfig.proxy.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		unmount("/getStateSmall");
		unmount("/getState");
		unmount("/getConfig");
		
		mountBookmarkablePage("/getConfig", currentConfig.getConfig);
		mountBookmarkablePage("/getStateSmall", currentConfig.getStateSmall);
		mountBookmarkablePage("/getState", currentConfig.getState);
	}
	@Override
	public void init()
	{
		// Remove wicket markings (attributes in the tags)
		getMarkupSettings().setStripWicketTags(true);
		
		mountBookmarkablePage("/setAdm", SetAdm.class);
		mountBookmarkablePage("/Install", Install.class);
		//mountBookmarkablePage("/replay", Replay.class);
	}
	
	
	/**
	 * @see wicket.Application#getHomePage()
	 */
	
	public Class<HomePage> getHomePage()
	{
		//if (needDBConfig == true) {
		//	return (Class<HomePage>)Install.class.getSuperclass();
		//} else {
			return (Class<HomePage>)HomePage.class;	
		//}
	}
}
