/**
 * 
 */
package br.jus.cnj.pje.nucleo;

/**
 * Encerra constantes com os nomes dos parâmetros gravados no sistema.
 * 
 * @author cristof
 *
 */
public final class Parametros {
	
	public static final String ID_TIPO_PROCESSO_DOCUMENTO_PETICAO_INICIAL = "idTipoProcessoDocumentoPeticaoInicial";
	
	public static final String APLICACAOSISTEMA = "aplicacaoSistema";

	public static final String TIPOJUSTICA = "tipoJustica";
	
	public static final String TIPODOCUMENTOSENTENCA = "idTipoProcessoDocumentoSentenca";
	
	public static final String TIPODOCUMENTOACORDAO = "idTipoProcessoDocumentoAcordao";
	
	public static final String TIPODOCUMENTODESPACHO = "idTipoProcessoDocumentoDespacho";
	
	public static final String TIPODOCUMENTODECISAO = "idTipoProcessoDocumentoDecisao";
	
	public static final String TIPODOCUMENTOATOORDINATORIO = "idTipoDocumentoAtoOrdinatorio";
	
	public static final String LCR_TIMEOUT = "timeoutLCR";
	
	public static final String ESPERA_MAXIMA_SEM_PRAZO = "esperaMaximaSemPrazo";
	
	public static final String ID_TIPO_DOCUMENTO_INTIMACAO_PAUTA = "idTipoProcessoDocumentoIntimacaoPauta";

	public static final String ID_MODELO_DOCUMENTO_INTIMACAO_PAUTA = "idModeloIntimacaoPauta";
	
	public static final String TIPOPARTEADVOGADO = "idTipoParteAdvogado";
	
	public static final String USUARIO_LOCALIZACAO_ATUAL = "usuarioLogadoLocalizacaoAtual";
        
    public static final String ID_USUARIO_SISTEMA = "idUsuarioSistema";

    public static final String ID_PAPEL_ASSESSOR_CHEFE = "Asses";

    public static final String IDENTIFICADOR_PAPEL_ADMIN = "admin";
    
    public static final String IDENTIFICADOR_PAPEL_ADMINISTRADOR = "administrador";
    
    public static final String IDENTIFICADOR_PAPEL_CONCILIADOR = "conciliador";
    
    public static final String ID_PAPEL_SECRETARIO_SESSAO = "idSecretarioSessao";
    
    public static final String ID_PAPEL_ADVOGADO = "idPapelAdvogado";

	public static final String ID_PAPEL_PROCURADOR = "idPapelProcurador";
	
	public static final String ID_PAPEL_PROCURADOR_CHEFE = "idPapelProcuradorChefe";
	
	public static final String ID_PAPEL_ASSISTENTE_ADVOGADO = "idPapelAssistenteAdvogado";
	
	public static final String ID_PAPEL_ASSISTENTE_PROCURADORIA = "idPapelAssistenteProcuradoria";

	public static final String ID_TIPO_PROCESSO_DOCUMENTO_VOTO = "idTipoProcessoDocumentoVoto";
	
	public static final String ID_TIPO_PROCESSO_DOCUMENTO_RELATORIO = "idTipoProcessoDocumentoRelatorio";
	
	public static final String ID_TIPO_PROCESSO_DOCUMENTO_EMENTA = "idTipoProcessoDocumentoEmenta";
	
	public static final String ID_TIPO_PROCESSO_DOCUMENTO_INTEIRO_TEOR = "idTipoProcessoDocumentoInteiroTeor";
	
	public static final String ID_TIPO_PROCESSO_DOCUMENTO_ACORDAO = "idTipoProcessoDocumentoAcordao";

	public static final String ID_TIPO_VOTO_IMPEDIMENTO_SUSPEICAO = "idTipoVotoSuspeicao";	

	// [PJEII-4330] Padronização do parâmetro do identificador do TipoProcessoDocumento Notas Orais
	public static final String ID_TIPO_DOCUMENTO_NOTAS_ORAIS = "pje:tipoProcessoDocumento:notasOrais";
	
    // [PJEII-4330] Padronização do parâmetro do identificador do Modelo de Acordao
	public static final String ID_MODELO_DOCUMENTO_ACORDAO = "pje:modelo:acordao";

    // [PJEII-4330] Padronização do parâmetro do identificador do Modelo do Inteiro Teor
	public static final String ID_MODELO_DOCUMENTO_INTEIRO_TEOR = "pje:modelo:inteiroTeor";
	
	public static final String ID_MODELO_CADASTRO_SEM_TERMO_COMPROMISSO = "pje:modelo:id:cadastroSemTermoDeCompromisso";
	
	public static final String ID_MODELO_CADASTRO_ADVOGADO = "pje:modelo:id:cadastroAdvogado";

