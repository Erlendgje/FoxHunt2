package no.hiof.mobapp.foxhunt.servlet.gui;

import java.util.Iterator;

import no.hiof.mobapp.foxhunt.servlet.Proxy;
import no.hiof.mobapp.foxhunt.servlet.WicketApplication;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.RefreshingView;
import org.apache.wicket.model.CompoundPropertyModel;


@SuppressWarnings("serial")
public class FoxHuntGO extends WebMarkupContainer {
        transient private WicketApplication wa = ((WicketApplication) WicketApplication.get());
        private final RefreshingView simulationGameObjectView;

        public FoxHuntGO(String id) {
                super(id);
                // TODO Auto-generated constructor stub


                // Set model property model
                setModel(new CompoundPropertyModel(this));

                /// Simulation Objects Container Content

                add(new AjaxLink("repopulate") {
                        private static final long serialVersionUID = 1889736090114423870L;

                        public void onClick(AjaxRequestTarget target) {
                                wa.proxy.simRepopulate();

                                simulationGameObjectView.modelChanged();
                                simulationGameObjectView.removeAll();

                                this.modelChanged();

                                //target.addComponent(menuContainer);
                                target.addComponent(this);
                        }
                });

                add(new AjaxLink("repopulatesmall") {
                        private static final long serialVersionUID = 1889736090114423870L;

                        public void onClick(AjaxRequestTarget target) {
                                wa.proxy.simRepopulateSmall();

                                simulationGameObjectView.modelChanged();
                                simulationGameObjectView.removeAll();

                                this.modelChanged();

                                //target.addComponent(menuContainer);
                                target.addComponent(this);
                        }
                });

                // A Refreshing View of the Simulation Game Objects
                simulationGameObjectView = new FoxHuntGOHunters("simObject");

                add(simulationGameObjectView);

                add(new AjaxEditableLabel("newHunterName")
                {
                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                                super.onSubmit(target);

                                simulationGameObjectView.modelChanged();
                                simulationGameObjectView.removeAll();

                                target.addComponent(this);
                        }
                });

             // Add general fox and hunter properties
                AjaxEditableLabel foxDetectionRange = new AjaxEditableLabel("foxDetectionRange");
                AjaxEditableLabel foxSpeed = new AjaxEditableLabel("foxSpeed");
                AjaxEditableLabel foxTurnTheta = new AjaxEditableLabel("foxTurnTheta");
                add(new Link("randomize") {
                        public void onClick() {
                                wa.proxy.randomizeFoxesAndUpdate();
                        }
                });

                AjaxEditableLabel hunterCatchRadius = new AjaxEditableLabel("hunterCatchRadius");

                add(foxDetectionRange);
                add(foxSpeed);
                add(foxTurnTheta);
                add(hunterCatchRadius);
        }

        // Getters and setters for general fox and hunter properties

        public String getFoxDetectionRange()
        {
                String out = getFirstGameObjectSimProperty("Fox", "detectionRange");
                if (out == null)
                {
                        out = "30";
                }

                return out;
        }

        public void setFoxDetectionRange(String foxDetectionRange)
        {
                setAllGameObjectSimProperty("Fox", "detectionRange", foxDetectionRange);
        }


        public String getFoxSpeed()
        {
                String out = getFirstGameObjectSimProperty("Fox", "speed");
                if (out == null)
                {
                        out = "2";
                }

                return out;
        }

        public void setFoxSpeed(String foxSpeed)
        {
                setAllGameObjectSimProperty("Fox", "speed", foxSpeed);
        }

        public String getFoxTurnTheta()
        {
                String out = getFirstGameObjectSimProperty("Fox", "turnTheta");
                if (out == null)
                {
                        out = "35";
                }

                return out;
        }

        public void setFoxTurnTheta(String foxTurnTheta)
        {
                setAllGameObjectSimProperty("Fox", "turnTheta", foxTurnTheta);
        }

        public String getHunterCatchRadius()
        {
                String out = getFirstGameObjectSimProperty("Hunter", "catchRadius");
                if (out == null)
                {
                        out = "9";
                }

                return out;
        }

        public void setHunterCatchRadius(String hunterCatchRadius)
        {
                setAllGameObjectSimProperty("Hunter", "catchRadius", hunterCatchRadius);
        }



        private String getFirstGameObjectSimProperty(String className, String property)
        {
                Iterator<Proxy.DBObject> gameObjectsIt = (Iterator<Proxy.DBObject>) wa.proxy.getSimulationObjects().iterator();

                while (gameObjectsIt.hasNext())
                {
                        Proxy.DBObject dbo = gameObjectsIt.next();
                        if (dbo == null)
                        {
                                continue;
                        }
                        if (dbo.getClassName().equals(className))
                        {
                                String out = dbo.getSimProperty(property);
                                if (out != null)
                                {
                                        return out;
                                }
                        }
                }
                return null;
        }

        public void setAllGameObjectSimProperty(String className, String property, String value)
        {
                Iterator<Proxy.DBObject> gameObjectsIt = (Iterator<Proxy.DBObject>) wa.proxy.getSimulationObjects().iterator();

                while (gameObjectsIt.hasNext())
                {
                        Proxy.DBObject dbo = gameObjectsIt.next();
                        if (dbo.getClassName().equals(className))
                        {
                                dbo.setSimProperty(property, value);
                        }
                }

        }

        // This method is called when we update the newHunter field
        public void setNewHunterName(String name) {
                // This isn't really the optimal way I think :P
                //Proxy.DBObject newDBO = wa.proxy.new DBObject(nextFreeHunterID, "Hunter", name);

                // Update the database
                //newDBO.addToSimulation();
                //newDBO.setName(name);
        }

        // This method is called for a blank newHunter field
        public String getNewHunterName() {
                return null;
        }
}
