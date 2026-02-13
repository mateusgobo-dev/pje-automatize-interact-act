/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.VerificaCertificado;
import br.com.infox.pje.service.ItextHtmlConverterService;
import br.com.itx.component.Util;
import br.com.itx.util.AssinaturaUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.certificado.CertificadoICP;
import br.jus.cnj.certificado.CertificadoICPBrUtil;
import br.jus.cnj.certificado.Signer;
import br.jus.cnj.certificado.Signer.SignatureAlgorithm;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoBinDAO;
import br.jus.cnj.pje.business.dao.ProcessoDocumentoBinPessoaAssinaturaDAO;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.InscricaoMFUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.service.CertificadoDigitalService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.csjt.pje.business.pdf.HtmlParaPdf;
import br.jus.csjt.pje.business.pdf.PdfException;
import br.jus.csjt.pje.commons.exception.BusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumentoPapel;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * @author cristof
 * 
 */
@Name(ProcessoDocumentoBinManager.NAME)
public class ProcessoDocumentoBinManager extends BaseManager<ProcessoDocumentoBin>{
	
	public static final String NAME = "processoDocumentoBinManager";

	@In
	private ProcessoDocumentoBinDAO processoDocumentoBinDAO;

	@In
	private ProcessoDocumentoBinPessoaAssinaturaDAO processoDocumentoBinPessoaAssinaturaDAO;
	
	@In
	private CertificadoDigitalService certificadoDigitalService;
	
	@In
	private ParametroService parametroService;
	
	@In
	private DocumentoBinManager documentoBinManager;

	@Logger
	private Log logger;

	@Override
	protected ProcessoDocumentoBinDAO getDAO(){
		return this.processoDocumentoBinDAO;
	}
	
	@Override
	public void flush() throws PJeBusinessException {
		super.flush();
	}

	/**
	 * Modificado o método para gerar o md5 em todo o sistema. Utilizava o DigestUtils.md5Hex(byte[] data), porém
	 * foi orientado alterar para o Crypto.encodeMD5(byte[] data).
	 * 
	 * @issue	PJEII-19266
	 */
	public byte[] getBinaryData(ProcessoDocumentoBin pdb) throws PJeBusinessException{
		byte[] data = documentoBinManager.getData(pdb.getNumeroDocumentoStorage());
		String hash = Crypto.encodeMD5(data);
		if(!hash.equals(pdb.getMd5Documento())){
			logger.error("O conteúdo do documento foi modificado em banco de dados!");
			throw new PJeBusinessException("O conteúdo do documento foi modificado em banco de dados!");
		}
		return data;
	}

	public ProcessoDocumentoBin getProcessoDocumentoBin(){
		return getProcessoDocumentoBin(null);
	}
	
	public ProcessoDocumentoBin getProcessoDocumentoBin(String texto){
		ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
		pdb.setModeloDocumento(texto);
		return pdb;
	}

	@Override
	public ProcessoDocumentoBin findById(Object id) throws PJeBusinessException{
		ProcessoDocumentoBin pdb = super.findById(id);
		if (pdb!=null)
			pdb.setContext(new Util().getContextPath());
		return pdb;
	}
	
	@Override
	public ProcessoDocumentoBin persist(ProcessoDocumentoBin entity)
			throws PJeBusinessException {
		ProcessoDocumentoBin pdb = super.persist(entity);

		if(pdb.getFile() != null){
			pdb.setNumeroDocumentoStorage(documentoBinManager.persist(pdb));
			super.persist(pdb); 
		}
		return pdb;
	}
	
	public boolean existsByNumeroDocumentoStorage(String numeroDocumentoStorage, Integer idProcessoDocumentoBin){
		return processoDocumentoBinDAO.existsByNumeroDocumentoStorage(numeroDocumentoStorage, idProcessoDocumentoBin);
	}

	public ProcessoDocumentoBinPessoaAssinatura acrescentaAssinatura(ProcessoDocumentoBin pdb, Pessoa pessoa) throws PJeBusinessException{
		boolean valido = verificaAssinatura(pdb, pessoa) || Boolean.getBoolean("modoTesteAssinatura");
		if (!valido) {
			throw new PJeBusinessException("pje.processoDocumentoBin.error.verificaAssinatura.validarAssinaturaDocumento");
		}
		Date agora = new Date();
		if(pdb.getDataAssinatura() == null){
			pdb.setDataAssinatura(agora);
		}
		ProcessoDocumentoBinPessoaAssinatura pdbpa = new ProcessoDocumentoBinPessoaAssinatura();
		if (pdb.getSignature() != null) {
			pdbpa.setAssinatura(pdb.getSignature());
			pdbpa.setCertChain(pdb.getCertChain());
			pdbpa.setDataAssinatura(agora);
			pdbpa.setPessoa(pessoa);
			pdbpa.setProcessoDocumentoBin(pdb);
			if(Contexts.getEventContext() != null && Contexts.getEventContext().get("byPassValidacaoAssinatura") != null ) {
				pdbpa.setAssinaturaCMS(true);
			}
		} else {
			List<ProcessoDocumentoBinPessoaAssinatura> list = this.processoDocumentoBinPessoaAssinaturaDAO.getAssinaturaDocumento(pdb, pessoa);
			if (!list.isEmpty()) {
				pdbpa = list.get(list.size() - 1);
			}
		}

		return pdbpa;
	}
	
