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
	private final DbPool m_pool;

	/**
	 * Creates a new PooledDbProxy that uses an underlying DbPool to
	 * pool connections.
	 * <p>
	 * It uses thead-local DbConnection instances to transparently
	 * handle transactions without having multiple instances of the
	 * proxy.
	 *
	 * @param driverClassName the full class name of the driver
	 * @param url the url for the particular db to connect to
	 * @param username the username when logging into the db.
	 * @param password the password to use.
	 * @param poolSize the size of the connection pool.
	 * @throws ClassNotFoundException if the driver class could not be found.
	 */
	public PooledDbProxy(String driverClassName,
	                     String url,
	                     String username,
	                     String password,
	                     int poolSize) throws ClassNotFoundException
	{
		Class.forName(driverClassName);
		m_pool = new DbPool(url, username, password, poolSize);
		m_translator = new SchemaAliasTranslator();
	}

	/** {@inheritDoc} */
	public void addAlias(String alias, String schema)
	{
		m_translator.addAlias(alias, schema);
	}

	/**
	 * Uses the internal translator to translate a query
	 * by resolving any alias.
	 *
	 * @param query the query to translate.
	 * @return the translated string.
	 * @see #addAlias(String, String) 
	 */
	private String translate(String query)
	{
		return m_translator.translate(query);
	}

	private DbConnection getConnection()
	{
		return s_connections.get();
	}

	/** {@inheritDoc} */
	public void beginTransaction(TransactionIsolation isolation) throws SQLException
	{
		getConnection().beginTransaction(m_pool, isolation);
	}

	/** {@inheritDoc} */
	public void rollback() throws SQLException
	{
		getConnection().rollback();
	}

	/** {@inheritDoc} */
	public void commit() throws SQLException
	{
		getConnection().commit();
	}

	/** {@inheritDoc} */
	@SuppressWarnings({"RedundantTypeArguments"})
	public <T> T insert(String insert, Object... args) throws SQLException
	{
		return (T) getConnection().insert(m_pool, translate(insert), args);
	}

	/** {@inheritDoc} */
	public <T> T query(ResultProcessor<T> processor, String query, Object... args) throws SQLException
	{
		return getConnection().query(m_pool, processor, translate(query), args);
	}

	/** {@inheritDoc} */
	public int update(String update, Object... args) throws SQLException
	{
		return getConnection().update(m_pool, translate(update), args);
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * This will call {@link xtras.sql.DbPool#isValid()} on the underlying {@link DbPool}.
	 */
	public boolean isValid()
	{
		return m_pool.isValid();
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * This will call {@link DbPool#shutdown()} on the underlying {@link DbPool}.
	 */
	public void shutdown()
	{
		m_pool.shutdown();
	}

	public boolean inTransaction()
	{
		return getConnection().isInTransaction();
	}

}
