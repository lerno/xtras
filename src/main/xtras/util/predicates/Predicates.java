package xtras.util.predicates;

import xtras.util.Predicate;

import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;

/**
 * Helper methods for predicates.
 *
 * @author Christoffer Lerno
 */
public final class Predicates
{
	Predicates() {}

	/**
	 * A predicate that evaluates to true if the object is not null.
	 */
	public final static Predicate NOT_NULL = new Predicate()
	{

		public boolean evaluate(Object object)
		{
			return object != null;
		}

		public String toString()
		{
			return "NOT NULL";
		}

	};

	/**
	 * A predicate that always evaluates to true.
	 */
	public final static Predicate TRUE = new Predicate()
	{
		public boolean evaluate(Object object)
		{
			return true;
		}

		public String toString()
		{
			return "TRUE";
		}

	};

	/**
	 * A predicate that always evaluates to false.
	 */
	public final static Predicate FALSE = new Predicate()
	{
		public boolean evaluate(Object object)
		{
			return false;
		}

		public String toString()
		{
			return "FALSE";
		}
	};

	/**
	 * Returns a predicate that evaluates to true for all non-null objects.
	 * @return a predicate that evaluates to true for non-null.
	 */
	@SuppressWarnings({"unchecked"})
	public static <P> Predicate<P> notNullPredicate()
	{
		return NOT_NULL;
	}

	/**
	 * Returns a predicate that always evaluates to true.
	 * @return a predicate that evaluates to true.
	 */
	@SuppressWarnings({"unchecked"})
	public static <P> Predicate<P> truePredicate()
	{
		return TRUE;
	}

	/**
	 * Returns a predicate that always evaluates to false.
	 *
	 * @return a predicate that evaluates to false.
	 */
	@SuppressWarnings({"unchecked"})
	public static <P> Predicate<P> falsePredicate()
	{
		return FALSE;
	}

	/**
	 * Cause predicates to be "and"ed together.
	 * <p/>
	 * For 0 predicates, the predicate defaults to true.
	 * <p/>
	 * For 1 predicate, the same predicate is returned.
	 * <p/>
	 * For 2+ predicates, a predicate is created that is the AND of the contained predicates.
	 *
	 * @param predicates the collection of predicates to join.
	 * @return the resulting predicate.
	 */
	public static <C> Predicate<C> and(Collection<Predicate<C>> predicates)
	{
		if (predicates.isEmpty())
		{
			return truePredicate();
		}
		if (predicates.size() == 1)
		{
			return predicates.iterator().next();
		}
		if (predicates.size() == 2)
		{
			Iterator<Predicate<C>> it = predicates.iterator();
			return new SimpleAndPredicate<C>(it.next(), it.next());
		}
		return new AndPredicate<C>(predicates);
	}

	/**
	 * Cause predicates to be "and"ed together.
	 * <p/>
	 * For 0 predicates, the predicate defaults to true.
	 * <p/>
	 * For 1 predicate, the same predicate is returned.
	 * <p/>
	 * For 2+ predicates, a predicate is created that is the AND of the contained predicates.
	 *
	 * @param predicates the collection of predicates to join.
	 * @return the resulting predicate.
	 */
	public static <C> Predicate<C> and(Predicate<C>... predicates)
	{
		return and(Arrays.asList(predicates));
	}

	/**
	 * Cause predicates to be "or"ed together.
	 * <p/>
	 * For 0 predicates, the predicate defaults to true.
	 * <p/>
	 * For 1 predicate, the same predicate is returned.
	 * <p/>
	 * For 2+ predicates, a predicate is created that is the OR of the contained predicates.
	 *
	 * @param predicates the collection of predicates to join.
	 * @return the resulting predicate.
	 */
	public static <C> Predicate<C> or(Collection<Predicate<C>> predicates)
	{
		if (predicates.isEmpty())
		{
			return truePredicate();
		}
		if (predicates.size() == 1)
		{
			return predicates.iterator().next();
		}
		if (predicates.size() == 2)
		{
			Iterator<Predicate<C>> it = predicates.iterator();
			return new SimpleOrPredicate<C>(it.next(), it.next());
		}
		return new OrPredicate<C>(predicates);
	}

	/**
	 * Cause predicates to be "or"ed together.
	 * <p/>
	 * For 0 predicates, the predicate defaults to true.
	 * <p/>
	 * For 1 predicate, the same predicate is returned.
	 * <p/>
	 * For 2+ predicates, a predicate is created that is the OR of the contained predicates.
	 *
	 * @param predicates the collection of predicates to join.
	 * @return the resulting predicate.
	 */
	public static <C> Predicate<C> or(Predicate<C>... predicates)
	{
		return or(Arrays.asList(predicates));
	}

	/**
	 * Returns an inverse of the predicate entered.
	 *
	 * @param predicate the predicate to invert.
	 * @return the resulting predicate.
	 */
	public static <C> Predicate<C> not(Predicate<C> predicate)
	{
		if (predicate instanceof NotPredicate)
		{
			return ((NotPredicate<C>) predicate).getWrappedPredicate();
		}
		return new NotPredicate<C>(predicate);
	}

	/** An optimized AND predicate for 2 predicates. */
	private static class SimpleAndPredicate<C> implements Predicate<C>
	{
		private final Predicate<C> m_predicate1;
		private final Predicate<C> m_predicate2;

		public SimpleAndPredicate(Predicate<C> predicate1, Predicate<C> predicate2)
		{
			m_predicate1 = predicate1;
			m_predicate2 = predicate2;
		}

		public boolean evaluate(C object)
		{
			return m_predicate1.evaluate(object) && m_predicate2.evaluate(object);
		}

		public String toString()
		{
			return String.format("(%s && %s)", m_predicate1, m_predicate2);
		}
	}

	/** An optimized OR predicate for 2 predicates. */
	private static class SimpleOrPredicate<C> implements Predicate<C>
	{
		private final Predicate<C> m_predicate1;
		private final Predicate<C> m_predicate2;

		public SimpleOrPredicate(Predicate<C> predicate1, Predicate<C> predicate2)
		{
			m_predicate1 = predicate1;
			m_predicate2 = predicate2;
		}

		public boolean evaluate(C object)
		{
			return m_predicate1.evaluate(object) || m_predicate2.evaluate(object);
		}

		public String toString()
		{
			return String.format("(%s || %s)", m_predicate1, m_predicate2);
		}
	}

}