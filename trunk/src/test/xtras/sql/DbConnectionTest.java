package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;
import xtras.lang.ObjectExtras;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

public class DbConnectionTest extends TestCase
{
	DbConnection m_dbConnection;
	DbPool m_pool;

	@Override
	protected void setUp() throws Exception
	{
		m_dbConnection = new DbConnection();
		File temp = File.createTempFile("dbtest", "db");
		temp.deleteOnExit();
		Class.forName("org.sqlite.JDBC");
		m_pool = new DbPool("jdbc:sqlite:" + temp.getAbsolutePath(), "", "", 2);
	}

	public void tearDown() throws SQLException
	{
		if (m_dbConnection.isInTransaction())
		{
			m_dbConnection.rollback();
		}
		else
		{
			m_dbConnection.close();
		}
	}

	@SuppressWarnings({"EqualsAndHashcode"})
	public void testFailToTestConnection() throws Exception
	{
		Connection c = m_pool.acquire();
		final AtomicInteger i = new AtomicInteger(0);
		m_pool.release(ObjectExtras.override(c, new Object()
		{
			@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
			@Override
			public boolean equals(Object obj)
			{
				return true;
			}
			public boolean isClosed() throws SQLException
			{
				if (i.getAndIncrement() > 0)
				{
					throw new SQLException();
				}
				else
				{
					return false;
				}
			}
		}), false);
		assertEquals(0, m_pool.getConnectionsBusy());
		assertEquals(1, m_pool.getConnectionsFree());
		m_dbConnection.open(m_pool);
		assertEquals(1, m_pool.getConnectionsBusy());
		assertEquals(0, m_pool.getConnectionsFree());
		m_dbConnection.startTransaction(null);
		m_dbConnection.close();
		assertEquals(0, m_pool.getConnectionsBusy());
		assertEquals(0, m_pool.getConnectionsFree());

	}

	public void testErrorsRemovedAfterClose() throws SQLException
	{
		m_dbConnection.open(m_pool);
		try
		{
			m_dbConnection.query("Illegal query");
			fail();
		}
		catch (SQLException e)
		{}
		finally
		{
			m_dbConnection.close();
		}
		assertEquals(false, m_dbConnection.hasErrors());
	}

	public void testQueryErrors() throws Exception
	{
		assertEquals(false, m_dbConnection.hasErrors());
		m_dbConnection.open(m_pool);
		m_dbConnection.query("select 1");
		m_dbConnection.close();
		assertEquals(false, m_dbConnection.hasErrors());
		m_dbConnection.open(m_pool);
		try
		{
			m_dbConnection.query("Illegal query");
			fail();
		}
		catch (SQLException e)
		{}
		assertEquals(true, m_dbConnection.hasErrors());
	}


	public void testUpdateErrors() throws Exception
	{
		assertEquals(false, m_dbConnection.hasErrors());
		m_dbConnection.open(m_pool);
		m_dbConnection.update("drop table if exists people");
		assertEquals(false, m_dbConnection.hasErrors());
		m_dbConnection.open(m_pool);
		try
		{
			m_dbConnection.update("Illegal update");
			fail();
		}
		catch (SQLException e)
		{
		}
		assertEquals(false, m_dbConnection.hasErrors());
	}
	public void testChangingPoolsDuringTransaction() throws Exception
	{
		m_dbConnection.open(m_pool);
		m_dbConnection.startTransaction(null);
		m_dbConnection.open(m_pool); // This is ok!
		try
		{
			m_dbConnection.open(new DbPool("x", "y", "z", 0));
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Changed pool during transaction.", e.getMessage());
		}
		assertEquals(false, m_dbConnection.isInTransaction());
		assertEquals(false, m_dbConnection.isOpen());
		assertEquals(false, m_dbConnection.hasErrors());
	}

	public void testInsertErrors() throws Exception
	{
		m_dbConnection.open(m_pool);
		m_dbConnection.startTransaction(TransactionIsolation.SERIALIZABLE);
		m_dbConnection.update("drop table if exists people");
		m_dbConnection.update("create table people (name, occupation);");
		m_dbConnection.commit();
		m_dbConnection.open(m_pool);
		m_dbConnection.insert("insert into people values (?, ?)", "Sune", "Programmer");
		assertEquals(false, m_dbConnection.isOpen());
		assertEquals(false, m_dbConnection.hasErrors());
		m_dbConnection.open(m_pool);
		try
		{
			m_dbConnection.insert("Illegal query");
			fail();
		}
		catch (SQLException e)
		{
		}
		assertEquals(false, m_dbConnection.isOpen());
		assertEquals(false, m_dbConnection.hasErrors());
	}

	public void testDoubleOpen() throws Exception
	{
		m_dbConnection.open(m_pool);
		assertEquals(true, m_dbConnection.isOpen());
		assertEquals(false, m_dbConnection.isInTransaction());
		try
		{
			m_dbConnection.open(m_pool);
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Connection not properly closed.", e.getMessage());
		}
		assertEquals(false, m_dbConnection.isOpen());
	}

	public void testErronouslyCommit() throws Exception
	{
		m_dbConnection.open(m_pool);
		assertEquals(true, m_dbConnection.isOpen());
		assertEquals(false, m_dbConnection.isInTransaction());
		try
		{
			m_dbConnection.commit();
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Tried to commit transaction outside of transaction.", e.getMessage());
		}
		assertEquals(false, m_dbConnection.isOpen());
	}

	public void testErronouslyRollback() throws Exception
	{
		m_dbConnection.open(m_pool);
		assertEquals(true, m_dbConnection.isOpen());
		assertEquals(false, m_dbConnection.isInTransaction());
		try
		{
			m_dbConnection.rollback();
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Tried to rollback outside of transaction.", e.getMessage());
		}
		assertEquals(false, m_dbConnection.isOpen());
	}

	public void testDoubleStartTransaction() throws Exception
	{
		m_dbConnection.open(m_pool);
		m_dbConnection.startTransaction(null);
		assertEquals(true, m_dbConnection.isOpen());
		assertEquals(true, m_dbConnection.isInTransaction());
		try
		{
			m_dbConnection.startTransaction(null);
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Tried to start transaction while already in transaction.", e.getMessage());
		}
		assertEquals(true, m_dbConnection.isOpen());
	}

	public void testOpenStatementWithoutConnection() throws Exception
	{
		try
		{
			m_dbConnection.openStatement("query");
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Tried to open statement without opening a connection.", e.getMessage());
		}
	}

	public void testOpenTwoStatements() throws Exception
	{
		m_dbConnection.open(m_pool);
		m_dbConnection.openStatement("select 1");
		try
		{
			m_dbConnection.openStatement("select 1");
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Tried to open statement with another statement open.", e.getMessage());
		}
	}

}