package xtras.sql;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.sql.SQLException;
import java.sql.ResultSet;

/** @author Christoffer Lerno */
public final class Db
{
	Db() {}

	private final static Object LOCK = new Object();
	private final static Map<String, DbProxy> s_dbs = new HashMap<String, DbProxy>();
	private final static ThreadLocal<DbProxy> s_activeDb = new ThreadLocal<DbProxy>();
	private static DbProxy s_defaultDb = null;
	private static boolean s_debugFlag = true;

	/**
	 * Registers a driver as associated with a particular key.
	 *
	 * @param key the key associated with this source.
	 * @param driverClassName the full class name of the driver
	 * @param url the url for the particular db to connect to
	 * @param username the username when logging into the db.
	 * @param password the password to use.
	 * @param poolSize the size of the connection pool.
	 * @throws ClassNotFoundException if the driver couldn't be found.
	 */
	public static void registerDriver(String key,
	                                  String driverClassName,
	                                  String url,
	                                  String username,
	                                  String password, int poolSize) throws ClassNotFoundException
	{
		registerDb(new PooledDbProxy(key, driverClassName, url, username, password, poolSize));
	}

	public static void registerDb(DbProxy db)
	{
		synchronized (LOCK)
		{
			if (s_dbs.containsKey(db.getName())) throw new IllegalStateException("Db '" + db.getName()
			                                                                     + "' already registered.");
			if (s_defaultDb == null)
			{
				s_defaultDb = db;
			}
			s_dbs.put(db.getName(), db);
		}
	}

	/**
	 * Associates a database schema with an alias.
	 *
	 * @param alias the alias for a schema.
	 * @param schema the schema to associate with the given key.
	 * @param key the key to use with this schema.
	 */
	public static void addSchema(String alias, String schema, String key)
	{
		DbProxy db = getDb(key);
		if (db == null) throw new IllegalStateException("Key '" + key + "' not yet registered.");
		db.addSchema(alias, schema);
	}

	static DbProxy getDb(String key)
	{
		DbProxy db;
		synchronized (LOCK)
		{
			db = s_dbs.get(key);
		}
		return db;
	}

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
				if (s_defaultDb == null) throw new IllegalArgumentException("No db found.");
				db = s_defaultDb;
			}
			s_activeDb.set(s_defaultDb);
		}
		return db;
	}

	public static void beginTransaction(TransactionIsolation transactionIsolation) throws SQLException
	{
		getSelectedDb().beginTransaction(transactionIsolation);
	}

	public static void beginTransaction() throws SQLException
	{
		getSelectedDb().beginTransaction();
	}

	public static void rollback() throws SQLException
	{
		getSelectedDb().rollback();
	}

	public static void commit() throws SQLException
	{
		getSelectedDb().commit();
	}

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

	public static void close() throws SQLException
	{
		getSelectedDb().close();
	}

	public static ResultSet query(String query, Object... args) throws SQLException
	{
		return getSelectedDb().query(query, args);
	}

	public static <T> List<T> queryAll(String query, Object... args) throws SQLException
	{
		return getSelectedDb().queryAll(query, args);
	}

	@SuppressWarnings({"RedundantTypeArguments"})
	public static <T> T queryOne(String query, Object... args) throws SQLException
	{
		return getSelectedDb().<T>queryOne(query, args);
	}

	@SuppressWarnings({"RedundantTypeArguments"})
	public static <T> T insert(String insert, Object... args) throws SQLException
	{
		return getSelectedDb().<T>insert(insert, args);
	}

	public static int update(String update, Object... args) throws SQLException
	{
		return getSelectedDb().update(update, args);
	}

	public static void select(String database)
	{
		DbProxy db = getDb(database);
		if (db == null) throw new IllegalArgumentException("Tried to select unknown database '" + database + "'.");
		db.close();
		s_activeDb.set(db);
	}

	public static boolean isInTransaction()
	{
		return getSelectedDb().isInTransaction();
	}

	public static boolean getDebugFlag()
	{
		return s_debugFlag;
	}
	
}
