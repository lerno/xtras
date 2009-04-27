package xtras.lang;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/** @author Christoffer Lerno */
public final class ObjectExtras
{
	protected ObjectExtras()
	{}

	/**
	 * Test equals for two (possibly null) objects.
	 *
	 * @param a object a, may be null.
	 * @param b object b, may be null.
	 * @return true if a == b or a.equals(b) or both a and b is null.
	 */
	@SuppressWarnings({"ObjectEquality"})
	public static boolean equals(Object a, Object b)
	{
		return a == b || (a != null && a.equals(b));
	}

	/**
	 * Creates a proxy to allow an object to partially adapt an interface.
	 * The remaining methods will return null (when returning objects)
	 * -1 for primitive numbers and false for primitive booleans.
	 *
	 * @param anInterface the interface to adapt.
	 * @param o the object that partially implements the interface.
	 * @return a proxy that "completely" implements the interface.
	 */
	public static <T> T adapt(Class<T> anInterface, Object o)
	{
		return (T) Proxy.newProxyInstance(anInterface.getClassLoader(),
		                                  new Class[]{anInterface},
		                                  new AdapterInvocationHandler(o));
	}

	/**
	 * Creates a proxy around a class, to allow another object to
	 * override parts of its functionality.
	 *
	 * @param toOverride the object to override.
	 * @param overridingObject the object that has methods that overrides the particular object.
	 * @return a proxy that overrides calls on the first object using implementation on the overriding object.
	 */
	public static <T> T override(T toOverride, Object overridingObject)
	{
		return (T) Proxy.newProxyInstance(toOverride.getClass().getClassLoader(),
		                                  toOverride.getClass().getInterfaces(),
		                                  new OverridingInvocationHandler(toOverride, overridingObject));
	}

	private static class AdapterInvocationHandler implements InvocationHandler
	{
		private final Object m_wrappedObject;

		public AdapterInvocationHandler(Object o)
		{
			m_wrappedObject = o;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			try
			{
				Method m = m_wrappedObject.getClass().getMethod(method.getName(), method.getParameterTypes());
				m.setAccessible(true);
				try
				{
					return m.invoke(m_wrappedObject, args);
				}
				catch (InvocationTargetException e)
				{
					throw e.getTargetException();
				}
			}
			catch (NoSuchMethodException e)
			{
				Class returnType = method.getReturnType();
				if (!returnType.isPrimitive())
				{
					return null;
				}
				return "boolean".equals(returnType.getName()) ? false : -1;
			}
		}
	}

	private static class OverridingInvocationHandler implements InvocationHandler
	{
		private final Object m_original;
		private final Object m_override;

		public OverridingInvocationHandler(Object original, Object override)
		{
			m_original = original;
			m_override = override;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
		{
			try
			{
				Method m = m_override.getClass().getMethod(method.getName(), method.getParameterTypes());
				if (m.getDeclaringClass().equals(Object.class))
				{
					throw new NoSuchMethodException();
				}
				m.setAccessible(true);
				return m.invoke(m_override, args);
			}
			catch (InvocationTargetException e)
			{
				throw e.getTargetException();
			}
			catch (NoSuchMethodException e)
			{
				try
				{
					return method.invoke(m_original, args);
				}
				catch (InvocationTargetException e1)
				{
					throw e1.getTargetException();
				}
			}
		}
	}

}
