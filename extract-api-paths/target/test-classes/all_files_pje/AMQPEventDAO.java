package br.jus.cnj.pje.business.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.ProjetoUtil;
import br.jus.cnj.pje.pjecommons.model.amqp.CloudEvent;
import br.jus.pje.nucleo.entidades.AMQPEvent;

@Name(AMQPEventDAO.NAME)
public class AMQPEventDAO extends BaseDAO<AMQPEvent> {

	public static final String NAME = "amqpEventDAO";

	@Override
	public Object getId(AMQPEvent e) {
		return e.getId();
	}

	/**
	 * Retorna true se existir AMQPEvent com base no payloadClass, payloadId e routingKey.
	 * 
	 * @param amqpEvent
	 * @return Booleano
	 */
	@SuppressWarnings("unchecked")
	public Boolean isExiste(AMQPEvent amqpEvent) {
		CloudEvent cloudEvent = amqpEvent.getCloudEvent();
		String payloadHash = cloudEvent.getPayloadHash();
		
		StringBuilder hql = new StringBuilder();
		hql.append("select o from AMQPEvent o ");
		hql.append("where ");
		hql.append("	o.routingKey = :routingKey and ");
		hql.append("	o.payloadHash = :payloadHash ");

		Query query = getEntityManager().createQuery(hql.toString());
		query.setParameter("routingKey", amqpEvent.getRoutingKey());
		query.setParameter("payloadHash", payloadHash);
		
		List<AMQPEvent> resultado = query.getResultList();
		return !ProjetoUtil.isVazio(resultado);
	}

	/**
	 * @return Coleção de AMQPEvent com erro.
	 */
	@SuppressWarnings("unchecked")
	public Collection<AMQPEvent> consultarComErro() {
		StringBuilder hql = new StringBuilder();
		hql.append("select o from AMQPEvent o ");
		hql.append("where ");
		hql.append("	o.errorMessage is not null");
		
		Query query = getEntityManager().createQuery(hql.toString());
		return query.getResultList();
	}


	/**
	 * Obtém a relação de ids de eventos pendentes até o momento especificado.
	 * @return Coleção de AMQPEvent com erro.
	 */
	@SuppressWarnings("unchecked")
	public Collection<Long> consultarIdsMensagensPendentes(Date dataHoraFinal) {
		StringBuilder hql = new StringBuilder();
		hql.append("select o.id from AMQPEvent o where o.dataCadastro < :dataHoraFinal order by o.id");
		
		return getEntityManager().createQuery(hql.toString())
				.setMaxResults(50000)
				.setParameter("dataHoraFinal", dataHoraFinal)
				.getResultList();
	}
}
