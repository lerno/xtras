package xtras.time;

/**
 * A simple class to measure time. You can stamp a value, and then check the time
 * between this value an the stamp before.
 * <p/>
 * For example, to measure the time it takes for a method to execute:
 * <p><code><pre>
 *   TimeMeasure measure = new TimeMeasure();
 *   measure.stamp();
 *   methodToTime();
 *   measure.stamp();
 *   System.out.println("The method took " + measure.time() + "ms to execute.");
 * </pre></code>
 *
 * @author Christoffer Lerno
 */
public class TimeMeasure
{
	private long m_stamp;
	private long m_stampOld;

	/** Creates a new time measure, stamping both old and current to the current time. */
	public TimeMeasure()
	{
		m_stampOld = System.currentTimeMillis();
		m_stamp = m_stampOld;
	}

	/** @return the time between the latest to stamps. */
	public long time()
	{
		return m_stamp - m_stampOld;
	}

	/**
	 * Stamp the current time.
	 * <p>This sets this time to current, and sets the old current stamp to old. The old old-stamp is discarded.
	 *
	 * @return the time between the latest two stamps (including the one just made).
	 */
	public long stamp()
	{
		m_stampOld = m_stamp;
		m_stamp = System.currentTimeMillis();
		return time();
	}

	/** @return the time of the latest stamp. */
	public long current()
	{
		return m_stamp;
	}

	/** @return time between now and the latest stamp. */
	public long untilNow()
	{
		return System.currentTimeMillis() - m_stamp;
	}

	/** @return the time of the next latest stamp. */
	public long old()
	{
		return m_stampOld;
	}


	public String toString()
	{
		return "Time[" + Time.toMsClock(m_stampOld)
		       + "-" + Time.toMsClock(m_stamp) + "]";
	}
}
