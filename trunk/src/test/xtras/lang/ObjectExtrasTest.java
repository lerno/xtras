package xtras.lang;

import junit.framework.TestCase;

/** @author Christoffer Lerno */
public class ObjectExtrasTest extends TestCase
{

	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmptyTest()
	{
		new ObjectExtras();
	}

	public void testEquals() throws Exception
	{
		assertTrue(ObjectExtras.equals("a", "a"));
		assertFalse(ObjectExtras.equals("a", null));
		assertFalse(ObjectExtras.equals(null, "a"));
		assertTrue(ObjectExtras.equals(null, null));
		assertFalse(ObjectExtras.equals("a", "b"));
	}

}
