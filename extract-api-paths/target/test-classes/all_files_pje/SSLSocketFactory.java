package br.com.infox.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

public class SSLSocketFactory extends javax.net.ssl.SSLSocketFactory {

	private javax.net.ssl.SSLSocketFactory factory;

	public SSLSocketFactory() {
		try {
			SSLContext sslcontext = SSLContext.getInstance("TLS");
			sslcontext.init(null, // No KeyManager required
					new TrustedManager[] { new TrustedManager() }, new java.security.SecureRandom());
			factory = sslcontext.getSocketFactory();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static SocketFactory getDefault() {
		return new SSLSocketFactory();
	}

	@Override
	public Socket createSocket(Socket socket, String s, int i, boolean flag) throws IOException {
		return factory.createSocket(socket, s, i, flag);
	}

	@Override
	public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1, int j) throws IOException {
		return factory.createSocket(inaddr, i, inaddr1, j);
	}

	@Override
	public Socket createSocket(InetAddress inaddr, int i) throws IOException {
		return factory.createSocket(inaddr, i);
	}

	@Override
	public Socket createSocket(String s, int i, InetAddress inaddr, int j) throws IOException {
		return factory.createSocket(s, i, inaddr, j);
	}

	@Override
	public Socket createSocket(String s, int i) throws IOException {
		return factory.createSocket(s, i);
	}

	@Override
	public Socket createSocket() throws IOException {
		return factory.createSocket();
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return factory.getSupportedCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return factory.getSupportedCipherSuites();
	}

}