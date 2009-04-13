package xtras.io;

import xtras.lang.StringExtras;

import java.io.*;
import java.net.*;
import java.util.*;

/** @author Christoffer Lerno */
public class IO
{
	/**
	 * Silently close a closeable object (e.g. a stream).
	 *
	 * @param closeable the closeable to close, may be null.
	 */
	public static void closeSilently(Closeable closeable)
	{
		if (closeable == null) return;
		try
		{
			closeable.close();
		}
		catch (Exception e)
		{
			// Do nothing.
		}
	}

	/**
	 * Silently close a server socket.
	 *
	 * @param socket the server socket to close, may be null.
	 */
	public static void closeSilently(ServerSocket socket)
	{
		if (socket == null) return;
		try
		{
			socket.close();
		}
		catch (Exception e)
		{
			// Do nothing.
		}
	}

	/**
	 * Silently close a socket.
	 *
	 * @param socket the socket to close, may be null.
	 */
	public static void closeSilently(Socket socket)
	{
		if (socket == null) return;
		try
		{
			socket.close();
		}
		catch (Exception e)
		{
			// Do nothing.
		}
	}


	/**
	 * Store a string as a file.
	 *
	 * @param file the file to save to.
	 * @param content the string to save.
	 * @throws IOException if there was an error writing to the file.
	 */
	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
	public static void store(File file, String content) throws IOException
	{
		FileOutputStream stream = null;
		try
		{
			stream = new FileOutputStream(file, false);
			stream.write(content.getBytes("UTF-8"));
		}
		finally
		{
			closeSilently(stream);
		}
	}

	/**
	 * Read an entire file as a string.
	 *
	 * @param file the file to read.
	 * @return the resulting string.
	 * @throws IOException if we failed to read the file.
	 */
	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
	public static String read(File file) throws IOException
	{
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(file);
			int size = stream.available();
			byte[] allBytes = new byte[size];
			int read = 0;
			while (read < allBytes.length)
			{
				int bytesRead = stream.read(allBytes, read, allBytes.length - read);
				if (bytesRead == -1) throw new EOFException("Unexpected EOF.");
				read += bytesRead;
			}
			return new String(allBytes);
		}
		finally
		{
			closeSilently(stream);
		}
	}

	/**
	 * Append string to a local file if it exists, otherwise it is created.
	 * <p>
	 * If the file exist we append data at the end of it.
	 *
	 * @param file the name of the file to append to.
	 * @param content The string to append, must have content and not be null.
	 * @throws IOException if there was some IO error writing the file.
	 */
	public static void append(File file, String content) throws IOException
	{
		assert StringExtras.hasContent(content);

		RandomAccessFile randomAccessFile = null;
		try
		{
			// Open file for writing
			//noinspection IOResourceOpenedButNotSafelyClosed
			randomAccessFile = new RandomAccessFile(file, "rw");

			// Move beyond end of file, ie write at end of file

			randomAccessFile.seek(randomAccessFile.length());

			// write content string
			randomAccessFile.writeBytes(content);
		}
		finally
		{
			IO.closeSilently(randomAccessFile);
		}
	}

	/**
	 * Reads a file, splitting the rows to create a list of rows.
	 *
	 * @param file the file to read.
	 * @return the rows in the file, return as a list of strings.
	 * @throws IOException if we failed to read the file for some reason.
	 */
	public static List<String> readAsList(File file) throws IOException
	{
		String dataFromFile = read(file);
		if (StringExtras.hasContent(dataFromFile))
		{
			return StringExtras.split(dataFromFile, StringExtras.LINE_SEPARATOR);
		}
		return Collections.emptyList();
	}

	/**
	 * Checks if a host can be connected to. NOTE: This method
	 * will swallow any IO exceptions and return false.
	 *
	 * @param host The host to connect to.
	 * @param port The port to connect to.
	 * @return true if a connection could be established, false otherwise.
	 */
	public static boolean isHostReachable(String host,int port)
	{
		Socket testSocket = null;
		try
		{
			//noinspection SocketOpenedButNotSafelyClosed
			testSocket = new Socket(host, port);
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
		finally
		{
			IO.closeSilently(testSocket);
		}
	}

}
