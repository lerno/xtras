package xtras.io;

/**
 * Interface needed to use an Output class with an arbitrary target output. 
 *
 * @author Christoffer Lerno
 */
public interface Out
{
	/**
	 * Print a linefeed.
	 */
	void println();

	/**
	 * Print a string.
	 *
	 * @param s the string to write.
	 */
	void print(String s);

}
