package xtras.util.predicates;

import xtras.util.Predicate;

/**
 * Evaluates to the reverse of the enclosed predicate.
 *
 * @author Christoffer Lerno
 */
class NotPredicate<P> implements Predicate<P>
{
	private final Predicate<P> m_predicate;

	public NotPredicate(Predicate<P> predicate)
	{
		m_predicate = predicate;
	}

	/**
	 * Evaluates to the reverse of the enclosed predicate.
	 *
	 * @param object the object to evaluate.
	 * @return true if the encolsed predicate returns false, false otherwise.
	 */
	public boolean evaluate(P object)
	{
		return !m_predicate.evaluate(object);
	}

	/** @return NOT [enclosed predicate] */
	public String toString()
	{
		return "NOT " + m_predicate;
	}

	Predicate<P> getWrappedPredicate()
	{
		return m_predicate;
	}
}
