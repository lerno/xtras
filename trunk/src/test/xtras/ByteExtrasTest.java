package xtras;

import junit.framework.*;

/**
 * @author Christoffer Lerno 
 */
public class ByteExtrasTest extends TestCase
{

	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmpty()
	{
		new ByteExtras();
	}

	public void testFromHex() throws Exception
	{
		assertEquals("ABCDEF00112233", ByteExtras.toHex(ByteExtras.fromHex("aBcDeF00112233")));
		try
		{
			ByteExtras.fromHex("ABC");
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("Cannot parse string as hex, length % 2 != 0", e.getMessage());
		}
		try
		{
			ByteExtras.fromHex("ABCG");
			fail();
		}
		catch (IllegalArgumentException e)
		{
			assertEquals("'G' not a valid hex character", e.getMessage());
		}

	}

	public void testFromUTF8() throws Exception
	{
		assertEquals("Test", new String(ByteExtras.fromUTF8("Test")));
	}

	public void testStringToBytes() throws Exception
	{
		try
		{
			ByteExtras.stringToBytes("Test", "NOENCODINGFOO");
			fail();
		}
		catch (RuntimeException e)
		{
			assertEquals("Unsupported encoding: NOENCODINGFOO", e.getMessage());
		}
	}

	public void testUnzipBig() throws Exception
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 4000; i++)
		{
			builder.append('A' + (int) (Math.random() * 26));
		}
		assertEquals(builder.toString(), new String(ByteExtras.unzip(ByteExtras.zip(builder.toString().getBytes()))));
	}

	public void testHead() throws Exception
	{
		byte[] bytes = new byte[]{1, 2, 3};
		assertEquals("010203", ByteExtras.toHex(ByteExtras.head(bytes, 4)));
		assertEquals("010203", ByteExtras.toHex(ByteExtras.head(bytes, 3)));
		assertEquals("0102", ByteExtras.toHex(ByteExtras.head(bytes, 2)));
		assertEquals("01", ByteExtras.toHex(ByteExtras.head(bytes, 1)));
		assertEquals("", ByteExtras.toHex(ByteExtras.head(bytes, 0)));
		assertEquals("", ByteExtras.toHex(ByteExtras.head(bytes, -1)));
	}

	public void testTail() throws Exception
	{
		byte[] bytes = new byte[]{1, 2, 3};
		assertEquals("010203", ByteExtras.toHex(ByteExtras.tail(bytes, 0)));
		assertEquals("0203", ByteExtras.toHex(ByteExtras.tail(bytes, 1)));
		assertEquals("03", ByteExtras.toHex(ByteExtras.tail(bytes, 2)));
		assertEquals("", ByteExtras.toHex(ByteExtras.tail(bytes, 3)));
		assertEquals("", ByteExtras.toHex(ByteExtras.tail(bytes, 4)));
	}

	public void testToHex() throws Exception
	{
		assertEquals("01FF80", ByteExtras.toHex(new byte[]{1, -1, -128}));
	}

}