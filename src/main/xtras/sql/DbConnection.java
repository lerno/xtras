package xtras.sql;

import xtras.lang.ObjectExtras;

import java.sql.*;

/** @author Christoffer Lerno */
class DbConnection
{
	private Connection m_connection;
	private DbPool m_pool;
	private boolean m_hasErrors;

	public DbConnection()
	{
		m_pool = null;
		m_connection = null;
	}

	private Connection newConnection(DbPool pool) throws SQLException
	{
		if (m_connection != null)
		{
			if (!ObjectExtras.equals(m_pool, pool))
			{
				try
				{
					rollback();
				}
				catch (Exception e)
				{
					// Ignore the rollback result since our original error
					// is using another connection.
				}
				throw new SQLException("Tried to mix multiple connections in single transaction.");
			}
			return m_connection;
		}
		else
		{
			m_hasErrors = false;
			return pool.acquire();
		}
	}

	/**
	 * Runs a query using a ResultProcessor to work on the ResultSet.
	 *
	 * @param pool the pool to get connections from (no new connection will be used
	 * if currently in a transaction).
	 * @param processor a ResultProcessor to work on the ResultSet.
	 * @param query the SQL query to execute.
	 * @param args the arguments to insert at each '?' in the query string.
	 * @return the result as generated by {@link ResultProcessor#getResult} after
	 * it has worked on the result set.
	 * @throws SQLException if there was an error performing the query.
	 */
	public <T> T query(DbPool pool, ResultProcessor<T> processor, String query, Object... args) throws SQLException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = newConnection(pool);
		try
		{
			statement = preparedStatement(connection, query, args);
			resultSet = statement.executeQuery();
			while (resultSet.next())
			{
				if (!processor.process(resultSet)) break;
			}
			return processor.getResult();
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			throw e;
		}
		finally
		{
			SQL.closeSilently(resultSet);
			SQL.closeSilently(statement);
			close(pool, connection);
		}
	}

	/**
	 * Releases a connection to the db if it is not the
	 * current connection used in a transaction.
	 *
	 * @param pool the pool to return the connection to.
	 * @param connection the connection to return.
	 */
	private void close(DbPool pool, Connection connection)
	{
		if (ObjectExtras.equals(m_connection, connection)) return;
		pool.release(connection, m_hasErrors);
	}

	/**
	 * Begin a database transaction with the given transaction isolation.
	 *
	 * @param pool the pool to acquire connections from.
	 * @param transactionIsolation the isolation level to use, or null for none.
	 * @throws SQLException if there was any issue starting the transaction.
	 * <em>Note that we are guaranteed that there is no open transaction after
	 * the exception is thrown.</em>
	 */
	public void beginTransaction(DbPool pool, TransactionIsolation transactionIsolation) throws SQLException
	{
		if (m_connection != null)
		{
			releaseTransaction();
			throw new SQLException("Tried to start transaction while already in transaction.");
		}
		Connection c = null;
		try
		{
			c = newConnection(pool);
			c.setAutoCommit(false);
			if (transactionIsolation != null)
			{
				c.setTransactionIsolation(transactionIsolation.getId());
			}
			m_connection = c;
			m_pool = pool;
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			close(pool, c);
			throw e;
		}
	}

	/**
	 * Attempts to perform a rollback on the current transaction.
	 * <p>
	 * This will end the transaction and discard any updates or inserts made
	 * in the transaction.
	 *
	 * @throws SQLException if there was an error doing rollback.
	 * <em>Note that we are guaranteed that there is no open transaction after
	 * the exception is thrown.</em>
	 */
	public void rollback() throws SQLException
	{
		try
		{
			if (m_connection == null) throw new SQLException("Tried to rollback outside of transaction.");
			m_connection.rollback();
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			throw e;
		}
		finally
		{
			releaseTransaction();
		}

	}

	/**
	 * Releases the connection of the current transaction.
	 * <p/>
	 * This implicitly attempts to perform a rollback, which should
	 * be a no-op if rollback or commit already has been performed.
	 */
	private void releaseTransaction()
	{
		if (m_connection != null)
		{
			try
			{
				m_connection.rollback();
			}
			catch (Exception e)
			{
				m_hasErrors = true;
			}
			Connection c = m_connection;
			DbPool pool = m_pool;
			m_connection = null;
			m_pool = null;
			close(pool, c);
		}
	}


	/**
	 * Creates a prepared statement on the given connection, and loads
	 * the parameters with the values given in {@code args}.
	 * <p/>
	 * If there are any errors, the statement will immediately be
	 * closed (if opened), and {@link #hasErrors} will return true.
	 *
	 * @param c the connection to use.
	 * @param query the SQL query with parameters.
	 * @param args the arguments to insert in the parameter slots.
	 * @return a prepared statement.
	 * @throws SQLException if there was an error creating a prepared statement.
	 * @see java.sql.PreparedStatement for an explanation of how parameters work.
	 */
	@SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
	private PreparedStatement preparedStatement(Connection c, String query, Object... args) throws SQLException
	{
		PreparedStatement statement = null;
		try
		{
			statement = c.prepareStatement(query);
			int index = 0;
			for (Object arg : args)
			{
				statement.setObject(++index, arg);
			}
			return statement;
		}
		catch (SQLException e)
		{
			SQL.closeSilently(statement);
			m_hasErrors = true;
			throw e;
		}
	}

	/**
	 * Executes an SQL update query and returns the number of rows changed.
	 *
	 * @param pool the pool to get connections from.
	 * @param update the sql update query, parameterized with '?'.
	 * @param args the arguments to insert in the parameter slots.
	 * @return the number of rows that was changed due to this update.
	 * @throws SQLException if there was an error executing the update.
	 * @see java.sql.PreparedStatement regarding parameterization.
	 */
	public int update(DbPool pool, String update, Object... args) throws SQLException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = newConnection(pool);
		try
		{
			statement = preparedStatement(connection, update, args);
			return statement.executeUpdate();
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			throw e;
		}
		finally
		{
			SQL.closeSilently(resultSet);
			SQL.closeSilently(statement);
			close(pool, connection);
		}
	}

	/**
	 * Attempts to perform a commit on the current transaction.
	 * <p>
	 * This will end the transaction and apply all updates or inserts made
	 * in the transaction.
	 *
	 * @throws SQLException if there was an error doing the commit.
	 * <em>Note that we are guaranteed that there is no open transaction after
	 * the exception is thrown, so the effect is that of doing rollback
	 * on the entire transaction.</em>
	 */
	public void commit() throws SQLException
	{
		try
		{
			if (m_connection == null)
			{
				throw new SQLException("Tried to commit transaction outside of transaction.");
			}
			m_connection.commit();
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			throw e;
		}
		finally
		{
			releaseTransaction();
		}

	}

	/**
	 * Returns true if there are any recent errors on this connection.
	 * <p>
	 * This flag will be cleared whenever a new connection is allocated.
	 *
	 * @return true if this connection had errors, false otherwise.
	 */
	public boolean hasErrors()
	{
		return m_hasErrors;
	}

	/**
	 * Executes an SQL insert query and returns the keys generated, if any.
	 *
	 * @param pool the pool to get connections from.
	 * @param insert the sql insert query, parameterized with '?'.
	 * @param args the arguments to insert in the parameter slots.
	 * @return the key generated by the query, or a {@link java.util.List} of
	 * keys if more than a single key was generated.
	 * @throws SQLException if there was an error executing the update.
	 * @see java.sql.PreparedStatement regarding how parameters work.
	 */
	@SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
	public Object insert(DbPool pool, String insert, Object... args) throws SQLException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = newConnection(pool);
		try
		{
			statement = preparedStatement(connection, insert, args);
			statement.executeUpdate();
			resultSet = statement.getGeneratedKeys();
			return SQL.readResultSet(resultSet);
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			throw e;
		}
		finally
		{
			SQL.closeSilently(resultSet);
			SQL.closeSilently(statement);
			close(pool, connection);
		}
	}

	public boolean isInTransaction()
	{
		return m_connection != null;
	}

}

