package no.hiof.mobapp.foxhunt.servlet;

import java.awt.MenuComponent;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;


import no.hiof.mobapp.foxhunt.game.GameObject;
import no.hiof.mobapp.foxhunt.server.Simulation;
import no.hiof.mobapp.foxhunt.server.Simulation.eMode;
import no.hiof.mobapp.foxhunt.servlet.gui.FoxHuntGO;
import no.hiof.mobapp.foxhunt.servlet.gui.FoxHuntGOHunters;
import no.hiof.mobapp.foxhunt.servlet.gui.FoxHuntMenu;
import no.hiof.mobapp.foxhunt.servlet.gui.FoxHuntSimSelectDropDown;

import org.apache.wicket.Application;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListItemModel;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.version.undo.Change;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableMultiLineLabel;




//class GameObjectList

/**
 * Homepage
 */
public class HomePage extends WebPage {
	// Access to simulation land
	transient private WicketApplication wa = ((WicketApplication) WicketApplication.get());
	
	private final WebMarkupContainer simulationGameObjectListContainer;
	private final WebMarkupContainer headerContainer;
	private final WebMarkupContainer menuContainer;
	
	private final DropDownChoice simulation_select;
		
	/**
	 * Constructor that is invoked when page is invoked without a session. final
	 * PageParameters parameters
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public HomePage() {
		if (wa.needDBConfig) {
			setResponsePage(Install.class);
		}
		/*
		if (wa == null)
		{
			try {
				wait(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		
		wa.proxy.simulationMode = eMode.SIMULATION;
		
		// Initialize containers
		
		//simulationGameObjectListContainer = new WebMarkupContainer("simulationObjects");
		headerContainer = new WebMarkupContainer("header");
		menuContainer = new FoxHuntMenu("menu");
		simulationGameObjectListContainer = new FoxHuntGO("simulationObjects");
		
		// Set model property model
		setModel(new CompoundPropertyModel(this));
		
		/// Header Container Content
		add(headerContainer.setOutputMarkupId(true));
		add(simulationGameObjectListContainer.setOutputMarkupId(true));
		
		// Simulation Management
		Form form = new Form("form");
		
		// Create the simulation drop down select
		simulation_select = new FoxHuntSimSelectDropDown("sim_select");

		form.add(simulation_select);
		
		menuContainer.add(form);

		add(menuContainer.setOutputMarkupId(true));
		
		((FoxHuntMenu)menuContainer).behavior_select.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			protected void onUpdate(AjaxRequestTarget target)
			{
				// Simulation Change - Need to re-fetch and rebuild everything.
				
				// Stop current simulation
				wa.proxy.simStop();
				
				System.out.println("Current Simulation ID is: "+ wa.proxy.selectSimulationObject.getID());
				
				// Create new simulation
				wa.proxy.simNullify();
				wa.proxy.checkSimAndFix();
								
				//simulationGameObjectView.modelChanged();
				//simulationGameObjectView.removeAll();

				simulationGameObjectListContainer.modelChanged();
								
				target.addComponent(menuContainer);
				target.addComponent(simulationGameObjectListContainer);
				
			}
		});
		
		simulation_select.add(new AjaxFormComponentUpdatingBehavior("onchange")
		{
			protected void onUpdate(AjaxRequestTarget target)
			{
				// Simulation Change - Need to re-fetch and rebuild everything.
				
				// Stop current simulation
				wa.proxy.simStop();
				
				System.out.println("Current Simulation ID is: "+ wa.proxy.selectSimulationObject.getID());
				
				// Create new simulation
				wa.proxy.simNullify();
				wa.proxy.checkSimAndFix();
								
				//simulationGameObjectView.modelChanged();
				//simulationGameObjectView.removeAll();

				simulationGameObjectListContainer.modelChanged();
				
				menuContainer.modelChanged();
				
				//currentID.modelChanged();
				
				//simulationNameLabel.modelChanged();
				
				target.addComponent(menuContainer);
				target.addComponent(simulationGameObjectListContainer);
				
			}
		});
		
		menuContainer.add(new AjaxLink("sim_new") {
			private static final long serialVersionUID = 1889736090124423870L;

			public void onClick(AjaxRequestTarget target) {

				wa.proxy.simNew();
								
				//simulationGameObjectView.modelChanged();
				//simulationGameObjectView.removeAll();

				//currentID.modelChanged();
				
				simulation_select.modelChanged();
				
				headerContainer.modelChanged();
				simulationGameObjectListContainer.modelChanged();
				menuContainer.modelChanged();
				
				
				target.addComponent(headerContainer);
				target.addComponent(simulationGameObjectListContainer);
				target.addComponent(menuContainer);
			}
		});

		menuContainer.add(new AjaxLink("sim_delete") {
			private static final long serialVersionUID = 1889736090114423870L;

			public void onClick(AjaxRequestTarget target) {
				// Delete it
				wa.proxy.simDelete();
				
				// Fix our stuff so that everything that needs updating get refreshed
				
				// Fetch a new game object list
				//simulationGameObjectView.modelChanged();
				//simulationGameObjectView.removeAll();
				
				//currentID.modelChanged();
				
			
				simulation_select.modelChanged();
				
				// Inform the containers that their object have changed
				headerContainer.modelChanged();
				simulationGameObjectListContainer.modelChanged();
				menuContainer.modelChanged();
				
				// Add the new containes to our ajax-return
				target.addComponent(headerContainer);
				target.addComponent(simulationGameObjectListContainer);
				target.addComponent(menuContainer);

			}
		});
				
		// Simulation Control
		add(new Link("start") {
			public void onClick() {
				// Set sim and tsim to null forcing a reinitalization from database
				wa.proxy.simNullify();
								
				// Start the simulation
				wa.proxy.simStart();
			}
		});
		
		add(new AjaxLink("stop") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				// TODO Auto-generated method stub
				wa.proxy.simStop();
				
				menuContainer.modelChanged();
				
				// Add the new containers to our ajax-return
				target.addComponent(menuContainer);
			}
		});

		add(new BookmarkablePageLink("linkToGetConfig", GetConfig.class));
		add(new BookmarkablePageLink("linkToGetState", GetState.class));
		//add(new Label("message", "Number of gameObjects in simulation: "
		//		+ Integer.toString(wa.sim.world.gameObjects.size())));

	}
}
