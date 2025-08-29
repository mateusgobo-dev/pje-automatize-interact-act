package br.jus.cnj.pje.intercomunicacao.util.cms;

import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.x509.Time;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.util.Store;

/**
 * Recupera informações de arquivos assinados no padrão CMS/pkcs#7
 * 
 * @author rodrigoar
 * 
 */
public class Envelope {

	private static final String OID_SIGNING_TIME = "1.2.840.113549.1.9.5";

	private static final String OID_MESSAGE_DIGEST = "1.2.840.113549.1.9.4";

	private static final String OID_CAPICOM_DOCUMENT_NAME = "1.3.6.1.4.1.311.88.2.1";

	private byte[] bytesArquivoOriginal;
	private Map<X509Certificate[], byte[]> assinaturas = 
			new HashMap<X509Certificate[], byte[]>(0);
	private String nomeArquivo;
	private byte[] hash;
	private String algoritmoHash;
	private Date dataAssinatura;

	/**
	 * verifica se os dados estão no padrão CMS/pkcs#7
	 * @param dados
	 * @return
	 */
	public static boolean isCMSSigned(byte[] dados){
		try{
			new Envelope(dados, false);
			return true;
		}
		catch(Exception e){
			return false;
		}
		
	}

	@SuppressWarnings("unchecked")
	private Envelope(byte[] dados, boolean init) throws Exception{
		CMSSignedData sd = new CMSSignedData(dados);

		if(init){
		// Extrair conteúdo original (não assinado)
		CMSProcessableByteArray cpb = (CMSProcessableByteArray) sd
				.getSignedContent();
		bytesArquivoOriginal = (byte[]) cpb.getContent();

		Store<X509CertificateHolder> certStore = sd.getCertificates();
		
		List<SignerInformation> siList = (List<SignerInformation>) sd
				.getSignerInfos().getSigners();
	
		//Extrair as assinaturas
		for (SignerInformation si : siList) {
			byte[] assinatura = si.getSignature();

			Collection<X509CertificateHolder> certHolders = certStore
					.getMatches(si.getSID());

			X509Certificate[] certs = new X509Certificate[certHolders.size()];
			JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
			int i = 0;
			for (X509CertificateHolder cert : certHolders) {
				certs[i++] = converter.getCertificate(cert);
			}

			assinaturas.put(certs, assinatura);

			Map<ASN1ObjectIdentifier, Attribute> signedAttributesMap = si
					.getSignedAttributes().toHashtable();
			
			//Extrair atributos especificos
			for (ASN1ObjectIdentifier oid : signedAttributesMap.keySet()) {
				Object value = signedAttributesMap.get(oid).getAttrValues()
						.getObjectAt(0).toASN1Primitive();

				//nome do documento
				if (oid.getId().equals(OID_CAPICOM_DOCUMENT_NAME)) {
					nomeArquivo = new String(ASN1OctetString.getInstance(value)
							.getOctets(), "UnicodeLittleUnmarked");
				}

				//hash
				if (oid.getId().equals(OID_MESSAGE_DIGEST)) {
					hash = ASN1OctetString.getInstance(value).getOctets();
					algoritmoHash = getAlgorimoHash(si.getDigestAlgOID());
				}

				//data da assinatura
				if (oid.getId().equals(OID_SIGNING_TIME)) {
					dataAssinatura = Time.getInstance(value).getDate();
				}
			}
		}		
		}
	}
	

	public Envelope(byte[] dados) throws Exception {
		this(dados, true);
	}
	
	
	private String getAlgorimoHash(String oid){
		
		if(oid != null){
		
			if(oid.equals(CMSSignedGenerator.DIGEST_GOST3411)){
				return "GOST";
			}
			
			if(oid.equals(CMSSignedGenerator.DIGEST_MD5)){
				return "MD5";
			}
			
			if(oid.equals(CMSSignedGenerator.DIGEST_RIPEMD128)){
				return "RIPEMD-128";
			}
			
			if(oid.equals(CMSSignedGenerator.DIGEST_RIPEMD160)){
				return "RIPEMD-160";
			}
			
			if(oid.equals(CMSSignedGenerator.DIGEST_RIPEMD256)){
				return "RIPEMD-256";
			}
			
			if(oid.equals(CMSSignedGenerator.DIGEST_SHA1)){
				return "SHA-1";
			}
			
			if(oid.equals(CMSSignedGenerator.DIGEST_SHA224)){
				return "SHA-224";
			}
			
			if(oid.equals(CMSSignedGenerator.DIGEST_SHA256)){
				return "SHA-256";
			}
			
			if(oid.equals(CMSSignedGenerator.DIGEST_SHA384)){
				return "SHA-384";
			}
			
			if(oid.equals(CMSSignedGenerator.DIGEST_SHA512)){
				return "SHA-512";
			}
		}
		
		return null;
	}

	/**
	 * Recupera o conteúdo original do arquivo em bytes
	 * @return
	 */
	public byte[] getBytesArquivoOriginal() {
		return bytesArquivoOriginal;
	}

	/**
	 * Recupera as assinaturas
	 * @return
	 */
	public Map<X509Certificate[], byte[]> getAssinaturas() {
		return assinaturas;
	}

	/**
	 * Recupera o nome do arquivo
	 * @return
	 */
	public String getNomeArquivo() {
		return nomeArquivo;
	}

	/**
	 * Recupera o hash do arquivo
	 * @return
	 */
	public byte[] getHash() {
		return hash;
	}

	/**
	 * Recupera a data de assinatura do arquivo 
	 * @return
	 */
	public Date getDataAssinatura() {
		return dataAssinatura;
	}
	
	/**
	 * Recupera o nome do algoritmo utilizado no hash
	 * @return
	 */
	public String getAlgoritmoHash() {
		return algoritmoHash;
	}
	
	

}
