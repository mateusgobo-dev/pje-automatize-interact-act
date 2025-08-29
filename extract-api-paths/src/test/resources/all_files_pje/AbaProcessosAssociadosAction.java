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
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoTrfConexaoManager;
import br.jus.cnj.pje.nucleo.service.ProcessoJudicialService;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfConexao;
import br.jus.pje.nucleo.enums.PrevencaoEnum;
import br.jus.pje.nucleo.enums.TipoConexaoEnum;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe de controle de aba de processos
 * Associados na tela de detalhes do processo
 * @author Rodrigo Santos Menezes (CNJ)
 *
 */
@Name("abaProcessosAssociadosAction")
@Scope(ScopeType.PAGE)
public class AbaProcessosAssociadosAction extends BaseAction<ProcessoTrfConexao>{

	private static final long serialVersionUID = 2558288643450790931L;

	@RequestParameter(value="associadosGridCount")
	private Integer associadosGridCount;
	
	@RequestParameter(value="id")
	private Integer idProcessoTrf;
	
	@RequestParameter
	private Integer idProcesso;
	
	@In
	private ProcessoTrfConexaoManager processoTrfConexaoManager;
	
	@In
	private ProcessoJudicialService processoJudicialService;
		
	@In
	private Identity identity;
	
	private EntityDataModel<ProcessoTrfConexao> model;

	private EntityDataModel<ProcessoTrfConexao> modelPrevencao;

	private EntityDataModel<ProcessoTrfConexao> modelDependencia;

	private EntityDataModel<ProcessoTrfConexao> modelDesmembramento;
	
	private EntityDataModel<ProcessoTrfConexao> modelVinculacaoDireta;

	private String numeroProcessoPesquisa = "";
	
	private Integer idProcessoPesquisa = null;
	
	private boolean mesmoOrgaoJulgador = false;

	

	@Create
	public void init(){
		idProcessoPesquisa = idProcessoTrf != null ? idProcessoTrf : idProcesso;
		criarModelGeral();
		pesquisar();
	}
	
	public void pesquisar(){
		criarModelTooglePrevencao();
		criarModelToogleDependencia();
		criarModelToogleDesmembramento();
	}
	
	public void limpar(){
		numeroProcessoPesquisa = "";
		pesquisar();
	}
	
	/**
	 * Inicialização dos criterios de pesquisa
	 */
	public void criarModelGeral(){

		model = new EntityDataModel<ProcessoTrfConexao>(ProcessoTrfConexao.class, super.facesContext, getRetriever());
		model.setDistinct(true);
		model.addOrder("o.tipoConexao", Order.ASC);
		model.addOrder("o.processoTrfConexo", Order.ASC);
		
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		if(idProcessoPesquisa != null){
			criterios.addAll(getCriteriosAba());
			try {
				model.setCriterias(criterios);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Ocorreu um erro ao tentar recuperar os processo associados. Tente novamente");
			}
		}	
		
	}
		
	/**
	 * Método responsável pelos critérios de pesquisa de processos associados.
	 * 
	 * @return lista com os critérios de pesquisa
	 */
	private List<Criteria> getCriteriosAba(){
		List<Criteria> criterios = new ArrayList<Criteria>(0);		
		criterios.add(Criteria.equals("processoTrf.idProcessoTrf", idProcessoPesquisa));
		criterios.add(Criteria.equals("ativo", true));
		return criterios;
	}
	
	public void criarModelTooglePrevencao() {
		
		modelPrevencao = new EntityDataModel<ProcessoTrfConexao>(ProcessoTrfConexao.class, super.facesContext, getRetriever());
		modelPrevencao.setDistinct(true);
		modelPrevencao.addOrder("o.tipoConexao", Order.ASC);
		modelPrevencao.addOrder("o.processoTrfConexo", Order.ASC);
		
		gerarCriteriosTooglePR();		
                                                                                                                                                                            
	}		
	
	/**
	 * Método responsável pelos critérios de pesquisa de processos associados por prevenção.
	 * 
	 * @return lista com os critérios de pesquisa
	 */
	private void gerarCriteriosTooglePR(){
		if(idProcessoPesquisa != null){
					
			List<Criteria> criterios = new ArrayList<Criteria>(0);		
			criterios.add(Criteria.equals("processoTrf.idProcessoTrf", idProcessoPesquisa));
			criterios.add(Criteria.equals("ativo", true));
			criterios.add(Criteria.equals("tipoConexao",TipoConexaoEnum.PR));
			
			if(numeroProcessoPesquisa !=null && !numeroProcessoPesquisa.isEmpty() ){
				criterios.add(Criteria.equals("processoTrfConexo.processo.numeroProcesso",numeroProcessoPesquisa));
			}
			
			if(mesmoOrgaoJulgador){
				try {
					criterios.add(Criteria.equals("processoTrf.orgaoJulgador", processoJudicialService.findById(idProcessoPesquisa).getOrgaoJulgador()));
				} catch (PJeBusinessException e) {
					String msg = "Ocorreu um erro ao tentar recuperar os processo associados. Tente novamente\n" + e.getLocalizedMessage();
					facesMessages.add(Severity.ERROR, msg);
				}
			}
			
			try {
				modelPrevencao.setCriterias(criterios);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Ocorreu um erro ao tentar recuperar os processo associados. Tente novamente");
			}
		}
	}
	public void criarModelToogleDependencia() {
		
		modelDependencia = new EntityDataModel<ProcessoTrfConexao>(ProcessoTrfConexao.class, super.facesContext, getRetriever());
		modelDependencia.setDistinct(true);
		modelDependencia.addOrder("o.tipoConexao", Order.ASC);
		modelDependencia.addOrder("o.processoTrfConexo", Order.ASC);
		
		gerarCriteriosToogleDP();								
	}		
	
