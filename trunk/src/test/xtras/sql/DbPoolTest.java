package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;
import xtras.time.Benchmark;
import xtras.lang.ObjectExtras;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;

public class DbPoolTest extends TestCase
{
	DbPool m_dbPool;
	Connection m_connection;
	File m_temp;

	public void setUp() throws Exception
	{
		m_temp = File.createTempFile("dbtest", "db");
		m_temp.deleteOnExit();
		Class.forName("org.sqlite.JDBC");
		m_dbPool = new DbPool("jdbc:sqlite:" + m_temp.getAbsolutePath(), "", "", 2);
		m_connection = null;
	}

	public void tearDown()
	{
		m_dbPool.shutdown();
	}

	public void testAcquire() throws Exception
	{
		Connection c1 = m_dbPool.acquire();
		final Connection c2 = m_dbPool.acquire();
		Benchmark.start();
		new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					Thread.sleep(30);
				}
				catch (InterruptedException e)
				{
					// Ignore.
				}
				m_dbPool.release(c2, false);
			}
		}.start();
		Connection c3 = m_dbPool.acquire();
		assertEquals(true, Benchmark.end() >= 30);
		assertEquals(c2, c3);
		m_dbPool.release(c1, false);
		assertEquals(c1, m_dbPool.acquire());
	}

	public void testAcquireInterrupted() throws Exception
	{
		// Acquire both connections.
		m_dbPool.acquire();
		m_dbPool.acquire();

		// Try to retrieve a connection on a separate thread.
		final AtomicReference<SQLException> ref = new AtomicReference<SQLException>(null);
		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					m_dbPool.acquire();
				}
				catch (SQLException e)
				{
					ref.set(e);
				}
			}
		};
		t.start();

		// Interrupt the thread.
		t.interrupt();
		t.join();

		// We should have gotten an SQL exception on the thread.
		assertEquals("Interrupt while waiting for connection.", ref.get().getMessage());
	}

	public void testAcquireAfterShutdown() throws Exception
	{
		m_dbPool.shutdown();
		try
		{
			m_dbPool.acquire();
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Db connection already shut down.", e.getMessage());
		}

	}

	public void testAcquireTimeout() throws Exception
	{
		// Steal all connections.
		m_dbPool.acquire();
		m_dbPool.acquire();

		// Wait 2 ms
		m_dbPool.setAcquireTimeout(2);

		try
		{
			m_dbPool.acquire();
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Timeout waiting to acquire db connection, exceeded 2ms.", e.getMessage());
		}
	}

	public void testAcquireNoTimeout() throws Exception
	{
		// Steal all connections.
		Connection c = m_dbPool.acquire();
		m_dbPool.acquire();

		// Wait indefinately.
		m_dbPool.setAcquireTimeout(-1);

		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					m_dbPool.acquire();
				}
				catch (SQLException e)
				{
					// Ignore.
				}
			}
		};
		t.start();
		t.join(30);
		assertEquals(true, t.isAlive());
		m_dbPool.release(c, false);
		t.join();
	}

	public void testRelease() throws Exception
	{
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
		Connection c = m_dbPool.acquire();
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(1, m_dbPool.getConnectionsBusy());
		m_dbPool.release(c, false);
		assertEquals(1, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
		m_dbPool.acquire();
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(1, m_dbPool.getConnectionsBusy());
	}

	public void testReleaseClosedConnection() throws Exception
	{
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
		Connection c = m_dbPool.acquire();
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(1, m_dbPool.getConnectionsBusy());
		c.close();
		m_dbPool.release(c, false);
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
	}

	public void testReleaseClosedConnectionWithTwoWaitingConnections() throws Exception
	{
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
		Connection c1 = m_dbPool.acquire();
		Connection c2 = m_dbPool.acquire();
		final AtomicInteger success = new AtomicInteger(0);
		Thread t1 = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					m_dbPool.acquire();
					success.incrementAndGet();
				}
				catch (SQLException e)
				{
					// Ignore
				}
			}
		};
		Thread t2 = new Thread()
		{
			@Override
			public void run()
			{
				try
				{
					m_dbPool.acquire();
					success.incrementAndGet();
				}
				catch (SQLException e)
				{
					// Ignore
				}
			}
		};
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(2, m_dbPool.getConnectionsBusy());
		t1.start();
		t2.start();
		c1.close();
		c2.close();
		m_dbPool.release(c1, false);
		m_dbPool.release(c2, false);
		t1.join();
		t2.join();
		assertEquals(2, success.get());
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(2, m_dbPool.getConnectionsBusy());
		m_dbPool.release(c1, false);
		m_dbPool.release(c2, false);
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(2, m_dbPool.getConnectionsBusy());
	}

	public void testRepeatRelease() throws Exception
	{
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
		Connection c = m_dbPool.acquire();
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(1, m_dbPool.getConnectionsBusy());
		m_dbPool.release(c, false);
		assertEquals(1, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
		m_dbPool.release(c, false);
		assertEquals(1, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
	}

	@SuppressWarnings({"EqualsAndHashcode"})
	public void testReleaseBrokenConnection() throws Exception
	{
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
		Connection c = m_dbPool.acquire();
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(1, m_dbPool.getConnectionsBusy());
		Connection override = ObjectExtras.override(c, new Object()
		{
			public Statement createStatement() throws SQLException
			{
				return ObjectExtras.adapt(Statement.class, new Object()
				{
					public boolean execute(String sql) throws SQLException
					{
						throw new SQLException();
					}
				});
			}

			@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
			@Override
			public boolean equals(Object obj)
			{
				return true;
			}
		});
		m_dbPool.release(override, false);
		assertEquals(1, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
		m_dbPool.acquire();
		m_dbPool.release(override, true);
		assertEquals(0, m_dbPool.getConnectionsFree());
		assertEquals(0, m_dbPool.getConnectionsBusy());
	}

	public void testToString() throws Exception
	{
		assertEquals("DbPool[jdbc:sqlite:" + m_temp.getAbsolutePath() + ", 0 free, 0 busy]", m_dbPool.toString());
		Connection c = m_dbPool.acquire();
		assertEquals("DbPool[jdbc:sqlite:" + m_temp.getAbsolutePath() + ", 0 free, 1 busy]", m_dbPool.toString());
		m_dbPool.acquire();
		assertEquals("DbPool[jdbc:sqlite:" + m_temp.getAbsolutePath() + ", 0 free, 2 busy]", m_dbPool.toString());
		m_dbPool.release(c, false);
		assertEquals("DbPool[jdbc:sqlite:" + m_temp.getAbsolutePath() + ", 1 free, 1 busy]", m_dbPool.toString());
		m_dbPool.shutdown();
		assertEquals("DbPool[jdbc:sqlite:" + m_temp.getAbsolutePath() + ", SHUTDOWN]", m_dbPool.toString());
	}

	public void testConnectionIsOk() throws Exception
	{
		// Exception when testing connection.
		final Statement s1 = ObjectExtras.adapt(Statement.class, new Object()
		{
			public boolean execute(String sql) throws SQLException
			{
				throw new SQLException();
			}
		});
		assertEquals(false, m_dbPool.connectionIsOk(ObjectExtras.adapt(Connection.class, new Object()
		{
			public Statement createStatement()
			{
				return s1;
			}
		}), true));

		// Exception, but function told connection had no error.
		assertEquals(true, m_dbPool.connectionIsOk(ObjectExtras.adapt(Connection.class, new Object()
		{
			public Statement createStatement()
			{
				return s1;
			}
		}), false));

		// No exception when testing.
		final Statement s2 = ObjectExtras.adapt(Statement.class, new Object()
		{
			public boolean execute(String sql) throws SQLException
			{
				return false;
			}
		});
		assertEquals(true, m_dbPool.connectionIsOk(ObjectExtras.adapt(Connection.class, new Object()
		{
			public Statement createStatement()
			{
				return s2;
			}
		}), true));
	}

	public void testResizePool() throws Exception
	{
		Connection c1 = m_dbPool.acquire();
		Connection c2 = m_dbPool.acquire();
		m_dbPool.release(c1, false);
		Thread.sleep(20);
		m_dbPool.release(c2, false);
		assertEquals(2, m_dbPool.getConnectionsFree());
		m_dbPool.resizePool(10);
		assertEquals(1, m_dbPool.getConnectionsFree());
		assertEquals(c2, m_dbPool.acquire());
		Connection c3 = m_dbPool.acquire();
		assertEquals(0, m_dbPool.getConnectionsFree());
		m_dbPool.resizePool(0);
		assertEquals(2, m_dbPool.getConnectionsBusy());
		m_dbPool.release(c3, false);
		m_dbPool.release(c2, false);
		m_dbPool.resizePool(-1);
		assertEquals(0, m_dbPool.getConnectionsFree());
	}

	public void testGetAcquireTimeout() throws Exception
	{
		m_dbPool.setAcquireTimeout(10);
		assertEquals(10, m_dbPool.getAcquireTimeout());
	}
}