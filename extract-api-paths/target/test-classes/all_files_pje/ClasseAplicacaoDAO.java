package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.ClasseAplicacao;

@Name(ClasseAplicacaoDAO.NAME)
public class ClasseAplicacaoDAO extends BaseDAO<ClasseAplicacao>{
	
	public static final String NAME = "classeAplicacaoDAO";

	@Override
	public Object getId(ClasseAplicacao e) {
		return e.getIdClasseAplicacao();
	}

}
