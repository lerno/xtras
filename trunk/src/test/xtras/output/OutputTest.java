package xtras.output;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;
import xtras.output.Output;
import xtras.StringExtras;

import java.io.ByteArrayOutputStream;
import java.io.Writer;
import java.io.StringWriter;

@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
public class OutputTest extends TestCase
{
	private final static String NEWLINE = StringExtras.LINE_SEPARATOR;

	Output m_output;

	public void testOutput() throws Exception
	{
		ByteArrayOutputStream writer = new ByteArrayOutputStream();
		m_output = new Output(writer);
		m_output.printf("This row %s printf style %s%n", "has", "formatting");
		m_output.println();
		m_output.printfln("Printf with %s", "linebreak");
		m_output.print("Print");
		m_output.println(" without format.");
		m_output.newTable("Title");
		m_output.addRow(1, 4);
		m_output.setColumns("A", "B");
		m_output.addRow(3, 2);
		m_output.sortTable(2);
		m_output.printTable();
		m_output.setMaxWidth(30);
		assertEquals(2, m_output.getTableRows());
		m_output.newIndexedTable();
		m_output.setColumns("I", "C", "D");
		m_output.addRow(1, 4);
		m_output.addRow(3, 2);
		m_output.sortTable(2);
		m_output.printTable();
		m_output.newTable();
		m_output.addRow("Column");
		m_output.addRow("without");
		m_output.addRow("heading.");
		m_output.printTable();
		assertEquals("This row has printf style formatting" + NEWLINE +
		             NEWLINE +
		             "Printf with linebreak" + NEWLINE +
		             "Print without format." + NEWLINE +
		             "-- Title --" + NEWLINE +
		             " A   B     " + NEWLINE +
		             "-----------" + NEWLINE +
		             " 3   2     " + NEWLINE +
		             " 1   4     " + NEWLINE +
		             "-----------" + NEWLINE +
			         " I   C   D " + NEWLINE +
		             "-----------" + NEWLINE +
		             " 0   3   2 " + NEWLINE +
		             " 1   1   4 " + NEWLINE +
		             "-----------" + NEWLINE +
		             "----------" + NEWLINE +
		             " Column   " + NEWLINE +
		             " without  " + NEWLINE +
		             " heading. " + NEWLINE +
		             "----------" + NEWLINE,
		             writer.toString());
		StringWriter w = new StringWriter();
		Output output2 = new Output(w);
		output2.println("Testing output");
		assertEquals("Testing output" + NEWLINE, w.toString());
	}
}