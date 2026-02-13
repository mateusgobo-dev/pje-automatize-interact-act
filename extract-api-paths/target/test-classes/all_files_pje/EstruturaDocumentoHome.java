package br.com.infox.editor.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.editor.bean.Estilo;
import br.com.infox.editor.manager.CssDocumentoManager;
import br.com.infox.editor.manager.EstruturaDocumentoTopicoMagistradoManager;
import br.com.infox.editor.manager.EstruturaDocumentoTopicoManager;
import br.com.infox.editor.service.NumeracaoDocumentoService;
import br.com.infox.editor.service.ProcessaModeloService;
import br.com.infox.editor.tree.HierarchicalListTree;
import br.com.infox.editor.tree.RichHierarchicalListTree;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Variavel;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumento;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopico;
import br.jus.pje.nucleo.entidades.editor.EstruturaTipoDocumento;
import br.jus.pje.nucleo.entidades.editor.Topico;
import br.jus.pje.nucleo.entidades.editor.XslDocumento;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoAdmissibilidade;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoConsideracoes;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoDispositivo;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoDispositivoAcordao;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoJulgamentoMerito;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoMeritoGrupoRecursos;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoPedidos;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoPrejudiciais;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoPrejudiciaisMeritoRecurso;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoPreliminares;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoPreliminaresMeritoRecurso;
import br.jus.pje.nucleo.entidades.editor.topico.TopicoTexto;

