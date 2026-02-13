package br.com.infox.pje.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.EventoAgrupamento;

@Name("eventoAgrupamentoDAO")
@AutoCreate
public class EventoAgrupamentoDAO extends BaseDAO<EventoAgrupamento>{

	@Override
	public Object getId(EventoAgrupamento e) {
		return e.getIdEventoAgrupamento();
	}

	@SuppressWarnings("unchecked")
	public List<EventoAgrupamento> getEventoAgrupamentoList(Agrupamento agrupamento) {
		List<EventoAgrupamento> eventoAgrupamentoList = new ArrayList<EventoAgrupamento>();
		if(agrupamento != null) {
			StringBuilder sb = new StringBuilder("SELECT o from EventoAgrupamento o ")
					.append(" WHERE ")
					.append(" o.evento.ativo = true ")
					.append(" AND o.agrupamento = :agrupamento ");
			
			Query q = getEntityManager().createQuery(sb.toString());
			q.setParameter("agrupamento", agrupamento);
			
			eventoAgrupamentoList = q.getResultList();
		}

		return eventoAgrupamentoList;
	}
}