	public static final String ID_TIPO_DOCUMENTO_CADASTRO_ADVOGADO = "pje:documento:tipo:cadastroAdvogado";
	
	public static final String ID_MODELO_CADASTRO_JUSPOSTULANDI = "pje:modelo:id:cadastroJusPostulandi";

	public static final String ID_TIPO_DOCUMENTO_CADASTRO_JUSPOSTULANDI = "pje:documento:tipo:cadastroJusPostulandi";

	public static final String ID_PAPEL_JUSPOSTULANDI = "pje:papel:id:jusPostulandi";

	public static final String ID_PAPEL_PERITO = "pje:papel:id:perito";
	
	public static final String ID_PAPEL_OFICIAL_JUSTICA = "pje:papel:id:oficialJustica";
	
	public static final String ID_PAPEL_OFICIAL_JUSTICA_DISTRIBUIDOR = "pje:papel:id:oficialJusticaDistribuidor";
	
	public static final String ID_MINISTERIO_PUBLICO = "id_ministerio_publico";
	/**
	 * Nome do parâmetro que encerrará o código (não o identificador) do fluxo a ser iniciado 
	 * quando constatada possível prevenção.
	 */
	public static final String CODIGO_FLUXO_PREVENCAO = "pje:fluxo:prevencao:codigo";

	public static final String CODIGO_AGRUPAMENTO_CLASSES_REMESSA_STF = "pje:classe:agrupamento:remessaManifestacaoProcessual:stf:codigo";
	
	public static final String CODIGO_AGRUPAMENTO_ASSUNTO_REMESSA_STF = "pje:assunto:agrupamento:remessaManifestacaoProcessual:stf:codigo";
	
	public static final String ID_MANIFESTANTE_REMESSA_STF = "pje:remessaManifestacaoProcessual:stf:idManifestante";
	
	public static final String SENHA_MANIFESTANTE_REMESSA_STF = "pje:remessaManifestacaoProcessual:stf:senhaManifestante";
	
	public static final String WSDL_REMESSA_STF = "pje:remessaManifestacaoProcessual:stf:wsdl";
	
	public static final String REMESSA_MANIFESTACAO_PROCESSUAL_NOME_TAREFA = "pje:fluxo:remessaManifestacaoProcessual:nomeTarefa";
		
	public static final String ID_APLICACAO_CLASSE_ESPECIAL = "pje:aplicacaoClasse:especial:id";
	
	public static final String EXIBE_PUBLICACAO_RELACAO_JULGAMENTO = "pje:sessao:exibePublicacaoRelacaoJulgamento";
	
	/**
	 * Nome do parâmetro que define o tempo que será utilizado para remessa.
	 */
	public static final String TIMEOUT_REMESSA = "pje:remessa:timeoutEntregaManifestacao";
	
	/**
	 * Esta chave não deve ser utilizada para funcionalidades que persistem informações.
	 * O seu uso está restrito ás funcionalidades de geração de tokens temporários, como o utilizado na consulta pública.
	 */
	public static final String CHAVE_VOLATIL_CRIPTOGRAFIA = "pje:criptografia:chavevolatil"; 

	public static final String ID_VERIFICADOR_PERIODICO_TIMER_PARAMETER = "idVerificadorPeriodicoTimerParameter";

	public static final String ID_DESTINACAO_PESSOA_CIENCIA_PUBLICA = "pje:fluxo:publicacao:idDestinacaoPessoaCienciaPublica";
	
	public static final String CODIGO_FLUXO_DIGITALIZACAO = "pje:fluxo:digitalizacao:codigo";

	public static final String CODIGO_FLUXO_ASSINATURA_BNMP = "pje:fluxo:assinaturaBnmp:codigo";
	
	public static final String ELASTICSEARCHIDXNAME = "pje:elasticsearch:index:name";
	
	public static final String ELASTICSEARCHIDXURL = "pje:elasticsearch:index:url";
	
	public static final String ELASTIC_SEARCH_LOG_REQUEST = "pje:elasticsearch:log:request";
	
	public static final String VAR_PROCURADOR_ENTIDADE = "pje:procuradorEntidade";

	public static final String SEMPRE_DISPARAR_FLUXO_INCIDENTAL = "pje:fluxo:incidental:sempreDisparar";
	
	public static final String PJE_FLUXO_NOTIFICAR_HABILITACAO_AUTOS = "pje:fluxo:notificarHabilitacaoAutos";
	
	public static final String PJE_AGRUPADOR_DOCS_NAO_LIDOS_PAPEIS="pje:agrupador:docsNaoLidos:papeis";
	
