package br.jus.cnj.pje.nucleo.service;

import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.core.certificado.VerificaCertificado;
import br.jus.cnj.certificado.CertificadoICP;
import br.jus.cnj.certificado.CertificadoICPBrUtil;
import br.jus.cnj.certificado.Signer;
import br.jus.cnj.certificado.Signer.SignatureAlgorithm;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.Pessoa;

import com.nakov.certificate.CRLVerifier;

@Name(value=AssinaturaDigitalService.NAME)
public class AssinaturaDigitalService extends BaseService {

	public static final String NAME = "assinaturaDigitalService";

	@Logger
	private Log logger;
	
	@In
	private ParametroService parametroService;
	
	@In
	private CertificadoDigitalService certificadoDigitalService;
	
	/**
	 * Metodo responsavel por realizar a validacao da assinatura digital, verificacao da situacao dos certificados digitais da cadeia de certificados e
	 * se a pessoa logada no sistema e a mesma pessoa do certificado digital que assinou a mensagem.
	 *  
	 * @param mensagemOriginal A mensagem original em bytes que foi assinada
	 * @param assinaturaBase64 A assinatura digital em base 64
	 * @param certChainBase64 A cadeia de certificado em base 64
	 * @param algoritmo O algoritmo utilizado na assinatura digital
	 * @param pessoaLogada A pessoa logada no sistema se for nulo a verificacao nao sera executada
	 * @throws Exception 
	 */
	public void validarAssinaturaDigitalEhPessoaLogada(byte[] mensagemOriginal, String assinaturaBase64, String certChainBase64, String algoritmo, Pessoa pessoaLogada) throws Exception {

		// Se a assinatura estiver em modo teste nao realiza nenhuma verificacao
		if(!VerificaCertificado.instance().isModoTesteCertificado()){
			
			Certificate[] certChain = getCertChain(certChainBase64);

			CertificadoICP certificado = getCerticadoICP(certChain);
		
			if (certificado == null){
				throw new PJeBusinessException("pje.processoDocumentoBin.error.verificaAssinatura.certificadoNaoEncontrado");
			}

			validarCadeiaCerticado(certificado.getX509Certificate(), certChain, getLcrTimeout());	
		
			validarAssinaturaDigital(certificado.getX509Certificate().getPublicKey(), mensagemOriginal, assinaturaBase64, algoritmo);
			
			if (pessoaLogada != null) {
				if (!certificado.getInscricaoMF().equals(InscricaoMFUtil.retiraMascara(pessoaLogada.getDocumentoCpfCnpj()))) {
					throw new Exception("A assinatura digital é inválida pois a pessoa logada não é a mesma pessoa do certificado que assinou");
				}
			}
		}
	}
	
	/**
	 * Metodo responsavel por realizar a validacao da assinatura digital e verificacao da situacao dos certificados digitais da cadeia de certificados.
	 * 
	 * @param mensagemOriginal A mensagem original em bytes que foi assinada
	 * @param assinaturaBase64 A assinatura digital em base 64
	 * @param certChainBase64 A cadeia de certificado em base 64
	 * @param algoritmo O algoritmo utilizado na assinatura digital
	 * @throws Exception 
	 */
	public void validarAssinaturaDigital(byte[] mensagemOriginal, String assinaturaBase64, String certChainBase64, String algoritmo) throws Exception {		
		validarAssinaturaDigitalEhPessoaLogada(mensagemOriginal, assinaturaBase64, certChainBase64, algoritmo, null);
	}
	
	/**
	 * Metodo responsavel por validar a assinatura digital 
	 * @param publicKey A chave publica do certificado do assinante
	 * @param mensagemOriginal A mensagem foi assinada
	 * @param assinaturaBase64 A assinatura em base 64
	 * @param algoritmo O algoritmo que foi utilizado para assinar
	 * @throws PJeBusinessException 
	 */
	private void validarAssinaturaDigital(PublicKey publicKey, byte[] mensagemOriginal, String assinaturaBase64, String algoritmo) throws PJeBusinessException {
		
		try{
			byte[] sign = SigningUtilities.base64Decode(assinaturaBase64);
			
			SignatureAlgorithm sa = translateFromDigest(algoritmo);
			
			if(sa == null){
				sa = translateAlgorithm(algoritmo);
			}
			
			boolean assinaturaValida = false;
			
			if(sa != null){
				assinaturaValida = Signer.verify(publicKey, sa, mensagemOriginal, sign);
			}
			else{
				Signature sigVerifier = Signature.getInstance(algoritmo);
				sigVerifier.initVerify(publicKey);
				sigVerifier.update(mensagemOriginal);
				assinaturaValida = sigVerifier.verify(sign);				
			}	
			
			if (assinaturaValida == false) {
				throw new PJeBusinessException("A assinatura digital da mensagem é inválida!");
			}
		}
		catch (Exception e) {
			throw new PJeBusinessException("pje.processoDocumentoBin.error.verificaAssinatura.validarAssinaturaDocumento", e);
		}		
	}

