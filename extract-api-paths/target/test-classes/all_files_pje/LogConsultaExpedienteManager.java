package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.LogConsultaExpedienteDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.LogConsultaExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;

@Name(LogConsultaExpedienteManager.NAME)
public class LogConsultaExpedienteManager extends BaseManager<LogConsultaExpediente> {
	
	public static final String NAME = "logConsultaExpedienteManager";
	
	@In
	private LogConsultaExpedienteDAO logConsultaExpedienteDAO;

	@Override
	protected LogConsultaExpedienteDAO getDAO() {
		return logConsultaExpedienteDAO;
	}
	
	public LogConsultaExpediente registrarConsultaExpediente(
		String codigo, ProcessoParteExpediente processoParteExpediente, String urlRequisicao, String ip
	) throws PJeBusinessException {
		LogConsultaExpediente logConsultaExpediente = new LogConsultaExpediente();
		logConsultaExpediente.setCodigo(codigo);
		logConsultaExpediente.setDataLog(new Date());
		logConsultaExpediente.setIp(ip);
		logConsultaExpediente.setProcessoParteExpediente(processoParteExpediente);
		logConsultaExpediente.setUrlRequisicao(urlRequisicao);
		persistAndFlush(logConsultaExpediente);
		return logConsultaExpediente;
	}
	

	
}
