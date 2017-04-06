package no.hiof.mobapp.foxhunt.game;

import java.util.Enumeration;


// Attribute class signaling that a gameObject is of a hunter type (i.e. Predator, PlayerHunter)
public interface ITagHunter {
	public double getKillRange();
	public void setLastCaughtBy(GameObject go);
	public boolean isLastCaughtBy(GameObject go);
}
