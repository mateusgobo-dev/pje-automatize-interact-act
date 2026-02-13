package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.component.tree.TipoPessoaTreeHandler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.component.tree.SearchTree2GridList;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Qualificacao;
import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.nucleo.entidades.TipoPessoaQualificacao;
import br.jus.pje.nucleo.entidades.TipoPessoaQualificacaoId;

@Name("tipoPessoaHome")
@BypassInterceptors
public class TipoPessoaHome extends AbstractTipoPessoaHome<TipoPessoa> {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(TipoPessoaHome.class);
	private TipoPessoa tipoPessoaSuperior;
	private Boolean inativado;
	private SearchTree2GridList<TipoPessoa> searchTree2GridList;
	private List<TipoPessoa> listaEntidades = new ArrayList<TipoPessoa>(0);

	@Override
	public void newInstance() {
		getInstance().setAtivo(Boolean.TRUE);
		limparTree();
		refreshGrid("tipoPessoaGrid");
		listaEntidades = new ArrayList<TipoPessoa>(0);
		super.newInstance();
	}

	private void limparTree() {
		TipoPessoaTreeHandler ret1 = getComponent("tipoPessoaSearchTree");
		TipoPessoaTreeHandler ret2 = getComponent("tipoPessoaFormTree");
		if (!getLockedFields().contains("tipoPessoaSuperior")) {
			ret1.clearTree();
		}
		ret2.clearTree();
		if (searchTree2GridList != null) {
			searchTree2GridList.refreshTreeList();
			searchTree2GridList = null;
		}
	}

	@Override
	protected TipoPessoa createInstance() {
		inativado = Boolean.FALSE;
		instance = super.createInstance();
		instance.setTipoPessoaSuperior(new TipoPessoa());
		return instance;
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
			if (ret != null) {
				limparTree();
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return ret;
	}

	/*
	 * verifica se o registro esta sendo usado em alguma de suas ligacoes. se n
	 * estiver, realiza a inativacao em cascata
	 */
	public String inactiveRecursive(TipoPessoa tipoPessoa) {
		if (tipoPessoa.getTipoPessoaList().size() > 0) {
			inativarFilhos(tipoPessoa);
		}
		tipoPessoa.setAtivo(Boolean.FALSE);
		String ret = super.update();
		limparTree();
		refreshGrid("tipoPessoaGrid");
		return ret;
	}

	private void inativarFilhos(TipoPessoa tipoPessoa) {
		tipoPessoa.setAtivo(Boolean.FALSE);
		Integer quantidadeFilhos = tipoPessoa.getTipoPessoaList().size();
		for (int i = 0; i < quantidadeFilhos; i++) {
			inativarFilhos(tipoPessoa.getTipoPessoaList().get(i));
		}
	}

	@Override
	public String update() {
		// Verifica se o pai atual é diferente de nulo
		if ((getInstance().getTipoPessoaSuperior() != null) && (tipoPessoaSuperior != null)
				&& (!getInstance().getTipoPessoaSuperior().getTipoPessoa().equals(tipoPessoaSuperior.getTipoPessoa()))) {
			getInstance().getTipoPessoaSuperior().getTipoPessoaList().remove(getInstance());
			tipoPessoaSuperior.getTipoPessoaList().add(getInstance());
			tipoPessoaSuperior.getTipoPessoaList();
		}

		/*
		 * Se o pai atual não for nulo e o pai selecionado for, o registro é
		 * excluido da lista do pai atual.
		 */
		if ((getInstance().getTipoPessoaSuperior() != null) && (tipoPessoaSuperior == null)) {
			getInstance().getTipoPessoaSuperior().getTipoPessoaList().remove(getInstance());
		}
		/*
		 * Se o pai atual for nulo e o pai selecionado não for, o registro é
		 * adicionado à lista do pai atual.
		 */
		if ((getInstance().getTipoPessoaSuperior() == null) && (tipoPessoaSuperior != null)) {
			tipoPessoaSuperior.getTipoPessoaList().add(getInstance());
			getInstance().setTipoPessoaSuperior(tipoPessoaSuperior);
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
				limparTree();
			} catch (Exception e) {
				log.error(e.getMessage());
			}
			return ret;
		}
	}

	@Override
	public String remove(TipoPessoa obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		inativado = Boolean.TRUE;
		super.update();
		newInstance();
		Contexts.removeFromAllContexts("tipoPessoaGrid");
		return "updated";
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (!inativado) {
			getInstance().setTipoPessoaSuperior(tipoPessoaSuperior);
		}
		return super.beforePersistOrUpdate();
	}

