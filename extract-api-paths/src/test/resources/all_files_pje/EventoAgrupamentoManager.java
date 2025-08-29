package br.com.infox.pje.manager;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.pje.dao.EventoAgrupamentoDAO;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.AgrupamentoManager;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.EventoAgrupamento;

@Name("eventoAgrupamentoManager")
@AutoCreate
public class EventoAgrupamentoManager extends BaseManager<EventoAgrupamento> {
	
	@In
	private EventoAgrupamentoDAO eventoAgrupamentoDAO;
	
	@Override
	protected BaseDAO<EventoAgrupamento> getDAO() {
		return eventoAgrupamentoDAO;
	}
	
	public List<Evento> recuperarEventos(Integer idAgrupamento){
		return recuperarEventos(this.getAgrupamento(idAgrupamento));
	}
	
	public List<Evento> recuperarEventos(Agrupamento agrupamento){
		List<EventoAgrupamento> eventoAgrupamentoList = eventoAgrupamentoDAO.getEventoAgrupamentoList(agrupamento);
		List<Evento> eventoList = new ArrayList<>();
		
		for (EventoAgrupamento gea : eventoAgrupamentoList) {
			eventoList.add(gea.getEvento());
		}
		
		return eventoList;
	}
	
	public List<EventoAgrupamento> recuperarEventoAgrupamentos(Integer idAgrupamento){
		return recuperarEventoAgrupamentos(this.getAgrupamento(idAgrupamento));
	}
	
	public List<EventoAgrupamento> recuperarEventoAgrupamentos(Agrupamento agrupamento){
		return eventoAgrupamentoDAO.getEventoAgrupamentoList(agrupamento);
	}
	
	private Agrupamento getAgrupamento(Integer idAgrupamento) {
		try {
			return ComponentUtil.getComponent(AgrupamentoManager.class).findById(idAgrupamento);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			return null;
		}
	}
}
