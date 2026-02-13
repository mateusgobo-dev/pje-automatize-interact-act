/**
 *  pje
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.nucleo;

/**
 * Classe destinada a permitir a definição estática dos principais nomes de papeis
 * que devem ser herdados por papeis concretos de uma instalação do PJe.
 * 
 * @author cristof
 *
 */
public class Papeis {
	
	/**
	 * Papel a ser herdado pelos papeis com função de administração em uma instalação do PJe.
	 * 
	 */
	public static final String SISTEMA = "pje:sistema"; 

	/**
	 * Papel a ser herdado pelos papeis com função de administração em uma instalação do PJe.
	 * 
	 */
	public static final String ADMINISTRADOR = "pje:administrador"; 

	/**
	 * Papel a ser herdado pelos papeis com função jurisdicional em uma instalação do PJe.
	 * 
	 */
	public static final String MAGISTRADO = "magistrado";

	public static final String PJE_MAGISTRADO = "pje:magistrado";

	/**
	 * Papel a ser herdado pelos papeis com função de alteração dos movimentos do processo em uma instalação do PJe.
	 * 
	 */
	public static final String CONTROLE_VISIBILIDADE_MOVIMENTO_PROCESSO = "pje:papel:controleVisibilidadeMovimentoProcesso";
	
	/**
	 * Papel a ser herdado pelos papeis que atuam como servidores ou auxiliares internos 
	 * em uma instalação do PJe.
	 * 
	 */
	public static final String INTERNO = "pje:auxiliarInterno";
	
	/**
	 * Papel a ser herdado pelos papeis de advogado em uma instalação do PJe.
	 * 
	 */
	public static final String PJE_ADVOGADO = "pje:advogado";

	/**
	 * Papel a ser herdado pelos papeis de assistente de advogado em uma instalação do PJe.
	 * 
	 */
	public static final String PJE_ASSISTENTE_ADVOGADO = "pje:assistenteAdvogado";

	public static final String ADVOGADO = "advogado";

	public static final String ASSISTENTE_ADVOGADO = "assistAdvogado";
	
	/**
	 * Papel a ser herdado pelos papeis de assistente de procuradorias em uma instalação do PJe.
	 * 
	 */
	public static final String PJE_ASSISTENTE_PROCURADOR = "pje:assistenteProcuradoria";

	public static final String PJE_ASSISTENTE_GESTOR_PROCURADOR = "pje:assistenteGestorProcuradoria";

	/** 
	 * Papel de Assistente de Procuradoria
	 */
	public static final String ASSISTENTE_PROCURADORIA = "assistProcuradoria";
	
	public static final String ASSISTENTE_GESTOR_PROCURADORIA = "assistGestorProcuradoria";

	/**
	 * Papel a ser herdado pelos papeis de peritos em uma instalação do PJe.
	 * 
	 */
	public static final String PJE_PERITO = "pje:perito";
	
	public static final String PERITO = "perito";

	/**
	 * Papel a ser herdado pelos papeis de procuradores públicos e membros do 
	 * Ministério Público em uma instalação do PJe.
	 * 
	 */
	public static final String PJE_REPRESENTANTE_PROCESSUAL = "pje:procurador";
	
	/** 
	 * Papel de Procurador/Defensor padrão
	 * 
	 */
	public static final String REPRESENTANTE_PROCESSUAL = "procurador";

	/** 
	 * Papel de Procurador/Defensor Gestor
	 * 
	 */
	public static final String REPRESENTANTE_PROCESSUAL_GESTOR = "procChefe";
	

	/**
	 * Papel a ser herdado pelos papeis de oficiais de justiça em uma instalação do PJe.
	 * 
	 */
	public static final String PJE_OFICIAL_JUSTICA = "pje:oficialjustica";
	
	public static final String PJE_OFICIAL_JUSTICA_ALTERA_CONTAGEM = "pje:oficialJustica:permiteAlterarContagemPrazoResposta";
	
