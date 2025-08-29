package br.com.infox.editor.query;

public interface EstruturaDocumentoQuery {
	
	String TIPO_PROCESSO_DOCUMENTO_PARAM = "tipoProcessoDocumento";
	String ESTRUTURA_DOCUMENTO_POR_TIPO_LIST_QUERY = "select o.estruturaDocumento from EstruturaTipoDocumento o" +
												" where o.tipoProcessoDocumento = :" + TIPO_PROCESSO_DOCUMENTO_PARAM +
												" and o.estruturaDocumento.ativo = true" +
												" order by o.estruturaDocumento.estruturaDocumento ";	

}