	// Parâmetro responsável por armazenar as situações de julgamento (separadas por vírgula) que podem gerar certidões.
	public static final String PJE_LISTA_SITUACAO_JULGAMENTO="pje:lista:situacaoJulgamento";
	
	public static final String PJE_HABILITAR_CARGO_VINCULACAO_REGIMENTAL="pje:habilitarCargoVinculacaoRegimental";
	
	// código do fluxo que será iniciado quando um processo for colocado em julgamento na sessão.
	public static final String PARAMETRO_FLUXO_PROCESSO_EM_JULGAMENTO = "pje:fluxo:sessao:processoEmJulgamento";
	
	// Indica se o botão para assinar relatório no arquivo votacaoColegiado.xhtml será ou não exibido (true ou false).
	public static final String PJE_FLUXO_OCULTAR_BOTAO_ASSINAR_RELATORIO="pje:fluxo:votacaoColegiado:ocultarBotaoAssinarRelatorio";

	// Indica se no fluxo de redistribuicao o presidente pode ser ou nao o relator do processo no momento da redistribuicao (true ou false).
	public static final String PRESIDENTE_RELATOR_PROCESSO_EM_REDISTRIBUICAO="pje:redistribuicao:presidenteRelatorProcessoEmRedistribuicao";
		
	/**
	 *  Parâmetro responsável por armanezar o ID do(s) tipo(s) de documento(s) que será(ão) utilizado(s) na criação do documento de voto.
	 */
	public static final String IDS_TIPOS_VOTO = "pje:flx:votacaoVogal:tiposVoto:ids";
	
	/**
	 *  Parâmetro responsável por armanezar o ID do(s) tipo(s) de documento(s) que será(ão) utilizado(s) na criação do documento de voto do vogal no painel do magistrado na sessão.
	 */
	public static final String IDS_TIPOS_VOTO_VOGAL_PAINEL_MAGISTRADO = "pje:painel:magistrado:sessao:tiposVotoVogal:ids";
	
	/**
	 *  Parâmetro responsável por indicar se o fluxo de derrubada de voto deve ser finalizado automaticamente
	 *  após alteração de voto.
	 */
	public static final String PJE_FLUXO_VOTO_DERRUBADO_EXCLUI_TAREFA_AO_VOTAR = "pje:fluxo:votoDerrubado:finalizarTarefaAoVotar";	
	
	public static final String BLOQUEAR_PROCESSO_REMETIDO = "pje:remessa:bloquearProcessoRemetido";
	
	public static final String PESO_MINIMO_CLASSE_JUDICIAL = "valorPesoClasseJudicialMin";
	public static final String PESO_MAXIMO_CLASSE_JUDICIAL = "valorPesoClasseJudicialMax";
	public static final String PESO_MINIMO_CLASSE_JUDICIAL_INCIDENTAL = "valorPesoClasseJudicialIncidentalMin";

	// Indica se o sistema utiliza base binária unificada entre o 1g e 2g.
	public static final String UTILIZAR_BASE_BINARIA_UNIFICADA="pje:remessa:utilizarBaseBinariaUnificada";
	
	//Indica se a expressão da DistribuicaoService ira validar ou não se existe uma instancia aberta
	public static final String VALIDAR_TASK_INSTANCE = "isValidarTaskInstanceOpen";
	
	/**
	 * Parâmetro responsável por armazenar o ID da pessoa configurada como Fiscal da Lei
	 */
	public static final String PJE_FISCAL_DA_LEI = "pje:FiscalDaLei";

	/**
	 * Nome do tipo de parte que é referente ao fiscal da lei
	 */
	public static final String VAR_TIPO_PARTE_FISCAL_LEI = "pje:FiscalDaLei:tipoParte";

	public static final String PJE_VISIBILIDADE_DOCUMENTO_SESSAO_ASSINATURA = "pje:sessao:plenarioVirtual:documentoAssinado";

	public static final String PJE_SESSAO_NAO_EXIBE_VOTACAO_ANTECIPADA_RELACAO_JULGAMENTO = "pje:sessao:ocultar:VotacaoAntecipadaRelacaoJulgamento";
	
	public static final String NOME_MODELO_CERTIDAO_PROTOCOLO = "pje:protocolo:certidao:modelo";
	
	/**
	 * Parâmetro com os ips permitidos para acessar os serviços REST restritos
	 */
	public static final String IPS_PERMITIDOS_PJE2 = "pje:integracao:ips-pje2";


	public static final String CAPTCHA_HABILITADO = "pje:captcha:habilitado";
	
	public static final String OCULTAR_VOTOS_ANTECIPADOS_NAO_MAGISTRADO = "pje:sessao:ocultarVotosAntecipadosNaoMagistrado";

	public static final String JNDINAMEJMSCONNECTION = "pje:jms:jndiConexao";
	
