package xtras.util.predicates;

import xtras.util.Predicate;

import java.util.Collection;

/**
 * An OR predicate, will return true if any of the enclosed predicates are true.
 *
 * @author Christoffer Lerno
 */
class OrPredicate<P> extends MultiPredicate<P>
{
	public OrPredicate(Collection<Predicate<P>> collection)
	{
		super(collection);
	}

	protected String getName()
	{
		return "OR";
	}

	/**
	 * Evaluate this predicate.
	 *
	 * @param object the object to evaluate.
	 * @return true if any of the enclosed predicates return true
	 */
	public boolean evaluate(P object)
	{
		for (Predicate<P> predicate : m_predicates)
		{
			if (predicate.evaluate(object))
			{
				return true;
			}
		}
		return false;
	}
}
