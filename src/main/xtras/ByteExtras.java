package xtras;

import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.Deflater;

/** @author Christoffer Lerno */
public final class ByteExtras
{
	private final static byte[] EMPTY_ARRAY = new byte[0];
	private final static String HEX = "0123456789ABCDEF";

	ByteExtras()
	{
	}

	/**
	 * Converts an array of bytes to its uppercase hex represenation.
	 * <p/>
	 * E.g. <tt>new byte[] { 1, -1, -128 }</tt> is converted to <tt>"01FF80"</tt>
	 *
	 * @param bytes the bytes to convert.
	 * @return the upper case hex represenation of the byte array.
	 */
	public static String toHex(byte[] bytes)
	{
		StringBuilder builder = new StringBuilder(bytes.length * 2);
		for (byte b : bytes)
		{
			int i = b & 0xFF;
			builder.append(HEX.charAt(i / 16));
			builder.append(HEX.charAt(i % 16));
		}
		return builder.toString();
	}

	/**
	 * Converts string to bytes according to a certain encoding
	 * using <tt>String#getBytes(String)</tt>.
	 * <p/>
	 * However, this method conveniently swallows encoding exceptions,
	 * for use with built-in encodings like UTF-8.
	 *
	 * @param string the string to convert.
	 * @param encoding the encoding to use.
	 * @return the byte encoding of the string.
	 * @throws RuntimeException containing the UnsupportedEncodingException if
	 * the encoding isn't supported.
	 */
	public static byte[] stringToBytes(String string, String encoding)
	{
		try
		{
			return string.getBytes(encoding);
		}
		catch (UnsupportedEncodingException e)
		{
			throw new RuntimeException("Unsupported encoding: " + encoding);
		}
	}

	/**
	 * Returns the UTF-8 encoding of this string using <tt>String#getBytes(String)</tt>
	 * without throwing checked exceptions.
	 *
	 * @param string the string to convert to UTF-8.
	 * @return the resulting bytes.
	 */
	public static byte[] fromUTF8(String string)
	{
		return stringToBytes(string, "UTF-8");
	}

	private static int convertHex(char c)
	{
		if (c >= '0' && c <= '9') return c - '0';
		if (c >= 'A' && c <= 'F') return c - 'A' + 10;
		if (c >= 'a' && c <= 'f') return c - 'a' + 10;
		throw new IllegalArgumentException("'" + c + "' not a valid hex character");
	}

	/**
	 * Parses a hex string into a byte array.
	 *
	 * @param hex the (non-spaced) hex string to convert.
	 * @return the resulting byte array.
	 * @throws IllegalArgumentException if the string cannot be parsed as hex.
	 */
	public static byte[] fromHex(String hex)
	{
		if (hex.length() % 2 == 1) throw new IllegalArgumentException("Cannot parse string as hex, length % 2 != 0");
		byte[] bytes = new byte[hex.length() / 2];
		int index = 0;
		int length = hex.length();
		for (int i = 0; i < length; i += 2)
		{
			int h = convertHex(hex.charAt(i));
			int l = convertHex(hex.charAt(i + 1));
			bytes[index++] = (byte) (h * 16 + l);
		}
		return bytes;
	}

	/**
	 * Returns the head (the first n bytes) of a byte array.
	 *
	 * @param array the array to retrieve the head of.
	 * @param length the number of bytes to retrieve.
	 * @return if the length is less than original array, returns a
	 *         the first <tt>length</tt> bytes in a new array, otherwise
	 *         returns a copy of the original array.
	 */
	public static byte[] head(byte[] array, int length)
	{
		if (length < 1) return EMPTY_ARRAY;
		byte[] result = new byte[Math.min(length, array.length)];
		System.arraycopy(array, 0, result, 0, result.length);
		return result;
	}

	/**
	 * Returns the tail (the last bytes) of a byte array.
	 *
	 * @param array the array to retrieve the tail of.
	 * @param firstIndex the first index to include in the original array.
	 * @return the sub-array starting from (including) the index
	 *         to the end of the original array. If firstIndex is same or
	 *         larger than the length of the original array, an empty array is
	 *         returned.
	 */
	public static byte[] tail(byte[] array, int firstIndex)
	{
		if (array.length <= firstIndex) return EMPTY_ARRAY;
		byte[] result = new byte[array.length - firstIndex];
		System.arraycopy(array, firstIndex, result, 0, result.length);
		return result;
	}

	/**
	 * The concatenation of two arrays.
	 *
	 * @param array1 the first array.
	 * @param array2 the second array.
	 * @return a new array with the combined length of array1 and array2,
	 *         with the elements of array1 followed by the elements of array2.
	 */
	public static byte[] cat(byte[] array1, byte[] array2)
	{
		byte[] result = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, result, 0, array1.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	/**
	 * The partianl concatenation of two arrays.
	 *
	 * @param array1 the first array.
	 * @param array2 the second array to partionally append.
	 * @param length the number of bytes to add from the second array.
	 * @return a new array with the combined length of array1 and <tt>length</tt>,
	 *         with the elements of array1 followed by the <tt>length</tt> first elements from array2.
	 */
	public static byte[] partialCat(byte[] array1, byte[] array2, int length)
	{
		byte[] result = new byte[array1.length + length];
		System.arraycopy(array1, 0, result, 0, array1.length);
		System.arraycopy(array2, 0, result, array1.length, length);
		return result;
	}

	/**
	 * Unzip a byte array to a new (unzipped) byte array.
	 *
	 * @param bytes the bytes to unzip.
	 * @return the unzipped bytes.
	 * @throws java.util.zip.DataFormatException if the bytes to unzip could not be parsed.
	 */
	public static byte[] unzip(byte[] bytes) throws DataFormatException
	{
		Inflater inflater = new Inflater();
		try
		{
			inflater.setInput(bytes);
			byte[] buffer = new byte[1024];
			byte[] result = new byte[0];
			int read;
			while ((read = inflater.inflate(buffer)) == buffer.length)
			{
				result = cat(result, buffer);
			}
			if (read > 0)
			{
				result = partialCat(result, buffer, read);
			}
			return result;
		}
		finally
		{
			inflater.end();
		}
	}

	/**
	 * Zips a number of bytes with optimal compression.
	 *
	 * @param bytes the bytes to compress.
	 * @return the compressed result.
	 */
	public static byte[] zip(byte[] bytes)
	{
		byte[] buffer = new byte[1024];
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
		try
		{
			deflater.setInput(bytes);
			deflater.finish();
			int read;
			byte[] result = new byte[0];
			while ((read = deflater.deflate(buffer)) == buffer.length)
			{
				result = cat(result, buffer);
			}
			if (read > 0)
			{
				result = partialCat(result, buffer, read);
			}
			return result;
		}
		finally
		{
			deflater.end();
		}
	}
}