	public boolean verificaAssinatura(ProcessoDocumentoBin pdb, Pessoa pessoa) throws PJeBusinessException{
		return verificaAssinatura(pdb, "MD5withRSA", pessoa);
	}
	
	public boolean verificaAssinatura(ProcessoDocumentoBin pdb, String algoritmoAssinatura, Pessoa pessoa) throws PJeBusinessException {
		if(Contexts.getEventContext() != null && Contexts.getEventContext().get("byPassValidacaoAssinatura") != null ) {
			return true;
		}
		
		if((!pdb.isBinario()) && (pdb.getModeloDocumento() == null)) {
			throw new PJeBusinessException("pje.processoDocumentoBin.error.acrescentarAssinatura.documentoNulo");
		}
		
		boolean valido = false;
		
		if((pdb.getSignature() != null) && (pdb.getCertChain() != null)) {
			if (pdb.isBinario()){
				valido = verificaAssinatura(getBinaryData(pdb), pdb.getSignature(), pdb.getCertChain(), algoritmoAssinatura, pessoa);
			} else {
				valido = verificaAssinatura(pdb.getModeloDocumento().getBytes(),  pdb.getSignature(), pdb.getCertChain(), algoritmoAssinatura, pessoa);
			}
		} else {
			List<ProcessoDocumentoBinPessoaAssinatura> listaAssinaturaPessoa = this.processoDocumentoBinPessoaAssinaturaDAO.getAssinaturaDocumento(pdb, pessoa);
			if (!listaAssinaturaPessoa.isEmpty()) {
				ProcessoDocumentoBinPessoaAssinatura pdbpa = listaAssinaturaPessoa.get(listaAssinaturaPessoa.size() - 1);
				if (pdb.isBinario()) {
					valido = verificaAssinatura(getBinaryData(pdb), pdbpa.getAssinatura(), pdbpa.getCertChain(), algoritmoAssinatura, pessoa);
				} else {
					valido = verificaAssinatura(pdb.getModeloDocumento().getBytes(),  pdbpa.getAssinatura(), pdbpa.getCertChain(), algoritmoAssinatura, pessoa);
				}
			}
		}
		
		return valido;
	}
	
	public boolean verificaAssinatura(ProcessoDocumentoBin pdb, String signature, String certChain, String algoritmoAssinatura, Pessoa pessoa) throws PJeBusinessException{
		boolean valido = false;
		if (pdb.isBinario()){
			valido = verificaAssinatura(getBinaryData(pdb), signature, certChain, algoritmoAssinatura, pessoa);
		}else{
			if (pdb.getModeloDocumento() == null){
				throw new PJeBusinessException("pje.processoDocumentoBin.error.acrescentarAssinatura.documentoNulo");
			}
			valido = verificaAssinatura(pdb.getModeloDocumento().getBytes(), signature, certChain, algoritmoAssinatura, pessoa);
		}

		return valido;
	}
	
	private void limpaAssinaturasInvalidas(ProcessoDocumentoBin pdb) throws PJeBusinessException{
		List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = pdb.getSignatarios();
		List<ProcessoDocumentoBinPessoaAssinatura> remocoes = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>();
		for (ProcessoDocumentoBinPessoaAssinatura assinatura : assinaturas){
			boolean valida = false;
			try{
				if(assinatura.getAssinaturaCMS()){
					valida = true;
				}else if (pdb.isBinario()){
					valida = verificaAssinatura(getBinaryData(pdb), assinatura.getAssinatura(), assinatura.getCertChain(), assinatura.getAlgoritmoDigest(), assinatura.getPessoa());
				} else{
					valida = verificaAssinatura(pdb.getModeloDocumento().getBytes(), assinatura.getAssinatura(), assinatura.getCertChain(), assinatura.getAlgoritmoDigest(), assinatura.getPessoa());
				}
			}catch (PJeBusinessException e){
				if(assinatura.getCertChain().equalsIgnoreCase("certchaindeteste")){
					valida = false;
				}else{
					throw e;
				}
			}
			if (!valida){
				remocoes.add(assinatura);
				processoDocumentoBinPessoaAssinaturaDAO.remove(assinatura);
			}
		}
		pdb.getSignatarios().removeAll(remocoes);
		if(pdb.getSignatarios().size() == 0){
			pdb.setDataAssinatura(null);
			/*
			 * PJE-JT: Ricardo Scholz : PJEII-3124 - 2012-10-08 Alteracoes feitas pela JT.
			 * Reset das informações de cadeia de certificados e assinatura, visando evitar
			 * inconsistências na base de dados, quando este método é invocado.
			 * 2012-10-11: Rollback (comentário) das alterações, por identificação de efeito 
			 * colateral.
			 */
			//pdb.setCertChain(null);
			//pdb.setSignature(null);
			/*
			 * PJE-JT: Fim.
			 */
		}
	}

