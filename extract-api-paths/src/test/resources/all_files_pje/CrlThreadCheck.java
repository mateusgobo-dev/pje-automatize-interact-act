package br.com.infox.core.certificado.crl;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.core.certificado.CertificadoException;
import br.com.itx.component.MeasureTime;

public class CrlThreadCheck {

	private static final LogProvider log = Logging.getLogProvider(CrlThreadCheck.class);

	private static final int SLEEP_LOCK = 200;
	private static final int SLEEP_CHECK_UPDATE = 5 * 1000 * 60;
	private static final int TIMEOUT = 2 * 1000;

	private Thread thread;
	private String url;
	private boolean locked;
	private X509CRL x509crl;
	private Map<BigInteger, X509CRLEntry> mapRevogados;

	private CertificateFactory certificatefactory;

	public CrlThreadCheck(String url) throws CertificadoException {
		try {
			certificatefactory = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			throw new CertificadoException("Erro ao obter o factory X.509", e);
		}
		this.url = url;
		locked = false;
		startUpdateThread();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CrlThreadCheck other = (CrlThreadCheck) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	public synchronized boolean isCertificadoRevogado(BigInteger serial) {
		MeasureTime mt = new MeasureTime(true);
		while (locked) {
			try {
				Thread.sleep(SLEEP_LOCK);
				if (mt.getTime() > TIMEOUT) {
					// TODO criar uma exeção pra isso
					throw new RuntimeException("Time out");
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return mapRevogados.get(serial) != null;
	}

	private void startUpdateThread() {
		// JobAtualizar executer = new JobAtualizar();
		// thread = new Thread(executer);
		// thread.setName("thread_" + url);
		// executer.setThread(thread);
		// thread.start();
	}

	protected boolean atualizarX509crl() {
		locked = true;
		File fileTmp = null;
		try {

			fileTmp = downloadTempFile();
			InputStream inputStream = new FileInputStream(fileTmp);

			System.out.println("Arquivo: " + fileTmp.getAbsoluteFile());

			X509CRL x509crlTemp = (X509CRL) certificatefactory.generateCRL(inputStream);
			Set<? extends X509CRLEntry> setEntries = x509crlTemp.getRevokedCertificates();
			mapRevogados = new HashMap<BigInteger, X509CRLEntry>();
			if (setEntries != null) {
				for (X509CRLEntry x509crlEntry : setEntries) {
					mapRevogados.put(x509crlEntry.getSerialNumber(), x509crlEntry);
				}
			}
			x509crl = x509crlTemp;

			inputStream.close();
			fileTmp.delete();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log.info("Erro ao atualizar o X509crl: " + e.getMessage());
			if (fileTmp != null) {
				fileTmp.delete();
			}
		} finally {
			locked = false;
		}
		return false;
	}

	private File downloadTempFile() throws Exception {
		InputStream inputStream = getInputStreamUrl(url);
		String fileName = getFileName(url);
		File f = File.createTempFile(fileName, ".tmp");
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f));
		byte[] buf = new byte[1024];
		int byteRead;
		int size = 0;
		while ((byteRead = inputStream.read(buf)) != -1) {
			os.write(buf, 0, byteRead);
			size += byteRead;
		}
		os.close();
		os.flush();
		inputStream.close();
		log.info(MessageFormat.format("O CRL do link ''{0}'' foi baixado para o arquivo ''{1}'' [{2} bytes]", url,
				fileName, size));
		return f;
	}

	public static InputStream getInputStreamUrl(String fileAddress) throws IOException {
		URLConnection URLConn = null;

		// URLConnection class represents a communication link between the
		// application and a URL.
		URL fileUrl = new URL(fileAddress);

		// openConnection method on a URL.
		URLConn = fileUrl.openConnection();
		return URLConn.getInputStream();
	}

	public static void main(String[] args) throws IOException {
		InputStream inputStreamUrl2 = getInputStreamUrl("http://www.infox.com.br/teste.html");
		log.info(inputStreamUrl2);
	}

	private String getFileName(String url) {
		return url.substring(url.lastIndexOf("/") + 1);
	}

	private class JobAtualizar implements Runnable {

		private Thread thread;

		public void setThread(Thread thread) {
			this.thread = thread;
		}

		@Override
		public void run() {

			while (true) {
				if (!locked) {
					log.info("Testando atualização [" + url + "]");
					long now = new Date().getTime();
					long nextUpdate = x509crl != null ? x509crl.getNextUpdate().getTime() : -1;
					long diferenca = 0;
					boolean atualizou = false;
					if (x509crl == null || now > nextUpdate) {
						log.info("Atualizando...");
						atualizou = atualizarX509crl();
						if (atualizou) {
							diferenca = x509crl.getNextUpdate().getTime() - new Date().getTime();
						}
					}
					try {
						diferenca = diferenca > 0 ? diferenca : 0;
						long sleepTime = diferenca + SLEEP_CHECK_UPDATE;
						Date date = new Date(sleepTime);
						log.info("Thread dormindo até: " + date);
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(SLEEP_LOCK);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public class TimeOutException extends Exception {

			private static final long serialVersionUID = 1L;

			public TimeOutException(String message) {
				super(message);
			}

		}

	}
}
