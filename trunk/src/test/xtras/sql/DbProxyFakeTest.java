package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.sql.SQLException;
import java.sql.ResultSet;

public class DbProxyFakeTest extends TestCase
{
	DbProxyFake m_fakeDb;

	public void setUp()
	{
		m_fakeDb = new DbProxyFake();
	}

	public void testInsert() throws Exception
	{
		m_fakeDb.addQuery("insert 1", Arrays.asList(new Object[]{1, 2}, new Object[]{3, 4}));
		assertEquals("[1, 2]", m_fakeDb.<List<Object>>insert("insert 1").toString());
	}

	public void testUpdate() throws Exception
	{
		m_fakeDb.addQuery("update test set x = 1", 1);
		assertEquals(1, m_fakeDb.update("update test set x = 1"));
	}

	public void testAddQuery() throws Exception
	{
		m_fakeDb.addQuery("select * from any where y = ?", new FakeResultGenerator()
		{
			public Object[] createResult(int row, Object[] arguments)
			{
				return new Object[]{(Integer) arguments[0] * row};
			}
		});
		final AtomicInteger i = new AtomicInteger(0);
		m_fakeDb.query(new ResultProcessor()
		{
			public Object getResult()
			{
				return null;
			}

			public boolean process(ResultSet result) throws SQLException
			{
				assertEquals(i.getAndIncrement() * 3, result.getObject(1));
				return i.get() < 5;
			}
		}, "select * from any where y = ?", 3);
	}

	public void testEmptyImplementation() throws SQLException
	{
		// All these are no ops.
		m_fakeDb.commit();
		m_fakeDb.rollback();
		assertEquals(false, m_fakeDb.inTransaction());
		m_fakeDb.beginTransaction(null);
		m_fakeDb.beginTransaction(TransactionIsolation.SERIALIZABLE);
		assertEquals(false, m_fakeDb.inTransaction());
		m_fakeDb.addAlias(null, null);
	}

	public void testMissingQuery()
	{
		try
		{
			m_fakeDb.query(null, "foobar");
			fail();
		}
		catch (SQLException e)
		{
			assertEquals("Fake db has no answer to the query 'foobar'.", e.getMessage());
		}
	}

	public void testQueryOneZero() throws Exception
	{
		m_fakeDb.addQuery("test", new FakeResultGenerator()
		{
			public Object[] createResult(int row, Object[] arguments)
			{
				return null;
			}
		});
		assertEquals(null, m_fakeDb.query(new SingleResultProcessor(), "test"));
	}

	public void testZeroInsert() throws Exception
	{
		m_fakeDb.addQuery("test", new FakeResultGenerator()
		{
			public Object[] createResult(int row, Object[] arguments)
			{
				return null;
			}
		});
		assertEquals(null, m_fakeDb.insert("test"));
	}
}