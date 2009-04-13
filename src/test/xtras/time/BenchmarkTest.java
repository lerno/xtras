package xtras.time;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

public class BenchmarkTest extends TestCase
{
	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmptyTest()
	{
		new Benchmark();
	}

	public void testStart() throws Exception
	{
		Benchmark.start();
		Thread.sleep(10);
		long stop = Benchmark.end();
		assertEquals(true, stop >= 10);
		assertEquals(stop, Benchmark.time());
	}
}