	public static final String OFICIAL_JUSTICA = "Ofjus";
	/**
	 * Papel de Oficial de Justica Distribuidor
	 */
	public static final String PJE_OFICIAL_JUSTICA_DISTRIBUIDOR = "pje:oficialJusticaDistribuidor";
	
	public static final String OFICIAL_JUSTICA_DISTRIBUIDOR = "OfjusDistr";

	/**
	 * Papel a ser herdado pelos papeis de secretários de sessão de julgamento em uma instalação do PJe.
	 * 
	 */
	public static final String SECRETARIO_SESSAO = "pje:secretariosessao";

	public static final String VISUALIZA_SIGILOSO = "pje:visualizaSigiloso";
	
	public static final String MANIPULA_SIGILOSO = "pje:manipulaSigiloso";

	public static final String PODE_INICIAR_FLUXO_DIGITALIZACAO = "pje:processo:fluxo:deflagrar:digitalizacao";
	
	public static final String PODE_INICIAR_FLUXO_COMUNICACAO_ENTRE_INSTANCIAS = "pje:podeSolicitarComunicacaoEntreOrgaosJulgadores";

	public static final String DESENTRANHA_DOCUMENTO = "pje:desentranhaDoc";
	
	public static final String CONSULTA_MNI = "pje:consulta:mni";	

	/**
	 * Administrador de Cadastro de Usuário: papel com permissão de registrar usuário em qualquer papel, exceto o de Administrador.
	 *  
	 */
	public static final String ADMINISTRADOR_CADASTRO_USUARIO = "pje:papel:permissaoCadastroUsuarioTodosPapeis";	
	
	/**
	 * Papel para Administrar os recursos de procuradorias:
	 *  
	 */
	public static final String PJE_ADMINISTRADOR_PROCURADORIA = "pje:papel:administrarProcuradorias";
	
	/**
	 * Papel para Administrar os recursos de órgão julgador:
	 *  
	 */
	public static final String PJE_ADMINISTRADOR_ORGAO_JULGADOR = "pje:papel:administrarOrgaoJulgador";
	
	/**
	 * Papel de Conciliador
	 */
	public static final String CONCILIADOR = "conciliador";
	
	/**
	 * Permite responder expediente de parte sem advogado/certificado
	 */
	public static final String PERMITE_RESPONDER_EXPEDIENTE = "pje:papel:servidor:permiteResponderExpediente";
	
	/**
	 * Papel de Analista Judiciário
	 */
	public static final String ANALISTA_JUDICIARIO = "pje:papel:analistaJudiciario";
	
	/**
	 * Papel de Assistente Gestor Advogado
	 */
	public static final String ASSISTENTE_GESTOR_ADVOGADO = "assistGestorAdvogado";
	
	/**
	 * Papel de Assistente Diretor de Distribuição
	 */
	public static final String DIRETOR_DISTRIBUICAO = "dir_distribuicao";

	/**
	 * Papel de Assistente Diretor de Distribuição
	 */
	public static final String PJE_SERVIDOR_MALOTE = "ServMalo";

	/**
	 * Papel de Servidor
	 */
	public static final String SERVIDOR = "servidor";
	
	/**
	 * Papel de Assistente Servidor de Distribuição
	 */
	public static final String SERVIDOR_DISTRIBUICAO = "serv_distrib";
	
	/**
	 * Papel de SERVIDOR_RET_DEST
	 */
	public static final String SERVIDOR_RET_DEST = "ServRetDest";
	
	/**
	 * Papel de Servidor de Secretaria
	 */
	public static final String SERVIDOR_SECRETARIA = "ServidorSecret";
	
	/**
	 * Papel de Assessor
	 */
	public static final String ASSESSOR = "Asses";
	
