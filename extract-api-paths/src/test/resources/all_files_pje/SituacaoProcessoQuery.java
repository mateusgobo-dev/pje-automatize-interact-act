package br.com.infox.pje.query;

public interface SituacaoProcessoQuery {

	String QUERY_PARAM_NOME_FLUXO = "nomeFluxo";
	String QUERY_PARAM_NOME_TAREFA = "nomeTarefa";
	String QUERY_PARAM_ID_TAREFA = "idTarefa";
	String QUERY_PARAM_ID_PROCESSO = "idProcesso";

	String COUNT_BY_FLUXO_TAREFA_QUERY = "select count(o) from SituacaoProcesso o where " + "o.nomeFluxo like :"
			+ QUERY_PARAM_NOME_FLUXO + " and " + "o.nomeTarefa like :" + QUERY_PARAM_NOME_TAREFA;

	String LIST_PROCESSOS_BY_TAREFA_DOCUMENTO_QUERY = "select s.idProcesso from SituacaoProcesso s " + "where s.idTarefa = :"
			+ QUERY_PARAM_ID_TAREFA + " and " + "not exists " + "(select pd from ProcessoDocumento pd "
			+ "inner join pd.processo p " + "where p.idProcesso = s.idProcesso and "
			+ "pd.dataInclusao > s.dataChegadaTarefa)";
	
	String LIST_PROCESSOS_BY_TAREFA_QUERY = "select s.idProcesso from SituacaoProcesso s " + "where s.idTarefa = :"
			+ QUERY_PARAM_ID_TAREFA;
	
	String SITUACAO_BY_PROCESSO_QUERY = "select s from SituacaoProcesso s " + "where s.idProcesso = :" + QUERY_PARAM_ID_PROCESSO;
}
