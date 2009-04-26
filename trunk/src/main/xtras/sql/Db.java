package xtras.sql;

import xtras.util.CollectionExtras;
import xtras.lang.ObjectExtras;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;

/**
 * This class provides the means to make simple interactions with one or several databases.
 * <p/>
 * By default it provides a pooled db proxy implementation.
 * <p/>
 * Typically you register a db linked to a string key.
 * <p/>
 * Here is an example where we register a sqlite driver to connect with the user "user" and the
 * password "password", with 10 connections in the pool, and use it.
 * <p/>
 * <pre>
 * // Let's register the db:
 * Db.register("pokerdb", "org.sqlite.JDBC", "jdbc:sqlite:poker.db", "user", "password", 10);}
 *
 * // Now we want to insert a new player.
 * Integer playerKey = Db.insert("insert into users values (?, ?)", "Foo", "foopass");
 *
 * // Let's update the password for "Foo"!
 * int rowsUpdated = Db.update("update users set password = ?", "bar");
 * </pre>
 * <p/>
 * It's possible to retrieve a single row or all rows:
 * <pre>
 * // Retrieve a list containing all columns in the first row.
 * List playerData = Db.queryOne("select * from users where key = ?", playerKey);
 *
 * // Retrieve all rows with all columns.
 * {@literal List<List> playerDataList = Db.queryAll("select * from users"); }
 * </pre>
 * <p/>
 * As a convenience, retrieving a single column will return the value rather than the
 * value embedded in a list of size 1:
 * <pre>
 * // The single column will be interpreted as a value rather than a list:
 * String user = Db.queryOne("select user from users where key = ?", playerKey);
 *
 * // Same behaviour when retrieving multiple rows:
 * {@literal List<String> userNames = Db.queryAll("select user from users"); }
 * </pre>
 * <p/>
 * We can also work with the result set itself, by writing a ResultProcessor.
 * <code>
 * TheResultClass theResult = Db.query(new MyNewResultProcessor(), "select * from users");
 * </code>
 * <p/>
 * If we start a transaction, we need to explicitly commit or rollback.
 * <pre>
 * Db.beginTransaction();
 * try
 * {
 *     ... make updates, inserts and queries.
 *
 *     // Everything ok, let's commit!
 *     Db.commit();
 * }
 * catch (Exception e)
 * {
 *     // Something failed, do a rollback.
 *     Db.rollback();
 * }
 * </pre>
 *
 *
 * @author Christoffer Lerno
 */
public final class Db
{
	Db() {}

	private final static Object LOCK = new Object();
	private final static Map<String, DbProxy> s_dbs = new HashMap<String, DbProxy>();
	private final static ThreadLocal<DbProxy> s_activeDb = new ThreadLocal<DbProxy>();
	private static DbProxy s_defaultDb = null;

	/**
	 * Creates and registers a new DbProxy with the given key.
	 * <p>
	 * This will method by default uses a PooledDbProxy.
	 *
	 * @param key the key associated with this source.
	 * @param driverClassName the full class name of the driver
	 * @param url the url for the particular db to connect to
	 * @param username the username when logging into the db.
	 * @param password the password to use.
	 * @param poolSize the size of the connection pool.
	 * @throws ClassNotFoundException if the driver couldn't be found.
	 */
	public static void register(String key,
	                            String driverClassName,
	                            String url,
	                            String username,
	                            String password, int poolSize) throws ClassNotFoundException
	{
		registerDb(new PooledDbProxy(key, driverClassName, url, username, password, poolSize));
	}

	/**
	 * Register a db proxy. This allows a programmer to add customized db proxy implementations
	 * instead of the default PooledDbProxy.
	 *
	 * @param dbProxy the database proxy to register.
	 * @throws IllegalStateException if a proxy with the same name was already registered.
	 */
	public static void registerDb(DbProxy dbProxy)
	{
		synchronized (LOCK)
		{
			if (s_dbs.containsKey(dbProxy.getName())) throw new IllegalStateException("Db '" + dbProxy.getName()
			                                                                          + "' already registered.");
			if (s_defaultDb == null)
			{
				s_defaultDb = dbProxy;
			}
			s_dbs.put(dbProxy.getName(), dbProxy);
		}
	}