	/**
	 * Papel de Visualizador de Painel Completo
	 */
	public static final String VISUALIZA_PAINEL_COMPLETO = "pje:visualizaPainelCompleto";
	/**
	 * Papel de Visualizacao Painel Completo
	 */
	public static final String PERFIL_VISUALIZACAO_PAINEL = "perfilVisualizacaoPainel";
	
	/**
	 * Papel de ESTAG_PREV
	 */
	public static final String ESTAG_PREV = "estag_prev";
	
	/**
	 * Papel de ESTAG_DISTRIB
	 */
	public static final String ESTAG_DISTRIB = "estag_distrib";
	
	/**
	 * Papel de ESTAG_ANALISE
	 */
	public static final String ESTAG_ANALISE = "estag_analise";
	
	/**
	 * Papel de ESTAG_RETIF
	 */
	public static final String ESTAG_RETIF = "estag_retif";
	
	/**
	 * Papel de Advogado Procurador
	 */
	public static final String ADVOGADO_PROCURADOR = "advogado_procurador";
	
	/**
	 * Papel de Jus Postulandi
	 */
	public static final String JUS_POSTULANDI = "jusPostulandi";
	
	/**
	 * Papel de usuário PUSH
	 */
	public static final String PUSH = "UsuPush";
	
	/**
	 * Papel de Processo Objeto Visualizador
	 */
	public static final String PROCESSO_OBJETO_VISUALIZADOR = "pje:processo:objeto:visualizador";
	
	/**
	 * Papel de Diretor de Secretaria
	 */
	public static final String DIRETOR_SECRETARIA = "dirSecretaria";
	
	/**
	 * Papel de ADM_CONHE
	 */
	public static final String ADM_CONHE = "AdmConh";
	
	/**
	 * Papel de SERV_CONHE
	 */
	public static final String SERV_CONHE = "ServConhe";
	
	/**
	 * Papel de PROTOCOLO
	 */
	public static final String PROTOCOLO = "protocolo";
	
	/**
	 * Papel de PROTOCOLO_SECAO
	 */
	public static final String PROTOCOLO_SECAO = "protocoloSecao";
	
	/**
	 * Papel de Redator
	 */
	public static final String REDATOR = "redator";
	
	/**
	 * Papel de VINCULACAO_AUXILIARES
	 */
	public static final String VINCULACAO_AUXILIARES = "pje:orgaojulgador:processos:vinculacaoAuxiliares:editor";
	
	/**
	 * Papel de OCULTAR_AGRUPADOR
	 */
	public static final String OCULTAR_AGRUPADOR = "pje:painel:agrupador:ocultar";
	
	/**
	 * Papel de OCULTAR_AGRUPADOR_PROCESSO_SEGREDO
	 */
	public static final String OCULTAR_AGRUPADOR_PROCESSO_SIGILOSO = "pje:painel:agrupador:ocultar:processoPedidoSegredo";
	
	/**
	 * Papel de OCULTAR_AGRUPADOR_PROCESSO_SEGREDO
	 */
	public static final String OCULTAR_AGRUPADOR_DOCUMENTO_SIGILOSO = "pje:painel:agrupador:ocultar:documentoPedidoSigilo";

	/**
	 * Papel de OCULTAR_AGRUPADOR_PEDIDO_JUSTICA_GRATUITA
	 */
	public static final String OCULTAR_AGRUPADOR_PEDIDO_JUSTICA_GRATUITA = "pje:painel:agrupador:ocultar:pedidoJusticaGratuita";

	/**
	 * Papel de AGRUPADOR_PEDIDO_TUTELA_LIMINAR
	 */
	public static final String AGRUPADOR_PEDIDO_TUTELA_LIMINAR = "pje:painel:liminarTutela:agrupador:manipula";
	
	/**
	 * Papel de OCULTAR_AGRUPADOR_PEDIDO_HABILITACAO_AUTOS
	 */
	public static final String OCULTAR_AGRUPADOR_PEDIDO_HABILITACAO_AUTOS = "pje:painel:agrupador:ocultar:habilitacaoAutos";
	
