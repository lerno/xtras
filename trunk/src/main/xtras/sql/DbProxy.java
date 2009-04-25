package xtras.sql;

import java.sql.SQLException;
import java.sql.ResultSet;

/** @author Christoffer Lerno */
public interface DbProxy
{
	void addSchema(String alias, String schema);

	void beginTransaction() throws SQLException;

	void beginTransaction(TransactionIsolation isolation) throws SQLException;

	void rollback() throws SQLException;

	void commit() throws SQLException;

	void close();

	ResultSet query(String query, Object... args) throws SQLException;

	<T> T insert(String insert, Object... args) throws SQLException;

	int update(String update, Object... args) throws SQLException;

	String getName();

	boolean isValid();

	void shutdown();

	boolean isInTransaction();
}
