/**
 * 
 */
package no.hiof.mobapp.foxhunt.game;

import java.sql.Connection;

/**
 * @author joink
 *
 */
public interface IDatabaseObject {
	public void updateDatabase(Connection con);
	public void initFromDatabase(Connection con);
}
