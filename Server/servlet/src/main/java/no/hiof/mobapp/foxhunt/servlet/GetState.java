package no.hiof.mobapp.foxhunt.servlet;

import java.text.DecimalFormat;
import java.util.List;

import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.objects.Hunter;
import no.hiof.mobapp.foxhunt.game.World;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

/**
 * GetState
 */
public class GetState extends WebPage {

	transient private World world = ((WicketApplication)WicketApplication.get()).proxy.getWorld();
	transient private WicketApplication wa = ((WicketApplication) WicketApplication.get());
	
	String element_gameObject = "gameObject";
	
	// How many decimals should the LatLoncoords have?
	DecimalFormat gpscoord = new DecimalFormat("#0.000000");
    /**
	 * Constructor
	 * @param parameters
	 *            Page parameters
	 */
    public GetState(PageParameters parameters) {
    	
    	// Check if the game world exist - if not the admin is currently setting it up and we should try to give out any state.
    	if (world == null) {
    		add(new Label("msg", "World under construction. Please wait"));
    		
    		// Dummy values as there is no world to get gameObjects from
    		add(new Label("gameObject", ""));
    		return;
    	}
    	// Do our setState part :)
    	// When a use requests a new world state he/she may also give a state update for him/herself.
    	// Normally this is just a new loc-lat, but may also be other type of states as pressedButton, etc.
    	
    	// Fetch arguments
    	String id = "NONE";
    	
    	// Do nothing unless we have a user ID
		if (parameters.containsKey("userid"))
		{
			id = parameters.getString("userid");
			
			// Check for other states
			// ...
			
			// Check for lat-lon update and update if all checks out
			if (parameters.containsKey("lat") && parameters.containsKey("lon"))
			{
				Hunter h = world.getHunter(id);
				if (h != null)
				{
					h.setLocation(parameters.getDouble("lat"), parameters.getDouble("lon"));
					
					if(!h.isTaken()){
						h.setTaken(true);
					}
					
				}
			}	
		}		
		
		final boolean fullState = parameters.containsKey("wc") ? true : false;

		
		// Hack section for gameover signalling of the future MPI.
		// Any messages from the server
        Label messages = new Label("msg", "");

        Boolean gameOver = false;
        
		if (wa.proxy.tsim == null) {
			gameOver = true;
		} else {
			gameOver = (wa.proxy.sim.getState() && wa.proxy.tsim.isAlive()) ? false : true;
		}
		
        messages.add(new AttributeModifier("gameOver", true, new Model( gameOver )));
        add(messages);
        
        
		// List of game objects
		add(new ListView(element_gameObject, (List<GameObject>)world.gameObjects) {
			private static final long serialVersionUID = 224321416113268089L;

			// This method is called for each 'entry' in the list.
			@Override protected void populateItem(ListItem item) {
				
				GameObject go = (GameObject)item.getModelObject();
				
				// General attributes valid for all gameObjects
				item.add(new AttributeModifier("id", true, new Model(Integer.toString(go.dbObjectID))));
				item.add(new AttributeModifier("class", true, new Model(go.getConfigValue("ClassName"))));
				item.add(new AttributeModifier("taken", true, new Model(go.isTaken())));
				
				item.add(new AttributeModifier("lt", true, new Model(gpscoord.format(go.getLocation().lat))));
				item.add(new AttributeModifier("ln", true, new Model(gpscoord.format(go.getLocation().lon))));
				
				// For a fox we want to show it's caught-state
				if (go instanceof no.hiof.mobapp.foxhunt.game.objects.Fox)
				{
					if ( ((no.hiof.mobapp.foxhunt.game.objects.Fox)go).isCaught() )
					{
						item.add(new AttributeModifier("iscaught", true, new Model(Boolean.toString(((no.hiof.mobapp.foxhunt.game.objects.Fox) go).isCaught()))));
					}
				}
				
				// For a hunter we want to show what prey it has caught
				if (go instanceof no.hiof.mobapp.foxhunt.game.IHunter)
				{
					/*
					StringBuffer caught = new StringBuffer();
					Enumeration<GameObject> caughtObjects =  ((no.hiof.mobapp.foxhunt.gamebehavior.IHunter)go).getCaughtObjects();
					
					
					while (caughtObjects.hasMoreElements())
					{
						
						caught.append(caughtObjects.nextElement().dbObjectID);
						if (caughtObjects.hasMoreElements()) caught.append( ", ");
					}
					
					if (caught.length() != 0)
					{
						item.add(new AttributeModifier("caught", true, new Model( caught)));
					}*/
					
					item.add(new AttributeModifier("caught", true, new Model( ((no.hiof.mobapp.foxhunt.game.IHunter)go).getCaughtTotal())));
					
					
					if (fullState)
					{
						item.add(new AttributeModifier("heading", true, new Model( ((no.hiof.mobapp.foxhunt.game.GameObject)go).getTheta())));
					}

				}
			}		
		});
    }
    
	@Override
    public String getMarkupType() {
            return "xml";
    }

}
