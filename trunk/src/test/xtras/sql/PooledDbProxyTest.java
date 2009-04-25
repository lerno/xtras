package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;
import xtras.lang.ObjectExtras;

import java.io.File;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PooledDbProxyTest extends TestCase
{
	private PooledDbProxy m_db;
	public void setUp() throws Exception
	{
		File temp = File.createTempFile("dbtest", "db");
		temp.deleteOnExit();
		m_db = new PooledDbProxy("sqlite", "org.sqlite.JDBC", "jdbc:sqlite:" + temp.getAbsolutePath(), "", "", 2);
		m_db.addSchema("test", "main");
	}

	public void tearDown()
	{
		m_db.shutdown();
	}

	public void testFailBeginTransaction() throws Exception
	{
		// We want to test what happens when starting a transaction fails.
		createBrokenConnection();

		// We will need to inspect the DbConnection to see that it was properly closed.
		DbConnection dbConnection = m_db.getConnection();

		// Beginning a transaction will now fail.
		try
		{
			m_db.beginTransaction();
			fail();
		}
		catch (SQLException e)
		{
			// Ok!
		}

		assertEquals(false, dbConnection.isInTransaction());
		try
		{
			dbConnection.getConnection();
		}
		catch (SQLException e)
		{
			assertEquals("Connection not open.", e.getMessage());
		}
	}

	public void testFailCommit() throws Exception
	{
		m_db.update("drop table if exists @test.people");
		m_db.update("create table @test.people (name, occupation);");
		m_db.beginTransaction();
		assertEquals(1, m_db.insert("insert into @test.people values (?, ?)", "Sune", "Programmer"));
		DbConnection dbConnection = m_db.getConnection();
		Connection c = dbConnection.getConnection();
		c.close();
		try
		{
			m_db.commit();
			fail();
		}
		catch (SQLException e)
		{
			// Ok!
		}
		assertEquals(false, dbConnection.isInTransaction());
		assertEquals(false, dbConnection.isOpen());
		assertEquals(null, m_db.queryOne("select * from @test.people"));
	}

	@SuppressWarnings({"EqualsAndHashcode"})
	public void testInsertWithZeroKeys() throws Exception
	{
		m_db.update("drop table if exists @test.people");
		m_db.update("create table @test.people (name, occupation);");
		assertEquals(1, m_db.insert("insert into @test.people values (?, ?)", "Sune", "Programmer"));
		m_db.query("select 1");
		DbConnection dbConnection = m_db.getConnection();
		final Connection c = dbConnection.getConnection();
		DbPool pool = m_db.getPool();
		pool.release(ObjectExtras.override(c, new Object()
		{
			@Override
			@SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
			public boolean equals(Object o)
			{
				return true;
			}
			@SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
			public PreparedStatement prepareStatement(String sql) throws SQLException
			{
				final PreparedStatement statement = c.prepareStatement(sql);
				return ObjectExtras.override(statement,
				                             new Object()
				                             {
					                             @SuppressWarnings({"JDBCResourceOpenedButNotSafelyClosed"})
					                             public ResultSet getGeneratedKeys() throws SQLException
					                             {
						                             return ObjectExtras.override(statement.getGeneratedKeys(),
						                                                          new Object()
						                                                          {
							                                                          public boolean next()
							                                                          {
								                                                          return false;
							                                                          }
						                                                          });
					                             }
				                             });
			}
		}), false);
		m_db.close();
		assertEquals(null, m_db.insert("insert into @test.people values (?, ?)", "Sune", "Programmer"));
	}

	public void testFailUpdate() throws Exception
	{
		createBrokenConnection();
		try
		{
			m_db.update("select 1");
			fail();
		}
		catch (SQLException e)
		{
			// Ok!
		}
		assertEquals(false, m_db.getConnection().isOpen());
	}

	public void testFailInsert() throws Exception
	{
		createBrokenConnection();
		try
		{
			m_db.insert("select 1");
			fail();
		}
		catch (SQLException e)
		{
			// Ok!
		}
		assertEquals(false, m_db.getConnection().isOpen());
	}

	public void testFailRollback() throws Exception
	{
		m_db.update("drop table if exists @test.people");
		m_db.update("create table @test.people (name, occupation);");
		m_db.beginTransaction();
		assertEquals(1, m_db.insert("insert into @test.people values (?, ?)", "Sune", "Programmer"));
		DbConnection dbConnection = m_db.getConnection();
		Connection c = dbConnection.getConnection();
		c.close();
		try
		{
			m_db.rollback();
			fail();
		}
		catch (SQLException e)
		{
			// Ok!
		}
		assertEquals(false, dbConnection.isInTransaction());
		assertEquals(false, dbConnection.isOpen());
		assertEquals(null, m_db.queryOne("select * from @test.people"));
	}

	private void createBrokenConnection() throws SQLException
	{
		// How do we break a connection?
		// Like this:
		// 1. Open a query
		m_db.query("select 1;");

		// 2. Retrieve the underlying Connection
		Connection connection = m_db.getConnection().getConnection();

		// 3. Close the query, which will return the connection to the pool.
		m_db.close();

		// 4. Close the underlying connection.
		connection.close();
	}
}