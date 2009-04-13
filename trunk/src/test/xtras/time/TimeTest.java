package xtras.time;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

public class TimeTest extends TestCase
{
	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmptyTest()
	{
		new Time();
	}

	public void testParseTimeStringMs() throws Exception
	{
		assertEquals(12, Time.parseTimeInterval("  12ms "));
	}

	public void testParseTimeStringSeconds() throws Exception
	{
		assertEquals(12000, Time.parseTimeInterval("12s "));
	}

	public void testParseTimeStringMinutes() throws Exception
	{
		assertEquals(2 * 60 * 1000, Time.parseTimeInterval(" 2m"));
	}

	public void testParseTimeStringHours() throws Exception
	{
		assertEquals(12 * 60 * 1000 * 60, Time.parseTimeInterval("12h"));
	}

	public void testParseTimeStringDays() throws Exception
	{
		assertEquals(3 * 60 * 1000 * 60 * 24, Time.parseTimeInterval("3d"));
	}

	public void testParseComplexTimeString() throws Exception
	{
		assertEquals(4L * 60 * 1000 * 60 * 24 + 6012, Time.parseTimeInterval("4d 6s  12ms"));
	}

	public void testIllegalValue()
	{
		try
		{
			Time.parseTimeInterval("w2h");
			fail("Should fail");
		}
		catch (IllegalArgumentException e)
		{}
	}


	public void testMissingTime()
	{
		try
		{
			Time.parseTimeInterval("3");
			fail("Should fail");
		}
		catch (IllegalArgumentException e)
		{}
	}

	public void testRepresentTime() throws Exception
	{
		assertEquals("12d 1h 5m 45s 559ms", Time.timeIntervalToString(Time.parseTimeInterval("11d 25h 5m 44s 1559ms")));
	}

	public void testRepresentTime0() throws Exception
	{
		assertEquals("0ms", Time.timeIntervalToString(0));
	}
}