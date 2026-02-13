package br.com.jt.pje.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.editor.manager.ProcessoDocumentoEstruturadoManager;
import br.com.infox.editor.service.EditorService;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.manager.ProcessoJtManager;
import br.com.infox.pje.service.PautaJulgamentoService;
import br.com.infox.utils.ItensLegendas;
import br.com.infox.view.GenericAction;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.jt.pje.bean.SearchProcessosPautaBean;
import br.com.jt.pje.list.ElaboracaoVotoList;
import br.com.jt.pje.manager.AnotacaoVotoManager;
import br.com.jt.pje.manager.ComposicaoProcessoSessaoManager;
import br.com.jt.pje.manager.ComposicaoSessaoManager;
import br.com.jt.pje.manager.DocumentoVotoManager;
import br.com.jt.pje.manager.PautaSessaoManager;
import br.com.jt.pje.manager.TipoVotoJTManager;
import br.com.jt.pje.manager.VotoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.pje.jt.entidades.AnotacaoVoto;
import br.jus.pje.jt.entidades.ComposicaoProcessoSessao;
import br.jus.pje.jt.entidades.DocumentoVoto;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.ProcessoJT;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.TipoVotoJT;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.jt.enums.ConclusaoEnum;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.editor.EstruturaDocumentoTopico;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturado;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.enums.editor.TipoTopicoEnum;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.DateUtil;


public abstract class AbstractVotoSessaoAction extends GenericAction {
    /**
     *
     */
    private static final long serialVersionUID = -6086661829465843875L;
    private static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_EMENTA =
    		ParametroUtil.instance().getTipoProcessoDocumentoEmenta();
    private static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_RELATORIO =
        ParametroUtil.instance().getTipoProcessoDocumentoRelatorio();
    private static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_FUNDAMENTACAO =
        ParametroUtil.instance().getTipoProcessoDocumentoFundamentacao();
    private static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_DISPOSITIVO =
        ParametroUtil.instance().getTipoProcessoDocumentoDispositivo();
    private Integer idProcesso;
    protected ProcessoTrf processoTrf;
    private Integer idSessao;
    private SessaoJT sessao;
    private Integer idVoto;
    private Voto voto;    private TipoVotoJT tipoVoto;
    private ElaboracaoVotoList elaboracaoVotoList = new ElaboracaoVotoList();
    private List<PautaSessao> listaPautaSessaoGrid = new ArrayList<PautaSessao>();
    private AnotacaoVoto anotacaoVoto;
    private List<AnotacaoVoto> anotacoesVotoEmOutrasSessoes;
    private AnotacaoVoto anotacaoSelecionada;
    private String anotacoes;
    private String possuiAnotacoes;
    private OrgaoJulgador orgaoJulgadorVoto;
    private OrgaoJulgador orgaoJulgadorRedator;
    private Integer idPautaSessao;
    private PautaSessao pautaSessao;
    protected Map<String, Boolean> mapLegenda = new HashMap<String, Boolean>();
    private DocumentoVoto documentoVotoEmenta;
    private DocumentoVoto documentoVotoRelatorio;
    private DocumentoVoto documentoVotoFundamentacao;
    private DocumentoVoto documentoVotoDispDecisorio;
    private String modeloEmenta;
    private String modeloRelatorio;
    private String modeloFundamentacao;
    private String modeloDispositivo;
    private String modeloIntegra;
    private boolean checkBoxSelecionarTodos;
    private boolean acordaoPublicado;
    private String certChain;
    private String signature;
    private ComposicaoProcessoSessao composicaoRedator;
    private ComposicaoProcessoSessao composicaoRelator;
    @In
    protected DocumentoVotoManager documentoVotoManager;
    @In
    protected ComposicaoSessaoManager composicaoSessaoManager;
    @In
    protected ProcessoDocumentoBinManager processoDocumentoBinManager;
    @In
    protected VotoManager votoManager;
    @In
    protected TipoVotoJTManager tipoVotoJTManager;
    @In
    protected AnotacaoVotoManager anotacaoVotoManager;
    @In
    protected PautaSessaoManager pautaSessaoManager;
    @In
    protected PautaJulgamentoService pautaJulgamentoService;
    @In
    protected ProcessoDocumentoManager processoDocumentoManager;
    @In
    protected ProcessoJtManager processoJtManager;
    @In
    protected ComposicaoProcessoSessaoManager composicaoProcessoSessaoManager;
    private PautaSessao pautaSessaoAnterior = new PautaSessao();
    private Voto votoAnterior = new Voto();
    
    @In
    protected EditorService editorService;
    
    @In
    protected ProcessoDocumentoEstruturadoManager processoDocumentoEstruturadoManager;
    
    protected EstruturaDocumentoTopico estruturaProcDocDispVoto;
    protected EstruturaDocumentoTopico estruturaProcDocDispSessao;

    //Atributos para armazenar votos por pautas para diminuir consultas ao banco de dados
    private HashMap<PautaSessao, Boolean> existeVotoComDivergenciaMap = new HashMap<PautaSessao, Boolean>();
    private HashMap<PautaSessao, Boolean> existeVotoComDestaqueMap = new HashMap<PautaSessao, Boolean>();
    private HashMap<PautaSessao, Boolean> existeVotoComObservacaoMap = new HashMap<PautaSessao, Boolean>();
    
    public void inicializar() {
        setProcessoTrf(findById(ProcessoTrf.class, getIdProcesso()));

        OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
        OrgaoJulgadorColegiado orgaoJulgadorColegiadoAtual = Authenticator.getOrgaoJulgadorColegiadoAtual();

        if (getSessao() == null) {
            setVoto(votoManager.getUltimoVotoMagistradoByProcessoOrgaoJulgador(
                    getProcessoTrf(), orgaoJulgadorAtual));
        } else {
            setVoto(votoManager.getUltimoVotoByOrgaoJulgadorProcessoSessao((orgaoJulgadorAtual != null)
                    ? orgaoJulgadorAtual : processoTrf.getOrgaoJulgador(),
                    getProcessoTrf(), getSessao()));

            if ((voto == null) && (idVoto != null)) {
                setVoto(EntityUtil.find(Voto.class, idVoto));
            }
        }

        if ((getVoto() != null) && (getVoto().getSessao() != null)) {
            if (orgaoJulgadorAtual != null) {
                setAnotacaoVoto(anotacaoVotoManager.getAnotacaoVotoByProcessoSessaoOrgaoJulgadorEColegiado(
                        getProcessoTrf(), getVoto().getSessao(),
                        orgaoJulgadorAtual, orgaoJulgadorColegiadoAtual));
                setAnotacoesVotoEmOutrasSessoes(anotacaoVotoManager.getAnotacoesVotoByProcessoSessaoOrgaoJulgadorEColegiado(
                        getProcessoTrf(), getVoto().getSessao(),
                        orgaoJulgadorAtual, orgaoJulgadorColegiadoAtual));
            } else {
                setAnotacaoVoto(anotacaoVotoManager.getAnotacaoVotoSemOJByProcessoSessaoEColegiado(
                        getProcessoTrf(), getVoto().getSessao(),
                        orgaoJulgadorColegiadoAtual));
                setAnotacoesVotoEmOutrasSessoes(anotacaoVotoManager.getAnotacoesVotoByProcessoEColegiadoExcluindoSessaoAtual(
                        getProcessoTrf(), orgaoJulgadorColegiadoAtual,
                        getVoto().getSessao()));
            }
        } else {
            setAnotacaoVoto(anotacaoVotoManager.getAnotacaoVotoSemSessaoByProcessoOrgaoJulgadorEColegiado(
                    getProcessoTrf(), orgaoJulgadorAtual,
                    orgaoJulgadorColegiadoAtual));
            setAnotacoesVotoEmOutrasSessoes(anotacaoVotoManager.getAnotacoesVotoSemSessaoByProcessoOrgaoJulgadorEColegiado(
                    getProcessoTrf(), orgaoJulgadorAtual,
                    orgaoJulgadorColegiadoAtual));
        }

        inicializar(orgaoJulgadorAtual);
    }

