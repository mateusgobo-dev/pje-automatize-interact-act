package br.com.infox.pje.query;

public interface EstadoCivilQuery {

	String ESTADO_CIVIL_ITEMS_QUERY = "select o from EstadoCivil o where o.ativo is true " + "order by o.estadoCivil";

}