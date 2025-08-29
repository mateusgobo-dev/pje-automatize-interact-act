package br.jus.cnj.pje.view.fluxo;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.taskmgmt.exe.TaskInstance;

import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.service.AbstractAssinarExpedienteCriminalService;
import br.jus.cnj.certificado.SigningUtilities;
import br.jus.cnj.pje.business.dao.ProcessoExpedienteCriminalDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.EntityManagerUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteCriminalManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.MandadoPrisao;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaDocumentoIdentificacao;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCriminal;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

public abstract class PreparaExpedienteCriminalAction<E extends ProcessoExpedienteCriminal, M extends ProcessoExpedienteCriminalManager<E, ? extends ProcessoExpedienteCriminalDAO<E>>>
	implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8135377876673224350L;

	public static final String DEFAULT_TRANSITION = "frameDefaultLeavingTransition";
	public static final String TASK_OPERATION = "taskOperation";
	
	public static final String PREPARAR_OPERATION = "Preparar";
	public static final String ASSINAR_OPERATION = "Assinar";
	public static final String RETIFICAR_OPERATION = "Retificar";
	

	@In
	private ProcessoJudicialManager processoJudicialManager;

	@In
	private PessoaService pessoaService;

	@In
	private DocumentoJudicialService documentoJudicialService;

	@In(create = false, required = true)
	private ProcessInstance processInstance;

	@In(create = true)
	private TaskInstance taskInstance;

	@In
	private FacesMessages facesMessages;

	@In(create = true, required = false)
	private EntityManagerUtil entityManagerUtil;	

	@Logger
	private Log logger;

	private E processoExpedienteCriminalEdit;
	private E processoExpedienteCriminalView;
	private ProcessoTrf processoJudicial;
	private String cpfPessoaPesquisa;
	private String nomePessoaPesquisa;
	private int passo;
	private int maxPasso;
	private String encodedCertChain;
	//private String assinaturas;
	private ArrayList<ParAssinatura> assinaturas;
	private String transicaoSaida;	
	private ModeloDocumento modelo;
	private List<Pessoa> pessoasCandidatas = new ArrayList<Pessoa>(0);
	private List<E> expedientesCadastrados = new ArrayList<E>(0);
	private List<MandadoPrisao> mandados = new ArrayList<MandadoPrisao>(0);
	private List<Pessoa> pessoasAdicionadas = new ArrayList<Pessoa>(0);

	public abstract M getManager();
	
	public abstract AbstractAssinarExpedienteCriminalService<E> getAssinarExpedienteCriminalService();
	
	public abstract void buscarDemaisMandados();	

	@Create
	public void init(){
		passo = 0;		
		transicaoSaida = (String) br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil.instance().getVariable(DEFAULT_TRANSITION);
		processoJudicial = this.loadProcessoJudicial();		
		//this.pesquisarPartesCandidatas();
	}

	/**
	 * Carrega o processo judicial.
	 * 
	 * @return o processo judicial vinculado a esta atividade.
	 */
	protected ProcessoTrf loadProcessoJudicial(){
		try{
			return this.processoJudicialManager.findByProcessInstance(processInstance);
		} catch (PJeBusinessException e){
			facesMessages.addFromResourceBundle(Severity.ERROR, "pje.preparaExpedienteCriminalAction.error.erroObterProcesso");
		} catch (PJeDAOException e){
			facesMessages.addFromResourceBundle(Severity.ERROR, "pje.preparaExpedienteCriminalAction.error.erroObterProcesso");
		}
		return null;
	}

	public void pesquisarPartesCandidatas(){
		try{
			pessoasCandidatas = pessoaService.pesquisarPessoasSemMandados(processoJudicial.getIdProcessoTrf(), getNomePessoaPesquisa(),
					getCpfPessoaPesquisa());
			for (E aux : expedientesCadastrados){
				if (pessoasCandidatas != null){
					if (pessoasCandidatas.contains(aux.getPessoa())){
						pessoasCandidatas.remove(aux.getPessoa());
					}
				}
			}
		} catch (PJeBusinessException e){
			e.printStackTrace();
		}

	}

	private E getExpedienteAdicionado(PessoaFisica pessoa){
		for (E aux : getExpedientesCadastrados()){
			if (aux.getPessoa().equals(pessoa)){
				return aux;
			}
		}

		return null;
	}

	public void editarProcessoExpedienteCriminal(PessoaFisica pessoa){
		E expedienteAux = getExpedienteAdicionado(pessoa);

		if (expedienteAux == null){
			E peCriminal = null;
			peCriminal = getManager().getExpediente(processoJudicial);
			peCriminal.setPessoa(pessoa);
			peCriminal.setProcessoTrf(processoJudicial);
			peCriminal.setInSigiloso(false);
			processoExpedienteCriminalEdit = peCriminal;
		}
		else{
			expedientesCadastrados.remove(expedienteAux);
			processoExpedienteCriminalEdit = expedienteAux;
		}
		
		passo = 0;
	}

	public void editarProcessoExpedienteCriminal(E expediente){
		processoExpedienteCriminalEdit = expediente;		
		setPasso(0);
	}

	public void copiarExpediente(){
		try{
			getManager().copiarExpediente(getProcessoExpedienteCriminalEdit(), getExpedientesCadastrados());
		} catch (PJeBusinessException e){
			getFacesMessages().add(Severity.ERROR, e.getMessage());
		}
	}

	public E getProcessoExpedienteCriminalEdit(){
		return processoExpedienteCriminalEdit;
	}

	public void setProcessoExpedienteCriminalEdit(E processoExpedienteCriminalEdit){
		this.processoExpedienteCriminalEdit = processoExpedienteCriminalEdit;
	}
	
	public E getProcessoExpedienteCriminalView() {
		return processoExpedienteCriminalView;
	}
	
	public void setProcessoExpedienteCriminalView(
			E processoExpedienteCriminalView) {
		this.processoExpedienteCriminalView = processoExpedienteCriminalView;
	}

	public void setProcessInstance(ProcessInstance processInstance){
		this.processInstance = processInstance;
	}

	public ProcessoTrf getProcessoJudicial(){
		return processoJudicial;
	}

	public void setProcessoJudicial(ProcessoTrf processoJudicial){
		this.processoJudicial = processoJudicial;
	}

	public String getCpfPessoaPesquisa(){
		return cpfPessoaPesquisa;
	}

	public void setCpfPessoaPesquisa(String cpfPessoaPesquisa){
		if(cpfPessoaPesquisa != null && cpfPessoaPesquisa.equals("___.___.___-__")){
			cpfPessoaPesquisa = "";
		}
		this.cpfPessoaPesquisa = cpfPessoaPesquisa;
	}

	public String getNomePessoaPesquisa(){
		return nomePessoaPesquisa;
	}

	public void setNomePessoaPesquisa(String nomePessoaPesquisa){
		this.nomePessoaPesquisa = nomePessoaPesquisa;
	}

	public List<Pessoa> getPessoasCandidatas(){
		return pessoasCandidatas;
	}

	public List<E> getExpedientesCadastrados(){
		return expedientesCadastrados;
	}

	public void setExpedientesCadastrados(List<E> expedientesCadastrados){
		this.expedientesCadastrados = expedientesCadastrados;
	}

	public int getPasso(){
		return passo;
	}
	
	public void setPasso(int passo) {
		this.passo = passo;
		//se não estiver preparando o expediente
		if(getPasso() == getMaxPasso()){
			atualizarTextoExpediente();
		}
	}

	public String getEncodedCertChain(){
		return encodedCertChain;
	}

	public void setEncodedCertChain(String encodedCertChain){
		this.encodedCertChain = encodedCertChain;
	}
	
	public List<ParAssinatura> getAssinaturas(){
		if(assinaturas == null){
			assinaturas = new ArrayList<ParAssinatura>();
			Set<ProcessoDocumentoBin> docs = new HashSet<ProcessoDocumentoBin>();
			for(ProcessoExpedienteCriminal expediente : getExpedientesCadastrados()){
				ProcessoDocumentoBin doc = expediente.getProcessoDocumento().getProcessoDocumentoBin();
				if(!docs.contains(doc) && !doc.isBinario()){
					docs.add(doc);
					ParAssinatura pa = new ParAssinatura();
					String contents;
					try {
						contents = new String(SigningUtilities.base64Encode(doc.getModeloDocumento().getBytes()));
						pa.setConteudo(contents);
						assinaturas.add(pa);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return assinaturas;
	}	

/*	public String getAssinaturas(){
		return assinaturas;
	}

	public void setAssinaturas(String assinaturas){
		this.assinaturas = assinaturas;
	}*/

	public String getTransicaoSaida(){
		return transicaoSaida;
	}

	public void setTransicaoSaida(String transicaoSaida){
		this.transicaoSaida = transicaoSaida;
	}

	public boolean isEditavel(){
		return (getProcessoExpedienteCriminalEdit() != null);
	}

	public void proximoPasso(){
		if (passo < maxPasso){
			setPasso(passo + 1);
		}
	}

	public void voltar(){
		if (passo > 0){
			setPasso(passo - 1);
		}
	}

	/*
	 * armazena na memória para serem gravados posteriormente
	 */
	public void armazenarExpediente(){
		/*
		 * se já foi adicionado à lista
		 * remove p/ ser readicionado com
		 * aquilo de foi alterado
		 */
		if(expedientesCadastrados.contains(processoExpedienteCriminalEdit)){
			expedientesCadastrados.remove(processoExpedienteCriminalEdit);
		}
		expedientesCadastrados.add(processoExpedienteCriminalEdit);
		Collections.sort(expedientesCadastrados);
		
		popularPessoas();
		processoExpedienteCriminalEdit = null;		
		setModelo(null);
		setPasso(0);
	}

	public String documentosParaAssinatura(){
		return getManager().documentosParaAssinatura(getExpedientesCadastrados());
	}

	public void gravarExpedientes(){
		try{
			for (E aux : expedientesCadastrados){
				/*
				 * Se retificando, seta a situação para null,
				 * para q a camada de negócio possa setar
				 * como PA (Pendente de assinatura)
				 */
				if(isRetificando()){
					aux.setSituacaoExpedienteCriminal(null);
				}				
				getManager().persist(aux, processoJudicial, taskInstance.getId());
			}

			/*
			 * se tiver transição de saída default,
			 * envia para a próxima tarefa
			 */
			if (transicaoSaida != null && !transicaoSaida.trim().isEmpty()){
				getTaskInstanceHome().end(transicaoSaida);
			}
		} catch (PJeBusinessException e){			
			facesMessages.addFromResourceBundle(Severity.ERROR, e.getCode(), e.getParams());			
		} catch (PJeDAOException e){
			facesMessages.addFromResourceBundle(Severity.ERROR, "pje.default.error.msg", e);			
		} catch (CertificadoException e){
			facesMessages.addFromResourceBundle(Severity.ERROR, "pje.default.error.msg", e);
		}
	}

	/**
	 * Finaliza os múltiplos expedientes pendentes, acrescentando as assinaturas necessárias. Se bem sucedido, será invocada a transição padrão
	 * existente definida na variável "frameDefaultLeavingTransition".
	 * 
	 */
	public void finalizarMultiplos(){
//		String[] signs = Signer.getEncodedSignatures(assinaturas);
		//String[] signs = new String[]{};
		List<String> signs = new ArrayList<String>(assinaturas.size());
		for(ParAssinatura par: assinaturas){
			signs.add(par.assinatura);
		}		
		if (expedientesCadastrados.size() != signs.size()){
			facesMessages.addFromResourceBundle(Severity.ERROR,
							"pje.atoComunicacaoService.error.numeroAssinaturasDivergente",
							expedientesCadastrados.size(), signs.size());
			return;
		}

		try{			
			for (int i = 0; i < expedientesCadastrados.size(); i++){
					E aux = expedientesCadastrados.get(i);
					aux = getManager().persist(aux, aux.getProcessoTrf(), taskInstance.getId());
					aux = getAssinarExpedienteCriminalService().assinarExpedienteCriminal(aux, signs.get(i),
							encodedCertChain, taskInstance.getId());
			}
			
			setExpedientesCadastrados(null);
			setProcessoExpedienteCriminalEdit(null);
			entityManagerUtil.flush();
			facesMessages.addFromResourceBundle(Severity.INFO,"pje.info.preparaExpedienteCriminalAction.sucesso");
			if (transicaoSaida != null && !transicaoSaida.isEmpty() && getTaskInstanceHome() != null){
				getTaskInstanceHome().end(transicaoSaida);
			}else{
				pesquisarExpedientesNaoAssinados();
			}
		} catch (PJeBusinessException e){
			facesMessages.addFromResourceBundle(Severity.ERROR, e.getCode(), e.getParams());			
		} catch (CertificadoException e){
			facesMessages.addFromResourceBundle(Severity.ERROR, "pje.default.error.msg", "Erro ao assinar expediente");
		} catch(Exception e){
			facesMessages.addFromResourceBundle(Severity.ERROR, "pje.default.error.msg", "Erro ao tentar gravar o expediente");
		}
	}

	public List<ModeloDocumento> getModelosDisponiveis(){
		try{
			return this.documentoJudicialService.getModelosDisponiveis();
		} catch (Exception e){
			getFacesMessages().add(Severity.ERROR, "pje.preparaExpedienteCriminalAction.error.erroObterModelos");
		}
		return null;
	}

	// usado no nó de assinatura
	protected void pesquisarExpedientesNaoAssinados(){
		expedientesCadastrados = getManager().recuperarExpedientesNaoAssinados(getProcessoJudicial());
	}

	public void cancelar(){
		processoExpedienteCriminalEdit = null;
	}

	public FacesMessages getFacesMessages(){
		return facesMessages;
	}

	public ProcessInstance getProcessInstance(){
		return processInstance;
	}

	public TaskInstance getTaskInstance(){
		return taskInstance;
	}

	public Log getLogger(){
		return logger;
	}

	public Boolean getFinalizar(){
		return (passo == maxPasso);
	}

	public DocumentoJudicialService getDocumentoJudicialService(){
		return documentoJudicialService;
	}

	public void setDocumentoJudicialService(DocumentoJudicialService documentoJudicialService){
		this.documentoJudicialService = documentoJudicialService;
	}

	public ModeloDocumento getModelo(){
		return modelo;
	}

	public void setModelo(ModeloDocumento modelo){
		this.modelo = modelo;
	}

	public List<MandadoPrisao> getMandados(){
		return mandados;
	}

	public void setMandados(List<MandadoPrisao> mandados){
		this.mandados = mandados;
	}

	public void setMaxPasso(int maxPasso){
		this.maxPasso = maxPasso;
	}

	public int getMaxPasso(){
		return maxPasso;
	}

	public TaskInstanceHome getTaskInstanceHome(){
		return TaskInstanceHome.instance();
	}

	public void popularPessoas(){
		for (E aux : expedientesCadastrados){
			if (!pessoasAdicionadas.contains(aux.getPessoa())){
				pessoasAdicionadas.add(aux.getPessoa());
			}
		}
	}

	public List<Pessoa> getPessoasAdicionadas(){
		return pessoasAdicionadas;
	}

	public void setPessoasAdicionadas(List<Pessoa> pessoasAdicionadas){
		this.pessoasAdicionadas = pessoasAdicionadas;
	}
	
	public String getTextoDocumentosPessoais(){
		StringBuilder texto = new StringBuilder();
		String retorno = "";
		if (getProcessoExpedienteCriminalEdit() != null
			&& processoExpedienteCriminalEdit.getPessoa() != null
			&& processoExpedienteCriminalEdit.getPessoa().getPessoaDocumentoIdentificacaoList() != null){
			for (PessoaDocumentoIdentificacao aux : processoExpedienteCriminalEdit.getPessoa().getPessoaDocumentoIdentificacaoList()){
				texto.append(aux.getTipoDocumento());
				texto.append(" - ");
				texto.append(aux.getNumeroDocumento());
				texto.append(" - ");
				texto.append(aux.getOrgaoExpedidor());
				texto.append("\n");
			}
			retorno = texto.substring(0, texto.lastIndexOf("\n") );
		}
		return retorno;
	}

	public String getTextoEnderecos(){
		StringBuilder texto = new StringBuilder();
		String retorno = "";
		if (getProcessoExpedienteCriminalEdit() != null
			&& processoExpedienteCriminalEdit.getPessoa() != null
			&& processoExpedienteCriminalEdit.getPessoa().getEnderecoList() != null){
			for (Endereco aux : processoExpedienteCriminalEdit.getPessoa().getEnderecoList()){
				texto.append(aux.getEnderecoCompleto());
				texto.append("\n");
			}
			retorno = texto.substring(0, texto.lastIndexOf("\n"));
		}
		return retorno;
	}
	
	public void atualizarTextoExpediente(){
		recuperarModeloDocumento();
		substituirModelo();
	}
	
	public void substituirModelo(){
		if(getModelo() != null){		
			if(!isExisteVariavelNumero()){
				getFacesMessages().addFromResourceBundle(Severity.ERROR,"pje.preparaExpedienteCriminalAction.error.variavelNumeroInexistente",getModelo());
				setModelo(null);
				return;
			}

			if(!isExisteVariavelNomeMagistrado()){
				getFacesMessages().addFromResourceBundle(Severity.ERROR,"pje.preparaExpedienteCriminalAction.error.variavelNomeMagistradoInexistente",getModelo());
				setModelo(null);
				return;
			}
			
			String texto = documentoJudicialService.processaConteudo(getModelo());
			getProcessoExpedienteCriminalEdit().getProcessoDocumento().getProcessoDocumentoBin().setModeloDocumento(texto);
			getManager().substituirMarcacaoNumeroExpediente(getProcessoExpedienteCriminalEdit());
		}
	}
	
	private String getTaskOperation(){
		String task = (String) br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil.instance().getVariable(TASK_OPERATION);
		if(task != null && !task.trim().equals("")){
			return task; 
		}else{
			getFacesMessages().add(Severity.ERROR, null, "pje.preparaExpedienteCriminalAction.error.modeloDocumentoInexistente");
			return null;
		}		
	}
	
	protected Boolean isPreparando(){
		String operation = getTaskOperation();
		return (operation != null && operation.trim().toUpperCase().equals(PREPARAR_OPERATION.toUpperCase()));
	}
	
	protected Boolean isAssinando(){
		String operation = getTaskOperation();
		return (operation != null && operation.trim().toUpperCase().equals(ASSINAR_OPERATION.toUpperCase()));
	}
	
	protected Boolean isRetificando(){
		String operation = getTaskOperation();
		return (operation != null && operation.trim().toUpperCase().equals(RETIFICAR_OPERATION.toUpperCase()));
	}
	
	public void recuperarModeloDocumento(){
		List<ModeloDocumento> modelos = null;
		try {
			modelos = documentoJudicialService.getModelosDisponiveis();
		} catch (Exception e) {
			getFacesMessages().add(Severity.ERROR,
					"pje.preparaExpedienteCriminalAction.error.erroObterModelos");
			return;
		}
		
		if(modelos == null || modelos.isEmpty()){
			getFacesMessages().add(Severity.ERROR,
					"Não foi possível atualizar o texto do expediente.\n" +
					"Não existe modelo de documento cadastrado para a tarefa");
			return;
		}else if(modelos.size() == 1){
			setModelo(modelos.get(0));
			
			if(!isExisteVariavelNumero()){
				getFacesMessages().addFromResourceBundle(Severity.ERROR,"pje.preparaExpedienteCriminalAction.error.variavelNumeroInexistente",getModelo());
				return;
			}

			if(!isExisteVariavelNomeMagistrado()){
				getFacesMessages().addFromResourceBundle(Severity.ERROR,"pje.preparaExpedienteCriminalAction.error.variavelNomeMagistradoInexistente",getModelo());
				return;
			}			
		}else{
			setModelo(null);
		}
	}
	
	public Boolean isPodeAssinar(){
		return (getExpedientesCadastrados() != null && !getExpedientesCadastrados().isEmpty() && Authenticator.isMagistrado());
	}
	
	public Boolean isExisteVariavelNumero(){
		if(getModelo() != null){
			return getModelo().getModeloDocumento().contains(ProcessoExpedienteCriminalManager.MARCACAO_NUMERO_EXPEDIENTE_REPLACE);
		}
		
		return false;
	}
	
	public Boolean isExisteVariavelNomeMagistrado(){
		if(getModelo() != null){
			return getModelo().getModeloDocumento().contains(ProcessoExpedienteCriminalManager.MARCACAO_MAGISTRADO_REPLACE);
		}
		
		return false;
	}
	
	public void removerExpediente(PessoaFisica pessoaFisica){
		for(E expediente : getExpedientesCadastrados()){
			if(expediente.getPessoa().equals(pessoaFisica)){
				getExpedientesCadastrados().remove(expediente);
				pessoasAdicionadas.remove(pessoaFisica);
				processoExpedienteCriminalEdit = null;
				setPasso(0);
				return;
			}
		}
	}
	
	public void cancelarExpediente(E expediente){
		try {
			getManager().cancelarExpediente(expediente);
			getManager().flush();
			getExpedientesCadastrados().remove(expediente);
			pessoasAdicionadas.remove(expediente.getPessoa());
			if(expediente.equals(processoExpedienteCriminalEdit)){
				processoExpedienteCriminalEdit = null;
			}
		} catch (PJeBusinessException e) {
			getFacesMessages().addFromResourceBundle(Severity.ERROR, e.getCode(), e.getParams());
		}
	}
	
	public void verExpediente(PessoaFisica pessoa){
		E expedienteAux = getExpedienteAdicionado(pessoa);
		 verExpediente(expedienteAux);
	}
	
	public void verExpediente(E expediente){
		processoExpedienteCriminalView = expediente;
	}
	
	public String getTextoExpediente(){
		if(getProcessoExpedienteCriminalView() != null){
			return getProcessoExpedienteCriminalView().getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento();
		}
		
		return null;
	}
	
	public class ParAssinatura{
		private String conteudo;
		private String assinatura;
		public String getConteudo() {
			return conteudo;
		}
		public void setConteudo(String conteudo) {
			this.conteudo = conteudo;
		}
		public String getAssinatura() {
			return assinatura;
		}
		public void setAssinatura(String assinatura) {
			this.assinatura = assinatura;
		}
	}
}
