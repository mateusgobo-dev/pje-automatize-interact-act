package br.jus.cnj.pje.nucleo.view;

import org.json.JSONObject;

import br.jus.cnj.pje.nucleo.PJeBusinessException;

/**
 * Interface para os controllers que implementarão os callbacks necessários para
 * o uso da taglib pje:ckEditor, nos casos em que o conteúdo se tornará um
 * processo documento. Aqui está todo o contrato necessário pelos plugins
 * criados para atender ao PJE no ckEditor.
 * 
 * @author eduardo.pereira@tse.jus.br
 *
 */

public interface ICkEditorGeraDocumentoController extends ICkEditorController {

	/**
	 * Verifica se o tipo do processo documento que está em composição foi
	 * definido
	 * 
	 * @return true se sim e false caso contrario
	 */
	boolean isTipoProcessoDocumentoDefinido();

	/**
	 * Verifica se o processo documento já foi salvo na base de dados.
	 * 
	 * @return true se sim e false caso contrario
	 */
	boolean isDocumentoPersistido();

	/**
	 * Define o tipo de processo documento.
	 * 
	 * @param tipoDocumentoString
	 */
	void selecionarTipoProcessoDocumento(String tipoDocumentoString);

	/**
	 * Recupera os tipos de documento disponiveis em uma tarefa jbpm, buscando o
	 * valor da variavel Variaveis.VARIAVEL_IDS_TIPOS_DOCUMENTOS_FLUXO tanto no
	 * contexto da tarefa quanto no contexto do fluxo
	 * 
	 * @return String com array JSON contendo os tipos de documento
	 */
	String getTiposDocumentosDisponiveis();

	/**
	 * Recupera os ids dos modelos de documentos configurados na variável de
	 * tarefa Variaveis.VARIAVEL_IDS_MODELOS_DOCUMENTOS_FLUXO
	 * 
	 * @return os ids dos modelos de documento.
	 */
	Integer[] obterIdsModeloDocumentoFluxo();

	/**
	 * Recupera uma lista de modelos de documento de acordo com o tipo de documento selecionado

	 * @return String contendo o array JSON com a lista de modelos.
	 */
	String getModelosPorTipoDocumentoSelecionado();

	/**
	 * Recupera o modelo de documento selecionado. Utilizado pelos plugins de
	 * pesquisa documental e definição de tipo e modelo de documento.
	 * 
	 * @param modeloDocumento
	 * @return String com o conteudo do modelo de acordo com o tipo e nome do
	 *         modelo informado.
	 */
	String recuperarModeloDocumento(String modeloDocumento);
	
	/**
	 * Verifica se o documento atual tem assinatura
	 * 
	 * @return true se sim , false caso contrario
	 */
	boolean isDocumentoAssinado() throws PJeBusinessException;
	
	/**
	 * Deve remover a lista de assinaturas do documento principal.
	 */
	void removerAssinatura();
	
	/**
	 * Recupera o tipo de um documento principal que já esteja ligado ao Backend
	 * 
	 * @return String representando tipo de documento.
	 */
	String getNomeTipoDocumentoPrincipal();
	
	/**
	 * Descarta o documento reiniciando o estado da controller e da tarefa em que se encontra o processo. 
	 */
	void descartarDocumento() throws PJeBusinessException;
	
	/**
	 * Operação de consulta de documentos indexados para o editor CKEditor.
	 * 
	 * @param filtros
	 * 
	 * @return {@link String} representando {@link JSONObject} com as informações de resposta.
	 */
	String consultarDocumentosIndexados(String filtros);
	
	/**
	 * Consulta de localizacoes para o auto-complete do Awsomeplete
	 * @param filtro
	 * @return {@link String} representando {@link JSONObject} com as informações de resposta.
	 */
	String consultarOrgaosJulgadores(String filtro);
	
	/**
	 * Consulta de tipo de documento para o auto-complete do Awsomeplete
	 * @param filtro
	 * @return {@link String} representando {@link JSONObject} com as informações de resposta.
	 */
	String consultarTipoDocumento(String filtro);
	
	/**
	 * Consulta de tipo de documento para o auto-complete do Awsomeplete
	 * @param filtro
	 * @return {@link String} representando {@link JSONObject} com as informações de resposta.
	 */
	String consultarPessoaAutorDocumento(String filtro);
	
	/**
	 * Operação que recupera o conteúdo de um documento a partir de seu Id para apresentação no CK Editor.
	 * 
	 * @param idDocumento
	 * @return
	 */
	String consultarConteudoDocumento(Integer idDocumento);
	
	/**
	 * Operação que avalia se a consulta de documentos indexados está habilitada.
	 * 
	 * @return {@link Boolean} para indicar funcionamento da consulta de documentos indexados.
	 */
	Boolean isConsultaDocumentosIndexadosHabilitada();
	
	/**
	 * * Consulta das versões de um documento com paginação
	 * {@link String} representando {@link JSONObject} com as informações de
	 * resposta.
	 */
	String obterVersoesDocumentoJSON();
	
	/**
	 * Retorna JSON com conteúdo do documento aberto em base64
	 */
	String obterConteudoDocumentoAtual();
	
	/**
	 * * Aplica versão de um documento.
	 */
	void aplicarVersaoDocumento(int versaoDocumento);
	
	/**
	 * Inicializa as variáveis de paginação
	 */
	void inicializarPaginacao();
	
	/**
	 * * Consulta das versoes de um documento * @param filtro * @return
	 * {@link String} representando {@link JSONObject} com as informações de
	 * resposta.
	 */
	String obterVersoesDocumentoJSONProximas();

	/**
	 * Retorna JSON com os tipos de votos disponíveis
	 */
	String obterTiposVoto();
	
	/**
	 * Informa se é permitido assinar, habilitando ou não o plugin de assinatura
	 * 
	 * @return boolean
	 */
	public boolean podeAssinar();
	
	/**
	 * Recupera os tipo de abas disponiveis para utilização na widget
	 * 
	 * @return String com array JSON contendo as abas
	 */
	String getAbasWidget();
	
	/**
	 * Selecionar os documentos anexos
	 */
	void selecionarAnexosConcluido();
}

