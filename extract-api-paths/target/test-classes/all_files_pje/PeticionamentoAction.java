package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.jboss.seam.transaction.Transaction;

import br.com.infox.cliente.component.securitytoken.SecurityTokenControler;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.utils.Constantes;
import br.com.itx.component.UrlUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.interfaces.ArquivoAssinadoUploader;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ConsultaProcessoTrfManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoVisibilidadeSegredoManager;
import br.jus.cnj.pje.vo.ArquivoAssinadoHash;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle da tela de peticionamento.
 * 
 * @author cristof
 * 
 *
 */
@Name(PeticionamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class PeticionamentoAction extends BaseAction<ProcessoTrf> implements Serializable, ArquivoAssinadoUploader {

	private static final long serialVersionUID = -3126287109181290379L;

	public static final String NAME = "peticionamentoAction";
	
	@In
	private Identity identity;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ProcessoParteManager processoParteManager;
	
	@In
	private ConsultaProcessoTrfManager consultaProcessoTrfManager;
	
	@In
	private ProcessoVisibilidadeSegredoManager processoVisibilidadeSegredoManager;
	
	@In
	private DocumentoJudicialService documentoJudicialService; 
	
	@RequestParameter(value="idProcesso")
	private Integer idProcessoSelecionado;
	
	private Integer numeroSequencia;
	private Integer digitoVerificador;
	private Integer ano;
	private String ramoJustica;
	private String respectivoTribunal;
	private Integer numeroOrigem;
	private ProtocolarDocumentoBean protocolarDocumentoBean;
	private List<Integer> processosVisibilidadeAtribuida = new ArrayList<Integer>();
	private boolean isNumeroProcessoIncompleto = false; 
	private EntityDataModel<ProcessoTrf> model;
	private boolean isUsuarioParteProcesso = false;
	private boolean isAdmin = false;
	private boolean isAdvogado = false;
	private boolean isAssAdvogado = false;
	private boolean isJusPostulandi = false;
	private boolean isProcurador = false;
	private boolean isMagistrado;
	private boolean exibeMensagemResolucaoCNJ = false;
	private boolean exibeAutosDigitais = false;
	private String urlPopUp;
	
	@Create
	public void init(){
		String numeroOrgaoJustica = ParametroUtil.getParametro("numeroOrgaoJustica");
		if (numeroOrgaoJustica != null) {
			this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
			this.respectivoTribunal = numeroOrgaoJustica.substring(1);
		}
		
		protocolarDocumentoBean = getProtocolarDocumentoBean();
		
		isAdmin = Authenticator.isPapelAdministrador();
		isAdvogado = Authenticator.isAdvogado();
		isAssAdvogado = Authenticator.isAssistenteAdvogado();
		isJusPostulandi = Authenticator.isJusPostulandi();
		isProcurador = Authenticator.isProcurador();
		isMagistrado = Authenticator.isMagistrado() || Authenticator.isVisualizaSigiloso();
		processosVisibilidadeAtribuida = processoVisibilidadeSegredoManager.recuperaVisibilidadeAtribuidaProcessoUsuarioLogado();
	}

	/**
	 * Método responsável por realizar a pesquisa de acordo com os dados informados e respeitando as regras 
	 * peticionamento
	 */
	public void pesquisar(){
		try {
			isNumeroProcessoIncompleto = numeroProcessoIncompleto();
			model = new EntityDataModel<ProcessoTrf>(ProcessoTrf.class, super.facesContext, getRetriever());
			
			List<Criteria> criterios = consultaProcessoTrfManager.obtemModelListagemProcessos(numeroSequencia, digitoVerificador, ano, numeroOrigem, 
					respectivoTribunal, isProcurador, isAdmin, 
					isMagistrado, isNumeroProcessoIncompleto);
			
			model.setCriterias(criterios);
			model.addOrder("o.dataAutuacao", Order.DESC);
			model.setGroupBy("o.idProcessoTrf");
			List<ProcessoTrf> list = consultaProcessoTrfManager.list(getSeach(criterios));

			if(list.size() == 0){
				facesMessages.add(Severity.WARN, FacesUtil.getMessage("consultaProcesso.processoNaoEncontrato"));
			}else if(list.size() > 1 && isNumeroProcessoIncompleto){
				facesMessages.add(Severity.WARN, FacesUtil.getMessage("consultaProcesso.representacaoProcessoParte"));
			}else if(list.size() == 1 && !isNumeroProcessoIncompleto && list.get(0).getSegredoJustica()){
				facesMessages.add(Severity.WARN, FacesUtil.getMessage("peticaoAvulsa.msg.processoSigiloso"), list.get(0).getNumeroProcesso());
			}
			
		} catch (Exception e) {
			facesMessages.add(Severity.ERROR, FacesUtil.getMessage("peticao.erroListagensProcessos"));
			e.printStackTrace();
		}
	}
	
	private Search getSeach(List<Criteria> criterios) throws NoSuchFieldException {
		Search search = new Search(ProcessoTrf.class);
		search.addCriteria(criterios);
		search.setDistinct(true);
		return search;
	}

	/**
	 * Verifica se o usuário tem acesso de visibilidade no processo informado
	 * 
	 * @param processoTrf a ser verificado
	 * @return <b>True</b> se tem visibilidade
	 */
	public boolean hasAcessoVisualizacaoSigiloso(ProcessoTrf processoTrf){
		return processosVisibilidadeAtribuida.contains(processoTrf.getIdProcessoTrf());
	}
	
	public boolean permiteVisualizarDadosHeader(){
		return processosVisibilidadeAtribuida.contains(idProcessoSelecionado);
	}
	
	

	/**
	 * PJEII-18556
	 * RN413, RN414 e Funcionalidade de Peticionamento avulso;
	 * O usuário somente visualiza os dados do processo sigiloso se e somente se, 
	 * ele tiver logado com o certificado e fazer parte do processo
	 *
	 * @return true se tem permissão
	 */
	public boolean hasAcessoSigiloso(){
		return isUsuarioParteProcesso;
	}
	
	/**
	 * retorna o objeto protocolarDocumentoMBean fazendo as verificações
	 * para evitar duplicidade do objeto.
	 * 
	 * @return ProtocolarDocumentoBean
	 */
	public ProtocolarDocumentoBean getProtocolarDocumentoBean() {
		if(protocolarDocumentoBean == null && idProcessoSelecionado != null){
			protocolarDocumentoBean = new ProtocolarDocumentoBean(idProcessoSelecionado, 
						ProtocolarDocumentoBean.EXIGE_DOCUMENTO_PRINCIPAL 
						| ProtocolarDocumentoBean.LANCAR_MOVIMENTACAO
						| ProtocolarDocumentoBean.PERMITIR_VINCULACAO_RESPOSTA
						| ProtocolarDocumentoBean.VINCULAR_DATA_JUNTADA, getActionName());
		}
		return protocolarDocumentoBean;
	}
	
	public void setProtocolarDocumentoBean(ProtocolarDocumentoBean protocolarDocumentoBean) {
		this.protocolarDocumentoBean = protocolarDocumentoBean;
	}
	
	
	/**
	 * Verifica a visualização do usuário em relação ao processo e monta uma URL para renderização da popup,
	 * bem como a mensagem de exibição informando que o acesso do usuário será registrado se o 
	 * mesmo não fizer parte do processo conforme resolução CNJ n.º 121.
	 * 
	 * 
	 * @param processoTrf
	 */
	public void verificaVisualizacaoProcesso(Integer idProcessoTrf){
		
		try {
			ProcessoTrf processoTrf = processoJudicialManager.findById(idProcessoTrf);
			isUsuarioParteProcesso = processoParteManager.isParte(processoTrf);
			urlPopUp = montarLink(idProcessoTrf, isAdvogado, isAssAdvogado, isJusPostulandi, isProcurador, isUsuarioParteProcesso);
			exibeMensagemResolucaoCNJ = exibeMensagemResolucaoCNJ(isUsuarioParteProcesso, isJusPostulandi);
			exibeAutosDigitais = true;
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, FacesUtil.getMessage("peticao.erroVisibilidadeProcesso"));
			e.printStackTrace();
		}
	}
	
	public void verificaVisualizacaoProcessoAutos(Integer idProcessoTrf){
		verificaVisualizacaoProcesso(idProcessoTrf);
		exibeAutosDigitais = true;
	}
	
	/**
	 * Método responsável por montar o link de acesso aos detalhes do processo.
	 * 
	 * @param idProcessoTrf Identificador do processo.
	 * @param isAssAdvogado 
	 * @param isAdvogado 
	 * @param isPostulandi
	 * @return Link de acesso aos detalhes do processo.
	 */
	public static String montarLink(Integer idProcessoTrf, boolean isAdvogado, boolean isAssAdvogado, boolean isJusPostulandi,
			boolean isProcurador, boolean isUsuarioParteProcesso) {
		StringBuilder retornoUrl = new StringBuilder();
		String chave = SecurityTokenControler.instance().gerarChaveAcessoProcesso(idProcessoTrf);
		
		if (Authenticator.getPessoaPushLogada() != null || isJusPostulandi) {
			retornoUrl.append(UrlUtil.montarLinkConsultaPublica(idProcessoTrf, chave));
		} else if (isAdvogado || isAssAdvogado || isProcurador || isUsuarioParteProcesso) {
				retornoUrl.append(UrlUtil.montarLinkDetalheProcesso(Constantes.URL_DETALHE_PROCESSO.PROCESSO_COMPLETO_ADVOGADO, idProcessoTrf, chave));
		} else {
			retornoUrl.append(UrlUtil.montarLinkDetalheProcesso(Constantes.URL_DETALHE_PROCESSO.PROCESSO_COMPLETO, idProcessoTrf, chave));
		}
		return retornoUrl.toString();
	}
	
	/**
	 * Método responsável por verificar se o usuário faz parte do do processo 
	 * 
	 * @param processoTRF
	 * @return true se o usuário logado faz parte 
	 */
	public boolean isUsuarioParteProcesso(ProcessoTrf processoTRF) {
		return processoParteManager.isParte(processoTRF, Authenticator.getPessoaLogada());
	}
	
	/**
	 * Método para verificação do número do processo 
	 * 
	 * @return <b>True</b> se o número do processo estiver incompleto.
	 */
	public boolean numeroProcessoIncompleto() {
		List<Object> composicaoNumProcessoList = new ArrayList<Object>();
		composicaoNumProcessoList.add(numeroSequencia);
		composicaoNumProcessoList.add(digitoVerificador);
		composicaoNumProcessoList.add(ano);
		composicaoNumProcessoList.add(numeroOrigem);
		composicaoNumProcessoList.add(ramoJustica);
		composicaoNumProcessoList.add(respectivoTribunal);
		
		if(composicaoNumProcessoList.contains(null) || composicaoNumProcessoList.contains("")){
			isNumeroProcessoIncompleto = true;
		}else{
			isNumeroProcessoIncompleto = false;
		}
		return isNumeroProcessoIncompleto;
	}
	
	public boolean isNumeroProcessoIncompleto() {
		return isNumeroProcessoIncompleto;
	}

	/**
	 * Limpa os campos da pesquisa de processo
	 * 
	 */
	public void limparCamposPesquisa() {
		this.numeroSequencia = null;
		this.digitoVerificador = null;
		this.ano = null;
		this.numeroOrigem = null;
  	}
	
	/**
	 * Método responsável pela lógica de exibição do alerta da resolução CNJ n.º 121 destinada ao registro
	 * da consulta de processos por terceiros
	 * 
	 * @param isUsuarioParteProcesso
	 * 
	 * @return true se a mensagem será exibida
	 */
	public boolean exibeMensagemResolucaoCNJ(boolean isUsuarioParteProcesso, boolean isJusPostulandi) {
		boolean retorno = false;
		if(!isUsuarioParteProcesso && !isJusPostulandi){
			retorno = true;
		}
		return retorno;
	}
	
	public void concluirPeticionamento() {
		try {
			this.documentoJudicialService.gravarAssinaturaDeProcessoDocumento(this.protocolarDocumentoBean.getArquivosAssinados(), this.protocolarDocumentoBean.getProcessoDocumentosParaAssinatura());			
		
			boolean resultado = getProtocolarDocumentoBean().concluir();
			
			if (resultado == false) {
				throw new Exception("Não foi possível concluir o protocolo do documento!");
			}
			else {
				this.facesMessages.add(Severity.INFO, "O peticionamento foi concluído com sucesso");
			}
		}
		catch (Exception e) {
			
			this.protocolarDocumentoBean.setArquivosAssinados(new ArrayList<ArquivoAssinadoHash>());
				
			try {
				Transaction.instance().rollback();
			} 
			catch (Exception e1) {
				e1.printStackTrace();
			}

			this.facesMessages.clear();
			this.facesMessages.add(Severity.ERROR, e.getMessage());		
		}			
	}	
	
	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getDigitoVerificador() {
		return digitoVerificador;
	}

	public void setDigitoVerificador(Integer digitoVerificador) {
		this.digitoVerificador = digitoVerificador;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	public String getRamoJustica() {
		return ramoJustica;
	}

	public void setRamoJustica(String ramoJustica) {
		this.ramoJustica = ramoJustica;
	}

	public String getRespectivoTribunal() {
		return respectivoTribunal;
	}

	public void setRespectivoTribunal(String respectivoTribunal) {
		this.respectivoTribunal = respectivoTribunal;
	}

	@Override
	protected BaseManager<ProcessoTrf> getManager() {
		return processoJudicialManager;
	}

	@Override
	public EntityDataModel<ProcessoTrf> getModel() {
		return this.model;
	}
	
	public Integer getIdProcessoSelecionado() {
		return idProcessoSelecionado;
	}

	public void setIdProcessoSelecionado(Integer idProcessoSelecionado) {
		this.idProcessoSelecionado = idProcessoSelecionado;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public boolean isAdvogado() {
		return isAdvogado;
	}
	
	public boolean isAssAdvogado() {
		return isAssAdvogado;
	}

	public boolean isJusPostulandi() {
		return isJusPostulandi;
	}

	public boolean isProcurador() {
		return isProcurador;
	}

	public boolean isMagistrado() {
		return isMagistrado;
	}

	public String getUrlPopUp() {
		return urlPopUp;
	}

	public void setUrlPopUp(String urlPopUp) {
		this.urlPopUp = urlPopUp;
	}

	public boolean isExibeMensagemResolucaoCNJ() {
		return exibeMensagemResolucaoCNJ;
	}

	public boolean isExibeAutosDigitais() {
		return exibeAutosDigitais;
	}

	public void setExibeAutosDigitais(boolean exibeAutosDigitais) {
		this.exibeAutosDigitais = exibeAutosDigitais;
	}

	@Override
	public void doUploadArquivoAssinado(HttpServletRequest servletRequest,
			ArquivoAssinadoHash arquivoAssinadoHash) throws Exception {
		
		this.protocolarDocumentoBean.addArquivoAssinado(arquivoAssinadoHash);
		
	}

	@Override
	public String getActionName() {
		return NAME;
	}
	
	
}
