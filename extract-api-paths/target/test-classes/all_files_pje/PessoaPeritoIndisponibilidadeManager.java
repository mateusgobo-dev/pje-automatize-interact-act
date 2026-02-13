package br.com.infox.pje.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.pje.dao.PessoaPeritoIndisponibilidadeDAO;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoIndisponibilidade;

@Name("pessoaPeritoIndisponibilidadeManager")
@AutoCreate
public class PessoaPeritoIndisponibilidadeManager extends BaseManager<PessoaPeritoIndisponibilidade> {

	@In
	private PessoaPeritoIndisponibilidadeDAO pessoaPeritoIndisponibilidadeDAO;
	
	@Override
	protected BaseDAO<PessoaPeritoIndisponibilidade> getDAO() {
		return pessoaPeritoIndisponibilidadeDAO;
	}
	
	public List<PessoaPeritoIndisponibilidade> recuperarAtivos(Especialidade especialidade, PessoaPerito perito, Date data) {
		return pessoaPeritoIndisponibilidadeDAO.recuperarAtivos(especialidade, perito, data);
	}

}
