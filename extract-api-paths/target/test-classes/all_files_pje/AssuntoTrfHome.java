package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.cliente.component.tree.AssuntoTrfTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.SearchTree2GridList;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

@Name(AssuntoTrfHome.NAME)
@BypassInterceptors
public class AssuntoTrfHome extends AbstractAssuntoTrfHome<AssuntoTrf> {

	private static final long serialVersionUID = 1L;
	private AssuntoTrf assuntoTrfSuperior;
	private SearchTree2GridList<AssuntoTrf> searchTree2GridList;
	public static final String NAME = "assuntoTrfHome";

	public static AssuntoTrfHome instance() {
		return ComponentUtil.getComponent(AssuntoTrfHome.NAME);
	}
	
	@Create
	public void create() {
		super.create();
		this.iniciaSearchTab();
	}

	public void iniciaSearchTab() {
		AssuntoTrf searchBean = getComponent("assuntoTrfSearch");
		searchBean.setPadraoSgt(null);
	}
	
	@Override
	public void onClickSearchTab() {
		super.onClickSearchTab();
		this.iniciaSearchTab();
	}

	@Override
	public void newInstance() {
		limparTrees();
		refreshGrid("assuntoTrfGrid");
		super.newInstance();
		getInstance().setAtivo(Boolean.TRUE);
		getInstance().setPadraoSgt(Boolean.FALSE);
	}

	private void limparTrees() {
		AssuntoTrfTreeHandler ret1 = getComponent("assuntoTrfSearchTree");
		AssuntoTrfTreeHandler ret2 = getComponent("assuntoTrfFormTree");
		if (!getLockedFields().contains("assuntoTrfSuperior"))
			ret1.clearTree();
		ret2.clearTree();
		if (searchTree2GridList != null) {
			searchTree2GridList.refreshTreeList();
			searchTree2GridList = null;
		}
	}

	@Override
	protected AssuntoTrf createInstance() {
		instance = super.createInstance();
		instance.setAssuntoTrfSuperior(new AssuntoTrf());
		return instance;
	}

	@Override
	public String persist() {
		AssuntoTrf assuntoTrf = getInstance();
		assuntoTrf.setAssuntoCompleto(assuntoTrf.getAssuntoTrf());
		String ret = super.persist();
		return ret;
	}

	public String inactiveRecursive(AssuntoTrf assuntoTrf) {
		if (assuntoTrf.getAssuntoTrfList().size() > 0) {
			inativarFilhos(assuntoTrf);
		}
		assuntoTrf.setAtivo(Boolean.FALSE);
		String ret = super.update();
		limparTrees();
		refreshGrid("assuntoTrfGrid");
		return ret;
	}

	private void inativarFilhos(AssuntoTrf assuntoTrf) {
		assuntoTrf.setAtivo(Boolean.FALSE);

		Integer quantidadeFilhos = assuntoTrf.getAssuntoTrfList().size();
		for (int i = 0; i < quantidadeFilhos; i++) {
			inativarFilhos(assuntoTrf.getAssuntoTrfList().get(i));
		}
	}

	@Override
	public String update() {
		/*
		 * Se o registro estiver como inativo na hora do update, todos os seus
		 * filhos serão inativados
		 */
		verificaListas();
		if (!getInstance().getAtivo()) {
			inactiveRecursive(getInstance());
			return "updated";
		} else {
			String ret = null;
			try {
				ret = super.update();
				// limparTrees();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			return ret;
		}
	}

	@Override
	public String remove(AssuntoTrf obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		super.update();
		newInstance();
		Contexts.removeFromAllContexts("assuntoTrfGrid");
		return "updated";
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		double valorPesoAssuntoMin = Double.parseDouble(ParametroUtil.getFromContext("valorPesoAssuntoMin", true));
		double valorPesoAssuntoMax = Double.parseDouble(ParametroUtil.getFromContext("valorPesoAssuntoMax", true));
		boolean valorValido = instance.getValorPeso() >= valorPesoAssuntoMin && instance.getValorPeso() <= valorPesoAssuntoMax;
		if (!valorValido) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Valor de peso precisa estar entre {0} e {1}.", valorPesoAssuntoMin, valorPesoAssuntoMax);
			return false;
		}
		getInstance().setAssuntoTrfSuperior(assuntoTrfSuperior);
		return super.beforePersistOrUpdate();
	}

