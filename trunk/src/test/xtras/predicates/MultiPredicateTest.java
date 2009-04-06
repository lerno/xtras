package xtras.predicates;
/**
 * @author Christoffer Lerno 
 */

import junit.framework.*;

import java.util.Collection;
import java.util.Arrays;

@SuppressWarnings("unchecked")
public class MultiPredicateTest extends TestCase
{
	MultiPredicate m_multiPredicate;

	private static class TestMultiArgumentPredicate extends MultiPredicate
	{

		public TestMultiArgumentPredicate(Collection collection)
		{
			super(collection);
		}

		protected String getName()
		{
			return "*mult*";
		}

		public boolean evaluate(Object object)
		{
			return false;
		}
	}

	protected void setUp() throws Exception
	{
		m_multiPredicate = new TestMultiArgumentPredicate(Arrays.asList(Predicates.truePredicate(),
		                                                                Predicates.falsePredicate()));
	}

	public void testToString() throws Exception
	{
		assertEquals("(TRUE *mult* FALSE)", m_multiPredicate.toString());
	}
}