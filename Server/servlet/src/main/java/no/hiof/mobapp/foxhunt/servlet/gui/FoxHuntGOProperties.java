package no.hiof.mobapp.foxhunt.servlet.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import no.hiof.mobapp.foxhunt.servlet.Proxy;
import no.hiof.mobapp.foxhunt.servlet.WicketApplication;

import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.markup.repeater.util.ModelIteratorAdapter;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class FoxHuntGOProperties extends RefreshingView {

        transient private WicketApplication wa = ((WicketApplication) WicketApplication.get());
        static private int nextFreeHunterID = 100;
        public FoxHuntGOProperties(String id) {
                super(id);
                // TODO Auto-generated constructor stub
        }

        @Override
        protected Iterator<Proxy.DBObject> getItemModels() {

                // Since we sometimes can be in between simulation creation, etc we need to do some sanity checks.
                if (wa.proxy.sim == null) {
                        wa.proxy.checkSimAndFix(); // TODO: Verify
                }

                //System.out.println("Fetching new list of objects (Simulation Game Objects) With SimID: " + wa.sim.getSimulationID());
                List<Proxy.DBObject> simObjects = wa.proxy.getSimulationObjects();

                Iterator<Proxy.DBObject> simObjectsIt = (Iterator<Proxy.DBObject>) simObjects.iterator();

                Vector<Proxy.DBObject> filteredSimObjects = new Vector<Proxy.DBObject>(10);
                // We loop over here to find what the nextFreeHunterID is (a bit dirty but hey)
                nextFreeHunterID = 100;

                while (simObjectsIt.hasNext()) {
                        Proxy.DBObject tmp = simObjectsIt.next();
                        if (tmp.getClassName() != null  && ( tmp.getClassName().equals("Hunter") || tmp.getClassName().equals("TagPlayer"))) {
                                filteredSimObjects.add(tmp);

                                if (tmp.getID() == nextFreeHunterID && nextFreeHunterID < 105) {
                                        nextFreeHunterID++;
                                }
                        }
                }

                // the iterator returns DBObjects, but we need it to return models,
                // we use this adapter class to perform on-the-fly conversion.
                return new ModelIteratorAdapter((Iterator<Proxy.DBObject>)filteredSimObjects.iterator()) {

                        protected IModel model(Object object) {
                                return new Model((Proxy.DBObject) object);
                        }

                };
        }
        
        
        @Override
        protected void populateItem(final Item item) {
                final Proxy.DBObject dbo = (Proxy.DBObject) item.getModelObject();

                item.setModel(new CompoundPropertyModel(dbo));

                item.add(new Label("ID"));
                item.add(new AjaxEditableLabel("className"));
                item.add(new AjaxEditableLabel("name"));

                item.add(new Link("action") {
                        private static final long serialVersionUID = 3062106375535388517L;

                        @Override
                        public void onClick() {
                                if (wa == null) {
                                        return;
                                }
                                item.modelChanging();

                                //simulationGameObjectView.modelChanging();

                                System.out.println("Wa = " + wa);
                                System.out.println("Wa.sim = " + wa.proxy.sim);
                                System.out.println("dbo.getID = " + dbo.getID());

                                // Remove item from the database config for the current simulation and invalidate listView
                                wa.proxy.removeSimulationObjectProperties(dbo.getID());
                                wa.proxy.removeSimulationObject(dbo.getID());

                                // Remove from current instantiated simulation as well TODO: Dangerous - use proxy method
                                wa.proxy.sim.world.gameObjects.remove(wa.proxy.sim.world.getHunter(Integer.toString(dbo.getID())));

                                //simulationGameObjectView.modelChanged();
                                //simulationGameObjectView.removeAll();
                        }
                });
        }
}

