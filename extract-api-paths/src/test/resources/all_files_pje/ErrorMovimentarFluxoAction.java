package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.jbpm.graph.action.Script;
import org.jbpm.graph.log.ActionLog;
import org.jbpm.logging.log.CompositeLog;
import org.jbpm.logging.log.ProcessLog;

@Name(ErrorMovimentarFluxoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class ErrorMovimentarFluxoAction implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "errorMovimentarFluxoAction";

	private Throwable lastThrowable;
	@Logger
	private Log log;

	private List<ProcessLog> processLogs;

	public String printLogs() {
		StringBuilder retorno = new StringBuilder();
		
		if (this.processLogs != null) {
			for (ProcessLog processLog : processLogs) {
				if (processLog.getParent() == null) {
					logLog("", processLog, retorno);
				}
			}
		}
		return retorno.toString();
	}

	private void logLog(String indentation, ProcessLog processLog, StringBuilder retorno) {
		boolean isComposite = processLog instanceof CompositeLog;
		retorno.append(indentation);
		retorno.append(isComposite ? "+ [" : "  [");
		retorno.append(processLog.getIndex());
		retorno.append("] ");
		if (processLog instanceof ActionLog 
				&& ((ActionLog) processLog).getException() == null
				&& ((ActionLog) processLog).getAction() != null 
				&& ((ActionLog) processLog).getAction() instanceof Script
				&& ((Script) ((ActionLog) processLog).getAction()).getExpression() != null) {
			ActionLog actionLog = (ActionLog) processLog;
			retorno.append(((Script) actionLog.getAction()).getExpression().replace("#{", "${"));
			retorno.append(" - ");
			retorno.append(actionLog.getAction().getEvent());
		} else {
			retorno.append(processLog.toString().replace("#{", "${"));
		}
		retorno.append(" on ");
		retorno.append(processLog.getToken());
		retorno.append("\n");
		
		if (isComposite) {
			CompositeLog compositeLog = (CompositeLog) processLog;
			List<ProcessLog> children = compositeLog.getChildren();
			if (children != null) {
				for (ProcessLog childLog : children) {
					logLog(indentation + "  ", childLog, retorno);
				}
			}
		}
	}

	public Throwable getLastThrowable() {
		return lastThrowable;
	}
	
	public void setLastThrowable(Throwable lastThrowable) {
		this.lastThrowable = lastThrowable;
	}

	public void setProcessLogs(List<ProcessLog> processLogs) {
		this.processLogs = processLogs;
	}

}
