package xtras.sql;

import xtras.time.Time;
import xtras.util.Tuple;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A DbPool implementation, which lazily allocates a number of
 * db connections up to its pool size.
 *
 * @author Christoffer Lerno
 */
public class DbPool
{
	private ArrayList<Tuple<Connection, Long>> m_freeConnections;
	private ArrayList<Connection> m_busyConnections;
	private final String m_url;
	private long m_acquireTimeout;
	private int m_poolSize;
	private String m_username;
	private String m_password;
	private volatile boolean m_shutdown;

	/**
	 * Create a db pool for the given db url and credentials.
	 *
	 * @param url the jdbc url to the database resource.
	 * @param username the username for logging into the database.
	 * @param password the password for the user.
	 * @param maxPoolSize the maximum pool size, note that no connections
	 * will be made when the pool is first created. Instead those will be created
	 * on-demand.
	 */
	public DbPool(String url, String username, String password, int maxPoolSize)
	{
		m_url = url;
		m_freeConnections = new ArrayList<Tuple<Connection, Long>>(maxPoolSize);
		m_busyConnections = new ArrayList<Connection>(maxPoolSize);
		m_acquireTimeout = Time.TEN_SECONDS;
		m_password = password;
		m_username = username;
		m_poolSize = maxPoolSize;
		m_shutdown = false;
	}

	/**
	 * Sets the maximum time to wait when waiting for a new connection.
	 * <p/>
	 * Default is 10000 ms.
	 *
	 * @param acquireTimeout the new timeout in ms, a negative value means
	 * no timeout.
	 */
	public void setAcquireTimeout(long acquireTimeout)
	{
		m_acquireTimeout = acquireTimeout;
	}

	/**
	 * Returns the number of ms to wait for a released connection before timing out,
	 * when trying to acquire a connection from the pool.
	 * <p/>
	 * A negative value means no timeout.
	 *
	 * @return the number of ms to wait.
	 */
	public long getAcquireTimeout()
	{
		return m_acquireTimeout;
	}

	/**
	 * Acquire a connection from the pool, waiting at the most {@link #getAcquireTimeout} ms
	 * if there currently are no connections available.
	 * <p/>
	 * <em>This method is thread-safe.</em>
	 *
	 * @return a java.sql.Connection object.
	 * @throws SQLException if the pool was shut down or there was a
	 * timeout waiting for a connection.
	 */
	public synchronized Connection acquire() throws SQLException
	{
		long startTime = System.currentTimeMillis();
		while (true)
		{
			if (m_shutdown) throw new SQLException("Db connection already shut down.");
			createConnectionOnDemand();
			if (m_freeConnections.size() > 0)
			{
				Connection c = m_freeConnections.remove(m_freeConnections.size() - 1).first;
				m_busyConnections.add(c);
				return c;
			}
			try
			{
				waitForConnection(startTime);
			}
			catch (InterruptedException e)
			{
				throw new SQLException("Interrupt while waiting for connection.");
			}
		}
	}

	/**
	 * Performs the internal wait, throwing an exception if the timeout has been reached.
	 *
	 * @param startTime the time when the original call was made.
	 * @throws SQLException if a timeout was detected.
	 * @throws InterruptedException if the wait was interrupted.
	 */
	private void waitForConnection(long startTime) throws SQLException, InterruptedException
	{
		if (m_acquireTimeout < 0)
		{
			wait();
			return;
		}
		long timeToWait = m_acquireTimeout - System.currentTimeMillis() + startTime;
		if (timeToWait < 1)
		{
			throw new SQLException("Timeout waiting to acquire db connection, " +
			                       "exceeded " + Time.timeIntervalToString(m_acquireTimeout) +
			                       ".");
		}
		wait(m_acquireTimeout);
	}

	/**
	 * Create a new connection if there are no free connections and the pool size
	 * has not yet been reached.
	 *
	 * @throws SQLException if there was an error creating a connection.
	 */
	@SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
	private void createConnectionOnDemand() throws SQLException
	{
		if (m_freeConnections.isEmpty() && m_busyConnections.size() < m_poolSize)
		{
			addFreeConnection(DriverManager.getConnection(m_url, m_username, m_password));
		}
	}

	/**
	 * Add a connection to the pool of free connections, together with a timestamp telling
	 * when this connection was added.
	 *
	 * @param connection the connection to add.
	 */
	private synchronized void addFreeConnection(Connection connection)
	{
		m_freeConnections.add(new Tuple<Connection, Long>(connection, System.currentTimeMillis()));
	}


