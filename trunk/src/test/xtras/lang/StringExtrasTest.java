package xtras.lang;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;
import xtras.lang.StringExtras;
import xtras.util.CollectionExtras;

import java.util.Arrays;

public class StringExtrasTest extends TestCase
{

	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmptyTest()
	{
		new StringExtras();
	}

	public void testFilters()
	{
		assertEquals(true, StringExtras.FILTER_HASH_COMMENT.evaluate("#"));
		assertEquals(true, StringExtras.FILTER_HASH_COMMENT.evaluate(" # eokfe"));
		assertEquals(false, StringExtras.FILTER_HASH_COMMENT.evaluate(""));
		assertEquals(true, StringExtras.FILTER_EMPTY_STRING.evaluate("  \n\r"));
		assertEquals(true, StringExtras.FILTER_EMPTY_STRING.evaluate(""));
		assertEquals(false, StringExtras.FILTER_EMPTY_STRING.evaluate("x"));
	}
	public void testCapsToCamel() throws Exception
	{
		assertEquals("A", StringExtras.capsToCamel("_A_"));
		assertEquals("A", StringExtras.capsToCamel("A_"));
		assertEquals("A", StringExtras.capsToCamel("_A"));
		assertEquals("ABraCaDaBra", StringExtras.capsToCamel("A_BRA_CA_DA_BRA"));
	}

	public void testCapitalize() throws Exception
	{
		assertEquals("Goodbye cruel world", StringExtras.capitalize("goodbye cruel world"));
	}

	public void testCapsToName() throws Exception
	{
		assertEquals(" a ", StringExtras.capsToName("_A_"));
		assertEquals("a ", StringExtras.capsToName("A_"));
		assertEquals(" a", StringExtras.capsToName("_A"));
		assertEquals("a bra ca da bra", StringExtras.capsToName("A_BRA_CA_DA_BRA"));
	}

	public void testContentLengthNull() throws Exception
	{
		assertEquals(-1, StringExtras.contentLength(null));
	}

	public void testContentLength() throws Exception
	{
		assertEquals(2, StringExtras.contentLength("  32 "));
	}

	public void testHasContentTrue() throws Exception
	{
		assertEquals(true, StringExtras.hasContent("  34 "));
	}

	public void testHasContentFalse() throws Exception
	{
		assertEquals(false, StringExtras.hasContent("  \n\r "));
	}

	public void testHasContentNull() throws Exception
	{
		assertEquals(false, StringExtras.hasContent(null));
	}

	public void testSplit() throws Exception
	{
		assertEquals("[abc,  def , g]", StringExtras.split("abc/ def \n/g//", "/\n").toString());
		assertEquals("[1, 2, 3]", StringExtras.split("1*2*3", "*").toString());
	}

	public void testGetPartOfQualifiedClassName() throws Exception
	{
		assertEquals("com", StringExtras.getPartOfQualifiedClassName("com.foo.bar", 0));
		assertEquals("foo", StringExtras.getPartOfQualifiedClassName("com.foo.bar", 1));
		assertEquals("bar", StringExtras.getPartOfQualifiedClassName("com.foo.bar", 2));
	}

	public void testGetPartOfQualifiedClassNameIndexOutOfBounds() throws Exception
	{
		try
		{
			StringExtras.getPartOfQualifiedClassName("com.foo.bar", 3);
			fail("Should fail");
		}
		catch (IndexOutOfBoundsException e)
		{
			// Ok!
		}
	}

	public void testPadRight() throws Exception
	{
		assertEquals("a  ", StringExtras.padRight("a", 3));
	}

	public void testPadRightSameLength() throws Exception
	{
		assertEquals("abc", StringExtras.padRight("abc", 3));
	}

	public void testPadRightTooLong() throws Exception
	{
		assertEquals("abcd", StringExtras.padRight("abcd", 3));
	}

	public void testPadLeft() throws Exception
	{
		assertEquals("  a", StringExtras.pad("a", 3));
	}

	public void testPadLeftSameLength() throws Exception
	{
		assertEquals("abc", StringExtras.pad("abc", 3));
	}

	public void testPadLeftTooLong() throws Exception
	{
		assertEquals("abcd", StringExtras.pad("abcd", 3));
	}


