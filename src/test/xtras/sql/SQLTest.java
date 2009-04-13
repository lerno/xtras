package xtras.sql;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

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
		SQL.closeSilently((Statement) Proxy.newProxyInstance(getClass().getClassLoader(),
		                                                     new Class[]{Statement.class},
		                                                     new InvocationHandler()
		                                                     {
			                                                     public Object invoke(Object proxy,
			                                                                          Method method,
			                                                                          Object[] args)
					                                                     throws Throwable
			                                                     {
				                                                     return null;
			                                                     }
		                                                     }));
		SQL.closeSilently((Statement) Proxy.newProxyInstance(getClass().getClassLoader(),
		                                                     new Class[]{Statement.class},
		                                                     new InvocationHandler()
		                                                     {
			                                                     public Object invoke(Object proxy,
			                                                                          Method method,
			                                                                          Object[] args)
					                                                     throws Throwable
			                                                     {
				                                                     throw new SQLException();
			                                                     }
		                                                     }));

		SQL.closeSilently((ResultSet) null);
		SQL.closeSilently((ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
		                                                     new Class[]{ResultSet.class},
		                                                     new InvocationHandler()
		                                                     {
			                                                     public Object invoke(Object proxy,
			                                                                          Method method,
			                                                                          Object[] args)
					                                                     throws Throwable
			                                                     {
				                                                     return null;
			                                                     }
		                                                     }));
		SQL.closeSilently((ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(),
		                                                     new Class[]{ResultSet.class},
		                                                     new InvocationHandler()
		                                                     {
			                                                     public Object invoke(Object proxy,
			                                                                          Method method,
			                                                                          Object[] args)
					                                                     throws Throwable
			                                                     {
				                                                     throw new SQLException();
			                                                     }
		                                                     }));

	}
}