package xtras.util;

import xtras.lang.ObjectExtras;

/**
 * A touple struct to carry two objects.
 *
 * @author Christoffer Lerno
 */
@SuppressWarnings({"InstanceVariableNamingConvention", "ClassEscapesDefinedScope"})
public class Tuple<A, B>
{
	/**
	 * The first value of the tuple.
	 */
	public final A first;

	/**
	 * The second value of the tuple.
	 */
	public final B second;

	/**
	 * Create a new tuple.
	 *
	 * @param object1 the first value in the tuple.
	 * @param object2 the second value of the tuple.
	 */
	public Tuple(A object1, B object2)
	{
		this.first = object1;
		this.second = object2;
	}


	/**
	 * Tests if this tuple equals another object.
	 *
	 * @param o the object to test.
	 * @return true if the othe object is a tuple and contains the same two objects
	 * in the same order.
	 */
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (!(o instanceof Tuple)) return false;
		Tuple tuple = (Tuple) o;
		return ObjectExtras.equals(first, tuple.first) && ObjectExtras.equals(second, tuple.second);
	}

	/**
	 * Calculates the hash by taking the hash of both objects in the
	 * tuple and multiplying the first object's hash by 31.
	 *
	 * @return the hash code of this tuple.
	 */
	public int hashCode()
	{
		return 31 * (first != null ? first.hashCode() : 0)
		       + (second != null ? second.hashCode() : 0);
	}
}