	public static final String LISTA_AGRUPAMENTO_PREVENCAO_260 = "listaAgrupamentosPrevencao260JE";
	
	public static final String ELEITORAL_ATIVA_COMPENSACAO = "pje:eleitoral:prevencao:habilita:compensacao";
	
	public static final String QUANTIDADE_MAXIMA_VERSOES_DOCUMENTO = "pje:documento:quantidadeMaximaVersoesDocumento";

	public static final String SIGLA_TRIBUNAL = "siglaTribunal";
	
	public static final String PRESUNCAO_ENTREGA_CORRESPONDENCIA = "presuncaoEntregaCorrespondencia";
	
	public static final String PJE_SISTEMA_EDITOR = "pje:sistema:editor";
	
	public static final String PJE_SISTEMA_TIPO_MODELO_DOCUMENTO_CKEDITOR = "pje:sistema:tipoModeloDocumento:ckEditor";
	
	/**
	 * Indica a URL ([esquema://]dominio[:porta]) que será utilizada pela usuário para acessar os links gerados pelo sistema.
	 */
	public static final String PJE_PREFIXO_URL_EXTERNA = "pje:prefixoUrlExterna";
	
	public static final String URL_SERVICO_MURAL = "pje:servico:mural";
	
	public static final String URL_PJE_JE_SERVICO_WSDL = "PJE_JE_SERVICOS_WSDL";
	
	public static final String PRAZO_PUBLICACAO_SESSAO_MURAL = "pje:prazoPublicacaoSessaoEmMural";
	
	public static final String TIPO_PRAZO_PUBLICACAO_SESSAO_MURAL = "pje:tipoPrazoPublicacaoSessaoEmMural";

	public static final String ID_TIPO_DOCUMENTO_CERTIDAO_JULGAMENTO = "pje:tipoDocumento:idTipoDocumentoCertidaoJulgamento";

	public static final String MOSTRAR_NOME_MAGISTRADO_LABEL_PLACAR = "pje:sessao:placar:mostrarNomeMagistradoLabelPlacar";
	
	public static final String ATRIBUTO_ORDENADOR_SESSAO_PAUTA_PROC_COMPOSICAO = "pje:sessao:pauta:composicao:atributoOrdenador";	
	
	public static final String RECAPTCHA_ATIVO = "reCaptchaAtivo";
	
	public static final String RECAPTCHA_SECRET_KEY = "reCaptchaSecretKey";
	
	public static final String RECAPTCHA_SITE_KEY = "reCaptchaSiteKey";
	
	public static final String IDS_ORGAOS_JULGADORES_PERMITIDOS_VISUALIZAR_ULTIMA_DISTRIBUICAO = "pje:ids_orgao_julgadores_visualizacao_ultima_distribuicao";
	
	public static final String ID_MODELO_DOCUMENTO_CANCELAMENTO_PAUTA = "pje:sistema:modeloDocumento:cancelamentoPauta";
	
	public static final String PERMITE_INCLUIR_PROCESSO = "pje:relacaoJulgamento:permiteIncluirProcesso";


	public static final String ID_OJ_PLANTAO = "idOjPlantao";
	
	public static final String HORA_INICIO_PLANTAO = "horaInicioPlantao";
	
	public static final String HORA_TERMINO_PLANTAO = "horaTerminoPlantao";
	
	public static final String VALIDA_DIA_ANTERIOR_POSTERIOR = "pje:plantaoJudicial:validaDiaAnteriorPosterior";
	
	public static final String MODO_MARCACAO_AUTOMATICA = "pje:modoMarcacaoAutomatica";

	public static final String PJE_PAINEL_MAGISTRADO_SESSAO_NOVO = "pje:painel:magistrado:sessao:novo";

	public static final String URL_PJE_CLIENTE = "pje:pje2:cliente:urlPjeCliente";
	
	public static final String PJE_SESSAO_HABILITAR_ACOES_EM_VOTACAO_ANTECIPADA = "pje:sessao:habilitarAcoesEmVotacaoAntecipada";
		
	public static final String REGISTRA_PJE_LEGACY_NUVEM = "pje:cloud:registraPjeLegacyNuvem";
	
	public static final String URL_API_GATEWAY = "pje:cloud:urlApiGateway";

	public static final String NOME_SISTEMA = "nomeSistema";
	
	public static final String NOME_SECAO_JUDICIARIA = "nomeSecaoJudiciaria";
	
	public static final String LOGO_TRIBUNAL = "imgLogo";
	
	public static final String NUMERO_ORGAO_JUSTICA = "numeroOrgaoJustica";
	
	public static final String NUMERO_INSTANCIA = "pje.nomeInstancia";
	
