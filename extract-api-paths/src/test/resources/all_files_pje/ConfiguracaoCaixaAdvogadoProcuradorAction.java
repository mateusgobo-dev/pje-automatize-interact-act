/**
 * pje-comum
 * Copyright (C) 2009-2015 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.CaixaAdvogadoProcuradorAssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.CaixaAdvogadoProcuradorClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.CaixaAdvogadoProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.CaixaRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.EstadoManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.PeriodoInativacaoCaixaRepresentanteManager;
import br.jus.cnj.pje.nucleo.manager.PessoaAssistenteProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.PrioridadeProcessoManager;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcuradorAssuntoTrf;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcuradorClasseJudicial;
import br.jus.pje.nucleo.entidades.CaixaRepresentante;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PeriodoInativacaoCaixaRepresentante;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PrioridadeProcesso;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe de controle da tela de configuração de {@link CaixaAdvogadoProcurador}
 * @author Rodrigo Santos Menezes - Conselho Nacional de Justiça
 *
 */
@Name(ConfiguracaoCaixaAdvogadoProcuradorAction.NAME)
@Scope(ScopeType.EVENT)
public class ConfiguracaoCaixaAdvogadoProcuradorAction extends BaseAction<CaixaAdvogadoProcurador>{

	public static final String NAME = "configuracaoCaixaAdvogadoProcuradorAction";
	private static final long serialVersionUID = -4313338172634430522L;

	private static final Logger logger = LoggerFactory.getLogger(ConfiguracaoCaixaAdvogadoProcuradorAction.class);

	@In
	private CaixaAdvogadoProcuradorManager caixaAdvogadoProcuradorManager;
	
	@In
	private PeriodoInativacaoCaixaRepresentanteManager periodoInativacaoCaixaRepresentanteManager;
	
	@In
	private PessoaProcuradoriaManager pessoaProcuradoriaManager;
	
	@In
	private CaixaRepresentanteManager caixaRepresentanteManager;
	
	@In
	private PrioridadeProcessoManager prioridadeProcessoManager;
	
	@In
	private EstadoManager estadoManager;
	
	@In
	private AssuntoTrfManager assuntoTrfManager;
	
	@In
	private CaixaAdvogadoProcuradorAssuntoTrfManager caixaAdvogadoProcuradorAssuntoTrfManager;
	
	@In
	private ClasseJudicialManager classeJudicialManager;
	
	@In
	private CaixaAdvogadoProcuradorClasseJudicialManager caixaAdvogadoProcuradorClasseJudicialManager;
	
	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;
	
	@In
	private PessoaAssistenteProcuradoriaManager pessoaAssistenteProcuradoriaManager;

	private Date dtInicialInativa;
	
	private Date dtFinalInativa;	
	
	private CaixaAdvogadoProcurador caixa;
	
	private EntityDataModel<PeriodoInativacaoCaixaRepresentante> periodos;
	
	private EntityDataModel<CaixaRepresentante> representantes;
	
	private EntityDataModel<CaixaAdvogadoProcuradorAssuntoTrf> assuntos;
	
	private EntityDataModel<CaixaAdvogadoProcuradorClasseJudicial> classes;
	
	private PessoaProcurador procurador;
	
	private PessoaAssistenteProcuradoria assistente;
	
	@RequestParameter(value="representanteSelecionado")
	private Integer representanteSelecionado;
	
	@RequestParameter(value="idCx")
	private Integer idCx;
	
	@RequestParameter(value="assuntoAssociadoSelecionado")
	private Integer assuntoAssociadoSelecionado;
	
	@RequestParameter(value="classeAssociadaSelecionada")
	private Integer classeAssociadaSelecionada;
	
	@RequestParameter(value="perInativ")
	private Integer perInativ;
	
	private String campoAssunto;
	
	private String campoClasse;
	
	private PrioridadeProcesso prioridade;
	
	private Date dataInicialDistribuicao;
	
	private Date dataFinalDistribuicao;

	private Date dataInicialCriacaoExpediente;
	
	private Date dataFinalCriacaoExpediente;
	
	private OrgaoJulgador orgaoJulgador;
	
	private AssuntoTrf assunto;
	
	private ClasseJudicial classe;
	
	private Integer paramIdCx;

	private String cpfProcurador;
	
	private String cpfAssistente;

	private static final String MAPA_ORGAOS_JULGADORES = "pje:usuario-externo:caixa:configuracao:mapa-orgaos-julgadores";
	private static final String LISTA_ESTADOS = "pje:usuario-externo:caixa:configuracao:lista-estados";
	private static final String LISTA_PRIORIDADES = "pje:usuario-externo:caixa:configuracao:lista-prioridades";
	

