/**
 * DocumentoProcessualParaProcessoDocumentoBinConverter.java
 * 
 * Data de criação: 24/11/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;

import br.com.infox.cliente.util.MimetypeUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.core.certificado.Certificado;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.core.certificado.DadosCertificado;
import br.com.infox.core.certificado.ValidaDocumento;
import br.com.infox.core.certificado.ValidaDocumento.ValidaDocumentoException;
import br.com.infox.core.certificado.util.CodificacaoCertificado;
import br.com.infox.core.certificado.util.DigitalSignatureUtils;
import br.com.infox.exceptions.NegocioException;
import br.com.itx.util.AssinaturaUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.certificado.Signer;
import br.jus.cnj.certificado.Signer.SignatureAlgorithm;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.intercomunicacao.v222.beans.Assinatura;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.intercomunicacao.util.cms.Envelope;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIParametroUtil;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.servicos.MimeUtilChecker;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.util.Crypto;

/**
 * Conversor de DocumentoProcessual para ProcessoDocumentoBin.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name(DocumentoProcessualParaProcessoDocumentoBinConverter.NAME)
public class DocumentoProcessualParaProcessoDocumentoBinConverter extends 
		IntercomunicacaoConverterAbstrato<DocumentoProcessual, ProcessoDocumentoBin> {

	public static final String NAME = "v222.documentoProcessualParaProcessoDocumentoBinConverter";
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	private static final String NOME_DOCUMENTO_PROCESSUAL = "documentoProcessual";
	private static final String ASSINATURA_TESTE = "assinatura modo teste";

	@In
	private PessoaService pessoaService;
	
	@In
	private DocumentoBinManager documentoBinManager;
	
	@Logger
	private Log log;
	
	/**
	 * Use o método converter(DocumentoProcessual, ProcessoTrf)
	 * 
	 * @see br.jus.cnj.pje.intercomunicacao.converter.IntercomunicacaoConverterAbstrato#converter(java.lang.Object)
	 */
	@Override
	public ProcessoDocumentoBin converter(DocumentoProcessual documento) {
		return this.converter(documento, false);
	}

 	public ProcessoDocumentoBin converter(DocumentoProcessual documento, boolean isRequisicaoDePJE) {
		ProcessoDocumentoBin resultado = null;

		if (isNotNull(documento)) {

			byte[] bytes = ProjetoUtil.converterParaBytes(documento.getConteudo());

			boolean isDocumentoAutoAssinado = isDocumentoAutoAssinado(bytes);
			boolean isDocumentoPossuiAssinatura = isDocumentoPossuiAssinatura(documento);

			resultado = new ProcessoDocumentoBin();
			resultado.getSignatarios().addAll(
					obterColecaoSignatarios(documento, resultado, isDocumentoAutoAssinado, bytes, isRequisicaoDePJE));
			resultado.setDataAssinatura(obterDataPrimeiraAssinatura(resultado.getSignatarios()));
			resultado.setUsuario(obterUsuarioLogado());
			resultado.setDataInclusao(ConversorUtil.converterParaDate(documento.getDataHora(), true));
			resultado.setValido(!resultado.getSignatarios().isEmpty() || obterValido(documento));
			if (ParametroUtil.instance().isBaseBinariaUnificada())
				resultado.setNumeroDocumentoStorage(obterNumeroDocumentoStorage(documento));

			if (isDocumentoAutoAssinado) {
				Envelope envelope = novoEnvelope(bytes);
				bytes = envelope.getBytesArquivoOriginal();

				resultado.setValido(true);
				resultado.setMd5Documento(Crypto.encodeMD5(bytes));

				if (!envelope.getAssinaturas().isEmpty()) {
					X509Certificate[] cadeia = envelope.getAssinaturas().keySet().iterator().next();
					String assinatura = converterBytesParaStringBase64(envelope.getAssinaturas().get(cadeia));
					String cadeiaString = converterCertificadoX509ParaCertificadoBase64(cadeia);

					resultado.setSignature(assinatura);
					resultado.setCertChain(cadeiaString);
				}
			} else {
				resultado.setMd5Documento(obterMd5Documento(documento, bytes));
			}
			if (isDocumentoPossuiAssinatura) {
				ProcessoDocumentoBinPessoaAssinatura pdbpa = resultado.getSignatarios().get(0);
				resultado.setValido(true);
				resultado.setSignature(pdbpa.getAssinatura());
				resultado.setCertChain(pdbpa.getCertChain());
			}

			resultado.setExtensao(MNIUtil.obterMimeType(documento));
			resultado.setBinario(!(
					resultado.getExtensao() == null || 
					MimetypeUtil.isMimetypeHtml(resultado.getExtensao())));
			if (!resultado.isBinario() && bytes != null) {
				try {
					resultado.setModeloDocumento(new String(bytes, "ISO-8859-1"));
				} catch (UnsupportedEncodingException e) {
					log.error("Não foi possível converter o conteúdo em HTML do documento {0}", documento.getIdDocumento());
					throw new IntercomunicacaoException("Não foi possível converter o conteúdo em HTML do documento "+documento.getIdDocumento(), e);
				}	
			} else if (resultado.isBinario() && bytes != null && bytes.length > 0) {
				//validar mymeType
				MimeUtilChecker mimeUtil = ComponentUtil.getComponent("mimeUtilChecker");
				String mimeFromBytes = mimeUtil.getMimeType(bytes);
				if(!mimeFromBytes.contains(resultado.getExtensao())){
					throw new IntercomunicacaoException(
							String.format("Tipo de arquivo informado não confere com o conteúdo do documento de id #%s. "
									+ "O conteúdo é do tipo %s e o tipo informado é %s", documento.getIdDocumento(), mimeFromBytes, resultado.getExtensao()));
				}	
			}
			resultado.setNomeArquivo(obterNomeArquivo(documento));
			resultado.setSize((bytes != null ? bytes.length : obterTamanhoArquivo(resultado.getNumeroDocumentoStorage())));
			resultado.setProcessoDocumento(bytes);
		}
		return resultado;
 	}
	
	/**
	 * Recupera o tamanho do arquivo quando a base binária estiver configurada
	 * Apenas o oidStorage é remetido e com ele o conteúdo é recuperado para atualizar o tamanho do processoDocumentoBin
	 * @param nrStorage
	 * @return tamanho do arquivo, caso bem sucedido
	 */
	private int obterTamanhoArquivo(String nrStorage){
		if (nrStorage!=null){
			try {
				byte[] conteudo = null;
				conteudo = documentoBinManager.getData(nrStorage);
				return conteudo.length;
			} catch (PJeBusinessException e) {
				log.error("Não foi possível obter o arquivo pelo oidStorage {0}. Verifique se a base binária unificada está devidamente configurada", nrStorage);
				throw new IntercomunicacaoException("Não foi possível recuperar o documento do storage. Verifique a configuração da base binária", e);
			}
		}
		return 0;
			
	}

	/**
	 * Converte um array de bytes para string base64.
	 * 
	 * @param bytes
	 * @return String base64
	 */
	protected String converterBytesParaStringBase64(byte[] bytes) {
		String resultado = null;
		
		if (bytes != null) {
			resultado = Base64.encodeBase64URLSafeString(bytes);
		}
		return resultado;
	}
	
	/**
	 * Converte uma cadeia de certificados na base64 para array de X509Certificate.
	 * 
	 * @param cadeiaBase64
	 * @param codificacaoCertificado PEM ou PkiPath
	 * @return array de X509Certificate
	 */
	protected X509Certificate[] converterCertificadoBase64ParaCertificadoX509(
			String cadeiaBase64, String codificacaoCertificado) {
		try {
			return DigitalSignatureUtils.loadCertPath(cadeiaBase64,	codificacaoCertificado);
		} catch (Exception e) {
			String mensagem = "Erro ao converter a cadeia de certificados para a array de X509Certificate, erro: %s";
			throw new IntercomunicacaoException(String.format(mensagem, e.getMessage()));
		}
	}

	/**
	 * Converte um array de X509Certificate para string base64.
	 * 
	 * @param cadeia
	 * @return String base64.
	 */
	protected String converterCertificadoX509ParaCertificadoBase64(X509Certificate[] cadeia) {
		try {
			return SigningUtilities.encodeCertChain(cadeia);
		} catch (Exception e) {
			String mensagem = "Erro ao converter a cadeia de certificados para a base64, erro: %s";
			throw new IntercomunicacaoException(String.format(mensagem, e.getMessage()));
		}
	}

	/**
	 * Retorna true se o documento possuir assinaturas.
	 * 
	 * @param documentoProcessual
	 * @return booleano
	 */
	protected Boolean isDocumentoPossuiAssinatura(DocumentoProcessual documentoProcessual) {
		return isNotNull(documentoProcessual.getAssinatura()) && 
				documentoProcessual.getAssinatura().isEmpty() == false && 
				isNotVazio(documentoProcessual.getAssinatura().get(0).getAssinatura());
	}

	/**
	 * Retorna true se o documento for auto assinado.
	 * 
	 * @param documentoProcessual
	 * @return booleano
	 */
	protected Boolean isDocumentoAutoAssinado(byte[] bytes) {
		return isNotNull(bytes) && Envelope.isCMSSigned(bytes);
	}

	/**
	 * Retorna novo envelope com dados da assinatura PKCS7.
	 * 
	 * @param bytes
	 * @return novo Envelope.
	 */
	protected Envelope novoEnvelope(byte[] bytes) {
		try {
			return new Envelope(bytes);
		} catch (Exception e) {
			throw new IntercomunicacaoException(e.getMessage());
		}
	}

	/**
	 * Retorna o algoritmo digest.
	 * 
	 * @param algoritmoDigest
	 * @return String do algoritmo
	 */
	@SuppressWarnings("unchecked")
	protected String obterAlgoritmoDigest(String algoritmoDigest) {
		
		if (StringUtils.isNotBlank(algoritmoDigest)) {
			Map<String, String> mapaAlgoritmo = new CaseInsensitiveMap();
			mapaAlgoritmo.put(SignatureAlgorithm.ASN1MD5withRSA.name(), "ASN1MD5");
			mapaAlgoritmo.put(SignatureAlgorithm.MD5withRSA.name(), "MD5");
			mapaAlgoritmo.put(SignatureAlgorithm.SHA1withRSA.name(), "SHA-1");
			mapaAlgoritmo.put(SignatureAlgorithm.SHA256withRSA.name(), "SHA-256");
			mapaAlgoritmo.put("MD5", "MD5");
			mapaAlgoritmo.put("SHA-1", "SHA-1");
			mapaAlgoritmo.put("SHA-256", "SHA-256");
			mapaAlgoritmo.put("SHA1", "SHA-1");
			mapaAlgoritmo.put("SHA256", "SHA-256");
			
			if (mapaAlgoritmo.containsKey(algoritmoDigest)) {
				algoritmoDigest= mapaAlgoritmo.get(algoritmoDigest);
			} else {
				String mensagem = "O algoritmo '%s' não é reconhecido, os valores válidos são: '%s'.";
				throw new NegocioException(String.format(mensagem, algoritmoDigest, mapaAlgoritmo.values().toString()));
			}
			
		}
		return algoritmoDigest;
	}
	
	/**
	 * Retorna a data da primeira assinatura, ou seja, a data mais antiga da coleção de assinaturas.
	 * 
	 * @param signatarios 
	 * @return data da primeira assinatura.
	 */
	protected Date obterDataPrimeiraAssinatura(
			final List<ProcessoDocumentoBinPessoaAssinatura> signatarios) {
		Date resultado = null;
		Date dataCorrente = null;
		
		if (ProjetoUtil.isNotVazio(signatarios)) {
			NavigableSet<Date> dates = new TreeSet<Date>();
			
			for (ProcessoDocumentoBinPessoaAssinatura signatario : signatarios) {
				dates.add(signatario.getDataAssinatura());
			}
			resultado = dates.first();
		}

		dataCorrente = new Date();
		if (resultado != null && resultado.compareTo(dataCorrente) > 0) {
			throw new NegocioException(java.text.MessageFormat.format("A data de primeira assinatura ({0}) do documento é inválida: "
					+ "superior à data atual ({1}).", 
					br.jus.pje.nucleo.util.DateUtil.dateToString(resultado, "dd/MM/yyyy HH:mm:ss.SSS"), 
					br.jus.pje.nucleo.util.DateUtil.dateToString(dataCorrente, "dd/MM/yyyy HH:mm:ss.SSS")));
		}
		
		return resultado;
	}
	
	/**
	 * Retorna o valor de documento válido.
	 * 
	 * @param documento
	 * @return booleano válido.
	 */
	protected Boolean obterValido(DocumentoProcessual documento) {
		String docValido = MNIParametroUtil.obterValor(documento, MNIParametro.PARAM_DOCUMENTO_VALIDO);
		return (isNotVazio(docValido) ? Boolean.parseBoolean(docValido) : Boolean.FALSE);
	}

	/**
	 * Retorna a string base64 da cadeia de certificados.
	 * 
	 * @param assinatura
	 * @return String base64 da cadeia de certificados.
	 */
	protected String obterCertChain(Assinatura assinatura) {
		String resultado =  assinatura.getCadeiaCertificado();

		String cadeiaCertificado = assinatura.getCadeiaCertificado();
		String codificacaoCertificado = assinatura.getCodificacaoCertificado();

		try {
			if (CodificacaoCertificado.isPEM(codificacaoCertificado)) {
				Certificate[] cadeia = DigitalSignatureUtils.loadCertPath(cadeiaCertificado, codificacaoCertificado);
				List<Certificate> certList = Arrays.asList(cadeia);
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				CertPath certPath = certFactory.generateCertPath(certList);
				byte[] encodedCertChain = certPath.getEncoded();
				resultado = new Base64(64).encodeToString(encodedCertChain);
			}
		} catch (Exception e) {
			String mensagem = "Não foi possível fazer o parser do certificado do documento para o formato base64, erro: %s";
			throw new IntercomunicacaoException(String.format(mensagem, e.getMessage()));
		}

		return resultado;
	}

	
	/**
	 * Retorna a coleção de assinaturas.
	 * 
	 * @param documento
	 * @param processoDocumentoBin
	 * @param isDocumentoAutoAssinado
	 * @param bytes
	 * @return assinaturas.
	 */
	protected Collection<ProcessoDocumentoBinPessoaAssinatura> obterColecaoSignatarios(
			DocumentoProcessual documento, ProcessoDocumentoBin processoDocumentoBin,
			boolean isDocumentoAutoAssinado, byte[] bytes, boolean isRequisicaoDePJE) {
		
		Collection<ProcessoDocumentoBinPessoaAssinatura> signatarios = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>();

		if (isDocumentoAutoAssinado) { //PKCS7 (Auto-assinado)
			Envelope envelope = novoEnvelope(bytes);

			Map<X509Certificate[], byte[]> mapaAssinaturas = envelope.getAssinaturas();
			Iterator<X509Certificate[]> assinaturas = mapaAssinaturas.keySet().iterator();
			while (assinaturas.hasNext()) {
				X509Certificate[] cadeia = assinaturas.next();
				String assinatura = converterBytesParaStringBase64(mapaAssinaturas.get(cadeia));
				String certChain = converterCertificadoX509ParaCertificadoBase64(cadeia);

				ProcessoDocumentoBinPessoaAssinatura pdbpa = processarNovoProcessoDocumentoBinPessoaAssinatura(
						processoDocumentoBin, 
						assinatura, 
						certChain, 
						envelope.getDataAssinatura(), 
						isDocumentoAutoAssinado, 
						envelope.getAlgoritmoHash(), 
						bytes,
						isRequisicaoDePJE,
						documento);
				signatarios.add(pdbpa);
			}
		} else if (isDocumentoPossuiAssinatura(documento)) { 
			//PDF com assinatura no signatário
			for (Assinatura assinatura : documento.getAssinatura()) {
				validarAssinatura(assinatura);
				
				ProcessoDocumentoBinPessoaAssinatura pdbpa = processarNovoProcessoDocumentoBinPessoaAssinatura(
						processoDocumentoBin, 
						assinatura.getAssinatura(), 
						obterCertChain(assinatura), 
						ConversorUtil.converterParaDate(assinatura.getDataAssinatura()), 
						CodificacaoCertificado.isPkiPath(assinatura.getCodificacaoCertificado()), 
						assinatura.getAlgoritmoHash(), 
						bytes,
						isRequisicaoDePJE,
						documento);
				signatarios.add(pdbpa);
			}
		} else if (MimetypeUtil.isMimetypePdf(documento.getMimetype())) {
			try {
				signatarios = processarNovoProcessoDocumentoBinPDFPessoaAssinaturaPAdES(processoDocumentoBin, bytes);
			} catch (Exception e) {
				throw new NegocioException(String.format(
					"Não foi possível recuperar a assinatura do documento '%s'. %s", documento.getDescricao(), e.getLocalizedMessage()));
			}
		}
		
		return signatarios;
	}

	/**
	 * Retorna o hash MD5 do documento. Será gerado o MD5 caso o hash enviado seja SHA, pois o PJE
	 * usa o MD5 para fazer algumas validações.
	 * 
	 * @param documento
	 * @param bytes
	 * @return MD5 do documento.
	 */
	protected String obterMd5Documento(DocumentoProcessual documento, byte[] bytes) {
		String hash = documento.getHash();

		if (StringUtils.length(hash) == 64 && isNotNull(bytes)) { // SHA-256
			// converte o hash para MD5, pois o PJe faz as validações usando o
			// algoritmo MD5.
			hash = Crypto.encodeMD5(bytes);
		}
		return hash;
	}

	/**
	 * Retorna o nome do arquivo.
	 * 
	 * @param documento
	 * @return stringo do nome do arquivo.
	 */
	protected String obterNomeArquivo(DocumentoProcessual documento) {
		String resultado = MNIParametroUtil.obterValor(documento,
				MNIParametro.PARAM_NOME_ARQUIVO);
		if (isVazio(resultado)) {
			if (isNotVazio(documento.getDescricao())) {
				resultado = documento.getDescricao();
			} else {
				resultado = NOME_DOCUMENTO_PROCESSUAL;
			}
		}
		return resultado;
	}

	/**
	 * Retorna o nome da pessoa do certificado passado por parâmetro.
	 * @param certChain String do certificado no formato PEM ou Base64 (PkiPath)
	 * @return nome da pessoa.
	 */
	protected String obterNomePessoa(String certChain) {
		String resultado = null;
		
		try {
			CodificacaoCertificado codificacao = 
					DigitalSignatureUtils.getCodificacaoCertificado(certChain, true);
			X509Certificate[] certificados = 
					DigitalSignatureUtils.loadCertPath(certChain, codificacao.getValor());
			Certificado dados = new Certificado(certificados);
			resultado = (dados != null ? dados.getNome() : null);
		} catch (CertificadoException e) {
			String mensagem = "Falha ao obter os dados do certificado do documento, erro %s";
			throw new NegocioException(String.format(mensagem, e.getMessage()));
		}
		return resultado;
	}
	
	/**
	 * Retorna o número do storage passado via parâmetro.
	 * 
	 * @param documento
	 * @return número do storage.
	 */
	protected String obterNumeroDocumentoStorage(DocumentoProcessual documento) {
		return MNIParametroUtil.obterValor(documento, MNIParametro.PARAM_STORAGE_ID);
	}
	
	/**
	 * Processa a assinatura do documento e retorna o objeto ProcessoDocumentoBinPessoaAssinatura.
	 * 
	 * @param processoDocumentoBin
	 * @param assinatura
	 * @param certChain
	 * @param dataAssinatura
	 * @param assinaturaCMS
	 * @param algoritmoDigest
	 * @param bytes
	 * @return ProcessoDocumentoBinPessoaAssinatura
	 */
	protected ProcessoDocumentoBinPessoaAssinatura processarNovoProcessoDocumentoBinPessoaAssinatura(
			ProcessoDocumentoBin processoDocumentoBin, String assinatura, String certChain, Date dataAssinatura, 
			boolean assinaturaCMS, String algoritmoDigest, byte[] bytes, boolean isRequisicaoDePJE, 
			DocumentoProcessual documento) {
		
		ProcessoDocumentoBinPessoaAssinatura pdbpa = new ProcessoDocumentoBinPessoaAssinatura();
		pdbpa.setProcessoDocumentoBin(processoDocumentoBin);
		pdbpa.setAssinatura(assinatura);
		pdbpa.setCertChain(certChain);
		pdbpa.setDataAssinatura(dataAssinatura);
		pdbpa.setAssinaturaCMS(assinaturaCMS);
		pdbpa.setAlgoritmoDigest(obterAlgoritmoDigest(algoritmoDigest));
		
		/*
		 * Adicionado verificação para saber se a assinatura foi
		 * realizada em modo teste, pois nessa situação o
		 * ValidaDocumento não funciona.
		 */
		if (AssinaturaUtil.isModoTeste(assinatura)) {
			if (ParametroUtil.instance().isAplicacaoModoProducao()) {
				String mensagem = "Não é possível assinar documento no modo teste em ambiente de produção. Documento: %s";
				String doc = documento.getTipoDocumento() +" - "+ documento.getDescricao();
				throw new NegocioException(String.format(mensagem, doc));
			}
			pdbpa.setNomePessoa(ASSINATURA_TESTE);
		} else {
			Certificado certificadoDigital = obterCertificadoDigital(certChain);
			pdbpa.setNomePessoa(certificadoDigital.getNome());
			
			if(!isRequisicaoDePJE){			
				// Validar a assinatura digital do documento
				validarAssinaturaDocumento(bytes, pdbpa, certChain,
						assinatura,
						CodificacaoCertificado.PKI_PATH.getValor(),
						algoritmoDigest);
			}
			pdbpa.setPessoa(extrairDadosPessoaCertificado(certificadoDigital));
		}
		return pdbpa;
	}
	
	/**
	 * Processa as assinaturas PAdES de um documento PDF e retorna uma lista de signatários.
	 * 
	 * @param processoDocumentoBin
	 * @param bytes
	 * @return List<ProcessoDocumentoBinPessoaAssinatura>
	 */
	protected List<ProcessoDocumentoBinPessoaAssinatura> processarNovoProcessoDocumentoBinPDFPessoaAssinaturaPAdES(
			ProcessoDocumentoBin processoDocumentoBin, byte[] bytes) {
		
		List<ProcessoDocumentoBinPessoaAssinatura> signatarios = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>();
		for (PdfPKCS7 pk : extrairAssinaturasPAdES(bytes)) {
			validarAssinaturaPAdES(pk);
			
			ProcessoDocumentoBinPessoaAssinatura pdbpa = new ProcessoDocumentoBinPessoaAssinatura();
			pdbpa.setProcessoDocumentoBin(processoDocumentoBin);
			pdbpa.setAssinatura("PAdES");
			pdbpa.setCertChain(obterCertChainPAdES(pk));
			pdbpa.setDataAssinatura(pk.getSignDate().getTime());
			pdbpa.setAssinaturaCMS(true);
			pdbpa.setAlgoritmoDigest(pk.getDigestAlgorithm());

			Certificado certificadoDigital = obterCertificadoDigitalPAdES(pk);
			pdbpa.setNomePessoa(certificadoDigital.getNome());

			pdbpa.setPessoa(extrairDadosPessoaCertificado(certificadoDigital));

			signatarios.add(pdbpa);
		}
		return signatarios;
	}
	
	/**
	 * Realiza a extracao de dados da pessoa do Certificado.
	 * Caso seja possivel recuperar o CPF a partir do certificado digital os dados da pessoa sao
	 * gravados no banco de dados e ela eh adicionada ao ProcessoDocumentoBinPessoaAssinatura.
	 * 
	 * @param pdbpa
	 * @param certificadoDigital
	 */
	private Pessoa extrairDadosPessoaCertificado(Certificado certificadoDigital) {
		Pessoa pessoa = null;
		String numeroCpfCertificado = null;
		String numeroCnpjCertificado = null;

		try {
			DadosCertificado dadosCertificado = DadosCertificado.parse(certificadoDigital);
			numeroCpfCertificado = dadosCertificado.getValor(DadosCertificado.CPF);
			numeroCnpjCertificado = dadosCertificado.getValor(DadosCertificado.PJ_CNPJ);
			
			// Se for possivel recuperar o CPF a partir do certificado digital...
			if (StringUtils.isBlank(numeroCnpjCertificado) && StringUtils.isNotBlank(numeroCpfCertificado)) {
				// Recuperar os dados da pessoa a partir do CPF
				pessoa = pessoaService.findByInscricaoMF(numeroCpfCertificado);
				// Gravar os dados no banco, caso eles ainda não existam
				if (pessoa.getIdPessoa() == null){
					pessoaService.persist(pessoa);
				}
			}
		} catch (PJeBusinessException e) {
			String mensagem = (String) ObjectUtils.defaultIfNull(e.getMessage(), e.getLocalizedMessage());
			throw new NegocioException(String.format(
				"Falha na recuperação dos dados da pessoa portadora do CPF: %s. Erro: %s", numeroCpfCertificado, mensagem));
			
		} catch (CertificadoException e) {
			throw new NegocioException(String.format(
				"Falha na validação da assinatura do documento, verifique se o certificado foi enviado no formato válido e se a assinatura está no formato base64. Erro: %s",
				e.getMessage()));
		}
		return pessoa;
	}

	/**
	 * Realiza a extracao das assinaturas PAdES de um documento PDF.
	 * Apenas retorna assinaturas que cobrem o documento inteiro.
	 * 
	 * @param bytesDocumentoPDF
	 * @return
	 */
	private List<PdfPKCS7> extrairAssinaturasPAdES(byte[] bytesDocumentoPDF) {
		List<PdfPKCS7> assinaturasPAdES = new ArrayList<PdfPKCS7>();
		PdfReader pdfReader = lerArquivoPDF(bytesDocumentoPDF);
		AcroFields acroFields = pdfReader.getAcroFields();
		List<String> signatureNames = acroFields.getSignatureNames();
		Security.addProvider(new BouncyCastleProvider());


		if (!signatureNames.isEmpty()) {
			for (String name : signatureNames) {
				PdfPKCS7 pk = acroFields.verifySignature(name);
				assinaturasPAdES.add(pk);
			}

			String lastSignatureName = signatureNames.get(signatureNames.size() - 1);
			if (!acroFields.signatureCoversWholeDocument(lastSignatureName)) {
				throw new PJeRuntimeException(
						"Falha na obteção das assinaturas no formato PAdES. A última assinatura deve cobrir todo o documento.");
			}
		}
		return assinaturasPAdES;
	}
	
	/**
	 * Realiza a leitura de um documento PDF atraves do leitor com.lowagie.text.pdf.PdfReader.
	 * 
	 * @param bytes
	 * @return PdfReader
	 */
	private PdfReader lerArquivoPDF(byte[] bytes) {
		PdfReader pdfReader = null;
		try {
			pdfReader = new PdfReader(bytes);
		} catch (Exception e) {
                        e.printStackTrace();
			throw new NegocioException(String.format("Falha na leitura do documento PDF. Erro: %s", e.getMessage()));
		}
		return pdfReader;
	}
	
	/**
	 * Realiza a verificacao de assinatura de um PKCS7 extraido de um documento PDF com assinatura PAdES.
	 * 
	 * @param pk
	 */
	private void validarAssinaturaPAdES(PdfPKCS7 pk) {
		try {
			if (!pk.verify()) {
				throw new NegocioException(String.format(
					"Falha na validação da assinatura do documento, verifique se o certificado foi enviado no formato válido e se a assinatura está no formato base64. Erro: %s",
					"Verificação da assinatura indicou alteração no documento."));
			}
                } catch (NegocioException e){
                    throw e;
		} catch (Exception e) {
                        e.printStackTrace();
			throw new NegocioException(String.format(
				"Falha na validação da assinatura do documento, verifique se o certificado foi enviado no formato válido e se a assinatura está no formato base64. Erro: %s",
				e.getMessage()));
		}
	}
	
	/**
	 * Obtem a cadeia de certificados em Base64 de um PKCS7 extraido de um documento PDF com assinatura PAdES.
	 * 
	 * @param pk
	 * @return
	 */
	private String obterCertChainPAdES(PdfPKCS7 pk){
		try {
			Certificate[] cadeia = pk.getCertificates();
			List<Certificate> certList = Arrays.asList(cadeia);
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			CertPath certPath = certFactory.generateCertPath(certList);
			byte[] encodedCertChain = certPath.getEncoded();

			return new Base64(64).encodeToString(encodedCertChain);
		} catch (Exception e) {
			throw new IntercomunicacaoException(String.format(
				"Não foi possível fazer o parser do certificado do documento para o formato base64. Erro: %s", e.getMessage()));
		}
	}
	
	/**
	 * Obtem o certificado digital de um PKCS7 extraido de um documento PDF com assinatura PAdES.
	 * 
	 * @param pk
	 * @return
	 */
	private Certificado obterCertificadoDigitalPAdES(PdfPKCS7 pk){		
		try {
			Certificate[] certificados = pk.getCertificates();
			Certificado certificadoDigital = new Certificado(certificados);
			return certificadoDigital;
		} catch (CertificadoException e) {
			throw new NegocioException(String.format(
					"Falha ao obter os dados do certificado do documento. Erro: %s", e.getMessage()));
		}  
	}
	
	/**
	 * Se o documento estiver presente será executada a validação da assinatura.
	 * 
	 * @param documento
	 * @param pdbpa
	 *            ProcessoDocumentoBinPessoaAssinatura
	 * @param assinatura
	 *            Assinatura
	 * @throws CertificadoException
	 * @throws ValidaDocumentoException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws InvalidKeyException 
	 */
	protected void validarAssinaturaDocumento(byte[] documento,
			ProcessoDocumentoBinPessoaAssinatura pdbpa, String certificado,
			String assinaturaBase64, String codificacaoCertificado,
			String algoritmoHash) {

		try {
			if (documento != null && documento.length > 0 && pdbpa.getAssinaturaCMS() == false) {
				ValidaDocumento validaDocumento = new ValidaDocumento(documento,
						certificado, assinaturaBase64, codificacaoCertificado);
				X509Certificate certificate = validaDocumento.getDadosCertificado().getMainCertificate();
				PublicKey publicKey = certificate.getPublicKey();

				Base64 decoder = new Base64();
				byte[] assinaturaByte = decoder.decode(assinaturaBase64);
				
				boolean verify = Signer.verify(publicKey,
						SignatureAlgorithm.valueOf(algoritmoHash), documento,
						assinaturaByte);
				if (verify == false) {
					throw new NegocioException("assinatura não corresponde aos dados do certificado.");
				}
			}
		} catch (Exception e) {
			throw new NegocioException(
					"Falha na validação da assinatura do documento, "
							+ "verifique se o certificado foi enviado no formato válido e se a "
							+ "assinatura está no formato base64, erro: "
							+ e.getMessage());
		}
	}
	
	/**
	 * Valida os campos da assinatura.
	 * 
	 * @param assinatura
	 */
	@SuppressWarnings("unchecked")
	protected void validarAssinatura(Assinatura assinatura) {
		
		if (assinatura == null || StringUtils.isBlank(assinatura.getAssinatura())) {
			throw new NegocioException("Assinatura do documento não informada.");
		}
		
		if (StringUtils.isBlank(assinatura.getCadeiaCertificado())) {
			throw new NegocioException("Certificado não atribuído.");
		}

		if (StringUtils.isBlank(assinatura.getCodificacaoCertificado()) == false) {
			String codificacao = assinatura.getCodificacaoCertificado();
			Map<String, String> mapa = new CaseInsensitiveMap();
			mapa.put(CodificacaoCertificado.PEM.name(), CodificacaoCertificado.PEM.name());
			mapa.put(CodificacaoCertificado.PKI_PATH.name(), CodificacaoCertificado.PKI_PATH.name());
			mapa.put(CodificacaoCertificado.PEM.getValor(), CodificacaoCertificado.PEM.name());
			mapa.put(CodificacaoCertificado.PKI_PATH.getValor(), CodificacaoCertificado.PKI_PATH.name());
			if (mapa.containsKey(codificacao) == false) {
				String mensagem = "A codificação do certificado '%s' não é reconhecida, os valores válidos são: '%s'.";
				throw new NegocioException(String.format(mensagem, codificacao, mapa.values().toString()));
			} else {
				assinatura.setCodificacaoCertificado(mapa.get(codificacao));
			}
		} else {
			assinatura.setCodificacaoCertificado(CodificacaoCertificado.PEM.name());
		}
		
		if (StringUtils.isBlank(assinatura.getAlgoritmoHash()) == false) {
			String algoritmo = assinatura.getAlgoritmoHash();
			Map<String, String> mapa = new CaseInsensitiveMap();
			mapa.put(SignatureAlgorithm.ASN1MD5withRSA.name(), SignatureAlgorithm.ASN1MD5withRSA.name());
			mapa.put(SignatureAlgorithm.MD5withRSA.name(), SignatureAlgorithm.MD5withRSA.name());
			mapa.put(SignatureAlgorithm.SHA1withRSA.name(), SignatureAlgorithm.SHA1withRSA.name());
			mapa.put(SignatureAlgorithm.SHA256withRSA.name(), SignatureAlgorithm.SHA256withRSA.name());
			mapa.put("MD5", SignatureAlgorithm.MD5withRSA.name());
			mapa.put("SHA-1", SignatureAlgorithm.SHA1withRSA.name());
			mapa.put("SHA-256", SignatureAlgorithm.SHA256withRSA.name());
			mapa.put("SHA1", SignatureAlgorithm.SHA1withRSA.name());
			mapa.put("SHA256", SignatureAlgorithm.SHA256withRSA.name());
			
			if (mapa.containsKey(algoritmo) == false) {
				String mensagem = "O algoritmo '%s' não é reconhecido, os valores válidos são: '%s'.";
				throw new NegocioException(String.format(mensagem, algoritmo, mapa.values().toString()));
			} else {
				assinatura.setAlgoritmoHash(mapa.get(algoritmo));
			}
		} else {
			assinatura.setAlgoritmoHash(SignatureAlgorithm.SHA256withRSA.name());
		}
	}
	
	/**
	 * Retorna os dados do certificado digital passado por parâmetro.
	 * @param certChain String do certificado no formato PEM ou Base64 (PkiPath)
	 * @return Dados do certificado digital
	 */
	protected Certificado obterCertificadoDigital(String certChain) {
		try {
			CodificacaoCertificado codificacao = 
					DigitalSignatureUtils.getCodificacaoCertificado(certChain, true);
			X509Certificate[] certificados = 
					DigitalSignatureUtils.loadCertPath(certChain, codificacao.getValor());
			return new Certificado(certificados);
		} catch (CertificadoException e) {
			String mensagem = "Falha ao obter os dados do certificado do documento, erro %s";
			throw new NegocioException(String.format(mensagem, e.getMessage()));
		}
	}
}