	public static final String PJE_MNI_LIMITE_QTD_BINARIO_MNI = "pje:mni:consultarProcesso:limite:quantidade:documentos:binarios";
	
	public static final String PJE_LOCALIZACAO_TRIBUNAL = "idLocalizacaoTribunal";

	/**
	 * Usar o parâmetro de sistema com este formato:
	 * {'ssl': 'false', 'debug': 'false', 'host':'<HOST>', 'password': '<senha>', 'port': '465', 'username': '<username>'}
	 */
	public static final String CONFIGURACAO_SMTP = "pje:mail";
	
	public static final String TEMPO_MINIMO_AUDIENCIA = "tempoMinimoAudiencia";
	
	public static final String PJE_USUARIO_SEM_CERTIFICADO_ENVIO_COMUNICACAO = "pje:comunicacao:usuario:semTermoAssinado";

	public static final String PJE_TIPO_DOCUMENTO_ID_TIPO_DOCUMENTO_PROTOCOLO_DISTRIBUICAO = "pje:tipoDocumento:idTipoDocumentoProtocoloDistribuicao";
	
	public static final String PJE_FLUXO_TRASLADO_DOCUMENTOS_PERMITE_ATUALIZAR_DATA_JUNTADA = "pje:fluxo:trasladoDocumentos:permiteAtualizarDataJuntada";
	
	public static final String PJE_DOCUMENTO_TRASLADO_PETICAO_INICIAL_TRASLADADA = "pje:documento:traslado:idPeticaoInicialTrasladada";
	
	public static final String PJE_MOVIMENTO_TRASLADO_DOCUMENTO = "pje:movimento:idTrasladoDeDocumentos";

	public static final String PJE_CONTEUDO_SIGILOSO = "pje:conteudo:sigiloso";

	public static final String BLOQUEIO_REGISTO_JULGAMENTO_VOTACAO_IMCOMPLETA = "pje:bloqueio:registro:julgamento:votacao:incompleta";
	
	public static final String PJE_MODELO_MINUTA_PREGAO = "pje:sessao:modeloDocumento:minutaPregao";

	public static final String SUB_NOME_SISTEMA = "subNomeSistema";

	public static final String EXIBE_JUSTIFICATIVA_PREVENCAO = "pje:exibeJustificativaPrevencao";
	
	public static final String NUMERO_REGIONAL = "numeroRegional";

	public static final String PJE_SISTEMA_EDITOR_CSS_CKEDITOR = "pje:sistema:editor:css:CkEditor";

	public static final String PJE_SISTEMA_EDITOR_CSS_VISUALIZAR_DOCUMENTO = "pje:sistema:editor:css:VisualizarDocumento";
	
	public static final String PJE_SISTEMA_EDITOR_CSS_PDF_DOCUMENTO = "pje:sistema:editor:css:PDFDocumento";
	
	public static final String INTEGRACAO_CONSUMIDOR_GOV_BR = "integracaoConsumidorGovBr";
	
	public static final String HABILITA_VISUALIZACAO_SIGILOSO_FLUXO_DESLOCADO = "pje:habilitarVisualizacaoSigilosoFluxoDeslocado";
	
	public static final String ID_PESSOA_INSS = "idPessoaInss";
	
	public static final String PJE_DOCUMENTO_MODELO_BASE_LIBRE_OFFICE = "pje:documento:modelo:base:libreOffice";
	
	public static final String PJE_DOCUMENTO_OCULTA_DOCUMENTO_COMUNICACAO = "pje:documento:ocultaDocumentoComunicacao";
	
	public static final String DOMINIO_ALGORITMOS_HASH_VALIDOS = "dominioAlgoritmosHashValidos";

	public static final String PJE_DOCUMENTO_MODELO_CERTIDAO_CIENCIA_DOMICILIO = "pje:documento:modelo:certidao:ciencia:domicilio";
	
	public static final String ID_PAPEL_SERVIDOR = "pje:papel:id:servidor";

	public static final String ID_PAPEL_PARA_REMESSA_VIA_PJ = "pje:remessa:id:papel:pj";
	
	// Indica que o documento já foi impresso e precisa ser retirado da impresso fsica. 
	public static final String PJE_FLUXO_AGUARDA_IMPRESSAO="pje:fluxo:impressaoEmLote:aguardaImpressao";	

	public static final String URL_PDPJ_MARKETPLACE = "pdpj:url:marketplace";

	public static final String PDPJ_NOTIFICACOES_SECRET_TOKEN = "pdpj:notificacoes:secretToken";
	
	public static final String DOMINIO_ALGORITMOS_HASH_RSA_VALIDOS = "dominioAlgoritmosHashRSAValidos";

	public static final String PJE_DESATIVA_SINALIZACAO_MOVIMENTACAO_AUDIENCIA = "pje:desativa:sinalizacao:movimentacao:audiencia";

