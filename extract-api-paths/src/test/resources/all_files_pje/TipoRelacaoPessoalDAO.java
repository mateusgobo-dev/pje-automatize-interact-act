package br.jus.cnj.pje.business.dao;

import java.util.List;

import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.pje.nucleo.entidades.TipoRelacaoPessoal;
import br.jus.pje.nucleo.enums.TipoPessoaRelacaoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name(TipoRelacaoPessoalDAO.NAME)
public class TipoRelacaoPessoalDAO extends BaseDAO<TipoRelacaoPessoal> {
	public static final String NAME = "tipoRelacaoPessoalDAO";

	@Override
	public Object getId(TipoRelacaoPessoal e) {
		return e.getCodigo();
	}
	
	public List<TipoRelacaoPessoal> findByCodigoAndTipoPessoaRelacaoEnum(String codigo, TipoPessoaRelacaoEnum[] tiposPessoaRelacaoEnum) throws PJeDAOException{
		try {
			Search search = new Search(TipoRelacaoPessoal.class);
			search.addCriteria(Criteria.equals("codigo", codigo));
			search.addCriteria(Criteria.in("tipoPessoaRelacao", tiposPessoaRelacaoEnum));
			search.addCriteria(Criteria.equals("ativo", Boolean.TRUE));
			return list(search);
		} catch (NoSuchFieldException e) {
			throw new PJeDAOException(e);
		}
	}

}
