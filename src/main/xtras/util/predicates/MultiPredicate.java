package xtras.util.predicates;

import xtras.util.Predicate;
import xtras.util.CollectionExtras;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

/**
 * A predicate using multiple arguments.
 *
 * @author Christoffer Lerno
 */
public abstract class MultiPredicate<P> implements Predicate<P>
{
	protected List<Predicate<P>> m_predicates;

	/**
	 * Constructor
	 * assert predicates!=null;
	 * assert !predicates.contains(null);
	 *
	 * @param predicates A collection of other predicates.
	 */
	public MultiPredicate(Collection<Predicate<P>> predicates)
	{
		assert predicates != null : "MultiPredicate constructor predicate collection was null";
		assert !predicates.contains(null) : "MultiPredicate constructor predicate collection contained a null";
		m_predicates = new ArrayList<Predicate<P>>(predicates);
	}

	protected abstract String getName();

	/**
	 * @return A String representation in the form "(Predicate1 NAME Predicate2 NAME ... PredicateN)"
	 */
	public String toString()
	{
		StringBuilder buffer = new StringBuilder(16);
		buffer.append('(').append(CollectionExtras.join(m_predicates, ' ' + getName() + ' ')).append(')');
		return buffer.toString();
	}

}