package br.jus.cnj.pje.auditoria;

import org.jboss.seam.annotations.In;
import org.slf4j.LoggerFactory;

import br.com.infox.ibpm.entity.log.LogUtil;
import br.jus.cnj.pje.indexer.IndexerQueueSender;
import br.jus.pje.indexacao.Indexador;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.entidades.log.EntityLogDetail;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;

public abstract class PjeLog {

	@In
	protected Indexador indexador;
	
	@In
	protected IndexerQueueSender indexerQueueSender;
	
	protected org.slf4j.Logger logger = LoggerFactory.getLogger(PjeLogMQ.class); 

	public abstract void log(Object instance, TipoOperacaoLogEnum tipoOperacao);

	protected abstract void log(Class<?> clazz,
			 Object id,
			 Object[] oldState, 
			 Object[] state, 
			 String[] nomes, 
			 Integer idUsuario, 
			 String ip, 
			 String url, 
			 TipoOperacaoLogEnum tipoOperacao);

	public EntityLogDetail createDetails(EntityLog log, Class<?> clazz, Object id, TipoOperacaoLogEnum tipoOperacao, String name, Object old, Object actual) throws Exception {
		if ((tipoOperacao == TipoOperacaoLogEnum.U) && LogUtil.compareObj(old, actual)) {
			return null;
		}

		if (!LogUtil.isValidForLog(clazz, name)) {
			return null;
		}

		EntityLogDetail detail = new EntityLogDetail(log, name, LogUtil.toStringForLog(actual), LogUtil.toStringForLog(old));
		return detail;
	}
	
}