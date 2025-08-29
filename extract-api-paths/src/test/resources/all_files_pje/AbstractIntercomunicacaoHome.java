package br.com.infox.cliente.home;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.SelectItem;
import javax.transaction.SystemException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.transaction.Transaction;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.Util;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.ElementoDominioHome;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.component.AbstractHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.entidades.vo.StatusEnvioManifestacaoProcessualVO;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ConsultarProcessoRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorService;
import br.jus.cnj.pje.intercomunicacao.service.MNIMediatorServiceAbstract;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.nucleo.CodigoMovimentoNacional;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeRuntimeException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.EnderecoWsdlManager;
import br.jus.cnj.pje.nucleo.manager.ProcessInstanceUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfManifestacaoProcessualManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.util.JsonHelper;
import br.jus.cnj.pje.ws.client.ConsultaPJeClient;
import br.jus.pje.jt.entidades.RemessaRecebimento;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfManifestacaoProcessual;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.entidades.lancadormovimento.ElementoDominio;
import br.jus.pje.nucleo.enums.CriticidadeAlertaEnum;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.Crypto.Type;
import br.jus.pje.nucleo.util.StringUtil;

public abstract class AbstractIntercomunicacaoHome extends AbstractHome<ProcessoTrf> {
	private static final long serialVersionUID = 1L;
	private int idMotivoRemessa;
	private ElementoDominio motivoRemessa;
	private List<ProcessoDocumento>	documentoNaoAssinadoList = new ArrayList<ProcessoDocumento>();

	public static final String		VAR_INTERCOMUNICACAO_HASH_PROCESSO = "intercomunicacao:hashProcesso";
	public static final String 		VAR_INTERCOMUNICACAO_ENDERECO_WSDL = "intercomunicacao:enderecoWsdl";
	public static final String		VAR_INTERCOMUNICACAO_CONTROLE_ENVIO	= "intercomunicacao:envio:idProcessInstance:";
	public static final String 		VAR_INTERCOMUNICACAO_PROCESSO = "intercomunicacao:processoTrf";
	public static final String 		VAR_INTERCOMUNICACAO_MOTIVO_REMESSA = "intercomunicacao:motivoRemessa";
	public static final String 		VAR_INTERCOMUNICACAO_PARAMETRO_MANIFESTACAO = "intercomunicacao:parametrosManifestacao";
	public static final String FILTRO_RECURSAIS = "recursais";
	public static final String FILTRO_ORIGINARIAS = "originarias";
	public static final String FILTRO_TODAS = "todas";
	public static final String FILTRO_CODIGOS_ESPECIFICOS = "codigos_especificos";
	
	private EnderecoWsdl enderecoWsdl;
    private Collection<EnderecoWsdl> colecaoEnderecoWsdl;
    
    @In (required = false)
    @Out (required = false)
    private ConsultaPJeClient consultaPJeClient;
    
    private String transicaoSaida;
    
    private Logger log = Logger.getLogger(AbstractIntercomunicacaoHome.class);
	
    @In (scope = ScopeType.SESSION, required = false)
	@Out (scope = ScopeType.SESSION, required = false)
	private Boolean loginDeServico = Boolean.TRUE;
    
    @In (required = false)
    @Out (required = false)
    private UsuarioLogin usuarioLogin;
    
	@In
	private ProcessoDocumentoManager processoDocumentoManager;
	
    @Override
    public void create() {
    	ParametroUtil.instance().getEnderecoWsdlAplicacaoOrigem();
    	ParametroUtil.instance().getEnderecoWsdlIntegracao();
    	
    	setTransicaoSaida(TaskInstanceUtil.instance().getVariableFrameDefaultLeavingTransition());
    	consultarProcessoTrf();
    }
    
	public List<ProcessoDocumento> getDocumentoNaoAssinadoList() {
		return documentoNaoAssinadoList;
	}

	public void setDocumentoNaoAssinadoList(List<ProcessoDocumento> documentoNaoAssinadoList) {
		this.documentoNaoAssinadoList = documentoNaoAssinadoList;
	}

