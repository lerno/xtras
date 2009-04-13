package xtras.lang;

/** @author Christoffer Lerno */
public final class ObjectExtras
{
	protected ObjectExtras()
	{}

	/**
	 * Test equals for two (possibly null) objects.
	 *
	 * @param a object a, may be null.
	 * @param b object b, may be null.
	 * @return true if a == b or a.equals(b) or both a and b is null.
	 */
	@SuppressWarnings({"ObjectEquality"})
	public static boolean equals(Object a, Object b)
	{
		return a == b || (a != null && a.equals(b));
	}
}
