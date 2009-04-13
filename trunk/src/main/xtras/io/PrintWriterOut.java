package xtras.io;

import java.io.PrintWriter;

/** @author Christoffer Lerno */
class PrintWriterOut implements Out
{
	private final PrintWriter m_writer;
	public PrintWriterOut(PrintWriter writer)
	{
		m_writer = writer;
	}

	public void print(String s)
	{
		m_writer.write(s);
		m_writer.flush();
	}

	public void println()
	{
		m_writer.println();
		m_writer.flush();
	}
}