	/**
	 * Método responsável por verificar a validade da assinatura do documento.
	 * 
	 * @param document Sequência de bytes do documento.
	 * @param signature Assinatura do documento.
	 * @param base64CertChain Cadeia de certificado codificada em base64.
	 * @param algoritmo Algoritmo utilizado.
	 * @param pessoa Pessoa que assinou o documento.
	 * @return Verdadeiro se a assinatura do documento é válida. Falso, caso contrário
	 * @throws PJeBusinessException Caso algo de errado ocorra.
	 */
	public boolean verificaAssinatura(byte[] document, String signature, String base64CertChain, String algoritmo, Pessoa p) throws PJeBusinessException {
		if(p != null && p.getIdUsuario() == 0){
			return verificaAssinaturaSistema(document, signature, base64CertChain);
		}
		
		Certificate[] certificados = recuperarCertificados(base64CertChain);
		CertificadoICP certificadoICP = obterCertificadoICP(certificados);
		
		try {
			certificadoDigitalService.validate(certificadoICP.getX509Certificate(), certificados, recuperarTimeout());
		} catch (CertificateException e1) {
			if(e1.getCause() != null){
				throw new PJeBusinessException(e1.getMessage(), e1, e1.getCause().getMessage());
			}
			throw new PJeBusinessException(e1.getMessage(), e1);
		}
		PublicKey publicKey = certificadoICP.getX509Certificate().getPublicKey();
		
		boolean valid = false;
		
		try{
			byte[] sign = SigningUtilities.base64Decode(signature);
			SignatureAlgorithm sa = translateFromDigest(algoritmo);
			
			if (sa == null) {
				sa = translateAlgorithm(algoritmo);
			}
			
			if(sa != null){
				valid = Signer.verify(publicKey, sa, document, sign);
				
				if (!valid) {
					logger.warn(FacesUtil.getMessage("pje.processoDocumentoBin.warn.verificaAssinatura.assinaturaInvalida", publicKey));
				}				
			} else {
				Signature sigVerifier = Signature.getInstance(algoritmo);
				sigVerifier.initVerify(publicKey);
				sigVerifier.update(document);
				valid = sigVerifier.verify(sign);

				if (!valid) {
					logger.warn(FacesUtil.getMessage("pje.processoDocumentoBin.warn.verificaAssinatura.assinaturaInvalida", publicKey));
				}
			}
		} catch (Exception e){
			// Código adicionado para funcionar a verificação para o assinador do AUD (assinarAtasAudiencia.xhtml)
			try {
				byte[] sign = SigningUtilities.base64Decode(signature);
				valid = Signer.verify(publicKey, SignatureAlgorithm.SHA1withRSA, document, sign);
				
				if (!valid) {
					logger.warn(FacesUtil.getMessage("pje.processoDocumentoBin.warn.verificaAssinatura.assinaturaInvalida", publicKey));
				}
			} catch (Exception ex) {
				throw new PJeBusinessException("pje.processoDocumentoBin.error.verificaAssinatura.validarAssinaturaDocumento", e);
			}
		}
		
		if (valid && p != null){
			valid = valid && ( certificadoICP.getInscricaoMF().equals(InscricaoMFUtil.retiraMascara(p.getDocumentoCpfCnpj())) 
					|| certificadoICP.getInscricaoMF().equals(ParametroUtil.getParametro("inscricaoMF")) );
			if (!valid) {
				logger.error(FacesUtil.getMessage("pje.processoDocumentoBin.error.verificaAssinatura.validarUsuarioTokenDiferente", publicKey));
				throw new PJeBusinessException("pje.processoDocumentoBin.error.verificaAssinatura.validarUsuarioTokenDiferente", publicKey);
			}
		}
		return valid;
	}
	
	/**
	 * Método responsável por recuperar o parâmetro de {@link Parametros#LCR_TIMEOUT}. 
	 * Timeout para download de LCRs das CAs.
	 * 
	 * @return Timeout
	 */
	public int recuperarTimeout() {
		int timeout = 0;
		try{
			String timeoutParameter = parametroService.valueOf(Parametros.LCR_TIMEOUT);
			if(StringUtils.isNotBlank(timeoutParameter)){
				timeout = Integer.parseInt(timeoutParameter.trim());
			}
		} catch (NumberFormatException e) {
			logger.warn(FacesUtil.getMessage("pje.processoDocumentoBin.warn.recuperarParamTimeout.naoDefinido", Parametros.LCR_TIMEOUT));
		}
		return timeout;
	}

	/**
	 * Método responsável por retornar uma instância que implementa a interface de {@link CertificadoICP}.
	 * 
	 * @param certificados Array de {@link Certificate}.
	 * @return {@link CertificadoICP}
	 * @throws PJeBusinessException Caso algo de errado ocorra.
	 */	
	public CertificadoICP obterCertificadoICP(Certificate[] certificados) throws PJeBusinessException {
		CertificadoICP certificadoICP = null;
		
		for (Certificate certificate : certificados) {
			X509Certificate x509Certificate = (X509Certificate) certificate;
			certificadoICP = CertificadoICPBrUtil.getInstance(x509Certificate, false);
			if (certificadoICP != null) {
				break;
			}
		}
		if (certificadoICP == null){
			throw new PJeBusinessException("pje.processoDocumentoBin.error.verificaAssinatura.certificadoNaoEncontrado");
		}
		return certificadoICP;
	}

