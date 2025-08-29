package br.com.infox.pje.query;

public interface EtniaQuery {

	String ETNIA_ITEMS_QUERY = "select o from Etnia o where o.ativo is true " + "order by o.etnia";

}