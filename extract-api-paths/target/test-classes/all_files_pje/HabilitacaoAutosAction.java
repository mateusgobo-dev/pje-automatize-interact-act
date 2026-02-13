package br.jus.csjt.pje.view.action;

import java.io.Serializable;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.Predicate;
import org.jboss.resteasy.util.DateUtil;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.jboss.seam.transaction.Transaction;
import org.richfaces.component.UITree;

import br.com.infox.cliente.actions.anexarDocumentos.AnexarDocumentos;
import br.com.infox.cliente.home.PessoaFisicaHome;
import br.com.infox.cliente.home.ProcessoParteHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.manager.HabilitacaoAutosManager;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.AjaxDataUtil;
import br.jus.cnj.pje.view.DocumentoCertidaoAction;
import br.jus.cnj.pje.view.ProtocolarDocumentoBean;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.csjt.pje.business.service.HabilitacaoAutosService;
import br.jus.pje.jt.entidades.HabilitacaoAutos;
import br.jus.pje.jt.enums.TipoDeclaracaoEnum;
import br.jus.pje.jt.enums.TipoSolicitacaoHabilitacaoEnum;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoPeticaoNaoLida;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.ProcessoParteSituacaoEnum;
import br.jus.pje.nucleo.enums.TipoProcuradoriaEnum;

