package xtras.lang;

import xtras.util.Predicate;

import java.util.*;

/** @author Christoffer Lerno */
public final class StringExtras
{
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	StringExtras()
	{
	}

	/**
	 * Pads the string with spaces up to the given length. Will not affect strings longer than given length.
	 *
	 * @param string the string to pad.
	 * @param length the min length.
	 * @return A right padded string
	 */
	public static String padRight(String string, int length)
	{
		StringBuilder result = new StringBuilder(length).append(string);
		return append(result, ' ', length - result.length()).toString();
	}

	/**
	 * Appends the char ch num times at the end of a StringBuffer.
	 *
	 * @param builder A StringBuilder.
	 * @param ch The char to append.
	 * @param num The number of times to append it.
	 * @return the appended StringBuilder, for possible chaining.
	 */
	public static StringBuilder append(StringBuilder builder, char ch, int num)
	{
		for (int i = 0; i < num; i++)
		{
			builder.append(ch);
		}
		return builder;
	}

	/**
	 * Pad a string with spaces at the beginning to the maximum size.
	 *
	 * @param s the string to pad.
	 * @param toSize desired string size.
	 * @return the padded string.
	 */
	public static String pad(String s, int toSize)
	{
		return pad(s, ' ', toSize);
	}

	/**
	 * Pad a string with char at the beginning to the maximum size.
	 *
	 * @param s the string to pad.
	 * @param c the character to use for padding.
	 * @param toSize desired string size.
	 * @return the padded string.
	 */
	public static String pad(String s, char c, int toSize)
	{
		StringBuilder result = new StringBuilder(toSize);
		return append(result, c, toSize - s.length()).append(s).toString();
	}

	/**
	 * Capitalizes a string, by turning the first character into uppercase if possible.
	 * <p>For example: "hello cruel world" would become "Hello cruel world"
	 *
	 * @param string the string to convert.
	 * @return the string capitalized.
	 */
	public static String capitalize(String string)
	{
		if (string.length() == 0)
		{
			return "";
		}
		StringBuilder builder = new StringBuilder();
		builder.append(Character.toUpperCase(string.charAt(0)));
		if (string.length() > 0)
		{
			builder.append(string.substring(1));
		}
		return builder.toString();
	}

	/**
	 * Converts a CAPS-based string of the type used for constants,
	 * to a normal name.
	 * <p>For example HELLO_CRUEL_WORLD becomes hello cruel world.
	 *
	 * @param caps the value in CAPS.
	 * @return the same value as a normal name.
	 */
	public static String capsToName(String caps)
	{
		return caps.replace("_", " ").toLowerCase();
	}

	/**
	 * Converts a CAPS-based string of the type used for constants, to a CamelHump version.
	 * <p>For example HELLO_CRUEL_WORLD becomes HelloCruelWorld
	 *
	 * @param caps the value in CAPS
	 * @return the same value as CamelHump
	 */
	public static String capsToCamel(String caps)
	{
		String[] parts = caps.split("_");
		StringBuilder builder = new StringBuilder();
		for (String part : parts)
		{
			builder.append(capitalize(part.toLowerCase()));
		}
		return builder.toString();
	}

	/**
	 * Returns the length of the trimmed string, returning -1 if the string is null.
	 *
	 * @param s the string to test.
	 * @return the trimmed string length or -1 if the string is null.
	 */
	public static int contentLength(String s)
	{
		return s == null ? -1 : s.trim().length();

	}

	/**
	 * Tests if a string has content.
	 * <p/>
	 * Adapted from EzString.
	 *
	 * @param s the string to be checked
	 * @return true if the string has content, false if it only has blanks or is null.
	 */
	public static boolean hasContent(String s)
	{
		return contentLength(s) > 0;
	}

