/**
 * 
 */
package br.jus.cnj.pje.business.dao;

import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import br.com.infox.pje.query.ProcessoDocumentoExpedienteQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;

/**
 * @author cristof
 * 
 */
@Name("processoDocumentoExpedienteDAO")
public class ProcessoDocumentoExpedienteDAO extends BaseDAO<ProcessoDocumentoExpediente> implements
		ProcessoDocumentoExpedienteQuery{

	@Override
	public Integer getId(ProcessoDocumentoExpediente e){
		return e.getIdProcessoDocumentoExpediente();
	}

	public ProcessoDocumento getProcessoDocumentoAtoByExpediente(ProcessoExpediente pe){
		Query q = getEntityManager().createQuery(GET_PROCESSO_DOCUMENTO_ATO_BY_EXPEDIENTE_QUERY);
		q.setParameter(QUERY_PARAM_PROCESSO_EXPEDIENTE, pe);

		ProcessoDocumento result = EntityUtil.getSingleResult(q);
		return result;
	}

	public boolean existeDocumentoExpedienteComAto(ProcessoDocumento processoDocumento){
		Query q = getEntityManager().createQuery(COUNT_PROCESSO_DOCUMENTO_EXPEDIENTE_COM_ATO_QUERY);
		q.setParameter(QUERY_PARAM_PROCESSO_DOCUMENTO, processoDocumento);

		Long result = EntityUtil.getSingleResult(q);
		return result > 0;
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getListaProcessoDocumentoVinculadoAto(ProcessoDocumento ato){
		Query q = getEntityManager().createQuery(GET_LISTA_PROCESSO_DOCUMENTO_VINCULADO_ATO_QUERY);
		q.setParameter(QUERY_PARAM_PROCESSO_DOCUMENTO_ATO, ato);

		List<ProcessoDocumento> result = q.getResultList();
		return result;
	}

	/**
	 * Retorna a lista de processo documento de um expediente.  
	 * 
	 *  
	 * @param ProcessoExpediente
	 * @author Eduardo Paulo
	 * @since 10/06/2015
	 * @return Uma lista de ProcessoDocumento
	 */
	
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumento> getListaProcessoDocumentoVinculadoExpediente(ProcessoExpediente pe){
		Query q = getEntityManager().createQuery(GET_LISTA_PROCESSO_DOCUMENTO_VINCULADO_EXPEDIENTE_QUERY);
		q.setParameter(QUERY_PARAM_PROCESSO_EXPEDIENTE, pe); 

		List<ProcessoDocumento> result = q.getResultList();
		return result;
	}
	
}
