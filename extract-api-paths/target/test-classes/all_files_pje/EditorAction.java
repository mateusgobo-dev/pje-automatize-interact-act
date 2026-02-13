package br.com.infox.editor.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.editor.exception.EditorServiceException;
import br.com.infox.editor.interpretadorDocumento.LinguagemFormalException;
import br.com.infox.editor.manager.EstruturaDocumentoManager;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoManager;
import br.com.infox.editor.service.EditorService;
import br.com.infox.ibpm.component.tree.AutomaticEventsTreeHandler;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.csjt.pje.view.action.TipoProcessoDocumentoAction;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.filters.ProcessoDocumentoEstruturadoFilter;

@Name(EditorAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EditorAction implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "editorAction";
	
	@In
	private EditorService editorService;
	@In
	private EstruturaDocumentoManager estruturaDocumentoManager;
	@In
	private AssinaturaDocumentoService assinaturaDocumentoService;
	@In(required = false)
	private TaskInstanceHome taskInstanceHome;
	@In(required = false)
	private ProcessoHome processoHome;
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	@In
	private ProcessoDocumentoEstruturadoManager processoDocumentoEstruturadoManager;
	@In(create=true)
	private ModeloMagistradoAction modeloMagistradoAction;
	@In(create=true)
	private TipoProcessoDocumentoAction tipoDocumento;
	
	private ProcessoDocumentoEstruturado documento;
	private ProcessoTrf processoTrf;
	private TipoProcessoDocumento tipoProcessoDocumento;
	private PessoaMagistrado pessoaMagistrado;
	private EstruturaDocumento estruturaDocumento;
	private String signature;
	private String certChain;
	private Integer idAgrupamentos;
	private boolean renderEventsTree;
	
	/**
	 * [PJEII-5142]
	 * 
	 * Metodo responsavel por setar valores automaticamente nas combobox tipo de
	 * documento e magistrado do editor estruturado, caso as mesmas possuam apenas uma opcao de escolha.
	 * 
	 * @param name
	 * @author thiago.carvalho
	 */
	public void autoInitOpcaoCombo(String name) {
		List<TipoProcessoDocumento> listaTipoDocumento = this.tipoDocumento.getTipoDocumentoItems(name);
		if (listaTipoDocumento != null && listaTipoDocumento.size() == 1) {
			this.tipoProcessoDocumento = listaTipoDocumento.get(0);
		}
		List<PessoaMagistrado> listaMagistrado = this.modeloMagistradoAction.getMagistradoItems();
		if (listaMagistrado != null && listaMagistrado.size() == 1) {
			this.pessoaMagistrado = listaMagistrado.get(0);
		}
	}
	
	public void inicializarDocumento() {
		//apagar o anterior, se existir
		if (getDocumento() != null && getDocumento().getIdProcessoDocumentoEstruturado() != null) {
			//documento já existia
			processoDocumentoEstruturadoManager.removerProcessoDocumentoEstruturado(this.getDocumento());
		}
		setDocumento(null);
		try {
			if (estruturaDocumento != null) {
				setDocumento(editorService.criarProcessoDocumentoEstruturado(estruturaDocumento, processoTrf, tipoProcessoDocumento, pessoaMagistrado));
			}
			if (processoHome != null) {
				processoHome.setTipoProcessoDocumento(tipoProcessoDocumento);
			}
		} catch (LinguagemFormalException e) {
			e.printStackTrace();
		}
	}
	
	public void ligarFiltroProcessoTopicoAtivo() {
		HibernateUtil.enableFilters(ProcessoDocumentoEstruturadoFilter.FILTER_PROCESSO_TOPICO_ATIVO);
	}
	
	public ProcessoDocumentoEstruturado getDocumento() {
		return documento;
	}
	
	public void setDocumento(ProcessoDocumentoEstruturado documento) {
		this.documento = documento;
		if (documento != null) {
			setTipoProcessoDocumento(documento.getProcessoDocumento().getTipoProcessoDocumento());
			setEstruturaDocumento(documento.getEstruturaDocumento());
			setPessoaMagistrado(documento.getMagistrado());
		}
	}
	
	public void carregarAcordao(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
		ProcessoDocumento acordao = processoDocumentoManager.getUltimoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoAcordao(), processoTrf.getProcesso());
		if (acordao != null) {
			setDocumento(EntityUtil.find(ProcessoDocumentoEstruturado.class, acordao.getIdProcessoDocumento()));
		}
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public TipoProcessoDocumento getTipoProcessoDocumento() {
		return tipoProcessoDocumento;
	}

	public void setTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
	}

	public PessoaMagistrado getPessoaMagistrado() {
		return pessoaMagistrado;
	}

	public void setPessoaMagistrado(PessoaMagistrado pessoaMagistrado) {
		this.pessoaMagistrado = pessoaMagistrado;
	}
	
	public EstruturaDocumento getEstruturaDocumento() {
		return estruturaDocumento;
	}
	
	public void setEstruturaDocumento(EstruturaDocumento estruturaDocumento) {
		this.estruturaDocumento = estruturaDocumento;
	}
	
	public List<EstruturaDocumento> getEstruturaDocumentoItens() {
		return estruturaDocumentoManager.getEstruturaDocumentoList(tipoProcessoDocumento);
	}
	
	@SuppressWarnings("unchecked")
	public List<TipoProcessoDocumento> getTipoProcessoDocumentoItems() {
		Query q = EntityUtil.createQuery("select o from TipoProcessoDocumento o where " +
				"o in (select tpd.tipoProcessoDocumento from AplicacaoClasseTipoProcessoDocumento tpd " +
				"where tpd.aplicacaoClasse.idAplicacaoClasse = #{parametroUtil.aplicacaoSistema.idAplicacaoClasse}) and " +
				"(o.inTipoDocumento = #{processoDocumentoHome.modelo ? 'P' : 'D'} OR o.inTipoDocumento = 'T') and " +
				"o.ativo = true order by o.tipoProcessoDocumento");
		return q.getResultList();
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getCertChain() {
		return certChain;
	}

	public void setCertChain(String certChain) {
		this.certChain = certChain;
	}

	public Integer getIdAgrupamentos() {
		return idAgrupamentos;
	}

	public void setIdAgrupamentos(Integer idAgrupamentos) {
		this.idAgrupamentos = idAgrupamentos;
	}

	public boolean isRenderEventsTree() {
		return renderEventsTree;
	}

	public void setRenderEventsTree(boolean renderEventsTree) {
		this.renderEventsTree = renderEventsTree;
	}
	
	/**
	 * Verifica se existe algum agrupamento vinculado ao tipo de documento
	 * selecionado.
	 */
	public void onSelectProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.tipoProcessoDocumento = tipoProcessoDocumento;
		AutomaticEventsTreeHandler tree = AutomaticEventsTreeHandler.instance();
		tree.clearList();
		tree.clearTree();
		
		renderEventsTree = false;

		if (tipoProcessoDocumento != null && tipoProcessoDocumento.getAgrupamento() != null) {
			idAgrupamentos = tipoProcessoDocumento.getAgrupamento().getIdAgrupamento();
			if (idAgrupamentos != null && idAgrupamentos > 0) {
				renderEventsTree = true;
				tree.setRootsSelectedMap(new HashMap<Evento, List<Evento>>());
				tree.getRoots(idAgrupamentos);
			}
		}
	}
	
	public void gerarDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		this.onSelectProcessoDocumento(tipoProcessoDocumento);
		this.inicializarDocumento();
		//"simular" clique no botão gravar do editor estruturado
		DocumentoAction documentoAction = ComponentUtil.getComponent("documentoAction");
		documentoAction.salvarDocumento();
		//"simular" clique no gravar no fluxo - salvar id do documento estruturado novo
		taskInstanceHome.update(taskInstanceHome);
	}

	public void save() {
		getDocumento().setModeloDocumento(editorService.getHtmlEditor(getDocumento()));
		try {
			editorService.gravarDocumentoEstruturado(getDocumento());
		} catch (EditorServiceException e) {
			e.printStackTrace();
		}
	}
	
	public void assinar(){
		save();
		assinaturaDocumentoService.assinarDocumento(documento.getProcessoDocumentoBin(), signature, certChain);
		if (taskInstanceHome != null) {
			endTask();
		}
	}

	public static EditorAction instance() {
		return ComponentUtil.getComponent(NAME);
	}
	
	public void endTask() {
		String transicaoSaida = (String) TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		taskInstanceHome.end(transicaoSaida);
	}
}
