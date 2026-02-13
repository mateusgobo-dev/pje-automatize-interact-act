package br.jus.cnj.pje.business.dao;

import java.util.List;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.pje.nucleo.entidades.ReservaHorario;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("reservaHorarioDAO")
public class ReservaHorarioDAO extends BaseDAO<ReservaHorario> {

	@Override
	public Integer getId(ReservaHorario e) {
		return e.getIdReservaHorario();
	}
	
	public ReservaHorario findByExpressoes(String expressaoCronInicio, String expressaoCronTermino){
		try {
			Search search = new Search(ReservaHorario.class);
			search.addCriteria(Criteria.equals("dsExpressaoCronInicio", expressaoCronInicio));
			search.addCriteria(Criteria.equals("dsExpressaoCronTermino", expressaoCronTermino));
			search.setMax(1);
			List<ReservaHorario> list = list(search);
			return ProjetoUtil.isNotVazio(list) ? list.get(0) : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
	}
}
