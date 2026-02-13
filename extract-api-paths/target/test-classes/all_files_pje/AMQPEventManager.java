package br.jus.cnj.pje.nucleo.manager;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.core.Events;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.infox.cliente.util.JSONUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.Util;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.amqp.AmqpExecutor;
import br.jus.cnj.pje.amqp.RabbitMQClient;
import br.jus.cnj.pje.amqp.model.dto.CloudEventBuilder;
import br.jus.cnj.pje.business.dao.AMQPEventDAO;
import br.jus.cnj.pje.nucleo.ConfiguracaoIntegracaoCloud;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventVerbEnum;
import br.jus.cnj.pje.util.ManagedBatchTransactionExecutor;
import br.jus.pje.nucleo.entidades.AMQPEvent;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Name(AMQPEventManager.NAME)
public class AMQPEventManager extends BaseManager<AMQPEvent>{

	public static final String NAME = "amqpEventManager";
	public static final String RAISE_TRANSACTION_SUCCESS_EVENT_AMQP = "raiseTransactionSuccessEventAMQP";
	public static final String RAISE_TRANSACTION_SUCCESS_EVENT_AMQP_SET = "raiseTransactionSuccessEventAMQPSet";

	@In
	private AMQPEventDAO amqpEventDAO;
	
	/**
	 * @return Instância de AMQPEventManager.
	 */
	public static AMQPEventManager instance() {
		return ComponentUtil.getComponent(AMQPEventManager.NAME);
	}
	
	/**
	 * Envia uma mensagem para o serviço de mensageria. A mensagem será registrada em banco para que seja feito o controle
	 * de mensagem enviada e erro ocorrido.
	 * 
	 * @param processoEvento ProcessoEvento.
	 * @param eventVerb (opcional) EventVerbEnum.
	 */
	@SuppressWarnings("rawtypes")
	public void enviarMensagem(Object entity, Class cloudEventClass, CloudEventVerbEnum... eventVerb) {
		if(ConfiguracaoIntegracaoCloud.isPublishMessages() || ParametroUtil.instance().isAplicacaoModoProducao()){
			AMQPEvent amqpEvent = prepararMensagem(entity, cloudEventClass, eventVerb);
			enviarMensagem(amqpEvent);
		}
	}	

	public AMQPEvent prepararMensagem(Object entity, Class cloudEventClass, CloudEventVerbEnum... eventVerb) {
		try {
			CloudEventVerbEnum event = (eventVerb != null && eventVerb.length > 0? eventVerb[0] : CloudEventVerbEnum.POST);

			CloudEvent ce = CloudEventBuilder.instance()
				.ofPayloadType(cloudEventClass)
				.withEntity(entity)
				.withEvent(event)
				.build();

			AMQPEvent amqpEvent = novoAMQPEvent(ce);
			return amqpEvent;
		} catch (JsonProcessingException e) {
			String mensagem = String.format("Não foi possível criar CloudEvent. Erro: %s", e.getLocalizedMessage());
			throw new PJeRuntimeException(mensagem);
		} catch (PJeBusinessException e) {
			throw new PJeRuntimeException(e.getLocalizedMessage(), e);
		}
	}	
	
	/**
	 * Envia uma mensagem para o serviço de mensageria. A mensagem será registrada em banco para que seja feito o controle
	 * de mensagem enviada e erro ocorrido.
	 * 
	 * @param amqpEvent AMQPEvent.
	 */
	public void enviarMensagem(AMQPEvent amqpEvent) {
		if(ConfiguracaoIntegracaoCloud.isPublishMessages() || ParametroUtil.instance().isAplicacaoModoProducao()){
			try {
				if (!getDAO().isExiste(amqpEvent)) {
					persistAndFlush(amqpEvent);
				} else {
					throw new IllegalStateException("Alguma vez isso ocorreu?!!! " + amqpEvent.toString());
				}
				EntityUtil.evict(amqpEvent);
				Events.instance().raiseTransactionSuccessEvent(RAISE_TRANSACTION_SUCCESS_EVENT_AMQP, amqpEvent);
			} catch (PJeBusinessException e) {
				throw new PJeRuntimeException(
						"Erro ao registrar mensagem no banco antes de enviar mensagem para o serviço de mensageria: "
								+ e.getLocalizedMessage(),
						e);
			}
		}
	}

