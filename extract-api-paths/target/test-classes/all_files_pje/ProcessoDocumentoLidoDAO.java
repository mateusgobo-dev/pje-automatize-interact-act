package br.jus.cnj.pje.business.dao;

import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoLido;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

@Name("processoDocumentoLidoDAO")
public class ProcessoDocumentoLidoDAO extends BaseDAO<ProcessoDocumentoLido>{

	private static final String SELECT_PROCESSO_DOCUMENTO_LIDO_PESSOA = "select o from ProcessoDocumentoLido o where o.pessoa = :pessoa and o.processoDocumento = :processoDocumento";
	private static final String SELECT_PROCESSO_DOCUMENTO_LIDO = "select o from ProcessoDocumentoLido o where o.processoDocumento in (:processosDocumentos)";
	
	@Override
	public Object getId(ProcessoDocumentoLido e) {
		return e.getIdProcessoDocumentoLido();
	}

	public ProcessoDocumentoLido getProcessoDocumentoLido(ProcessoDocumento processoDocumento, Pessoa pessoa){
		Query query = getEntityManager().createQuery(SELECT_PROCESSO_DOCUMENTO_LIDO_PESSOA);
		query.setParameter("pessoa", pessoa);
		query.setParameter("processoDocumento", processoDocumento);
		return EntityUtil.getSingleResult(query);
	}
	
	@SuppressWarnings("unchecked")
	public List<ProcessoDocumentoLido> listProcessosDocumentosLidos(List<ProcessoDocumento> documentos) {
		if (documentos == null || documentos.size() == 0) {
			return Collections.emptyList();
		}
		
		Query query = getEntityManager().createQuery(SELECT_PROCESSO_DOCUMENTO_LIDO);
		query.setParameter("processosDocumentos", documentos);
		
		return query.getResultList();
	}

	/**
	 * Recupera o objeto {@link ProcessoDocumentoLido} de acordo com o argumento informado.
	 * 
	 * @param processoDocumento {@link ProcessoDocumento}
	 * @return O objeto {@link ProcessoDocumentoLido} de acordo com o argumento informado.
	 */
	public ProcessoDocumentoLido recuperarDocumentoLido(ProcessoDocumento processoDocumento) {
		List<ProcessoDocumentoLido> resultado = null;
		Search search = new Search(ProcessoDocumentoLido.class);
		try {
			search.addCriteria(Criteria.equals("processoDocumento", processoDocumento));
			resultado = list(search);
		} catch (NoSuchFieldException e) {
			throw PJeDAOExceptionFactory.getDaoException(e);
		}
		return !resultado.isEmpty() ? resultado.get(0) : null;
	}
}
