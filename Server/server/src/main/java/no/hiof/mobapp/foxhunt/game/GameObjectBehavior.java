/**
 * 
 */
package no.hiof.mobapp.foxhunt.game;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * @author joink
 *
 */
public abstract class GameObjectBehavior {
	protected GameObject gobject;
	/* (non-Javadoc)
	 * @see no.hiof.mobapp.foxhunt.gamebehavior.IWorldBehavior#move(double)
	 */
	public abstract void move(double deltaTime, LatLongCoord target);
	public abstract void move(double deltaTime);
	
	public abstract void setGameObject(GameObject gameObject);
	
	@SuppressWarnings("unchecked")
	public static GameObjectBehavior buildFromName(String behaviorName, GameObject gameObject)
	{
		
		//System.out.print("Trying to create behavior: " + behaviorName);
		try {
			Class<GameObjectBehavior> behClass= (Class<GameObjectBehavior>) Class.forName("no.hiof.mobapp.foxhunt.game.behavior.individual." + behaviorName).asSubclass(GameObjectBehavior.class);
			Constructor<?> c = behClass.getConstructor(no.hiof.mobapp.foxhunt.game.GameObject.class);
			GameObjectBehavior behCon = (GameObjectBehavior)c.newInstance(gameObject);
			
			//System.out.println(". Success");
			return behCon;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
}
