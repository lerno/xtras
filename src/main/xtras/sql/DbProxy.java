package xtras.sql;

import java.sql.SQLException;

/**
 * Implement DbProxy to allow a db proxy implementation be
 * registered with the Db class.
 *
 * @author Christoffer Lerno */
public interface DbProxy
{
	/**
	 * Add a schema alias for this proxy. This will cause search-and-replace on
	 * this alias to that of the schema in any query on this proxy.
	 * <p>
	 * For example {@code addAlias("foo", "bar") } will cause the SQL
	 * query "select * from {@literal <FOO>}.test" to be translated to 
	 * "select * from bar.test".
	 * @param alias the alias to replace when encountered in a sql-string on the form {@literal <alias>}.
	 * @param schema the schema to replace the alias with.
	 */
	void addAlias(String alias, String schema);

	/**
	 * Begin a database transaction with the given transaction isolation.
	 *
	 * @param isolation the isolation level to use, or null for none.
	 * @throws SQLException if there was any issue starting the transaction.
	 * <em>Note that a correct implementation guarantees that there are no open transaction
	 * after the exception is thrown.</em>
	 */
	void beginTransaction(TransactionIsolation isolation) throws SQLException;

	/**
	 * Rollbacks the current transaction.
	 * <p>
	 * This will end the transaction and discard any updates or inserts made
	 * in the transaction.
	 *
	 * @throws SQLException if there was an error doing rollback.
	 * <em>Note that a correct implementation guarantees that there are no open transaction
	 * after the exception is thrown.</em>
	 */
	void rollback() throws SQLException;

	/**
	 * Commits on the current transaction.
	 * <p>
	 * This will end the transaction and apply all updates or inserts made
	 * in the transaction.
	 * <p>
	 * <em>Note that a correct implementation guarantees that there are no open transaction
	 * after the exception is thrown, so the effect of an exception during
	 * commit is the same as running {@link #rollback()}.</em>
	 *
	 * @throws SQLException if there was an error doing the commit.
	 */
	void commit() throws SQLException;

	/**
	 * Executes an SQL insert query and returns the keys generated, if any.
	 * <p/>
	 * For more details on how arguments are replaced into the query
	 * string, see {@link java.sql.Connection#prepareStatement(String)}.
	 *
	 * @param insert the sql insert query, parameterized with '?'.
	 * @param args the arguments to insert in the parameter slots.
	 * @return the key generated by the query, or a {@link java.util.List} of
	 * keys if more than a single key was generated.
	 * @throws SQLException if there was an error executing the update.
	 */
	<T> T insert(String insert, Object... args) throws SQLException;

	/**
	 * Executes an SQL update query and returns the number of rows changed.
	 * <p/>
	 * For more details on how arguments are replaced into the query
	 * string, see {@link java.sql.Connection#prepareStatement(String)}.
	 *
	 * @param update the sql update query, parameterized with '?'.
	 * @param args the arguments to insert in the parameter slots.
	 * @return the number of rows that was changed due to this update.
	 * @throws SQLException if there was an error executing the update.
	 */
	int update(String update, Object... args) throws SQLException;

	/**
	 * Runs a query using a ResultProcessor to work on the ResultSet.
	 * <p/>
	 * For more details on how arguments are replaced into the query
	 * string, see {@link java.sql.Connection#prepareStatement(String)}.
	 * 
	 * @param processor a ResultProcessor to work on the ResultSet.
	 * @param query the SQL query to execute.
	 * @param args the arguments to insert at each '?' in the query string.
	 * @return the result as generated by {@link ResultProcessor#getResult} after
	 * it has worked on the result set.
	 * @throws SQLException if there was an error performing the query.
	 */
	<T> T query(ResultProcessor<T> processor, String query, Object... args) throws SQLException;

	/**
	 * Checks if this proxy is valid (ie has not yet been shut down).
	 *
	 * @return true if this proxy is valid, false otherwise.
	 */
	boolean isValid();

	/**
	 * Disconnects all connections and invalidates this proxy.
	 */
	void shutdown();

	/**
	 * Check if this proxy is currently in a transaction.
	 *
	 * @return true if this proxy is in a transaction, false otherwise.
	 */
	boolean inTransaction();
}