package br.jus.je.pje.action;

import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.ibpm.home.ProcessoHome;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.PublicadorDJE;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.view.fluxo.ComunicacaoProcessualAction;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.Log;

/**
 * Componente Action usado para interface entre a View e a tela de cadastro de
 * expediente do DEJE.
 *
 * @author lucio.ribeiro
 */
@Name(CadastroExpedienteDEJEAction.NAME)
@Scope(ScopeType.PAGE)
public class CadastroExpedienteDEJEAction implements Serializable {

    public static final String NAME = "cadastroExpedienteDEJEAction";
    private static final long serialVersionUID = 1L;
    @Logger
    private Log logger;
    @In(create = true, required = false)
    private ComunicacaoProcessualAction comunicacaoProcessualAction;
    @In
    private ProcessoDocumentoBinManager processoDocumentoBinManager;
    @In(create = true, required = false)
    private PublicadorDJE publicadorDJE;
    private Map<ProcessoExpediente, Boolean> processoExpedienteSelecionadoMap;
    private Map<ProcessoExpediente, Boolean> processoExpedientePublicadoMap;

    /**
     * Método de inicialização da classe
     */
    @Create
    public void init() {
        // inicializa os maps que controlam os expedientes selecionados e enviados para publicação
        setProcessoExpedientePublicadoMap(new HashMap<ProcessoExpediente, Boolean>());
        setProcessoExpedienteSelecionadoMap(new HashMap<ProcessoExpediente, Boolean>());

        List<ProcessoExpediente> expedientesDiario = comunicacaoProcessualAction.getExpedientesDiario();
        for (ProcessoExpediente processoExpediente : expedientesDiario) {
            // para indicar se o expediente foi selecionado pelo usuario
            getProcessoExpedienteSelecionadoMap().put(processoExpediente, Boolean.FALSE);

            // para indicar se o expediente já foi enviado para o diário
            getProcessoExpedientePublicadoMap().put(processoExpediente, null);
        }
    }

    /**
     * @return ProcessoTrf corrente
     */
    public ProcessoTrf getProcessoTrf() {
        return ProcessoJbpmUtil.getProcessoTrf();
    }

    /**
     * Controla os expedientes que foram selecionados pelo usuario
     *
     * @param exp ProcessoExpediente selecionado
     */
    public void selecionarExpediente(ProcessoExpediente exp) {
        if (getProcessoExpedienteSelecionadoMap().get(exp)) {
            getProcessoExpedienteSelecionadoMap().put(exp, Boolean.FALSE);
        } else {
            getProcessoExpedienteSelecionadoMap().put(exp, Boolean.TRUE);
        }
    }

    /**
     *
     * @return Flag indicando se tem algum documento selecionado para publicação
     */
    private boolean validacaoEnviar() {
        boolean validacaoOK = false;

        // verifica se tem algum expediente selecionado para publicar no DE
        for (Iterator<ProcessoExpediente> it = getProcessoExpedienteSelecionadoMap().keySet().iterator(); it.hasNext();) {
            ProcessoExpediente processoDocumento = it.next();
            if (getProcessoExpedienteSelecionadoMap().get(processoDocumento)) {
                validacaoOK = true;
                break;
            }
        }

        return validacaoOK;
    }

