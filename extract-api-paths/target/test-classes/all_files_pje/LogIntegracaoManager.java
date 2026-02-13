package br.com.infox.pje.manager;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.infox.pje.dao.LogIntegracaoDAO;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.webservice.PjeEurekaRegister;
import br.jus.pje.nucleo.entidades.LogIntegracao;

/**
 * Classe que acessa o DAO e contem a regra de negocios referente a entidade de
 * LogIntegracao.
 * 
 * @author Adriano Pamplona
 */
@Name(LogIntegracaoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class LogIntegracaoManager extends GenericManager {

	public static final String NAME = "logIntegracaoManager";

	@In
	private LogIntegracaoDAO logIntegracaoDAO;

	/**
	 * @return Instância da classe.
	 */
	public static LogIntegracaoManager instance() {
		return ComponentUtil.getComponent(NAME);
	}

	/**
	 * @return Lista dos logs das requisições ao Domicílio Eletrônico.
	 */
	public List<LogIntegracao> consultarLogDomicilioEletronico() {
		PjeEurekaRegister eureka = PjeEurekaRegister.instance();

		return logIntegracaoDAO.consultarURL(eureka.getURLComunicacaoProcessual(), eureka.getURLDomicilioEletronico());
	}
}
