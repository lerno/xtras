package xtras.util;

import xtras.lang.ObjectExtras;

/** @author Christoffer Lerno */
@SuppressWarnings({"InstanceVariableNamingConvention", "ClassEscapesDefinedScope"})
public class Tuple<A, B>
{
	public final A first;
	public final B second;

	public Tuple(A object1, B object2)
	{
		this.first = object1;
		this.second = object2;
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Tuple)) return false;
		Tuple tuple = (Tuple) o;
		return ObjectExtras.equals(first, tuple.first) && ObjectExtras.equals(second, tuple.second);
	}

	public int hashCode()
	{
		return 31 * (first != null ? first.hashCode() : 0)
		       + (second != null ? second.hashCode() : 0);
	}
}
