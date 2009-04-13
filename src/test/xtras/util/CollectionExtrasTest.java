package xtras.util;

import junit.framework.TestCase;

import java.util.*;

import xtras.util.predicates.Predicates;
import xtras.lang.StringExtras;
import xtras.util.CollectionExtras;
import xtras.util.CountedSet;

/** @author Christoffer Lerno */

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

	public void testGetAny() throws Exception
	{
		assertEquals("T", CollectionExtras.getAny(Arrays.asList("T", "E", "S", "T")));
		assertEquals(null, CollectionExtras.getAny(new HashSet()));
		assertEquals("T", CollectionExtras.getAny(new HashSet(Arrays.asList("T"))));
	}

	@SuppressWarnings({"InstantiationOfUtilityClass"})
public static class ArrayExtrasTest extends TestCase
	{


		@SuppressWarnings({"InstantiationOfUtilityClass"})
		public void testEmptyTest()
		{
			new ArrayExtras();
		}

		public void testIterable()
		{
			String result = "";
			for (String s : ArrayExtras.iterable(new String[]{"a", "b", "c"}))
			{
				result += s;
			}
			assertEquals("abc", result);
		}


		public void testIterableEmpty()
		{
			String result = "";
			for (String s : ArrayExtras.iterable(new String[0]))
			{
				result += s;
			}
			assertEquals("", result);
		}

		public void testToStringWithNoElement() throws Exception
		{
			assertEquals("", ArrayExtras.toString(new int[]{}));
		}

		public void testToStringWithSingleElement() throws Exception
		{
			assertEquals("3", ArrayExtras.toString(new int[]{3}));
		}

		public void testToStringWithMultipleElements()
		{
			assertEquals("3, 4, 6", ArrayExtras.toString(new int[]{3, 4, 6}));
		}


		public void testToStringWithMultipleElementsAndSeparator()
		{
			assertEquals("3-4-6", ArrayExtras.toString(new int[]{3, 4, 6}, "-"));
		}

		public void testToStringWithMultipleElementsAndEmptySeparator()
		{
			assertEquals("346", ArrayExtras.toString(new int[]{3, 4, 6}, ""));
		}


		public void testToStringWithMultipleObjectElementsAndSeparator()
		{
			assertEquals("3-4-6", ArrayExtras.toString(new String[]{"3", "4", "6"}, "-"));
		}

		public void testToStringWithMultipleObjectElementsAndEmptySeparatorString()
		{
			assertEquals("346", ArrayExtras.toString(new Object[]{3, "4", "6"}, ""));
		}

		public void testInsert() throws Exception
		{
			assertTrue(Arrays.equals(new Object[]{"A", 1, 4}, ArrayExtras.insert(new Object[]{"A", 1}, 4, 2)));
			assertTrue(Arrays.equals(new Object[]{"A", 1, 4}, ArrayExtras.insert(new Object[]{"A", 4}, 1, 1)));
			assertTrue(Arrays.equals(new Object[]{"A", 1, 4}, ArrayExtras.insert(new Object[]{1, 4}, "A", 0)));
			assertTrue(Arrays.equals(new Object[]{"A", 1, 4}, ArrayExtras.append(new Object[]{"A", 1}, 4)));
			assertTrue(Arrays.equals(new Object[]{"A", 1, 4}, ArrayExtras.prepend(new Object[]{1, 4}, "A")));
		}
	}

	/**
 * @author Christoffer Lerno
	 */

	@SuppressWarnings("unchecked")
	public static class ArrayIteratorTest extends TestCase
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
}