	/**
	 * Unregisters a db proxy and runs its shutdown method.
	 * <p/>
	 * If this was the default db, then a random other db will replace it
	 * as the default.
	 *
	 * @param key the key of the proxy to unregister.
	 * @return true if there was a DbProxy with this name, false otherwise.
	 */
	public static boolean unregisterDb(String key)
	{
		DbProxy proxy;
		synchronized (LOCK)
		{
			proxy = s_dbs.remove(key);
			if (ObjectExtras.equals(proxy, s_defaultDb))
			{
				s_defaultDb = CollectionExtras.getAny(s_dbs.values());
			}
		}
		if (proxy == null) return false;
		proxy.shutdown();
		return true;
	}

	/**
	 * Associates a database schema with an alias. This will call DbProxy#addSchema(String, String) on
	 * the db with the given key.
	 *
	 * @param alias the alias for a schema.
	 * @param schema the schema to associate with the given key.
	 * @param key the key to use with this schema.
	 * @throws IllegalArgumentException if there is no db with this key registered yet.
	 */
	public static void addSchema(String alias, String schema, String key)
	{
		DbProxy db = getDb(key);
		if (db == null) throw new IllegalArgumentException("Key '" + key + "' not yet registered.");
		db.addAlias(alias, schema);
	}

	/**
	 * Returns the db for a given key.
	 *
	 * @param key the key to retrieve the db for.
	 * @return the db or null if there is no db with this name.
	 */
	static DbProxy getDb(String key)
	{
		DbProxy db;
		synchronized (LOCK)
		{
			db = s_dbs.get(key);
		}
		return db;
	}

	/**
	 * Return the currently selected (on this thread) db.
	 *
	 * @return the currently selected db.
	 * @throws IllegalStateException if no db was found.
	 */
	private static DbProxy getSelectedDb()
	{
		DbProxy db = s_activeDb.get();
		if (db != null && !db.isValid())
		{
			s_activeDb.set(null);
			db = null;
		}
		if (db == null)
		{
			synchronized (LOCK)
			{
				if (s_defaultDb == null) throw new IllegalStateException("No db found.");
				db = s_defaultDb;
			}
			s_activeDb.set(s_defaultDb);
		}
		return db;
	}

	/**
	 * Starts a transaction with a given isolation level.
	 *
	 * @param transactionIsolation the isolation level desired.
	 * @throws SQLException if there was an error starting the transaction.
	 * @see Db#beginTransaction();
	 */
	public static void beginTransaction(TransactionIsolation transactionIsolation) throws SQLException
	{
		getSelectedDb().beginTransaction(transactionIsolation);
	}

	/**
	 * Starts a transaction, ensuring that all updates and inserts declared
	 * after this statment will either <em>all</em> be performed, or none of them
	 * will be performed.
	 * <p>
	 * To end a transaction, it is required to use either rollback() or commit().
	 * Either will close the last result set, and also end the transaction.
	 * <p>Note that if this operation fails, we are ensured that the current
	 * statement and result set is closed, and the connection properly
	 * released.
	 * If the operation succeeds we have acquired a connection that is
	 * set with auto-commit off.
	 *
	 * @throws SQLException if there was an error starting the transaction
	 * or if we already had entered a transaction.
	 */
	public static void beginTransaction() throws SQLException
	{
		getSelectedDb().beginTransaction(null);
	}

	/**
	 * Rollbacks the current transaction.
	 * <p>
	 * Regardless if the rollback succeeds or not, we are guaranteed to
	 * have closed any result sets and statements, as well as having
	 * released the current connection.
	 * <p>
	 * The transaction is also guaranteed to have ended.
	 *
	 * @throws SQLException if there was an error when calling rollback,
	 * or if we were not in a transaction.
	 */
	public static void rollback() throws SQLException
	{
		getSelectedDb().rollback();
	}

	/**
	 * Commits all changes in the current transaction.
	 * <p>
	 * Regardless if the commit succeeds or not, we are guaranteed to
	 * have closed any result sets and statements, as well as having
	 * released the current connection.
	 * <p>
	 * If there is an error committing and the transaction was properly
	 * started, the result will be the same as calling Db#rollback().
	 *
	 * @throws SQLException if there was an error when calling rollback,
	 * or if we were not in a transaction.
	 */
	public static void commit() throws SQLException
	{
		getSelectedDb().commit();
	}

	/**
	 * Unregisters and calls DbProxy#shutdown on all registered proxies.
	 */
	public static void unregisterAll()
	{
		ArrayList<DbProxy> dbs;
		synchronized (LOCK)
		{
			dbs = new ArrayList<DbProxy>(s_dbs.values());
			s_dbs.clear();
			s_defaultDb = null;
		}
		for (DbProxy db : dbs)
		{
			db.shutdown();
		}
	}


