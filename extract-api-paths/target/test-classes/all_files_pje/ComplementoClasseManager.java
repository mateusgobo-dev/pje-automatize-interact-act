package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import br.jus.cnj.pje.business.dao.ComplementoClasseDAO;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.ComplementoClasse;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("complementoClasseManager")
public class ComplementoClasseManager extends BaseManager<ComplementoClasse>{
	
	@In
	private ComplementoClasseDAO complementoClasseDAO;

	@Override
	protected ComplementoClasseDAO getDAO() {
		return this.complementoClasseDAO;
	}

	public List<ComplementoClasse> getListComplementoClasse(ProcessoTrf processoTrf) {
		return this.getDAO(). getListComplementoClasse(processoTrf.getClasseJudicial());
	}
	
	public List<ComplementoClasse> getListComplementoClasse(ClasseJudicial classeJudicial) {
		return this.getDAO().getListComplementoClasse(classeJudicial);
	}

}