	public static final String INSCRICAO_MF = "inscricaoMF";
	
	public static final String PJE_EF_AJUSTAR_VALOR_CAUSA_CDA = "pje:ef:ajustarValorCausaCDA";
	
	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO = "pdpj:integracao:DomicilioEletronico";
	
	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_CLIENTID = "pdpj:integracao:DomicilioEletronico:clientId";
	
	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_SECRET = "pdpj:integracao:DomicilioEletronico:secret";
	
	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_SERVICENAME = "pdpj:integracao:DomicilioEletronico:serviceName";
	
	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_COMUNICACAOPROCESSUAL_SERVICENAME = "pdpj:integracao:DomicilioEletronico:ComunicacaoProcessual:serviceName";
	
	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_SERVICE_ONLINE_CHECK = "pdpj:integracao:DomicilioEletronico:serviceOnlineCheck";
	
	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_COMUNICACAOPROCESSUAL_SERVICE_ONLINE_CHECK = "pdpj:integracao:DomicilioEletronico:ComunicacaoProcessual:serviceOnlineCheck";

	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_REGISTRACIENCIA_LINK_INTEIROTEOR = "pdpj:integracao:DomicilioEletronico:registraCienciaLinkInteiroTeor";

	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_CACHE_CONSULTA_ONLINE_HABILITADA = "pdpj:integracao:DomicilioEletronico:cacheLocal:consultaOnline:habilitado";

	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_QTD_PRAZO_INTIMACAO = "pdpj:integracao:DomicilioEletronico:qtdPrazoIntimacao";

	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_QTD_PRAZO_CITACAO_PJ_DIREITO_PUBLICO = "pdpj:integracao:DomicilioEletronico:qtdPrazoCitacaoPjDireitoPublico";

	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_QTD_PRAZO_CITACAO_PF_PJ_DIR_PRIVADO = "pdpj:integracao:DomicilioEletronico:qtdPrazoCitacaoPfPjDireitoPrivado";

	public static final String PDPJ_INTEGRACAO_DOMICILIOELETRONICO_TIPO_PRAZO = "pdpj:integracao:DomicilioEletronico:tipoPrazo";

	public static final String PJE_DOMICILIO_ELETRONICO_FLUXO_CITACAO_EXPIRADA = "pje:DomicilioEletronico:fluxo:citacaoExpirada";

	public static final String PJE_DOMICILIO_ELETRONICO_FLUXO_TRATAMENTO_COMUNICACOES = "pje:DomicilioEletronico:fluxo:tratamentoComunicacoes";

	public static final String PJE_DOMICILIO_ELETRONICO_UTILIZAR_ALERTA_OFFLINE = "pje:DomicilioEletronico:utilizarAlertaDomicilioOffline";

	public static final String PJE_DOMICILIO_ELETRONICO_MSG_POSSIVEL_ATRASO_ENVIO = "pje:DomicilioEletronico:msgAlertaPossivelAtrasoEnvioDomicilio";

	public static final String PJE_DOMICILIO_ELETRONICO_DATA_HORA_ULTIMA_EXECUCAO_JOB_TCD = "pje:DomicilioEletronico:dataHoraUltimaExecucaoJobTCD";
	
	public static final String PJE_JOB_TRATAMENTO_COMUNICACOES_DOMICILIO_ELETRONICO_ATIVA = "pje:jobs:tratamentoComunicacoesDomicilioEletronico:ativa";
	
	public static final String PJE_JOB_TRATAMENTO_COMUNICACOES_DOMICILIO_ELETRONICO_CRON = "pje:jobs:tratamentoComunicacoesDomicilioEletronico:cron";

	public static final String PJE_JOB_REENVIO_REQUISICOES_DOMICILIO_ELETRONICO_ATIVA = "pje:jobs:reenvioRequisicoesDomicilioEletronico:ativa";

	public static final String PJE_JOB_REENVIO_REQUISICOES_DOMICILIO_ELETRONICO_CRON = "pje:jobs:reenvioRequisicoesDomicilioEletronico:cron";

	public static final String PJE_DOMICILIO_ELETRONICO_TRIBUNAL_ORIGEM = "pje:DomicilioEletronico:tribunalOrigem";

	public static final String PJE_DOMICILIO_ELETRONICO_BLOQUEIA_COMPETENCIAS = "pje:DomicilioEletronico:bloqueiaCompetencias";

	public static final String PJE_DOMICILIO_ELETRONICO_COMPETENCIAS_BLOQUEADAS = "pje:DomicilioEletronico:competenciasBloqueadas";

