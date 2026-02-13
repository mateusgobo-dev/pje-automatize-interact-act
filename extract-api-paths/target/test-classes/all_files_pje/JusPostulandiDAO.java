package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.PessoaFisica;

@Name(JusPostulandiDAO.NAME)
public class JusPostulandiDAO extends AbstractUsuarioDAO<PessoaFisica>{
	
	public static final String NAME = "jusPostulandiDAO";

	@Override
	public Object getId(PessoaFisica e) {
		return e.getIdPessoa();
	}

}