	/**
	 * Método responsável por recuperar os certificados a partir da cadeia de certificados o qual está codificada em base64.
	 * 
	 * @param base64CertChain Cadeia de certificados codificada em base64.
	 * @return Array de {@link Certificate}.
	 * @throws PJeBusinessException Caso algo de errado ocorra.
	 */
	private Certificate[] recuperarCertificados(String base64CertChain) throws PJeBusinessException {
		Certificate[] certificados = null;
		try{
			certificados = SigningUtilities.getCertChain(base64CertChain);
		} catch (Exception e){
			throw new PJeBusinessException("pje.processoDocumentoBin.error.verificarAssinatura.obterCadeiaCertificados", e);
		}
		return certificados;
	}

	private boolean verificaAssinaturaSistema(byte[] document, String signature, String base64CertChain) throws PJeBusinessException{
		Certificate[] certChain = recuperarCertificados(base64CertChain);
		X509Certificate certAux = (X509Certificate) certChain[0];
		PublicKey publicKey = certAux.getPublicKey();
		boolean valid = false;
		try{

			byte[] sign = SigningUtilities.base64Decode(signature);
			valid = Signer.verify(publicKey, SignatureAlgorithm.MD5withRSA, document, sign);

		} catch (Exception e){
			throw new PJeBusinessException("pje.processoDocumentoBin.error.verificaAssinatura.validarAssinaturaDocumento", e);
		}
		return valid;
	}

	public ProcessoDocumentoBin finalizaProcessoDocumentoBin(ProcessoDocumentoBin pdb, TipoProcessoDocumento tpd, Pessoa... pessoas) throws PJeBusinessException{
		if(!pdb.isBinario()){
			pdb.setNumeroDocumentoStorage(documentoBinManager.persist(pdb.getModeloDocumento().getBytes(),"text/html"));
		}
		if(VerificaCertificado.instance().isModoTesteCertificado()){
			return finalizaEmModoTeste(pdb, tpd, pessoas);
		}
		limpaAssinaturasInvalidas(pdb);

		List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>(pessoas.length);
		for (Pessoa p : pessoas){
			assinaturas.add(this.acrescentaAssinatura(pdb, p));
		}
		pdb.setValido(verificaValidacao(pdb, assinaturas, tpd));
		this.processoDocumentoBinPessoaAssinaturaDAO.persistAll(assinaturas);
		pdb.getSignatarios().addAll(assinaturas);
		ProcessoDocumentoBin ret = this.persist(pdb);
		return ret;

	}
	
	private ProcessoDocumentoBin finalizaEmModoTeste(ProcessoDocumentoBin pdb, TipoProcessoDocumento tpd, Pessoa... pessoas) throws PJeBusinessException{
		// Se estiver em modo teste, não deve verificar a cadeia/assinaturas, pois gerará exception
		//limpaAssinaturasInvalidas(pdb);
		List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>(pessoas.length);
		for(Pessoa p: pessoas){
			ProcessoDocumentoBinPessoaAssinatura assinatura = new ProcessoDocumentoBinPessoaAssinatura();
			assinatura.setPessoa(p);
			assinatura.setNomePessoa(p.getNome());
			assinatura.setAssinatura("assinaturadeteste");
			assinatura.setCertChain("certchaindeteste");
			assinatura.setProcessoDocumentoBin(pdb);
			assinatura.setDataAssinatura(new Date());
			assinaturas.add(assinatura);
		}
		pdb.setValido(verificaValidacao(pdb, assinaturas, tpd));
		pdb.setDataAssinatura(new Date());
		pdb.getSignatarios().addAll(assinaturas);
		processoDocumentoBinPessoaAssinaturaDAO.persistAll(assinaturas);
		return persist(pdb);
	}

