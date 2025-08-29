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
package br.jus.pje.nucleo;

/**
 * Classe definidora de constantes a serem utilizadas para sinalização de eventos
 * dentro do padrão EVENTO-OBSERVADOR.
 * 
 * @author CNJ
 *
 */
public class Eventos {
	
	/**
	 * Evento a ser utilizado para apontar a ocorrência de agendamento de serviços
	 * periódicos
	 */
	public static final String AGENDA_SERVICOS = "pje:eventos:agendaServicosPeriodicos";
	
	/**
	 * Evento a ser utilizado unicamente quando houver uma persistência inicial de uma 
	 * conexão de processos.
	 * 
	 */
	public static final String CONEXAO_PROCESSUAL_CRIADA = "pje:processo:conexao:criada";
	
	/**
	 * Evento a ser utilizado quando do início de uma operação de redistribuição ampla que 
	 * justifique a utilização de semáforos para evitar interações dos usuários até seu 
	 * término.
	 */
	public static final String REDISTRIBUIR_PROCESSO = "pje:eventos:redistribuirProcesso";

	public static final String EVENTO_PRECLUSAO_MANIFESTACAO = "processoJudicialService:preclusaoManifestacao";
	public static final String EVENTO_ESTOURO_PRAZO = "processoJudicialService:estouroPrazo";
	
	public static final String EVENTO_CIENCIA_DADA = "processoJudicialService:cienciaDada";
	public static final String EVENTO_EXPEDIENTE_FECHADO = "processoJudicialService:expedienteFechado";
	
	public static final String EVENTO_PUBLICACAO_DJE = "pje:eventos:publicacaoDJE";
	public static final String EVENTO_DISPONIBILIZACAO_DJE = "pje:eventos:disponibilizacaoDJE";

	public static final String EVENTO_ENCERRAMENTO_PRAZO_NAO_PROCESSUAL = "pje:eventos:prazoNaoProcessual:encerrado";

	/**
	 * Evento a ser utilizado quando a data da audiência chegar
	 */
	public static final String EVENTO_DATA_AUDIENCIA = "processoJudicialService:dataAudiencia";

	/**
	 * Evento a ser deflagrado quando necessária a realização de uma reindexação completa dos
	 * processos do sistema.
	 * 
	 */
	public static final String REINDEXAR_PROCESSOS = "pje:eventos:indexacao:reindexar:processos";
	
	/**
	 * Variável de Evento a ser utilizada para armazenar informações de eventos que 
	 * dispararam alguma sinalizacao.
	 * 
	 */
	public static final String EVENTO_SINALIZACAO = "pje:evento:sinalizacao:processos";
	

	/**
	 * Evento a ser deflagrado quando necessária a realização de uma reindexação completa dos
	 * documentos do sistema.
	 */
	public static final String REINDEXAR_DOCUMENTOS = "pje:eventos:indexacao:reindexar:documentos";

    /**
     * Evento a ser deflagrado quando necessária a realização de uma reindexação completa dos
     * documentos de identificação do sistema.
     */
    public static final String REINDEXAR_DOCUMENTOS_IDENTIFICACAO = "pje:eventos:indexacao:reindexar:documentosidentificacao";


    public static final String SELECIONADA_TAREFA = "selectedTarefasTree";

	public static final String ALTERACAO_PROCURADORIA_PARTE = "pje:eventos:alteracaoProcuradoriaParte";

	public static final String ALTERACAO_PROCURADORIA_PESSOA = "pje:eventos:alteracaoProcuradoriaPessoa";

	public static final String SELECIONADA_CAIXA_DE_TAREFA = "selectedTarefasTreeCaixa";

	public static final String INICIAR_FLUXO_PETICAO_INCIDENTAL = "pje:eventos:incidental:iniciarFluxoIncidental";
	
	public static final String INICIAR_FLUXO_PETICAO_INCIDENTAL_COM_VARIAVEIS_EXTRAS = "pje:eventos:incidental:iniciarFluxoIncidental:variaveisExtras";

	public static final String INICIAR_FLUXO_NOTIFICAR_HABILITACAO_AUTOS = "pje:fluxo:notificarHabilitacaoAutos";

	public static final String REINDEXAR_MOVIMENTOS = "pje:eventos:indexacao:reindexar:movimentos";

	public static final String REINDEXAR_VOTOSCOLEGIADO = "pje:eventos:indexacao:votoscolegiado";
	
	public static final String HISTORICO_MOVIMENTACAO_CAIXA_PROCURADORIA = "pje:eventos:caixa:registrarHistoricoMovimentacao";
	