    protected void inicializar(OrgaoJulgador orgaoJulgador) {
        documentoVotoEmenta = iniciarDocumentoVotoVazio(documentoVotoEmenta,
                TIPO_PROCESSO_DOCUMENTO_EMENTA);
        documentoVotoRelatorio = iniciarDocumentoVotoVazio(documentoVotoEmenta,
                TIPO_PROCESSO_DOCUMENTO_RELATORIO);
        documentoVotoFundamentacao = iniciarDocumentoVotoVazio(documentoVotoEmenta,
                TIPO_PROCESSO_DOCUMENTO_FUNDAMENTACAO);
        documentoVotoDispDecisorio = iniciarDocumentoVotoVazio(documentoVotoEmenta,
                TIPO_PROCESSO_DOCUMENTO_DISPOSITIVO);

        //Se não existe um voto, inicializa um vazio
        if (voto == null) {
            voto = new Voto();
            tipoVoto = new TipoVotoJT();
            voto.setImpedimentoSuspeicao(false);
            voto.setOrgaoJulgador(orgaoJulgador);
            voto.setUsuarioInclusao(Authenticator.getUsuarioLogado());
            voto.setDestaque(false);
            voto.setMarcacaoDestaque(false);
            voto.setMarcacaoDivergencia(false);
            voto.setMarcacaoObservacao(false);
            voto.setLiberacao(false);
        } else {
            tipoVoto = voto.getTipoVoto();

            carregarDocumentosDoVoto();

            if (sessao == null) {
                sessao = voto.getSessao();
            }

            orgaoJulgadorVoto = voto.getOrgaoJulgador();
        }

        carregarModelosDeDocumentos();

        if (anotacaoVoto != null) {
            anotacoes = anotacaoVoto.getAnotacao();
        } else {
            anotacoes = null;
        }

        /**
         * [PJEII-6929] Implementação de validação dos objetos para evitar NullPointerException que ocorria para determinados processos.
         * @author fernando.junior (19/04/2013)
         */
        if (documentoVotoDispDecisorio != null) {
        	documentoVotoDispDecisorio.setVoto(voto);
        }
        if (documentoVotoEmenta != null) {
        	documentoVotoEmenta.setVoto(voto);
        }
        if (documentoVotoRelatorio != null) {
        	documentoVotoRelatorio.setVoto(voto);
        }
        if (documentoVotoFundamentacao != null) {
        	documentoVotoFundamentacao.setVoto(voto);
        }

        voto.setProcessoTrf(processoTrf);

        // Setar primeiro anotacoes de outras sessoes como selecionado, se houver
        if ((getAnotacoesVotoEmOutrasSessoes() != null) &&
                (getAnotacoesVotoEmOutrasSessoes().size() > 0)) {
            setAnotacaoSelecionada(getAnotacoesVotoEmOutrasSessoes().get(0));
        }
        //FIXME MÉTODO Comentado porque não será mais usado
        //construirModeloIntegra();
        construirModeloEstruturado();        
        setTab("integra");

        verificaAcordaoPublicado();

        setComposicaoRedator(composicaoProcessoSessaoManager.getComposicaoProcessoSessao(
                getProcessoTrf(), getSessao(), getOrgaoJulgadorRedator()));
        setComposicaoRelator(composicaoProcessoSessaoManager.getComposicaoProcessoSessao(
                getProcessoTrf(), getSessao(), getOrgaoJulgadorRelator()));
    }

    /**
     * [PJEII-6929] Implementação de validação dos objetos para evitar NullPointerException que ocorria para determinados processos.
     * @author fernando.junior (19/04/2013)
     */
    protected void carregarModelosDeDocumentos() {
    	if (documentoVotoEmenta != null && documentoVotoEmenta.getProcessoDocumentoBin() != null) {
    		modeloEmenta = documentoVotoEmenta.getProcessoDocumentoBin().getModeloDocumento();
    	}
    	if (documentoVotoRelatorio != null && documentoVotoRelatorio.getProcessoDocumentoBin() != null) {
    		modeloRelatorio = documentoVotoRelatorio.getProcessoDocumentoBin().getModeloDocumento();
    	}
    	if (documentoVotoFundamentacao != null && documentoVotoFundamentacao.getProcessoDocumentoBin() != null) {
    		modeloFundamentacao = documentoVotoFundamentacao.getProcessoDocumentoBin().getModeloDocumento();
    	}
    	if (documentoVotoDispDecisorio != null && documentoVotoDispDecisorio.getProcessoDocumentoBin() != null) {
    		modeloDispositivo = documentoVotoDispDecisorio.getProcessoDocumentoBin().getModeloDocumento();
    	}
    }

    protected void carregarDocumentosDoVoto() {
        documentoVotoEmenta = documentoVotoManager.getDocumentoVotoByVotoETipo(voto,
                TIPO_PROCESSO_DOCUMENTO_EMENTA);
        documentoVotoRelatorio = documentoVotoManager.getDocumentoVotoByVotoETipo(voto,
                TIPO_PROCESSO_DOCUMENTO_RELATORIO);
        documentoVotoFundamentacao = documentoVotoManager.getDocumentoVotoByVotoETipo(voto,
                TIPO_PROCESSO_DOCUMENTO_FUNDAMENTACAO);
        documentoVotoDispDecisorio = documentoVotoManager.getDocumentoVotoByVotoETipo(voto,
                TIPO_PROCESSO_DOCUMENTO_DISPOSITIVO);
    }

    private DocumentoVoto iniciarDocumentoVotoVazio(
        DocumentoVoto documentoVoto, TipoProcessoDocumento tipoProcessoDocumento) {
        documentoVoto = new DocumentoVoto();
        documentoVoto.setAtivo(true);
        documentoVoto.setDataInclusao(new Date());
        documentoVoto.setDocumentoSigiloso(false);
        documentoVoto.setProcesso(processoTrf.getProcesso());
        documentoVoto.setTipoProcessoDocumento(tipoProcessoDocumento);
        documentoVoto.setProcessoDocumento("");

        ProcessoDocumentoBin documentoVotoBin = new ProcessoDocumentoBin();
        documentoVotoBin.setDataInclusao(new Date());
        documentoVotoBin.setModeloDocumento(" ");

        documentoVoto.setProcessoDocumentoBin(documentoVotoBin);

        return documentoVoto;
    }

