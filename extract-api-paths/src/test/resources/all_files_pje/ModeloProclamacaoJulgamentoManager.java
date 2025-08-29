package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.ModeloProclamacaoJulgamentoDAO;
import br.jus.pje.nucleo.entidades.ModeloProclamacaoJulgamento;
import br.jus.pje.nucleo.entidades.Pessoa;

@Name(ModeloProclamacaoJulgamentoManager.NAME)
public class ModeloProclamacaoJulgamentoManager extends BaseManager<ModeloProclamacaoJulgamento>{
	
	public static final String NAME = "modeloProclamacaoJulgamentoManager";
	
	@In
	private ModeloProclamacaoJulgamentoDAO modeloProclamacaoJulgamentoDAO;

	@Override
	protected ModeloProclamacaoJulgamentoDAO getDAO() {
		return modeloProclamacaoJulgamentoDAO;
	}

	public List<ModeloProclamacaoJulgamento> recuperarModelos(Pessoa _pessoa) throws Exception {
		return modeloProclamacaoJulgamentoDAO.recuperarModelos(_pessoa);
	}

	public ModeloProclamacaoJulgamento recuperarPorId(Integer id) {
		return modeloProclamacaoJulgamentoDAO.find(id);
	}
}