	/**
	 * Metodo responsavel por validar situacao da cadeia de certificado digitais
	 * 
	 * @param certificado O certificado assinante 
	 * @param certChain A cadeia de certificado do certificado assinante
	 * @param lcrTimeout O timeout para consulta online das lista de certificados revogados
	 * @throws PJeBusinessException 
	 */
	private void validarCadeiaCerticado(X509Certificate certificado, Certificate[] certChain, int lcrTimeout) throws PJeBusinessException {

		try {
			certificadoDigitalService.validate(certificado, certChain, lcrTimeout);
		} 
		catch (CertificateException e1) {
			if(e1.getCause() != null){
				throw new PJeBusinessException(e1.getMessage(), e1, e1.getCause().getMessage());
			}
			throw new PJeBusinessException(e1.getMessage(), e1);
		}
	}

	/**
	 * Metodo responsavel por recuperar o valor do parametro timeoutLCR caso não consiga sera utilizado o valor 0.
	 * Verificando o codigo da classe que realmente executa o download da CRL quando o valor da timeout e 0 ele utiliza a constante 3000. 
	 * ver o metodo downloadCRLFromWeb da classe {@link CRLVerifier}   
	 * @return O timeout
	 * @throws Exception
	 */
	public int getLcrTimeout() throws Exception {

		String timeoutParameter = "";
		
		int timeout = 0;
		
		try{
			timeoutParameter = parametroService.valueOf(Parametros.LCR_TIMEOUT);
			timeout = Integer.parseInt(timeoutParameter.trim());
		}
		catch (Exception e) {
			logger.warn(MessageFormat.format("O parâmetro [{0}] não foi definido ou é inválido. Será utilizado o valor padrão para realizar o download de LCRs.", Parametros.LCR_TIMEOUT));

		}
		
		return timeout;
	}

	/**
	 * Decodifica uma String representativa de uma cadeia de bytes, no padrão
	 * base 64, para uma lista de certificados.
	 *  
	 * @param certChainBase64 A cadeia de certificados no padrao base64
	 * @return A lista de certificados resultantes da decodificacao 
	 * @throws PJeBusinessException 
	 */
	public Certificate[] getCertChain(String certChainBase64) throws PJeBusinessException {
		try{
			return SigningUtilities.getCertChain(certChainBase64);
		} 
		catch (Exception e){
			throw new PJeBusinessException("pje.processoDocumentoBin.error.verificarAssinatura.obterCadeiaCertificados", e);
		}
	}
	
	/**
	 * Metodo responsavel por recuperar e instanciar o certificado icpbrasil de uma pessoa dentre os certifacados da cadeia 
	 * @param certChain A cadeia de certificado digitais
	 * @return O certificado icpbr da pessoa 
	 */
	public CertificadoICP getCerticadoICP(Certificate[] certChain) {

		CertificadoICP certificado = null;

		for (Certificate cert : certChain) {
			X509Certificate certAux = (X509Certificate) cert;
			certificado = CertificadoICPBrUtil.getInstance(certAux);
			if (certificado != null) {
				break;
			}
		}		
		
		return certificado;
	}

	private static SignatureAlgorithm translateAlgorithm(String alg){
		
		if(alg.equalsIgnoreCase("MD5withRSA")) {			
			return SignatureAlgorithm.MD5withRSA;
		}
		else if(alg.equalsIgnoreCase("SHA1withRSA")) {
			return SignatureAlgorithm.SHA1withRSA;
		}
		else if(alg.equalsIgnoreCase("ASN1MD5withRSA")){
			return SignatureAlgorithm.ASN1MD5withRSA;
		}
		else if(alg.equalsIgnoreCase("SHA256withRSA")){
			return SignatureAlgorithm.SHA256withRSA;
		}
		else {
			return null;
		}
	}
	
	private static SignatureAlgorithm translateFromDigest(String da){
		
		if (da.equalsIgnoreCase("md5") || da.equalsIgnoreCase("1.2.840.113549.2.5")){
			return SignatureAlgorithm.MD5withRSA;
		}
		else if(da.equalsIgnoreCase("sha1") || da.equalsIgnoreCase("1.3.14.3.2.26")){
			return SignatureAlgorithm.SHA1withRSA;
		}
		else if(da.equalsIgnoreCase("sha256") || da.equalsIgnoreCase("sha-256") || da.equalsIgnoreCase("2.16.840.1.101.3.4.2.1")){
			return SignatureAlgorithm.SHA256withRSA;
		}
		else if(da.equalsIgnoreCase("asn1md5")){
			return SignatureAlgorithm.MD5withRSA;
		}
		else{
			return null;
		}
	}	
}