	public void limparGrupoTarefa() {
		newInstance();
		assuntoTrfSuperior = null;
	}

	public void set(AssuntoTrf assuntoTrf) {
		instance = assuntoTrf;
		getInstance().setAssuntoTrfSuperior(assuntoTrfSuperior);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			assuntoTrfSuperior = getInstance().getAssuntoTrfSuperior();
		}
		if (id == null) {
			assuntoTrfSuperior = null;
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
				AssuntoTrf me = getInstance();
				newInstance();
				getInstance().setAssuntoTrfSuperior(me);
				EntityUtil.flush();
				assuntoTrfSuperior = getInstance().getAssuntoTrfSuperior();
				getInstance().setAssuntoTrfSuperior(assuntoTrfSuperior);
			}
		}
		return outcome;
	}

	public AssuntoTrf getAssuntoTrfSuperior() {
		return assuntoTrfSuperior;
	}

	public void setAssuntoTrfSuperior(AssuntoTrf assuntoTrfSuperior) {
		this.assuntoTrfSuperior = assuntoTrfSuperior;
	}

	@Override
	public void onClickFormTab() {
		if (isManaged()) {
			assuntoTrfSuperior = getInstance().getAssuntoTrfSuperior();
		} else {
			assuntoTrfSuperior = null;
		}
		super.onClickFormTab();
	}

	// Método da treeView na aba de pesquisa que retorna as informações
	// na Grid do listView
	public SearchTree2GridList<AssuntoTrf> getSearchTree2GridList() {
		if (searchTree2GridList == null) {
			AssuntoTrf searchBean = getComponent("assuntoTrfSearch");
			AssuntoTrfTreeHandler treeHandler = getComponent("assuntoTrfSearchTree");
			searchTree2GridList = new SearchTree2GridList<AssuntoTrf>(searchBean, treeHandler);
			String filterName[] = { "codAssuntoTrfOutro", "ativo", "codAssuntoTrf", "assuntoTrf" };
			searchTree2GridList.setFilterName(filterName);
		}
		return searchTree2GridList;
	}

	private void verificaListas() {
		/*
		 * Verifica se o pai atual e o pai selecionado são diferentes de nulo e
		 * se os dois são diferentes um do outro e remove o registro da lista do
		 * pai atual e insere na lista do pai selecionado.
		 */
		if ((getInstance().getAssuntoTrfSuperior() != null) && (assuntoTrfSuperior != null)
				&& (!getInstance().getAssuntoTrfSuperior().getAssuntoTrf().equals(assuntoTrfSuperior.getAssuntoTrf()))) {

			getInstance().getAssuntoTrfSuperior().getAssuntoTrfList().remove(getInstance());
			assuntoTrfSuperior.getAssuntoTrfList().add(getInstance());
			assuntoTrfSuperior.getAssuntoTrfList();
		}
		/*
		 * Se o pai atual não for nulo e o pai selecionado for, o registro é
		 * excluido da lista do pai atual.
		 */
		if ((getInstance().getAssuntoTrfSuperior() != null) && (assuntoTrfSuperior == null)) {
			getInstance().getAssuntoTrfSuperior().getAssuntoTrfList().remove(getInstance());
		}
		/*
		 * Se o pai atual for nulo e o pai selecionado não for, o registro é
		 * adicionado à lista do pai atual.
		 */
		if ((getInstance().getAssuntoTrfSuperior() == null) && (assuntoTrfSuperior != null)) {
			assuntoTrfSuperior.getAssuntoTrfList().add(getInstance());
			getInstance().setAssuntoTrfSuperior(assuntoTrfSuperior);
		}
	}

	public void pesoChangedInGrid() {
		EntityUtil.flush();
	}
}