    public void persist() {
        setMD5Documento();

        persistBin();

        persistVoto();

        if (!Strings.isEmpty(getAnotacoes())) {
            persistAnotacoes();
        }

        persistDocumentoVoto();

        votoManager.update(getProcessoTrf());
    }

    protected void persistVoto() {
        verificarMarcacoes();

        if (voto.getOrgaoJulgador().equals(processoTrf.getOrgaoJulgador())) {
            for (TipoVotoJT tv : tipoVotoJTManager.getTipoVotoRelator()) {
                if (tv.getConclusao().equals(ConclusaoEnum.RE)) {
                    tipoVoto = tv;

                    break;
                }
            }
        }

        votoManager.persistVoto(voto, tipoVoto, Authenticator.getUsuarioLogado());
    }

    protected void persistAnotacoes() {
        anotacaoVoto = new AnotacaoVoto();
        anotacaoVoto.setAnotacao(anotacoes);
        anotacaoVoto.setDataInclusao(new Date());
        anotacaoVoto.setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
        anotacaoVoto.setOrgaoJulgadorColegiado(Authenticator.getOrgaoJulgadorColegiadoAtual());
        anotacaoVoto.setProcessoTrf(processoTrf);
        anotacaoVoto.setUsuarioInclusao(Authenticator.getUsuarioLogado());
        anotacaoVoto.setSessao(this.voto.getSessao());

        votoManager.persist(anotacaoVoto);
    }

    protected void setMD5Documento() {
    	/* [PJEII-7592] - Sistema não gera acórdão
    	 * Adicionada verificação de documentos nulos, onde uma exceção é levantada mostrando mensagem de erro mais específica.
    	 * Um contorno para esse problema está descrito na issue PJEII-7592 (opção limpar votos)
    	 */
    	if (documentoVotoRelatorio==null || documentoVotoFundamentacao==null || documentoVotoDispDecisorio==null || documentoVotoEmenta==null) {
    		FacesMessages.instance().clear();
	    	  throw new AplicationException("Não foi possível encontrar um dos seguintes documentos: Relatório, Fundamentação, Dispositivo ou Ementa.\n" +
	    	  								"Para iniciar novo voto utilize a funcionalidade Processo -> Outras ações -> Limpar Votos");
    	}
    }

    protected void persistBin() {
        documentoVotoEmenta.getProcessoDocumentoBin()
                           .setModeloDocumento(modeloEmenta);
        documentoVotoRelatorio.getProcessoDocumentoBin()
                              .setModeloDocumento(modeloRelatorio);
        documentoVotoFundamentacao.getProcessoDocumentoBin()
                                  .setModeloDocumento(modeloFundamentacao);
        documentoVotoDispDecisorio.getProcessoDocumentoBin()
                                  .setModeloDocumento(modeloDispositivo);

        votoManager.persistVarios(documentoVotoRelatorio.getProcessoDocumentoBin(),
            documentoVotoFundamentacao.getProcessoDocumentoBin(),
            documentoVotoDispDecisorio.getProcessoDocumentoBin(),
            documentoVotoEmenta.getProcessoDocumentoBin());
    }

    protected void persistDocumentoVoto() {
        votoManager.persistVarios(documentoVotoEmenta, documentoVotoRelatorio,
            documentoVotoFundamentacao, documentoVotoDispDecisorio);
    }

    public void update() {
        persistAtualizaAnotacoes();

        OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();

        if (orgaoJulgadorAtual != null) {
            atualizarVoto();
            setMD5Documento();
            atualizarDocumentos();
            //construirModeloIntegra();
            construirModeloEstruturado();
        }

        if (orgaoJulgadorAtual == null) {
            if (!ParametroUtil.instance()
                                  .isPermissaoAcessoVotoPreSessaoSecretarioSemAcesso()) {
                getDocumentoVotoDispDecisorio().getProcessoDocumentoBin()
                    .setModeloDocumento(getModeloDispositivo());
                votoManager.update(getDocumentoVotoDispDecisorio());
            }
        }

        votoManager.update(getProcessoTrf());

        FacesMessages.instance()
                     .add(Severity.INFO, "Registro alterado com sucesso.");
    }

    protected void persistAtualizaAnotacoes() {
        if ((getAnotacaoVoto() == null) && !Strings.isEmpty(getAnotacoes())) {
            persistAnotacoes();
        } else if ((getAnotacaoVoto() != null) &&
                !getAnotacaoVoto().getAnotacao().equals(getAnotacoes())) {
            atualizarAnotacoes();
        }
    }

    protected void atualizarVoto() {
        verificarMarcacoes();
        votoManager.atualizarVoto(voto, tipoVoto,
            Authenticator.getUsuarioLogado());
    }

    protected void atualizarAnotacoes() {
        anotacaoVoto.setAnotacao(anotacoes);
        anotacaoVoto.setDataAlteracao(new Date());
        anotacaoVoto.setUsuarioAlteracao(Authenticator.getUsuarioLogado());
        votoManager.update(anotacaoVoto);
    }

    protected void verificarMarcacoes() {
        StringBuilder modeloDocumento = new StringBuilder();
        modeloDocumento.append(modeloEmenta);
        modeloDocumento.append(modeloRelatorio);
        modeloDocumento.append(modeloFundamentacao);
        modeloDocumento.append(modeloDispositivo);

        String[] regexs = {
                "<span.*?\\w?class=\\\"divergencia\\\".*?\\w?>.*?\\w+.*?</span>",
                "<span.*?\\w?class=\\\"destaque\\\".*?\\w?>.*?\\w+.*?</span>",
                "<span.*?\\w?class=\\\"obs\\\".*?\\w?>.*?\\w+.*?</span>"
            };

        Pattern pattern = Pattern.compile(regexs[0]);
        Matcher matcher = pattern.matcher(modeloDocumento.toString());
        voto.setMarcacaoDivergencia(matcher.find());

        pattern = Pattern.compile(regexs[1]);
        matcher = pattern.matcher(modeloDocumento.toString());
        voto.setMarcacaoDestaque(matcher.find());

        pattern = Pattern.compile(regexs[2]);
        matcher = pattern.matcher(modeloDocumento.toString());
        voto.setMarcacaoObservacao(matcher.find());
    }

    protected void atualizarDocumentos() {
        documentoVotoEmenta.getProcessoDocumentoBin()
                           .setModeloDocumento(modeloEmenta);
        documentoVotoRelatorio.getProcessoDocumentoBin()
                              .setModeloDocumento(modeloRelatorio);
        documentoVotoFundamentacao.getProcessoDocumentoBin()
                                  .setModeloDocumento(modeloFundamentacao);
        documentoVotoDispDecisorio.getProcessoDocumentoBin()
                                  .setModeloDocumento(modeloDispositivo);

        votoManager.updateVarios(documentoVotoEmenta, documentoVotoRelatorio,
            documentoVotoFundamentacao, documentoVotoDispDecisorio);
    }

