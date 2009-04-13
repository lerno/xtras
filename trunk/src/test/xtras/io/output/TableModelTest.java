package xtras.io.output;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import xtras.lang.StringExtras;
import xtras.io.output.IndexedTableModel;
import xtras.io.output.PrintStreamOut;
import xtras.io.output.TableModel;

/**
 * @author Christoffer Lerno
 */
@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
public class TableModelTest extends TestCase
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
