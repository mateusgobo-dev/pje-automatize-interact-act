package br.com.infox.pje.query;

public interface EstatisticaProcessoJusticaFederalQuery {

	String QUERY_PARAMETER_ORGAO_JULGADOR = "orgaoJulgador";
	String QUERY_PARAMETER_PROCESSO = "processo";
	String QUERY_PARAMETER_DATA_INCIO = "dataInicio";
	String QUERY_PARAMETER_DATA_FIM = "dataFim";
	String QUERY_PARAMETER_SECAO = "secao";
	String QUERY_PARAMETER_PESSOA = "pessoa";
	String QUERY_PARAMETER_EVENTO1 = "evento1";
	String QUERY_PARAMETER_EVENTO2 = "evento2";
	String QUERY_PARAMETER_EVENTO3 = "evento3";
	String QUERY_PARAMETER_EVENTO4 = "evento4";
	String QUERY_PARAMETER_EVENTO5 = "evento5";

	String LIST_ORGAO_JULGADOR_SECAO_QUERY = "select oj from OrgaoJulgador oj "
			+ "where oj.jurisdicao IN (select j from Jurisdicao j inner join j.municipioList m "
			+ "where j = oj.jurisdicao and m.municipio.estado.codEstado = :" + QUERY_PARAMETER_SECAO
			+ ") order by oj.numeroVara";

	String LIST_COMPETENCIA_ORGAO_JULGADOR_QUERY = "select oj.competencia from OrgaoJulgadorCompetencia oj "
			+ "where oj.orgaoJulgador = :" + QUERY_PARAMETER_ORGAO_JULGADOR + " order by oj.competencia";

	String QTD_PROCESSOS_VARA_QUERY = "select count(distinct jf.processoTrf) from EstatisticaProcessoJusticaFederal jf "
			+ "inner join jf.processoTrf.processoParteList ppl " + "where jf.secaoJudiciaria = :"
			+ QUERY_PARAMETER_SECAO
			+ " and jf.processoTrf.orgaoJulgador = :"
			+ QUERY_PARAMETER_ORGAO_JULGADOR
			+ " and (ppl.pessoa = :"
			+ QUERY_PARAMETER_PESSOA
			+ " or :"
			+ QUERY_PARAMETER_PESSOA
			+ " is null) "
			+ "and (jf.processoTrf not in "
			+ "(select e.processoTrf from EstatisticaProcessoJusticaFederal e "
			+ "where e.processoTrf = jf.processoTrf and (e.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ " or e.codEvento = :"
			+ QUERY_PARAMETER_EVENTO2
			+ ")) "
			+ "or (select max(e1.dtEvento) from EstatisticaProcessoJusticaFederal e1 "
			+ "where e1.processoTrf = jf.processoTrf and (e1.codEvento = :"
			+ QUERY_PARAMETER_EVENTO1
			+ " or e1.codEvento = :"
			+ QUERY_PARAMETER_EVENTO2
			+ ")) "
			+ "<= "
			+ "(select max(e2.dtEvento) from EstatisticaProcessoJusticaFederal e2 "
			+ "where e2.processoTrf = jf.processoTrf and e2.codEvento = :"
			+ QUERY_PARAMETER_EVENTO3
			+ ")) "
			+ "and (jf.processoTrf not in "
			+ "(select e3.processoTrf from EstatisticaProcessoJusticaFederal e3 "
			+ "where e3.processoTrf = jf.processoTrf and (e3.codEvento = :"
			+ QUERY_PARAMETER_EVENTO4
			+ ")) "
			+ "or (select max(e4.dtEvento) from EstatisticaProcessoJusticaFederal e4 "
			+ "where e4.processoTrf = jf.processoTrf and (e4.codEvento = :"
			+ QUERY_PARAMETER_EVENTO4
			+ ")) "
			+ "<= "
			+ "(select max(e5.dtEvento) from EstatisticaProcessoJusticaFederal e5 "
			+ "where e5.processoTrf = jf.processoTrf and e5.codEvento = :" + QUERY_PARAMETER_EVENTO5 + ")) ";

	String PROCESSO_SEGREDO_JUSTICA_QUERY = "select o.processo from ProcessoVisibilidadeSegredo o "
			+ "where o.processo.idProcesso = :" + QUERY_PARAMETER_PROCESSO + " and o.pessoa.idUsuario = :"
			+ QUERY_PARAMETER_PESSOA;

	String PROCESSO_TEXTO_SIGILOSO_QUERY = "select o.processoDocumento.processo from ProcessoDocumentoVisibilidadeSegredo o "
			+ "where o.processoDocumento.processo.idProcesso = :"
			+ QUERY_PARAMETER_PROCESSO
			+ " and o.pessoa.idUsuario = :" + QUERY_PARAMETER_PESSOA;

	String LIST_USUARIO_VISIBILIDADE_SEGREDO_QUERY = "select o.pessoa from ProcessoVisibilidadeSegredo o "
			+ "where o.processo.idProcesso = :" + QUERY_PARAMETER_PROCESSO;

	String LIST_JUIZ_FEDERAL_POR_ORGAO_JULGADOR_QUERY = "select distinct o.pessoaMagistrado from EstatisticaProcessoJusticaFederal "
			+ "o where o.orgaoJulgador =:" + QUERY_PARAMETER_ORGAO_JULGADOR;
}