	/**
	 * Método refatorado para envio de processo do Inferior para Superior e vice-versa.
	 * 
	 * @author Thiago Oliveira/Guilherme Bispo
	 * @since 1.4.5
	 * @param processo
	 * @param enderecoWsdl
	 * @param qNameSpaceURI
	 * @param qLocalPort
	 * @param alerta
	 * @throws Exception
	 */
	public ManifestacaoProcessualRespostaDTO enviarProcesso(ProcessoTrf processo, EnderecoWsdl enderecoWsdl, String alerta, String paramRemessaRetorno) throws Exception {
		if (isEnvioManifestacaoProcessualEmAndamento()) { 
			throw new Exception("O envio da Manifestação Processual encontra-se em andamento, favor aguardar o fim do processamento.");
		} else {
			iniciarEnvioManifestacaoProcessual();
		}

		String url = enderecoWsdl.getWsdlIntercomunicacao();
		MNIMediatorService mediator = MNIMediatorServiceAbstract.instance(enderecoWsdl);

		ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual = getManifestacaoProcessual();
		String numeroString = recuperarNumeroProcessoOuReferencia(processo, paramRemessaRetorno);
		atribuirNumeroProcessoDaInstanciaDeOrigem(enderecoWsdl, mediator, manifestacaoProcessual, numeroString, paramRemessaRetorno);
		
		 String codigoMovimento = CodigoMovimentoNacional.CODIGO_MOVIMENTO_COMUNICACAO_REMESSA;
         RemessaRecebimento remessaRecebimento = null;

         if(codigoMovimento != null) {
                 ElementoDominioHome edh = ElementoDominioHome.instance();
                 remessaRecebimento = edh.getRemessaRecebimentoByIdRemessa(Long.valueOf(idMotivoRemessa));

                 if(remessaRecebimento != null && remessaRecebimento.getElementoRecebimento() != null) {
                         manifestacaoProcessual.addParametro(MNIParametro.PARAM_COMPLEMENTO_MOVIMENTO, remessaRecebimento.getElementoRecebimento().getCodigoGlossario());
                 }
         }

        manifestacaoProcessual.addParametro(MNIParametro.PARAM_URL_ORIGEM_ENVIO, enderecoWsdl.getWsdlIntercomunicacao());
        manifestacaoProcessual.addParametro(MNIParametro.PARAM_URL_ORIGEM_CONSULTA, enderecoWsdl.getWsdlConsulta());

 		UsuarioLogin login = getUsuarioLogin();
 		manifestacaoProcessual.setLogin(login.getLogin());
 		manifestacaoProcessual.setSenha(login.getSenha());

		ManifestacaoProcessualRespostaDTO resposta = mediator.entregarManifestacaoProcessual(manifestacaoProcessual);
		StatusEnvioManifestacaoProcessualVO status = getStatusEnvioManifestacaoProcessualVO(false);
		status.setRespostaManifestacaoProcessual(resposta);
		setStatusEnvioManifestacaoProcessualVO(status);
		
		if (!resposta.getSucesso()) {
			finalizarEnvioManifestacaoProcessual();
			throw new Exception(resposta.getMensagem());
		} else {
			posEnviarProcesso(processo, paramRemessaRetorno, resposta.getNumeroProcesso(), alerta, enderecoWsdl);
			finalizarEnvioManifestacaoProcessual();
		}
		
		return resposta;
	}
	
	public void posEnviarProcesso(ProcessoTrf processo, String paramRemessaRetorno, String numeroProcessoDestino, String mensagemAlerta, EnderecoWsdl enderecoWsdl) {
	   getEntityManager().clear();
	   if (getTransicaoSaida() != null && !getTransicaoSaida().isEmpty() && TaskInstanceHome.instance() != null) {
	      if (paramRemessaRetorno.equalsIgnoreCase("remessa")) {
	         deletar();
	      }
	      TaskInstanceHome.instance().end(getTransicaoSaida());
	   }
	   processo = this.getProcessoTrfManager().find(ProcessoTrf.class, processo.getIdProcessoTrf());
	   numeroProcessoDestino = NumeroProcessoUtil.mascaraNumeroProcesso(numeroProcessoDestino);
	   
	   AlertaHome.instance().inserirAlerta(processo, mensagemAlerta, CriticidadeAlertaEnum.I);
	   getProcessoTrfManager().atualizaStatusRemetidoOutraInstancia(processo.getIdProcessoTrf());
	   salvarProcessoTrfManifestacao(processo, enderecoWsdl, numeroProcessoDestino);
	   ComponentUtil.getProcessoJudicialService().gravarVariavelTarefaTodasTarefas(
	         processo, Variaveis.PJE_FLUXO_MNI_AGUARDA_REMESSA, true);
	   EntityUtil.getEntityManager().flush();
	}



	
	/**
	 * Método executado após a entrega de manifestação.
	 */
	public abstract void posEnviarProcesso();
	protected abstract ManifestacaoProcessualRequisicaoDTO getManifestacaoProcessual();
	
	public int getIdMotivoRemessa() {
		return idMotivoRemessa;
	}

	public void setIdMotivoRemessa(int idMotivoRemessa) {
		this.idMotivoRemessa = idMotivoRemessa;

		if (idMotivoRemessa > 0) {
			motivoRemessa = getEntityManager().find(ElementoDominio.class, Long.valueOf(idMotivoRemessa));
		}
	}

