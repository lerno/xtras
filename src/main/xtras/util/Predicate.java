package xtras.util;

/**
 * Predicates are used when for selecting objects.
 *
 * @author Christoffer Lerno
 */
public interface Predicate<C>
{
	/**
	 * Evaluates the object according to the predicate.
	 * 
	 * @param object the object to evaluate.
	 * @return true iff value evaluates to true.
	 */
	boolean evaluate(C object);
}
