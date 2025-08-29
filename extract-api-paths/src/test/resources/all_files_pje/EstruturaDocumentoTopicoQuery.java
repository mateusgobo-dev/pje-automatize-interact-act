package br.com.infox.editor.query;

public interface EstruturaDocumentoTopicoQuery {

	String ID_ESTRUTURA_DOCUMENTO_TOPICO_PARAM = "idEstruturaDocumentoTopico";
	String ESTRUTURA_DOCUMENTO_PARAM = "estruturaDocumento";
	String COUNT_PROCESSO_DOCUMENTO_POR_EST_DOC_TOPICO_QUERY = "select count(o) from ProcessoDocumentoEstruturadoTopico o" +
												" where o.estruturaDocumentoTopico.idEstruturaDocumentoTopico = :" + ID_ESTRUTURA_DOCUMENTO_TOPICO_PARAM;
	
	String ESTRUTURA_DOCUMENTO_TOPICO_LIST = "select o from EstruturaDocumentoTopico o " +
											 "where o.estruturaDocumento = :" + ESTRUTURA_DOCUMENTO_PARAM +
											 " order by o.ordem";
}
