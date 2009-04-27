package xtras.io;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;


/**
 * This is a class that can produce well-formatted output to a stream or
 * a writer. Includes possibility to print pretty tables.
 *
 * @author Christoffer Lerno
 */
public class Output
{
	private final Out m_writer;
	private TableModel m_table;

	/**
	 * Creates an Output wrapping an OutputStream.
	 *
	 * @param stream the stream to use for output.
	 */
	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
	public Output(OutputStream stream)
	{
		this(new PrintStreamOut(stream instanceof PrintStream
		                        ? (PrintStream) stream
		                        : new PrintStream(stream)));
	}

	/**
	 * Creates an Output wrapping a Writer.
	 *
	 * @param writer the writer to use for output.
	 */
	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
	public Output(Writer writer)
	{
		this(new PrintWriterOut(writer instanceof PrintWriter
		                        ? (PrintWriter) writer
		                        : new PrintWriter(writer)));
	}

	/**
	 * Creates an Output around a custom implementation of Out.
	 *
	 * @param out the object to use for writing.
	 */
	public Output(Out out)
	{
		m_writer = out;
	}

	/**
	 * Print formatted text.
	 *
	 * @param s the string to print.
	 * @param args arguments to the formatted string.
	 */
	public void printf(String s, Object... args)
	{
		print(String.format(s, args));
	}

	/**
	 * Print text without formatting.
	 *
	 * @param s the text to print.
	 */
	public void print(String s)
	{
		m_writer.print(s);
	}

	/**
	 * Create a new table.
	 *
	 * @param title the title of the table.
	 */
	public void newTable(String title)
	{
		m_table = new TableModel(title);
		m_table.setMaxWidth(120);
	}

	/**
	 * Create a new table without a title.
	 */
	public void newTable()
	{
		newTable(null);
	}

	/**
	 * Sets the column names of a table.
	 *
	 * @param columns the column names.
	 */
	public void setColumns(String... columns)
	{
		assertTable();
		m_table.setColumns(columns);
	}

	/**
	 * Throw an illegal exception if a table is not yet created.
	 */
	private void assertTable()
	{
		if (m_table == null) throw new IllegalStateException("Attempted to edit table before it was created.");
	}

	/**
	 * Set the max with for all columns.
	 *
	 * @param maxWidth the max width of all headers.
	 */
	public void setMaxWidth(int maxWidth)
	{
		assertTable();
		m_table.setMaxWidth(maxWidth);
	}

	/**
	 * Prints a new line.
	 */
	public void println()
	{
		m_writer.println();
	}

	/**
	 * Prints a formatted string followed by a newline.
	 *
	 * @param string a formatted string to print.
	 * @param args the arguments to its string.
	 */
	public void printfln(String string, Object... args)
	{
		printf(string, args);
		println();
	}

	/**
	 * Prints a string followed by newline.
	 *
	 * @param string the string to print.
	 */
	public void println(String string)
	{
		print(string);
		println();
	}

	/**
	 * Will create a table where each row has an index.
	 * The first column will always be the header for the
	 * index column, which always is printed first.
	 *
	 * @param title the title of the table.
	 */
	public void newIndexedTable(String title)
	{
		m_table = new IndexedTableModel(title);
	}

	/**
	 * Will create a table without a title where each row has an index.
	 * The first column will always be the header for the
	 * index column, which always is printed first.
	 */
	public void newIndexedTable()
	{
		newIndexedTable(null);
	}

	/**
	 * Sorts the current table on the given column.
	 *
	 * @param column, starting with 1.
	 */
	public void sortTable(int column)
	{
		assertTable();
		m_table.sort(column);
	}


	/**
	 * Prints the current table.
	 */
	public void printTable()
	{
		assertTable();
		m_table.print(m_writer);
	}

	/**
	 * Get the number of rows in the current table.
	 * 
	 * @return the number of rows in the latest table created.
	 */
	public int getTableRows()
	{
		assertTable();
		return m_table.getRows();
	}

	/**
	 * Add a row to the current table.
	 *
	 * @param values the column values of the row.
	 */
	public void addRow(Object... values)
	{
		assertTable();
		m_table.addRow(values);
	}
}
