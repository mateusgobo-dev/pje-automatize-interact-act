package br.com.infox.editor.query;

public interface EstruturaDocumentoTopicoMagistradoQuery {

	String PESSOA_MAGISTRADO_PARAM = "pessoaMagistrado";
	String ESTRUTURA_DOCUMENTO_TOPICO_PARAM = "estruturaDocumentoTopico";

	String ESTRUTURA_DOCUMENTO_TOPICO_MAGISTRADO_QUERY = "select o from EstruturaDocumentoTopicoMagistrado o " +
														 "where o.pessoaMagistrado = :" +PESSOA_MAGISTRADO_PARAM +
														 "  and o.estruturaDocumentoTopico = :" + ESTRUTURA_DOCUMENTO_TOPICO_PARAM;
}