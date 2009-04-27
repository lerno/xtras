package xtras.sql;

/**
 * A basic result processor implementation, to simplify anonymous ResultProcessor classes.
 *
 * @author Christoffer Lerno
 */
public abstract class AbstractResultProcessor<C> implements ResultProcessor<C>
{
	/**
	 * The value of this processor.
	 */
	private C m_value = null;

	/**
	 * Sets the current value of this processor.
	 *
	 * @param value the result of this processor.
	 */
	protected void setResult(C value)
	{
		m_value = value;
	}

	/**
	 * {@inheritDoc}
	 */
	public C getResult()
	{
		return m_value;
	}
}