	/*
	 * PJE-JT: Ricardo Scholz : PJEII-4851 PJEII-5454 PJEII-5521 - 2013-04-24
	 * Refatoração do método que verifica se um determinado documento deve ser
	 * considerado válido pelo sistema, de acordo com os papeis das pessoas que
	 * o assinaram.
	 */
	/**
	 * Realiza a verificação de validação de um documento, de acordo com os 
	 * papeis das pessoas que o assinaram. A regra é a estabelecida pelo CNJ, 
	 * transcrita abaixo e disponível em 
	 * http://www.cnj.jus.br/wikipje/index.php/Regras_de_neg%C3%B3cio#RN302
	 * 
	 * "(...) O documento será considerado válido no sistema de acordo
	 * com o parâmetro "exigibilidade". A exibigibilidade é facultativa quando o
	 * papel a ela vinculado pode assinar o documento, mas não é necessário que 
	 * o faça. Se a exigibilidade for suficiente, esse papel é o único 
	 * necessário para que o documento seja considerado válido. Se o parâmetro 
	 * estiver marcado como "obrigatório" para todos os papeis vinculados ao 
	 * documento, serão necessárias as assinaturas de todos os papeis vinculados
	 * ao documento para que esse seja considerado válido. Se estiver marcado 
	 * como "obrigatório" para três papeis, e outros dois papeis estiverem 
	 * marcados como "facultativo", só a assinatura dos três primeiros papeis 
	 * (marcados como "obrigatório") serão necessárias para que o documento seja 
	 * considerado válido. Se apenas um dos papeis vinculados ao documento 
	 * estiver marcado como suficiente, mesmo que outros papeis estejam marcados 
	 * como "obrigatório", a assinatura do papel marcado como "suficiente" será 
	 * a única necessária para que o documento seja considerado válido."
	 * 
	 * Em outras palavras, a regra estabelece a seguinte ordem de precedência:
	 * SUFICIENTE >> OBRIGATÓRIO >> FACULTATIVO/SEM EXIGIBILIDADE
	 * Quando há exigibilidades suficientes, pelo menos uma delas deve ser 
	 * satisfeita (disjunção lógica). Quando não há exigibilidades suficientes, 
	 * havendo exigibilidades obrigatórias, todas elas devem ser satisfeitas 
	 * (conjunção lógica). Quando também não há exigibilidades obrigatórias, a 
	 * validação deve retornar <code>true</code>. Ou seja, as exigibilidades 
	 * obrigatórias só são consideradas se não houver suficientes. E as 
	 * exigibilidades facultativas nunca são consideradas para efeitos de 
	 * validação de documentos. Se nenhuma exigibilidade estiver definida,
	 * o algoritmo também retorna <code>true</code>.
	 * 
	 * @param pdb		Documento binário do qual se está checando a validade.
	 * @param assinaturas	Lista de assinaturas do documento.
	 * @param tpd		Exigibilidades de assinatura para o tipo de documento.
	 * @return	<code>true</code> se pelo menos um dos papeis suficientes
	 * 			assinou o documento; não havendo exigibilidades suficientes,
	 * 			se todos os papeis obrigatórios assinaram.
	 * 			<code>false</code> caso contrário.
	 */
	public boolean verificaValidacao(ProcessoDocumentoBin pdb, 
			List<ProcessoDocumentoBinPessoaAssinatura> assinaturas, 
			TipoProcessoDocumento tpd){

		//Se o documento já está válido, não modifica seu status.
		if(pdb.getValido()) {
			return true;
		}
		
		//Estabelece valor padrão 'false', modificando-o caso
		//não existam restrições de exigibilidade ou uma das restrições
		//de exigibilidade suficientes seja satisfeita.
		boolean validado = false;
		
		//Recupera as restrições de exigibilidade
		if (!HibernateUtil.getSession().contains(tpd)) {
			tpd = (TipoProcessoDocumento) HibernateUtil.getSession().merge(tpd);
		}		
		List<TipoProcessoDocumentoPapel> exigibilidades = tpd.getPapeis();
		
		//Se não há restrições de exigibilidade, retorna que o documento está 
		//validado
		if (exigibilidades.size() == 0){
			validado = true;
		}
		//Se há restrições de exigibilidade, verifica os papeis e exigibilidades
		else {
			//Cria um conjunto, com todos os papeis dos assinantes
			Set<Papel> papeis = mapeiaPapeis(assinaturas);

			//Ordena as restrições de exigibilidade por precedência 
			//[suficiente >> obrigatório >> facultativo], para otimizar
			//o algoritmo de checagem.
			TipoProcessoDocumentoPapelValidacaoComparator comparator =
					new TipoProcessoDocumentoPapelValidacaoComparator();
			Collections.sort(exigibilidades, comparator);

			//Percorre a lista ordenada de exigibilidades, testando se as
			//assinaturas requeridas estão presentes em cada caso.

			//Variáveis auxiliares
			int index = 0;
			TipoProcessoDocumentoPapel exigibilidade = exigibilidades.get(index);

			//Percorre a lista enquanto exigibilidades SUFICIENTES forem 
			//encontradas
			while(exigibilidade.getExigibilidade().isSuficiente()) {
				//Atualiza o índice
				index++;
				//Se alguma das exigibilidades suficientes for satisfeita,
				//retorna 'true'.
				if(papeis.contains(exigibilidade.getPapel())) {
					validado = true;
					break;
				}
				//Recupera próxima da lista, se houver
				if(index < exigibilidades.size()) {
					exigibilidade = exigibilidades.get(index);
				} 
				//Se não houver, interrompe o laço para evitar
				//loop infinito
				else {
					break;
				}
			}

			//Se não houve exigibilidades SUFICIENTES, checa as OBRIGATÓRIAS
			if(index == 0) {
				//Estabelece valor padrão 'true', modificando-o caso um dos 
				//papeis com exigibilidade obrigatória não seja encontrado.
				validado = true;

				//Percorre todas as exigibilidades OBRIGATÓRIAS
				while(exigibilidade.getExigibilidade().isObrigatorio()) {
					//Atualiza o índice
					index++;
					//Se alguma das exigibilidades obrigatórias não for
					//satisfeita, modifica a 'flag' para 'false' e
					//encerra o laço
					if(!papeis.contains(exigibilidade.getPapel())) {
						validado = false;
						break;
					}
					//Recupera próxima da lista, se houver
					if(index < exigibilidades.size()) {
						exigibilidade = exigibilidades.get(index);
					}
					//Se não houver, interrompe o laço para evitar
					//loop infinito
					else {
						break;
					}
				}
			}
		}
		return validado;
	}
	
