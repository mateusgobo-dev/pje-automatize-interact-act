package br.com.infox.pje.query;

public interface ProcessoExpedienteQuery {

	String QUERY_PARAM_PROCESSO_TRF = "processoTrf";

	String LIST_NAO_ENVIADOS_QUERY = "select o from ProcessoExpediente o " + "where not exists"
			+ "(select pde from ProcessoDocumentoExpediente pde "
			+ "where pde.processoDocumento.processoDocumentoBin.certChain is not null "
			+ "and pde.processoDocumento.processoDocumentoBin.signature is not null " + "and pde.anexo = false "
			+ "and pde.processoExpediente = o) " + "and o.processoTrf = :" + QUERY_PARAM_PROCESSO_TRF + " "
			+ "and o.dtExclusao is null";

	String COUNT_EXPEDIENTE_ATIVO_BY_PROCESSO_TRF_QUERY = "select count(o) from ProcessoExpediente o where "
			+ "o.dtExclusao is null and o.processoTrf = :" + QUERY_PARAM_PROCESSO_TRF;

}