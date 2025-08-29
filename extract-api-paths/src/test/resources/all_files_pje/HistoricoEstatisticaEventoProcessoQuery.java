package br.com.infox.pje.query;

public interface HistoricoEstatisticaEventoProcessoQuery {

	String QUERY_PARAMETER_COD_ESTADO = "estado";

	String LIST_SECAO_NAO_ATUALIZADA_QUERY = "select heep.secaoJudiciaria.cdSecaoJudiciaria, to_char(heep.dtUltimaAtualizacao,'dd/MM/yyyy') "
			+ "from HistoricoEstatisticaEventoProcesso heep "
			+ "where heep.dtUltimaAtualizacao < current_date()-1"
			+ "order by heep.secaoJudiciaria.cdSecaoJudiciaria";

	String DT_ULTIMA_ATUALIZACAO_SESSAO_QUERY = "select to_char(o.dtUltimaAtualizacao,'dd/MM/yyyy') "
			+ "from HistoricoEstatisticaEventoProcesso o " + "where o.secaoJudiciaria.cdSecaoJudiciaria = :"
			+ QUERY_PARAMETER_COD_ESTADO + " and o.dtUltimaAtualizacao < current_date()-1"
			+ "order by o.secaoJudiciaria.cdSecaoJudiciaria";
}