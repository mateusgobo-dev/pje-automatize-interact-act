package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.CompetenciaManager;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;


@Name(CompetenciaAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class CompetenciaAction implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "competenciaAction";
		
	public void toggleHabilitacaoClasseAtendimentoPlantao(Competencia competencia,ClasseJudicial classeJudicial) {
		CompetenciaManager competenciaManager = ComponentUtil.getComponent(CompetenciaManager.class);
		competenciaManager.toggleHabilitacaoClasseAtendimentoPlantao(competencia,classeJudicial);
	}	
	
	public boolean isClasseAtendimentoPlantao(Competencia competencia,ClasseJudicial classeJudicial) {
		CompetenciaManager competenciaManager = ComponentUtil.getComponent(CompetenciaManager.class);
		return competenciaManager.isClasseAtendimentoPlantao(competencia,classeJudicial);
	}
	
	public List<ClasseJudicial> getClasseJudicialByCompetencia(Competencia competencias) {
		List<Competencia> competenciasList = new ArrayList<Competencia>(0);
		competenciasList.add(competencias);
		ClasseJudicialManager classeJudicalManager = ComponentUtil.getComponent(ClasseJudicialManager.class);
		return classeJudicalManager.getByCompetencia(competenciasList);
	}
	

}
