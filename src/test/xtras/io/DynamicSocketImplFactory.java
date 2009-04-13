package xtras.io;

import java.net.SocketImplFactory;
import java.net.SocketImpl;
import java.net.Socket;
import java.net.ServerSocket;
import java.util.Queue;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Used to fake out sockets. Load it with a socket implemenation and that is the socket that
 * will get loaded.
 *
 * @author Christoffer Lerno
 */
public class DynamicSocketImplFactory implements SocketImplFactory
{
	private static DynamicSocketImplFactory s_instance = null;
	private static Queue<SocketImpl> s_nextSockets = new ConcurrentLinkedQueue<SocketImpl>();

	private DynamicSocketImplFactory()
	{
	}

	public SocketImpl createSocketImpl()
	{
		return s_nextSockets.poll();
	}

	public static synchronized void setNextSocket(SocketImpl... nextSockets)
	{
		if (s_instance == null)
		{
			s_instance = new DynamicSocketImplFactory();
			try
			{
				Socket.setSocketImplFactory(s_instance);
				ServerSocket.setSocketFactory(s_instance);
			}
			catch (Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		s_nextSockets = new ConcurrentLinkedQueue<SocketImpl>(Arrays.asList(nextSockets));
	}
}