	// outject para referencia em fluxo (configuração de eventos para gerar movimentação)
	@Out(required = false)
	public ElementoDominio getMotivoRemessa() {
		return motivoRemessa;
	}

	public void setMotivoRemessa(ElementoDominio motivoRemessa) {
		this.motivoRemessa = motivoRemessa;
	}

	public ArrayList<SelectItem> getListaMotivosRemessa() {

		String variavelFluxo = (String) TaskInstanceUtil.instance().getVariable("comboMotivoRemessa");

		List<SelectItem> listaMotivosRemessa = new ArrayList<SelectItem>();

		if (variavelFluxo != null) {
			String dominio = variavelFluxo.split(";")[0];
			String listaElementos = variavelFluxo.split(";")[1];

			List<ElementoDominio> list = ElementoDominioHome.instance().getListaElementosPorDominio(dominio, listaElementos);

			SelectItem selectItem;
			if (list.size() > 1) {
				selectItem = new SelectItem(-1, "Selecione...");
				listaMotivosRemessa.add(selectItem);
			}

			for (ElementoDominio elemento : list) {
				selectItem = new SelectItem(elemento.getIdElementoDominio(), elemento.getValor());
				listaMotivosRemessa.add(selectItem);
			}
		}

		return (ArrayList<SelectItem>) listaMotivosRemessa;
	}

	public Boolean validaMotivoRemessa() {
		if (this.idMotivoRemessa > 0)
			return true;
		return false;
	}

	/**
	 * Gera o hash do XML da ManifestacaoProcessual
	 * 
	 * @param manifestacaoProcessual
	 * @return
	 * @throws Exception
	 */
	private String hash(ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual) throws Exception {
		return Crypto.encode(SerializationUtils.serialize(manifestacaoProcessual), Type.SHA1);
	}

	private String geraHashProcessoTrf(ProcessoTrf processoTrf) throws Exception {
		EnderecoWsdl wsdl = getEnderecoWsdl();
		String login = (wsdl != null ? wsdl.getLogin() : null);
		String senha = (wsdl != null ? wsdl.getSenha() : null);
		
		ConsultaPJeClient client = null;
		if(enderecoWsdl != null) {
			client = new ConsultaPJeClient(enderecoWsdl);
		} 
		List<String> listaIdCarregarBinario = new ArrayList<String>();
		listaIdCarregarBinario.add("*");
		ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual = new ManifestacaoProcessualRequisicaoDTO();
		manifestacaoProcessual.setProcessoTrf(processoTrf);
		
		return hash(manifestacaoProcessual);
	}

	protected void gravaVariavelHashProcessoTrf(ProcessoTrf processoTrf) throws Exception {
		setVariavelHashProcesso(geraHashProcessoTrf(processoTrf));
		setVariavelEnderecoWsdl(getEnderecoWsdl());
	}

	public void verificaIntegridadeRemessa() throws Exception {
		org.jbpm.graph.exe.ProcessInstance processInstance = org.jboss.seam.bpm.ProcessInstance.instance();
		String hashProcessoCriacao = (String) processInstance.getContextInstance().getVariable(VAR_INTERCOMUNICACAO_HASH_PROCESSO + processInstance.getId());

		String hashProcessoEnvio = geraHashProcessoTrf(ProcessoTrfHome.instance().getInstance());

		/*
		 * if(hashProcessoCriacao != null && !hashProcessoCriacao.equals(hashProcessoEnvio)){ throw new
		 * Exception("O processo a ser remetido mudou, Favor recadastrar a remessa!"); }
		 */
	}

	@Override
	public Class<ProcessoTrf> getEntityClass() {
		return ProcessoTrf.class;
	}

	protected void deletar() {

	}
	
	/**
	 * Método que retorna o número processo originário na remessa para a instância superior
	 * ou o número do processo referência no retorno à instância inferior.
	 * 
	 * @param processo
	 * @param paramRemessaRetorno
	 * @return numeroString
	 */
	private String recuperarNumeroProcessoOuReferencia(ProcessoTrf processo, String paramRemessaRetorno) {
	    String numeroString = processo.getNumeroProcesso();
	   
	    if(paramRemessaRetorno.equals(MNIParametro.PARAM_RETORNO)){
	        if(processo.getProcessoReferencia() != null){
	            numeroString = processo.getProcessoReferencia().getNumeroProcesso();
	        } else if (processo.getDesProcReferencia() != null){
	            numeroString = processo.getDesProcReferencia();
	        }
	    }
	    return numeroString;
	}
	
