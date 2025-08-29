package br.com.infox.ibpm.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.home.ProcessoTrfRedistribuicaoHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.SearchTree2GridList;
import br.com.infox.ibpm.component.tree.EventoTreeHandler;
import br.com.infox.ibpm.jbpm.actions.RegistraEventoAction;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.EventoTipoICR;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.TipoEvento;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;

@Name("eventoHome")
@BypassInterceptors
public class EventoHome extends AbstractEventoHome<Evento> {

	private static final long serialVersionUID = 1L;
	private Evento eventoSuperior;
	private Boolean inativado;
	private SearchTree2GridList<Evento> searchTree2GridList;
	private List<Evento> eventoProcessualLeafList = new ArrayList<Evento>(0);
	private static final LogProvider log = Logging.getLogProvider(RegistraEventoAction.class);

	public static EventoHome instance() {
		return ComponentUtil.getComponent("eventoHome");
	}

	@Override
	public void newInstance() {
		getInstance().setAtivo(Boolean.TRUE);
		limparTrees();
		refreshGrid("eventoGrid");
		super.newInstance();
	}

	private void limparTrees() {
		EventoTreeHandler tree = getComponent(EventoTreeHandler.NAME);
		EventoTreeHandler ret1 = getComponent("eventoSearchTree");
		EventoTreeHandler ret2 = getComponent("eventoFormTree");
		if (!getLockedFields().contains("eventoSuperior"))
			ret1.clearTree();
		ret2.clearTree();
		if (searchTree2GridList != null) {
			searchTree2GridList.refreshTreeList();
			searchTree2GridList = null;
		}
		if (tree != null) {
			tree.clearTree();
		}
	}

	// TODO verifica se esta sendo usado
	@Override
	public String inactive(Evento instance) {
		inactiveRecursive(instance);
		return super.inactive(instance);
	}

