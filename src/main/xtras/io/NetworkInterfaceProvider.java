package xtras.io;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Interface to enable proper testing of IO getIP().
 *
 * @author Christoffer Lerno
 */
interface NetworkInterfaceProvider
{
	/**
	 * Get all network interfaces available.
	 *
	 * @return an enumeration with the network interfaces.
	 * @throws SocketException if there was an exception retrieving the enumeration.
	 */
	Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException;
}
