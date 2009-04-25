package xtras.sql;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

/** @author Christoffer Lerno */
public final class SQL
{
	SQL()
	{
	}

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

	public static void closeSilently(Statement s)
	{
		if (s != null)
		{
			try
			{
				s.close();
			}
			catch (SQLException ignore)
			{
			}
		}
	}

	public static void closeSilently(Connection c)
	{
		if (c != null)
		{
			try
			{
				c.close();
			}
			catch (SQLException ignore)
			{
			}
		}
	}
	
	public static void closeSilently(ResultSet rs)
	{
		if (rs != null)
		{
			try
			{
				rs.close();
			}
			catch (SQLException ignore)
			{
			}
		}
	}


}
