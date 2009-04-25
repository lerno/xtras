package xtras.sql;

import xtras.lang.ObjectExtras;

import java.sql.*;
import java.util.*;

/** @author Christoffer Lerno */
public class FakeDb implements DbProxy
{
	private Map<String, FakeResultGenerator> m_queries;
	private final String m_key;
	private boolean m_shutdown;

	public FakeDb(String key)
	{
		m_key = key;
		m_shutdown =false;
		m_queries = new HashMap<String, FakeResultGenerator>();
	}

	public FakeDb(String key, Map<String, FakeResultGenerator> map)
	{
		m_key = key;
		m_shutdown =false;
		m_queries = map;
	}

	public void addQuery(String query, Object... result)
	{
		m_queries.put(query, new SingleRowResult(result));
	}

	public void addQuery(String query, List<Object[]> result)
	{
		m_queries.put(query, new MultiRowResult(result));
	}

	public void addQuery(String query, FakeResultGenerator resultGenerator)
	{
		m_queries.put(query, resultGenerator);
	}

	public void addSchema(String alias, String schema) {}

	public void beginTransaction(TransactionIsolation isolation) throws SQLException {}

	public void beginTransaction() throws SQLException {}

	public void close() {}

	public void commit() throws SQLException {}

	public String getName()
	{
		return m_key;
	}

	private FakeResultSet getResult(String query, Object[] args) throws SQLException
	{
		FakeResultGenerator generator = m_queries.get(query);
		if (generator == null) throw new SQLException("Fake db has no answer to the query '" + query + "'.");
		return new FakeResultSet(generator, args);
	}

	public <T> T insert(String insert, Object... args) throws SQLException
	{
		FakeResultSet rs = getResult(insert, args);
		rs.next();
		return (T) rs.getValues();
	}

	public boolean isValid()
	{
		return !m_shutdown;
	}

	public ResultSet query(String query, Object... args) throws SQLException
	{
		return ObjectExtras.adapt(ResultSet.class, getResult(query, args));
	}

	public void rollback() throws SQLException {}

	public void shutdown()
	{
		m_shutdown = true;
	}

	public boolean isInTransaction()
	{
		return false;
	}

	public int update(String update, Object... args) throws SQLException
	{
		ResultSet set = query(update, args);
		return set.next() ? (Integer) set.getObject(1) : 0;
	}

	private static class FakeResultSet
	{
		private int m_row;
		private Object[] m_values;
		private final Object[] m_arguments;
		private final FakeResultGenerator m_generator;

		private FakeResultSet(FakeResultGenerator generator, Object[] arguments)
		{
			m_row = 0;
			m_values = null;
			m_arguments = arguments;
			m_generator = generator;
		}

		public ResultSetMetaData getMetaData() throws SQLException
		{
			return ObjectExtras.adapt(ResultSetMetaData.class,
			                          new Object()
			{
				public int getColumnCount() throws SQLException
				{
					return m_values == null ? 0 : m_values.length;
				}
			});
		}

		public Object getObject(int columnIndex) throws SQLException
		{
			return m_values[columnIndex - 1];
		}

		public int getRow() throws SQLException
		{
			return m_row;
		}

		public Object getValues()
		{
			if (m_values == null) return null;
			return m_values.length == 1 ? m_values[0] : Arrays.asList(m_values);
		}

		public boolean next() throws SQLException
		{
			m_values = m_generator.createResult(m_row++, m_arguments);
			return m_values != null;
		}
	}

	private static class MultiRowResult implements FakeResultGenerator
	{
		private final List<Object[]> m_values;

		private MultiRowResult(List<Object[]> values)
		{
			m_values = values;
		}

		public Object[] createResult(int row, Object[] arguments)
		{
			return row < m_values.size() ? m_values.get(row) : null;
		}
	}

	private static class SingleRowResult implements FakeResultGenerator
	{
		private final Object[] m_value;

		private SingleRowResult(Object[] value)
		{
			m_value = value;
		}

		public Object[] createResult(int row, Object[] arguments)
		{
			return row == 0 ? m_value : null;
		}
	}
}
