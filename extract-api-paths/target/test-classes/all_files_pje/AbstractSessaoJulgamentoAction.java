package br.com.jt.pje.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Strings;

import br.com.infox.DAO.SearchField;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.utils.ItensLegendas;
import br.com.itx.component.Util;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.util.ParAssinatura;
import br.jus.pje.jt.entidades.PautaSessao;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.entidades.Voto;
import br.jus.pje.jt.enums.ClassificacaoTipoSituacaoPautaEnum;
import br.jus.pje.jt.enums.ConclusaoEnum;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;


public abstract class AbstractSessaoJulgamentoAction
    extends AbstractVotoSessaoAction {
    private static final long serialVersionUID = 1L;
    public static final TipoProcessoDocumento TIPO_PROCESSO_DOCUMENTO_ACORDAO = ParametroUtil.instance()
                                                                                             .getTipoProcessoDocumentoAcordao();
    private int totalProcessos = 0;
    private long totalJulgados = 0;
    private long totalRetirados = 0;
    private long totalPendentes = 0;
    private long totalDeliberados = 0;
    private long acompanhaRelator = 0;
    private long divergeRelator = 0;
    private long naoVotou = 0;
    private List<PautaSessao> listPautaSessao = new ArrayList<PautaSessao>();

    // Assinatura em lote
    private ArrayList<ParAssinatura> assinaturas;
    private String encodedCertChain;
    private Set<Integer> rowsToUpdate;
    @In
    protected ProcessoDocumentoManager processoDocumentoManager;
    @In
    public ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager;
    
    @Logger
	private Log log;

    //Atributo para armazenar voto por pauta para diminuir consultas ao banco de dados
    private HashMap<PautaSessao, Boolean> votoRelatorLiberadoMap = new HashMap<PautaSessao, Boolean>();
    
    /**
     * @return the assinaturas
     */
    public List<ParAssinatura> getAssinaturas() {
        if ((assinaturas == null) ||
                (assinaturas.size() != getListPautaSessao().size())) {
            assinaturas = new ArrayList<ParAssinatura>();

            List<PautaSessao> pautasSelecionadas = getListPautaSessao();

            for (PautaSessao pautaSelecionada : pautasSelecionadas) {
                ParAssinatura pa = new ParAssinatura();
                String contents;

                try {
                	iniciarHomesProcessos(pautaSelecionada.getProcessoTrf());
                	
                    inicializarIntegra(pautaSelecionada.getProcessoTrf());
                    // TODO Verificar se codificação está ok
                    contents = new String(SigningUtilities.base64Encode(
                                getModeloIntegra().getBytes()));

                    if ((contents != null) && !contents.equals("")) {
                        pa.setConteudo(contents);
                        assinaturas.add(pa);
                    } else {
                        FacesMessages.instance()
                                     .add(Severity.ERROR,
                            "Favor verificar se voto está preenchido.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            limparDadosProcesso();
        }

        return assinaturas;
    }

    public void finalizarMultiplos() {
        assinarEmLote();
    }

    public void setAssinaturas(ArrayList<ParAssinatura> assinaturas) {
        this.assinaturas = assinaturas;
    }

    public void iniciar() {
        setSessao(findById(SessaoJT.class, getIdSessao()));
        iniciarLegenda();
        carregarResultados();

        // [PJEII-4718][PJEII-4763] Alteração da obtenção da PautaSessao para comportar a tela de visualização de voto
        // do secretário de sessão
        if (getPautaSessao() == null) {
        	if (getIdPautaSessao() != null) {
        		setPautaSessao(findById(PautaSessao.class, getIdPautaSessao()));
        	} else {
        		setPautaSessao(pautaSessaoManager.getPautaProcessoApregoadoBySessao(getSessao()));
        	}
        }

        // [PJEII-4718][PJEII-4763] Alteração do bloco abaixo para comportar a alteração feita acima.
        if (getPautaSessao() != null) {
            setIdPautaSessao(getPautaSessao().getIdPautaSessao());
            setIdProcesso(getPautaSessao().getProcessoTrf().getIdProcessoTrf());
            inicializar();
            carregarQuantidadeVotos();

            listPautaSessao.clear();
            getPautaSessao().setCheckBoxSelecionado(true);
            listPautaSessao.add(getPautaSessao());
        }

        //Carregar Lista da Grid
        carregarListaPautaSessaoGrid();
    }
    
    public void carregarListaPautaSessaoGrid(){
    	super.setListaPautaSessaoGrid(this.getElaboracaoVotoList().list());
    }

    public boolean processoJaPossuiEvento(PautaSessao pautaSessao) {
        return pautaJulgamentoService.processoJaPossuiEventoLancadoNaSessao(pautaSessao.getProcessoTrf()
                                                                                       .getProcesso(),
            getSessao());
    }
    
    private void iniciarHomesProcessos(ProcessoTrf processoTrf){
		ProcessoTrfHome.instance().setInstance(null);
		ProcessoTrfHome.instance().setId(processoTrf.getIdProcessoTrf());
		ProcessoHome.instance().setInstance(null);
		ProcessoHome.instance().setId(processoTrf.getProcesso().getIdProcesso());
	}

    protected void assinarEmLote() {
        List<PautaSessao> pautasSelecionadas = getListPautaSessao();
        
        for (PautaSessao pautaSelecionada : pautasSelecionadas) {
        	iniciarHomesProcessos(pautaSelecionada.getProcessoTrf());
    		
    		inicializarIntegra(pautaSelecionada.getProcessoTrf());
    		setCertChain(this.encodedCertChain);
    		setSignature(getSignature(this.getModeloIntegra()));

    		if ((getSignature() == null) || getSignature().trim().equals("")) {
    		    FacesMessages.instance()
    		                 .add(Severity.ERROR,
    		        "Assinatura vazia para a pauta de sessão de id " +
    		        pautaSelecionada.getIdPautaSessao());

                return;
    		}

    		assinarProcessoDocumento();
    		// PJEII-4718
    		// Após assinar movimenta para Default Transition
    		movimentarAposAssinatura(pautaSelecionada.getProcessoTrf());
        }
        
        limparDadosProcesso();
        removeAll();
    }

    /**
     * PJEII-4718
	 * Frederico Carneiro
     * Movimenta o processo para a default transition da tarefa Assinar Acordao
     * @param processoTrf
     */
	public void movimentarAposAssinatura(ProcessoTrf processoTrf) {
		Integer idTarefa = Integer.valueOf(ParametroUtil.getParametro("idTarefaAssinarAcordao"));
		
		try{
			
		pautaJulgamentoService.movimentarProcessoAssinarAcordao(processoTrf, idTarefa);
			
		} catch (Exception e){
			
			e.printStackTrace();
			if(processoTrf != null && processoTrf.getNumeroProcesso() != null){
				log.error(Severity.ERROR, "[ASSINAR ACORDAO] Erro ao movimentar processo " + processoTrf.getNumeroProcesso(), e);
				FacesMessages.instance().add(Severity.ERROR, "Erro ao movimentar processo " +  processoTrf.getNumeroProcesso());
			} else {
				log.error(Severity.ERROR, "[ASSINAR ACORDAO] Erro ao movimentar processo ", e);
				FacesMessages.instance().add(Severity.ERROR, "Erro ao movimentar processo ");
			}
			
		}
		Util.beginTransaction();
	}

    private String getSignature(String modeloIntegra) {
        for (ParAssinatura par : assinaturas) {
            String doc;

            try {
                doc = new String(SigningUtilities.base64Decode(
                            par.getConteudo()));

                if (doc.equals(modeloIntegra)) {
                    return par.getAssinatura();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return null;
    }

    protected void inicializarIntegra(ProcessoTrf processoTrf) {
        OrgaoJulgador orgaoJulgador = Authenticator.getOrgaoJulgadorAtual();

        if (orgaoJulgador == null) {
            orgaoJulgador = pautaSessaoManager.getOrgaoJulgadorRedatorByProcessoSessao(processoTrf,getSessao());
        }

        setProcessoTrf(processoTrf);
        setVoto(votoManager.getVotoProcessoByOrgaoJulgadorSessao(
                getProcessoTrf(), orgaoJulgador, getSessao()));
        inicializar(orgaoJulgador);
        //construirModeloIntegra();
        construirModeloEstruturado();
    }

    protected void carregarResultados() {
        setTotalProcessos(getElaboracaoVotoList().getResultList().size());
        setTotalJulgados(pautaSessaoManager.quantidadeProcessoBySessaoClassificacao(
                getSessao(), ClassificacaoTipoSituacaoPautaEnum.J));
        setTotalRetirados(pautaSessaoManager.quantidadeProcessoBySessaoClassificacao(
                getSessao(), ClassificacaoTipoSituacaoPautaEnum.R));
        setTotalPendentes(pautaSessaoManager.quantidadeProcessoBySessaoClassificacao(
                getSessao(), ClassificacaoTipoSituacaoPautaEnum.A));
        setTotalDeliberados(pautaSessaoManager.quantidadeProcessoBySessaoClassificacao(
                getSessao(), ClassificacaoTipoSituacaoPautaEnum.D));
    }

    protected void carregarQuantidadeVotos() {
        setAcompanhaRelator(votoManager.quantidadeVotosAcompanhamRelatorByProcessoSessao(
                getProcessoTrf(), getSessao()));
        setDivergeRelator(votoManager.quantidadeVotosDivergentesByProcessoSessao(
                getProcessoTrf(), getSessao()));
        setNaoVotou(votoManager.quantidadeVotosSemConclusaoByProcessoSessao(
                getProcessoTrf(), getSessao()));

        Voto votoRelator = votoManager.getVotoProcessoByOrgaoJulgadorSessao(getProcessoTrf(),
                getProcessoTrf().getOrgaoJulgador(), getSessao());

        if ((votoRelator != null) && (votoRelator.getTipoVoto() != null) &&
                votoRelator.getTipoVoto().getConclusao().equals(ConclusaoEnum.RE)) {
            acompanhaRelator += 1;

            //naoConhece += 1;
        }
    }

    /**
     *  Funcionalidade utilizada pelo secretario da sessao na tela botoesAddRemoveAllSecretario.xhtml.
     *
     */
    public void addRemoveAllPautaSessao() {
        this.listPautaSessao = new ArrayList<PautaSessao>();
        this.setProcessoTrf(null);

        if (this.isCheckBoxSelecionarTodos()) {
            addAllPautaSessao();
        } else {
            removeAllPautaSessao();
        }
    }

	public void addAllPautaSessao(){
		for(PautaSessao ps : super.getListaPautaSessaoGrid()){
			ps.setCheckBoxSelecionado(true);
			this.listPautaSessao.add(ps);
		}
	}

 	public void removeAllPautaSessao(){
		for(PautaSessao ps : super.getListaPautaSessaoGrid()){
			ps.setCheckBoxSelecionado(false);
		}
	}
	
	public void addRemovePautaSessao(PautaSessao row){
		if(row.isCheckBoxSelecionado()){
			this.getListPautaSessao().add(row);
		}else{
			this.getListPautaSessao().remove(row);
		}
		
		super.setCheckBoxSelecionarTodos(false);
		
		if(this.getListPautaSessao().size() == super.getListaPautaSessaoGrid().size()){
			super.setCheckBoxSelecionarTodos(true);
		}
		
		// Caso tenha mais de 1 processo selecionado, processamento em lote.
		if(this.getListPautaSessao().size() > 1 || this.getListPautaSessao().size() < 1){
			super.setProcessoTrf(null);
		}
	}

    /**
     * Metodo utilizado para melhorar a performance da grid com ajax.
     * @param rowKey
     */
    public void addRowKeyToAjaxUpdate(Integer rowKey) {
        if ((rowKey != null) && (rowKey >= 0)) {
            this.rowsToUpdate = new HashSet<Integer>();
            this.rowsToUpdate.add(rowKey);
        } else {
            this.rowsToUpdate = null;
        }
    }

    public void removeAll() {
        removeAllPautaSessao();
        setListPautaSessao(new ArrayList<PautaSessao>(0));
    }

    protected abstract char getSiglaPainel();

    public void pesquisarProcessos(String sigla) {
        getElaboracaoVotoList()
            .setSearchFieldMap(new HashMap<String, SearchField>());
        getElaboracaoVotoList().addSearchFields();

        if (!Strings.isEmpty(sigla)) {
            if (sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[3]) ||
                    sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[4]) ||
                    sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[5]) ||
                    sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[6]) ||
                    sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[12]) ||
                    sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[13])) {
                filtroSituacao(sigla);
            }

            getMapLegenda().put(sigla, !getMapLegenda().get(sigla));
            getElaboracaoVotoList()
                .addSearchFieldsPainel(getMapLegenda(), getSiglaPainel());
        }

        super.setListaPautaSessaoGrid(super.getElaboracaoVotoList().list());
        /**
         * Antonio Lucas
         * [PJEII-4426] 
         * Ao desmarcar um filtro, a situação do checkbox 'Marcar Todos' não era atualizada,
         * mesmo quando mudava a quantidade de processos nasessão
         */
        verificaCheckBoxMarcarTodos();
    }
    
	// Controle dos processos apregoados e checkbox.
    protected void verificaCheckBoxMarcarTodos(){
		super.setCheckBoxSelecionarTodos(false);
		if(getListPautaSessao().size() == super.getListaPautaSessaoGrid().size()){
			super.setCheckBoxSelecionarTodos(true);
		}
    }

    private void filtroSituacao(String sigla) {
        int[] valorSigla = new int[5];

        if (sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[3])) {
            valorSigla[0] = 4;
            valorSigla[1] = 5;
            valorSigla[2] = 6;
            valorSigla[3] = 12;
            valorSigla[4] = 13;
        } else if (sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[4])) {
            valorSigla[0] = 3;
            valorSigla[1] = 5;
            valorSigla[2] = 6;
            valorSigla[3] = 12;
            valorSigla[4] = 13;
        } else if (sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[5])) {
            valorSigla[0] = 3;
            valorSigla[1] = 4;
            valorSigla[2] = 6;
            valorSigla[3] = 12;
            valorSigla[4] = 13;
        } else if (sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[6])) {
            valorSigla[0] = 3;
            valorSigla[1] = 4;
            valorSigla[2] = 5;
            valorSigla[3] = 12;
            valorSigla[4] = 13;
        } else if (sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[12])) {
            valorSigla[0] = 3;
            valorSigla[1] = 4;
            valorSigla[2] = 5;
            valorSigla[3] = 6;
            valorSigla[4] = 13;
        } else if (sigla.equals(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[13])) {
            valorSigla[0] = 3;
            valorSigla[1] = 4;
            valorSigla[2] = 5;
            valorSigla[3] = 6;
            valorSigla[4] = 12;
        }

        getMapLegenda()
            .put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[valorSigla[0]], false);
        getMapLegenda()
            .put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[valorSigla[1]], false);
        getMapLegenda()
            .put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[valorSigla[2]], false);
        getMapLegenda()
            .put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[valorSigla[3]], false);
        getMapLegenda()
            .put(ItensLegendas.SIGLAS_SECRETARIO_LEGENDAS[valorSigla[4]], false);
 
    }
    
    public boolean votoRelatorLiberado(PautaSessao pautaSessao) {
        if (votoRelatorLiberadoMap.isEmpty() || !votoRelatorLiberadoMap.containsKey(pautaSessao)) {
        	votoRelatorLiberadoMap.put(pautaSessao, votoManager.existeVotoRelatorLiberado(pautaSessao.getProcessoTrf(),
                    getSessao()));
        }
    	
    	return votoRelatorLiberadoMap.get(pautaSessao);
    }

    /*
     * PJE-JT: Ricardo Scholz : PJEII-1419 - 2012-06-19 Alteracoes feitas pela JT.
     * Programação defensiva. Inclusão de checagem de nulidade para 'getSessao()'
     * e 'getSessao().getSituacaoSessao()'.
     */
    public boolean sessaoIniciada() {
        return (getSessao() != null) &&
        (getSessao().getSituacaoSessao() != null) &&
        getSessao().getSituacaoSessao().equals(SituacaoSessaoEnum.I);
    }

    /*
     * PJE-JT: Fim.
     */
    public Integer countAcordaoAssinado(PautaSessao pautaSessao, Voto voto) {
        return processoDocumentoBinPessoaAssinaturaManager.countDocumentoAcordaoAssinado(pautaSessao.getProcessoTrf(),
            getSessao(), voto);
    }

    public void iniciarVisualizacaoVoto() {
        setPautaSessao(findById(PautaSessao.class, getIdPautaSessao()));
        setSessao(getPautaSessao().getSessao());
        setIdProcesso(getPautaSessao().getProcessoTrf().getIdProcessoTrf());
        super.inicializar();
    }

    /*
     * inicio get e set
     */
    public int getTotalProcessos() {
        return totalProcessos;
    }

    public void setTotalProcessos(int totalProcessos) {
        this.totalProcessos = totalProcessos;
    }

    public long getTotalJulgados() {
        return totalJulgados;
    }

    public void setTotalJulgados(long totalJulgados) {
        this.totalJulgados = totalJulgados;
    }

    public long getTotalRetirados() {
        return totalRetirados;
    }

    public void setTotalRetirados(long totalRetirados) {
        this.totalRetirados = totalRetirados;
    }

    public long getTotalPendentes() {
        return totalPendentes;
    }

    public void setTotalPendentes(long totalPendentes) {
        this.totalPendentes = totalPendentes;
    }

    public long getTotalDeliberados() {
        return totalDeliberados;
    }

    public void setTotalDeliberados(long totalDeliberados) {
        this.totalDeliberados = totalDeliberados;
    }

    public long getAcompanhaRelator() {
        return acompanhaRelator;
    }

    public void setAcompanhaRelator(long acompanhaRelator) {
        this.acompanhaRelator = acompanhaRelator;
    }

    public long getDivergeRelator() {
        return divergeRelator;
    }

    public void setDivergeRelator(long divergeRelator) {
        this.divergeRelator = divergeRelator;
    }

    public long getNaoVotou() {
        return naoVotou;
    }

    public void setNaoVotou(long naoVotou) {
        this.naoVotou = naoVotou;
    }

    public void setListPautaSessao(List<PautaSessao> listPautaSessao) {
        this.listPautaSessao = listPautaSessao;
    }

    public List<PautaSessao> getListPautaSessao() {
        return listPautaSessao;
    }

    public String getEncodedCertChain() {
        return encodedCertChain;
    }

    public void setEncodedCertChain(String encodedCertChain) {
        this.encodedCertChain = encodedCertChain;
    }

    public Set<Integer> getRowsToUpdate() {
        return rowsToUpdate;
    }

    public void setRowsToUpdate(Set<Integer> rowsToUpdate) {
        this.rowsToUpdate = rowsToUpdate;
    }
}
