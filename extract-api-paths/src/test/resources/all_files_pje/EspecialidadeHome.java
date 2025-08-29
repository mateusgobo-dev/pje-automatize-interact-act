package br.com.infox.cliente.home;

import static org.jboss.seam.faces.FacesMessages.instance;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.cliente.component.tree.EspecialidadeTreeHandler;
import br.com.infox.component.tree.SearchTree2GridList;
import br.jus.pje.nucleo.entidades.Especialidade;

@Name("especialidadeHome")
@BypassInterceptors
public class EspecialidadeHome extends AbstractEspecialidadeHome<Especialidade> {

	private static final long serialVersionUID = 1L;
	private Especialidade especialidadePai;
	private Boolean inativado;
	private SearchTree2GridList<Especialidade> searchTree2GridList;

	@Override
	public void newInstance() {
		getInstance().setAtivo(Boolean.TRUE);
		limparTrees();
		refreshGrid("especialidadeGrid");
		super.newInstance();
	}

	private void limparTrees() {
		EspecialidadeTreeHandler ret1 = getComponent("especialidadeSearchTree");
		EspecialidadeTreeHandler ret2 = getComponent("especialidadeFormTree");
		if (!getLockedFields().contains("especialidadePai"))
			ret1.clearTree();
		ret2.clearTree();
		if (searchTree2GridList != null) {
			searchTree2GridList.refreshTreeList();
			searchTree2GridList = null;
		}
	}

	@Override
	protected Especialidade createInstance() {
		inativado = Boolean.FALSE;
		instance = super.createInstance();
		instance.setEspecialidadePai(new Especialidade());
		return instance;
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			getInstance();
			refreshGrid("especialidadeGrid");
			ret = super.persist();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	public String inactiveRecursive(Especialidade especialidade) {
		if (especialidade.getEspecialidadeList().size() > 0) {
			inativarFilhos(especialidade);
		}
		especialidade.setAtivo(Boolean.FALSE);
		String ret = super.update();
		limparTrees();
		refreshGrid("especialidadeGrid");
		return ret;
	}

	private void inativarFilhos(Especialidade especialidade) {
		especialidade.setAtivo(Boolean.FALSE);

		Integer quantidadeFilhos = especialidade.getEspecialidadeList().size();
		for (int i = 0; i < quantidadeFilhos; i++) {
			inativarFilhos(especialidade.getEspecialidadeList().get(i));
		}
	}

	@Override
	public String update() {
		// Verifica se o pai atual é diferente de nulo
		if ((getInstance().getEspecialidadePai() != null)
				&& (especialidadePai != null)
				&& (!getInstance().getEspecialidadePai().getEspecialidade().equals(especialidadePai.getEspecialidade()))) {
			getInstance().getEspecialidadePai().getEspecialidadeList().remove(getInstance());
			especialidadePai.getEspecialidadeList().add(getInstance());
			especialidadePai.getEspecialidadeList();
		}
		/*
		 * Se o pai atual não for nulo e o pai selecionado for, o registro é
		 * excluido da lista do pai atual.
		 */
		if ((getInstance().getEspecialidadePai() != null) && (especialidadePai == null)) {
			getInstance().getEspecialidadePai().getEspecialidadeList().remove(getInstance());
		}
		/*
		 * Se o pai atual for nulo e o pai selecionado não for, o registro é
		 * adicionado à lista do pai atual.
		 */
		if ((getInstance().getEspecialidadePai() == null) && (especialidadePai != null)) {
			especialidadePai.getEspecialidadeList().add(getInstance());
			getInstance().setEspecialidadePai(especialidadePai);
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
				/*
				 * getEntityManager().merge(getInstance());
				 * getEntityManager().flush();
				 */
				super.update();
				ret = getUpdatedMessage().getValue().toString();
				/*
				 * instance().add(StatusMessage.Severity.INFO,
				 * "Registro alterado com sucesso");
				 */
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if (cause instanceof ConstraintViolationException) {
					instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
				}
			}
			// try{
			// ret = super.update();
			// limparTrees();
			// }
			// catch (Exception e) {
			// System.out.println(e.getMessage());
			// }
			return ret;
		}
	}

	@Override
	public String remove(Especialidade obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		inativado = Boolean.TRUE;
		super.update();
		newInstance();
		Contexts.removeFromAllContexts("especialidadeGrid");
		return "updated";
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (!inativado) {
			getInstance().setEspecialidadePai(especialidadePai);
		}
		return super.beforePersistOrUpdate();
	}

	public void limparTipoEntidade() {
		newInstance();
		especialidadePai = null;
	}

	public void set(Especialidade especialidade) {
		instance = especialidade;
		getInstance().setEspecialidadePai(especialidadePai);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			especialidadePai = getInstance().getEspecialidadePai();
		}
		if (id == null) {
			especialidadePai = null;
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
			System.out.println(e.getMessage());
		}
		if (outcome != null) {
			if (!outcome.equals("")) {
				Especialidade me = getInstance();
				newInstance();
				getInstance().setEspecialidadePai(me);
				getEntityManager().flush();
				especialidadePai = getInstance().getEspecialidadePai();
				getInstance().setEspecialidadePai(especialidadePai);
			}
		}
		return outcome;
	}

	public Especialidade getEspecialidadePai() {
		return especialidadePai;
	}

	public void setEspecialidadePai(Especialidade especialidadePai) {
		this.especialidadePai = especialidadePai;
	}

	@Override
	public void onClickFormTab() {
		if (isManaged()) {
			especialidadePai = getInstance().getEspecialidadePai();
		} else {
			especialidadePai = null;
		}
		super.onClickFormTab();
	}

	public SearchTree2GridList<Especialidade> getSearchTree2GridList() {
		if (searchTree2GridList == null) {
			Especialidade searchBean = getComponent("especialidadeSearch");
			EspecialidadeTreeHandler treeHandler = getComponent("especialidadeSearchTree");
			searchTree2GridList = new SearchTree2GridList<Especialidade>(searchBean, treeHandler);
			String[] filterName = { "codEspecialidade", "ativo", "especialidade" };
			searchTree2GridList.setFilterName(filterName);
		}
		return searchTree2GridList;
	}
}