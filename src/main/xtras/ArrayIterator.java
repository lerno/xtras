package xtras;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Wraps an iterator around a primitive array. Does not support remove.
 *
 * @author Christoffer Lerno
 */
public class ArrayIterator<C> implements Iterator<C>
{
	private int m_pointer;
	private final C[] m_array;

	/**
	 * Creates a new array iterator around an array.
	 *
	 * @param array the array to create an iterator for.
	 */
	public ArrayIterator(C[] array)
	{
		m_array = array;
		m_pointer = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasNext()
	{
		return m_pointer < m_array.length;
	}

	/**
	 * {@inheritDoc}
	 */
	public C next()
	{
		if (!hasNext()) throw new NoSuchElementException();
		return m_array[m_pointer++];
	}

	/**
	 * Removes the current element, not supported for array iterators.
	 */
	public void remove()
	{
		throw new UnsupportedOperationException("Cannot use remove on array iterator");
	}

}