	/**
	 * Split the data string in tokens given a delimiter string.
	 * <p/>
	 * From EzString.
	 *
	 * @param data the string to split.
	 * @param delimiters the delimiters to split on. See StringTokenizer for behaviour.
	 * @return a list of strings created from the original string.
	 */
	public static List<String> split(String data, String delimiters)
	{
		StringTokenizer tokenizer = new StringTokenizer(data, delimiters);
		LinkedList<String> tokens = new LinkedList<String>();
		while (tokenizer.hasMoreTokens())
		{
			tokens.add(tokenizer.nextToken());
		}
		return tokens;
	}

	/**
	 * Get a part from qualified class name.
	 * <p/>
	 * if s is "com.foo.Bar"
	 * <p/>
	 * n=0 -> com
	 * <br>
	 * n=1 -> foo
	 * <br>
	 * n=2 -> Bar
	 * <br>
	 * n=3 is a violated precondition the calling client is in error.
	 * <p/>
	 * From EzString.
	 *
	 * @param s string like "com.foo.bar"
	 * @param n n>=0 n<number of parts in s (=3 in com.foo.bar)
	 * @return part of s
	 * @throws IndexOutOfBoundsException if n < 0 or n >= the number of parts.
	 */
	public static String getPartOfQualifiedClassName(String s, int n)
	{
		return StringExtras.split(s, ".").get(n);
	}

	/**
	 * Taken from TreeInfo , its still a local copy in TreeInfo, also used in Help
	 *
	 * @param length the number of spaces.
	 * @return String with only length spaces in it or "" if numberOfSpaces was less than 0. example buildBlankString(3) -> "   "
	 */
	public static String buildBlankString(int length)
	{
		return append(new StringBuilder(Math.max(0, length)), ' ', length).toString();
	}

	// String filters
	public final static Predicate<String> FILTER_EMPTY_STRING = new Predicate<String>()
	{
		public boolean evaluate(String string)
		{
			return !StringExtras.hasContent(string);
		}
	};

	public final static Predicate<String> FILTER_HASH_COMMENT = new Predicate<String>()
	{
		public boolean evaluate(String string)
		{
			return string.trim().startsWith("#");
		}
	};

	/**
	 * Formats a string grouped. This is mainly used for numeric presentations
	 * to be readable. For instance, turning 25000 into 25,000. Note that the
	 * groups are constructed from the right.
	 *
	 * @param source The string to group.
	 * @param groupLength The number of chars in each group. If the length is
	 * greater than the lenght of the data, no grouping will be done.
	 * @param delimeter The string to add between each group.
	 * @return The formatted string.
	 */
	public static String group(String source, int groupLength, String delimeter)
	{
		// No grouping if the groups are longer that the source.
		if (groupLength > source.length())
		{
			return source;
		}
		StringBuilder result = new StringBuilder();
		// First step might be shorter if the source length isn't perfectly matched.
		int currentStep = source.length() % groupLength;
		if (currentStep == 0)
		{
			currentStep = groupLength;
		}
		int currentPosition = 0;
		while (currentPosition < source.length())
		{
			if (result.length() > 0)
			{
				result.append(delimeter);
			}
			result.append(source.substring(currentPosition, currentPosition + currentStep));
			currentPosition += currentStep;
			// Subsequent steps are of the given size.
			currentStep = groupLength;
		}
		return result.toString();
	}

