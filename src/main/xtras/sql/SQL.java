package xtras.sql;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/**
 * A utility class with helper methods for working with database connections.
 *
 * @author Christoffer Lerno
 */
public final class SQL
{
	SQL()
	{
	}

	/**
	 * Reads a full row from a result set, returning a List or a plain object
	 * depending on the number of columns.
	 * <p/>
	 * For one single column, this method returns the result of {@code resultSet.getObject(1)},
	 * For 0 or 2+ columns, a list is created and the result of {@code resultSet.getObject(i)},
	 * is put in the list at index i - 1.
	 * <p/>
	 * For example, the result of "select key from someTable" will return a
	 * single object which is the value for the 'key' column.
	 * The result of "select key, user, pasword from someTable" on the other hand,
	 * will return a {@link java.util.List} with the values of the columns 'key',
	 * 'user' and 'password' in that order.
	 *  
	 * @param resultSet the result set to be read.
	 * @return the value of the current row in the result set.
	 * @throws SQLException if there was an error reading from the ResultSet.
	 */
	public static Object readResultSet(ResultSet resultSet) throws SQLException
	{
		int columns = resultSet.getMetaData().getColumnCount();
		if (columns == 1)
		{
			return resultSet.getObject(1);
		}
		List<Object> list = new ArrayList<Object>(columns);
		for (int i = 0; i < columns; i++)
		{
			list.add(resultSet.getObject(i + 1));
		}
		return list;
	}

	/**
	 * Closes a statement, ignoring any exceptions.
	 *
	 * @param statement the statement to close, the statement may be {@code null}.
	 */
	public static void closeSilently(Statement statement)
	{
		if (statement != null)
		{
			try
			{
				statement.close();
			}
			catch (SQLException ignore)
			{
			}
		}
	}

	/**
	 * Closes a connection, ignoring any exceptions.
	 *
	 * @param connection the connection to close, the connection may be {@code null}.
	 */
	public static void closeSilently(Connection connection)
	{
		if (connection != null)
		{
			try
			{
				connection.close();
			}
			catch (SQLException ignore)
			{
			}
		}
	}
	
	/**
	 * Closes a result set, ignoring any exceptions.
	 *
	 * @param resultSet the result set to close, the value may be {@code null}.
	 */
	public static void closeSilently(ResultSet resultSet)
	{
		if (resultSet != null)
		{
			try
			{
				resultSet.close();
			}
			catch (SQLException ignore)
			{
			}
		}
	}

}
