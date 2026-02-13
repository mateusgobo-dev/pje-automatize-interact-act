package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.bean.ConsultaProcessoTrfImpresso;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfImpresso;

@Scope(ScopeType.CONVERSATION)
@Name("consultaProcessoTrfImpressoHome")
@BypassInterceptors
public class ConsultaProcessoTrfImpressoHome implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8151960828432323816L;
	private ConsultaProcessoTrfImpresso instance = new ConsultaProcessoTrfImpresso();
	private ProcessoTrf processoTrf;

	public ConsultaProcessoTrfImpresso getInstance() {
		return instance;
	}

	public void setInstance(ConsultaProcessoTrfImpresso instance) {
		this.instance = instance;
	}

	private Integer idPesquisa;

	public Integer getIdPesquisa() {
		return idPesquisa;
	}

	public void setIdPesquisa(Integer idFluxoPesquisa) {
		this.idPesquisa = idFluxoPesquisa;
	}

	public void setIdProcessoTrf(Integer idProcessoTrf) {
		this.processoTrf = getEntityManager().find(ProcessoTrf.class, idProcessoTrf);
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public EntityManager getEntityManager() {
		return EntityUtil.getEntityManager();
	}

	public boolean isEditable() {
		return true;
	}

	public void limparTela() {
		instance = new ConsultaProcessoTrfImpresso();
	}

	/**
	 * Retorna os resultados do grid
	 * 
	 * @return lista de processos
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoTrfImpresso> getResultList() {
		GridQuery grid = (GridQuery) Component.getInstance("processoTrfInicialImpressoGrid");
		List<ProcessoTrfImpresso> list = grid.getResultList();
		/*
		 * if (list.size() == 1) { ProcessoTrf pTrf = list.get(0);
		 * Redirect.instance().setViewId("/Processo/Consulta/list.xhtml");
		 * Redirect.instance().setParameter("id", pTrf.getIdProcessoTrf());
		 * Redirect.instance().execute(); return null; }
		 */
		return list;
	}

	public String getHomeName() {
		return "consultaProcessoTrfImpressoHome";
	}

}