	public void set(TipoPessoa tipoPessoa) {
		instance = tipoPessoa;
		getInstance().setTipoPessoaSuperior(tipoPessoaSuperior);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (isManaged() && changed) {
			tipoPessoaSuperior = getInstance().getTipoPessoaSuperior();
		}
		if (id == null) {
			tipoPessoaSuperior = null;
		}
	}

	public String persistAndNext() {
		String outcome = null;
		try {
			outcome = persist();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		if (outcome != null) {
			if (!outcome.equals("")) {
				TipoPessoa me = getInstance();
				newInstance();
				getInstance().setTipoPessoaSuperior(me);
				getEntityManager().flush();
				tipoPessoaSuperior = getInstance().getTipoPessoaSuperior();
				getInstance().setTipoPessoaSuperior(tipoPessoaSuperior);
			}
		}
		return outcome;
	}

	public TipoPessoa getTipoPessoaSuperior() {
		return tipoPessoaSuperior;
	}

	public void setTipoPessoaSuperior(TipoPessoa tipoPessoaSuperior) {
		this.tipoPessoaSuperior = tipoPessoaSuperior;
	}

	@Override
	public void onClickFormTab() {
		if (isManaged()) {
			tipoPessoaSuperior = getInstance().getTipoPessoaSuperior();
		} else {
			tipoPessoaSuperior = null;
		}
		super.onClickFormTab();
	}

	public SearchTree2GridList<TipoPessoa> getSearchTree2GridList() {
		if (searchTree2GridList == null) {
			TipoPessoa searchBean = getComponent("tipoPessoaSearch");
			TipoPessoaTreeHandler treeHandler = getComponent("tipoPessoaSearchTree");
			searchTree2GridList = new SearchTree2GridList<TipoPessoa>(searchBean, treeHandler);
			String[] filterName = { "tipoPessoa", "ativo" };
			searchTree2GridList.setFilterName(filterName);
		}
		return searchTree2GridList;
	}

	/*
	 * Métodos para Adicionar e Remover documentos de identificação de certo
	 * tipo de usuário
	 */
	public void addTipoPessoaQualificacao(Qualificacao obj, String gridId) {
		TipoPessoaQualificacaoId tId = new TipoPessoaQualificacaoId();
		tId.setIdTipoPessoa(getInstance().getIdTipoPessoa());
		tId.setIdQualificacao(obj.getIdQualificacao());
		TipoPessoaQualificacao tp = new TipoPessoaQualificacao();
		tp.setId(tId);
		tp.setObrigatorio(true);
		tp.setQualificacao(obj);
		tp.setTipoPessoa(getInstance());

		tp = getEntityManager().merge(tp);
		getInstance().getTipoPessoaQualificacaoList().add(tp);
		obj.getTipoPessoaQualificacaoList().add(tp);

		getEntityManager().flush();
		refreshGrid(gridId);
		refreshGrid("tipoPessoaQualificacaoGrid");
	}

	public void removeTipoPessoaQualificacao(TipoPessoaQualificacao obj, String gridId) {
		getInstance().getTipoPessoaQualificacaoList().remove(obj);
		getEntityManager().remove(obj);
		getEntityManager().flush();
		refreshGrid(gridId);
		refreshGrid("qualificacaoGrid");
	}

	public List<TipoPessoa> listaEntidades() {
		String query = "select o from TipoPessoa o where o.tipoPessoa = 'Entidades'";
		Query q = getEntityManager().createQuery(query).setMaxResults(1);
		TipoPessoa tp = (TipoPessoa) q.getSingleResult();

		listaEntidades.add(tp);
		filhosEntidade(tp);
		return listaEntidades;
	}

	@SuppressWarnings("unchecked")
	private void filhosEntidade(TipoPessoa tp) {
		String query = "select o from TipoPessoa o where o.tipoPessoaSuperior = :tipoPessoa";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("tipoPessoa", tp);
		List<TipoPessoa> listaFilhos = q.getResultList();

		listaEntidades.addAll(listaFilhos);
		Integer tamVet = listaFilhos.size();

		for (int i = 0; i < tamVet; i++) {
			filhosEntidade(listaFilhos.get(i));
		}
	}

	public static TipoPessoaHome instance() {
		return ComponentUtil.getComponent("tipoPessoaHome");
	}

	@Override
	public boolean isEditable() {
		return ParametroUtil.instance().getPermitirCadastrosBasicos();
	}
	
	public TipoPessoa buscarPorCodigo(String codTipoPessoa) {
		String query = "select o from TipoPessoa o where o.codTipoPessoa = :codTipoPessoa";
		Query q = getEntityManager().createQuery(query).setMaxResults(1);
		q.setParameter("codTipoPessoa", codTipoPessoa);
		try {
			return (TipoPessoa) q.getSingleResult();
		}
		catch(NoResultException e) {
			return null;
		}
	}
	
}