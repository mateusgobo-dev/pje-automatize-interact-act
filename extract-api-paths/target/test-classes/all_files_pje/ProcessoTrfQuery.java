package br.com.infox.pje.query;

public interface ProcessoTrfQuery {

	/* Traz a primeira parte ativa ou passiva do processo que não seja advogado */
	String QUERY_PARAMETER_ID_PROCESSO_TRF = "idProcessoTrf";
	String QUERY_PARAMETER_PROCESSO_TRF = "processoTrf";
	String QUERY_PARAMETER_IN_PARTICIPACAO = "inParticipacao";
	String QUERY_PARAMETER_TIPO_PARTE_ADVOGADO = "tipoParteAdvogado";
	String GET_PESSOA_PARTE_BY_PROCESSO_TRF = "getPessoaParteByProcessoTrf";
	String GET_PESSOA_PARTE_BY_PROCESSO_TRF_QUERY = "select o from ProcessoParte o " + " where o.inParticipacao = :"
			+ QUERY_PARAMETER_IN_PARTICIPACAO + " and o.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF
			+ " and o.tipoParte != :" + QUERY_PARAMETER_TIPO_PARTE_ADVOGADO + " order by o.idProcessoParte";

	String GET_SITUACAO_PROCESSO_BY_PROCESSO_TRF_QUERY = "select o from SituacaoProcesso o " + "where o.idProcesso = :"
			+ QUERY_PARAMETER_ID_PROCESSO_TRF;
	/* Retorna os peritos do processo */
	String GET_PERITOS_BY_PROCESSO_TRF_QUERY = "select distinct o.pessoaPerito from ProcessoPericia o "
			+ "where o.processoTrf = :processoTrf";

	/* Traz as rpvs do processo não canceladas ou rejeitadas */
	String QUERY_PARAMETER_ID_RPV_STATUS_CANCELADA = "idRpvStatusCancelada";
	String QUERY_PARAMETER_ID_RPV_STATUS_REJEITADA = "idRpvStatusRejeitada";
	String GET_RPVS_BY_PROCESSO_TRF_QUERY = "select o from Rpv o " + " where o.processoTrf = :"
			+ QUERY_PARAMETER_PROCESSO_TRF + " and o.rpvStatus.idRpvStatus <> :"
			+ QUERY_PARAMETER_ID_RPV_STATUS_CANCELADA + " and o.rpvStatus.idRpvStatus <> :"
			+ QUERY_PARAMETER_ID_RPV_STATUS_REJEITADA;

	/* Traz uma lista de ProcessoParteRepresentante de uma parte do processo */
	String QUERY_PARAMETER_PESSOA = "pessoa";
	String LIST_REPRESENTANTE_BY_PESSOA_AND_PROCESSO_TRF_QUERY = " select ppr from ProcessoParteRepresentante ppr "
			+ " inner join ppr.parteRepresentante ppa2 " + " where ppa2.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF
			+ " and ppr.processoParte.pessoa = :" + QUERY_PARAMETER_PESSOA;

	/* Traz um processoParte especifico, filtrando por processo, pessoa e polo */
	String GET_PROCESSO_PARTE_BY_PESSOA_POLO_AND_PROCESSO_TRF_QUERY = "select o from ProcessoParte o "
			+ " where o.processoTrf = :" + QUERY_PARAMETER_PROCESSO_TRF + " and o.pessoa = :" + QUERY_PARAMETER_PESSOA
			+ " and o.inParticipacao = :" + QUERY_PARAMETER_IN_PARTICIPACAO;

	/* Traz a lista de autores ou reus de um processo */
	String LIST_PROCESSO_PARTE_PRINCIPAL_TRF_BY_PROCESSO_TRF_QUERY = "select ppa from ProcessoParte ppa "
			+ " where ppa.processoTrf =:" + QUERY_PARAMETER_PROCESSO_TRF + " and ppa.tipoParte != :"
			+ QUERY_PARAMETER_TIPO_PARTE_ADVOGADO + " and ppa.inParticipacao = :" + QUERY_PARAMETER_IN_PARTICIPACAO
			+ " " + " and ppa not in " + "(select distinct ppa2 from ProcessoParteRepresentante ppr "
			+ "inner join ppr.parteRepresentante ppa2 " + "where ppa2 = ppa) " + "order by ppa.pessoa.nome";

	String IS_PROCESSO_APTO_PARA_SESSAO = "select CASE o.selecionadoPauta " +
										  "			WHEN 'S' THEN o.selecionadoPauta" +
										  "			WHEN 'N' THEN o.selecionadoJulgamento" +
										  "		  END " +
										  "from ProcessoTrf o " +
										  "where o = :"+QUERY_PARAMETER_PROCESSO_TRF;
	
	/* Retorna o processo pelo numero */
	String QUERY_PARAMETER_NUMERO = "numeroProcesso";
	String GET_PROCESSO_BY_NUMERO = "from ProcessoTrf o where o.processo.numeroProcesso =" + QUERY_PARAMETER_NUMERO;
	
    String QUERY_PARAMETER_PROCESSO = "processo";
    String QUERY_PARAMETER_EVENTO = "evento";
        
    /**
     * [PJEII-4329] Criado para verificar se o processo está concluso
     */
    String GET_PROCESSO_EVENTO_CONCLUSO_QUERY = "select count(o) from ProcessoEvento o "
            + "where o.processo = :" + QUERY_PARAMETER_PROCESSO + " and "
            + "(o.evento = :" + QUERY_PARAMETER_EVENTO + " or o.evento.eventoSuperior = :" + QUERY_PARAMETER_EVENTO+ ")";
}