	public void enviarMensagens(Collection<AMQPEvent> amqpEvents) {
		if((ConfiguracaoIntegracaoCloud.isPublishMessages() || ParametroUtil.instance().isAplicacaoModoProducao()) && !amqpEvents.isEmpty()){
			try {
				Set<Long> idSet = new HashSet<>();
				for (AMQPEvent ev: amqpEvents) {
					if (!getDAO().isExiste(ev)) {
						persist(ev);
					} else {
						throw new IllegalStateException("Alguma vez isso ocorreu?!!! " + ev.toString());
					}
					idSet.add(ev.getId());
				}
				Events.instance().raiseTransactionSuccessEvent(RAISE_TRANSACTION_SUCCESS_EVENT_AMQP_SET, idSet);
			} catch (PJeBusinessException e) {
				throw new PJeRuntimeException(e.getLocalizedMessage(), e);
			}
		}
	}

	/**
	 * @return Coleção de eventos com erro.
	 */
	public Collection<AMQPEvent> consultarEventosComErro() {
		return getDAO().consultarComErro();
	}
	
	/**
	 * Obtém a relação de mensagens a serem despachadas que foram cadastradas até quatro minutos atrás.
	 * @return Relação de ids dos eventos.
	 */
	public Collection<Long> consultarIdsMensagensPendentes() {
		GregorianCalendar dataHora = new GregorianCalendar();
		dataHora.add(GregorianCalendar.MINUTE, -60);

		return getDAO().consultarIdsMensagensPendentes(dataHora.getTime());
	}

	/**
	 * Envia uma mensagem para o serviço de mensageria após a transação atual ser executada com sucesso.
	 * 
	 * @param amqpEvent AMQPEvent.
	 */
	@Observer (RAISE_TRANSACTION_SUCCESS_EVENT_AMQP)
	public void enviarMensagemAssincrona(AMQPEvent amqpEvent) {
		Future<Boolean> retorno = AmqpExecutor.getInstance().getExecutor().submit(
			() -> {
				Util.beginTransaction();
				AMQPEventManager amqpEventManager = AMQPEventManager.instance();
				AMQPEvent amqpPersistent = null;
				Boolean resultado = false;
				try {
					amqpPersistent = amqpEventManager.findById(amqpEvent.getId());

					if(amqpPersistent == null) {
						throw new Exception("Evento AMQP não encontrado para o id: " + amqpEvent.getId());
					}

					CloudEvent cloudEvent = amqpPersistent.getCloudEvent();
					byte[] json = JSONUtil.converterObjetoParaBytes(cloudEvent);
					RabbitMQClient client = RabbitMQClient.instance();
					client.sendMessage(cloudEvent.getRoutingKey(), json, true);
					
					amqpEventManager.remove(amqpPersistent);
					amqpEventManager.flush();
					resultado = true;
				} catch (IOException | TimeoutException e) {
					registrarErro(
							e, 
							String.format("Erro ao enviar mensagem ao broker. Erro: %s", e.getLocalizedMessage()), 
							amqpPersistent);
				} catch (PJeBusinessException e) {
					registrarErro(
							e, 
							String.format("Erro ao remover AMQPEvent. Erro: %s", e.getLocalizedMessage()), 
							amqpPersistent);
				} catch (Exception e) {
					registrarErro(
							e, 
							String.format("Erro enviar mensagem ao serviço de mensageria. Erro: %s", e.getLocalizedMessage()), 
							amqpPersistent);
				} finally {
					Util.commitTransction();
				}
				return resultado;
			}
		);
	}

	private <K,E> E findById(BaseManager<E> man, K id) {
		if (man==null) 
			throw new NullPointerException("findById: man");
		
		try {
			E entity = man.findById(id);
			return entity;
		} catch (PJeBusinessException ex) {
			throw new IllegalStateException("Erro recuperando entidade pelo id" + id, ex);
		}
	}

	/**
	 * Envia uma mensagem para o serviço de mensageria após a transação atual ser executada com sucesso.
	 * 
	 * @param amqpEvent AMQPEvent.
	 */
	@Observer(RAISE_TRANSACTION_SUCCESS_EVENT_AMQP_SET)
	public void observerEnviarSetMensagemAssincrona(Collection<Long> idEvents) {
		logger.debug("AMQPEventManager.observerEnviarSetMensagemAssincrona: " + (idEvents==null ? 0 : idEvents.size()));
		enviarSetMensagemAssincrona(idEvents);
	}
	
