package xtras.io;

import java.net.SocketImpl;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.io.*;

/** @author Christoffer Lerno */
public class SocketImplAdaptor extends SocketImpl
{

	protected void accept(SocketImpl s) throws IOException
	{
	}

	protected int available() throws IOException
	{
		return 0;
	}

	protected void bind(InetAddress host, int aPort) throws IOException
	{
	}

	protected void close() throws IOException
	{
	}

	protected void connect(InetAddress anAddress, int aPort) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	protected void connect(String host, int aPort) throws IOException
	{
		throw new UnsupportedOperationException();
	}

	protected void connect(SocketAddress anAddress, int timeout) throws IOException
	{
	}

	protected void create(boolean stream) throws IOException
	{
	}

	protected OutputStream getOutputStream() throws IOException
	{
		return new PipedOutputStream(new PipedInputStream());
	}

	protected InputStream getInputStream() throws IOException
	{
		return new PipedInputStream(new PipedOutputStream());
	}

	protected void listen(int backlog) throws IOException
	{
	}

	protected void sendUrgentData(int data) throws IOException
	{
	}

	public Object getOption(int optID) throws SocketException
	{
		return null;
	}

	public void setOption(int optID, Object value) throws SocketException
	{
	}
}