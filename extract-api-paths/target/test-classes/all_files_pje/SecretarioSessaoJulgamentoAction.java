package br.com.jt.pje.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.editor.manager.AnotacaoManager;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoManager;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.infox.utils.ItensLegendas;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.list.ComposicaoSessaoList;
import br.com.jt.pje.manager.OrgaoJulgadorColegiadoOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.PessoaMagistradoManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.util.CustomJbpmTransactional;
import br.jus.cnj.pje.util.CustomJbpmTransactionalClass;
import br.jus.pje.jt.entidades.ComposicaoSessao;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.TipoSituacaoPauta;
import br.jus.pje.jt.entidades.TipoVotoJT;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.jt.enums.SituacaoAnaliseEnum;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.jt.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.enums.editor.TipoTopicoEnum;
import br.jus.pje.nucleo.util.Crypto;


@Name(SecretarioSessaoJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
@CustomJbpmTransactionalClass
public class SecretarioSessaoJulgamentoAction
    extends AbstractSessaoJulgamentoAction {
    private static final long serialVersionUID = -5189451542478126913L;
    public static final String NAME = "secretarioSessaoJulgamentoAction";
    private static final String MSG_INICIAR_SESSAO = "Esta operação registrará o início da sessão. Deseja continuar?";
    private static final String MSG_ENCERRAR_SESSAO = "Esta operação registrará o encerramento da sessão e não	permitirá apregoar mais processos. Processos com dispositivo liberado não poderão mais ser editados. Deseja continuar?";
    private static final String MSG_FECHAR_SESSAO = "Esta operação registrará o fechamento da sessão. Deseja continuar?";
    private static final TipoVotoJT TIPO_VOTO_ACOMPANHA_RELATOR = ParametroUtil.instance()
                                                                               .getTipoVotoAcompanhaRelator();
    private static final TipoVotoJT TIPO_VOTO_DIVERGE_EM_PARTE = ParametroUtil.instance()
                                                                              .getTipoVotoDivergeEmParte();
    private static final TipoVotoJT TIPO_VOTO_DIVERGENTE = ParametroUtil.instance()
                                                                        .getTipoVotoDivergente();
    private List<Voto> listVoto = new ArrayList<Voto>();
    private List<ComposicaoSessao> listComposicaoSessao = new ArrayList<ComposicaoSessao>();
    private ProcessoTrf processoMesa;
    private ComposicaoSessao presidente;
    private OrgaoJulgador orgaoJulgador;
    private PessoaProcurador pessoaProcurador;
    private PessoaProcurador procuradorNaSessao;
    private String mensagemParaExibir;
    private boolean podeEncerrarSessao;
    private boolean controleAberturaToogle = false;
    private ComposicaoSessaoList composicaoSessaoList = new ComposicaoSessaoList();
    @In
    private PessoaMagistradoManager pessoaMagistradoManager;
    @In
    private OrgaoJulgadorColegiadoOrgaoJulgadorManager orgaoJulgadorColegiadoOrgaoJulgadorManager;
    @In
    private AssinaturaDocumentoService assinaturaDocumentoService;
    @In
    private ProcessoJudicialService processoJudicialService; 
    @In
    private EntityManager entityManager;
    @In
    private ModalComposicaoProcessoAction modalComposicaoProcessoAction;
    private boolean situacaoAnalise;
    
	@Logger
	private Log log;
	@In
	private ProcessoDocumentoEstruturadoManager processoDocumentoEstruturadoManager;
	@In
	private AnotacaoManager anotacaoManager;

    private String topicoDisSessao_;
    private String topicoDisVoto_;
    
    private boolean podeLiberarDispositivo;

    public void abreFechaToogle() {
        if (this.controleAberturaToogle) {
            this.controleAberturaToogle = false;
        } else {
            this.controleAberturaToogle = true;
        }
    }

    public void iniciarVisualizacaoVoto() {
        setPautaSessao(findById(PautaSessao.class, getIdPautaSessao()));
        setSessao(getPautaSessao().getSessao());
        
        // [PJEII-4718][PJEII-4763] Adição do idSessao, para que o método super.iniciar() possa acessar a sessão sem precisar de parâmetro
        setIdSessao(this.getSessao().getIdSessao());
        
        setProcessoTrf(getPautaSessao().getProcessoTrf());
        setIdProcesso(getProcessoTrf().getIdProcessoTrf());
        
        verificarSituacaoAnalise();

        OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();

        if (orgaoJulgadorAtual == null) {
            setVoto(votoManager.getVotoProcessoByOrgaoJulgadorSessao(
                    getProcessoTrf(), getOrgaoJulgadorRedator(), getSessao()));
            setAnotacaoVoto(anotacaoVotoManager.getAnotacaoVotoSemOJByProcessoSessaoEColegiado(
                    getProcessoTrf(), getVoto().getSessao(),
                    Authenticator.getOrgaoJulgadorColegiadoAtual()));
            //Preencher anotacoes
            setAnotacoesVotoEmOutrasSessoes(anotacaoVotoManager.getAnotacoesVotoByProcessoEColegiadoExcluindoSessaoAtual(
                    getProcessoTrf(),
                    Authenticator.getOrgaoJulgadorColegiadoAtual(),
                    getVoto().getSessao()));
            super.inicializar(orgaoJulgadorAtual);
        } else {
            super.inicializar();
        }
        
        // [PJEII-4718][PJEII-4763] chamando o super.iniciar() para que a lista de pauta da sessão seja inicializada
        super.iniciar();
        /**
         * [PJEII-4390] : fernando.junior (09/01/2013) 
         * 
         * Realiza tratamento nos links dentro dos editores para corrigir erro de redirecionamento dos mesmos. 
         */
        corrigirLinksEditor();
    }
	
	public void updateDispositivosSessaoAndVoto(){
		 
		ProcessoDocumentoEstruturado processoDocumentoEstruturado = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(processoTrf.getIdProcessoTrf());
		
     if(processoDocumentoEstruturado!=null && processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList()!=null){
        	
        	for(ProcessoDocumentoEstruturadoTopico tempProcDocTopico : processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList()){
            	
            	if(tempProcDocTopico.getTopico().getTipoTopico().equals(TipoTopicoEnum.IT_DISP_SESSAO)){
            	   tempProcDocTopico.setConteudo(topicoDisSessao_);
            	   processoDocumentoEstruturadoManager.update(tempProcDocTopico);
            	}
            	
                if(tempProcDocTopico.getTopico().getTipoTopico().equals(TipoTopicoEnum.IT_DISP_VOTO)){
                	tempProcDocTopico.setConteudo(topicoDisVoto_);
                	processoDocumentoEstruturadoManager.update(tempProcDocTopico);
            	}  	        	        	
              }
            }
	    }

    
	private void verificarSituacaoAnalise() {
		boolean situacaoAnalise = false;
        if(getPautaSessao() != null && getPautaSessao().getSituacaoAnalise() != null){
        	if(getPautaSessao().getSituacaoAnalise().equals(SituacaoAnaliseEnum.A)){
        		situacaoAnalise  = true;
        	}
        }
        setSituacaoAnalise(situacaoAnalise);
	}


    @CustomJbpmTransactional
    public void update() {
    	try{
	        updateDispositivosSessaoAndVoto(); 
	        ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class, getDocumentoVotoDispDecisorio().getIdProcessoDocumento());
	        pd.getProcessoDocumentoBin().setModeloDocumento(getModeloDispositivo());
	        setOrgaoJulgadorRedator(getComposicaoRedator().getComposicaoSessao().getOrgaoJulgador());
	        getPautaSessao().setOrgaoJulgadorRedator(getOrgaoJulgadorRedator());
	        if (getComposicaoRedator().getMagistradoSubstituto() != null) {
	            getPautaSessao().setMagistradoRedator(getComposicaoRedator().getMagistradoSubstituto());
	        } else if (getComposicaoRedator().getComposicaoSessao().getMagistradoSubstituto() != null) {
	            getPautaSessao().setMagistradoRedator(getComposicaoRedator().getComposicaoSessao().getMagistradoSubstituto());
	        } else {
	            getPautaSessao().setMagistradoRedator(getComposicaoRedator().getComposicaoSessao().getMagistradoPresente());
	        }
	        
	        getPautaSessao().setSituacaoAnalise(this.isSituacaoAnalise() ? SituacaoAnaliseEnum.A : SituacaoAnaliseEnum.N);
	
	        votoManager.updateVarios(pd, getPautaSessao());
	
	        persistAtualizaAnotacoes();
	
	        if (assinaturaDocumentoService.isProcessoDocumentoAssinado(pd)) {
	            assinaturaDocumentoService.removeAllSignature(pd);
	        }
	
	        FacesMessages.instance().clear();
	        FacesMessages.instance().add(Severity.INFO, "Registro alterado com sucesso.");
			this.atualizarGridProcessos();
			transitarProcesso();
			
			construirModeloEstruturado();
			
    	}catch(Exception e){
    		FacesMessages.instance().clear();
	        FacesMessages.instance().add(Severity.ERROR, "Erro ao alterar registro.");
    	}
    }

    private void transitarProcesso() throws Exception {    	
		if(isProcessoProntoParaTransitar()){
			ParametroUtil parametroUtil = ParametroUtil.instance();
			pautaJulgamentoService.moverParaDefaultTransition(getProcessoTrf(),parametroUtil.getIdMinutarDispositivoSessao(),"Mover default transition de minutar dispositivo sessão");
		}
	}                
    
    private boolean isProcessoNaTarefa(ProcessoTrf processo){
    	ParametroUtil parametroUtil = ParametroUtil.instance();
    	Long quantidade = processoJudicialService.existeIdTarefaNoProcesso(processo,parametroUtil.getIdMinutarDispositivoSessao());
    	return quantidade > 0;
    }
    
    public boolean isProcessoProntoParaTransitar() {    	
    	if (getProcessoTrf() == null) {
    		return false;
    	}
    	return ((sessaoEncerrada() || sessaoFechada()) && isSituacaoAnalise() && isProcessoNaTarefa(getProcessoTrf())); 
    }
    
    public boolean isProcessoNaTarefaAssinarAcordao() {    	
    	if (getProcessoTrf() != null) {
    		ParametroUtil parametroUtil = ParametroUtil.instance();
    		Long quantidade = processoJudicialService.existeIdTarefaNoProcesso(getProcessoTrf() ,parametroUtil.getIdTarefaAssinarAcordao());
    		return quantidade > 0;
    	}
    	return false;
    }
        
    public void updateSeNaoTransitar() {        	
    	setPodeLiberarDispositivo(true);
    	if (!isProcessoProntoParaTransitar()) {
    		setPodeLiberarDispositivo(false);
    		update();
    	}
    }
    

    @Override
    public void iniciarLegenda() {
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[0], false);	// Divergência
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[1], false);	// Destaque
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[3], false);	// Julgados
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[4], false);	// Deliberação em Sessão
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[5], false);	// Retirados de Pauta
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[6], false);	// Pendentes
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[7], false);	// Sustentação Oral
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[8], false);	// Preferências
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[9], false);	// Acórdãos Assinados
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[10], false);	// Acórdãos Assinados pelo Magistrado
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[11], false);	// Acórdãos Não Assinados
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[12], false);	// Julgados sem Pendência de lançamentos
        getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[13], false);	// Julgados com Pendência de lançamentos
		
		/**
		 * (fernando.junior - 21/01/2013) Adição de novas legendas
		 */
		getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[14], false);	// Divergência com análise pendente
		getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[15], false);	// Divergência não concluída/liberada
		getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[16], false);	// Destaque não concluído/liberado
		getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[17], false);	// Anotação
		getMapLegenda().put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[18], false);	// Anotação não concluída
    }

    @Override
    protected char getSiglaPainel() {
        return 'S';
    }

    public void iniciarLancadorMovimentos() {
        setPautaSessao(findById(PautaSessao.class, getIdPautaSessao()));
    }

    public void atualizaPauta(PautaSessao pautaSessao) {
        setPautaSessao(pautaSessao);
        /*
         * PJE-JT: David Vieira: [PJEII-523]
         * Replicando códigos do movimentar.page.xml que inicializam os
         * componentes gerenciadores do processo.
         *
         * A falta desse código estava gerando inconsistências ao gerar
         * complementos que dependiam dos componentes acimas inicializados
         */
        ProcessoHome.instance()
                    .setProcessoIdProcesso(pautaSessao.getProcessoTrf()
                                                      .getIdProcessoTrf());
        ProcessoTrfHome.instance().setarInstancia();

        /*
         * PJE-JT: Fim
         */
    }

    public void gravarLancamentos() {
        Boolean sucesso = EventsTreeHandler.instance()
                                           .registraEventosSemFluxo(getPautaSessao()
                                                                        .getProcessoTrf()
                                                                        .getProcesso());

        if (sucesso) {
            FacesMessages.instance()
                         .add(Severity.INFO, "Movimentos lançados com sucesso");
            setMensagemParaExibir(null);
            //Necessário para exibir o componente tree novamente
            EventsTreeHandler.instance().setRegistred(false);
            //TODO: Verificar se isso é realmente necessário, pois foi a única maneira que vi de,
            //após lançado um evento, conseguir que o eventsTree fosse exibido novamente
            EventsTreeHandler.instance().setAgrupamentosInstance(null);
        } else {
            //TODO: Não está sendo possível recuperar a mensagem lançada no FacesMessages de EventsTreeHandler
            //			List<FacesMessage> listMensagens = FacesMessages.instance().getCurrentMessages();
            //			List<FacesMessage> listMensagens2 = FacesMessages.instance().getCurrentGlobalMessages();
            setMensagemParaExibir(
                "Favor informar os movimentos e seus respectivos complementos");
        }
    }

    public void addAll() {
        /*
         * [PJEII-3272] Remoção do objeto processoTrf na seleção múltipla dos processos em pauta.
         * @author Fernando Barreira (10/10/2012)
         */
        setProcessoTrf(null);

        if (getListPautaSessao().size() == 0) {
            getListPautaSessao().addAll(getElaboracaoVotoList().getResultList());
        } else {
            for (PautaSessao pautaSessao : getElaboracaoVotoList()
                                               .getResultList()) {
                if (!getListPautaSessao().contains(pautaSessao)) {
                    getListPautaSessao().add(pautaSessao);
                }
            }
        }
    }

    public String[][] getItemsLegenda() {
        if (sessaoEncerrada() || sessaoFechada()) {
            return ItensLegendas.LEGENDAS_SECRETARIO_SESSAO_ENCERRADA_ARRAY;
        }

        return ItensLegendas.LEGENDAS_SECRETARIO_ARRAY;
    }

    public boolean sessaoNaoIniciada() {
        return getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.S);
    }

    public void incluirProcuradorOrgaoJulgadorNaSessao() {
        if (pessoaProcurador != null) {
            procuradorNaSessao = pessoaProcurador;
        }

        if (orgaoJulgador != null) {
            ComposicaoSessao composicaoSessao = new ComposicaoSessao();
            composicaoSessao.setSessao(getSessao());
            composicaoSessao.setOrgaoJulgador(orgaoJulgador);
            composicaoSessao.setPresenteSessao(true);
            composicaoSessao.setPresidente(false);

            listComposicaoSessao.add(composicaoSessao);
        }

        setPessoaProcurador(null);
        setOrgaoJulgador(null);
    }

    private PautaSessao criarPautaSessao(ProcessoTrf processoTrf,
        TipoSituacaoPauta tipoSituacaoPauta, TipoInclusaoEnum tipoInclusaoEnum) {
        PautaSessao ps = new PautaSessao();
        ps.setSessao(getSessao());
        ps.setProcessoTrf(processoTrf);
        ps.setOrgaoJulgadorRedator(processoTrf.getOrgaoJulgador());
        ps.setSustentacaoOral(false);
        ps.setTipoInclusao(tipoInclusaoEnum);
        ps.setSituacaoAnalise(SituacaoAnaliseEnum.N);
        ps.setPreferencia(false);
        ps.setTipoSituacaoPauta(tipoSituacaoPauta);
        ps.setDataSituacaoPauta(new Date());
        ps.setUsuarioSituacaoPauta(Authenticator.getUsuarioLogado());

        return ps;
    }

    public void podeIncluirProcessoMesa() {
        if (processoMesa == null) {
//            FacesMessages.instance().add(Severity.ERROR, "Escolha um processo");
            setMensagemParaExibir("Escolha um processo");
            return;
        }

        try {
            pautaSessaoManager.podeInserirProcessoEmMesaDuranteSessao(processoMesa);
            preencherListComposicaoSessao();
        } catch (NegocioException e) {
            processoMesa = null;
            FacesMessages.instance().add(Severity.ERROR, e.getMensagem());
        }
    }

    private void preencherListComposicaoSessao() {
        listComposicaoSessao.clear();
        listComposicaoSessao.addAll(composicaoSessaoList.getResultList());

        for (ComposicaoSessao cs : listComposicaoSessao) {
            if (cs.getPresidente()) {
                setPresidente(cs);

                break;
            }
        }
		// PJEII-3599 - thiago.carvalho - Atualizar Processos em Pauta
		this.atualizarGridProcessos();
    }

    public void addRemoveComposicaoSessao(ComposicaoSessao row) {
        if (!listComposicaoSessao.contains(row)) {
            listComposicaoSessao.add(row);
        } else {
            listComposicaoSessao.remove(row);

            if (row.equals(presidente)) {
                setPresidente(null);
            }
        }
    }

    public void addPresidente(ComposicaoSessao row) {
        setPresidente(row);

        if (!listComposicaoSessao.contains(row)) {
            listComposicaoSessao.add(row);
        }
    }

    public void incluirProcessoMesaDuranteSessao() {
    	try {
			incluirProcessoEmMesaDuranteSessao();
		} catch (Exception e) {
			log.debug(e);
	        FacesMessages.instance().add(Severity.ERROR, e.getMessage());
		}
    }
    /**
     * 04/03/2012 - PJE-JT - Antonio Lucas PJEII-5830
     * Criação de Método intermediário para poder por a anotação 
     * @CustomJbpmTransactional para fazer o rollback, caso algo dê errado.
     */
    @CustomJbpmTransactional
    private void incluirProcessoEmMesaDuranteSessao() throws Exception {
        PautaSessao pautaSessao = criarPautaSessao(processoMesa,
                ParametroUtil.instance().getTipoSituacaoPautaPendente(),
                TipoInclusaoEnum.MS);

        pautaJulgamentoService.inserirPautaAtualizandoVotoEAnotacao(pautaSessao);

        composicaoProcessoSessaoManager.inserirComposicaoProcesso(pautaSessao,
            presidente, listComposicaoSessao);

        pautaJulgamentoService.inserirVotosEDocumentosPorOrgaoJulgadorDaComposicao(pautaSessao,
            Authenticator.getUsuarioLogado());

        votoManager.atualizaPautaResultadoVotacao(pautaSessao.getProcessoTrf(),
            pautaSessao,
            composicaoSessaoManager.getOrgaoJulgadorBySessao(getSessao()));

		/**
		 * 04/03/2012 - PJE-JT - Antonio Lucas PJEII-5830
		 * Quando o usuário incluir um processo em sessão de julgamento,
		 *  o processo incluído deve ser movido para a default transition 
		 *  do nó onde se encontra (O processo deve estar em "Aguardando inclusão em pauta ou sessão"). 
		 *  Desta forma, ele será remetido ao nó 
		 *  "Aguardando sessão de julgamento" e, no encerramento da sessão, 
		 *  o processo cairá no nó "Assinar acórdão".
		 */
		pautaJulgamentoService.moverParaDefaultTransition(processoMesa, ParametroUtil.instance().getIdTarefaInclusaoPauta(), 
				"Mover para default transition o processo em mesa.");
        
        limparProcessoMesa();
        carregarResultados();
		
		// PJEII-3599 - thiago.carvalho - Atualizar Processos em Pauta
		this.atualizarGridProcessos();
    }

    private void limparProcessoMesa() {
        setProcessoMesa(null);
        setPresidente(null);
    }

    public void alteraPresenteSessao(ComposicaoSessao composicaoSessao) {
        int index = 0;

        for (int i = 0; i < listComposicaoSessao.size(); i++) {
            ComposicaoSessao composicao = listComposicaoSessao.get(i);

            if (composicao.getOrgaoJulgador()
                              .equals(composicaoSessao.getOrgaoJulgador())) {
                index = i;
            }
        }

        listComposicaoSessao.get(index)
                            .setPresenteSessao(!composicaoSessao.getPresenteSessao());
    }

    public void alterarPresidenteSessao(ComposicaoSessao composicaoSessao) {
        int index = 0;

        for (int i = 0; i < listComposicaoSessao.size(); i++) {
            ComposicaoSessao composicao = listComposicaoSessao.get(i);

            if (composicao.getOrgaoJulgador()
                              .equals(composicaoSessao.getOrgaoJulgador())) {
                index = i;
            }

            if (composicao.getPresidente()) {
                composicao.setPresidente(false);
            }
        }

        listComposicaoSessao.get(index).setPresidente(true);
    }

    public void carregarComposicaoSessao() {
        if (getSessao().getPessoaProcurador() != null) {
            procuradorNaSessao = getSessao().getPessoaProcurador();
        }

        listComposicaoSessao.clear();
        listComposicaoSessao.addAll(composicaoSessaoList.getResultList());
    }

    public List<PessoaMagistrado> magistradoSubstitutoSessaoItems(
        OrgaoJulgador orgaoJulgador) {
        return pessoaMagistradoManager.magistradoSubstitutoSessaoItems(orgaoJulgador,
            getSessao());
    }

    public void gravarMagistrado(ComposicaoSessao row) {
        try {
            update(row);
            FacesMessages.instance().clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<OrgaoJulgador> listOrgaoJulgadorSemComposicaoSessaoByColegiadoItems() {
        List<OrgaoJulgador> listOrgaoJulgador = new ArrayList<OrgaoJulgador>();
        listOrgaoJulgador.addAll(orgaoJulgadorColegiadoOrgaoJulgadorManager.getOrgaoJulgadorSemComposicaoSessaoByColegiadoSessaoItems(
                getSessao()));

        List<OrgaoJulgador> listOJ = new ArrayList<OrgaoJulgador>();
        boolean remover = false;

        for (ComposicaoSessao composicaoSessao : listComposicaoSessao) {
            for (OrgaoJulgador orgaoJulgador : listOrgaoJulgador) {
                if (orgaoJulgador.equals(composicaoSessao.getOrgaoJulgador())) {
                    remover = true;
                    listOJ.add(orgaoJulgador);
                }
            }
        }

        if (remover) {
            for (OrgaoJulgador oj : listOJ) {
                listOrgaoJulgador.remove(oj);
            }
        }

        return listOrgaoJulgador;
    }

    public void atualizarComposicaoSessao() {
        if (procuradorNaSessao != null) {
            getSessao().setPessoaProcurador(procuradorNaSessao);
            update(getSessao());
        }

        for (ComposicaoSessao cs : listComposicaoSessao) {
            update(cs);
        }

        FacesMessages.instance().clear();
        FacesMessages.instance()
                     .add(Severity.INFO, "Registro alterado com sucesso");
    }

    public void carregarVotacao() {
        listVoto.clear();
        entityManager.clear();
        listVoto = votoManager.getVotosComposicaoProcessoByProcessoSessao(getProcessoTrf(),
                getSessao());
    }

    public void marcarConclusao(char tipo, Voto row) {
        if (tipo == 'A') {
            getListVoto().get(getListVoto().indexOf(row))
                .setTipoVoto(TIPO_VOTO_ACOMPANHA_RELATOR);
        } else if (tipo == 'P') {
            getListVoto().get(getListVoto().indexOf(row))
                .setTipoVoto(TIPO_VOTO_DIVERGE_EM_PARTE);
        } else if (tipo == 'D') {
            getListVoto().get(getListVoto().indexOf(row))
                .setTipoVoto(TIPO_VOTO_DIVERGENTE);
        }
    }

    public void atualizarConclusoes() {
        for (Voto v : listVoto) {
            update(v);
        }

        carregarQuantidadeVotos();
    }

    public void iniciarSessao() {
        pautaJulgamentoService.iniciarSessao(getSessao(),
            Authenticator.getUsuarioLogado());
    }

    public boolean exibirBotaoIniciar() {
        return sessaoNaoIniciada() &&
        ((new Date()).compareTo(getSessao().getDataSessao()) >= 0);
    }

    public void julgarProcessos() {
		/*
		 * [PJEII-3272] Método update passa a ser chamado apenas em caso do julgamento de um único processo. 
		 * @author Fernando Barreira (10/10/2012)
		 */
		if (getListPautaSessao().size() == 1) {
			update();
		}
		
        FacesMessages.instance().clear();
        pautaJulgamentoService.atualizarSituacaoVariosProcessosEmPauta(getListPautaSessao(),
            Authenticator.getUsuarioLogado(),
            ParametroUtil.instance().getTipoSituacaoPautaJulgado(),
            this.isSituacaoAnalise());
        limparDadosProcesso();
        removeAll();
        removeAllPautaSessao();
        super.setCheckBoxSelecionarTodos(false);
        carregarResultados();
		
		this.atualizarGridProcessos();
    }

    public void retirarProcessosPauta() {
        FacesMessages.instance().clear();
        pautaJulgamentoService.atualizarSituacaoVariosProcessosEmPauta(getListPautaSessao(),
            Authenticator.getUsuarioLogado(),
            ParametroUtil.instance().getTipoSituacaoPautaRetiradoPauta());
        limparDadosProcesso();
        removeAll();
        removeAllPautaSessao();
        super.setCheckBoxSelecionarTodos(false);
        carregarResultados();
 		
		this.atualizarGridProcessos();
   }

    public void deliberarProcessos(TipoSituacaoPauta tipoSituacaoPauta) {
        FacesMessages.instance().clear();
        pautaJulgamentoService.atualizarSituacaoVariosProcessosEmPauta(getListPautaSessao(),
            Authenticator.getUsuarioLogado(), tipoSituacaoPauta);
        limparDadosProcesso();
        removeAll();
        removeAllPautaSessao();
        super.setCheckBoxSelecionarTodos(false);
        carregarResultados();
		
		this.atualizarGridProcessos();
    }

	public void atualizarGridProcessos(){
		super.setListaPautaSessaoGrid(this.getElaboracaoVotoList().list());
	}
	
    public void verificarPendenciasSessao() {
        setPodeEncerrarSessao(false);
        pautaJulgamentoService.verificarPendenciasSessao(getSessao());
        setPodeEncerrarSessao(true);
    }

    public void encerrarSessao() {
    	
    	
    	pautaJulgamentoService.encerrarSessao(getSessao(), Authenticator.getUsuarioLogado());
         
         //Move os processos no fluxo
         List<PautaSessao> listaPautaSessaoBySessao = pautaSessaoManager.listaPautaSessaoBySessao(getSessao());
         Integer idTarefa = Integer.valueOf(ParametroUtil.getParametro("idTarefaAguardandoSessaoJulgamento"));
         
         for (PautaSessao pautaSessao : listaPautaSessaoBySessao){
 			
         	ProcessoTrf processoTrf = pautaSessao.getProcessoTrf();
         	
     		if(processoTrf != null){
     			
 				try{
 				
 					pautaJulgamentoService.movimentarProcessoEncerrarSessao(processoTrf, idTarefa);
 					
 				} catch (Exception e){
 					
 					
 					e.printStackTrace();
 					
 					if(e.getCause() instanceof PJeDAOException){
 						
 						log.error(Severity.ERROR, "[ENCERRAR SESSÃO JULGAMENTO] Erro: processo " + processoTrf.getNumeroProcesso() + " deve estar na tarefa Aguardando sessão de julgamento" , e);
 						FacesMessages.instance().add(Severity.ERROR, "Erro: processo " + processoTrf.getNumeroProcesso() + " deve estar na tarefa Aguardando sessão de julgamento");
 						
 					} else {
 					
	 					if(processoTrf != null && processoTrf.getNumeroProcesso() != null){
	 						log.error(Severity.ERROR, "[ENCERRAR SESSÃO JULGAMENTO] Erro ao movimentar processo " + processoTrf.getNumeroProcesso(), e);
	 						FacesMessages.instance().add(Severity.ERROR, "Erro ao movimentar processo " +  processoTrf.getNumeroProcesso());
	 					} else {
	 						log.error(Severity.ERROR, "[ENCERRAR SESSÃO JULGAMENTO] Erro ao movimentar processo ", e);
	 						FacesMessages.instance().add(Severity.ERROR, "Erro ao movimentar processo ");
	 					}
 					}
 					
 					Util.beginTransaction();
 					
 				}
     		}
 		}
    }

    public void fecharSessao()
        throws InstantiationException, IllegalAccessException {
        String msgError = pautaJulgamentoService.fecharSessao(getSessao(),
                Authenticator.getUsuarioLogado());

        if (msgError != null) {
            FacesMessages.instance().add(Severity.ERROR, msgError);
        } else {
            FacesMessages.instance().clear();
        }
    }

    public String avisoSessao() {
        if (sessaoNaoIniciada()) {
            return MSG_INICIAR_SESSAO;
        } else if (sessaoIniciada()) {
            return MSG_ENCERRAR_SESSAO;
        } else if (sessaoEncerrada()) {
            return MSG_FECHAR_SESSAO;
        } else {
            return null;
        }
    }

    @Override
    protected void assinarProcessoDocumento() {
    }
    
    public boolean verificaSeOStatusDeTodosOsProcessosEstaoPendentes(){
    	for (PautaSessao p : super.getListaPautaSessaoGrid()){
    		if (!ParametroUtil.instance().getTipoSituacaoPautaPendente().equals(p.getTipoSituacaoPauta()))
    			return false;
    	}
    	return true;
    }

	public String verificaSeStatusPendente(PautaSessao p) {
		boolean pendente = ParametroUtil.instance().getTipoSituacaoPautaPendente().equals(p.getTipoSituacaoPauta());

		String retorno = null;

		if (!pendente) {
			retorno = String.format("Este processo já foi apregoado anteriormente e seu resultado foi %s. Deseja apregoar novamente??",p.getTipoSituacaoPauta().getTipoSituacaoPauta());
		}

		return retorno;
	}

	/**
	 * Método criado para atualizar o modelo quando se escolhe a opção 'não' na
	 * confirmação para se apregoar um processo que já contém um resultado.
	 * 
	 * @param p
	 */
	public void desabilitarCheckbox(PautaSessao p) {
		p.setCheckBoxSelecionado(false);
	}
      	
	/** 
	 * [PJEII-3272] Método sobrescrito para reproduzir no clique do check box a ação do método apregoarProcesso, 
	 * chamado no duplo clique no link do processo em pauta. 
	 * @author Fernando Barreira (10/10/2012)
	 * 
	 * [PJEII-4425] (18/12/2012) Os processos não devem voltar pra situação pendente ao serem marcados pelo checkbox
	 * @author Antonio Lucas
	 * 
	 */
	public void addRemovePautaSessao(PautaSessao row){
		if(row.isCheckBoxSelecionado()){
			super.getListPautaSessao().add(row);
		}else{	
			pautaJulgamentoService.updateSituacaoPautaSessao(row, Authenticator.getUsuarioLogado(), ParametroUtil.instance().getTipoSituacaoPautaPendente());
			super.getListPautaSessao().remove(row);
		}
		
		// Caso tenha apenas 1 processo apregoado
		if(super.getListPautaSessao().size() == 1){
			this.executarApregoarProcesso(getListPautaSessao().get(getListPautaSessao().size() - 1));
		}else{
			this.setProcessoTrf(null);
			pautaJulgamentoService.updateSituacaoPautaSessao(super.getListPautaSessao(), Authenticator.getUsuarioLogado(), ParametroUtil.instance().getTipoSituacaoPautaPendente());
		}
		
		// Controle dos processos apregoados e checkbox.
		super.setCheckBoxSelecionarTodos(false);
		if(super.getListPautaSessao().size() == super.getListaPautaSessaoGrid().size()){
			super.setCheckBoxSelecionarTodos(true);
		}
	}
	
    public void addRemoveAllPautaSessao() {
        super.setListPautaSessao(new ArrayList<PautaSessao>());
        this.setProcessoTrf(null);
        if (this.isCheckBoxSelecionarTodos()) {
            super.addAllPautaSessao();
        } else {
            super.removeAllPautaSessao();
        }
        pautaJulgamentoService.updateSituacaoPautaSessao(super.getListaPautaSessaoGrid(), Authenticator.getUsuarioLogado(), ParametroUtil.instance().getTipoSituacaoPautaPendente());
    }
	 
	/**
	 * [PJEII-4425] Deve executar a mesma ação ao clicar no checkbox 
	 * ou duplo clique no número do processo 
	 * @author Antonio Lucas (18/12/2012) 
	 */
    public void apregoarProcesso(PautaSessao pautaSessao) {
    	this.executarApregoarProcesso(pautaSessao);
        
        // Controle dos processos apregoados e checkbox.
        super.setListPautaSessao(new ArrayList<PautaSessao>());
        super.getListPautaSessao().add(pautaSessao);
        super.setCheckBoxSelecionarTodos(false);

        for(PautaSessao ps : super.getListaPautaSessaoGrid()){
        	ps.setCheckBoxSelecionado(false);
   
        }
        
        if (super.getListPautaSessao().size() == super.getListaPautaSessaoGrid().size()) {
            super.setCheckBoxSelecionarTodos(true);
        }

        pautaSessao.setCheckBoxSelecionado(true);
        
        ProcessoDocumentoEstruturado processoDocumentoEstruturado = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(pautaSessao.getProcessoTrf().getIdProcessoTrf());
        carregaVotoCompleto(processoDocumentoEstruturado);
        carregarConteudoDispVotoAndSessao(processoDocumentoEstruturado);        
    }

    public void carregarConteudoDispVotoAndSessao(ProcessoDocumentoEstruturado documentoEstruturado){
    	
    	//FIXME 4878 - utilizando a consulta para mostrar os topicos
        //ProcessoDocumentoEstruturado processoDocumentoEstruturado = processoDocumentoEstruturadoManager.getProcessoDocumentoEstruturadoByIdProcessoTrf(documentoEstruturado.getProcessoTrf().getIdProcessoTrf());
        if(documentoEstruturado!=null && documentoEstruturado.getProcessoDocumentoEstruturadoTopicoList()!=null){
        	
        	for(ProcessoDocumentoEstruturadoTopico tempProcDocTopico : documentoEstruturado.getProcessoDocumentoEstruturadoTopicoList()){
            	
            	if(tempProcDocTopico.getTopico().getTipoTopico().equals(TipoTopicoEnum.IT_DISP_SESSAO)){
            		topicoDisSessao_ = tempProcDocTopico.getConteudo();
            	}
            	
                if(tempProcDocTopico.getTopico().getTipoTopico().equals(TipoTopicoEnum.IT_DISP_VOTO)){
                	topicoDisVoto_ = tempProcDocTopico.getConteudo();
            	}  	        	        	
            }       	
        }
    	    	
    }
    
    public void construirModeloEstruturado(){    	
    	ProcessoDocumentoEstruturado processoDocumentoEstruturado = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(processoTrf.getIdProcessoTrf());
    	carregaVotoCompleto(processoDocumentoEstruturado);
    	carregarConteudoDispVotoAndSessao(processoDocumentoEstruturado);
    }

    public void executarApregoarProcesso(PautaSessao pautaSessao) {
        pautaJulgamentoService.apregoarProcesso(pautaSessao, Authenticator.getUsuarioLogado());
        setIdProcesso(pautaSessao.getProcessoTrf().getIdProcessoTrf());
        setPautaSessao(pautaSessao);
        
        /**
         * [PJEII-3529] Limpando o OrgaoJulgadorRedator para evitar "lixo". (fernando.junior - 14/02/2013) 
         */
        setOrgaoJulgadorRedator(null);
        
        inicializar();
        carregarQuantidadeVotos();
        
        /**
         * [PJEII-4763]
         * Informar se o processo possui dispositivo liberado
         */
        this.setSituacaoAnalise(this.getPautaSessao().getSituacaoAnalise().equals(SituacaoAnaliseEnum.A));
        // [PJEII-4763] Fim
    }

    public void carregaModalComposicaoProcesso() {
        modalComposicaoProcessoAction.carregarComposicao().doProcesso(getProcessoTrf())
         	.daPauta(getPautaSessao()).naSessao(getSessao()).executar();
    }

    /*
     * inicio get e set
     */
    public boolean getPodeLiberarDispositivo() {
		return podeLiberarDispositivo;
	}
    
    public void setPodeLiberarDispositivo(boolean podeLiberarDispositivo) {
		this.podeLiberarDispositivo = podeLiberarDispositivo;
	}
    
    public void setProcessoMesa(ProcessoTrf processoMesa) {
        this.processoMesa = processoMesa;
    }

    public ProcessoTrf getProcessoMesa() {
        return processoMesa;
    }

    public void setComposicaoSessaoList(
        ComposicaoSessaoList composicaoSessaoList) {
        this.composicaoSessaoList = composicaoSessaoList;
    }

    public ComposicaoSessaoList getComposicaoSessaoList() {
        return composicaoSessaoList;
    }

    public void setListComposicaoSessao(
        List<ComposicaoSessao> listComposicaoSessao) {
        this.listComposicaoSessao = listComposicaoSessao;
    }

    public List<ComposicaoSessao> getListComposicaoSessao() {
        return listComposicaoSessao;
    }

    public void setPresidente(ComposicaoSessao presidente) {
        this.presidente = presidente;
    }

    public ComposicaoSessao getPresidente() {
        return presidente;
    }

    public void setListVoto(List<Voto> listVoto) {
        this.listVoto = listVoto;
    }

    public List<Voto> getListVoto() {
        return listVoto;
    }

    public OrgaoJulgador getOrgaoJulgador() {
        return orgaoJulgador;
    }

    public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
        this.orgaoJulgador = orgaoJulgador;
    }

    public PessoaProcurador getPessoaProcurador() {
        return pessoaProcurador;
    }

    public void setPessoaProcurador(PessoaProcurador pessoaProcurador) {
        this.pessoaProcurador = pessoaProcurador;
    }

    public PessoaProcurador getProcuradorNaSessao() {
        return procuradorNaSessao;
    }

    public void setProcuradorNaSessao(PessoaProcurador procuradorNaSessao) {
        this.procuradorNaSessao = procuradorNaSessao;
    }

    public void setMensagemParaExibir(String mensagemParaExibir) {
        this.mensagemParaExibir = mensagemParaExibir;
    }

    public String getMensagemParaExibir() {
        return mensagemParaExibir;
    }

    public boolean getPodeEncerrarSessao() {
        return podeEncerrarSessao;
    }

    public void setPodeEncerrarSessao(boolean podeEncerrarSessao) {
        this.podeEncerrarSessao = podeEncerrarSessao;
    }

    public boolean getControleAberturaToogle() {
        return controleAberturaToogle;
    }

    public void setControleAberturaToogle(boolean controleAberturaToogle) {
        this.controleAberturaToogle = controleAberturaToogle;
    }

    public List<ProcessoDocumentoBinPessoaAssinatura> getListProcessoDocumentoBinPessoaAssinatura() {
        List<ProcessoDocumentoBinPessoaAssinatura> lista = new ArrayList<ProcessoDocumentoBinPessoaAssinatura>();

        if ((getDocumentoVotoDispDecisorio() != null) &&
                (getDocumentoVotoDispDecisorio().getIdProcessoDocumento() != 0)) {
            ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class,
                    getDocumentoVotoDispDecisorio().getIdProcessoDocumento());
            lista.addAll(processoDocumentoBinPessoaAssinaturaManager.getAssinaturaDocumento(
                    pd.getProcessoDocumentoBin()));

            return lista;
        }

        return lista;
    }

    @Override
    public void assinar() {
        // TODO Auto-generated method stub
    }

    /**
     * [PJEII-4763]
     * Informar se o processo possui dispositivo liberado
     */
	public boolean isSituacaoAnalise() {
		return situacaoAnalise;
	}

	public void setSituacaoAnalise(boolean situacaoAnalise) {
		this.situacaoAnalise = situacaoAnalise;
	}
	// [PJEII-4763] Fim 

    
    @Override
    public void pesquisarProcessos(String sigla) {
    	if (!Strings.isEmpty(sigla)) {
            if (sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[0]) || 
            		sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[14]) ||
            		sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[15])) {
            	filtroDivergencia(sigla);
            } else if (sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[1]) || 
            		sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[16])) {
            	filtroDestaque(sigla);
            } else if (sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[17]) || 
            		sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[18])) {
            	filtroAnotacao(sigla);
            }
            
            super.pesquisarProcessos(sigla);
    	}
    }

    public void pesquisarProcessosSessao() {
    	super.pesquisarProcessos(null);
    }
    
    /**
	 * Verifica a existência de divergências para o documento, retornando um nº indicando o tipo de divergência encontrada (se houver):
	 * 0 - O documento não possui divergências
	 * 1 - Divergência pendente
	 * 2 - Divergência não concluída/liberada
	 * 3 - Divergência
	 * 
	 * @author fernando.junior (21/01/2013) 
	 * 
	 * [PJEII-5293] Método alterado para que verificasse as anotações pelo documento, não pelos tópicos.
	 * @author fernando.junior (05/02/2013) 
	 */
	public int existeDivergencia(int idProcessoTrf) {
		ProcessoDocumentoEstruturado documento = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(idProcessoTrf);
		
		if ( documento != null && documento.getIdProcessoDocumentoEstruturado() != null ) {
			return anotacaoManager.temDivergencias(documento);
		}
		
		return 0;
	}
	
	/**
	 * Verifica a existência de destaques para o documento, retornando um nº indicando o tipo de destaque encontrado (se houver):
	 * 0 - O documento não possui destaques
	 * 1 - Destaque não concluído/liberado
	 * 2 - Destaque
	 * 
	 * @author fernando.junior (21/01/2013)
	 * 
	 * [PJEII-5293] Método alterado para que verificasse as anotações pelo documento, não pelos tópicos.
	 * @author fernando.junior (05/02/2013) 
	 */
	public int existeDestaque(int idProcessoTrf) {
		ProcessoDocumentoEstruturado documento = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(idProcessoTrf);
		
		if (documento != null && documento.getIdProcessoDocumentoEstruturado() != null) {
			return anotacaoManager.temDestaques(documento);
		}
		
		return 0;
	}
	
	/**
	 * Verifica a existência de anotação para o documento, retornando um nº indicando o tipo de anotação:
	 * 0 - O documento não possui anotações
	 * 1 - Anotação não concluída
	 * 2 - Anotação
	 * 
	 * @author fernando.junior (21/01/2013)
	 * 
	 * [PJEII-5293] Método alterado para que verificasse as anotações pelo documento, não pelos tópicos.
	 * @author fernando.junior (05/02/2013) 
	 */
	public int existeAnotacao(int idProcessoTrf) {
		ProcessoDocumentoEstruturado documento = processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(idProcessoTrf);
		
		if (documento != null && documento.getIdProcessoDocumentoEstruturado() != null) {
			return anotacaoManager.temAnotacoes(documento);
		}
		
		return 0;
	}
	       
    public String getTopicoDisSessao_() {
		return topicoDisSessao_;
	}
	
    public void setTopicoDisSessao_(String topicoDisSessao_) {
		this.topicoDisSessao_ = topicoDisSessao_;
	}

    public String getTopicoDisVoto_() {
		return topicoDisVoto_;
	}

    public void setTopicoDisVoto_(String topicoDisVoto_) {
		this.topicoDisVoto_ = topicoDisVoto_;
	}
	
}


