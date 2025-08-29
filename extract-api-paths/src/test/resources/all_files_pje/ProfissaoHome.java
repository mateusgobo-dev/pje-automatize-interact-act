package br.com.infox.cliente.home;

import static org.jboss.seam.faces.FacesMessages.instance;

import org.hibernate.Session;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.component.suggest.ProfissaoSuggestBean;
import br.com.infox.cliente.component.tree.ProfissaoTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.SearchTree2GridList;
import br.jus.pje.nucleo.entidades.Profissao;

@Name("profissaoHome")
@BypassInterceptors
public class ProfissaoHome extends AbstractProfissaoHome<Profissao> {

	private static final long serialVersionUID = 1L;
	private Profissao profissaoSuperior;
	private Boolean inativado;
	private SearchTree2GridList<Profissao> searchTree2GridList;
	private static final LogProvider log = Logging.getLogProvider(EtniaHome.class);

	@Override
	public void newInstance() {
		getInstance().setAtivo(Boolean.TRUE);
		limparTrees();
		Contexts.removeFromAllContexts("profissaoSuggest");
		refreshGrid("profissaoGrid");
		super.newInstance();
	}

	private void limparTrees() {
		ProfissaoTreeHandler ret1 = getComponent("profissaoSearchTree");
		if (!getLockedFields().contains("profissaoSuperior")) {
			ret1.clearTree();
		}
		if (searchTree2GridList != null) {
			searchTree2GridList.refreshTreeList();
			searchTree2GridList = null;
		}
		limparProfissaoSuperior();
	}

