package br.com.infox.editor.query;

public interface PreferenciaQuery {
	String USUARIO_PREFERENCIA_PARAM = "usuario";
	String PREFERENCIA_EDITOR_PARAM = "preferenciaEditor";
	
	String PREFERENCIA_POR_USUARIO_E_DESCRICAO_QUERY = "select o from Preferencia o " +
			"where o.usuario = :" + USUARIO_PREFERENCIA_PARAM + " and " +
			"o.preferenciaEditor = :" + PREFERENCIA_EDITOR_PARAM;
}