	public void testBuildBlankStringNegativeNumberOfBlanks()
	{
		assertEquals("", StringExtras.buildBlankString(-1));
	}

	public void testBuildBlankStringNoBlanks()
	{
		assertEquals("", StringExtras.buildBlankString(0));
	}

	public void testBuildBlankStringOneBlank()
	{
		assertEquals(" ", StringExtras.buildBlankString(1));
	}

	public void testBuildBlankStringTwoBlanks()
	{
		assertEquals("  ", StringExtras.buildBlankString(2));
	}

	public void testGroup()
	{
		assertEquals("100", StringExtras.group("100", 3, ","));
		assertEquals("100", StringExtras.group("100", 4, ","));
		assertEquals("1,500", StringExtras.group("1500", 3, ","));
		assertEquals("1X500", StringExtras.group("1500", 3, "X"));
		assertEquals("15,00", StringExtras.group("1500", 2, ","));
		assertEquals("2,15,00", StringExtras.group("21500", 2, ","));
	}

	public void testIndexOf() throws Exception
	{
		StringBuilder builder = new StringBuilder("xyzzt");
		assertEquals(-1, StringExtras.indexOf(builder, 'a'));
		assertEquals(4, StringExtras.indexOf(builder, 't'));
		assertEquals(2, StringExtras.indexOf(builder, 'z'));
		assertEquals(0, StringExtras.indexOf(builder, 'x'));
		assertEquals(-1, StringExtras.indexOf(new StringBuilder(), '\0'));
	}

	public void testTimeDifferenceAsString() throws Exception
	{
		assertEquals("1 second", StringExtras.timeDifferenceAsString(1000));
		assertEquals("2 seconds", StringExtras.timeDifferenceAsString(2000));
		assertEquals("59 seconds", StringExtras.timeDifferenceAsString(59000));
		assertEquals("1 min 0 sec", StringExtras.timeDifferenceAsString(60000));
		assertEquals("59 min 59 sec", StringExtras.timeDifferenceAsString(3599000));
		assertEquals("1h 0m 0s", StringExtras.timeDifferenceAsString(3600000));
		assertEquals("23h 59m 59s", StringExtras.timeDifferenceAsString(3600000 * 24 - 1000));
		assertEquals("1d 0h 0m 0s", StringExtras.timeDifferenceAsString(3600000 * 24));
	}

	public void testTrimFirst() throws Exception
	{
		assertEquals("yo \noek", StringExtras.trimFirst(" \n\tyo \noek"));
		assertEquals("yo oek ", StringExtras.trimFirst("yo oek "));
	}

	public void testTrimLast() throws Exception
	{
		assertEquals("yo \noek", StringExtras.trimLast("yo \noek \n\t"));
		assertEquals(" yo oek", StringExtras.trimLast(" yo oek"));
		assertEquals(" yo oek", StringExtras.trimLast(" yo oek "));
	}

	public void testStringInCommon() throws Exception
	{
		assertEquals("ABC", StringExtras.stringInCommon(Arrays.asList("ABCDE", "ABCRF", "ABCDF")));
		assertEquals("", StringExtras.stringInCommon(Arrays.asList("ABCDE", "DER", "BDG")));
		assertEquals("ABC", StringExtras.stringInCommon(Arrays.asList("ABCDE", "ABC", "ABCDF")));
	}

	public void testSplitOnLength() throws Exception
	{
		assertEquals("ABC DE*FGHIJK*LO*MNOP W", CollectionExtras.join(StringExtras.splitOnLength(
				"ABC \nDE FGHIJK LO MNOP W",
				6), "*"));
		assertEquals("A*B*C", CollectionExtras.join(StringExtras.splitOnLength("A\nBC", 1), "*"));

	}

	public void testNumberWithPluralS() throws Exception
	{
		assertEquals("0 sunes", StringExtras.numberWithPluralS(0, "sune"));
		assertEquals("-1 sunes", StringExtras.numberWithPluralS(-1, "sune"));
		assertEquals("1 sune", StringExtras.numberWithPluralS(1, "sune"));
		assertEquals("2 sunes", StringExtras.numberWithPluralS(2, "sune"));
	}
}