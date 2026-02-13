package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.LazyInitializationException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jbpm.graph.exe.ProcessInstance;

import br.com.infox.cliente.home.ProcessoTrfLogDistribuicaoHome;
import br.com.infox.cliente.home.ProcessoTrfRedistribuicaoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.Util;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.entidades.vo.MiniPacVO;
import br.jus.cnj.pje.extensao.ConectorECT;
import br.jus.cnj.pje.extensao.ConectorMandados;
import br.jus.cnj.pje.extensao.ConectorTelegrama;
import br.jus.cnj.pje.extensao.PontoExtensaoException;
import br.jus.cnj.pje.extensao.PublicadorDJE;
import br.jus.cnj.pje.extensao.auxiliar.DestinatarioECT;
import br.jus.cnj.pje.extensao.auxiliar.ExpedientePublicacao;
import br.jus.cnj.pje.extensao.auxiliar.ParteExpediente;
import br.jus.cnj.pje.extensao.auxiliar.RemetenteECT;
import br.jus.cnj.pje.nucleo.MuralException;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.CepManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoBinManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.manager.PublicacaoDiarioEletronicoManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.TramitacaoProcessualService;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.ConsultaExpedienteAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBin;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfLogDistribuicao;
import br.jus.pje.nucleo.entidades.ProcessoTrfRedistribuicao;
import br.jus.pje.nucleo.entidades.PublicacaoDiarioEletronico;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name("comunicacaoProcessualAction")
@Scope(ScopeType.CONVERSATION)
public class ComunicacaoProcessualAction extends BaseAction<ProcessoTrf>
    implements Serializable {
	
    private static final long serialVersionUID = -708562071875391335L;
    
    public static final String VARIAVEL_EXPEDIENTE = "comunicacaoProcessualAction:idsExpedientes";
    public static final String VARIAVEL_ERROS_CONECTOR_ECT = "variavelErrosConectorEct";
    public static final String PARAMETRO_ATIVACAO_CORREIOS = "urlServicoPostal";
    
    public static final String PARAMETRO_INSTANCIA_JUSTICA = "aplicacaoSistema";
    
    public static final String VARIAVEL_ERROS_CONECTOR_TELEGRAMA = "variavelErrosConectorTelegrama";
    public static final String PARAMETRO_ATIVACAO_TELEGRAMA = "urlTelegrama";
    
    public static final String VARIAVEL_ERROS_CONECTOR_DJE= "variavelErrosConectorDJE";
    
    public static final String  VARIAVEL_ERROS_CONECTOR_MURAL = "variavelErrosConectorMural";
    
    @Logger
    private Log logger;
    
    @In(create = false)
    private ProcessInstance processInstance;
    
    @In
    private transient AtoComunicacaoService atoComunicacaoService;
    
    @In(create = true, required = false)
    private transient PublicadorDJE publicadorDJE;
    
    @In(create = true, required = false)
    private transient ConectorECT conectorECT;
    
    
    @In(create = true, required = false)
    private transient ConectorTelegrama conectorTelegrama;
    
    
    @In(create = true, required = false)
    private transient ConectorMandados conectorMandados;
    
    @In
    private transient ProcessoJudicialManager processoJudicialManager;
    
    @In(create = true, required = false)
    private transient Util util;
    
    private int contaCorreios = 0;
    private int contaMandados = 0;
    private int contaEletronicos = 0;
    private int contaEdital = 0;
    private int contaPrec = 0;
    private int contaDJE = 0;
    private int contaTelefone = 0;
    private int contaPessoal = 0; 
    private int contaTelegrama = 0;
    private int contaMural = 0;
    
    private ProcessoExpediente expedienteSelecionado;
    
    private List<ProcessoExpediente> expedientes;
    
    private Map<ExpedicaoExpedienteEnum, List<ProcessoExpediente>> mapaExpedientes;
    
    private String elMovimento;
    
    private String varProcessoExpedienteEl;
    
    private ExpedicaoExpedienteEnum expedicaoExpedienteEnum;
    private List<String> errosConectorEct = new ArrayList<String>(0);

    private List<String> errosConectorTelegrama = new ArrayList<String>(0);

    @In(create = true)
    private ProcessoParteExpedienteManager processoParteExpedienteManager;
    
    @In
    private TramitacaoProcessualService tramitacaoProcessualService;
    
    @In
    private ProcessoDocumentoBinManager processoDocumentoBinManager;
    
    @In
    private EstadoManager estadoManager;
    
    @In
    private CepManager cepManager;
    
    @In
    private ProcessoExpedienteManager processoExpedienteManager;
    
    @In(create = true)
    private ParametroService parametroService;
    
    @In(create = true)
    private ProcessoTrfRedistribuicaoHome processoTrfRedistribuicaoHome;
    
    private int instanciaJustica = -1;
    
    @Override
    public EntityDataModel<ProcessoTrf> getModel() {
    	throw new UnsupportedOperationException("O modelo não está definiido.");
    }

    @Create
    public void init() {
        logger.trace("Inicializando componente {0}", ComunicacaoProcessualAction.class.getName());
        if (processInstance != null) {
            String idsExp = (String) processInstance.getContextInstance().getVariable(VARIAVEL_EXPEDIENTE);
            inicializaExpedientes(idsExp);
        }
    }

    private void inicializaExpedientes(String idsExp) {
        inicializaExpedientes(recuperaExpedientes(idsExp));
    }
    
    private void inicializaExpedientes(List<ProcessoExpediente> expedientes) {
    	this.expedientes = expedientes;
        mapaExpedientes = classificaExpedientes(expedientes);                
    }

	private List<ProcessoExpediente> recuperaExpedientes(String idsExp) {
        List<ProcessoExpediente> expedientes = new ArrayList<ProcessoExpediente>();
        if (idsExp != null) {
            for (String idExp : idsExp.split(",")) {
                try {
                    ProcessoExpediente pe = atoComunicacaoService.getAtoComunicacao(Integer.parseInt(idExp));

					if (pe != null) {
						expedientes.add(pe);
					}
                } catch (NumberFormatException e) {
                    logger.error("Erro de formatação ao tentar converter [{0}] para um inteiro. {1}", idExp, e.getLocalizedMessage());
                } catch (PJeBusinessException e) {
                    logger.error("Erro de negócio ao tentar recuperar o expediente de número [{0}]. {1}", idExp, e.getLocalizedMessage());
                } catch (PJeDAOException e) {
                    logger.error("Erro ao tentar acessar o expediente de número [{0}]. {1}", idExp, e.getLocalizedMessage());
                }
            }
        }
        return expedientes;
	}

	private Map<ExpedicaoExpedienteEnum, List<ProcessoExpediente>> classificaExpedientes(List<ProcessoExpediente> expedientes) {
		Map<ExpedicaoExpedienteEnum, List<ProcessoExpediente>> mapaExpedientes = new HashMap<ExpedicaoExpedienteEnum, List<ProcessoExpediente>>();

        mapaExpedientes.put(ExpedicaoExpedienteEnum.C, new ArrayList<>());
        mapaExpedientes.put(ExpedicaoExpedienteEnum.D, new ArrayList<>());
        mapaExpedientes.put(ExpedicaoExpedienteEnum.E, new ArrayList<>());
        mapaExpedientes.put(ExpedicaoExpedienteEnum.L, new ArrayList<>());
        mapaExpedientes.put(ExpedicaoExpedienteEnum.M, new ArrayList<>());
        mapaExpedientes.put(ExpedicaoExpedienteEnum.P, new ArrayList<>());
        mapaExpedientes.put(ExpedicaoExpedienteEnum.T, new ArrayList<>());
        mapaExpedientes.put(ExpedicaoExpedienteEnum.S, new ArrayList<>());                
        mapaExpedientes.put(ExpedicaoExpedienteEnum.G, new ArrayList<>());
        mapaExpedientes.put(ExpedicaoExpedienteEnum.R, new ArrayList<>());
        
        for (ProcessoExpediente pe : expedientes) {
        	mapaExpedientes.get(pe.getMeioExpedicaoExpediente()).add(pe);
        }
        
        contaCorreios = mapaExpedientes.get(ExpedicaoExpedienteEnum.C).size();
        contaEdital = mapaExpedientes.get(ExpedicaoExpedienteEnum.D).size();
        contaEletronicos = mapaExpedientes.get(ExpedicaoExpedienteEnum.E).size();
        contaPrec = mapaExpedientes.get(ExpedicaoExpedienteEnum.L).size();
        contaMandados = mapaExpedientes.get(ExpedicaoExpedienteEnum.M).size();
        contaDJE = mapaExpedientes.get(ExpedicaoExpedienteEnum.P).size();
        contaTelefone = mapaExpedientes.get(ExpedicaoExpedienteEnum.T).size();
        contaPessoal = mapaExpedientes.get(ExpedicaoExpedienteEnum.S).size();
        contaTelegrama = mapaExpedientes.get(ExpedicaoExpedienteEnum.G).size();
        contaMural = mapaExpedientes.get(ExpedicaoExpedienteEnum.R).size();
        
        return mapaExpedientes;
	}

    public boolean haComunicacao(ExpedicaoExpedienteEnum tipoComunicacao) {
        int conta = 0;

        switch (tipoComunicacao) {
        case C:
            conta = contaCorreios;
            break;

        case D:
            conta = contaEdital;
            break;

        case E:
            conta = contaEletronicos;
            break;

        case L:
            conta = contaPrec;
            break;

        case M:
            conta = contaMandados;
            break;

        case P:
            conta = contaDJE;
            break;

        case T:
        	conta = contaTelefone;
        	break;
        	
        case S:
        	conta = contaPessoal;
        	break;

        case G:
        	conta = contaTelegrama;
        	break;
        	
        case R:
        	conta = contaMural;
        	break;        	
        	
		case N:
			break;
			
		default:
			break;
        }

        return conta > 0;
    }
    
    @Transactional(TransactionPropagationType.REQUIRED)
    public int enviarExpedientesLancarMovimentosCertidaoDeDividaAtiva(String varProcessoExpedienteEl, String elMovimento) {
        List<ProcessoExpediente> expedientes = criarExpedientesCertidaoDeDividaAtiva();
        inicializaExpedientes(expedientes);
        int contaExpedientesEnviados = enviarExpedientesLancarMovimentos(ExpedicaoExpedienteEnum.G, varProcessoExpedienteEl, elMovimento);
        return contaExpedientesEnviados;
    }
    
    private List<ProcessoExpediente> criarExpedientesCertidaoDeDividaAtiva() {
    	ProcessoTrf processoTrf = tramitacaoProcessualService.recuperaProcesso();
    	Processo processo = processoTrf.getProcesso();
    	List<ProcessoDocumento> listaDocumentos = processo.getProcessoDocumentoList();
    	ProcessoDocumento documentoPrincipal = buscaDocumentoPrincipal(listaDocumentos);
    	if (documentoPrincipal == null) {
            logger.error("Não foi encontrado documento de Certidão de Dívida Ativa (CDA) neste processo. (idProcesso = {0})", processo.getIdProcesso());
            return null;
    	}
    	Map<ExpedicaoExpedienteEnum, List<MiniPacVO>> miniPacVOMap = atoComunicacaoService.recuperaInformacoesPartes(processoTrf.getProcessoPartePoloPassivoSemAdvogadoList(), ExpedicaoExpedienteEnum.G, null);
    	if (miniPacVOMap == null) {
			logger.error("Não foi possível recuperar a lista de partes do polo passivo do processo. (idProcesso = {0})", processoTrf.getIdProcessoTrf());
    		return null;
    	}
    	List<ProcessoDocumento> listaDocumentosDedup = deduplicaListaProcessoDocumento(listaDocumentos);
    	Collection<ProcessoExpediente> expedientes = atoComunicacaoService.criarAtosComunicacao(processoTrf, documentoPrincipal, miniPacVOMap, listaDocumentosDedup, Boolean.FALSE);
    	if (expedientes == null) {
			logger.error("Não foi possível criar os atos de comunicacao para o processo. (idProcesso = {0})", processoTrf.getIdProcessoTrf());
    		return null;
    	}
    	List<ProcessoExpediente> listaExpedientes = new ArrayList<ProcessoExpediente>(expedientes);
    	return listaExpedientes;
    }

    private List<ProcessoDocumento> deduplicaListaProcessoDocumento(List<ProcessoDocumento> listaDocumentos) {
    	List<ProcessoDocumento> listaDocumentosDedup = new ArrayList<ProcessoDocumento>();
    	for (ProcessoDocumento processoDocumento : listaDocumentos) {
    		int idProcessoDocumento = processoDocumento.getIdProcessoDocumento();
    		if (!listaProcessoDocumentoContemId(listaDocumentosDedup, idProcessoDocumento)) {
    			listaDocumentosDedup.add(processoDocumento);
    		}
    	}
    	return listaDocumentosDedup;
	}

	private boolean listaProcessoDocumentoContemId(List<ProcessoDocumento> listaDocumentos, int idProcessoDocumento) {
		for (ProcessoDocumento processoDocumento : listaDocumentos) {
			if (processoDocumento.getIdProcessoDocumento() == idProcessoDocumento) {
				return true;
			}
		}
		return false;
	}

	private ProcessoDocumento buscaDocumentoPrincipal(List<ProcessoDocumento> listaDocumentos) {
    	ProcessoDocumento documentoPrincipal = null;
    	for (ProcessoDocumento documento : listaDocumentos) {
    		if (
    			documento.getAtivo() &&
    			!documento.getDocumentoSigiloso() &&
    			(documentoPrincipal == null || documento.getDataInclusao().before(documentoPrincipal.getDataInclusao())) 
    		) {
    			documentoPrincipal = documento;
    		}
    	}
    	if (documentoPrincipal == null) {
    		logger.error("Não foi encontrado um documento que possa ser considerado principal (pode haver sigilo).");
    	}
		return documentoPrincipal;
	}

	public List<ProcessoExpediente> recuperarExpedientesTelegrama(String dataString, String formato) {
		SimpleDateFormat format = new SimpleDateFormat(formato);
		Date data = new Date();
		try {
			data = format.parse(dataString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		List<ProcessoExpediente> listaProcessoExpediente = recuperarExpedientesTelegrama(data);
		return listaProcessoExpediente;
	}
	
	@Transactional(TransactionPropagationType.REQUIRED)
    public List<ProcessoExpediente> recuperarExpedientesTelegrama(Date data) {
    	List<ProcessoExpediente> listaProcessoExpediente = processoExpedienteManager.getAtosComunicacaoTelegrama(data);
    	List<ProcessoExpediente> listaProcessoExpedienteFiltrada = new ArrayList<ProcessoExpediente>();
		Integer idOrgaoJulgadorUsuarioAtual = getIdOrgaoUsuarioAtual();
    	for (ProcessoExpediente processoExpediente : listaProcessoExpediente) {
    		Integer idOrgaoCriacaoExpediente = determinaIdOrgaoJulgadorDaCriacaoExpediente(processoExpediente);
    		if (idOrgaoJulgadorUsuarioAtual == idOrgaoCriacaoExpediente) {
    			listaProcessoExpedienteFiltrada.add(processoExpediente);
    		}
    	}
    	return listaProcessoExpedienteFiltrada;
    }

	private Integer getIdOrgaoUsuarioAtual() {
		Integer idOrgao;
		if (getInstanciaJustica() == 1) {
			idOrgao = Authenticator.getIdOrgaoJulgadorAtual();
		} else {
			idOrgao = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
		}
		return idOrgao;
	}

	@Transactional
	private Integer determinaIdOrgaoJulgadorDaCriacaoExpediente(ProcessoExpediente processoExpediente) {
		Integer idOrgaoConsiderado = null;
		if (processoExpediente != null) {
			if (processoExpediente.getOrgaoJulgador() != null) {
				idOrgaoConsiderado = processoExpediente.getOrgaoJulgador().getIdOrgaoJulgador();
			} else {
				ProcessoTrfLogDistribuicaoHome processoTrfLogDistribuicaoHome = ComponentUtil.getComponent(ProcessoTrfLogDistribuicaoHome.NAME);
				Date dataExpediente = processoExpediente.getDtCriacao();
				ProcessoTrf processoTrf = processoExpediente.getProcessoTrf();
				List<ProcessoTrfRedistribuicao> listaRedistribuicao = processoTrfRedistribuicaoHome.recuperarPorProcesso(processoTrf);
				ProcessoTrfLogDistribuicao processoTrfLogDistribuicao = processoTrfLogDistribuicaoHome.recuperarPorProcesso(processoTrf);
				Date dataDistribuicao = processoTrfLogDistribuicao.getDataLog();
				Date dataConsiderada = dataDistribuicao;
				idOrgaoConsiderado = getIdOrgao(processoTrfLogDistribuicao);
				for (ProcessoTrfRedistribuicao processoTrfRedistribuicao : listaRedistribuicao) {
					Date dataRedistribuicao = processoTrfRedistribuicao.getDataRedistribuicao();
					if (dataExpediente.after(dataRedistribuicao) 
						&& (DateUtil.diferencaDias(dataRedistribuicao, dataConsiderada) >= 0)
					) {
						dataConsiderada = dataRedistribuicao;
						Integer idOrgao = getIdOrgao(processoTrfRedistribuicao);
						if (idOrgao != null) {
							idOrgaoConsiderado = idOrgao;
						}
					}
				}
			}
		}
		return idOrgaoConsiderado;
	}

	private Integer getIdOrgao(ProcessoTrfLogDistribuicao processoTrfLogDistribuicao) {
		Integer idOrgaoConsiderado;
		if (getInstanciaJustica() == 1) {
			if (processoTrfLogDistribuicao == null || processoTrfLogDistribuicao.getOrgaoJulgador() == null) {
				return null;
			}
			idOrgaoConsiderado = processoTrfLogDistribuicao.getOrgaoJulgador().getIdOrgaoJulgador();
		} else {
			if (processoTrfLogDistribuicao == null || processoTrfLogDistribuicao.getOrgaoJulgadorColegiado() == null) {
				return null;
			}
			idOrgaoConsiderado = processoTrfLogDistribuicao.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado();
		}
		return idOrgaoConsiderado;
	}
    
	private Integer getIdOrgao(ProcessoTrfRedistribuicao processoTrfRedistribuicao) {
		Integer idOrgaoConsiderado;
		if (getInstanciaJustica() == 1) {
			if (processoTrfRedistribuicao == null || processoTrfRedistribuicao.getOrgaoJulgador() == null) {
				return null;
			}
			idOrgaoConsiderado = processoTrfRedistribuicao.getOrgaoJulgador().getIdOrgaoJulgador();
		} else {
			if (processoTrfRedistribuicao == null || processoTrfRedistribuicao.getOrgaoJulgadorColegiado() == null) {
				return null;
			}
			idOrgaoConsiderado = processoTrfRedistribuicao.getOrgaoJulgadorColegiado().getIdOrgaoJulgadorColegiado();
		}
		return idOrgaoConsiderado;
	}
    
	private int getInstanciaJustica() {
		if (instanciaJustica < 0) {
	        String parametroInstanciaJusticaString = parametroService.valueOf(PARAMETRO_INSTANCIA_JUSTICA);
	        instanciaJustica = Integer.parseInt(parametroInstanciaJusticaString);
		}
        return instanciaJustica;
	}
	
	public int enviarExpedientesPeloCorreio() {
		int cont = 0;
        
        tramitacaoProcessualService.apagaVariavel(VARIAVEL_ERROS_CONECTOR_ECT);
        errosConectorEct = new ArrayList<String>(0);
        String parametroAtivacaoCorreios = parametroService.valueOf(PARAMETRO_ATIVACAO_CORREIOS);

        for (ProcessoExpediente pe : expedientes) {
            if (pe.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.C) {
                for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()) {
                	try{
	                    OrgaoJulgador oj = ppe.getProcessoJudicial().getOrgaoJulgador();
	                    Endereco origem = oj.getLocalizacao().getEndereco();
	                    RemetenteECT remetente = new RemetenteECT();
	
	                    if (conectorECT != null && parametroAtivacaoCorreios != null) {
	                        remetente.setBairro(origem.getNomeBairro());
	                        remetente.setCep(origem.getCep().getNumeroCep());
	                        remetente.setCidade(origem.getNomeCidade());
	                        remetente.setComplemento(origem.getComplemento());
	                        remetente.setEstado(origem.getCep().getMunicipio().getEstado().getCodEstado());
	                        remetente.setLogradouro(origem.getNomeLogradouro());
	                        remetente.setNome(oj.getOrgaoJulgador());
	                        remetente.setPontoReferencia("");
	                        remetente.setTelefone(oj.getDddTelefone() + oj.getNumeroTelefone());
	                    }

	                    byte[] documento = null;

	                    if (conectorECT != null && parametroAtivacaoCorreios != null) {                        
	                    	//gerando PDF com o expediente e seus documentos vinculados	                    	
	                    	if(pe.getProcessoDocumentoExpedienteList() != null){
	                    		//ordenando a lista de documentos. Primerio o doc principal, depois os anexos
		                    	List<ProcessoDocumentoExpediente> pdeList = new ArrayList<ProcessoDocumentoExpediente>(0);
		                    	pdeList.addAll(pe.getProcessoDocumentoExpedienteList());
		                    	Collections.sort(pdeList, new Comparator<ProcessoDocumentoExpediente> (){
									@Override
									public int compare(ProcessoDocumentoExpediente o1, ProcessoDocumentoExpediente o2) {
										if(o1.getAnexo() == false && o2.getAnexo() == true){
											return -1;
										}else if(o1.getAnexo() == true && o2.getAnexo() == false){
											return +1;
										}
										
										return 0;
									}
		                    	});
	                    		
	                    		List<byte[]> documentos = new ArrayList<byte[]>(0);
	                    		for(ProcessoDocumentoExpediente pde : pdeList){
	                    			ProcessoDocumentoBin pdBin = pde.getProcessoDocumento().getProcessoDocumentoBin();	                    			
	                    			if(pdBin.isBinario()){
	                    				if(pdBin.getProcessoDocumento() != null){
		                    				documento = processoDocumentoBinManager.adicionaRodapeAssinatura(pdBin.getProcessoDocumento(), pdBin);
		                    				documentos.add(documento);
	                    				}
	                    			}else{
	                    				if(pdBin.getModeloDocumento() != null){
		                    				documento = processoDocumentoBinManager.convertHtml2Pdf(pdBin, new Util().getUrlProject());
		                    				documento = processoDocumentoBinManager.adicionaRodapeAssinatura(documento, pdBin);
		                    				documentos.add(documento);
	                    				}
	                    			}
	                    		}
                    		
	                    		documento = processoDocumentoBinManager.unificarPDFs(documentos);
	                    	}
	                    }

	                    for (ProcessoParteExpedienteEndereco e : ppe.getProcessoParteExpedienteEnderecoList()) {
	                    	if(e.getNumeroAr() == null){
		                    	
	                        
	                            if (conectorECT != null && parametroAtivacaoCorreios != null) {

									Integer idProcessoParteExp = e.getProcessoParteExpediente()
											.getIdProcessoParteExpediente();
									Integer idEndereco = e.getEndereco().getIdEndereco();

									tramitacaoProcessualService.gravaVariavel("varIdProcessoParteExp",
											idProcessoParteExp);
									tramitacaoProcessualService.gravaVariavel("varIdEndereco", idEndereco);

									String codigoEnvio = "";
									DestinatarioECT destinatario = geraDestinatario(ppe, e);

									Integer idLocalizacaoFisica = pe.getOrgaoJulgador().getLocalizacao()
											.getIdLocalizacao();
									String siglaUO = pe.getOrgaoJulgador().getSigla();

									TipoProcessoDocumento tipo = e.getProcessoParteExpediente().getProcessoDocumento()
											.getTipoProcessoDocumento();
									String tipoDocumento = tipo.getCodigoDocumento();

									String numeroDocumento = String.valueOf(
											e.getProcessoParteExpediente().getProcessoDocumento().getIdProcessoDocumento());

									tramitacaoProcessualService.gravaVariavel("siglaUO", siglaUO);
									tramitacaoProcessualService.gravaVariavel("idLocalizacaoFisica",
											idLocalizacaoFisica);
									tramitacaoProcessualService.gravaVariavel("tipoDocumento", tipoDocumento);
									tramitacaoProcessualService.gravaVariavel("numeroDocumento", numeroDocumento);
									
							  	    codigoEnvio = conectorECT
											.enviaCorrespondencia(ppe
													.getProcessoJudicial()
													.getNumeroProcesso(),
													remetente, destinatario,
													documento);
									
	                                if(codigoEnvio != null && !codigoEnvio.trim().isEmpty()){
	                                	try{
	                                		e.setNumeroAr(codigoEnvio);
	                                		processoParteExpedienteManager.persistAndFlush(ppe);
	                                	}catch(PJeBusinessException be){
                    						be.printStackTrace();
                    						String msg = "Erro ao gravar expediente ["
                    								+ ppe.getNomePessoaParte()+" - "
                    								+ ppe.getProcessoExpediente().getTipoProcessoDocumento()+" - CEP "
                    								+ e.getEndereco().getCep()                    								
                    								+ " ERRO: " + be.getLocalizedMessage()
                    								+ "]";
	                                		
	                                		//caso de erro ao gravar o expediente,
	                                		//o numero do AR deve ser cancelado no SGP
	                                		if(e.getNumeroAr() != null){
	                                			try{
	                                				conectorECT.cancelaCorrespondencia(e.getNumeroAr());
	                                			}catch(PontoExtensaoException pex){
	                        						be.printStackTrace();
	                        						msg = msg + "[Foi gerada a etiqueta "+e.getNumeroAr()+" no sistema SGP, " +
	                        								    "porem não foi possivel cancela-la. ERRO "+pex.getLocalizedMessage()+"]";
	                                			}
	                                		}
	                                		
	                                		errosConectorEct.add(msg);
	                                	}
	                                }else{
										errosConectorEct
												.add("Não foi gerada etiqueta nos Correios para o expediente ["
														+ ppe.getNomePessoaParte()+" - "
														+ ppe.getProcessoExpediente().getTipoProcessoDocumento()
														+ "]");
	                                }
	                            }
	                    	}
	                    	
                            if (getElMovimento() != null) {
                                lancarMovimentacoesExpedientes(pe);
                            }
                            tramitacaoProcessualService.apagaVariavel("varIdProcessoParteExp");
                            tramitacaoProcessualService.apagaVariavel("varIdEndereco");
                            tramitacaoProcessualService.apagaVariavel("siglaUO");
                            tramitacaoProcessualService.apagaVariavel("idLocalizacaoFisica");
                            tramitacaoProcessualService.apagaVariavel("tipoDocumento");
                            tramitacaoProcessualService.apagaVariavel("numeroDocumento");
	                    	
	                    	cont++;
	                    }
                	} catch (PontoExtensaoException e) {
                		e.printStackTrace();
						errosConectorEct.add("Erro ao gerar etiqueta nos Correios ["
								+ ppe.getNomePessoaParte()+" - "
								+ ppe.getProcessoExpediente().getTipoProcessoDocumento()
								+ " ERRO: " + e.getLocalizedMessage()
								+ "]");
					} catch(Exception e){
						e.printStackTrace();
						errosConectorEct
								.add("Erro inesperado ao tratar expediente ["
										+ ppe.getNomePessoaParte()+" - "
										+ ppe.getProcessoExpediente().getTipoProcessoDocumento()
										+ " ERRO: " + e.getLocalizedMessage()
										+ "]");
					}
                }
            }
        }
        
        tramitacaoProcessualService.gravaVariavel(VARIAVEL_ERROS_CONECTOR_ECT, errosConectorEct);
        return cont;
    }
	
	
	public int enviarExpedientesDJE() {
		return this.enviarExpedientesDJE(null);
	}
	

	/**
	 * Método responsável por encaminhar expedientes para o DJE, caso exista publicador instanciado.
	 * @return quantidade de expedientes encaminhados ao DJE
	 */
	public int enviarExpedientesDJE(ProcessoExpediente expediente) {
		if (publicadorDJE == null) {
			return 0;
		}
		
		List<ProcessoExpediente> expedientesPublicacao = new ArrayList<ProcessoExpediente>();
		if (expediente != null)	expedientesPublicacao.add(expediente);		
		else expedientesPublicacao.addAll(expedientes);
		
		int quantidade = 0;
		for (ProcessoExpediente pe : expedientesPublicacao) {
			if (pe.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.P || expediente != null ) {
				String reciboPublicacao = null;
				ExpedientePublicacao expedientePublicacao = montarExpedienteParaPublicacao(pe);
				try {
					tramitacaoProcessualService.apagaVariavel(VARIAVEL_ERROS_CONECTOR_DJE);
					reciboPublicacao = publicadorDJE.publicar(expedientePublicacao);
					quantidade++;
				} catch (Exception e) {
					e.printStackTrace();
					logger.error("Não foi possível encaminhar a publicação [{0}].", expedientePublicacao.getIdProcessoExpediente());
					StringBuffer erro = new StringBuffer();
					erro.append("Houve um erro na comunicação com o DJE e o expediente não será publicado. Por gentileza, entre em contato com o suporte. ");
					if(e.getLocalizedMessage() != null) {
						erro.append( "Possível motivo: ");
						erro.append(e.getLocalizedMessage());
					}
					tramitacaoProcessualService.gravaVariavel(VARIAVEL_ERROS_CONECTOR_DJE, erro.toString());	
			        /*ComponentUtil.getProcessoExpedienteManager().fecharExpedientes(pe);
			        if(!pe.getDocumentoExistente()) {
			        	 pe.getProcessoDocumento();
			        	 try {
							ComponentUtil.getProcessoDocumentoManager().excluirDocumento(pe.getProcessoDocumento(), Authenticator.getUsuarioSistema(), erro.toString());
			        	 } catch (PJeBusinessException e1) {
			        		 logger.error("Não foi possível excluir documento após erro de publicação no diário.");
			        		 e1.printStackTrace();
			        	 }
			        }
			        if(pe.getProcessoDocumentoExpedienteList() != null && !pe.getProcessoDocumentoExpedienteList().isEmpty()) {
			        	for(ProcessoDocumentoExpediente procDocExpeVinculado: pe.getProcessoDocumentoExpedienteList()) {
			        		procDocExpeVinculado.getProcessoDocumento();
			        		
			        		if(procDocExpeVinculado.getProcessoDocumento().getSelected() == null){
			        			try {
			        				ComponentUtil.getProcessoDocumentoManager().excluirDocumento(procDocExpeVinculado.getProcessoDocumento(), Authenticator.getUsuarioSistema(), erro.toString());
			        			} catch (PJeBusinessException e1) {
			        				logger.error("Não foi possível excluir documentos vinculados ao exepediente após erro de publicação no diário.");
			        				e1.printStackTrace();
			        			}
			        		}
			        	}
			        }*/
				}
				if (StringUtils.isNotBlank(reciboPublicacao)) {
					try {
						PublicacaoDiarioEletronicoManager publicacaoDiarioEletronicoManager = ComponentUtil.getComponent(PublicacaoDiarioEletronicoManager.class);
						for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()) {
							PublicacaoDiarioEletronico publicacaoDJE = publicacaoDiarioEletronicoManager.getPublicacao(ppe);
							publicacaoDJE.setReciboPublicacaoDiarioEletronico(reciboPublicacao);
							publicacaoDJE.setProcessoParteExpediente(ppe);
							publicacaoDiarioEletronicoManager.merge(publicacaoDJE);
							publicacaoDiarioEletronicoManager.flush();

							processoParteExpedienteManager.merge(ppe);
						}
						processoParteExpedienteManager.flush();
					} catch (PJeBusinessException e) {
						e.printStackTrace();
					}
					if (getElMovimento() != null) {
						lancarMovimentacoesExpedientes(pe);
					}
				}
			}
		}
		
		return quantidade;
	}

	/** 
	 * Monta o objeto {@link ExpedientePublicacao} que é enviado ao ponto de extensão para publicação no DJE.
	 * 
	 * @param processoExpediente Expediente a ser publicado.
	 * @return {@link ExpedientePublicacao} com as informações a serem publicadas.
	 */
	private ExpedientePublicacao montarExpedienteParaPublicacao(ProcessoExpediente processoExpediente) {
		ExpedientePublicacao expedientePublicacao = new ExpedientePublicacao();
		expedientePublicacao.setCodigoOrgao(Integer.toString(processoExpediente.getProcessoTrf().getOrgaoJulgador().getIdOrgaoJulgador()));
		
		ProcessoDocumentoBin docBin = processoExpediente.getProcessoDocumento().getProcessoDocumentoBin();
		expedientePublicacao.setDocumento(docBin.isBinario() ? docBin.getProcessoDocumento() : docBin.getModeloDocumento().getBytes());
		expedientePublicacao.setIdProcesso(processoExpediente.getProcessoTrf().getIdProcessoTrf());
		expedientePublicacao.setIdProcessoExpediente(processoExpediente.getIdProcessoExpediente());
		
		Set<ParteExpediente> parteExpedienteList = new HashSet<ParteExpediente>();
		for (ProcessoParteExpediente ppe : processoExpediente.getProcessoParteExpedienteList()) {
			ParteExpediente parteExpediente = new ParteExpediente(
					ppe.getIdProcessoParteExpediente(), ppe.getPessoaParte().getNome(), ppe.getPessoaParte().getDocumentoCpfCnpj());
			
			parteExpedienteList.add(parteExpediente);
		}
		expedientePublicacao.setParteExpedienteList(parteExpedienteList);
		
		return expedientePublicacao;
	}
	
    /**
     * Método responsável por encaminhar expedientes para a central de mandados.
     * @author Ronny Paterson (ronny.silva@trt8.jus.br) / David Vieira (davidv@trt7.jus.br)
     * @since 1.4.2
     * @see
     * @category PJE-JT
     */
    public int enviarExpedientesPelaCentralMandados() {
        if (conectorMandados == null) {
            return 0;
        }

        int cont = 0;

        for (ProcessoExpediente pe : expedientes) {
            if (pe.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.M) {
                try {
                    conectorMandados.encaminharExpedientesMovimentacaoMandados(pe);

                    if (getElMovimento() != null) {
                        lancarMovimentacoesExpedientes(pe);
                    }

                    cont++;
                } catch (PontoExtensaoException e1) {
                    logger.error("Não foi possível encaminhar à central de mandados o expediente [{0}].",
                        pe.getIdProcessoExpediente());
                }
            }

            FacesMessages.instance().clear();
            FacesMessages.instance()
                         .add(Severity.INFO, "Encaminhado com sucesso !");
        }

        return cont;
    }

    public int enviarExpedientesPeloSistema() {
    	return this.enviarExpedientes(ExpedicaoExpedienteEnum.E);
    }
    
    public int enviarExpedientesPorCarta() {
    	return this.enviarExpedientes(ExpedicaoExpedienteEnum.L);
    }
    
    private int enviarExpedientes(ExpedicaoExpedienteEnum expediente) {
        int cont = 0;

        for (ProcessoExpediente pe : expedientes) {
            if (pe.getMeioExpedicaoExpediente() == expediente) {
                try {
                    if (getElMovimento() != null) {
                        lancarMovimentacoesExpedientes(pe);
                    }

                    cont++;
                } catch (Exception e) {
                    logger.error("Não foi possível lançar o movimento para o expediente [{0}] encaminhado por " + expediente.name(),
                        pe.getIdProcessoExpediente());
                }
            }

            FacesMessages.instance().clear();
            FacesMessages.instance()
                         .add(Severity.INFO, "Encaminhado com sucesso !");
        }

        return cont;
    }
    
    public int enviarExpedientesPorMural(){
        FacesMessages.instance().clear();
    	// Agrupa os expedientes por idProcessoDocumento "Vinculado" e pelas partes do processo.
    	int count = 0;
    	for (ProcessoExpediente pe : expedientes) {
    		if (pe.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.R) {
	    		try{ 
	    			tramitacaoProcessualService.apagaVariavel(VARIAVEL_ERROS_CONECTOR_MURAL);
	    			ComponentUtil.getMuralService().enviarDadosMural(pe.getProcessoTrf().getNumeroProcesso(), pe.getProcessoDocumento().getIdProcessoDocumento(), pe);
		   			String atoPublicadoComComplemento = pe.getProcessoDocumento().getTipoProcessoDocumento().getTipoProcessoDocumento() + " " + pe.getMeioExpedicaoExpediente().getLabel();
	    			MovimentoAutomaticoService.preencherMovimento().deCodigo(92)
	    				.associarAoProcesso(pe.getProcessoTrf())
	    				.associarAoDocumento(pe.getProcessoDocumento())
	    				.comProximoComplementoVazio().preencherComTexto(atoPublicadoComComplemento)
	    				.comProximoComplementoVazio().preencherComTexto(DateUtil.dateHourToString(new Date()))
	    			.lancarMovimento();
	    			atoComunicacaoService.registraCienciaAutomatizada(pe.getProcessoParteExpedienteList());
	    			count++;
	    		} catch (MuralException e) {
	    			String msg = "Erro ao publicar o processo {0} no Mural eletrônico";
	    	        FacesMessages.instance().add(Severity.ERROR, msg, pe.getProcessoTrf().getNumeroProcesso());
	    	        logger.error(msg, pe.getProcessoTrf().getNumeroProcesso());
	    	        StringBuffer erro = new StringBuffer();
					erro.append("Houve um erro na comunicação com o Mural eletrônico e o expediente não será publicado. Por gentileza, entre em contato com o suporte. ");
					if(e.getLocalizedMessage() != null) {
						erro.append( "Possível motivo: ");
						erro.append(e.getLocalizedMessage());
					}
					tramitacaoProcessualService.gravaVariavel(VARIAVEL_ERROS_CONECTOR_MURAL, erro.toString());	
			        ComponentUtil.getProcessoExpedienteManager().fecharExpedientes(pe);
			        if(!pe.getDocumentoExistente()) {
			        	 pe.getProcessoDocumento();
			        	 try {
							ComponentUtil.getProcessoDocumentoManager().excluirDocumento(pe.getProcessoDocumento(), Authenticator.getUsuarioSistema(), erro.toString());
			        	 } catch (PJeBusinessException e1) {
			        		 logger.error("Não foi possível excluir documento após erro de publicação no Mural eletrônico.");
			        		 e1.printStackTrace();
			        	 }
			        }
			        if(pe.getProcessoDocumentoExpedienteList() != null && !pe.getProcessoDocumentoExpedienteList().isEmpty()) {
			        	for(ProcessoDocumentoExpediente procDocExpeVinculado: pe.getProcessoDocumentoExpedienteList()) {
			        		procDocExpeVinculado.getProcessoDocumento();
			        		
			        		if(procDocExpeVinculado.getProcessoDocumento().getSelected() == null){
			        			try {
			        				ComponentUtil.getProcessoDocumentoManager().excluirDocumento(procDocExpeVinculado.getProcessoDocumento(), Authenticator.getUsuarioSistema(), erro.toString());
			        			} catch (PJeBusinessException e1) {
			        				logger.error("Não foi possível excluir documentos vinculados ao exepediente após erro de publicação no Mural eletrônico.");
			        				e1.printStackTrace();
			        			}
			        		}
			        	}
			        }
			        throw new NullPointerException();
	    		}
    		}  
    	}
    	FacesMessages.instance().add(Severity.INFO, "Comunicação realizada com sucesso. ");
    	return count;
    }

    
    @Transactional
    private int enviarExpedientesPorTelegrama() {
        int cont = 0;
        
        tramitacaoProcessualService.apagaVariavel(VARIAVEL_ERROS_CONECTOR_TELEGRAMA);
        errosConectorTelegrama = new ArrayList<String>(0);
        String parametroAtivacaoTelegrama = "ativado";

        for (ProcessoExpediente pe : expedientes) {
            if (pe.getMeioExpedicaoExpediente() == ExpedicaoExpedienteEnum.G) {
                for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()) {
                	try{
	                    RemetenteECT remetente = geraRemetente(ppe, parametroAtivacaoTelegrama);

	                    byte[] documento = geraDocumentos(pe, parametroAtivacaoTelegrama);

	                    ppe = EntityUtil.refreshEntity(ppe);
	                    for (ProcessoParteExpedienteEndereco e : ppe.getProcessoParteExpedienteEnderecoList()) {
	                    	if(e.getNumeroAr() == null){
		                    	DestinatarioECT destinatario = geraDestinatario(ppe, e);
	                        
		                    	Usuario usuario = Authenticator.getUsuarioLogado();
		                    	
		                    	String[] auxiliar = new String[10];
		                    	auxiliar[1] = " - ";
		                    	auxiliar[7] = ConsultaExpedienteAction.criptografaIdProcessoParteExpediente(ppe.getIdProcessoParteExpediente());
		                    	auxiliar[8] = String.valueOf(pe.getIdProcessoExpediente());
		                    	auxiliar[9] = usuario.getLogin();

	                            if (conectorTelegrama != null && parametroAtivacaoTelegrama != null) {
									String codigoEnvio = conectorTelegrama.enviaTelegrama(
											ppe.getProcessoJudicial().getIdProcessoTrf(),
											ppe.getIdProcessoParteExpediente(),
											ppe.getProcessoJudicial().getNumeroProcesso(),
											remetente, 
											destinatario,
											documento,
											auxiliar
									);
									
									if (codigoEnvio != null && !codigoEnvio.trim().isEmpty()) {
										try {
											e.setNumeroAr(codigoEnvio);
											processoParteExpedienteManager.persistAndFlush(ppe);
										} catch (PJeBusinessException be) {
											be.printStackTrace();
											String msg = "Erro ao gravar expediente [" + ppe.getNomePessoaParte()
													+ " - " + ppe.getProcessoExpediente().getTipoProcessoDocumento()
													+ " - CEP " + e.getEndereco().getCep() + " ERRO: "
													+ be.getLocalizedMessage() + "]";

											// caso de erro ao gravar o
											// expediente,
											// o numero do AR deve ser cancelado
											// no SGP
											if (e.getNumeroAr() != null) {
												try {
													conectorTelegrama.cancelaTelegrama(e.getNumeroAr());
												} catch (PontoExtensaoException pex) {
													be.printStackTrace();
													msg = msg + "[Foi gerado o protocolo " + e.getNumeroAr()
															+ " porem nao foi possivel cancela-lo. ERRO "
															+ pex.getLocalizedMessage() + "]";
												}
											}

											errosConectorTelegrama.add(msg);
										}
									} else {
										errosConectorTelegrama.add("Nao foi gerado protocolo de envio de Telegrama para o expediente ["
												+ ppe.getNomePessoaParte() + " - "
												+ ppe.getProcessoExpediente().getTipoProcessoDocumento() + "]");
									}
								}
	                    	}
	                    	
                            if (getElMovimento() != null) {
                                lancarMovimentacoesExpedientes(pe);
                            }
	                    	
	                    	cont++;
	                    }
					} catch (PontoExtensaoException e) {
						e.printStackTrace();
						errosConectorTelegrama.add("Erro ao enviar Telegrama [" + ppe.getNomePessoaParte() + " ERRO: "
								+ e.getLocalizedMessage() + "]");
					} catch (Exception e) {
						e.printStackTrace();
						errosConectorTelegrama.add("Erro inesperado ao tratar expediente [" + ppe.getNomePessoaParte()
								+ " ERRO: " + e.getLocalizedMessage() + "]");
					}
                }
            }
        }
        
        tramitacaoProcessualService.gravaVariavel(VARIAVEL_ERROS_CONECTOR_TELEGRAMA, errosConectorTelegrama);
        return cont;
	}

	private RemetenteECT geraRemetente(ProcessoParteExpediente ppe, String parametroAtivacao) {
		OrgaoJulgador oj = ppe.getProcessoJudicial()
		                      .getOrgaoJulgador();
		Localizacao localizacao = oj.getLocalizacao(); 
		Endereco origem = localizacao.getEndereco();
		if (origem == null) {
			return null;
		}
		
		RemetenteECT remetente = new RemetenteECT();

		if (conectorTelegrama != null && parametroAtivacao != null) {
		    remetente.setBairro(origem.getNomeBairro());
		    remetente.setCep(origem.getCep().getNumeroCep());
		    remetente.setCidade(origem.getNomeCidade());
		    remetente.setComplemento(origem.getComplemento());
		    remetente.setEstado(origem.getCep().getMunicipio().getEstado().getCodEstado());
		    remetente.setLogradouro(origem.getNomeLogradouro());
		    remetente.setNome(oj.getOrgaoJulgador());
		    remetente.setPontoReferencia("");
		    remetente.setTelefone(oj.getDddTelefone() +
		        oj.getNumeroTelefone());
		}
		return remetente;
	}

	private byte[] geraDocumentos(ProcessoExpediente pe, String parametroAtivacao) {
		byte[] documento = null;

		if (conectorECT != null && parametroAtivacao != null) {                        
			//gerando PDF com o expediente e seus documentos vinculados	                    	
			if(pe.getProcessoDocumentoExpedienteList() != null){
				//ordenando a lista de documentos. Primerio o doc principal, depois os anexos
		    	List<ProcessoDocumentoExpediente> pdeList = new ArrayList<ProcessoDocumentoExpediente>(0);
		    	pdeList.addAll(pe.getProcessoDocumentoExpedienteList());
		    	Collections.sort(pdeList, new Comparator<ProcessoDocumentoExpediente> (){
					@Override
					public int compare(ProcessoDocumentoExpediente o1, ProcessoDocumentoExpediente o2) {
						if(o1.getAnexo() == false && o2.getAnexo() == true){
							return -1;
						}else if(o1.getAnexo() == true && o2.getAnexo() == false){
							return +1;
						}
						
						return 0;
					}
		    	});
				
				List<byte[]> documentos = new ArrayList<byte[]>(0);
				for(ProcessoDocumentoExpediente pde : pdeList){
					ProcessoDocumentoBin pdBin = pde.getProcessoDocumento().getProcessoDocumentoBin();	                    			
					if(pdBin.isBinario()){
						if(pdBin.getProcessoDocumento() != null){
		    				documento = processoDocumentoBinManager.adicionaRodapeAssinatura(pdBin.getProcessoDocumento(), pdBin);
		    				documentos.add(documento);
						}
					}else{
						String modeloDocumento = pdBin.getModeloDocumento().trim();
						if(modeloDocumento != null && !modeloDocumento.isEmpty()) {
		    				documento = processoDocumentoBinManager.convertHtml2Pdf(pdBin, new Util().getUrlProject());
		    				documento = processoDocumentoBinManager.adicionaRodapeAssinatura(documento, pdBin);
		    				documentos.add(documento);
						}
					}
				}
			
				documento = processoDocumentoBinManager.unificarPDFs(documentos);
			}
		}
		return documento;
	}

	private static String trataNulo(final String s) {
		return (s != null && !s.trim().isEmpty()) ? s : "";
	}
	
	@Transactional
	private DestinatarioECT geraDestinatario(ProcessoParteExpediente ppe, ProcessoParteExpedienteEndereco e) {
		DestinatarioECT destinatario = new DestinatarioECT();
		
		if (ppe != null && ppe.getPessoaParte() != null && ppe.getPessoaParte().getNome() != null && !ppe.getPessoaParte().getNome().isEmpty()) {
			destinatario.setNome(ppe.getPessoaParte().getNome());
		}
		
		if (e == null || e.getEndereco() == null) {
			return destinatario;
		}
		Endereco end = e.getEndereco();
		Cep cepEnd = end.getCep();
		Cep cep;
		try {
			cep = cepManager.findById(cepEnd.getIdCep());
		} catch (PJeBusinessException pjeBusinessException) {
			cep = null;
			pjeBusinessException.printStackTrace();
		}
		
		String numeroCep = (cep != null) ? cep.getNumeroCep() : null;
		Municipio municipio = (cep != null) ? cep.getMunicipio() : null;
		
		Estado estado = null;
		try {
			estado = (municipio != null) ? municipio.getEstado() : null;
		} catch (LazyInitializationException lazyInitializationException) {
			try {
				int idEstado = municipio.getEstado().getIdEstado();
				estado = estadoManager.findById(idEstado);
			} catch (PJeBusinessException pjeBusinessException) {
				// ignora, assume que não conhece o estado.
			}
		}
		
		String codEstado = (estado != null) ? estado.getCodEstado() : null;
		destinatario.setNumero(trataNulo(end.getNumeroEndereco()));
		destinatario.setBairro(trataNulo(end.getNomeBairro()));
		destinatario.setCep(trataNulo(numeroCep));
		destinatario.setCidade(trataNulo(municipio.getMunicipio()));
		destinatario.setComplemento(trataNulo(end.getComplemento()));
		destinatario.setEstado(trataNulo(codEstado));
		destinatario.setLogradouro(trataNulo(end.getNomeLogradouro()));
		destinatario.setPontoReferencia("");
		
		return destinatario;
	}

    public void enviarDEJT(String login, String senha, String xml,
        byte[] xmlAssinado, Calendar dataPublicacao)
        throws PontoExtensaoException {
        if (publicadorDJE != null) {
            publicadorDJE.publicar(login, senha, xml, xmlAssinado,
                dataPublicacao);
        } else {
        	
        	throw new IllegalArgumentException("Erro ao localizar conector do DEJT. Por favor contatar o administrador do sistema.");
        }
    }

    /**
     * Chama o PublicadorDJE para publicar o documento no Diario Eletronico via webservice
     * 
     * @param usuarioLogado usuário logado
     * @param numeroProcesso Número do processo
     * @param numeroProcessoExpediente código identificador do ProcessoExpediente
     * @param comunicado Texto HTML
     * @throws PontoExtensaoException Se ocorrer alguma exceção durante a publicação
     */
    public void enviarDEJE(String usuarioLogado, String numeroProcesso, String numeroProcessoExpediente,
        String comunicado) throws PontoExtensaoException {
        if (publicadorDJE != null) {
            publicadorDJE.publicar(usuarioLogado, numeroProcesso, numeroProcessoExpediente, comunicado);
        } else {
        	throw new IllegalArgumentException("Erro ao localizar conector do DEJE. Por favor contatar o administrador do sistema.");
        }
    }
    
    public void registrarCienciaExpedientePessoal(){    
		init();	
    	for (ProcessoExpediente pe : expedientes) {
            if (pe.getMeioExpedicaoExpediente().isExpedicaoRealizadaPessoalmente()) {                
            	atoComunicacaoService.registraCienciaPessoal(pe.getProcessoParteExpedienteList());                
            }
    	}                
    }    
    
    public List<ProcessoExpediente> getExpedientes(ExpedicaoExpedienteEnum tipo) {
        return mapaExpedientes.get(tipo);
    }

    public List<ProcessoExpediente> getExpedientesCorreios() {
        return getExpedientes(ExpedicaoExpedienteEnum.C);
    }

    public List<ProcessoExpediente> getExpedientesEdital() {
        return getExpedientes(ExpedicaoExpedienteEnum.D);
    }
    
    public List<ProcessoExpediente> getExpedientesMural() {
        return getExpedientes(ExpedicaoExpedienteEnum.R);
    }

    public List<ProcessoExpediente> getExpedientesEletronico() {
        return getExpedientes(ExpedicaoExpedienteEnum.E);
    }

    public List<ProcessoExpediente> getExpedientesPrecatorias() {
        return getExpedientes(ExpedicaoExpedienteEnum.L);
    }

    public List<ProcessoParteExpediente> getProcessoParteExpedientesCorreios() {
        return getProcessoParteExpedientes(ExpedicaoExpedienteEnum.C);
    }

    public List<ProcessoParteExpediente> getProcessoParteExpedientesPrecatorias() {
        return getProcessoParteExpedientes(ExpedicaoExpedienteEnum.L);
    }

    public List<ProcessoExpediente> getExpedientesTelefone() {
        return getExpedientes(ExpedicaoExpedienteEnum.T);
    }
    public List<ProcessoExpediente> getExpedientesPessoal() {
        return getExpedientes(ExpedicaoExpedienteEnum.S);
    }    
    
    public List<ProcessoExpediente> getExpedientesTelegrama() {
        return getExpedientes(ExpedicaoExpedienteEnum.G);
    }


    private List<ProcessoParteExpediente> getProcessoParteExpedientes(
        ExpedicaoExpedienteEnum tipo) {
        List<ProcessoParteExpediente> list = new ArrayList<ProcessoParteExpediente>(0);
        List<ProcessoExpediente> peList;

        if (tipo.equals(ExpedicaoExpedienteEnum.C)) {
            peList = getExpedientesCorreios();
        } else if (tipo.equals(ExpedicaoExpedienteEnum.L)) {
            peList = getExpedientesPrecatorias();
        } else {
            return list;
        }

        for (ProcessoExpediente pe : peList) {
            for (ProcessoParteExpediente ppe : pe.getProcessoParteExpedienteList()) {
                list.add(ppe);
            }
        }

        return list;
    }

    public List<ProcessoExpediente> getExpedientesMandados() {
        return getExpedientes(ExpedicaoExpedienteEnum.M);
    }

    public List<ProcessoExpediente> getExpedientesDiario() {
        return getExpedientes(ExpedicaoExpedienteEnum.P);
    }

    /**
     * @return the expedienteSelecionado
     */
    public ProcessoExpediente getExpedienteSelecionado() {
        return expedienteSelecionado;
    }

    /**
     * @param expedienteSelecionado the expedienteSelecionado to set
     */
    public void setExpedienteSelecionado(
        ProcessoExpediente expedienteSelecionado) {
        this.expedienteSelecionado = expedienteSelecionado;
    }

    public void lancarMovimentacoes(ExpedicaoExpedienteEnum tipo,
        String codigoNacional) {
        List<ProcessoExpediente> expedientes = getExpedientes(tipo);

        for (ProcessoExpediente pe : expedientes) {
            try {
                ProcessoTrf processo = pe.getProcessoParteExpedienteList().get(0)
                                         .getProcessoJudicial();
                ProcessoEvento mov = processoJudicialManager.obtemMovimentacao(codigoNacional,processo);
                String descricao = processoJudicialManager.obtemDescricaoMovimento(mov, (Object) pe);
                mov.setTextoFinalExterno(descricao);
                mov.setTextoFinalInterno(descricao); 
                mov.setProcessoDocumento(pe.getProcessoDocumento());
                processoJudicialManager.insereMovimentacoes(processo, mov);
            } catch (PJeBusinessException e) {
                logger.error("Erro ao tentar criar a movimentação processual vinculada ao processo {0}. {1}",
                    pe.getProcessoTrf().getProcesso().getNumeroProcesso(),
                    e.getLocalizedMessage());
            }
        }
    }

    /**
     * Método responsável por encaminhar expedientes com lançamento de movimentos para os diversos meios de comunicação.
     * @author Ronny Paterson (ronny.silva@trt8.jus.br) / David Vieira (davidv@trt7.jus.br)
     * @since 1.4.2
     * @see
     * @category PJE-JT
     */
    @Transactional
    public int enviarExpedientesLancarMovimentos(
        ExpedicaoExpedienteEnum tipoComunicacao,
        String varProcessoExpedienteEl, String elMovimento) {
        setElMovimento(elMovimento);
        setVarProcessoExpedienteEl(varProcessoExpedienteEl);

        int contaExpedientesEnviados = 0;

        switch (tipoComunicacao) {
        case C:
        	contaExpedientesEnviados = enviarExpedientesPeloCorreio();
            break;

        case D:
            contaExpedientesEnviados = 0;
            break;

        case E:
            contaExpedientesEnviados = enviarExpedientesPeloSistema();
            break;

        case L:
            contaExpedientesEnviados = enviarExpedientesPorCarta();
            break;

        case M:
            contaExpedientesEnviados = enviarExpedientesPelaCentralMandados();
            break;

        case P:
            contaExpedientesEnviados = enviarExpedientesDJE();
            break;
            
        case T:
            contaExpedientesEnviados = 0;
            break;
        case S:
            contaExpedientesEnviados = 0;
            break;            

        case G:
        	contaExpedientesEnviados = enviarExpedientesPorTelegrama();
            break;

        case R:
        	contaExpedientesEnviados = enviarExpedientesPorMural();
        	break; 
            
		case N:
			break;
			
		default:
			break;

        }        

        return contaExpedientesEnviados;
    }

	/**
     * Método responsável por lançar movimentações a partir do envio de expedientes.
     * @author Ronny Paterson (ronny.silva@trt8.jus.br) / David Vieira (davidv@trt7.jus.br)
     * @since 1.4.2
     * @see
     * @category PJE-JT
     */
    public void lancarMovimentacoesExpedientes(
        ProcessoExpediente processoExpediente) {
        Contexts.getEventContext()
                .set(this.getVarProcessoExpedienteEl(), processoExpediente);

        try {
            util.eval(elMovimento.replace("${", "#{"));
        } catch (Exception e) {
            throw new AplicationException("Erro ao tentar executar a EL '" +
                elMovimento.replace("${", "#{") +
                "' de lançamento de movimento do ConectorMandados#encaminharExpedientesMovimentacaoMandados();");
        }
    }

    public String getElMovimento() {
        return elMovimento;
    }

    public void setElMovimento(String elMovimento) {
        this.elMovimento = elMovimento;
    }

    public String getVarProcessoExpedienteEl() {
        return varProcessoExpedienteEl;
    }

    public void setVarProcessoExpedienteEl(String varProcessoExpedienteEl) {
        this.varProcessoExpedienteEl = varProcessoExpedienteEl;
    }

    /* EL convenience getters and setters */
    public String getExpedicaoExpedienteEnum() {
        return (expedicaoExpedienteEnum == null) ? null
                                                 : expedicaoExpedienteEnum.name();
    }

    public void setStatus(String expedicaoExpedienteEnum) {
        this.expedicaoExpedienteEnum = ExpedicaoExpedienteEnum.valueOf(expedicaoExpedienteEnum);
    }

    @Override
    protected BaseManager<ProcessoTrf> getManager() {
        return null;
    }    

	@SuppressWarnings("unchecked")
	public String getErrosECT(){    	
		List<String> erros = (List<String>) tramitacaoProcessualService.recuperaVariavel(VARIAVEL_ERROS_CONECTOR_ECT);

		if (erros == null) {
			return null;
		}

		List<String> errosRemover = new ArrayList<String>();
		
		List<ProcessoExpediente> expedientes = getExpedientesCorreios();
		
		for (ProcessoExpediente e : expedientes) {
			for (ProcessoParteExpediente ppe : e.getProcessoParteExpedienteList()) {

				for (ProcessoParteExpedienteEndereco end : ppe.getProcessoParteExpedienteEnderecoList()) {
					if(end.getNumeroAr() != null) {
						for(String erro : erros) {
							 if(erro.contains(end.getEndereco().getUsuario().getNome())) {
								 errosRemover.add(erro);
							 }
						}
					}
				}
			}
		}
		
		erros.removeAll(errosRemover);
    	
    	if(erros != null && !erros.isEmpty()){
    		StringBuilder result = new StringBuilder();
    		result.append("<div class=\"avisoGlobal\">");
    		result.append("  <table style=\"border:none; width:100%; text-align:left; \">");
	    	for(String erro : erros){
	    		result.append("  <tr>");
	    		result.append("    <td>"+erro+"</td>");
	    		result.append("  <tr>");
	    	}
	    	result.append("  </table>");
	    	result.append("</div>");
	    	return result.toString();
    	}
    	
    	return null;
    }
	
	 @Transactional
	 public int lancarMovimentoEenviarUltimoExpedienteParaDJE(String elMovimento, String varExpediente) {
		 setElMovimento(elMovimento);
		 setVarProcessoExpedienteEl(varExpediente);
		 ProcessoExpediente ultimoExpediente = this.processoExpedienteManager.getUltimoExpedienteCriado(tramitacaoProcessualService.recuperaProcesso());
		 return enviarExpedientesDJE(ultimoExpediente);
	 }
	
}
