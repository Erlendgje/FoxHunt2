package no.hiof.mobapp.foxhunt.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLScript {

	public final static char QUERY_ENDS = ';';

	private Connection conn;
	private BufferedReader rdr;
	private Statement statement;

	/**
	 * Constructor: takes a bufferedReader and a sql connection to create the SqlScript object.
	 * Note that construction does not automatically read the script.
	 * @param bufRdr BufferedReader to the script data.
	 * @param connection SQL Connection
	 * @throws SQLException
	 */
	public SQLScript(BufferedReader bufRdr, Connection connection) throws SQLException {
		rdr = bufRdr;
		conn = connection;
		statement = conn.createStatement();
	}

	/**
	 * Loads the Sql Script from the BufferedReader and parses it into a statement.
	 * @throws IOException
	 * @throws SQLException
	 */
	public void loadScript() throws IOException, SQLException {
		String line;
		StringBuffer query = new StringBuffer();
		boolean queryEnds = false;

		while ((line = rdr.readLine()) != null) {
			if (isComment(line))
				continue;
			queryEnds = checkStatementEnds(line);
			query.append(line);
			if (queryEnds) {
				statement.addBatch(query.toString());
				System.out.println("New Query Added: " + query.toString());
				query.setLength(0);
			}
		}
	}

	/**
	 * @param line
	 * @return
	 */
	private boolean isComment(String line) {
		if ((line != null) && (line.length() > 0))
			return (line.charAt(0) == '#') || ((line.charAt(0) == '-') && (line.charAt(1) == '-'));
		return false;
	}

	/**
	 * Executes the statement created by loadScript.
	 *
	 * @throws IOException
	 * @throws SQLException
	 */
	public void execute() throws IOException, SQLException {
		statement.executeBatch();
	}

	private boolean checkStatementEnds(String s) {
		return (s.indexOf(QUERY_ENDS) != -1);
	}

	/**
	 * @return the statement
	 */
	public Statement getStatement() {
		return statement;
	}

	/**
	 * @param statement the statement to set
	 */
	public void setStatement(Statement statement) {
		this.statement = statement;
	}
}