	/**
	 * Registra o início do envio da manifestação processual para posterior controle.
	 */
	public void iniciarEnvioManifestacaoProcessual() {
		StatusEnvioManifestacaoProcessualVO status = getStatusEnvioManifestacaoProcessualVO(true);
		status.setDataEnvio(new Date());
		setStatusEnvioManifestacaoProcessualVO(status);
	}
	
	/**
	 * Finaliza o envio da manifestação processual.
	 */
	public void finalizarEnvioManifestacaoProcessual() {
		setStatusEnvioManifestacaoProcessualVO(null);
	}
	
	/**
	 * @return True se a requisição está em andamento.
	 */
	public Boolean isEnvioManifestacaoProcessualEmAndamento() {
		return (getStatusEnvioManifestacaoProcessualVO(false) != null);
	}
	
	/**
	 * @return True se a requisição está em andamento há mais de 5 minutos.
	 */
	public Boolean isEnvioManifestacaoProcessualEmAndamentoHaMaisDe5Minutos() {
		Boolean resultado = Boolean.FALSE;
		
		if (isEnvioManifestacaoProcessualEmAndamento()) {
			StatusEnvioManifestacaoProcessualVO status = getStatusEnvioManifestacaoProcessualVO(false);
			Date dataEnvio = status.getDataEnvio();
			Date dataAtual = new Date();
			long diferencaEmMilisegundos = (dataEnvio != null ? dataAtual.getTime() - dataEnvio.getTime() : 0);  
			long diferencaEmMinutos = diferencaEmMilisegundos / 1000 / 60;
			resultado = (diferencaEmMinutos > 5);
		}
		return resultado;
	}
	
	/**
	 * @return True se a requisição foi finalizada com erro.
	 */
	public Boolean isRequisicaoFinalizadaComErro() {
		StatusEnvioManifestacaoProcessualVO status = getStatusEnvioManifestacaoProcessualVO(false);
		return status != null && status.isRequisicaoFinalizadaComErro();
	}

	/**
	 * Atribui o StatusEnvioManifestacaoProcessualVO no escopo de Processo e de Aplicação para fazer
	 * o controle de requisições duplicadas.
	 * 
	 * @param status
	 */
	protected void setStatusEnvioManifestacaoProcessualVO(StatusEnvioManifestacaoProcessualVO status) {
		String variavel = getVariavelIntercomunicacaoControleEnvio();
		if (status != null) {
			FacesUtil.setApplicationAttribute(variavel, status);
			ProcessInstanceUtil.instance().setVariable(variavel, status);
		} else {
			FacesUtil.removeApplicationAttribute(variavel);
			ProcessInstanceUtil.instance().removeVariable(variavel);
		}
	}
	
	/**
	 * @param criar True se for para criar caso a variável não exista.
	 * @return Status de envio da Manifestação Processual.
	 */
	protected StatusEnvioManifestacaoProcessualVO getStatusEnvioManifestacaoProcessualVO(Boolean criar) {
		String variavel = getVariavelIntercomunicacaoControleEnvio();
		StatusEnvioManifestacaoProcessualVO status = FacesUtil.getApplicationAttribute(variavel);
		
		if (status == null) {
			status = ProcessInstanceUtil.instance().getVariable(variavel);
		}
		
		if (status == null && criar) {
			status = new StatusEnvioManifestacaoProcessualVO();
			ProcessInstanceUtil.instance().setVariable(variavel, status);
			FacesUtil.setApplicationAttribute(variavel, status);
		}
		return status;
	}
	
	/**
	 * @return Nome da variável de controle de envio de Manifestação Processual.
	 */
	protected String getVariavelIntercomunicacaoControleEnvio() {
		ProcessInstance pi = org.jboss.seam.bpm.ProcessInstance.instance();
		return VAR_INTERCOMUNICACAO_CONTROLE_ENVIO + pi.getId();
	}
	
