/**
 * AssinadorDocumentoTest.java.
 *
 * Data de criação: 03/11/2014
 */
package br.jus.cnj.pje.intercomunicacao.v222.servico;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.core.certificado.ValidaDocumento;
import br.com.infox.core.certificado.util.CodificacaoCertificado;
import br.com.infox.core.certificado.util.DigitalSignatureUtils;
import br.jus.cnj.certificado.Signer;
import br.jus.cnj.certificado.Signer.SignatureAlgorithm;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.intercomunicacao.util.cms.Envelope;
import br.jus.cnj.pje.signerapplet.keystore.ProviderUtil;
import br.jus.pje.nucleo.util.Crypto;

/**
 * Classe responsável pela assinatura de um documento para que seja usado no IntercomunicacaoTest.
 * A assinatura compreende na geração da string de assinatura e a string do certificado para que
 * seja enviado via XML.
 * 
 * @author Adriano Pamplona
 */
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
public class AssinadorDocumentoTest {

	private static final String PATH_DOCUMENTO = "teste-documento.html";
	private static final String PATH_CERTIFICADO_PEM = "certificado-adriano.pem";
	private static final String PATH_CERTIFICADO_BASE64 = "certificado-adriano.base64";
	private static final String PATH_CADEIA_CERTIFICADO_PEM = "certificado-cadeia-adriano.pem";
	private static final SignatureAlgorithm ALGORITMO_ASSINATURA = SignatureAlgorithm.SHA256withRSA;
	
