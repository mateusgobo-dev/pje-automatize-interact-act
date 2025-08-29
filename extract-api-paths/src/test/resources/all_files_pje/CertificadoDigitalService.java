/**
 * 
 */
package br.jus.cnj.pje.nucleo.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;

import com.nakov.certificate.CRLVerifier;

import br.com.infox.ibpm.util.GerenciadorAutoridadesCertificadoras;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.certificado.CertificadoICPBrUtil;
import br.jus.cnj.certificado.Signer;
import br.jus.cnj.certificado.Signer.SignatureAlgorithm;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.extensao.AssinadorA1;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.auxiliar.ResultadoAssinatura;
import br.jus.cnj.pje.nucleo.dto.BinarioACs;
import br.jus.csjt.pje.commons.util.Base64;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;

/**
 * @author cristof
 *
 */
@Name("certificadoDigitalService")
@Scope(ScopeType.APPLICATION)
public class CertificadoDigitalService implements Serializable{
	
	private static final long serialVersionUID = -4454286697185047013L;
	
	private static final int[] array = {99, 101, 106, 110, 112};

	private static final Map<String, File> mapaCRLs = new HashMap<String, File>();
	
	private static KeyStore pjeKS;
	
	private static PrivateKey pjePrivateKey;
	
	private static Certificate cert;
	
	@In(create = true, required = false)
	private transient AssinadorA1 assinadorA1;
	
	static{
		try {
			pjeKS =  KeyStore.getInstance("PKCS12");
			InputStream is = CertificadoDigitalService.class.getClassLoader().getResourceAsStream("META-INF/pje.pfx");
			pjeKS.load(is, new char[]{(char)array[4], (char)array[2], (char)array[1], (char)array[0], (char)array[3], (char)array[2], (char)array[4], (char)array[2], (char)array[1]});
			String alias = pjeKS.aliases().nextElement();
			cert = pjeKS.getCertificate(alias);
			pjePrivateKey = (PrivateKey) pjeKS.getKey(alias, new char[]{(char)array[4], (char)array[2], (char)array[1], (char)array[0], (char)array[3], (char)array[2], (char)array[4], (char)array[2], (char)array[1]});
		} catch (KeyStoreException e) {
			e.printStackTrace();
			pjeKS = null;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		}
	}
	
	@Logger
	private Log logger;
	
	@Create
	public void init(){
		atualizaAutoridadesCertificadoras();
	}
	
	@Observer(Eventos.EVENTO_AUTORIDADES_CERTIFICADORAS_ALTERADAS)
	public static void atualizaAutoridadesCertificadoras() {
		GerenciadorAutoridadesCertificadoras gerenciadorAutoridadesCertificadoras = ComponentUtil.getComponent(GerenciadorAutoridadesCertificadoras.class);

		BinarioACs confiaveis = gerenciadorAutoridadesCertificadoras.getConfiaveis();
		BinarioACs intermediarias = gerenciadorAutoridadesCertificadoras.getIntermediarias();
		
		if(confiaveis != null && intermediarias != null) {
			CertificadoICPBrUtil.updateCertificatesSecure(
				confiaveis.getContentBase64(), confiaveis.getSignedChecksum(), confiaveis.getChecksumCodec(), confiaveis.getSignatureAlgorithm(),
				intermediarias.getContentBase64(), intermediarias.getSignedChecksum(), intermediarias.getChecksumCodec(), intermediarias.getSignatureAlgorithm());
		}
	}
	
	private ExecutorService executorService;
	private ExecutorService getExecutorService() {
		if (executorService==null)
			executorService = Executors.newSingleThreadExecutor();
		return executorService;
	}
	
	@Destroy()
	public void onDestroyComponent() {
		if (executorService!=null) {
			executorService.shutdownNow();
			executorService = null;
		}
	}
	
