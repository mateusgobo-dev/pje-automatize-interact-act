package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.actions.ModeloDocumentoAction;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.DocumentoCertidaoManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoHistoricoManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.nucleo.entidades.Documento;
import br.jus.pje.nucleo.entidades.DocumentoCertidao;
import br.jus.pje.nucleo.entidades.DocumentoHistorico;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.TipoOperacaoDocumentoHistoricoEnum;

@Name(DocumentoCertidaoAction.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class DocumentoCertidaoAction {

	public static final String NAME = "documentoCertidaoAction";
	
	private Map<ProcessoDocumento, DocumentoCertidao> cache = new LinkedHashMap<ProcessoDocumento, DocumentoCertidao>(0);
	private DocumentoCertidaoBean documentoCertidaoBean;
	
	@In
	private ModeloDocumentoManager modeloDocumentoManager;
	
	@In
	private DocumentoCertidaoManager documentoCertidaoManager;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private DocumentoHistoricoManager documentoHistoricoManager; 
	
	@In
	private DocumentoManager documentoManager;
	
	private static final LogProvider log = Logging.getLogProvider(DocumentoCertidaoAction.class);
	
	/**
	 * Método responsável por gerar o documento de certidão de protocolo do documento.
	 * 
	 * @param processoDocumento Documento do processo.
	 * @throws PJeBusinessException Caso ocorra algum erro.
	 */
	public DocumentoCertidao gerarCertidao(ProcessoDocumento processoDocumento) throws PJeBusinessException {
		if(processoDocumento != null && processoDocumento.getDataJuntada() != null && !documentoTemCertidao(processoDocumento)) {
			String parametro = ParametroUtil.getParametro(Parametros.NOME_MODELO_CERTIDAO_PROTOCOLO);
			if(parametro != null) {
				ModeloDocumento modeloDocumento = this.modeloDocumentoManager.findById(Integer.parseInt(parametro));
				
				this.documentoCertidaoBean = new DocumentoCertidaoBean();
				this.documentoCertidaoBean.carregarDadosCertidao(processoDocumento);

				Documento documento = new Documento();
				documento.setAtivo(Boolean.TRUE);
				documento.setBinario(Boolean.FALSE);	
				documento.setValido(Boolean.TRUE);
				documento.setConteudo(ModeloDocumentoAction.instance().getConteudo(modeloDocumento));
				documento.setNome(modeloDocumento.getTituloModeloDocumento());
				documento.setHistoricosDocumento(new ArrayList<DocumentoHistorico>());
				
				documentoManager.persist(documento);
				
				DocumentoHistorico documentoHistorico = new DocumentoHistorico();
				documentoHistorico.setConteudoDocumento(documento.getConteudo());
				documentoHistorico.setDataOperacao(new Date());
				documentoHistorico.setDocumento(documento);
				documentoHistorico.setResponsavel(Authenticator.getUsuarioLogado());
				documentoHistorico.setTipoOperacao(TipoOperacaoDocumentoHistoricoEnum.I);
				
				documentoHistoricoManager.persist(documentoHistorico);

				DocumentoCertidao docCertidao = new DocumentoCertidao();
				docCertidao.setDocumento(documento);
				docCertidao.setProcessoDocumento(processoDocumento);

				this.documentoCertidaoManager.persistAndFlush(docCertidao);
				
				return docCertidao;
			}
		}
		return null;
	}
	
	/**
	 * Método que verifica se o documento possui certidão
	 * 
	 * @param processoDocumento
	 * @return true se o documento não possuir
	 */
	private boolean documentoTemCertidao(ProcessoDocumento processoDocumento) {
		boolean retorno = false;
		if(documentoCertidaoManager.recuperarDocumentoCertidao(processoDocumento) != null){
			retorno =  true;
		}
		return retorno;
	}

	public DocumentoCertidao recuperarCertidao(ProcessoDocumento processoDocumento){
		if(processoDocumento != null){
			if (this.cache.containsKey(processoDocumento)) {
				return this.cache.get(processoDocumento);
			}
			DocumentoCertidao documentoCertidao = this.documentoCertidaoManager.recuperarDocumentoCertidao(processoDocumento);
			this.cache.put(processoDocumento, documentoCertidao);
			return documentoCertidao;
		}
		return null;
	}
	
	public String obterConteudoCertidao(Integer idProcessoDocumento){
		ProcessoDocumento processoDocumento = EntityUtil.find(ProcessoDocumento.class, idProcessoDocumento);
		if(processoDocumento.getDocumentoPrincipal() != null){
			processoDocumento = processoDocumento.getDocumentoPrincipal();
		}
		DocumentoCertidao documentoCertidao = this.recuperarCertidao(processoDocumento);

		if(documentoCertidao == null){
			try {
				documentoCertidao = this.gerarCertidao(processoDocumento);
			} catch (PJeBusinessException e) {
				documentoCertidao = null;
			}
		}


		if(documentoCertidao != null){
			return documentoCertidao.getDocumento().getConteudo();
		}

		return StringUtils.EMPTY;
	}
	
	public List<ProcessoDocumento> recuperarDocumentosJuntados(ProcessoTrf processo, int idProcessoTrf, int idProcessoDoc){
		List<ProcessoDocumento> retorno = null;
		if(idProcessoDoc > 0) {
			ProcessoDocumento principal = null;
			try {
				principal = ComponentUtil.getProcessoDocumentoManager().findById(idProcessoDoc);
			} catch (PJeBusinessException e) {
				log.error("Erro ao tentar recuperar o documento. " + e.getMessage());
				FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar recuperar o documento. " + e.getMessage());
			}
			if(principal != null) {
				retorno = ComponentUtil.getProcessoDocumentoManager().recuperaDocumentosJuntados(principal);
			}
		} else {
			if(processo == null && idProcessoTrf > 0) {
				processo = ComponentUtil.getProcessoTrfManager().find(ProcessoTrf.class, idProcessoTrf);
			}
			retorno = ComponentUtil.getProcessoDocumentoManager().recuperaDocumentosJuntados(processo, null);
		}
		return retorno; 
	}


	public DocumentoCertidaoBean getDocumentoCertidaoBean() {
		return documentoCertidaoBean;
	}

}
