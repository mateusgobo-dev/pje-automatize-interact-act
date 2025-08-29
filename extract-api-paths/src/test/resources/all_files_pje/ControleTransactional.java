package br.jus.cnj.pje.util;

import javax.transaction.Status;

import org.jboss.seam.bpm.ManagedJbpmContext;

import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.nucleo.Constants;

public class ControleTransactional {

	public static boolean verificarNecessidadeDeCommitAndClearJbpm(int cont, boolean processamentoBatch) {
		if (Util.isTransactionMarkedRollback()) {
			Util.rollbackTransaction();
			Util.beginTransaction();
		} else if (processamentoBatch == false || (cont % getBatchSize() == 0)) {
			if (processamentoBatch) {
				if (Util.getStatus() == Status.STATUS_ACTIVE) {
					try {
						JbpmUtil.limpaSessaoSemFecharContexto(ManagedJbpmContext.instance());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			HibernateUtil.getSession().flush();
			Util.commitTransction();
			Util.beginTransaction();
			EntityUtil.getEntityManager().clear();

			return true;
		}

		return false;
	}

	public static Integer getBatchSize() {
		return Constants.HIBERNATE_BATCH_SIZE;
	}

	public static void beginTransaction() {
		if (Util.isTransactionMarkedRollback() || Util.getStatus() != Status.STATUS_NO_TRANSACTION) {
			Util.rollbackTransaction();
		}

		Util.beginTransaction();
	}

	public static void rollbackTransaction() {
		Util.rollbackTransaction();
	}

	public static void beginTransactionAndClearJbpm() {
		beginTransaction();
		JbpmUtil.limpaSessaoSemFecharContexto(ManagedJbpmContext.instance());
	}

	public static void commitTransactionAndFlushAndClear() {
		HibernateUtil.getSession().flush();
		Util.commitTransction();
		EntityUtil.getEntityManager().clear();
	}
}