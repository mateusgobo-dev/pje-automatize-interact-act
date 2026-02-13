package br.com.infox.pje.action;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.ProcessoMagistradoManager;
import br.jus.cnj.pje.view.BaseAction;
import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.ProcessoMagistrado;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Order;
import br.jus.pje.search.Search;

@Scope(ScopeType.PAGE)
@Name("processoMagistradoAction")
public class ProcessoMagistradoAction extends BaseAction<ProcessoMagistrado> implements Serializable {

	private static final long serialVersionUID = -9141122469857022400L;

	@In(create = true)
	private ProcessoMagistradoManager processoMagistradoManager;
	
	private EntityDataModel<ProcessoMagistrado> magistradosDoProcesso;
	
	@In
	private ParametroUtil parametroUtil;
	
	@RequestParameter(value="idProcessoMagistrado")
	private Integer idProcessoMagistrado; 
	
	@Override
	protected ProcessoMagistradoManager getManager() {
		return processoMagistradoManager;
	}

	@Create
	public void init() {
		atualizarPesquisa();
	}
	
	public void atualizarPesquisa() {
		magistradosDoProcesso = new EntityDataModel<ProcessoMagistrado>(ProcessoMagistrado.class, this.facesContext, new ProcessoMagistradoRetriever(getManager(), this.facesMessages));	
	}
	
	@Override
	public EntityDataModel<ProcessoMagistrado> getModel() {
		return magistradosDoProcesso;
	}
	
	public Boolean exibirAba() {
		if(Authenticator.hasRole(Papeis.VISUALIZA_MAGISTRADOS_ASSOCIADOS_PROCESSO) && 
				(!parametroUtil.isPrimeiroGrau())){
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Classe privada para pesquisa de magistrados do processo
	 */
	private class ProcessoMagistradoRetriever implements DataRetriever<ProcessoMagistrado> {
		
		private ProcessoMagistradoManager processoMagistradoManager;
		private FacesMessages facesMessages;
		
		public ProcessoMagistradoRetriever(ProcessoMagistradoManager processoMagistradoManager, FacesMessages facesMessages) {
			this.processoMagistradoManager = processoMagistradoManager;
			this.facesMessages = facesMessages;
		}

		@Override
		public Object getId(ProcessoMagistrado obj) {
			return obj.getIdProcessoMagistrado();
		}

		@Override
		public ProcessoMagistrado findById(Object id) throws Exception {
			return processoMagistradoManager.findById(id);
		}

		@Override
		public List<ProcessoMagistrado> list(Search search) {
			atualizarDadosPesquisa(search);			
			return processoMagistradoManager.list(search);
		}

		@Override
		public long count(Search search) {
			atualizarDadosPesquisa(search);
			return processoMagistradoManager.count(search);
		}
		
		private void atualizarDadosPesquisa(Search search) {			
			try {
				search.addCriteria(Criteria.equals("processo", ProcessoTrfHome.instance().getInstance()));
				search.addCriteria(Criteria.equals("ativo", true));
				search.addOrder("dataVinculacao", Order.DESC);
			} catch (NoSuchFieldException e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar a lista de magistrados do processo.");
				e.printStackTrace();
			}						
		}				
	}

}
