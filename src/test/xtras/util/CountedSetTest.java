package xtras.util;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.util.*;

public class CountedSetTest extends TestCase
{
	CountedSet<String> m_countedSet;

	protected void setUp() throws Exception
	{
		m_countedSet = new CountedSet<String>();
	}


	public void testAddValueZero()
	{
		assertEquals(0, m_countedSet.size());
		m_countedSet.add("Sune", 0);
		assertEquals(0, m_countedSet.size());
	}

	public void testAdd() throws Exception
	{
		assertEquals(0, m_countedSet.get("Sune").intValue());
		m_countedSet.add("Sune");
		assertEquals(1, m_countedSet.get("Sune").intValue());
		m_countedSet.add("Sune");
		assertEquals(2, m_countedSet.get("Sune").intValue());
		m_countedSet.add("Arne");
		assertEquals(2, m_countedSet.get("Sune").intValue());
	}

	public void testAddValue() throws Exception
	{
		assertEquals(0, m_countedSet.get("Sune").intValue());
		m_countedSet.add("Sune", 3);
		assertEquals(3, m_countedSet.get("Sune").intValue());
		m_countedSet.add("Sune", 2);
		assertEquals(5, m_countedSet.get("Sune").intValue());
		m_countedSet.add("Arne", 8);
		assertEquals(5, m_countedSet.get("Sune").intValue());
	}

	public void testAddAll() throws Exception
	{
		m_countedSet.addAll(Arrays.asList("a", "b", "b", "a", "c", "a"));
		assertEquals(3, m_countedSet.get("a").intValue());
		assertEquals(2, m_countedSet.get("b").intValue());
		assertEquals(1, m_countedSet.get("c").intValue());
	}

	public void testConstructor() throws Exception
	{
		m_countedSet = new CountedSet<String>(Arrays.asList("a", "b", "b", "a", "c", "a"));
		assertEquals(3, m_countedSet.get("a").intValue());
		assertEquals(2, m_countedSet.get("b").intValue());
		assertEquals(1, m_countedSet.get("c").intValue());
	}

	public void testRetainAll()
	{
		m_countedSet.addAll(Arrays.asList("a", "b", "b", "a", "c", "a"));
		m_countedSet.retainAll(Arrays.asList("d", "e", null, "b"));
		assertEquals(0, m_countedSet.get("a").intValue());
		assertEquals(2, m_countedSet.get("b").intValue());
		assertEquals(0, m_countedSet.get("c").intValue());
	}

	public void testContains()
	{
		m_countedSet.addAll(Arrays.asList("a", "b", "b", "a", "c", "a"));
		assertEquals(true, m_countedSet.contains("a"));
		assertEquals(true, m_countedSet.contains("c"));
		assertEquals(false, m_countedSet.contains("e"));
		assertEquals(false, m_countedSet.contains(null));
	}

	public void testIterator()
	{
		m_countedSet.addAll(Arrays.asList("a", "b", "b", "a", "c", "a"));
		Iterator<String> it = m_countedSet.iterator();
		String value = it.next();
		assertEquals(true, m_countedSet.get(value) > 0);
		it.remove();
		assertEquals(0, m_countedSet.get(value).intValue());
		it.next();
		assertEquals(true, it.hasNext());
		it.next();
		assertEquals(false, it.hasNext());
	}
	public void testToArray()
	{
		m_countedSet.addAll(Arrays.asList("a", "b", "b", "a", "c", "a"));
		List list = new ArrayList(Arrays.asList(m_countedSet.toArray()));
		Collections.sort(list);
		assertEquals("[a, b, c]", list.toString());
		String[] values = new String[3];
		assertEquals(values, m_countedSet.toArray(values));
		List list2 = new ArrayList(Arrays.asList(values));
		Collections.sort(list2);
		assertEquals("[a, b, c]", list2.toString());
	}

	public void testRemoveAll()
	{
		m_countedSet.addAll(Arrays.asList("a", "b", "b", "a", "c", "a"));
		m_countedSet.removeAll(Arrays.asList("d", "e", null, "b"));
		assertEquals(3, m_countedSet.get("a").intValue());
		assertEquals(0, m_countedSet.get("b").intValue());
		assertEquals(1, m_countedSet.get("c").intValue());
	}


	public void testContainsAll()
	{
		m_countedSet.addAll(Arrays.asList("a", "b", "b", "a", "c", "a"));
		assertEquals(false, m_countedSet.containsAll(Arrays.asList(null, "b")));
		assertEquals(true, m_countedSet.containsAll(Arrays.asList("c", "b")));
	}


}