    /**
     * Envia o documento para publicação no Diário Eletrônico
     */
    public void enviar() {
        if (!validacaoEnviar()) {
            FacesMessages.instance().add(StatusMessage.Severity.INFO,
                    "Não há nenhum expediente selecionado para publicação.");
            return;
        }

        boolean expedientesPublicados = true;

        // obtem o usuario logado
        Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();
        String cpf = pessoaLogada.getDocumentoCpfCnpj();
        cpf = cpf.substring(0, 3) + cpf.substring(4, 7) + cpf.substring(8, 11) + cpf.substring(12);

        // obtem alguns dados do processo
        String numeroProcesso = getProcessoTrf().getProcesso().getNumeroProcesso();
        String classeProcessual = getProcessoTrf().getClasseJudicialStr();
        String descricaoProcesso = "Processo nº " + numeroProcesso + " (" + classeProcessual + ")";

        // lista os ProcessoExpediente para publicar no DE
        List<ProcessoExpediente> expedientesDiario = comunicacaoProcessualAction.getExpedientesDiario();
        for (ProcessoExpediente processoExpediente : expedientesDiario) {
            // verifica se o expediente foi selecionado pelo usuário
            if (getProcessoExpedienteSelecionadoMap().get(processoExpediente)) {
                String idProcessoExpediente = processoExpediente.getIdProcessoExpediente() + "";
                ProcessoDocumentoBin pdbin = processoExpediente.getProcessoDocumento().getProcessoDocumentoBin();
                String tipoDocumento = processoExpediente.getProcessoDocumento().getTipoProcessoDocumento().getTipoProcessoDocumento();
                int idProcessoParteExpediente = processoExpediente.getProcessoParteExpedienteList().get(0).getIdProcessoParteExpediente();

                try {
                    // verifica se é PDF
                    if (pdbin.isBinario()) {
                        // TODO: enviar documento PDF
                        byte[] documento = getProcessoDocumentoBinManager().getBinaryData(pdbin);

                        // envia documento PDF para o DE
                        publicadorDJE.publicar(cpf + "#" + tipoDocumento,
                                descricaoProcesso, idProcessoExpediente, documento,
                                getProcessoTrf().getIdProcessoTrf(), idProcessoParteExpediente);

                    } else {
                        String documentoHtml = pdbin.getModeloDocumento();

                        // envia documento HTML para o DE
                        publicadorDJE.publicar(cpf + "#" + tipoDocumento,
                                descricaoProcesso, idProcessoExpediente, documentoHtml);
                    }

                    // indica que este ProcessoExpediente foi enviado para publicação
                    getProcessoExpedientePublicadoMap().put(processoExpediente, Boolean.TRUE);
                } catch (PontoExtensaoException e) {
                    logger.error(e);
                    expedientesPublicados = false;

                    // indica que este ProcessoExpediente NÃO foi enviado para publicação
                    getProcessoExpedientePublicadoMap().put(processoExpediente, Boolean.FALSE);
                } catch (Exception e) {
                    logger.error(e);
                    expedientesPublicados = false;

                    // indica que este ProcessoExpediente NÃO foi enviado para publicação
                    getProcessoExpedientePublicadoMap().put(processoExpediente, Boolean.FALSE);
                }
            }
        }

        // exibe mensagem na tela para o usuario
        FacesMessages.instance().clear();
        if (expedientesPublicados) {
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Matéria(s) publicada(s) com sucesso.");
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "Não foi possível enviar um ou mais expedientes para publicação. "
                    + "Verifique a coluna \"Enviado?\" para saber quais expedientes "
                    + "não foram enviados e tente novamente.");
        }
    }

    /**
     * @return the processoExpedientePublicadoMap
     */
    public Map<ProcessoExpediente, Boolean> getProcessoExpedientePublicadoMap() {
        return processoExpedientePublicadoMap;
    }

    /**
     * @param processoExpedientePublicadoMap the processoExpedientePublicadoMap
     * to set
     */
    public void setProcessoExpedientePublicadoMap(Map<ProcessoExpediente, Boolean> processoExpedientePublicadoMap) {
        this.processoExpedientePublicadoMap = processoExpedientePublicadoMap;
    }

    /**
     * @return the processoExpedienteSelecionadoMap
     */
    public Map<ProcessoExpediente, Boolean> getProcessoExpedienteSelecionadoMap() {
        return processoExpedienteSelecionadoMap;
    }

    /**
     * @param processoExpedienteSelecionadoMap the
     * processoExpedienteSelecionadoMap to set
     */
    public void setProcessoExpedienteSelecionadoMap(Map<ProcessoExpediente, Boolean> processoExpedienteSelecionadoMap) {
        this.processoExpedienteSelecionadoMap = processoExpedienteSelecionadoMap;
    }

    /**
     * @return the processoDocumentoBinManager
     */
    public ProcessoDocumentoBinManager getProcessoDocumentoBinManager() {
        return processoDocumentoBinManager;
    }

    /**
     * @param processoDocumentoBinManager the processoDocumentoBinManager to set
     */
    public void setProcessoDocumentoBinManager(ProcessoDocumentoBinManager processoDocumentoBinManager) {
        this.processoDocumentoBinManager = processoDocumentoBinManager;
    }
}
