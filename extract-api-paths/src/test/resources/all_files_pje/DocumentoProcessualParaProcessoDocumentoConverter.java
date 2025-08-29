/**
 * DocumentoProcessualParaProcessoDocumentoConverter.java
 * 
 * Data de criação: 24/11/2013
 */
package br.jus.cnj.pje.intercomunicacao.v222.converter;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.exceptions.NegocioException;
import br.jus.cnj.intercomunicacao.v222.beans.DocumentoProcessual;
import br.jus.cnj.pje.intercomunicacao.exception.IntercomunicacaoException;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.intercomunicacao.v222.util.ConversorUtil;
import br.jus.cnj.pje.intercomunicacao.v222.util.MNIParametroUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TipoProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.util.ArrayUtil;

/**
 * Conversor de DocumentoProcessual para ProcessoDocumento.
 * 
 * @author Adriano Pamplona
 */
@AutoCreate
@Name(DocumentoProcessualParaProcessoDocumentoConverter.NAME)
public class DocumentoProcessualParaProcessoDocumentoConverter
		extends
		IntercomunicacaoConverterAbstrato<DocumentoProcessual, ProcessoDocumento> {

	public static final String NAME = "v222.documentoProcessualParaProcessoDocumentoConverter";
	
	@In
	private TipoProcessoDocumentoManager tipoProcessoDocumentoManager;
	@In (value = DocumentoProcessualParaProcessoDocumentoBinConverter.NAME)
	private DocumentoProcessualParaProcessoDocumentoBinConverter documentoProcessualParaProcessoDocumentoBinConverter;
	@In
	private ParametroService parametroService;
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@Logger
	private Log log;

	/**
	 * Use o método converter(DocumentoProcessual, ProcessoTrf,
	 * ProcessoDocumento)
	 * 
	 * @see br.jus.cnj.pje.intercomunicacao.converter.IntercomunicacaoConverterAbstrato#converter(java.lang.Object)
	 */
	@Override
	@Deprecated
	public ProcessoDocumento converter(DocumentoProcessual objeto) {
		return null;
	}

	/**
	 * Use o método converter(DocumentoProcessual, ProcessoTrf)
	 * 
	 * @see br.jus.cnj.pje.intercomunicacao.converter.IntercomunicacaoConverterAbstrato#converter(java.lang.Object)
	 */
	@Override
	@Deprecated
	public List<ProcessoDocumento> converterColecao(
			List<DocumentoProcessual> colecaoObjeto) {
		return null;
	}

	public ProcessoDocumento converter(DocumentoProcessual documento,
			Processo processo, ProcessoDocumento documentoPrincipal,
			boolean processarVinculados, boolean isRequisicaoDePJE) {
		ProcessoDocumento resultado = null;

		if (isNotNull(documento, processo)) {

			resultado = new ProcessoDocumento();
			resultado.setAtivo(true);
			resultado.setProcesso(processo);
			resultado.setDataInclusao(obterDataInclusao(documento));
			resultado.setDataAlteracao(null);
			resultado.setDataExclusao(null);
			resultado.setDataJuntada(obterDataJuntada(documento));
			resultado.setDocumentoSigiloso(documento.isSetNivelSigilo() ? documento.getNivelSigilo() > 0 : false);
			resultado.setUsuarioAlteracao(null);
			resultado.setUsuarioExclusao(null);
			resultado.setUsuarioInclusao(obterUsuarioLogado());
			resultado.setUsuarioJuntada(resultado.getUsuarioInclusao());
			resultado.setNomeUsuarioAlteracao(null);
			resultado.setNomeUsuarioExclusao(null);
			resultado.setNomeUsuarioInclusao(obterUsuarioLogado().getNome());
			resultado.setTipoProcessoDocumento(obterTipoProcessoDocumento(documento.getTipoDocumento()));
			resultado.setDocumentoPrincipal(obterDocumentoPrincipal(documento, documentoPrincipal));
			resultado.setInstancia(obterInstancia(documento));
			resultado.setProcessoDocumentoBin(obterProcessoDocumentoBin(documento, resultado, isRequisicaoDePJE));
			resultado.setNumeroDocumento(obterNumeroDocumento(documento));
			resultado.setIdInstanciaOrigem(obterIdInstanciaOrigem(documento));
			resultado.setProcessoDocumento(obterNomeProcessoDocumento(documento,
					resultado.getTipoProcessoDocumento()));
			resultado.setNumeroOrdem(obterNumeroOrdem(documento, resultado.getDocumentoPrincipal()));
			
			carregarCampoAny(documento);
			if (processarVinculados) {
				resultado.getDocumentosVinculados().addAll(
						obterColecaoDocumentosVinculados(documento, processo,
								resultado, processarVinculados, isRequisicaoDePJE));
			}
		}
		return resultado;
	}

	public List<ProcessoDocumento> converterColecao(
			List<DocumentoProcessual> colecaoObjeto, Processo processo,
			ProcessoDocumento documentoPrincipal, boolean processarVinculados, boolean isRequisicaoDePJE) {
		Transformer transformer = novoTransformador(processo,
				documentoPrincipal, processarVinculados, isRequisicaoDePJE);
		return aplicarTransformador(colecaoObjeto, transformer);
	}

	/**
	 * Carrega o campo ANY para gravação em tabela específica.
	 * Obs: ainda será implementado.
	 * 
	 * @param documento
	 */
	@SuppressWarnings("unused")
	protected void carregarCampoAny(DocumentoProcessual documento) {
		try {
			if (documento.isSetAny()) {
				Object any = documento.getAny();
				XmlRootElement annotation = any.getClass().getAnnotation(XmlRootElement.class);
				String namespace = annotation.namespace();
				StringWriter writer = new StringWriter();

				JAXBContext jaxbContext = JAXBContext.newInstance(any.getClass());
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

				jaxbMarshaller.marshal(any, writer);

				// TODO (Didi): incluir o campo ANY em uma tabela (CDA)
				String xml = writer.toString();
				writer.close();
			}
		} catch (Exception e) {
			String mensagem = "Erro ao converter o campo ANY do documento, erro: %s";
			throw new IntercomunicacaoException(String.format(mensagem,	e.getMessage()));
		}
	}

	/**
	 * Retorna um novo transformador de objetos que é usado pela conversão de
	 * coleção.
	 * 
	 * @return novo transformador de objeto.
	 */
	protected Transformer novoTransformador(final Processo processo,
			final ProcessoDocumento documentoPrincipal,
			final boolean processarVinculados, final boolean isRequisicaoDePJE) {
		return new Transformer() {
			@Override
			public ProcessoDocumento transform(Object input) {
				return converter((DocumentoProcessual) input, processo,
						documentoPrincipal, processarVinculados, isRequisicaoDePJE);
			}
		};
	}

	/**
	 * Retorna a coleção de documentos vinculados.
	 * 
	 * @param documento
	 * @param processo
	 * @param documentoPrincipal
	 * @param processarVinculados
	 * @return documentos vinculados.
	 */
	protected Collection<ProcessoDocumento> obterColecaoDocumentosVinculados(
			DocumentoProcessual documento, Processo processo,
			ProcessoDocumento documentoPrincipal, boolean processarVinculados, boolean isRequisicaoDePJE) {

		Collection<ProcessoDocumento> resultado = new ArrayList<ProcessoDocumento>();

		if (documento.isSetDocumentoVinculado()) {
			for (DocumentoProcessual vinculado : documento.getDocumentoVinculado()) {
				resultado.add(converter(vinculado, processo, documentoPrincipal, processarVinculados, isRequisicaoDePJE));
			}
		}
		return resultado;
	}

	/**
	 * Retorna o número do documento passado por parametro
	 * @param documento
	 * @return
	 */
	protected String obterNumeroDocumento(DocumentoProcessual documento) {
		String numeroDocumento = MNIParametroUtil.obterValor(documento, MNIParametro.PARAM_NUMERO_DOCUMENTO);
		return numeroDocumento==null?"":numeroDocumento;
	}
	
	/**
	 * Retorna o id do documento da instância de origem
	 * @param documento
	 * @return
	 */
	protected String obterIdInstanciaOrigem(DocumentoProcessual documento){
		return documento.getIdDocumento();
	}
	
	/**
	 * Retorna a data de inclusão do documento, o parâmetro MNIParametro.PARAM_DATA_INCLUSAO 
	 * pode ser usado para definir uma data específica.
	 * 
	 * @param documento
	 * @return Data de inclusão do documento.
	 */
	protected Date obterDataInclusao(DocumentoProcessual documento) {
		Date resultado = null;
		String dataInclusaoUnparsed = MNIParametroUtil.obterValor(documento, MNIParametro.PARAM_DATA_INCLUSAO);
		
		if (isVazio(dataInclusaoUnparsed)) {
			resultado = ConversorUtil.converterParaDate(documento.getDataHora(), true);
		} else {
			resultado = new Date(Long.parseLong(dataInclusaoUnparsed));
		}

		if (resultado.compareTo(new Date()) > 0) {
			throw new NegocioException("Data de inclusão do documento inválida. Data superior à data atual");
		}

		return resultado;
	}

	/**
	 * Retorna a data de juntada do documento, o parâmetro MNIParametro.PARAM_DATA_JUNTADA 
	 * pode ser usado para definir uma data específica.
	 * 
	 * @param documento
	 * @return Data de juntada do documento.
	 */
	protected Date obterDataJuntada(DocumentoProcessual documento) {
		Date resultado = null;
		String dataInclusaoUnparsed = MNIParametroUtil.obterValor(documento, MNIParametro.PARAM_DATA_JUNTADA);
		
		if (isVazio(dataInclusaoUnparsed)) {
			resultado = new Date();
		} else {
			resultado = new Date(Long.parseLong(dataInclusaoUnparsed));
		}

		if (resultado.compareTo(new Date()) > 0) {
			throw new NegocioException("Data de juntada do documento inválida. Data superior à atual.");
		}
		return resultado;
	}
	
	/**
	 * Retorna o documento principal do DocumentoProcessual passado por parâmetro.
	 * 
	 * @param documento
	 * @param documentoPrincipal
	 * @return Documento principal.
	 */
	protected ProcessoDocumento obterDocumentoPrincipal(
			DocumentoProcessual documento, ProcessoDocumento documentoPrincipal) {
		
		ProcessoDocumento resultado = documentoPrincipal;
		
		if (isNull(documentoPrincipal) && documento.isSetIdDocumentoVinculado()) {
			String idDocumentoPai = documento.getIdDocumentoVinculado();
			try {
				resultado = processoDocumentoManager.findById(new Integer(idDocumentoPai));
			} catch (Exception e) {
				String mensagem = "Não foi possível recuperar o documento principal com id = %s, erro: %s";
				throw new IntercomunicacaoException(String.format(mensagem, idDocumentoPai, e.getMessage()));
			}
		}
		return resultado;
	}
	
	/**
	 * Retorna a instância da instalação.
	 * 
	 * @param documentoProcessual
	 * @return Instância da instalação do PJe.
	 */
	protected String obterInstancia(DocumentoProcessual documentoProcessual) {
		String instancia = MNIParametroUtil.obterValor(documentoProcessual,
				MNIParametro.PARAM_INSTANCIA_DOCUMENTO);
		if (isVazio(instancia)) {
			instancia = ParametroUtil.instance().getInstancia();
		}
		return instancia;
	}

	/**
	 * Retorna a descrição do documento. A ordem de recuperação é seguida conforme abaixo:
	 * 1) nome passado pela remessa; ou
	 * 2) descrição do documento; ou
	 * 3) descrição do tipo do documento.
	 * 
	 * A primeira informação não nula será recuperada.
	 * 
	 * @param documento Documento
	 * @param tipoProcessoDocumento Tipo do documento.
	 * @return descrição.
	 */
	protected String obterNomeProcessoDocumento(DocumentoProcessual documento,
			TipoProcessoDocumento tipoProcessoDocumento) {
		List<String> resultados = new ArrayList<String>();
		resultados.add(MNIParametroUtil.obterValor(documento, MNIParametro.PARAM_NOME_DOCUMENTO));
		resultados.add(documento.getDescricao());
		resultados.add(tipoProcessoDocumento.getTipoProcessoDocumento());
		
		return (String) ArrayUtil.firstNonNull(resultados.toArray());
	}

	/**
	 * Retorna o documento binário.
	 * 
	 * @param documento
	 * @param documentoPai
	 * @return Documento binário.
	 */
	protected ProcessoDocumentoBin obterProcessoDocumentoBin(
			DocumentoProcessual documento, ProcessoDocumento documentoPai, boolean isRequisicaoDePJE) {
		ProcessoDocumentoBin bin = documentoProcessualParaProcessoDocumentoBinConverter.converter(documento, isRequisicaoDePJE);
		bin.getProcessoDocumentoList().add(documentoPai);
		return bin;
	}

	/**
	 * Retorna o tipo do documento.
	 * 
	 * @param codigoTipoDocumento
	 * @return tipo do documento.
	 */
	protected TipoProcessoDocumento obterTipoProcessoDocumento(
			String codigoTipoDocumento) {
		try {
			return tipoProcessoDocumentoManager.findByCodigoDocumento(codigoTipoDocumento, Boolean.TRUE);
		} catch (PJeBusinessException e) {
			String mensagem = "Erro ao consultar o tipo do documento %s, erro: %s";
			throw new IntercomunicacaoException(String.format(mensagem,
					codigoTipoDocumento, e.getMessage()));
		}
	}
	
	/**
	 * Retorna o número de ordem do documento da instância de origem.
	 * @param documento
	 * @param documentoPrincipal
	 * @return
	 */
	protected Integer obterNumeroOrdem(DocumentoProcessual documento, ProcessoDocumento documentoPrincipal){
		String numeroOrdem = MNIParametroUtil.obterValor(documento, MNIParametro.PARAM_NUMERO_ORDEM);
		if (StringUtils.isBlank(numeroOrdem) && documentoPrincipal != null) {
			int quantidade = ProjetoUtil.getTamanho(documentoPrincipal.getDocumentosVinculados());
			numeroOrdem = String.valueOf((quantidade + 1));
		}
		
		return (NumberUtils.isNumber(numeroOrdem) ? NumberUtils.toInt(numeroOrdem) : null);
	}	
}
