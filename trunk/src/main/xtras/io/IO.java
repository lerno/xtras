package xtras.io;

import xtras.lang.StringExtras;

import java.io.*;
import java.net.*;
import java.util.*;

/** @author Christoffer Lerno */
public class IO
{

	private static NetworkInterfaceProvider s_provider = new DefaultNetworkInterfaceProvider();

	/**
	 * Silently close a closeable object (e.g. a stream).
	 *
	 * @param closeable the closeable to close, may be null.
	 */
	public static void closeSilently(Closeable closeable)
	{
		if (closeable == null) return;
		try
		{
			closeable.close();
		}
		catch (Exception e)
		{
			// Do nothing.
		}
	}

	/**
	 * Silently close a server socket.
	 *
	 * @param socket the server socket to close, may be null.
	 */
	public static void closeSilently(ServerSocket socket)
	{
		if (socket == null) return;
		try
		{
			socket.close();
		}
		catch (Exception e)
		{
			// Do nothing.
		}
	}

	/**
	 * Silently close a socket.
	 *
	 * @param socket the socket to close, may be null.
	 */
	public static void closeSilently(Socket socket)
	{
		if (socket == null) return;
		try
		{
			socket.close();
		}
		catch (Exception e)
		{
			// Do nothing.
		}
	}


	/**
	 * Store a string as a file.
	 *
	 * @param file the file to save to.
	 * @param content the string to save.
	 * @throws IOException if there was an error writing to the file.
	 */
	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
	public static void store(File file, String content) throws IOException
	{
		FileOutputStream stream = null;
		try
		{
			stream = new FileOutputStream(file, false);
			stream.write(content.getBytes("UTF-8"));
		}
		finally
		{
			closeSilently(stream);
		}
	}

	/**
	 * Read an entire file as a string.
	 *
	 * @param file the file to read.
	 * @return the resulting string.
	 * @throws IOException if we failed to read the file.
	 */
	@SuppressWarnings({"IOResourceOpenedButNotSafelyClosed"})
	public static String read(File file) throws IOException
	{
		FileInputStream stream = null;
		try
		{
			stream = new FileInputStream(file);
			int size = stream.available();
			byte[] allBytes = new byte[size];
			int read = 0;
			while (read < allBytes.length)
			{
				int bytesRead = stream.read(allBytes, read, allBytes.length - read);
				if (bytesRead == -1) throw new EOFException("Unexpected EOF.");
				read += bytesRead;
			}
			return new String(allBytes);
		}
		finally
		{
			closeSilently(stream);
		}
	}

	/**
	 * Append string to a local file if it exists, otherwise it is created.
	 * <p>
	 * If the file exist we append data at the end of it.
	 *
	 * @param file the name of the file to append to.
	 * @param content The string to append, must have content and not be null.
	 * @throws IOException if there was some IO error writing the file.
	 */
	public static void append(File file, String content) throws IOException
	{
		assert StringExtras.hasContent(content);

		RandomAccessFile randomAccessFile = null;
		try
		{
			// Open file for writing
			//noinspection IOResourceOpenedButNotSafelyClosed
			randomAccessFile = new RandomAccessFile(file, "rw");

			// Move beyond end of file, ie write at end of file

			randomAccessFile.seek(randomAccessFile.length());

			// write content string
			randomAccessFile.writeBytes(content);
		}
		finally
		{
			IO.closeSilently(randomAccessFile);
		}
	}

	/**
	 * Reads a file, splitting the rows to create a list of rows.
	 *
	 * @param file the file to read.
	 * @return the rows in the file, return as a list of strings.
	 * @throws IOException if we failed to read the file for some reason.
	 */
	public static List<String> readAsList(File file) throws IOException
	{
		String dataFromFile = read(file);
		if (StringExtras.hasContent(dataFromFile))
		{
			return StringExtras.split(dataFromFile, StringExtras.LINE_SEPARATOR);
		}
		return Collections.emptyList();
	}

