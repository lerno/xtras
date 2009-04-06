package xtras;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;
import xtras.IO;

import java.net.Socket;
import java.io.Closeable;
import java.io.IOException;
import java.io.File;

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
	}

	public void testStore() throws Exception
	{
		File file = File.createTempFile("wohoo", "xxxxxx");
		file.deleteOnExit();
		IO.store(file, "Some text");
		assertEquals("Some text", IO.read(file));
	}
}