	/**
	 * Returns the first resulting row of the given query and closes the result set.
	 * <p>
	 * This is the same as doing:
	 * <pre>
	 * try
	 * {
	 *     ResultSet rs = Db.query(query, args);
	 *     if (!rs.next()) return null;
	 *     // Extract values from the row.
	 *     return getValuesFromRow(rs);
	 * }
	 * finally
	 * {
	 *     Db.close();
	 * }
	 * </pre>
	 * This query usually returns a list containing the values of all columns in order,
	 * but as a convenience, retrieving a single column will return the value rather than the
	 * value embedded in a list of size 1.
	 *
	 * @param query the query to run.
	 * @param args the arguments to the query.
	 * @return a list of values with the values in the columns, unless there is a single value -
	 * in this case that value is returned.
	 * @throws SQLException if the query fails for some reason.
	 * @see #query
	 */
	@SuppressWarnings({"RedundantTypeArguments"})
	public static <T> T queryOne(String query, Object... args) throws SQLException
	{
		return getSelectedDb().query(new SingleResultProcessor<T>(), query, args);
	}

	/**
	 * Returns the first all the resulting rows of the given query and closes the result set.
	 * <p>
	 * This is the same as doing:
	 * <pre>
	 * try
	 * {
	 *     ResultSet rs = Db.query(query, args);
	 *     List list = new ArrayList();
	 *     while (rs.next())
	 *     {
	 *         list.add(getValuesFromRow(rs));
	 *     }
	 *     return list;
	 * }
	 * finally
	 * {
	 *     Db.close();
	 * }
	 * </pre>
	 * Each entry in the list is usually a list containing the values of all columns in order,
	 * but as a convenience, retrieving a single column will return the a list of those
	 * values rather than a list of lists with a single value.
	 * 
	 * @param query the query to run.
	 * @param args the arguments to the query.
	 * @return a list of rows
	 * @throws SQLException if the query fails for some reason.
	 * @see #query
	 */
	public static <T> List<T> queryAll(String query, Object... args) throws SQLException
	{
		return getSelectedDb().query(new AllResultProcessor<T>(), query, args);
	}

	/**
	 * Performs a db query and allows a ResultProcessor work on each row in the
	 * result set. The method then returns the object returned by ResultProcessor#getResult.
	 *
	 * @param processor the result processor to use.
	 * @param query the query to use, with '?' to represent arguments.
	 * @param args a list of arguments, should always be same number as the '?' in the query.
	 * @return the result from the ResultProcessor's {@code getResult} method
	 * @throws SQLException if there was an error performing the query.
	 * @see xtras.sql.ResultProcessor#getResult()
	 */
	public static <T> T query(ResultProcessor<T> processor, String query, Object... args) throws SQLException
	{
		return getSelectedDb().query(processor, query, args);
	}

	/**
	 * Performs an insert and returns a single key or a list, containing the key(s)
	 * generated by the insert.
	 * <p/>
	 * This will automatically close the current statement and result set. The current
	 * connection will remain available only if in a transaction, otherwise the
	 * connection will be released.
	 *
	 * @param insert the insert to run.
	 * @param args the arguments to the insert.
	 * @return the key generated if a single key, a list (List) of keys if the number of keys is != 1.
	 * @throws SQLException if the insert fails for some reason.
	 */
	@SuppressWarnings({"RedundantTypeArguments"})
	public static <T> T insert(String insert, Object... args) throws SQLException
	{
		return getSelectedDb().<T>insert(insert, args);
	}

	/**
	 * Runs a db SQL update.
	 *
	 * @param update the sql update statement.
	 * @param args the arguments to the sql statement.
	 * @return the number of rows in the update.
	 * @throws SQLException if the update failed.
	 */
	public static int update(String update, Object... args) throws SQLException
	{
		return getSelectedDb().update(update, args);
	}

	/**
	 * Select the database to use for consequent queries on this thread.
	 * <p>
	 * This will implicitly cause a close on the old proxy previously used.
	 *
	 * @param key the key of the db to use.
	 * @throws IllegalArgumentException if there is no db with the key.
	 */
	public static void select(String key)
	{
		DbProxy db = getDb(key);
		if (db == null) throw new IllegalArgumentException("Tried to select unknown database '" + key + "'.");
		//db.close();
		s_activeDb.set(db);
	}

	/**
	 * Test if the db is currently in a transaction.
	 *
	 * @return true if in a transaction, false otherwise.
	 */
	public static boolean isInTransaction()
	{
		return getSelectedDb().inTransaction();
	}
}
