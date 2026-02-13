package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.RelacaoPessoalDAO;
import br.jus.pje.nucleo.entidades.RelacaoPessoal;

@Name(RelacaoPessoalManager.NAME)
public class RelacaoPessoalManager extends BaseManager<RelacaoPessoal>{
	
	public static final String NAME ="relacaoPessoalManager";
	
	@In
	private RelacaoPessoalDAO relacaoPessoalDAO;


	@Override
	protected RelacaoPessoalDAO getDAO() {
		return relacaoPessoalDAO;
	}
	
	

}
