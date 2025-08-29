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
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Strings;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.PessoaProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradorManager;
import br.jus.cnj.pje.nucleo.manager.ProcuradoriaManager;
import br.jus.cnj.pje.nucleo.manager.UsuarioManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * Classe de controle da tela 
 * de pesquisa de {@link PessoaProcurador}
 * @author Rodrigo Santos Menezes
 *
 */
@Name(ConsultaProcuradorAction.NAME)
@Scope(ScopeType.PAGE)
public class ConsultaProcuradorAction extends BaseAction<PessoaProcurador>{
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "consultaProcuradorAction";
	
	private String nome;
	private String cpf;
	private Boolean ativo = Boolean.TRUE;
	private Procuradoria procuradoria;
	
	private EntityDataModel<PessoaProcurador> model;
	
	@RequestParameter(value="procuradorGridCount")
	private Integer procuradorGridCount;
	
	@In
	private ProcuradorManager procuradorManager;
	
	@In
	private PessoaService pessoaService;
	
	@In
	private ProcuradoriaManager procuradoriaManager;
	
	@In
	private UsuarioManager usuarioManager;
	
	@In
	private Identity identity;
	
	@In
	private PessoaProcuradorManager pessoaProcuradorManager;
	
	@Create
	public void init(){
		pesquisar();
	}
	
	public void pesquisar(){
		
		try{
			model = new EntityDataModel<PessoaProcurador>(PessoaProcurador.class, super.facesContext, getRetriever());
			
			List<Criteria> criterios = new ArrayList<Criteria>(0);
			criterios.addAll(getCriteriosTelaPesquisa());
			
			model.setCriterias(criterios);
			model.addOrder("o.nome", Order.ASC);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}	

	public List<Criteria> getCriteriosTelaPesquisa(){
		
		List<Criteria> criterios = new ArrayList<Criteria>(0);	
		
		//OficialJusticaAtivo
		if(getAtivo() != null){
			if(getAtivo() == Boolean.TRUE){
				criterios.add(Criteria.bitwiseAnd("pessoa.especializacoes", PessoaFisica.PRO, PessoaFisica.PRO));
			}else{
				criterios.add(Criteria.not(Criteria.bitwiseAnd("pessoa.especializacoes", PessoaFisica.PRO, PessoaFisica.PRO)));
			}
		}
		
		//Nome
		if(!Strings.isEmpty(nome)){
			criterios.add(Criteria.contains("nome", nome));
		}

		//CPF
		if(!Strings.isEmpty(cpf)){
			criterios.add(Criteria.equals("pessoaDocumentoIdentificacaoList.tipoDocumento.codTipo","CPF"));
			criterios.add(Criteria.startsWith("pessoaDocumentoIdentificacaoList.numeroDocumento", cpf));
		}
		
		//procuradoria
		if(getProcuradoria() != null){
			List<PessoaProcurador> listaPessoaProcurador = pessoaProcuradorManager.getPessoaProcuradores(procuradoria);
			Object[] ids = new Object[listaPessoaProcurador.size()];
			for(int i=0; i<listaPessoaProcurador.size(); i++){
				ids[i] = listaPessoaProcurador.get(i).getIdUsuario();
			}	
			criterios.add(Criteria.in("idUsuario", ids));
		}
		
		//Localizacao
		if(!identity.hasRole("administrador") && !identity.hasRole("admin") && !identity.hasRole(Papeis.ADMINISTRADOR) && 
				!identity.hasRole(Papeis.PJE_ADMINISTRADOR_PROCURADORIA)){
			
			Localizacao loc = Authenticator.getLocalizacaoAtual();
			if(loc != null){
				List<Integer> usuarios = usuarioManager.getIdsUsuariosLocalizacao(loc);
				if(usuarios != null){
					criterios.add(Criteria.in("idUsuario", usuarios.toArray(new Integer[usuarios.size()])));						
				}
			}			
		}
		
		criterios.add(Criteria.equals("pessoa.unificada", Boolean.FALSE));
		
		return criterios;
	}
	
	@Override
	protected DataRetriever<PessoaProcurador> getRetriever() {
		
		final ProcuradorManager manager = (ProcuradorManager)getManager();
		final Integer tableCount = procuradorGridCount;
		DataRetriever<PessoaProcurador> retriever = new DataRetriever<PessoaProcurador>() {
			
			@Override
			public PessoaProcurador findById(Object id) throws Exception {
				try {
					return manager.findById(id);
				} catch (PJeBusinessException e) {
					throw new Exception(e);
				}
			}
			
			@Override
			public List<PessoaProcurador> list(Search search) {
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
			public Object getId(PessoaProcurador obj){
				return manager.getId(obj);
			}
		};
		return retriever;
	}		
	
	public void limparCampos(){
		this.ativo = null;
		this.cpf = null;
		this.procuradoria = null;
		this.nome = null;
	}
	
	/**
	 * Este método inativa o procurador informado
	 * @param instance O {@link PessoaProcurador} informado
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String inativa(PessoaProcurador instance) {		
		try {
			PessoaFisica pessoaFisica = (PessoaFisica) pessoaService.desespecializa(instance.getPessoa(), PessoaProcurador.class);
			instance = pessoaFisica.getPessoaProcurador();
			instance.setProcuradorAtivo(Boolean.FALSE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaProcurador.perfil.inativado");
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
	 * Este método reativa o procurador.
	 * @param instance {@link PessoaProcurador}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String ativa(PessoaProcurador instance){
		try {
			PessoaFisica pessoaFisica = (PessoaFisica)pessoaService.especializa(instance.getPessoa(), PessoaProcurador.class);
			instance = pessoaFisica.getPessoaProcurador();
			instance.setProcuradorAtivo(Boolean.TRUE);
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.INFO, "pessoaProcurador.perfil.ativado");
		} catch (PJeBusinessException e) {
			FacesMessages.instance().clear();
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "perfil.erro");
			e.printStackTrace();
		}
		pesquisar();
		return "update";
	}	
	
	public List<Procuradoria> getProcuradorias(){

		if(!Authenticator.isPapelAdministrador() && 
				!identity.hasRole(Papeis.PJE_ADMINISTRADOR_PROCURADORIA)){
			
			try {
				Pessoa p = Authenticator.getPessoaLogada();
				if(p instanceof PessoaFisica){
					PessoaFisica pf = (PessoaFisica)p;
					if(pf.getPessoaProcurador() != null){
						List <Procuradoria> list = new ArrayList<Procuradoria>();
						Procuradoria procuradoriaAtual = Authenticator.getProcuradoriaAtualUsuarioLogado();
						if(procuradoriaAtual != null) {
							list.add(procuradoriaAtual);
						}else {
							return null;
						}
					}	
				}
			} catch (ClassCastException c) {
				c.printStackTrace();
				return null;
			}
		}else{
			return procuradoriaManager.getlistProcuradorias();
		}
		return null;
	}	
	
	@Override
	protected BaseManager<PessoaProcurador> getManager() {
		return procuradorManager;
	}
	
	@Override
	public EntityDataModel<PessoaProcurador> getModel() {
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

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Procuradoria getProcuradoria() {
		return procuradoria;
	}

	public void setProcuradoria(Procuradoria procuradoria) {
		this.procuradoria = procuradoria;
	}
	
	

}
