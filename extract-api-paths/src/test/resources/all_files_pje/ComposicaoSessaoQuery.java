package br.com.jt.pje.query;

public interface ComposicaoSessaoQuery {
	
	String QUERY_PARAMETER_SESSAO = "sessao";
	String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";
	String QUERY_PARAMETER_LIST_OJ = "listOJ";
	String QUERY_PARAMETER_PROCESSO_TRF = "processoTrf";
	
	String ORGAO_JULGADOR_BY_SESSAO_QUERY = "select o.orgaoJulgador from ComposicaoSessao o where " +
								 			"o.sessao = :"+QUERY_PARAMETER_SESSAO;
	
	String COMPOSICAO_SESSAO_LIST_BY_SESSAO_QUERY = "select o from ComposicaoSessao o where " +
													"o.sessao = :"+QUERY_PARAMETER_SESSAO;
	
	String COMPOSICAO_SESSAO_QUERY = COMPOSICAO_SESSAO_LIST_BY_SESSAO_QUERY +
									 " and orgaoJulgador = :"+QUERY_PARAMETER_ORGAO_JULGADOR;
	
	String COMPOSICAO_SESSAO_SEM_COMPOSICAO_PROCESSO_BY_SESSAO_PROCESSO_QUERY = COMPOSICAO_SESSAO_LIST_BY_SESSAO_QUERY +
															 					" and o not in (select cps.composicaoSessao from ComposicaoProcessoSessao cps " +
															 					"				where cps.pautaSessao.processoTrf = :"+QUERY_PARAMETER_PROCESSO_TRF+")";
	
}