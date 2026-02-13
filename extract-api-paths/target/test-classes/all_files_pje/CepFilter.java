/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.entidades.filters;

public interface CepFilter {

	public static final String CONDITION_CEP_ESTADO = "nr_cep like :numeroCep || '%'";
	public static final String FILTER_PARAM_NUMERO_CEP = "numeroCep";
	public static final String FILTER_CEP_ESTADO = "cepEstado";

}