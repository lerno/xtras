package xtras.sql;

import java.sql.Statement;
import java.sql.SQLException;
import java.sql.ResultSet;

/** @author Christoffer Lerno */
public final class SQL
{
	SQL()
	{
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
