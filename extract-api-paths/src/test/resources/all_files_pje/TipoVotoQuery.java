package br.com.jt.pje.query;

public interface TipoVotoQuery {
	
	String TIPO_VOTO_ATIVO = "select o from TipoVotoJT o where o.ativo = true ";
	
	String TIPO_VOTO_RELATOR_QUERY = TIPO_VOTO_ATIVO+
									 " and o.tipoResponsavel = 'R'";
	
	String TIPO_VOTO_VOGAL_QUERY = TIPO_VOTO_ATIVO+
								   " and o.tipoResponsavel = 'V'";
	
}
