package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;
import xtras.util.CollectionExtras;


import java.sql.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

public class DbTest extends TestCase
{
	public void setUp() throws Exception
	{
		Db.unregisterAll();
		File temp = File.createTempFile("dbtest", "db");
		temp.deleteOnExit();
		Db.register("sqlite", "org.sqlite.JDBC", "jdbc:sqlite:" + temp.getAbsolutePath(), "", "", 2);
		Db.addSchema("test", "main", "sqlite");
	}

	public void tearDown()
	{
		Db.unregisterAll();
	}

	public void testRegisterDataSource() throws Exception
	{
		File temp = File.createTempFile("dbtest", "db");
		temp.deleteOnExit();
		Db.register("sqlite2", "org.sqlite.JDBC", "jdbc:sqlite:" + temp.getAbsolutePath(), "", "", 3);
		try
		{
			Db.register("sqlite2", "org.sqlite.JDBC", "jdbc:sqlite:" + temp.getAbsolutePath(), "", "", 3);
			fail();
		}
		catch (IllegalStateException e)
		{
			assertEquals("Db 'sqlite2' already registered.", e.getMessage());
		}
	}


	public void testQueryAll() throws Exception
	{
		Db.update("drop table if exists <test>.people");
		Db.update("create table <test>.people (name, occupation);");
		assertEquals(1, Db.insert("insert into <test>.people values (?, ?)", "Sune", "Programmer"));
		assertEquals(2, Db.insert("insert into <test>.people values (?, ?)", "Gurgi", "QA Engineer"));
		assertEquals("[[Sune, Programmer], [Gurgi, QA Engineer]]",
		             Db.queryAll("select * from <test>.people").toString());
	}

	public void testQueryOne() throws Exception
	{
		Db.update("drop table if exists <test>.people");
		Db.update("create table <test>.people (name, occupation);");
		assertEquals(1, Db.insert("insert into <test>.people values (?, ?)", "Sune", "Programmer"));
		assertEquals(2, Db.insert("insert into <test>.people values (?, ?)", "Gurgi", "QA Engineer"));
		assertEquals("[Sune, Programmer]",
		             Db.<Object>queryOne("select * from <test>.people").toString());
	}

	public void testQuery() throws Exception
	{
		Db.update("drop table if exists <test>.people");
		Db.update("create table <test>.people (name, occupation);");
		assertEquals(1, Db.insert("insert into <test>.people values (?, ?)", "Sune", "Programmer"));
		assertEquals(2, Db.insert("insert into <test>.people values (?, ?)", "Gurgi", "QA Engineer"));
		Integer value = Db.query(new ResultProcessor<Integer>()
		{
			public Integer getResult()
			{
				return 42;
			}

			public boolean process(ResultSet result) throws SQLException
			{
				switch (result.getRow())
				{
					case 2:
						assertEquals("Sune", result.getString(1));
						assertEquals("Programmer", result.getString(2));
						break;
					case 3:
						assertEquals("Gurgi", result.getString(1));
						assertEquals("QA Engineer", result.getString(2));
						break;
					default:
						fail();
				}
				return true;
			}
		}, "select * from <test>.people");
		assertEquals(42, value.intValue());
	}

	public void testInsert() throws Exception
	{
		Db.update("drop table if exists <test>.people");
		Db.update("create table <test>.people (name, occupation);");
		assertEquals(1, Db.insert("insert into <test>.people values (?, ?)", "Sune", "Programmer"));
		assertEquals(2, Db.insert("insert into <test>.people values (?, ?)", "Gurgi", "QA Engineer"));
		assertEquals("[[Sune, Programmer], [Gurgi, QA Engineer]]",
		             Db.queryAll("select * from <test>.people").toString());
	}

	public void testRegisterSchema() throws Exception
	{
		Db.addSchema("bar", "bar_schema", "sqlite");
		try
		{
			Db.addSchema("BAR", "bar_schema", "sqlite");
			fail();
		}
		catch (IllegalStateException e)
		{
			assertEquals("Db schema 'bar' already registered.", e.getMessage());
		}
		try
		{
			Db.addSchema("foo", "bar_schema", "Foo");
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Key 'Foo' not yet registered.", e.getMessage());
		}
	}