	/**
	 * Método responsável pelos critérios de pesquisa de processos associados por prevenção.
	 * 
	 * @return lista com os critérios de pesquisa
	 */
	private void gerarCriteriosToogleDP(){
		if(idProcessoPesquisa != null){
			List<Criteria> criterios = new ArrayList<Criteria>(0);		
			criterios.add(Criteria.equals("processoTrf.idProcessoTrf", idProcessoPesquisa));
			criterios.add(Criteria.equals("ativo", true));
			criterios.add(Criteria.equals("tipoConexao",TipoConexaoEnum.DP));
			
			if(numeroProcessoPesquisa !=null && !numeroProcessoPesquisa.isEmpty() ){
				criterios.add(Criteria.equals("processoTrfConexo.processo.numeroProcesso",numeroProcessoPesquisa));
			}
			if(mesmoOrgaoJulgador){
				try {
					criterios.add(Criteria.equals("processoTrf.orgaoJulgador", processoJudicialService.findById(idProcessoPesquisa).getOrgaoJulgador()));
				} catch (PJeBusinessException e) {
					String msg = "Ocorreu um erro ao tentar recuperar os processo associados. Tente novamente\n" + e.getLocalizedMessage();
					facesMessages.add(Severity.ERROR, msg);
				}
			}
			try {
				modelDependencia.setCriterias(criterios);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Ocorreu um erro ao tentar recuperar os processo associados. Tente novamente");
			}
		}
	}
	public void criarModelToogleDesmembramento() {
		
		modelDesmembramento = new EntityDataModel<ProcessoTrfConexao>(ProcessoTrfConexao.class, super.facesContext, getRetriever());
		modelDesmembramento.setDistinct(true);
		modelDesmembramento.addOrder("o.tipoConexao", Order.ASC);
		modelDesmembramento.addOrder("o.processoTrfConexo", Order.ASC);
		
		gerarCriteriosToogleDM();								
	}		
	
	/**
	 * Método responsável pelos critérios de pesquisa de processos associados por desmembramento.
	 * 
	 * @return lista com os critérios de pesquisa
	 */
	private void gerarCriteriosToogleDM(){
		if(idProcessoPesquisa != null){
			List<Criteria> criterios = new ArrayList<Criteria>(0);		
			criterios.add(Criteria.equals("processoTrf.idProcessoTrf", idProcessoPesquisa));
			criterios.add(Criteria.equals("ativo", true));
			criterios.add(Criteria.equals("tipoConexao",TipoConexaoEnum.DM));
			
			if(numeroProcessoPesquisa !=null && !numeroProcessoPesquisa.isEmpty() ){
				criterios.add(Criteria.equals("processoTrfConexo.processo.numeroProcesso",numeroProcessoPesquisa));
			}
			if(mesmoOrgaoJulgador){
				try {
					criterios.add(Criteria.equals("processoTrf.orgaoJulgador", processoJudicialService.findById(idProcessoPesquisa).getOrgaoJulgador()));
				} catch (PJeBusinessException e) {
					String msg = "Ocorreu um erro ao tentar recuperar os processo associados. Tente novamente\n" + e.getLocalizedMessage();
					facesMessages.add(Severity.ERROR, msg);
				}
			}
			try {
				modelDesmembramento.setCriterias(criterios);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Ocorreu um erro ao tentar recuperar os processo associados. Tente novamente");
			}
		}
	}
	public void criarModelToogleVinculacaoDireta() {
		
		modelVinculacaoDireta = new EntityDataModel<ProcessoTrfConexao>(ProcessoTrfConexao.class, super.facesContext, getRetriever());
		modelVinculacaoDireta.setDistinct(true);
		modelVinculacaoDireta.addOrder("o.tipoConexao", Order.ASC);
		modelVinculacaoDireta.addOrder("o.processoTrfConexo", Order.ASC);
		
		gerarCriteriosToogleDM();								
	}		
	
