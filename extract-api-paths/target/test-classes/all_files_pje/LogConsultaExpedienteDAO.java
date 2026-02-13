package br.jus.cnj.pje.business.dao;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.LogConsultaExpediente;

@Name("logConsultaExpedienteDAO")
public class LogConsultaExpedienteDAO extends BaseDAO<LogConsultaExpediente> {
	
	@Override
	public Object getId(LogConsultaExpediente log) {
		return log.getIdLog();
	}

}
