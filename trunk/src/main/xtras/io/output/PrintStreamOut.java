package xtras.io.output;

import xtras.io.Out;

import java.io.PrintStream;

/** @author Christoffer Lerno */
class PrintStreamOut implements Out
{
	private final PrintStream m_stream;

	public PrintStreamOut(PrintStream stream)
	{
		m_stream = stream;
	}

	public void print(String s)
	{
		m_stream.print(s);
		m_stream.flush();
	}

	public void println()
	{
		m_stream.println();
		m_stream.flush();
	}
}
