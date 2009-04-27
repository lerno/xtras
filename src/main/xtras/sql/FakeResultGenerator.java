package xtras.sql;

/**
 * This class generates a faked db result in response to a query from DbProxyFake.
 *
 * @author Christoffer Lerno
 */
public interface FakeResultGenerator
{
	/**
	 * Create a result for a given row.
	 * <p/>
	 * Examples:
	 * <p/>
	 * A result with infinitely many rows that each returns the current row number (starting with 1):
	 * <pre>
	 * new FakeResultGenerator()
	 * {
	 *    Object[] createResult(int row, Object[] arguments)
	 *    {
	 *       return new Object[] { row + 1 };
	 *    }
	 * }
	 * </pre>
	 * A single row result that returns the sum of the two (integer) arguments to query:
	 * <pre>
	 * new FakeResultGenerator()
	 * {
	 *    Object[] createResult(int row, Object[] arguments)
	 *    {
	 *       // Return null for all other rows than 0, for a single row ResultSet.
	 *       return row == 0 ? new Object[] { (Integer) arguments[0]) + (Integer) arguments[1] } : null;
	 *    }
	 * }
	 * </pre>
	 * 
	 * @param row the row to create a response for, starting with 0 for the first row.
	 * @param arguments the arguments sent to the query, to allow customizing of the result.
	 * @return the resulting columns on the row, or null to simulate the end of the result set.
	 */
	Object[] createResult(int row, Object[] arguments);
}
