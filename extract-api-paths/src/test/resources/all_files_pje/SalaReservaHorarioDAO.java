package br.jus.cnj.pje.business.dao;

import java.util.List;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.pje.nucleo.entidades.ReservaHorario;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaReservaHorario;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Name("salaReservaHorarioDAO")
public class SalaReservaHorarioDAO extends BaseDAO<SalaReservaHorario> {

	@Override
	public Integer getId(SalaReservaHorario e) {
		return e.getIdSalaReservaHorario();
	}
	
	public SalaReservaHorario find(ReservaHorario reservaHorario, Sala sala, boolean somenteAtivos){
		try {
			Search search = new Search(SalaReservaHorario.class);
			search.addCriteria(Criteria.equals("reservaHorario.idReservaHorario", reservaHorario.getIdReservaHorario()));
			search.addCriteria(Criteria.equals("sala.idSala", sala.getIdSala()));
			if (somenteAtivos){
				search.addCriteria(Criteria.equals("ativo", somenteAtivos));
			}
			search.addOrder("idSalaReservaHorario", Order.ASC);
			search.setMax(1);
			List<SalaReservaHorario> list = list(search);
			return ProjetoUtil.isNotVazio(list) ? list.get(0) : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	public SalaReservaHorario find(ReservaHorario reservaHorario, String identificador, Sala sala, boolean somenteAtivos){
		try {
			Search search = new Search(SalaReservaHorario.class);
			search.addCriteria(Criteria.equals("reservaHorario.idReservaHorario", reservaHorario.getIdReservaHorario()));
			search.addCriteria(Criteria.equals("identificadorReservaHorario", identificador));
			search.addCriteria(Criteria.equals("sala.idSala", sala.getIdSala()));
			if (somenteAtivos){
				search.addCriteria(Criteria.equals("ativo", somenteAtivos));
			}
			search.addOrder("idSalaReservaHorario", Order.ASC);
			search.setMax(1);
			List<SalaReservaHorario> list = list(search);
			return ProjetoUtil.isNotVazio(list) ? list.get(0) : null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	public List<SalaReservaHorario> find(String identificador, OrgaoJulgador orgaoJulgador, boolean somenteAtivos){
		try {
			Search search = new Search(SalaReservaHorario.class);
			search.addCriteria(Criteria.equals("identificadorReservaHorario", identificador));
			search.addCriteria(Criteria.equals("sala.orgaoJulgador.idOrgaoJulgador", orgaoJulgador.getIdOrgaoJulgador()));
			if (somenteAtivos){
				search.addCriteria(Criteria.equals("ativo", somenteAtivos));
			}
			search.addOrder("idSalaReservaHorario", Order.ASC);
			return list(search);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}	
	
	public List<SalaReservaHorario> findByIdentificador(String identificador, boolean somenteAtivos){
		try {
			Search search = new Search(SalaReservaHorario.class);
			search.addCriteria(Criteria.equals("identificadorReservaHorario", identificador));
			if (somenteAtivos){
				search.addCriteria(Criteria.equals("ativo", somenteAtivos));
			}
			search.addOrder("idSalaReservaHorario", Order.ASC);
			return list(search);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	public List<SalaReservaHorario> findBySala(Sala sala, boolean somenteAtivos){
		try {
			Search search = new Search(SalaReservaHorario.class);
			search.addCriteria(Criteria.equals("sala.idSala", sala.getIdSala()));
			if (somenteAtivos){
				search.addCriteria(Criteria.equals("ativo", somenteAtivos));
			}
			search.addOrder("reservaHorario.dsTraducaoExpressaoCron", Order.ASC);
			return list(search);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}		
	}
}
