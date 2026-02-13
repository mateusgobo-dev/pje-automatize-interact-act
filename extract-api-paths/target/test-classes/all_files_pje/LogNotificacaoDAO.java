package br.com.infox.pje.dao;

import java.io.Serializable;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.cnj.pje.business.dao.BaseDAO;
import br.jus.pje.nucleo.entidades.LogNotificacao;

@Name(LogNotificacaoDAO.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class LogNotificacaoDAO extends BaseDAO<LogNotificacao> implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "logNotificacaoDAO";
	
	@Override
	public Object getId(LogNotificacao e) {
		return e.getId();
	}
	
	
	public Boolean existeNotificaoProcessadaComSucesso(String idNotificacao) {
		String qStr = "SELECT l FROM LogNotificacao l " + "WHERE l.idNotificacao = :idNotificacao "
				+ "AND l.sucesso = true ";

		Query q = entityManager.createQuery(qStr);
		q.setParameter("idNotificacao", idNotificacao);

		return !q.getResultList().isEmpty();
	}

	public LogNotificacao findLastByNotificacaoId(String notificacaoId) {
		String qStr = "SELECT l FROM LogNotificacao l " + "WHERE l.idNotificacao = :idNotificacao ORDER BY l.id DESC";

		Query q = entityManager.createQuery(qStr);
		q.setParameter("idNotificacao", notificacaoId);

		return q.getResultList().isEmpty() ? null : (LogNotificacao) q.getResultList().get(0);
	}
	
}
