package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.PessoaProcuradoriaEntidadeList;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.ProcuradoriaDAO;
import br.jus.cnj.pje.nucleo.manager.CaixaRepresentanteManager;
import br.jus.pje.nucleo.entidades.CaixaRepresentante;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoriaLocal;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;

@Name("pessoaAssistenteProcuradoriaLocalHome")
@BypassInterceptors
public class PessoaAssistenteProcuradoriaLocalHome extends AbstractHome<PessoaAssistenteProcuradoriaLocal> {

	private static final long serialVersionUID = 1L;

	private Procuradoria procuradoria;

	private List<PessoaProcuradoriaEntidade> list = new ArrayList<PessoaProcuradoriaEntidade>(0);
	private boolean checkAll = false;
	private TipoProcuradoriaEnum tipoProcuradoria; 

	@SuppressWarnings("unchecked")
	public PessoaAssistenteProcuradoriaLocal buscarAssistenteLocal(Procuradoria obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from PessoaAssistenteProcuradoriaLocal o ");
		if (getProcuradoria() != null) {
			sb.append("where o.procuradoria = :procuradoria ");
		} else {
			sb.append("where o.procuradoria.localizacao = :localizacao ");
		}
		sb.append("and o.usuario = :usuario");

		Query q = this.getEntityManager().createQuery(sb.toString());
		if (getProcuradoria() != null) {
			q.setParameter("procuradoria", this.getProcuradoria());
		} else {
			q.setParameter("localizacao", Authenticator.getLocalizacaoAtual());
		}
		q.setParameter("usuario", PessoaAssistenteProcuradoriaHome.instance().getInstance());
		List<PessoaAssistenteProcuradoriaLocal> lista = q.getResultList();
		if (lista != null && !lista.isEmpty()) {
			return lista.get(0);
		}
		return null;
	}

	public List<PessoaProcuradoriaEntidade> getList() {
		return this.list;
	}

	public Procuradoria getProcuradoria() {
		return this.procuradoria;
	}
	
	public TipoProcuradoriaEnum getTipoProcuradoria() {
		return tipoProcuradoria;
	}
	
	public void setTipoProcuradoria(TipoProcuradoriaEnum tipoProcuradoria) {
		this.tipoProcuradoria = tipoProcuradoria;
	}

	@SuppressWarnings("unchecked")
	public Procuradoria getProcuradoria(Localizacao localizacao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Procuradoria o ");
		sb.append("where o.localizacao = :localizacao");
		Query q = this.getEntityManager().createQuery(sb.toString());
		q.setParameter("localizacao", localizacao);
		List<Procuradoria> lista = q.getResultList();
		if (lista != null && !lista.isEmpty()) {
			return (Procuradoria) lista.get(0);
		}
		return null;
	}

	public static PessoaAssistenteProcuradoriaLocalHome instance() {
		return ComponentUtil.getComponent("pessoaAssistenteProcuradoriaLocalHome");
	}

