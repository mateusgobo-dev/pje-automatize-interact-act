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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.component.agenda.AgendaSessao;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.BaseManager;
import br.jus.cnj.pje.nucleo.manager.SessaoJudicialManager;
import br.jus.cnj.pje.view.EntityDataModel.DataRetriever;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Compoenente de controle da modal de relação de julgamento em painelSecretarioSessaoJF.xhtml
 * @author Rodrigo Santos Menezes
 *
 */

@Name(ModalSessaoRelacaoJulgamentoAction.NAME)
@Scope(ScopeType.PAGE)
public class ModalSessaoRelacaoJulgamentoAction extends BaseAction<Sessao>{

	private static final long serialVersionUID = -173377996938242740L;

	public static final String NAME = "modalSessaoRelacaoJulgamentoAction";
	
	private EntityDataModel<Sessao> model;
	
	private Date dataSelcionada;
	
	private AgendaSessao agenda;
	
	@In
	private SessaoJudicialManager sessaoJudicialManager;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private FacesContext facesContext;
	
	@Create
	public void init(){
		agenda = ComponentUtil.getComponent("agendaSessao");
		pesquisar();
	}
	
	public void pesquisar(){
		dataSelcionada = agenda.getCurrentDate();
		DataRetriever<Sessao> sessaoRet = new ModalSessaoRelacaoJulgamentoRetriever(sessaoJudicialManager, facesMessages);
		model = new EntityDataModel<Sessao>(Sessao.class, facesContext, sessaoRet);
		
		try {
			model.setCriterias(criteriosPesquisa());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		model.setDistinct(true);
	}
	
	private List<Criteria> criteriosPesquisa(){
		Criteria existente = Criteria.isNull("dataExclusao");
		Criteria sessaDoDia = Criteria.equals("dataSessao", dataSelcionada);
		Criteria sessaoContinua = Criteria.and(
											Criteria.not(Criteria.isNull("dataFimSessao")),
											Criteria.greaterOrEquals("dataFimSessao", dataSelcionada),
											Criteria.lessOrEquals("dataSessao", dataSelcionada));
		Criteria orgaoJulgadorColegiado = Criteria.equals("orgaoJulgadorColegiado", Authenticator.getOrgaoJulgadorColegiadoAtual());	
		
		List<Criteria> criterios = new ArrayList<Criteria>();
		criterios.add(existente);
		criterios.add(orgaoJulgadorColegiado);
		criterios.add(Criteria.or(sessaDoDia, sessaoContinua));
		
		return criterios;
	}
	
	private class ModalSessaoRelacaoJulgamentoRetriever implements DataRetriever<Sessao>{
		
		private Long count;
		
		private FacesMessages facesMessages;
		
		private SessaoJudicialManager manager;
		
		
		public ModalSessaoRelacaoJulgamentoRetriever(SessaoJudicialManager manager,FacesMessages facesMessages){
			this.manager = manager;
			this.facesMessages = facesMessages;
		}

		@Override
		public Object getId(Sessao p) {
			return manager.getId(p);
		}

		@Override
		public Sessao findById(Object id) throws Exception {
			return manager.findById(id);
		}

		@Override
		public List<Sessao> list(Search search) {
			try {
				return manager.list(search);
			} catch (Exception nsfe){
				facesMessages.add(Severity.ERROR, "Erro ao recuperar as sessões do dia selecionado.");
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
					facesMessages.add(Severity.ERROR, "Erro ao tentar recuperar o número de sessões do dia.");
					return 0;
				}
			}
			return count;
		}
	};	

	@Override
	protected BaseManager<Sessao> getManager() {
		return this.sessaoJudicialManager;
	}

	@Override
	public EntityDataModel<Sessao> getModel() {
		// TODO Auto-generated method stub
		return model;
	}
	
	public Date getDataSelcionada() {
		return dataSelcionada;
	}
	
	public void setDataSelcionada(Date dataSelcionada) {
		this.dataSelcionada = dataSelcionada;
	}
	
	public AgendaSessao getAgenda() {
		return agenda;
	}
	
	public void setAgenda(AgendaSessao agenda) {
		this.agenda = agenda;
	}
}
