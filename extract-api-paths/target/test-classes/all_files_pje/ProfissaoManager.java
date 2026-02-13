package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProfissaoDAO;
import br.jus.pje.nucleo.entidades.Profissao;

@Name(ProfissaoManager.NAME)
public class ProfissaoManager extends BaseManager<Profissao>{
	
	public static final String NAME = "profissaoManager";
	
	@In
	private ProfissaoDAO profissaoDAO;

	@Override
	protected BaseDAO<Profissao> getDAO() {
		return this.profissaoDAO;
	}
	
	public List<Profissao> recuperarProfissoesPorParteNomeProfissao(String profissao){
		return this.profissaoDAO.suggestList(profissao);
	}

}
