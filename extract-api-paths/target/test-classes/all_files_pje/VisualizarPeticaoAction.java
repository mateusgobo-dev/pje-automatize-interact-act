/**
 * 
 */
package br.jus.cnj.pje.view.fluxo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.manager.HabilitacaoAutosManager;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.je.pje.entity.vo.BinarioVO;
import br.jus.pje.jt.entidades.HabilitacaoAutos;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle do frame WEB-INF/xhtml/flx/visualizarPeticao.xhtml
 */
@Name("visualizarPeticaoAction")
@Scope(ScopeType.CONVERSATION)
public class VisualizarPeticaoAction extends TramitacaoFluxoAction {
	
	private static final Map<String, String> prms;
	
	private Integer idPeticao;
	
	private Boolean opened = true;
	
	private ProcessoDocumento documentoPrincipal;
	
	@In
	private DocumentoJudicialService documentoJudicialService;
	
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
	@In
	private FacesContext facesContext;
	
	private class AnexosRetreiver implements DataRetriever<ProcessoDocumento>{
		
		private Long count = null;
		
		private Integer idDocumentoPrincipal;
		
		private ProcessoDocumentoManager manager;
		
		public AnexosRetreiver(Integer idDocumentoPrincipal, ProcessoDocumentoManager manager){
			this.idDocumentoPrincipal = idDocumentoPrincipal;
			this.manager = manager;
		}

		@Override
		public Object getId(ProcessoDocumento obj) {
			return obj.getIdProcessoDocumento();
		}

		@Override
		public ProcessoDocumento findById(Object id) throws Exception {
			if(id == null || !Number.class.isAssignableFrom(id.getClass())){
				return null;
			}
			return this.manager.findById(id);
		}

		@Override
		public List<ProcessoDocumento> list(Search search) {
			try{
				if(idDocumentoPrincipal == null){
					idDocumentoPrincipal = -1;
				}
				search.addCriteria(Criteria.equals("documentoPrincipal.idProcessoDocumento", idDocumentoPrincipal));
				search.addCriteria(Criteria.equals("ativo", true));
				search.addOrder("dataJuntada", Order.ASC);
				return manager.list(search);
			}catch(NoSuchFieldException e){
				logger.info("Erro ao tentar recuperar os documentos: {0}", e.getLocalizedMessage());
			}
			return Collections.emptyList();
		}

		@Override
		public long count(Search search) {
			if(count == null){
				search.setMax(1);
				list(search);
				count = manager.count(search);
			}
			return count;
		}
		
	};
	
	static {
		prms = new HashMap<String, String>();
		prms.put("idPeticao", Variaveis.VARIAVEL_FLUXO_PETICAO_INCIDENTAL);
		prms.put("opened", "pje:fluxo:incidental:visualizar:aberto");
	}
	
	private EntityDataModel<ProcessoDocumento> anexos;

	@Override
	protected Map<String, String> getParametrosConfiguracao() {
		return prms;
	}
	
	public Boolean getOpened() {
		return opened;
	}
	
	public ProcessoDocumento getDocumentoPrincipal() {
		if (documentoPrincipal == null) {
			carregarDocumentoPrincipal();
		}
		return documentoPrincipal;
	}
	
	private void carregarDocumentoPrincipal() {
		if (idPeticao != null && idPeticao > 0) {
			documentoPrincipal = documentoJudicialService.getDocumento(idPeticao, processoJudicial);
			if (documentoPrincipal != null) {
				ProcessoDocumentoBin bin = documentoPrincipal.getProcessoDocumentoBin();
				if (bin.isBinario()) {
					BinarioVO binario = new BinarioVO();
					binario.setIdBinario(bin.getIdProcessoDocumentoBin());
					binario.setMimeType(bin.getExtensao());
					binario.setNomeArquivo(bin.getNomeArquivo());
					binario.setNumeroStorage(bin.getNumeroDocumentoStorage());
					Contexts.getSessionContext().set("download-binario", binario);
				}
			}
		}
	}
	
	public EntityDataModel<ProcessoDocumento> getAnexos() {
		if(anexos == null){
			inicializarAnexos();
		}
		return anexos;
	}
	
	private void inicializarAnexos(){
		AnexosRetreiver retreiver = new AnexosRetreiver(idPeticao, processoDocumentoManager);
		anexos = new EntityDataModel<ProcessoDocumento>(ProcessoDocumento.class, facesContext, retreiver);
	}
	
	/**
	 * Recupera a habilitação nos autos relativa ao processo e id documento da tarefa
	 */
	
	public HabilitacaoAutos recuperaHabilitacao() {
		return getHabilitacaoAutosManager().recuperaHabilitacao(processoJudicial, idPeticao);
	}

	private HabilitacaoAutosManager getHabilitacaoAutosManager(){
		return ComponentUtil.getComponent("habilitacaoAutosManager");
	}
	
}
