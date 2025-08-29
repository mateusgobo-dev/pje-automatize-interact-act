package br.com.infox.pje.query;

public interface RpvQuery {

	/* Parametros */
	String QUERY_PARAMETER_PROCESSO_TRF = "processoTrf";
	String QUERY_PARAMETER_ID_PROCESSO_TRF = "idProcessoTrf";
	String QUERY_PARAMETER_ID_BENEFICIARIO = "idBeneficiario";
	String QUERY_PARAMETER_ID_RPV_STATUS_CANCELADA = "idRpvStatusCancelada";
	String QUERY_PARAMETER_ID_RPV_STATUS_REJEITADA = "idRpvStatusRejeitada";
	String QUERY_PARAMETER_ID_RPV = "idRpv";
	String QUERY_PARAMETER_RPV = "rpv";
	String QUERY_PARAMETER_TIPO_PARTE_CESSIONARIO = "tipoParteCessionario";
	String QUERY_PARAMETER_CPF = "cpf";
	String QUERY_PARAMETER_UF = "uf";

	/*
	 * Retorna uma lista de rpv originárias do beneficiario do processo e que
	 * não estejam canceladas ou rejeitadasdo
	 */
	String LIST_RPV_ORIGINARIA_BY_RPV_QUERY = " select o from Rpv o " + " where o.processoTrf.idProcessoTrf = :"
			+ QUERY_PARAMETER_ID_PROCESSO_TRF + " and " + " o.idRpv <> :" + QUERY_PARAMETER_ID_RPV + " and "
			+ " o.inEspecieRequisicao = 'O'" + " and o.beneficiario.idUsuario = :" + QUERY_PARAMETER_ID_BENEFICIARIO
			+ " and o.rpvStatus.idRpvStatus <> :" + QUERY_PARAMETER_ID_RPV_STATUS_CANCELADA
			+ " and o.rpvStatus.idRpvStatus <> :" + QUERY_PARAMETER_ID_RPV_STATUS_REJEITADA;

	/*
	 * Retorna uma lista de rpv do beneficiario do processo que sejam
	 * parcial incontronversa e que não estejam canceladas ou
	 * rejeitadas
	 */
	String LIST_RPV_PARCIAL_BY_RPV_QUERY = " select o from Rpv o "
			+ " where o.processoTrf.idProcessoTrf = :" + QUERY_PARAMETER_ID_PROCESSO_TRF
			+ " and o.idRpv <> :" + QUERY_PARAMETER_ID_RPV + " and " + " o.inEspecieRequisicao = 'P' "
			+ " and o.beneficiario.idUsuario = :" + QUERY_PARAMETER_ID_BENEFICIARIO
			+ " and o.rpvStatus.idRpvStatus <> :" + QUERY_PARAMETER_ID_RPV_STATUS_CANCELADA
			+ " and o.rpvStatus.idRpvStatus <> :" + QUERY_PARAMETER_ID_RPV_STATUS_REJEITADA;

	/*
	 * Retorna uma lista de rpv do beneficiario do processo que sejam apenas
	 * ressarcimento de custas
	 */
	String LIST_RPV_RESSARCIMENTO_CUSTAS_BY_RPV_QUERY = " select o from Rpv o "
			+ " where o.processoTrf.idProcessoTrf = :" + QUERY_PARAMETER_ID_PROCESSO_TRF + " and " + " o.idRpv <> :"
			+ QUERY_PARAMETER_ID_RPV + " and " + " o.inRessarcimentoCustas = true"
			+ " and o.beneficiario.idUsuario = :" + QUERY_PARAMETER_ID_BENEFICIARIO
			+ " and o.rpvStatus.idRpvStatus <> :" + QUERY_PARAMETER_ID_RPV_STATUS_CANCELADA
			+ " and o.rpvStatus.idRpvStatus <> :" + QUERY_PARAMETER_ID_RPV_STATUS_REJEITADA;

	/* Retorna uma lista de RpvPessoaParte com os cessionarios da RPV */
	String LIST_CESSIONARIO_BY_RPV_QUERY = "select pp from RpvPessoaParte pp " + " where pp.rpv = :"
			+ QUERY_PARAMETER_RPV + " and pp.tipoParte = :" + QUERY_PARAMETER_TIPO_PARTE_CESSIONARIO;

	/* Retorna o status da oab */
	String STATUS_OAB_ADVOGADO_QUERY = "select o.situacaoInscricao from  DadosAdvogadoOAB o " + " where o.numCPF = :"
			+ QUERY_PARAMETER_CPF + " and o.uf = :" + QUERY_PARAMETER_UF + " order by o.idDadosAdvogadoOAB desc";

}