	/**
	 * Papel de OCULTAR_AGRUPADOR_ANALISE_PREVENCAO
	 */
	public static final String OCULTAR_AGRUPADOR_ANALISE_PREVENCAO = "pje:painel:agrupador:ocultar:analisePrevencao";
	
	/**
	 * Papel de OCULTAR_AGRUPADOR_DOCUMENTOS_NAO_LIDOS
	 */
	public static final String OCULTAR_AGRUPADOR_DOCUMENTOS_NAO_LIDOS = "pje:painel:agrupador:ocultar:processosDocumentosNaoLidos";
	
	/**
	 * Papel de OCULTAR_AGRUPADOR_MANDADO_DEVOLVIDO
	 */
	public static final String OCULTAR_AGRUPADOR_MANDADOS_DEVOLVIDOS = "pje:painel:agrupador:ocultar:mandadoDevolvido";
	
	/**
	 * Papel de OCULTAR_AGRUPADOR_AGUARDANDO_REVISAO
	 */
	public static final String OCULTAR_AGRUPADOR_AGUARDANDO_REVISAO = "pje:painel:agrupador:ocultar:aguardandoRevisao";
	
	/**
	 * Papel de OCULTAR_AGRUPADOR_PETICOES_AVULSAS
	 */
	public static final String OCULTAR_AGRUPADOR_PETICOES_AVULSAS = "pje:painel:agrupador:ocultar:peticoesAvulsas";
	
	/**
	 * Papel de MOVIMENTACAO_JUNTADA
	 */
	public static final String MOVIMENTACAO_JUNTADA = "pje:lancarMovimentacaoJuntada";
	
	/**
	 * Papel de AUXIIAR_INTERNO
	 */
	public static final String AUXIIAR_INTERNO = "pje:auxiliarInterno";
	
	/**
	 * Papel de CARACTERISTICAS_PESSOAIS
	 */
	public static final String CARACTERISTICAS_PESSOAIS = "pje:caracteristicasPessoais";
	
	/**
	 * Papel de PODE_INSERIR_PROCESSO_EXISTENTE
	 */
	public static final String PODE_INSERIR_PROCESSO_EXISTENTE = "pje:papel:podeInserirProcessoExistente";
	
	/**
	 * Papel de GESTOR
	 */
	public static final String GESTOR = "gestor";
	
	/**
	 * Papel de VISUALIZA_SITUACOES_ATUAIS
	 */
	public static final String VISUALIZA_SITUACOES_ATUAIS = "pje:papel:visualizaSituacoesAtuais";
	
	/**
	 * Papel de DEFLAGRAR_DIGITALIZACAO
	 */
	public static final String DEFLAGRAR_DIGITALIZACAO = "pje:processo:fluxo:deflagrar:digitalizacao";
	
	/**
	 * Papel de VISUALIZAR_PARTES_EXCLUIDAS
	 */
	public static final String VISUALIZAR_PARTES_EXCLUIDAS = "pje:papel:visualizaPartesExcluidas";
	
	/**
	 * Papel de VISUALIZAR_SITUACOES
	 */
	public static final String VISUALIZAR_SITUACOES = "pje:papel:visualizaSituacoes";
	
	/**
	 * Papel de PROCESSO_OBJETO_EDITOR
	 */
	public static final String PROCESSO_OBJETO_EDITOR = "pje:processo:objeto:editor";
	
	/**
	 * Papel de VISUALIZAR_PETICIONAMENTO_AVULSO
	 */
	public static final String VISUALIZAR_PETICIONAMENTO_AVULSO = "pje:processo:visualizaPeticionamentoAvulso";
	
	/**
	 * Papel de Manipular Substituições de Magistrados
	 */
	public static final String MANIPULA_SUBSTITUICAO_MAGISTRADO = "pje:papel:manipulaSubstituicaoMagistrado";
	
