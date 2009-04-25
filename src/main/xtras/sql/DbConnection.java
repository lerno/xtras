package xtras.sql;

import java.sql.*;

/** @author Christoffer Lerno */
class DbConnection
{
	private Connection m_connection;
	private DbPool m_pool;
	private PreparedStatement m_currentStatement;
	private ResultSet m_resultSet;
	private boolean m_inTransaction;
	private StackTraceElement[] m_stackTrace;
	private boolean m_hasErrors;

	public DbConnection()
	{
		m_inTransaction = false;
		m_stackTrace = null;
		m_hasErrors = false;
	}

	public ResultSet query(String sql, Object... args) throws SQLException
	{
		try
		{
			openStatement(sql, args);
			m_resultSet = m_currentStatement.executeQuery();
			return m_resultSet;
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			throw e;
		}
	}

	@SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
	public <T> T insert(String sql, Object... args) throws SQLException
	{
		try
		{
			openStatement(sql, args);
			m_currentStatement.executeUpdate();
			m_resultSet = m_currentStatement.getGeneratedKeys();
			if (m_resultSet.next())
			{
				return (T) SQL.readResultSet(m_resultSet);
			}
			else
			{
				return null;
			}
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			throw e;
		}
		finally
		{
			close();
		}
	}

	public void closeStatement()
	{
		SQL.closeSilently(m_resultSet);
		m_resultSet = null;
		SQL.closeSilently(m_currentStatement);
		m_currentStatement = null;
	}

	private boolean shouldReleaseConnection()
	{
		if (m_connection == null) return false;
		try
		{
			if (!m_hasErrors &&
			    !m_connection.isClosed() &&
			    m_inTransaction) return false;
		}
		catch (SQLException e)
		{
			// Ignore exception on isClosed(), treat as "should release".
		}
		return true;
	}

	private void releaseConnection(DbPool pool)
	{
		if (!shouldReleaseConnection()) return;
		pool.release(m_connection, m_hasErrors);
		m_inTransaction = false;
		m_connection = null;
		m_stackTrace = null;
		m_hasErrors = false;
		m_pool = null;
	}

	private void setAutoCommit(boolean value) throws SQLException
	{
		try
		{
			getConnection().setAutoCommit(value);
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			throw e;
		}
	}

	public DbConnection open(DbPool pool) throws SQLException
	{
		try
		{
			if (m_connection != null)
			{
				// Opening a connection while
				// in a transaction on the same pool is a no op.
				if (m_inTransaction)
				{
					if (pool.equals(m_pool)) return this;
					throw addStackTrace(new SQLException("Changed pool during transaction."));
				}
				throw addStackTrace(new SQLException("Connection not properly closed."));
			}
			m_connection = pool.acquire();
			m_pool = pool;
			m_hasErrors = false;
			setAutoCommit(true);
			if (Db.getDebugFlag())
			{
				StackTraceElement[] elements = Thread.currentThread().getStackTrace();
				if (elements.length > 3)
				{
					m_stackTrace = new StackTraceElement[elements.length - 3];
					System.arraycopy(elements, 3, m_stackTrace, 0, m_stackTrace.length);
				}
			}
			return this;
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			close();
			throw e;
		}
	}

	@SuppressWarnings({"ThrowableInstanceNeverThrown"})
	private SQLException addStackTrace(SQLException e)
	{
		if (m_stackTrace != null)
		{
			SQLException openException = new SQLException("Connection opened but not closed.");
			openException.setStackTrace(m_stackTrace);
			e.initCause(openException);
		}
		return e;
	}

	public boolean isInTransaction()
	{
		return m_inTransaction;
	}

	public void startTransaction(TransactionIsolation transactionIsolation) throws SQLException
	{
		if (m_inTransaction)
		{
			throw new SQLException("Tried to start transaction while already in transaction.");
		}
		setAutoCommit(false);
		if (transactionIsolation != null)
		{
			m_connection.setTransactionIsolation(transactionIsolation.getId());
		}
		m_inTransaction = true;
	}

	public void rollback() throws SQLException
	{
		try
		{
			if (!m_inTransaction) throw new SQLException("Tried to rollback outside of transaction.");
			m_inTransaction = false;
			m_connection.rollback();
		}
		finally
		{
			close();
		}

	}

	public void close()
	{
		closeStatement();
		releaseConnection(m_pool);
	}

	@SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
	public DbConnection openStatement(String query, Object... args) throws SQLException
	{
		if (m_connection == null)
		{
			throw new SQLException("Tried to open statement without opening a connection.");
		}
		if (m_currentStatement != null)
		{
			throw new SQLException("Tried to open statement with another statement open.");
		}
		try
		{
			m_currentStatement = m_connection.prepareStatement(query);
			int index = 0;
			for (Object arg : args)
			{
				m_currentStatement.setObject(++index, arg);
			}
			return this;
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			throw e;
		}
	}

	public Connection getConnection() throws SQLException
	{
		if (!isOpen()) throw new SQLException("Connection not open.");
		return m_connection;
	}

	public boolean isOpen()
	{
		return m_connection != null;
	}

	public int update(String update, Object... args) throws SQLException
	{
		try
		{
			openStatement(update, args);
			return m_currentStatement.executeUpdate();
		}
		catch (SQLException e)
		{
			m_hasErrors = true;
			throw e;
		}
		finally
		{
			close();
		}
	}

	public void commit() throws SQLException
	{
		try
		{
			if (!m_inTransaction)
			{
				throw new SQLException("Tried to commit transaction outside of transaction.");
			}
			m_inTransaction = false;
			m_connection.commit();
		}
		finally
		{
			close();
		}

	}

	public boolean hasErrors()
	{
		return m_hasErrors;
	}
}

