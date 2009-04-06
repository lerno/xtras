package xtras;

/** @author Christoffer Lerno */
public final class ObjectExtras
{
	protected ObjectExtras()
	{}

	public static boolean equals(Object a, Object b)
	{
		return a == b || (a != null && a.equals(b));
	}
}
