package xtras.sql;

import xtras.util.Tuple;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.sql.SQLException;

/** @author Christoffer Lerno */
public class PooledDbProxy implements DbProxy
{
	private final static ThreadLocal<DbConnection> s_connections = new ThreadLocal<DbConnection>()
	{
		@Override
		protected DbConnection initialValue()
		{
			return new DbConnection();
		}
	};

	private final Object m_schemaLock = new Object();
	private final List<Schema> m_schemas;
	private final Set<String> m_alias;
	private final String m_key;
	private final DbPool m_pool;

	/**
	 * Registers a driver as associated with a particular key.
	 *
	 * @param key the key associated with this source.
	 * @param driverClassName the full class name of the driver
	 * @param url the url for the particular db to connect to
	 * @param username the username when logging into the db.
	 * @param password the password to use.
	 * @param poolSize the size of the connection pool.
	 * @throws ClassNotFoundException if the driver class could not be found.
	 */
	public PooledDbProxy(String key,
	              String driverClassName,
	              String url,
	              String username,
	              String password,
	              int poolSize) throws ClassNotFoundException
	{
		Class.forName(driverClassName);
		m_key = key;
		m_pool = new DbPool(url, username, password, poolSize);
		m_alias = new HashSet<String>();
		m_schemas = new ArrayList<Schema>();
	}

	/**
	 * Associates an alias with a schema.
	 *
	 * @param alias the alias to use for this schema.
	 * @param schema the key to use with this schema.
	 */
	public void addAlias(String alias, String schema)
	{
		alias = alias.toLowerCase();
		Pattern pattern = Pattern.compile(Pattern.quote("<" + alias + ">."), Pattern.CASE_INSENSITIVE);
		synchronized (m_schemaLock)
		{
			if (m_alias.contains(alias)) throw new IllegalStateException("Db schema '" +
			                                                             alias + "' already registered " +
			                                                             "with key '" + m_key + "'.");
			m_schemas.add(new Schema(schema, pattern));
			m_alias.add(alias);
		}
	}

	public synchronized String translate(String query)
	{
		synchronized (m_schemaLock)
		{
			for (Schema schema : m_schemas)
			{
				Matcher matcher = schema.second.matcher(query);
				if (matcher.find())
				{
					query = matcher.replaceAll(schema.first + ".");
				}
			}
		}
		return query;
	}

	DbConnection getConnection()
	{
		return s_connections.get();
	}

	public void beginTransaction() throws SQLException
	{
		beginTransaction(null);
	}

	public void beginTransaction(TransactionIsolation isolation) throws SQLException
	{
		getConnection().beginTransaction(m_pool, isolation);
	}

	public void rollback() throws SQLException
	{
		getConnection().rollback();
	}

	public void commit() throws SQLException
	{
		getConnection().commit();
	}

	@SuppressWarnings({"RedundantTypeArguments"})
	public <T> T insert(String insert, Object... args) throws SQLException
	{
		return (T) getConnection().insert(m_pool, translate(insert), args);
	}

	public <T> T query(ResultProcessor<T> processor, String query, Object... args) throws SQLException
	{
		return getConnection().query(m_pool, processor, translate(query), args); 
	}

	public <T> T queryOne(String query, Object... args) throws SQLException
	{
		return (T) getConnection().queryOne(m_pool, translate(query), args);
	}

	public <T> List<T> queryAll(String query, Object... args) throws SQLException
	{
		return getConnection().queryAll(m_pool, translate(query), args);
	}
	
	public int update(String update, Object... args) throws SQLException
	{
		return getConnection().update(m_pool, translate(update), args);
	}

	public String getName()
	{
		return m_key;
	}

	public boolean isValid()
	{
		return m_pool.isValid();
	}

	public void shutdown()
	{
		m_pool.shutdown();
	}

	public boolean isInTransaction()
	{
		return getConnection().isInTransaction();
	}

	private static class Schema extends Tuple<String, Pattern>
	{
		private Schema(String object1, Pattern object2) { super(object1, object2); }
	}

}
