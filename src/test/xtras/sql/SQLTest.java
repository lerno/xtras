package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;

import xtras.lang.ObjectExtras;

public class SQLTest extends TestCase
{
	@SuppressWarnings({"InstantiationOfUtilityClass"})
	public void testEmptyTest()
	{
		new SQL();
	}

	public void testCloseSilently() throws Exception
	{
		SQL.closeSilently((Statement) null);
		SQL.closeSilently(ObjectExtras.adapt(Statement.class, new Object()));
		SQL.closeSilently(ObjectExtras.adapt(Statement.class, new Object()
		{
			public void close() throws SQLException
			{
				throw new SQLException();
			}
		}));
		SQL.closeSilently(ObjectExtras.adapt(ResultSet.class, new Object()));
		SQL.closeSilently(ObjectExtras.adapt(ResultSet.class, new Object()
		{
			public void close() throws SQLException
			{
				throw new SQLException();
			}
		}));
		SQL.closeSilently(ObjectExtras.adapt(Connection.class, new Object()));
		SQL.closeSilently(ObjectExtras.adapt(Connection.class, new Object()
		{
			public void close() throws SQLException
			{
				throw new SQLException();
			}
		}));

	}

}