	/**
	 * @throws Exception
	 */
	@Test
	public void test1_VerificacaoAssinaturaFormatoPEM() throws Exception {
		InputStream is = AssinadorDocumentoTest.class.getResourceAsStream(PATH_CERTIFICADO_PEM);
		String stringCertificadoCadeiaPEM = IOUtils.toString(is);
		printCadeiaPEM(stringCertificadoCadeiaPEM);
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test2_VerificacaoAssinaturaFormatoBase64() throws Exception {
		InputStream is = AssinadorDocumentoTest.class.getResourceAsStream(PATH_CERTIFICADO_BASE64);
		String stringCertificadoCadeiaPEM = IOUtils.toString(is);
		printCadeiaBase64(stringCertificadoCadeiaPEM);
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void test3_AssinadorToken() throws Exception {
		InputStream is = AssinadorDocumentoTest.class.getResourceAsStream(PATH_DOCUMENTO);
		byte[] documento = ProjetoUtil.converterParaBytes(is);
		
		KeyStore ks = obterKeyStore();
		String alias = obterAlias(ks);
		PrivateKey privateKey = obterPrivateKey(ks, alias);
		Certificate certificate = obterCertificate(ks, alias);
		PublicKey publicKey = obterPublicKey(certificate);
		
		byte[] assinatura = assinarDocumento(privateKey, documento);
		String assinaturaBase64 = Base64.encodeBase64String(assinatura);
		String cadeiaBase64 = obterCadeiaBase64(certificate);
		String cadeiaPEM = obterCadeiaPEM(certificate);
		String hashSha256Documento = Crypto.encodeSHA256(documento);
		String hashMd5Documento = Crypto.encodeMD5(documento);
		
		System.out.println("\nPEM -----------------------------------------------------------------");
		System.out.println(cadeiaPEM);
		System.out.println("\nPEM PARA ENVIO NO SOUPUI (substitui \\n por &#10;) -------------------");
		String cadeiaPEMparaXML = cadeiaPEM.replace("\n\r", "&#10;");
		cadeiaPEMparaXML = cadeiaPEMparaXML.replace("\n", "&#10;");
		cadeiaPEMparaXML = cadeiaPEMparaXML.replace("\r", "&#10;");
		System.out.println(cadeiaPEMparaXML);
		System.out.println("\nAssinatura ----------------------------------------------------------");
		System.out.println(assinaturaBase64);
		System.out.println("\nCadeia de certificado(s) --------------------------------------------");
		System.out.println(cadeiaBase64);
		System.out.println("\nHash SHA256 do documento --------------------------------------------");
		System.out.println(hashSha256Documento);
		System.out.println("\nHash MD5 do documento -----------------------------------------------");
		System.out.println(hashMd5Documento);
		System.out.println("\nValidação da assinatura ---------------------------------------------");
		new ValidaDocumento(documento, cadeiaBase64, assinaturaBase64, CodificacaoCertificado.PKI_PATH.getValor());
		new ValidaDocumento(documento, cadeiaPEM, assinaturaBase64, CodificacaoCertificado.PEM.getValor());
		
		Certificate[] certs = SigningUtilities.getCertChain(cadeiaBase64);
		System.out.println(certs);
		byte[] sign = SigningUtilities.base64Decode(assinaturaBase64);
		boolean verificado = Signer.verify(publicKey, ALGORITMO_ASSINATURA, documento, sign);
		System.out.println("Assinatura: "+ (verificado ? "OK" : "NOK"));
	}
	
	@Test 
	@Ignore
	public void test4_ValidarAssinatura() throws Exception {
		String assinaturaBase64 = "DpyKxDkIXkEiYXejlJRR9zLSagXc2QZeoQcUZmDt6PFnSXD1YdXNDBz9rZvyIqrW5h7JndiQysXIeolz36iCcNrnlzDb4LcKf6KldgNSreVLLMENtwqMlzbZ7MEDo9xoGuDiqRGa6WAVkIMaJgzzcMvVEK90WqxWL4XGmeemtRNKS8iDDdBCVUsFC9vw7ofrYd7QMCPjkLtk060NwMsBVzG+FmkYh+PZh/+d/i9GxPuZZhT++dXt+dsbXNdWqTExFrpvvgovLeQ+pMjO/cblEaiioII8VT1uz1XInJQry2vUh+d1G5/RgVGzaRIEPusnqkByqA30ahM7+j9jFIqbEA==";
		String certificado = IOUtils.toString(AssinadorDocumentoTest.class.getResourceAsStream(PATH_CERTIFICADO_BASE64));
		SignatureAlgorithm algoritmo = SignatureAlgorithm.SHA256withRSA;

		InputStream is = AssinadorDocumentoTest.class.getResourceAsStream(PATH_DOCUMENTO);
		byte[] documento = ProjetoUtil.converterParaBytes(is);
		byte[] assinatura = SigningUtilities.base64Decode(assinaturaBase64);
		
		//PEM
//		X509Certificate[] certs = DigitalSignatureUtils.loadCertPath(certificado, CodificacaoCertificado.PEM.getValor());
//		Certificate cert = certs[0];
		
		//BASE64
		X509Certificate[] certs = DigitalSignatureUtils.loadCertPath(certificado, CodificacaoCertificado.PKI_PATH.getValor());
		Certificate cert = certs[0];
		
		boolean verificado = Signer.verify(cert.getPublicKey(), algoritmo, documento, assinatura);
		System.out.println("Assinatura: "+ (verificado ? "OK" : "NOK"));
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void test5_AssinadorCadeia() throws Exception {
		InputStream is = AssinadorDocumentoTest.class.getResourceAsStream(PATH_DOCUMENTO);
		byte[] documento = ProjetoUtil.converterParaBytes(is);
		
		KeyStore ks = obterKeyStore();
		String alias = obterAlias(ks);
		PrivateKey privateKey = obterPrivateKey(ks, alias);
		Certificate certificate = obterCertificate(ks, alias);
		//PublicKey publicKey = obterPublicKey(certificate);
		
		byte[] assinatura = assinarDocumento(privateKey, documento);
		String assinaturaBase64 = Base64.encodeBase64String(assinatura);
		String cadeiaBase64 = obterCadeiaBase64(certificate);
		String cadeiaPEM = obterCadeiaPEM(certificate);
		String hashSha256Documento = Crypto.encodeSHA256(documento);
		String hashMd5Documento = Crypto.encodeMD5(documento);
		
		System.out.println("\nPEM -----------------------------------------------------------------");
		System.out.println(cadeiaPEM);
		System.out.println("\nPEM PARA ENVIO NO SOUPUI (substitui \\n por &#10;) -------------------");
		String cadeiaPEMparaXML = cadeiaPEM.replace("\n\r", "&#10;");
		cadeiaPEMparaXML = cadeiaPEMparaXML.replace("\n", "&#10;");
		cadeiaPEMparaXML = cadeiaPEMparaXML.replace("\r", "&#10;");
		System.out.println(cadeiaPEMparaXML);
		System.out.println("\nAssinatura ----------------------------------------------------------");
		System.out.println(assinaturaBase64);
		System.out.println("\nCadeia de certificado(s) --------------------------------------------");
		System.out.println(cadeiaBase64);
		System.out.println("\nHash SHA256 do documento --------------------------------------------");
		System.out.println(hashSha256Documento);
		System.out.println("\nHash MD5 do documento -----------------------------------------------");
		System.out.println(hashMd5Documento);
		System.out.println("\nValidação da assinatura ---------------------------------------------");
		new ValidaDocumento(documento, cadeiaBase64, assinaturaBase64, CodificacaoCertificado.PKI_PATH.getValor());
		new ValidaDocumento(documento, cadeiaPEM, assinaturaBase64, CodificacaoCertificado.PEM.getValor());
		
		String certificado = IOUtils.toString(AssinadorDocumentoTest.class.getResourceAsStream(PATH_CADEIA_CERTIFICADO_PEM));
		CertificateFactory cf = CertificateFactory.getInstance(DigitalSignatureUtils.X509_CERTIFICATE_TYPE);
		Collection<? extends Certificate> certs = cf.generateCertificates(new ByteArrayInputStream(certificado.getBytes()));
		Certificate cadeiaCertificado = certs.iterator().next();
		
		boolean verificado = Signer.verify(cadeiaCertificado.getPublicKey(), ALGORITMO_ASSINATURA, documento, assinatura);
		System.out.println("Assinatura: "+ (verificado ? "OK" : "NOK"));
	}
	
	@Test
	public void test6_ConverterCadeiaPEMParaEnvioXML() throws Exception {
		String certificado = IOUtils.toString(AssinadorDocumentoTest.class.getResourceAsStream(PATH_CADEIA_CERTIFICADO_PEM));
		String cadeiaPEMparaXML = certificado.replace("\n\r", "&#10;");
		cadeiaPEMparaXML = certificado.replace("\r\n", "&#10;");
		cadeiaPEMparaXML = cadeiaPEMparaXML.replace("\n", "&#10;");
		cadeiaPEMparaXML = cadeiaPEMparaXML.replace("\r", "&#10;");
		System.out.println(cadeiaPEMparaXML);
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void test7_AssinadorTokenComCadeiaCompleta() throws Exception {
		InputStream is = AssinadorDocumentoTest.class.getResourceAsStream(PATH_DOCUMENTO);
		byte[] documento = ProjetoUtil.converterParaBytes(is);
		
		KeyStore ks = obterKeyStore();
		String alias = obterAlias(ks);
		PrivateKey privateKey = obterPrivateKey(ks, alias);
		Certificate[] certificates = ks.getCertificateChain(alias);
		PublicKey publicKey = obterPublicKey(certificates[0]);
		
		byte[] assinatura = assinarDocumento(privateKey, documento);
		String assinaturaBase64 = Base64.encodeBase64String(assinatura);
		//
		List<Certificate> certList = Arrays.asList(certificates);
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		CertPath certPath = certFactory.generateCertPath(certList);
		byte[] encodedCertChain = certPath.getEncoded();
		
		//return Base64.encodeBase64String(encodedCertChain);
		String cadeiaBase64 = new Base64(64).encodeToString(encodedCertChain);
		//
		//
		StringBuilder resultado = new StringBuilder();
		for (Certificate certificate : certificates) {
			byte[] bytes = certificate.getEncoded();
			
			resultado.append("-----BEGIN CERTIFICATE-----");
			resultado.append("\n");
			resultado.append(new Base64(64).encodeToString(bytes));
			resultado.append("-----END CERTIFICATE-----");
			resultado.append("\n");
		}
		resultado.deleteCharAt(resultado.length()-1);
		String cadeiaPEM = resultado.toString();
		//
		String hashSha256Documento = Crypto.encodeSHA256(documento);
		String hashMd5Documento = Crypto.encodeMD5(documento);
		
		System.out.println("\nPEM -----------------------------------------------------------------");
		System.out.println(cadeiaPEM);
		System.out.println("\nPEM PARA ENVIO NO SOUPUI (substitui \\n por &#10;) -------------------");
		String cadeiaPEMparaXML = cadeiaPEM.replace("\n\r", "&#10;");
		cadeiaPEMparaXML = cadeiaPEMparaXML.replace("\n", "&#10;");
		cadeiaPEMparaXML = cadeiaPEMparaXML.replace("\r", "&#10;");
		System.out.println(cadeiaPEMparaXML);
		System.out.println("\nAssinatura ----------------------------------------------------------");
		System.out.println(assinaturaBase64);
		System.out.println("\nCadeia de certificado(s) --------------------------------------------");
		System.out.println(cadeiaBase64);
		System.out.println("\nHash SHA256 do documento --------------------------------------------");
		System.out.println(hashSha256Documento);
		System.out.println("\nHash MD5 do documento -----------------------------------------------");
		System.out.println(hashMd5Documento);
		System.out.println("\nValidação da assinatura ---------------------------------------------");
		new ValidaDocumento(documento, cadeiaBase64, assinaturaBase64, CodificacaoCertificado.PKI_PATH.getValor());
		new ValidaDocumento(documento, cadeiaPEM, assinaturaBase64, CodificacaoCertificado.PEM.getValor());
		
		Certificate[] certs = SigningUtilities.getCertChain(cadeiaBase64);
		System.out.println(certs);
		byte[] sign = SigningUtilities.base64Decode(assinaturaBase64);
		boolean verificado = Signer.verify(publicKey, ALGORITMO_ASSINATURA, documento, sign);
		System.out.println("Assinatura: "+ (verificado ? "OK" : "NOK"));
	}
	
	/**
	 * Método responsável pela validação da assinatura do documento P7S.
	 * Obs: Método em elaboração.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test8_ValidacaoP7S() throws Exception {
		InputStream is = AssinadorDocumentoTest.class.getResourceAsStream("teste-documento.p7s");
		byte[] documento = ProjetoUtil.converterParaBytes(is);
		
		Envelope envelope = new Envelope(documento);
		//documento = envelope.getBytesArquivoOriginal();

		X509Certificate[] cadeia = envelope.getAssinaturas().keySet().iterator().next();
		byte[] assinatura = envelope.getAssinaturas().get(cadeia);
		
		PublicKey publicKey = cadeia[0].getPublicKey();
		//boolean verificado = Signer.verify(publicKey, SignatureAlgorithm.SHA1withRSA, documento, assinatura);
		
		Provider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
		Signature sigVerifier = Signature.getInstance("SHA1withRSA", "BC");
		sigVerifier.initVerify(publicKey);
		sigVerifier.update(documento);
		Boolean verificado = sigVerifier.verify(assinatura);
		System.out.println("Assinatura: "+ (verificado ? "OK" : "NOK"));
	}
	
	/**
	 * Retorna a cadeia de certificados no formato base64.
	 * 
	 * @param certificate
	 * @return String dos certificados no formato base64.
	 * @throws Exception
	 */
	private String obterCadeiaBase64(Certificate certificate) throws Exception {
		List<Certificate> certList = Arrays.asList(certificate);
		CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
		CertPath certPath = certFactory.generateCertPath(certList);
		byte[] encodedCertChain = certPath.getEncoded();
		
		//return Base64.encodeBase64String(encodedCertChain);
		return new Base64(64).encodeToString(encodedCertChain);
	}

	/**
	 * Retorna o certificado no formato PEM.
	 * 
	 * @param certificate
	 * @return String PEM
	 * @throws Exception 
	 */
	private String obterCadeiaPEM(Certificate certificate) throws Exception {
		byte[] bytes = certificate.getEncoded();

		StringBuilder resultado = new StringBuilder();
		resultado.append("-----BEGIN CERTIFICATE-----");
		resultado.append("\n");
		resultado.append(new Base64(64).encodeToString(bytes));
		resultado.append("-----END CERTIFICATE-----");
		
		return resultado.toString();
	}

	/**
	 * Assina o documento e retorna o array de bytes da assinatura.
	 * 
	 * @param privateKey
	 * @param documento
	 * @return assinatura
	 * @throws Exception
	 */
	private byte[] assinarDocumento(PrivateKey privateKey, byte[] documento) throws Exception {
		byte[][] assinatura = Signer.sign(privateKey, ALGORITMO_ASSINATURA, documento);
		return assinatura[0];
	}

	/**
	 * Retorna a chave pública do certificado.
	 * 
	 * @param certificate
	 * @return Chave pública do certificado.
	 */
	private PublicKey obterPublicKey(Certificate certificate) {
		return certificate.getPublicKey();
	}

	/**
	 * Retorna o certificado do respectivo alias no keystore.
	 * 
	 * @param ks
	 * @param alias
	 * @return Certificado.
	 * @throws Exception
	 */
	private Certificate obterCertificate(KeyStore ks, String alias) throws Exception {
		Certificate[] certChain = ks.getCertificateChain(alias);
		return certChain[0];
	}
	
	/**
	 * Retorna a chave privada do certificado.
	 * 
	 * @param ks
	 * @param alias
	 * @return Chave privada.
	 * @throws Exception
	 */
	private PrivateKey obterPrivateKey(KeyStore ks, String alias) throws Exception {
		return (PrivateKey) ks.getKey(alias, null);
	}
	
	/**
	 * Retorna o primeiro alias do keystore.
	 * 
	 * @param ks
	 * @return Alias.
	 * @throws Exception
	 */
	private String obterAlias(KeyStore ks) throws Exception {
		List<String> aliasesList = Collections.list(ks.aliases());
		return aliasesList.get(0);
	}
	
	/**
	 * Retorna o keystore do token.
	 * 
	 * @return Keystore.
	 * @throws Exception
	 */
	private KeyStore obterKeyStore() throws Exception {
		InputStream providers = ClassLoader.getSystemResourceAsStream("br/jus/cnj/pje/intercomunicacao/servico/providers.csv");
		
		//Se ocorrer um erro java.lang.NoClassDefFoundError: sun/security/pkcs11/SunPKCS11 é
		//necessário colocar o jar sunpkcs11.jar no classpath, o arquivo em questão não está 
		//disponível na versão de 64 bits do java.
		ProviderUtil provider = new ProviderUtil(providers, "ISO-8859-1", null);
		return provider.getKeyStoreMap().values().iterator().next().getKeyStore();
	}

	/**
	 * Recebe cadeia no formato pem. cadeia-eduardo.txt
	 * @param arquivo
	 * @throws Exception
	 */
	private static void printCadeiaPEM(String certificado) throws Exception {
		byte[] bytes = certificado.getBytes();
		
		CertificateFactory cf = CertificateFactory.getInstance(DigitalSignatureUtils.X509_CERTIFICATE_TYPE);
		Collection<? extends Certificate> certs = cf.generateCertificates(new ByteArrayInputStream(bytes));
		
		System.out.println("Total: "+ certs.size());
		for (Certificate cert : certs) {
			System.out.println(" - "+ ((X509Certificate)cert).getSubjectDN());
		}
		System.out.println("--------------------------------------------------------");
	}
	
	/**
	 * Recebe cadeia no formato pem. cadeia-eduardo.txt
	 * @param arquivo
	 * @throws Exception
	 */
	private static void printCadeiaBase64(String certificado) throws Exception {
		byte[] bytesDecoded = Base64.decodeBase64(certificado.getBytes());
		
		CertificateFactory cf = CertificateFactory.getInstance(DigitalSignatureUtils.X509_CERTIFICATE_TYPE);
		CertPath certPath = cf.generateCertPath(new ByteArrayInputStream(bytesDecoded), DigitalSignatureUtils.CERT_CHAIN_ENCODING);
		Collection<? extends Certificate> certs = certPath.getCertificates();
		
		System.out.println("Total: "+ certs.size());
		for (Certificate cert : certs) {
			System.out.println(" - "+ ((X509Certificate)cert).getSubjectDN());
		}
		System.out.println("--------------------------------------------------------");
	}
}
