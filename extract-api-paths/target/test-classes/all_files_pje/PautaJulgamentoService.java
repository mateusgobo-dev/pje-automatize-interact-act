package br.com.infox.pje.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.NoResultException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;

import br.com.infox.cliente.home.CaixaFiltroHome;
import br.com.infox.cliente.home.PainelUsuarioHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.core.certificado.util.VerificaCertificadoPessoa;
import br.com.infox.core.dao.GenericDAO;
import br.com.infox.core.manager.GenericManager;
import br.com.infox.exceptions.NegocioException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.CaixaFiltroManager;
import br.com.infox.pje.manager.ProcessoJtManager;
import br.com.infox.pje.processor.SessaoJTFechamentoPautaProcessor;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.action.AbstractSessaoJulgamentoAction;
import br.com.jt.pje.manager.AnotacaoVotoManager;
import br.com.jt.pje.manager.ComposicaoProcessoSessaoManager;
import br.com.jt.pje.manager.ComposicaoSessaoManager;
import br.com.jt.pje.manager.DocumentoVotoManager;
import br.com.jt.pje.manager.HistoricoSituacaoPautaManager;
import br.com.jt.pje.manager.HistoricoSituacaoSessaoManager;
import br.com.jt.pje.manager.PautaSessaoManager;
import br.com.jt.pje.manager.SessaoManager;
import br.com.jt.pje.manager.TipoVotoJTManager;
import br.com.jt.pje.manager.VotoManager;
import br.jus.cnj.pje.entidades.vo.ConsultaProcessoVO;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ConsultaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoEventoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.FluxoService;
import br.jus.cnj.pje.servicos.AtividadesLoteService;
import br.jus.cnj.pje.util.CustomJbpmTransactional;
import br.jus.cnj.pje.util.CustomJbpmTransactionalClass;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.jt.entidades.AnotacaoVoto;
import br.jus.pje.jt.entidades.ComposicaoProcessoSessao;
import br.jus.pje.jt.entidades.ComposicaoSessao;
import br.jus.pje.jt.entidades.DocumentoVoto;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.ProcessoJT;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.TipoSituacaoPauta;
import br.jus.pje.jt.entidades.TipoVotoJT;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.jt.enums.ClassificacaoTipoSituacaoPautaEnum;
import br.jus.pje.jt.enums.ConclusaoEnum;
import br.jus.pje.jt.enums.SituacaoAnaliseEnum;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.jt.enums.TipoInclusaoEnum;
import br.jus.pje.jt.enums.TipoSituacaoPautaJTEnum;
import br.jus.pje.nucleo.entidades.Agrupamento;
import br.jus.pje.nucleo.entidades.CaixaFiltro;
import br.jus.pje.nucleo.entidades.EventoAgrupamento;
import br.jus.pje.nucleo.entidades.HistoricoMovimentacaoLote;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.Crypto;


