package br.com.infox.pje.query;

public interface CargoQuery {

	String CARGO_ITEMS_QUERY = "select o from Cargo o where o.ativo = true " + "order by o.cargo";

}