	// TODO verifica se esta sendo usado
	@SuppressWarnings("unchecked")
	public List<Evento> getEventoRedistribuicaoList() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.eventoRedistribuicao from MotivoEventoRedistribuicao o ");
		sb.append("where o.motivoRedistribuicao = :motivoRedistribuicao ");
		Query query = EntityUtil.getEntityManager().createQuery(sb.toString());
		query.setParameter("motivoRedistribuicao", ProcessoTrfRedistribuicaoHome.instance().getInstance()
				.getMotivoRedistribuicao());
		return query.getResultList();
	}

	@Override
	protected Evento createInstance() {
		inativado = Boolean.FALSE;
		instance = super.createInstance();
		instance.setEventoSuperior(new Evento());
		return instance;
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	public String inactiveRecursive(Evento evento) {
		if (evento.getEventoList().size() > 0) {
			inativarFilhos(evento);
		}
		evento.setAtivo(Boolean.FALSE);
		String ret = super.update();
		limparTrees();
		refreshGrid("eventoGrid");
		return ret;
	}

	private void inativarFilhos(Evento evento) {
		evento.setAtivo(Boolean.FALSE);
		Integer quantidadeFilhos = evento.getEventoList().size();
		for (int i = 0; i < quantidadeFilhos; i++) {
			inativarFilhos(evento.getEventoList().get(i));
		}
	}

	@Override
	public String update() {
		// Verifica se o pai atual é diferente de nulo
		if ((getInstance().getEventoSuperior() != null) && (eventoSuperior != null)
				&& (!getInstance().getEventoSuperior().getEvento().equals(eventoSuperior.getEvento()))) {
			getInstance().getEventoSuperior().getEventoList().remove(getInstance());
			eventoSuperior.getEventoList().add(getInstance());
			eventoSuperior.getEventoList();
		}

		/*
		 * Se o pai atual não for nulo e o pai selecionado for, o registro é
		 * excluido da lista do pai atual.
		 */
		if ((getInstance().getEventoSuperior() != null) && (eventoSuperior == null)) {
			getInstance().getEventoSuperior().getEventoList().remove(getInstance());
		}

		/*
		 * Se o pai atual for nulo e o pai selecionado não for, o registro é
		 * adicionado à lista do pai atual.
		 */
		if ((getInstance().getEventoSuperior() == null) && (eventoSuperior != null)) {
			eventoSuperior.getEventoList().add(getInstance());
			getInstance().setEventoSuperior(eventoSuperior);
		}

		/*
		 * Se o registro estiver como inativo na hora do update, todos os seus
		 * filhos serão inativados
		 */
		if (!getInstance().getAtivo()) {
			inactiveRecursive(getInstance());
			return "updated";
		} else {
			String ret = null;
			try {
				ret = super.update();
//				limparTrees();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			return ret;
		}
	}

	@Override
	public String remove(Evento obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		inativado = Boolean.TRUE;
		super.update();
		newInstance();
		Contexts.removeFromAllContexts("eventoGrid");
		return "updated";
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (!inativado) {
			getInstance().setEventoSuperior(eventoSuperior);
		}
		return super.beforePersistOrUpdate();
	}

	public void limparEvento() {
		newInstance();
		eventoSuperior = null;
	}

	public void set(Evento evento) {
		instance = evento;
		getInstance().setEventoSuperior(eventoSuperior);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			if (getInstance().getEventoSuperior() != null) {
				eventoSuperior = getEntityManager().find(Evento.class,
						getInstance().getEventoSuperior().getIdEvento());
			}
		}
		if (id == null) {
			eventoSuperior = null;
		}
	}

	public String persistAndNext() {
		String outcome = null;
		try {
			outcome = persist();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		if (outcome != null) {
			if (!outcome.equals("")) {
				Evento me = getInstance();
				newInstance();
				getInstance().setEventoSuperior(me);
				getEntityManager().flush();
				eventoSuperior = (Evento) getInstance().getEventoSuperior();
				getInstance().setEventoSuperior(eventoSuperior);
			}
		}
		return outcome;
	}

	public Evento getEventoSuperior() {
		return eventoSuperior;
	}

	public void setEventoSuperior(Evento eventoSuperior) {
		this.eventoSuperior = eventoSuperior;
	}

	@Override
	public void onClickFormTab() {
		if (isManaged()) {
			eventoSuperior = (Evento) getInstance().getEventoSuperior();
		} else {
			eventoSuperior = null;
		}
		super.onClickFormTab();
	}

	public SearchTree2GridList<Evento> getSearchTree2GridList() {
		if (searchTree2GridList == null) {
			Evento searchBean = getComponent("eventoSearch");
			AbstractTreeHandler<Evento> treeHandler = getComponent("eventoSearchTree");
			searchTree2GridList = new SearchTree2GridList<Evento>(searchBean, treeHandler);
			// Adicionado os filtros extras
			String filterName[] = { "evento", "codEvento", "ativo", "codEventoOutro", "complementar" };
			searchTree2GridList.setFilterName(filterName);
		}
		return searchTree2GridList;
	}

	public void addTipoEvento(TipoProcessoDocumento obj, String gridId) {
		if (getInstance() != null) {
			getInstance().getTipoProcessoDocumentoList().add(obj);
			getEntityManager().merge(instance);
			getEntityManager().flush();
			refreshGrid(gridId);
			refreshGrid("tipoProcessoDocumentoRightGrid");
			FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso!");
		}
		
	}

	public void removeTipoEvento(TipoEvento obj, String gridId) {
		if (getInstance() != null) {
			getInstance().getTipoProcessoDocumentoList().remove(obj.getTipoProcessoDocumento());
			getEntityManager().merge(instance);
			getEntityManager().flush();
			refreshGrid(gridId);
			refreshGrid("tipoEventoGrid");
			FacesMessages.instance().add(Severity.INFO, "Registro inativado com sucesso.");
		}
		
	}
	
	public void addTipoICREvento(TipoInformacaoCriminalRelevante obj, String gridId) {
		if (getInstance() != null) {
			getInstance().getTipoICRList().add(obj);
			getEntityManager().merge(instance);
			getEntityManager().flush();
			refreshGrid(gridId);
			refreshGrid("tipoInformacaoCriminalRelevanteRightGrid");
			FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso!");
		}
	}

	public void removeTipoICREvento(EventoTipoICR obj, String gridId) {
		if (getInstance() != null) {
			getInstance().getTipoICRList().remove(obj.getTipoInformacaoCriminalRelevante());
			getEntityManager().merge(instance);
			getEntityManager().flush();
			refreshGrid(gridId);
			refreshGrid("tipoICREventoGrid");
			FacesMessages.instance().add(Severity.INFO, "Registro inativado com sucesso.");
		}
	}

	public List<Evento> getEventoLeafMagistrado() {
		Evento ep = EntityUtil.getEntityManager().find(Evento.class, 1);
		return getEventoLeaf(ep);
	}

	public List<Evento> getEventoLeaf(Evento eventoSuperior) {
		for (Evento evt : eventoSuperior.getEventoList()) {
			if (evt.getEventoList().size() > 0) {
				getEventoLeaf((Evento) evt);
			} else {
				this.eventoProcessualLeafList.add((Evento) evt);
			}
		}
		return this.eventoProcessualLeafList;
	}

	/* Método que registra movimentações pelo código CNJ */
	@SuppressWarnings("unchecked")
	public void registraCodigoCNJ(Processo processo, String... idList) {
		try {
			List<String> ids = Arrays.asList(idList);
			String select = "SELECT o FROM Evento o WHERE o.codEvento IN (:idList)";
			List<Evento> list = EntityUtil.getEntityManager().createQuery(select).setParameter("idList", ids)
					.getResultList();
			Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");

			if (processo == null) {
				processo = ProcessoHome.instance().getInstance();
				if (processo == null) {
					return;
				}
			}
			for (Evento e : list) {
				RegistraEventoAction.instance().registrarEventoProcessual(processo, e, usuario);
			}

		} catch (Exception ex) {
			String action = "registrar eventos da expressão definida no fluxo "
					+ "#{registraEventoAction.registra()}: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"registra()", "RegistraEventoAction", "BPM"));
		}
	}

	// TODO verifica se esta sendo usado
	@Override
	public boolean isEditable() {
		return ParametroUtil.instance().getPermitirCadastrosBasicos();
	}
}