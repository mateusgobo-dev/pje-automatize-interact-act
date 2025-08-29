package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.SessaoEnteExternoDAO;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.SessaoEnteExterno;

@Name(SessaoEnteExternoManager.NAME)
public class SessaoEnteExternoManager extends BaseManager<SessaoEnteExterno>{

	public static final String NAME = "sessaoEnteExternoManager";
	
	@In
	private SessaoEnteExternoDAO sessaoEnteExternoDAO;

	@Override
	protected BaseDAO<SessaoEnteExterno> getDAO() {
		return sessaoEnteExternoDAO;
	}

	/**
	 * metodo responsavel por recuperar todos os @SessaoEnteExterno com a pessoa passada em parametro.
	 * @param pessoaSecundaria
	 * @return
	 */
	public List<SessaoEnteExterno> recuperaSessaoEnteExterno(Pessoa _pessoa) {
		return sessaoEnteExternoDAO.recuperaSessaoEnteExterno(_pessoa);
	}

}