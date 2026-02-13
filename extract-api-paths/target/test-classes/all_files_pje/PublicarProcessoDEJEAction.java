package br.jus.je.pje.action;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.component.ValidacaoAssinaturaProcessoDocumento;
import br.com.infox.cliente.home.ProcessoDocumentoBinHome;
import br.com.infox.cliente.util.ProcessoJbpmUtil;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.component.FileHome;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.PublicadorDJE;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinPessoaAssinaturaManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;

/**
 * Criada na solicitação [PJEII-6117] Classe que controla o frame
 * publicarProcessoDEJE.xhtml
 *
 * @author lucio.ribeiro
 */
@Name(PublicarProcessoDEJEAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PublicarProcessoDEJEAction implements Serializable {

    public static final String NAME = "publicarProcessoDEJEAction";
    private static final long serialVersionUID = 1L;
    private static final LogProvider log = Logging.getLogProvider(PublicarProcessoDEJEAction.class);
    private Map<ProcessoDocumento, Boolean> documentosSelecionadosMap;
    private Map<ProcessoDocumento, Boolean> processoDocumentoPublicadoMap;
    private Map<ProcessoDocumento, Boolean> processoDocumentoJaEnviadoPublicacaoAnteriorMap;
    private List<ProcessoDocumento> processoDocumentoList;

    @In
    private ProcessoExpedienteManager processoExpedienteManager;
    
    @In
    private ProcessoDocumentoExpedienteManager processoDocumentoExpedienteManager;
    
    @In
    private ProcessoDocumentoManager processoDocumentoManager;
    
    @In
    private ProcessoDocumentoBinManager processoDocumentoBinManager;
    
    @In
    private ProcessoDocumentoBinHome processoDocumentoBinHome;
    
    @In
    private ProcessoDocumentoBinPessoaAssinaturaManager processoDocumentoBinPessoaAssinaturaManager;
    
    @In
    private DocumentoJudicialService documentoJudicialService;
    
    @In
    private AtoComunicacaoService atoComunicacaoService;
    
    @In(create = true, required = false)
    private PublicadorDJE publicadorDJE;
    
    @In
    private ParametroService parametroService;
    
    @In
    private FacesMessages facesMessages;

    private String parametroIdPessoPublica; 
    
    private boolean parametroPessoaPublicaOkey = false;


    /**
     * Método de inicialização da classe.
     */
    @Create
    public void init() {
    
    	//recuperar o id da pessoa no parâmetro 
        parametroIdPessoPublica = parametroService.valueOf(Parametros.ID_DESTINACAO_PESSOA_CIENCIA_PUBLICA);
        
        if(parametroIdPessoPublica!=null){
        	
        	//Existe o parametro setado
        	parametroPessoaPublicaOkey = true;
        	
        }else{
        	return;
        }
    	
    	List pdl = processoDocumentoManager.getDocumentosAssinadosSemProcessoExpediente(getProcessoTrf().getIdProcessoTrf(), new Integer(parametroIdPessoPublica));
        setProcessoDocumentoList(pdl);

        // inicializa o map que controla os documentos selecionados
        setDocumentosSelecionadosMap(new HashMap<ProcessoDocumento, Boolean>());
        setProcessoDocumentoPublicadoMap(new HashMap<ProcessoDocumento, Boolean>());
        setProcessoDocumentoJaEnviadoPublicacaoAnteriorMap(new HashMap<ProcessoDocumento, Boolean>());
        
        for (ProcessoDocumento doc : getProcessoDocumentoList()) {
            // para indicar se o documento foi selecionado pelo usuario
            getDocumentosSelecionadosMap().put(doc, Boolean.FALSE);

            // para indicar se o processo já foi enviado para o diário
            getProcessoDocumentoPublicadoMap().put(doc, null);
            
            
        }
        
        List<ProcessoDocumento> pdljaEnviadosList = processoDocumentoManager.getDocumentosAssinadosJaEnviadosParaPublicacaoDeProcesso(getProcessoTrf().getIdProcessoTrf(), new Integer(parametroIdPessoPublica));
        
        for(ProcessoDocumento doc : pdljaEnviadosList ){
        	getProcessoDocumentoJaEnviadoPublicacaoAnteriorMap().put(doc, Boolean.TRUE);
        }
       
        
    	
    	
    }

    /**
     * @return ProcessoTrf corrente
     */
    public ProcessoTrf getProcessoTrf() {
        return ProcessoJbpmUtil.getProcessoTrf();
    }

    /**
     * Controla os documentos que foram selecionados pelo usuario
     *
     * @param doc Documento selecionado
     */
    public void selecionarDocumento(ProcessoDocumento doc) {
        if (getDocumentosSelecionadosMap().get(doc)) {
            getDocumentosSelecionadosMap().put(doc, Boolean.FALSE);
        } else {
            getDocumentosSelecionadosMap().put(doc, Boolean.TRUE);
        }
    }

    /**
     *
     * @return Flag indicando se tem algum documento selecionado para publicação
     */
    private boolean validacaoPublicar() {
        boolean validacaoOK = false;

        // verifica se tem algum documento selecionado para publicar no DE
        for (Iterator<ProcessoDocumento> it = getDocumentosSelecionadosMap().keySet().iterator(); it.hasNext();) {
            ProcessoDocumento processoDocumento = it.next();
            if (getDocumentosSelecionadosMap().get(processoDocumento)) {
                validacaoOK = true;
                break;
            }
        }

        return validacaoOK;
    }

    /**
     * Método executado para publicar os documentos selecionados no Diário
     * Eletrônico
     */
    public void publicar() {
        if (!validacaoPublicar()) {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "Não há nenhum documento selecionado para enviar para publicação.");
            return;
        }
        
        if(publicadorDJE == null) {
        	 FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                     "Conector para o DJe não encontrado");
             return;
        }

        boolean documentosPublicados = true;

        // obtem o usuario logado
        Pessoa pessoaLogada = (Pessoa) ProcessoHome.instance().getUsuarioLogado();
        String cpf = pessoaLogada.getDocumentoCpfCnpj();
        cpf = cpf.substring(0, 3) + cpf.substring(4, 7) + cpf.substring(8, 11) + cpf.substring(12);

        // obtem alguns dados do processo
        String numeroProcesso = getProcessoTrf().getProcesso().getNumeroProcesso();
        String classeProcessual = getProcessoTrf().getClasseJudicialStr();
        String descricaoProcesso = "Processo nº " + numeroProcesso + " (" + classeProcessual + ")";
        
        for (Iterator<ProcessoDocumento> it = getDocumentosSelecionadosMap().keySet().iterator(); it.hasNext();) {
            ProcessoDocumento processoDocumento = it.next();

            // se o documento está selecionado para publicação
            if (getDocumentosSelecionadosMap().get(processoDocumento)) {
                ProcessoDocumentoBin pdbin = processoDocumento.getProcessoDocumentoBin();
                String tipoDocumento = processoDocumento.getTipoProcessoDocumento().getTipoProcessoDocumento();

                try {
                    int idProcessoExpediente = inserirProcessoExpediente(processoDocumento);
                    ProcessoExpediente processoExpediente = processoExpedienteManager.find(idProcessoExpediente);
                    ProcessoParteExpediente ppe = processoExpediente.getProcessoParteExpedienteList().get(0);
                    OrgaoJulgador oj = ppe.getProcessoJudicial().getOrgaoJulgador();

                    byte[] documento = null;
                    if (pdbin.isBinario()) {
                        documento = getProcessoDocumentoBinManager().getBinaryData(pdbin);
                    } else {
                        documento = pdbin.getModeloDocumento().getBytes();
                    }
                    
                    // PJEII-19615 Alterado para ficar igual ao ComunicacaoProcessualAction
        		    publicadorDJE.publicar(Integer.toString(oj.getIdOrgaoJulgador()), ppe.getPessoaParte().getNome(),
        	 			    ppe.getPessoaParte().getDocumentoCpfCnpj(), documento,
        				    ppe.getProcessoJudicial().getIdProcessoTrf(), ppe.getIdProcessoParteExpediente());
                    
                    // indica que este ProcessoExpediente foi publicado
                    getProcessoDocumentoPublicadoMap().put(processoDocumento, Boolean.TRUE);
                } catch (PontoExtensaoException e) {
        		    log.error(e.getMessage(), e);
                    documentosPublicados = false;

                    // indica que este ProcessoExpediente NÃO foi publicado
                    getProcessoDocumentoPublicadoMap().put(processoDocumento, Boolean.FALSE);
                } catch (Exception e) {
        		    log.error(e.getMessage(), e);
                    documentosPublicados = false;

                    // indica que este ProcessoExpediente NÃO foi publicado
                    getProcessoDocumentoPublicadoMap().put(processoDocumento, Boolean.FALSE);
                }
            }
        }

        // exibe mensagem na tela para o usuario
        FacesMessages.instance().clear();
        if (documentosPublicados) {
            FacesMessages.instance().add(StatusMessage.Severity.INFO, "Documento(s) enviados para publicação com sucesso.");
        } else {
            FacesMessages.instance().add(StatusMessage.Severity.ERROR,
                    "Não foi possível enviar um ou mais documentos para publicação. "
                    + "Verifique a coluna \"Enviado?\" para saber quais documentos "
                    + "não foram enviados e tente novamente.");
        }
    }

    @In
    PessoaService pessoaService;
    
    @In
    ProcessoParteExpedienteManager processoParteExpedienteManager;

    
    /**
     * Este método foi necessário para que o VerificadorPeriodico possa 
     * consultar o ProcessoExpediente associado ao documento publicado para
     * poder lançar a movimentação
     * 
     * @param pd ProcessoDocumento para que o expediente seja cadastrado
     * @return Int com o código do ProcessoExpediente cadastrado
     * @throws PJeBusinessException Se ocorrer alguma exceção durante o cadastro
     */
    private int inserirProcessoExpediente(ProcessoDocumento pd) 
            throws PJeBusinessException {
    	
    	ProcessoDocumento pdNovo = this.documentoJudicialService.getDocumento();
    	this.atoComunicacaoService.copiaDadosDocumento(pd, pdNovo);
    	pdNovo.setIdInstanciaOrigem(pd.getIdInstanciaOrigem());
    	pdNovo.setInstancia(pd.getInstancia());
    	pdNovo.setTipoProcessoDocumento(pd.getTipoProcessoDocumento());
    	this.processoDocumentoManager.inserirProcessoDocumento(pdNovo, pd.getProcessoTrf(), pd.getProcessoDocumentoBin());
    	
    	ProcessoExpediente pe = new ProcessoExpediente();
    	pe.setDocumentoExistente(Boolean.TRUE);
    	pe.setDtCriacao(new Date());
    	pe.setInTemporario(Boolean.FALSE);
    	pe.setMeioExpedicaoExpediente(ExpedicaoExpedienteEnum.P);
    	pe.setProcessoDocumento(pdNovo);
    	pe.setProcessoDocumentoVinculadoExpediente(pd);
    	pe.setProcessoTrf(getProcessoTrf());
    	pe.setTipoProcessoDocumento(pd.getTipoProcessoDocumento());
    	pe.setUrgencia(Boolean.FALSE);
    	processoExpedienteManager.persistAndFlush(pe);
        
        Pessoa pessoaPublica = null;
        pessoaPublica = pessoaService.findById(new Integer(parametroIdPessoPublica));
               
        ProcessoParteExpediente ppe = new ProcessoParteExpediente();
		ppe.setCienciaSistema(false);
		ppe.setDtCienciaParte(null);
		ppe.setDtPrazoLegal(null);
		ppe.setDtPrazoProcessual(null);
		ppe.setPessoaParte(pessoaPublica);
		ppe.setPrazoLegal(0);
		ppe.setTipoPrazo(TipoPrazoEnum.S);
		ppe.setProcessoExpediente(pe);
		ppe.setFechado(false);
		ppe.setProcessoJudicial(pe.getProcessoTrf());
		
        processoParteExpedienteManager.persistAndFlush(ppe);
        pe.getProcessoParteExpedienteList().add(ppe);
        
        ProcessoDocumentoExpediente pde = new ProcessoDocumentoExpediente();
        pde.setProcessoDocumento(pd);
        pde.setProcessoExpediente(pe);
        pde.setAnexo(Boolean.FALSE);
        processoDocumentoExpedienteManager.persistAndFlush(pde);
        pe.getProcessoDocumentoExpedienteList().add(pde);

        return pe.getIdProcessoExpediente();
    }
    
    /**
     * Executado para fazer o download do documento pdf
     * @return String
     */
	public String setDownloadInstance() {
		exportData();
		return "/download.xhtml";
	}

    /**
     * Metodo responsavel por gerar o pdf para download
     */
	private void exportData() {
		String idProc = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("idProcessoDocumentoBin");
		String nomeArquivo = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("nomeArqProcDocBin");

        FileHome fileHome = FileHome.instance();
		try {
			fileHome.setFileName(nomeArquivo);
            
            ProcessoDocumentoBin pdbin = getProcessoDocumentoBinManager().findById(new Integer(idProc));
            byte[] data = getProcessoDocumentoBinManager().getBinaryData(pdbin);
            data = ValidacaoAssinaturaProcessoDocumento.instance().inserirInfoAssinaturasPDF(pdbin, data);
            fileHome.setData(data);
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Erro ao abrir o documento.");
			e.printStackTrace();
			log.error("Erro ao abrir documento." + e.getMessage());
		}
		Contexts.getConversationContext().set("fileHome", fileHome);
	}

    /**
     *
     * @param pdb ProcessoDocumentoBin
     * @return Data de assinatura do documento
     */
    public Date getDataPrimeiraAssinatura(ProcessoDocumentoBin pdb) {
        return processoDocumentoBinPessoaAssinaturaManager.getDataPrimeiraAssinaturaDocumento(pdb);
    }

    /**
     *
     * @param pd ProcessoDocumento
     * @return Flag indicando se o documento está assinado
     */
    public boolean isAssinado(ProcessoDocumento pd) {
        return processoDocumentoManager.checkAssinaturaProcessoDocumentoBin(pd.getProcessoDocumentoBin());
    }

    /**
     * @return the processoDocumentoList
     */
    public List<ProcessoDocumento> getProcessoDocumentoList() {
        return processoDocumentoList;
    }

    /**
     * @param processoDocumentoList the processoDocumentoList to set
     */
    public void setProcessoDocumentoList(List<ProcessoDocumento> processoDocumentoList) {
        this.processoDocumentoList = processoDocumentoList;
    }

    /**
     * @return the documentosSelecionadosMap
     */
    public Map<ProcessoDocumento, Boolean> getDocumentosSelecionadosMap() {
        return documentosSelecionadosMap;
    }

    /**
     * @param documentosSelecionadosMap the documentosSelecionadosMap to set
     */
    public void setDocumentosSelecionadosMap(Map<ProcessoDocumento, Boolean> documentosSelecionadosMap) {
        this.documentosSelecionadosMap = documentosSelecionadosMap;
    }

    /**
     * @return the processoDocumentoPublicadoMap
     */
    public Map<ProcessoDocumento, Boolean> getProcessoDocumentoPublicadoMap() {
        return processoDocumentoPublicadoMap;
    }

    /**
     * @param processoDocumentoPublicadoMap the processoDocumentoPublicadoMap to
     * set
     */
    public void setProcessoDocumentoPublicadoMap(Map<ProcessoDocumento, Boolean> processoDocumentoPublicadoMap) {
        this.processoDocumentoPublicadoMap = processoDocumentoPublicadoMap;
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

    /**
     * @return the processoDocumentoBinHome
     */
    public ProcessoDocumentoBinHome getProcessoDocumentoBinHome() {
        return processoDocumentoBinHome;
    }

    /**
     * @param processoDocumentoBinHome the processoDocumentoBinHome to set
     */
    public void setProcessoDocumentoBinHome(ProcessoDocumentoBinHome processoDocumentoBinHome) {
        this.processoDocumentoBinHome = processoDocumentoBinHome;
    }
    
    public boolean isParametroPessoaPublicaOkey() {
        return parametroPessoaPublicaOkey;
	}

    public void setParametroPessoaPublicaOkey(boolean parametroPessoaPublicaOkey) {
        this.parametroPessoaPublicaOkey = parametroPessoaPublicaOkey;
    }

    public Map<ProcessoDocumento, Boolean> getProcessoDocumentoJaEnviadoPublicacaoAnteriorMap() {
        return processoDocumentoJaEnviadoPublicacaoAnteriorMap;
    }

    public void setProcessoDocumentoJaEnviadoPublicacaoAnteriorMap(
        Map<ProcessoDocumento, Boolean> processoDocumentoJaEnviadoPublicacaoAnteriorMap) {
        this.processoDocumentoJaEnviadoPublicacaoAnteriorMap = processoDocumentoJaEnviadoPublicacaoAnteriorMap;
	}

    
}
