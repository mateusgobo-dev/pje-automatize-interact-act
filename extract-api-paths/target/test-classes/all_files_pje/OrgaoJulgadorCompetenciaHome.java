package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.entidades.Sala;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.tree.CompetenciaTreeHandler;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.pje.nucleo.entidades.CompetenciaClasseAssunto;
import br.jus.pje.nucleo.entidades.OjClasseTipoAudiencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoCompetencia;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCompetencia;

@Name("orgaoJulgadorCompetenciaHome")
@BypassInterceptors
public class OrgaoJulgadorCompetenciaHome extends AbstractOrgaoJulgadorCompetenciaHome<OrgaoJulgadorCompetencia> {

	private static final long serialVersionUID = 1L;
	private String tipo = "M";
	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;

	public static OrgaoJulgadorCompetenciaHome instance() {
		return ComponentUtil.getComponent("orgaoJulgadorCompetenciaHome");
	}

	public Boolean isDataValida() {
		if (instance.getDataFim() != null && instance.getDataFim().after(instance.getDataInicio())) {
			return true;
		}
		if (instance.getDataFim() == null) {
			return true;
		}
		return false;
	}

	private Boolean verificarOJ() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from OrgaoJulgadorCompetencia o ");
		sb.append("where o.competencia = :competencia and ");
		sb.append("o.orgaoJulgador = :orgaoJulgador");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("competencia", CompetenciaHome.instance().getInstance());
		q.setParameter("orgaoJulgador", instance.getOrgaoJulgador());
		try {
			Long retorno = (Long) q.getSingleResult();
			if (retorno > 0) {
				return false;
			}
			return true;
		} catch (NoResultException no) {
			return Boolean.TRUE;
		}
		
	}

	@Override
	public String persist() {
		String persist = null;
		StringBuilder msg = new StringBuilder();
		if (isDataValida()) {
			if (tipo == null || tipo.equals("M")) {
				if (OrgaoJulgadorHome.instance().getInstance().getOrgaoJulgador() == null && verificarOJ()) {
					persist = super.persist();
					msg.append( "CompetenciaOrgaoJulgador_created");
				} else {
					if (OrgaoJulgadorHome.instance().getInstance().getOrgaoJulgador() != null) {

						if (ParametroUtil.instance().isAssociaCompetenciaSalaAudiencia()) {
							for (Sala sala : getInstance().getOrgaoJulgador().getSalaList()) {
								if (!sala.getCompetenciaList().contains(getInstance().getCompetencia())) {
									sala.getCompetenciaList().add(getInstance().getCompetencia());
								}
							}
						}

						persist = super.persist();
						msg.append( "CompetenciaOrgaoJulgador_created");
						
					} else {
						msg.append("CompetenciaOrgaoJulgador_ja_inserido");
					}
				}
			} else {
				OrgaoJulgadorColegiadoCompetencia colegiadoCompetencia = new OrgaoJulgadorColegiadoCompetencia();
				colegiadoCompetencia.setCompetencia(CompetenciaHome.instance().getInstance());
				colegiadoCompetencia.setDataFim(instance.getDataFim());
				colegiadoCompetencia.setDataInicio(instance.getDataInicio());
				colegiadoCompetencia.setOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
				try {
					getEntityManager().persist(colegiadoCompetencia);
					getEntityManager().flush();
					msg.append( "CompetenciaOrgaoJulgador_created");
				} catch (Exception e) {
					
					msg.append("CompetenciaOrgaoJulgador_ja_inserido");
				}
				newInstance();
				persist = "persisted";
				refreshGrid("orgaoJulgadorColegiadoCompetenciaGrid");
			}
		} else {
			msg.append("CompetenciaOrgaoJulgador_data_invalida");
		}
		FacesMessages.instance().clear();
		FacesMessages.instance().add(FacesUtil.getMessage(msg.toString()));
		refreshGrid("orgaoJulgadorCompetenciaGrid");
		return persist;
	}

	@Override
	public String update() {
		String ret = null;
		if (isDataValida()) {
			ret = super.update();
			ajustarDataFimTipoAudiencia(getInstance());
		} else {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(FacesUtil.getMessage("CompetenciaOrgaoJulgador_data_invalida"));
			getEntityManager().refresh(instance);
		}
		refreshGrid("orgaoJulgadorCompetenciaGrid");
		return ret;
	}

	@Override
	public void newInstance() {
		/* tipo = null; */
		orgaoJulgadorColegiado = null;
		
		CompetenciaTreeHandler competenciaTreeHandler = ComponentUtil.getComponent("competenciaTree");
		if(competenciaTreeHandler != null) {
			competenciaTreeHandler.clearTree();
		}
		
		super.newInstance();
	}

	public void setIdOrgao(Object id) {
		OrgaoJulgadorColegiadoCompetenciaHome.instance().newInstance();
		this.setId(id);
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		if (changed) {
			tipo = "M";
		}
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgador> getOrgaoJulgadorItems() {
		StringBuffer ejbql = new StringBuffer();
		ejbql.append("select o from OrgaoJulgador o ");
		ejbql.append("where o.ativo = true ");
		ejbql.append("order by o.orgaoJulgadorOrdemAlfabetica");
		return EntityUtil.createQuery(ejbql.toString()).getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiado> getOrgaoJulgadorColegiadoItems() {
		StringBuffer ejbql = new StringBuffer();
		ejbql.append("select o from OrgaoJulgadorColegiado o ");
		ejbql.append("order by o.orgaoJulgadorColegiado");
		return EntityUtil.createQuery(ejbql.toString()).getResultList();
	}

	@SuppressWarnings("unchecked")
	public Boolean validarOjc() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorCompetencia o ");
		sb.append("where o.orgaoJulgador.orgaoJulgador = :oj ");
		sb.append("and o.competencia.competencia = :competencia ");
		if (getInstance().getIdOrgaoJulgadorCompetencia() != 0) {
			sb.append("and o.idOrgaoJulgadorCompetencia != :id");
		}

		String sql = sb.toString();

		Query q = getEntityManager().createQuery(sql);
		q.setParameter("competencia", getInstance().getCompetencia().getCompetencia());
		q.setParameter("oj", OrgaoJulgadorHome.instance().getInstance().getOrgaoJulgador());
		if (getInstance().getIdOrgaoJulgadorCompetencia() != 0) {
			q.setParameter("id", getInstance().getIdOrgaoJulgadorCompetencia());
		}

		List<OrgaoJulgadorCompetencia> list = q.getResultList();

		if (list.size() > 0) {
			Boolean dataFimNull = Boolean.FALSE;
			for (OrgaoJulgadorCompetencia orgaoJulgadorCompetencia : list) {
				if (orgaoJulgadorCompetencia.getDataFim() == null) {
					dataFimNull = Boolean.TRUE;
				}
			}

			if (dataFimNull) {
				FacesMessages.instance().add(Severity.ERROR,
						"Não é possível inserir. Existe uma competência igual sem uma Data Final definida.");
				return Boolean.FALSE;
			} else {
				Boolean dataOk = Boolean.TRUE;
				for (OrgaoJulgadorCompetencia ojc : list) {
					Date dataInicio = getInstance().getDataInicio();
					if ((dataInicio.after(ojc.getDataInicio()) && dataInicio.before(ojc.getDataFim()))
							|| dataInicio.equals(ojc.getDataInicio()) || dataInicio.equals(ojc.getDataFim())) {
						dataOk = Boolean.FALSE;
					}
				}
				if (!dataOk) {
					FacesMessages.instance().add(Severity.ERROR,
							"A data inicial desta Competência está em conflito com outras.");
					return Boolean.FALSE;
				}
			}
		} else {
			if (getInstance().getDataFim() != null) {
				if (getInstance().getDataFim().before(getInstance().getDataInicio())) {

					FacesMessages.instance().add(Severity.ERROR, "Data final não pode ser menor que a data inicial");
					return Boolean.FALSE;

				}
			}
		}

		return Boolean.TRUE;
	}

	public void persistOJC() {
		if (validarOjc()) {
			this.persist();
		}
	}

	public void updateOJC() {
		if (validarOjc()) {
			this.update();
		} else {
			getEntityManager().refresh(instance);
			refreshGrid("orgaoJulgadorCompetenciaGrid2");
			newInstance();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void ajustarDataFimTipoAudiencia(OrgaoJulgadorCompetencia ojc){
		StringBuilder sb = new StringBuilder();
		sb.append("select o2 from OjClasseTipoAudiencia o2 ");
		sb.append("where o2.classeJudicial in (select o3.classeAplicacao.classeJudicial from CompetenciaClasseAssunto o3 where o3.competencia=:competencia) and ");
		sb.append("o2.orgaoJulgador=:orgaoJulgador and ");
		sb.append("(o2.dtFim = null or o2.dtFim >= now()) ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("competencia", ojc.getCompetencia());
		q.setParameter("orgaoJulgador", ojc.getOrgaoJulgador());
		List<OjClasseTipoAudiencia> list = q.getResultList();
		
		for (OjClasseTipoAudiencia ojClasseTipoAudiencia : list) {
			sb = new StringBuilder();
			sb.append("select o from OrgaoJulgadorCompetencia o ");
			sb.append("where o.orgaoJulgador=:orgaoJulgador and ");
			sb.append("o.competencia in (select o1 from CompetenciaClasseAssunto o1 where o1.classeAplicacao.classeJudicial=:classeJudicial) ");
			q = getEntityManager().createQuery(sb.toString());
			q.setParameter("orgaoJulgador", ojc.getOrgaoJulgador());
			q.setParameter("classeJudicial", ojClasseTipoAudiencia.getClasseJudicial());
			List<OrgaoJulgadorCompetencia> list2 = q.getResultList();
			Date maiorDataOrgaoJulgadorCompetencia = null;
			for (OrgaoJulgadorCompetencia orgaoJulgadorCompetencia : list2) {
				if(maiorDataOrgaoJulgadorCompetencia == null || (orgaoJulgadorCompetencia.getDataFim() != null && orgaoJulgadorCompetencia.getDataFim().after(maiorDataOrgaoJulgadorCompetencia))){
					maiorDataOrgaoJulgadorCompetencia = orgaoJulgadorCompetencia.getDataFim();
				}
			}
			
			sb = new StringBuilder();
			sb.append("select o from CompetenciaClasseAssunto o ");
			sb.append("where o.classeAplicacao.classeJudicial=:classeJudicial and ");
			sb.append("o.competencia in (select o1 from OrgaoJulgadorCompetencia o1 where o1.orgaoJulgador=:orgaoJulgador) ");
			q = getEntityManager().createQuery(sb.toString());
			q.setParameter("orgaoJulgador", ojc.getOrgaoJulgador());
			q.setParameter("classeJudicial", ojClasseTipoAudiencia.getClasseJudicial());
			List<CompetenciaClasseAssunto> l = q.getResultList();
			Date maiorDataCompetenciaClasseAssunto = null;
			for (CompetenciaClasseAssunto competenciaClasseAssunto : l) {
				if(maiorDataCompetenciaClasseAssunto == null || (competenciaClasseAssunto.getDataFim() != null && competenciaClasseAssunto.getDataFim().after(maiorDataCompetenciaClasseAssunto))){
					maiorDataCompetenciaClasseAssunto = competenciaClasseAssunto.getDataFim();
				}
			}
			
			Date dataFim = null;
			if(maiorDataOrgaoJulgadorCompetencia != null && maiorDataCompetenciaClasseAssunto == null || maiorDataOrgaoJulgadorCompetencia == null && maiorDataCompetenciaClasseAssunto != null){
				dataFim = maiorDataOrgaoJulgadorCompetencia != null ? maiorDataOrgaoJulgadorCompetencia : maiorDataCompetenciaClasseAssunto; 
			}else if(maiorDataOrgaoJulgadorCompetencia != null && maiorDataCompetenciaClasseAssunto != null){
				dataFim = maiorDataOrgaoJulgadorCompetencia.before(maiorDataCompetenciaClasseAssunto) ? maiorDataOrgaoJulgadorCompetencia : maiorDataCompetenciaClasseAssunto;
			}else{
				dataFim = null;
			}
			
			ojClasseTipoAudiencia.setDtFim(dataFim);
			getEntityManager().merge(ojClasseTipoAudiencia);
		}
		getEntityManager().flush();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		refreshGrid("orgaoJulgadorCompetenciaGrid2");
		return super.afterPersistOrUpdate(ret);
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	@Override
	public String remove(OrgaoJulgadorCompetencia obj) {
		obj.setDataFim(new Date());
		String update = update();
		refreshGrid("orgaoJulgadorCompetenciaGrid2");
		
		return update;
	}
	
	public Boolean menorQueDataAtual(Date data){
		if(data != null){
			return data.before(new Date());
		}else{
			return true;
		}
	}
	
	
}