	/**
	 * Papel de Vesualizar os Magistrados Associados ao Processo
	 */
	public static final String VISUALIZA_MAGISTRADOS_ASSOCIADOS_PROCESSO = "pje:papel:visualizaMagistradosAssociadosProcesso";
	
	/**
	 * Papel de SERV_NUCL
	 */
	public static final String SERV_NUCL = "ServNucl";
	
	public static final String PROCURADOR_MP = "procuradorMP";
	
	public static final String PROCURADOR_CHEFE_MP = "procChefeMP";
	
	/**
	 * Papel de VISUALIZA_ABA_ASSOCIADOS260CE para Justiça Eleitoral
	 */
	public static final String VISUALIZA_ABA_ASSOCIADOS260CE = "pje:visualizaAbaAssociados260CE";
	
	/**
	 * Papel de VISUALIZA_ABA_ASSOCIADOS
	 */
	public static final String VISUALIZA_ABA_ASSOCIADOS = "pje:visualizaAbaAssociados";
	
	/**
	 * Papel VISUALIZA ABA EXPEDIENTES
	 */
	public static final String VISUALIZA_ABA_EXPEDIENTES = "pje:processo:expedientes:visualizar";
	
	/**
	 * Papel de PODE_RECLASSIFICAR_DOCUMENTO
	 */
	public static final String PODE_RECLASSIFICAR_DOCUMENTO = "pje:papel:podeReclassificarDocumento";

	/**
	 * Papel de PERMITE_ORDENAR_PAUTA_SESSAO
	 */
	public static final String PERMITE_ORDENAR_PAUTA_SESSAO = "pje:relacaoJulgamento:permiteOrdenarPautaSessao";
	
	/**
	 * Papel da JE para atribuir a funcionalidade de publicar decisoes em mural ou em sessao
	 */
	public static final String PUBLICAR_PROCESSO_DECISAO_SESSAO_MURAL = "pje:processoPublicarSessaoEmMural";
	
	
	public static final String RECURSO_RETIFICAR_PROCESSO = "/pages/Processo/RetificacaoAutuacao/updateRetificacaoAutuacao.seam";
	

	/**
	 * Papel da JE para atribuir a funcionalidade de gravar decisoes
	 */
	public static final String GRAVAR_PROCESSO_DECISOES_SESSAO_MURAL = "pje:processoGravarSessaoPublicacao";
	
	public static final String PODE_FECHAR_EXPEDIENTES = "pje:processo:expedientes:permiteFechar";
	
	public static final String EXIGE_RECAPTCHA = "pje:papel:exigeRecaptcha";
	
	public static final String CADASTRA_PARTE_SEM_DOCUMENTO = "pje:papel:cadastraParteSemDocumento";
	
	public static final String PLACAR_SESSAO_JULGAMENTO_WEBSOCKET = "pje:websocket:placarSessaoJulgamento";
	
	public static final String APROVEITAR_ADVOGADOS = "pje:papel:aproveitarAdvogados";
	
	public static final String PJE_ADMINISTRADOR_AUTUACAO = "pje:papel:administrarAutuacao";

	public static final String PDPJ_VISUALIZAR_MARKETPLACE = "pdpj:marketplace:visualizar";

	/**
	 * Papel que permite remover processo da relação de julgamento quando a pauta já está fechada
	 */
	public static final String PERMITE_REMOVER_PROCESSO_PAUTA_FECHADA = "pje:relacaoJulgamento:permiteRemoverProcessoPautaFechada";
	
	public static final String MANIPULA_INFORMACAO_CRIMINAL = "pje:criminal:manipulaInformacaoCriminal";

	public static final String VISUALIZA_INFORMACAO_CRIMINAL = "pje:criminal:visualizaInformacaoCriminal";
	
	public static final String CONSULTAR_CDA = "pje:consultarCDA";
	
	public static final String EDITAR_CDA = "pje:editarCDA";
	
	public static final String EDITAR_CDA_VIA_MNI = "pje:editarCDAviaMNI";
	
}