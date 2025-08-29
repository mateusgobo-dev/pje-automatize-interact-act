package br.com.infox.ibpm.query;

public interface TipoProcessoDocumentoQuery {

	String QUERY_PARAM_TIPO_PROCESSO_DOCUMENTO_INTIMACAO_PAUTA = "tipoProcessoDocumentoIntimacaoPauta";

	String TIPO_PROCESSO_DOCUMENTO_EXPEDIENTE_LIST_QUERY = "select o from TipoProcessoDocumento o "
			+ "where o.sistema is null and inTipoDocumento = 'E' and o.ativo = true " + "  and o != :"
			+ QUERY_PARAM_TIPO_PROCESSO_DOCUMENTO_INTIMACAO_PAUTA;

	String TIPO_PROCESSO_DOCUMENTOS = "SELECT o FROM TipoProcessoDocumento o WHERE o.ativo = :ativo AND  o.idTipoProcessoDocumento <> :idTipoProcessoDocumento";
	String TIPO_PROCESSO_DOCUMENTOS_IN = "SELECT o FROM TipoProcessoDocumento o WHERE o.ativo = :ativo AND o.idTipoProcessoDocumento in (:idTipoProcessoDocumento)";
	String TIPO_PROCESSO_DOCUMENTOS_NOT_IN = "SELECT o FROM TipoProcessoDocumento o WHERE o.ativo = :ativo AND o.idTipoProcessoDocumento not in (:idTipoProcessoDocumento)";

}