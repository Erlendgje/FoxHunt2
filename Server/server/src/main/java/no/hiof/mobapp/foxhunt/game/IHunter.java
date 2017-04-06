package no.hiof.mobapp.foxhunt.game;

import java.util.Enumeration;


// Attribute class signaling that a gameObject is of a hunter type (i.e. Predator, PlayerHunter)
public interface IHunter {
	public double getKillRange();
	public void haveCaught(GameObject go);
	public Enumeration<GameObject> getCaughtObjects();
	public int getCaughtTotal();
}
