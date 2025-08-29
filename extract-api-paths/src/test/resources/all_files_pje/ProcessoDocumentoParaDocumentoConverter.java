/**
 * ProcessoDocumentoParaDocumentoConverter.java
 * 
 * Data de criação: 23/09/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import br.com.infox.cliente.util.ParametroUtil;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Transformer;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.core.certificado.util.CodificacaoCertificado;
import br.com.infox.core.certificado.util.DigitalSignatureUtils;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.intercomunicacao.v222.beans.Assinatura;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.intercomunicacao.v222.servico.IntercomunicacaoService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.DocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteDestinatario;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;
import br.jus.pje.nucleo.util.Crypto;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Conversor de ProcessoDocumento para Documento da intercomunicação.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name(ProcessoDocumentoParaDocumentoConverter.NAME)
public class ProcessoDocumentoParaDocumentoConverter
		extends
		IntercomunicacaoConverterAbstrato<ProcessoDocumento, DocumentoProcessual> {

	public static final String NAME = "v222.processoDocumentoParaDocumentoConverter";
	
	private static final String CHARSET_UTF_8 = "UTF-8";
	private static final String CHARSET_ISO_8859_1 = "ISO-8859-1";
	public static final String HTML_MIME_TYPE = "text/html";

	@In
	private PessoaProcuradoriaManager pessoaProcuradoriaManager;

	@In
	private ProcessoJudicialService processoJudicialService;

	@In
	private ProcessoEventoManager processoEventoManager;

	@In
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	@In
	private DocumentoBinManager documentoBinManager;
	
	@Logger
	private Log log;
	
	private final String TODOS = "*";

	@Override
	public DocumentoProcessual converter(ProcessoDocumento objeto) {
		List<String> listaIdCarregarBinario = new ArrayList<String>();
		listaIdCarregarBinario.add(TODOS);
		return converter(objeto, false, listaIdCarregarBinario);
	}

	/**
	 * Converte um ProcessoDocumento para DocumentoProcessual com a opção de
	 * passar se os binários serão carregados.
	 * 
	 * @param processoDocumento
	 * @param listaIdCarregarBinario Lista dos ID's dos documentos que irão carregar o binário
	 *            Booleano se os documentos serão carregados.
	 * @return DocumentoProcessual
	 */
	public DocumentoProcessual converter(ProcessoDocumento processoDocumento,
			List<String> listaIdCarregarBinario) {
		
		ProcessoParteExpediente processoParteExpediente = null;	
		List<ProcessoParteExpediente> processosPartesExpedientes = getProcessoParteExpedienteManager()
				.getPartesDoExpedienteBy(processoDocumento.getIdProcessoDocumento(), true);
		
		/* Encontra o processoParteExpediente correspondente ao usuário logado. 
		Ele pode ser a parte ou um de seus representantes, incluindo os procuradores */
		if (CollectionUtils.isNotEmpty(processosPartesExpedientes)){
			for (ProcessoParteExpediente procParteExpediente : processosPartesExpedientes) {
				if (usuarioLogadoEstaRelacionadoAoExpediente(procParteExpediente)){
					processoParteExpediente = procParteExpediente;
					break;
				}
			}
		}
		
		boolean ignorarAutenticacao = IntercomunicacaoService.getInstance().getIgnoraAutenticacao(); 
		boolean foiEncontradoExpedienteDoUsuarioLogado = processoParteExpediente != null;
		
		boolean pendenteCiencia = (foiEncontradoExpedienteDoUsuarioLogado
				&& processoParteExpediente.getFechado() == false
				&& processoParteExpediente.getDtCienciaParte() == null
				&& !(processoParteExpediente.getTipoPrazo() == TipoPrazoEnum.S)
				&& !ignorarAutenticacao);

		return converter(processoDocumento, pendenteCiencia, listaIdCarregarBinario);
	}
	
	private boolean usuarioLogadoEstaRelacionadoAoExpediente(ProcessoParteExpediente processoParteExpediente){
		boolean estaRelacionado = false;
		
		int idUsuarioLogado = Authenticator.getUsuarioLogado().getIdUsuario();
		
		if (processoParteExpediente.getPessoaParte() != null && processoParteExpediente.getPessoaParte().getIdUsuario() == idUsuarioLogado) {
			estaRelacionado = true;
		} else if (processoParteExpediente.getProcuradoria() != null && pessoaProcuradoriaManager.getPessoaProcuradoria(idUsuarioLogado, processoParteExpediente.getProcuradoria().getIdProcuradoria()) != null) {
			estaRelacionado = true;
		} else if (Authenticator.getPapelAtual() != null && Authenticator.getPapelAtual().equals(ParametroUtil.instance().getPapelAdvogado())){
			try{
				if (processoJudicialService.isAdvogado(processoParteExpediente.getProcessoJudicial(), Authenticator.getPessoaLogada(), processoParteExpediente.getPessoaParte())){
					estaRelacionado = true;
				}
			} catch (PJeBusinessException pjeBusinessException){
				String mensagem = String.format("Não foi possível verificar se o usuário é representante de parte do processo.");
				log.error(mensagem, pjeBusinessException);
				throw new IntercomunicacaoException(mensagem, pjeBusinessException);
			}
		} else {
			for (ProcessoParteExpedienteDestinatario destinatario : processoParteExpediente.getDestinatarioList()) {
				if (destinatario.getIdDestinatario() == idUsuarioLogado) {
					estaRelacionado = true;
				}
			}
		}
		return estaRelacionado;
	}

	
	public DocumentoProcessual converter(ProcessoDocumento processoDocumento, boolean pendenteCiencia, List<String> listaIdCarregarBinario) {
		return converter(processoDocumento, pendenteCiencia, listaIdCarregarBinario, Boolean.TRUE);
	}
	/**
	 * Converte um ProcessoDocumento para DocumentoProcessual com a opção de
	 * passar se os binários serão carregados e se o conteúdo será
	 * criptografado.
	 * 
	 * @param processoDocumento
	 * @param listaIdCarregarBinario
	 *            Lista dos ID's dos documentos que irão carregar o conteúdo binário.
	 * @param pendenteCiencia
	 *            Boleano que indica se o conteúdo do documento está 
	 *            pendente de visualização por fata de ciência dos destinatários.
	 * @return DocumentoProcessual
	 */
	public DocumentoProcessual converter(ProcessoDocumento processoDocumento, boolean pendenteCiencia, List<String> listaIdCarregarBinario, boolean carregarSigiloso) {
		return converter(processoDocumento, pendenteCiencia, listaIdCarregarBinario, carregarSigiloso, false);
	}
	
	private DocumentoProcessual converter(ProcessoDocumento processoDocumento, boolean pendenteCiencia, List<String> listaIdCarregarBinario, boolean carregarSigiloso, boolean isRequisicaoPJe) {
		DocumentoProcessual documento = null;

		if (isNotNull(processoDocumento) && (processoDocumento.getDocumentoSigiloso() != true || carregarSigiloso)) {
			documento = new DocumentoProcessual();
			documento.setIdDocumentoVinculado(obterIdDocumentoVinculado(processoDocumento));
			documento.setIdDocumento(converterParaString(processoDocumento.getIdProcessoDocumento()));
			documento.setDescricao(processoDocumento.getProcessoDocumento());
			if (isNotNull(processoDocumento.getDataJuntada())) {
				documento.setDataHora(converterParaDataHora(processoDocumento.getDataJuntada()));
			} else {
				documento.setDataHora(converterParaDataHora(processoDocumento.getDataInclusao()));
			}
			documento.setTipoDocumento(processoDocumento.getTipoProcessoDocumento().getCodigoDocumento());
			documento.setNivelSigilo(obterNivelSigilo(processoDocumento));
			documento.setDescricao(processoDocumento.getProcessoDocumento());
			Integer movimento = obterMovimento(processoDocumento);
			if (isNotNull(movimento)) {
				documento.setMovimento(movimento);
			}
			documento.getDocumentoVinculado().addAll(obterColecaoDocumentosVinculados(
					processoDocumento, pendenteCiencia, listaIdCarregarBinario, carregarSigiloso, isRequisicaoPJe));
			
			ProcessoDocumentoBin processoDocumentoBin = processoDocumento.getProcessoDocumentoBin();
			documento.setHash(processoDocumentoBin.getMd5Documento());
			ProcessoDocumentoBinPessoaAssinaturaManager assinaturaManager = 
					(ProcessoDocumentoBinPessoaAssinaturaManager) Component.getInstance("processoDocumentoBinPessoaAssinaturaManager");
			List<ProcessoDocumentoBinPessoaAssinatura> assinaturas = assinaturaManager.getAssinaturaDocumento(processoDocumentoBin);
			for(ProcessoDocumentoBinPessoaAssinatura assina : assinaturas){
				Assinatura assinatura = new Assinatura();
				assinatura.setAssinatura(assina.getAssinatura());
				assinatura.setCadeiaCertificado(assina.getCertChain());
				assinatura.setDataAssinatura(converterParaDataHora(assina.getDataAssinatura()));
				assinatura.setAlgoritmoHash(assina.getAlgoritmoDigest());
				assinatura.setCodificacaoCertificado(obterCodificacaoCertificado(assina));
				documento.getAssinatura().add(assinatura);
			}
			
			String modeloDocumento = processoDocumentoBin.getModeloDocumento();
			String mime = processoDocumentoBin.getExtensao();
			// PJEII-5054 carregar apenas os metadados do documento quando
			// carregarBinario == false
			carregarBinario(listaIdCarregarBinario, pendenteCiencia, documento, modeloDocumento, mime, processoDocumentoBin, isRequisicaoPJe);
			if ((mime==null) && (documento.getConteudo()!=null))
				mime = documento.getConteudo().getContentType();
			documento.setMimetype(mime);
		}

		return documento;
	}
	
	private void carregarBinario(List<String> listaIdCarregarBinario, boolean pendenteCiencia, DocumentoProcessual documento, String modeloDocumento, String mime, ProcessoDocumentoBin processoDocumentoBin, boolean isRequisicaoPJe){
		
		if (isRequisicaoPJe){
			carregarConteudoDocumentosRemessa(documento, modeloDocumento, mime, processoDocumentoBin);

		}else{
			carregarConteudoDocumentos(listaIdCarregarBinario, pendenteCiencia, documento, modeloDocumento, mime, processoDocumentoBin);
		}
	}
	
	private void carregarConteudoDocumentosRemessa(DocumentoProcessual documento, String modeloDocumento, String mime, ProcessoDocumentoBin processoDocumentoBin) {
		byte[] conteudo = null;
		
		if (isVazio(modeloDocumento)) {
			mime = processoDocumentoBin.getExtensao();
		} else
		{
			try {
				conteudo = obterConteudoModeloDocumento(processoDocumentoBin);
			} catch (UnsupportedEncodingException e) {
				String mensagem = String.format("Não foi possível carregar o conteúdo do documento. Documento: %s; DocumentoBin: %s: Storage: %s", 
						documento.getIdDocumento(),
						processoDocumentoBin.getIdProcessoDocumentoBin(),
						processoDocumentoBin.getNumeroDocumentoStorage());					
				log.error(mensagem);
				throw new IntercomunicacaoException(mensagem, e);
			}
			mime = HTML_MIME_TYPE;
		}
		documento.setHash(processoDocumentoBin.getMd5Documento());
		documento.setConteudo(ProjetoUtil.converterParaDataHandler(conteudo, mime));//Informa o mimetype do documento.
		
	}

	private void carregarConteudoDocumentos(List<String> listaIdCarregarBinario, boolean pendenteCiencia, DocumentoProcessual documento, String modeloDocumento, String mime, ProcessoDocumentoBin processoDocumentoBin) {
		String id = documento.getIdDocumento();
		Boolean carregarBinario = listaIdCarregarBinario.contains(id) || listaIdCarregarBinario.contains(TODOS);
		
		if (carregarBinario) {
			byte[] conteudo = null;
			
			try {
				if (isVazio(modeloDocumento)) {
					conteudo = documentoBinManager.getData(processoDocumentoBin.getNumeroDocumentoStorage());
					mime = processoDocumentoBin.getExtensao();
				} else
				{
					conteudo = obterConteudoModeloDocumento(processoDocumentoBin);
					mime = HTML_MIME_TYPE;
				}
			} catch (Exception e) {
				String mensagem = String.format("Não foi possível carregar o conteúdo do documento. Documento: %s; DocumentoBin: %s: Storage: %s", 
				documento.getIdDocumento(),
				processoDocumentoBin.getIdProcessoDocumentoBin(),
				processoDocumentoBin.getNumeroDocumentoStorage());
			
				log.error(mensagem);
				throw new IntercomunicacaoException(mensagem, e);
			}
	
			if (pendenteCiencia) {
				final String PENDENTE_CIENCIA = "Visualização do documento bloqueada enquanto não houver ciência de seus destinatários";
				conteudo = PENDENTE_CIENCIA.getBytes(Charset.forName("UTF-8"));
			} else {
				// PJEII-11881 não enviar o hash (chave para decriptar)
				documento.setHash(processoDocumentoBin.getMd5Documento());
			}

			documento.setConteudo(StringUtils.isBlank(mime) ? 
				ProjetoUtil.converterParaDataHandler(conteudo) : 
				ProjetoUtil.converterParaDataHandler(conteudo, mime));
		}
	
	}

	public DocumentoProcessual converterParaRemessa(ProcessoDocumento processoDocumento, boolean pendenteCiencia, List<String> listaIdCarregarBinario, boolean carregarSigiloso) {
		return converter(processoDocumento, pendenteCiencia, listaIdCarregarBinario, carregarSigiloso, true);
	}

	/**
	 * Converte uma coleção de objetos para uma outra coleção de objetos do tipo
	 * de destino.
	 * 
	 * @param colecaoObjeto
	 *            Coleção de objetos de origem.
	 * @param listaIdCarregarBinario Lista dos ID's dos documentos que irão carregar o binário
	 *            Booleano se os documentos serão carregados.
	 * @return Coleção de objetos de destino.
	 */
	public List<DocumentoProcessual> converterColecao(
			List<ProcessoDocumento> colecaoObjeto, List<String> listaIdCarregarBinario) {
		Transformer transformer = novoTransformador(listaIdCarregarBinario);
		return aplicarTransformador(colecaoObjeto, transformer);
	}

	/**
	 * Converte uma coleção de objetos para uma outra coleção de objetos do tipo
	 * de destino.
	 * 
	 * @param colecaoObjeto
	 *            Coleção de objetos de origem.
	 * @param pendenteCiencia
	 * @param listaIdCarregarBinario Lista dos ID's dos documentos que irão carregar o binário
	 *            Booleano se os documentos serão carregados.
	 * @return Coleção de objetos de destino.
	 */
	public List<DocumentoProcessual> converterColecao(
			List<ProcessoDocumento> colecaoObjeto, boolean pendenteCiencia,
			List<String> listaIdCarregarBinario) {
		Transformer transformer = novoTransformador(pendenteCiencia, listaIdCarregarBinario);
		return aplicarTransformador(colecaoObjeto, transformer);
	}

	/**
	 * Retorna um novo transformador de objetos que é usado pela conversão de
	 * coleção.
	 * 
	 * @return novo transformador de objeto.
	 * @param pendenteCiencia
	 * @param listaIdCarregarBinario Lista dos ID's dos documentos que irão carregar o binário
	 *            Booleano se os documentos serão carregados.
	 */
	private Transformer novoTransformador(final boolean pendenteCiencia,
			final List<String> listaIdCarregarBinario) {
		return new Transformer() {
			@Override
			public DocumentoProcessual transform(Object input) {
				return converter((ProcessoDocumento) input, pendenteCiencia,
						listaIdCarregarBinario);
			}
		};
	}

	/**
	 * Retorna um novo transformador de objetos que é usado pela conversão de
	 * coleção.
	 * 
	 * @return novo transformador de objeto.
	 * @param listaIdCarregarBinario Lista dos ID's dos documentos que irão carregar o binário
	 *            Booleano se os documentos serão carregados.
	 */
	private Transformer novoTransformador(final List<String> listaIdCarregarBinario) {
		return new Transformer() {
			@Override
			public DocumentoProcessual transform(Object input) {
				return converter((ProcessoDocumento) input, listaIdCarregarBinario);
			}
		};
	}

	/**
	 * @param processoDocumento
	 * @return id do movimento
	 */
	private Integer obterMovimento(ProcessoDocumento processoDocumento) {
		ProcessoEvento processoEvento = getProcessoEventoManager()
				.findByDocumento(processoDocumento);
		return (isNotNull(processoEvento) ? processoEvento
				.getIdProcessoEvento() : null);
	}

	/**
	 * @param objeto
	 * @param processoDocumento
	 * @return nível de sigilo.
	 */
	private int obterNivelSigilo(ProcessoDocumento processoDocumento) {
		return (isNotNull(processoDocumento.getDocumentoSigiloso())
				&& processoDocumento.getDocumentoSigiloso() ? 5 : 0);
	}
	
	/**
	 * Retorna a codificação do certificado.
	 * 
	 * @param assina ProcessoDocumentoBinPessoaAssinatura
	 * @return codificação do certificado PEM ou PkiPath.
	 */
	private String obterCodificacaoCertificado(
			ProcessoDocumentoBinPessoaAssinatura assina) {
		CodificacaoCertificado resultado = null;
		
		String certChain = assina.getCertChain();
		resultado = DigitalSignatureUtils.getCodificacaoCertificado(certChain, true);
		return resultado.getValor();
	}

	/**
	 * Retorna o ID do documento pai.
	 * 
	 * @param processoDocumento ProcessoDocumento
	 * @return ID do documento pai.
	 */
	private String obterIdDocumentoVinculado(ProcessoDocumento processoDocumento) {
		String resultado = null;
		
		if (isNotNull(processoDocumento.getDocumentoPrincipal())) {
			ProcessoDocumento documentoPrincipal = processoDocumento.getDocumentoPrincipal();
			resultado = converterParaString(documentoPrincipal.getIdProcessoDocumento());
		}
		return resultado;
	}

	/**
	 * @return the processoEventoManager
	 */
	private ProcessoEventoManager getProcessoEventoManager() {
		return processoEventoManager;
	}

	/**
	 * @return the processoParteExpedienteManager
	 */
	private ProcessoParteExpedienteManager getProcessoParteExpedienteManager() {
		return processoParteExpedienteManager;
	}
	
	/**
	 * Retorna o conteúdo do atributo ProcessoDocumentoBin.modeloDocumento, o array de bytes será 
	 * primeiramente recuperado com charset UTF-8, se o hash não bater o array de bytes será 
	 * recuperado com charset ISO-8859-1.
	 * 
	 * @param bin
	 * @return Array de bytes do documento.
	 * @throws UnsupportedEncodingException
	 */
	private byte[] obterConteudoModeloDocumento(ProcessoDocumentoBin bin) 
			throws UnsupportedEncodingException {
		byte[] resultado = null;
		
		if (isNotNull(bin) && isNotVazio(bin.getModeloDocumento())) {
			String md5 = bin.getMd5Documento();
			String conteudoString = bin.getModeloDocumento();
			
			resultado = conteudoString.getBytes(CHARSET_UTF_8);
			
			if (Crypto.isMD5Valido(resultado, md5) == Boolean.FALSE) {
				resultado = conteudoString.getBytes(CHARSET_ISO_8859_1);
			}
		}
		return resultado;
	}
			
	/**
	 * Retorna a coleção de documentos vinculados.
	 * 
	 * @param documento
	 * @return documentos vinculados.
	 */
	private Collection<DocumentoProcessual> obterColecaoDocumentosVinculados(ProcessoDocumento processoDocumento, boolean pendenteCiencia, List<String> listaIdCarregarBinario, boolean carregarSigiloso, boolean isRequisicaoPJe) {

		Collection<DocumentoProcessual> resultado = new ArrayList<DocumentoProcessual>();
		Set<ProcessoDocumento> vinculados = processoDocumento.getDocumentosVinculados();
		
		if (ProjetoUtil.isNotVazio(vinculados)) {
			for (ProcessoDocumento vinculado : vinculados) {
				if (vinculado.equals(processoDocumento) || !vinculado.getAtivo() || vinculado.getDataJuntada() == null){
					continue;
				}
				//só retorna dos vinculados que estão assinados
				if(!vinculado.getProcessoDocumentoBin().getSignatarios().isEmpty()){
					if (isRequisicaoPJe){
						resultado.add(converterParaRemessa(vinculado, pendenteCiencia, listaIdCarregarBinario, carregarSigiloso));
					}else{
						resultado.add(converter(vinculado, pendenteCiencia, listaIdCarregarBinario, carregarSigiloso));
					}
				}
			}
		}
		return resultado;
	}
}
