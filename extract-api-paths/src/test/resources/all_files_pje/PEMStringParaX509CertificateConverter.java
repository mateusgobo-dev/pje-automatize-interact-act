/**
 * PEMStringParaX509CertificateConverter.java.
 *
 * Data de criação: 05/11/2014
 */
package br.com.infox.core.certificado.converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;

import br.com.infox.core.certificado.CertificadoException;
import java.security.cert.CertificateException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;

/**
 * Conversor de um certificado no formato PEM para o objeto Certificate.
 * 
 * @author Adriano Pamplona
 */
public class PEMStringParaX509CertificateConverter extends
		CertificadoConverterAbstrato<String, X509Certificate[]> {
    
        private JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter();

        public PEMStringParaX509CertificateConverter() {
                certificateConverter.setProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
	
        @Override
	public X509Certificate[] converter(String string) throws CertificadoException {
		X509Certificate[] resultado = null;

		if (isNotVazio(string)) {
			string = corrigirPEMString(string);
			Security.addProvider(new BouncyCastleProvider());
			
			ByteArrayInputStream bais = null;
			InputStreamReader isr = null;
			PEMParser reader = null;

			try {
				byte[] bytes = string.getBytes();

				bais = new ByteArrayInputStream(bytes);
				isr = new InputStreamReader(bais);
				reader = new PEMParser(isr);
				List<Certificate> cadeia = carregarCadeiaCertificados(reader);
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
	
	/* [PJEII-23563]
	 * 
	 * Método para corrigir a quebra de linha da cadeia de caracteres de certificados tipo PEM
	 * Este método também resolve o problema do SOAPUI que não envia a cadeia de caracteres com 
	 * quebra de linha
	 */
	private String corrigirPEMString(String string){
	    final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----"; 
	    final String END_CERTIFICATE = "-----END CERTIFICATE-----";
	    final String NEW_LINE = System.getProperty("line.separator");   
	    
		if(!string.contains(BEGIN_CERTIFICATE+NEW_LINE)){
			if(string.contains(BEGIN_CERTIFICATE)){
				string = string.replaceAll(BEGIN_CERTIFICATE, BEGIN_CERTIFICATE+NEW_LINE);	
			}else{
				string = BEGIN_CERTIFICATE+NEW_LINE+string;
			}
		}

		if(!string.contains(NEW_LINE+END_CERTIFICATE)){
			if(string.contains(END_CERTIFICATE)){
				string = string.replaceAll(END_CERTIFICATE, NEW_LINE+END_CERTIFICATE);							
			}else{
				string = string+NEW_LINE+END_CERTIFICATE;
			}
		}
		
		return string;
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
