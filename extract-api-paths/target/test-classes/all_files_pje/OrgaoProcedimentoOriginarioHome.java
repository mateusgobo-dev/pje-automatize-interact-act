package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.ibpm.component.suggest.CepSuggestBean;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.OrgaoProcedimentoOriginario;
import br.jus.pje.nucleo.entidades.TipoOrigem;

@Scope(ScopeType.CONVERSATION)
@Name("orgaoProcedimentoOriginarioHome")
@BypassInterceptors
public class OrgaoProcedimentoOriginarioHome extends
		AbstractOrgaoProcedimentoOriginarioHome<OrgaoProcedimentoOriginario> {

	private static final long serialVersionUID = 1L;

	private String cdUf;

	private CepSuggestBean getCepSuggestBean() {
		return getComponent("cepSuggest");
	}

	public void setCep(Cep cep) {
		getCepSuggestBean().setInstance(cep);
	}

	public Cep getCep() {
		return getCepSuggestBean().getInstance();
	}

	@Override
	public void setOrgaoProcedimentoOriginarioId(Integer id) {
		super.setOrgaoProcedimentoOriginarioId(id);
		OrgaoProcedimentoOriginario opo = getInstance();

		// APENAS PARA FORÇAR ATUALIZAÇÃO DO OBJETO.
		Cep cep = opo.getCep();
		setCep(cep);
	}

	@Observer("cepChangedEvent")
	public void setEndereco(Cep cep) {
		OrgaoProcedimentoOriginario opo = getInstance();
		if (cep != null) {
			String nmLogradouro = cep.getNomeLogradouro();
			String nmBairro = cep.getNomeBairro();
			if(cep.getMunicipio() != null) {
				String cdUf = cep.getMunicipio().getEstado().getCodEstado();
				String nmCidade = cep.getMunicipio().getMunicipio();
				opo.setCdUf(cdUf);
				opo.setNmCidade(nmCidade);
			}

			opo.setCep(cep);
			opo.setNmLogradouro(nmLogradouro);
			opo.setNmBairro(nmBairro);
		}
	}

	/*
	 *  *************************** PERSISTÊNCIA
	 * ********************************************************
	 */

	@Override
	protected boolean beforePersistOrUpdate() {
		if (getInstance().getCep() == null) {
			String mensagem = Messages.instance().get("orgaoProcedimentoOriginario.cep.problema.problema");
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, mensagem);
		} else {
			// Correção de NullPointer ao cadastrar dois órgãos em sequência
			if (getInstance().getCdUf() == null) {
				this.setEndereco(getInstance().getCep());
			} else if (!naoHaRegistroComMesmoCodigoOrigem(getInstance())) {
				String mensagem = Messages.instance().get("orgaoProcedimentoOriginario.jaCadastradoNoSistema");
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, mensagem);
				return false;
			}
		}
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		if (beforePersistOrUpdate()) {
			String resultado = super.persist();
			refreshGrid("orgaoProcedimentoOriginarioGrid");
			return resultado;
		}

		return "";
	}

	@Override
	public String remove(OrgaoProcedimentoOriginario o) {
		String mensagem = Messages.instance().get("orgaoProcedimentoOriginario.emUso");
		FacesMessages.instance().add(StatusMessage.Severity.ERROR, mensagem);
		return "";
	}

	@Override
	public void newInstance() {
		getCepSuggestBean().setInstance(null);
		super.newInstance();
	}

	@Override
	public String update() {
		if (beforePersistOrUpdate()) {
			return super.update();
		}
		return "";
	}

	protected boolean naoHaRegistroComMesmoCodigoOrigem(OrgaoProcedimentoOriginario opo) {
		String sql = sqlNaoHaRegistroComMesmoCodigoOrigem(opo);
		Query q = super.getEntityManager().createQuery(sql);
		return q.getResultList().isEmpty();
	}

	public String sqlNaoHaRegistroComMesmoCodigoOrigem(OrgaoProcedimentoOriginario opo) {
		StringBuilder sql = new StringBuilder();
		sql.append(" select opo from OrgaoProcedimentoOriginario opo ");
		sql.append(" where  ");
		sql.append("       opo.ativo = true ");
		sql.append(opo.getId() != null ? evitaCompararComProprioRegistro(opo.getId()) : "");
		sql.append(comparaNomeOrgaoProcedimento(opo.getDsNomeOrgao()));
		sql.append(comparaCdUf(opo.getCdUf()));
		sql.append(comparaNmCidade(opo.getNmCidade()));
		sql.append(comparaTipoOrigem(opo.getTipoOrigem()));
		
		return sql.toString();
	}

	private String comparaTipoOrigem(TipoOrigem tipoOrigem) {
		return "   and opo.tipoOrigem = " + tipoOrigem.getId();
	}

	private String evitaCompararComProprioRegistro(Integer id) {
		return "   and opo.id <> " + id;
	}

	private String comparaNomeOrgaoProcedimento(String dsNomeOrgao) {
		return "   and lower(opo.dsNomeOrgao) = '" + dsNomeOrgao.toLowerCase() + "' ";
	}

	private String comparaCdUf(String cdUf) {
		return "   and lower(opo.cdUf) = '" + cdUf.toLowerCase() + "' ";
	}

	private String comparaNmCidade(String nmCidade) {
		return "   and lower(opo.nmCidade) = '" + nmCidade.toLowerCase() + "' ";
	}

	/*
	 *  *************************** COMBOBOX: listaTipoOrigem e listaEstado (UFs)
	 * **********************
	 */

	public List<SelectItem> listaTipoOrigem() {
		EntityManager em = super.getEntityManager();
		ListaComboTipoOrigem listaCombo = new ListaComboTipoOrigem(em);
		String hql = "select to from TipoOrigem to where to.ativo = true ";
		listaCombo.setInstrucaoHql(hql);
		listaCombo.executar();
		return listaCombo.resultado();
	}

	public List<SelectItem> listaEstado() {
		EntityManager em = super.getEntityManager();
		ListaComboEstado listaCombo = new ListaComboEstado(em);
		String hql = "select e from Estado e where e.ativo = true ";
		listaCombo.setInstrucaoHql(hql);
		listaCombo.executar();
		return listaCombo.resultado();
	}

	class ListaComboTipoOrigem {

		List<SelectItem> itensParaExibicao;
		List<TipoOrigem> itensDaBase;
		EntityManager entityManager;
		String hql = "";

		public ListaComboTipoOrigem(EntityManager entityManager) {
			this.entityManager = entityManager;
			inicializar();
		}

		private void inicializar() {
			itensParaExibicao = new ArrayList<SelectItem>();
			itensParaExibicao.add(new SelectItem(0, "Selecione"));
		}

		public void setInstrucaoHql(String hql) {
			this.hql = hql;
		}

		public void executar() {
			if (this.hql.isEmpty() == false) {
				lerRegistrosDaBase();
				preencherExibicao();
			}
		}

		public List<SelectItem> resultado() {
			return getItensParaExibicao();
		}

		@SuppressWarnings("unchecked")
		private void lerRegistrosDaBase() {
			StringBuilder sb = new StringBuilder();
			sb.append(hql);
			Query q = this.entityManager.createQuery(sb.toString());
			itensDaBase = q.getResultList();
		}

		private void preencherExibicao() {
			for (TipoOrigem t : itensDaBase) {
				adicioneItemNaExibicao(t);
			}
		}

		private void adicioneItemNaExibicao(TipoOrigem t) {
			long id = t.getId();
			String descricao = t.getDsTipoOrigem();
			SelectItem novoItemCombo = new SelectItem(id, descricao);
			itensParaExibicao.add(novoItemCombo);
		}

		private List<SelectItem> getItensParaExibicao() {
			return itensParaExibicao;
		}
	}

	class ListaComboEstado {

		List<SelectItem> itensParaExibicao;
		List<Estado> itensDaBase;
		EntityManager entityManager;
		String hql = "";

		public ListaComboEstado(EntityManager entityManager) {
			this.entityManager = entityManager;
			inicializar();
		}

		private void inicializar() {
			itensParaExibicao = new ArrayList<SelectItem>();
		}

		public void setInstrucaoHql(String hql) {
			this.hql = hql;
		}

		public void executar() {
			if (this.hql.isEmpty() == false) {
				lerRegistrosDaBase();
				preencherExibicao();
			}
		}

		public List<SelectItem> resultado() {
			return getItensParaExibicao();
		}

		@SuppressWarnings("unchecked")
		private void lerRegistrosDaBase() {
			StringBuilder sb = new StringBuilder();
			sb.append(hql);
			Query q = this.entityManager.createQuery(sb.toString());
			itensDaBase = q.getResultList();
		}

		private void preencherExibicao() {
			for (Estado t : itensDaBase) {
				adicioneItemNaExibicao(t);
			}
		}

		private void adicioneItemNaExibicao(Estado t) {
			String codigo = t.getCodEstado();
			String descricao = t.getEstado();
			SelectItem novoItemCombo = new SelectItem(codigo, codigo + " - " + descricao);
			itensParaExibicao.add(novoItemCombo);
		}

		private List<SelectItem> getItensParaExibicao() {
			return itensParaExibicao;
		}
	}

	public String getCdUf() {
		return cdUf;
	}

	public void setCdUf(String cdUf) {
		this.cdUf = cdUf;
	}
}
