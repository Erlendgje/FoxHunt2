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
import no.hiof.mobapp.foxhunt.servlet.gui.FoxHuntGOHunters;
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
public class Install extends WebPage {
	// Access to simulation land
	transient private WicketApplication wa = ((WicketApplication) WicketApplication.get());
	
	/**
	 * Constructor that is invoked when page is invoked without a session. final
	 * PageParameters parameters
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public Install() {
		
		add(new Link("createandpopulate") {
			public void onClick() {
				wa.proxy.DBInstall();
				
				wa.setConfig(wa.configFoxhunt);
			}
		});
	}
}
