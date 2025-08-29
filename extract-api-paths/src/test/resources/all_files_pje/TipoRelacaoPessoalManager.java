package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.TipoRelacaoPessoalDAO;
import br.jus.pje.nucleo.entidades.TipoRelacaoPessoal;
import br.jus.pje.nucleo.enums.TipoPessoaRelacaoEnum;

@Name(TipoRelacaoPessoalManager.NAME)
public class TipoRelacaoPessoalManager extends BaseManager<TipoRelacaoPessoal> {
	
	public static final String NAME = "tipoRelacaoPessoalManager";
	
	@In
	private TipoRelacaoPessoalDAO tipoRelacaoPessoalDAO;

	@Override
	protected BaseDAO<TipoRelacaoPessoal> getDAO() {
		return tipoRelacaoPessoalDAO;
	}
	
	public List<TipoRelacaoPessoal> findByCodigoAndTipoPessoaRelacaoEnum(String codigo, TipoPessoaRelacaoEnum[] tiposPessoaRelacaoEnum){
		return tipoRelacaoPessoalDAO.findByCodigoAndTipoPessoaRelacaoEnum(codigo, tiposPessoaRelacaoEnum);
	}

}
