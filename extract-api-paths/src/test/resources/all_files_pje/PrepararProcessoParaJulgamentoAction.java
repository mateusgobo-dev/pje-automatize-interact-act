package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.exception.AplicationException;
import br.com.jt.pje.manager.SessaoManager;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.EntityManagerUtil;
import br.jus.cnj.pje.nucleo.manager.SessaoPautaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.enums.RelatorRevisorEnum;

/**
 * [PJEII-4329]
 * Classe que controla o frame prepararProcessoParaJulgamento.xhtml
 *
 * @author lucio.ribeiro
 */
@Name(PrepararProcessoParaJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PrepararProcessoParaJulgamentoAction implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public static final String NAME = "prepararProcessoParaJulgamentoAction";
    private Sessao sessaoSugerida;
    private String transicaoSaidaDefault;
    private Boolean processoConcluso;
    private Boolean exigePauta;
    private List<Sessao> sessoes;
    @In
    private SessaoManager sessaoManager;
    @In
    private ProcessoTrfManager processoTrfManager;
    @In
    private EntityManagerUtil entityManagerUtil;
    @In
    private SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager;
    @In(create = false, required = false)
    private TaskInstanceHome taskInstanceHome;

    /**
     * @return ProcessoTrf corrente
     */
    public ProcessoTrf getProcessoTrf() {
        return ProcessoJbpmUtil.getProcessoTrf();
    }

    /**
     * Inicializa os campos da pagina
     */
    @Create
    public void init() {
        transicaoSaidaDefault = (String) TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);

        // preenche o combo de sessoes se o processo estiver concluso
        ProcessoTrf processoTrf = getProcessoTrf();

        Boolean varExigePauta = processoTrf.getClasseJudicial().getPauta();
        setExigePauta(varExigePauta);

        Boolean varProcessoConcluso = getProcessoTrfManager().isProcessoConcluso(processoTrf);
        setProcessoConcluso(varProcessoConcluso);

        if (getProcessoConcluso()) {
            if (getExigePauta()) {
                List<Sessao> result = getSessaoManager().getSessoesJulgamentoComDiasFechamentoPautaOrgaoColegiado(
                        processoTrf.getOrgaoJulgadorColegiado(), processoTrf.getOrgaoJulgador());
                setSessoes(result);
            }
            else {
                List<Sessao> result = getSessaoManager().getSessoesJulgamento(
                        processoTrf.getOrgaoJulgadorColegiado(), processoTrf.getOrgaoJulgador());
                setSessoes(result);
            }
        } else {
            setSessoes(null);
        }
    }

    /**
     *
     * @param sessao Sessao
     * @return String formatando a Sessao no formato Data/Hora
     */
    public String getDataHoraSessao(Sessao sessao) {
        return ProcessoTrfHome.instance().getDataHoraSessao(sessao);
    }

    /**
     * Grava os dados sugeridos da Pauta de Julgamento
     */
    public void gravar() {

        // valida a tela
        if (!beforeUpdate()) {
            return;
        }

        ProcessoTrf processoTrf = getProcessoTrf();
        processoTrf.setSessaoSugerida(getSessaoSugerida());
        
        if (getExigePauta()) {
            processoTrf.setSelecionadoPauta(Boolean.TRUE);
            processoTrf.setPessoaMarcouPauta(Authenticator.getPessoaLogada());
        }
        else {
            processoTrf.setSelecionadoJulgamento(Boolean.TRUE);
            processoTrf.setPessoaMarcouJulgamento(Authenticator.getPessoaLogada());
        }

        getProcessoTrfManager().update(processoTrf);
        getEntityManagerUtil().flush();

        if (this.transicaoSaidaDefault != null) {
            getTaskInstanceHome().end(transicaoSaidaDefault);
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro(s) alterado(s) com sucesso!");
        }
    }

    /**
     * Desfaz a gravação dos dados para colocar o processo em julgamento
     */
    public void desfazerGravarProcessoParaJulgamento() {
        ProcessoTrf processoTrf = getProcessoTrf();

        // verifica se o processo ja foi julgado
        if(getSessaoPautaProcessoTrfManager().isProcessoJulgado(processoTrf)) {
            throw new AplicationException("Não é possível realizar a operação! O processo já foi julgado.");
        }
        
        processoTrf.setSessaoSugerida(null);
        processoTrf.setSelecionadoPauta(Boolean.FALSE);
        processoTrf.setPessoaMarcouPauta((PessoaFisica) null);
        processoTrf.setSelecionadoJulgamento(Boolean.FALSE);
        processoTrf.setPessoaMarcouJulgamento((PessoaFisica) null);

        getProcessoTrfManager().update(processoTrf);
        getEntityManagerUtil().flush();
    }

    /**
     *
     * @return flag confirmando se os campos da tela foram validados
     */
    private boolean beforeUpdate() {
        if (!getProcessoConcluso()) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Não é possível gravar a data da sessão de julgamento! Este processo não possui evento Concluso.");
            return false;
        }

        if (getExigePauta()) {
            if (!podeSelecionarPauta()) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Usuário não está autorizado a realizar esta operação.");
                return false;
            }
        }
        else {
            if (!podeSelecionarJulgamento()) {
                FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Usuário não está autorizado a realizar esta operação.");
                return false;
            }
        }
    
        if ((BooleanUtils.isTrue(getProcessoTrf().getExigeRevisor()) || Authenticator.getOrgaoJulgadorColegiadoAtual().getPresidenteRelacao())
                && (getSessaoSugerida() == null)) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR, "A data da sessão de julgamento não foi selecionada! Favor Selecionar.");
            return false;
        }

        return true;
    }

    /**
     * 
     * @return Flag indicando se o usuario pode sugerir Data de Pauta de Julgamento
     */
	private boolean podeSelecionarPauta(){
        ProcessoTrf processoTrf = getProcessoTrf();
		ClasseJudicial cj = processoTrf.getClasseJudicial();
		OrgaoJulgadorColegiado ojc = processoTrf.getOrgaoJulgadorColegiado();
		boolean prontoRevisao = processoTrf.getProntoRevisao() != null ? processoTrf.getProntoRevisao() : false;
		boolean pautaSemRevisor = cj.getPauta() && !BooleanUtils.isTrue(processoTrf.getExigeRevisor());
		boolean pautaComRevisor = cj.getPauta() && BooleanUtils.isTrue(processoTrf.getExigeRevisor());
		boolean revisadoAntecipacao = processoTrf.getRevisado() || ojc.getPautaAntecRevisao();
		boolean ojRelator = processoTrf.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual());
		boolean relator = RelatorRevisorEnum.REL.equals(ojc.getRelatorRevisor()) && ojRelator;
		boolean revisor = RelatorRevisorEnum.REV.equals(ojc.getRelatorRevisor()) && !ojRelator;

		return (pautaSemRevisor && ojRelator)
			|| (pautaComRevisor && prontoRevisao && ((relator && revisadoAntecipacao) || (revisor && processoTrf
					.getRevisado())));
	}

    /**
     * 
     * @return Flag indicando se o usuario pode sugerir Data de Julgamento
     */
	private boolean podeSelecionarJulgamento(){
        ProcessoTrf processoTrf = getProcessoTrf();
		ClasseJudicial cj = processoTrf.getClasseJudicial();
		OrgaoJulgadorColegiado ojc = processoTrf.getOrgaoJulgadorColegiado();
		boolean prontoRevisao = processoTrf.getProntoRevisao() != null ? processoTrf.getProntoRevisao() : false;
		boolean semPautaRevisao = !cj.getPauta() && !BooleanUtils.isTrue(processoTrf.getExigeRevisor());
		boolean revisaoSemPauta = !cj.getPauta() && BooleanUtils.isTrue(processoTrf.getExigeRevisor());
		boolean revisadoAntecipacao = processoTrf.getRevisado() || ojc.getPautaAntecRevisao();
		boolean ojRelator = processoTrf.getOrgaoJulgador().equals(Authenticator.getOrgaoJulgadorAtual());
		boolean relator = RelatorRevisorEnum.REL.equals(ojc.getRelatorRevisor()) && ojRelator;
		boolean revisor = RelatorRevisorEnum.REV.equals(ojc.getRelatorRevisor()) && !ojRelator;

		return (semPautaRevisao && ojRelator)
			|| (revisaoSemPauta && prontoRevisao && 
                ((relator && revisadoAntecipacao) || (revisor && processoTrf.getRevisado())));
	}
    
    /**
     * @return the sessaoManager
     */
    public SessaoManager getSessaoManager() {
        return sessaoManager;
    }

    /**
     * @param sessaoManager the sessaoManager to set
     */
    public void setSessaoManager(SessaoManager sessaoManager) {
        this.sessaoManager = sessaoManager;
    }

    /**
     * @return the sessoes
     */
    public List<Sessao> getSessoes() {
        return sessoes;
    }

    /**
     * @param sessoes the sessoes to set
     */
    public void setSessoes(List<Sessao> sessoes) {
        this.sessoes = sessoes;
    }

    /**
     * @return the sessaoSugerida
     */
    public Sessao getSessaoSugerida() {
        return sessaoSugerida;
    }

    /**
     * @param sessaoSugerida the sessaoSugerida to set
     */
    public void setSessaoSugerida(Sessao sessaoSugerida) {
        this.sessaoSugerida = sessaoSugerida;
    }

    /**
     * @return the processoTrfManager
     */
    public ProcessoTrfManager getProcessoTrfManager() {
        return processoTrfManager;
    }

    /**
     * @param processoTrfManager the processoTrfManager to set
     */
    public void setProcessoTrfManager(ProcessoTrfManager processoTrfManager) {
        this.processoTrfManager = processoTrfManager;
    }

    /**
     * @return the entityManagerUtil
     */
    public EntityManagerUtil getEntityManagerUtil() {
        return entityManagerUtil;
    }

    /**
     * @param entityManagerUtil the entityManagerUtil to set
     */
    public void setEntityManagerUtil(EntityManagerUtil entityManagerUtil) {
        this.entityManagerUtil = entityManagerUtil;
    }

    /**
     * @return the taskInstanceHome
     */
    public TaskInstanceHome getTaskInstanceHome() {
        return taskInstanceHome;
    }

    /**
     * @param taskInstanceHome the taskInstanceHome to set
     */
    public void setTaskInstanceHome(TaskInstanceHome taskInstanceHome) {
        this.taskInstanceHome = taskInstanceHome;
    }

    /**
     * @return the sessaoPautaProcessoTrfManager
     */
    public SessaoPautaProcessoTrfManager getSessaoPautaProcessoTrfManager() {
        return sessaoPautaProcessoTrfManager;
    }

    /**
     * @param sessaoPautaProcessoTrfManager the sessaoPautaProcessoTrfManager to set
     */
    public void setSessaoPautaProcessoTrfManager(SessaoPautaProcessoTrfManager sessaoPautaProcessoTrfManager) {
        this.sessaoPautaProcessoTrfManager = sessaoPautaProcessoTrfManager;
    }

    /**
     * @return the processoConcluso
     */
    public Boolean getProcessoConcluso() {
        return processoConcluso;
    }

    /**
     * @param processoConcluso the processoConcluso to set
     */
    public void setProcessoConcluso(Boolean processoConcluso) {
        this.processoConcluso = processoConcluso;
    }

    /**
     * @return the exigePauta
     */
    public Boolean getExigePauta() {
        return exigePauta;
    }

    /**
     * @param exigePauta the exigePauta to set
     */
    public void setExigePauta(Boolean exigePauta) {
        this.exigePauta = exigePauta;
    }
}
