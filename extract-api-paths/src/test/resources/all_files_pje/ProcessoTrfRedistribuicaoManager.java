package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.business.dao.ProcessoTrfRedistribuicaoDAO;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;

@Name(ProcessoTrfRedistribuicaoManager.NAME)
public class ProcessoTrfRedistribuicaoManager extends BaseManager<ProcessoTrfRedistribuicao>{

	public static final String NAME = "processoTrfRedistribuicaoManager";
	
	@In
	private ProcessoTrfRedistribuicaoDAO processoTrfRedistribuicaoDAO;

	@Override
	protected BaseDAO<ProcessoTrfRedistribuicao> getDAO() {
		return processoTrfRedistribuicaoDAO;
	}

	public List<ProcessoTrfRedistribuicao> recuperaRedistribuicoesProcessos(Pessoa _pessoa) {
		return processoTrfRedistribuicaoDAO.recuperaRedistribuicoesProcessos(_pessoa);
	}
	
	public List<OrgaoJulgador> recuperar(ProcessoTrf processoTrf, String tiposRedistribuicao) {
		return processoTrfRedistribuicaoDAO.recuperar(processoTrf, tiposRedistribuicao);
	}
	
}