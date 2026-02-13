package br.com.infox.pje.query;

public interface ModeloDocumentoLocalQuery {

	/*
	 * Parametros utilizados na consulta.
	 */
	public final static String QUERY_PARAMETER_MODELO_DOCUMENTO = "modeloDocumento";
	public final static String QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO = "tipoProcessoDocumento";
	
	/**
	 * Listagem de modelos de documento filtrado por tipo
	 */

	/*
	 * [PJEII-1571] Classe alterada para comportar as modificações requeridas pela issue.
	 */
	public final static String SELECT_MODELO_QUERY = "select mdl from ModeloDocumentoLocal mdl where mdl.ativo = true ";
	
	public final static String SELECT_MODELO_POR_TIPO_QUERY = SELECT_MODELO_QUERY
			+ " and mdl.tipoProcessoDocumento = :" + QUERY_PARAMETER_TIPO_PROCESSO_DOCUMENTO;
	

}
