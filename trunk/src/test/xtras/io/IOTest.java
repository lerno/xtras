package xtras.io;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.Closeable;
import java.io.IOException;
import java.io.File;
import java.io.PrintStream;

import xtras.io.IO;

public class IOTest extends TestCase
{
	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmptyTest()
	{
		new IO();
	}

	public void testCloseSilently() throws Exception
	{
		IO.closeSilently((Socket) null);
		IO.closeSilently((Closeable) null);
		IO.closeSilently((ServerSocket) null);
		IO.closeSilently(new Closeable()
		{
			public void close() throws IOException
			{
				throw new IOException();
			}
		});
		IO.closeSilently(new Socket());
		IO.closeSilently(new Socket()
		{
			@Override
			public void close() throws IOException
			{
				throw new IOException();
			}
		});
		IO.closeSilently(new ServerSocket());
		IO.closeSilently(new ServerSocket()
		{
			@Override
			public void close() throws IOException
			{
				throw new IOException();
			}
		});
	}


	public void testAppend() throws Exception
	{
		File testFile = File.createTempFile("TestTest", ".tmp");
		try
		{
			IO.append(testFile, "Bob");
			IO.append(testFile, "ba Fett");
			assertEquals("Bobba Fett", IO.read(testFile));
		}
		finally
		{
			testFile.delete();
		}
	}

	public void testStore() throws Exception
	{
		File file = File.createTempFile("wohoo", "xxxxxx");
		file.deleteOnExit();
		IO.store(file, "Some text");
		assertEquals("Some text", IO.read(file));
	}


	public void testReadAsList() throws Exception
	{
		File testFile = File.createTempFile("TestTest", ".tmp");
		try
		{
			PrintStream stream = new PrintStream(testFile);
			stream.println();
			stream.println("1 2 3");
			stream.println("");
			stream.println(" ");
			stream.println("455");
			stream.close();
			assertEquals("[1 2 3,  , 455]", IO.readAsList(testFile).toString());
		}
		finally
		{
			testFile.delete();
		}
	}

	public void testReadAsListEmpty() throws Exception
	{
		File testFile = File.createTempFile("TestTest", ".tmp");
		try
		{
			assertEquals(0, IO.readAsList(testFile).size());
		}
		finally
		{
			testFile.delete();
		}
	}
	
	public void testGetIP() throws Exception
	{
		ServerSocket socket = null;
		try
		{
			socket = new ServerSocket(19999);
			assertEquals(true, IO.isHostReachable(IO.getIP(), 19999));
			assertEquals(false, IO.isHostReachable(IO.getIP(), 19998));
		}
		finally
		{
			IO.closeSilently(socket);
		}
	}
}