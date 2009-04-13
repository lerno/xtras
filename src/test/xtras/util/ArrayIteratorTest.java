package xtras.util;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Christoffer Lerno 
 */

@SuppressWarnings("unchecked")
public class ArrayIteratorTest extends TestCase
{
	ArrayIterator m_arrayIterator;

	public void testHasNextEmpty() throws Exception
	{
		assertFalse(new ArrayIterator(new Object[0]).hasNext());
	}

	public void testHasNextOneElement() throws Exception
	{
		assertTrue(new ArrayIterator(new Object[1]).hasNext());
	}

	public void testHasNextReachedEnd() throws Exception
	{
		Iterator it = new ArrayIterator(new Object[1]);
		it.next();
		assertFalse(it.hasNext());
	}

	public void testNextEmpty() throws Exception
	{
		try
		{
			new ArrayIterator(new Object[0]).next();
			fail("Should fail");
		}
		catch (NoSuchElementException e)
		{
		}
	}

	public void testNextFirst() throws Exception
	{
		assertEquals("a", new ArrayIterator(new Object[]{"a", 2}).next());
	}

	public void testNextMiddle() throws Exception
	{
		Iterator it = new ArrayIterator(new Object[]{"a", 2, "b", "c"});
		it.next();
		it.next();
		assertEquals("b", it.next());
	}


	public void testNextLast() throws Exception
	{
		Iterator it = new ArrayIterator(new Object[]{"a", 2, "b", "c"});
		it.next();
		it.next();
		it.next();
		assertEquals("c", it.next());
	}

	public void testNextNoneLest() throws Exception
	{
		Iterator it = new ArrayIterator(new Object[]{"a", 2, "b", "c"});
		it.next();
		it.next();
		it.next();
		it.next();
		try
		{
			it.next();
			fail("Should fail");
		}
		catch (NoSuchElementException e)
		{
		}
	}

	public void testRemove() throws Exception
	{
		Iterator it = new ArrayIterator(new Object[]{"a", 2, "b", "c"});
		it.next();
		try
		{
			it.remove();
			fail("Should fail");
		}
		catch (UnsupportedOperationException e)
		{
		}
	}
}