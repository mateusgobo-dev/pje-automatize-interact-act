/**
 * 
 */
package br.jus.cnj.pje.view;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.entidades.vo.PesquisaExpedientesVO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PrioridadeProcessoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoJudicialManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.TipoSituacaoExpedienteEnum;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Componente de controle da aba agrupadores do painel do usuario externo
 * Esta classe é a controladora da página Painel/painel_usuario_externo/include/abaAgraupdores.xhtml
 * 
 * @author Zeniel Chaves
 *
 */
@Name("painelUsuarioExternoAbaAgrupadoresAction")
@Scope(ScopeType.PAGE)
public class PainelUsuarioExternoAbaAgrupadoresAction extends BaseAction<ProcessoTrf> {
	
	private static final long serialVersionUID = -6592182243493983778L;

	private static ParametroUtil parametroUtil = ComponentUtil.getComponent(ParametroUtil.NAME);
	
	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.view.BaseAction#getManager()
	 */
	@Override
	protected BaseManager<ProcessoTrf> getManager() {
		return ComponentUtil.getComponent(ProcessoJudicialManager.class);
	}

	/* (non-Javadoc)
	 * @see br.jus.cnj.pje.view.BaseAction#getModel()
	 */
	@Override
	public EntityDataModel<ProcessoTrf> getModel() {
		return null;
	}

	public boolean getIsAmbienteColegiado() {
		return !parametroUtil.isPrimeiroGrau();
	}
	
	private AgrupadoresExpedientesRetriever retrieverAgrupadoresExpedientes;
	
	private EntityDataModel<ProcessoParteExpediente> expedientesAgrupador;
	
	private Long cnt;

	/**
	 * Campos do formulário de pesquisa
	 */
	private String numeroProcessoConsulta;

	private Integer numeroSequencia;
	
	private Integer digitoVerificador;
	
	private Integer ano;
	
	private String ramoJustica;
	
	private String respectivoTribunal;
	
	private Integer numeroOrigem;
	
	private String campoClasse;
	
	private String campoAssunto;
	
	private Date dataAutuacaoInicial;
	
	private Date dataAutuacaoFinal;
	
	private String nomeDestinatario;
	
	private String documentoIdentificacaoDestinatario;
	
	private String codigoOABRepresentante;
		
	private OrgaoJulgador orgaoJulgador;

	private OrgaoJulgadorColegiado orgaoJulgadorColegiado;

	private PrioridadeProcesso prioridade;
		
	private TipoSituacaoExpedienteEnum tipoSituacaoExpediente;
	
	private PesquisaExpedientesVO criteriosPesquisaExpediente = null;
	

	public String getNumeroProcessoConsulta() {
		return numeroProcessoConsulta;
	}

