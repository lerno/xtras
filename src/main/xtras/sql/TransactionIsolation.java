package xtras.sql;

import java.sql.Connection;

/**
 * An enum for the possible transaction levels in {@code java.sql.Connection}.
 * <p>
 * @author Christoffer Lerno
 */
public enum TransactionIsolation
{
	/**
	 * Dirty reads, non-repeatable reads and phantom reads can occur.
	 * It allows a row to be read back before that read has been committed,
	 * (a "dirty read"), so that the read may become invalid.
	 *
	 * @see Connection#TRANSACTION_READ_UNCOMMITTED
	 **/
	READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
	/**
	 * Dirty reads are prevented, non-repeatable reads and phatom reads
	 * may occur. This level only prohibits transactions
	 * from reading a row with uncommitted changes in it.
	 *
	 * @see Connection#TRANSACTION_READ_COMMITTED
	 **/
	READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),

	/**
	 * Dirty reads and non-repeatable reads are prevented,
	 * phantom reads may occur.
	 * In addition to prohibiting reads of rows with uncommitted
	 * changes, it prohibits the case where one transaction
	 * alters a row, and the first transaction
     * rereads the row, getting different values the second time
     * (a "non-repeatable read").
	 * @see Connection#TRANSACTION_REPEATABLE_READ
	 **/
	REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),

	/**
	 * Prevents dirty reads, non-repeatable reads and phantom reads.
	 * This is includes all effects as detailed in {@link TransactionIsolation#REPEATABLE_READ},
	 * and also prohibits the situation where one transaction reads
	 * a table using {@code WHERE}, then another transaction inserts
	 * a row, so that if the first transaction rereads with the
	 * same {@code WHERE}, an additional "phantom" row will be found.
	 * @see Connection#TRANSACTION_SERIALIZABLE
	 **/
	SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE)

	;
	private final int m_transactionIsolation;

	TransactionIsolation(int isolation)
	{
		m_transactionIsolation = isolation;
	}

	/**
	 * Returns this TransactionIsolation object's integer value
	 * as defined in {@link java.sql.Connection}.
	 *
	 * @return the integer value as defined in {@link java.sql.Connection}
	 */
	public int getId()
	{
		return m_transactionIsolation;
	}

	/**
	 * Returns the TransactionIsolation given the integer value
	 * counterpart in {@link java.sql.Connection}.
	 *
	 * @param id the transaction isolation id.
	 * @return the enum object with the same id, or null if no match was found.
	 */
	public static TransactionIsolation fromId(int id)
	{
		for (TransactionIsolation isolation : values())
		{
			if (isolation.getId() == id) return isolation;
		}
		return null;
	}
}
