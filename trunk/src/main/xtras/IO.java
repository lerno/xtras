package xtras;

import java.io.*;
import java.net.Socket;

/** @author Christoffer Lerno */
public class IO
{
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

	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
	public static String read(File file) throws IOException
	{
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(file);
			int size = stream.available();
			byte[] allBytes = new byte[size];
			stream.read(allBytes);
			return new String(allBytes);
		}
		finally
		{
			closeSilently(stream);
		}
	}
}
