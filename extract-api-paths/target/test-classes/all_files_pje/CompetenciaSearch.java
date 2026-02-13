package br.com.infox.cliente.entity.search;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.AplicacaoClasse;
import br.jus.pje.nucleo.entidades.Competencia;

@Name("competenciaSearch")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class CompetenciaSearch extends Competencia {

	private static final long serialVersionUID = 1L;

	private String classeJudicial;
	private AplicacaoClasse aplicacaoClasse;
	private String assuntoTrf;
	private Boolean competenciaEspecializada;

	public String getClasseJudicial() {
		return classeJudicial;
	}

	public void setClasseJudicial(String classeJudicial) {
		this.classeJudicial = classeJudicial;
	}

	public AplicacaoClasse getAplicacaoClasse() {
		return aplicacaoClasse;
	}

	public void setAplicacaoClasse(AplicacaoClasse aplicacaoClasse) {
		this.aplicacaoClasse = aplicacaoClasse;
	}

	public String getAssuntoTrf() {
		return assuntoTrf;
	}

	public void setAssuntoTrf(String assuntoTrf) {
		this.assuntoTrf = assuntoTrf;
	}

	public void setCompetenciaEspecializada(Boolean competenciaEspecializada) {
		this.competenciaEspecializada = competenciaEspecializada;
	}

	public Boolean getCompetenciaEspecializada() {
		return competenciaEspecializada;
	}

}