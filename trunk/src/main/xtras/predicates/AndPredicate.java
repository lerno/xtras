package xtras.predicates;

import xtras.Predicate;

import java.util.Collection;

/**
 * Predicate to evaluate true iff all enclosed predicates evaluate to true.
 *
 * @author Christoffer Lerno
 */
class AndPredicate<P> extends MultiPredicate<P>
{
	public AndPredicate(Collection<Predicate<P>> predicates)
	{
		super(predicates);
	}

	protected String getName()
	{
		return "AND";
	}

	/**
	 * Evaluate this predicate.
	 *
	 * @param object the object to evaluate.
	 * @return true if all of the enclosed predicates return true.
	 */
	public boolean evaluate(P object)
	{
		for (Predicate<P> predicate : m_predicates)
		{
			if (!predicate.evaluate(object))
			{
				return false;
			}
		}
		return true;
	}
}