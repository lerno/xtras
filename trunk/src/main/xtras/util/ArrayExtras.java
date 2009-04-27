package xtras.util;

import java.util.Iterator;

/**
 * Array related extra utilities.
 *
 * @author Christoffer Lerno
 */
public final class ArrayExtras
{

	ArrayExtras()
	{}

	/**
	 * Returns an int array as a comma separated string.
	 *
	 * @param values int array with values.
	 * @return String rep, example "42, 4711, 1977"
	 */
	public static String toString(int[] values)
	{
		return toString(values, ", ");
	}

	/**
	 * Insert an object into an array, creating a new array.
	 *
	 * @param array the array to insert an object into.
	 * @param object the object to insert.
	 * @param position the position in the array where the object should be inserted.
	 * @return the resulting expanded array.
	 */
	public static Object[] insert(Object[] array, Object object, int position)
	{
		Object[] result = new Object[array.length + 1];
		if (position > 0)
		{
			System.arraycopy(array, 0, result, 0, position);
		}
		if (position < array.length)
		{
			System.arraycopy(array, position, result, position + 1, array.length - position);
		}
		result[position] = object;
		return result;
	}

	/**
	 * Append an object to an array, creating a new array.
	 *
	 * @param array the array to be appended.
	 * @param object the object to append.
	 * @return the resulting expanded array.
	 */
	public static Object[] append(Object[] array, Object object)
	{
		return insert(array, object, array.length);
	}

	/**
	 * Prepend an object to an array, creating a new array.
	 *
	 * @param array the array to be prepended.
	 * @param object the object to prepend.
	 * @return the resulting expanded array.
	 */
	public static Object[] prepend(Object[] array, Object object)
	{
		return insert(array, object, 0);
	}

	/**
	 * Concatenates a list of values to a string, separating each object with the
	 * given separator.
	 *
	 * @param values non null array
	 * @param separator a non null String
	 * @return String rep, example toString(new int[]{3,5,8,9},"*"); ->  "3*5*8*9"
	 */
	public static String toString(int[] values, String separator)
	{
		StringBuilder builder = new StringBuilder(values.length * (separator.length() + 3));
		for (int i : values)
		{
			if (builder.length() > 0)
			{
				builder.append(separator);
			}
			builder.append(i);
		}
		return builder.toString();
	}


	/**
	 * Note Arrays.toString(Objec[]) is an alternativ woth looking into if you want to end up with something like "[foo,3,4]"
	 *
	 * @param values non null array
	 * @param separator a non null String
	 * @return String rep, example toString(new Object[]{3,5,8,9},"*"); ->  "3*5*8*9"
	 */
	public static String toString(Object[] values, String separator)
	{
		return CollectionExtras.join(iterable(values), separator);
	}

	/**
	 * Concatenates a list of values to a string, separating each object with ', '.
	 *
	 * @param values the array to join.
	 * @return the resulting string.
	 */
	public static String toString(Object[] values)
	{
		return toString(values, ", ");
	}

	/**
	 * Creates an iterable for this array to allow it to be used as
	 * any type of iterable.
	 *
	 * @param objectArray the array create an iterable for.
	 * @return the resulting iterable wrapper.
	 */
	public static <C> Iterable<C> iterable(final C[] objectArray)
	{
		return new Iterable<C>()
		{
			public Iterator<C> iterator()
			{
				return new ArrayIterator<C>(objectArray);
			}
		};
	}

}
