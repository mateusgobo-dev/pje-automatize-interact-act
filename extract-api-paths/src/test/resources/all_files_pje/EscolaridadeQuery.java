package br.com.infox.pje.query;

public interface EscolaridadeQuery {

	String ESCOLARIDADE_ITEMS_QUERY = "select o from Escolaridade o where o.ativo is true " + "order by o.escolaridade";

}