	public Future<Boolean> assinaSistemaAsync(final ProcessoDocumentoBin pdb) {
		FutureTask<Boolean> future = new FutureTask<Boolean>(new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				boolean assinou = false;
				try {
					assinou = fazAssinaSistema(pdb);
				} catch (Exception ex) {
					assinou = false;
					logger.error("Falha ao assinar documento: {0}", ex, pdb.getIdProcessoDocumentoBin());
				}
				return assinou;				
			}
		});
		getExecutorService().execute(future);
		return future;
	}
	
	@BypassInterceptors	
	private static boolean fazAssinaSistema(ProcessoDocumentoBin pdb) throws IOException, InvalidKeyException, CertificateException {
		if ((pjePrivateKey==null) || (cert==null)) 
			return false;
		
		Thread.yield();
		
		byte[] contents = null;
		if(pdb.getModeloDocumento() != null && !pdb.getModeloDocumento().isEmpty()){
			contents = pdb.getModeloDocumento().getBytes();
		}
		else if(pdb.getFile() != null && pdb.getFile().exists()){
			FileInputStream fis = new FileInputStream(pdb.getFile());
			try {
				contents = new byte[fis.available()];
				fis.read(contents);
			} finally {
				fis.close();
			}
		}
		else{
			//logger.warn("Tentativa de assinar documento vazio");
			contents = new byte[]{};
		}

		pdb.setCertChain(SigningUtilities.encodeCertChain(new Certificate[]{cert}));
		if ((contents==null) || (contents.length==0))
			return false;
		
		byte[] assinatura = Signer.sign(pjePrivateKey, SignatureAlgorithm.MD5withRSA, new byte[][]{contents})[0];
		pdb.setSignature(Base64.encodeBytes(assinatura));
		if(pdb.getDataAssinatura() == null)
			pdb.setDataAssinatura(new Date());
		return true;
	}
	
	public ProcessoDocumentoBin assinaSistema(ProcessoDocumentoBin pdb){
		try {
			byte[] contents = null;
			if(!pdb.isBinario() && pdb.getModeloDocumento() != null && !pdb.getModeloDocumento().isEmpty()){
				contents = pdb.getModeloDocumento().getBytes();
			}
			else if(pdb.getFile() != null && pdb.getFile().exists()){
				FileInputStream fis = new FileInputStream(pdb.getFile());
				contents = new byte[fis.available()];
				fis.read(contents);
				fis.close();
			}
			else{
				logger.warn("Tentativa de assinar documento vazio");
				contents = new byte[]{};
			}
		
			
			if(assinadorA1 != null) {
				ResultadoAssinatura assinatura = assinadorA1.assinarHash(pdb.getMd5Documento());
				pdb.setSignature(assinatura.getAssinatura());
				pdb.setCertChain(assinatura.getCadeiaCertificado());
			}
			else {
				byte[] assinatura = Signer.sign(pjePrivateKey, SignatureAlgorithm.MD5withRSA, new byte[][]{contents})[0];
				pdb.setSignature(Base64.encodeBytes(assinatura));
				pdb.setCertChain(SigningUtilities.encodeCertChain(new Certificate[]{cert}));
			}
			
		} catch (InvalidKeyException | CertificateException | IOException | PontoExtensaoException e) {
			logger.error(e);
		} 
		return pdb;
	}
	

	public byte[] assinaSistema (byte[] conteudo){
		byte[] assinatura = null;
		try {
			assinatura = Signer.sign(pjePrivateKey, SignatureAlgorithm.MD5withRSA, conteudo)[0];
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return assinatura;
	}
	
	public String getCertChainSistema() {
		try{
			return SigningUtilities.encodeCertChain(new Certificate[]{cert});
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void validate(X509Certificate cert, Certificate[] certChain, int timeout) throws CertificateException{
		List<String> crlDPs;
		try {
			crlDPs = CRLVerifier.getCrlDistributionPoints(cert);
			for(String crlDP: crlDPs){
				if(!mapaCRLs.containsKey(crlDP)){
					try{
						X509CRL crl = CRLVerifier.downloadCRL(crlDP, timeout);
						if(crl != null){
							File f = File.createTempFile("tempCRL", ".crl" );
							FileOutputStream fos = new FileOutputStream(f);
							fos.write(crl.getEncoded());
							fos.flush();
							fos.close();
							mapaCRLs.put(crlDP, f);
						}
					}catch (Exception e) {
						logger.error("Erro ao tentar baixar o CRL '" + crlDP + "', deve ser liberado o acesso à internet no servidor de aplicação para o funcionamento da assinatura.");
						logger.error(e.getMessage());
					}
				}
			}
		} catch (IOException e) {
			logger.error("Erro ao tentar obter a lista de pontos de distribuição da lista de certificados revogados: {0}", e.getLocalizedMessage());
		}catch (Exception e) {
			logger.error("Erro ao tentar obter a lista de pontos de distribuição da lista de certificados revogados: {0}", e.getLocalizedMessage());
		}
		try {
			boolean valid = CertificadoICPBrUtil.validate(cert, certChain, timeout, mapaCRLs, false);
			if(!valid){
				throw new CertificateException("pje.certificadoDigitalService.error.verificaAssinatura.validarCertificado");
			}
		} catch (Exception e) {
			throw new CertificateException("pje.certificadoDigitalService.error.verificaAssinatura.validarCertificado", e);
		} 
	}

	/**
	 * Metodo responsavel por validar a data de expiracao e habilitacao do certificado digital 
	 * @param x509Certificate O certificado que sera validado
	 * @throws Exception 
	 */
	public void validarDataExpiracaoEhHabilitacaoDoCertificadoDigital(X509Certificate x509Certificate) throws Exception {

		Calendar hoje = Calendar.getInstance();
		
		Date dataValidadeFim = x509Certificate.getNotAfter();
		Date dataValidadeInicio = x509Certificate.getNotBefore();
		
		if (hoje.before(dataValidadeInicio)) {
			throw new Exception("Certificado não esta ativo, será ativado em: "  + DateFormat.getDateInstance().format(dataValidadeInicio));
		}
		else if (hoje.after(dataValidadeFim)) {
			throw new CertificateExpiredException("Certificado expirado em: " + DateFormat.getDateInstance().format(dataValidadeFim));
		}		
	}
}
