package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.pje.nucleo.entidades.ProcessoHistoricoClasse;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.SimNaoFacultativoEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name("processoHistoricoClasseHome")
@BypassInterceptors
public class ProcessoHistoricoClasseHome extends AbstractHome<ProcessoHistoricoClasse> implements Serializable {

	public static final String PESSOA_LOGADA_VAR = "pessoaLogada";
	public static Logger logger = Logger.getLogger(PessoaHome.class.getCanonicalName());

	private static final long serialVersionUID = 1L;
	
	public ProcessoHistoricoClasseHome() {

	}

	public static ProcessoHistoricoClasseHome instance() {
		return ComponentUtil.getComponent(ProcessoHistoricoClasseHome.class);
	}
	
	@Override
	protected boolean beforePersistOrUpdate() {
		ProcessoTrf processo = ProcessoTrfHome.instance().getInstance();
		ProcessoHistoricoClasse phu = retornaUltimoHistorico(processo);
		if (isAnulacao() && phu != null) {
			getInstance().setClasseJudicialAtual(phu.getClasseJudicialAnterior());
			getInstance().setClasseJudicialAnterior(phu.getClasseJudicialAtual());
		} else {
			getInstance().setClasseJudicialAnterior(processo.getClasseJudicial());
		}
		getInstance().setProcessoTrf(processo);
		getInstance().setDataInicio(verificaDataInicio(processo, phu));
		getInstance().setDataFim(new Date());
		return super.beforePersistOrUpdate();
	}

	private Date verificaDataInicio(ProcessoTrf processo, ProcessoHistoricoClasse phu) {
		if (phu == null) {
			return processo.getDataDistribuicao();
		} else {
			return phu.getDataFim();
		}
	}

	private ProcessoHistoricoClasse retornaUltimoHistorico(ProcessoTrf processo) {
		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sql = new StringBuilder();
		sql.append("select o from ProcessoHistoricoClasse o ").append("where o.processoTrf = :processoTrf ")
				.append("order by o.dataFim desc");

		Query query = em.createQuery(sql.toString());
		query.setParameter("processoTrf", processo);
		query.setMaxResults(1);
		try {
			return (ProcessoHistoricoClasse) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public String persist() {
		return super.persist();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		EntityManager em = getEntityManager();
		processoTrf.setClasseJudicial(getInstance().getClasseJudicialAtual());
		processoTrf.setExigeRevisor(SimNaoFacultativoEnum.S.equals(getInstance().getClasseJudicialAtual().getExigeRevisor()));
		List<ProcessoParte> partesList = processoTrf.getProcessoParteList();
		ProcessoParteManager processoParteManager = ComponentUtil.getComponent(ProcessoParteManager.class);
		for (ProcessoParte pp : partesList) {
			TipoParte novoTipoParte = processoParteManager.retornarNovoTipoParte(pp, processoTrf, false);
			if(novoTipoParte != null){
				pp.setTipoParte(novoTipoParte);
				em.merge(pp);
			}
		}
		em.flush();
		
		if (getInstance().getInversaoPolos()){
			processoParteManager.inverterPolo(processoTrf);
		}
		
		ProcessoTrfHome.instance().getInstance().setClasseJudicial(getInstance().getClasseJudicialAtual());
		em.persist(ProcessoTrfHome.instance().getInstance());
		
		ProcessoTrfHome.instance().setClasseJudicialFiltro(null);
		
		String transicaoSaida = (String)TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		if (StringUtils.isNotBlank(transicaoSaida)) {
			TaskInstanceHome.instance().end(transicaoSaida);
		}
		
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Operação realizada com sucesso");
		
		return super.afterPersistOrUpdate(ret);
	}
	
	public List<String> getEvolucaoClassesDisponiveis() {
		String classesDisponiveis = (String) ComponentUtil.getTramitacaoProcessualService()
				.recuperaVariavelTarefa(Variaveis.VARIAVEL_FLUXO_EVOLUCAO_CLASSE_CLASSES_DISPONIVEIS);
		if (StringUtil.isEmpty(classesDisponiveis)) {
			return null;
		} else {
			return Arrays.asList(classesDisponiveis.split(","));
		}
	}
	
	public Boolean isAnulacao() {
		String anulacao = (String) ComponentUtil.getTramitacaoProcessualService()
				.recuperaVariavelTarefa(Variaveis.VARIAVEL_FLUXO_EVOLUCAO_CLASSE_CLASSES_ANULACAO);
		return Boolean.valueOf(anulacao);
	}

}
