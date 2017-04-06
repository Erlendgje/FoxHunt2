package no.hiof.mobapp.foxhunt.servlet;

import java.util.Arrays;
import java.util.List;

import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.game.LatLongCoord;
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
public class GetConfig extends WebPage {

	private static final long serialVersionUID = 1L;
	transient private WicketApplication wa = ((WicketApplication)WicketApplication.get());
	
	transient private World world = wa.proxy.getWorld();
    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * final PageParameters parameters
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public GetConfig(PageParameters parameters) {
    	// Fetch arguments
    	String id = "NONE";
    	double killrange = 5.0;
    	
		if (parameters.containsKey("userid"))
		{
			id = parameters.getString("userid");
		}
		
		if (wa.proxy.sim != null)
		{
			if (id.equals("NONE"))
			{
				id = "100";
			}
			
			//System.out.println("Looking up Hunter: " + id);
			Hunter h = wa.proxy.getWorld().getHunter(id);
			
			//System.out.println("Returned: " + h);
			
			if (h != null) {
				killrange = h.getKillRange();
			}
		}
		
        // Add the simplest type of label
        add(new Label("message", id));

        // Add the border
        add(new ListView("point", Arrays.asList(world.getMap().cords)) {
			@Override protected void populateItem(ListItem item) {
				LatLongCoord c = (LatLongCoord)item.getModelObject();

				item.add(new AttributeModifier("lat", true, new Model(Double.toString(c.lat))));
				item.add(new AttributeModifier("lon", true, new Model(Double.toString(c.lon))));
			}		
		});
        
        //Label boundary = new Label("boundary", "");
        /*
        boundary.add(new AttributeModifier("ltmin", true, new Model(Double.toString(world.getMap().cords[2].lat))));
        boundary.add(new AttributeModifier("ltmax", true, new Model(Double.toString(world.getMap().cords[0].lat))));
        boundary.add(new AttributeModifier("lnmin", true, new Model(Double.toString(world.getMap().cords[3].lon))));
        boundary.add(new AttributeModifier("lnmax", true, new Model(Double.toString(world.getMap().cords[1].lon))));
		add(boundary);
		*/

        Label displaySettings = new Label("displaysettings", "");

        displaySettings.add(new AttributeModifier("gps", true, new Model( wa.proxy.getSimulationProperty("ShowGPSPos"))));
        displaySettings.add(new AttributeModifier("opponents", true, new Model( wa.proxy.getSimulationProperty("ShowOtherHunters"))));
        displaySettings.add(new AttributeModifier("points", true,new Model( wa.proxy.getSimulationProperty("ShowPoints"))));
        displaySettings.add(new AttributeModifier("catchrange", true,new Model(Double.toString(killrange))));
        
        add(displaySettings);
        
		// A Wicket-style Loop/List of Items
		add(new ListView("gameObject", (List)world.gameObjects) {
			// This method is called for each 'entry' in the list.
			@Override protected void populateItem(ListItem item) {
				GameObject go = (GameObject)item.getModelObject();
				item.add(new AttributeModifier("id", true, new Model(Integer.toString(go.dbObjectID))));
				item.add(new AttributeModifier("groupid", true, new Model(Integer.toString(go.getGroupID()))));
				item.add(new AttributeModifier("class", true, new Model(go.getConfigValue("ClassName"))));
				item.add(new AttributeModifier("name", true, new Model(go.getConfigValue("name"))));
				
				item.add(new AttributeModifier("lt", true, new Model(Double.toString(go.getLocation().lat))));
				item.add(new AttributeModifier("ln", true, new Model(Double.toString(go.getLocation().lon))));
				
				//item.add(new Label("objectID", );
				
				//item.add(new Label("group", Integer.toString(go.getGroupID())));
			}		
		});
    }
    
    @Override
    public String getMarkupType() {
            return "xml";
    }

}