	public Boolean getAssitenteProcuradoria() {
		StringBuilder ejbql = new StringBuilder();
		ejbql.append("select count(o) from PessoaAssistenteProcuradoriaLocal o where ");
		ejbql.append("o.procuradoria = :procuradoria ");
		ejbql.append("and o.usuario = :usuario ");
		if (isManaged()) {
			ejbql.append("and o.idUsuarioLocalizacao != :id");
		}

		Query query = this.getEntityManager().createQuery(ejbql.toString());
		query.setParameter("procuradoria", getInstance().getProcuradoria());
		query.setParameter("usuario", getInstance().getUsuario());
		if (isManaged()) {
			query.setParameter("id", getInstance().getIdUsuarioLocalizacao());
		}
		try {
			Long retorno = (Long)query.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	@SuppressWarnings("unchecked")
	public List<Procuradoria> getListAssitenteProcuradoriaItems() {
		List<Procuradoria> assistenteProcuradoriaItems = null;
		
		if(tipoProcuradoria == null){
			assistenteProcuradoriaItems = Collections.EMPTY_LIST;
		} else {
			ProcuradoriaDAO procuradoriaDAO = ComponentUtil.getComponent("procuradoriaDAO");
			assistenteProcuradoriaItems = procuradoriaDAO.getlistProcuradorias(tipoProcuradoria);
		}

		return assistenteProcuradoriaItems;
	}

	@Override
	public void newInstance() {
		setTipoProcuradoria(null);
		setProcuradoria(null);
		limparLista();
		checkAll = false;
		super.newInstance();
	}

	public void limparLista() {
		this.list.clear();
	}

	@Override
	public String persist() {
		this.getInstance().setUsuario(PessoaAssistenteProcuradoriaHome.instance().getInstance().getPessoa());
		this.getInstance().setResponsavelLocalizacao(Boolean.FALSE);

		if (!Authenticator.isAdministradorProcuradoriadefensoria()) {
			Localizacao localizacao = Authenticator.getLocalizacaoAtual();
			Procuradoria procuradoria = this.getProcuradoria(localizacao);

			this.getInstance().setLocalizacaoFisica(localizacao);
			if (procuradoria != null)
				this.getInstance().setProcuradoria(procuradoria);
			else
				FacesMessages.instance().add(Severity.ERROR,
						"Não existe nenhuma procuradoria vinculada a esta localização.");
		} else
			this.getInstance().setLocalizacaoFisica(this.getInstance().getProcuradoria().getLocalizacao());

		this.getInstance().setTipo(this.getInstance().getProcuradoria().getTipo());
		return super.persist();
	}

	@Override
	public String update() {
		if (Authenticator.isAdministradorProcuradoriadefensoria()) {
			if (!getInstance().getLocalizacaoFisica().equals(getInstance().getProcuradoria().getLocalizacao())) { // Houve mudança na localização?
				excluirVinculacaoCaixa(getInstance());				
			}
			getInstance().setLocalizacaoFisica(getInstance().getProcuradoria().getLocalizacao());
		}
		this.getInstance().setTipo(this.getInstance().getProcuradoria().getTipo());

		return super.update();
	}
	
	/**
	 * Método responsável por excluir a vinculação do usuário a uma caixa caso a sua localização seja alterada.
	 */
	private void excluirVinculacaoCaixa(PessoaAssistenteProcuradoriaLocal pessoaAssistenteProcuradoriaLocal) {
		CaixaRepresentanteManager caixaRepresentanteManager = ComponentUtil.getComponent(CaixaRepresentanteManager.NAME);
		List<CaixaRepresentante> caixasRepresentante = caixaRepresentanteManager.recuperarCaixasRepresentante(
				pessoaAssistenteProcuradoriaLocal.getLocalizacaoFisica().getIdLocalizacao(), pessoaAssistenteProcuradoriaLocal.getUsuario().getIdUsuario());
		
		for (CaixaRepresentante caixaRepresentante : caixasRepresentante) {
			getEntityManager().remove(caixaRepresentante);
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (getAssitenteProcuradoria()) {
			FacesMessages.instance().add(Severity.ERROR, "Registro já cadastrado.");
			newInstance();
			return false;
		}
		getInstance().setPapel(ParametroUtil.instance().getPapelAssistenteProcuradoria());
		return super.beforePersistOrUpdate();
	}

	public void setList(List<PessoaProcuradoriaEntidade> list) {
		this.list = list;
	}

	public void setLista(PessoaProcuradoriaEntidade row) {
		if (this.list.contains(row)) {
			this.list.remove(row);
		} else {
			this.list.add(row);
		}
	}

	public void marcarDesmarcaTodos() {
		PessoaProcuradoriaEntidadeList list = getComponent(PessoaProcuradoriaEntidadeList.NAME);
		for (PessoaProcuradoriaEntidade entidade : list.getResultList()) {
			setLista(entidade);
		}
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}

	@Override
	public String remove(PessoaAssistenteProcuradoriaLocal obj) {
		String ret = null;
		try {
			excluirVinculacaoCaixa(obj);
			ret = super.remove(obj);
			newInstance();
		} catch (Exception e) {
			if (e.getLocalizedMessage()
					.contains(
							"org.hibernate.exception.ConstraintViolationException: could not delete: [br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoriaLocal"))
				FacesMessages.instance().add(Severity.ERROR, "Remova a entidade Associada.");
			else
				FacesMessages.instance().add(Severity.ERROR,
						"Falha na transação. Verifique esse erro junto ao suporte.");

			e.printStackTrace();
		}

		return ret;
	}

	public void setCheckAll(boolean checkAll) {
		this.checkAll = checkAll;
	}

	public boolean isCheckAll() {
		return checkAll;
	}
	
	
	
	@Override
	public void setId(Object id) {
		super.setId(id);
		if(id != null){
			this.setTipoProcuradoria(getInstance().getProcuradoria().getTipo());
		}
	}
}