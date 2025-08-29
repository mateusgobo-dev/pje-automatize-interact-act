package br.com.infox.editor.query;

public interface CssDocumentoQuery {

	final String CSS_LIST = "select o from CssDocumento o order by o.nome";
	final String CSS_CONTEUDO_LIST = "select o.conteudo from CssDocumento o";
	final String CSS_ESTILOS_LIST = "select new br.com.infox.editor.bean.Estilo(o) from CssDocumento o order by o.nome";
}