package xtras.io;

import xtras.lang.StringExtras;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * This class is for producing a well-formatted table printout.
 *
 * @author Christoffer Lerno
 * @author Per Huss (original idea & implementation)
 */
class TableModel
{
	protected String[] m_columnNames;
	private int[] m_columnWidths;
	protected final List<Object[]> m_rows;
	private final String m_title;
	private int m_maxWidth;

	/**
	 * Creates a new table with the given title.
	 *
	 * @param title the title or null if no title is needed.
	 */
	public TableModel(String title)
	{
		m_title = title;
		m_rows = new ArrayList<Object[]>();
		m_maxWidth = Integer.MAX_VALUE;
	}

	/**
	 * Sets the column names, needs to be done before adding rows to the table.
	 *
	 * @param columnNames the names of the columns.
	 * @throws IllegalArgumentException if the number of columns does not match previous rows.
	 */
	public void setColumns(String... columnNames)
	{
		adjustColumnWidth(columnNames);
		m_columnNames = columnNames;
	}

	/**
	 * Adjust column widths to fit the new columns.
	 *
	 * @param columns the columns to add.
	 * @throws IllegalArgumentException if the number of columns in the row does not match previous rows.
	 */
	private void adjustColumnWidth(Object[] columns)
	{
		if (m_columnWidths == null)
		{
			m_columnWidths = new int[columns.length];
		}
		if (m_columnWidths.length != columns.length)
		{
			throw new IllegalArgumentException("Wrong number of columns in row.");
		}
		for (int i = 0; i < columns.length; i++)
		{
			m_columnWidths[i] =  Math.max(m_columnWidths[i],
			                              Math.min(m_maxWidth, String.valueOf(columns[i]).length()));
		}
	}

	/**
	 * Adds another row to the table.
	 *
	 * @param columns values of the columns.
	 * @throws IllegalArgumentException if the number of columns in the row does not match previous rows.
	 */
	public void addRow(Object... columns)
	{
		adjustColumnWidth(columns);
		m_rows.add(columns);
	}

	/**
	 *
	 * @param i Column number (1..number of cols)
	 */
	public void sort(int i)
	{
		if (i < 1 || i > m_columnWidths.length)
		{
			throw new IllegalArgumentException("Invalid column number.");
		}
		Collections.sort(m_rows, new LineComparator(i - 1));
	}

	public void print(Out stream)
	{
		if (m_title != null && m_columnWidths.length > 0)
		{
			m_columnWidths[m_columnWidths.length - 1] += Math.max(0, m_title.length() + 6  - getTotalWidth());
		}
		printTitle(stream);
		printHeader(stream);
		printSeparator(stream);
		for (Object[] column : m_rows)
		{
			printRow(column, stream);
		}
		printSeparator(stream);
	}

	private void printTitle(Out stream)
	{
		if (m_title != null)
		{
			int width = getTotalWidth() - m_title.length() - 2;
			StringBuilder buf = new StringBuilder();
			StringExtras.append(buf, '-', width / 2);
			stream.print(buf.toString());
			stream.print(" ");
			stream.print(m_title);
			stream.print(" ");
			if (width % 2 == 1)
			{
				stream.print("-");
			}
			println(stream, buf);
		}
	}

	private void printRow(Object[] column, Out stream)
	{
		String[] columns = new String[column.length];
		int maxLines = 1;
		for (int i = 0; i < column.length; i++)
		{
			columns[i] = String.valueOf(column[i]);
			maxLines = Math.max(maxLines, StringExtras.splitOnLength(columns[i], m_maxWidth).size());
		}
		List<StringBuilder> lines = new ArrayList<StringBuilder>(maxLines);
		for (int i = 0; i < maxLines; i++)
		{
			lines.add(new StringBuilder());
		}
		for(int i = 0; i < column.length; i++)
		{
			for (int line = 0; line < maxLines; line++)
			{
				StringBuilder builder = lines.get(line);
				if (i != 0)
				{
					builder.append(" ");
				}
				builder.append(" ");
				List<String> splitColumn = StringExtras.splitOnLength(columns[i], m_maxWidth);
				String value = splitColumn.size() <= line ? "" : splitColumn.get(line);
				builder.append(StringExtras.padRight(value, m_columnWidths[i] + 1));
			}
		}
		for (StringBuilder line : lines)
		{
			println(stream, line);
		}
	}

	private void println(Out stream, Object o)
	{
		stream.print(String.valueOf(o));
		stream.println();
	}

	private void printSeparator(Out stream)
	{
		StringBuilder buf = new StringBuilder();
		StringExtras.append(buf, '-', getTotalWidth());
		println(stream, buf);
	}

	private void printHeader(Out stream)
	{
		if (m_columnNames == null) return;
		for (int i = 0; i < m_columnNames.length; i++)
		{
			stream.print(" ");
			if (i > 0)
			{
				stream.print(" ");
			}
			stream.print(StringExtras.padRight(m_columnNames[i], m_columnWidths[i] + 1));
		}
		stream.println();
	}

	private int getTotalWidth()
	{
		int width = 0;
		for (int columnWidth : m_columnWidths)
		{
			width += columnWidth + 3;
		}
		return width - 1;
	}

	public int getRows()
	{
		return m_rows.size();
	}

	public void setMaxWidth(int maxWidth)
	{
		m_maxWidth = maxWidth;
	}

	private final static class LineComparator implements Comparator<Object[]>
	{
		private final int m_column;

		LineComparator(int column)
		{
			m_column = column;
		}

		@SuppressWarnings({"ObjectEquality"})
		public int compare(Object[] row1, Object[] row2)
		{
			Object o1 = row1[m_column];
			Object o2 = row2[m_column];

			if (o1 == o2)
			{
				return 0;
			}
			if (o1 == null)
			{
				return -1;
			}
			if (o2 == null)
			{
				return 1;
			}
			if (o1.getClass() == o2.getClass() && o1 instanceof Comparable)
			{
				//noinspection unchecked
				return ((Comparable<Object>) o1).compareTo(o2);
			}
			else
			{
				return String.valueOf(o1).compareTo(String.valueOf(o2));
			}
		}
	}
}
