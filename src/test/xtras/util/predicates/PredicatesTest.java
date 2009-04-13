package xtras.util.predicates;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.Arrays;

import xtras.util.Predicate;

/**
 * @author Christoffer Lerno
 */

@SuppressWarnings("unchecked")
public class PredicatesTest extends TestCase
{
	@SuppressWarnings({"InstantiationOfUtilityClass"})
	Predicates m_predicates = new Predicates();

	public void testAnd() throws Exception
	{
		assertEquals(Predicates.TRUE, Predicates.and());
		assertEquals(Predicates.TRUE, Predicates.and(Collections.EMPTY_LIST));
	}

	public void testSinglePredicate() throws Exception
	{
		Predicate test = new Predicate()
		{
			public boolean evaluate(Object object)
			{
				return false;
			}
		};
		assertEquals(test, Predicates.and(test));
		assertEquals(test, Predicates.or(test));
	}

	public void testEvaluateAndAllTrue() throws Exception
	{
		assertEquals("(TRUE AND FALSE)", new AndPredicate(Arrays.asList(Predicates.TRUE, Predicates.FALSE)).toString());
		assertEquals("(TRUE && FALSE)", Predicates.and(Predicates.TRUE, Predicates.FALSE).toString());
		assertTrue(Predicates.and(Predicates.truePredicate(), Predicates.truePredicate()).evaluate(new Object()));
		assertTrue(Predicates.and(Predicates.truePredicate(),
		                          Predicates.truePredicate(),
		                          Predicates.truePredicate()).evaluate(new Object()));
	}

	public void testEvaluateAndFalseTrue() throws Exception
	{
		assertFalse(Predicates.and(Predicates.truePredicate(), Predicates.falsePredicate()).evaluate(new Object()));
		assertFalse(Predicates.and(Predicates.truePredicate(),
		                           Predicates.truePredicate(),
		                           Predicates.falsePredicate()).evaluate(new Object()));
	}

	public void testEvaluateAndFalseFalse() throws Exception
	{
		assertFalse(Predicates.and(Predicates.falsePredicate(), Predicates.falsePredicate()).evaluate(new Object()));
		assertFalse(Predicates.and(Predicates.falsePredicate(),
		                           Predicates.falsePredicate(),
		                           Predicates.falsePredicate()).evaluate(new Object()));
	}

	public void testOr() throws Exception
	{
		assertEquals(Predicates.TRUE, Predicates.or());
		assertEquals(Predicates.TRUE, Predicates.or(Collections.EMPTY_LIST));
		assertEquals("(TRUE OR FALSE)", new OrPredicate(Arrays.asList(Predicates.TRUE, Predicates.FALSE)).toString());
		assertEquals("(TRUE || FALSE)", Predicates.or(Predicates.TRUE, Predicates.FALSE).toString());
	}

	public void testEvaluateOrAllTrue() throws Exception
	{
		assertTrue(Predicates.or(Predicates.truePredicate(), Predicates.truePredicate()).evaluate(new Object()));
		assertTrue(Predicates.or(Predicates.truePredicate(),
		                         Predicates.truePredicate(),
		                         Predicates.truePredicate()).evaluate(new Object()));
	}

	public void testEvaluateOrFalseTrue() throws Exception
	{
		assertTrue(Predicates.or(Predicates.truePredicate(), Predicates.falsePredicate()).evaluate(new Object()));
		assertTrue(Predicates.or(Predicates.falsePredicate(),
		                         Predicates.truePredicate(),
		                         Predicates.falsePredicate()).evaluate(new Object()));
	}

	public void testEvaluateorFalseFalse() throws Exception
	{
		assertFalse(Predicates.or(Predicates.falsePredicate(), Predicates.falsePredicate()).evaluate(new Object()));
		assertFalse(Predicates.or(Predicates.falsePredicate(),
		                          Predicates.falsePredicate(),
		                          Predicates.falsePredicate()).evaluate(new Object()));
	}

	public void testEvaluateNot() throws Exception
	{
		assertFalse(Predicates.not(Predicates.truePredicate()).evaluate(new Object()));
		assertTrue(Predicates.not(Predicates.falsePredicate()).evaluate(new Object()));
	}

	public void testEvaluateNotNot() throws Exception
	{
		assertEquals(Predicates.truePredicate(), Predicates.not(Predicates.not(Predicates.truePredicate())));
	}

	public void testToString() throws Exception
	{
		assertEquals("NOT TRUE", Predicates.not(Predicates.truePredicate()).toString());
	}


	public void testNotNullPredicate() throws Exception
	{
		assertEquals("NOT NULL", Predicates.NOT_NULL.toString());
		assertFalse(Predicates.notNullPredicate().evaluate(null));
		assertTrue(Predicates.NOT_NULL.evaluate(new Object()));
	}
}