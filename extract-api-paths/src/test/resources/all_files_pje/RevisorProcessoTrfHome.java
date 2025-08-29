package br.com.infox.cliente.home;

import java.util.Date;

import javax.persistence.Query;

import org.apache.commons.lang.BooleanUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.RevisorProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoComposicaoOrdem;

@Name(RevisorProcessoTrfHome.NAME)
@BypassInterceptors
public class RevisorProcessoTrfHome extends AbstractHome<RevisorProcessoTrf> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "revisorProcessoTrfHome";
	private ProcessoTrf processoTrf;
	private OrgaoJulgador orgaoJulgadorRevisor;

	public static RevisorProcessoTrfHome instance() {
		return (RevisorProcessoTrfHome) Component.getInstance(NAME);
	}

	public void gravarOrgaoRevisor(ProcessoTrf processo) {
		gravarOrgaoRevisor(processo, null);
	}
	
	public void gravarOrgaoRevisor(ProcessoTrf processo, Sessao sessaoSugerida) {
		
		RevisorProcessoTrf rev = getRevisorProcessoTrf(processo);
		
		if (rev != null && rev.getIdRevisorProcessoTrf() != 0 && orgaoJulgadorRevisor != null) {
			setInstance(rev);
			getInstance().setDataFinal(new Date());
			super.update();
			
			newInstance();
			getInstance().setDataInicio(new Date());
			getInstance().setProcessoTrf(processo);
			getInstance().setOrgaoJulgadorRevisor(orgaoJulgadorRevisor);
			getInstance().setSessaoSugerida(sessaoSugerida);
			processo.setOrgaoJulgadorRevisor(orgaoJulgadorRevisor);
			super.persist();
			
		} else if (rev != null && orgaoJulgadorRevisor == null) {
			setInstance(rev);
			getInstance().setDataFinal(new Date());
			processo.setOrgaoJulgadorRevisor(null);
			super.update();
			
		} else {
			newInstance();
			getInstance().setDataInicio(new Date());
			getInstance().setProcessoTrf(processo);
			getInstance().setOrgaoJulgadorRevisor(orgaoJulgadorRevisor);
			getInstance().setSessaoSugerida(sessaoSugerida);
			processo.setOrgaoJulgadorRevisor(orgaoJulgadorRevisor);
			super.persist();
		}
		
	}

	/**
	 * Pega o revisor pelo do processo
	 * 
	 * @param processo
	 * @return
	 */
	public RevisorProcessoTrf getRevisorProcessoTrf(ProcessoTrf processo) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from RevisorProcessoTrf o where ");
		sb.append("o.processoTrf.idProcessoTrf = :id and ");
		sb.append("o.dataFinal = null ");
		Query q = getEntityManager().createQuery(sb.toString()).setParameter("id", processo.getIdProcessoTrf());

		return EntityUtil.getSingleResult(q);
	}

	/**
	 * Retorna a sessão composição ordem para o orgão juylgador revisor da
	 * sessão retorna null caso não encontre
	 * 
	 * @return
	 */
	public OrgaoJulgador getRevisorSessao(ProcessoTrf processo) {
		Criteria criteria = HibernateUtil.getSession().createCriteria(SessaoComposicaoOrdem.class);
		criteria.add(Restrictions.eq("sessao", processo.getSessaoSugerida()));
		criteria.add(Restrictions.eq("orgaoJulgador", processo.getOrgaoJulgador()));
		criteria.setFirstResult(0);
		criteria.setMaxResults(1);
		SessaoComposicaoOrdem scoTemp = (SessaoComposicaoOrdem)criteria.uniqueResult();
		if (scoTemp != null && scoTemp.getOrgaoJulgadorRevisor() != null) {
			return scoTemp.getOrgaoJulgadorRevisor();
		} else {
			return null;
		}
	}

	/**
	 * Define o Oj revisor no detalhes do processo a depender da sessão
	 * selecionada para o processo tenta primeiro encontrar o revisor do
	 * processo, caso não encontre seta o revisor como sendo o revisor da
	 * sessão, caso ainda não encontre seta como null para na combo de revisores
	 * exibir "Selecione"
	 * 
	 * @param processo
	 */
	public void defineOrgaoJulgadorRevisor(ProcessoTrf processo) {
		Contexts.removeFromAllContexts("comboRevisor");
		Contexts.removeFromAllContexts("divComboRevisor");
		if (!BooleanUtils.isTrue(ProcessoTrfHome.instance().getInstance().getExigeRevisor())
				&& ProcessoTrfHome.instance().getInstance().getSessaoSugerida() != null) {
			ProcessoTrfHome.instance().setExibeBotaoGravar(true);
		}
		RevisorProcessoTrf rev = getRevisorProcessoTrf(processo);
		if (rev != null && rev.getIdRevisorProcessoTrf() != 0) {
			this.setOrgaoJulgadorRevisor(rev.getOrgaoJulgadorRevisor());
		} else if (processo.getOrgaoJulgadorRevisor() != null) {
			this.setOrgaoJulgadorRevisor(processo.getOrgaoJulgadorRevisor());
		} else {
			this.setOrgaoJulgadorRevisor(getRevisorSessao(processo));
		}
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public OrgaoJulgador getOrgaoJulgadorRevisor() {
		return orgaoJulgadorRevisor;
	}

	public void setOrgaoJulgadorRevisor(OrgaoJulgador orgaoJulgadorRevisor) {
		this.orgaoJulgadorRevisor = orgaoJulgadorRevisor;
	}
}