	@Override
	protected Profissao createInstance() {
		inativado = Boolean.FALSE;
		instance = super.createInstance();
		instance.setProfissaoSuperior(new Profissao());
		return instance;
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			getInstance();
			refreshGrid("profissaoGrid");
			ret = super.persist();
			if (ret != null) {
				limparTrees();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	public String inactiveRecursive(Profissao profissao) {
		
		if (profissao.getProfissaoList().size() > 0) {
			inativarFilhos(profissao);
		}
		
		profissao.setAtivo(Boolean.FALSE);
		String ret = super.update();
		limparTrees();
		refreshGrid("profissaoGrid");
		
		if (ret != null && ret != "") {
		  	FacesMessages.instance().clear();
		  	FacesMessages.instance().add(StatusMessage.Severity.INFO, super.getInactiveSuccess());		  	 
		}
		
		return ret;
	}

	private void inativarFilhos(Profissao profissao) {
		profissao.setAtivo(Boolean.FALSE);

		Integer quantidadeFilhos = profissao.getProfissaoList().size();
		for (int i = 0; i < quantidadeFilhos; i++) {
			inativarFilhos(profissao.getProfissaoList().get(i));
		}
	}

	@Override
	public String update() {
		// Verifica se o pai atual é diferente de nulo
		if ((getInstance().getProfissaoSuperior() != null) && (profissaoSuperior != null)
				&& (!getInstance().getProfissaoSuperior().getProfissao().equals(profissaoSuperior.getProfissao()))) {
			getInstance().getProfissaoSuperior().getProfissaoList().remove(getInstance());
			profissaoSuperior.getProfissaoList().add(getInstance());
			profissaoSuperior.getProfissaoList();
			getInstance().setProfissaoSuperior(profissaoSuperior);
		}
		/*
		 * Se o pai atual não for nulo e o pai selecionado for, o registro é
		 * excluido da lista do pai atual.
		 */
		if ((getInstance().getProfissaoSuperior() != null) && (profissaoSuperior == null)) {
			getInstance().getProfissaoSuperior().getProfissaoList().remove(getInstance());
			getInstance().setProfissaoSuperior(profissaoSuperior);
		}
		/*
		 * Se o pai atual for nulo e o pai selecionado não for, o registro é
		 * adicionado à lista do pai atual.
		 */
		if ((getInstance().getProfissaoSuperior() == null) && (profissaoSuperior != null)) {
			profissaoSuperior.getProfissaoList().add(getInstance());
			getInstance().setProfissaoSuperior(profissaoSuperior);
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
				getEntityManager().merge(getInstance());
				getEntityManager().flush();
				ret = getUpdatedMessage().getValue().toString();
				instance().add(StatusMessage.Severity.ERROR, "Registro alterado com sucesso");
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if (cause instanceof ConstraintViolationException) {
					instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
				}
			}
			return ret;
		}
	}

	@Override
	public String remove(Profissao obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		inativado = Boolean.TRUE;
		super.update();
		newInstance();
		Contexts.removeFromAllContexts("profissaoGrid");
		return "updated";
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (!inativado) {
			getInstance().setProfissaoSuperior(profissaoSuperior);
		}
		return super.beforePersistOrUpdate();
	}

	public void limparTipoEntidade() {
		newInstance();
		profissaoSuperior = null;
	}

	public void set(Profissao profissao) {
		instance = profissao;
		getInstance().setProfissaoSuperior(profissaoSuperior);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			profissaoSuperior = getInstance().getProfissaoSuperior();
		}
		if (id == null) {
			profissaoSuperior = null;
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
				Profissao me = getInstance();
				newInstance();
				getInstance().setProfissaoSuperior(me);
				getEntityManager().flush();
				profissaoSuperior = getInstance().getProfissaoSuperior();
				getInstance().setProfissaoSuperior(profissaoSuperior);
			}
		}
		return outcome;
	}

	public Profissao getProfissaoSuperior() {
		return profissaoSuperior;
	}

	public void setProfissaoSuperior(Profissao profissaoSuperior) {
		this.profissaoSuperior = profissaoSuperior;
	}

	@Override
	public void onClickFormTab() {
		if (isManaged()) {
			profissaoSuperior = getInstance().getProfissaoSuperior();
		} else {
			profissaoSuperior = null;
		}
		super.onClickFormTab();
	}

	public SearchTree2GridList<Profissao> getSearchTree2GridList() {
		if (searchTree2GridList == null) {
			Profissao searchBean = getComponent("profissaoSearch");
			ProfissaoTreeHandler treeHandler = getComponent("profissaoSearchTree");
			searchTree2GridList = new SearchTree2GridList<Profissao>(searchBean, treeHandler);
			String filterName[] = { "codCbo", "ativo", "profissao", "profissaoSuperior" };
			searchTree2GridList.setFilterName(filterName);
		}
		return searchTree2GridList;
	}

	@Override
	public boolean isEditable() {
		return ParametroUtil.instance().getPermitirCadastrosBasicos();
	}
	
	@Override
	/**
	 * Cria uma instância nova da entidade tipada, permitindo que o desenvolvedor
	 * solicite que a entidade antiga seja desligada no contexto de gerenciamento JPA.
	 *  
	 * @param detach true, para desligar a entidade antiga do contexto de gerenciamento JPA.
	 */
	public void clearInstance(boolean detach){

		if (super.isManaged()) {
			try {
				// Faz com que o hibernate pare de gerenciar o objeto, mantendo suas propriedades para reaproveitamento nos locks.
				if(detach){
					((Session) getEntityManager().getDelegate()).evict(instance);
				}
			} catch (Exception e) {
				// Ignora a possível exceção lançada, por exemplo, caso a
				// entidade não seja encontrada.
				log.error("Erro ao limpar a instância atual: [" + e.getLocalizedMessage() + "].", e);
			}
		}

		setId(null);
		clearForm();
		instance = createInstance();
	}

	/**
	 * Limpa o campo suggest de profissao superior.
	 */
	private void limparProfissaoSuperior() {
		this.profissaoSuperior = null;
		ProfissaoSuggestBean.instance().setDefaultValue(null);
		ProfissaoSuggestBean.instance().setInstance(null);
	}
}