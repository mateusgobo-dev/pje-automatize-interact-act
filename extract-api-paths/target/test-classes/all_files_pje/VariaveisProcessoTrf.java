package br.com.infox.cliente.component;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;

@Name("variaveisProcessoTrf")
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
public class VariaveisProcessoTrf {

	@SuppressWarnings("unchecked")
	public List<String> getNomesPartesProcesso(ProcessoParteParticipacaoEnum participacaoEnum) {
		ProcessoTrf processo = getProcesso();
		if (processo == null) {
			return Collections.emptyList();
		}
		String hql = "select o.pessoa.nome from ProcessoParte o "
				+ "where o.inParticipacao = :participacaoEnum and o.processoTrf = :processoTrf";
		Query query = EntityUtil.createQuery(hql);
		query.setParameter("processoTrf", processo);
		query.setParameter("participacaoEnum", participacaoEnum);
		return query.getResultList();
	}

	@Factory(scope = ScopeType.CONVERSATION)
	public List<String> getNomesPartesPoloAtivoProcesso() {
		return getNomesPartesProcesso(ProcessoParteParticipacaoEnum.A);
	}

	@Factory(scope = ScopeType.CONVERSATION)
	public List<String> getNomesPartesPoloPassivoProcesso() {
		return getNomesPartesProcesso(ProcessoParteParticipacaoEnum.P);
	}

	@Factory(scope = ScopeType.CONVERSATION)
	public String getAutor() {
		return getNomeParte(getNomesPartesPoloAtivoProcesso());
	}
	
	@Factory(scope = ScopeType.CONVERSATION)
	public String getReu() {
		return getNomeParte(getNomesPartesPoloPassivoProcesso());
	}	
	
	private String getNomeParte(List<String> nomes) {
		if (nomes.size() == 0) {
			return null;
		} else if (nomes.size() == 1) {
			return nomes.get(0);
		} else {
			return nomes.get(0) + " e outros";
		}		
	}
	
	@SuppressWarnings("unchecked")
	@Factory(scope = ScopeType.CONVERSATION)
	public List<String> getNomesAssuntosProcesso() {
		ProcessoTrf processo = getProcesso();
		if (processo == null) {
			return Collections.emptyList();
		}
		String hql = "select o.assuntoTrf.assuntoTrf from ProcessoAssunto o " + "where o.processoTrf = :processoTrf";
		Query query = EntityUtil.createQuery(hql);
		query.setParameter("processoTrf", getProcesso());
		return query.getResultList();
	}

	@Factory(scope = ScopeType.CONVERSATION)
	public String getNumeroProcesso() {
		ProcessoTrf processo = getProcesso();
		return processo != null ? processo.getNumeroProcesso() : null;
	}

	@Factory(scope = ScopeType.CONVERSATION)
	public String getNomeClasseJudicialProcesso() {
		ClasseJudicial classeJudicial = getClasseJudicial();
		return classeJudicial != null ? classeJudicial.getClasseJudicial() : null;
	}

	private ClasseJudicial getClasseJudicial() {
		ProcessoTrf processo = getProcesso();
		return processo != null ? processo.getClasseJudicial() : null;
	}

	@Factory(scope = ScopeType.CONVERSATION)
	public String getPoloAtivoClasseJudicialProcesso() {
		String tipoParte = "";
		if (getProcesso() != null) {
			if (CollectionUtilsPje.isNotEmpty(getProcesso().getListaPartePrincipalAtivo())
					&& getProcesso().getListaPartePrincipalAtivo().get(0) != null) {
				tipoParte = getProcesso().getListaPartePrincipalAtivo().get(0).getTipoParte().getTipoParte();				
			}
		}
		return tipoParte;
		
	}

	@Factory(scope = ScopeType.CONVERSATION)
	public String getPoloPassivoClasseJudicialProcesso() {
		String tipoParte = "";
		if (getProcesso() != null) {
			if (CollectionUtilsPje.isNotEmpty(getProcesso().getListaPartePrincipalPassivo())
					&& getProcesso().getListaPartePrincipalPassivo().get(0) != null) {
				tipoParte = getProcesso().getListaPartePrincipalAtivo().get(0).getTipoParte().getTipoParte();				
			}
		}
		return tipoParte;
	}

	private ProcessoTrf getProcesso() {
		ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();
		return processoTrfHome.isManaged() ? processoTrfHome.getInstance() : null;
	}

}