@Name(PautaJulgamentoService.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
@CustomJbpmTransactionalClass
public class PautaJulgamentoService {
    public static final String NAME = "pautaJulgamentoService";
    private static final String MSG_PROCESSO_PENDENTE = "Existe processo pendente, não é possível encerrar a sessão.";
    private static final String MSG_PROCESSO_JULGADO_SEM_MOVIMENTO = "Não é possível fechar a sessão de julgamento, pois existe processo julgado sem movimentação lançada.";
    private static final String MSG_REMOVER_COMPOSICAO_SESSAO = "Existe processo na Pauta de Julgamento. Não é possível remover um órgão julgador da composição da sessão.";
    private static final TipoSituacaoPauta TIPO_SITUACAO_PAUTA_APREGOADO = ParametroUtil.instance()
                                                                                        .getTipoSituacaoPautaApregoado();
    private static final TipoSituacaoPauta TIPO_SITUACAO_PAUTA_PENDENTE = ParametroUtil.instance()
                                                                                       .getTipoSituacaoPautaPendente();
    private final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_EMENTA = ParametroUtil.instance()
                                                                                      .getTipoProcessoDocumentoEmenta();
    private final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_RELATORIO = ParametroUtil.instance()
                                                                                         .getTipoProcessoDocumentoRelatorio();
    private final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_FUNDAMENTACAO = ParametroUtil.instance()
                                                                                             .getTipoProcessoDocumentoFundamentacao();
    private final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_DISPOSITIVO = ParametroUtil.instance()
                                                                                           .getTipoProcessoDocumentoDispositivo();
	@In
	private PautaSessaoManager pautaSessaoManager;
	@In
	private ComposicaoSessaoManager composicaoSessaoManager;
    @In
    protected ComposicaoProcessoSessaoManager composicaoProcessoSessaoManager;
	@In
	private VotoManager votoManager;
	@In
	private GenericManager genericManager;
	@In
	private AnotacaoVotoManager anotacaoVotoManager;
	@In
	private HistoricoSituacaoSessaoManager historicoSituacaoSessaoManager;
	@In
	private HistoricoSituacaoPautaManager historicoSituacaoPautaManager;
	@In
	private SessaoManager sessaoManager;
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	@In
	private ProcessoEventoManager processoEventoManager;
	@In
	private ProcessoJtManager processoJtManager;
	@In
	private GenericDAO genericDAO;
	@In
	private ProcessoDocumentoBinManager processoDocumentoBinManager;
	@In
	private TipoVotoJTManager tipoVotoJTManager;
	@In
	private DocumentoVotoManager documentoVotoManager;
	@In
	private CaixaFiltroManager caixaFiltroManager; 
	@In
	private SessaoJTFechamentoPautaProcessor sessaoJTFechamentoPautaProcessor;
	@In
	private ProcessoJudicialManager processoJudicialManager;
	@In(create = false, required = false)
	private ProcessInstance processInstance;
	@In
	private FluxoService fluxoService;
	
	
	@Logger
	private Log log;
	
	@In(create = true)
	private AtividadesLoteService atividadesLoteService;

    private boolean canInclude(List<OrgaoJulgador> list, SessaoJT sessao) {
        if (list.size() < sessao.getOrgaoJulgadorColegiado()
                                    .getMinimoParticipante()) {
            throw new NegocioException(
                "Número de participantes menor que o mínimo");
        }

        return true;
    }

    private boolean canRemove(SessaoJT sessao) {
        if (pautaSessaoManager.existePautaSessao(sessao)) {
            throw new NegocioException(MSG_REMOVER_COMPOSICAO_SESSAO);
        }

        return true;
    }
    
    public Integer getIdSessao() {
    	Integer retorno = 0;
		
		PautaSessao ps = pautaSessaoManager.getUltimaPautaByProcesso(loadProcessoJudicial());
		if (ps == null || ps.getSessao() == null) {
			throw new AplicationException("Não existe nenhuma sessão para esse processo.");
		}
		retorno = ps.getSessao().getIdSessao();
		return retorno;
    }
    
    /**
	 * Carrega o processo judicial.
	 * 
	 * @return o processo judicial vinculado a esta atividade.
	 */
	private ProcessoTrf loadProcessoJudicial(){
		try{
			return this.processoJudicialManager.findByProcessInstance(processInstance);
		} catch (PJeBusinessException e){
			FacesMessages.instance().add(Severity.ERROR, "Houve um erro ao tentar obter o processo judicial.");
		} catch (PJeDAOException e){
			FacesMessages.instance().add(Severity.ERROR, "Houve um erro de banco de dados ao tentar obter o processo judicial.");
		}
		return null;
	}

    public void atualizarComposicoesSessao(List<OrgaoJulgador> list,
        SessaoJT sessao, List<ComposicaoSessao> listComposicao) {
        if ((list == null) || (list.size() == 0) || (sessao == null)) {
            return;
        }

        if (!canInclude(list, sessao)) {
            return;
        }

        List<OrgaoJulgador> listOJSessao = new ArrayList<OrgaoJulgador>();
        listOJSessao.addAll(composicaoSessaoManager.getOrgaoJulgadorBySessao(
                sessao));

        boolean sucessRemove = removerComposicaoSessao(sessao, list,
                listOJSessao);

        if (sucessRemove) {
            atualizarComposicaoSessao(listComposicao);
        }
    }

    private boolean removerComposicaoSessao(SessaoJT sessao,
        List<OrgaoJulgador> list, List<OrgaoJulgador> listOrgaoJulgadorSessao) {
        List<OrgaoJulgador> listaRemoverOrgao = new ArrayList<OrgaoJulgador>();

        for (OrgaoJulgador orgao : listOrgaoJulgadorSessao) {
            if (!list.contains(orgao)) {
                listaRemoverOrgao.add(orgao);
            }
        }

        if (listaRemoverOrgao.size() > 0) {
            if (!canRemove(sessao)) {
                return false;
            }

            for (OrgaoJulgador oj : listaRemoverOrgao) {
                ComposicaoSessao cs = composicaoSessaoManager.getComposicaoSessao(sessao,
                        oj);
                genericManager.remove(cs);
            }
        }

        return true;
    }
    
	@CustomJbpmTransactional
    public boolean incluirPauta(ProcessoTrf processoTrf, SessaoJT sessao, TipoInclusaoEnum tipoInclusaoEnum) throws Exception{
		
		boolean erro = false;
		
		try{
			
			processoTrf = composicaoSessaoManager.find(ProcessoTrf.class, processoTrf.getIdProcessoTrf());
    		sessao = composicaoSessaoManager.find(SessaoJT.class, sessao.getIdSessao());
			
		    PautaSessao ps = new PautaSessao();
		    ps.setSessao(sessao);
		    ps.setProcessoTrf(processoTrf);
		    ps.setOrgaoJulgadorRedator(processoTrf.getOrgaoJulgador());
		    ps.setSustentacaoOral(false);
		    ps.setTipoInclusao(tipoInclusaoEnum);
		    ps.setSituacaoAnalise(SituacaoAnaliseEnum.N);
		    
		    ps.setPreferencia(false);
		    ps.setTipoSituacaoPauta(ParametroUtil.instance()
		                                         .getTipoSituacaoPautaAguardandoSessaoJulgamento());
		    ps.setDataSituacaoPauta(new Date());
		    ps.setUsuarioSituacaoPauta(Authenticator.getUsuarioLogado());
		    
		    
		    if( !isPautaAberta(sessao) ) {
		    	
		    	String nomeCaixa = SessaoJTFechamentoPautaProcessor.instance().obterNomeCaixa(sessao);
		    		//efetivamente transita o processo entre nós 
		        	moverParaDefaultTransitionMovendoParaCaixa(processoTrf, nomeCaixa);
				
		    }
		    
		    inserirPautaAtualizandoVotoEAnotacao(ps);
		    composicaoProcessoSessaoManager.inserirComposicaoProcesso(ps, composicaoSessaoManager.composicaoSessaoListBySessao(sessao));
		    
		} catch (Exception e){
			
			erro = true;
			e.printStackTrace();
			if(processoTrf != null){
				log.error(Severity.ERROR, "[INSERIR PAUTA] Erro ao movimentar processo " + processoTrf.getNumeroProcesso(), e);
				FacesMessages.instance().add(Severity.ERROR, "Erro ao movimentar processo " +  processoTrf.getNumeroProcesso());
			} else {
				log.error(Severity.ERROR, "[INSERIR PAUTA] Erro ao movimentar processo ", e);
				FacesMessages.instance().add(Severity.ERROR, "Erro ao movimentar processo ");
			}
			
			throw e;
		}
		
		return erro;
	}
	
	@CustomJbpmTransactional
	public Long fecharPauta(Usuario usuario, SessaoJT sessao, List<ProcessoTrf> listaProcessoMovimentado, Long idTaskInstace, PautaSessao pautaSessao, List<PautaSessao> processosPautaSessaoInclusaoPA) throws Exception{
		
		ProcessoTrf processo = pautaSessao.getProcessoTrf();
		if(processo != null){
				ParametroUtil parametroUtil = ParametroUtil.instance();
				Integer idTarefa = Integer.valueOf(ParametroUtil.getParametro("idTarefaInclusaoPauta"));
				idTaskInstace = obtemIdTaskInstacePorProcesso(processo, idTarefa);
				
				moverParaDefaultTransition(processo,parametroUtil.getIdTarefaInclusaoPauta(),"Mover default transition de pauta");
				
				if(processosPautaSessaoInclusaoPA != null && processosPautaSessaoInclusaoPA.size() > 0 && processosPautaSessaoInclusaoPA.contains(pautaSessao)){
						
					sessaoJTFechamentoPautaProcessor.lancarMovimentoInclusaoPautaPorProcesso(processo, sessao);
					sessaoJTFechamentoPautaProcessor.intimacaoDePauta(usuario, sessao, processo);
				}
				
				listaProcessoMovimentado.add(processo);
			
		}
		return idTaskInstace;
	}

    private void atualizarComposicaoSessao(List<ComposicaoSessao> list) {
        for (ComposicaoSessao cs : list) {
            genericManager.update(cs);
        }
    }

    public void inserirPautaAtualizandoVotoEAnotacao(
        List<PautaSessao> pautaSessaoList) {
        for (PautaSessao ps : pautaSessaoList) {
            inserirPautaAtualizandoVotoEAnotacao(ps);
        }
    }

    public void inserirPautaAtualizandoVotoEAnotacao(PautaSessao pautaSessao) {
        pautaSessaoManager.persist(pautaSessao);

        List<Voto> votoList = new ArrayList<Voto>();
        votoList.addAll(votoManager.getVotosProcessoSemSessaoByOrgaoJugador(
                pautaSessao.getSessao(), pautaSessao.getProcessoTrf()));

        for (Voto v : votoList) {
            // [PJE-4384] IBARRA - if retirado por questões de incompatibilidade com o pacote 06. 
            // Esta condição não faz sentido, pois o voto pode ser incluído em uma sessão mesmo que esteja somente apto 
            // (não há necessidade de liberação do voto) se não vincular o ID da sessão com o voto, o sistema não consegue controlar 
            // os votos que são de uma sessão e que são de um novo processo. 
            //if(v.getLiberacao()){
            v.setSessao(pautaSessao.getSessao());
            votoManager.update(v);

            //}
        }

        List<AnotacaoVoto> anotacaoList = new ArrayList<AnotacaoVoto>();
        List<AnotacaoVoto> listTemp = new ArrayList<AnotacaoVoto>();
        listTemp.addAll(anotacaoVotoManager.getAnotacoesSemSessaoByProcesso(
                pautaSessao.getProcessoTrf()));

        //[PJEII-3218] Criação da condição para que não haja mais de uma anotação com as seguintes informações duplicadas:
        //idOrgaoJulgador, idOrgaoJulgadorColegiado e idProcessoTrf
        //Caso haja, é necessário recuperar somente o registro mais atual. A ordenação foi realizada no próprio sql
        AnotacaoVoto oldAV = null;

        for (AnotacaoVoto av : listTemp) {
            if ((oldAV != null) &&
                    !((av.getProcessoTrf().getIdProcessoTrf() == oldAV.getProcessoTrf()
                                                                          .getIdProcessoTrf()) &&
                    (av.getOrgaoJulgador().getIdOrgaoJulgador() == oldAV.getOrgaoJulgador()
                                                                            .getIdOrgaoJulgador()) &&
                    (av.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado() == oldAV.getOrgaoJulgadorColegiado()
                                                                                              .getIdOrgaoJulgadorColegiado()) &&
                    (av.getDataInclusao().compareTo(oldAV.getDataInclusao()) == -1))) {
                anotacaoList.add(av);
            }

            if (oldAV == null) {
                anotacaoList.add(av);
                oldAV = av;
            }
        }

        for (AnotacaoVoto av : anotacaoList) {
            av.setSessao(pautaSessao.getSessao());
            anotacaoVotoManager.update(av);
        }
    }

    public void inserirVotosEDocumentosPorOrgaoJulgadorDaComposicao(
        PautaSessao pautaSessao, Usuario usuario) {
        List<OrgaoJulgador> listOJ = new ArrayList<OrgaoJulgador>();
        listOJ.addAll(listOrgaoJulgadorSemVoto(pautaSessao));

        List<Voto> listVoto = new ArrayList<Voto>();
        listVoto.addAll(criarListVoto(listOJ, usuario, pautaSessao));

        inserirVotosComDocumentos(listVoto);
    }

    private DocumentoVoto criarDocumentoVoto(Voto voto,
        TipoProcessoDocumento tipoProcessoDocumento) {
        DocumentoVoto documentoVoto = new DocumentoVoto();
        documentoVoto.setAtivo(true);
        documentoVoto.setDataInclusao(new Date());
        documentoVoto.setDocumentoSigiloso(false);
        documentoVoto.setProcesso(voto.getProcessoTrf().getProcesso());
        documentoVoto.setTipoProcessoDocumento(tipoProcessoDocumento);
        documentoVoto.setProcessoDocumento("");
        documentoVoto.setVoto(voto);

        ProcessoDocumentoBin documentoVotoBin = new ProcessoDocumentoBin();
        documentoVotoBin.setDataInclusao(new Date());
        documentoVotoBin.setModeloDocumento(" ");

        documentoVoto.setProcessoDocumentoBin(documentoVotoBin);

        return documentoVoto;
    }

    private void inserirVotosComDocumentos(List<Voto> votos) {
        List<TipoProcessoDocumento> listTipoProcessoDocumento = new ArrayList<TipoProcessoDocumento>(4);
        listTipoProcessoDocumento.add(TIPO_PROCESSO_DOCUMENTO_EMENTA);
        listTipoProcessoDocumento.add(TIPO_PROCESSO_DOCUMENTO_RELATORIO);
        listTipoProcessoDocumento.add(TIPO_PROCESSO_DOCUMENTO_FUNDAMENTACAO);
        listTipoProcessoDocumento.add(TIPO_PROCESSO_DOCUMENTO_DISPOSITIVO);

        for (Voto v : votos) {
            votoManager.persist(v);

            for (TipoProcessoDocumento tpd : listTipoProcessoDocumento) {
                DocumentoVoto documentoVoto;
                documentoVoto = criarDocumentoVoto(v, tpd);
                genericManager.persist(documentoVoto.getProcessoDocumentoBin());
                genericManager.persist(documentoVoto);
            }
        }
    }

    private Voto criarVoto(OrgaoJulgador orgaoJulgador,
        PautaSessao pautaSessao, Usuario usuario) {
        Voto voto = new Voto();
        voto.setLiberacao(false);
        voto.setImpedimentoSuspeicao(false);
        voto.setOrgaoJulgador(orgaoJulgador);
        voto.setUsuarioInclusao(usuario);
        voto.setDataInclusao(new Date());
        voto.setDestaque(false);
        voto.setMarcacaoDestaque(false);
        voto.setMarcacaoDivergencia(false);
        voto.setMarcacaoObservacao(false);
        voto.setProcessoTrf(pautaSessao.getProcessoTrf());
        voto.setSessao(pautaSessao.getSessao());

        TipoVotoJT tipoVoto = null;

        if (voto.getOrgaoJulgador()
                    .equals(pautaSessao.getProcessoTrf().getOrgaoJulgador())) {
            for (TipoVotoJT tv : tipoVotoJTManager.getTipoVotoRelator()) {
                if (tv.getConclusao().equals(ConclusaoEnum.RE)) {
                    tipoVoto = tv;

                    break;
                }
            }
        }

        if (tipoVoto != null) {
            voto.setTipoVoto(tipoVoto);
            voto.setUsuarioTipoVoto(usuario);
            voto.setDataTipoVoto(new Date());
        }

        return voto;
    }

    public Boolean processoJaPossuiEventoLancadoNaSessao(Processo processo,
        SessaoJT sessaoJT) {
        if (SituacaoSessaoEnum.E.equals(sessaoJT.getSituacaoSessao())) {
            // Recupera-se o agrupamento configurado para os eventos durante a sessão
            Integer idAgrupamentoJulgamento = ParametroUtil.instance()
                                                           .getAgrupamentoJulgamento();
            Agrupamento agrupamento = genericDAO.find(Agrupamento.class,
                    idAgrupamentoJulgamento);

            for (EventoAgrupamento eventoAgrupamento : agrupamento.getEventoAgrupamentoList()) {
                // Todos os movimentos do processo que lançaram algum evento pertencente ao agrupamento de julgamento
                // e que foram lançados após o encerramento da sessão
                List<ProcessoEvento> listMovimentosAgrupamentoJulgamentoLancadosAposEncerramento =
                    processoEventoManager.getMovimentosLancadosParaProcesso(eventoAgrupamento.getEvento(),
                        sessaoJT, processo);

                if (listMovimentosAgrupamentoJulgamentoLancadosAposEncerramento.size() > 0) {
                    return true;
                }
            }
        }
        //Caso a sessão esteja fechada, todos os eventos já devem ter sido lançados
        else if (SituacaoSessaoEnum.F.equals(sessaoJT.getSituacaoSessao())) {
            return true;
        }

        return false;
    }

    public void iniciarSessao(SessaoJT sessao, Usuario usuario) {
        List<PautaSessao> listPauta = new ArrayList<PautaSessao>();
        listPauta.addAll(pautaSessaoManager.listaPautaSessaoBySessao(sessao));

        TipoSituacaoPauta tipoSituacaoPautaPendente = ParametroUtil.instance()
                                                                   .getTipoSituacaoPautaPendente();

        for (PautaSessao ps : listPauta) {
            historicoSituacaoPautaManager.gravarHistorico(ps);

            pautaSessaoManager.atualizarSituacaoPauta(ps,
                tipoSituacaoPautaPendente, usuario);

            inserirVotosEDocumentosPorOrgaoJulgadorDaComposicao(ps, usuario);
        }

        historicoSituacaoSessaoManager.gravarHistorico(sessao);

        sessaoManager.atualizarSituacaoSessao(sessao, SituacaoSessaoEnum.I,
            usuario);
    }

    private List<OrgaoJulgador> listOrgaoJulgadorSemVoto(
        PautaSessao pautaSessao) {
        List<OrgaoJulgador> listOJ = new ArrayList<OrgaoJulgador>();
        listOJ.addAll(composicaoSessaoManager.getOrgaoJulgadorBySessao(
                pautaSessao.getSessao()));

        List<Voto> listVoto = new ArrayList<Voto>();
        listVoto.addAll(votoManager.getVotosByProcessoSessao(
                pautaSessao.getProcessoTrf(), pautaSessao.getSessao()));

        for (Voto v : listVoto) {
            if (listOJ.contains(v.getOrgaoJulgador())) {
                listOJ.remove(v.getOrgaoJulgador());
            }
        }

        return listOJ;
    }

    private List<Voto> criarListVoto(List<OrgaoJulgador> listOrgaoJulgador,
        Usuario usuario, PautaSessao pautaSessao) {
        List<Voto> listVoto = new ArrayList<Voto>();

        for (OrgaoJulgador oj : listOrgaoJulgador) {
            listVoto.add(criarVoto(oj, pautaSessao, usuario));
        }

        return listVoto;
    }

    /**
 	 * [PJEII-4425] (18/12/2012) Os processos não devem voltar pra situação pendente ao serem marcados pelo checkbox
	 * @author Antonio Lucas
     */
    public void apregoarProcesso(PautaSessao pautaSessao, Usuario usuario) {
        PautaSessao pautaProcessoApregoado = pautaSessaoManager.getPautaProcessoApregoadoBySessao(pautaSessao.getSessao());

        if (pautaProcessoApregoado != null) {
            historicoSituacaoPautaManager.gravarHistorico(pautaProcessoApregoado);
            pautaSessaoManager.atualizarSituacaoPauta(pautaProcessoApregoado,
                TIPO_SITUACAO_PAUTA_PENDENTE, usuario);
        }

        historicoSituacaoPautaManager.gravarHistorico(pautaSessao);
        pautaSessaoManager.atualizarSituacaoPauta(pautaSessao,
            TIPO_SITUACAO_PAUTA_APREGOADO, usuario);
        votoManager.liberarVotos(pautaSessao.getProcessoTrf(),
            pautaSessao.getSessao());
    }

    public void updateSituacaoPautaSessao(List<PautaSessao> listaPautaSessao, Usuario usuario, TipoSituacaoPauta tipoSituacaoPauta){
    	for (PautaSessao pautaSessao : listaPautaSessao) {
    		updateSituacaoPautaSessao(pautaSessao, usuario, tipoSituacaoPauta);
    	}
    }
    
    public void updateSituacaoPautaSessao(PautaSessao pautaSessao, Usuario usuario, TipoSituacaoPauta tipoSituacaoPauta){
    	historicoSituacaoPautaManager.gravarHistorico(pautaSessao);
		pautaSessaoManager.atualizarSituacaoPauta(pautaSessao, tipoSituacaoPauta, usuario);
		if(tipoSituacaoPauta.equals(TIPO_SITUACAO_PAUTA_APREGOADO)){
			votoManager.liberarVotos(pautaSessao.getProcessoTrf(), pautaSessao.getSessao());
		}
    }
    
    public void atualizarSituacaoVariosProcessosEmPauta(
	        List<PautaSessao> listPautaSessao, Usuario usuario,
	        TipoSituacaoPauta tipoSituacaoPauta) {
    	for (PautaSessao ps : listPautaSessao) {
            historicoSituacaoPautaManager.gravarHistorico(ps);
            pautaSessaoManager.atualizarSituacaoPauta(ps, tipoSituacaoPauta,
                usuario);
        }
    }
    
	public void atualizarSituacaoVariosProcessosEmPauta(
			List<PautaSessao> listPautaSessao, Usuario usuario,
			TipoSituacaoPauta tipoSituacaoPauta, boolean situacaoAnalise) {
		for (PautaSessao ps : listPautaSessao) {
            historicoSituacaoPautaManager.gravarHistorico(ps);
            ps.setSituacaoAnalise(situacaoAnalise ? SituacaoAnaliseEnum.A : SituacaoAnaliseEnum.N);
            pautaSessaoManager.atualizarSituacaoPauta(ps, tipoSituacaoPauta,
                usuario);
        }
	}

    public void verificarPendenciasSessao(SessaoJT sessao) {
        if (pautaSessaoManager.existeProcessoPendente(sessao)) {
            throw new NegocioException(Messages.instance()
                                               .get(MSG_PROCESSO_PENDENTE));
        }

        // Removido pq a conclusão não é mais obrigatória
        // Será obrigatória novamente quando existir um modo automatizado de preencher o placar de votação
//        if (pautaSessaoManager.existeProcessoJulgadoSemConclusao(sessao)) {
//            throw new NegocioException(Messages.instance()
//                                               .get(MSG_PROCESSO_JULGADO_SEM_CONCLUSAO));
//        }
    }

    @CustomJbpmTransactional
    public void encerrarSessao(SessaoJT sessao, Usuario usuario) {
        if (pautaSessaoManager.existeProcessoPendente(sessao)) {
            throw new NegocioException(Messages.instance()
                                               .get(MSG_PROCESSO_PENDENTE));
        }

        // Removido pq a conclusão não é mais obrigatória
        // Será obrigatória novamente quando existir um modo automatizado de preencher o placar de votação
//        if (pautaSessaoManager.existeProcessoJulgadoSemConclusao(sessao)) {
//            throw new NegocioException(Messages.instance()
//                                               .get(MSG_PROCESSO_JULGADO_SEM_CONCLUSAO));
//        }

        List<PautaSessao> listPauta = new ArrayList<PautaSessao>();
        listPauta.addAll(pautaSessaoManager.getProcessosRetiradoPautaOuDeliberado(
                sessao));

        registrarEventoSituacaoPauta(usuario, listPauta);

        historicoSituacaoSessaoManager.gravarHistorico(sessao);

        sessaoManager.atualizarSituacaoSessao(sessao, SituacaoSessaoEnum.E,
            usuario);

        remeterNovoRelator(sessao);

    }

    @CustomJbpmTransactional
	public void movimentarProcessoEncerrarSessao(ProcessoTrf processoTrf, Integer idTarefa) throws Exception{
		
    	moverParaDefaultTransition(processoTrf, idTarefa, "Mover default transition de sessão julgamento");
    	
	}
    
    /**
     * Após a assinatura do acórdão, faz a movimentação para o default transition
     * da tarefa assinar acordao
     * @param processoTrf
     * @param idTarefa
     * @throws Exception
     */
    @CustomJbpmTransactional
    public void movimentarProcessoAssinarAcordao(ProcessoTrf processoTrf,
			Integer idTarefa) throws Exception {
		
		moverParaDefaultTransition(processoTrf, idTarefa, "Mover default transition de assinar acórdão");
		
	}

    private void remeterNovoRelator(SessaoJT sessao) {
        List<PautaSessao> listPautaJulgados = new ArrayList<PautaSessao>();
        listPautaJulgados.addAll(pautaSessaoManager.getProcessosJulgados(sessao));

        //Remeter os autos para o gabinete do redator, quando este for diferente do relator
        // e somente para processos julgados
        for (PautaSessao pautaSessao : listPautaJulgados) {
            ProcessoTrf processoTrf = pautaSessao.getProcessoTrf();
            OrgaoJulgador orgaoJulgadorRedator = pautaSessao.getOrgaoJulgadorRedator();
            OrgaoJulgador orgaoJulgadorRelator = processoTrf.getOrgaoJulgador();
            boolean redatorDiferenteDoRelator = (orgaoJulgadorRedator != null) &&
                !orgaoJulgadorRedator.equals(orgaoJulgadorRelator);

            if (redatorDiferenteDoRelator) {
                //Mudar Órgão Julgador responsável pelo processo
                processoTrf.setOrgaoJulgador(pautaSessao.getOrgaoJulgadorRedator());
                //Retirar da caixa
                processoTrf.getProcesso().setCaixa(null);
                processoTrf.setPessoaRelator(pautaSessao.getMagistradoRedator());

                //Alterar a responsabilidade para o magistrado(OrgaoJulgadorCargo) do gabinete(OrgaoJulgador)
                // que está ativo e recebe distribuição
                List<OrgaoJulgadorCargo> orgaoJulgadorCargoList = pautaSessao.getOrgaoJulgadorRedator()
                                                                             .getOrgaoJulgadorCargoList();

                for (OrgaoJulgadorCargo orgaoJulgadorCargo : orgaoJulgadorCargoList) {
                    if (orgaoJulgadorCargo.getAtivo() &&
                            orgaoJulgadorCargo.getRecebeDistribuicao()) {
                        processoTrf.setOrgaoJulgadorCargo(orgaoJulgadorCargo);

                        break;
                    }
                }

                genericManager.update(processoTrf);
                genericManager.update(processoTrf.getProcesso());

                //Atribuir o relator originário (somente para JT)
                ProcessoJT processoJt = processoJtManager.getProcessoJtPorId(processoTrf.getIdProcessoTrf());
                processoJt.setOrgaoJulgadorRelatorOriginario(orgaoJulgadorRelator);
                genericManager.update(processoJt);

                // Lançar movimentos de recibidos os autos
                // **************************************************************************************
                MovimentoAutomaticoService.preencherMovimento()
                                          .deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_COMUNICACAO_RECEBIMENTO)
                                          .associarAoProcesso(processoTrf)
                                          .lancarMovimento();
            }
        }
    }

    public String fecharSessao(SessaoJT sessao, Usuario usuario)
        throws InstantiationException, IllegalAccessException {
        if (existeProcessoJulgadoSemMovimento(sessao)) {
            return MSG_PROCESSO_JULGADO_SEM_MOVIMENTO;
        }

        copiarVotosProcessosNaoJulgados(sessao);
        desmarcarAptoPautaJulgamentoProcessosJulgados(sessao);

        historicoSituacaoSessaoManager.gravarHistorico(sessao);

        sessaoManager.atualizarSituacaoSessao(sessao, SituacaoSessaoEnum.F,
            usuario);

        return null;
    }

    private boolean existeProcessoJulgadoSemMovimento(SessaoJT sessao) {
        List<PautaSessao> processosJulgados = pautaSessaoManager.getProcessosJulgados(sessao);

        for (PautaSessao pautaSessao : processosJulgados) {
            if (!processoJaPossuiEventoLancadoNaSessao(
                        pautaSessao.getProcessoTrf().getProcesso(),
                        pautaSessao.getSessao())) {
                return true;
            }
        }

        return false;
    }

    private void copiarVotosProcessosNaoJulgados(SessaoJT sessao)
        throws InstantiationException, IllegalAccessException {
        List<PautaSessao> processosNaoJulgados = pautaSessaoManager.getProcessosRetiradoPautaOuDeliberado(sessao);

        for (PautaSessao pautaSessao : processosNaoJulgados) {
            votoManager.copiarVotos(pautaSessao.getProcessoTrf(), sessao);
        }
    }

    private void desmarcarAptoPautaJulgamentoProcessosJulgados(SessaoJT sessao) {
        List<PautaSessao> processosJulgados = pautaSessaoManager.getProcessosJulgados(sessao);

        for (PautaSessao pautaSessao : processosJulgados) {
            pautaSessao.getProcessoTrf().setSelecionadoPauta(false);
            pautaSessao.getProcessoTrf().setSelecionadoJulgamento(false);
            genericManager.update(pautaSessao);
        }
    }

    private void registrarEventoSituacaoPauta(Usuario usuario,
        List<PautaSessao> listPauta) {
        for (PautaSessao ps : listPauta) {
            // Retirado de Pauta
            if (ps.getTipoSituacaoPauta().getClassificacao()
                      .equals(ClassificacaoTipoSituacaoPautaEnum.R)) {
                // Código = 897 - Descrição = Retirado de pauta o processo
                // **************************************************************************************
                MovimentoAutomaticoService.preencherMovimento()
                                          .deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_RETIRADO_PAUTA)
                                          .associarAoProcesso(ps.getProcessoTrf()
                                                                .getProcesso())
                                          .lancarMovimento();

                // Movimento Deliberado em Sessão
            } else if (ps.getTipoSituacaoPauta().getClassificacao()
                             .equals(ClassificacaoTipoSituacaoPautaEnum.D)) {
                // Código = 873 - Descrição = Deliberado em sessão (#{tipo de deliberação})
                // **************************************************************************************
                if (ps.getTipoSituacaoPauta().getCodigoTipoSituacaoPauta()
                          .equals(TipoSituacaoPautaJTEnum.PV.name())) {
                    MovimentoAutomaticoService.preencherMovimento()
                                              .deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_PEDIDO_VISTA)
                                              .associarAoProcesso(ps.getProcessoTrf()
                                                                    .getProcesso())
                                              .lancarMovimento();
                } else if (ps.getTipoSituacaoPauta().getCodigoTipoSituacaoPauta()
                                 .equals(TipoSituacaoPautaJTEnum.AD.name())) {
                    MovimentoAutomaticoService.preencherMovimento()
                                              .deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_ADIADO)
                                              .associarAoProcesso(ps.getProcessoTrf()
                                                                    .getProcesso())
                                              .lancarMovimento();
                } else if (ps.getTipoSituacaoPauta().getCodigoTipoSituacaoPauta()
                                 .equals(TipoSituacaoPautaJTEnum.CD.name())) {
                    MovimentoAutomaticoService.preencherMovimento()
                                              .deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_CONVERTIDO_DILIGENCIA)
                                              .associarAoProcesso(ps.getProcessoTrf()
                                                                    .getProcesso())
                                              .lancarMovimento();
                } else if (ps.getTipoSituacaoPauta().getCodigoTipoSituacaoPauta()
                                 .equals(TipoSituacaoPautaJTEnum.RC.name())) {
                    MovimentoAutomaticoService.preencherMovimento()
                                              .deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_RETIFICADO_JULGAMENTO)
                                              .associarAoProcesso(ps.getProcessoTrf()
                                                                    .getProcesso())
                                              .lancarMovimento();
                } else if (ps.getTipoSituacaoPauta().getCodigoTipoSituacaoPauta()
                                 .equals(TipoSituacaoPautaJTEnum.SS.name())) {
                    MovimentoAutomaticoService.preencherMovimento()
                                              .deCodigo(CodigoMovimentoNacional.CODIGO_MOVIMENTO_SESSAO_SUSPENSO_SOBRESTADO)
                                              .associarAoProcesso(ps.getProcessoTrf()
                                                                    .getProcesso())
                                              .lancarMovimento();
                }
            }
        }
    }

    public boolean podeAssinarAcordao(PautaSessao pautaSessao,
        Usuario usuarioLogado) {
        if ((usuarioLogado != null) && (pautaSessao != null) &&
                (pautaSessao.getTipoSituacaoPauta() != null) &&
                (pautaSessao.getTipoSituacaoPauta().getClassificacao() != null) &&
                (pautaSessao.getProcessoTrf() != null)) {
            if ((sessaoEncerrada(pautaSessao.getSessao()) ||
                    sessaoFechada(pautaSessao.getSessao())) &&
                    ((pautaSessao.getMagistradoRedator() != null) &&
                    usuarioLogado.getIdUsuario()
                                     .equals(pautaSessao.getMagistradoRedator()
                                                            .getIdUsuario())) &&
                    pautaSessao.getTipoSituacaoPauta().getClassificacao()
                                   .equals(ClassificacaoTipoSituacaoPautaEnum.J)) {
                return true;
            } else if ((sessaoEncerrada(pautaSessao.getSessao()) ||
                    sessaoFechada(pautaSessao.getSessao())) &&
                    (pautaSessao.getMagistradoRedator() == null)){
            	atualizarMagistradoRedator(pautaSessao);
            }
        }

        return false;
    }

	public void atualizarMagistradoRedator(PautaSessao pautaSessao) {
		OrgaoJulgador orgaoJulgadorRedator = getOrgaoJulgadorRedator(pautaSessao.getProcessoTrf(), pautaSessao.getSessao());
		ComposicaoProcessoSessao composicaoProcessoSessao = composicaoProcessoSessaoManager.getComposicaoProcessoSessao(pautaSessao.getProcessoTrf(), pautaSessao.getSessao(), orgaoJulgadorRedator);
		
		pautaSessao.setOrgaoJulgadorRedator(composicaoProcessoSessao.getComposicaoSessao().getOrgaoJulgador());
		if (composicaoProcessoSessao.getMagistradoSubstituto() != null) {
		    pautaSessao.setMagistradoRedator(composicaoProcessoSessao.getMagistradoSubstituto());
		} else if (composicaoProcessoSessao.getComposicaoSessao().getMagistradoSubstituto() != null) {
		    pautaSessao.setMagistradoRedator(composicaoProcessoSessao.getComposicaoSessao().getMagistradoSubstituto());
		} else {
			pautaSessao.setMagistradoRedator(composicaoProcessoSessao.getComposicaoSessao().getMagistradoPresente());
		}
		pautaSessaoManager.update(pautaSessao);
	}
    
    public OrgaoJulgador getOrgaoJulgadorRedator(ProcessoTrf processoTrf, SessaoJT sessaoJT) {
            OrgaoJulgador retorno = pautaSessaoManager.getOrgaoJulgadorRedatorByProcessoSessao(processoTrf,
                    sessaoJT);

            // Se sessão estiver encerrada ou fechada...
            if ((sessaoJT != null) && (processoTrf != null) &&
                    (SituacaoSessaoEnum.E.equals(sessaoJT.getSituacaoSessao()) ||
                    SituacaoSessaoEnum.F.equals(sessaoJT.getSituacaoSessao()))) {
                ProcessoJT processoJt = processoJtManager.getProcessoJtPorId(processoTrf.getIdProcessoTrf());

                if ((processoJt != null) &&
                        (processoJt.getOrgaoJulgadorRelatorOriginario() != null)) {
                    retorno = processoTrf.getOrgaoJulgador();
                }
            }

        return retorno;
    }

    private boolean isDocumentoVotoVazio(DocumentoVoto documentoVotoEmenta) {
        return (documentoVotoEmenta == null) ||
        (documentoVotoEmenta.getProcessoDocumentoBin() == null) ||
        (documentoVotoEmenta.getProcessoDocumentoBin().getModeloDocumento() == null) ||
        documentoVotoEmenta.getProcessoDocumentoBin().getModeloDocumento().trim()
                           .equals("");
    }

    public boolean sessaoFechada(SessaoJT sessao) {
        return (sessao != null) &&
        sessao.getSituacaoSessao().equals(SituacaoSessaoEnum.F);
    }

    public boolean sessaoEncerrada(SessaoJT sessao) {
        return (sessao != null) &&
        sessao.getSituacaoSessao().equals(SituacaoSessaoEnum.E);
    }
    
    public boolean sessaoIniciada(SessaoJT sessao) {
    	return (sessao != null) &&
    	        sessao.getSituacaoSessao().equals(SituacaoSessaoEnum.I);
    }
    
    public boolean sessaoAguardandoSessao(SessaoJT sessao) {
    	return (sessao != null) &&
    	        sessao.getSituacaoSessao().equals(SituacaoSessaoEnum.S);
    }
    
    public boolean sessaoAberta(SessaoJT sessao) {
    	return (sessao != null) &&
    	        sessao.getSituacaoSessao().equals(SituacaoSessaoEnum.A);
    }

    @SuppressWarnings("unchecked")
    public boolean isAssinadoAcordaoProcurador(PautaSessao pautaSessao,
        Pessoa pessoaLogada, TipoProcessoDocumento tipoProcessoDocumento) {
        try {
            List<ProcessoDocumentoBinPessoaAssinatura> listProcessoDocumentoBinPessoaAssinatura =
                (List<ProcessoDocumentoBinPessoaAssinatura>) EntityUtil.getEntityManager()
                                                                       .createQuery("select pdbpa from ProcessoDocumentoBinPessoaAssinatura pdbpa " +
                    "where exists ( select pd from ProcessoDocumento pd where pd.processoDocumentoBin = pdbpa.processoDocumentoBin " +
                    " and pd.processo= " +
                    pautaSessao.getProcessoTrf().getIdProcessoTrf() +
                    " and pd.tipoProcessoDocumento.idTipoProcessoDocumento=" +
                    tipoProcessoDocumento.getIdTipoProcessoDocumento() +
                    " and pd.ativo=true )").getResultList();

            //Se o documento já foi assinado pelo procurador logado
            for (ProcessoDocumentoBinPessoaAssinatura processoDocumentoBinPessoaAssinatura : listProcessoDocumentoBinPessoaAssinatura) {
                if (processoDocumentoBinPessoaAssinatura.getPessoa()
                                                            .equals(pessoaLogada) &&
                        (processoDocumentoBinPessoaAssinatura.getAssinatura() != null) &&
                        (processoDocumentoBinPessoaAssinatura.getCertChain() != null)) {
                    return true;
                }
            }
        } catch (NoResultException e) {
            return false;
        }

        return false;
    }

    public boolean podeAssinarAcordaoProcurador(PautaSessao pautaSesssao,
        Voto voto) {
        ProcessoDocumento processoDocumentoJaExistente = documentoVotoManager.getUltimoDocumentoVoto(AbstractSessaoJulgamentoAction.TIPO_PROCESSO_DOCUMENTO_ACORDAO,
                pautaSesssao.getProcessoTrf().getProcesso(), voto);

        //Se documento já existe e já foi assinado
        if ((processoDocumentoJaExistente != null) &&
                (processoDocumentoJaExistente.getProcessoDocumentoBin() != null) &&
                processoDocumentoManager.isDocumentoAssinado(
                    processoDocumentoJaExistente.getProcessoDocumentoBin())) {
            return true;
        }

        return false;
    }

    public void persistEAssinarProcessoDocumentoBin(
        ProcessoDocumentoBin procDocBin, String certChain, String signature,
        TipoProcessoDocumento tipoProcessoDocumento, Pessoa pessoaAssinatura) {
        try {
            if ((certChain != null) && (signature != null)) {
                procDocBin.setCertChain(certChain);
                procDocBin.setSignature(signature);
            }

            VerificaCertificadoPessoa.verificaCertificadoPessoaLogada(procDocBin.getCertChain());
        } catch (Exception e) {
            throw new AplicationException("Erro ao assinar o documento: " +
                e.getMessage() + ".");
        }

        pautaSessaoManager.persist(procDocBin);

        try {
            processoDocumentoBinManager.finalizaProcessoDocumentoBin(procDocBin,
                tipoProcessoDocumento, pessoaAssinatura);
        } catch (PJeBusinessException e) {
            throw new AplicationException("Erro ao assinar o documento: " +
                e.getMessage() + ".");
        }
    }
    
	public boolean isPautaAberta(SessaoJT sessao){
		Boolean retorno = false;
		if (sessao != null && sessao.getSituacaoSessao().equals(SituacaoSessaoEnum.A)){
			retorno = true;
		}
		return retorno;
	}

	
	 public Long obtemIdTaskInstacePorProcesso(ProcessoTrf processoTrf, Integer idTarefa){
        ConsultaProcessoTrfManager consultaProcessoTrfManager = (ConsultaProcessoTrfManager)br.com.itx.util.ComponentUtil.getComponent("consultaProcessoTrfManager");
       
        ConsultaProcessoVO consultaProcessoVO = consultaProcessoTrfManager.consultaProcessoVO(processoTrf,idTarefa);
       
        Long idTaskInstance = consultaProcessoVO.getIdTaskInstance();
        return idTaskInstance;
	 }
	 
	 public Integer obtemIdTaskPorProcesso(ProcessoTrf processoTrf, Integer idTarefa){
	        ConsultaProcessoTrfManager consultaProcessoTrfManager = (ConsultaProcessoTrfManager)br.com.itx.util.ComponentUtil.getComponent("consultaProcessoTrfManager");
	       
	        ConsultaProcessoVO consultaProcessoVO = consultaProcessoTrfManager.consultaProcessoVO(processoTrf, idTarefa);
	       
	        Integer idTask = consultaProcessoVO.getIdTask();
	        return idTask;
	}
	 
	public Integer obtemIdTaskPorProcesso(ProcessoTrf processoTrf, String nomeTarefa){
	        ConsultaProcessoTrfManager consultaProcessoTrfManager = (ConsultaProcessoTrfManager)br.com.itx.util.ComponentUtil.getComponent("consultaProcessoTrfManager");
	       
	        ConsultaProcessoVO consultaProcessoVO = consultaProcessoTrfManager.consultaProcessoVO(processoTrf, nomeTarefa);
	       
	        Integer idTask = consultaProcessoVO.getIdTask();
	        return idTask;
	}
	
	 
	
	
	public void moverParaDefaultTransition(ProcessoTrf processo, Integer idTarefa, String historico) throws Exception{
		HistoricoMovimentacaoLote hml = atividadesLoteService.adicionaHistoricoMovimentacao(historico);
		
		Long idTask = obtemIdTaskInstacePorProcesso(processo, idTarefa);
		Long idDefaultTransition = obtemIdDefaultTransition(idTask, processo);
		
		if (idDefaultTransition == null) {
			new Exception("Erro ao transitar processo. Não foi configurado o defaultTransition no fluxo.");
		}

		iniciarHomesProcessos(processo);
		iniciarBusinessProcess(idTask);

		atividadesLoteService.transitarProcesso(idTask, idDefaultTransition, processo, hml);

	}

	public void moverParaDefaultTransitionMovendoParaCaixa(ProcessoTrf processoTrf, String nomeCaixa) throws Exception{

		Integer idTarefa = Integer.valueOf(ParametroUtil.getParametro("idTarefaInclusaoPauta"));
		Long idTaskInstace = obtemIdTaskInstacePorProcesso(processoTrf, idTarefa);
		iniciarHomesProcessos(processoTrf);
		iniciarBusinessProcess(idTaskInstace);
		ParametroUtil parametroUtil = ParametroUtil.instance();
		
		String defaultTransition = (String) TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);
		moverParaDefaultTransition(processoTrf,parametroUtil.getIdTarefaInclusaoPauta(),"Mover default transition de pauta");
		
		Integer idTask = obtemIdTaskPorProcesso(processoTrf, defaultTransition);
		Tarefa tarefa = genericManager.find(Tarefa.class, idTask);
		
        //Adiciona processo em uma caixa
        CaixaFiltro caixa = caixaFiltroManager.existsCaixaByNomeAndTarefa(nomeCaixa, tarefa);
        
        //Se a caixa não existir cria a caixa
        if(caixa == null){
        	
        	caixa = CaixaFiltroHome.instance().addCaixaComRetorno(idTask, nomeCaixa);
        }
        
        List<Integer> listaIdProcesso = new ArrayList<Integer>();
		if(processoTrf != null){
			listaIdProcesso.add(processoTrf.getIdProcessoTrf());
		}
		PainelUsuarioHome.instance().setProcessoCaixa(listaIdProcesso, caixa);
	}

	
	public Long obtemIdDefaultTransition(Long idTaskOrigem, ProcessoTrf processoTrf){

		iniciarHomesProcessos(processoTrf);
		iniciarBusinessProcess(idTaskOrigem);

		String defaultTransition = (String) TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);

		ProcessInstance pi = org.jboss.seam.bpm.ProcessInstance.instance();

		Long idDestino = null;

		Token tk = pi.getRootToken();
		if (!tk.getProcessInstance().hasEnded() && tk.getAvailableTransitions().size() > 0){
			if (defaultTransition != null){
				for (Transition t : tk.getAvailableTransitions()){
					if (t.getName().equals(defaultTransition)){
						idDestino = t.getId();
						break;
					}
				}
			}
		}

		return idDestino;
	}

	public void iniciarHomesProcessos(ProcessoTrf processoTrf){
		fluxoService.iniciarHomesProcessos(processoTrf);
	}

	public void iniciarBusinessProcess(Long idTaskInstance){
		fluxoService.iniciarBusinessProcess(idTaskInstance);
	}

	public AtividadesLoteService getAtividadesLoteService(){
		return atividadesLoteService;
	}

	public void setAtividadesLoteService(AtividadesLoteService atividadesLoteService){
		this.atividadesLoteService = atividadesLoteService;
	}

	public Long recuperarDefaultTransition(Long idTaskInstance){
		Transition frameDefaultTransition = atividadesLoteService.getFrameDefaultLeavingTransition(idTaskInstance);
		return frameDefaultTransition.getId();
	}
	
    
    public boolean possuiVistaRegimental(){
    	
    	ProcessoTrfHome processoTrfHome = ProcessoTrfHome.instance();
    	ProcessoTrf processoTrf = processoTrfHome.getInstance();
    	

    	PautaSessao ultimaPautaByProcesso = pautaSessaoManager.getUltimaPautaByProcesso(processoTrf);
    	TipoSituacaoPauta tipoSituacaoPauta = ultimaPautaByProcesso.getTipoSituacaoPauta();
    	if(tipoSituacaoPauta.getCodigoTipoSituacaoPauta().equalsIgnoreCase(TipoSituacaoPautaJTEnum.PV.name())){
    		return true;
    	} 
    	
    	return false;
    	
    }

	
	
}
