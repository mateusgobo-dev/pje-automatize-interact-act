package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.TaskInstance;

import com.google.common.base.MoreObjects;

import br.com.infox.cliente.NumeroProcessoUtil;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.entidades.vo.SelecaoAssuntoVO;
import br.jus.cnj.pje.entidades.vo.StatusEnvioManifestacaoProcessualVO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRequisicaoDTO;
import br.jus.cnj.pje.intercomunicacao.dto.ManifestacaoProcessualRespostaDTO;
import br.jus.cnj.pje.intercomunicacao.util.constant.MNIParametro;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoAssuntoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.cnj.pje.ws.AssuntoJudicial;
import br.jus.cnj.pje.ws.client.ConsultaPJeClient;
import br.jus.csjt.pje.commons.util.MunicipioIBGESuggestBean;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.jt.entidades.ProcessoJT;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.EnderecoWsdl;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteRepresentante;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Search;

@Name("intercomunicacaoRemessaHome")
public class IntercomunicacaoRemessaHome extends AbstractIntercomunicacaoHome {

	private static final long serialVersionUID = 9182605957934228195L;

	@In(create=true)
	private TramitacaoProcessualService tramitacaoProcessualService;
	
	@In
	private ProcessoJudicialManager processoJudicialManager;
	
	@In
	private ClasseJudicialManager classeJudicialManager;
	
	@In
	private ParametroService parametroService;

	private ManifestacaoProcessualRequisicaoDTO manifestacaoProcessual;
	private ProcessoJT processoJt;

	// Dados iniciais
	private br.jus.cnj.pje.ws.Jurisdicao jurisdicaoSelecionada;
	private Collection<br.jus.cnj.pje.ws.Jurisdicao> colecaoJurisdicao;
	
	private br.jus.cnj.pje.ws.ClasseJudicial classeJudicialSelecionada;
	private Collection<br.jus.cnj.pje.ws.ClasseJudicial> colecaoClasseJudicial;
	
	// Outros parametros - Caracteristicas do processo
	private String segredoJustica;
	private String justicaGratuita;
	private Boolean tutelaLiminar;
	private String valorCausa;

	private AssuntoTrf selectedRowAssuntoPrincipal = new AssuntoTrf();
	private List<SelecaoAssuntoVO> selecaoAssuntos;
	private List<SelectItem> competenciaList;
	private Integer competenciaConflito = null;
	private Set<Integer> linhasParaAtualizar = new HashSet<Integer>();
	private Integer linhaUltimoAssuntoPrincipal;
	private String buscaAssunto;
	private boolean mostrarMensagemAssuntoAtualizado = true;

	@In
	protected FacesContext facesContext;
	
	private EntityDataModel<ProcessoDocumento> processoDocumentoDataModel;
	
	public TaskInstanceHome getTaskInstanceHome() {
		return TaskInstanceHome.instance();
	}

	public String getSegredoJustica() {
		return segredoJustica;
	}

