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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.util.Strings;
import org.richfaces.component.html.HtmlTree;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PessoaPeritoManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.Especialidade;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe de controle da tela
 * de pesquisa de {@link PessoaPerito}
 * @author Rodrigo Santos Menezes
 *
 */
@Name(ConsultaPeritoAction.NAME)
@Scope(ScopeType.PAGE)
public class ConsultaPeritoAction extends BaseAction<PessoaPerito>{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "consultaPeritoAction";
	
	private EntityDataModel<PessoaPerito> model;
	
	private String nome;
	private String cpf;
	private Boolean ativo = Boolean.TRUE;
	private Especialidade especialidade;
	
	@In
	private PessoaPeritoManager pessoaPeritoManager;
	
	@In
	private PessoaService pessoaService;
	
	
	@RequestParameter(value="peritoGridCount")
	private Integer peritoGridCount;
	
	@Create
	public void init(){
		pesquisar();
	}
	
	public void pesquisar(){
		
		try{
			model = new EntityDataModel<PessoaPerito>(PessoaPerito.class, super.facesContext, getRetriever());
			
			List<Criteria> criterios = new ArrayList<Criteria>(0);
			criterios.addAll(getCriteriosTelaPesquisa());
			
			model.setCriterias(criterios);
			model.addOrder("o.nome", Order.ASC);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public List<Criteria> getCriteriosTelaPesquisa(){
		
		List<Criteria> criterios = new ArrayList<Criteria>(0);
		
		criterios.add(Criteria.equals("pessoa.unificada", Boolean.FALSE));
		
		//Nome
		if(!Strings.isEmpty(nome)){
			criterios.add(Criteria.contains("nome", nome));
		}
		
		//peritoAtivo
		if(getAtivo() != null){
			if(getAtivo() == Boolean.TRUE){
				criterios.add(Criteria.bitwiseAnd("pessoa.especializacoes", PessoaFisica.PER, PessoaFisica.PER));
			}else{
				criterios.add(Criteria.not(Criteria.bitwiseAnd("pessoa.especializacoes", PessoaFisica.PER, PessoaFisica.PER)));
			}
		}

		//CPF
		if(!Strings.isEmpty(cpf)){
			criterios.add(Criteria.equals("pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo","CPF"));
			criterios.add(Criteria.startsWith("pessoaDocumentoIdentificacaoList.numeroDocumento", cpf));
		}
		
		//Especialidade
		if(getEspecialidade() != null){
			criterios.add(Criteria.equals("pessoaPeritoEspecialidadeList.especialidade", especialidade));
		}
				
		return criterios;
	}
	
	@Override
	protected DataRetriever<PessoaPerito> getRetriever() {
		
		final PessoaPeritoManager manager = (PessoaPeritoManager)getManager();
		final Integer tableCount = peritoGridCount;
		DataRetriever<PessoaPerito> retriever = new DataRetriever<PessoaPerito>() {
			
			@Override
			public PessoaPerito findById(Object id) throws Exception {
				try {
					return manager.findById(id);
				} catch (PJeBusinessException e) {
					throw new Exception(e);
				}
			}
			
			@Override
			public List<PessoaPerito> list(Search search) {
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
			public Object getId(PessoaPerito obj){
				return manager.getId(obj);
			}
		};
		return retriever;
	}		
	
	public void limparCampos(){
		this.ativo = null;
		this.cpf = null;
		this.nome = null;
		this.especialidade = null;
	}
	
	/**
	 * Este método inativa o perito informado
	 * @param instance O {@link PessoaPerito} informado
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String inativa(PessoaPerito instance) {		
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.desespecializa(instance.getPessoa(), PessoaPerito.class);
			instance = pessoaFisica.getPessoaPerito();
			instance.setPeritoAtivo(Boolean.FALSE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "cadastroPerito.perfil.inativado");
			Authenticator.deslogar(instance.getPessoa(), "perfil.atualizar");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "perfil.erro");
			e.printStackTrace();
		}
		pesquisar();
		return "update";
	}	
	
	/**
	 * Este método reativa o perito.
	 * @param instance {@link PessoaPerito}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ativa(PessoaPerito instance){
		try {
			PessoaFisica pessoaFisica = (PessoaFisica)pessoaService.especializa(instance.getPessoa(), PessoaPerito.class);
			instance = pessoaFisica.getPessoaPerito();
			instance.setPeritoAtivo(Boolean.TRUE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "cadastroPerito.perfil.ativado");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "perfil.erro");
			e.printStackTrace();
		}
		pesquisar();
		return "update";
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
	
	@SuppressWarnings("rawtypes")
	public void selecionarEspecialidade(NodeSelectedEvent event) {
        HtmlTree tree = (HtmlTree) event.getComponent();
        especialidade = (Especialidade)((br.com.infox.component.tree.EntityNode) tree.getRowData()).getEntity();
    }	
	
	@Override
	protected BaseManager<PessoaPerito> getManager() {
		// TODO Auto-generated method stub
		return this.pessoaPeritoManager;
	}
	
	@Override
	public EntityDataModel<PessoaPerito> getModel() {
		return this.model;
	}
	
	public Especialidade getEspecialidade() {
		return especialidade;
	}
	
	public void setEspecialidade(Especialidade especialidade) {
		this.especialidade = especialidade;
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

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	
}
