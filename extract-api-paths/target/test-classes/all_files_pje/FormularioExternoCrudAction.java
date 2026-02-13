package br.jus.cnj.pje.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.ClasseJudicialSuggestBean;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.FormularioExternoManager;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.FormularioExterno;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;

@Name(FormularioExternoCrudAction.NAME)
@Scope(ScopeType.PAGE)
public class FormularioExternoCrudAction extends BaseAction<FormularioExterno> implements Serializable{

	@In
	private FormularioExternoManager formularioExternoManager;
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "formularioExternoCrudAction";
	
	private EntityDataModel<FormularioExterno> model;
	private FormularioExterno searchModel = new FormularioExterno();
	private FormularioExterno managedModel = new FormularioExterno();
	private String tab;
	private ClasseJudicial classeJudicial;
	private ClasseJudicialSuggestBean classeJudicialSuggest; 

	
	@Create
	public void init() {
		pesquisar();
	}
	
	public void ativar(FormularioExterno formulario) {
		try {
			formulario = this.formularioExternoManager.findById(formulario.getId());
			formulario.setAtivo(true);
			this.formularioExternoManager.persistAndFlush(formulario);
			this.pesquisar();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	public void inativar(FormularioExterno formulario) {
		try {
			formulario = this.formularioExternoManager.findById(formulario.getId());
			formulario.setAtivo(false);
			this.formularioExternoManager.persistAndFlush(formulario);
			this.pesquisar();
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		}
	}
	
	public void selecionar(FormularioExterno formulario) {
		try {
			this.managedModel = this.formularioExternoManager.findById(formulario.getId());
			Hibernate.initialize(this.managedModel.getClassesJudiciaisFormulario());
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		};
	}
	
	public void limparCamposPesquisa() {
		this.searchModel = new FormularioExterno();
	}
	
	public void criarFormulario() {
		try {
			this.formularioExternoManager.persistAndFlush(managedModel);
			this.pesquisar();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "Formulário criado como sucesso.");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "Ocorreu um erro ao criar o registro: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void atualizarFormulario() {
		try {
			Hibernate.initialize(this.managedModel.getClassesJudiciaisFormulario());
			
			if(this.classeJudicial != null && !this.managedModel.getClassesJudiciaisFormulario().contains(this.classeJudicial)) {
				this.managedModel.getClassesJudiciaisFormulario().add(classeJudicial);
				this.classeJudicial = null;
			}
			
			this.formularioExternoManager.merge(managedModel);
			this.formularioExternoManager.flush();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "Formulário atualizado como sucesso.");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "Ocorreu um erro ao atualizar o registro: " + e.getMessage());
			e.printStackTrace();
		}		
	}
	
	public void novoFormulario() {
		this.managedModel = new FormularioExterno();
	}
	
	public void pesquisar() {
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		criterios.addAll(getCriteriosTelaPesquisa());
		
		try {			
			model = new EntityDataModel<FormularioExterno>(FormularioExterno.class, super.facesContext, getRetriever());
			model.setCriterias(criterios);
			model.addOrder("o.nome", Order.ASC);
		} catch (Exception e) {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "Ocorreu um erro ao executar a pesquisa: " + e.getMessage());
			e.printStackTrace();
		}		
	}
	
	private List<Criteria> getCriteriosTelaPesquisa(){
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		if(!StringUtil.isEmpty(searchModel.getNome())) {
			criterios.add(Criteria.contains("nome", searchModel.getNome()));
		}
		
		return criterios;
	}
	
	public void removerClasse(ClasseJudicial classeJudicial) {
		try {
			this.managedModel = this.formularioExternoManager.findById(this.managedModel.getId());
			this.managedModel.getClassesJudiciaisFormulario().remove(classeJudicial);
			this.formularioExternoManager.persistAndFlush(managedModel);
		} catch (PJeBusinessException e) {
			e.printStackTrace();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "Ocorreu um erro ao remover uma classe associada ao formulário: " + e.getMessage());
		}
	}
	
	public ClasseJudicialSuggestBean getClasseJudicialSuggest() {
		if(classeJudicialSuggest == null) {
			classeJudicialSuggest = (ClasseJudicialSuggestBean) Component.getInstance("classeJudicialSuggest");
		}
		return classeJudicialSuggest;
	}
	
	public void setClasseJudicialSuggest(ClasseJudicialSuggestBean classeJudicialSuggest) {
		this.classeJudicialSuggest = classeJudicialSuggest;
	}
	
	public ClasseJudicial getClasseJudicial() {
		return classeJudicial;
	}
	
	public void setClasseJudicial(ClasseJudicial classeJudicial) {
		this.classeJudicial = classeJudicial;
	}
	
	public String getTab() {
		return tab;
	}
	
	public void setTab(String tab) {
		this.tab = tab;
	}
	
	public FormularioExterno getSearchModel() {
		return searchModel;
	}
	
	public void setSearchModel(FormularioExterno searchModel) {
		this.searchModel = searchModel;
	}
	
	public FormularioExterno getManagedModel() {
		return managedModel;
	}
	
	public void setManagedModel(FormularioExterno managedModel) {
		this.managedModel = managedModel;
	}

	@Override
	protected BaseManager<FormularioExterno> getManager() {
		return this.formularioExternoManager;
	}

	@Override
	public EntityDataModel<FormularioExterno> getModel() {
		return this.model;
	}
}