	/**
	 * Método responsável pelos critérios de pesquisa de processos associados por vinculação direta.
	 * 
	 * @return lista com os critérios de pesquisa
	 */
	private void gerarCriteriosToogleAS(){
		if(idProcessoPesquisa != null){
			List<Criteria> criterios = new ArrayList<Criteria>(0);		
			criterios.add(Criteria.equals("processoTrf.idProcessoTrf", idProcessoPesquisa));
			criterios.add(Criteria.equals("ativo", true));
			criterios.add(Criteria.equals("tipoConexao",TipoConexaoEnum.AS));
			
			if(numeroProcessoPesquisa !=null && !numeroProcessoPesquisa.isEmpty() ){
				criterios.add(Criteria.equals("processoTrfConexo.processo.numeroProcesso",numeroProcessoPesquisa));
			}
			if(mesmoOrgaoJulgador){
				try {
					criterios.add(Criteria.equals("processoTrf.orgaoJulgador", processoJudicialService.findById(idProcessoPesquisa).getOrgaoJulgador()));
				} catch (PJeBusinessException e) {
					String msg = "Ocorreu um erro ao tentar recuperar os processo associados. Tente novamente\n" + e.getLocalizedMessage();
					facesMessages.add(Severity.ERROR, msg);
				}
			}
			try {
				modelVinculacaoDireta.setCriterias(criterios);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Ocorreu um erro ao tentar recuperar os processo associados. Tente novamente");
			}
		}
	}
	
	/**
	 * Método responsável pela contagem dos processos efetivamente associados 
	 * removendo os tipos Não Prevento
	 * @return Long com quantidade processos efetivamente associados
	 */
	public Long getQuantidadeProcessosEfetivamenteAssociados(){
        Search search;
        try {
            search = getModel().search.copy();
            search.addCriteria(Criteria.notEquals("prevencao", PrevencaoEnum.RE));
            return getRetriever().count(search);
        } catch (NoSuchFieldException e) {
        	facesMessages.add(Severity.ERROR,"Erro ao calcular a quantidade de registros: {0}", e.getLocalizedMessage());
        }
        return 0L;      
    }
	
	/**
	 * Retriever de resultado da pesquisa do {@link ProcessoTrfConexao}
	 */
	@Override
	protected DataRetriever<ProcessoTrfConexao> getRetriever() {
		
		final ProcessoTrfConexaoManager manager = (ProcessoTrfConexaoManager)getManager();
		final Integer tableCount = associadosGridCount;
		DataRetriever<ProcessoTrfConexao> retriever = new DataRetriever<ProcessoTrfConexao>() {
			
			@Override
			public ProcessoTrfConexao findById(Object id) throws Exception {
				try {
					return manager.findById(id);
				} catch (PJeBusinessException e) {
					throw new Exception(e);
				}
			}
			
			@Override
			public List<ProcessoTrfConexao> list(Search search) {
				return manager.list(search);
			}
			
			@Override
			public long count(Search search) {
				if (tableCount != null && tableCount >=0){
					return tableCount;
				}

				return manager.count(search);
			}
			
			@Override
			public Object getId(ProcessoTrfConexao obj){
				return manager.getId(obj);
			}
		};
		return retriever;
	}
	
	@Override
	protected BaseManager<ProcessoTrfConexao> getManager() {
		return processoTrfConexaoManager;
	}

	@Override
	public EntityDataModel<ProcessoTrfConexao> getModel() {
		return model;
	}
	
	public EntityDataModel<ProcessoTrfConexao> getModelPrevencao() {
		return modelPrevencao;
	}
	
	public EntityDataModel<ProcessoTrfConexao> getModelDependencia() {
		return modelDependencia;
	}
	
	public EntityDataModel<ProcessoTrfConexao> getModelDesmembramento() {
		return modelDesmembramento;
	}
	
	public EntityDataModel<ProcessoTrfConexao> getModelVinculacaoDireta() {
		return modelVinculacaoDireta;
	}
	
	
	public String getNumeroProcessoPesquisa() {
		return numeroProcessoPesquisa;
	}

	public void setNumeroProcessoPesquisa(String numeroProcessoPesquisa) {
		this.numeroProcessoPesquisa = numeroProcessoPesquisa;
	}
	public boolean isMesmoOrgaoJulgador() {
		return mesmoOrgaoJulgador;
	}

	public void setMesmoOrgaoJulgador(boolean mesmoOrgaoJulgador) {
		this.mesmoOrgaoJulgador = mesmoOrgaoJulgador;
	}
	
	/**
	 * Verifica se o usuário tem permissão para visualizar determinado processo
	 * na tela de processo associados.
	 * 
	 * @see ProcessoJudicialService#visivel(ProcessoTrf, br.jus.pje.nucleo.entidades.UsuarioLocalizacao, Identity)
	 */
	public boolean podeVisualizarProcesso(ProcessoTrfConexao processoTrfConexao) {
		if (processoTrfConexao != null) {
			ProcessoTrf processoConexo = processoTrfConexao.getProcessoTrfConexo();
			if (processoConexo == null || !processoConexo.getSegredoJustica()) {
				return true;
			} else {
				return processoJudicialService
						.visivel(processoConexo,
								Authenticator.getUsuarioLocalizacaoAtual(),
								(Identity) Component.getInstance("org.jboss.seam.security.identity"));
			}			
		}
		return false;
	}
	
}
