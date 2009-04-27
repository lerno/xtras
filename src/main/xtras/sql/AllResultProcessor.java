package xtras.sql;

import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A processor that collects all values in a result set and returns it as
 * a list.
 *
 * @author Christoffer Lerno
 */
public class AllResultProcessor<D> extends AbstractResultProcessor<List<D>>
{
	/**
	 * Creates a new processor.
	 */
	public AllResultProcessor()
	{
		setResult(new ArrayList<D>());
	}

	/**
	 * Adds values to the list by running {@link SQL#readResultSet(java.sql.ResultSet)} on
	 * the result set.
	 *
	 * @param resultSet the result set to handle.
	 * @return true always.
	 * @throws SQLException if there was an exception reading from the result set.
	 */
	public boolean process(ResultSet resultSet) throws SQLException
	{
		getResult().add((D) SQL.readResultSet(resultSet));
		return true;
	}
}
