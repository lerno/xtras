package xtras.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/** @author Christoffer Lerno */
public class SingleResultProcessor<C> implements ResultProcessor<C>
{
	private C m_value = null;

	public void reset()
	{
		m_value = null;
	}

	public C getResult()
	{
		return m_value;
	}

	public boolean process(ResultSet result) throws SQLException
	{
		m_value = (C) SQL.readResultSet(result);
		return false;
	}
}
