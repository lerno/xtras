package xtras.sql;

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

	private final SchemaAliasTranslator m_translator;
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
		m_translator = new SchemaAliasTranslator();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addAlias(String alias, String schema)
	{
		m_translator.addAlias(alias, schema);
	}

	/**
	 * {@inheritDoc}
	 */
	public String translate(String query)
	{
		return m_translator.translate(query);
	}

	private DbConnection getConnection()
	{
		return s_connections.get();
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

	public boolean inTransaction()
	{
		return getConnection().isInTransaction();
	}

}