	public void setSegredoJustica(String segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	public String getJusticaGratuita() {
		return justicaGratuita;
	}

	public void setJusticaGratuita(String justicaGratuita) {
		this.justicaGratuita = justicaGratuita;
	}

	public Boolean getTutelaLiminar() {
		return tutelaLiminar;
	}

	public void setTutelaLiminar(Boolean tutelaLiminar) {
		this.tutelaLiminar = tutelaLiminar;
	}

	public String getValorCausa() {
		return valorCausa;
	}

	public void setValorCausa(String valorCausa) {
		this.valorCausa = valorCausa;
	}

	public ProcessoJT getProcessoJt() {
		return processoJt;
	}

	public void setProcessoJt(ProcessoJT processoJt) {
		this.processoJt = processoJt;
	}
	// FIM DOS GETTERS AND SETTERS

	@BypassInterceptors
	public String getBuscaAssunto() {
		return buscaAssunto;
	}

	@BypassInterceptors
	public void setBuscaAssunto(String buscaAssunto) {
		this.buscaAssunto = buscaAssunto;
	}

	@BypassInterceptors
	public List<SelecaoAssuntoVO> getSelecaoAssuntos() {
		if (selecaoAssuntos == null) {
			selecaoAssuntos = new ArrayList<SelecaoAssuntoVO>();
		}
		return selecaoAssuntos;
	}

	@BypassInterceptors
	public void setSelecaoAssuntos(List<SelecaoAssuntoVO> selecaoAssuntos) {
		this.selecaoAssuntos = selecaoAssuntos;
	}

	@Override
	public void create() {
		super.create();
		
		consultarColecaoEnderecoWsdl();
		carregarTaskInstance();
		
		//Remessa gravada
		if (getVariavelProcessoTrf() != null && getVariavelEnderecoWsdl() != null) {
			setInstance(getVariavelProcessoTrf());
			setEnderecoWsdl(getVariavelEnderecoWsdl());
			if(getVariavelMotivoRemessa() != null) {
				setIdMotivoRemessa(getVariavelMotivoRemessa());
			}
			
			setJurisdicaoSelecionada(getInstance().getJurisdicao());
			setClasseJudicialSelecionada(getInstance().getClasseJudicial());
			
			consultarColecaoJurisdicao();
			consultarColecaoClasseJudicial();
			atualizarListaDeAssuntos(); 
		} else {
			try{
				gravaVariavelHashProcessoTrf(getInstance());
			}catch(Exception e){
				throw new AplicationException(e.getMessage() + "\nNão foi possível gravar o hash do Processo para execução da remessa. Favor entrar em contato com a central de atendimento ao usuário!");
			}
			consultarColecaoJurisdicao();
			setJurisdicaoSelecionada((Jurisdicao) null);
			setClasseJudicialSelecionada((ClasseJudicial) null);
			setIdMotivoRemessa(-1);
			finalizarEnvioManifestacaoProcessual();
		}
		
		if (isRequisicaoFinalizadaComErro()) {
			StatusEnvioManifestacaoProcessualVO status = getStatusEnvioManifestacaoProcessualVO(false);
			FacesUtil.adicionarMensagemInfo(false, 
					"Requisição finalizada com erro, tente novamente! Erro: "+ status.getMensagem());
			finalizarEnvioManifestacaoProcessual();
		}
	}

	/**
	 * Grava os dados existentes para remeter para o segundo grau.
	 * 
	 */
	public void consolidaProcessoTrf() {
		try {
			validarProcessoRemessa();
			setVariavelProcessoTrf(getInstance());
			setVariavelMotivoRemessa(getIdMotivoRemessa());
			setVariavelEnderecoWsdl(getEnderecoWsdl());
			FacesUtil.adicionarMensagemInfo(true, "Configuração da remessa gravada!");
		} catch (Exception e) {
			FacesUtil.adicionarMensagemInfo(true, e);
		}
	}

	public void deletar() {
		setVariavelProcessoTrf(null);
		setVariavelMotivoRemessa(null);
		setVariavelEnderecoWsdl(null);
		setVariavelHashProcesso(null);
		
		finalizarEnvioManifestacaoProcessual();
	}

	// METODO UTILIZADO PARA A ABA CARACTERISTICAS DO PROCESSO
	/**
	 * Configura a visualização dos dados de "Características do processo"
	 * 
	 * @author Rafael Barros
	 * @since 1.4.4
	 */
	public void visualizarDadosCaracteristicasProcesso() {
		if (BooleanUtils.isTrue(getInstance().getSegredoJustica())) {
			this.segredoJustica = "Sim";
		} else {
			this.segredoJustica = "Não";
		}

		this.setTutelaLiminar(BooleanUtils.isTrue(getInstance().getTutelaLiminar()));

		if (BooleanUtils.isTrue(getInstance().getJusticaGratuita())) {
			this.justicaGratuita = "Sim";
		} else {
			this.justicaGratuita = "Não";
		}

		if (getInstance().getValorCausa() != null) {
			this.valorCausa = StringUtil.formatarValorMoeda(getInstance().getValorCausa(), true);
		}
	}

	/**
	 * Remove todas as partes selecionadas do polo ativo com seus representantes
	 * 
	 * @author Guilherme Bispo
	 * @since 1.4.4
	 */
	public void removerPartesPoloAtivo(ProcessoParte processoParte) {
		getInstance().getProcessoParteList().remove(processoParte);
	}

	/**
	 * Remove todas as partes selecionadas do polo passivo com seus
	 * representantes
	 * 
	 * @author Guilherme Bispo
	 * @since 1.4.4
	 */
	public void removerPartesPoloPassivo(ProcessoParte processoParte) {
		getInstance().getProcessoParteList().remove(processoParte);
	}

	
	public List<ProcessoParte> getListaParteAtivo(){
		return  removeAdvogados(getInstance().getListaPartePoloObj(true, ProcessoParteParticipacaoEnum.A));
	}
	
	public List<ProcessoParte> getListaPartePassivo(){
		return removeAdvogados(getInstance().getListaPartePoloObj(true, ProcessoParteParticipacaoEnum.P));
	}

	public List<ProcessoParte> getListaParteTerceiro(){
		return removeAdvogados(getInstance().getListaPartePoloObj(true, ProcessoParteParticipacaoEnum.T));
	}
	
	/**
	 * Função que remove os advogados das partes para ser exibido somente como parte representante
	 * 
	 * @param listaProcessoParteSemAdvogado
	 * @return
	 */
	private List<ProcessoParte> removeAdvogados(List<ProcessoParte> listaProcessoParte){
		List<ProcessoParte> advogados = new ArrayList<ProcessoParte>(1);

		for(ProcessoParte processoParte : listaProcessoParte){
			boolean parteRepresentante =processoParte.getProcessoParteRepresentanteList2() != null 
					&& !processoParte.getProcessoParteRepresentanteList2().isEmpty(); 
			if(parteRepresentante){
				advogados.add(processoParte);
			}
		}
		
		listaProcessoParte.removeAll(advogados);
		
		return listaProcessoParte;
	}
	
	/**
	 * Duplica todas as partes do processo, copiando quem está no polo ativo
	 * para o passivo e vice-versa.
	 * 
	 * @author Guilherme Bispo e Thiago Oliveira
	 * @since 1.4.4
	 */
	
	public void duplicarPartes() {
		List<ProcessoParte> listaPartes = getListaParte();

		// Percorre as partes do polo ativo e passivo do processo
		for (ProcessoParte processoParte : listaPartes) {

			try {

				// Se for Terceiro, não duplica
				if (!processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.T)) {

					ProcessoParte parteDuplicada = new ProcessoParte();
					BeanUtilsBean.getInstance().copyProperties(parteDuplicada, processoParte);

					ProcessoParteParticipacaoEnum inParticipacao;

					// Se for ativo, duplica para Passivo
					if (processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)) {
						inParticipacao = ProcessoParteParticipacaoEnum.P;
					} else {
						// Se for Passivo, duplica para Ativo
						inParticipacao = ProcessoParteParticipacaoEnum.A;
					}

					parteDuplicada.setInParticipacao(inParticipacao);
					parteDuplicada.setProcessoParteRepresentanteList(new ArrayList<ProcessoParteRepresentante>());

					// Se a parte já existir, não duplica
					if (getInstance().getProcessoParteList().contains(parteDuplicada)) {
						continue;
					}

					// Percorre todos os representantes da parte duplicada
					for (ProcessoParteRepresentante representante : processoParte.getProcessoParteRepresentanteList()) {

						// Cria um novo representante com a cópia dos atributos
						// do representante corrente
						ProcessoParteRepresentante cloneRepresentante = new ProcessoParteRepresentante();
						BeanUtilsBean.getInstance().copyProperties(cloneRepresentante, representante);
						ProcessoParte cloneParte = new ProcessoParte();
						BeanUtilsBean.getInstance().copyProperties(cloneParte, representante.getParteRepresentante());

						// Altera sua participação
						cloneParte.setInParticipacao(inParticipacao);

						// Se o represante já estiver cadastrado como parte
						if (getInstance().getProcessoParteList().contains(
								cloneParte)) {
							cloneParte = getInstance().getProcessoParteList().get(getInstance().getProcessoParteList().indexOf(cloneParte));
						} else {
							getInstance().getProcessoParteList().add(cloneParte);
						}

						cloneRepresentante.setParteRepresentante(cloneParte);
						cloneRepresentante.setProcessoParte(parteDuplicada);
						parteDuplicada.getProcessoParteRepresentanteList().add(cloneRepresentante);
					}

					// Adiciona a parte duplicada a lista de partes do processo
					getInstance().getProcessoParteList().add(parteDuplicada);
				}
				atualizarTipoPartesClasseJudicialSelecionada();
				FacesUtil.adicionarMensagemInfo(true, "Polos duplicados com sucesso.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Inverte todas as partes do processo, quem está no polo ativo muda para o
	 * passivo e vice-versa.
	 * 
	 * @author Guilherme Bispo
	 * @since 1.4.4
	 */
	public void inverterPartesProcesso() {
		List<ProcessoParte> listaPartes = getListaParte();
		// Percorre as partes do polo ativo e passivo do processo
		for (ProcessoParte processoParte : listaPartes) {

			// Se for ativo, duplica para Passivo
			if (processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.A)) {
				processoParte.setInParticipacao(ProcessoParteParticipacaoEnum.P);

			} else if (processoParte.getInParticipacao().equals(ProcessoParteParticipacaoEnum.P)) {
				// Se for Passivo, duplica para Ativo
				processoParte.setInParticipacao(ProcessoParteParticipacaoEnum.A);
			}
		}
		atualizarTipoPartesClasseJudicialSelecionada();
		FacesUtil.adicionarMensagemInfo(true, "Polos invertidos com sucesso. ");
	}

	private List<ProcessoParte> getListaParte() {
		List<ProcessoParte> listaPartes = getListaParteAtivo(); 
		listaPartes.addAll(getListaPartePassivo());
		return listaPartes;
	}

	// Método criado em 25/07/2012 por Rafael Barros
	// Utilizado na aba "Dados iniciais" quando há mudança de classe judicial
	// Serve para atualizar as listas de assuntos e também modificar a
	// classificação das partes de acordo com a classe judicial selecionada
	/**
	 * Atualiza tipo de partes e a lista de assunto quando selecionada uma nova
	 * classe judicial
	 * 
	 * @author Rafael Barros
	 * @since 1.4.4
	 */
	public void atualizarDadosMudancaClasseJudicial() {
		if (getClasseJudicialSelecionada() != null) {
			atualizarTipoPartesClasseJudicialSelecionada();
		}
		this.mostrarMensagemAssuntoAtualizado = false;
		this.atualizarListaDeAssuntos();
		this.mostrarMensagemAssuntoAtualizado = true;
	}

	// Método criado em 25/07/2012 por Rafael Barros para modificar o tipo das
	// partes de acordo com a classe judicial escolhida
	/**
	 * Atualiza tipo de partes de acordo com a classe judicial selecionada
	 * 
	 * @author Rafael Barros
	 * @since 1.4.4
	 */
	public void atualizarTipoPartesClasseJudicialSelecionada() {
		// Lista que será utilizada para modificar o tipo de participação de
		// acordo com a classe judicial

		for (int i = 0; i < getInstance().getProcessoParteList().size(); i++) {
			ProcessoParte ppParte = getInstance().getProcessoParteList()
					.get(i);
			if (ppParte.getIsAtivo() && ppParte.getPartePrincipal()) {
				TipoParte tipoParte = null;
				if (classeJudicialSelecionada != null) {
				  	switch(ppParte.getInParticipacao()){
				  		case A:
				  			tipoParte = converteTipoParteInteropParaTipoParte(classeJudicialSelecionada.getTipoPartePoloAtivo());
				  			break;
				  	
				  		case P:
				  			tipoParte = converteTipoParteInteropParaTipoParte(classeJudicialSelecionada.getTipoPartePoloPassivo());
				  			break;
				  	
				  		default:
				  			break;
				  	}					
				}
			  	
		  		if(tipoParte != null){
		  			ppParte.setTipoParte(tipoParte);
		  		}
			}
		}
	}

	// Método criado em 13/07/2012 por Rafael Barros devido à ISSUE PJEII-1432 /
	// PJEII-1434
	// Utilizado pela aba "Informações da Justiça do Trabalho" para montar os
	// MUNICÍPIOS de acordo com a UF selecionada
	private MunicipioIBGESuggestBean getMunicipioIBGESuggestBean() {
		return ComponentUtil.getComponent(MunicipioIBGESuggestBean.NAME);
	}

	// Método criado em 13/07/2012 por Rafael Barros devido à ISSUE PJEII-1432 /
	// PJEII-1434
	// Utilizado pelo método "wire" na aba "Informações da Justiça do Trabalho"
	/**
	 * Retorna o processo da JT equivalente ao processo TRF consultado
	 * 
	 * @author Rafael Barros
	 * @since 1.4.4
	 */
	private ProcessoJT getProcessoJtPorId(int idProcessoJt) {
		ProcessoJT processoJt = null;
		try {
			processoJt = ((ProcessoJT) getEntityManager().createQuery(
					"FROM " + ProcessoJT.class.getSimpleName()
							+ " WHERE idProcessoJt = " + idProcessoJt)
					.getSingleResult());
		} catch (NoResultException e) {
			// pode estar editando um processoTrf que nao possui processoJt
			// associado
		} catch (NonUniqueResultException e) {
			// mais de um processoJt para o processoTrf gerenciado
			throw new AplicationException(
					"Mais de um processoJt para o processoTrf gerenciado.");
		}

		return processoJt;
	}

	// Método criado em 13/07/2012 por Rafael Barros devido à ISSUE PJEII-1432 /
	// PJEII-1434
	// Utilizado pela aba "Informações da Justiça do Trabalho"
	/**
	 * Método utilizado para configurar o ProcessoJT e também para configurar o
	 * município de acordo com o ProcessoJT selecionado
	 * 
	 * @author Rafael Barros
	 * @since 1.4.4
	 */
	public void wire() {
		MunicipioIBGESuggestBean municipioIBGESuggestBean = getMunicipioIBGESuggestBean();
		this.processoJt = getProcessoJtPorId(getInstance()
				.getIdProcessoTrf());

		if (this.processoJt != null) {
			this.processoJt.setProcessoTrf(getInstance());
			if (this.processoJt.getMunicipioIBGE() != null) {
				municipioIBGESuggestBean.setEstado(this.processoJt.getMunicipioIBGE().getUf());
			}
		}

	}

	/**
	 * Faz a remessa do processo para o segundo grau
	 * 
	 * @author Gabriel Azevedo
	 * @since 1.4.4
	 */
	public void remeter() {
		try {
			validarProcessoRemessa();
			
			MNIParametro.getListIndiceParteSituacao().clear();
			MNIParametro.getListIndiceParteRepresentateSituacaoValor().clear();
			MNIParametro.getListIndiceParteSigiloValor().clear();
			MNIParametro.getListIndiceParteRepresentateSigiloValor().clear();
			
			MNIParametro.getListIndiceParteSigiloValor().clear();
			MNIParametro.getListIndiceParteRepresentateSigiloValor().clear();

			ConsultaPJeClient consultaPJeClient = getConsultaPJeClient();

			Boolean logouComCertificado = Authenticator.isLogouComCertificado();
			manifestacaoProcessual = new ManifestacaoProcessualRequisicaoDTO();
			manifestacaoProcessual.setProcessoTrf(getInstance());
			manifestacaoProcessual.setConsultaPJeClient(consultaPJeClient);
			manifestacaoProcessual.setCompetenciaConflito(competenciaConflito);
			manifestacaoProcessual.setIsRequisicaoPJE(Boolean.TRUE);
			manifestacaoProcessual.addParametro(MNIParametro.PARAM_LOGOU_COM_CERTIFICADO, String.valueOf(logouComCertificado));
			manifestacaoProcessual.getProcessoTrf().getJurisdicao().setIdJurisdicao(getJurisdicaoSelecionada().getId());
			
			setVariavelProcessoTrf(getInstance());
			setVariavelMotivoRemessa(getIdMotivoRemessa());

			setDocumentoNaoAssinadoList(getDocumentosNaoAssinadosUsuarioInterno(getInstance()));

			if (getDocumentoNaoAssinadoList() == null || getDocumentoNaoAssinadoList().isEmpty()) {
				enviarProcesso();
			}
			 
		} catch (Exception e) {
			FacesUtil.adicionarMensagemInfo(true, e);
		}
	}

	public void enviarProcesso() {
		ManifestacaoProcessualRespostaDTO resposta = new ManifestacaoProcessualRespostaDTO();
		String mensagemResposta = "Processo remetido com sucesso!";
		String numeroProcessoDestino = "";

		try {
			ProcessoTrf processo = getInstance();

			resposta = enviarProcesso(processo, getEnderecoWsdl(),
					"Processo enviado à instância superior.",
					MNIParametro.PARAM_REMESSA);
			
			numeroProcessoDestino = NumeroProcessoUtil.mascaraNumeroProcesso(resposta.getNumeroProcesso());
			if(!processo.getNumeroProcesso().equals(numeroProcessoDestino)){
				mensagemResposta += " Número do processo no destino: "+numeroProcessoDestino;
			}
			
			FacesUtil.adicionarMensagemInfo(true, mensagemResposta);
			
		} catch (Exception e) {
			finalizarEnvioManifestacaoProcessual();
			FacesUtil.adicionarMensagemInfo(true, e);
		}

	}
	
	public void posEnviarProcesso() {
		ProcessoTrf processo = getInstance();
		StatusEnvioManifestacaoProcessualVO status = getStatusEnvioManifestacaoProcessualVO(true);
		
		super.posEnviarProcesso(
					processo, 
					MNIParametro.PARAM_REMESSA, 
					status.getProtocoloRecebimento(), 
					"Processo enviado à instância superior.",
					getEnderecoWsdl());
	}

	public void validarProcessoRemessa() throws Exception {
		StringBuilder erros = new StringBuilder();
		
		try {
			if (getVariavelProcessoTrf() != null) {
				setInstance(getVariavelProcessoTrf());
			}
			
			erros.append(validarExpedientes(getInstance()));

			if (getInstance().getJurisdicao() == null
					|| "".equals(getInstance().getJurisdicao().getJurisdicao())) {
				erros.append("Favor preencher a jurisdição.\n");
			}

			if (getInstance().getClasseJudicial() == null
					|| "".equals(getInstance().getClasseJudicial().getClasseJudicial())) {
				erros.append("Favor preencher a classe judicial.\n");
			}

			if (!this.validaMotivoRemessa())
				erros.append("Favor preencher o motivo da remessa.\n");

			if (getInstance().getProcessoAssuntoList() == null
					|| getInstance().getProcessoAssuntoList().size() == 0) {
				erros.append("Deve existir pelo menos um assunto associado.\n");
			}

			if (!verificarExistenciaAssuntoPrincipalAssociado()) {
				erros.append("Deve existir pelo menos um assunto principal associado.\n");
			}
			
			if (!verificarExistenciaAssuntoNaoComplementar()) {
				erros.append("Deve existir pelo menos um assunto não complementar associado.\n");
			}			
			
			// Verifica se existe pelo menos uma parte no polo ativo.
			Integer tamAtivo = getInstance().getListaParteAtivo().size();
			
			if (tamAtivo == 0) {
				erros.append("Deve existir pelo menos uma parte no polo ativo.\n");
			}

			// Verifica se existe pelo menos uma parte no polo passivo.
			Integer tamPassivo = getInstance().getListaPartePassivo().size();
			br.jus.cnj.pje.ws.ClasseJudicial classeSelecionada = recuperarDadosClasseJudicialInteropPorCodigo(getInstance().getClasseJudicial());
			
			if (tamPassivo == 0 && (classeSelecionada != null && classeSelecionada.getExigePoloPassivo())) {
				erros.append("Deve existir pelo menos uma parte no polo passivo.\n");
			}

			if (erros.length() > 0) {
				throw new Exception(erros.toString());
			}

		} catch (PJeBusinessException e) {
			throw new Exception(e.getCode());
		}
	}

	/**
	 * Verifica se o processo está sendo visualizado por outro usuário
	 * 
	 * @author Rafael Barros
	 * @since 1.4.4
	 * @return String
	 */
	public String verificaProcessoConsultado() {
		if (getInstance().getNumeroProcesso() != null) {
			ProcessoTrf processoTrf = getProcessoTrfManager().getProcessoTrfByProcesso(getInstance().getProcesso());
			Processo processo = (processoTrf != null ? processoTrf.getProcesso() : null);
			if (processo != null && 
				processo.getActorId() != null &&
				!processo.getActorId().equals(Authenticator.getUsuarioLogado().getLogin())) {
				return "Este Processo esta sendo visualizado pelo usuário: "+ processo.getActorId();
			}
		}
		return null;
	}

	/**
	 * Atualiza as grids processoTrfDocumentoGrd na aba de processos
	 * 
	 * @author Rafael Barros
	 * @since 1.4.4
	 * @return String
	 */
	public void refreshGridDocumento() {
		carregaCompetencia();
		refreshGrid("processoTrfDocumentoRemessaGrid");
	}

	/**
	 * @author Thiago Shiono
	 * @since 1.4.4
	 */
	@BypassInterceptors
	public void setSelectedRowAssuntoPrincipal(SelecaoAssuntoVO selecaoAssunto,
			Integer numeroLinha) {
		
		if(selecaoAssunto.getAssuntoTrf().getComplementar()){
			FacesUtil.adicionarMensagemInfo(false, 
					"Assunto complementar não pode ser marcado como principal!");
			return;
		}
		
		this.selectedRowAssuntoPrincipal = selecaoAssunto.getAssuntoTrf();

		ProcessoTrf proc = getInstance();

		if (selecaoAssunto.getSelecionado() != null
				&& selecaoAssunto.getSelecionado()) {
			for (ProcessoAssunto pa : proc.getProcessoAssuntoList()) {
				if (pa.getAssuntoTrf()
						.getCodAssuntoTrf()
						.equals(selecaoAssunto.getAssuntoTrf()
								.getCodAssuntoTrf())) {
					pa.setAssuntoPrincipal(true);
				} else {
					pa.setAssuntoPrincipal(false);
				}
			}

			for (SelecaoAssuntoVO sa : getSelecaoAssuntos()) {
				if (sa.getAssuntoTrf().getCodAssuntoTrf()
						.equals(selectedRowAssuntoPrincipal.getCodAssuntoTrf())) {
					sa.setAssuntoPrincipal(true);
				} else {
					sa.setAssuntoPrincipal(false);
				}
			}

			linhasParaAtualizar.clear();

			if (linhaUltimoAssuntoPrincipal != null) {
				linhasParaAtualizar.add(linhaUltimoAssuntoPrincipal);
			}

			linhasParaAtualizar.add(numeroLinha);
			linhaUltimoAssuntoPrincipal = numeroLinha;
		} else {
			FacesUtil.adicionarMensagemError(false, "Assunto não selecionado.");
		}

	}

	public void carregaCompetencia() {
		this.competenciaConflito = null;
		competenciaList = new ArrayList<SelectItem>();
		br.jus.cnj.pje.ws.Jurisdicao jurisdicao = getJurisdicaoSelecionada();
		br.jus.cnj.pje.ws.ClasseJudicial classeJudicial = getClasseJudicialSelecionada();

		List<br.jus.cnj.pje.ws.AssuntoJudicial> assuntoList = new ArrayList<br.jus.cnj.pje.ws.AssuntoJudicial>();
		if (getInstance().getProcessoAssuntoList() != null
				&& getInstance().getProcessoAssuntoList().size() > 0) {
			for (br.jus.pje.nucleo.entidades.ProcessoAssunto processoAssuntoEntity : getInstance()
					.getProcessoAssuntoList()) {
				AssuntoJudicial assuntoJudicial = new AssuntoJudicial();
				assuntoJudicial.setCodigo(processoAssuntoEntity.getAssuntoTrf()
						.getCodAssuntoTrf());
				assuntoList.add(assuntoJudicial);
			}
		}
		if (jurisdicao != null && classeJudicial != null && (assuntoList != null && assuntoList.size() > 0)) {
			List<br.jus.cnj.pje.ws.Competencia> competenciasInterop = getConsultaPJeClient().consultarCompetencias(jurisdicao, classeJudicial, assuntoList);
			if (competenciasInterop != null && competenciasInterop.size() > 1) {
				competenciaList.add(new SelectItem(-1, "Selecione..."));
				for (br.jus.cnj.pje.ws.Competencia competenciaInterop : competenciasInterop) {
					SelectItem s = new SelectItem(competenciaInterop.getId(),
							competenciaInterop.getDescricao());
					competenciaList.add(s);
				}
			}else if(competenciasInterop != null && competenciasInterop.size() == 1){
				competenciaConflito = competenciasInterop.get(0).getId();
			}
		}
	}

	/**
	 * Adiciona um assunto ao processo ou remove um assunto do processo
	 * 
	 * @author Thiago Shiono
	 * @since 1.4.4
	 */
	@BypassInterceptors
	public void addRemoveAssunto(SelecaoAssuntoVO selecaoAssunto,
			Integer numeroLinha) {
		ProcessoTrf proc = getInstance();

		if (selecaoAssunto.getSelecionado()) {

			ProcessoAssunto processoAssunto = new ProcessoAssunto();

			boolean isPrimeiro = linhaUltimoAssuntoPrincipal == null;

			if (isPrimeiro) {
				this.selectedRowAssuntoPrincipal = selecaoAssunto
						.getAssuntoTrf();
				
				if(!selectedRowAssuntoPrincipal.getComplementar()){
					linhaUltimoAssuntoPrincipal = numeroLinha;
				}
			}

			processoAssunto.setAssuntoPrincipal(selecaoAssunto
					.getAssuntoPrincipal());
			processoAssunto.setAssuntoTrf(selecaoAssunto.getAssuntoTrf());
			processoAssunto.setProcessoTrf(proc);

			proc.getProcessoAssuntoList().add(processoAssunto);

			if (!ParametroJtUtil.instance().justicaTrabalho() && this.mostrarMensagemAssuntoAtualizado) {
				FacesUtil.adicionarMensagemInfo(false, "Assunto associado ao processo com sucesso!");
			}
		} else {
			if (selecaoAssunto.getAssuntoPrincipal() != null
					&& selecaoAssunto.getAssuntoPrincipal()) {
				FacesUtil.adicionarMensagemError(false, 
						"Não é possível remover o assunto principal.");
				selecaoAssunto.setSelecionado(true);
			} else {
				Iterator<ProcessoAssunto> it = proc.getProcessoAssuntoList()
						.iterator();

				while (it.hasNext()) {
					ProcessoAssunto processoAssunto = it.next();

					if (processoAssunto
							.getAssuntoTrf()
							.getCodAssuntoTrf()
							.equals(selecaoAssunto.getAssuntoTrf()
									.getCodAssuntoTrf())) {
						it.remove();
					}
				}
			}
		}

		linhasParaAtualizar.clear();
		linhasParaAtualizar.add(numeroLinha);
	}

	/**
	 * Retorna uma lista de assuntos selecionados
	 * 
	 * @author Thiago Shiono
	 * @since 1.4.4
	 * @return List<SelecaoAssuntoVO>
	 */
	@BypassInterceptors
	public List<SelecaoAssuntoVO> listaAssuntoSelecionados() {
		List<SelecaoAssuntoVO> listaItensSelecionados = new ArrayList<SelecaoAssuntoVO>();

		for (SelecaoAssuntoVO sa : getSelecaoAssuntos()) {
			if (sa.getSelecionado() == true) {
				listaItensSelecionados.add(sa);
			}
		}

		return listaItensSelecionados;
	}

	/**
	 * Método que retorna o último documento do tipo despacho do processo.
	 * 
	 * @author Thiago de Almeida Oliveira, Guilherme D. Bispo
	 */
	public ProcessoDocumento getUltimoDespacho() {
		return ProcessoDocumentoManager.instance().getUltimoProcessoDocumento(
				ParametroUtil.instance().getTipoProcessoDocumentoDespacho(),
				getInstance().getProcesso());
	}

	public void setUltimoDespacho(String ultimoDespacho) {

	}

	/**
	 * Atualiza lista de assuntos de acordo com a classe judicial selecionada
	 * 
	 * @author Thiago Shiono
	 * @since 1.4.4
	 */
	@BypassInterceptors
	public void atualizarListaDeAssuntos() {
		// trazer todas os assuntos do segundo grau
		ConsultaPJeClient client = getConsultaPJeClient();
		
		List<AssuntoJudicial> assuntosInstanciaDestino = client.consultarAssuntosJudiciais(
				getJurisdicaoSelecionada(),
				getClasseJudicialSelecionada());
		getSelecaoAssuntos().clear();

		// Cria a lista com todos os possíveis assuntos e os 'selecionados'
		Boolean novo = isNovosAssuntos();
		
		ProcessoAssuntoManager processoAssuntoManager = (ProcessoAssuntoManager) Component.getInstance("processoAssuntoManager");
		List<ProcessoAssunto> assuntosOriginais = processoAssuntoManager.retornaAssuntos(getInstance().getIdProcessoTrf());


		int qtd = 0;
		for (AssuntoJudicial assunto : assuntosInstanciaDestino) {
			AssuntoTrf assuntoTrf = new AssuntoTrf();
			assuntoTrf.setCodAssuntoTrf(assunto.getCodigo());
			assuntoTrf.setAssuntoTrf(assunto.getDescricao());
			assuntoTrf.setComplementar(assunto.getComplementar());

			SelecaoAssuntoVO selecaoAssuntoVO = new SelecaoAssuntoVO();
			boolean selecionado = false;
			Boolean assuntoPrincipal = false;
			ProcessoAssunto pa = selecionaAssuntosOriginariosParaRemessa(assuntosOriginais, assunto);
			if (pa!=null){
				selecionado = true;
				assuntoPrincipal = pa.getAssuntoPrincipal();
			}
			selecaoAssuntoVO.setAssuntoTrf(assuntoTrf);
			selecaoAssuntoVO.setSelecionado(selecionado);
			selecaoAssuntoVO.setAssuntoPrincipal(assuntoPrincipal);
			getSelecaoAssuntos().add(selecaoAssuntoVO);
			if(novo && selecionado){
				addRemoveAssunto(selecaoAssuntoVO, qtd);
			}
			qtd++;
		}
	}
	/**
	 * retorna um processo assunto caso encontre compatibilidade entre o assunto de destino e os assuntos existentes
	 * @param processoAssuntoList assuntos da instancia de origem
	 * @param assuntoInstanciaDestino assunto de destino a ser verificado
	 * @return null se nao encontrado
	 */
	private ProcessoAssunto selecionaAssuntosOriginariosParaRemessa(List<ProcessoAssunto> processoAssuntoList, AssuntoJudicial assuntoInstanciaDestino){
		for (int i = 0; i < processoAssuntoList.size(); i++) {
			br.jus.pje.nucleo.entidades.ProcessoAssunto processoAssuntoEntity = processoAssuntoList.get(i);
			if (assuntoInstanciaDestino.getCodigo().equals(
					processoAssuntoEntity.getAssuntoTrf()
							.getCodAssuntoTrf())) {
				boolean assuntoPrincipal = processoAssuntoEntity
						.getAssuntoPrincipal();
				if (assuntoPrincipal) {
					linhaUltimoAssuntoPrincipal = i;
				}
				return processoAssuntoEntity;
			}
		}
		return null;
		
	}
	
	/**
	 * verifica se a atualização dos assuntos é nova
	 * @return
	 */
	private boolean isNovosAssuntos(){
		if(getInstance().getProcessoAssuntoList() == null || getInstance().getProcessoAssuntoList().size() == 0)
			return true;
		return false;
	}

	/**
	 * recupera os dados da classe judicial selecionada em uma lista com os dados das classes recuperadas pela interoperabilidade
	 * @param codigoClasseJudicial 
	 */
	private br.jus.cnj.pje.ws.ClasseJudicial recuperarDadosClasseJudicialInteropPorCodigo(ClasseJudicial classeJudicial){
		String codigo = (classeJudicial != null ? classeJudicial.getCodClasseJudicial() : null);
		Predicate filtro = novoFiltroClasseJudicialPeloCodigo(codigo);
		return (br.jus.cnj.pje.ws.ClasseJudicial) 
				CollectionUtils.find(getColecaoClasseJudicial(), filtro);
	}
	
	/**
	 * Converte o tipoParte recebido pela interoperabilidade apenas para mostrar na remessa como tipo de parte
	 * @param tipoParte
	 * @return
	 */
	private TipoParte converteTipoParteInteropParaTipoParte(br.jus.cnj.pje.ws.TipoParte tipoParte){
		TipoParte tp = new TipoParte();
		tp.setTipoParte(tipoParte.getDescTipoParte());
		tp.setIdTipoParte(tipoParte.getIdTipoParte());
		return tp;
		
	}
	
	/**
	 * Este método poderá ser chamado em eventos de tarefa em fluxos de remessa
	 * para instância superior. Permite que seja acionada uma transição de saída
	 * com intenção de cancelamento da remessa.
	 */
	public void permiteCancelarRemessa(){
		
		if(getTransicaoDispensaRequeridos()){
			this.deletar();
			try {
				if(getEntityManager().contains(getInstance())){
					getInstance().getProcessoAssuntoList().clear();
					processoJudicialManager.refresh(getInstance());
				}
			} catch (PJeBusinessException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean getTransicaoDispensaRequeridos(){
		boolean transicaoDispensaRequeridos = false;
		String transicoesComDispensaRequeridos = (String) tramitacaoProcessualService.recuperaVariavelTarefa("pje:fluxo:transicao:dispensaRequeridos");
		if (transicoesComDispensaRequeridos != null) {
			transicaoDispensaRequeridos = br.com.infox.cliente.Util.listaContem(transicoesComDispensaRequeridos, TaskInstanceHome.instance().getTransicaoSaida());	
		}
		
		return transicaoDispensaRequeridos;
	}

	public Boolean verificarSegredo() {
		return getInstance().getSegredoJustica() != null
				&& getInstance().getSegredoJustica();
	}

	@BypassInterceptors
	public Set<Integer> getLinhasParaAtualizar() {
		return linhasParaAtualizar;
	}

	@BypassInterceptors
	public void setLinhasParaAtualizar(Set<Integer> linhasParaAtualizar) {
		this.linhasParaAtualizar = linhasParaAtualizar;
	}

	@Override
	protected ManifestacaoProcessualRequisicaoDTO getManifestacaoProcessual() {
		return manifestacaoProcessual;
	}

	public void alterarParaPoloAtivo(ProcessoParte processoParte) {
		processoParte.setInParticipacao(ProcessoParteParticipacaoEnum.A);
	}

	public void alterarParaPoloPassivo(ProcessoParte processoParte) {
		processoParte.setInParticipacao(ProcessoParteParticipacaoEnum.P);
	}

	public static IntercomunicacaoRemessaHome instance() {
		return (IntercomunicacaoRemessaHome) Component
				.getInstance(IntercomunicacaoRemessaHome.class);
	}

	public List<SelectItem> getCompetenciaList() {
		return competenciaList;
	}

	public Integer getCompetenciaConflito() {
		return competenciaConflito;
	}

	public void setCompetenciaConflito(Integer competenciaConflito) {
		this.competenciaConflito = competenciaConflito;
	}

	/**
	 * Consulta as jurisdições da instância de integração.
	 */
	public void consultarColecaoJurisdicao() {
		if (getEnderecoWsdl() != null) {
			ConsultaPJeClient consultaPjeClient = new ConsultaPJeClient(getEnderecoWsdl());
			setConsultaPJeClient(consultaPjeClient);

			List<br.jus.cnj.pje.ws.Jurisdicao> listJurisdicao = CollectionUtilsPje.ordenarLista(consultaPjeClient.consultarJurisdicoes(), "descricao");
			String codigosPermitidos = (String) TaskInstanceUtil.instance().getVariable(Variaveis.PJE_FLUXO_MNI_SECAOSUBSECAO_CODIGOS_PERMITIDOS);
			
			if (codigosPermitidos != null) {
				Predicate filtro = criarFiltroJurisdicao(codigosPermitidos);
				CollectionUtils.filter(listJurisdicao, filtro);
			}
			
			setColecaoJurisdicao(listJurisdicao);
			Predicate filtro = novoFiltroJurisdicaoPeloId(getJurisdicaoSelecionada());
			br.jus.cnj.pje.ws.Jurisdicao jurisdicaoSelecionadaDaLista = (br.jus.cnj.pje.ws.Jurisdicao) CollectionUtils.find(getColecaoJurisdicao(), filtro);
			setJurisdicaoSelecionada(jurisdicaoSelecionadaDaLista);
		} else {
			setColecaoJurisdicao(Collections.emptyList());
		}
	}
	
	/**
	 * Consulta as classes judiciais com base na jurisdição.
	 */
	public void consultarColecaoClasseJudicial() {
		ConsultaPJeClient client = getConsultaPJeClient();
		Collection<br.jus.cnj.pje.ws.ClasseJudicial> classes = client.consultarClassesJudiciaisRemessa(getJurisdicaoSelecionada());
		String filtroUtilizado = (String) TaskInstanceUtil.instance().getVariable(Variaveis.PJE_FLUXO_MNI_CLASSE_FILTRO_UTILIZADO);
		String codigosPermitidos = (String) TaskInstanceUtil.instance().getVariable(Variaveis.PJE_FLUXO_MNI_CLASSE_CODIGOS_PERMITIDOS);
		Predicate filtro = criarFiltroClasseJudicialRecursal(filtroUtilizado, codigosPermitidos);
		CollectionUtils.filter(classes, filtro);
		setColecaoClasseJudicial(classes);
		
		filtro = novoFiltroClasseJudicialPeloId(getClasseJudicialSelecionada());
		br.jus.cnj.pje.ws.ClasseJudicial classeJudicialSelecionadaDaLista = (br.jus.cnj.pje.ws.ClasseJudicial) CollectionUtils.find(getColecaoClasseJudicial(), filtro);
		setClasseJudicialSelecionada(classeJudicialSelecionadaDaLista);
		
		if (ProjetoUtil.isVazio(getColecaoClasseJudicial())) {
			FacesUtil.adicionarMensagemError(false, 
					"Nenhuma classe recursal foi encontrada!");
		}
	}

	/**
	 * @return jurisdicaoSelecionada.
	 */
	public br.jus.cnj.pje.ws.Jurisdicao getJurisdicaoSelecionada() {
		return jurisdicaoSelecionada;
	}

	/**
	 * @param jurisdicaoSelecionada jurisdicaoSelecionada.
	 */
	public void setJurisdicaoSelecionada(br.jus.cnj.pje.ws.Jurisdicao jurisdicaoSelecionada) {
		this.jurisdicaoSelecionada = jurisdicaoSelecionada;

		if (jurisdicaoSelecionada != null) {
			Integer id = jurisdicaoSelecionada.getId();
			String descricao = jurisdicaoSelecionada.getDescricao();
			
			Jurisdicao jurisdicao = new Jurisdicao();
			jurisdicao.setIdJurisdicao(id);
			jurisdicao.setJurisdicao(descricao);
			getInstance().setJurisdicao(jurisdicao);
		}
	}
	
	/**
	 * @param jurisdicao jurisdicao.
	 */
	public void setJurisdicaoSelecionada(Jurisdicao jurisdicao) {
		br.jus.cnj.pje.ws.Jurisdicao jurisdicaoSelecionada = null;
		
		if (jurisdicao != null) {
			Integer id = MoreObjects.firstNonNull(jurisdicao.getNumeroOrigem(), jurisdicao.getIdJurisdicao());
			jurisdicaoSelecionada = new br.jus.cnj.pje.ws.Jurisdicao();
			jurisdicaoSelecionada.setId(id);
			jurisdicaoSelecionada.setDescricao(jurisdicao.getJurisdicao());
		}
		
		setJurisdicaoSelecionada(jurisdicaoSelecionada);
	}

	/**
	 * @return colecaoJurisdicao.
	 */
	public Collection<br.jus.cnj.pje.ws.Jurisdicao> getColecaoJurisdicao() {
		if (colecaoJurisdicao == null) {
			colecaoJurisdicao = new ArrayList<br.jus.cnj.pje.ws.Jurisdicao>();
		}
		return colecaoJurisdicao;
	}

	/**
	 * @param colecaoJurisdicao colecaoJurisdicao.
	 */
	public void setColecaoJurisdicao(Collection<br.jus.cnj.pje.ws.Jurisdicao> colecaoJurisdicao) {
		this.colecaoJurisdicao = colecaoJurisdicao;
	}

	/**
	 * @return classeJudicialSelecionada.
	 */
	public br.jus.cnj.pje.ws.ClasseJudicial getClasseJudicialSelecionada() {
		return classeJudicialSelecionada;
	}

	/**
	 * @param classeJudicialSelecionada classeJudicialSelecionada.
	 */
	public void setClasseJudicialSelecionada(br.jus.cnj.pje.ws.ClasseJudicial classeJudicialSelecionada) {
		this.classeJudicialSelecionada = classeJudicialSelecionada;
		
		if (classeJudicialSelecionada != null) {
			String codigo = classeJudicialSelecionada.getCodigo();
			
			ClasseJudicial classeJudicial = new ClasseJudicial();
			classeJudicial.setIdClasseJudicial(Integer.parseInt(codigo));
			classeJudicial.setCodClasseJudicial(codigo);
			getInstance().setClasseJudicial(classeJudicial);
		}
	}
	
	/**
	 * @param classeJudicial classeJudicial.
	 */
	public void setClasseJudicialSelecionada(ClasseJudicial classeJudicial) {
		br.jus.cnj.pje.ws.ClasseJudicial classeJudicialSelecionada = null;
		
		if (classeJudicial != null) {
			classeJudicialSelecionada = new br.jus.cnj.pje.ws.ClasseJudicial();
			classeJudicialSelecionada.setCodigo(classeJudicial.getCodClasseJudicial());
			classeJudicialSelecionada.setDescricao(classeJudicial.getClasseJudicial());
		}
		
		setClasseJudicialSelecionada(classeJudicialSelecionada);
	}

	/**
	 * @return colecaoClasseJudicial.
	 */
	public Collection<br.jus.cnj.pje.ws.ClasseJudicial> getColecaoClasseJudicial() {
		if (colecaoClasseJudicial == null) {
			colecaoClasseJudicial = new ArrayList<br.jus.cnj.pje.ws.ClasseJudicial>();
		}
		return colecaoClasseJudicial;
	}

	/**
	 * @param colecaoClasseJudicial colecaoClasseJudicial.
	 */
	public void setColecaoClasseJudicial(Collection<br.jus.cnj.pje.ws.ClasseJudicial> colecaoClasseJudicial) {
		this.colecaoClasseJudicial = colecaoClasseJudicial;
	}
	
	/**
	 * @param classe Classe Judicial.
	 * @return Filtro de classe judicial pelo ID.
	 */
	private Predicate novoFiltroClasseJudicialPeloId(final br.jus.cnj.pje.ws.ClasseJudicial classe) {
		return new Predicate() {
			
			@Override
			public boolean evaluate(Object objeto) {
				br.jus.cnj.pje.ws.ClasseJudicial classeTemp = (br.jus.cnj.pje.ws.ClasseJudicial) objeto;
				String idClasseTemp = (classeTemp != null ? classeTemp.getCodigo() : null);
				String idClasse = (classe != null ? classe.getCodigo() : null);
				return (idClasseTemp != null ? idClasseTemp.equals(idClasse) : false);
			}
		};
	}
	
	/**
	 * @param jurisdicao Jurisdição.
	 * @return Filtro de Jurisdicao pelo ID.
	 */
	private Predicate novoFiltroJurisdicaoPeloId(final br.jus.cnj.pje.ws.Jurisdicao jurisdicao) {
		return new Predicate() {
			
			@Override
			public boolean evaluate(Object objeto) {
				br.jus.cnj.pje.ws.Jurisdicao jurisdicaoTemp = (br.jus.cnj.pje.ws.Jurisdicao) objeto;
				Integer idJurisdicaoTemp = (jurisdicaoTemp != null ? jurisdicaoTemp.getId() : null);
				Integer idJurisdicao = (jurisdicao != null ? jurisdicao.getId() : null);
				return (idJurisdicaoTemp != null ? idJurisdicaoTemp.equals(idJurisdicao) : false);
			}
		};
	}
	
	/**
	 * @param classe Classe Judicial.
	 * @return Filtro de classe judicial pelo Código.
	 */
	private Predicate novoFiltroClasseJudicialPeloCodigo(final String codigo) {
		return new Predicate() {
			
			@Override
			public boolean evaluate(Object objeto) {
				br.jus.cnj.pje.ws.ClasseJudicial classe = (br.jus.cnj.pje.ws.ClasseJudicial) objeto;
				return (classe != null ? classe.getCodigo().equals(codigo) : false);
			}
		};
	}
	
	/**
	 * Consulta os endereços WSDL exceto a referência da instância atual.
	 */
	protected void consultarColecaoEnderecoWsdl() {
		consultarColecaoEnderecoWsdlExcetoInstanciaAtual();
		if (getColecaoEnderecoWsdl().size() == 1) {
			EnderecoWsdl endereco = (EnderecoWsdl) CollectionUtils.get(getColecaoEnderecoWsdl(), 0);
			setEnderecoWsdl(endereco);
		} else if (getColecaoEnderecoWsdl().isEmpty()) {
			String mensagem = "Não foi possível consultar os dados iniciais em razão de não haver um Endereço WSDL definido para a remessa.";
			throw new AplicationException(mensagem);
		}
	}
	
	/**
	 * Carregar o ID da tarefa.
	 */
	private void carregarTaskInstance() {
		TaskInstanceHome home = TaskInstanceHome.instance();
		home.setTaskId(TaskInstance.instance().getId());
	}
	
	/**
	 * Método responsável por abrir a aba "Partes".
	 */
	public void abrirAbaPartes() {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		if (getInstance() != processoTrf) {
			getInstance().getProcessoParteList().clear();
			getInstance().getProcessoParteList().addAll(processoTrf.getProcessoParteList());
		}
		setTab("tabPartes");
	}
	
    /**
     * Método responsável por pesquisar os {@link ProcessoDocumento} e popular o {@link EntityDataModel} para posterior utilização.
     */
    public void pesquisarDocumentos() {
    	ProcessoDocumentoManager processoDocumentoManager = ComponentUtil.getProcessoDocumentoManager();
    	DocumentoJudicialService documentoJudicialService = ComponentUtil.getDocumentoJudicialService();
		DataRetriever<ProcessoDocumento> dataRetriever = new ProcessoDocumentoRetriever(processoDocumentoManager, documentoJudicialService);		
		setProcessoDocumentoDataModel(new EntityDataModel<ProcessoDocumento>(ProcessoDocumento.class, this.facesContext, dataRetriever));
    }	
	
	/**
	 * Classe responsável pela paginação verdadeira da tabela de documentos vinculaveis ao processo.
	 */
	private class ProcessoDocumentoRetriever implements DataRetriever<ProcessoDocumento> {		
		private ProcessoDocumentoManager processoDocumentoManager;
		private DocumentoJudicialService documentoJudicialService;
		private ProcessoTrf processoTrf;
		
		public ProcessoDocumentoRetriever(ProcessoDocumentoManager processoDocumentoManager, DocumentoJudicialService documentoJudicialService) {
			this.processoDocumentoManager = processoDocumentoManager;
			this.documentoJudicialService = documentoJudicialService;
		}

		@Override
		public Object getId(ProcessoDocumento obj) {
			return processoDocumentoManager.getId(obj);
		}

		@Override
		public ProcessoDocumento findById(Object id) throws Exception {
			return processoDocumentoManager.findById(id);
		}

		@Override
		public List<ProcessoDocumento> list(Search search) {
			search.setMax(10);
			return documentoJudicialService.getDocumentos(getProcessoTrf(), search.getFirst(), search.getMax(), true, true, true, true, true);
		}

		@Override
		public long count(Search search) {
			return documentoJudicialService.getCountDocumentos(getProcessoTrf(), true, false, true);
		}

		/**
		 * @return processoTrf.
		 */
		private ProcessoTrf getProcessoTrf() {
			if (processoTrf == null) {
				processoTrf = EntityUtil.refreshEntity(getInstance());
				getEntityManager().detach(processoTrf);
			}
			return processoTrf;
		}
	}
	
	public EntityDataModel<ProcessoDocumento> getProcessoDocumentoDataModel() {
		return processoDocumentoDataModel;
	}

	public void setProcessoDocumentoDataModel(EntityDataModel<ProcessoDocumento> processoDocumentoDataModel) {
		this.processoDocumentoDataModel = processoDocumentoDataModel;
	}
	
	/**
	 * Retorna true se o assunto possuir descrição ou código conforme valor de pesquisa.
	 * 
	 * @param assunto
	 * @param pesquisar
	 * @return Booleano.
	 */
	public Boolean isDescricaoEhCodigo(AssuntoTrf assunto, String pesquisar) {
		return (assunto != null && (
			StringUtils.containsIgnoreCase(assunto.getAssuntoTrf(), pesquisar) || 
			StringUtils.containsIgnoreCase(assunto.getCodAssuntoTrf(), pesquisar)));
	}
	
	/**
	 * Verifica se existe dados para remeter para o segundo grau.
	 * 
	 */
	public boolean isRemessaGravada() {
		return (getVariavelProcessoTrf() != null && getVariavelEnderecoWsdl() != null);
	}
	
	/**
	 * Método responsável por verificar a existência de um assunto principal associado ao processo a ser remetido
	 * @return Retorna "true" caso exista um assunto principal associado ao processo a ser remetido e "false" caso contrário
	 */
	private boolean verificarExistenciaAssuntoPrincipalAssociado() {
		if (ProjetoUtil.isNotVazio(getInstance().getProcessoAssuntoList())) {
			for (ProcessoAssunto processoAssunto : getInstance().getProcessoAssuntoList()) {
				if (processoAssunto.getAssuntoPrincipal()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Método responsável por verificar a existência de um assunto não complementar associado ao processo a ser remetido
	 * @return Retorna "true" caso exista um assunto não complementar associado ao processo a ser remetido e "false" caso contrário
	 */
	private boolean verificarExistenciaAssuntoNaoComplementar() {
		if (ProjetoUtil.isNotVazio(getInstance().getProcessoAssuntoList())) {
			for (ProcessoAssunto processoAssunto : getInstance().getProcessoAssuntoList()) {
				if (!processoAssunto.getAssuntoTrf().getComplementar()) {
					return true;
				}
			}
		}
		
		return false;
	}
}