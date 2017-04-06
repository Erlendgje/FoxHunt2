package no.hiof.mobapp.foxhunt.game;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/**
 * @author joink
 *
 */
public abstract class WorldBehavior {
	protected World world;
	
	/* (non-Javadoc)
	 * @see no.hiof.mobapp.foxhunt.gamebehavior.IWorldBehavior#move(double)
	 */
	public abstract void move(double deltaTime);
	public abstract void setWorld(World currentWorld);
	
	@SuppressWarnings("unchecked")
	public static WorldBehavior buildFromName(String behaviorName, World world)
	{
		
		System.out.print("Trying to create behavior: " + behaviorName);
		try {
			Class<WorldBehavior> behClass= (Class<WorldBehavior>) Class.forName("no.hiof.mobapp.foxhunt.game.behavior.world." + behaviorName).asSubclass(WorldBehavior.class);
			Constructor<?> c = behClass.getConstructor(no.hiof.mobapp.foxhunt.game.World.class);
			WorldBehavior behCon = (WorldBehavior)c.newInstance(world);
			
			System.out.println(". Success");
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
