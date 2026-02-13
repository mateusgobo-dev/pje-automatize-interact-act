/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
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

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PessoaOficialJusticaManager;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe de controle da tela
 * de pesquisa de {@link PessoaOficialJustica}
 * @author Rodrigo Santos Menezes
 *
 */
@Name(ConsultaOficialJusticaAction.NAME)
@Scope(ScopeType.PAGE)
public class ConsultaOficialJusticaAction extends BaseAction<PessoaOficialJustica>{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "consultaOficialJusticaAction";
	
	private String nome;
	private String cpf;
	private String matricula;
	private Boolean ativo = Boolean.TRUE;
	
	private EntityDataModel<PessoaOficialJustica> model;
	
	@RequestParameter(value="oficialJusticaGridCount")
	private Integer oficialJusticaGridCount;
	
	@In
	private PessoaOficialJusticaManager pessoaOficialJusticaManager;
	
	@In
	private PessoaService pessoaService;
	
	@In
	private LocalizacaoService localizacaoService;
	
	
	@Create
	public void init(){
		pesquisar();
	}
	
	public void pesquisar(){
		try{
			model = new EntityDataModel<PessoaOficialJustica>(PessoaOficialJustica.class, super.facesContext, getRetriever());
			model.setDistinct(true);
			model.setCriterias(getCriteriosTelaPesquisa());
			model.addOrder("o.nome", Order.ASC);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Monta os criterios da pesquisa de Oficial de Justica, conforme os filtros da tela.
	 * 
	 * @return retorna uma lista de criterios conforme os filtros da tela de pesquisa do Oficial de Justica.
	 */
	private List<Criteria> getCriteriosTelaPesquisa(){
		List<Criteria> criteriaList = new ArrayList<Criteria>(0);
		
		criteriaList.add(Criteria.equals("pessoa.unificada", false));
		
		if(getAtivo() != null){
			if(getAtivo()){
				criteriaList.add(Criteria.bitwiseAnd("pessoa.especializacoes", PessoaFisica.OFJ, PessoaFisica.OFJ));
			}else{
				criteriaList.add(Criteria.not(Criteria.bitwiseAnd("pessoa.especializacoes", PessoaFisica.OFJ, PessoaFisica.OFJ)));
			}
		}		

		if(StringUtils.isNotBlank(getNome())){
			criteriaList.add(Criteria.contains("nome", getNome().trim()));
		}
		
		if(StringUtils.isNotBlank(getCpf())){
			criteriaList.add(Criteria.equals("pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo","CPF"));
			criteriaList.add(Criteria.startsWith("pessoaDocumentoIdentificacaoList.numeroDocumento", getCpf().trim()));
		}
		
		if(StringUtils.isNotBlank(getMatricula())){
			criteriaList.add(Criteria.contains("numeroMatricula", getMatricula().trim()));
		}
		
		adicionarCriteriaLocalizacaoExcetoAdmin(criteriaList);
		
		return criteriaList;
	}

	/**
	 * Metodo para considerar a localizacao do usuario logado caso o perfil seja diferente de admin ou administrador.
	 * 
	 * @param criteriaList	lista de criterios da consulta referente a tela de pesquisa de Oficial de Justica
	 */
	private void adicionarCriteriaLocalizacaoExcetoAdmin(List<Criteria> criteriaList) {
		if (!Authenticator.isPapelAdministrador()) {
			criteriaList.add(Criteria.in("usuarioLocalizacaoList.localizacaoFisica.idLocalizacao", 
				localizacaoService.getTreeIdsList(Authenticator.getUsuarioLocalizacaoAtual().getLocalizacaoFisica()).toArray()));
		}
	}
	
	@Override
	protected DataRetriever<PessoaOficialJustica> getRetriever() {
		
		final PessoaOficialJusticaManager manager = (PessoaOficialJusticaManager)getManager();
		final Integer tableCount = oficialJusticaGridCount;
		DataRetriever<PessoaOficialJustica> retriever = new DataRetriever<PessoaOficialJustica>() {
			
			@Override
			public PessoaOficialJustica findById(Object id) throws Exception {
				try {
					return manager.findById(id);
				} catch (PJeBusinessException e) {
					throw new Exception(e);
				}
			}
			
			@Override
			public List<PessoaOficialJustica> list(Search search) {
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
			public Object getId(PessoaOficialJustica obj){
				return manager.getId(obj);
			}
		};
		return retriever;
	}	

	/**
	 * Este método inativa o oficial de justiça informado
	 * @param instance O {@link PessoaOficialJustica} informado
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String inativa(PessoaOficialJustica instance) {		
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.desespecializa(instance.getPessoa(), PessoaOficialJustica.class);
			instance = pessoaFisica.getPessoaOficialJustica();
			instance.setOficialJusticaAtivo(Boolean.FALSE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaOficialJustica.perfil.inativado");
			Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");
			e.printStackTrace();
		}
		pesquisar();
		return "update";
	}	
	
	/**
	 * Este método reativa o oficial de justiça.
	 * @param pessoaAdvogado {@link PessoaOficialJustica}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ativa(PessoaOficialJustica pessoaOficialJustica){
		try {
			PessoaFisica pessoaFisica = (PessoaFisica)pessoaService.especializa(pessoaOficialJustica.getPessoa(), PessoaOficialJustica.class);
			pessoaOficialJustica = pessoaFisica.getPessoaOficialJustica();
			pessoaOficialJustica.setOficialJusticaAtivo(Boolean.TRUE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaOficialJustica.perfil.ativado");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");
			e.printStackTrace();
		}
		pesquisar();
		return "update";
	}	
	
	public void limparCampos(){
		this.ativo = null;
		this.cpf = null;
		this.matricula = null;
		this.nome = null;
	}
	
	@Override
	protected BaseManager<PessoaOficialJustica> getManager() {
		return pessoaOficialJusticaManager;
	}
	
	@Override
	public EntityDataModel<PessoaOficialJustica> getModel() {
		return this.model;
	}
	
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	public String getCpf() {
		return cpf;
	}
	
	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	
	public String getMatricula() {
		return matricula;
	}
	
	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
}