	/**
	 * Atribui o número do processo na manifestacaoProcessual caso seja uma retorno avulso ou remessa.
	 * 
	 * @param port
	 * @param manifestacaoProcessual
	 * @param numeroString
	 * @param paramRemessaRetorno
	 * @throws Exception
	 */
	protected void atribuirNumeroProcessoDaInstanciaDeOrigem(
			EnderecoWsdl wsdl,
			MNIMediatorService port, 
			ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual,
			String numeroString, 
			String paramRemessaRetorno) throws Exception {

		ConsultarProcessoRequisicaoDTO consulta = new ConsultarProcessoRequisicaoDTO();
		consulta.setNumeroProcesso(numeroString);
		consulta.setIncluirCabecalho(Boolean.TRUE);
		
		ConsultarProcessoRespostaDTO respostaConsulta = port.consultarProcesso(consulta);
		// Se o processo existir na outra instância então o envio será avulso.
		if (respostaConsulta.getSucesso()) {
			manifestacaoProcessual.setNumeroProcesso(numeroString);
			String codigoPeticao = ParametroUtil.instance().getTipoProcessoDocumentoPeticaoInicial().getCodigoDocumento();
			manifestacaoProcessual.getProcessoDocumentoList();
		} else {
			manifestacaoProcessual.getProcessoTrf().getProcesso().setNumeroProcesso(numeroString);
		}
		
		//se for retorno de processo e o processo não existir na origem então lançar o erro.
		if (paramRemessaRetorno.equalsIgnoreCase("retorno") && !respostaConsulta.getSucesso()) {
			//se a mensagem for de processo não encontrado é porque o processo não veio da instância inferior, caso contrário
			//ocorreu um erro qualquer e a exceção será lançada.
			String mensagem = respostaConsulta.getMensagem();
			if (StringUtils.endsWith(mensagem, "não encontrado!")) {
				mensagem = "Não é possível retornar um processo originário, ou seja, o processo '%s' não foi encontrado na instância de origem.";
				mensagem = String.format(mensagem, numeroString);
			}
			throw new Exception(mensagem);
		}
	}

	/**
	 * @return enderecoWsdl.
	 */
	public EnderecoWsdl getEnderecoWsdl() {
		return enderecoWsdl;
	}

	/**
	 * @param enderecoWsdl enderecoWsdl.
	 */
	public void setEnderecoWsdl(EnderecoWsdl enderecoWsdl) {
		this.enderecoWsdl = enderecoWsdl;
		if (enderecoWsdl != null) {
			getUsuarioLogin().setLogin(enderecoWsdl.getLogin());
			getUsuarioLogin().setSenha(enderecoWsdl.getSenha());
		}
	}

	/**
	 * @return colecaoEnderecoWsdl.
	 */
	public Collection<EnderecoWsdl> getColecaoEnderecoWsdl() {
		if (colecaoEnderecoWsdl == null) {
			colecaoEnderecoWsdl = new ArrayList<EnderecoWsdl>();
		}
		return colecaoEnderecoWsdl;
	}

	/**
	 * @param colecaoEnderecoWsdl colecaoEnderecoWsdl.
	 */
	public void setColecaoEnderecoWsdl(Collection<EnderecoWsdl> colecaoEnderecoWsdl) {
		this.colecaoEnderecoWsdl = colecaoEnderecoWsdl;
	}
	
	/**
	 * @return the consultaPJeClient
	 */
	protected ConsultaPJeClient getConsultaPJeClient() {
		if (consultaPJeClient == null) {
			if (getEnderecoWsdl() != null) {
				consultaPJeClient = new ConsultaPJeClient(getEnderecoWsdl());
			} else {
				String mensagem = "Não foi possível consultar os dados iniciais em razão de não haver um Endereço WSDL definido para a remessa.";
				throw new PJeRuntimeException(mensagem);
			}
		}
		return consultaPJeClient;
	}

	/**
	 * @param consultaPJeClient the consultaPJeClient to set
	 */
	protected void setConsultaPJeClient(ConsultaPJeClient consultaPJeClient) {
		this.consultaPJeClient = consultaPJeClient;
	}
	
	/**
	 * Consulta os endereços WSDL cadastrados na aplicação.
	 */
	protected void consultarColecaoEnderecoWsdlExcetoInstanciaAtual() {
		EnderecoWsdlManager manager = EnderecoWsdlManager.instance();
		Collection<EnderecoWsdl> lista = manager.consultarEnderecosExceto(ParametroUtil.instance().getInstancia());
		String codigosPermitidos = (String) TaskInstanceUtil.instance().getVariable(Variaveis.PJE_FLUXO_MNI_INSTANCIA_CODIGOS_PERMITIDOS);
		
		if (codigosPermitidos != null) {
			Predicate filtro = criarFiltroInstancia(codigosPermitidos);
			CollectionUtils.filter(lista, filtro);
		}
		
		setColecaoEnderecoWsdl(lista);
	}
	
	/**
	 * Consulta o ProcessoTrf e atribui à instância da classe.
	 */
	protected void consultarProcessoTrf() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		Processo processo = processoTrf.getProcesso();
		setInstance(getProcessoTrfManager().getProcessoTrfByProcesso(processo));
		
		getInstance().getProcesso().getProcessoDocumentoList().get(0).getProcessoDocumento();
		Hibernate.initialize(getInstance().getPrioridadeProcessoList());
		Hibernate.initialize(getInstance().getProcessoParteList());
		Hibernate.initialize(getInstance().getProcessoAssuntoList());
		Hibernate.initialize(getInstance().getComplementoJE());
		Hibernate.initialize(getInstance().getAssuntoTrfList());
		Hibernate.initialize(getInstance().getOrgaoJulgador());
		Hibernate.initialize(getInstance().getOrgaoJulgadorCargo());
		Hibernate.initialize(getInstance().getOrgaoJulgadorColegiado());
		Hibernate.initialize(getInstance().getOrgaoJulgadorRevisor());
		
