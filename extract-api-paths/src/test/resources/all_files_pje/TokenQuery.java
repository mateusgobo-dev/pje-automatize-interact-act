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
package br.jus.pje.nucleo.entidades.acesso;

public interface TokenQuery{

	String OBTER_TOKEN = "Token.obterToken";

	String PARAM_ID_TOKEN = "tokenId";
	String PARAM_IP = "ip";

	String HQL_OBTER_TOKEN = "select o from Token o where o.id = :" + PARAM_ID_TOKEN + " and o.ip = :" + PARAM_IP;

}
