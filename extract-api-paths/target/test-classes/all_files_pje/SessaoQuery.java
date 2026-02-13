package br.com.jt.pje.query;

public interface SessaoQuery {
	
	String QUERY_PARAMETER_SALA_HORARIO = "salaHorario";
	String QUERY_PARAMETER_DATA = "data";
	String QUERY_PARAMETER_ORGAO_JULGADOR = "OJ";
	String QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO = "OJC";
	String QUERY_PARAMETER_SESSAO = "sessao";
	
	String EXISTE_SESSAO_QUERY = "select o from SessaoJT o where " +
								 "cast(o.dataSessao as date) = :"+QUERY_PARAMETER_DATA +
							     " and o.salaHorario = :"+QUERY_PARAMETER_SALA_HORARIO;
	
	/*
	 * PJE-JT: Ricardo Scholz : PJEII-3823 - 2012-11-14 Alteracoes feitas pela JT.
	 * Inclusão de restrição "and o.situacaoSessao = 'A'", de forma que apenas as sessões com situação
	 * 'Aberta' sofrem o fechamento automático.
	 */
	String SESSOES_COM_DATA_FECHAMENTO_PAUTA_DIA_CORRENTE_QUERY = "select o from SessaoJT o where " +
											 					"cast(o.dataFechamentoPauta as date) = current_date "+
										 						"and o.orgaoJulgadorColegiado.fechamentoAutomatico = true " +
										 						"and o.situacaoSessao = 'A'";
	/*
	 * PJE-JT: Fim.
	 */
        
        
 
    /**
     * [PJEII-4329] - Criada para obter a sessao de julgamento
     */
    String SESSAO_JULGAMENTO_POR_ORGAO_JULGADOR_E_ORGAO_JULGADOR_COLEGIADO = "select o.sessao from SessaoComposicaoOrdem o "
            + "where o.sessao.dataAberturaSessao = null and o.sessao.dataSessao >= :" + QUERY_PARAMETER_DATA
            + " and o.sessao.orgaoJulgadorColegiado = :" + QUERY_PARAMETER_ORGAO_JULGADOR_COLEGIADO 
            + " and o.orgaoJulgador = :" + QUERY_PARAMETER_ORGAO_JULGADOR
            + " and o.sessao.dataRealizacaoSessao is null and o.sessao.dataFechamentoPauta is null "
            // FIXME TSE: Ordenacao da sessoes por data
            + "order by o.sessao.dataSessao ";


    /**
     * [PJEII-4330] Consulta dos órgãos julgadores de uma sessao
     */
    String ORGAOS_JULGADORES_DA_SESSAO = "Select o.orgaoJulgador from SessaoComposicaoOrdem o where o.sessao = :" + QUERY_PARAMETER_SESSAO;
        
}
