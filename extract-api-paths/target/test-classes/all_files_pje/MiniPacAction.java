package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;

import org.apache.commons.collections.Closure;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.jbpm.TaskInstanceHome;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.entidades.vo.MiniPacVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.DocumentoJudicialService;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoManager;
import br.jus.cnj.pje.nucleo.manager.TaskInstanceUtil;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.DomicilioEletronicoService;
import br.jus.cnj.pje.nucleo.service.EnderecoService;
import br.jus.cnj.pje.nucleo.service.MiniPacService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.cnj.pje.view.UnifiedEntityDataModel;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoCalculoMeioComunicacaoEnum;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Search;

/**
 * Classe action para tratar as interações do frame do miniPac (xhtml/components/form/miniPAC.xhtml).
 * 
 * O miniPAC busca agilizar a confecção de atos de comunicação, trazendo para a tela de minuta a opção
 * de indicar que o documento ora minutado gerará um expediente quando de sua assinatura.
 * 
 * O frame pode ser acoplado a qualquer tarefa, notadamente àquelas com editor de texto. Para tanto, basta
 * que o administrador inclua o componente 'miniPAC' nas variáveis da tarefa.
 * 
 * Os meios de comunicação que serão exibidos no frame também são configuráveis em fluxo, pela variável de tarefa
 * {@link PreparaAtoComunicacaoAction#VAR_MEIOSCOMUNICACOES}. O administrador deve definir a variável retro mencionada com a lista dos meios
 * desejados, por exemplo, 'P,D,C,M'. Se a variável não estiver definida, os meios 
 * {@link ExpedicaoExpedienteEnum#E} Sistema, {@link ExpedicaoExpedienteEnum#M} Central de Mandados e {@link ExpedicaoExpedienteEnum#P}
 * Diário Eletrônicos são exibidos por padrão.
 * 
 * Se o administrador quiser disponibilizar o miniPAC para geração de atos de comunicação para documentos já existentes (assinados),
 * basta adicionar o componente miniPAC em tarefa sem editor de textos e adicionar a variável {@link Variaveis.PJE_FLUXO_MINI_PAC_DOCUMENTO_EXISTE}
 * com o valor true (boolean). Isso fará com que o miniPAC permita ao usuário escolher o documento principal a ser utilizado como ato
 * de comunicação, além de vincular outros documentos.
 * 
 * Quando utilizado em tarefas com editor de texto (Minuta), 
 * cada {@link ProcessoParte} escolhida pelo usuário gerará um objeto VO ({@link MiniPacVO},
 * cada um com seus prazos, meios de comunicação, endereços e demais propriedades de um ato de domunicação, 
 * que será gravado em contexto para posterior recuperação, quando do assinatura do documento.
 * Ao assinar o documento, a variável será lida do contexto e os expedientes serão criados, conforme definido pelo servidor.
 * Mais detalhes em {@link MiniPacService#processarMiniPac(ProcessoTrf, ProcessoDocumento)}
 * 
 * É possível configurar a tarefa de assinatura para que o usuário que assina possa revisar e alterar os dados gravados pelo servidor.
 * 
 * Quando utilizado em tarefas sem editor de texto (Documentos já existentes),
 * a geração dos expedientes ocorre assim que o servidor escolher as partes, meios de comunicação, prazos, documento principal e
 * demais informações do expedietes. Sem necessidade de criação/assinatura de novo documento.
 * 
 */
@Name("miniPacAction")
@Scope(ScopeType.CONVERSATION)
public class MiniPacAction extends TramitacaoFluxoAction implements Serializable {

	private static final long serialVersionUID = 1L;

	@In
	private DocumentoJudicialService documentoJudicialService;

	@In
	private ProcessoDocumentoManager processoDocumentoManager;

	@In
	private AtoComunicacaoService atoComunicacaoService;

	@In
	private MiniPacService miniPacService;

	@In
	protected FacesContext facesContext;

	private static Map<String, String> prms;

	private Map<Integer, Boolean> documentosSelecionados;
	private Map<ExpedicaoExpedienteEnum, Boolean> meiosSelecionadosMap;
	private Map<Endereco, Boolean> enderecosSelecionados = new HashMap<>();
	private Map<Integer, Set<ExpedicaoExpedienteEnum>> miniPacMeioProibido = new HashMap<>();

	private List<MiniPacVO> miniPacVOList;
	private List<ProcessoParteParticipacaoEnum> polosSelecionadosList;
	private List<Endereco> enderecosPossiveis;

	private EntityDataModel<ProcessoDocumento> processoDocumentoDataModel;
	private Integer idDocPrincipal;
	private ProcessoDocumento processoDocumentoPrincipal;
	private MiniPacVO destinatarioSelecionado;
	private String prazoGeral;
	private String prazoIndividual;
	private String mensagemPossivelAtrasoEnvioDomicilio;
	private Boolean intimacaoPessoalGeral = Boolean.FALSE;
	private Boolean intimacaoUrgenteGeral = Boolean.FALSE;
	private Boolean isProcessoDocJaExiste = Boolean.FALSE;
	private Boolean isContemParteDomicilioEletronico = Boolean.FALSE;
	private Boolean isNaoContemParteDomicilioEletronico = Boolean.FALSE;
	private boolean exibirDivOutrosDestinatarios = false;

	String[] meiosDefinidosFluxo;

	private List<TipoProcessoDocumento> tiposDocumentosDisponiveis;
	private TipoProcessoDocumento tipoProcessoDocumentoSelecionado;

	/**
     * @see br.jus.cnj.pje.view.fluxo.TramitacaoFluxoAction#init()
     * 
     * Busca saber se o administrador definiu a variável de tarefa {@link PreparaAtoComunicacaoAction#VAR_MEIOSCOMUNICACOES}
     * com informações dos meios que serão exibidos no frame miniPAC.
     * Cada tarefa pode ter uma configuração diferente.
     * 
     * Além disso, recupera possíveis dados já gravados em uma iteração anterior do usuário para exibição.
     */
	@Override
	@Create
	public void init() {
		super.init();
		filtrarMeiosDeExpedientes();
		inicializarMapas();
	}

    /**
     * Iniciliza variáveis utilizadas pelo frame miniPAC, tais como os objetos {@link MiniPacVO} gravados anteriormente,
     * os meios de comunicação indicados em fluxo que serão exibidos no frame, o prazo geral padrão a ser utilizado na tarefa,
     * possíveis documentos a serem utilizados nos atos de comunicação, se o frame será utilizado para geração de atos de comunicação 
     * utilizando-se documentos já existente no sistema etc.
     * 
     * Se o administrador não definir no fluxo o prazo geral padrão, o sistema utilizará 15 (quinze) dias, como indicado pelo novo CPC.
     */
    @SuppressWarnings("unchecked")
	private void inicializarMapas() {
        documentosSelecionados = new HashMap<>();
        polosSelecionadosList = new ArrayList<>(3);
        
        miniPacVOList = (List<MiniPacVO>) tramitacaoProcessualService.recuperaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_LIST_VO);
        // É usado quando usuário seleciona um meio em que o expediente não pode ser enviado.
        miniPacMeioProibido = new HashMap<>();
        
