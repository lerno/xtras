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


	private interface Test1
	{
		String toString();

		int intValue();

		String toUpperCase();

		boolean isImplemented();
	}

	public void testAdapt() throws Exception
	{
		Test1 obj1 = ObjectExtras.adapt(Test1.class, "foo");
		Test1 obj2 = ObjectExtras.adapt(Test1.class, 1);
		assertEquals("foo", obj1.toString());
		assertEquals("1", obj2.toString());
		assertEquals("FOO", obj1.toUpperCase());
		assertEquals(null, obj2.toUpperCase());
		assertEquals(-1, obj1.intValue());
		assertEquals(1, obj2.intValue());
		assertEquals(false, obj1.isImplemented());
	}

	private static class Test1Impl implements Test1
	{
		private final int m_value;
		private final String m_string;

		private Test1Impl(String string, int value)
		{
			m_string = string;
			m_value = value;
		}

		public int intValue()
		{
			return m_value;
		}

		public boolean isImplemented()
		{
			return true;
		}

		public String toUpperCase()
		{
			return m_string.toUpperCase();
		}
	}

	public void testOverride() throws Exception
	{
		Test1 obj1 = new Test1Impl("Foo", 5);
		Test1 obj1Override = ObjectExtras.override(obj1, new Object()
		{
			public String toUpperCase()
			{
				return "UPPERCASE";
			}
		});
		assertEquals("FOO", obj1.toUpperCase());
		assertEquals("UPPERCASE", obj1Override.toUpperCase());
		assertEquals(5, obj1Override.intValue());
		assertEquals(true, obj1Override.isImplemented());
		assertEquals(obj1Override, obj1);
		assertEquals(obj1.hashCode(), obj1Override.hashCode());
	}

	private static class Test1Impl2 implements Test1
	{
		private Test1Impl2()
		{
		}

		public int intValue()
		{
			return -1;
		}

		public boolean isImplemented()
		{
			return true;
		}

		public String toUpperCase()
		{
			throw new RuntimeException("FOO");
		}
	}

	public void testOverrideWithExceptions() throws Exception
	{
		Test1 obj1 = new Test1Impl2();
		Test1 obj1Override = ObjectExtras.override(obj1, new Object()
		{
			public String toUpperCase()
			{
				throw new RuntimeException("BAR");
			}
		});
		try
		{
			obj1.toUpperCase();
		}
		catch (RuntimeException e)
		{
			assertEquals("FOO", e.getMessage());
		}
		try
		{
			obj1Override.toUpperCase();
		}
		catch (RuntimeException e)
		{
			assertEquals("BAR", e.getMessage());
		}
		try
		{
			ObjectExtras.override(obj1, new Object()).toUpperCase();
		}
		catch (RuntimeException e)
		{
			assertEquals("FOO", e.getMessage());
		}
	}

}
