/**
 * pje-web
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.OrgaoJulgadorColegiadoHome;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.SucessaoOJsColegiadoManager;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.SucessaoOJsColegiado;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

/**
 * @author Everton Nogueira
 * Componente de controle da página de sucessão de órgãos julgadores.
 */
@Name(SucessaoOJsColegiadoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class SucessaoOJsColegiadoAction extends BaseAction<SucessaoOJsColegiado>{
	private static final long serialVersionUID = -8578376101497955888L;

	public static final String NAME = "sucessaoOJsColegiadoAction";
	
	@In
	private SucessaoOJsColegiadoManager sucessaoOJsColegiadoManager;
	
	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;
	
	private EntityDataModel<SucessaoOJsColegiado> listSucessao;
	private List<OrgaoJulgador> listOrgaoJulgador;
	private boolean alterar;
	
	@Create
	public void init(){
		super.newInstance();
		atualizarPesquisa();
		setAlterar(Boolean.FALSE);
	}
	
	@Override
	public void persist() {
		if(!getInstance().getOrgaoJulgadorSucedido().equals(getInstance().getOrgaoJulgadorSucessor())){
			getInstance().setOrgaoJulgadorColegiado(OrgaoJulgadorColegiadoHome.instance().getInstance());
			super.persistAndFlush();
			atualizarPesquisa();
			super.newInstance();
		}else{
			facesMessages.add(Severity.ERROR, "Os Órgãos Julgadores não podem ser iguais.");
		}
	}
	
	public void remove(SucessaoOJsColegiado sucessao){
		setInstance(sucessao);
		super.removeAndFlush();
		atualizarPesquisa();
		super.newInstance();
		setAlterar(Boolean.FALSE);
	}
	
	public void initUpdate(SucessaoOJsColegiado sucessao){
		setInstance(sucessao);
		setAlterar(Boolean.TRUE);
	}
	
	public void update(){
		super.mergeAndFlush();
		atualizarPesquisa();
		super.newInstance();
		setAlterar(Boolean.FALSE);
	}
	
	@Override
	protected BaseManager<SucessaoOJsColegiado> getManager() {
		return sucessaoOJsColegiadoManager;
	}

	@Override
	public EntityDataModel<SucessaoOJsColegiado> getModel() {
		if(listSucessao == null){
			atualizarPesquisa();
		}
		return listSucessao;
	}
	
	private void atualizarPesquisa() {
		DataRetriever<SucessaoOJsColegiado> dataRetriever = new SucessaoOJsColegiadoRetriever(sucessaoOJsColegiadoManager, this.facesMessages);
		listSucessao = new EntityDataModel<SucessaoOJsColegiado>(SucessaoOJsColegiado.class, this.facesContext, dataRetriever);		
	}
	
	/**
	 * Classe privada para pesquisa de Sucessores de OJs do Colegiado
	 */
	private class SucessaoOJsColegiadoRetriever implements DataRetriever<SucessaoOJsColegiado> {
		
		private SucessaoOJsColegiadoManager sucessaoOJsColegiadoManager;
		private FacesMessages facesMessages;
		
		public SucessaoOJsColegiadoRetriever(SucessaoOJsColegiadoManager sucessaoOJsColegiadoManager, FacesMessages facesMessages) {
			this.sucessaoOJsColegiadoManager = sucessaoOJsColegiadoManager;
			this.facesMessages = facesMessages;
		}

		@Override
		public Object getId(SucessaoOJsColegiado obj) {
			return obj.getIdSucessaoOJsColegiado();
		}

		@Override
		public SucessaoOJsColegiado findById(Object id) throws Exception {
			return sucessaoOJsColegiadoManager.findById(id);
		}

		@Override
		public List<SucessaoOJsColegiado> list(Search search) {
			atualizarDadosPesquisa(search);			
			return sucessaoOJsColegiadoManager.list(search);
		}

		@Override
		public long count(Search search) {
			atualizarDadosPesquisa(search);
			return sucessaoOJsColegiadoManager.count(search);
		}
		
		private void atualizarDadosPesquisa(Search search) {			
			try {
				search.addCriteria(Criteria.equals("orgaoJulgadorColegiado", OrgaoJulgadorColegiadoHome.instance().getInstance()));
				search.addOrder("dataSucessao", Order.ASC);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar a lista de sucessões do colegiado.");
				e.printStackTrace();
			}						
		}				
	}
	
	public List<OrgaoJulgador> getListOrgaoJulgador() {
		if(listOrgaoJulgador == null){
			listOrgaoJulgador = orgaoJulgadorManager.findAll();
		}
		return listOrgaoJulgador;
	}

	public void setListOrgaoJulgador(List<OrgaoJulgador> listOrgaoJulgador) {
		this.listOrgaoJulgador = listOrgaoJulgador;
	}

	public boolean isAlterar() {
		return alterar;
	}

	public void setAlterar(boolean alterar) {
		this.alterar = alterar;
	}
}
