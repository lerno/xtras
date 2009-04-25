package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

public class TransactionIsolationTest extends TestCase
{
	public void testTransactionIsolation() throws Exception
	{
		assertEquals(TransactionIsolation.SERIALIZABLE,
		             TransactionIsolation.valueOf("SERIALIZABLE"));
		assertEquals(TransactionIsolation.READ_UNCOMMITTED,
		             TransactionIsolation.fromId(TransactionIsolation.READ_UNCOMMITTED.getId()));
		assertEquals(null,
		             TransactionIsolation.fromId(-1));
	}
}