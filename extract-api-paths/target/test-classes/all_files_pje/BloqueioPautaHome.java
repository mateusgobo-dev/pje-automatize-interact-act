package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoAudienciaManager;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.BloqueioPauta;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoAudiencia;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.util.DateUtil;

@Name("bloqueioPautaHome")
public class BloqueioPautaHome extends AbstractHome<BloqueioPauta> {
	private static final long serialVersionUID = -9149517076793225600L;
	private List<Sala> salaAudienciaItens;
	private Boolean tipoOrgaoJulgador;
	private OrgaoJulgador orgao;
	private Sala sala;
	private Date dtInicial;
	private Date dtFinal;

	@In
	private ProcessoAudienciaManager processoAudienciaManager;	
	
	public void setIdBloqueio(Integer id) {
		super.setId(id);
		setOrgao(getInstance().getSalaAudiencia().getOrgaoJulgador());
	}

	public Integer getIdBloqueio() {
		return (Integer) getId();
	}

	public BloqueioPautaHome instance() {
		return getComponent("bloqueioPautaHome");
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorItems() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgador o ");
		sb.append(" where o.ativo = true");
		sb.append(" order by o.orgaoJulgadorOrdemAlfabetica");
		return getEntityManager().createQuery(sb.toString()).getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorUsuarioItems() {
		if (Authenticator.isPapelAdministrador()) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from OrgaoJulgador o ");
			sb.append("where o.ativo = true ");

			return getEntityManager().createQuery(sb.toString()).getResultList(); 
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from OrgaoJulgador o ");
			sb.append(" where o.ativo = true");
			sb.append(" and o.idOrgaoJulgador = :idOrgaoJulgador");
			sb.append(" order by o.orgaoJulgadorOrdemAlfabetica");
			
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("idOrgaoJulgador", Authenticator.getIdOrgaoJulgadorAtual());
			
			return q.getResultList();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Sala> consultaSalaPeloOrgao(OrgaoJulgador orgaoJulgador) {
		if (orgaoJulgador != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select o from Sala o ");
			sb.append("where o.tipoSala = 'A' and o.ativo = true and o.orgaoJulgador = :orgaoJulgador");
			javax.persistence.Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("orgaoJulgador", orgaoJulgador);
			setSalaAudienciaItens(q.getResultList());
		}
		return salaAudienciaItens;
	}

	public List<Sala> getSalaAudienciaItens(OrgaoJulgador orgaoJulgador) {
		if (orgaoJulgador != null) {
			salaAudienciaItens = consultaSalaPeloOrgao(orgaoJulgador);
		}
		return salaAudienciaItens;
	}

	public void setSalaAudienciaItens(List<Sala> salaAudienciaItens) {
		this.salaAudienciaItens = salaAudienciaItens;
	}

	public OrgaoJulgador getOrgao() {
		return orgao;
	}

	public void setOrgao(OrgaoJulgador orgao) {
		this.orgao = orgao;
	}

	public Sala getSala() {
		return sala;
	}

	public void setSala(Sala sala) {
		this.sala = sala;
	}

	public void setTipoOrgaoJulgador(Boolean tipoOrgaoJulgador) {
		this.tipoOrgaoJulgador = tipoOrgaoJulgador;
	}

	public Boolean getTipoOrgaoJulgador() {
		return tipoOrgaoJulgador;
	}

	public void inativar(BloqueioPauta obj) {
		obj.setAtivo(false);
		setId(obj.getIdBloqueioPauta());
		update();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.ERROR, "Registro inativado com sucesso.");
		FacesMessages.instance().clear();
	}
	
	/**
	 * Método responsável por inicializar as variáveis de pesquisa.
	 */
	public void inicializarVariaveis() {
		List<OrgaoJulgador> orgaoJulgadorList = this.getOrgaoJulgadorUsuarioItems();
		
		if (orgaoJulgadorList != null && orgaoJulgadorList.size() == 1) {
			this.orgao = orgaoJulgadorList.get(0);
			
			List<Sala> salaList = this.getSalaAudienciaItens(orgao);
			
			if (salaList != null && salaList.size() == 1) {
				this.sala = salaList.get(0);
			}
		}
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (DateUtil.isDataComHoraMenor(getInstance().getDtFinal(), getInstance().getDtInicial())) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Data Final é Menor que a data inicial!");
			return false;
		}
		
		// Recupera a lista de audiências designadas no período e sala do bloqueio de pauta
		List<ProcessoAudiencia> audiencias = processoAudienciaManager.procurarAudienciasDesignadasPorPeriodo(
				getInstance().getDtInicial(), getInstance().getDtFinal(), getInstance().getSalaAudiencia());
		
		// Se existem audiências designadas na sala e no período do bloqueio da sala...
		if (audiencias.size() > 0) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Bloqueio de pauta não realizado! \n");
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Verifique audiências ativas no Menu:\n"); 
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Audiências e Sessões > Pauta de Audiência.");
			return false;
		}		
		
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		return super.persist();
	}

	@Override
	public void onClickFormTab() {
		super.onClickFormTab();
		if (getInstance().getAtivo() == null) {
			getInstance().setAtivo(true);
		}
	}
	
	@Override
	public void onClickSearchTab() {
		super.onClickSearchTab();
		inicializarVariaveis();
	}

	@Override
	public void newInstance() {
		orgao = null;
		sala = null;
		dtInicial = null;
		dtFinal = null;
		super.newInstance();
		getInstance().setAtivo(true);
	}

	public Date getDtInicial() {
		return dtInicial;
	}

	public void setDtInicial(Date dtInicial) {
		this.dtInicial = dtInicial;
	}

	public Date getDtFinal() {
		return dtFinal;
	}

	public void setDtFinal(Date dtFinal) {
		this.dtFinal = dtFinal;
	}
}