    protected boolean ocorreuAlteracaoNoVoto() {
        if ((getSessao() != null) &&
                !(getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.S) ||
                getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.A))) {
            return false;
        }

        boolean ocorreuAlteracaoTipoVoto = ocorreuAlteracaoDocumento(documentoVotoEmenta,
                modeloEmenta) ||
            ocorreuAlteracaoDocumento(documentoVotoRelatorio, modeloRelatorio) ||
            ocorreuAlteracaoDocumento(documentoVotoFundamentacao,
                modeloFundamentacao) ||
            ocorreuAlteracaoDocumento(documentoVotoDispDecisorio,
                modeloDispositivo) || ocorreuAlteracaoTipoVoto();

        return ocorreuAlteracaoTipoVoto;
    }

    private Boolean ocorreuAlteracaoDocumento(DocumentoVoto documentoVoto,
        String modelo) {
        if (modelo == null) {
            if (documentoVoto.getProcessoDocumentoBin().getModeloDocumento() != null) {
                return Boolean.TRUE;
            }
        } else if (!modelo.equals(documentoVoto.getProcessoDocumentoBin()
                                                   .getModeloDocumento())) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    public boolean sessaoEncerrada() {
        return pautaJulgamentoService.sessaoEncerrada(getSessao());
    }

    public boolean sessaoFechada() {
        return pautaJulgamentoService.sessaoFechada(getSessao());
    }

    public boolean sessaoIniciada() {
        return pautaJulgamentoService.sessaoIniciada(getSessao());
    }
    
    public boolean sessaoAguardandoSessao() {
        return pautaJulgamentoService.sessaoAguardandoSessao(getSessao());
    }
    
    public boolean sessaoAberta() {
        return pautaJulgamentoService.sessaoAberta(getSessao());
    }

    protected void filtroVoto(String sigla) {
        int[] valorSigla = new int[3];

        if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[5])) {
            valorSigla[0] = 6;
            valorSigla[1] = 7;
            valorSigla[2] = 9;
        } else if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[6])) {
            valorSigla[0] = 5;
            valorSigla[1] = 7;
            valorSigla[2] = 9;
        } else if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[7])) {
            valorSigla[0] = 6;
            valorSigla[1] = 5;
            valorSigla[2] = 9;
        } else if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[9])) {
            valorSigla[0] = 6;
            valorSigla[1] = 5;
            valorSigla[2] = 7;
        }

        getMapLegenda()
            .put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[valorSigla[0]], false);
        getMapLegenda()
            .put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[valorSigla[1]], false);
        getMapLegenda()
            .put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[valorSigla[2]], false);
    }
    
    protected void filtroDivergencia(String sigla) {
    	int[] valorSigla = new int[2];
    	
    	if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[0])) {
    		valorSigla[0] = 10;
    		valorSigla[1] = 11;
    	} else if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[10])) {
    		valorSigla[0] = 0;
    		valorSigla[1] = 11;
    	} else if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[11])) {
    		valorSigla[0] = 0;
    		valorSigla[1] = 10;
    	}
    	
    	getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[valorSigla[0]], false);
    	getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[valorSigla[1]], false);
    }
    
    protected void filtroDestaque(String sigla) {
    	if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[1])) {
    		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[12], false);
    	} else if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[12])) {
    		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[1], false);
    	}
    }
    
    protected void filtroAnotacao(String sigla) {
    	if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[13])) {
    		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[14], false);
    	} else if (sigla.equals(ItensLegendas.SIGLAS_LEGENDAS_VOTO[14])) {
    		getMapLegenda().put(ItensLegendas.SIGLAS_LEGENDAS_VOTO[13], false);
    	}
    }

    private Boolean ocorreuAlteracaoTipoVoto() {
        if (tipoVoto == null) {
            if (voto.getTipoVoto() != null) {
                return Boolean.TRUE;
            }
        } else if (!tipoVoto.equals(voto.getTipoVoto())) {
            return Boolean.TRUE;
        }

        return Boolean.FALSE;
    }

    /**
     * Verifica se o usu&aacute;rio tem permiss&atilde;o para visualizar as marca&ccedil;&otilde;es do voto
     */
    public boolean esconderMarcacoes() {
        return desabilitarEditor();
    }

    public boolean desabilitarEditor() {
        return ((orgaoJulgadorVoto != null) &&
        !(orgaoJulgadorVoto.equals(Authenticator.getOrgaoJulgadorAtual()))) ||
        (Authenticator.getOrgaoJulgadorAtual() == null);
    }

    public boolean desabilitarDispositivo() {
        return ((orgaoJulgadorVoto != null) &&
        !(orgaoJulgadorVoto.equals(Authenticator.getOrgaoJulgadorAtual())) &&
        Authenticator.isMagistrado()) ||
        (ParametroUtil.instance()
                      .isPermissaoAcessoVotoPreSessaoSecretarioSemAcesso() &&
        Authenticator.getOrgaoJulgadorAtual() == null);
    }

    public List<OrgaoJulgador> getOrgaoJulgadorItems() {
        return composicaoSessaoManager.getOrgaoJulgadorBySessao(sessao);
    }

    public void getVotoProcessoByOrgaoJulgadorSessao() {
        voto = votoManager.getVotoProcessoByOrgaoJulgadorSessao(processoTrf,  orgaoJulgadorVoto, sessao);
        inicializar(orgaoJulgadorVoto);
        construirModeloIntegra();
        setTab("integra");
    }

    public List<TipoVotoJT> getTipoVotoItems() {
        OrgaoJulgador orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();

        if ((((orgaoJulgador != null) && (getProcessoTrf() != null) &&
                orgaoJulgador.equals(getOrgaoJulgadorRelator())) ||
                (orgaoJulgador == null))) {
            return tipoVotoJTManager.getTipoVotoRelator();
        } else {
            return tipoVotoJTManager.getTipoVotoVogal();
        }
    }

    public OrgaoJulgador getOrgaoJulgadorRelator() {
        OrgaoJulgador retorno = (getProcessoTrf() != null)
            ? getProcessoTrf().getOrgaoJulgador() : null;

        // Se sessão estiver encerrada ou fechada...
        if ((getSessao() != null) && (getProcessoTrf() != null) &&
                (SituacaoSessaoEnum.E.equals(getSessao().getSituacaoSessao()) ||
                SituacaoSessaoEnum.F.equals(getSessao().getSituacaoSessao()))) {
            ProcessoJT processoJt = processoJtManager.getProcessoJtPorId(getProcessoTrf()
                                                                             .getIdProcessoTrf());

            if ((processoJt != null) &&
                    (processoJt.getOrgaoJulgadorRelatorOriginario() != null)) {
                retorno = processoJt.getOrgaoJulgadorRelatorOriginario();
            }
        }

        return retorno;
    }

    public void copiarVoto() {
        setOrgaoJulgadorVoto(null);

        String modeloVotoEmenta = getModeloEmenta();
        String modeloVotoRelatorio = getModeloRelatorio();
        String modeloVotoFundamentacao = getModeloFundamentacao();
        String modeloVotoDispDecisorio = getModeloDispositivo();

        OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
        setVoto(votoManager.getVotoProcessoByOrgaoJulgadorSessao(
                getProcessoTrf(), orgaoJulgadorAtual, getSessao()));

        inicializar(orgaoJulgadorAtual);

        setModeloEmenta(modeloVotoEmenta);
        setModeloRelatorio(modeloVotoRelatorio);
        setModeloFundamentacao(modeloVotoFundamentacao);
        setModeloDispositivo(modeloVotoDispDecisorio);

        construirModeloIntegra();
        setTab("integra");
    }

    public List<OrgaoJulgador> getOrgaoJulgadorComposicaoItems() {
        return ordenarListOJ(votoManager.getOrgaoJulgadorComVotoLiberado(
                processoTrf, sessao));
    }

    private List<OrgaoJulgador> ordenarListOJ(List<OrgaoJulgador> list) {
        OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();

        if (list.contains(orgaoJulgadorAtual)) {
            list.remove(orgaoJulgadorAtual);
        }

        List<OrgaoJulgador> listAux = new ArrayList<OrgaoJulgador>();

        if (orgaoJulgadorAtual != null) {
            listAux.add(orgaoJulgadorAtual);
        } else {
            if (list.contains(processoTrf.getOrgaoJulgador())) {
                list.remove(processoTrf.getOrgaoJulgador());
            }

            listAux.add(processoTrf.getOrgaoJulgador());
        }

        if (list.contains(orgaoJulgadorRedator) &&
                (((orgaoJulgadorAtual != null) &&
                !orgaoJulgadorAtual.equals(orgaoJulgadorRedator)) ||
                (orgaoJulgadorAtual == null))) {
            list.remove(orgaoJulgadorRedator);
            listAux.add(orgaoJulgadorRedator);
        }

        listAux.addAll(list);

        return listAux;
    }
    
    /**
     * [PJEII-4390] : fernando.junior (09/01/2013)
     * 
     * Método que corrige o redirecionamento dos links dentro dos editores, que antes abriam dentro do iframe do editor.
     * Com a correção os links são redirecionados para uma nova aba.
     */
    public void corrigirLinksEditor() {
    	// Ementa
    	if (getModeloEmenta() != null) {
    		setModeloEmenta(getModeloEmenta().replaceAll("<a href=", "<a target='_blank' href="));
    	}
    	
    	// Relatório
    	if (getModeloRelatorio() != null) {
    		setModeloRelatorio(getModeloRelatorio().replaceAll("<a href=", "<a target='_blank' href="));
    	}
    	
    	// Fundamentação
    	if (getModeloFundamentacao() != null) {
    		setModeloFundamentacao(getModeloFundamentacao().replaceAll("<a href=", "<a target='_blank' href="));
    	}
    	
    	// Dispositivo
    	if (getModeloDispositivo() != null) {
    		setModeloDispositivo(getModeloDispositivo().replaceAll("<a href=", "<a target='_blank' href="));
    	}
    }
    
    public void carregaVotoCompleto(ProcessoDocumentoEstruturado processoDocumentoEstruturado) {
    	StringBuilder sb = new StringBuilder(); 	
    	boolean sessaoIniciada = false;

        if (getSessao() != null) {
            sessaoIniciada = !(getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.A) ||  getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.S));
        }        
        OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
        boolean hasPermissaoTodos = (orgaoJulgadorAtual != null) || ParametroUtil.instance().isPermissaoAcessoVotoPreSessaoSecretarioTodos();
        boolean hasPermissaoDispositivo = (orgaoJulgadorAtual != null) ||!ParametroUtil.instance().isPermissaoAcessoVotoPreSessaoSecretarioSemAcesso();
        
        if (sessaoIniciada || hasPermissaoTodos) {     
        	String docCompleto = editorService.getHtmlEditor(processoDocumentoEstruturado);
            sb.append(docCompleto);                
        }else{
        	
        	if (sessaoIniciada || hasPermissaoDispositivo) {            	
            	if(processoDocumentoEstruturado!=null && processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList()!=null){
                	for(ProcessoDocumentoEstruturadoTopico tempProcDocTopico : processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList()){
                        if(tempProcDocTopico.getTopico().getTipoTopico().equals(TipoTopicoEnum.IT_DISP_VOTO)){
                        	sb.append(tempProcDocTopico.getConteudo());
                    	}  	        	        	
                    }   
                }
             }
          }
        setModeloIntegra(sb.toString());
    }
    
    public void construirModeloEstruturado(){    	
    	ProcessoDocumentoEstruturado processoDocumentoEstruturado = getDocumentoEstruturado();
    	if (processoDocumentoEstruturado != null){
    		carregaVotoCompleto(processoDocumentoEstruturado);
    		SecretarioSessaoJulgamentoAction ssja = ComponentUtil.getComponent(SecretarioSessaoJulgamentoAction.NAME);
    		ssja.carregarConteudoDispVotoAndSessao(processoDocumentoEstruturado);
    	}
    }

    public String getHtmlEditorSecretarioDispositivo(ProcessoDocumentoEstruturado processoDocumentoEstruturado) {
    	List<ProcessoDocumentoEstruturadoTopico> whiteListTopicos = new ArrayList<ProcessoDocumentoEstruturadoTopico>();
    	
        boolean hasPermissaoDispositivo = !ParametroUtil.instance().isPermissaoAcessoVotoPreSessaoSecretarioSemAcesso();
        
    	if (hasPermissaoDispositivo) {            	
        	if(processoDocumentoEstruturado!=null && processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList()!=null){
            	for(ProcessoDocumentoEstruturadoTopico tempProcDocTopico : processoDocumentoEstruturado.getProcessoDocumentoEstruturadoTopicoList()){
                    if(tempProcDocTopico.getTopico().getTipoTopico().equals(TipoTopicoEnum.IT_DISP_VOTO)
                    		|| tempProcDocTopico.getTopico().getTipoTopico().equals(TipoTopicoEnum.IT_DISP_SESSAO)
                    		|| tempProcDocTopico.getTopico().getTipoTopico().equals(TipoTopicoEnum.DISPOSITIVO_ACORDAO)){
                    	whiteListTopicos.add(tempProcDocTopico);
                	}
                }
            	return editorService.getHtmlEditor(processoDocumentoEstruturado, true, whiteListTopicos);
            }
        }
        return "";
    }
    
    public ProcessoDocumentoEstruturado getDocumentoEstruturado() {
    	return processoDocumentoEstruturadoManager.getUltimoAcordaoEstruturadoByIdProcessoTrf(processoTrf.getIdProcessoTrf());
    }

    public void construirModeloIntegra() {
        StringBuilder sb = new StringBuilder();
        boolean sessaoIniciada = false;

        if (getSessao() != null) {
            sessaoIniciada = !(getSessao().getSituacaoSessao()
                                   .equals(SituacaoSessaoEnum.A) ||
                getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.S));
        }

        OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();
        boolean hasPermissaoTodos = (orgaoJulgadorAtual != null) ||
            ParametroUtil.instance()
                         .isPermissaoAcessoVotoPreSessaoSecretarioTodos();
        boolean hasPermissaoDispositivo = (orgaoJulgadorAtual != null) ||
            !ParametroUtil.instance()
                          .isPermissaoAcessoVotoPreSessaoSecretarioSemAcesso();

        if (sessaoIniciada || hasPermissaoTodos) {
            if (!Strings.isEmpty(modeloEmenta)) {
                sb.append("<b>Ementa</b>");
                sb.append("<br />");
                sb.append(modeloEmenta);
                sb.append("<br /><br />");
            }

            if (!Strings.isEmpty(modeloRelatorio)) {
                sb.append("<b>Relatorio</b>");
                sb.append("<br />");
                sb.append(modeloRelatorio);
                sb.append("<br /><br />");
            }

            if (!Strings.isEmpty(modeloFundamentacao)) {
                sb.append("<b>Fundamentação</b>");
                sb.append("<br />");
                sb.append(modeloFundamentacao);
                sb.append("<br /><br />");
            }
        }

        if (sessaoIniciada || hasPermissaoDispositivo) {
            if (!Strings.isEmpty(modeloDispositivo)) {
                sb.append("<b>Dispositivo</b>");
                sb.append("<br />");
                sb.append(modeloDispositivo);
            }
        }

        setModeloIntegra(sb.toString());
    }

    public Voto votoProcessoPauta(PautaSessao pautaSessao) {
        if (!pautaSessaoAnterior.equals(pautaSessao)) {
            OrgaoJulgador orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();

            if (orgaoJulgador == null) {
                orgaoJulgador = pautaSessao.getProcessoTrf().getOrgaoJulgador();
            }

            pautaSessaoAnterior = pautaSessao;
            votoAnterior = votoManager.getVotoProcessoByOrgaoJulgadorSessao(pautaSessao.getProcessoTrf(),
                    orgaoJulgador, pautaSessao.getSessao());
        }

        return votoAnterior;
    }

    public boolean existeVotoComDivergencia(PautaSessao pautaSessao) {
    	if (existeVotoComDivergenciaMap.isEmpty() || !existeVotoComDivergenciaMap.containsKey(pautaSessao)) {
    		existeVotoComDivergenciaMap.put(pautaSessao, votoManager.existeVotoComDivergencia(pautaSessao.getProcessoTrf(),
    	            pautaSessao.getSessao()));
        }
    	
    	return existeVotoComDivergenciaMap.get(pautaSessao);
    }

    public boolean existeVotoComDestaque(PautaSessao pautaSessao) {
    	if (existeVotoComDestaqueMap.isEmpty() || !existeVotoComDestaqueMap.containsKey(pautaSessao)) {
    		existeVotoComDestaqueMap.put(pautaSessao, votoManager.existeVotoComDestaque(pautaSessao.getProcessoTrf(),
    	            pautaSessao.getSessao()));
        }
    	
    	return existeVotoComDestaqueMap.get(pautaSessao);
    }

    public boolean existeVotoComObservacao(PautaSessao pautaSessao) {
    	if (existeVotoComObservacaoMap.isEmpty() || !existeVotoComObservacaoMap.containsKey(pautaSessao)) {
    		existeVotoComObservacaoMap.put(pautaSessao, votoManager.existeVotoComObservacao(pautaSessao.getProcessoTrf(),
    	            pautaSessao.getSessao()));
        }
    	
    	return existeVotoComObservacaoMap.get(pautaSessao);
    }

    public void limparFiltros() {
        SearchProcessosPautaBean searchBean = ComponentUtil.getComponent(
                "searchProcessosPautaBean");
        searchBean.clearSearchFields();
        iniciarLegenda();
        setElaboracaoVotoList(new ElaboracaoVotoList());
        this.setListaPautaSessaoGrid(this.elaboracaoVotoList.list());
    }

    public abstract void iniciarLegenda();

    public String titleDataView() {
        StringBuilder sb = new StringBuilder();

        if (getSessao() != null) {
            sb.append("Sessão de Julgamento do(a) ");
            sb.append(getSessao().getOrgaoJulgadorColegiado());
            sb.append(" - ");
            sb.append(getSessao().getTipoSessao());
            sb.append(" - ");
            sb.append(DateUtil.getDataFormatada(getSessao().getDataSessao(),
                    "dd/MM/yyyy"));
            sb.append(" ");
            sb.append(DateUtil.getDataFormatada(
                    new Date(getSessao().getSalaHorario().getHoraInicial()
                                 .getTime()), "HH:mm"));
        } else {
            sb.append("Órgão Julgador Colegiado: ");
            sb.append(Authenticator.getOrgaoJulgadorColegiadoAtual());
        }

        return sb.toString();
    }

    protected void limparDadosProcesso() {
        setProcessoTrf(null);
        setVoto(null);
        setAnotacoes(null);
        setAnotacaoVoto(null);
        setModeloDispositivo(null);
        setModeloEmenta(null);
        setModeloFundamentacao(null);
        setModeloIntegra(null);
        setModeloRelatorio(null);
    }

    protected boolean verificaAcordaoPublicado() {
       /* acordaoPublicado = processoDocumentoManager.isAcordaoPublicado(documentoVotoManager.getUltimoDocumentoVotoAssinado(
                    TIPO_PROCESSO_DOCUMENTO_ACORDAO,
                    getProcessoTrf().getProcesso(), getVoto()));*/
    	
    	/**
    	 * [PJEII-4539] - Desabilitando mecanismo de verificação de acórdão publicado para manutenção e melhoria, pois não contempla a situação de um segundo julgamento do processo,
    	 * isto trava os magistrados de poderem votar e publicar um segundo acórdão no processo.
    	 */
    	
    	acordaoPublicado = false;

        return acordaoPublicado;
    }

    //TODO: rename nas páginas
    public boolean podeAssinarAcordaoMagistrado() {
        if (getPautaSessao() != null) {
            return pautaJulgamentoService.podeAssinarAcordao(getPautaSessao(),
                Authenticator.getUsuarioLogado());
        }

        return false;
    }

    public boolean podeAssinarAcordaoMagistrado(PautaSessao pautaSessao) {
        return pautaJulgamentoService.podeAssinarAcordao(pautaSessao,
            Authenticator.getUsuarioLogado());
    }

    public abstract void assinar();

    protected void assinarProcessoDocumento() {        
        /**
         * [PJEII-5763] PJE-JT: Ricardo Maia : PJE-1.4.7
         * Força a criação de um novo acórdão sempre que for necessário assinar acórdão.
         */
        assinaEPersisteDocumento(getModeloIntegra(), getProcessoTrf());
    }

    private void assinaEPersisteDocumento(String modeloDocumento,
        ProcessoTrf processoTrf) {
    	ProcessoDocumento pd = processoDocumentoManager.getProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoAcordao(),
                getProcessoTrf().getProcesso());    	
    	
    	if(pd != null){
    		assinaEPersisteDocumentoBinExistente(modeloDocumento, processoTrf, pd);
    	} else{
    		assinaEPersisteDocumentoBinNovo(modeloDocumento, processoTrf);
    	}        
    }
        
    private void assinaEPersisteDocumentoBinNovo(String modeloDocumento,
            ProcessoTrf processoTrf){
        ProcessoDocumentoBin bin = new ProcessoDocumentoBin();
        bin.setModeloDocumento(modeloDocumento);
        bin.setCertChain(certChain);
        bin.setDataInclusao(new Date());
        bin.setSignature(signature);

        Usuario usuarioLogado = Authenticator.getUsuarioLogado();
        bin.setUsuario(usuarioLogado);

        DocumentoVoto documento = new DocumentoVoto();
        documento.setAtivo(true);
        documento.setDataInclusao(new Date());
        documento.setUsuarioInclusao(usuarioLogado);
        documento.setDocumentoSigiloso(false);
        documento.setProcesso(processoTrf.getProcesso());
        documento.setTipoProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoAcordao());
        documento.setProcessoDocumento("Acórdão");
        documento.setPapel(Authenticator.getPapelAtual());
        documento.setVoto(getVoto());

        documento.setProcessoDocumentoBin(bin);

        pautaJulgamentoService.persistEAssinarProcessoDocumentoBin(bin,
            certChain, signature, documento.getTipoProcessoDocumento(),
            Authenticator.getPessoaLogada());

        genericManager.persist(documento);

    }
        
    private void assinaEPersisteDocumentoBinExistente(String modeloDocumento,
            ProcessoTrf processoTrf, ProcessoDocumento pd){
    	
    	ProcessoDocumentoBin bin = pd.getProcessoDocumentoBin();
        bin.setModeloDocumento(modeloDocumento);
        bin.setCertChain(certChain);
        bin.setSignature(signature);

        Usuario usuarioLogado = Authenticator.getUsuarioLogado();
        bin.setUsuario(usuarioLogado);
 
        insertDocumentoVoto(pd.getIdProcessoDocumento(), getVoto().getIdVoto());
        
        genericManager.update(bin);

        pautaJulgamentoService.persistEAssinarProcessoDocumentoBin(bin,
                certChain, signature, ParametroUtil.instance().getTipoProcessoDocumentoAcordao(),
                Authenticator.getPessoaLogada());       	
    }

        
    public void insertDocumentoVoto(Integer idDocumentoVoto, Integer idVoto){
    	StringBuilder sql = new StringBuilder();
		sql.append("Insert into tb_documento_voto(id_documento_voto, id_voto) values(:id_documento_voto, :id_voto) ");
		Query q = EntityUtil.createNativeQuery(sql, "tb_documento_voto");
		q.setParameter("id_documento_voto", idDocumentoVoto);
		q.setParameter("id_voto", idVoto);
		q.executeUpdate();
    }

    public boolean getProcessoDocumentoAcordaoJaExiste() {
        ProcessoDocumento processoDocumentoJaExistente = null;

        if (getProcessoTrf() != null) {
            processoDocumentoJaExistente = processoDocumentoManager.getProcessoDocumento(ParametroUtil.instance().getTipoProcessoDocumentoAcordao(),
                    getProcessoTrf().getProcesso());
        }

        return (processoDocumentoJaExistente != null);
    }

    public List<ComposicaoProcessoSessao> redatorItems() {
        return composicaoProcessoSessaoManager.getComposicaoProcessoByProcessoSessao(getProcessoTrf(),
            getSessao());
    }

    /*
     * inicio dos get e set
    */
    public Integer getIdProcesso() {
        return idProcesso;
    }

    public void setIdProcesso(Integer idProcesso) {
        this.idProcesso = idProcesso;
    }

    public ProcessoTrf getProcessoTrf() {
        return processoTrf;
    }

    public void setProcessoTrf(ProcessoTrf processoTrf) {
        this.processoTrf = processoTrf;
    }

    public Integer getIdSessao() {
        return idSessao;
    }

    public void setIdSessao(Integer idSessao) {
        this.idSessao = idSessao;
    }

    public SessaoJT getSessao() {
        return sessao;
    }

    public void setSessao(SessaoJT sessao) {
        this.sessao = sessao;
    }

    public Integer getIdVoto() {
        return idVoto;
    }

    public void setIdVoto(Integer idVoto) {
        this.idVoto = idVoto;
    }

    public Voto getVoto() {
        return voto;
    }

    public void setVoto(Voto voto) {
        this.voto = voto;
    }

    public TipoVotoJT getTipoVoto() {
        return tipoVoto;
    }

    public void setTipoVoto(TipoVotoJT tipoVoto) {
        this.tipoVoto = tipoVoto;
    }

    public AnotacaoVoto getAnotacaoVoto() {
        return anotacaoVoto;
    }

    public void setAnotacaoVoto(AnotacaoVoto anotacaoVoto) {
        this.anotacaoVoto = anotacaoVoto;
    }

    public String getAnotacoes() {
        return anotacoes;
    }

    public void setAnotacoes(String anotacoes) {
        this.anotacoes = anotacoes;
    }

    public String getPossuiAnotacoes() {
        possuiAnotacoes = "false";

        if (!Strings.isEmpty(this.anotacoes)) {
            possuiAnotacoes = "true";
        }

        return possuiAnotacoes;
    }

    public void setPossuiAnotacoes(String possuiAnotacoes) {
        this.possuiAnotacoes = possuiAnotacoes;
    }

    public OrgaoJulgador getOrgaoJulgadorVoto() {
        return orgaoJulgadorVoto;
    }

    public void setOrgaoJulgadorVoto(OrgaoJulgador orgaoJulgadorVoto) {
        this.orgaoJulgadorVoto = orgaoJulgadorVoto;
    }

    /*
     * PJE-JT: Ricardo Scholz : PJEII-1419 - 2012-06-19 Alteracoes feitas pela JT.
     * O bug estava ocorrendo porque este atributo estava nulo, no momento do
     * carregamento da referida página. Adotou-se a solução a seguir: caso o
     * atributo requisitado esteja nulo, realiza-se o carregamento da informação.
     * Entretanto, isso implica na execução de uma query no banco de dados,
     * motivo pelo qual ocorre o uso da estrutura condicional, visando evitar
     * execuções desnecessárias do método
     * 'pautaSessaoManager.getOrgaoJulgadorRedatorByProcessoSessao()'.
     * Não foi encontrado um ponto anterior a este 'getter', em que fosse possível
     * realizar o carregamento da variável 'orgaoJulgadorRelator'.
     */
    public OrgaoJulgador getOrgaoJulgadorRedator() {
        if ((orgaoJulgadorRedator == null) && (this.processoTrf != null) &&
                (this.sessao != null)) {
            OrgaoJulgador retorno = pautaSessaoManager.getOrgaoJulgadorRedatorByProcessoSessao(this.processoTrf,
                    this.sessao);

            // Se sessão estiver encerrada ou fechada...
            if ((getSessao() != null) && (getProcessoTrf() != null) &&
                    (SituacaoSessaoEnum.E.equals(getSessao().getSituacaoSessao()) ||
                    SituacaoSessaoEnum.F.equals(getSessao().getSituacaoSessao()))) {
                ProcessoJT processoJt = processoJtManager.getProcessoJtPorId(getProcessoTrf()
                                                                                 .getIdProcessoTrf());

                if ((processoJt != null) &&
                        (processoJt.getOrgaoJulgadorRelatorOriginario() != null)) {
                    retorno = getProcessoTrf().getOrgaoJulgador();
                }
            }

            orgaoJulgadorRedator = retorno;
        }

        return orgaoJulgadorRedator;
    }

    /*
     * PJE-JT: Fim.
     */
    public void setOrgaoJulgadorRedator(OrgaoJulgador orgaoJulgadorRedator) {
        this.orgaoJulgadorRedator = orgaoJulgadorRedator;
    }

    public DocumentoVoto getDocumentoVotoEmenta() {
        return documentoVotoEmenta;
    }

    public void setDocumentoVotoEmenta(DocumentoVoto documentoVotoEmenta) {
        this.documentoVotoEmenta = documentoVotoEmenta;
    }

    public DocumentoVoto getDocumentoVotoRelatorio() {
        return documentoVotoRelatorio;
    }

    public void setDocumentoVotoRelatorio(DocumentoVoto documentoVotoRelatorio) {
        this.documentoVotoRelatorio = documentoVotoRelatorio;
    }

    public DocumentoVoto getDocumentoVotoFundamentacao() {
        return documentoVotoFundamentacao;
    }

    public void setDocumentoVotoFundamentacao(
        DocumentoVoto documentoVotoFundamentacao) {
        this.documentoVotoFundamentacao = documentoVotoFundamentacao;
    }

    public DocumentoVoto getDocumentoVotoDispDecisorio() {
        return documentoVotoDispDecisorio;
    }

    public void setDocumentoVotoDispDecisorio(
        DocumentoVoto documentoVotoDispDecisorio) {
        this.documentoVotoDispDecisorio = documentoVotoDispDecisorio;
    }

    public String getModeloEmenta() {
        return modeloEmenta;
    }

    public void setModeloEmenta(String modeloEmenta) {
        this.modeloEmenta = modeloEmenta;
    }

    public String getModeloRelatorio() {
        return modeloRelatorio;
    }

    public void setModeloRelatorio(String modeloRelatorio) {
        this.modeloRelatorio = modeloRelatorio;
    }

    public String getModeloFundamentacao() {
        return modeloFundamentacao;
    }

    public void setModeloFundamentacao(String modeloFundamentacao) {
        this.modeloFundamentacao = modeloFundamentacao;
    }

    public String getModeloDispositivo() {
        return modeloDispositivo;
    }

    public void setModeloDispositivo(String modeloDispositivo) {
        this.modeloDispositivo = modeloDispositivo;
    }

    public String getModeloIntegra() {
        return modeloIntegra;
    }

    public void setModeloIntegra(String modeloIntegra) {
        this.modeloIntegra = modeloIntegra;
    }

    public ElaboracaoVotoList getElaboracaoVotoList() {
        return elaboracaoVotoList;
    }

    public void setElaboracaoVotoList(ElaboracaoVotoList elaboracaoVotoList) {
        this.elaboracaoVotoList = elaboracaoVotoList;
    }

    public void setIdPautaSessao(Integer idPautaSessao) {
        this.idPautaSessao = idPautaSessao;
    }

    public Integer getIdPautaSessao() {
        return idPautaSessao;
    }

    public void setPautaSessao(PautaSessao pautaSessao) {
        this.pautaSessao = pautaSessao;
    }

    public PautaSessao getPautaSessao() {
        return pautaSessao;
    }

    public void setMapLegenda(Map<String, Boolean> mapLegenda) {
        this.mapLegenda = mapLegenda;
    }

    public Map<String, Boolean> getMapLegenda() {
        return mapLegenda;
    }

    public List<AnotacaoVoto> getAnotacoesVotoEmOutrasSessoes() {
        return anotacoesVotoEmOutrasSessoes;
    }

    public void setAnotacoesVotoEmOutrasSessoes(
        List<AnotacaoVoto> anotacoesVotoEmOutrasSessoes) {
        this.anotacoesVotoEmOutrasSessoes = anotacoesVotoEmOutrasSessoes;
    }

    public AnotacaoVoto getAnotacaoSelecionada() {
        return anotacaoSelecionada;
    }

    public void setAnotacaoSelecionada(AnotacaoVoto anotacaoSelecionada) {
        this.anotacaoSelecionada = anotacaoSelecionada;
    }

    public boolean isCheckBoxSelecionarTodos() {
        return checkBoxSelecionarTodos;
    }

    public void setCheckBoxSelecionarTodos(boolean checkBoxSelecionarTodos) {
        this.checkBoxSelecionarTodos = checkBoxSelecionarTodos;
    }

    public PautaSessao getPautaSessaoAnterior() {
        return pautaSessaoAnterior;
    }

    public void setPautaSessaoAnterior(PautaSessao pautaSessaoAnterior) {
        this.pautaSessaoAnterior = pautaSessaoAnterior;
    }

    public boolean getAcordaoPublicado() {
        return acordaoPublicado;
    }

    public void setAcordaoPublicado(boolean acordaoPublicado) {
        this.acordaoPublicado = acordaoPublicado;
    }

    public String getCertChain() {
        return certChain;
    }

    public void setCertChain(String certChain) {
        this.certChain = certChain;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public ComposicaoProcessoSessao getComposicaoRedator() {
        return composicaoRedator;
    }

    public void setComposicaoRedator(ComposicaoProcessoSessao composicaoRedator) {
        this.composicaoRedator = composicaoRedator;
    }

    public ComposicaoProcessoSessao getComposicaoRelator() {
        return composicaoRelator;
    }
    
    public PessoaMagistrado getPessoaMagistradoRelator() {
    	ComposicaoProcessoSessao v = getComposicaoRelator();
    	if (v.getMagistradoSubstituto() == null) {
			if (v.getComposicaoSessao().getMagistradoSubstituto() == null) {
				if (v.getComposicaoSessao().getMagistradoPresente() == null) {
					return new PessoaMagistrado();
				} else {
					return v.getComposicaoSessao().getMagistradoPresente();
				}
			} else {
				return v.getComposicaoSessao().getMagistradoSubstituto();
			}
    	} else {
			return v.getMagistradoSubstituto();
    	}
    }

    public void setComposicaoRelator(ComposicaoProcessoSessao composicaoRelator) {
        this.composicaoRelator = composicaoRelator;
    }

    public List<PautaSessao> getListaPautaSessaoGrid() {
        return listaPautaSessaoGrid;
    }

    public void setListaPautaSessaoGrid(List<PautaSessao> listaPautaSessaoGrid) {
        this.listaPautaSessaoGrid = listaPautaSessaoGrid;
    }

	public EstruturaDocumentoTopico getEstruturaProcDocDispVoto() {
		return estruturaProcDocDispVoto;
	}

	public void setEstruturaProcDocDispVoto(
			EstruturaDocumentoTopico estruturaProcDocDispVoto) {
		this.estruturaProcDocDispVoto = estruturaProcDocDispVoto;
	}

	public EstruturaDocumentoTopico getEstruturaProcDocDispSessao() {
		return estruturaProcDocDispSessao;
	}

	public void setEstruturaProcDocDispSessao(
			EstruturaDocumentoTopico estruturaProcDocDispSessao) {
		this.estruturaProcDocDispSessao = estruturaProcDocDispSessao;
	}
    
    
}
