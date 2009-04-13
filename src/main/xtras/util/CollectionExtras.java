package xtras.util;

import xtras.util.predicates.Predicates;
import xtras.util.ArrayExtras;
import xtras.lang.StringExtras;
import xtras.util.Predicate;

import java.util.*;


/**
 * Utility methods for collections.
 *
 * @author Christoffer Lerno
 */
public final class CollectionExtras
{
	CollectionExtras() {}

	/**
	 * Returns a list as a comma separated string.
	 *
	 * @param values a collection of values
	 * @return String rep, example "42, 4711, 1977"
	 */
	public static String toString(Iterable values)
	{
		return join(values, ", ");
	}

	/**
	 * Joins the elements of a list using to string of each element,
	 * then inserting the separator between them.
	 *
	 * @param values the values to join.
	 * @param separator the separator string.
	 * @return the joined values as a string.
	 */
	public static String join(Iterable values, String separator)
	{
		return join(values, separator, 16);
	}

	/**
	 * Joins the elements of a list using to string of each element,
	 * then inserting the separator between them.
	 *
	 * @param values the values to join.
	 * @param separator the separator string.
	 * @param estimatedSize the number of chars you think the returned string will contain
	 * @return the joined values as a string.
	 */
	public static String join(Iterable values, String separator, int estimatedSize)
	{
		StringBuilder builder = new StringBuilder(estimatedSize);
		Iterator it = values.iterator();
		if (it.hasNext())
		{
			builder.append(it.next());
		}
		while (it.hasNext())
		{
			builder.append(separator);
			builder.append(it.next());
		}
		return builder.toString();
	}

	/**
	 * Converts an array of strings to a list of integers.
	 * <p/>
	 * Adapted from EzString.
	 *
	 * @param strings the array of strings to convert.
	 * @return a list of the integers parsed from the strings, non-parsable strings are ignored.
	 */
	public static List<Integer> stringArrayToIntegerList(String[] strings)
	{
		ArrayList<Integer> ints = new ArrayList<Integer>();
		for (String s : strings)
		{
			if (StringExtras.hasContent(s))
			{
				try
				{
					ints.add(Integer.valueOf(s.trim()));
				}
				catch (NumberFormatException e)
				{
					// Do not add this value.
				}
			}

		}
		return ints;
	}

	/**
	 * Return a random object from a collection.
	 *
	 * @param collection the collection to return an object from.
	 * @return a random object form the collection, or null if the collection was empty.
	 */
	public static <C> C randomElement(Collection<C> collection)
	{
		if (collection.isEmpty())
		{
			return null;
		}
		int element = (int) (Math.random() * collection.size());
		Iterator<C> it = collection.iterator();
		for (int i = 0; i < element; i++)
		{
			it.next();
		}
		return it.next();
	}

	/**
	 * Filter entries, removing entries in place.
	 *
	 * @param collection the collection to filter in place.
	 * @param filter the filter function to use, the element is removed if the filter evaluates to true.
	 * @return the collection we just performed the filtering on.
	 */
	public static <C, D extends Collection<? extends C>> D filterCollection(D collection, Predicate<C> filter)
	{
		for (Iterator<? extends C> it = collection.iterator(); it.hasNext();)
		{
			if (filter.evaluate(it.next()))
			{
				it.remove();
			}
		}
		return collection;
	}

	/**
	 * Retain entries, removing entries in place.
	 *
	 * @param collection the collection to filter in place.
	 * @param predicate the predicate function to use, the element is removed if the filter evaluates to false.
	 * @return the collection given as parameter.
	 */
	public static <C, D extends Collection<? extends C>> D retainCollection(D collection, Predicate<C> predicate)
	{
		for (Iterator<? extends C> it = collection.iterator(); it.hasNext();)
		{
			if (!predicate.evaluate(it.next()))
			{
				it.remove();
			}
		}
		return collection;
	}


	/**
	 * Filters a collection of strings in place, removing any empty string.
	 *
	 * @param strings the collection of strings to filter.
	 * @return the collection that was filtered in place.
	 */
	public static Collection<String> filterEmptyLines(Collection<String> strings)
	{
		return filterCollection(strings, StringExtras.FILTER_EMPTY_STRING);
	}

