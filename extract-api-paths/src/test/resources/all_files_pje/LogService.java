package br.com.infox.ibpm.service;

import java.io.Serializable;
import java.util.Date;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.Util;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.DateUtil;

@Name(LogService.NAME)
@Install(precedence = Install.FRAMEWORK)
@Scope(ScopeType.EVENT)
@AutoCreate
public class LogService implements Serializable {

	private static final long serialVersionUID = 1L;
	public final static String NAME = "logService";
	
	@In
	private EmailService emailService;
	
	public void enviarLogPorEmail(Log log, Throwable t, Class<?> clazz, String metodo) {
		StringBuilder sbEmail = new StringBuilder();
		sbEmail.append("### ERRO na execucao do job ");
		sbEmail.append(clazz.getName());
		sbEmail.append(".");
		sbEmail.append(metodo);
		sbEmail.append("###<BR/>");
		sbEmail.append("### Caused by ");
		sbEmail.append(t.getMessage());
		sbEmail.append(" ###<BR/>");
		sbEmail.append("### Stack Trace ###<BR/>");
		sbEmail.append(printStackTrace(t).replace("\n", "<BR/>"));
		sbEmail.append("<BR/>### Fim da execucao do Job ###<BR/>");		
		try {
			Usuario usuario = new Usuario();
			usuario.setEmail(ParametroUtil.getParametro("emailSistema"));
			StringBuilder subject = new StringBuilder();
			subject.append("PJe - Erro ao executar JOB - ");
			subject.append(DateUtil.dateToString(new Date(), "dd/MM/yyyy - HH:mm"));
			emailService.enviarEmail(usuario, subject.toString(), sbEmail.toString());
		} catch(Exception exceptionEmail) {
			exceptionEmail.printStackTrace();
		}
	}
	
	public static LogService instance() {
		return (LogService) org.jboss.seam.Component.getInstance(LogService.NAME);
	}
	
	private String printStackTrace(Throwable t) {
		return Util.instance().printStackTraceToString(t);
	}

}
