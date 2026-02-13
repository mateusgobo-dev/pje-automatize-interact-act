package br.jus.cnj.pje.indexer;

import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.indexacao.ElasticSearchProvider;
import br.jus.pje.indexacao.Indexador;

import java.io.IOException;
import java.io.Serializable;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name(IndexerQueueListener.NAME)
@Scope(ScopeType.APPLICATION)
public class IndexerQueueListener implements MessageListener {

	public static final String NAME = "indexerQueueListener";
	
	@In
	private Indexador indexador;

	@Logger
	private Log logger;
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void onMessage(Message message) {
		try {
			if (message instanceof ObjectMessage) {
				Serializable obj = ((ObjectMessage)message).getObject();
				if (obj instanceof IndexVO) {
					IndexVO indexVO = (IndexVO)obj;
					this.indexador.indexar(indexVO.getClazz(), indexVO.getId());
				}
			}
		} catch (JMSException ex) {
			logger.error("Erro ao processar mensagem JMS {0} no listener de indexação.", ex, message);
		} catch (PJeBusinessException | ElasticSearchProvider.TimeOutException e) {
			throw new RuntimeException(e);
		}
	}
}