	/**
	 * Randomly selects a key from a map, according to weight (the integer value of the key).
	 * <p/>
	 * I.e. "A" => 3, "B" => 1. Gives 75% chance of selecting A, 25% chance of selecting B.
	 *
	 * @param map the map to select from. The value indicates the weight.
	 * @return the selected key or null if this map is empty.
	 */
	public static <C> C selectWeightedRandom(Map<C, Integer> map)
	{
		int totalValue = 0;
		for (Integer value : map.values())
		{
			totalValue += value;
		}
		if (totalValue == 0)
		{
			return null;
		}

		int selectedValue = (int) (Math.random() * totalValue);
		for (Map.Entry<C, Integer> entry : map.entrySet())
		{
			selectedValue -= entry.getValue();
			if (selectedValue < 0)
			{
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Checks if a collection contains any duplicates. Note that this
	 * will be a very costly method to use on long collections, so don't
	 * do it if you don't have to.
	 * The check will be using the equals() method for duplicates.
	 *
	 * @param collection The collection to test.
	 * @return true if there are any duplicates, false otherwise.
	 * @throws NullPointerException if the collection is null.
	 */
	public static boolean hasDuplicates(Collection<?> collection)
	{
		// Can't have duplicates with 0 or 1 collection...
		if (collection.size() < 2)
		{
			return false;
		}
		// Try to add all collection to a set and see if it fails.
		for (Object o : collection)
		{
			if (Collections.frequency(collection, o) > 1)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Return the first element to match the predicate in the collection.
	 *
	 * @param iterable the iterable to find elements in.
	 * @param predicate the predicate used for matching.
	 * @return the first match or null if no match is found.
	 */
	public static <C, D extends C> D getFirstMatch(Iterable<D> iterable, Predicate<C> predicate)
	{
		for (D c : iterable)
		{
			if (predicate.evaluate(c))
			{
				return c;
			}
		}
		return null;
	}

	/**
	 * Return the first element not to match the predicate in the collection.
	 *
	 * @param iterable the iterable to find elements in.
	 * @param predicate the predicate used for matching.
	 * @return the first non match or null if all is matching
	 */
	public static <C, D extends C> C getFirstMismatch(Iterable<D> iterable, Predicate<C> predicate)
	{
		return getFirstMatch(iterable, Predicates.not(predicate));
	}

	/**
	 * Return true if all elements satisfy the predicate.
	 *
	 * @param iterable the iterable to find elements in.
	 * @param predicate the predicate used for matching.
	 * @return true if all elements match the predicate, false otherwise.
	 */
	public static <C> boolean matchAll(Iterable<? extends C> iterable, Predicate<C> predicate)
	{
		return getFirstMismatch(iterable, predicate) == null;
	}

	/**
	 * Return true if none of the elements satisfy the predicate.
	 *
	 * @param iterable the iterable to find elements in.
	 * @param predicate the predicate used for matching.
	 * @return true if none of the elements match the predicate, false otherwise.
	 */
	public static <C> boolean matchNone(Iterable<? extends C> iterable, Predicate<C> predicate)
	{
		return getFirstMatch(iterable, predicate) == null;
	}

	/**
	 * Filter entries, creating a new resulting list.
	 *
	 * @param collection the collection to create the result from.
	 * @param filter the filter function to use, the element is not included if the filter evaluates to true.
	 * @return the filtered list we created.
	 */
	public static <C, D extends C> List<D> toFilteredList(Iterable<D> collection, Predicate<C> filter)
	{
		LinkedList<D> list = new LinkedList<D>();
		for (D value : collection)
		{
			if (!filter.evaluate(value))
			{
				list.add(value);
			}
		}
		return list;
	}

	/**
	 * Retain entries, creating a new resulting list.
	 *
	 * @param collection the collection to filter in place.
	 * @param predicate the predicate function to use, the element is removed if the filter evaluates to false.
	 * @return the filtered list we created.
	 */
	public static <C, D extends C> List<D> toRetainedList(Iterable<D> collection, Predicate<C> predicate)
	{
		LinkedList<D> list = new LinkedList<D>();
		for (D value : collection)
		{
			if (predicate.evaluate(value))
			{
				list.add(value);
			}
		}
		return list;
	}

	/**
	 * Creates a map from a list of key values.
	 * <p/>
	 * E.g. mapFromKeyValuePairs(1, "one", 2, "two"), becomes the map { 1 -> "one", 2 -> "two" }
	 *
	 * @param keyValuePairs a number of key values in alternating order, e.g. key1, value1, key2, value2
	 * @return a key-value map
	 * @throws IllegalArgumentException of the number of elements given is an odd number.
	 */
	public static Map mapFromKeyValuePairs(Object... keyValuePairs)
	{
		if (keyValuePairs.length % 2 != 0) throw new IllegalArgumentException("Incomplete key-value pairs given "
		                                                                      + ArrayExtras.toString(keyValuePairs));
		HashMap map = new HashMap(keyValuePairs.length / 2);
		for (int i = 0; i < (keyValuePairs.length / 2); i++)
		{
			map.put(keyValuePairs[i * 2], keyValuePairs[i * 2 + 1]);
		}
		return map;
	}

	/**
	 * Returns a single value from an iterable by returning the first
	 * value returned by its iterator.
	 *
	 * @param iterable the iterable to get a value from.
	 * @return the first value from tne iterator, or null if the iterator has no elements.
	 */
	public static <T> T getAny(Iterable<? extends T> iterable)
	{
		Iterator<? extends T> iterator = iterable.iterator();
		return iterator.hasNext() ? iterator.next() : null;
	}

}
