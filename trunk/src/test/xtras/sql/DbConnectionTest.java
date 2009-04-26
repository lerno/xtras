package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

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
	}

	public void testQueryErrors() throws Exception
	{
		assertEquals(false, m_dbConnection.hasErrors());
		m_dbConnection.query(m_pool, new SingleResultProcessor(), "select 1");
		assertEquals(false, m_dbConnection.hasErrors());
		try
		{
			m_dbConnection.query(m_pool, new SingleResultProcessor(), "Illegal query");
			fail();
		}
		catch (SQLException e)
		{}
		assertEquals(true, m_dbConnection.hasErrors());
	}

	public void testUpdateErrors() throws Exception
	{
		assertEquals(false, m_dbConnection.hasErrors());
		m_dbConnection.update(m_pool, "drop table if exists people");
		assertEquals(false, m_dbConnection.hasErrors());
		try
		{
			m_dbConnection.update(m_pool, "Illegal update");
			fail();
		}
		catch (SQLException e)
		{
		}
		assertEquals(true, m_dbConnection.hasErrors());
	}

	public void testChangingPoolsDuringTransaction() throws Exception
	{
		m_dbConnection.beginTransaction(m_pool, null);
		m_dbConnection.query(m_pool, new SingleResultProcessor(), "select 1");
		try
		{
			m_dbConnection.query(new DbPool("x", "y", "z", 0), new SingleResultProcessor(), "select 1");
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Tried to mix multiple connections in single transaction.", e.getMessage());
		}
		assertEquals(false, m_dbConnection.isInTransaction());
		assertEquals(false, m_dbConnection.hasErrors());
	}

	public void testChangingPoolsDuringTransactionWithRollbackException() throws Exception
	{
		Connection c = m_pool.acquire();
		m_pool.release(c, false);
		m_dbConnection.beginTransaction(m_pool, null);
		m_dbConnection.query(m_pool, new SingleResultProcessor(), "select 1");
		try
		{
			c.close();
			m_dbConnection.query(new DbPool("x", "y", "z", 0), new SingleResultProcessor(), "select 1");
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Tried to mix multiple connections in single transaction.", e.getMessage());
		}
		assertEquals(false, m_dbConnection.isInTransaction());
		assertEquals(true, m_dbConnection.hasErrors());
	}

	public void testInsertErrors() throws Exception
	{
		m_dbConnection.beginTransaction(m_pool, TransactionIsolation.SERIALIZABLE);
		m_dbConnection.update(m_pool, "drop table if exists people");
		m_dbConnection.update(m_pool, "create table people (name, occupation);");
		m_dbConnection.commit();
		m_dbConnection.insert(m_pool, "insert into people values (?, ?)", "Sune", "Programmer");
		assertEquals(false, m_dbConnection.isInTransaction());
		assertEquals(false, m_dbConnection.hasErrors());
		try
		{
			m_dbConnection.insert(m_pool, "Illegal query");
			fail();
		}
		catch (SQLException e)
		{
		}
		assertEquals(true, m_dbConnection.hasErrors());
	}

	public void testErronouslyCommit() throws Exception
	{
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
	}

	public void testErronouslyRollback() throws Exception
	{
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
	}

	public void testDoubleStartTransaction() throws Exception
	{
		m_dbConnection.beginTransaction(m_pool, null);
		assertEquals(true, m_dbConnection.isInTransaction());
		try
		{
			m_dbConnection.beginTransaction(m_pool, null);
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Tried to start transaction while already in transaction.", e.getMessage());
		}
		assertEquals(false, m_dbConnection.isInTransaction());
		assertEquals(false, m_dbConnection.hasErrors());
	}

	public void testDoubleStartTransactionWithRollbackException() throws Exception
	{
		Connection c = m_pool.acquire();
		m_pool.release(c, false);
		m_dbConnection.beginTransaction(m_pool, null);
		assertEquals(true, m_dbConnection.isInTransaction());
		c.close();
		try
		{
			m_dbConnection.beginTransaction(m_pool, null);
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Tried to start transaction while already in transaction.", e.getMessage());
		}
		assertEquals(false, m_dbConnection.isInTransaction());
		assertEquals(true, m_dbConnection.hasErrors());
	}

	public void testBeginTransactionFailed() throws Exception
	{
		Connection c = m_pool.acquire();
		m_pool.release(c, false);
		c.close();
		try
		{
			m_dbConnection.beginTransaction(m_pool, null);
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("database connection closed", e.getMessage());
		}
		assertEquals(false, m_dbConnection.isInTransaction());
		assertEquals(true, m_dbConnection.hasErrors());
	}
}