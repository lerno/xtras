package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;
import xtras.util.CollectionExtras;


import java.sql.*;
import java.io.File;

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

	public void testQuery() throws Exception
	{
		ResultSet set = Db.query("select 1");
		assertEquals(true, set.next());
		assertEquals(1, set.getInt(1));
		assertEquals(false, set.next());
		Db.close();
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

	public void testInsert() throws Exception
	{
		try
		{
			Db.update("drop table if exists <test>.people");
			Db.update("create table <test>.people (name, occupation);");
			assertEquals(1, Db.insert("insert into <test>.people values (?, ?)", "Sune", "Programmer"));
			assertEquals(2, Db.insert("insert into <test>.people values (?, ?)", "Gurgi", "QA Engineer"));
			ResultSet set = Db.query("select * from <test>.people");
			assertEquals(true, set.next());
			assertEquals("Sune", set.getString(1));
			assertEquals("Programmer", set.getString(2));
			assertEquals(true, set.next());
		}
		finally
		{
			Db.close();
		}
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
			assertEquals("Db schema 'bar' already registered with key 'sqlite'.", e.getMessage());
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
		Db.registerDb(new FakeDb("fake", CollectionExtras.mapFromKeyValuePairs("select 1", new FakeResultGenerator()
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
		int entries = 0;
		ResultSet result = Db.query("select some stuff", 2, 5);
		while (result.next())
		{
			assertEquals(2 * entries, result.getObject(1));
			assertEquals(5 * entries, result.getObject(2));
			assertEquals(entries + 1, result.getRow());
			entries++;
		}
		assertEquals(4, entries);
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
			Db.query("some query");
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
			Db.query("some query");
		}
		catch (IllegalStateException e)
		{
			assertEquals("No db found.", e.getMessage());
		}
		assertEquals(false, Db.unregisterDb("sqlite"));
		try
		{
			Db.query("some query");
		}
		catch (IllegalStateException e)
		{
			assertEquals("No db found.", e.getMessage());
		}
	}
}