package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ClasseAplicacaoDAO;
import br.jus.pje.nucleo.entidades.ClasseAplicacao;

@Name(ClasseAplicacaoManager.NAME)
public class ClasseAplicacaoManager extends BaseManager<ClasseAplicacao> {
	
	public static final String NAME = "classeAplicacaoManager";	
	@In
	private ClasseAplicacaoDAO classeAplicacaoDAO;

	@Override
	protected BaseDAO<ClasseAplicacao> getDAO() {
		return classeAplicacaoDAO;
	}

}
