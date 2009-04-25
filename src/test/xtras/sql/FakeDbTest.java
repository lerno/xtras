package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.util.List;
import java.util.Arrays;
import java.sql.SQLException;
import java.sql.ResultSet;

public class FakeDbTest extends TestCase
{
	FakeDb m_fakeDb;

	public void setUp()
	{
		m_fakeDb = new FakeDb("fake");
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
		ResultSet set = m_fakeDb.query("select * from any where y = ?", 3);
		assertEquals(true, set.next());
		assertEquals(0, set.getObject(1));
		assertEquals(true, set.next());
		assertEquals(3, set.getObject(1));
		assertEquals(true, set.next());
		assertEquals(6, set.getObject(1));
		assertEquals(true, set.next());
		assertEquals(9, set.getObject(1));
	}

	public void testEmptyImplementation() throws SQLException
	{
		// All these are no ops.
		m_fakeDb.commit();
		m_fakeDb.close();
		m_fakeDb.rollback();
		assertEquals(false, m_fakeDb.isInTransaction());
		m_fakeDb.beginTransaction();
		m_fakeDb.beginTransaction(TransactionIsolation.SERIALIZABLE);
		assertEquals(false, m_fakeDb.isInTransaction());
		m_fakeDb.addSchema(null, null);
	}
}