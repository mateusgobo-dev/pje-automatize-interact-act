package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;
import org.json.JSONException;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.core.certificado.CertificadoException;
import br.com.infox.ibpm.component.tree.EventsEditorTreeHandler;
import br.com.infox.ibpm.component.tree.EventsHomologarMovimentosTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.PessoaFisicaManager;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ControleVersaoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ModeloDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.OrgaoJulgadorService;
import br.jus.cnj.pje.nucleo.service.TipoProcessoDocumentoPapelService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.nucleo.service.UsuarioService;
import br.jus.cnj.pje.nucleo.view.CkEditorGeraDocumentoAbstractAction;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;


@Name(EditorTextoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class EditorTextoAction extends CkEditorGeraDocumentoAbstractAction implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = 6758707644654226885L;

	public static final String NAME = "editorTextoAction";

	@In(create = false, required = true)
	private FacesMessages facesMessages;

	@In(create = true, required = true)
	private TaskInstanceUtil taskInstanceUtil;

	@In(create = false, required = true)
	private TaskInstanceHome taskInstanceHome;

	@In(create = true)
	private ProcessoDocumentoBinManager processoDocumentoBinManager;

	@In(create = true)
	private ModeloDocumentoManager modeloDocumentoManager;

	@In(create = true, required = true)
	private transient ProcessoJudicialManager processoJudicialManager;

	@In(create = true)
	private transient DocumentoJudicialService documentoJudicialService;

	@In(create = true)
	private transient UsuarioService usuarioService;
	
	@In(create = true)
	private transient OrgaoJulgadorService orgaoJulgadorService;
	
	@In(create = true)
	private transient PessoaFisicaManager pessoaFisicaManager; 

    @In
    private TramitacaoProcessualService tramitacaoProcessualService;

    @In
    private br.jus.csjt.pje.view.action.TipoProcessoDocumentoAction tipoDocumento;

    @In
	private ProcessInstance processInstance;

    @In(required = false)
    private EventsHomologarMovimentosTreeHandler eventsHomologarMovimentosTree;

    @In
    private TipoProcessoDocumentoPapelService tipoProcessoDocumentoPapelService;
    
    @Logger
	private Log logger;

	private List<ProcessoDocumentoBinPessoaAssinatura> listaAssinatura;
 	
	private boolean assinado;
	private boolean renderEventsTree;
 	private boolean perguntaTransitar;
	private boolean warning;
	private boolean alert;
	private boolean obrigatorio;
	private Integer idMinuta;
	private String limparDocumento;
	private String transicaoSaida;
	private Integer idAgrupamentos;
	private boolean uploadArquivoAssinadoRealizado;
	private boolean conteudoAlterado = false;
	
	private String actionInstanceId;

	@In(create = true)
	private ControleVersaoDocumentoManager controleVersaoDocumentoManager;
	
	@Create
	public void load() throws Exception {
		this.geraActionInstanceId();

		setProtocolarDocumentoBean(new ProtocolarDocumentoBean(taskInstanceUtil.getProcesso(
				taskInstanceUtil.getProcessInstance().getId()).getIdProcessoTrf(),
				ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL | ProtocolarDocumentoBean.RECUPERA_DOCUMENTO_FLUXO,NAME));


		transicaoSaida = (String)taskInstanceUtil.getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);

		Boolean obrigatorio = (Boolean) tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.VARIAVEL_OBRIGATORIO);
		
		setObrigatorio(obrigatorio != null ? obrigatorio : false);

		limparDocumento = (String) tramitacaoProcessualService.recuperaVariavelTarefa(Variaveis.VARIAVEL_LIMPAR_DOCUMENTO_FRAME);
		if (limparDocumento == null)
		{
			limparDocumento = "";
		}
		
		carregarDocumentoPrincipalPorVariavelFluxoOuCriarNovo(true);
		
		listaAssinatura = processoDocumentoBinManager.obtemAssinaturas(getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin());
		limparArquivosAssinadosAnteriormente();
		
		this.setConteudoAlterado(false);
		this.validarAction();
	}

	/**
	 * Recarrega o documento principal a partir da variavel de fluxo
	 * Variaveis.MINUTA_EM_ELABORACAO, caso a variavel esteja
	 * vazia, determina como documento principal um novo documento vazio, marcado como ativo e não
	 * sigiloso.
	 * 
	 * @throws PJeBusinessException
	 */
	private void carregarDocumentoPrincipalPorVariavelFluxoOuCriarNovo(Boolean atualizaAtributos) throws PJeBusinessException {
		idMinuta = (Integer) tramitacaoProcessualService.recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO);
		ProcessoDocumento documentoPrincipal = documentoJudicialService.getDocumento(idMinuta);
		
		if(documentoPrincipal != null && documentoPrincipal.getIdProcessoDocumento() > 0){
			getProtocolarDocumentoBean().setDocumentoPrincipal(documentoPrincipal);
			getProtocolarDocumentoBean().loadArquivosAnexadosDocumentoPrincipal();
			assinado = getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getSignatarios().size() > 0;
			if(atualizaAtributos) {
				setTipoProcessoDocumento(getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento());
				onSelectProcessoDocumentoEditorAssinatura(getTipoProcessoDocumento());
			}
		}else{
			getProtocolarDocumentoBean().setDocumentoPrincipal(documentoJudicialService.getDocumento());
		}
		Long jbpmTask = null;
		if (TaskInstance.instance() != null) {
    		jbpmTask = TaskInstance.instance().getId();
        }
		getProtocolarDocumentoBean().getDocumentoPrincipal().setIdJbpmTask(jbpmTask);
    	getProtocolarDocumentoBean().getDocumentoPrincipal().setExclusivoAtividadeEspecifica(Boolean.TRUE);
	}

	@Override
	public boolean podeAssinar() {
		boolean retorno = false;
		if( isTipoProcessoDocumentoDefinido() && isDocumentoPersistido() && !isDocumentoVazio()) {
			retorno = isApresentaBotaoAssinar();
		}
		return retorno;
	}	
	
	/**
 	 * Metodo que verifica se é obrigatória a assinatura pelo tipo processo documento
 	 * e papel do usuário logado
 	 * @return boolean
 	 */
 	public boolean isApresentaBotaoAssinar(){
 		return tipoProcessoDocumentoPapelService.verificarExigibilidadeAssina(
 				Authenticator.getPapelAtual(),
 				getTipoProcessoDocumento());
	}

	public String getModeloDocumento() {
		if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null &&
				getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin() != null) {

			return getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().getModeloDocumento();
		}
		return null;
	}

	public void onSelectProcessoDocumentoEditorAssinatura(TipoProcessoDocumento tipoProcessoDocumento)
	{
		this.setTipoProcessoDocumento(tipoProcessoDocumento);
		defineEstadoComponenteArvoreMovimentacoesProcessuais(tipoProcessoDocumento);
		this.validarAction();
	}

	/**
	 * Define o estado do componente de arvore de movimentações processuais.
	 *
	 * @param tipoProcessoDocumento
	 */
	private void defineEstadoComponenteArvoreMovimentacoesProcessuais(TipoProcessoDocumento tipoProcessoDocumento) {
		reiniciaComponenteArvoreMovimentacoesProcessuais();

		if (tipoProcessoDocumento != null && tipoProcessoDocumento.getAgrupamento() != null){

			idAgrupamentos = tipoProcessoDocumento.getAgrupamento().getIdAgrupamento();

			if (idAgrupamentos != null && idAgrupamentos > 0){
				setRenderEventsTree(true);
				EventsEditorTreeHandler.instance().setRootsSelectedMap(new HashMap<Evento, List<Evento>>());
				EventsEditorTreeHandler.instance().getRoots(idAgrupamentos);
			}
		}
	}

	/**
	 * Reinicia o estado do componente de arvore de movimentações processuais, esvaziando sua listagem e não permitindo a sua renderização.
	 */
	private void reiniciaComponenteArvoreMovimentacoesProcessuais() {
		EventsEditorTreeHandler.instance().clearList();
		EventsEditorTreeHandler.instance().clearTree();
		setRenderEventsTree(false);
	}

	/**
	 * Metodo que define o tipo de processo documento por sua descrição.
	 * @param tipoDocumentoString
	 */
	@Override
	public void selecionarTipoProcessoDocumento(String tipoDocumentoString){
		super.selecionarTipoProcessoDocumento(tipoDocumentoString);
		defineEstadoComponenteArvoreMovimentacoesProcessuais(getTipoProcessoDocumento());
	}

	public void assinarDocumento() throws PJeBusinessException {
		if (!verificaCamposPreenchidos(true)) {
			return;
		}
		atualizaDocumentoPrincipal();
		finalizarDocumento();
 	}

	/**
	 * Atualiza o documento principal, recuperando-o do banco de dados com o id gravado na variavel de tarefa "minutaEmElaboracao"
	 *
	 * @throws PJeBusinessException
	 */
	public void atualizaDocumentoPrincipal() throws PJeBusinessException {
		idMinuta = (Integer)tramitacaoProcessualService.recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO);

		if (idMinuta != null && idMinuta != 0) {
			getProtocolarDocumentoBean().setDocumentoPrincipal(documentoJudicialService.getDocumento(idMinuta));
		}
	}

	public boolean verificaCamposPreenchidos(boolean mensagem) {
 		warning = false;
		if (isDocumentoVazio() || !isTipoProcessoDocumentoDefinido()) {
			warning = true;
			if (mensagem) {
				facesMessages.clear();
				facesMessages.addFromResourceBundle(Severity.ERROR, "editorTexto.erro.salvarDocumentoVazio");
			}
			return false;
		}
		warning = false;
		return true;
	}

	/**
	 * Verifica se o modelo de documento está nulo ou vazio.
	 *
	 * @return true se vazio ou nulo, false caso contrário.
	 */
	public boolean isDocumentoVazio(){
		boolean result = Boolean.FALSE;

		if(getModeloDocumento() == null || "".equals(getModeloDocumento().trim())){
			result = Boolean.TRUE;
		}

		return result;
	}
	
	public boolean isConteudoAlterado() {
		return conteudoAlterado;
	}

	public void setConteudoAlterado(boolean conteudoAlterado) {
		this.conteudoAlterado = conteudoAlterado;
		this.validarAction();
	}

	public void processarModeloDocumento() {
		ProcessoDocumentoHome.instance().processarModeloDocumento();
		if (ProcessoHome.instance().getProcessoDocumentoBin() == null) {
			return ;
		}
		getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().setModeloDocumento(
				ProcessoHome.instance().getProcessoDocumentoBin().getModeloDocumento());
	}

	public void naoProcessarModelo()
	{
		if (warning)
		{
			ProcessoDocumentoHome.instance().setModeloDocumentoLocalTemp(null);
 		}
	}

	public boolean verificaObrigatorio()
	{
 		warning = false;
		if ( !verificaCamposPreenchidos(false) && obrigatorio)
		{
			warning = true;
			facesMessages.clear();
			facesMessages.add(Severity.ERROR, "Esta tarefa exige que seja redigido um documento.");
			return false;
		}
		return true;
	}

	public void dispararMudarModelo()
	{
		warning = false;
		if (ProcessoDocumentoHome.instance().getModeloDocumentoLocalTemp() != null && getModeloDocumento() != null)
		{
			warning = true;
		}
	}

	public void disparaFinalizar()
	{
		if (!verificaObrigatorio())
		{
			return;
		}
 		if (verificaCamposPreenchidos(false))
		{
			if (!assinado)
			{
				salvar();
			}
  		}
 	}

	/**
	 * Salva o documento principal, associando os arquivos que foram adicionados por upload.
	 *
	 */
	public void salvar() {
		if (!verificaCamposPreenchidos(true)){
			return;
		}
		try{
			getProtocolarDocumentoBean().getDocumentoPrincipal().setProcessoDocumento(getTipoProcessoDocumento().getTipoProcessoDocumento());
			getProtocolarDocumentoBean().getDocumentoPrincipal().setTipoProcessoDocumento(getTipoProcessoDocumento());

			if(getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento() == 0){
				getProtocolarDocumentoBean().loadArquivosAnexadosDocumentoPrincipal();
			}

			getProtocolarDocumentoBean().gravarRascunho();
			this.setConteudoAlterado(false);

			idMinuta = getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento();
			tramitacaoProcessualService.gravaVariavel(Variaveis.MINUTA_EM_ELABORACAO, idMinuta);
			this.validarAction();

		}catch (Exception e){
			facesMessages.add(Severity.ERROR, "Não foi possível gravar o documento [{0}:{1}]", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
	}

	public void salvar(String conteudo) {
		try {
			refreshProcessoDocumentoBin();
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR,"Erro ao recuperar o conteúdo do documento");
		}
		getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin().setModeloDocumento(conteudo);
		this.salvar();
	}
	
	private ProcessoDocumentoBin refreshProcessoDocumentoBin() throws PJeBusinessException {
		if(getProtocolarDocumentoBean().getDocumentoPrincipal() != null && getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento() != 0) {
			return processoDocumentoBinManager.refresh(getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin());
		}
		return null;
	}


	public void finalizarDocumento() {
		try {
			Boolean geraAtoProferido = getProtocolarDocumentoBean().getDocumentoPrincipal().getTipoProcessoDocumento().getDocumentoAtoProferido();
			this.getProtocolarDocumentoBean().concluirAssinaturaAction();
			this.validarAction();

			if (!isDocumentoAssinado()){
				facesMessages.add(Severity.INFO, "O documento não foi assinado!");
				return;
			}
			if (idMinuta == null) {
				tramitacaoProcessualService.gravaVariavel(Variaveis.MINUTA_EM_ELABORACAO,
						getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento());
			}
			if (this.transicaoSaida != null) {
				finalizar(geraAtoProferido);
			}

		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível finalizar o documento. {0}: {1}.",
					e.getClass().getCanonicalName(), e.getLocalizedMessage());

		} catch (PJeDAOException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível finalizar o documento. {0}: {1}.",
					e.getClass().getCanonicalName(), e.getLocalizedMessage());

		} catch (CertificadoException e){
			facesMessages.add(Severity.ERROR, "Houve uma inconsistência na verificação da assinatura. {0}: {1}.",
					e.getClass().getCanonicalName(), e.getLocalizedMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void removerAssinatura() {
		try {
			alert=ProcessoDocumentoHome.instance().verificarDocumentoEventoRelacionado(getProtocolarDocumentoBean().getDocumentoPrincipal());
			if (alert){
				return ;
			}
			if(ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(getProtocolarDocumentoBean().getDocumentoPrincipal())){
				getProtocolarDocumentoBean().getDocumentoPrincipal().setDataJuntada(null);
				ComponentUtil.getAssinaturaDocumentoService().removeAssinatura(getProtocolarDocumentoBean().getDocumentoPrincipal());
			}

			if(getProtocolarDocumentoBean().getArquivos() != null && !getProtocolarDocumentoBean().getArquivos().isEmpty()) {
				for (ProcessoDocumento arquivo : getProtocolarDocumentoBean().getArquivos()) {
					if(ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(arquivo)){
						arquivo.setDataJuntada(null);
						ComponentUtil.getAssinaturaDocumentoService().removeAssinatura(arquivo);
					}
				}
			}

			assinado = false;

			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.INFO, "Assinatura removida com sucesso.");
		} catch (Exception e) {
 			FacesMessages.instance().add(Severity.ERROR, "Erro ao remover a assinatura.");
		}
		listaAssinatura = processoDocumentoBinManager.obtemAssinaturas(getProtocolarDocumentoBean().getDocumentoPrincipal().getProcessoDocumentoBin());
		this.validarAction();
	}

	public void finalizar(Boolean geraAtoProferido)	{
		LancadorMovimentosService.instance().lancarMovimentosTemporarios(taskInstanceUtil.getProcessInstance());

		if(geraAtoProferido == null || geraAtoProferido) {
			tramitacaoProcessualService.gravaVariavel(Variaveis.ATO_PROFERIDO, tramitacaoProcessualService.recuperaVariavel(
					Variaveis.MINUTA_EM_ELABORACAO));
		}

		tramitacaoProcessualService.gravaVariavel(Variaveis.ULTIMO_DOCUMENTO_JUNTADO_NESTE_FLUXO, 
				tramitacaoProcessualService.recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO));
		tramitacaoProcessualService.apagaVariavel(Variaveis.MINUTA_EM_ELABORACAO);

		taskInstanceHome.saidaDireta(transicaoSaida);
		Contexts.getBusinessProcessContext().flush();
		perguntaTransitar = false;
		limparArquivosAssinadosAnteriormente();
	}

	private void limparArquivosAssinadosAnteriormente() {
		getProtocolarDocumentoBean().getArquivosAssinados().clear();
		uploadArquivoAssinadoRealizado = Boolean.FALSE;
	}

	/**
	 * Verifica se todas as condições para liberação do componente de assinatura estão satisfeitas para sua visualização
	 *
	 * Condições:
	 * 	-Documento principal deve existir na base de dados
	 *  -movimentação deve estar informada
	 *  -perfil de usuário autorizado para o tipo de documento
	 *  -documento principal não foi assinado e/ou arquivos de upload acrescentados não foram assinados
	 *
	 * @return true para habilitar o componente de assinatura e false caso contrario
	 * @throws PJeBusinessException
	 */
	public boolean isHabilitaAssinar() throws PJeBusinessException {
		if (!(getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento() > 0)) {
			return Boolean.FALSE;
		} else if (precisaTerMovimentacao()) {
			if(movimentacaoIncompleta()){
				return Boolean.FALSE;
			}
			if(!EventsEditorTreeHandler.instance().validarMovimentacao()){
				return Boolean.FALSE;
			}
		}else if(!liberaCertificacao()){
			return Boolean.FALSE;
		}else if(assinado){
			for (ProcessoDocumento processoDocumento : getProtocolarDocumentoBean().getArquivos()) {
				if(!documentoJudicialService.temAssinatura(processoDocumento)){
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public boolean isDocumentoPersistido(){
		if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null && getProtocolarDocumentoBean().getDocumentoPrincipal().getIdProcessoDocumento() > 0) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public boolean isHabilitaProximaTarefa() throws PJeBusinessException
	{

		boolean ret = ((!assinado && !liberaCertificacao() ))  ;

		return ret;
	}

	public boolean movimentacaoIncompleta()
	{
		return precisaTerMovimentacao() && !enventosSelecionados();
	}

	private boolean precisaTerMovimentacao()
	{
		return EventsEditorTreeHandler.instance().getRoots() != null && EventsEditorTreeHandler.instance().getRoots().size() > 0;
	}

	private boolean enventosSelecionados()
	{
		return EventsEditorTreeHandler.instance().getEventoBeanList() != null &&  EventsEditorTreeHandler.instance().getEventoBeanList().size() > 0;
	}

	public void dispararDescartar() {
		warning = false;
 		alert=ProcessoDocumentoHome.instance().verificarDocumentoEventoRelacionado(getProtocolarDocumentoBean().getDocumentoPrincipal());
 		if (getModeloDocumento() != null) {
			warning = true;
		}
	}

	public void descartarDocumento() throws PJeBusinessException {
		if (alert){
			return ;
		}
		Object variable =   tramitacaoProcessualService.recuperaVariavel(Variaveis.MINUTA_EM_ELABORACAO);

		if (variable != null && variable instanceof Integer) {
			getProtocolarDocumentoBean().setDocumentoPrincipal(documentoJudicialService.getDocumento(idMinuta));
			if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null) {
				getProtocolarDocumentoBean().acaoRemoverTodos();
				salvar("");
			}
		}
 		Contexts.getBusinessProcessContext().flush();

 	}

	public boolean isDocumentoValido()
	{
		if (getModeloDocumento() != null)
		{
 			return  !"".equals(getModeloDocumento().trim()) && getTipoProcessoDocumento() != null;
		}
		return false;
	}

	public boolean liberaCertificacao() throws PJeBusinessException {
		if (getProtocolarDocumentoBean().getDocumentoPrincipal() != null && usuarioService != null && usuarioService.getLocalizacaoAtual() != null ){
			return documentoJudicialService.podeAssinar(getTipoProcessoDocumento(), usuarioService.getLocalizacaoAtual().getPapel());
		}
		return false;
	}

	public boolean isTransicaoSaidaValido() {
		if ((transicaoSaida == null || "".equals(transicaoSaida)))
		{
			return false;
		}
		return true;
	}

	public String getTransicaoSaida() {
		return transicaoSaida;
	}
	public boolean isAssinado() {
		return assinado;
	}

	public boolean isPerguntaTransitar() {
		return perguntaTransitar;
	}

	public void setPerguntaTransitar(boolean perguntaTransitar) {
		this.perguntaTransitar = perguntaTransitar;
	}

	public boolean isRenderEventsTree() {
		return renderEventsTree;
	}

	public void setRenderEventsTree(boolean renderEventsTree) {
		this.renderEventsTree = renderEventsTree;
	}

	public Integer getIdAgrupamentos() {
		return idAgrupamentos;
	}

	public void setIdAgrupamentos(Integer idAgrupamentos) {
		this.idAgrupamentos = idAgrupamentos;
	}

	public boolean isWarning() {
		return warning;
	}

	public void setWarning(boolean warning) {
		this.warning = warning;
	}

	public List<ProcessoDocumentoBinPessoaAssinatura> getListaAssinatura() {
		return listaAssinatura;
	}

	public void setListaAssinatura(List<ProcessoDocumentoBinPessoaAssinatura> listaAssinatura) {
		this.listaAssinatura = listaAssinatura;
	}

	public boolean isObrigatorio() {
		return obrigatorio;
	}

	public void setObrigatorio(boolean obrigatorio) {
		this.obrigatorio = obrigatorio;
	}

	public boolean isAlert() {
		return alert;
	}

	public void setAlert(boolean alert) {
		this.alert = alert;
	}

	/**
	 * Metodo que retorna lista com modelos de documento de acordo com a pesquisa de modelo de documento para a suggestion box.
	 * @param textoEntrada (contendo o titulo ou parte do conteudo do modelo de documento)
	 * @return Lista com os modelos de documento filtrados para apresentação na suggestion box de pesquisa
	 * @author eduardo.pereira@tse.jus.br
	 */
	public List<ModeloDocumento> pesquisarModelo(Object textoEntrada) {
		List<ModeloDocumento> lista = new ArrayList<ModeloDocumento>();
		try {
			Integer[] idsModelos = obterIdsModeloDocumentoFluxo();
			lista = this.modeloDocumentoManager.getModelosPorTipoTituloOuDescricao(
					getTipoProcessoDocumento(), (String) textoEntrada, idsModelos);
		} catch (Exception e) {
			this.facesMessages.clear();
			this.facesMessages.add(Severity.ERROR, "Erro durante a pesquisa de modelos de documento!");
			e.printStackTrace();
		}
		return lista;
	}
	
	public boolean isDocumentoAssinado() throws PJeBusinessException{
		boolean assinado = Boolean.FALSE;
		if(idMinuta != null && idMinuta > 0){
			assinado = ComponentUtil.getAssinaturaDocumentoService().isDocumentoAssinado(idMinuta);
		}
		return assinado;
	}
	
	public boolean isUploadArquivoAssinadoRealizado(){
		return uploadArquivoAssinadoRealizado;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.getProtocolarDocumentoBean().addArquivoAssinado(arquivoAssinadoHash);
		uploadArquivoAssinadoRealizado = Boolean.TRUE;
	}

	public String getActionName() {
		return NAME;
	}
	
	public String obterConteudoDocumentoAtual() {
		String conteudo = this.getModeloDocumento();
		String conteudoJson = "";
		try {
			conteudoJson = controleVersaoDocumentoManager.obterConteudoDocumentoJSON(conteudo);
		} catch (JSONException e) {
			logger.error(e.getMessage());
			conteudoJson = "";
		} 
		return conteudoJson;
	}
	
	/**
	 * Operação utilizado pelo plugin de pesquisa documental para recuperar modelo de documento.
	 * 
	 * @param modeloDocumento com o nome do modelo
	 * 
	 * @return String com o modelo de documento
	 */
	public String recuperarModeloDocumento(String modeloDocumento){
		selecionarModeloProcessoDocumento(modeloDocumento);
		return getModeloDocumento();
	}

	@Override
	public String obterTiposVoto() {
		return null;
	}
	
	private void geraActionInstanceId() {
		actionInstanceId = this.getActionName();
		
	}

	public String getActionInstanceId() {
		return actionInstanceId;
	}

	public void setActionInstanceId(String actionInstanceId) {
		this.actionInstanceId = actionInstanceId;
	}
	
	public boolean validarAction() {
		boolean valido = (this.verificaObrigatorio() && !this.isDocumentoVazio() && this.isTipoProcessoDocumentoDefinido() && this.isDocumentoPersistido() && this.isDocumentoValido() && !this.isConteudoAlterado());
		String mensagem = "";
		
		if(!valido) {
			if(!this.isDocumentoPersistido()) {
				mensagem = "Documento não foi salvo";
			}else if(this.isDocumentoVazio()) {
				mensagem = "O documento está com o conteúdo vazio";
			}else if(this.isConteudoAlterado()) {
				mensagem = "Há informações não salvas no documento";
			}else {
				mensagem = "Há informações obrigatórioas ainda não preenchidas";
			}
		}
		this.gravaInformacaoValidacao(valido, mensagem);
		return valido;
	}
	
	private void gravaInformacaoValidacao(boolean valido, String mensagem) {
		TaskInstance.instance().setVariableLocally(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_RESULTADO.concat(this.getActionInstanceId()), valido);
		TaskInstance.instance().setVariableLocally(Variaveis.PJE_PREFIXO_VARIAVEL_TAREFA_VALIDACAO_COMPONENTE_MENSAGEM.concat(this.getActionInstanceId()), mensagem);
	}
}
