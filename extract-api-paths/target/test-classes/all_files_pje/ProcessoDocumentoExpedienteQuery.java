package br.com.infox.pje.query;

public interface ProcessoDocumentoExpedienteQuery {

	String QUERY_PARAM_PROCESSO_DOCUMENTO_ATO = "processoDocumentoAto";
	String QUERY_PARAM_PROCESSO_EXPEDIENTE = "processoExpediente";
	String QUERY_PARAM_PROCESSO_DOCUMENTO = "processoDocumento";

	String GET_PROCESSO_DOCUMENTO_ATO_BY_EXPEDIENTE_QUERY = "select o.processoDocumentoAto from ProcessoDocumentoExpediente o "
			+ "where o.processoExpediente = :"
			+ QUERY_PARAM_PROCESSO_EXPEDIENTE
			+ " "
			+ "and o.processoDocumentoAto is not null";

	String COUNT_PROCESSO_DOCUMENTO_EXPEDIENTE_COM_ATO_QUERY = "select count(pde) from ProcessoDocumentoExpediente pde "
			+ "where exists (select pdpa from ProcessoDocumentoBinPessoaAssinatura pdpa "
			+ "where pdpa.processoDocumentoBin = pde.processoDocumento.processoDocumentoBin) "
			+ "  and pde.anexo = false " + "  and pde.processoDocumentoAto = :" + QUERY_PARAM_PROCESSO_DOCUMENTO;

	String GET_LISTA_PROCESSO_DOCUMENTO_VINCULADO_ATO_QUERY = "select pde.processoDocumento from ProcessoDocumentoExpediente pde "
			+ "where exists (select pdpa from ProcessoDocumentoBinPessoaAssinatura pdpa "
			+ "where pdpa.processoDocumentoBin = pde.processoDocumento.processoDocumentoBin) "
			+ "  and pde.anexo = false " + "  and pde.processoDocumentoAto = :" + QUERY_PARAM_PROCESSO_DOCUMENTO_ATO;
	
	String GET_LISTA_PROCESSO_DOCUMENTO_VINCULADO_EXPEDIENTE_QUERY = "select pde.processoDocumento from ProcessoDocumentoExpediente pde "
			+ "where exists (select pdpa from ProcessoDocumentoBinPessoaAssinatura pdpa "
			+ "where pdpa.processoDocumentoBin = pde.processoDocumento.processoDocumentoBin) "
			+ "  and pde.anexo = true " + " and pde.processoExpediente = :" + QUERY_PARAM_PROCESSO_EXPEDIENTE;

}