package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.bean.ConsultaProcessoTrfDocumentoImpresso;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrfImpresso;

@Scope(ScopeType.CONVERSATION)
@Name("consultaProcessoTrfDocumentoImpressoHome")
@BypassInterceptors
public class ConsultaProcessoTrfDocumentoImpressoHome implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2430170019370084847L;
	private ConsultaProcessoTrfDocumentoImpresso instance = new ConsultaProcessoTrfDocumentoImpresso();
	private ProcessoTrf processoTrf;

	public ConsultaProcessoTrfDocumentoImpresso getInstance() {
		return instance;
	}

	public void setInstance(ConsultaProcessoTrfDocumentoImpresso instance) {
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
		instance = new ConsultaProcessoTrfDocumentoImpresso();
	}

	/**
	 * Retorna os resultados do grid
	 * 
	 * @return lista de processos
	 */
	@SuppressWarnings("unchecked")
	public List<ProcessoTrfImpresso> getResultList() {
		GridQuery grid = (GridQuery) Component.getInstance("processoTrfIncidentalDocumentoImpressoGrid");
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
		return "consultaProcessoTrfDocumentoImpressoHome";
	}

}