	public static final String REGEX_PADRAO_INTERVALO = "((([0-9]{1,7}-[0-9]{1,7};)|([0-9]{1,7};))*(([0-9]{1,7}-[0-9]{1,7})|([0-9]{1,7}));?)";

	public String getCpfAssistente() {
		return cpfAssistente;
	}
	public void setCpfAssistente(String cpfAssistente) {
		this.cpfAssistente = cpfAssistente;
	}
	public String getCpfProcurador() {
		return cpfProcurador;
	}
	public void setCpfProcurador(String cpfProcurador) {
		this.cpfProcurador = cpfProcurador;
	}
	public Integer getParamIdCx() {
		return paramIdCx;
	}
	public void setParamIdCx(Integer paramIdCx) {
		this.paramIdCx = paramIdCx;
		carregarCaixa(paramIdCx);
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, List<OrgaoJulgador>> getCacheOrgaosJulgadores() {
		if(Contexts.getSessionContext().get(MAPA_ORGAOS_JULGADORES) == null) {
			Contexts.getSessionContext().set(MAPA_ORGAOS_JULGADORES, new HashMap<Integer,List<OrgaoJulgador>>(0));
		}
		return (Map<Integer, List<OrgaoJulgador>>)Contexts.getSessionContext().get(MAPA_ORGAOS_JULGADORES);
	}
	
	@SuppressWarnings("unchecked")
	private List<Estado> getCacheEstados() {
		if(Contexts.getSessionContext().get(LISTA_ESTADOS) == null) {
			Contexts.getSessionContext().set(LISTA_ESTADOS, new ArrayList<Estado>(0));
		}
		return (List<Estado>)Contexts.getSessionContext().get(LISTA_ESTADOS);
	}
	
	private void setCacheEstados(List<Estado> estados) {
		Contexts.getSessionContext().set(LISTA_ESTADOS, estados);
	}
	
	@SuppressWarnings("unchecked")
	private List<PrioridadeProcesso> getCachePrioridades() {
		if(Contexts.getSessionContext().get(LISTA_PRIORIDADES) == null) {
			Contexts.getSessionContext().set(LISTA_PRIORIDADES, new ArrayList<PrioridadeProcesso>(0));
		}
		return (List<PrioridadeProcesso>)Contexts.getSessionContext().get(LISTA_PRIORIDADES);
	}
	
	private void setCachePrioridades(List<PrioridadeProcesso> prioridades) {
		Contexts.getSessionContext().set(LISTA_PRIORIDADES, prioridades);
	}
	
	private void carregarCaixa(Integer idCaixa) {
		if(idCaixa != null && idCaixa > 0) {
			try {
				caixa = caixaAdvogadoProcuradorManager.findById(idCaixa);
				if(caixa != null){
					
					pesquisarPeriodosInativacao();
					pesquisarClasses();
					pesquisarAssuntos();
					pesquisarRepresentantes();
				}
				
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Ocorreu um erro ao recuperar a caixa. Por favor, tente novamente.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Método de inicialização da action
	 */
	@Create
	public void init(){
		carregarCaixa(idCx);
	}
	
	/**
	 * Método de pesquisa da aba de períodos de inativação
	 */
	public void pesquisarPeriodosInativacao(){
		DataRetriever<PeriodoInativacaoCaixaRepresentante> periodoRet 
							= new PeriodoInativacaoRetriever(periodoInativacaoCaixaRepresentanteManager, facesMessages);					
		periodos = new EntityDataModel<PeriodoInativacaoCaixaRepresentante>
							(PeriodoInativacaoCaixaRepresentante.class,	super.facesContext, periodoRet);
		periodos.setDistinct(true);
	}
	
	
	/**
	 * Método de pesquisa do agrupador de representantes da caixa
	 */
	public void pesquisarRepresentantes(){
		DataRetriever<CaixaRepresentante> cxRepRet = new CaixaRepresentanteRetriever(caixaRepresentanteManager, facesMessages);
		representantes = new EntityDataModel<CaixaRepresentante>(CaixaRepresentante.class, super.facesContext, cxRepRet);
	}
	
	/**
	 * Método de pesquisa de assuntos relacionados ao filtro da caixa
	 */
	public void pesquisarAssuntos(){
		DataRetriever<CaixaAdvogadoProcuradorAssuntoTrf> cxAs = new AssuntosRetriever(caixaAdvogadoProcuradorAssuntoTrfManager, facesMessages);
		assuntos = new EntityDataModel<CaixaAdvogadoProcuradorAssuntoTrf>(CaixaAdvogadoProcuradorAssuntoTrf.class, super.facesContext, cxAs);
		assuntos.setDistinct(true);
	}
	
	public List<AssuntoTrf> pesquisarAssuntos(Object valor) throws NumberFormatException, PJeException{
		String txt = ((String) valor).trim();
		List<AssuntoTrf> ret = new ArrayList<AssuntoTrf>();
		AssuntoTrfManager manager = ComponentUtil.getComponent(AssuntoTrfManager.class);
		//Busca pelo código da classe
		if (txt.matches("\\d*")) {
			ret.add(manager.findByCodigo(Integer.parseInt(txt)));
		}else{
			try {
				String textoPesquisa = txt.replaceAll("\\s", "%");
				Search search = new Search(AssuntoTrf.class);
				search.setDistinct(true);
				search.setMax(15);
				Criteria nome = Criteria.contains("assuntoTrf", textoPesquisa);
				search.addCriteria(nome);
				search.addCriteria(Criteria.equals("possuiFilhos", Boolean.FALSE));
				search.addCriteria(Criteria.equals("ativo", Boolean.TRUE));
				List<AssuntoTrf> assuntos = manager.list(search);
				ret.addAll(assuntos);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Houve um erro ao buscar os assuntos.");
			}
		}
		return ret;
	}
	
	/**
	 * Método de pesquisa de classes relacionadas ao filtro da caixa
	 */
	public void pesquisarClasses(){
		DataRetriever<CaixaAdvogadoProcuradorClasseJudicial> cxCl = new ClassesRetriever(caixaAdvogadoProcuradorClasseJudicialManager, facesMessages);
		classes = new EntityDataModel<CaixaAdvogadoProcuradorClasseJudicial>(CaixaAdvogadoProcuradorClasseJudicial.class, super.facesContext, cxCl);
		classes.setDistinct(true);
	}
	
	/**
	 * Retriever das consultas de {@link PeriodoInativacaoCaixaRepresentante}
	 * @author Rodrigo Santos Menezes - CNJ
	 *
	 */
	private class PeriodoInativacaoRetriever implements DataRetriever<PeriodoInativacaoCaixaRepresentante>{
		
		private Long count;
		
		private FacesMessages facesMessages;
		
		private PeriodoInativacaoCaixaRepresentanteManager manager;
		
		
		public PeriodoInativacaoRetriever(PeriodoInativacaoCaixaRepresentanteManager manager, 
										  FacesMessages facesMessages){
			this.manager = manager;
			this.facesMessages = facesMessages;
		}

		@Override
		public Object getId(PeriodoInativacaoCaixaRepresentante p) {
			return manager.getId(p);
		}

		@Override
		public PeriodoInativacaoCaixaRepresentante findById(Object id) throws Exception {
			return manager.findById(id);
		}

		@Override
		public List<PeriodoInativacaoCaixaRepresentante> list(Search search) {
			try {
				search.addCriteria(Criteria.equals("caixaAdvogadoProcurador", caixa));
				search.addOrder("o.dataInicio", Order.ASC);
				return manager.list(search);
			} catch (NoSuchFieldException nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os períodos de inatividade.");
			}
			return Collections.emptyList();
		}

		@Override
		public long count(Search search) {
			if(count == null){
				try {
					search.setMax(0);
					list(search);
					count = manager.count(search);
				} catch (Exception e) {
					facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de períodos de inatividade.");
					return 0;
				}
			}
			return count;
		}
	};
	
	/**
	 * Retriever das consultas de {@link CaixaRepresentante}
	 * @author Rodrigo Santos Menezes - CNJ
	 *
	 */	
	private class CaixaRepresentanteRetriever implements DataRetriever<CaixaRepresentante>{
		private Long count;		
		private FacesMessages facesMessages;
		private CaixaRepresentanteManager manager;
		
		public CaixaRepresentanteRetriever(CaixaRepresentanteManager manager, FacesMessages facesMessages){
			this.manager = manager;
			this.facesMessages = facesMessages;
		}

		@Override
		public Object getId(CaixaRepresentante p) {
			return manager.getId(p);
		}

		@Override
		public CaixaRepresentante findById(Object id) throws Exception {
			return manager.findById(id);
		}

		@Override
		public List<CaixaRepresentante> list(Search search) {
			try {
				atualizarDadosPesquisa(search);
				return manager.list(search);
			} catch (NoSuchFieldException nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os representantes da caixa");
			}
			return Collections.emptyList();
		}

		@Override
		public long count(Search search) {
			if(count == null){
				try {
					atualizarDadosPesquisa(search);
					search.setMax(0);

					count = manager.count(search);
				} catch (Exception e) {
					facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar a quantidade de representantes da caixa");
					return 0;
				}
			}
			return count;
		}
		
		private void atualizarDadosPesquisa(Search search) throws NoSuchFieldException {
			List<Criteria> criterios = new ArrayList<Criteria>(0);
			criterios.add(Criteria.or(
					Criteria.bitwiseAnd("representante.especializacoes", PessoaFisica.PRO, PessoaFisica.PRO),
					Criteria.bitwiseAnd("representante.especializacoes", PessoaFisica.ASP, PessoaFisica.ASP),
					Criteria.bitwiseAnd("representante.especializacoes", PessoaFisica.ADV, PessoaFisica.ADV),
					Criteria.bitwiseAnd("representante.especializacoes", PessoaFisica.ASA, PessoaFisica.ASA)));
			
			criterios.add(Criteria.equals("caixaAdvogadoProcurador", caixa));
			
			search.addCriteria(criterios);
			search.addOrder("o.representante.nome", Order.ASC);
		}
	};	
	
	/**
	 * Retriever das consultas de {@link CaixaAdvogadoProcuradorAssuntoTrf}
	 * @author Rodrigo Santos Menezes - CNJ
	 *
	 */
	private class AssuntosRetriever implements DataRetriever<CaixaAdvogadoProcuradorAssuntoTrf>{
		
		private Long count;
		
		private FacesMessages facesMessages;
		
		private CaixaAdvogadoProcuradorAssuntoTrfManager manager;
		
		
		public AssuntosRetriever(CaixaAdvogadoProcuradorAssuntoTrfManager manager, 
										  FacesMessages facesMessages){
			this.manager = manager;
			this.facesMessages = facesMessages;
		}

		@Override
		public Object getId(CaixaAdvogadoProcuradorAssuntoTrf p) {
			return manager.getId(p);
		}

		@Override
		public CaixaAdvogadoProcuradorAssuntoTrf findById(Object id) throws Exception {
			return manager.findById(id);
		}

		@Override
		public List<CaixaAdvogadoProcuradorAssuntoTrf> list(Search search) {
			try {
				search.addCriteria(Criteria.equals("caixaAdvogadoProcurador", caixa));
				return manager.list(search);
			} catch (NoSuchFieldException nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar os assuntos associados ao filtro");
			}
			return Collections.emptyList();
		}

		@Override
		public long count(Search search) {
			if(count == null){
				try {
					search.setMax(0);
					list(search);
					count = manager.count(search);
				} catch (Exception e) {
					facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar a quantidade de assuntos relacionados");
					return 0;
				}
			}
			return count;
		}
	};	

	/**
	 * Retriever das consultas de {@link CaixaAdvogadoProcuradorClasseJudicial}
	 * @author Rodrigo Santos Menezes - CNJ
	 *
	 */
	private class ClassesRetriever implements DataRetriever<CaixaAdvogadoProcuradorClasseJudicial>{
		
		private Long count;
		
		private FacesMessages facesMessages;
		
		private CaixaAdvogadoProcuradorClasseJudicialManager manager;
		
		
		public ClassesRetriever(CaixaAdvogadoProcuradorClasseJudicialManager manager, 
										  FacesMessages facesMessages){
			this.manager = manager;
			this.facesMessages = facesMessages;
		}

		@Override
		public Object getId(CaixaAdvogadoProcuradorClasseJudicial p) {
			return manager.getId(p);
		}

		@Override
		public CaixaAdvogadoProcuradorClasseJudicial findById(Object id) throws Exception {
			return manager.findById(id);
		}

		@Override
		public List<CaixaAdvogadoProcuradorClasseJudicial> list(Search search) {
			try {
				search.addCriteria(Criteria.equals("caixaAdvogadoProcurador", caixa));
				return manager.list(search);
			} catch (NoSuchFieldException nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar as classes relacionadas ao filtro");
			}
			return Collections.emptyList();
		}

		@Override
		public long count(Search search) {
			if(count == null){
				try {
					search.setMax(0);
					list(search);
					count = manager.count(search);
				} catch (Exception e) {
					facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar a quantidade de classes relacionadas ao filtro");
					return 0;
				}
			}
			return count;
		}
	};
	
	/**
	 * Método responsável por verificar se o usuário pode visualizar a aba
	 * @return
	 */
	public boolean podeVisualizarAba(){
		return (Authenticator.getIdProcuradoriaAtualUsuarioLogado() != null && Authenticator.getIdProcuradoriaAtualUsuarioLogado() > 0 ? true : false);
	}
	
	/**
	 * Método responsável pela atualização das caixas
	 */
	public void incluirCaixa(){
		if(caixa != null){
			try {
				getManager().persistAndFlush(caixa);
				facesMessages.add(Severity.INFO,"Informação gravada com sucesso");
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Ocorreu um erro ao gravar a caixa. Por favor, tente novamente");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Método responsável pela deleção de período de inativação
	 * @param periodoSelecionado
	 */
	public void inativaPeriodo(){
		try {
			
			if(perInativ != null){
				PeriodoInativacaoCaixaRepresentante periodoSelecionado = periodoInativacaoCaixaRepresentanteManager.findById(perInativ);
				if(periodoSelecionado != null){
					periodoInativacaoCaixaRepresentanteManager.remove(periodoSelecionado);
					periodoInativacaoCaixaRepresentanteManager.flush();
					pesquisarPeriodosInativacao();
				}
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR,"Ocorreu um erro ao tentar excluir o período de inativação. Por favor, tente novamente");
			logger.error("Ocorreu um erro ao tentar excluir o período de inativação");
			e.printStackTrace();
		}
	}
	
	/**
	 * Método responsável pela inclusão de um período de inativação
	 */
	public void incluirPeriodo(){
		if(dtInicialInativa == null){
			facesMessages.add(Severity.WARN, "É necessário informar a data inicial.");
			logger.debug("Data inicial não informada.");
		} else if(DateUtil.isDataMenor(dtInicialInativa, new Date())) {
			facesMessages.add(Severity.WARN, "Data inicial tem que ser igual ou superior a data atual.");
			logger.debug("Data inicial tem que ser igual ou superior a data atual.");
		} else if(dtFinalInativa != null && DateUtil.isDataMaior(dtInicialInativa, dtFinalInativa)){
			facesMessages.add(Severity.WARN, "A data inicial não pode ser superior a data final.");
			logger.debug("A data inicial não pode ser superior a data final.");
		} else {

			PeriodoInativacaoCaixaRepresentante picr = new PeriodoInativacaoCaixaRepresentante();
			
			if (dtFinalInativa != null) {
				picr.setDataFim(DateUtil.getEndOfDay(dtFinalInativa));
			}	
			
			picr.setDataInicio(dtInicialInativa);
			picr.setCaixaAdvogadoProcurador(caixa);
			
			try {
				periodoInativacaoCaixaRepresentanteManager.persistAndFlush(picr);
				dtInicialInativa = null;
				dtFinalInativa = null;
				init();
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Ocorreu um erro ao tentar gravar o período de inativação.");
			}
		}
	}
	
	
	/**
	 * Método responsável por resetar a lista de representantes selecionados na tela
	 */
	public void resetSelecionados(){
		Contexts.getPageContext().set("idsProcuradoresSelecionados", new ArrayList<Integer>());
		Contexts.getPageContext().set("idsAssistentesSelecionados", new ArrayList<Integer>());		
	}
	
	public void excluirCaixaRepresentante(){
		try {
			if(representanteSelecionado != null){
				CaixaRepresentante cxRep = caixaRepresentanteManager.findById(representanteSelecionado);
				if(cxRep != null){
					caixaRepresentanteManager.remove(cxRep);
					caixaRepresentanteManager.flush();
					pesquisarRepresentantes();
				}
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR, "Não foi possível excluir o representante desta caixa");
			e.printStackTrace();
		} 		
	}

	public void excluirAssuntoCaixa(){
		try {
			if(assuntoAssociadoSelecionado != null){
				CaixaAdvogadoProcuradorAssuntoTrf capatrf;
				capatrf = caixaAdvogadoProcuradorAssuntoTrfManager.findById(assuntoAssociadoSelecionado);
				if(capatrf != null){
					caixaAdvogadoProcuradorAssuntoTrfManager.remove(capatrf);
					caixaAdvogadoProcuradorAssuntoTrfManager.flush();
					pesquisarAssuntos();
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Ocorreu um erro ao tentar excluir o assunto selecionado. Por favor, tente novamente");
		} 
	}
	
	public void excluirClasseCaixa(){
		try{
			if(classeAssociadaSelecionada != null){
				CaixaAdvogadoProcuradorClasseJudicial capcj = new CaixaAdvogadoProcuradorClasseJudicial();
				capcj = caixaAdvogadoProcuradorClasseJudicialManager.findById(classeAssociadaSelecionada);
				if(capcj != null){
					caixaAdvogadoProcuradorClasseJudicialManager.remove(capcj);
					caixaAdvogadoProcuradorClasseJudicialManager.flush();
					pesquisarClasses();
				}
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			facesMessages.add(Severity.ERROR, "Ocorreu um erro ao tentar excluir a classe selecionada. Por favor, tente novamente");
		} 
	}
	
	/**
	 * Recupera a lista de prioridades ativas da instalação.
	 * 
	 * @return a lista de prioridades
	 */
	public List<PrioridadeProcesso> getPrioridades(){
		List<PrioridadeProcesso> prioridades = getCachePrioridades();
		
		if(prioridades.size() == 0) {
			try {
				prioridades = prioridadeProcessoManager.listActive();
				setCachePrioridades(prioridades);
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Houve um erro ao tentar recuperar as listas de prioridades: {0}.", e.getLocalizedMessage());
				return Collections.emptyList();
			}
		}
		return prioridades;
	}	
	
	public List<Estado> getEstados(){
		List<Estado> estados = getCacheEstados();
		
		if(estados.size() == 0) {
			estados = estadoManager.estadoItems();
			setCacheEstados(estados);
		}
		return estados;
	}
	
	public String getDescricaoParaExibicao(Object selected){
		String selecionado = "";
		if (selected == null || selected.toString() == null){
			return selecionado;
		}
		else{
			if (selected.toString().length() > 25){
				selecionado = selected.toString().substring(0, 25) + "...";
			}
			else{
				selecionado = selected.toString();
			}
			return selecionado;
		}
	}
	
	public void incluirFiltros(){
		try {
			//Valida o padrão do campo intervalo vide PJEII-18836
			if((caixa.getIntervaloNumeroProcesso() == null) ||
				(caixa.getIntervaloNumeroProcesso() != null && caixa.getIntervaloNumeroProcesso().isEmpty()) ||
				caixa.getIntervaloNumeroProcesso().matches(REGEX_PADRAO_INTERVALO)){
				
				caixaAdvogadoProcuradorManager.persistAndFlush(caixa);
				if(assunto != null){
					CaixaAdvogadoProcuradorAssuntoTrf capat = new CaixaAdvogadoProcuradorAssuntoTrf();
					capat.setAssuntoTrf(assunto);
					capat.setCaixaAdvogadoProcurador(caixa);
					caixaAdvogadoProcuradorAssuntoTrfManager.persistAndFlush(capat);
					pesquisarAssuntos();
					assunto = null;
				}
	
				if(classe != null){
					CaixaAdvogadoProcuradorClasseJudicial capcj = new CaixaAdvogadoProcuradorClasseJudicial();
					capcj.setClasseJudicial(classe);
					capcj.setCaixaAdvogadoProcurador(caixa);
					caixaAdvogadoProcuradorClasseJudicialManager.persistAndFlush(capcj);
					pesquisarClasses();
					classe = null;
				}
				
				facesMessages.add(Severity.INFO,"Filtros salvos com sucesso");
			} else {
				facesMessages.add(Severity.ERROR,"Intervalo de sequencia inválido");
			}
		} catch (PJeBusinessException e) {
			facesMessages.add(Severity.ERROR,"Ocorreu um erro ao tentar persistir a caixa");
			e.printStackTrace();
		}	
	}
	
	public List<OrgaoJulgador> findOrgaosJulgadoresByJurisdicao(){
		List<OrgaoJulgador> lista = new ArrayList<OrgaoJulgador>(0);
		if(caixa != null){
			lista = getCacheOrgaosJulgadores().get(caixa.getJurisdicao().getIdJurisdicao());
			if(lista == null) {
				lista = orgaoJulgadorManager.findAllbyJurisdicao(caixa.getJurisdicao());
				if(lista == null){
					lista = new ArrayList<OrgaoJulgador>(0);
				}
				else {
					getCacheOrgaosJulgadores().put(caixa.getJurisdicao().getIdJurisdicao(), lista);
				}
			}
		}
		return lista;
	}
	
	public boolean isAssistenteProcuradoria(PessoaFisica pessoaFisica) {
		return (pessoaFisica.getEspecializacoes() & PessoaFisica.ASP) == PessoaFisica.ASP;
	}
	
	@Override
	protected BaseManager<CaixaAdvogadoProcurador> getManager() {
		return caixaAdvogadoProcuradorManager;
	}

	@Override
	public EntityDataModel<CaixaAdvogadoProcurador> getModel() {
		return null;
	}
	
	public void setCaixa(CaixaAdvogadoProcurador caixa) {
		this.caixa = caixa;
	}
	
	public CaixaAdvogadoProcurador getCaixa() {
		return caixa;
	}
	
	public EntityDataModel<PeriodoInativacaoCaixaRepresentante> getPeriodos() {
		return periodos;
	}
	
	public EntityDataModel<CaixaRepresentante> getRepresentantes() {
		return representantes;
	}
	
	public EntityDataModel<CaixaAdvogadoProcuradorClasseJudicial> getClasses() {
		return classes;
	}
	
	public EntityDataModel<CaixaAdvogadoProcuradorAssuntoTrf> getAssuntos() {
		return assuntos;
	}
	
	public Date getDtFinalInativa() {
		return dtFinalInativa;
	}
	
	public void setDtFinalInativa(Date dtFinalInativa) {
		this.dtFinalInativa = dtFinalInativa;
	}
	
	public Date getDtInicialInativa() {
		return dtInicialInativa;
	}
	
	public void setDtInicialInativa(Date dtInicialInativa) {
		this.dtInicialInativa = dtInicialInativa;
	}
	
	public String getCampoAssunto() {
		return campoAssunto;
	}

	public void setCampoAssunto(String campoAssunto) {
		this.campoAssunto = campoAssunto;
	}

	public String getCampoClasse() {
		return campoClasse;
	}

	public void setCampoClasse(String campoClasse) {
		this.campoClasse = campoClasse;
	}

	public Date getDataInicialDistribuicao() {
		return dataInicialDistribuicao;
	}

	public void setDataInicialDistribuicao(Date dataInicialDistribuicao) {
		this.dataInicialDistribuicao = dataInicialDistribuicao;
	}

	public Date getDataFinalDistribuicao() {
		return dataFinalDistribuicao;
	}

	public void setDataFinalDistribuicao(Date dataFinalDistribuicao) {
		this.dataFinalDistribuicao = dataFinalDistribuicao;
	}

	public Date getDataInicialCriacaoExpediente() {
		return dataInicialCriacaoExpediente;
	}
	public void setDataInicialCriacaoExpediente(Date dataInicialCriacaoExpediente) {
		this.dataInicialCriacaoExpediente = dataInicialCriacaoExpediente;
	}
	public Date getDataFinalCriacaoExpediente() {
		return dataFinalCriacaoExpediente;
	}
	public void setDataFinalCriacaoExpediente(Date dataFinalCriacaoExpediente) {
		this.dataFinalCriacaoExpediente = dataFinalCriacaoExpediente;
	}
	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}
	
	public PrioridadeProcesso getPrioridade() {
		return prioridade;
	}
	
	public void setPrioridade(PrioridadeProcesso prioridade) {
		this.prioridade = prioridade;
	}
	
	public AssuntoTrf getAssunto() {
		return assunto;
	}
	
	public void setAssunto(AssuntoTrf assunto) {
		this.assunto = assunto;
	}
	
	public ClasseJudicial getClasse() {
		return classe;
	}
	
	public void setClasse(ClasseJudicial classe) {
		this.classe = classe;
	}
	
	public List<ClasseJudicial> pesquisarClasses(Object valor){
		String txt = ((String) valor).trim();
		List<ClasseJudicial> ret = new ArrayList<ClasseJudicial>();
		ClasseJudicialManager manager = ComponentUtil.getComponent(ClasseJudicialManager.class);
		//Busca pelo código da classe
		if (txt.matches("\\d*")) {
			ret.add(manager.findByCodigo(txt));
		}else{
			try {
				String textoPesquisa = txt.replaceAll("\\s", "%");
				Search search = new Search(ClasseJudicial.class);
				search.setDistinct(true);
				search.setMax(15);
				Criteria nome = Criteria.contains("classeJudicial", textoPesquisa);
				search.addCriteria(nome);
				search.addCriteria(Criteria.equals("possuiFilhos", Boolean.FALSE));
				search.addCriteria(Criteria.equals("ativo", Boolean.TRUE));
				List<ClasseJudicial> classes = manager.list(search);
				ret.addAll(classes);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Houve um erro ao buscar as classes judiciais.");
			}
		}
		return ret;
	}

	public PessoaProcurador getProcurador() {
		return this.procurador;
	}
	
	public void setProcurador(PessoaProcurador pessoaProcurador) {
		this.procurador = pessoaProcurador;
	}
	
	public List<PessoaProcurador> pesquisarProcuradores(Object valor){
		String txt = (String) valor;
		boolean isNumeric = false;
		List<PessoaProcurador> ret = new ArrayList<PessoaProcurador>();
		PessoaProcuradorManager manager = ComponentUtil.getComponent(PessoaProcuradorManager.class);
		try {
			String textoPesquisa = txt.replaceAll("\\.", "").replaceAll("\\-", "");
			isNumeric = StringUtil.ehInteiro(textoPesquisa); 

			Search search = new Search(PessoaProcurador.class);
			search.setMax(15);
				
			search.addCriteria(Criteria.equals("pessoaProcuradorias.procuradoria.idProcuradoria", Authenticator.getIdProcuradoriaAtualUsuarioLogado() ));
			search.addCriteria(Criteria.equals("pessoaProcuradorias.chefeProcuradoria", false));
			if(isNumeric) {
				search.addCriteria(Criteria.startsWith("pessoa.login", textoPesquisa));
			}
			else {
				search.addCriteria(Criteria.contains("pessoa.nome", textoPesquisa));
			}
			search.addOrder("o.pessoa.nome", Order.ASC);
			List<PessoaProcurador> procuradores = manager.list(search);
			ret.addAll(procuradores);

		} catch (NoSuchFieldException e) {
			facesMessages.add(Severity.ERROR, "Houve um erro ao buscar os procuradores.");
		}
		return ret;
	}

	public void vincularProcurador() {
		if(procurador != null) {
			try {
				PessoaFisica representante = procurador.getPessoa();
				List<CaixaRepresentante> existente = this.caixaRepresentanteManager.getCaixasRepresentantes(representante.getIdPessoa(), this.caixa.getIdCaixaAdvogadoProcurador());
				if(existente == null || existente.size() == 0) {
					CaixaRepresentante caixaRepresentante = new CaixaRepresentante();
					caixaRepresentante.setRepresentante(representante);
					caixaRepresentante.setCaixaAdvogadoProcurador(this.caixa);
					this.caixaRepresentanteManager.persistAndFlush(caixaRepresentante);
					pesquisarRepresentantes();
					procurador = null;
				}
			} catch (PJeBusinessException e) {
				this.facesMessages.add(Severity.ERROR, "Houve um erro ao tentar associar o procurador {0} à caixa {1}: {2}", procurador.getNome(), caixa.getNomeCaixaAdvogadoProcurador(), e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	public PessoaAssistenteProcuradoria getAssistente() {
		return this.assistente;
	}
	
	public void setAssistente(PessoaAssistenteProcuradoria pessoaAssistente) {
		this.assistente = pessoaAssistente;
	}

	public List<PessoaAssistenteProcuradoria> pesquisarAssistentes(Object valor){
		String txt = (String) valor;
		boolean isNumeric = false;
		List<PessoaAssistenteProcuradoria> ret = new ArrayList<PessoaAssistenteProcuradoria>();
		PessoaAssistenteProcuradoriaManager manager = ComponentUtil.getComponent(PessoaAssistenteProcuradoriaManager.class);
			try {
				String textoPesquisa = txt.replaceAll("\\.", "").replaceAll("\\-", "");
				isNumeric = StringUtil.ehInteiro(textoPesquisa); 

				Search search = new Search(PessoaAssistenteProcuradoria.class);
				search.setMax(15);
				
				search.addCriteria(Criteria.bitwiseAnd("pessoa.especializacoes", PessoaFisica.ASP, PessoaFisica.ASP));
				search.addCriteria(Criteria.equals("usuarioLocalizacaoList.papel.idPapel", ParametroUtil.instance().getPapelAssistenteProcuradoria().getIdPapel()));
				search.addCriteria(Criteria.equals("usuarioLocalizacaoList.localizacaoFisica.idLocalizacao", Authenticator.getLocalizacaoFisicaAtual().getIdLocalizacao()));
				if(isNumeric) {
					search.addCriteria(Criteria.startsWith("pessoa.login", textoPesquisa));
				}
				else {
					search.addCriteria(Criteria.contains("pessoa.nome", textoPesquisa));
				}
				search.addOrder("o.pessoa.nome", Order.ASC);
				List<PessoaAssistenteProcuradoria> assistentes = manager.list(search);
				ret.addAll(assistentes);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Houve um erro ao buscar os procuradores.");
			}
		return ret;
	}

	public void vincularAssistente() {
		if(assistente != null) {
			try {
				PessoaFisica representante = assistente.getPessoa();
				List<CaixaRepresentante> existente = this.caixaRepresentanteManager.getCaixasRepresentantes(representante.getIdPessoa(), this.caixa.getIdCaixaAdvogadoProcurador());
				if(existente == null || existente.size() == 0) {
					CaixaRepresentante caixaRepresentante = new CaixaRepresentante();
					caixaRepresentante.setRepresentante(representante);
					caixaRepresentante.setCaixaAdvogadoProcurador(this.caixa);
					this.caixaRepresentanteManager.persistAndFlush(caixaRepresentante);
					pesquisarRepresentantes();
					assistente = null;
				}
			} catch (PJeBusinessException e) {
				this.facesMessages.add(Severity.ERROR, "Houve um erro ao tentar associar o assistente {0} à caixa {1}: {2}", assistente.getNome(), caixa.getNomeCaixaAdvogadoProcurador(), e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

}