		getInstance().getProcessoAssuntoList().clear();
		EntityUtil.evict(getInstance());
	}

	/**
	 * @return transicaoSaida.
	 */
	public String getTransicaoSaida() {
		return transicaoSaida;
	}

	/**
	 * @param transicaoSaida transicaoSaida.
	 */
	public void setTransicaoSaida(String transicaoSaida) {
		this.transicaoSaida = transicaoSaida;
	}
	
	/**
	 * Salva a referência do processo remetido/retornado de outra instância.
	 * 
	 * @param processo
	 * @param enderecoWsdl
	 * @param numeroProcessoDestino
	 */
	protected void salvarProcessoTrfManifestacao(ProcessoTrf processo, EnderecoWsdl enderecoWsdl,
			String numeroProcessoDestino) {
		
		try {
			enderecoWsdl = EntityUtil.refreshEntity(enderecoWsdl);
			enderecoWsdl = getEnderecoWsdlManager().persist(enderecoWsdl);
			
			ProcessoTrfManifestacaoProcessual processoManifestacao = new ProcessoTrfManifestacaoProcessual();
			processoManifestacao.setEnderecoWsdl(enderecoWsdl);
			processoManifestacao.setProcessoTrf(processo);
			processoManifestacao.setNumeroProcessoManifestacao(numeroProcessoDestino);
			
			getProcessoTrfManifestacaoProcessualManager().persist(processoManifestacao);
		} catch (PJeBusinessException e) {
			throw new AplicationException(e);
		}
	}

	/**
	 * @return getProcessInstance().
	 */
	protected ProcessInstance getProcessInstance() {
		return org.jboss.seam.bpm.ProcessInstance.instance();
	}
	
	/**
	 * @return variável ProcessoTrf
	 */
	protected ProcessoTrf getVariavelProcessoTrf() {
		String nome = VAR_INTERCOMUNICACAO_PROCESSO + getProcessInstance().getId();
		return ProcessInstanceUtil.instance().getVariable(nome);
	}
	
	/**
	 * @param processoTrf ProcessoTrf
	 */
	protected void setVariavelProcessoTrf(ProcessoTrf processoTrf) {
		String nome = VAR_INTERCOMUNICACAO_PROCESSO + getProcessInstance().getId();
		if (processoTrf == null) {
			ProcessInstanceUtil.instance().deleteVariable(nome);
		} else {
			ProcessInstanceUtil.instance().setVariable(nome, processoTrf);
		}
	}
	
	/**
	 * @return variável EnderecoWsdl
	 */
	protected EnderecoWsdl getVariavelEnderecoWsdl() {
		String nome = VAR_INTERCOMUNICACAO_ENDERECO_WSDL + getProcessInstance().getId();
		return ProcessInstanceUtil.instance().getVariable(nome);
	}
	
	/**
	 * @param enderecoWsdl EnderecoWsdl
	 */
	protected void setVariavelEnderecoWsdl(EnderecoWsdl enderecoWsdl) {
		String nome = VAR_INTERCOMUNICACAO_ENDERECO_WSDL + getProcessInstance().getId();
		if (enderecoWsdl == null) {
			ProcessInstanceUtil.instance().deleteVariable(nome);
		} else {
			ProcessInstanceUtil.instance().setVariable(nome, enderecoWsdl);
		}
	}
	
	/**
	 * @return variável motivo da remessa.
	 */
	protected Integer getVariavelMotivoRemessa() {
		String nome = VAR_INTERCOMUNICACAO_MOTIVO_REMESSA + getProcessInstance().getId();
		return ProcessInstanceUtil.instance().getVariable(nome);
	}
	
	/**
	 * @param motivo Integer
	 */
	protected void setVariavelMotivoRemessa(Integer motivo) {
		String nome = VAR_INTERCOMUNICACAO_MOTIVO_REMESSA + getProcessInstance().getId();
		
		if (motivo == null || motivo == -1) {
			ProcessInstanceUtil.instance().deleteVariable(nome);
		} else {
			ProcessInstanceUtil.instance().setVariable(nome, motivo);
		}
	}
	
	/**
	 * @return variavelHashProcesso.
	 */
	protected String getVariavelHashProcesso() {
		String nome = VAR_INTERCOMUNICACAO_HASH_PROCESSO + getProcessInstance().getId();
		return ProcessInstanceUtil.instance().getVariable(nome);
	}

	/**
	 * @param hash hash.
	 */
	protected void setVariavelHashProcesso(String hash) {
		String nome = VAR_INTERCOMUNICACAO_HASH_PROCESSO + getProcessInstance().getId();
		
		if (hash == null) {
			ProcessInstanceUtil.instance().deleteVariable(nome);
		} else {
			ProcessInstanceUtil.instance().setVariable(nome, hash);
		}
	}
	
	/**
	 * @return ProcessoTrfManager
	 */
	protected ProcessoTrfManager getProcessoTrfManager() {
		return ProcessoTrfManager.instance();
	}
	
	/**
	 * @return ProcessoTrfManifestacaoProcessualManager
	 */
	protected ProcessoTrfManifestacaoProcessualManager getProcessoTrfManifestacaoProcessualManager() {
		return ProcessoTrfManifestacaoProcessualManager.instance();
	}
	
	/**
	 * @return EnderecoWsdlManager
	 */
	protected EnderecoWsdlManager getEnderecoWsdlManager() {
		return EnderecoWsdlManager.instance();
	}

	/**
	 * Exibe os campos de login/senha para que o usuário entre com suas credenciais da requisição MNI.
	 */
	public void atribuirLoginUsuario() {
		EnderecoWsdl wsdl = getEnderecoWsdl();
		if (wsdl != null) {
			setUsuarioLogin(null);
			
			setLoginDeServico(Boolean.FALSE);
		}
	}
	
	/**
	 * Oculta os campos de login/senha e faz uso do login de serviço para a requisição MNI.
	 */
	public void atribuirLoginAdministrador() {
		EnderecoWsdl wsdl = getEnderecoWsdl();
		if (wsdl != null) {
			EntityUtil.evict(wsdl);
			wsdl = EntityUtil.refreshEntity(wsdl);
			setEnderecoWsdl(wsdl);
			
			setLoginDeServico(Boolean.TRUE);
		}
	}
	
	/**
	 * Verifica se as credenciais (login/senha) informadas fazem a conexão com o endpoint.
	 */
	public void verificarAcessoEndpoint() {
		EnderecoWsdlHome.instance().verificarAcessoEndpoint(
				getEnderecoWsdl(), 
				getUsuarioLogin());
	}
	
	/**
	 * @return loginDeServico.
	 */
	public Boolean getLoginDeServico() {
		if (loginDeServico == null) {
			loginDeServico = Boolean.TRUE;
		}
		return loginDeServico;
	}

	/**
	 * @param loginDeServico Atribui loginDeServico.
	 */
	public void setLoginDeServico(Boolean loginDeServico) {
		this.loginDeServico = loginDeServico;
	}
	
	/**
	 * Conforme parâmetro seta o tempo na transação da remessa.
	 * @throws SystemException
	 */
	protected void setarTimeoutRemessa() throws SystemException {
		ParametroService parametroService = (ParametroService) Component.getInstance("parametroService");
		String parametroTimeout = parametroService.valueOf(Parametros.TIMEOUT_REMESSA);
	    int timeout = 0;
	    
		if (!StringUtil.isEmpty(parametroTimeout)) {
			try {
				timeout = Integer.parseInt(parametroTimeout);
				
				if (timeout != 0) {
					Transaction.instance().setTransactionTimeout(timeout);
				}
			} catch (Exception e) {
				String mensagem = MessageFormat.format("O parametro {0} está com formato inválido: {1}",Parametros.TIMEOUT_REMESSA,parametroTimeout);
				log.error(mensagem);
			}
		}
	}

	/**
	 * @return usuarioLogin.
	 */
	public UsuarioLogin getUsuarioLogin() {
		if (usuarioLogin == null) {
			usuarioLogin = new UsuarioLogin();
		}
		return usuarioLogin;
	}

	/**
	 * @param usuarioLogin Atribui usuarioLogin.
	 */
	public void setUsuarioLogin(UsuarioLogin usuarioLogin) {
		this.usuarioLogin = usuarioLogin;
	}

	/**
	 * Adiciona a variável de tarefa de nome 'varParametroEntregarManifestacaoProcessual' na manifestação processual.
	 * O valor da variável deve estar no formato JSON, os atributos serão incluídos individualmente nos parâmetros da
	 * manifestação.<br/>
	 * Ex: 
	 * #{taskInstanceUtil.setVariable("intercomunicacao:parametrosManifestacao", "{timeout:100, maxOps:50, AceitaDependente:true, nome:'Fulano'}")}<br/>
	 * A variável será convertida conforme exemplo abaixo:
	 * <pre>
	 * {@code
	 * <parametros nome="timeout" valor="100"/>
	 * <parametros nome="maxOps" valor="50"/>
	 * <parametros nome="AceitaDependente" valor="TRUE"/>
	 * <parametros nome="nome" valor="Fulano"/>
	 * }
	 * </pre>
	 * @param manifestacaoProcessual ManifestacaoProcessual
	 */
	protected void atribuirVariaveisTarefaNaManifestacao(ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual) {
		Object variavel = TaskInstanceUtil.instance().getVariable(VAR_INTERCOMUNICACAO_PARAMETRO_MANIFESTACAO);
		if (variavel != null && variavel instanceof String) {
			Map<String, Object> mapa = JsonHelper.converterParaMap((String) variavel);
			Set<String> chaves = mapa.keySet();
			for (String chave : chaves) {
				String valor = String.valueOf(mapa.get(chave));

				manifestacaoProcessual.addParametro(chave, valor);
			}
			
		}
	}
	
	protected List<ProcessoDocumento> getDocumentosNaoAssinadosUsuarioInterno(ProcessoTrf processo) throws PJeBusinessException {
		return processoDocumentoManager.getDocumentosNaoAssinadosUsuarioInterno(processo.getProcesso().getIdProcesso());
	}
	
	/** 
	 * Verifica se existem expedientes em aberto do processo 
	 */
	protected StringBuilder validarExpedientes(ProcessoTrf processo) throws PJeBusinessException{
		ProcessoParteExpedienteManager ppem = ComponentUtil.getComponent(ProcessoParteExpedienteManager.NAME);
		StringBuilder erro = new StringBuilder(); 
		if(ppem.getQuantidadeExpedientesAbertosPorProcesso(processo)>0) {
			erro.append("O processo possui expedientes com prazo em aberto. Remessa cancelada.");
		}

		return erro;
	}
	
	/**
	 * Método responsável por criar um filtro para o campo "Instância", o qual filtrará os objetos a partir dos códigos permitidos especificados
	 * @param codigosPermitidos Lista de códigos permitidos, separados por vírgula
	 * @return Filtro para o campo "Instãncia"
	 */
	protected Predicate criarFiltroInstancia(final String codigosPermitidos) {
		return new Predicate() {
			@Override
			public boolean evaluate(Object objeto) {
				EnderecoWsdl enderecoWsdl = (EnderecoWsdl) objeto;
				return (enderecoWsdl != null && Util.listaContem(codigosPermitidos, String.valueOf(enderecoWsdl.getIdEnderecoWsdl())));
			}
		};
	}
	
	/**
	 * Método responsável por criar um filtro para o campo "Jurisdição", o qual filtrará os objetos a partir dos códigos permitidos especificados
	 * @param codigosPermitidos Lista de códigos permitidos, separados por vírgula
	 * @return Filtro para o campo "Jurisdição"
	 */
	protected Predicate criarFiltroJurisdicao(final String codigosPermitidos) {
		return new Predicate() {
			@Override
			public boolean evaluate(Object objeto) {
				br.jus.cnj.pje.ws.Jurisdicao jurisdicao = (br.jus.cnj.pje.ws.Jurisdicao) objeto;
				return (jurisdicao != null && Util.listaContem(codigosPermitidos, String.valueOf(jurisdicao.getId())));
			}
		};
	}
	
	/**
	 * Método responsável por criar um filtro para o campo "ClasseJudicial", o qual filtrará os objetos a partir dos códigos permitidos especificados
	 * @param filtroUtilizado Filtro a ser utilizado. Valores possíveis: RECURSAIS, ORIGINARIAS, TODAS ou CODIGOS_ESPECIFICOS
	 * @param codigosPermitidos Lista de códigos permitidos, separados por vírgula
	 * @return Filtro para o campo "Instãncia"
	 */
	protected Predicate criarFiltroClasseJudicialRecursal(final String filtroUtilizado, final String codigosPermitidos) {
		return new Predicate() {
			@Override
			public boolean evaluate(Object objeto) {
				br.jus.cnj.pje.ws.ClasseJudicial classe = (br.jus.cnj.pje.ws.ClasseJudicial) objeto;
				
				if (classe == null || !classe.getRemessaInstancia()) {
					return false;
				}
				
				if (FILTRO_RECURSAIS.equalsIgnoreCase(filtroUtilizado)) {
					return classe.getRecursal();	
				} else if (FILTRO_ORIGINARIAS.equalsIgnoreCase(filtroUtilizado)) {
					return !classe.getRecursal();
				} else if (FILTRO_CODIGOS_ESPECIFICOS.equalsIgnoreCase(filtroUtilizado) && codigosPermitidos != null) {
					return Util.listaContem(codigosPermitidos, String.valueOf(classe.getCodigo()));
				}
				
				return true;
			}
		};
	}
}