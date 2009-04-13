package xtras.time;

/**
 * Helper for common benchmarks.
 * <p>Usage is simple:
 * <p>Invoke <code>Benchmark.start()</code> to begin taking time, call <code>Benchmark.end()</code> to end.
 * <br>The result will then be available by calling <code>Benchmark.time()</code>.
 * <p>This class uses thread local variables and so may be used simultaneously on several threads
 * without benchmarks interfering with each other.
 *
 * @author Christoffer Lerno
 */
public final class Benchmark
{
	Benchmark() {}

	private static ThreadLocal<TimeMeasure> s_timeMeasure = new ThreadLocal<TimeMeasure>()
	{
		protected TimeMeasure initialValue()
		{
			return new TimeMeasure();
		}
	};

	/**
	 * Stamps the start time.
	 */
	public static void start()
	{
		s_timeMeasure.get().stamp();
	}

	/**
	 * Stamps the end time.
	 *
	 * @return the time the between start time and end time.
	 */
	public static long end()
	{
		return s_timeMeasure.get().stamp();
	}

	/**
	 * @return the time between start time and end time.
	 */
	public static long time()
	{
		return s_timeMeasure.get().time();
	}
}