	/**
	 * Retorna um conjunto contendo todos os papeis das pessoas que constam
	 * em uma lista de assinaturas. Os papeis relacionados ao
	 * <code>UsuarioLocalizacao</code> de cada pessoa também são incluídos.
	 * @param assinaturas	assinaturas contendo as pessoas cujos papeis
	 * 						serão utilizados para formação do conjunto.
	 * @return
	 */
	private static Set<Papel> mapeiaPapeis(List<ProcessoDocumentoBinPessoaAssinatura> assinaturas) {
		//FIXME A modelagem da entidade ProcessoDocumentoBinPessoaAssinatura
		//precisa ser refatorada, de forma a guardar o papel utilizado no
		//momento da assinatura. A utilização de todos os papeis atualmente
		//relacionados à pessoa que assinou pode causar efeitos colaterais
		//indesejados na validação, caso o conjunto de papeis relacionado a uma
		//pessoa se modifique ao longo do tempo, e haja uma nova assinatura.
		//Além disso, deve-se considerar apenas o papel que assinou, e não os
		//demais papeis aos quais a pessoa ou a localização estão associadas.
		
		//Conjunto de papeis
		Set<Papel> papeis = new HashSet<Papel>();
		
		//Preenche o mapa com todos os papeis presentes
		for (ProcessoDocumentoBinPessoaAssinatura sig : assinaturas) {
			// Retorna caso exista uma inconsistência nos dados
			if (sig == null || sig.getPessoa() == null) {
				continue;
			}
			
			// Adiciona ao conjunto de papeis todos os papeis relacionados ao assinante
			papeis.addAll(sig.getPessoa().getPapelSet());
			
			// Adiciona ao conjunto todos os papeis relacionados ao UsuarioLocalizacao
			for (UsuarioLocalizacao usuarioLocalizacao : sig.getPessoa().getUsuarioLocalizacaoList()) {
				papeis.add(usuarioLocalizacao.getPapel());
			}
		}
		return papeis;
	}
	/*
	 * PJE-JT: Fim.
	 */
	
	public boolean existemSignatarios(Integer idProcessoDocumentoBin) {
		List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = this.obtemAssinaturas(idProcessoDocumentoBin);
		return CollectionUtilsPje.isNotEmpty(assinaturas);
	}

	public List<ProcessoDocumentoBinPessoaAssinatura> obtemAssinaturas(Integer idProcessoDocumentoBin){
		return processoDocumentoBinPessoaAssinaturaDAO.getAssinaturas(idProcessoDocumentoBin);
	}

	public List<ProcessoDocumentoBinPessoaAssinatura> obtemAssinaturas(ProcessoDocumentoBin pdb){
		return this.obtemAssinaturas(pdb.getIdProcessoDocumentoBin());
	}

	/**
	 * Cria um processo documento binário informando a data de inclusao e a descricao do documento.
	 * 
	 * @param dataInclusao do documento
	 * @param modeloDocumento descrição, conteúdo do documento.
	 * @return ProcessoDocumentoBin persistido.
	 */
	public ProcessoDocumentoBin inserirProcessoDocumentoBin(Date dataInclusao, String modeloDocumento){
		ProcessoDocumentoBin pdb = new ProcessoDocumentoBin();
		pdb.setModeloDocumento(modeloDocumento);
		pdb.setExtensao("text/html");
		pdb.setDataInclusao(dataInclusao);
		try{
			processoDocumentoBinDAO.persist(pdb);
		} catch (Exception e){
			logger.error("Exceção ao realizar a tarefa: {0}", e.getLocalizedMessage());
		}
		return pdb;
	}
	
	public ProcessoDocumentoBin findByIdentificadorUnico(String uid){
		return processoDocumentoBinDAO.findByIdentificadorUnico(uid);
	}

	public List<ProcessoDocumentoBinPessoaAssinatura> verificaAssinaturas(ProcessoDocumentoBin pdb) {
		List<ProcessoDocumentoBinPessoaAssinatura> ret = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>();
		for(ProcessoDocumentoBinPessoaAssinatura sign: pdb.getSignatarios()){
			try {
				if(sign.getAssinaturaCMS()){
					ret.add(sign);
				}else if(AssinaturaUtil.isModoTeste(sign.getAssinatura())){
					ret.add(sign);
				}else  if(verificaAssinatura(pdb, sign.getAssinatura(), sign.getCertChain(), sign.getAlgoritmoDigest(), sign.getPessoa())){
					ret.add(sign);
				}
			} catch (PJeBusinessException e) {
				e.printStackTrace();
				 if(AssinaturaUtil.isModoTeste(sign.getAssinatura())) {
					 ret.add(sign);
				 }				
			}
		}
		return ret;
	}
	
