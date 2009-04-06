package xtras;

/** @author Christoffer Lerno */
public interface Predicate<C>
{
	/**
	 * @param object the object to evaluate.
	 * @return true iff value evaluates to true.
	 */
	boolean evaluate(C object);
}
