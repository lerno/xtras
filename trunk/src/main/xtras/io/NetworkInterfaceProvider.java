package xtras.io;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/** @author Christoffer Lerno */
public interface NetworkInterfaceProvider
{
	Enumeration<NetworkInterface> getNetworkInterfaces() throws SocketException;
}
