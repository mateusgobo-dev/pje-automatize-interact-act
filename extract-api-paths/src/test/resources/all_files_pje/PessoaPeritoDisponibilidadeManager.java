package br.com.infox.pje.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.pje.dao.PessoaPeritoDisponibilidadeDAO;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaPeritoDisponibilidade;
import br.jus.pje.nucleo.enums.SemanaEnum;

@Name("pessoaPeritoDisponibilidadeManager")
@AutoCreate
public class PessoaPeritoDisponibilidadeManager extends BaseManager<PessoaPeritoDisponibilidade> {

	@In
	private PessoaPeritoDisponibilidadeDAO pessoaPeritoDisponibilidadeDAO;
	
	@Override
	protected BaseDAO<PessoaPeritoDisponibilidade> getDAO() {
		return pessoaPeritoDisponibilidadeDAO;
	}
	
	public List<PessoaPeritoDisponibilidade> recuperarAtivos(Especialidade especialidade, PessoaPerito perito) {
		return pessoaPeritoDisponibilidadeDAO.recuperarAtivos(especialidade, perito, null);
	}
	
	public List<PessoaPeritoDisponibilidade> recuperarAtivos(Especialidade especialidade, PessoaPerito perito, Date data) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(data);
		
		return pessoaPeritoDisponibilidadeDAO.recuperarAtivos(especialidade, perito, SemanaEnum.getSemanaEnum(calendar.get(Calendar.DAY_OF_WEEK)));
	}

}
