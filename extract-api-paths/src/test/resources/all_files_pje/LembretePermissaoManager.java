package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.LembretePermissaoDAO;
import br.jus.pje.nucleo.entidades.LembretePermissao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("lembretePermissaoManager")
public class LembretePermissaoManager extends BaseManager<LembretePermissao> {
	
	@In
	LembretePermissaoDAO lembretePermissaoDAO;
	
	@Override
	protected BaseDAO<LembretePermissao> getDAO() {
		return lembretePermissaoDAO;
	}
	
	/**
	 * Recupera lista de permissões por lembrete.
	 * 
	 * @param idLembrete
	 * @return List<LembretePermissao> 
	 */
	public List<LembretePermissao> recuperaListaDePermissoesPorIdLembrete(Integer idLembrete) {
		Search search = new Search(LembretePermissao.class);
		addCriteria(search, Criteria.equals("lembrete.idLembrete", idLembrete));
		return list(search);
	}
	
	/**
	 * Metodo que remove todas as permissões do lembrete.
	 * 
	 * @param idLembrete
	 */
	public void removePermissoesPorIdLembrete(Integer idLembrete){
		lembretePermissaoDAO.removePermissoesPorIdLembrete(idLembrete);
	}

	/**
	 * metodo responsavel por recuperar todas as permissoes lembretes da pessoa passada em parametro.
	 * @param pessoaSecundaria
	 * @return
	 * @throws Exception 
	 */
	public List<LembretePermissao> recuperarLembretesPermissao(Pessoa _pessoa) throws Exception {
		return lembretePermissaoDAO.recuperarLembretesPermissao(_pessoa);
	}

	/**
	 * metodo responsavel por recuperar LembretePermissao pelo id do objeto
	 * @param idLembrete
	 * @return
	 */
	public LembretePermissao recuperaLembretePermissao(Integer idLembrete) {
		return lembretePermissaoDAO.find(idLembrete);
	}

}