	public static final String PJE_DOMICILIO_ELETRONICO_BLOQUEIA_ORGAO_PUBLICO = "pje:DomicilioEletronico:bloqueiaOrgaoPublico";

	public static final String PJE_DOMICILIO_ELETRONICO_BLOQUEIA_PESSOA_FISICA = "pje:DomicilioEletronico:bloqueiaPessoaFisica";

	public static final String PJE_DOMICILIO_ELETRONICO_ORGAOS_PUBLICOS_BLOQUEADOS = "pje:DomicilioEletronico:orgaosPublicosBloqueados";

	public static final String PJE_WHITE_LIST_PREVENCAO = "pje:lista:nao:prevencao";

	public static final String PJE_SSO_ENDPOINT_TOKEN = "pje:sso:endpoint:token";	

	public static final String PJE_FLUXO_OCULTAR_BOTAO_ASSINAR_VOTO="pje:fluxo:votacaoColegiado:ocultarBotaoAssinarVoto";	
	
	public static final String REQUEST_TIMEOUT_CUSTOM_EM_SEGUNDOS = "pje:requestTimeoutCustom:segundos";

	public static final String PJE_URL_TABELAS_PROCESSUAIS_UNIFICADAS = "pje:urlTabelasProcessuaisUnificadas";

	public static final String MENSAGEM_PLANTAO = "mensagemPlantao";
			
	public static final String SSO_HEALTH_ENABLED = "pje:sso:health:enabled";
	
	public static final String PJE_MODELO_SEM_INT_MANIFESTACAO_ID="pje:peticao-sem-interesse-manifestacao:idModeloDocumento";

	public static final String ID_PAPEL_ADMINISTRADOR_JUDICIAL = "pje:papel:id:administradorjudicial";

	public static final String ID_PAPEL_LEILOEIRO = "pje:papel:id:leiloeiro";
	
	public static final String ID_PAPEL_CURADOR = "pje:papel:id:curador";
	
	public static final String ID_PAPEL_JUIZO_DEPRECANTE = "pje:papel:id:juizodeprecante";
	
    public static final String ID_MODELO_TOPICO_SINTESE = "pje:modelo:id:topicoSintese";
    
    public static final String GRUPO_ASSUNTO_TOPICO_SINTESE = "grupoAssuntoTopicoSintese";
	
	public static final String PDPJ_INTEGRACAO_BNMP_DE_PARA_TIPO_DOCUMENTO_PECA = "pdpj:integracao:bnmp:dePara:tipoDocumento";
	
	public static final String PDPJ_INTEGRACAO_BNMP_TIPOS_DOCUMENTOS_SIGILOSOS_POR_PADRAO = "pdpj:integracao:bnmp:tiposDeDocumentosSigilosos";
	
	public static final String PDPJ_INTEGRACAO_BNMP_DE_PARA_TIPO_DOCUMENTO_EVENTO = "pdpj:integracao:bnmp:dePara:tipoDocumento:evento";

	public static final String PJE_CONSULTA_DOCUMENTO_PERMITE_DOCUMENTO_SIGILOSO  = "pje:consultaDocumento:permiteDocumentoSigiloso";

	public static final String PJE_CONSULTA_DOCUMENTO_NIVEIS_ACESSO_NAO_PERMITIDO = "pje:consultaDocumento:niveisAcessoNaoPermitido";

	public static final String PJE_ASSOCIA_COMPETENCIA_SALA_AUDIENCIA = "pje:salaAudiencia:associaCompetencia";

	public static final String PJE_JOB_REENVIO_MENSAGENS_AMQP_CRON = "pje:jobs:reenvioMensagensAmqp:cron";

	public static final String PJE_CONVERSAO_HTML_PDF_EXTERNA_ENABLE = "pje:conversao:htmlpdf:externa:enable";

	public static final String PJE_CONVERSAO_HTML_PDF_EXTERNA_URL = "pje:conversao:htmlpdf:externa:url";
	
	// informações do endpoint disponibizado pelo EJUD, usando na validação de processo a ser migrado
	public static final String URL_BASE_CONSULTA_PROCESSO_EJUD = "pje:migracao:urlConsultaAPIEJUD";
	
	public static final String URL_AUTENTICACAO_CONSULTA_PROCESSO_EJUD = "pje:migracao:urlAutenticacaoAPIEJUD";
	
	public static final String USUARIO_AUTENTICACAO_CONSULTA_PROCESSO_EJUD = "pje:migracao:usuarioAPIEJUD";
	
	public static final String SENHA_AUTENTICACAO_CONSULTA_PROCESSO_EJUD = "pje:migracao:senhaAPIEJUD";
	
	// parametros auxiliares no processo de validação de migração de processo	
	public static final String MIGRACAO_PROCESSO_LISTA_COMPETENCIA = "pje:migracao:listaIdCompentenciaMigracao";
	
