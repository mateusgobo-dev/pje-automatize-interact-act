package br.com.infox.editor.query;

public interface AutoTextoQuery {

	String DESCRICAO_AUTOTEXTO_PARAM = "descricao";
	String LOCALIZACAO_AUTOTEXTO_PARAM = "localizacao";
	String USUARIO_AUTOTEXTO_PARAM = "usuario";

	String AUTOTEXTO_POR_DESCRICAO_E_LOCALIZACAO_LIST_QUERY = "select o from AutoTexto o where " +
		"o.localizacao = :" + LOCALIZACAO_AUTOTEXTO_PARAM + " and o.publico = true and " +
		"lower(o.descricao) like " +
		"concat('%', lower(:" + DESCRICAO_AUTOTEXTO_PARAM + "), '%') order by o.descricao";

	String AUTOTEXTO_POR_DESCRICAO_E_USUARIO_LIST_QUERY = "select o from AutoTexto o where " +
		"o.usuario = :" + USUARIO_AUTOTEXTO_PARAM + " and o.publico = false and " +
		"lower(o.descricao) like " +
		"concat('%', lower(:" + DESCRICAO_AUTOTEXTO_PARAM + "), '%') order by o.descricao";

	String AUTOTEXTO_POR_LOCALIZACAO_LIST_QUERY = "select o from AutoTexto o where " +
		"o.localizacao = :" + LOCALIZACAO_AUTOTEXTO_PARAM + " and o.publico = true order by o.descricao";

	String AUTOTEXTO_POR_USUARIO_LIST_QUERY = "select o from AutoTexto o where " +
		"o.usuario = :" + USUARIO_AUTOTEXTO_PARAM + " and o.publico = false order by o.descricao";
}