    	isProcessoDocJaExiste = (Boolean) tramitacaoProcessualService.recuperaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_DOCUMENTO_EXISTE);
    	
    	if (isProcessoDocJaExiste == null){
    	    isProcessoDocJaExiste = Boolean.FALSE;
    	}
    	
    	prazoGeral = (String) tramitacaoProcessualService.recuperaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_PRAZO_GERAL);
    	if (!StringUtil.isSet(prazoGeral)){
    	    prazoGeral = "15";
    	}
    	prazoIndividual = prazoGeral;    

        if (ProjetoUtil.isNotVazio(miniPacVOList)) {        	
            if (!CollectionUtilsPje.isEmpty(CollectionUtilsPje.selectFilteredCollection(miniPacVOList, "inParticipacao", ProcessoParteParticipacaoEnum.A))){
                polosSelecionadosList.add(ProcessoParteParticipacaoEnum.A);
            }
            if (!CollectionUtilsPje.isEmpty(CollectionUtilsPje.selectFilteredCollection(miniPacVOList, "inParticipacao", ProcessoParteParticipacaoEnum.P))){
                polosSelecionadosList.add(ProcessoParteParticipacaoEnum.P);
            }
            if (!CollectionUtilsPje.isEmpty(CollectionUtilsPje.selectFilteredCollection(miniPacVOList, "inParticipacao", ProcessoParteParticipacaoEnum.T))){
                polosSelecionadosList.add(ProcessoParteParticipacaoEnum.T);
            }
        } else {
            miniPacVOList = new ArrayList<>(0);
        }
 	   
    	List<ProcessoDocumento> documentosVinculadosList = (List<ProcessoDocumento>) tramitacaoProcessualService.recuperaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_DOCS_VINCULADOS);
    	
    	if((isProcessoDocJaExiste != null && isProcessoDocJaExiste) || documentosVinculadosList != null){
    		idDocPrincipal = (Integer) tramitacaoProcessualService.recuperaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_DOC_PRINCIPAL);
    		pesquisarDocumentos();
    	}
		    	
    	if(documentosVinculadosList != null){
	    	for(ProcessoDocumento pd : documentosVinculadosList){
	    		documentosSelecionados.put(pd.getIdProcessoDocumento(), true);
	    	}
    	}
    }

    /**
     * Método responsável por verificar os meios de comunicação definidos na variável {@link PreparaAtoComunicacaoAction#VAR_MEIOSCOMUNICACOES}.
     * Caso a váriavel não tenha sido definida na tarefa os meios abaixo serão utilizados:
     * 
     *  <li> ExpedicaoExpedienteEnum.E (Enviar Via Sistema)
     *  <li> ExpedicaoExpedienteEnum.P (Diário Eletrônico)
     *  <li> ExpedicaoExpedienteEnum.M (Central de Mandados)
     */
    private void filtrarMeiosDeExpedientes() {
        if (meiosSelecionadosMap == null) {
            meiosSelecionadosMap = new EnumMap<ExpedicaoExpedienteEnum, Boolean>(ExpedicaoExpedienteEnum.class);
            String valorVariavel = (String) tramitacaoProcessualService.recuperaVariavelTarefa(PreparaAtoComunicacaoAction.VAR_MEIOSCOMUNICACOES);
            if (valorVariavel != null && valorVariavel instanceof String) {
                meiosDefinidosFluxo = ((String) valorVariavel).split(",");
                for (String meio : meiosDefinidosFluxo) {
                    meiosSelecionadosMap.put(ExpedicaoExpedienteEnum.valueOf(meio.trim()), Boolean.TRUE);
                }
            } else {
                meiosSelecionadosMap.put(ExpedicaoExpedienteEnum.E, Boolean.TRUE);
                meiosSelecionadosMap.put(ExpedicaoExpedienteEnum.P, Boolean.TRUE);
                meiosSelecionadosMap.put(ExpedicaoExpedienteEnum.M, Boolean.TRUE);
            }
        }
    }

    /**
     * Método responsável pelo controle da aplicação do meio de comunicação ({@link ExpedicaoExpedienteEnum}) ao(s) destinatário(s) do(s) expediente(s).
     * 
     * Sempre que o botão relativo a um meio de comunicação for pressionado, o método irá, dependendo do estado atual,
     * adicionar ou remover (toggle) o meio indicado às partes já selecionadas no frame.
     * 
     * Se não hover partes já selecionadas, o método apenas indica que o meio indicado está selecionado/removido 
     * para qualquer parte que futuramente for selecionada.
     * 
     * Quando estiver adicionando um meio de comunicação:
     * <li> para cada parte que estiver selecionada será verificado essa parte
     * deve ser notificada preferencialmente por meio eletrônico, conforme determina o novo CPC. Órgãos públicos, entidades,
     * pessoas jurídicas (que não sejam de pequeno ou médio porte), terão automaticamente o meio de comunicaçã eletrônico adicionado,
     * desde que a parte tenha os pré-requisitos para ser notificada via sistema (ter se registrado no Pje com certificado digital, por exemplo).
     * <li> se a parte já selecionada não puder ser notificada pelo meio indicado, o método irá adicionar a parte em um mapa
     * para melhor exibir ao usuário quais partes puderam ser notificadas pelo meio indicado e quais não.
     *  
     * @param meio Meio de Comunicação que será utilizado na criação no expediente.
     */
	public void toggleMeio(ExpedicaoExpedienteEnum meio) {
		Boolean isIncluir = meiosSelecionadosMap.get(meio);
		Integer idProcessoParte;
		Set<ExpedicaoExpedienteEnum> meiosProibidos;

		for (MiniPacVO miniPacVO : miniPacVOList) {
			idProcessoParte = Integer.valueOf(miniPacVO.getIdProcessoParte());

			if (isIncluir && miniPacVO.getAtivo()) {
				if (!isMeioProibido(miniPacVO, meio)) {
					if (!miniPacVO.getMeios().contains(meio)) {
						if (atoComunicacaoService.isPodeInserirMeio(miniPacVO.getProcessoParte(), meio, miniPacVO.isPessoal())) {
						miniPacVO.getMeios().add(meio);
					} else {
						meiosProibidos = miniPacMeioProibido.get(idProcessoParte);
						if (meiosProibidos == null) {
							meiosProibidos = new HashSet<>();
						}
						meiosProibidos.add(meio);
						miniPacMeioProibido.put(idProcessoParte, meiosProibidos);
					}
				}
                }
			} else {
				miniPacVO.getMeios().remove(meio);
				meiosProibidos = miniPacMeioProibido.get(idProcessoParte);
				if (meiosProibidos == null) {
					meiosProibidos = new HashSet<>();
				}
				meiosProibidos.remove(meio);
				miniPacMeioProibido.put(idProcessoParte, meiosProibidos);
			}
		}
		meiosSelecionadosMap.put(meio, !isIncluir);
	}

	/**
     * Método responsável pelo controle da aplicação do Meio de Comunicação ({@link ExpedicaoExpedienteEnum}) a um determinado Destinatário ({@link MiniPacVO})
	 * 
     * Quando o usuário clicar na célula que faz interseção da parte com o meio de comunicação, o método irá adicionar/remover (toggle) o 
     * {@link ExpedicaoExpedienteEnum} naquela {@link ProcessoParte} / {@link MiniPacVO} específica(o).
     * 
     * Quando estiver adicionando um meio de comunicação:
     * <li> se a parte já selecionada não puder ser notificada pelo meio indicado, o método irá adicionar a parte em um mapa
     * para melhor exibir ao usuário quais partes puderam ser notificadas pelo meio indicado e quais não. 
	 * 
	 * @param miniPacVO Destinatário do expediente.
     * @param meio Meio de Comunicação que será utilizado na criação no expediente;
	 */
	public void toggleMeio(MiniPacVO miniPacVO, ExpedicaoExpedienteEnum meio) {
		Integer idProcessoParte = Integer.valueOf(miniPacVO.getIdProcessoParte());
		Set<ExpedicaoExpedienteEnum> meiosProibidos;

		if (!miniPacVO.getMeios().remove(meio)) {
			if (!isMeioProibido(miniPacVO, meio)) {
				meiosProibidos = miniPacMeioProibido.containsKey(idProcessoParte)
						? miniPacMeioProibido.get(idProcessoParte)
						: new HashSet<>();

				if (!ExpedicaoExpedienteEnum.E.equals(meio) ||
						atoComunicacaoService.isPodeInserirMeio(miniPacVO.getProcessoParte(), meio, miniPacVO.getPessoal())) {
					miniPacVO.getMeios().add(meio);
				} else {
					meiosProibidos.add(meio);
					miniPacMeioProibido.put(idProcessoParte, meiosProibidos);
				}
			}
		} else {
			meiosProibidos = miniPacMeioProibido.containsKey(idProcessoParte) 
					? miniPacMeioProibido.get(idProcessoParte)
					: new HashSet<>();
			meiosProibidos.remove(meio);
			miniPacMeioProibido.put(idProcessoParte, meiosProibidos);
		}
	}
    
    public void onFrameLoad() {
		DomicilioEletronicoService domicilio = DomicilioEletronicoService.instance();
		if (domicilio.isIntegracaoHabilitada() && domicilio.isUtilizaAlertaDomicilioOffline() && !domicilio.isOnline() ) {
			facesMessages.add(Severity.ERROR, "Domicílio Eletrônico offline! Os expedientes serão criados no PJe mas não serão enviados ao Domicílio Eletrônico neste momento.");
		}
	}

	public boolean isOrgaoPublico(MiniPacVO vo) {
		if (vo == null || vo.getPessoa() == null) {
			return false;
		}

		return TipoPessoaEnum.J.equals(vo.getPessoa().getInTipoPessoa())
				&& ((PessoaJuridica) vo.getPessoa()).getOrgaoPublico() 
				&& vo.getIsHabilitaDomicilioEletronico();
	}

	public boolean isPessoaJuridicaHabilitadaDomicilio(MiniPacVO vo) {
		if (vo == null || vo.getPessoa() == null) {
			return false;
		}

		return TipoPessoaEnum.J.equals(vo.getPessoa().getInTipoPessoa())
				&& !((PessoaJuridica) vo.getPessoa()).getOrgaoPublico()
				&& vo.getIsHabilitaDomicilioEletronico() == true;
	}

	public boolean isPessoaFisicaHabilitadaDomicilio(MiniPacVO vo) {
		if (vo == null || vo.getPessoa() == null) {
			return false;
		}

		return TipoPessoaEnum.F.equals(vo.getPessoa().getInTipoPessoa())
				&& vo.getIsHabilitaDomicilioEletronico() == true;
	}

	/**
	* Método auxiliar para indicar se um determinado {@link ProcessoParte} / {@link MiniPacVO}
	* pode ou não ser receber ato de comunicação por um meio {@link ExpedicaoExpedienteEnum} também específico. 
	* 
	* @param miniPacVO
	* @param meio
	* @return
	*/
	public boolean isMeioProibido(MiniPacVO miniPacVO, ExpedicaoExpedienteEnum meio) {
		if (miniPacVO == null) {
			return false;
		}

		Integer idProcessoParte;
		try {
			idProcessoParte = Integer.valueOf(miniPacVO.getIdProcessoParte());
		} catch (NumberFormatException e) {
			return false;
		}

		Set<ExpedicaoExpedienteEnum> meiosProibidos = miniPacMeioProibido.get(idProcessoParte);
		if (meiosProibidos != null && meiosProibidos.contains(meio)) {
			return true;
		}

		return false;
	}

	public boolean exibirMeio(MiniPacVO miniPac, ExpedicaoExpedienteEnum meio) {
		if (miniPac == null) {
			return false;
		}

		if (isOrgaoPublico(miniPac)) {
			return true;
		}

		boolean tipoAtoProibido = false;

		if (miniPac.getTipoProcessoDocumento() != null) {
			String tipoAto = miniPac.getTipoProcessoDocumento().getTipoProcessoDocumento();
			if (tipoAto != null) {
				tipoAtoProibido = "Citação".equalsIgnoreCase(tipoAto.trim())
						|| "Intimação".equalsIgnoreCase(tipoAto.trim())
						|| "Notificação".equalsIgnoreCase(tipoAto.trim());
			}
		}

		if (tipoAtoProibido && !miniPac.getPessoal() && ExpedicaoExpedienteEnum.E.equals(meio)) {
			return false;
		}

		if (ExpedicaoExpedienteEnum.E.equals(meio) && isMeioProibido(miniPac, ExpedicaoExpedienteEnum.E)) {
			return false;
		}

		return true;
	}

    /**
     * [PJEII-20844]
     * Verifica se o expediente é do tipo que exige o tipo de calculo de prazo
     * @param vo
     * @return boolean
     */
    public boolean isExibeTipoCalculoPrazo(MiniPacVO minipac, ExpedicaoExpedienteEnum expe){
    	boolean estaNaLista = minipac != null && minipac.getMeios() != null && minipac.getMeios().contains(expe);
    	if(ExpedicaoExpedienteEnum.M.equals(expe) && estaNaLista){
    		if(minipac != null && minipac.getTipoCalculo() == null ){
    			minipac.setTipoCalculo(obterTipoCalculoPelaCategoriaDaCompetencia(processoJudicial.getCompetencia()));
    		}
    		return true;
    	}else{
    		return false;
    	}
    }
    
	/**
	 * [PJEII-20844] - A opção padrão que virá selecionada no PAC deve ser buscada a partir de uma marcação equivalente que deverá 
	 * existir na competência do processo relacionado (PJEVII-1471).
	 */
	private TipoCalculoMeioComunicacaoEnum obterTipoCalculoPelaCategoriaDaCompetencia(Competencia competencia){
		return ( competencia == null ) ? TipoCalculoMeioComunicacaoEnum.CD : competencia.getTipoCalculoMeioComunicacao();
	}
    
	/**
	 * Obtém os tipos de prazo da central de mandados disponíveis para seleção pelo usuário.
	 * 
	 * @return sequência de tipos de prazo central de mandados disponíveis
	 */
	public List<TipoCalculoMeioComunicacaoEnum> obtemTiposPrazoCentralMandado(){
		return Arrays.asList(TipoCalculoMeioComunicacaoEnum.values());
	}
    
    /**
     * Método utilizado para efetivamente gravar as informações do frame em variável de contexto para posterior recuperação.
     * 
     * Se o usuário não selecionar nenhum destinatário e mandar gravar os dados do miniPAC, a variável de contexto será apagada.
     * Isso permite que o usuário "cancele" a criação de atos de comunicação previamente configurados.
     * 
     * Os dados gravados são os seguintes:
     * <li> a lista de objetos {@link MiniPacVO}, representando os destinatários dos atos de comunicação, na variável {@link Variaveis#PJE_FLUXO_MINI_PAC_LIST_VO}
     * <li> o possível documento existente a ser utilizado como ato de comunicação, na variável {@link Variaveis#PJE_FLUXO_MINI_PAC_DOC_PRINCIPAL}
     * <li> os possíveis documentos vinculados ao documento principal utilizado como ato de comunicação, na variável {@link Variaveis#PJE_FLUXO_MINI_PAC_DOCS_VINCULADOS}
     */
    public void sincronizarVariavelVOContexto() {
    	if (!miniPacVOList.isEmpty()){
    		for(MiniPacVO destinatario : miniPacVOList){
    			if (Boolean.TRUE.equals(destinatario.getAtivo()) && destinatario.getMeios().isEmpty()) {
    				facesMessages.add(Severity.ERROR, "É necessário selecionar pelo menos um Meio de Comunicação por destinatário!");
    				return;
    			}

				if (destinatario.getAtivo() == true 
						&& destinatario.getIsHabilitaDomicilioEletronico() == true
						&& destinatario.getTipoProcessoDocumento() == null) {
					facesMessages.add(Severity.ERROR,
							"É necessário selecionar o Tipo de Comunicação por destinatário cadastrado no domicílio!");
					return;
				}
        	}
    		
            tramitacaoProcessualService.gravaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_LIST_VO, miniPacVOList);
            if(!documentosSelecionados.isEmpty()){
                tramitacaoProcessualService.gravaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_DOCS_VINCULADOS, obterDocumentosSelecionados());
            }
            if(processoDocumentoPrincipal != null){
                tramitacaoProcessualService.gravaVariavel(Variaveis.PJE_FLUXO_MINI_PAC_DOC_PRINCIPAL, processoDocumentoPrincipal.getIdProcessoDocumento());
            }
            facesMessages.add(Severity.INFO, "Dados do(s) ato(s) de comunicação gravados com sucesso!");
        } else {
            miniPacService.apagarVariaveisMiniPacContexto();
        }
    }
    
	/**
	 * Método responsável por criar os atos de comunicação (ver {@link MiniPacService#processarMiniPac(ProcessoTrf, ProcessoDocumento)} configurados no frame, 
	 * nos casos onde o administrador tenha configurado a tarefa para utilizar documento já existente no sistema.
	 *  
	 * Se o usuário não tiver indicado no frame algum destinatário ({@link MiniPacVO} ou documento principal, 
	 * o sistema se recusará a criar qualquer expediente e exibirá mensagem.
	 * 
	 * Caso a criação do(s) ato(s) de comunicação ocorra sem erros, o processo será movimentado para a transição de saída padrão, se existir.
	 */
	public void finalizarExpedientes() {
    	if(processoDocumentoPrincipal == null || !processoDocumentoPrincipal.getSelected()){
			facesMessages.add(Severity.ERROR, "É necessário selecionar o documento principal para geração do expediente!");
			return;
    	}    	    	
    	
    	if (CollectionUtilsPje.isEmpty(miniPacVOList)) {
			facesMessages.add(Severity.ERROR, "É necessário selecionar um destinatário!");
			return;
		}
    	
		if (miniPacVOList.stream()
				.anyMatch(destinatario -> destinatario.getAtivo() == true
						&& destinatario.getIsHabilitaDomicilioEletronico() == true
						&& destinatario.getTipoProcessoDocumento() == null)) {
			facesMessages.add(Severity.ERROR, "É necessário selecionar o Tipo de Comunicação por destinatário cadastrado no domicílio!");
			return;
    	}
    	
    	boolean haUsuarioAtivo = false;
    	for(MiniPacVO destinatario : miniPacVOList){
			if (Boolean.TRUE.equals(destinatario.getAtivo())) {
				if (destinatario.getMeios().isEmpty()) {
					facesMessages.add(Severity.ERROR, "É necessário selecionar pelo menos um Meio de Comunicação por destinatário!");
					return;
				} else if (destinatario.getMeios().contains(ExpedicaoExpedienteEnum.C) && CollectionUtilsPje.isEmpty(destinatario.getEnderecos())) {
					facesMessages.add(Severity.ERROR, "O destinatário \"" + destinatario.getNome() + "\" não possui endereços.");
					return;
				}
				
				haUsuarioAtivo = true;
			}
    	}
    	
    	//Caso em que o usuário exclua todos os destinatários deixando todos inativos
    	if (!haUsuarioAtivo) {
			facesMessages.add(Severity.ERROR, "Todos os destinatários foram excluídos. É necessário ter pelo menos um destinatário.");
			return;
		}    	   

		String transicaoSaida = (String)TaskInstanceUtil.instance().getVariable(Variaveis.NOME_VARIAVEL_DEFAULT_TRANSITION);

		try{
			if (!processoDocumentoPrincipal.getProcessoDocumentoBin().getSignatarios().isEmpty()){
			    sincronizarVariavelVOContexto();
			    miniPacService.processarMiniPac(processoDocumentoPrincipal.getProcessoTrf(), processoDocumentoPrincipal, true);
				if (transicaoSaida != null) {
					TaskInstanceHome.instance().end(transicaoSaida);
				}				
			} else {
				facesMessages.add(Severity.WARN, "Não é possivel criar expediente para documento não assinado!");
				return;
			}
		} catch (PJeDAOException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível criar expedientes. {0}: {1}.", e.getClass().getCanonicalName(), e.getLocalizedMessage());
		}
    }  

    /**
     * Método auxiliar responsável por obter a lista dos documentos escolhidos pelo usuário no frame miniPAC.
     * 
     * @return Lista de ProcessoDocumento contendo apenas os documentos que serão vinculados ao expediente.
     */
	private List<ProcessoDocumento> obterDocumentosSelecionados() {
		List<ProcessoDocumento> listDocumento = new ArrayList<>();
	 
		for (Entry<Integer, Boolean> entry : documentosSelecionados.entrySet()) {
			if (Boolean.TRUE.equals(entry.getValue())) {

				try {
					ProcessoDocumento docProcessoDocumento = processoDocumentoManager.findById(entry.getKey());
					listDocumento.add(docProcessoDocumento);
				} catch (PJeBusinessException e) {
					Logger.getLogger(MiniPacAction.class.getName()).log(Level.INFO, String.format(e.getMessage()));
				}
			}
		}

		return listDocumento.isEmpty() ? null : listDocumento;
	}
    
    /**
     * Método responsável por remover o destinatário ({@link MiniPacVO} informado da lista de destinatários do(s) expediente(s).
     * Utilizado quando o usuário quer remover um destinatário dentre vários do mesmo polo, por exemplo.
     * 
     * @param miniPacVO a ser removido da lista de destinatáros ativos.
     */
	public void inativarDestinatario(MiniPacVO miniPacVO) {
        miniPacVO.setAtivo(Boolean.FALSE);
        miniPacVO.getMeios().clear();
        miniPacVO.setPessoal(Boolean.FALSE);
        miniPacVO.setPrazo(0);
        
        if(!miniPacVOList.isEmpty()) {
        	isNaoContemParteDomicilioEletronico = miniPacVOList.stream().anyMatch(parte -> 
			parte.getIsHabilitaDomicilioEletronico() == Boolean.TRUE && parte.getAtivo() == Boolean.TRUE);
        }
    }

    /**
     * Método responsável por reativar o destinatário ({@link MiniPacVO} informado da lista de destinatários do(s) expediente(s).
     * Utilizado quando o usuári tiver inativado anteriormente um destinatário. ({@link #inativarDestinatario(MiniPacVO)}
     * 
     * @param miniPacVO
     */
    public void reativarDestinatario(MiniPacVO miniPacVO) {
        miniPacVO.setAtivo(Boolean.TRUE);
        miniPacVO.setPrazo(Integer.parseInt(getPrazoGeral()));
        miniPacVO.setPessoal(getIntimacaoPessoalGeral());
        for (ExpedicaoExpedienteEnum meio : meiosSelecionadosMap.keySet()) {
            if (Boolean.FALSE.equals(meiosSelecionadosMap.get(meio))){
                miniPacVO.getMeios().add(meio);
            }            
        }
        if(!miniPacVOList.isEmpty()) {
        	isNaoContemParteDomicilioEletronico = miniPacVOList.stream().anyMatch(parte -> 
			parte.getIsHabilitaDomicilioEletronico() == Boolean.TRUE && parte.getAtivo() == Boolean.TRUE);
        }
    }
    
    /**
     * Método responsável por definir se o alerta de que o endereço utilizado no expediente é do representante.
     * Normalmente o endereço utilizado em um ato de comunicação é do representante da parte.
     * Para ficar claro ao usuário que o endereço exibido no frame é do representante e não da parte, um ícone é exibido após o endereço.
     * 
     * Se o ato de comunicação for pessoal, não haverá alerta/ícone, já que o endereço a ser utilizado será o da própria parte.
     * 
     * @param miniPacVO
     * @return <b>true</b> se o expediente não for pessoal e a parte possui representante.
     */
    public Boolean exibirAlertaEnderecoRepresentante(MiniPacVO miniPacVO){
    	if(Boolean.TRUE.equals(miniPacVO.isPessoal())){
    		return Boolean.FALSE;
    	}else{
    		return !miniPacVO.getProcessoParte().getProcessoParteRepresentanteList().isEmpty();
    	}
    }    
    
    /**
     * Método responsável por alterar o endereço utilizado no expediente, 
     * quando o usuário indicar que o ato de comunicação será pessoal ou não.
     * 
     * Se o ato de comunicação for pessoal, o endereço da parte será utilizado.
     * Caso contrário, o endereço do representante será utilizado.
     * 
     * O endereço incluído/alterado mais recente no sistema será o utilizado, caso haja mais de um.
     *  
     * @param miniPacVO
     */
	public void alterarEndereco(MiniPacVO miniPacVO) {
		if (miniPacVO.getProcessoParte() != null) {
			miniPacVO.getEnderecos().clear();
			Endereco endereco = atoComunicacaoService.getMelhorEnderecoParaComunicacao(miniPacVO.getProcessoParte(), miniPacVO.isPessoal());
			if (endereco != null) {
				if (!miniPacVO.getEnderecos().contains(endereco)) {
					miniPacVO.getEnderecos().add(endereco);
				}
			} else {
				facesMessages.add(Severity.ERROR, "Não foi possível encontrar um endereço.");
			}
		}
	}
    
    /**
     * Método responsável por pesquisar os {@link ProcessoDocumento} e popular o {@link EntityDataModel} para posterior utilização.
     */
    public void pesquisarDocumentos(){
		DataRetriever<ProcessoDocumento> dataRetriever = new ProcessoDocumentoRetriever(processoDocumentoManager, documentoJudicialService);		
		processoDocumentoDataModel = new EntityDataModel<ProcessoDocumento>(ProcessoDocumento.class, this.facesContext, dataRetriever);
    }
        
	/**
	 * Método responsável por atualizar a lista de documentos selecionados para serem utilizado no ato de comunicação,
	 * indicando qual é o documento principal.
	 */
	public void selecionarDocumentoPrincipal(){
		for(ProcessoDocumento pDoc : processoDocumentoDataModel.getPage()){
			pDoc.setSelected(false);	
    	}
		processoDocumentoPrincipal.setSelected(true);		
		documentosSelecionados.put(processoDocumentoPrincipal.getIdProcessoDocumento(), false);
	}
	
	/**
	 * Método responsável por atualizar a lista de endereços a serem utilizados no ato de comunicação.
	 * O frame miniPAC permite que o servidor possa indicar mais de um endereço para o expediente,
	 * bastando clicar na célula do endereço para que um popup para seleção de outros endereços seja exibido.
	 * 
	 * Ver /Processo/Fluxo/expedientes/miniPacEnderecos.xhtml
	 */
	public void selecionarEnderecos(){
		List<Endereco> listEnderecos = new ArrayList<>();		
		for(Entry<Endereco, Boolean> endereco : enderecosSelecionados.entrySet()){
			if(endereco.getValue()){
				listEnderecos.add(endereco.getKey());
			}
		}
		if(listEnderecos.isEmpty()){
			facesMessages.add(Severity.ERROR, "É necessário selecionar pelo menos um endereço!");
			return;
		}
		destinatarioSelecionado.getEnderecos().clear();
		destinatarioSelecionado.getEnderecos().addAll(listEnderecos);
	}
	
	public Boolean getEnderecoSelecionado(){		
		for(Entry<Endereco, Boolean> endereco : enderecosSelecionados.entrySet()){
			if(Boolean.TRUE.equals(endereco.getValue())){
				return true;
			}
		}
		return false;
	}

    /**
     * Método auxiliar para remover todo polo indicado ({@link ProcessoParteParticipacaoEnum} da lista de destinatários.
     * 
     * @param enumParticipacao polo a ser removido.
     */
    private void removerVOList(ProcessoParteParticipacaoEnum enumParticipacao) {
        List<MiniPacVO> filteredCollection = (List<MiniPacVO>) CollectionUtilsPje.selectFilteredCollection(miniPacVOList, "inParticipacao", enumParticipacao);
        miniPacVOList.removeAll(filteredCollection);
    }

    /**
     * Método auxiliar para adicionar todo polo indicado ({@link ProcessoParteParticipacaoEnum} à lista de destinatários.
     * 
     * Todas as partes ({@link ProcessoParte} do polo indicado são percorridas e, estando ativas,
     * um objeto VO ({@link MiniPacVO} é criado para cada uma e adicinado à lista de destinatários.
     * 
     * Ao adicionar cada parte, é verificado o seguinte:
     * <li> se o administrador de fluxo indicou que o frame trabalhará com o meio eletrônico ({@link ExpedicaoExpedienteEnum#E},
     * será verificado se a parte pode ser notificada via sistema. Em caso positivo, o meio eletrônico é adicionado automaticamente
     * àquela parte, independente de o usuário ter selecionado o meio antes. Isso é para atender o comando do novo CPC (Art. 246 § 1º)
     * <li> se o usuário já tiver selecinado algum meio de comunicação antes da seleção do polo, ao adicionar as partes do polo recém
     * indicado, o código tentará já incluir os meios já selecionados.
     * 
     * @param enumParticipacao polo a ser adicionado.
     */
    private void adicionarVOList(ProcessoParteParticipacaoEnum enumParticipacao) {
        MiniPacVO miniPacVO;
		List<ProcessoParte> processoParteList = new ArrayList<>();

        for(MiniPacVO vo : miniPacVOList){
        	if(vo.getProcessoParte().getInParticipacao().equals(enumParticipacao)){
        		processoParteList.add(vo.getProcessoParte());
        	}
        }

        if(processoParteList.isEmpty()){
        	processoParteList = processoJudicial.getListaPartePrincipal(enumParticipacao);
        }        

		// Filtra a lista de partes para retirar elementos repetidos
		List<ProcessoParte> processoParteFiltradaList = processoParteList.stream()
																		 .distinct()
																		 .collect(Collectors.toList());

		processoParteList = processoParteFiltradaList;

		// Para cada parte ativa, carrega o MiniPacVO e adiciona à lista
        for (ProcessoParte processoParte : processoParteList) {        	
			if (Boolean.FALSE.equals(processoParte.getIsBaixado())) {
                miniPacVO = miniPacService.carregaMiniPacVO(processoParte, getIntimacaoPessoalGeral(), this.prazoGeral);

				boolean isPessoaHabilitadaDomicilioEletronico = isPessoaHabilitadaDomicilioEletronico(processoParte.getPessoa());

				miniPacVO.setIsHabilitaDomicilioEletronico(isPessoaHabilitadaDomicilioEletronico);
                
                if(this.tipoProcessoDocumentoSelecionado != null && miniPacVO.getTipoProcessoDocumento() == null) {
                	miniPacVO.setTipoProcessoDocumento(this.tipoProcessoDocumentoSelecionado);
                }

				// Se for órgão público, marque automaticamente o meio "Sistema"
				if (isOrgaoPublico(miniPacVO)) {
					if (!miniPacVO.getMeios().contains(ExpedicaoExpedienteEnum.E)) {
						miniPacVO.getMeios().add(ExpedicaoExpedienteEnum.E);
					}
				}

                processarMeiosJaIndicados(miniPacVO);                          	 
                miniPacVOList.add(miniPacVO);               
            }
        }
    }

    /**
     * Método auxiliar para incluir ao destinatário ({@link MiniPacVO} indicado, os meios previamente já selecionados no frame.
     * 
     *  @param miniPacVO destinatário a ser avaliado sobre a inclusão dos meios já selecionados no frame
     */
	private void processarMeiosJaIndicados(MiniPacVO miniPacVO) {
		for (Map.Entry<ExpedicaoExpedienteEnum, Boolean> entry : meiosSelecionadosMap.entrySet()) {
			ExpedicaoExpedienteEnum meio = entry.getKey();

			if (miniPacVO.getMeios().contains(meio)) {
				continue;
			}

			if (Boolean.FALSE.equals(meiosSelecionadosMap.get(meio)) 
					&& (atoComunicacaoService.isPodeInserirMeio(miniPacVO.getProcessoParte(), meio, miniPacVO.isPessoal()))) {
				miniPacVO.getMeios().add(meio);
			}
		}
	}
    

	public Map<ExpedicaoExpedienteEnum, Boolean> getMeiosSelecionadosMap() {
        return meiosSelecionadosMap;
    }

    public String getPrazoGeral() {
        return prazoGeral;
    }

    /**
     * Atualiza o prazo indicado para todos os destinatários exibidos no frame.
     * @param prazoGeral
     */
    public void setPrazoGeral(final String prazoGeral) {
        this.prazoGeral = prazoGeral;
        CollectionUtils.forAllDo(miniPacVOList, new Closure() {
            public void execute(Object objeto) {
                ((MiniPacVO) objeto).setPrazo("".equals(prazoGeral) ? 0 : Integer.parseInt(prazoGeral));
            }
        });
    }         
    
    @Override
    protected Map<String, String> getParametrosConfiguracao() {
        return prms;
    }

    /**
     * Retorna {@link String} contendo os endereços (um por linha) do destinatário ({@link MiniPacVO} indicado.
     * 
     * @param miniPacVO destinário cujos endereços devem ser retornados.
     * @return {@link String} contendo os endereços completos do destinatário indicado.
     */
    public String getEnderecoCompletoVO(MiniPacVO miniPacVO) {
        EnderecoService enderecoService = ComponentUtil.getComponent(EnderecoService.class);        
        StringBuilder sb = new StringBuilder();
        for(Endereco endereco : miniPacVO.getEnderecos()){
        	endereco = enderecoService.getEndereco(endereco.getIdEndereco());
        	if (endereco != null) {
        		sb.append(endereco.getEnderecoCompleto()).append("<br/>");
        	}
        }
        return sb.toString();
    }

    public ProcessoTrf getProcessoJudicial() {
        return processoJudicial;
    }

    public String getPrazoIndividual() {
        return this.prazoIndividual;
    }

    public void setPrazoIndividual(String prazo) {
        this.prazoIndividual = prazo;
    }

    public List<MiniPacVO> getMiniPacVOList() {
        return miniPacVOList;
    }

	public String getMensagemPossivelAtrasoEnvioDomicilio() {
		return mensagemPossivelAtrasoEnvioDomicilio;
	}

	public void setMensagemPossivelAtrasoEnvioDomicilio(String mensagemPossivelAtrasoEnvioDomicilio) {
		this.mensagemPossivelAtrasoEnvioDomicilio = mensagemPossivelAtrasoEnvioDomicilio;
	}

    /**
     * Atualiza todos os destinatários ({@link MiniPacVO}, definindo se o ato de comunicação é ou não (toggle) pessoal.
     * O endereço de cada destinatário afetado é atualizado, conforme a indicação do usuário no frame, 
     * pois para ato de comunicação pessoal, o endereço a ser utilizado é o da parte. Caso contrário, do representante, se houver. 
     */
	public void toggleIntimacaoPessoalGeral() {
		for (MiniPacVO miniPacVO : miniPacVOList) {
			if (isOrgaoPublico(miniPacVO)) {
				miniPacVO.setPessoal(false);
			} else {
				miniPacVO.setPessoal(getIntimacaoPessoalGeral());
				if (!miniPacVO.getPessoal() && miniPacVO.getTipoProcessoDocumento() != null) {
					String tipoAto = miniPacVO.getTipoProcessoDocumento().getTipoProcessoDocumento();
					if (tipoAto != null && 
							("Citação".equalsIgnoreCase(tipoAto.trim())
							|| "Intimação".equalsIgnoreCase(tipoAto.trim())
							|| "Notificação".equalsIgnoreCase(tipoAto.trim()))) {
						if (miniPacVO.getMeios().contains(ExpedicaoExpedienteEnum.E)) {
							miniPacVO.getMeios().remove(ExpedicaoExpedienteEnum.E);
						}
					}
				}
				alterarEndereco(miniPacVO);
			}
		}
	}

    /**
     * Atualiza todos os destinatários ({@link MiniPacVO}, definindo se o ato de comunicação é ou não (toggle) urgente.
     */
    public void toggleIntimacaoUrgenteGeral(){
    	for (MiniPacVO miniPacVO : miniPacVOList) {
            miniPacVO.setUrgente(getIntimacaoUrgenteGeral());
        }
    }
    
    /**
     * Atualiza o destinatário ({@link MiniPacVO} indicado, definindo se o ato de comunicação é ou não (toggle) urgente.
     * 
     * @param miniPacVO destinatário a ser atualizado
     */
    public void toggleIntimacaoUrgente(MiniPacVO miniPacVO){
    	miniPacVO.setUrgente(miniPacVO.getUrgente());
    }
    
    public Boolean getIntimacaoUrgenteGeral() {
		return intimacaoUrgenteGeral;
	}

    public void setIntimacaoUrgenteGeral(Boolean intimacaoUrgenteGeral) {
		this.intimacaoUrgenteGeral = intimacaoUrgenteGeral;
	}

	public Boolean getIntimacaoPessoalGeral() {
        return intimacaoPessoalGeral;
    }

    public void setIntimacaoPessoalGeral(Boolean intimacaoPessoalGeral) {
        this.intimacaoPessoalGeral = intimacaoPessoalGeral;
    }
    
    /**
     * Adiciona ou remove (toggle) todas as partes do polo ({@link ProcessoParteParticipacaoEnum} indicado.
     * Ver {@link #adicionarVOList(ProcessoParteParticipacaoEnum)} e {@link #removerVOList(ProcessoParteParticipacaoEnum)}
     * 
     * @param polo polo a ser adicionado ou removido da lista de destinatários do frame
     */
    public void togglePolo(ProcessoParteParticipacaoEnum polo) {
        if (!polosSelecionadosList.remove(polo)){
            polosSelecionadosList.add(polo);
            adicionarVOList(polo);            
        } else {
            removerVOList(polo);
        }
        
        verificaContemParteDomicilio();
    }

	private void verificaContemParteDomicilio() {
		if(CollectionUtilsPje.isNotEmpty(miniPacVOList)) {
          	isContemParteDomicilioEletronico = miniPacVOList.stream().anyMatch(parte -> 
          	parte.getIsHabilitaDomicilioEletronico() == Boolean.TRUE);
          	
          	isNaoContemParteDomicilioEletronico = miniPacVOList.stream().anyMatch(parte -> 
    			parte.getIsHabilitaDomicilioEletronico() == Boolean.TRUE && parte.getAtivo() == Boolean.TRUE);
          } else {
        	  isContemParteDomicilioEletronico = Boolean.FALSE;
        	  isNaoContemParteDomicilioEletronico = Boolean.FALSE;
          }
	}

    public List<ProcessoParteParticipacaoEnum> getPolosSelecionadosList() {
        return polosSelecionadosList;
    }

	public Map<Integer, Boolean> getDocumentosSelecionados() {
		return documentosSelecionados;
	}

	public void setDocumentosSelecionados(
			Map<Integer, Boolean> documentosSelecionados) {
		this.documentosSelecionados = documentosSelecionados;
	}
	
	public Boolean getIsProcessoDocJaExiste() {
		return isProcessoDocJaExiste;
	}

	public void setIsProcessoDocJaExiste(Boolean isProcessoDocJaExiste) {
		this.isProcessoDocJaExiste = isProcessoDocJaExiste;
	}
	
	public EntityDataModel<ProcessoDocumento> getProcessoDocumentoDataModel() {
		return processoDocumentoDataModel;
	}

	public void setProcessoDocumentoDataModel(
			EntityDataModel<ProcessoDocumento> processoDocumentoDataModel) {
		this.processoDocumentoDataModel = processoDocumentoDataModel;
	}

	public ProcessoDocumento getProcessoDocumentoPrincipal() {
		return processoDocumentoPrincipal;
	}

	public void setProcessoDocumentoPrincipal(ProcessoDocumento processoDocumentoPrincipal) {
		this.processoDocumentoPrincipal = processoDocumentoPrincipal;
	}
	
	public MiniPacVO getDestinatarioSelecionado() {
		return destinatarioSelecionado;
	}

	/**
	 * Define o destinatário ({@link MiniPacVO} indicado e seus endereços para seleção no popup /Processo/Fluxo/expedientes/miniPacEnderecos.xhtml
	 * 
	 * @param destinatarioSelecionado
	 */
	public void setDestinatarioSelecionado(MiniPacVO destinatarioSelecionado) {
		this.destinatarioSelecionado = destinatarioSelecionado;		
		enderecosSelecionados.clear();
		Set<Endereco> enderecosPossiveisSet = atoComunicacaoService.getEnderecosParaComunicacao(destinatarioSelecionado.getProcessoParte(), destinatarioSelecionado.getPessoal());
		this.enderecosPossiveis = new ArrayList<>(enderecosPossiveisSet);		

	    // Inicialize totalEnderecos com o tamanho da lista de endereços possíveis
	    this.totalEnderecos = enderecosPossiveis.size();

		for(Endereco endereco : destinatarioSelecionado.getEnderecos()){
			enderecosSelecionados.put(endereco, true);
		}

	    // Inicialize o enderecoDataModel
	    enderecoDataModel = new UnifiedEntityDataModel<>(Endereco.class, facesContext, new EnderecoRetriever());
	    enderecoDataModel.setPageSize(pageSize); // Define o pageSize no data model

	    // Ajuste para índice iniciar em 0
	    enderecoDataModel.setCurrentPage((currentPage > 0) ? currentPage - 1 : 0);
 
	    // Chame o update() para atualizar o modelo de dados**
//	    enderecoDataModel.update();

	    // Chama o applyFilter() sem filtros para inicializar corretamente
	    ((EnderecoRetriever) enderecoDataModel.getDataRetriever()).applyFilter(null, null);

	}

	public Map<Endereco, Boolean> getEnderecosSelecionados() {
		return enderecosSelecionados;
	}

	public void setEnderecosSelecionados(Map<Endereco, Boolean> enderecosSelecionados) {
		this.enderecosSelecionados = enderecosSelecionados;
	}

	public List<Endereco> getEnderecosPossiveis() {
		return enderecosPossiveis;
	}

	public void setEnderecosPossiveis(List<Endereco> enderecosPossiveis) {
		this.enderecosPossiveis = enderecosPossiveis;
	}

	public String[] getMeiosDefinidosFluxo() {
		return meiosDefinidosFluxo;
	}

	public void setMeiosDefinidosFluxo(String[] meiosDefinidosFluxo) {
		this.meiosDefinidosFluxo = meiosDefinidosFluxo;
	}
	
	/**
	 * Classe responsável pela paginação verdadeira da tabela de documentos vinculaveis ao processo.
	 */
	private class ProcessoDocumentoRetriever implements DataRetriever<ProcessoDocumento>{		
		private ProcessoDocumentoManager processoDocumentoManager;
		private DocumentoJudicialService documentoJudicialService;	
		
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
			List<ProcessoDocumento> listDoc = documentoJudicialService.getDocumentos(getProcessoJudicial(), search.getFirst(), search.getMax(), true, true, true, true, false);
			//Percorrendo documentos do processo a procura do documento que será utilizado no expediente.
    		if(idDocPrincipal != null){
		    	for (ProcessoDocumento pd : listDoc) {
					if(pd.getIdProcessoDocumento() == idDocPrincipal){
						pd.setSelected(true);
						break;
					}
				}
    		}
    		return listDoc;
		}

		@Override
		public long count(Search search) {
			return 	documentoJudicialService.getCountDocumentos(getProcessoJudicial(), true, false, true);
		}
	}
	
	public boolean isExibirDivOutrosDestinatarios() {
		return exibirDivOutrosDestinatarios;
	}
	
	public void setExibirDivOutrosDestinatarios(boolean exibirDivOutrosDestinatarios) {
		this.exibirDivOutrosDestinatarios = exibirDivOutrosDestinatarios;
	}
	
	public boolean habilitaDomicilio(MiniPacVO miniPacVO) {
		boolean retorno = false;
		
		if (isExisteProcessoPartePessoa(miniPacVO)) {
			retorno = isPessoaHabilitadaDomicilioEletronico(miniPacVO.getProcessoParte().getPessoa());
		}

		return retorno;
	}

	/**
	 * Obtm os tipos de documentos disponveis para prtica nesse n.
	 * 
	 * @return a lista de tipos de documento ({@link TipoProcessoDocumento}) que podem ser criados neste n.
	 */
	public List<TipoProcessoDocumento> getTiposDocumentosDisponiveis(){
		if (tiposDocumentosDisponiveis == null || tiposDocumentosDisponiveis.isEmpty()) {
			tiposDocumentosDisponiveis = ComponentUtil.getComponent(DocumentoJudicialService.class).getTiposDocumentoMinutaMiniPac();
		}
		return tiposDocumentosDisponiveis;
	}

	/**
	 * Atribui uma lista de tipos de documento como passveis de criao neste n.
	 * 
	 * @param tiposDocumentosDisponiveis a lista de tipos de documento passiveis de criao a ser atribuda.
	 * 
	 */
	public void setTiposDocumentosDisponiveis(List<TipoProcessoDocumento> tiposDocumentosDisponiveis){
		this.tiposDocumentosDisponiveis = tiposDocumentosDisponiveis;
	}

	public TipoProcessoDocumento getTipoProcessoDocumentoSelecionado() {
		return tipoProcessoDocumentoSelecionado;
	}
	
	public void setTipoProcessoDocumentoSelecionado(TipoProcessoDocumento tipoProcessoDocumentoSelecionado) {
		this.tipoProcessoDocumentoSelecionado = tipoProcessoDocumentoSelecionado;
	}

	public void setTipoProcessoDocumentoSelecionado() {
		if(!miniPacVOList.isEmpty()) {
			miniPacVOList.stream().forEach(miniPac -> miniPac.setTipoProcessoDocumento(this.tipoProcessoDocumentoSelecionado));
		}
	}

	public Boolean getIsContemParteDomicilioEletronico() {
		return isContemParteDomicilioEletronico;
	}

	public void setIsContemParteDomicilioEletronico(Boolean isContemParteDomicilioEletronico) {
		this.isContemParteDomicilioEletronico = isContemParteDomicilioEletronico;
	}

	public Boolean getIsNaoContemParteDomicilioEletronico() {
		return isNaoContemParteDomicilioEletronico;
	}

	public void setIsNaoContemParteDomicilioEletronico(Boolean isNaoContemParteDomicilioEletronico) {
		this.isNaoContemParteDomicilioEletronico = isNaoContemParteDomicilioEletronico;
	}

	/**
	 * @param vo MiniPacVO
	 * @return True se existir pessoa para o ProcessoParte.
	 */
	protected boolean isExisteProcessoPartePessoa(MiniPacVO vo) {
		return (vo != null && 
				vo.getProcessoParte() != null && 
				vo.getProcessoParte().getPessoa() != null); 
	}
	
	private boolean isPessoaHabilitadaDomicilioEletronico(Pessoa pessoa) {
		return DomicilioEletronicoService.instance().isPessoaHabilitada(pessoa);
	}

	public boolean isPossuiAlgumaParteHabilitadaDomicilio() {
		if (!miniPacVOList.isEmpty()){
			for(MiniPacVO miniPacVO : miniPacVOList){
				if (miniPacVO.getIsHabilitaDomicilioEletronico() && miniPacVO.getAtivo() && (miniPacVO.getMeios().contains(ExpedicaoExpedienteEnum.E)) ) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Retorna true se for para exibir mensagem de alerta sobre possível atraso de envio de expedientes ao Domicílio (caso esteja offline).
	 * A mensagem só será exibida se:
	 * - O parâmetro PJE_DOMICILIO_ELETRONICO_UTILIZAR_ALERTA_OFFLINE estiver configurado como false;
	 * - O parâmetro PJE_DOMICILIO_ELETRONICO_MSG_POSSIVEL_ATRASO_ENVIO não estiver vazio;
	 * - Tiver alguma parte habilitada no Domicílio.
	 *
	 * @return Boleano
	 */
	public boolean isExibeMensagemPossivelAtrasoEnvioDomicilio() {
		DomicilioEletronicoService domicilio = DomicilioEletronicoService.instance();
		mensagemPossivelAtrasoEnvioDomicilio = domicilio.msgAlertaPossivelAtrasoEnvioDomicilio();
		return !BooleanUtils.toBoolean(domicilio.isUtilizaAlertaDomicilioOffline()) && isPossuiAlgumaParteHabilitadaDomicilio() && !Strings.isEmpty(mensagemPossivelAtrasoEnvioDomicilio);
	}
	
	private String cepFilter;
	private String enderecoCompletoFilter;
	private UnifiedEntityDataModel<Endereco> enderecoDataModel;
	private int pageSize = 10; 
	private int totalEnderecos;
	private int currentPage = 1; // Página atual, iniciando em 1
	
    public void clearFilter() {
        // Limpa os valores dos filtros
        this.cepFilter = null;
        this.enderecoCompletoFilter = null;

        // Limpa os filtros aplicados no EnderecoRetriever
        if (enderecoDataModel != null) {
            ((EnderecoRetriever) enderecoDataModel.getDataRetriever()).clearFilter();
            enderecoDataModel.setRefreshPage(true);
        }
    }

	public int getCurrentPage() {
	    if (enderecoDataModel != null) {
	        return enderecoDataModel.getCurrentPage() + 1; // Ajuste, pois páginas começam em 0 no modelo
	    }
	    return currentPage;
	}

	public void setCurrentPage(int currentPage) {
	    this.currentPage = currentPage;
	    if (enderecoDataModel != null) {
	        enderecoDataModel.setCurrentPage((currentPage > 0) ? currentPage - 1 : 0);
	        enderecoDataModel.setRefreshPage(true); // Força a atualização dos dados
	    }
	}

	public int getTotalEnderecos() {
	    return totalEnderecos;
	}

	public int getPageSize() {
	    return pageSize;
	}

	public void setPageSize(int pageSize) {
	    this.pageSize = pageSize;
	}

	public String getCepFilter() {
	    return cepFilter;
	}

	public void setCepFilter(String cepFilter) {
	    this.cepFilter = cepFilter;
	}

	public String getEnderecoCompletoFilter() {
	    return enderecoCompletoFilter;
	}

	public void setEnderecoCompletoFilter(String enderecoCompletoFilter) {
	    this.enderecoCompletoFilter = enderecoCompletoFilter;
	}

	public UnifiedEntityDataModel<Endereco> getEnderecoDataModel() {
	    return enderecoDataModel;
	}

	public void applyFilter() {
	    if (enderecoDataModel != null) {
	        ((EnderecoRetriever) enderecoDataModel.getDataRetriever()).applyFilter(cepFilter, enderecoCompletoFilter);
	        enderecoDataModel.setRefreshPage(true);
	    }
	}
	
	public int getNumeroEnderecosSelecionados() {
	    int count = 0;
	    for (Boolean selected : enderecosSelecionados.values()) {
	        if (selected != null && selected) {
	            count++;
	        }
	    }
	    return count;
	}

	private class EnderecoRetriever implements UnifiedEntityDataModel.DataRetriever<Endereco> {
		private List<Endereco> enderecosPossiveis; // Lista original de endereços
		private List<Endereco> filteredEnderecos; // Lista filtrada de endereços
		public EnderecoRetriever() {
			// Acessa a lista de endereços possíveis do MiniPacAction
			this.enderecosPossiveis = MiniPacAction.this.enderecosPossiveis;
			// Inicializa a lista filtrada com todos os endereços
			this.filteredEnderecos = new ArrayList<>(enderecosPossiveis);
			// Aplica a ordenação padrão
			applySort();
			filteredEnderecos.size();
		}

		@Override
		public Object getId(Endereco entity) {
			return entity.getIdEndereco();
		}

		@Override
		public Endereco findById(Object id) throws Exception {
			for (Endereco e : enderecosPossiveis) {
				if (e.getIdEndereco() == (int) id) {
					return e;
				}
			}
			return null;
		}

		@Override
		public List<Endereco> list(int firstRow, int maxRows) {
			int endIndex = Math.min(firstRow + maxRows, filteredEnderecos.size());
			if (firstRow >= filteredEnderecos.size()) {
				return new ArrayList<>();
			}
			return filteredEnderecos.subList(firstRow, endIndex);
		}

		@Override
		public int count() {
			return filteredEnderecos.size();
		}

		public void applyFilter(String cepFilter, String enderecoCompletoFilter) {
		    // Inicia a lista filtrada com todos os endereços possíveis
		    filteredEnderecos = new ArrayList<>(enderecosPossiveis);

		    // Verifica se algum dos filtros foi preenchido
		    if ((cepFilter != null && !cepFilter.trim().isEmpty()) ||
		        (enderecoCompletoFilter != null && !enderecoCompletoFilter.trim().isEmpty())) {

		        // Aplica os filtros usando Streams e Lambdas
		        filteredEnderecos = filteredEnderecos.stream()
		            .filter(e -> {
		                boolean matchesCep = true;
		                boolean matchesEnderecoCompleto = true;

		                // Aplica o filtro de CEP, se fornecido
		                if (cepFilter != null && !cepFilter.trim().isEmpty()) {
		                    String cep = e.getCep().getNumeroCep();;
		                    if (cep == null) {
		                        matchesCep = false;
		                    } else {
		                        // Remove o hífen para comparar apenas os números
		                        String cepNoHyphen = cep.replace("-", "");
		                        String cepFilterNoHyphen = cepFilter.replace("-", "");
		                        matchesCep = cepNoHyphen.equals(cepFilterNoHyphen);
		                    }
		                }

		                // Aplica o filtro de Endereço Completo, se fornecido
		                if (matchesCep && enderecoCompletoFilter != null && !enderecoCompletoFilter.trim().isEmpty()) {
		                    String enderecoCompleto = e.getEnderecoCompleto();
		                    if (enderecoCompleto == null) {
		                        matchesEnderecoCompleto = false;
		                    } else {
		                        // Normaliza os textos para comparação sem acentuação e em minúsculas
		                        String normalizedEndereco = normalizarTexto(enderecoCompleto);
		                        String normalizedFilter = normalizarTexto(enderecoCompletoFilter);
		                        matchesEnderecoCompleto = normalizedEndereco.contains(normalizedFilter);

//		                        List<String> normalizedFilters = Arrays.asList(normalizedFilter.split("\\s+"));
//                                List<String> normalizedEnderecos = Arrays.asList(normalizedEndereco.split("\\s+"));
//
//                                // VERIFICAR SE PELO MENOS UMA PALAVRA DO PADRÃO ESTÁ PRESENTE EM ALGUMA PARTE DA FRASE
//                                matchesEnderecoCompleto = normalizedFilters.stream()
//                                        .filter(palavra -> palavra.length() >= 4)  // LIMITA A BUSCA PARA PALAVRAS COM 4 OU MAIS CARACTERES
//                                        .anyMatch(palavra -> 
//                                        	normalizedEnderecos.stream().anyMatch(endereco -> endereco.contains(palavra))
//                                        );
		                    } 
		                }

		                // Retorna true se o endereço corresponder a todos os filtros aplicados
		                return matchesCep && matchesEnderecoCompleto;
		            })
		            .collect(Collectors.toList());
		    }

		    // Reaplica a ordenação padrão
		    applySort();

		    filteredEnderecos.size();

		    // Redefine a página atual para a primeira página
		    MiniPacAction.this.setCurrentPage(1);

		    // Atualiza o modelo de dados para refletir as alterações
		    if (MiniPacAction.this.enderecoDataModel != null) {
		        MiniPacAction.this.enderecoDataModel.setRefreshPage(true);
		        MiniPacAction.this.enderecoDataModel.update();
		    }
		}


		public void clearFilter() {
			// Reseta a lista filtrada para a lista completa
			filteredEnderecos = new ArrayList<>(enderecosPossiveis);
			// Reaplica a ordenação
			applySort();
			filteredEnderecos.size();
			// Opcional: redefinir a página atual
			MiniPacAction.this.setCurrentPage(1);
		}

		public void applySort() {
			// Ordena a lista filtrada por dataAlteracao em ordem decrescente, nulos no final
			filteredEnderecos.sort((e1, e2) -> {
				if (e1.getDataAlteracao() == null && e2.getDataAlteracao() == null) {
					return 0;
				} else if (e1.getDataAlteracao() == null) {
					return 1;
				} else if (e2.getDataAlteracao() == null) {
					return -1;
				} else {
					return e2.getDataAlteracao().compareTo(e1.getDataAlteracao());
				}
			});
		}

		private String normalizarTexto(String texto) {
			if (!isBlank(texto)) {
				String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);

				// Substitui todos os tipos de espaços por um único espaço
				String resultado = Pattern.compile("[\\p{Z}\\p{C}&&[^ ]]|\\s+").matcher(normalized).replaceAll(" ");

				// Remove caracteres não ASCII mas mantém certos símbolos (%@&ºª)
				resultado = Pattern.compile("\\p{InCombiningDiacriticalMarks}+|[^\\p{ASCII}%@&ºª]").matcher(resultado).replaceAll("");

				return resultado.toLowerCase().trim();
			}

			return texto;
		}

		private boolean isBlank(final CharSequence cs) {
			final int strLen = length(cs);
			if (strLen == 0) {
				return true;
			}
			for (int i = 0; i < strLen; i++) {
				if (!Character.isSpaceChar(cs.charAt(i))) {
					return false;
				}
			}
			return true;
		}

		private int length(final CharSequence cs) {
			return cs == null ? 0 : cs.length();
		}

		@Override
		public List<Endereco> listFiltred(int firstRow, int maxRows) {
			return null;
		}

		@Override
		public int countFiltred(Object filter) {
			return 0;
		}

		@Override
		public void clearCount() {

		}

		@Override
		public int firstPage() {
			return 0;
		}

		@Override
		public int lastPage() {
			return 0;
		}

		@Override
		public int currentPage() {
			return 0;
		}

		@Override
		public int maxPagesAllowed() {
			return 0;
		}

		@Override
		public boolean refreshPage() {
			return false;
		}

		@Override
		public List<Endereco> applySort(Comparator<Endereco> comparator) {
			return null;
		}

		@Override
		public void applyFilter(Predicate<Endereco> filter) {

		}

		@Override
		public Endereco getCurrent() {
			return null;
		}

		@Override
		public void update() {
		}
	}
}