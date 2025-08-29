package br.com.infox.pje.query;

/**
 * Classe que armazena todas os sql's e namedQuerys referentes a entidade
 * SessaoComposicaoOrdem
 * 
 * @author daniel
 * 
 */
public interface SessaoComposicaoOrdemQuery {

	String QUERY_PARAMETER_SESSAO = "sessao";

	/*
	 * Obtem a lista de Orgaos Julgadores da Composição da Sessao informada
	 */
	String LIST_ORGAO_JULGADOR_COMPOSICAO_SESSAO_QUERY = "select distinct(o.orgaoJulgador) from SessaoComposicaoOrdem o "
			+ "where o.sessao = :" + QUERY_PARAMETER_SESSAO;

}