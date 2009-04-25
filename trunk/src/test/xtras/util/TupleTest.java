package xtras.util;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

public class TupleTest extends TestCase
{
	@SuppressWarnings({"EqualsBetweenInconvertibleTypes", "LiteralAsArgToStringEquals"})
	public void testEquals() throws Exception
	{
		Tuple a = new Tuple(1, "String");
		assertEquals(a, new Tuple(1, "String"));
		assertEquals(false, a.equals(new Tuple(1, "Foo")));
		assertEquals(false, a.equals(new Tuple(2, "String")));
		assertEquals(a.hashCode(), new Tuple(1, "String").hashCode());
		assertEquals(false, a.equals("String"));
		assertEquals(a.hashCode(), 31 + "String".hashCode());
		assertEquals(a, a);
		assertEquals(0, new Tuple(null, null).hashCode());
		assertEquals(62, new Tuple(2, null).hashCode());
		assertEquals(2, new Tuple(null, 2).hashCode());
	}
}