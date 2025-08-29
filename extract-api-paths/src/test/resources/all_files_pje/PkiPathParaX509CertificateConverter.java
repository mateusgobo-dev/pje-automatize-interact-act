/**
 * PkiPathParaX509CertificateConverter.java.
 *
 * Data de criação: 05/11/2014
 */
package br.com.infox.core.certificado.converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;

import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.util.DigitalSignatureUtils;
import java.security.cert.CertificateException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;

/**
 * Conversor de um certificado no formato P7S para o objeto Certificate.
 * 
 * @author Adriano Pamplona
 */
@SuppressWarnings("unchecked")
public class PkiPathParaX509CertificateConverter extends
		CertificadoConverterAbstrato<String, X509Certificate[]> {
    
        private final JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();

        public PkiPathParaX509CertificateConverter() {
                certificateConverter.setProvider(BouncyCastleProvider.PROVIDER_NAME);
        }

	@Override
	public X509Certificate[] converter(String string) throws CertificadoException {
		X509Certificate[] resultado = null;

		if (isNotVazio(string)) {
			Security.addProvider(new BouncyCastleProvider());
			
			ByteArrayInputStream bais = null;
			InputStreamReader isr = null;
			PEMParser reader = null;

			try {
				byte[] bytes = Base64.decodeBase64(string);

				bais = new ByteArrayInputStream(bytes);
				CertificateFactory cf = CertificateFactory.getInstance(DigitalSignatureUtils.X509_CERTIFICATE_TYPE);
				CertPath certPath = cf.generateCertPath(bais, DigitalSignatureUtils.CERT_CHAIN_ENCODING);
				
				List<X509Certificate> cadeia = (List<X509Certificate>) certPath.getCertificates();
				resultado = cadeia.toArray(new X509Certificate[cadeia.size()]);
			} catch (Exception e) {
				throw new CertificadoException(e.getMessage());
			} finally {
				fechar(reader);
				fechar(isr);
				fechar(bais);
			}
		}
		return resultado;
	}

	/**
	 * Carrega a cadeia de certificados.
	 * @param reader
	 * @return Lista da cadeia de certificados.
	 * @throws IOException
	 */
	protected List<Certificate> carregarCadeiaCertificados(PEMParser reader) throws IOException, CertificateException {
		List<Certificate> resultado = new ArrayList<Certificate>();
		
		Object certificado = null;
		while ((certificado = reader.readObject()) != null) {
			if (certificado instanceof X509Certificate) {
				X509Certificate x509 = (X509Certificate) certificado;
				resultado.add(x509);
			} else if (certificado instanceof X509CertificateHolder){
                X509Certificate x509 = certificateConverter.getCertificate((X509CertificateHolder)certificado);
                resultado.add(x509);
            }
		}
		return resultado;
	}

	/**
	 * Fecha o PEMReader passado por parâmetro.
	 * 
	 * @param reader
	 */
	protected void fechar(PEMParser reader) {
		if (isNotNull(reader)) {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Fecha o InputStreamReader passado por parâmetro.
	 * 
	 * @param isr
	 */
	protected void fechar(InputStreamReader isr) {
		if (isNotNull(isr)) {
			try {
				isr.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * Fecha o ByteArrayInputStream passado por parâmetro.
	 * 
	 * @param bais
	 */
	protected void fechar(ByteArrayInputStream bais) {
		if (isNotNull(bais)) {
			try {
				bais.close();
			} catch (IOException e) {
			}
		}
	}
}
