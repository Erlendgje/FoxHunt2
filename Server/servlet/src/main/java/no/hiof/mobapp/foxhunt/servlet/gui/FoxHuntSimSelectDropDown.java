package no.hiof.mobapp.foxhunt.servlet.gui;

import no.hiof.mobapp.foxhunt.servlet.Proxy;
import no.hiof.mobapp.foxhunt.servlet.WicketApplication;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.LoadableDetachableModel;

public class FoxHuntSimSelectDropDown extends DropDownChoice {

        public FoxHuntSimSelectDropDown(String id) {
                super(id, ((WicketApplication) WicketApplication.get()).proxy.simulationObjectsModel,   new IChoiceRenderer() {
                        public Object getDisplayValue(Object obj) {
                                Proxy.SimulationObject c = (Proxy.SimulationObject) obj;
                                return c.getID() + " - " + c.getName();
                        }

                        public String getIdValue(Object obj, int index) {
                                Proxy.SimulationObject c = (Proxy.SimulationObject) obj;
                                return Integer.toString(c.getID());
                        }
                });
        }
}
