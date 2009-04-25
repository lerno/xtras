package xtras.io;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.net.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import xtras.lang.StringExtras;

public class IOTest extends TestCase
{
	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmptyTest()
	{
		new IO();
	}

	@Override
	protected void tearDown() throws Exception
	{
		IO.setNetworkInterfaceProvider(null);
	}

	@SuppressWarnings({"SocketOpenedButNotSafelyClosed"})
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


	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
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


	public void testIsHostReachableFailed() throws Exception
	{
		DynamicSocketImplFactory.setNextSocket(new SocketImplAdaptor()
		{
			protected void connect(SocketAddress anAddress, int timeout) throws IOException
			{
				throw new IOException("Meh");
			}
		});
		assertFalse(IO.isHostReachable("127.0.0.1", 9999));
	}

	public void testIsHostReachable() throws Exception
	{
		DynamicSocketImplFactory.setNextSocket(new SocketImplAdaptor());
		assertTrue(IO.isHostReachable("127.0.0.1", 9998));
	}

	public void testGetBestIpGuessLocalhost() throws Exception
	{

		Map<String, InetAddress> interfaces = new HashMap<String, InetAddress>();
		interfaces.put("1", InetAddress.getByName("127.0.1.1"));
		assertEquals(InetAddress.getLocalHost(), IO.getBestIpGuess(interfaces));
		InetAddress linkLocal1 = InetAddress.getByName("169.254.1.1");
		InetAddress linkLocal2 = InetAddress.getByName("169.254.1.2");
		interfaces.put("eee0", linkLocal1);
		interfaces.put("ddd0", linkLocal2);

		// Selects in alphabetical order of interface names, so expect ddd0, i.e. linkLocal2
		assertEquals(linkLocal2, IO.getBestIpGuess(interfaces));

		InetAddress siteLocal1 = InetAddress.getByName("10.7.1.2");
		interfaces.put("d1", siteLocal1);

		// Will prefer site local, so expect d1, i.e. siteLocal1
		assertEquals(siteLocal1, IO.getBestIpGuess(interfaces));

		InetAddress siteLocal2 = InetAddress.getByName("10.7.45.2");
		InetAddress siteLocal3 = InetAddress.getByName("10.8.12.2");
		interfaces.put("e1", siteLocal2);
		interfaces.put("e3", siteLocal3);

		// Having a choice between interfaces starting with e, and starting with some other
		// letter, choose the first in string order that starts with e.
		assertEquals(siteLocal2, IO.getBestIpGuess(interfaces));

		InetAddress regular1 = InetAddress.getByName("218.1.1.1");
		interfaces.put("az", regular1);
		// Giving a non-site local will have preference over other ips.
		assertEquals(regular1, IO.getBestIpGuess(interfaces));

		InetAddress regular2 = InetAddress.getByName("231.21.21.1");
		interfaces.put("eth1000", regular2);
		// Again, e-prefix will have precedence.
		assertEquals(regular2, IO.getBestIpGuess(interfaces));

	}

	public void testGetIp() throws Exception
	{
		IO.getIP();
		IO.setNetworkInterfaceProvider(new NetworkInterfaceProvider()
		{
			public Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException
			{
				return null;
			}
		});
		assertEquals(IO.getIP(), InetAddress.getLocalHost().getHostAddress());
		IO.setNetworkInterfaceProvider(new NetworkInterfaceProvider()
		{
			public Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException
			{
				throw new SocketException();
			}
		});
		assertEquals(IO.getIP(), "127.0.0.1");
	}

	/** @author Christoffer Lerno */
	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
	public static class TableModelTest extends TestCase
	{
		private final static String NEWLINE = StringExtras.LINE_SEPARATOR;

		public void testPrinting()
		{
			TableModel model = new TableModel("Some Random");
			model.setColumns("Id", "Name", "Value");
			try
			{
				model.addRow(1221, "Zero");
				fail();
			}
			catch (IllegalArgumentException e)
			{
				assertEquals("Wrong number of columns in row.", e.getMessage());
			}
			model.addRow(null, "Minus", -2);
			model.addRow(4711, "One", 8);
			model.addRow(11, "Two", 7);
			model.addRow(52, "Three", 9);
			model.addRow(null, "Four", 0);
			model.addRow(null, "Zero", -1);
			try
			{
				model.sort(4);
				fail();
			}
			catch (IllegalArgumentException e)
			{
			}
			model.sort(1);
			ByteArrayOutputStream writer = new ByteArrayOutputStream();

			model.print(new PrintStreamOut(new PrintStream(writer)));

			assertEquals("---- Some Random -----" + NEWLINE +
			             " Id     Name    Value " + NEWLINE +
			             "----------------------" + NEWLINE +
			             " null   Minus   -2    " + NEWLINE +
			             " null   Four    0     " + NEWLINE +
			             " null   Zero    -1    " + NEWLINE +
			             " 11     Two     7     " + NEWLINE +
			             " 52     Three   9     " + NEWLINE +
			             " 4711   One     8     " + NEWLINE +
			             "----------------------" + NEWLINE,
			             writer.toString());
		}

		public void testPrintingIndexedTableModel()
		{
			IndexedTableModel model = new IndexedTableModel(null);
			model.setColumns("Id", "Num", "Name", "Value");
			model.addRow(1, "One", 8);
			model.addRow(2, "Two", 7);
			model.addRow(3, "Three", 9);
			model.sort(3);
			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			model.print(new PrintStreamOut(new PrintStream(writer)));

			assertEquals(" Id   Num   Name    Value " + NEWLINE +
			             "--------------------------" + NEWLINE +
			             " 0    2     Two     7     " + NEWLINE +
			             " 1    1     One     8     " + NEWLINE +
			             " 2    3     Three   9     " + NEWLINE +
			             "--------------------------" + NEWLINE,
			             writer.toString());
		}

		public void testSortingOddObjects()
		{
			Object a = new Object()
			{
				@Override
				public String toString()
				{
					return "a";
				}
			};
			Object b = new Object()
			{
				@Override
				public String toString()
				{
					return "b";
				}
			};

			TableModel model = new TableModel(null);
			model.setColumns("Id");
			model.addRow(b);
			model.addRow(a);
			model.sort(1);
			ByteArrayOutputStream writer = new ByteArrayOutputStream();
			model.print(new PrintStreamOut(new PrintStream(writer)));

			assertEquals(" Id " + NEWLINE +
			             "----" + NEWLINE +
			             " a  " + NEWLINE +
			             " b  " + NEWLINE +
			             "----" + NEWLINE,
			             writer.toString());
		}

		public void testPrintingWithMaxWidth()
		{
			TableModel model = new TableModel("Some Random");
			model.setColumns("Id", "Name", "Value");
			model.setMaxWidth(4);
			model.addRow(4711, "One", 8);
			model.addRow(11, "Two", 7);
			model.addRow(52, "Three", 99999);
			assertEquals(3, model.getRows());
			ByteArrayOutputStream writer = new ByteArrayOutputStream();

			model.print(new PrintStreamOut(new PrintStream(writer)));

			assertEquals("---- Some Random ----" + NEWLINE +
			             " Id     Name   Value " + NEWLINE +
			             "---------------------" + NEWLINE +
			             " 4711   One    8     " + NEWLINE +
			             " 11     Two    7     " + NEWLINE +
			             " 52     Thre   9999  " + NEWLINE +
			             "        e      9     " + NEWLINE +
			             "---------------------" + NEWLINE,
			             writer.toString());
		}
	}
}