	public static final String MIGRACAO_PROCESSO_QUERIE_VERIFICA_REMESSA_INTERNA = "pje:migracao:querieBuscaRemessaInternaMigracao";
	
	public static final String MIGRACAO_PROCESSO_HABILITA_SINALIZACAO_PROCESSO_MIGRADO = "pje:migracao:marcarProcessoMigradoEproc";
	
	public static final String MIGRACAO_PROCESSO_CD_CERTIDAO_MIGRACAO = "pje:migracao:cdCertidaoMigracao";
	
	public static final String MIGRACAO_PROCESSO_USUARIO_SINALIZACAO_MNI = "pje:migracao:usuarioSinalizacaoMigracao";
	
	public static final String MIGRACAO_PROCESSO_FLUXO_NOTIFICAO = "pje:migracao:fluxoNotificacaoMigracao";
	
	public static final String MIGRACAO_PROCESSO_FLUXO_DESTINO = "pje:migracao:fluxoMigracao";
	
	public static final String MIGRACAO_DESABILITA_VALIDACAO_EJUD = "pje:migracao:desabilitaValidacaoEjud";
	
	public static final String MIGRACAO_DESABILITA_VALIDACAO_OUTRA_INSTANCIA = "pje:migracao:desabilitaValidacaoOutraInstancia";
	
	public static final String MIGRACAO_DESABILITA_VALIDACAO_CONCLUSAO = "pje:migracao:desabilitaValidacaoConclusao";
	
	public static final String MIGRACAO_DESABILITA_VALIDACAO_REMESSA_INTERNA = "pje:migracao:desabilitaValidacaoRemessaInterna";
	
	public static final String MIGRACAO_DESABILITA_VALIDACAO_COMPETENCIA = "pje:migracao:desabilitaValidacaoCompetencia";
	
	public static final String MIGRACAO_DESABILITA_VALIDACAO_EXPENDIENTE_ABERTO = "pje:migracao:desabilitaValidacaoExpedienteAberto";
	
	public static final String MIGRACAO_DESABILITA_VALIDACAO_CONEXAO = "pje:migracao:desabilitaValidacaoConexao";
	
	public static final String MIGRACAO_DESABILITA_VALIDACAO_AUDIENCIA_ABERTA = "pje:migracao:desabilitaValidacaoAudienciaAberta";
	
	public static final String MIGRACAO_DESABILITA_VALIDACAO_PENDENCIA_ASSINATURA = "pje:migracao:desabilitaValidacaoPendenciaAssinatura";
	
	public static final String MIGRACAO_DESABILITA_PROCESSO_BAIXADO_ARQUIVADO = "pje:migracao:desabilitaValidacaoProcessoBaixadaOuArquivado";
	
	public static final String MIGRACAO_PROCESSO_LISTA_EVENTOS_ARQUIVAMENTO = "pje:migracao:listaCDEventoArquivamento";
	
	public static final String MIGRACAO_DESABILITA_PROCESSO_CPF_CNPJ_INVALIDO = "pje:migracao:desabilitaValidacaoProcessoCPFCNPJInvalido";

	public static final String PJE_IDS_TIPO_DOCUMENTO_RESTRITO_AO_GABINETE = "pje:autosDigitais:tipoDocumentoRestritoAoGabinete:ids";

	public static final String URL_WSDL_CORREIOS = "urlWsdlCorreios";
	
	public static final String PJE_NIVEL_ACESSO_REMOVER_VISUALIZACAO_SEGREDO_JUSTICA = "pje:nivelAcessoRemoverVisualizacaoSegredoJustica";

	public static final String CUSTOM_LOGIN_INFO = "customLoginInfo";

	public static final String PJE_QTD_PRAZO_DE_GRACA = "pje:qtdPrazoDeGraca";

	public static final String PJE_TIPO_PRAZO_DE_GRACA = "pje:tipoPrazoDeGraca";
	
	public static final String MIGRACAO_CHAVE_TOKEN = "pje:migracao:migracaoChaveToken";

	public static final String PJE_DOMICILIO_ELETRONICO_CODIGO_CLASSE_PROCESSUAL =  "pje:DomicilioEletronico:codigoClasseProcessual";

	public static final String PJE_ATIVAR_LOG_ACESSO_AUTOS = "pje:ativar:log:acesso:autos";

	public static final String PJE_ATIVAR_LOG_DOWNLOAD_DOCUMENTOS = "pje:ativar:log:download:documentos";

	// Ativa os logs de download de documentos + acesso aos autos do MNI
	public static final String PJE_ATIVAR_LOG_ACESSO_MNI = "pje:ativar:log:autos:mni";

	private Parametros(){
	}

}
