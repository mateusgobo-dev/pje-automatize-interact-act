package br.com.infox.pje.manager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.dao.LogNotificacaoDAO;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.pje.nucleo.entidades.LogNotificacao;


@Name(LogNotificacaoManager.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class LogNotificacaoManager extends BaseManager<LogNotificacao> {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "logNotificacaoManager";

	@In
	private LogNotificacaoDAO logNotificacaoDAO;


	public static LogNotificacaoManager instance() {
		return ComponentUtil.getComponent(NAME);
	}
	

	@Override
	protected BaseDAO<LogNotificacao> getDAO() {
		return logNotificacaoDAO;
	}


	public boolean existeNotificaoProcessadaComSucesso(String notificacaoId) {
		return logNotificacaoDAO.existeNotificaoProcessadaComSucesso(notificacaoId);
	}

	public LogNotificacao findLastByNotificacaoId(String notificacaoId) {
		return logNotificacaoDAO.findLastByNotificacaoId(notificacaoId);
	}

}
