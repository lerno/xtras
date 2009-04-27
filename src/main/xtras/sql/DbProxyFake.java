package xtras.sql;

import xtras.lang.ObjectExtras;

import java.sql.*;
import java.util.*;

/**
 * A DbProxy implementation that allows (limited) mocked responses to queries.
 * <p>
 * Adding a new response to a query can be done in several different ways.
 * Either by storing a single row as a response:
 * <pre>
 * // This will return a single row with values "A string" and 12345 to the
 * // *exact* query "select * from thetable where key = ?".
 * m_dbProxyFake.addQuery("select * from thetable where key = ?", "A string", 12345);
 * </pre>
 * <p/>
 * Or similarly a list of Object arrays containing the rows in the result set:
 * <pre>
 * // This will return a two rows with values 1, 2, 4 to the
 * // *exact* query "select my_number from thetable".
 * m_dbProxyFake.addQuery("select * from thetable", Arrays.asList(new Object[] { 1 },
 *                                                                new Object[] { 2 },
 *                                                                new Object[] { 4 });
 * </pre>
 * Finally it is possible to make custom responses by implementing a {@link xtras.sql.FakeResultGenerator}
 * that can take the arguments sent with the query.
 * <pre>
 * m_fakeDb.addQuery("select * from thetable where key > ?", new FakeResultGenerator()
 * {
 *    public Object[] createResult(int row, Object[] arguments)
 *    {
 *       return row > 3 ? null : new Object[]{"name" + (row + (Integer) arguments[0])};
 *     }
 * });
 *
 * // This will return the list ["name5", "name6", "name7", "name8"]
 * m_fakeDb.query(new AllResultProcessor(), "select * from thetable where key > ?", 5);
 * </pre>
 * See {@link xtras.sql.FakeResultGenerator} on how to write fake results.
 *
 * @author Christoffer Lerno
 */
public class DbProxyFake implements DbProxy
{
	private final Map<String, FakeResultGenerator> m_queries;
	private boolean m_shutdown;

	/**
	 * Creates a new fake.
	 */
	public DbProxyFake()
	{
		m_shutdown =false;
		m_queries = new HashMap<String, FakeResultGenerator>();
	}

	/**
	 * Set the single row query result for the given query.
	 *
	 * @param query the query to respond to.
	 * @param result the single row response.
	 */
	public void addQuery(String query, Object... result)
	{
		m_queries.put(query, new SingleRowResult(result));
	}

	/**
	 * Set the multi-row query result for the given query.
	 *
	 * @param query the query to respond to.
	 * @param result a list with the each entry representing the columns of a result set row.
	 */
	public void addQuery(String query, List<Object[]> result)
	{
		m_queries.put(query, new MultiRowResult(result));
	}

	/**
	 * Set an adaptive query result for the given query.
	 *
	 * @param query the query to respond to.
	 * @param resultGenerator the generator to generate results for this query.
	 */
	public void addQuery(String query, FakeResultGenerator resultGenerator)
	{
		m_queries.put(query, resultGenerator);
	}

	/**
	 * This method does nothing in the fake.
	 */
	public void addAlias(String alias, String schema) {}

	/**
	 * This method does nothing.
	 */
	public void beginTransaction(TransactionIsolation isolation) {}

	/**
	 * This method does nothing.
	 */
	public void commit() {}

	/**
	 * Retrieve the fake result set for a query.
	 *
	 * @param query the query to get the result for.
	 * @param args the arguments to the query (if any).
	 * @return the fake result set generated for this query.
	 * @throws SQLException if there is no stored response for this query.
	 */
	private FakeResultSet getResult(String query, Object[] args) throws SQLException
	{
		FakeResultGenerator generator = m_queries.get(query);
		if (generator == null) throw new SQLException("Fake db has no answer to the query '" + query + "'.");
		return new FakeResultSet(generator, args);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T insert(String insert, Object... args) throws SQLException
	{
		FakeResultSet rs = getResult(insert, args);
		rs.next();
		return (T) rs.getValues();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isValid()
	{
		return !m_shutdown;
	}

	/**
	 * This method does nothing.
	 */
	public void rollback() {}

	/**
	 * {@inheritDoc}
	 */
	public void shutdown()
	{
		m_shutdown = true;
	}

	/**
	 * Since transactions are not supported with the fake, this method returns false.
	 *
	 * @return false, always.
	 */
	public boolean inTransaction()
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int update(String update, Object... args) throws SQLException
	{
		FakeResultSet result = getResult(update, args);
		return result.next() ? (Integer) result.getObject(1) : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T query(ResultProcessor<T> processor, String query, Object... args) throws SQLException
	{
		FakeResultSet result = getResult(query, args);
		ResultSet resultSet = ObjectExtras.adapt(ResultSet.class, result);
		while (resultSet.next())
		{
			if (!processor.process(resultSet))
			{
				break;
			}
		}
		return processor.getResult();
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
			return m_row + 1;
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
