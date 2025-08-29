package br.jus.cnj.pje.business.dao;


import java.util.List;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.SessaoEnteExterno;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(SessaoEnteExternoDAO.NAME)
public class SessaoEnteExternoDAO extends BaseDAO<SessaoEnteExterno>{

	public static final String NAME = "sessaoEnteExternoDAO";

	@Override
	public Object getId(SessaoEnteExterno e) {
		return e.getIdSessaoEnteExterno();
	}

	/**
	 * metodo responsavel por recuperar todos os @SessaoEnteExterno com a pessoa passada em parametro.
	 * @param _pessoa
	 * @return
	 */
	public List<SessaoEnteExterno> recuperaSessaoEnteExterno(Pessoa _pessoa) {
		List<SessaoEnteExterno> resultado = null;
		Search search = new Search(SessaoEnteExterno.class);
		try {
			search.addCriteria(Criteria.equals("pessoaAcompanhaSessao.idUsuario", _pessoa.getIdPessoa()));
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		resultado = list(search);
		return resultado;
	}
}