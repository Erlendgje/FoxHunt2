package no.hiof.mobapp.foxhunt.servlet.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import no.hiof.mobapp.foxhunt.servlet.Proxy;
import no.hiof.mobapp.foxhunt.servlet.WicketApplication;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableMultiLineLabel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.time.Duration;

@SuppressWarnings("serial")
public class FoxHuntMenu extends WebMarkupContainer {
        transient private WicketApplication wa = ((WicketApplication) WicketApplication.get());

        DateFormat df = SimpleDateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
        private final Label currentID;

        public DropDownChoice behavior_select;

        public FoxHuntMenu(String id) {
                super(id);
                // TODO Auto-generated constructor stub


                // Set model property model
                setModel(new CompoundPropertyModel(this));


                // Simulation Info
                //currentID = new Label("sim_currentid", Integer.toString(wa.sim.getSimulationID()));
                currentID = new Label("gameid"); // , new PropertyModel(selectSimulationObject, "ID")

                /// Menu Container Content

                final AjaxEditableLabel simulationNameLabel= new AjaxEditableLabel("gamename") // , new PropertyModel(selectSimulationObject, "name")
                {
                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                                //simulation_select.modelChanged();

                                modelChanged();

                                target.addComponent(this);
                                super.onSubmit(target);
                        }
                };

                AjaxEditableMultiLineLabel gameComments = new AjaxEditableMultiLineLabel("gamecomments")
                {
                        @Override
                        protected void onSubmit(AjaxRequestTarget target) {
                                //menuContainer.modelChanged();

                                target.addComponent(this);
                                super.onSubmit(target);
                        }


                };

             // Simulation Management
                Form behaviorform = new Form("behaviorform");

                // Create the simulation drop down select
                behavior_select = new DropDownChoice("behavior_select", new PropertyModel(this, "behavior"),
                                Arrays.asList(new String[] { "DefaultWorldBehavior", "Tag" }));

                behaviorform.add(behavior_select);

                add(behaviorform);


                add(new AjaxCheckBox("gamepropshowpoints") {
                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                                // TODO Auto-generated method stub

                        }
                });

                add(new AjaxCheckBox("gamepropshowotherhunters") {
                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                                // TODO Auto-generated method stub

                        }
                });

                add(new AjaxCheckBox("gamepropshowgpspos") {
                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                                // TODO Auto-generated method stub

                        }
                });

                add(new AjaxCheckBox("gameproplogging") {
                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                                // TODO Auto-generated method stub

                        }
                });

                add(new Label("gamestate"));

                add(new Label("gamecreated"));
                add(new Label("gamelastrun"));

                Label gameLasted = new Label("gamelasted");

                gameLasted.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(2)));

                add(gameLasted);

                add(gameComments);
                add(currentID);
                add(simulationNameLabel);
        }
        public String getGameid()
        {
                //return this.selectSimulationObject;
                if (wa.proxy.selectSimulationObject != null)
                {
                        return ""+wa.proxy.selectSimulationObject.getID();
                }
                return null;
        }
        
        
        public void setGameid(String gameid)
        {
                return;
        }

        public String getGamename()
        {
                return wa.proxy.selectSimulationObject.getName();
        }

        public void setGamename(String gamename)
        {
                wa.proxy.selectSimulationObject.setName(gamename);
        }

        public String getGamestate()
        {
                if (wa == null) {
                        return "";
                }

                if (wa.proxy.tsim == null) {
                        return "Stopped!";
                }

                return (wa.proxy.sim.getState() && wa.proxy.tsim.isAlive()) ? "Running" : "Stopped";
        }

        public void setGamestate(String gameState)
        {
                return;
        }

        public String getGamecreated()
        {
                String lastInit = wa.proxy.getSimulationProperty("LastInit");

                if (lastInit == null)
                {
                        lastInit = ""+System.currentTimeMillis();
                }

                return df.format(new Date( Long.parseLong(lastInit)));

                //return this.selectSimulationObject;
                //return ""+this.selectSimulationObject.getID();
        }

        public void setGamecomments(String gamecomments)
        {
                wa.proxy.setSimulationProperty("Comment", gamecomments);
        }

        public String getGamecomments()
        {
                String comment = wa.proxy.getSimulationProperty("Comment");

                return comment;
        }

        public void setGamepropshowpoints(String gamepropshowpoints)
        {
                wa.proxy.setSimulationProperty("ShowPoints", gamepropshowpoints);
        }

        public String getGamepropshowpoints()
        {
                String comment = wa.proxy.getSimulationProperty("ShowPoints");

                return comment;
        }

        public void setGamepropshowotherhunters(String gamepropshowotherhunters)
        {
                wa.proxy.setSimulationProperty("ShowOtherHunters", gamepropshowotherhunters);
        }

        public String getGamepropshowotherhunters()
        {
                String comment = wa.proxy.getSimulationProperty("ShowOtherHunters");

                return comment;
        }

        public void setGameproplogging(String gameproplogging)
        {
                wa.proxy.setSimulationProperty("Logging", gameproplogging);
        }

        public String getGameproplogging()
        {
                String comment = wa.proxy.getSimulationProperty("Logging");

                return comment;
        }


        public void setGamepropshowgpspos(String gamepropshowgpspos)
        {
                wa.proxy.setSimulationProperty("ShowGPSPos", gamepropshowgpspos);
        }

        public String getGamepropshowgpspos()
        {
                String comment = wa.proxy.getSimulationProperty("ShowGPSPos");

                return comment;
        }

        public void setGamecreated()
        {
                return;
        }

        public String getGamelastrun()
        {
                String lastRun = wa.proxy.getSimulationProperty("LastRun");

                if (lastRun != null)
                {
                        return df.format(new Date( Long.parseLong(lastRun)));
                } else {
                        return "never";
                }

                //return this.selectSimulationObject;
                //return ""+this.selectSimulationObject.getID();
        }

        public void setGamelastrun()
        {
                return;
        }

        public String getGamelasted()
        {
                // Uhm. what? wa doesn't exist? Some insane intermediate state
                if (wa == null)
                {
                        return "ERR";
                }
                // We are in a transition state where the simulation have just been deleted and is in the process of being created.
                if (wa.proxy.sim == null)
                {
                        return "";
                }
                String lastStopped = wa.proxy.getSimulationProperty("LastStopped");
                String lastStarted = wa.proxy.getSimulationProperty("LastRun");

                if (lastStopped == null)
                {
                        lastStopped = ""+System.currentTimeMillis();
                }

                if (lastStarted == null)
                {
                        lastStarted = ""+System.currentTimeMillis();
                }

                long diff = Long.parseLong(lastStopped) - Long.parseLong(lastStarted);

                diff = diff/1000; // the time is specified in milliseconds, convert it to seconds

                String lasted = ""+ (int)(diff/60) +":"+ diff%60;

                return lasted;
                //return this.selectSimulationObject;
                //return ""+this.selectSimulationObject.getID();
        }

        public void setGamelasted()
        {
                return;
        }

        public void setBehavior(String newBehavior) {
                //wa.proxy.current_behavior = newBehavior;
                //System.out.println("Setting behavior to " + wa.current_behavior);
                //wa.sim.setSimulationProperty("Behavior", wa.current_behavior);
        }

        public String getBehavior() {
                return "test";
                //return wa.current_behavior;
        }

        public Proxy.SimulationObject getSim_select() {
                return wa.proxy.selectSimulationObject;
        }

        public void setSim_select(Proxy.SimulationObject selectedMake) {
                wa.proxy.selectSimulationObject = selectedMake;
        }
}
                                       
