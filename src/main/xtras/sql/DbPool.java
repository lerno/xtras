package xtras.sql;

import xtras.time.Time;
import xtras.util.Tuple;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** @author Christoffer Lerno */
public class DbPool
{
	private ArrayList<Tuple<Connection, Long>> m_freeConnections;
	private ArrayList<Connection> m_busyConnections;
	private final String m_url;
	private long m_pollTimeout;
	private int m_poolSize;
	private String m_username;
	private String m_password;
	private volatile boolean m_shutdown;

	public DbPool(String url, String username, String password, int poolSize)
	{
		m_url = url;
		m_freeConnections = new ArrayList<Tuple<Connection, Long>>(poolSize);
		m_busyConnections = new ArrayList<Connection>(poolSize);
		m_pollTimeout = Time.TEN_SECONDS;
		m_password = password;
		m_username = username;
		m_poolSize = poolSize;
		m_shutdown = false;
	}

	public void setPollTimeout(long pollTimeout)
	{
		m_pollTimeout = pollTimeout;
	}

	@SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
	public synchronized Connection acquire() throws SQLException
	{
		boolean firstPass = true;
		while (true)
		{
			if (m_shutdown) throw new SQLException("Db connection already shut down.");
			if (m_freeConnections.isEmpty() && m_busyConnections.size() < m_poolSize)
			{
				addFreeConnection(DriverManager.getConnection(m_url, m_username, m_password));
			}
			if (m_freeConnections.isEmpty())
			{
				if (firstPass)
				{
					try
					{
						wait(m_pollTimeout);
					}
					catch (InterruptedException e)
					{
						throw new SQLException("Interrupt while waiting for connection.");
					}
				}
				else
				{
					throw new SQLException("Timeout waiting to acquire db connection, " +
					                       "exceeded " + Time.timeIntervalToString(m_pollTimeout) +
					                       ".");
				}
			}
			else
			{
				Connection c = m_freeConnections.remove(m_freeConnections.size() - 1).first;
				m_busyConnections.add(c);
				return c;
			}
			firstPass = false;
		}
	}

	private synchronized void addFreeConnection(Connection connection)
	{
		m_freeConnections.add(new Tuple<Connection, Long>(connection, System.currentTimeMillis()));
	}


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

	public void release(Connection connection, boolean lastCallHadError)
	{
		boolean connectionOk = connectionIsOk(connection, lastCallHadError);
		releaseConnection(connection, connectionOk);
	}

	private synchronized void releaseConnection(Connection connection, boolean connectionOk)
	{
		if (!m_busyConnections.remove(connection)) return;
		if (connectionOk)
		{
			addFreeConnection(connection);
			notify();
		}
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

	public synchronized void shutdown()
	{
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

	public synchronized int getConnectionsBusy()
	{
		return m_busyConnections.size();
	}

	public synchronized int getConnectionsFree()
	{
		return m_freeConnections.size();
	}

}