	public Future<List<Long>> enviarSetMensagemAssincrona(Collection<Long> idEvents) {
		if (idEvents==null || idEvents.isEmpty())
			return null;
		
		String sInfo = "AMQPEventManager.enviarSetMensagemAssincrona: " + (idEvents==null ? 0 : idEvents.size());
		logger.debug(sInfo);
		
		Future<List<Long>> retorno = AmqpExecutor.getInstance().getExecutor().submit(
			() -> {
				List<Long> erros = new ArrayList<>();				
				RabbitMQClient.ISendMessage sender = RabbitMQClient.instance().createTransactionalMessageSenderSyncBeforeCommit();
				try {
					new ManagedBatchTransactionExecutor<Long,AMQPEvent>(sInfo)
							.maxElementsBeforeCommit(240)
							.maxTimeInSecsBeforeCommit(60)
							.supplier(idEvents)
							.transform(idEvent->findById(AMQPEventManager.instance(), idEvent))
							.acceptElement(amqpPersistent->amqpPersistent!=null)
							.consume(amqpPersistent->{
								try {
									CloudEvent cloudEvent = amqpPersistent.getCloudEvent();
									byte[] json = JSONUtil.converterObjetoParaBytes(cloudEvent);
									sender.sendMessage(cloudEvent.getRoutingKey(), json);
									AMQPEventManager.instance().remove(amqpPersistent);
								} catch (IOException | PJeBusinessException e) {
									throw new IllegalStateException(e.getLocalizedMessage(), e);
								}
							})
							.handleElementException((mbt, e)-> {
								try {
									erros.add(mbt.getCurrentKey());
									String sError = "Erro ao enviar mensagem ao broker: " + e;
									logger.error(sError);
									AMQPEvent ev = mbt.getCurrentElement();
									if (sError.length()>255) {
										sError = sError.substring(0, 255);
									}
									ev.setErrorMessage(sError);
									ev.setQuantidadeTentativas( ev.getQuantidadeTentativas() + 1);
									AMQPEventManager.instance().merge(ev);
								} catch (PJeBusinessException ex) {
									logger.error(ex);
									return true;
								}
								return false;
							})
							.beforeFlush(mbt-> {
								logger.debug("AMQPEventManager.enviarSetMensagemAssincrona: beforeFlush");
							})
							.afterFlush(mbt-> {
								logger.debug("AMQPEventManager.enviarSetMensagemAssincrona: afterFlush");
							})
							.run();
				} catch (Exception ex) {
					logger.error("AMQPEventManager.enviarSetMensagemAssincrona: Ocorreu um erro: " + ex.getLocalizedMessage(), ex);
				} finally {
					sender.close();
				}
				return erros;
			}
		);
		return retorno;
	}
	
	@Override
	protected AMQPEventDAO getDAO() {
		return this.amqpEventDAO;
	}

	/**
	 * Registra um erro no banco de dados para controle de reenvio.
	 * 
	 * @param e Throwable
	 * @param mensagem String
	 * @param amqpEvent AMQPEvent
	 */
	protected void registrarErro(Throwable e, String mensagem, AMQPEvent amqpEvent) {
		logger.error(mensagem);
		
		if (amqpEvent != null) {
			if (mensagem!=null && mensagem.length()>255) {
				mensagem = mensagem.substring(0,255);
			}
			amqpEvent.setErrorMessage(mensagem);
			amqpEvent.setQuantidadeTentativas(amqpEvent.getQuantidadeTentativas()+1);
			AMQPEventManager.instance().mergeAndFlush(amqpEvent);
		}
	}
	
	/**
	 * Retorna novo AMQPEvent.
	 * 
	 * @param cloudEvent CloudEvent.
	 * @return novo AMQPEvent.
	 */
	protected AMQPEvent novoAMQPEvent(CloudEvent cloudEvent) {
		AMQPEvent entidade = new AMQPEvent();
		entidade.setCloudEvent(cloudEvent);
		entidade.setPayloadHash(cloudEvent.getPayloadHash());
		entidade.setRoutingKey(cloudEvent.getRoutingKey());
		entidade.setDataCadastro(new Date());
		entidade.setQuantidadeTentativas(0);
		
		return entidade;
	}
}
