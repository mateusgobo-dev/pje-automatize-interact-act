package br.com.jt.pje.query;

public interface TipoSessaoQuery {

	String TIPO_SESSAO_ITEMS_QUERY = "select o from TipoSessao o where o.ativo = true " +
								     "order by o.tipoSessao";
}
