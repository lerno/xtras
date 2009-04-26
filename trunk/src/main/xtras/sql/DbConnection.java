package xtras.sql;

import xtras.lang.ObjectExtras;

import java.sql.*;
import java.util.List;

/** @author Christoffer Lerno */
class DbConnection
{
	private Connection m_connection;
	private DbPool m_pool;
	private boolean m_hasErrors;
	private final SingleResultProcessor m_singleResultProcessor;
	private final AllResultProcessor m_allResultProcessor;

	public DbConnection()
	{
		m_singleResultProcessor = new SingleResultProcessor();
		m_allResultProcessor = new AllResultProcessor();
		m_pool = null;
		m_connection = null;
		m_hasErrors = false;
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
			return pool.acquire();
		}
	}

	public <T> T query(DbPool pool, ResultProcessor<T> processor, String sql, Object... args) throws SQLException
	{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = newConnection(pool);
		try
		{
			statement = preparedStatement(connection, sql, args);
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

	private void close(DbPool pool, Connection connection)
	{
		if (ObjectExtras.equals(m_connection, connection)) return;
		pool.release(connection, m_hasErrors);
	}

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
			m_pool = null;
			m_connection = null;
			pool.release(c, m_hasErrors);
		}
	}


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

	public boolean hasErrors()
	{
		return m_hasErrors;
	}

	public <T> T queryOne(DbPool pool, String query, Object... args) throws SQLException
	{
		m_singleResultProcessor.reset();
		return (T) query(pool, m_singleResultProcessor, query, args);
	}


	public <T> List<T> queryAll(DbPool pool, String query, Object... args) throws SQLException
	{
		m_allResultProcessor.reset();
		return (List<T>) query(pool, m_allResultProcessor, query, args);
	}

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

