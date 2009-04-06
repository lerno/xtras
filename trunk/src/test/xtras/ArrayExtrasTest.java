package xtras;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;
import xtras.ArrayExtras;

import java.util.Arrays;

@SuppressWarnings({"InstantiationOfUtilityClass"})
public class ArrayExtrasTest extends TestCase
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