	private static SignatureAlgorithm translateAlgorithm(String alg){
		if(alg.equalsIgnoreCase("MD5withRSA")){
			return SignatureAlgorithm.MD5withRSA;
		}else if(alg.equalsIgnoreCase("SHA1withRSA")){
			return SignatureAlgorithm.SHA1withRSA;
		}else if(alg.equalsIgnoreCase("ASN1MD5withRSA")){
			return SignatureAlgorithm.ASN1MD5withRSA;
		}else if(alg.equalsIgnoreCase("SHA256withRSA")){
			return SignatureAlgorithm.SHA256withRSA;
		}else{
			return null;
		}
	}
	
	private static SignatureAlgorithm translateFromDigest(String da){
		if(da.equalsIgnoreCase("md5") || da.equalsIgnoreCase("1.2.840.113549.2.5")){
			return SignatureAlgorithm.MD5withRSA;
		}else if(da.equalsIgnoreCase("SHA-1") || da.equalsIgnoreCase("sha1") || da.equalsIgnoreCase("1.3.14.3.2.26")){
			return SignatureAlgorithm.SHA1withRSA;
		}else if(da.equalsIgnoreCase("SHA-256") || da.equalsIgnoreCase("sha256") || da.equalsIgnoreCase("sha-256") || da.equalsIgnoreCase("2.16.840.1.101.3.4.2.1")){
			return SignatureAlgorithm.SHA256withRSA;
		}else if(da.equalsIgnoreCase("asn1md5")){
			return SignatureAlgorithm.MD5withRSA;
		}else{
			return null;
		}
	}

	public boolean podeAssinar(TipoProcessoDocumento tipoDocumento, Papel... papeis) 
	{
		return processoDocumentoBinPessoaAssinaturaDAO.podeAssinar(tipoDocumento, papeis);
	}

	/*
	 * PJE-JT: Ricardo Scholz : PJEII-4851 PJEII-5454 PJEII-5521 - 2013-04-24
	 * Refatoração do método que verifica se um determinado documento deve ser
	 * considerado válido pelo sistema, de acordo com os papeis das pessoas que
	 * o assinaram.
	 */
	/**
	 * <code>Comparator</code> utilizado para ordenação das exigibilidades
	 * considerando a precedência estabelecida em regra do CNJ.
	 * Ao ser utilizado, deve inserir as exigibilidades SUFICIENTES primeiro,
	 * seguidas das exigibilidades OBRIGATÓRIAS, e por fim as exigibilidades
	 * FACULTATIVAS.
	 * 
	 * @see ProcessoDocumentoBinManager#verificaValidacao(ProcessoDocumentoBin, 
	 * List, TipoProcessoDocumento)
	 */
	class TipoProcessoDocumentoPapelValidacaoComparator implements 
	Comparator<TipoProcessoDocumentoPapel> {

		@Override
		public int compare(TipoProcessoDocumentoPapel o1,
				TipoProcessoDocumentoPapel o2) {

			if(o2 == null) {
				return -1;
			} else if (o1 == null) {
				return 1;
			} else if(o1.getExigibilidade() == o2.getExigibilidade()) {
				return 0;
			} else if(o1.getExigibilidade().isSuficiente()) {
				return -1;
			} else if(o1.getExigibilidade().isObrigatorio()) {
				if(o2.getExigibilidade().isSuficiente()) {
					return 1;
				}
				return -1;
			}
			return 1;
		}
	}
	/*
	 * PJE-JT: Fim.
	 */
	
