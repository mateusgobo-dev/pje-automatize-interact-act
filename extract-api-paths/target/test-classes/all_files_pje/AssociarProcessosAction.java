package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;

/**
 * PJEII-3755 Classe responsável por controlar as ações da página de Associar
 * Processos
 *
 * @author lucio.ribeiro
 */
@Name(AssociarProcessosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class AssociarProcessosAction implements Serializable {

    public static final String NAME = "associarProcessosAction";
    private static final LogProvider log = Logging.getLogProvider(AssociarProcessosAction.class);
    private static final long serialVersionUID = 1L;
    private ProcessoTrf processoAssociado;
    private TipoConexaoEnum tipoConexao;
    private List<ProcessoTrfConexao> processosTRFConexoes = new ArrayList<ProcessoTrfConexao>();
    private List<ProcessoTrfConexao> processosTRFConexosAssociadosAoPrincipal = new ArrayList<ProcessoTrfConexao>();
    @In
    private ProcessoTrfConexaoManager processoTrfConexaoManager;
    @In
    private TramitacaoProcessualService tramitacaoProcessualService;
    @In
    private ParametroService parametroService;

    /**
     * Método executado quando a ação de associar processos é invocado
     */
    public void associarProcesso() {
        if (getProcessoAssociado() != null) {

            if (!beforeAssociarProcesso()) {
                return;
            }

            Date dataAssociacao = new Date();
            associarPrincipalAoConexo(dataAssociacao);
            associarConexoAoPrincipal(dataAssociacao);

            setProcessoAssociado(null);
            setTipoConexao(null);
        }
    }

    /**
     * Método executado quando a ação de Incluir no processo principal é
     * invocado
     */
    public void persist() {
        if (!beforePersistOrUpdate()) {
            return;
        }

        try {
            //obtem variavel para verificar se eh pra lancar movimentacao
            Boolean lancarMovimentacao = (Boolean) getTramitacaoProcessualService().recuperaVariavelTarefa("pje:fluxo:lancarMovimentacao");

            // obtem o codigo do evento de apensamento
            String codEvento = null;
            if ((lancarMovimentacao == null) || lancarMovimentacao) {
            	codEvento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_APENSAMENTO;
            }

            for (ProcessoTrfConexao p : getProcessosTRFConexoes()) {
                boolean temDuplicidade = getProcessoTrfConexaoManager().verificaDuplicidade(p.getProcessoTrf(), p.getProcessoTrfConexo());

                if (temDuplicidade) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                            "Associação entre " + p.getProcessoTrf()
                            + " e " + p.getProcessoTrfConexo() + " já cadastrada!");
                    return;
                } else {
                    getProcessoTrfConexaoManager().persist(p, codEvento);
                }
            }

            for (ProcessoTrfConexao p : getProcessosTRFConexosAssociadosAoPrincipal()) {
                boolean temDuplicidade = getProcessoTrfConexaoManager().verificaDuplicidade(p.getProcessoTrf(), p.getProcessoTrfConexo());

                if (temDuplicidade) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                            "Associação entre " + p.getProcessoTrf()
                            + " e " + p.getProcessoTrfConexo() + " já cadastrada!");
                    return;
                } else {
                    getProcessoTrfConexaoManager().persist(p, codEvento);
                }
            }

            getProcessoTrfConexaoManager().flush();
            getProcessosTRFConexoes().clear();
            getProcessosTRFConexosAssociadosAoPrincipal().clear();

            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro(s) cadastrado(s) com sucesso!");
        } catch (PJeBusinessException ex) {
            FacesMessages.instance().clear();
            FacesMessages.instance().add(Severity.ERROR, "Erro ao realizar a associação do Processo");
            log.error(ex);
        }
    }

    /**
     * Método executado quando a ação de remover um processo CONEXO é invocado
     */
    public void remove(ProcessoTrfConexao obj) {
        try {
            //obtem variavel para verificar se eh pra lancar movimentacao
            Boolean lancarMovimentacao = (Boolean) getTramitacaoProcessualService().recuperaVariavelTarefa("pje:fluxo:lancarMovimentacao");

            // obtem o codigo do evento de desapensamento
            String codEvento = null;
            if ((lancarMovimentacao == null) || lancarMovimentacao) {
            	codEvento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_DESAPENSAMENTO;
            }

            getProcessoTrfConexaoManager().remove(obj, codEvento);
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro removido com sucesso!");
        } catch (PJeBusinessException ex) {
            FacesMessages.instance().clear();
            FacesMessages.instance().add(Severity.ERROR, "Erro ao remover a associação do Processo");
            log.error(ex);
        }
    }

    /**
     *
     * @return Flag que indica se pode ou não adicionar na lista de processos
     * associados o processo selecionado
     */
    private boolean beforeAssociarProcesso() {
        if (getProcessoAssociado() != null) {
            ProcessoTrf processoPrincipal = ProcessoTrfHome.instance().getInstance();

            for (ProcessoTrfConexao ptc : getProcessosTRFConexoes()) {
                if ((ptc.getProcessoTrf().equals(processoPrincipal)) && (ptc.getProcessoTrfConexo().equals(getProcessoAssociado()))) {
                    FacesMessages.instance().add(StatusMessage.Severity.ERROR, "O processo selecionado já foi adicionado a lista abaixo!");
                    return false;
                }
            }

            if (getTipoConexao() == null) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Favor selecionar o tipo de associação!");
                return false;
            }
        }

        return true;
    }

    /**
     * Método utilizado para criar o processo conexo e associar o processo
     * PRINCIPAL ao CONEXO que foi selecionado
     *
     * @param dataAssociacao Data da execução da ação
     */
    private void associarPrincipalAoConexo(Date dataAssociacao) {
        ProcessoTrfConexao pc = criarProcessoTrfConexo(dataAssociacao);

        ProcessoTrf processoSelecionado = ProcessoTrfHome.instance().getInstance();
        pc.setProcessoTrf(processoSelecionado);
        pc.setProcessoTrfConexo(getProcessoAssociado());

        getProcessosTRFConexoes().add(pc);
    }

    /**
     * Método utilizado para criar o processo conexo e associar o processo
     * CONEXO ao PRINCIPAL que foi selecionado
     *
     * @param dataAssociacao Data da execução da ação
     */
    private void associarConexoAoPrincipal(Date dataAssociacao) {
        ProcessoTrfConexao pc = criarProcessoTrfConexo(dataAssociacao);
        pc.setProcessoTrf(getProcessoAssociado());

        ProcessoTrf processoSelecionado = ProcessoTrfHome.instance().getInstance();
        pc.setProcessoTrfConexo(processoSelecionado);

        getProcessosTRFConexosAssociadosAoPrincipal().add(pc);
    }

    /**
     *
     * @param dataAssociacao Data da execução da ação
     * @return ProcessoTrfConexo criado
     */
    private ProcessoTrfConexao criarProcessoTrfConexo(Date dataAssociacao) {
        ProcessoTrfConexao pc = new ProcessoTrfConexao();

        pc.setDtValidaPrevencao(dataAssociacao);
        pc.setPrevencao(PrevencaoEnum.PR);
        pc.setAtivo(Boolean.TRUE);
        pc.setTipoConexao(getTipoConexao());
        pc.setDtPossivelPrevencao(dataAssociacao);
        pc.setJustificativa(null);

        return pc;
    }

    /**
     *
     * @return Flag que indica se pode ou não inserir ou gravar o processo
     * CONEXO selecionado
     */
    protected boolean beforePersistOrUpdate() {
        ProcessoTrf processoSelecionado = ProcessoTrfHome.instance().getInstance();
        if (processoSelecionado == null) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Favor selecionar o processo principal!");
            return false;
        }

        if ((getProcessosTRFConexoes() == null) || (getProcessosTRFConexoes().isEmpty())) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Favor selecionar o processo associado!");
            return false;
        }

        return true;
    }

    /**
     *
     * @return Lista de TipoConexao
     */
    public List<TipoConexaoEnum> getTipoConexaoItems() {
        List<TipoConexaoEnum> lista = new ArrayList<TipoConexaoEnum>();
        lista.add(TipoConexaoEnum.DM);
        lista.add(TipoConexaoEnum.DP);
        lista.add(TipoConexaoEnum.PR);
        return lista;
    }

    /**
     * @return the processoAssociado
     */
    public ProcessoTrf getProcessoAssociado() {
        return processoAssociado;
    }

    /**
     * @param processoAssociado the processoAssociado to set
     */
    public void setProcessoAssociado(ProcessoTrf processoAssociado) {
        this.processoAssociado = processoAssociado;
    }

    /**
     * @return the tipoConexao
     */
    public TipoConexaoEnum getTipoConexao() {
        return tipoConexao;
    }

    /**
     * @param tipoConexao the tipoConexao to set
     */
    public void setTipoConexao(TipoConexaoEnum tipoConexao) {
        this.tipoConexao = tipoConexao;
    }

    /**
     * @return the processosTRFConexoes
     */
    public List<ProcessoTrfConexao> getProcessosTRFConexoes() {
        return processosTRFConexoes;
    }

    /**
     * @param processosTRFConexoes the processosTRFConexoes to set
     */
    public void setProcessosTRFConexoes(List<ProcessoTrfConexao> processosTRFConexoes) {
        this.processosTRFConexoes = processosTRFConexoes;
    }

    /**
     * @return the processosTRFConexosAssociadosAoPrincipal
     */
    public List<ProcessoTrfConexao> getProcessosTRFConexosAssociadosAoPrincipal() {
        return processosTRFConexosAssociadosAoPrincipal;
    }

    /**
     * @param processosTRFConexosAssociadosAoPrincipal the
     * processosTRFConexosAssociadosAoPrincipal to set
     */
    public void setProcessosTRFConexosAssociadosAoPrincipal(List<ProcessoTrfConexao> processosTRFConexosAssociadosAoPrincipal) {
        this.processosTRFConexosAssociadosAoPrincipal = processosTRFConexosAssociadosAoPrincipal;
    }

    /**
     * @return the processoTrfConexaoManager
     */
    public ProcessoTrfConexaoManager getProcessoTrfConexaoManager() {
        return processoTrfConexaoManager;
    }

    /**
     * @param processoTrfConexaoManager the processoTrfConexaoManager to set
     */
    public void setProcessoTrfConexaoManager(ProcessoTrfConexaoManager processoTrfConexaoManager) {
        this.processoTrfConexaoManager = processoTrfConexaoManager;
    }

    /**
     * @return the tramitacaoProcessualService
     */
    public TramitacaoProcessualService getTramitacaoProcessualService() {
        return tramitacaoProcessualService;
    }

    /**
     * @param tramitacaoProcessualService the tramitacaoProcessualService to set
     */
    public void setTramitacaoProcessualService(TramitacaoProcessualService tramitacaoProcessualService) {
        this.tramitacaoProcessualService = tramitacaoProcessualService;
    }

    /**
     * @return the parametroService
     */
    public ParametroService getParametroService() {
        return parametroService;
    }

    /**
     * @param parametroService the parametroService to set
     */
    public void setParametroService(ParametroService parametroService) {
        this.parametroService = parametroService;
    }
}
