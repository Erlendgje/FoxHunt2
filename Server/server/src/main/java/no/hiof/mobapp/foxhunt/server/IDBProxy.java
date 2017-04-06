package no.hiof.mobapp.foxhunt.server;

public interface IDBProxy {
	public String getSimulationProperty(String property);
	public String getSimulationProperty(String property, int simID);
	public void removeSimulationProperty(String property, int simID);
	public void setSimulationProperty(String property, String value);
	public void setSimulationProperty(String property, String value, int simID);
}
