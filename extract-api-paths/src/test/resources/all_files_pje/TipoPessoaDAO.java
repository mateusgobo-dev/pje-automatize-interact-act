package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.TipoPessoa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(TipoPessoaDAO.NAME)
public class TipoPessoaDAO extends BaseDAO<TipoPessoa> {
	public static final String NAME = "tipoPessoaDAO";

	@Override
	public Object getId(TipoPessoa e) {
		return e.getIdTipoPessoa();
	}
	
	public TipoPessoa findByCdTipoPessoa(String cdTipoPessoa){
		TipoPessoa tipoPessoa = null;
		try {
			Search search = new Search(TipoPessoa.class);
			search.addCriteria(Criteria.equals("codTipoPessoa", cdTipoPessoa));
			tipoPessoa = (TipoPessoa) list(search).get(0);
		} catch (NoSuchFieldException e) {
			throw PJeDAOExceptionFactory.getDaoException(e);
		}
		return tipoPessoa;
		
	}
	
	public TipoPessoa recuperaTipoPessoa(int idTipoPessoa){
 		TipoPessoa tipoPessoa = null;
 		try {
 			Search search = new Search(TipoPessoa.class);
 			search.addCriteria(Criteria.equals("idTipoPessoa", idTipoPessoa));
 			tipoPessoa = (TipoPessoa) list(search).get(0);
 		} catch (NoSuchFieldException e) {
 			throw PJeDAOExceptionFactory.getDaoException(e);
 		}
 		return tipoPessoa;
 	}
}