	/**
	 * Tests if a connection is ok using the {@code lastCallHadError} flag as
	 * a hint if the connection should be tested with a simple statement
	 * to see that the db connection is ok.
	 *
	 * @param connection the connection to test.
	 * @param lastCallHadError a hint that the connection had errors and
	 * that a diagnostic db query should be made to verify that it is ok.
	 * @return true if the connection tested ok, false otherwise.
	 */
	@SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
	boolean connectionIsOk(Connection connection, boolean lastCallHadError)
	{
		try
		{
			if (connection.isClosed()) return false;
			if (lastCallHadError)
			{
				Statement s = null;
				try
				{
					s = connection.createStatement();
					s.execute("select 1;");
				}
				finally
				{
					SQL.closeSilently(s);
				}
			}
			return true;
		}
		catch (SQLException e)
		{
			return false;
		}
	}

	/**
	 * Release a connection back to the pool, using a hint {@code lastCallHadError} to
	 * determine if the connection should be tested before being allowed back into the pool.
	 * <p/>
	 * If the connection is not ok <em>and</em> was a member of this pool, it is closed
	 * and discarded.
	 *
	 * @param connection the connection to return to the pool.
	 * @param lastCallHadError a hint that the connection had errors and should
	 * be tested before being returned to the pool.
	 */
	public void release(Connection connection, boolean lastCallHadError)
	{
		boolean connectionOk = connectionIsOk(connection, lastCallHadError);
		releaseConnection(connection, connectionOk);
	}

	private synchronized void releaseConnection(Connection connection, boolean connectionOk)
	{
		// Ignore connections that might already have been released or does not belong to this pool.
		if (!m_busyConnections.remove(connection)) return;
		if (connectionOk)
		{
			addFreeConnection(connection);
		}
		else
		{
			SQL.closeSilently(connection);
		}
		notifyAll();
	}

	public int resizePool(long maxAge)
	{
		List<Connection> oldConnections = removeOldFreeConnections(maxAge);
		for (Connection c : oldConnections)
		{
			SQL.closeSilently(c);
		}
		return oldConnections.size();
	}

	private List<Connection> removeOldFreeConnections(long maxAge)
	{
		long earliestOkTime = System.currentTimeMillis() - maxAge;
		List<Connection> removedConnections = new ArrayList<Connection>();
		for (Iterator<Tuple<Connection, Long>> it = m_freeConnections.iterator(); it.hasNext(); )
		{
			Tuple<Connection, Long> tuple = it.next();
			if (tuple.second > earliestOkTime)
			{
				break;
			}
			it.remove();
			removedConnections.add(tuple.first);
		}
		return removedConnections;
	}

	/**
	 * Causes this db pool to shutdown and release all its connections.
	 * <p/>
	 * All connections, both free and busy will be closed and all
	 * references to the connections will be purged. The pool is then set
	 * in shutdown state and cannot be used.
	 * <p/>
	 * <em>This method is thread-safe.</em>
	 */
	public synchronized void shutdown()
	{
		if (m_shutdown) return;
		for (Tuple<Connection, Long> tuple : m_freeConnections)
		{
			SQL.closeSilently(tuple.first);
		}
		for (Connection c : m_busyConnections)
		{
			SQL.closeSilently(c);
		}
		m_freeConnections.clear();
		m_busyConnections.clear();
		m_shutdown = true;
		notifyAll();
	}

	/**
	 * Checks if this pool has been shutdown or not.
	 *
	 * @return false if this pool is shutdown, true otherwise.
	 */
	public boolean isValid()
	{
		return !m_shutdown;
	}

	/**
	 * Displays a representation of this object on the form:
	 * 'DbPool[jdbc:url:here, 2 free, 4 busy] when ok and
	 * 'DbPool[jdbc:url:here, SHUTDOWN] when shut down.
	 *
	 * @return the representation of this pool.
	 */
	@Override
	public String toString()
	{
		return "DbPool[" + m_url + ", "
		       + (m_shutdown ? "SHUTDOWN" : getConnectionsFree() + " free, " + getConnectionsBusy() + " busy") + "]";
	}

	/**
	 * Retrieves the number of connections that are currently acquired from
	 * the pool and have yet to be released back.
	 *
	 * @return the number of busy connections.
	 */
	public synchronized int getConnectionsBusy()
	{
		return m_busyConnections.size();
	}

	/**
	 * Retrieves the number of connections in the pool that are free to use.
	 *
	 * @return the number of free connections.
	 */
	public synchronized int getConnectionsFree()
	{
		return m_freeConnections.size();
	}

}
