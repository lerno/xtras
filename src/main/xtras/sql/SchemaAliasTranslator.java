package xtras.sql;

import xtras.util.Tuple;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/** @author Christoffer Lerno */
class SchemaAliasTranslator
{
	private final Object m_schemaLock = new Object();
	private final List<Schema> m_schemas;
	private final Set<String> m_alias;

	public SchemaAliasTranslator()
	{
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
			                                                             alias + "' already registered.");
			m_schemas.add(new Schema(schema, pattern));
			m_alias.add(alias);
		}
	}

	public String translate(String query)
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

	private static class Schema extends Tuple<String, Pattern>
	{
		private Schema(String object1, Pattern object2) { super(object1, object2); }
	}

}
