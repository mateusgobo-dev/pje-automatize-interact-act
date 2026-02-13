package br.com.infox.pje.query;

public interface ProcessoParteExpedienteQuery {

	String QUERY_PARAM_PROCESSO_EXPEDIENTE = "processoExpediente";
	String QUERY_PARAM_PROCESSO_TRF = "processoTrf";
	String QUERY_PARAM_PESSOA = "pessoa";
	String QUERY_PARAM_PESSOA_LOGADA = "pessoaLogada";
	String QUERY_PARAM_TIPO_REPRESENTANTE = "tipoRepresentante";
	String QUERY_PARAM_ID_PROCESSO_LIST = "idProcessoList";
	String QUERY_PARAM_TIPO_PD_SENTENCA = "tipoPDSentenca";
	String QUERY_PARAM_TIPO_PD_ACORDAO = "tipoPDAcordao";
	String QUERY_PARAM_DATA_ATUAL = "dataAtual";

//	String COUNT_PARTES_NAO_CIENTES_QUERY = "select count(o) from ProcessoParteExpediente o "
//			+ "where o.processoExpediente = :" + QUERY_PARAM_PROCESSO_EXPEDIENTE + " " + "and o.dtCienciaParte is null";
//
//	String COUNT_PARTES_INTIMACAO_AUTO_PENDENTE_QUERY = "select count(o) from ProcessoParteExpediente o "
//			+ "where o.pendencia is not null " + "and o.processoExpediente.meioExpedicaoExpediente = 'E' "
//			+ "and o.processoJudicial = :" + QUERY_PARAM_PROCESSO_TRF;
//
//	String LIST_PARTES_INTIMACAO_AUTO_PENDENTE_QUERY = "select o from ProcessoParteExpediente o "
//			+ "where o.pendencia is not null " + "and o.processoExpediente.meioExpedicaoExpediente = 'E' "
//			+ "and o.processoJudicial.idProcessoTrf = :" + QUERY_PARAM_PROCESSO_TRF;
//
//	String COUNT_EXPEDIENTE_ENTIDADE_INTIMACAO_QUERY = "select count(o) from ProcessoParteExpediente o "
//			+ "inner join o.processoExpediente pe " + "inner join o.pessoaParte pp "
//			+ "inner join pp.pessoaProcuradoriaEntidadeList ent "
//			+ "inner join ent.pessoaProcuradorProcuradoriaList pppList " + "where pe.processoTrf = :"
//			+ QUERY_PARAM_PROCESSO_TRF + " and " + "o.dtCienciaParte != null and " + "(pp = :" + QUERY_PARAM_PESSOA
//			+ " or pp in (select o.pessoaProcuradoriaEntidade.pessoa from "
//			+ "           PessoaProcuradorProcuradoria o where o.pessoaProcurador = :" + QUERY_PARAM_PESSOA + "))";
//
//	String COUNT_ADVOGADO_INTIMADO_EXPEDIENTE_QUERY = "select count(ppe) from ProcessoParteExpediente ppe "
//			+ "where ppe.processoJudicial = :" + QUERY_PARAM_PROCESSO_TRF + " "
//			+ "	and ppe.dtCienciaParte != null " + "   and (ppe.pessoaParte = :" + QUERY_PARAM_PESSOA_LOGADA
//			+ "	or ppe.pessoaParte in " + "		(select pp " + "		 from ProcessoParte pp "
//			+ "		 inner join pp.processoParteRepresentanteList ppList "
//			+ "		 inner join ppList.processoParteRepresentante ppr " + "		 where ppList.tipoRepresentante = :"
//			+ QUERY_PARAM_TIPO_REPRESENTANTE + " " + "		 	and ppr.pessoa = :" + QUERY_PARAM_PESSOA_LOGADA + ")) ";
//
//	String COUNT_EXPEDIENTE_NAO_FECHADO_QUERY = "select count(o) from ProcessoParteExpediente o "
//			+ "inner join o.processoExpediente pe " + "where o.fechado = false and " + "o.pendencia is null and "
//			+ "pe.processoTrf = :" + QUERY_PARAM_PROCESSO_TRF;

	String LIST_EXPEDIENTE_BY_ID_PROCESSO_LIST_QUERY = "select o from ProcessoParteExpediente o "
			+ "inner join o.processoExpediente pe " + "inner join pe.processoTrf pTrf " + "inner join pTrf.processo p "
			+ "inner join p.processoDocumentoList pdList " + "where pe.processoTrf.idProcessoTrf in (:"
			+ QUERY_PARAM_ID_PROCESSO_LIST + " ) and o.dtPrazoLegal < :" + QUERY_PARAM_DATA_ATUAL + " and "
			+ "o.dtCienciaParte != null and " + "pe.meioExpedicaoExpediente = 'E' and " + "exists "
			+ "(select procTrf from ProcessoTrf procTrf " + "inner join procTrf.processo proc "
			+ "inner join proc.processoDocumentoList procDocList " + "where procTrf = pTrf and "
			+ "(procDocList.tipoProcessoDocumento = :" + QUERY_PARAM_TIPO_PD_ACORDAO + " "
			+ " or procDocList.tipoProcessoDocumento = :" + QUERY_PARAM_TIPO_PD_SENTENCA + "))";

	String PROCESSO_PARTE_EXPEDIENTE_COM_DOCUMENTO_QUERY = "select o from ProcessoParteExpediente o "
			+ "where o.processoJudicial = :" + QUERY_PARAM_PROCESSO_TRF
			+ "and o.processoExpediente.inTemporario is false "
			+ "and o.processoExpediente.dtExclusao is null "
			+ "and o.pendencia is null "
			+ "and	not exists (select ppe from ProcessoParteExpediente ppe " + "inner join ppe.processoExpediente pe "
			+ "inner join pe.processoDocumentoExpedienteList pdList  " + "inner join pdList.processoDocumento pd "
			+ "inner join pd.processoDocumentoBin pdBin "
			+ "where (pdBin.signature is null or pdBin.signature = '') and "
			+ "(pdBin.certChain is null or pdBin.certChain = '') and " + "pdList.anexo = false and ppe = o) "
			+ "order by o.processoExpediente.dtCriacao";
}