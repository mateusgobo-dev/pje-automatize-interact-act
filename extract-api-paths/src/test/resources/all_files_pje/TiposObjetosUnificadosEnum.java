/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.nucleo.enums;

public enum TiposObjetosUnificadosEnum implements PJeEnum {

	LOG_ACESSO("log de registro de unificacao de log de acesso"),
	CARACTERISTICA_FISICA("log de registro de unificacao de caracteristicas fisicas"),
	MEIO_CONTATO_CADASTRADOS("log de registro de unificacao de meios de contato cadastrados pelas pessoas secundarias"),
	MEIO_CONTATO_PROPRIETARIA("log de registro de unificacao de meios de contato onde as pessoas secundarias sao as proprietarias"),
	NOMES_ALTERNATIVOS_CADASTRADOS("log de registro de unificacao de nomes alternativos cadastrados pelas pessoas secundarias"),
	NOMES_ALTERNATIVOS_PROPRIETARIA("log de registro de unificacao de nomes alternativos onde as pessoas secundarias sao as proprietarias"),
	CONEXOES_PREVENCAO("log de registro de unificacao de conexoes de prevencoes validadadas pelas pessoas secundarias"),
	SEGREDO_PROCESSO("log de registro de unificacao de solicitacoes de segredo em processos pelas pessoas secundarias"),
	SIGILO_PROCESSO_PARTE("log de registro de unificacao de cadastros de sigilo em processos partes pelas pessoas secundarias"),
	CAIXA_REPRESENTANTE("log de registro de unificacao de caixas de representantes das pessoas secundarias"),
	SESSAO_ENTE_EXTERNO("log de registro de unificacao de sessao ente externos das pessoas secundarias"),
	PROCESSO_REDISTRIBUICAO("log de registro de unificacao de redistribuicoes de processos pelas pessoas secundarias"),
	PROCESSO_PARTE_HISTORICO("log de registro de unificacao de alteraçoes em processos parte pelas pessoas secundarias"),
	PROCESSO_TAG("log de registro de unificacao de tags em processos criados/alterados pelas pessoas secundarias"),
	LEMBRETE("log de registro de unificacao de lembretes em processos criados/alterados pelas pessoas secundarias"),
	PERMISSAO_LEMBRETE("log de registro de unificacao de permissoes em lembretes de processos criados/alterados pelas pessoas secundarias"),
	PROCESSOS_PROTOCOLADOS("log de registro de unificacao de processos protocolados pelas pessoas secundarias"),
	PARAMETROS_ALTERADOS("log de registro de unificacao de processos protocolados pelas pessoas secundarias"),
	ENTITY_LOGS("log de registro de unificacao de registros de alterações em entidades realizadas pelas pessoas secundarias"),
	SOLICITACAO_NO_DESVIO("log de registro de solicitações para encaminhar processos para o nó de desvio realizadas pelas pessoas secundarias"),
	SESSAO_PAUTA_PROC_INCLUSORA("log de registro de inclusoes de processos em pauta realizadas pelas pessoas secundarias"),
	SESSAO_PAUTA_PROC_EXCLUSORA("log de registro de exclusoes de processos em pauta realizadas pelas pessoas secundarias"),
	SESSAO_INCLUSORA("log de registro de inclusoes de sessões realizadas pelas pessoas secundarias"),
	SESSAO_EXCLUSORA("log de registro de exclusoes de sessões realizadas pelas pessoas secundarias"),
	QUADRO_AVISO("log de registro de unificação de inclusões de avisos no quadro de aviso pelas pessoas secundarias"),
	PROCESSO_DOCUMENTO_FAVORITO("log de registro de unificação de marcações de documentos em processos como favoritos pelas pessoas secundarias"),
	NOTAS_SESSAO_JULG("log de registro de unificação de criações de notas em sessoes de julgamento pelas pessoas secundarias"),
	MODELOS_PROCLAMACAO_JULG("log de registro de unificação de criações de modelos de proclamacoes de julgamento pelas pessoas secundarias"),
	LOG_HIST_MOVIMENTACAO("log de registro de unificação de logs historicos de movimentacoes de julgamento pelas pessoas secundarias"),
	VISIBILIDADE_DOC_IDENTIFICACAO("log de registro de unificação de permissões de visibilidade de documentos de identificacao das pessoas secundarias");

	private String label;

	TiposObjetosUnificadosEnum(String label) {
		this.label = label;
	}

	@Override
	public String getLabel() {
		return this.label;
	}
}