package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoriaLocal;

@Name(PessoaAssistenteProcuradoriaLocalDAO.NAME)
public class PessoaAssistenteProcuradoriaLocalDAO extends BaseDAO<PessoaAssistenteProcuradoriaLocal>{

	public static final String NAME = "pessoaAssistenteProcuradoriaLocalDAO";
	
	@Override
	public Object getId(PessoaAssistenteProcuradoriaLocal e) {
		return e.getIdUsuarioLocalizacao();
	}

}
