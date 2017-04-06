package no.hiof.mobapp.foxhunt.game;

import java.util.Enumeration;


// Attribute class signaling that a gameObject have stamina (can be tired)
public interface IStamina {
	public double getMaxStamina();
	public void setMaxStamina(double maxStam);
	
	public void setStaminaRebuildRate(double alpha);
	public double getStaminaRebuildRate();
	public void rebuildStamina(double dT);
	
	public void setCurrentStamina(double curStam);
	public double getRemaingStamina();
	
	public double getStaminaSpeed(double factor, double dT);
}