	/**
	 * Find the index of a character in a character sequence.
	 *
	 * @param sequence a character sequence, e.g. a StringBuilder or StringBuffer.
	 * @param character the character to search.
	 * @return the index of the character, or -1 if the character was not found.
	 */
	public static int indexOf(CharSequence sequence, char character)
	{
		int length = sequence.length();
		for (int i = 0; i < length; i++)
		{
			if (sequence.charAt(i) == character)
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Return a string describing the time difference as a string.
	 *
	 * @param timeDifferenceInMs the time difference in milliseconds.
	 * @return a string of the format x seconds/x minutes/x hours/x days.
	 */
	public static String timeDifferenceAsString(long timeDifferenceInMs)
	{
		long timeDifferenceInSeconds = timeDifferenceInMs / 1000;
		int seconds = (int) (timeDifferenceInSeconds % 60);
		timeDifferenceInSeconds /= 60;
		int minutes = (int) (timeDifferenceInSeconds % 60);
		timeDifferenceInSeconds /= 60;
		int hours = (int) (timeDifferenceInSeconds % 24);
		timeDifferenceInSeconds /= 24;
		if (timeDifferenceInSeconds > 0)
		{
			return String.format("%dd %dh %dm %ds", timeDifferenceInSeconds, hours, minutes, seconds);
		}
		else if (hours > 0)
		{
			return String.format("%dh %dm %ds", hours, minutes, seconds);
		}
		else if (minutes > 0)
		{
			return String.format("%d min %d sec", minutes, seconds);
		}
		return String.format("%d second%s", seconds, seconds == 1 ? "" : "s");
	}

	/**
	 * Removes whitespace from the beginning of a string, returning a new string with
	 * the whitespace removed.
	 *
	 * @param line the line to have whitespace removed.
	 * @return the resulting string.
	 */
	public static String trimFirst(String line)
	{
		return line.replaceFirst("^[\\W]*", "");
	}

	/**
	 * Removes whitespace from the end of a string, returning a new string with
	 * the whitespace removed.
	 *
	 * @param line the line to have whitespace removed.
	 * @return the resulting string.
	 */
	public static String trimLast(String line)
	{
		return line.replaceFirst("[\\W]*$", "");
	}

	/**
	 * Return the largest possible string that will return true
	 * for all strings in a collection when a string.startsWith() is performed.
	 * <p/>
	 * E.g. "ABCD", "ABEC", "ABC" will return "AB".
	 *
	 * @param strings the string to get the common string for.
	 * @return the initial substring common to all strings or an empty
	 *         string if the strings has nothing in common.
	 */
	public static String stringInCommon(Collection<String> strings)
	{
		SortedSet<String> sorted = new TreeSet<String>(strings);
		String first = sorted.first();
		String last = sorted.last();
		int maxSize = Math.min(first.length(), last.length());
		for (int i = 0; i < maxSize; i++)
		{
			if (first.charAt(i) != last.charAt(i))
			{
				return first.substring(0, i);
			}
		}
		return first.substring(0, maxSize);
	}

	/**
	 * Split a string into several lines, trying to ensure that words are not
	 * split.
	 *
	 * @param string the string to split.
	 * @param lineLength the maximum line length.
	 * @return a list of lines, at least 1.
	 */
	public static List<String> splitOnLength(String string, int lineLength)
	{
		LinkedList<String> line = new LinkedList<String>();
		String currentLine = string.replace('\n', ' ').replace("  ", " ").trim();
		while (currentLine.length() > lineLength)
		{
			int space = currentLine.length() == lineLength ? lineLength : currentLine.substring(0,
			                                                                                    lineLength +
			                                                                                    1).lastIndexOf(' ');
			if (space != -1)
			{
				line.add(currentLine.substring(0, space));
				currentLine = currentLine.substring(space + 1).trim();
			}
			else
			{
				line.add(currentLine.substring(0, lineLength));
				currentLine = currentLine.substring(lineLength).trim();
			}
		}
		if (currentLine.length() > 0 || line.isEmpty())
		{
			line.add(currentLine);
		}
		return line;
	}

	/**
	 * Format number + type with an added plural s for all values except 1.
	 *
	 * @param amount the amount to present.
	 * @param type the type.
	 * @return the string [amount] [type]s for all values except for when amount is 1, for that value
	 *         it returns [amount] [type].
	 */
	public static String numberWithPluralS(int amount, String type)
	{
		StringBuilder builder = new StringBuilder().append(amount).append(' ').append(type);
		if (amount != 1)
		{
			builder.append('s');
		}
		return builder.toString();
	}
}
