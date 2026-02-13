package br.com.jt.pje.query;

public interface AnotacaoVotoQuery {
	
	String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";
	String QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO = "orgaoJulgadorColegiado";
	String QUERY_PARAMETER_PROCESSO_TRF = "processoTrf";
	String QUERY_PARAMETER_SESSAO = "sessao";
	
	String ANOTACAO_VOTO_SEM_SESSAO_BY_PROCESSO_ORGAO_JULGADOR_E_COLEGIADO_QUERY = "select o from AnotacaoVoto o " +
												 						"where o.processoTrf = :"+
												 						QUERY_PARAMETER_PROCESSO_TRF+
												 						" and o.orgaoJulgador = :"+
												 						QUERY_PARAMETER_ORGAO_JULGADOR+
												 						" and o.orgaoJulgadorColegiado = :" +
												 						QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO;
	String ANOTACOES_VOTO_BY_PROCESSO_E_ORGAO_JULGADOR_COLEGIADO_EXCLUINDO_SESSAO_ATUAL_QUERY = "select o from AnotacaoVoto o " +
				"where o.processoTrf = :"+
				QUERY_PARAMETER_PROCESSO_TRF+
				" and o.orgaoJulgadorColegiado = :" +
				QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO +
				" and o.orgaoJulgador is null" +
				" and o.sessao != :" +
				QUERY_PARAMETER_SESSAO;
	
	String ANOTACOES_VOTO_BY_PROCESSO_E_ORGAO_JULGADOR_E_COLEGIADO_EXCLUINDO_SESSAO_ATUAL_QUERY = "select o from AnotacaoVoto o " +
			"where o.processoTrf = :"+
			QUERY_PARAMETER_PROCESSO_TRF+
			" and o.orgaoJulgadorColegiado = :" +
			QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO +
			" and o.orgaoJulgador = :"+
			QUERY_PARAMETER_ORGAO_JULGADOR+
			" and o.sessao != :" +
			QUERY_PARAMETER_SESSAO;
	
	String ANOTACOES_VOTO_BY_PROCESSO_E_ORGAO_JULGADOR_E_COLEGIADO_SEM_SESSAO_QUERY = "select o from AnotacaoVoto o " +
			"where o.processoTrf = :"+
			QUERY_PARAMETER_PROCESSO_TRF+
			" and o.orgaoJulgadorColegiado = :" +
			QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO +
			" and o.orgaoJulgador = :"+
			QUERY_PARAMETER_ORGAO_JULGADOR+
			" and o.sessao is null";
	
	String ANOTACOES_VOTO_BY_PROCESSO_E_ORGAO_JULGADOR_SEM_SESSAO_QUERY = "select o from AnotacaoVoto o " +
			"where o.processoTrf = :"+
			QUERY_PARAMETER_PROCESSO_TRF+
			" and o.orgaoJulgador = :" +
			QUERY_PARAMETER_ORGAO_JULGADOR +
			" and o.sessao != :" +
			QUERY_PARAMETER_SESSAO;
	
	String ANOTACOES_SEM_SESSAO_BY_PROCESSO_QUERY = "select o from AnotacaoVoto o where "+
													"o.sessao is null " +
													"and o.processoTrf = :" +QUERY_PARAMETER_PROCESSO_TRF;
	
	String ANOTACOES_BY_SESSAO_PROCESSO_QUERY = "select o from AnotacaoVoto o where "+
													"o.sessao = :" +QUERY_PARAMETER_SESSAO+
													" and o.processoTrf = :" +QUERY_PARAMETER_PROCESSO_TRF;
	
	String ANOTACAO_VOTO_BY_PROCESSO_SESSAO_ORGAO_JULGADOR_E_COLEGIADO_QUERY = "select o from AnotacaoVoto o where "+
													"o.sessao = :" +QUERY_PARAMETER_SESSAO+
													" and o.processoTrf = :" +QUERY_PARAMETER_PROCESSO_TRF+
													" and o.orgaoJulgador = :"+QUERY_PARAMETER_ORGAO_JULGADOR+
													" and o.orgaoJulgadorColegiado = :"+QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO;
	
	String ANOTACAO_VOTO_SEM_OJ_BY_PROCESSO_SESSAO_E_COLEGIADO_QUERY = "select o from AnotacaoVoto o where "+
													"o.sessao = :" +QUERY_PARAMETER_SESSAO+
													" and o.processoTrf = :" +QUERY_PARAMETER_PROCESSO_TRF+
													" and o.orgaoJulgador is null "+
													" and o.orgaoJulgadorColegiado = :"+QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO;
	
}