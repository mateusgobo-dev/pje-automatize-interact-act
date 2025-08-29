package br.jus.cnj.pje.util;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.view.ErrorMovimentarFluxoAction;

public class ErrorMovimentarFluxoLogAppender extends AppenderSkeleton {
	
	public static final String NAME = "errorMovimentarFluxoLogAppender";

	@Override
	protected void append(LoggingEvent event) {
		if (event == null) {
			return;
		}
		Throwable throwable = (event.getThrowableInformation() == null)?null:event.getThrowableInformation().getThrowable();
		if (throwable != null) {
			ErrorMovimentarFluxoAction action = ComponentUtil.getComponent(ErrorMovimentarFluxoAction.NAME);
			if (action != null) {
				action.setLastThrowable(throwable);
			}
		}
	}

	@Override
	public void close() {}

	@Override
	public boolean requiresLayout() {
		return false;
	}

}
