package xtras.io.output;

import xtras.util.ArrayExtras;

/**
 * Indexed subclass of TableModel
 *
 * @author Christoffer Lerno
 * @author Tommy Safstrom (original idea of TableModel subclass)
 */
class IndexedTableModel extends TableModel
{

	/**
	 * Will create a table where each row has an index.
	 * The first input ot the method is the header for the
	 * index column, which always is printed first.
	 *
	 * @param title the title of the table or null for no title.
	 */
	public IndexedTableModel(String title)
	{
		super(title);
	}

	@Override
	public void addRow(Object... columns)
	{
		super.addRow(ArrayExtras.prepend(columns, getRows()));
	}

	/**
	 * Sorts the current table on the given column
	 * and updates the indexes aswell.
	 *
	 * @param column, starting with 1.
	 */
	public void sort(int column)
	{
		// Sort the rows.
		super.sort(column + 1);

		// Reset the indexes.
		Integer index = 0;
		for (Object[] row : m_rows)
		{
			row[0] = index++;
		}
	}
}
