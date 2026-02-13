package br.com.infox.cliente.home;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.component.tree.RamoAtividadeTreeHandler;
import br.com.infox.component.tree.SearchTree2GridList;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.RamoAtividade;

@Name("ramoAtividadeHome")
@BypassInterceptors
public class RamoAtividadeHome extends AbstractRamoAtividadeHome<RamoAtividade> {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(RamoAtividadeHome.class);
	private RamoAtividade ramoAtividadePai;
	private Boolean inativado;
	private SearchTree2GridList<RamoAtividade> searchTree2GridList;

	public static RamoAtividadeHome instance() {
		return ComponentUtil.getComponent("ramoAtividadeHome");
	}

	@Override
	public void newInstance() {
		getInstance().setAtivo(Boolean.TRUE);
		limparTrees();
		refreshGrid("ramoAtividadeGrid");
		super.newInstance();
	}

	private void limparTrees() {
		RamoAtividadeTreeHandler ret1 = getComponent("ramoAtividadeSearchTree");
		RamoAtividadeTreeHandler ret2 = getComponent("ramoAtividadeFormTree");
		if (!getLockedFields().contains("ramoAtividadePai")) {
			ret1.clearTree();
		}
		ret2.clearTree();
		if (searchTree2GridList != null) {
			searchTree2GridList.refreshTreeList();
			searchTree2GridList = null;
		}
	}

	@Override
	protected RamoAtividade createInstance() {
		inativado = Boolean.FALSE;
		instance = super.createInstance();
		instance.setRamoAtividadePai(new RamoAtividade());
		return instance;
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			getInstance();
			refreshGrid("ramoAtividadeGrid");
			ret = super.persist();
			if (ret != null) {
				limparTrees();
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return ret;
	}

	public String inactiveRecursive(RamoAtividade ramoAtividade) {
		
		if (ramoAtividade.getRamoAtividadeList().size() > 0) {
			inativarFilhos(ramoAtividade);
		}
		
		ramoAtividade.setAtivo(Boolean.FALSE);
		String ret = super.update();
		limparTrees();
		refreshGrid("ramoAtividadeGrid");
		
		if (ret != null && ret != "") {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, super.getInactiveSuccess());
		}
		
		return ret;
	}

	private void inativarFilhos(RamoAtividade ramoAtividade) {
		ramoAtividade.setAtivo(Boolean.FALSE);
		Integer quantidadeFilhos = ramoAtividade.getRamoAtividadeList().size();
		for (int i = 0; i < quantidadeFilhos; i++) {
			inativarFilhos(ramoAtividade.getRamoAtividadeList().get(i));
		}
	}

	@Override
	public String update() {
		// Verifica se o pai atual é diferente de nulo
		if ((getInstance().getRamoAtividadePai() != null)
				&& (ramoAtividadePai != null)
				&& (!getInstance().getRamoAtividadePai().getRamoAtividade().equals(ramoAtividadePai.getRamoAtividade()))) {
			getInstance().getRamoAtividadePai().getRamoAtividadeList().remove(getInstance());
			ramoAtividadePai.getRamoAtividadeList().add(getInstance());
			ramoAtividadePai.getRamoAtividadeList();
		}

		/*
		 * Se o pai atual não for nulo e o pai selecionado for, o registro é
		 * excluido da lista do pai atual.
		 */
		if ((getInstance().getRamoAtividadePai() != null) && (ramoAtividadePai == null)) {
			getInstance().getRamoAtividadePai().getRamoAtividadeList().remove(getInstance());
		}

		/*
		 * Se o pai atual for nulo e o pai selecionado não for, o registro é
		 * adicionado à lista do pai atual.
		 */
		if ((getInstance().getRamoAtividadePai() == null) && (ramoAtividadePai != null)) {
			ramoAtividadePai.getRamoAtividadeList().add(getInstance());
			getInstance().setRamoAtividadePai(ramoAtividadePai);
		}

		/*
		 * Se o registro estiver como inativo na hora do update, todos os seus
		 * filhos serão inativados
		 */
		if (!getInstance().getAtivo()) {
			inactive(getInstance());
			return "updated";
		} else {
			String ret = null;
			try {
				ret = super.update();
				limparTrees();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			return ret;
		}
	}

	@Override
	public String remove(RamoAtividade obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		inativado = Boolean.TRUE;
		super.update();
		newInstance();
		Contexts.removeFromAllContexts("ramoAtividadeGrid");
		return "updated";
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (!inativado) {
			getInstance().setRamoAtividadePai(ramoAtividadePai);
		}
		return super.beforePersistOrUpdate();
	}

	public void limparTipoEntidade() {
		newInstance();
		ramoAtividadePai = null;
	}

	public void set(RamoAtividade ramoAtividade) {
		instance = ramoAtividade;
		getInstance().setRamoAtividadePai(ramoAtividadePai);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			ramoAtividadePai = getInstance().getRamoAtividadePai();
		}
		if (id == null) {
			ramoAtividadePai = null;
		}
	}

	/*
	 * Grava o registro atual e seta o localizacaoPai do proximo com o valor do
	 * localizacao do ultimo registro inserido
	 */
	public String persistAndNext() {
		String outcome = null;
		try {
			outcome = super.persist();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if (outcome != null) {
			if (!outcome.equals("")) {
				RamoAtividade me = getInstance();
				newInstance();
				getInstance().setRamoAtividadePai(me);
				getEntityManager().flush();
				ramoAtividadePai = getInstance().getRamoAtividadePai();
				getInstance().setRamoAtividadePai(ramoAtividadePai);
			}
		}
		return outcome;
	}

	public RamoAtividade getRamoAtividadePai() {
		return ramoAtividadePai;
	}

	public void setRamoAtividadePai(RamoAtividade ramoAtividadePai) {
		this.ramoAtividadePai = ramoAtividadePai;
	}

	@Override
	public void onClickFormTab() {
		if (isManaged()) {
			ramoAtividadePai = getInstance().getRamoAtividadePai();
		} else {
			ramoAtividadePai = null;
		}
		super.onClickFormTab();
	}

	public SearchTree2GridList<RamoAtividade> getSearchTree2GridList() {
		if (searchTree2GridList == null) {
			RamoAtividade searchBean = getComponent("ramoAtividadeSearch");
			RamoAtividadeTreeHandler treeHandler = getComponent("ramoAtividadeSearchTree");
			searchTree2GridList = new SearchTree2GridList<RamoAtividade>(searchBean, treeHandler);
			String filterName[] = { "ramoAtividade", "codRamoAtividade", "ativo" };
			searchTree2GridList.setFilterName(filterName);
		}
		return searchTree2GridList;
	}

	public RamoAtividade buscarPorCodigo(String codRamoAtividade) {
		String query = "select o from RamoAtividade o where o.codRamoAtividade = :codRamoAtividade";
		Query q = getEntityManager().createQuery(query).setMaxResults(1);
		q.setParameter("codRamoAtividade", codRamoAtividade);
		try {
			return (RamoAtividade) q.getSingleResult();
		}
		catch(NoResultException e) {
			return null;
		}
	}
	
}