package no.hiof.mobapp.foxhunt.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import no.hiof.mobapp.foxhunt.game.LatLongCoord;
import no.hiof.mobapp.foxhunt.server.Simulation;

import org.apache.wicket.Application;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

/**
 * Homepage
 */
public class SetAdm extends WebPage {
	
	private interface ICommand
	{
		public String invoke(PageParameters arg);
	}
	HashMap<String, ICommand> table = new HashMap<String, ICommand>(10);
	
    /**
	 * Constructor that is invoked when page is invoked without a session.
	 * final PageParameters parameters
	 * 
	 * @param parameters
	 *            Page parameters
	 */
    public SetAdm(PageParameters parameters) {

    	// Fetch arguments
    	String human = "";
    	String retcode = "";
    	
    	table.put("stop", new ICommand() { 
    		public String invoke(PageParameters arg) {
    			WicketApplication wa = (WicketApplication)WicketApplication.get();
    			
    			// Stop current simulation
    			wa.proxy.simStop();
    			
    			add(new MultiLineLabel("message", "Server stopped. Running State: "+ Boolean.toString( (wa.proxy.tsim != null) ? wa.proxy.tsim.isAlive() : false)));
    	        return "TRUE";
    		}
    	});
    	
    	table.put("restart", new ICommand() { 
    		public String invoke(PageParameters arg) {
    			WicketApplication wa = (WicketApplication)WicketApplication.get();
    			
    			// Stop current simulation
    			wa.proxy.simStop();
    			wa.proxy.simStart();
    			
    			add(new MultiLineLabel("message", "Server restarted successfully"));
    	        return Boolean.toString(((WicketApplication)WicketApplication.get()).proxy.tsim.isAlive());
    		}
    	});
    	
    	table.put("addBorder", new ICommand() { 
    		public String invoke(PageParameters arg) { 			
    			WicketApplication wa = (WicketApplication)WicketApplication.get();
    			String out = "";
    			int POINTS_IN_POLY = 4;
    			
    			String coords[] = arg.getString("addBorder").split(";");
    			
    			out += "String is: '" + arg.getString("addBorder") + "'\n";
    			out += "Number of coordinate pairs: " + coords.length + "\n";
    			
    			if (coords.length != POINTS_IN_POLY) {
    				out += "Invalid number of coordinate pairs (Must be 4)\n";
    			} else {
    				ArrayList<LatLongCoord> llc = new ArrayList<LatLongCoord>(5);
    				
    				for (int i = 0; i < POINTS_IN_POLY; i++) {
    					String co[] = coords[i].split(",");  				
    					llc.add(new LatLongCoord(co[0], co[1]));
    				
    					out += llc.get(llc.size()-1) + "\n";
    				}
    			}
    			add(new MultiLineLabel("message", out));
    			return "TRUE";
    		}
    	});
    	
    	//Map<MyClass.Key, String>  data = (HashMap<String,ICommand>)parameters.entrySet();
    	//for ( Map.Entry<String, ICommand> entry: ) )  {
    	//	doSomething(entry.getValue());
    	
 
	    Map<String, String> data = parameters;   
	    
	    boolean validCommand = false;
	    for( Map.Entry<String, String> entry: data.entrySet() ) {
	    	if (table.containsKey(entry.getKey())) {
	    		validCommand = true;
	    		retcode += ((ICommand)table.get(entry.getKey())).invoke(parameters);
	    		break;
	    	}
	    }
	    
	    if (!validCommand) {
	    	human += "Error. Invalid Command.\n\nValid commands are:\n";
			// No valid command was received
		    for( Map.Entry<String, ICommand> entry: table.entrySet() ) {
		    		//System.out.println("Command: " + entry.getKey());
		    		human += "Command: " + entry.getKey() + "\n";
		    }
		    add(new MultiLineLabel("message", human));
		    retcode = "FALSE";
	    }
	    add(new Label("retcode", retcode));
	}
}
