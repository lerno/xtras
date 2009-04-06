package xtras;

import junit.framework.TestCase;

import java.util.*;

import xtras.predicates.Predicates;

/**
 * @author Christoffer Lerno 
 */

@SuppressWarnings("unchecked")
public class CollectionExtrasTest extends TestCase
{

	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmptyTest()
	{
		new CollectionExtras();
	}

	public void testToStringWithSingleElement() throws Exception
	{
		assertEquals("3", CollectionExtras.toString(Collections.singletonList(3)));
	}

	public void testToStringWithMultipleElements()
	{
		List<Object> l = new LinkedList<Object>();
		l.add(3);
		l.add("Sune");
		l.add(null);
		assertEquals("3, Sune, null", CollectionExtras.toString(l));
	}

	public void testStringArrayToIntegerList() throws Exception
	{
		String[] testArray = new String[]{"Sune", "1 ", "7y", " 2 ", null, "3"};
		assertEquals("[1, 2, 3]", CollectionExtras.stringArrayToIntegerList(testArray).toString());
	}

	public void testRandomElementNoElements() throws Exception
	{
		LinkedList list = new LinkedList();
		assertEquals(null, CollectionExtras.randomElement(list));
	}

	public void testRandomElementOneElements() throws Exception
	{
		assertEquals("1", CollectionExtras.randomElement(Collections.singletonList("1")));
	}

	public void testRandomElementTwoElements() throws Exception
	{
		LinkedList list = new LinkedList();
		list.add("1");
		list.add("2");
		while (!("1".equals(CollectionExtras.randomElement(list))))
		{
		}
		while (!("2".equals(CollectionExtras.randomElement(list))))
		{
		}
	}

	public void testSelectWeightedRandomIllegal() throws Exception
	{
		assertEquals(null, CollectionExtras.selectWeightedRandom(Collections.singletonMap("String", -1)));
	}

	public void testSelectWeightedRandomEmpty() throws Exception
	{
		CountedSet<String> counterMap = new CountedSet<String>();
		assertEquals(null, CollectionExtras.selectWeightedRandom(counterMap));
	}

	public void testSelectWeightedRandomSingle() throws Exception
	{
		CountedSet<String> counterMap = new CountedSet<String>();

		counterMap.add("Arne");
		assertEquals("Arne", CollectionExtras.selectWeightedRandom(counterMap));
	}


	public void testSelectWeightedRandom() throws Exception
	{
		CountedSet<String> countedSet = new CountedSet<String>(Arrays.asList("Arne", "Arne", "Sune"));
		while ("Arne".equals(CollectionExtras.selectWeightedRandom(countedSet)))
		{
			;
		}
		while ("Sune".equals(CollectionExtras.selectWeightedRandom(countedSet)))
		{
			;
		}
	}

	public void testHasDuplicates()
	{
		Collection<Integer> temp = new ArrayList<Integer>();
		assertFalse(CollectionExtras.hasDuplicates(temp));
		temp.add(Integer.valueOf(3));
		assertFalse(CollectionExtras.hasDuplicates(temp));
		temp.add(Integer.valueOf(4));
		assertFalse(CollectionExtras.hasDuplicates(temp));
		temp.add(Integer.valueOf(3));
		assertTrue(CollectionExtras.hasDuplicates(temp));
	}

	public void testGetFirstMatch() throws Exception
	{
		List<String> strings = Arrays.asList("xxx", "xxx", " # xxx", "xxx");
		assertEquals(" # xxx", CollectionExtras.getFirstMatch(strings, StringExtras.FILTER_HASH_COMMENT));
	}

	public void testGetFirstMismatch() throws Exception
	{
		List<String> strings = Arrays.asList("# xxx", "# xxx", " xxx", "# xxx");
		assertEquals(" xxx", CollectionExtras.getFirstMismatch(strings, StringExtras.FILTER_HASH_COMMENT));
	}

	public void testMatchAll() throws Exception
	{
		List<String> strings = Arrays.asList("# xxx", "# xxx", " xxx", "# xxx");
		assertFalse(CollectionExtras.matchAll(strings, StringExtras.FILTER_HASH_COMMENT));
		List<String> strings2 = Arrays.asList("# xxx", "# xxx", " # xxx", "# xxx");
		assertTrue(CollectionExtras.matchAll(strings2, StringExtras.FILTER_HASH_COMMENT));
	}

	public void testMatchNone() throws Exception
	{
		List<String> strings = Arrays.asList("# xxx", "# xxx", " xxx", "# xxx");
		assertFalse(CollectionExtras.matchNone(strings, StringExtras.FILTER_HASH_COMMENT));
		List<String> strings2 = Arrays.asList(" xxx", " xxx", "  xxx", " xxx");
		assertTrue(CollectionExtras.matchNone(strings2, StringExtras.FILTER_HASH_COMMENT));
	}

	public void testToFilteredList() throws Exception
	{
		List<String> strings = CollectionExtras.toFilteredList(Arrays.asList("# xxx", "# xxx", " xxx", "# xxx"),
		                                                       StringExtras.FILTER_HASH_COMMENT);
		assertEquals("[ xxx]", strings.toString());
	}

	public void testToRetainedList() throws Exception
	{
		List<String> strings = CollectionExtras.toRetainedList(Arrays.asList("# xxx", "# xxx", " xxx", "# xxxx"),
		                                                       StringExtras.FILTER_HASH_COMMENT);
		assertEquals("[# xxx, # xxx, # xxxx]", strings.toString());
	}

	public void testMapFromKeyValuePairsZeroElement() throws Exception
	{
		assertEquals(0, CollectionExtras.mapFromKeyValuePairs().size());
	}

	public void testMapFromKeyValuePairs()
	{
		Map map = CollectionExtras.mapFromKeyValuePairs(1, "one", 2, "two");
		assertEquals(2, map.size());
		assertEquals("one", map.get(1));
		assertEquals("two", map.get(2));
		assertEquals(null, map.get(3));
	}

	public void testMapFromKeyValuePairsOddArguments()
	{
		try
		{
			CollectionExtras.mapFromKeyValuePairs(0);
			fail("Odd arguments should not be allowed");
		}
		catch (IllegalArgumentException e)
		{
		}
	}

	public void testFilterCollection() throws Exception
	{
		List list = new ArrayList(Arrays.asList("A", null, "B", null));
		assertSame(list, CollectionExtras.filterCollection(list, Predicates.NOT_NULL));
		assertEquals(Arrays.asList(null, null), list);
	}

	public void testRetainCollection() throws Exception
	{
		List list = new ArrayList(Arrays.asList("A", null, "B", null));
		assertSame(list, CollectionExtras.retainCollection(list, Predicates.NOT_NULL));
		assertEquals(Arrays.asList("A", "B"), list);
	}

	public void testFilterEmptyLines() throws Exception
	{
		ArrayList<String> list = new ArrayList(Arrays.asList("Foo", "", "Bar", "", ""));
		CollectionExtras.filterEmptyLines(list);
		assertEquals(Arrays.asList("Foo", "Bar"), list);
	}

	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmpty() throws Exception
	{
		new CollectionExtras();
	}
}