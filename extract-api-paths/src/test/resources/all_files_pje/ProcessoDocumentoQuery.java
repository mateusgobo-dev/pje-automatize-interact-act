package br.com.infox.pje.query;

/**
 * Interface com as strings, representando parametros queries e namedQueries
 * para a entidade de processoDocumento.
 * 
 * @author Daniel
 * 
 */
public interface ProcessoDocumentoQuery {

	String QUERY_PARAMETER_PROCESSO = "processo";
	String QUERY_PARAMETER_PROCESSO_DOC_BIN = "procDocBin";
	String QUERY_PARAMETER_TIPO_DOC = "tipoDoc";
	
	String LIST_PROCESSO_DOCUMENTO_BY_TIPO_QUERY = "select pd from ProcessoDocumento pd "+
												   "where pd.processo = :"+QUERY_PARAMETER_PROCESSO+" and "+
												   "pd.tipoProcessoDocumento = :"+QUERY_PARAMETER_TIPO_DOC;
	
	
	String LIST_ULTIMO_PROCESSO_DOCUMENTO_BY_TIPO_QUERY = "select o from ProcessoDocumento o where o.tipoProcessoDocumento= :"+QUERY_PARAMETER_TIPO_DOC+
															" and o.processo = :"+QUERY_PARAMETER_PROCESSO+" and o.ativo=true " +
															"and o.dataInclusao = (  select MAX(o2.dataInclusao) from ProcessoDocumento o2 " +
																					"where o2.idProcessoDocumento = o.idProcessoDocumento)";
	
	String IS_DOCUMENTO_ASSINADO = "select pdbpa from ProcessoDocumentoBinPessoaAssinatura pdbpa where pdbpa.processoDocumentoBin= :"+QUERY_PARAMETER_PROCESSO_DOC_BIN;
	
	String GET_DOCUMENTO_BY_TIPO_PROCESSO = "select o from ProcessoDocumento o where o.tipoProcessoDocumento= :"+ QUERY_PARAMETER_TIPO_DOC +" " +
					"and o.processo= :"+QUERY_PARAMETER_PROCESSO+" and o.ativo=true";
	
}