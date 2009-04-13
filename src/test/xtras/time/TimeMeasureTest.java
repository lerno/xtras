package xtras.time;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

public class TimeMeasureTest extends TestCase
{
	TimeMeasure m_timeMeasure;

	public void testTimeMeasure() throws Exception
	{
		m_timeMeasure = new TimeMeasure();
		long current = m_timeMeasure.current();
		assertEquals(0, m_timeMeasure.time());
		Thread.sleep(50);
		long untilNow = m_timeMeasure.untilNow();
		long time = m_timeMeasure.stamp();
		assertEquals(current, m_timeMeasure.old());
		assertEquals("Time[" + Time.toMsClock(m_timeMeasure.old())
		             + "-" + Time.toMsClock(m_timeMeasure.current()) + "]", m_timeMeasure.toString());
		assertEquals(time + current, m_timeMeasure.current());
		assertEquals(true, untilNow >= 10);
		assertEquals(true, time >= untilNow);
		assertEquals(true, time >= 10);
		assertEquals(time, m_timeMeasure.time());
		m_timeMeasure.stamp();
		m_timeMeasure.stamp();
		assertEquals(true, m_timeMeasure.time() < 50);
	}
}