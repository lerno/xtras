package xtras.sql;

import java.sql.Connection;

/**
 * An enum for the possible transaction levels in java.sql.Connection.
 * <p>
 * @author Christoffer Lerno
 */
public enum TransactionIsolation
{
	/** @see Connection#TRANSACTION_READ_UNCOMMITTED **/
	READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
	/** @see Connection#TRANSACTION_READ_COMMITTED **/
	READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
	/** @see Connection#TRANSACTION_REPEATABLE_READ **/
	REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
	/** @see Connection#TRANSACTION_SERIALIZABLE **/
	SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE)

	;
	private final int m_transactionIsolation;

	TransactionIsolation(int isolation)
	{
		m_transactionIsolation = isolation;
	}

	public int getId()
	{
		return m_transactionIsolation;
	}

	public static TransactionIsolation fromId(int id)
	{
		for (TransactionIsolation isolation : values())
		{
			if (isolation.getId() == id) return isolation;
		}
		return null;
	}
}