	public static final String HISTORICO_MOVIMENTACAO_CAIXA_PROCURADORIA_EXPEDIENTE = "pje:eventos:caixa:registrarHistoricoMovimentacaoExpediente";

	public static final String EVENTO_PREVENCAO_EXTERNA = "processoJudicialService:prevencaoExterna";
	
	public static final String EVENTO_MOVIMENTACAO_LOTE = "pje:eventos:atividadeLote:movimentacao";

	/**
	 * Evento a ser lançado quando houver alteração de conteúdo de processos ou expedientes ou de caixas no painel do usuário externo
	 */
	public static final String EVENTO_ALTERACAO_CONTEUDO_PAINEL_EXTERNO = "pje:eventos:painelExterno:alteracaoConteudo";
	
	public static final String EVENTO_ATUALIZAR_CAIXAS_PROCURADORES = "pje:eventos:atualizar:caixas:procuradores";
	
	public static final String EVENTO_PROCESSINSTANCE_FINALIZADA = "pje:eventos:processInstance:finalizada";
	
	public static final String EVENTO_ACORDAO_GERADO = "processoJudicialService:acordaoGerado";
	public static final String EVENTO_ENCERRA_SESSAO = "processoJudicialService:encerraSessao";
	public static final String EVENTO_PROCESSO_JULGADO_COLEGIADO = "pje:colegiado:processo:julgado";
	public static final String EVENTO_DECISAO_VOGAL_COLEGIADA_LIBERADA = "pje:colegiado:voto:vogal:liberado";
	
	public static final String EVENTO_ATUALIZAR_CADASTRO_SSO_USUARIO = "pje:eventos:atualizar:cadastro:sso:usuario";
	
	public static final String EVENTO_AUTORIDADES_CERTIFICADORAS_ALTERADAS = "pje:cloud:autoridadesCertificadoras:atualizado";

	public static final String EVENTO_ATUALIZAR_GUIA_RECOLHIMENTO_POS_PROTOCOLAR = "pje:eventos:atualizar:guiaRecolhimento:posProtocolar";

	public static final String EVENTO_ATUALIZAR_GUIA_RECOLHIMENTO_POS_JUNTADA = "pje:eventos:atualizar:guiaRecolhimento:posJuntada";

	public static final String EVENTO_LOGIN_SSO_REGISTRAR = "pje:eventos:login:sso:registrar";

	public static final String EVENTO_USUARIO_DADOS_LOGIN = "pje:usuario:dadoslogin";
	public static final String EVENTO_REMOVER_DOCUMENTOS_SESSAO_NAO_JULGADO = "pje:remover:documentos:sessao:naoJulgado";

	public static final String EVENTO_MOVIMENTAR_PROCESSO = "pje:evento:movimentarProcesso";

	public static final String EVENTO_DOMICILIO_ELETRONICO_ENVIAR_EXPEDIENTE = "pje:evento:domicilioEletronico:enviarExpediente";
	public static final String EVENTO_DOMICILIO_ELETRONICO_ENVIAR_EXPEDIENTES = "pje:evento:domicilioEletronico:enviarExpedientes";
	public static final String EVENTO_DOMICILIO_ELETRONICO_CITACAO_EXPIRADA = "pje:evento:domicilioEletronico:citacaoExpirada";

	public static final String EVENTO_DOMICILIO_ELETRONICO_ALTERAR_REPRESENTANTE = "pje:evento:domicilioEletronico:alterarRepresentante";
	public static final String EVENTO_SINALIZAR_FLUXO = "processoJudicialService:sinalizarFluxo";
	public static final String EVENTO_RESPOSTA_EXPEDIENTE = "pje:eventos:processoDocumento:respostaExpediente";
	public static final String EVENTO_ALTERACAO_PROCESSO_PARTE = "pje:eventos:processoParte:alteracao";
	public static final String EVENTO_ALTERACAO_PROCESSO_PARTE_ASSINCRONO = "pje:eventos:processoParte:alteracao:async";
	public static final String EVENTO_REMOCAO_PROCESSO_PARTE = "pje:eventos:processoParte:remocao";
	public static final String EVENTO_REMOCAO_PROCESSO_PARTE_ASSINCRONO = "pje:eventos:processoParte:remocao:async";	
	public static final String EVENTO_NOTIFICAR_INTIMACAO = "pje:eventos:intimacaoInss:previdenciario:notificar";

	
	/**
	 * Evento para alteração de partes dos autos
	 */
	public static final String EVENTO_PESSOA_RETIRAR_VISUALIZACAO_SIGILOSO = "pje:evento:removeVisualizacaoSigiloso";
	public static final String EVENTO_PORTAL_PETICAO_RECEBIDA = "pje:eventos:portal:peticao:recebida";
}
