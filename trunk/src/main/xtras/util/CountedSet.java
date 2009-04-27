package xtras.util;

import java.util.*;

/**
 * A counted set allows each addition of an object to the set to be counted.
 * <p/>
 * Example:
 * <pre>
 * m_countedSet.addAll(Arrays.asList("a", "b", "b", "a", "c", "a"));
 * m_countedSet.get("a"); // returns 3.
 * m_countedSet.get("b"); // returns 2.
 * m_countedSet.get("c"); // returns 3.
 * </pre>
 * 
 * @author Christoffer Lerno
 */
public class CountedSet<C> extends HashMap<C, Integer>
{
	private static final long serialVersionUID = 5092809703906258574L;

	/**
	 * Create an empty counted set.
	 */
	public CountedSet()
	{
	}

	/**
	 * Create a counted set populated with the contents of the given collection.
	 *
	 * @param c the collection to add to this set.
	 */
	public CountedSet(Collection<? extends C> c)
	{
		this();
		addAll(c);
	}

	/**
	 * Adds this key to the set a number of times.
	 *
	 * @param c the key for the counter.
	 * @param timesToAdd the times to add the key, may not be negative.
	 * @throws IllegalArgumentException if the value is negative.
	 * @return the number of times the key has occured in the set.
	 */
	public int add(C c, int timesToAdd)
	{
		if (timesToAdd < 0) throw new IllegalArgumentException("Negative value " + timesToAdd);
		int times = get(c) + timesToAdd;
		if (times == 0) return 0;
		put(c, times);
		return times;
	}

	/**
	 * Counts up the counter for this key.
	 *
	 * @param c the key to add.
	 * @return true if this was the first time this key was added.
	 */
	public boolean add(C c)
	{
		return add(c, 1) > 1;
	}

	/**
	 * Gets the current value of the counter.
	 *
	 * @param key the key of the counter.
	 * @return 0 if no matching value is found otherwise the counter value.
	 */
	public Integer get(Object key)
	{
		Integer currentValue = super.get(key);
		if (currentValue == null)
		{
			return 0;
		}
		else
		{
			return currentValue;
		}
	}

	/**
	 * Adds all values of a collection to this set.
	 *
	 * @param c the collection to add.
	 * @return true if none of the values in the collection occured before in the set.
	 */
	public boolean addAll(Collection<? extends C> c)
	{
		boolean allUnqiue = true;
		for (C value : c)
		{
			allUnqiue = add(value) && allUnqiue;
		}
		return allUnqiue;
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element.
	 *
	 * @param o element whose presence in this set is to be tested.
	 * @return <tt>true</tt> if this set contains the specified element.
	 */
	public boolean contains(Object o)
	{
		return get(o) > 0;
	}

	/**
	 * Returns an iterator over the elements in this set.  The elements are
	 * returned in no particular order.
	 * <p>
	 * Removing a key from the iterator will set the counter to 0 for this value.
	 *
	 * @return an iterator over the elements in this set.
	 */
	public Iterator<C> iterator()
	{
		return keySet().iterator();
	}

	/**
	 * Returns an array containing all of the elements in this set.
	 *
	 * @return an array containing all of the elements in this set.
	 */
	public Object[] toArray()
	{
		return keySet().toArray();
	}

	/**
	 * Returns an array containing all of the elements in this set; the
	 * runtime type of the returned array is that of the specified array.
	 *
	 * @param a the array into which the elements of this set are to
	 * be stored, if it is big enough; otherwise, a new array of the
	 * same runtime type is allocated for this purpose.
	 * @return an array containing the elements of this set.
	 * @throws ArrayStoreException the runtime type of a is not a supertype
	 * of the runtime type of every element in this set.
	 * @throws NullPointerException if the specified array is <tt>null</tt>.
	 */
	@SuppressWarnings({"SuspiciousToArrayCall"})
	public <T> T[] toArray(T[] a)
	{
		return keySet().toArray(a);
	}





	/**
	 * Returns <tt>true</tt> if this set contains all of the elements of the
	 * specified collection.
	 *
	 * @param c collection to be checked for containment in this set.
	 * @return <tt>true</tt> if this set contains all of the elements of the
	 *         specified collection.
	 * @throws NullPointerException if the specified collection is
	 * <tt>null</tt>.
	 */
	public boolean containsAll(Collection<?> c)
	{
		return keySet().containsAll(c);
	}

	/**
	 * Retains only the elements in this set that are contained in the
	 * specified collection.
	 * <p/>
	 * Removing an element means setting the counter of that key to 0.
	 *
	 * @param c collection that defines which elements this set will retain.
	 * @return <tt>true</tt> if this collection changed as a result of the
	 *         call.
	 * @throws NullPointerException if the specified collection is
	 * <tt>null</tt>.
	 */
	public boolean retainAll(Collection<?> c)
	{
		return keySet().retainAll(c);
	}

	/**
	 * Removes from this set all of its elements that are contained in the
	 * specified collection.
	 * <p/>
	 * Removing a value also means setting its counter to 0
	 *
	 * @param c collection that defines which elements will be removed from
	 * this set.
	 * @return <tt>true</tt> if this set changed as a result of the call.
	 * @throws NullPointerException if the specified collection is
	 * <tt>null</tt>.
	 */
	public boolean removeAll(Collection<?> c)
	{
		return keySet().removeAll(c);
	}
}