	public void setNumeroProcessoConsulta(String numeroProcessoConsulta) {
		this.numeroProcessoConsulta = numeroProcessoConsulta;
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

	public Integer getNumeroOrgaoJustica() {
		try {
			return Integer.parseInt(this.ramoJustica + this.respectivoTribunal);
		} catch (NumberFormatException ex) {
			return null;
		}
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

	public Integer getNumeroOrigem() {
		return numeroOrigem;
	}

	public void setNumeroOrigem(Integer numeroOrigem) {
		this.numeroOrigem = numeroOrigem;
	}

	public String getCampoClasse() {
		return campoClasse;
	}

	public void setCampoClasse(String campoClasse) {
		this.campoClasse = campoClasse;
	}

	public String getCampoAssunto() {
		return campoAssunto;
	}

	public void setCampoAssunto(String campoAssunto) {
		this.campoAssunto = campoAssunto;
	}

	public Date getDataAutuacaoInicial() {
		return dataAutuacaoInicial;
	}

	public void setDataAutuacaoInicial(Date dataAutuacaoInicial) {
		this.dataAutuacaoInicial = dataAutuacaoInicial;
	}

	public Date getDataAutuacaoFinal() {
		return dataAutuacaoFinal;
	}

	public void setDataAutuacaoFinal(Date dataAutuacaoFinal) {
		this.dataAutuacaoFinal = dataAutuacaoFinal;
	}

	public String getNomeDestinatario() {
		return nomeDestinatario;
	}

	public void setNomeDestinatario(String nomeDestinatario) {
		this.nomeDestinatario = nomeDestinatario;
	}

	public String getDocumentoIdentificacaoDestinatario() {
		return documentoIdentificacaoDestinatario;
	}

	public void setDocumentoIdentificacaoDestinatario(String documentoIdentificacaoDestinatario) {
		this.documentoIdentificacaoDestinatario = documentoIdentificacaoDestinatario;
	}

	public String getCodigoOABRepresentante() {
		return codigoOABRepresentante;
	}

	public void setCodigoOABRepresentante(String codigoOABRepresentante) {
		this.codigoOABRepresentante = codigoOABRepresentante;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgadorColegiado getOrgaoJulgadorColegiado() {
		return orgaoJulgadorColegiado;
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		this.orgaoJulgadorColegiado = orgaoJulgadorColegiado;
	}

	public PrioridadeProcesso getPrioridade() {
		return prioridade;
	}

	public void setPrioridade(PrioridadeProcesso prioridade) {
		this.prioridade = prioridade;
	}
	
	/**
	 * Recupera a lista de prioridades ativas da instalação.
	 * 
	 * @return a lista de prioridades
	 */
	public List<PrioridadeProcesso> getPrioridades(){
		try {
			return ComponentUtil.getComponent(PrioridadeProcessoManager.class).listActive();
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar as listas de prioridades: {0}.", e.getLocalizedMessage());
			return Collections.emptyList();
		}
	}

	public TipoSituacaoExpedienteEnum getTipoSituacaoExpediente() {
		return tipoSituacaoExpediente;
	}

	public void setTipoSituacaoExpediente(TipoSituacaoExpedienteEnum tipoSituacaoExpediente) {
		this.tipoSituacaoExpediente = tipoSituacaoExpediente;
	}


	/**
	 * Variaveis relacionadas à pesquisa
	 * ordem, pesquisa ativa ou não, situação marcada
	 */	
	private String ordenacao;

	private Order order = Order.DESC;

	public String getOrdenacao() {
		return ordenacao;
	}

	public void setOrdenacao(String ordenacao) {
		this.ordenacao = ordenacao;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	/**
	 * Esta função consolida as opções de pesquisa do painel na aba de agrupadores - pesquisas gerais do painel
	 * e.g. expedientes em uma dada situação / expedientes de um processo específico / expedientes para um destinatário X
	 * @return
	 */
	private PesquisaExpedientesVO getCriteriosPesquisaExpedientes() {
//        criteriosPesquisaExpediente = new PesquisaExpedientesVO(getTipoSituacaoExpediente());
//
//        criteriosPesquisaExpediente.setNumeroProcesso(this.numeroProcessoConsulta);
//        criteriosPesquisaExpediente.setNumeroSequencia(this.numeroSequencia);
//        criteriosPesquisaExpediente.setDigitoVerificador(this.digitoVerificador);
//        criteriosPesquisaExpediente.setNumeroAno(this.ano);
//        criteriosPesquisaExpediente.setNumeroOrgaoJustica(this.getNumeroOrgaoJustica());
//        criteriosPesquisaExpediente.setNumeroOrigem(this.numeroOrigem);
//        
//        criteriosPesquisaExpediente.setAssuntoJudicial(this.campoAssunto);
//        criteriosPesquisaExpediente.setClasseJudicial(this.campoClasse);
//        criteriosPesquisaExpediente.setIntervaloDataAutuacao(this.dataAutuacaoInicial, this.dataAutuacaoFinal);
//        criteriosPesquisaExpediente.setPrioridadeObj(this.prioridade);
//        criteriosPesquisaExpediente.setOrgaoJulgadorObj(this.orgaoJulgador);
//        criteriosPesquisaExpediente.setOrgaoJulgadorColegiadoObj(this.orgaoJulgadorColegiado);
//        criteriosPesquisaExpediente.setNomeDestinatario(this.nomeDestinatario);
//        criteriosPesquisaExpediente.setDocumentoIdentificacao(this.documentoIdentificacaoDestinatario);
//        criteriosPesquisaExpediente.setOabRepresentanteDestinatario(this.codigoOABRepresentante);
//
        return criteriosPesquisaExpediente;
    }	

	public boolean isPesquisaAtivada() {
		return criteriosPesquisaExpediente.isPesquisaAlterada();
	}
	
	
	public void limpaPesquisa() {
        this.numeroProcessoConsulta = null;
        this.numeroSequencia = null;
        this.digitoVerificador = null;
        this.ano = null;
        this.numeroOrigem = null;
        this.campoAssunto = null;
        this.campoClasse = null;
        this.dataAutuacaoInicial = null;
        this.dataAutuacaoFinal = null;
        this.prioridade = null;
        this.orgaoJulgador = null;
        this.orgaoJulgadorColegiado = null;
        this.nomeDestinatario = null;
        this.documentoIdentificacaoDestinatario = null;
        this.codigoOABRepresentante = null;
        
        retrieverAgrupadoresExpedientes.limpaCache();
        
        this.pesquisarAgrupadores();
	}

	private String idTipoSituacaoExpedienteSelecionada;
	
	public void setIdTipoSituacaoExpedienteSelecionada(String idTipoSituacaoExpediente) {
		this.idTipoSituacaoExpedienteSelecionada = idTipoSituacaoExpediente;
	}
	
	public String getIdTipoSituacaoExpedienteSelecionada() {
		return this.idTipoSituacaoExpedienteSelecionada;
	}	

	public void pesquisarAgrupadores() {
		if(this.ordenacao != null && !StringUtil.fullTrim(this.ordenacao).isEmpty()){
			setOrdenacao(this.ordenacao);
		}
		retrieverAgrupadoresExpedientes.setCriteriosPesquisaExpediente(this.getCriteriosPesquisaExpedientes());
		expedientesAgrupador.setRefreshPage(Boolean.TRUE);
	}
	
	public int getContadorAgrupadorPorValor(String idTipoSituacaoExpediente) {
		TipoSituacaoExpedienteEnum tipoSituacaoExpediente = TipoSituacaoExpedienteEnum.valueOf(idTipoSituacaoExpediente);
		return this.getContadorAgrupador(tipoSituacaoExpediente);
	}
	
	public int getContadorAgrupador(TipoSituacaoExpedienteEnum tipoSituacaoExpediente) {
		System.out.println("BUSCANDO CONTADOR INDICADO");
		this.setTipoSituacaoExpediente(tipoSituacaoExpediente);
		
		retrieverAgrupadoresExpedientes.setCriteriosPesquisaExpediente(this.getCriteriosPesquisaExpedientes());
		expedientesAgrupador.setRefreshPage(Boolean.TRUE);
		
		return expedientesAgrupador.getRowCount();
	}
	
	/**
	 * Recupera o modelo de dados pertinentes à pesquisa atual.
	 * 
	 * @return o modelo de dados
	 */
	public EntityDataModel<ProcessoParteExpediente> getExpedientesAgrupador(){
		System.out.println("BUSCANDO EXPEDIENTES INDICADOS");
		
		return expedientesAgrupador;
	}

/////////////////
	
	
	@Create
	public void init() throws PJeBusinessException{
		System.out.println("Acessando init()");
		String numeroOrgaoJustica = parametroUtil.recuperarNumeroOrgaoJustica();
		if(numeroOrgaoJustica != null){
			this.ramoJustica = numeroOrgaoJustica.substring(0, 1);
			this.respectivoTribunal = numeroOrgaoJustica.substring(1);
		}
		
		HttpSession session = (HttpSession)facesContext.getExternalContext().getSession(false);
		session.setMaxInactiveInterval(3600);

		ProcessoParteExpedienteManager processoParteExpedienteManager = ComponentUtil.getComponent(ProcessoParteExpedienteManager.class);
		retrieverAgrupadoresExpedientes = new AgrupadoresExpedientesRetriever(processoParteExpedienteManager, cnt);
		
		expedientesAgrupador = new EntityDataModel<ProcessoParteExpediente>(ProcessoParteExpediente.class, super.facesContext, retrieverAgrupadoresExpedientes);		
	}
	
	
	/**
	 * Classes assessórias
	 * agrupadores retriever - controla a pesquisa de expedientes nos agrupadores
	 */
	private class AgrupadoresExpedientesRetriever implements DataRetriever<ProcessoParteExpediente> {
		
		private ProcessoParteExpedienteManager manager;
		
		private Long cont;
		
		private Map<Integer, List<ProcessoParteExpediente>> mapaDataModelExpedientes = new HashMap<Integer, List<ProcessoParteExpediente>>();
		
		private PesquisaExpedientesVO criteriosPesquisaExpediente = null;
		
		public AgrupadoresExpedientesRetriever(ProcessoParteExpedienteManager manager, Long cont){
			this.manager = manager;
			this.cont = cont;
		}

		@Override
		public Object getId(ProcessoParteExpediente ac) {
			return manager.getId(ac);
		}

		@Override
		public ProcessoParteExpediente findById(Object id) throws Exception {
			return manager.findById(id);
		}

		@Override
		public List<ProcessoParteExpediente> list(Search search) {
			if(search.getOrders().isEmpty()) {
				if(criteriosPesquisaExpediente.getTipoSituacaoExpediente() != null && 
						criteriosPesquisaExpediente.getTipoSituacaoExpediente() == TipoSituacaoExpedienteEnum.SEM_PRAZO) {
					search.addOrder("ppe.dt_ciencia_parte", Order.DESC);
				} else {
					search.addOrder("ppe.dt_prazo_legal_parte", Order.ASC);
				}
			}

			try {
				return this.recuperaResultadoDoCache(search);
			} catch (PJeBusinessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return Collections.emptyList();
		}

		@Override
		public long count(Search search) {
			try {
				search.setMax(0);
				List<ProcessoParteExpediente> listaExpedientes = this.list(search);
				cont = (long) listaExpedientes.size();
			} catch (Exception e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de expedientes.");
				return 0;
			}
			return cont;
		}
		
		public void setCriteriosPesquisaExpediente(PesquisaExpedientesVO criteriosPesquisaExpediente) {
			this.criteriosPesquisaExpediente = criteriosPesquisaExpediente;
		}
		
		/**
		 * Faz a verificação se a consulta de expedientes já está no CACHE - há um CACHE por hash de consulta
		 * @return
		 * @throws PJeBusinessException
		 */
		private List<ProcessoParteExpediente> recuperaResultadoDoCache(Search search) throws PJeBusinessException{
			Integer hashCriteriosPesquisaAtual = (Integer)this.criteriosPesquisaExpediente.hashCode();
			
			List<ProcessoParteExpediente> resultadoPesquisa = null;
			if(mapaDataModelExpedientes != null && !mapaDataModelExpedientes.isEmpty()) {
				resultadoPesquisa = mapaDataModelExpedientes.get(hashCriteriosPesquisaAtual);
			}
			
			if(resultadoPesquisa == null) {
				resultadoPesquisa = manager.getExpedientes(this.criteriosPesquisaExpediente, search);
				mapaDataModelExpedientes.put(hashCriteriosPesquisaAtual, resultadoPesquisa);
			}

			return resultadoPesquisa;
		}
		
		public void limpaCache() {
			mapaDataModelExpedientes.clear();
		}
	};
}
