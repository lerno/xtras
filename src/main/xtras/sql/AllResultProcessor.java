package xtras.sql;

import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;

/** @author Christoffer Lerno */
public class AllResultProcessor<D> implements ResultProcessor<List<D>>
{
	public List<D> m_value;

	public AllResultProcessor()
	{
		reset();
	}

	public void reset()
	{
		m_value = new ArrayList<D>();
	}

	public List<D> getResult()
	{
		return m_value;
	}

	public boolean process(ResultSet result) throws SQLException
	{
		m_value.add((D) SQL.readResultSet(result));
		return true;
	}
}