	public void testBeginTransaction() throws Exception
	{
		Db.update("drop table if exists <test>.people");
		Db.update("create table <test>.people (name, occupation);");
		Db.beginTransaction();
		assertEquals(true, Db.isInTransaction());
		Db.rollback();
	}

	public void testRollback() throws Exception
	{
		Db.update("drop table if exists <test>.people");
		Db.update("create table <test>.people (name, occupation);");
		Db.beginTransaction(TransactionIsolation.SERIALIZABLE);
		assertEquals(1, Db.insert("insert into <test>.people values (?, ?)", "Sune", "Programmer"));
		assertEquals(2, Db.insert("insert into <test>.people values (?, ?)", "Gurgi", "QA Engineer"));
		Db.rollback();
		assertEquals(0, Db.queryOne("select count(*) from <test>.people"));
	}

	public void testCommit() throws Exception
	{
		Db.update("drop table if exists <test>.people");
		Db.update("create table <test>.people (name, occupation);");
		Db.beginTransaction();
		assertEquals(1, Db.insert("insert into <test>.people values (?, ?)", "Sune", "Programmer"));
		assertEquals(2, Db.insert("insert into <test>.people values (?, ?)", "Gurgi", "QA Engineer"));
		Db.commit();
		assertEquals(false, Db.isInTransaction());
		assertEquals(2, Db.queryOne("select count(*) from <test>.people"));
	}

	public void testRegisterFake() throws Exception
	{
		Db.registerDb("fake", new DbProxyFake(CollectionExtras.mapFromKeyValuePairs("select 1", new FakeResultGenerator()
		{
			public Object[] createResult(int row, Object[] arguments)
			{
				return row == 0 ? new Object[]{2} : null;
			}
		}, "select some stuff", new FakeResultGenerator()
		{
			public Object[] createResult(int row, Object[] arguments)
			{
				return row < 4 ? new Object[]{row * (Integer) arguments[0], row * (Integer) arguments[1]} : null;
			}
		})));
		Db.select("fake");
		assertEquals(2, Db.queryOne("select 1"));
		int index = 0;
		for (Object values : Db.queryAll("select some stuff", 2, 5))
		{
			assertEquals("[" + 2 * index + ", " + 5 * index + "]", values.toString());
			index++;
		}
		final AtomicInteger i = new AtomicInteger(0);

		Integer value = Db.query(new ResultProcessor<Integer>()
		{
			public Integer getResult()
			{
				return 42;
			}

			public boolean process(ResultSet result) throws SQLException
			{
				assertEquals(2 * i.get(), result.getObject(1));
				assertEquals(5 * i.get(), result.getObject(2));
				assertEquals(i.incrementAndGet() + 1, result.getRow());
				return true;
			}
		}, "select some stuff", 2, 5);
		assertEquals((Integer) 42, value);
		assertEquals(4, i.get());
	}

	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testDb() throws Exception
	{
		new Db();
	}

	public void testNoDbs() throws Exception
	{
		Db.unregisterAll();
		try
		{
			Db.queryOne("some query");
			fail();
		}
		catch (IllegalStateException e)
		{
			assertEquals("No db found.", e.getMessage());
		}
	}

	public void testSelect() throws Exception
	{
		try
		{
			Db.select("xyzzy");
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Tried to select unknown database 'xyzzy'.", e.getMessage());
		}
	}

	public void testUnregisterDb() throws Exception
	{
		assertEquals(true, Db.unregisterDb("sqlite"));
		try
		{
			Db.queryOne("some query");
			fail();
		}
		catch (IllegalStateException e)
		{
			assertEquals("No db found.", e.getMessage());
		}
		assertEquals(false, Db.unregisterDb("sqlite"));
		try
		{
			Db.queryOne("some query");
			fail();
		}
		catch (IllegalStateException e)
		{
			assertEquals("No db found.", e.getMessage());
		}
	}
}