@Name(HabilitacaoAutosAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class HabilitacaoAutosAction implements Serializable, ArquivoAssinadoUploader {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8948195475041588284L;

	public static final String NAME = "habilitacaoAutosAction";
	
	private static final String incluido = "destaqueIncluido";
	private static final String removido = "destaqueRemovido";
	
	@In(create = true)
	private transient HabilitacaoAutosService habilitacaoAutosService;
	@In
	private ProcessoJudicialService processoJudicialService;
	
	private ProcessoTrf processoTrf;
	private List<ProcessoParte> processoParteList = new ArrayList<ProcessoParte>(0);
	private List<ProcessoParteRepresentante> representanteRemovidoList = new ArrayList<ProcessoParteRepresentante>(0);
	private PessoaAdvogado   pessoaAdvogado;
	private List<Procuradoria> defensoriaRemovidaList = new ArrayList<Procuradoria>(0);
	private Usuario usuarioSolicitante;
	private UsuarioLocalizacao usuarioLocalizacao;
	private TipoDeclaracaoEnum tipoDeclaracao;
	private TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao;
	private boolean mostrarAbaAnexarDocumentos;
	private boolean mostrarAbaVincularPartes;
	private boolean mostrarAbaSelecionarPolo = Boolean.TRUE;
	private String tab = "selecionarPoloTab";
	private Map<ProcessoParte, Boolean> mapaProcessoParteSelecionado = new HashMap<ProcessoParte, Boolean>();
	private Map<ProcessoDocumentoPeticaoNaoLida, Boolean> mapaProcessoDocumentoPeticaoNaoLidaSelecionada = new HashMap<ProcessoDocumentoPeticaoNaoLida, Boolean>(0);
	private List<ProcessoDocumentoPeticaoNaoLida> processoDocumentoPeticaoNaoLidaList = new ArrayList<ProcessoDocumentoPeticaoNaoLida>(0);
	private final int LINHAS_TABELA = 10;
	private int paginaAtual = 1;
	
	private boolean selecionarTodos		   = Boolean.FALSE;	
	private boolean poloAtivoSelecionado   = Boolean.FALSE;
	private boolean poloPassivoSelecionado = Boolean.FALSE;
	
	private boolean mostrarBotaoAssinatura = Boolean.FALSE;

	private HabilitacaoAutos habilitacaoAutos;
	
	@In(create = true)
	private HabilitacaoAutosManager habilitacaoAutosManager;

	@In(required=false)
	private Identity identity;
	
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	
	@In(create=true)
	private DocumentoJudicialService documentoJudicialService;
	
	@In(create=true)
	private AjaxDataUtil ajaxDataUtil;
	
	public HabilitacaoAutosAction(){
		processoTrf = ProcessoTrfHome.instance().getInstance();
	}
	
	
	@Create
	public void init(){	
		usuarioSolicitante = ProcessoHome.instance().getUsuarioLogado();
		
		usuarioLocalizacao = new UsuarioLocalizacao();
		usuarioLocalizacao.setUsuario(usuarioSolicitante);
				
		//Caso o processo esteja em segredo de justiï¿½a e o advogado nï¿½o possua visibilidade no processo sï¿½ serï¿½ exibida a aba de petiï¿½ï¿½o.
		if(processoTrf.getSegredoJustica() && !processoJudicialService.visivel(processoTrf, usuarioLocalizacao, null)){
			setTab("peticaoDocumentosTab");
			mostrarAbaAnexarDocumentos = true;
			mostrarAbaSelecionarPolo = false;
			pessoaAdvogado = ((PessoaFisica) ProcessoHome.instance().getUsuarioLogado()).getPessoaAdvogado();
		}
		
		if (this.protocolarDocumentoBean == null) {
			
			if(this.identity.hasRole(Papeis.INTERNO)){
				this.protocolarDocumentoBean = new ProtocolarDocumentoBean(this.processoTrf.getIdProcessoTrf(),
								ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL 
		 						| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO
		 						| ProtocolarDocumentoBean.PERMITE_SELECIONAR_MOVIMENTACAO
		 						| ProtocolarDocumentoBean.UTILIZAR_MODELOS,
		 						getActionName());
		 	}
			else {
				this.protocolarDocumentoBean = new ProtocolarDocumentoBean(this.processoTrf.getIdProcessoTrf(),  
		 						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL  
								| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO
								| ProtocolarDocumentoBean.PERMITIR_VINCULACAO_RESPOSTA, 
								getActionName());
			}

			if (Objects.nonNull(this.protocolarDocumentoBean.getDocumentoPrincipal())) {
				this.protocolarDocumentoBean.getDocumentoPrincipal().setProcessoDocumento("Habilitação nos autos");
			}
		}
	}

	
	public void limparGrid(){
		mapaProcessoDocumentoPeticaoNaoLidaSelecionada.clear();
		processoDocumentoPeticaoNaoLidaList.clear();
	}
	
	/**
	 * ValidaÃ§Ãµes para que seja habilitada a aba "Anexar Documento".
	 */
	@SuppressWarnings("static-access")
	public void habilitarAbaAnexarDocumento(){
		
		//Valida se o tipo de solicitaï¿½ï¿½o foi selecionado.
		if (tipoSolicitacaoHabilitacao == null) {
			FacesMessages.instance().add(Severity.ERROR, "É necessário selecionar o tipo de habilitação nos autos.");
			return;
		}
		
		//Valida se o tipo de declaraï¿½ï¿½o foi selecionado.
		if(tipoDeclaracao == null){
			FacesMessages.instance().add(Severity.ERROR, "É necessário selecionar o tipo de declaração.");
			return;
		}
		
		UsuarioLocalizacao usuarioLocalizacao = Authenticator.getUsuarioLocalizacaoAtual();
		PessoaFisica pessoaFisica = ((PessoaFisica) ProcessoHome.instance().getUsuarioLogado());
		
		boolean habilitacaoAutosParaAdvogado = habilitacaoAutosManager
				.verificarHabilitacaoAutosParaAdvogado(pessoaFisica, usuarioLocalizacao);
		boolean habilitacaoAutosParaDefensor = habilitacaoAutosManager
				.verificarHabilitacaoAutosParaDefensor(pessoaFisica, usuarioLocalizacao);

        
		if (!habilitacaoAutosParaAdvogado && !habilitacaoAutosParaDefensor) {
			FacesMessages.instance().add(Severity.ERROR,
					"Esta operação só é possível para os perfis de Advogado ou de Denfensor.");
			return;
		}
		
		else {
			usuarioSolicitante = Authenticator.instance().getUsuarioLogado();
			if (habilitacaoAutosParaAdvogado) {

				// Caso seja habilitao nos autos por substabelecimento
				// verifico se o usurio informou o advogado.
				if (isSolicitacaoPorSubstabelecimento()) {
					pessoaAdvogado = ((PessoaFisicaHome) Component.getInstance("pessoaFisicaHome")).getInstance()
							.getPessoaAdvogado();
					if (pessoaAdvogado == null) {
						FacesMessages.instance().add(Severity.ERROR,
								"Para habilitao nos autos por substabelecimento  necessrio informar o representante.");
						return;
					}
				} else {
					pessoaAdvogado = ((PessoaFisica) ProcessoHome.instance().getUsuarioLogado()).getPessoaAdvogado();
				}

				if (advogadoIsRepresentanteOutroPolo(pessoaAdvogado.getPessoa())) {
					FacesMessages.instance().add(Severity.ERROR,
							"No  permitido representar partes de polos diferentes.");
					return;
				}
				// Valida se o advogado representa apenas um dos polos.
				for (ProcessoParte processoParte : mapaProcessoParteSelecionado.keySet()) {
					if (mapaProcessoParteSelecionado.get(processoParte)) {

						if (!processoParteList.isEmpty()
								&& processoParte.getInParticipacao() != processoParteList.get(0).getInParticipacao()) {
							// disparar erro por selecionar partes de polos
							// diferentes
							FacesMessages.instance().add(Severity.ERROR,
									"No  permitido representar partes de polos diferentes.");
							return;
						}
						processoParteList.add(processoParte);
					}
				}
			} else {
				
				if (isSolicitacaoPorSubstituicao()) {
					
					for (ProcessoParte processoParte : mapaProcessoParteSelecionado.keySet()) {
						if (mapaProcessoParteSelecionado.get(processoParte)) {
							
							@SuppressWarnings("unused")
							List<Procuradoria> listaRepresentantesDefensoriaAtivos = null;
							List<ProcessoParteRepresentante> listaRepresentantesAtivos = (List<ProcessoParteRepresentante>) getRepresentantesAtivos(processoParte);
							
							//valida se existe pelo menos um advogado ou uma procuradoria para a pessoa representada
							if(listaRepresentantesAtivos.isEmpty() && processoParte.getProcuradoria() == null){
								mapaProcessoParteSelecionado.clear();
								FacesMessages.instance().add(Severity.ERROR,
										"Não é permitido substituir partes sem representante(s)");
								return;
							}
							
							if(processoParte.getProcuradoria() != null && 
									processoParte.getProcuradoria().getIdProcuradoria() == getProcuradoria().getIdProcuradoria()){
								mapaProcessoParteSelecionado.clear();
								FacesMessages.instance().add(Severity.ERROR,
										"Esta Defensoria já está representando a parte ("+processoParte.getNomeParte()+")");
								return;
							}
							
							if(processoParte.getProcuradoria() != null && !isDefensoria()){
								mapaProcessoParteSelecionado.clear();
								FacesMessages.instance().add(Severity.ERROR,
										"Atualmente somente a Defensoria pode substituir o representante " + processoParte.getNomeParte());
								return;
							}
							
							if(listaRepresentantesAtivos.isEmpty() && (processoParte.getProcuradoria() !=null && !processoParte.getProcuradoria().getTipo().equals(TipoProcuradoriaEnum.D))){
								mapaProcessoParteSelecionado.clear();
								FacesMessages.instance().add(Severity.ERROR,
										"Não é permitido que a Defensoria substitua o representante " + processoParte.getProcuradoria().getNome());
								return;
							}
							
							if(!listaRepresentantesAtivos.isEmpty() && 
									(defensoriaRemovidaList.isEmpty() && representanteRemovidoList.isEmpty())){
								mapaProcessoParteSelecionado.clear();
								FacesMessages.instance().add(Severity.ERROR,
										"Para prosseguir é necessário remover pelos menos um representante da parte");
								return;
							}
							
							processoParteList.add(processoParte);
						}
					}
				} 
				else {
					FacesMessages.instance().add(Severity.ERROR,
							"O tipo de solicitação selecionada não é permitida.");
					return;
				}		
			}
		}
		if (processoParteList.isEmpty()) {
			// disparar erro por nï¿½o selecionar partes;
			FacesMessages.instance().add(Severity.ERROR, "É necessário selecionar pelo menos uma parte para representar");
			return;
		}						
		setMostrarAbaAnexarDocumentos(true);
		tab = "peticaoDocumentosTab";		
	}
	
	public void validarRemocaoRepresentante(List<ProcessoParteRepresentante> listaRepresentantesAtivos, ProcessoParte processoParte){
		if(listaRepresentantesAtivos.size() > 0 || (processoParte.getProcuradoria() !=null)){
			removerListaRepresentantes(listaRepresentantesAtivos);
		}
	}
	public void removerListaRepresentantes(List<ProcessoParteRepresentante> lista) {
		if(!lista.isEmpty() && lista.size() > 0){
			for(ProcessoParteRepresentante parte : lista){
				if ((parte.getParteRepresentante().getInParticipacao() == ProcessoParteParticipacaoEnum.A
						&& !poloAtivoSelecionado)
						|| (parte.getParteRepresentante().getInParticipacao() == ProcessoParteParticipacaoEnum.P
								&& !poloPassivoSelecionado)) {
		
					FacesMessages.instance().add(Severity.ERROR, "No  permitido remover o representante de outro polo.");
					return;
				}
		
				representanteRemovidoList.add(parte);
			}
		}
	}
	
	public void removerDefensoriaRepresentante(Procuradoria procuradoria) {
		defensoriaRemovidaList.add(procuradoria);
	}

	
	public void habilitarAbaVincularPartes(){
		
		if(!poloAtivoSelecionado && !poloPassivoSelecionado){
			FacesMessages.instance().add(Severity.ERROR, "É necessário selecionar um polo para representar");
			return;
		}
		
		if(poloAtivoSelecionado && poloPassivoSelecionado){
			FacesMessages.instance().add(Severity.ERROR, "Não é permitido representar partes de ambos polos.");
			return;
		}
		
		if(advogadoIsRepresentanteOutroPolo(usuarioSolicitante)){
			FacesMessages.instance().add(Severity.ERROR, "Não é permitido representar partes de polos diferentes.");
			return;
		}
		
		List<ProcessoParte>partesList  = poloAtivoSelecionado? processoTrf.getListaPartePrincipalAtivo() :processoTrf.getListaPartePrincipalPassivo();
		for(ProcessoParte pp : partesList){
			mapaProcessoParteSelecionado.put(pp, false);
		}
		
		setMostrarAbaVincularPartes(true);
		tab = "vincularPartesTab";
	}
	
	/**
	 * Inclui o processoParteRepresentante na lista de advogados removidos na habilitaï¿½ï¿½o nos autos.
	 * 
	 * @param ProcessoParteRepresentante
	 */
	public void removerRepresentante(ProcessoParteRepresentante parte){
		
		if((parte.getParteRepresentante().getInParticipacao() == ProcessoParteParticipacaoEnum.A && !poloAtivoSelecionado) || 
		   (parte.getParteRepresentante().getInParticipacao() == ProcessoParteParticipacaoEnum.P && !poloPassivoSelecionado)){
			
			FacesMessages.instance().add(Severity.ERROR, "Não é permitido remover o representante de outro polo.");
			return;
		}

		representanteRemovidoList.add(parte);
	}
	
	/**
	 * Remove o processoParteRepresentante da lista de advogados removidos na habilitaï¿½ï¿½o nos autos.
	 * 
	 * @param ProcessoParteRepresentante
	 */
	public void desfazerRemocaoRepresentante(ProcessoParteRepresentante parte){
		representanteRemovidoList.remove(parte);
	}

	/**
	 * Verifica se o usuï¿½rio informado como parï¿½metro jï¿½ representa alguma parte do outro polo.
	 * 
	 * @param usuario
	 * @return <b>TRUE</b> caso o advogado jï¿½ represente o outro polo.<br/>
	 * 		   <b>FALSE</b> caso o advogado nï¿½o seja representante do outro polo.
	 */
	public boolean advogadoIsRepresentanteOutroPolo(Usuario usuario){
		
		List<ProcessoParte> listProcessoParte = poloAtivoSelecionado ? processoTrf.getListaPartePassivo() : processoTrf.getListaParteAtivo();
		
		for (ProcessoParte pp : listProcessoParte) {
			if(pp.getPessoa().getIdUsuario().equals(usuario.getIdUsuario())){
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Verifica se um determinado tipo de solicitaï¿½ï¿½o estï¿½ disponivel na habilitaï¿½ï¿½o nos autos.
	 */
	public boolean permiteHabilitacaoPorTipoDeSolicitacao(TipoSolicitacaoHabilitacaoEnum tipoSolicitacao){
		
		//Sï¿½ permitir habilitaï¿½ï¿½o por substabelecimento por parte de usuï¿½rios que jï¿½ representem uma das partes do mesmo polo.
		if(tipoSolicitacao == TipoSolicitacaoHabilitacaoEnum.I){
			
			List<ProcessoParte> listProcessoParte = poloAtivoSelecionado ? processoTrf.getListaParteAtivo() : processoTrf.getListaPartePassivo();

			for (ProcessoParte pp : listProcessoParte) {
				if(pp.getPessoa().getIdUsuario().equals(usuarioSolicitante.getIdUsuario())){
					return true;
				}
			}
			
			return false;
		}else{
			return true;
		}
	}
		
    public String getPartesHabilitacaoAutos(){
		
		StringBuilder saida = new StringBuilder();
		
		for (ProcessoParte processoParte : processoParteList){
			
			saida.append(processoParte.getNomeParte());
			saida.append("<br/>");
			
		}
		
		return saida.toString();
		
	}
    
	public void assinarConfirmarHabilitacaoAutos() throws PJeBusinessException{
		
		habilitacaoAutosService.assinarFinalizarHabilitacaoAutos( pessoaAdvogado,
																  usuarioSolicitante,
																  processoTrf,
																  processoParteList, 
																  representanteRemovidoList,
																  tipoDeclaracao,
																  tipoSolicitacaoHabilitacao);
	}	
	
	public void assinarFinalizarHabilitacaoAutosMultiplusDocumentos() throws PJeBusinessException{
		habilitacaoAutosService.assinarFinalizarHabilitacaoAutosMultiplusDocumentos( pessoaAdvogado,
																					 usuarioSolicitante,
																					 processoTrf,
																					 processoParteList, 
																					 representanteRemovidoList,
																					 tipoDeclaracao,
																					 tipoSolicitacaoHabilitacao);
	}
	
	public List<ProcessoDocumentoPeticaoNaoLida> getProcessoDocumentoPeticaoNaoLidaList() {
			
		// busca lista no banco
		List<ProcessoDocumentoPeticaoNaoLida> processoDocumentoPeticaoNaoLidaListAux = habilitacaoAutosService.getProcessoDocumentoPeticaoNaoLidaHabilitacaoAutosList();
		
		if (processoDocumentoPeticaoNaoLidaListAux != null) {
			processoDocumentoPeticaoNaoLidaList = processoDocumentoPeticaoNaoLidaListAux;
		}
		for (ProcessoDocumentoPeticaoNaoLida pd : processoDocumentoPeticaoNaoLidaList) {
			if(!mapaProcessoDocumentoPeticaoNaoLidaSelecionada.containsKey(pd)){
				mapaProcessoDocumentoPeticaoNaoLidaSelecionada.put(pd, Boolean.FALSE);
			}
		}

		return processoDocumentoPeticaoNaoLidaList;
	}
	
	public List<ProcessoDocumentoPeticaoNaoLida> getProcessoDocumentoPeticaoNaoLidaSubList() {
		int inicio = (paginaAtual - 1) * LINHAS_TABELA;
		int fim = Math.min(paginaAtual * LINHAS_TABELA, getResultCount());

		return getProcessoDocumentoPeticaoNaoLidaList().subList(inicio, fim);
	}
	
	public Map<ProcessoDocumentoPeticaoNaoLida, Boolean> getMapaProcessoDocumentoPeticaoNaoLidaSelecionada() {
		return mapaProcessoDocumentoPeticaoNaoLidaSelecionada;
	}

	public void setMapaProcessoDocumentoPeticaoNaoLidaSelecionada(
			Map<ProcessoDocumentoPeticaoNaoLida, Boolean> mapaProcessoDocumentoPeticaoNaoLidaSelecionada) {
		this.mapaProcessoDocumentoPeticaoNaoLidaSelecionada = mapaProcessoDocumentoPeticaoNaoLidaSelecionada;
	}
	
	
	
	public List<Procuradoria> getDefensoriaRemovidaList() {
		return defensoriaRemovidaList;
	}


	public void setDefensoriaRemovidaList(List<Procuradoria> defensoriaRemovidaList) {
		this.defensoriaRemovidaList = defensoriaRemovidaList;
	}


	public String getNumeroProcesso(ProcessoDocumentoPeticaoNaoLida pd) {
		Processo processo;
		if (pd.getHabilitacaoAutos() != null) {
			processo = pd.getHabilitacaoAutos().getProcesso().getProcesso();
		}
		else {
			processo = pd.getProcessoDocumento().getProcesso();
		}
		
		return processo.getNumeroProcesso();
	}
	
	public String getNomeSolicitante(ProcessoDocumentoPeticaoNaoLida pd) {
		if (pd.getHabilitacaoAutos() != null) {
			if (pd.getHabilitacaoAutos().getAdvogado() != null) {
				return pd.getHabilitacaoAutos().getAdvogado().getNome();
			} else if (pd.getHabilitacaoAutos().getProcuradoria() != null) {
				return pd.getHabilitacaoAutos().getProcuradoria().getNome();
			}
		}
		return pd.getProcessoDocumento().getUsuarioInclusao().getNome();
	}
	
	public String getDataSolicitacao(ProcessoDocumentoPeticaoNaoLida pd) {
		if (pd.getHabilitacaoAutos() != null) {
			return DateUtil.formatDate(pd.getHabilitacaoAutos().getDataHora(), "dd/MM/yyyy");
		}
		else {
			return DateUtil.formatDate(pd.getProcessoDocumento().getDataInclusao(), "dd/MM/yyyy");
		}		
	}
	
	public String getClasseJudicialSigla(ProcessoDocumentoPeticaoNaoLida pd) {
		if (pd.getHabilitacaoAutos() != null) {
			return pd.getHabilitacaoAutos().getProcesso().getClasseJudicial().getClasseJudicialSigla();
		} else {
			return habilitacaoAutosService.getProcessoTrfByProcesso(
						pd.getProcessoDocumento().getProcesso()).getClasseJudicial().getClasseJudicialSigla();
		}
	}
	
	public String getClasseJudicial(ProcessoDocumentoPeticaoNaoLida pd) {
		if (pd.getHabilitacaoAutos() != null) {
			return pd.getHabilitacaoAutos().getProcesso().getClasseJudicialStr();
		}
		else {
			return habilitacaoAutosService.getProcessoTrfByProcesso(pd.getProcessoDocumento().getProcesso()).getClasseJudicialStr();
		}
	}
	
	public int getLinhasTabela() {
		return LINHAS_TABELA;
	}
	
	public int getResultCount() {
		if (processoDocumentoPeticaoNaoLidaList.size() == 0) {
			this.getProcessoDocumentoPeticaoNaoLidaList();
		}
		
		return getProcessoDocumentoPeticaoNaoLidaList().size();
	}
	
	public int getNumeroPaginas() {
		if (getResultCount() % LINHAS_TABELA == 0) {
			return getResultCount() / LINHAS_TABELA;
		}
		
		return getResultCount() / LINHAS_TABELA + 1;
	}

	public int getPaginaAtual() {
		return paginaAtual;
	}

	public void setPaginaAtual(int paginaAtual) {
		this.paginaAtual = paginaAtual;
		this.selecionarTodos = false;
	}
	
	public Boolean getPoloAtivoSelecionado() {
		return poloAtivoSelecionado;
	}

	public void setPoloAtivoSelecionado(Boolean poloAtivoSelecionado) {
		this.poloAtivoSelecionado = poloAtivoSelecionado;
		this.poloPassivoSelecionado = poloAtivoSelecionado? Boolean.FALSE : this.poloPassivoSelecionado;
	}

	public Boolean getPoloPassivoSelecionado() {
		return poloPassivoSelecionado;
	}

	public void setPoloPassivoSelecionado(Boolean poloPassivoSelecionado) {
		this.poloPassivoSelecionado = poloPassivoSelecionado;
		this.poloAtivoSelecionado = poloPassivoSelecionado? Boolean.FALSE : this.poloAtivoSelecionado;
	}

	public boolean isSelecionarTodos() {
		selecionarTodos = getResultCount() > 0;
		
		for (int i = 0; (selecionarTodos) && (i < getProcessoDocumentoPeticaoNaoLidaSubList().size()); i++) {
			selecionarTodos = mapaProcessoDocumentoPeticaoNaoLidaSelecionada.get(getProcessoDocumentoPeticaoNaoLidaSubList().get(i));
		}
		
		return selecionarTodos;
	}

	public void setSelecionarTodos(boolean selecionarTodos) {
		this.selecionarTodos = selecionarTodos;
	}
	
	public void toggleSelecionarTodos() {
		for (ProcessoDocumentoPeticaoNaoLida pd : getProcessoDocumentoPeticaoNaoLidaSubList()) {
			mapaProcessoDocumentoPeticaoNaoLidaSelecionada.put(pd, selecionarTodos);
		}
	}
	
	public int getIdProcesso(ProcessoDocumentoPeticaoNaoLida pd) {
		Processo processo;
		
		if (pd.getHabilitacaoAutos() != null) {
			processo = pd.getHabilitacaoAutos().getProcesso().getProcesso();
		}
		else {
			processo = pd.getProcessoDocumento().getProcesso();
		}

		return processo.getIdProcesso();
	}
	
	public String getMetodoHabilitacaoAutos(ProcessoDocumentoPeticaoNaoLida pd) {
		
		if (pd.getHabilitacaoAutos() != null) {
			return "Automática";
		}
		else {
			return "Manual";
		}

	}
	
	public ProcessoTrf getProcessoTrf(ProcessoDocumentoPeticaoNaoLida pd) {
		if (pd.getHabilitacaoAutos() != null) {
			return pd.getHabilitacaoAutos().getProcesso();
		}
		else {
			return habilitacaoAutosService.getProcessoTrfByProcesso(pd.getProcessoDocumento().getProcesso());
		}
	}
	
	public String getProcessoDeclaracao(ProcessoDocumentoPeticaoNaoLida pd) {
		if (pd.getHabilitacaoAutos() != null) {
			return pd.getHabilitacaoAutos().getTipoDeclaracao().getLabel();
		}
		else {
			return "Declaro, sob as penas da lei, que neste ato apresentei instrumento de mandato";
		}
	}
	
	public boolean haProcessosSelecionadosParaRetirarDestaque() {
		for (ProcessoDocumentoPeticaoNaoLida pd : mapaProcessoDocumentoPeticaoNaoLidaSelecionada.keySet()) {
			if (mapaProcessoDocumentoPeticaoNaoLidaSelecionada.get(pd)) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<ProcessoParte> getProcessoPartePoloAtivoSemAdvogadoList(ProcessoDocumentoPeticaoNaoLida pd) {
		return habilitacaoAutosService.getProcessoPartePoloAtivoList(getProcessoTrf(pd));
	}
	
	public List<ProcessoParte> getProcessoPartePoloPassivoSemAdvogadoList(ProcessoDocumentoPeticaoNaoLida pd) {
		return habilitacaoAutosService.getProcessoPartePoloPassivoList(getProcessoTrf(pd));
	}
	
	public void alterarListaPeticaoAvulsa(){
		
		
		for (ProcessoDocumentoPeticaoNaoLida processoDocumentoPeticaoNaoLida : mapaProcessoDocumentoPeticaoNaoLidaSelecionada.keySet()){
			
			if(mapaProcessoDocumentoPeticaoNaoLidaSelecionada.get(processoDocumentoPeticaoNaoLida)){
				habilitacaoAutosService.retirarDestaque(processoDocumentoPeticaoNaoLida);
			}
		}
		
		limparGrid();
	}
	
	public void habilitarAssinatura( String idCampo ) {
		
		if ("tipoProcessoDocumentoHtml".equalsIgnoreCase(idCampo)) {
			AnexarDocumentos.instance().onSelectProcessoDocumento();
		}
		
		mostrarBotaoAssinatura = camposDoModeloPreenchido();
	}

	public boolean camposDoModeloPreenchido() {
		String processoDocumento = AnexarDocumentos.instance().getPdHtml().getProcessoDocumento();  
		String modeloDocumento = removeTags( AnexarDocumentos.instance().getPdbHtml().getModeloDocumento() );
		
		boolean processoDocumentoOk = processoDocumento==null ? false : !processoDocumento.trim().isEmpty();
		boolean tipoProcessoDocumentoOk = AnexarDocumentos.instance().getPdHtml().getTipoProcessoDocumento() != null;
		boolean modeloDocumentoOk = modeloDocumento==null ? false : !modeloDocumento.trim().isEmpty();

		return processoDocumentoOk && tipoProcessoDocumentoOk && modeloDocumentoOk;
	}
	
	public boolean getMostrarBotaoAssinatura() {
		return this.mostrarBotaoAssinatura;
	}
	
	private static String removeTags(String textoHtml) {
		return textoHtml==null ? null : textoHtml.replaceAll("\\<.*?\\>", "").replaceAll("\n", "").replaceAll("\r", "").replaceAll("&nbsp;", "");
	}
	
	public String getProcessoParteNomeDocumento(ProcessoParte pp) {
		StringBuilder retorno = new StringBuilder();
		
		retorno.append(pp.getNomeParte());
		
		String documento = pp.getPessoa().getDocumentoCpfCnpj();
		
		if (pp.getPessoa() instanceof PessoaFisica) {
			if (documento == null) {
				retorno.append(" - CPF não cadastrado");
			}
			else {
				retorno.append(" - CPF: ");
				retorno.append(pp.getPessoa().getDocumentoCpfCnpj());
			}
		}
		else if (pp.getPessoa() instanceof PessoaJuridica) {
			if (documento == null) {
				retorno.append(" - CNPJ não cadastrado");
			}
			else {
				retorno.append(" - CNPJ: ");
				retorno.append(pp.getPessoa().getDocumentoCpfCnpj());
			}
		}
		
		return retorno.toString();
	}
	
	public boolean possuiPrioridade(ProcessoDocumentoPeticaoNaoLida pd) {
		ProcessoTrf processo;
		
		if (pd.getHabilitacaoAutos() != null) {
			processo = pd.getHabilitacaoAutos().getProcesso();
		}
		else {
			processo = habilitacaoAutosService.getProcessoTrfByProcesso(pd.getProcessoDocumento().getProcesso());
		}
		
		return (processo.getProcessoPrioridadeProcessoList() != null) && (processo.getProcessoPrioridadeProcessoList().size() > 0);
	}
	
	public void salvarHabilitacaoAutos() {
		try {
			// Grava os dados das assinaturas
			this.documentoJudicialService.gravarAssinaturaDeProcessoDocumento(
				getProtocolarDocumentoBean().getArquivosAssinados(),
				getProtocolarDocumentoBean().getProcessoDocumentosParaAssinatura()
			);
			
			AnexarDocumentos anexarDocumentos = (AnexarDocumentos)Component.getInstance(AnexarDocumentos.class);
		
			anexarDocumentos.getDocumentosSalvos().add(getProtocolarDocumentoBean().getDocumentoPrincipal());
		 	anexarDocumentos.getDocumentosSalvos().addAll(getProtocolarDocumentoBean().getArquivos());
			
		 	if(!isDefensoria()){
				if (processoTrf.getSegredoJustica()
						&& !processoJudicialService.visivel(processoTrf, usuarioLocalizacao, null)) {
					habilitacaoAutosService.salvarHabilitacaoManual(pessoaAdvogado, processoTrf);
				} else {
					habilitacaoAutosService.salvarHabilitacaoAutomatica(pessoaAdvogado, usuarioSolicitante, processoTrf,
							processoParteList, representanteRemovidoList, tipoDeclaracao, tipoSolicitacaoHabilitacao);
				}
		 	}else{

				if (processoTrf.getSegredoJustica()
						&& !processoJudicialService.visivel(processoTrf, usuarioLocalizacao, null)) {
					habilitacaoAutosService.salvarHabilitacaoManualDefensoria(pessoaAdvogado, usuarioSolicitante, processoTrf,
							processoParteList, representanteRemovidoList, tipoDeclaracao, tipoSolicitacaoHabilitacao, getProcuradoria(), defensoriaRemovidaList);
				} else {					
					habilitacaoAutosService.salvarHabilitacaoAutomaticaDefensoria(((PessoaFisica) ProcessoHome.instance().getUsuarioLogado()).getPessoaAdvogado(), usuarioSolicitante, processoTrf,
							processoParteList, representanteRemovidoList, tipoDeclaracao, tipoSolicitacaoHabilitacao, getProcuradoria(), defensoriaRemovidaList);
				}
		 	}
			
			this.gerarCertidao(protocolarDocumentoBean.getDocumentoPrincipal());

			this.protocolarDocumentoBean.getArquivosAssinados().clear();
			
			this.ajaxDataUtil.sucesso();
		} 
		catch (Exception e) {
			
			this.protocolarDocumentoBean.getArquivosAssinados().clear();
			
			try {
				Transaction.instance().rollback();
			}
			catch (Exception e1) {
				throw new RuntimeException(e1);	
			}
			
			ajaxDataUtil.erro();
			
			FacesMessages.instance().add(Severity.ERROR, "Erro ao salvar pedido de habilitação nos autos do processo: " + e.getLocalizedMessage());
		}
	}
	
	@SuppressWarnings("static-access")
	public Procuradoria getProcuradoria(){
		ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager) Component.getInstance("procuradoriaManager");
		Procuradoria procuradoria = procuradoriaManager.recuperaPorLocalizacao(Authenticator.instance().getLocalizacaoUsuarioLogado());
		return procuradoria;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<Procuradoria> getRepresentantesDefensoriaAtivos(ProcessoParte processoParte) {

		Predicate condicaoProcuradoriaAtiva = new Predicate() {
			public boolean evaluate(Object obj) {
				return ((Procuradoria) obj).getAtivo().equals(Boolean.TRUE);
			};
		};
		
		List<Procuradoria> listProcuradoriaAtiva = new ArrayList<Procuradoria>();
		listProcuradoriaAtiva.add(processoParte.getProcuradoria());
		return CollectionUtilsPje.select(listProcuradoriaAtiva, condicaoProcuradoriaAtiva);
	}
	public Boolean isSolicitacaoPorSubstituicao() {
		return (tipoSolicitacaoHabilitacao == null) ? false
				: (tipoSolicitacaoHabilitacao == TipoSolicitacaoHabilitacaoEnum.R);
	}


	
	@SuppressWarnings("unchecked")
	public Collection<ProcessoParteRepresentante> getRepresentantesAtivos(ProcessoParte processoParte){
		
		Predicate p = new Predicate() {
			public boolean evaluate(Object obj){
				return ((ProcessoParteRepresentante)obj).getInSituacao() == ProcessoParteSituacaoEnum.A;
			};
		};
				
		return CollectionUtilsPje.select(processoParte.getProcessoParteRepresentanteList(), p);
	}
	
	public List<String> getNomesRepresentados(ProcessoDocumentoPeticaoNaoLida pd) {
		List<String> nomesRepresentados = new ArrayList<String>();
		if (pd.getHabilitacaoAutos() != null) {
			List<ProcessoParte> representados = pd.getHabilitacaoAutos().getRepresentados();
			for (ProcessoParte pp : representados) {
				nomesRepresentados.add(pp.getNomeParte());
			}
		}
		return nomesRepresentados;
	}
	
	public String getSituacaoHabilitacao(ProcessoDocumentoPeticaoNaoLida pd) {
		String situacaoHabilitacao = "";
		if(pd.getHabilitacaoAutos() != null) {
			situacaoHabilitacao = pd.getHabilitacaoAutos().getSituacaoHabilitacao().getLabel();
		}
		return situacaoHabilitacao;
	}
	
	//get e set	
	public Boolean isSolicitacaoPorSubstabelecimento(){
		return (tipoSolicitacaoHabilitacao == null) ? false : (tipoSolicitacaoHabilitacao == TipoSolicitacaoHabilitacaoEnum.I);
	}
	
	public void setTipoSolicitacaoHabilitacao(TipoSolicitacaoHabilitacaoEnum tipoSolicitacaoHabilitacao) {
		this.tipoSolicitacaoHabilitacao = tipoSolicitacaoHabilitacao;
	}
	
	public Map<ProcessoParte, Boolean> getMapaProcessoParteSelecionado() {
		return mapaProcessoParteSelecionado;
	}
	
	public void setMapaProcessoParteSelecionado(
			Map<ProcessoParte, Boolean> mapaProcessoParteSelecionado) {
		this.mapaProcessoParteSelecionado = mapaProcessoParteSelecionado;
	}
	
	public boolean getMapaProcessoParte(ProcessoParte processoParte){
		return mapaProcessoParteSelecionado.get(processoParte);
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}
	
	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}
	
	public List<ProcessoParte> getProcessoParteList() {
		return processoParteList;
	}
	
	public void setProcessoParteList(List<ProcessoParte> processoParteList) {
		this.processoParteList = processoParteList;
	}
	
	public List<ProcessoParteRepresentante> getRepresentanteRemovidoList() {
		return representanteRemovidoList;
	}

	public void setRepresentanteRemovidoList(List<ProcessoParteRepresentante> representanteRemovidoList) {
		this.representanteRemovidoList = representanteRemovidoList;
	}

	public PessoaAdvogado getPessoaAdvogado() {
		return pessoaAdvogado;
	}
	
	public void setPessoaAdvogado(PessoaAdvogado advogado) {
		this.pessoaAdvogado = advogado;
	}

	public TipoDeclaracaoEnum getTipoDeclaracao() {
		return tipoDeclaracao;
	}

	public void setTipoDeclaracao(TipoDeclaracaoEnum tipoDeclaracao) {
		this.tipoDeclaracao = tipoDeclaracao;
	}

	public TipoSolicitacaoHabilitacaoEnum getTipoSolicitacaoHabilitacao() {
		return tipoSolicitacaoHabilitacao;
	}

	public String getTab(){
		return tab;
	}
	
	public void setTab(String tab){
		this.tab = tab;
	}
	
	public boolean isMostrarAbaAnexarDocumentos(){
		return mostrarAbaAnexarDocumentos;
	}
	
	public void setMostrarAbaAnexarDocumentos(boolean mostrarAbaAnexarDocumentos){
		this.mostrarAbaAnexarDocumentos = mostrarAbaAnexarDocumentos;
	}
			
	public boolean isMostrarAbaVincularPartes() {
		return mostrarAbaVincularPartes;
	}

	public void setMostrarAbaVincularPartes(boolean mostrarAbaVincularPartes) {
		this.mostrarAbaVincularPartes = mostrarAbaVincularPartes;
	}
	
	
	public boolean isMostrarAbaSelecionarPolo() {
		return mostrarAbaSelecionarPolo;
	}


	public void setMostrarAbaSelecionarPolo(boolean mostrarAbaSelecionarPolo) {
		this.mostrarAbaSelecionarPolo = mostrarAbaSelecionarPolo;
	}


	public TipoDeclaracaoEnum[] getTipoDeclaracaoEnumValues() {
		return TipoDeclaracaoEnum.values();
	}
	
	public List<TipoSolicitacaoHabilitacaoEnum> getTipoSolicitacaoHabilitacaoEnumValues() {
		List<TipoSolicitacaoHabilitacaoEnum> lista = new ArrayList<TipoSolicitacaoHabilitacaoEnum>();
		if(isDefensoria()){
			lista.add(TipoSolicitacaoHabilitacaoEnum.R);
		}else{
			lista = TipoSolicitacaoHabilitacaoEnum.getVisiveis();
		}
		return lista;
	}
	
	public boolean isDefensoria(){
		boolean isDefensor = Boolean.FALSE;
		ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager) Component.getInstance("procuradoriaManager");
		Procuradoria procuradoria = procuradoriaManager.recuperaPorLocalizacao(Authenticator.getLocalizacaoAtual());
		if(procuradoria != null && procuradoria.getTipo().equals(TipoProcuradoriaEnum.D)){
			isDefensor = Boolean.TRUE;
		}
		return isDefensor;
	}
	
	public boolean isDefensoriaRepresentante(ProcessoParte processoParte) throws PJeBusinessException{
		boolean isDefensor = Boolean.FALSE;
		ProcuradoriaManager procuradoriaManager = (ProcuradoriaManager) Component.getInstance("procuradoriaManager");
		Procuradoria procuradoria = procuradoriaManager.findById(processoParte.getProcuradoria().getIdProcuradoria());
		if(procuradoria != null && procuradoria.getTipo().equals(TipoProcuradoriaEnum.D)){
			isDefensor = Boolean.TRUE;
		}
		return isDefensor;
	}
	
	public Boolean habilitarRemocaoDefensoriaPoloPassivoSelecionado(Procuradoria procuradoria){
		return procuradoria!= null && isDefensoria() && poloPassivoSelecionado && 
				procuradoria.getIdProcuradoria() != getProcuradoria().getIdProcuradoria(); 
	}
	
	public Boolean habilitarRemocaoDefensoriaPoloAtivoSelecionado(Procuradoria procuradoria){
		return procuradoria!= null && isDefensoria() && poloAtivoSelecionado && 
				procuradoria.getIdProcuradoria() != getProcuradoria().getIdProcuradoria(); 
	}

	/**
	 * Mï¿½todo responsï¿½vel por gerar o documento de certidï¿½o de protocolo do documento.
	 * 
	 * @param documento Documento do processo.
	 */
	private void gerarCertidao(ProcessoDocumento documento) {
		try {
			ComponentUtil.<DocumentoCertidaoAction>getComponent(DocumentoCertidaoAction.NAME).gerarCertidao(documento);
		} catch (PJeBusinessException ex) {
			FacesMessages.instance().add(Severity.ERROR, "Erro ao tentar gerar o documento de certidão.");
		}
	}
	
	
	/**
	 * Recupera uma lista com os tipos de polos presentes no processo.
	 * 
	 * @return a lista de polos existentes
	 */
	public List<String> getPoloAtivo(){
		List<String> ret = new ArrayList<String>();
		if (processoTrf.getListaPartePrincipalAtivo().size() > 0){
			ret.add("Polo ativo");
		}
		return ret;
	}
	
	public List<String> getPoloPassivo(){
		List<String> ret = new ArrayList<String>();
		if (processoTrf.getListaPartePrincipalPassivo().size() > 0){
			ret.add("Polo passivo");
		}
		return ret;
	}
	
	public Boolean adviseNodeOpened(UITree tree) {
		return true;
	}

	public HabilitacaoAutos getHabilitacaoAutos() {
		return habilitacaoAutos;
	}
	
	public void setHabilitacaoAutos(HabilitacaoAutos habilitacaoAutos) {
		this.habilitacaoAutos = habilitacaoAutos;
	}
	
	public Boolean isSolicitante(String representante) {
		return this.getHabilitacaoAutos().getAdvogado().toString().contains(representante);
	}
	
	/**
	 * Retorna lista de representantes das partes do processo relativo ï¿½ habilitaï¿½ï¿½o nos autos informada, com informaï¿½ï¿½o para identificar os incluï¿½dos e os removidos no pedido. 
	 * @param habilitacaoAutos HabilitacaoAutos A habilitaï¿½ï¿½o nos autos pertinente
	 * @param parte ProcessoParte A parte cujos representantes serï¿½o verificados
	 * @return List<ProcessoParte> Lista dos representantes formatados
	 */
	
	public List<ProcessoParte> listaRepresentanteFormatado(HabilitacaoAutos habilitacaoAutos, ProcessoParte parte) {
		List<ProcessoParte> listaFormatados = new ArrayList<ProcessoParte>();
		for (ProcessoParte representante : getProcessoParteHome().obtemProcessoParte_RepresentanteTodos(parte)) {
			if (habilitacaoAutos.getAdvogado().getNome().equals(representante.getNomeParte())) {
				representante.setFormatar(incluido);
				listaFormatados.add(representante);
			} else if (habilitacaoAutos.getRepresentantesRemovidos().toString().contains(representante.getNomeParte().toString())) {
				representante.setFormatar(removido);
				listaFormatados.add(representante);
			} else {
				if (!representante.getIsBaixado()) {
					representante.setFormatar(org.apache.commons.lang.StringUtils.EMPTY);
					listaFormatados.add(representante);
				}
			}
		}
		return listaFormatados;
	}
	
	private ProcessoParteHome getProcessoParteHome(){
		return ComponentUtil.getComponent("processoParteHome");
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest, ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
	}

	@Override
	public String getActionName() {
		return NAME;
	}

	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		return protocolarDocumentoBean;
	}	
}
