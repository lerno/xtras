/**
 * The SQL package contains companion classes to {@code java.sql.*}.
 * <p/>
 * The {@link xtras.sql.SQL} class has assorted utility functions,
 * with {@link xtras.sql.Db} offering a Db registry with database
 * connections with built in pooling capability, and the possibility
 * to create fake db proxies ({@link xtras.sql.DbProxyFake}) that return programatically generated
 * results.
 * <p/>
 * <em>The DB classes are still experimental and not recommended for use yet.</em>
 */
package xtras.sql;