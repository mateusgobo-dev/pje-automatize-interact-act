package br.com.infox.pje.query;

/**
 * Interface com as strings, representando parametros queries e namedQueries
 * para a entidade de processoDocumentoTrfLocal.
 * 
 * @author Daniel
 * 
 */
public interface ProcessoDocumentoTrfLocalQuery {

	String QUERY_PARAM_PROCESSO_DOCUMENTO = "idProcessoDocumento";

	String LIST_PD_TRF_BY_PD_QUERY = "select o from ProcessoDocumentoTrfLocal o "
			+ "where o.idProcessoDocumentoTrf = :" + QUERY_PARAM_PROCESSO_DOCUMENTO;

}