package br.jus.cnj.pje.business.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.CalendarioEvento;

@Name(CalendarioEventoDAO.NAME)
public class CalendarioEventoDAO extends BaseDAO<CalendarioEvento> {

	public static final String NAME = "calendarioEventoDAO";
	@Override
	public Object getId(CalendarioEvento e) {
		return e.getIdCalendarioEvento();
	}
	
	/**
	 * Carrega a lista de feriados
	 * @return lista de feriados (List<CalendarioEvento>)
	 */
	@SuppressWarnings("unchecked")
	public List<CalendarioEvento> obterListaDeFeriadosRelevantes() {
		List<CalendarioEvento> listaDeFeriados;
		
		// obter lista de feriados
		String hql = "select o from CalendarioEvento o where o.ativo = true and (o.inJudiciario = true or o.inFeriado = true)";
		
		Query query = getEntityManager().createQuery(hql);
		listaDeFeriados = query.getResultList();
		
		return (listaDeFeriados != null ? listaDeFeriados : new ArrayList<CalendarioEvento>());
	}

	/**
	 * @return True se existir pelo menos um registro com in_prazos_recalculados = false.
	 */
	public Boolean isRecalcularPrazos() {
		String hql = "select count(o) from CalendarioEvento o where o.inPrazosRecalculados = false";
		Query query = getEntityManager().createQuery(hql);
		Number count = (Number) query.getSingleResult();
		
		return (count.intValue() > 0);
	}

	/**
	 * Atribui CalendarioEvento.inPrazosRecalculados = true para todos os registros.
	 */
	public void prazosRecalculados() {
		String hql = "update CalendarioEvento o set o.inPrazosRecalculados = true where o.inPrazosRecalculados = false";
		Query query = getEntityManager().createQuery(hql);
		query.executeUpdate();
	}
}