    public byte[] convertHtml2Pdf(ProcessoDocumentoBin processoDocumentoBin, String resourcePath){
    	if(!processoDocumentoBin.isBinario()){
    		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			com.lowagie.text.Document document = new com.lowagie.text.Document();
			
			PdfWriter writer = null;
			try {
				String html = processoDocumentoBin.getModeloDocumento();
				
				if (resourcePath != null){
					html = html.replaceAll("src=\"(.*?)/img", "src=\"" + resourcePath + "/img");
				}
				
				writer = PdfWriter.getInstance(document, byteArray);
				document.open();
				PdfContentByte cb = writer.getDirectContent();
				
				PdfImportedPage page = null;
				int pageOfCurrentReaderPDF = 0;
				PdfReader pdfReader = null;
				byte[] conteudoPdf = null;				
				
				html = ItextHtmlConverterService.instance().converteImagensHtml(html);
				conteudoPdf = HtmlParaPdf.converte(html);
				pdfReader = new PdfReader(conteudoPdf);
				
				while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()){
					pageOfCurrentReaderPDF++;
					page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);					
					document.setPageSize(pdfReader.getPageSize(pageOfCurrentReaderPDF));
					document.newPage();
					cb.addTemplate(page, 0, 0);
				}

				pageOfCurrentReaderPDF = 0;
				
				document.close();
				pdfReader.close();
				
				return byteArray.toByteArray();
			} catch (DocumentException e1) {
				throw new BusinessException("pje.default.error.msg", e1.getLocalizedMessage());
			} catch (IOException e) {
				throw new BusinessException("pje.default.error.msg", e.getLocalizedMessage());
			} catch (PdfException e) {
				throw new BusinessException("pje.default.error.msg", e.getLocalizedMessage());
			}
    	}
    	
    	return null;
    }
    
    public byte[] adicionaRodapeAssinatura(byte[] documento, ProcessoDocumentoBin bin){
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
		com.lowagie.text.Document document = new com.lowagie.text.Document();
		
		PdfWriter writer = null;
		try {
			
			writer = PdfWriter.getInstance(document, byteArray);
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			
			PdfImportedPage page = null;
			int pageOfCurrentReaderPDF = 0;
			PdfReader pdfReader = null;
			byte[] conteudoPdf = documento;
							
			pdfReader = new PdfReader(conteudoPdf);
			
			while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()){
				pageOfCurrentReaderPDF++;
				page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);					
				document.setPageSize(pdfReader.getPageSize(pageOfCurrentReaderPDF));
				document.newPage();
				cb.addTemplate(page, 0, 0);
			}

			ProcessoDocumentoBinHome.instance().setId(bin.getIdProcessoDocumentoBin());
			
			// rodape de validacao da assinatura
			String urlValidacaoAssinatura = ValidacaoAssinaturaProcessoDocumento.instance()
					.getUrlValidacao()
					+ "?nd="
					+ ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento();
			String assinatura1 = "Assinado eletronicamente. A Certificação Digital pertence a: "
						+ ValidacaoAssinaturaProcessoDocumento.instance().getNomesAssinaturas(bin);
	    	
	    	BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
	    	
			cb.beginText();
			cb.setFontAndSize(bf, 7);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, assinatura1.toString(), 15, 33, 0);
			cb.endText();

			cb.beginText();
			cb.setFontAndSize(bf, 7);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, urlValidacaoAssinatura, 15, 23, 0);
			cb.endText();

			cb.beginText();
			cb.setFontAndSize(bf, 7);
			cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "Número do documento: "
				+ ValidacaoAssinaturaProcessoDocumento.instance().getCodigoValidacaoDocumento(), 15,
					13, 0);
			cb.endText();				

			pageOfCurrentReaderPDF = 0;
			
			document.close();
			pdfReader.close();
			
			return byteArray.toByteArray();
		} catch (DocumentException e1) {
			throw new BusinessException("pje.default.error.msg", e1.getLocalizedMessage());
		} catch (IOException e) {
			throw new BusinessException("pje.default.error.msg", e.getLocalizedMessage());
		}    	
    }
    
    public byte[] unificarPDFs(List<byte[]> documentos){
		ByteArrayOutputStream byteArray = null;
		try{
			byteArray = new ByteArrayOutputStream();
			com.lowagie.text.Document document = new com.lowagie.text.Document(PageSize.A4, 0, 0, 0, 0);
			PdfWriter writer = PdfWriter.getInstance(document, byteArray);
			document.open();

			PdfContentByte cb = writer.getDirectContent();			

			PdfImportedPage page;
			int pageOfCurrentReaderPDF = 0;
			PdfReader pdfReader = null;
			
			for (byte[] doc : documentos){				
				try {
					pdfReader = new PdfReader(doc);
				} catch (IOException e) {
					throw new BusinessException("pje.default.error.msg", e.getLocalizedMessage());
				}
				
				while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()){
					pageOfCurrentReaderPDF++;
					page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
					document.setPageSize(pdfReader.getPageSize(pageOfCurrentReaderPDF));
					document.newPage();
					cb.addTemplate(page, 0, 0);
				}
				
				pageOfCurrentReaderPDF = 0;
			}

			document.close();
		} catch (DocumentException e) {
			throw new BusinessException("pje.default.error.msg", e.getLocalizedMessage());
		}

		return byteArray.toByteArray();
    }

	public boolean temAssinatura(ProcessoDocumentoBin conteudo) {
		Search s = new Search(ProcessoDocumentoBinPessoaAssinatura.class);
		addCriteria(s, 
				Criteria.equals("processoDocumentoBin", conteudo));
		return count(s) > 0;
	}
	
	
	public boolean atualizarProcessoDocumentoBinHtml(int idProcessoDocumentoBin, int tamanho, Object dados, String mimeType) throws PJeBusinessException{
		try {
			ProcessoDocumentoBin procDocBin = findById(idProcessoDocumentoBin);
			procDocBin.setModeloDocumento(dados.toString());
			procDocBin.setSize(tamanho);
			procDocBin.setExtensao(mimeType);
			merge(procDocBin);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		flush();
		return true;
	}
	
	
	public  boolean atualizaProcessoDocumentoBinBinarios(int idProcessoDocumentoBin, int tamanho, Object dados, String mimeType) throws PJeBusinessException{
		
		try {
			String oidStorage = documentoBinManager.persist((byte[]) dados, "application/octet-stream");
			ProcessoDocumentoBin procDocBin = findById(idProcessoDocumentoBin);
			procDocBin.setNumeroDocumentoStorage(oidStorage);
			procDocBin.setSize(tamanho);
			procDocBin.setExtensao(mimeType);
			merge(procDocBin);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			return false;
		}
		flush();
		return true;
	}
	
	/**
	 * Obter por id Processo Documento
	 * @param idProcDoc
	 * @return ProcessoDocumentoBin
	 */
	public ProcessoDocumentoBin recuperar(Integer idProcDoc){
		return getDAO().recuperar(idProcDoc);
	}
	
	public static ProcessoDocumentoBinManager instance() {
		return ComponentUtil.getComponent(NAME);
	}
	
}
