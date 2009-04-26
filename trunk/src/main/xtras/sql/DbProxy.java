package xtras.sql;

import java.sql.SQLException;
import java.util.List;

/** @author Christoffer Lerno */
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

	<T> T queryOne(String query, Object... args) throws SQLException;

	void beginTransaction() throws SQLException;

	void beginTransaction(TransactionIsolation isolation) throws SQLException;

	void rollback() throws SQLException;

	void commit() throws SQLException;

	<T> T insert(String insert, Object... args) throws SQLException;

	int update(String update, Object... args) throws SQLException;

	<T> T query(ResultProcessor<T> processor, String query, Object... args) throws SQLException;

	String getName();

	boolean isValid();

	void shutdown();

	boolean isInTransaction();

	<T> List<T> queryAll(String query, Object... args) throws SQLException;
}
