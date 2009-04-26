package xtras.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A processor that reads a single row from a result set.
 *
 * @author Christoffer Lerno */
public class SingleResultProcessor<C> implements ResultProcessor<C>
{
	private C m_value = null;

	/**
	 * {@inheritDoc}
	 */
	public C getResult()
	{
		return m_value;
	}

	/**
	 * Read the current row using {@link SQL#readResultSet(java.sql.ResultSet)},
	 * then return false, to avoid reading more row in case of a multi row result.
	 *
	 * @param result the result set for this processor to work on.
	 * @return false to prevent more rows from being read.
	 * @throws SQLException if there was an error reading the result.
	 */
	public boolean process(ResultSet result) throws SQLException
	{
		m_value = (C) SQL.readResultSet(result);
		return false;
	}
}
