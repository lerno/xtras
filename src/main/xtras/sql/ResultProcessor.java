package xtras.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A helper object that is able to process rows in a result set and collect the data into a single
 * result.
 *
 * @author Christoffer Lerno
 */
public interface ResultProcessor<C>
{
	/**
	 * Process a result set row.
	 * <p>
	 * A result processor is fed the result set once for every row
	 * or until the processor signals that no more rows are needed.
	 * In principle, the code looks like this:
	 *
	 * <pre>
	 * try
	 * {
	 *    ResultSet set = statement.executeQuery();
	 *    while (set.next())
	 *    {
	 *        if (!processor.process(set)) break;
	 *    }
	 *    return processor.getResult();
	 * }
	 * finally
	 * {
	 *    ... close result set etc. ...
	 * }
	 * </pre>
	 * Rows are therefore guaranteed to appear in the same order as from the query,
	 * and the processor should usually not call {@link java.sql.ResultSet#next} explicitly.
	 *
	 * @param resultSet the result set to process.
	 * @return true if the processor is able to process another row of data,
	 * false if the processor is done processing data.
	 * @throws SQLException if there was error reading from the result set.
	 */
	boolean process(ResultSet resultSet) throws SQLException;

	/**
	 * The result from working on the given result set.
	 *
	 * @return the collected result from working with the result set.
	 * @see #process
	 */
	C getResult();
}
