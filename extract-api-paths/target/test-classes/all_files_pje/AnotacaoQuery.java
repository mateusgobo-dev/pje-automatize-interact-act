package br.com.infox.editor.query;

public interface AnotacaoQuery {
	
	final String TOPICO_PARAM = "topico";
	final String DOCUMENTO_PARAM = "documento";
	final String ORGAO_JULGADOR_PARAM = "orgaoJulgador";
	final String TIPO_ANOTACAO_PARAM = "tipoAnotacao";
	
	final String ANOTACOES_TOPICO = "select o from Anotacao o where o.topico = :" + TOPICO_PARAM + 
			" and o.statusAnotacao <> 'E'";
	final String ANOTACOES_DOCUMENTO = "select o from Anotacao o where o.documento = :" + DOCUMENTO_PARAM + 
			" and o.statusAnotacao <> 'E'";
	final String ORGAO_JULGADOR_POSSUI_ANOTACOES_DO_TIPO = "select 1 from Anotacao o where o.documento = :" + DOCUMENTO_PARAM + 
			" and o.orgaoJulgador = :" + ORGAO_JULGADOR_PARAM + " and o.tipoAnotacao = :" + TIPO_ANOTACAO_PARAM;
}
