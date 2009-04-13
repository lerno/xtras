package xtras.io;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.*;

import xtras.lang.StringExtras;

public class IOTest extends TestCase
{
	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmptyTest()
	{
		new IO();
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


	@SuppressWarnings({"SocketOpenedButNotSafelyClosed"})
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

	/**
 * @author Christoffer Lerno
	 */
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