	/**
	 * Checks if a host can be connected to. NOTE: This method
	 * will swallow any IO exceptions and return false.
	 *
	 * @param host The host to connect to.
	 * @param port The port to connect to.
	 * @return true if a connection could be established, false otherwise.
	 */
	public static boolean isHostReachable(String host,int port)
	{
		Socket testSocket = null;
		try
		{
			//noinspection SocketOpenedButNotSafelyClosed
			testSocket = new Socket(host, port);
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
		finally
		{
			IO.closeSilently(testSocket);
		}
	}


	public static List<NetworkInterface> getNetworkInterfaces() throws SocketException
	{
		Enumeration<NetworkInterface> e = s_provider.getNetworkInterfaces();
		return e == null ? Collections.<NetworkInterface>emptyList() : Collections.list(e);
	}

	private static Map<String, InetAddress> collectAllIp(boolean preferIPv6) throws SocketException
	{
		Map<String, InetAddress> map = new HashMap<String, InetAddress>();
		for (NetworkInterface networkInterface : getNetworkInterfaces())
		{
			Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
			while (addresses.hasMoreElements())
			{
				InetAddress address = addresses.nextElement();
				if ((address instanceof Inet4Address && !preferIPv6)
				    || (address instanceof Inet6Address && preferIPv6)
				    || !map.containsKey(networkInterface.getDisplayName()))
				{
					map.put(networkInterface.getDisplayName(), address);
				}
			}
		}
		return map;
	}

	private static boolean isLocalAddress(InetAddress address)
	{
		return address.isAnyLocalAddress() || address.isLoopbackAddress()
		       || address.isLinkLocalAddress() || address.isSiteLocalAddress();
	}

	private static boolean isPreferredInterface(String interfaceName)
	{
		return interfaceName.startsWith("e");
	}

	private static InetAddress getBestNonLocalAddress(Map<String, InetAddress> addresses)
	{
		List<InetAddress> candidates = new ArrayList<InetAddress>();
		for (String interfaceName : new TreeSet<String>(addresses.keySet()))
		{
			InetAddress address = addresses.get(interfaceName);
			if (isLocalAddress(address)) continue;
			if (isPreferredInterface(interfaceName))
			{
				return address;
			}
			candidates.add(address);
		}
		return candidates.size() > 0 ? candidates.get(0) : null;
	}

	private static InetAddress getBestSiteLocalAddress(Map<String, InetAddress> addresses)
	{
		List<InetAddress> candidates = new ArrayList<InetAddress>();
		for (String interfaceName : new TreeSet<String>(addresses.keySet()))
		{
			InetAddress address = addresses.get(interfaceName);
			if (!address.isSiteLocalAddress()) continue;
			if (isPreferredInterface(interfaceName))
			{
				return address;
			}
			candidates.add(address);
		}
		return candidates.size() > 0 ? candidates.get(0) : null;
	}

	private static InetAddress getFirstLinkLocalAddress(Map<String, InetAddress> addresses)
	{
		for (String interfaceName : new TreeSet<String>(addresses.keySet()))
		{
			InetAddress address = addresses.get(interfaceName);
			if (address.isLinkLocalAddress())
			{
				return address;
			}
		}
		return null;
	}

	protected static InetAddress getBestIpGuess(Map<String, InetAddress> addresses) throws UnknownHostException
	{
		InetAddress bestNonLocal = getBestNonLocalAddress(addresses);
		if (bestNonLocal != null) return bestNonLocal;
		InetAddress bestSiteLocal = getBestSiteLocalAddress(addresses);
		if (bestSiteLocal != null) return bestSiteLocal;
		InetAddress firstLinkLocal = getFirstLinkLocalAddress(addresses);
		return firstLinkLocal == null ? InetAddress.getLocalHost() : firstLinkLocal;
	}
	/**
	 * Returns the ip of first en* interface for this machine, if not available,
	 * InetAddress.getLocalHost() will be used.
	 * <p>
	 * If any exceptions are thrown then method will default
	 * to 127.0.0.1.
	 *
	 * @return The ip or 127.0.0.1 if we fail to extract the ip.
	 */
	public static String getIP()
	{
		try
		{
			return getBestIpGuess(collectAllIp(false)).getHostAddress();
		}
		catch (IOException e)
		{
			return "127.0.0.1";
		}

	}

	static void setNetworkInterfaceProvider(NetworkInterfaceProvider provider)
	{
		s_provider = provider == null ? new DefaultNetworkInterfaceProvider() : provider;
	}

	private static class DefaultNetworkInterfaceProvider implements NetworkInterfaceProvider
	{
		public Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException
		{
			return NetworkInterface.getNetworkInterfaces();
		}
	}

}