@Name(EstruturaDocumentoHome.NAME)
@Scope(ScopeType.CONVERSATION)
public class EstruturaDocumentoHome extends AbstractHome<EstruturaDocumento> implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "estruturaDocumentoHome";

	private HierarchicalListTree<EstruturaDocumentoTopico> estruturaDocumentoTopicoTree = new RichHierarchicalListTree<EstruturaDocumentoTopico>("Tópicos do Documento");
	private List<EstruturaDocumentoTopico> estruturaDocumentoTopicoList;
	private EstruturaDocumentoTopico estruturaSelecionadaEdicao;
	private Variavel variavel;
	private List<Estilo> estilos;

	private boolean gravado;
	private boolean novo;

	@In
	private EstruturaDocumentoTopicoManager estruturaDocumentoTopicoManager;
	@In
	private EstruturaDocumentoTopicoMagistradoManager estruturaDocumentoTopicoMagistradoManager;
	@In
	private ProcessaModeloService processaModeloService;
	@In
	private NumeracaoDocumentoService numeracaoDocumentoService;
	@In
	private CssDocumentoManager cssDocumentoManager;

	public HierarchicalListTree<EstruturaDocumentoTopico> getEstruturaDocumentoTopicoTree() {
		return estruturaDocumentoTopicoTree;
	}

	public void clearTree() {
		estruturaDocumentoTopicoTree.clear();
	}

	public boolean podeAdicionarTopico(Topico topico) {
		return !(topico.isUsoUnico() && contemTopico(topico));
	}

	private boolean contemTopico(Topico topico) {
		for (EstruturaDocumentoTopico estruturaDocumentoTopico : estruturaDocumentoTopicoTree.getHierarchicalList()) {
			if (estruturaDocumentoTopico.getTopico().getTipoTopico().equals(topico.getTipoTopico()))
				return true;
		}
		return false;
	}

	public void adicionarTopico(Topico topico) throws InstantiationException, IllegalAccessException {
		Topico novoTopico = EntityUtil.cloneEntity(topico, false);
		EstruturaDocumentoTopico estruturaDocumentoTopico = estruturaDocumentoTopicoManager.criarEstruturaDocumentoTopico(getInstance(), novoTopico);
		setEstruturaSelecionadaEdicao(estruturaDocumentoTopico);

		gravado = false;
		novo = true;
	}

	public void editarTopico() {
		setEstruturaSelecionadaEdicao(estruturaDocumentoTopicoTree.getSelected());
		gravado = false;
		novo = false;
	}
	
	@WebRemote
	public List<Estilo> getEstilos() {
		if (estilos == null) {
			estilos = cssDocumentoManager.getEstilos();
		}
		return estilos;
	}

	public void removerTopico() {
		EstruturaDocumentoTopico estruturaDocumentoTopico = estruturaDocumentoTopicoTree.getSelected();
		if (estruturaDocumentoTopicoManager.temProcessoDocumentoAssociado(estruturaDocumentoTopico)) {
			FacesMessages.instance().add(Severity.INFO, "Não é possível remover esse tópico, pois ele já está associado a um documento.");
			return;
		}
		estruturaDocumentoTopicoTree.removeNode(estruturaDocumentoTopico);
	}

	public boolean podeSubirNivelTopico() {
		return estruturaDocumentoTopicoTree.canMoveRight();
	}

	public void subirNivelTopico() {
		estruturaDocumentoTopicoTree.moveRight();
	}

	public boolean podeDescerNivelTopico() {
		return estruturaDocumentoTopicoTree.canMoveLeft();
	}

	public void descerNivelTopico() {
		estruturaDocumentoTopicoTree.moveLeft();
	}

	public boolean podeMoverTopicoParaCima() {
		return estruturaDocumentoTopicoTree.canMoveUp();
	}

	public void moverTopicoParaCima() {
		estruturaDocumentoTopicoTree.moveUp();
	}

	public boolean podeMoverTopicoParaBaixo() {
		return estruturaDocumentoTopicoTree.canMoveDown();
	}

	public void moverTopicoParaBaixo() {
		estruturaDocumentoTopicoTree.moveDown();
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		if (id == null || changed) {
			super.setId(id);
		}

		if (changed) {
			// getInstance().getEstruturaDocumentoTopicoList(); Removido pois perdia os id's e acabava duplicando os tópicos
			
			estruturaDocumentoTopicoList = estruturaDocumentoTopicoManager.getEstruturaDocumentoTopicoList(getInstance());
			estruturaDocumentoTopicoTree.setHierarchicalList(estruturaDocumentoTopicoList);
			estruturaDocumentoTopicoTree.buildHierarchicalList();
		}
	}

	@Override
	public void newInstance() {
		super.newInstance();
		clearTree();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (estruturaDocumentoTopicoTree.isEmpty()) {
			FacesMessages.instance().add(Severity.ERROR, "A estrutura deve conter pelo menos um tópico associado.");
			return false;
		}
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		getInstance().setDataCriacao(new Date());
		getInstance().setPessoaCriacao(Authenticator.getPessoaLogada());
		String result = super.persist();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Estrutura cadastrada com sucesso.");
		return result;
	}
	
	@Override
	public String update() {
		String result = super.update();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Estrutura alterada com sucesso.");
		return result;
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		if (ret != null) {
			excluirTopicosRemovidos();
			gravarEstruturaDocumentoTopicoList();
			getInstance().setEstruturaDocumentoTopicoList(estruturaDocumentoTopicoTree.getHierarchicalList());
		}
		return super.afterPersistOrUpdate(ret);
	}

	private void excluirTopicosRemovidos() {
		List<EstruturaDocumentoTopico> estruturaDocumentoTopicoTreeList = estruturaDocumentoTopicoManager.getEstruturaDocumentoTopicoList(getInstance());
		for (EstruturaDocumentoTopico estruturaDocumentoTopico : estruturaDocumentoTopicoTreeList) {
			if (!estruturaDocumentoTopicoTree.getHierarchicalList().contains(estruturaDocumentoTopico)) {
				estruturaDocumentoTopicoMagistradoManager.removerTopicoMagistradoAssociado(estruturaDocumentoTopico);
				
				getEntityManager().remove(estruturaDocumentoTopico);
				getEntityManager().remove(estruturaDocumentoTopico.getTopico());
			}
		}
		getEntityManager().flush();
	}

	public String getNumeracaoFormatada(EstruturaDocumentoTopico edTopico) {
		return numeracaoDocumentoService.getNumeracaoDocumento(edTopico.getNivel(), edTopico.getNumeracao());
	}

	private void gravarEstruturaDocumentoTopicoList() {
		List<EstruturaDocumentoTopico> estruturaDocumentoTopicoTreeList = estruturaDocumentoTopicoTree.getHierarchicalList();
		for (EstruturaDocumentoTopico estruturaDocumentoTopico : estruturaDocumentoTopicoTreeList) {
			if (estruturaDocumentoTopico.getTopico().getIdTopico() == null) {
				getEntityManager().persist(estruturaDocumentoTopico.getTopico());
			} else {
				getEntityManager().merge(estruturaDocumentoTopico.getTopico());
			}

			if (estruturaDocumentoTopico.getIdEstruturaDocumentoTopico() == 0) {
				getEntityManager().persist(estruturaDocumentoTopico);
			} else {
				getEntityManager().merge(estruturaDocumentoTopico);
			}
		}
		getEntityManager().flush();
	}

	public void removeTipoProcessoDocumento(EstruturaTipoDocumento estruturaTipoDocumento) {
		getEntityManager().remove(estruturaTipoDocumento);
		getEntityManager().flush();
	}

	public void addTipoProcessoDocumento(TipoProcessoDocumento tipoProcessoDocumento) {
		EstruturaTipoDocumento estruturaTipoDocumento = new EstruturaTipoDocumento();
		estruturaTipoDocumento.setEstruturaDocumento(getInstance());
		estruturaTipoDocumento.setTipoProcessoDocumento(tipoProcessoDocumento);

		getEntityManager().persist(estruturaTipoDocumento);
		getEntityManager().flush();
	}

	public void changeEdicaoHabilitado() {
		if (!getEstruturaSelecionadaEdicao().getTopico().isOpcional()) {
			getEstruturaSelecionadaEdicao().getTopico().setHabilitado(true);
		}
	}

	public void changeEdicaoNumerado() {
		getEstruturaSelecionadaEdicao().setNumerado(getEstruturaSelecionadaEdicao().getTopico().isExibirTitulo());
	}

	public void updateTopicoSelecionado() {
		String erro = processaModeloService.validaModelo(getEstruturaSelecionadaEdicao().getTopico().getConteudoPadrao());
		if (erro == null) {
			if (novo) {
				estruturaDocumentoTopicoTree.addNode(getEstruturaSelecionadaEdicao());
			}

			gravado = true;
			estruturaDocumentoTopicoTree.buildHierarchicalList();
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Modelo inválido! " + erro);
		}
	}

	public List<Topico> getTopicoList() {
		List<Topico> topicoList = new ArrayList<Topico>();
		topicoList.add(new TopicoDispositivo());
		topicoList.add(new TopicoJulgamentoMerito());
		topicoList.add(new TopicoPedidos());
		topicoList.add(new TopicoPrejudiciais());
		topicoList.add(new TopicoPreliminares());
		
		topicoList.add(new TopicoAdmissibilidade());
		topicoList.add(new TopicoPreliminaresMeritoRecurso());
		topicoList.add(new TopicoPrejudiciaisMeritoRecurso());
		topicoList.add(new TopicoMeritoGrupoRecursos());
		topicoList.add(new TopicoDispositivoAcordao());
		topicoList.add(new TopicoConsideracoes());
		
		topicoList.add(new TopicoTexto());
		return topicoList;
	}

	@SuppressWarnings("unchecked")
	@Factory(scope = ScopeType.CONVERSATION, value = "xslDocumentoItems")
	public List<XslDocumento> getXslDocumentoItems() {
		return EntityUtil.createQuery("from XslDocumento").getResultList();
	}

	public boolean isGravado() {
		return gravado;
	}

	public void setGravado(boolean gravado) {
		this.gravado = gravado;
	}

	public Variavel getVariavel() {
		return variavel;
	}

	public void setVariavel(Variavel variavel) {
		this.variavel = variavel;
	}

	public EstruturaDocumentoTopico getEstruturaSelecionadaEdicao() {
		return estruturaSelecionadaEdicao;
	}

	public void setEstruturaSelecionadaEdicao(EstruturaDocumentoTopico estruturaSelecionadaEdicao) {
		this.estruturaSelecionadaEdicao = estruturaSelecionadaEdicao;
	}
}
