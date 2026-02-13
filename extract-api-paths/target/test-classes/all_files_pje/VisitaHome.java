/* $Id: VisitaHome.java 14029 2010-11-16 12:44:42Z laercio $ */

package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Diligencia;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.ProcessoExpedienteCentralMandado;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpedienteVisita;
import br.jus.pje.nucleo.entidades.Visita;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("visitaHome")
@BypassInterceptors
public class VisitaHome extends AbstractVisitaHome<Visita> {

	private static final long serialVersionUID = 1L;
	private List<ProcessoParteExpediente> processoParteExpedienteList = new ArrayList<ProcessoParteExpediente>(0);
	// private Boolean possuiDiligencia = Boolean.FALSE;
	private Diligencia diligencia;

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
	}

	@Override
	public void newInstance() {
		// DiligenciaHome.instance().setInstance(p)
		setProcessoParteExpedienteList(new ArrayList<ProcessoParteExpediente>(0));
		// setPossuiDiligencia(Boolean.FALSE);
		super.newInstance();
	}

	// Era utilizado quando era criada uma diligencia pra cada visita cadastrada
	// @Override
	// public String persist() {
	// String ret = null;
	// if (!possuiDiligencia){
	// DiligenciaHome.instance().newInstance();
	// Diligencia diligencia = DiligenciaHome.instance().getInstance();
	// diligencia.setProcessoExpedienteCentralMandado(ProcessoExpedienteCentralMandadoHome.instance().getInstance());
	// getEntityManager().persist(diligencia);
	// getInstance().setDiligencia(diligencia);
	// DiligenciaHome.instance().setInstance(diligencia);
	// }
	// ret = super.persist();
	// return ret;
	// }

	@Override
	public String remove(Visita obj) {
		removerProceessoParteExpedienteVisita(obj);
		return super.remove(obj);
	}

	@SuppressWarnings("unchecked")
	private void removerProceessoParteExpedienteVisita(Visita obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoParteExpedienteVisita o where ");
		sb.append("o.visita = :visita");
		Query q = getEntityManager().createQuery(sb.toString()).setParameter("visita", obj);
		List<ProcessoParteExpedienteVisita> list = q.getResultList();
		for (ProcessoParteExpedienteVisita pv : list) {
			getEntityManager().remove(pv);
		}
		EntityUtil.flush();

	}

	@Override
	public String update() {
		String ret = null;
		try {
			Visita visita = getInstance();
			
			/*
			 * [PJEII-3324] PJE-JT: Sérgio Ricardo : PJE-1.4.5 
			 * Adição de verificação da data da visita, impedindo valores futuros 
			 */			
			if (!visita.getDtVisita().after(new Date())) {
				getEntityManager().merge(getInstance());
				getEntityManager().flush();
				ret = getUpdatedMessage().getValue().toString();
				FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro alterado com sucesso");				
			} else {
				FacesMessages.instance().add(Severity.ERROR, "A data/hora da visita não pode ser superior à data/hora atual .");
			}
			
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof ConstraintViolationException) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
			}
		}
		return ret;
	}

	public static VisitaHome instance() {
		return ComponentUtil.getComponent("visitaHome");
	}

	public void inserirNomeDiligenciado(ProcessoParteExpediente obj) {
		if (obj.getCheck()) {
			getProcessoParteExpedienteList().add(obj);
		} else {
			getProcessoParteExpedienteList().remove(obj);
		}
	}

	public int getSizeNomeDiligenciadoGrid(){
		GridQuery grid = (GridQuery) Component.getInstance("nomeDiligenciadoGrid");
		int size = grid.getResultList().size();
		return size;
	}
	
	public void alteraValorCheck(ProcessoParteExpediente obj){
		if(getSizeNomeDiligenciadoGrid() == 1 && obj.getCheck() == null){
			obj.setCheck(true);
			inserirNomeDiligenciado(obj);
		}
	}	
	
	public void inserir() {
		if (getProcessoParteExpedienteList().size() > 0) {
			Context session = Contexts.getSessionContext();
			Pessoa pessoa = (Pessoa) session.get("usuarioLogado");
			PessoaOficialJustica pessoaOficialJustica = ((PessoaFisica) pessoa).getPessoaOficialJustica();
			getInstance().setPessoaOficialJustica(pessoaOficialJustica);
			List<ProcessoParteExpediente> lista = getProcessoParteExpedienteList();
			Visita visita = getInstance();
			
			/*
			 * [PJEII-3324] PJE-JT: Sérgio Ricardo : PJE-1.4.5 
			 * Adição de verificação da data da visita, impedindo valores futuros 
			 */
			if (!visita.getDtVisita().after(new Date())) {
				persist();
				for (ProcessoParteExpediente processoParteExpediente : lista) {
					ProcessoParteExpedienteVisita parteExpedienteVisita = new ProcessoParteExpedienteVisita();
					parteExpedienteVisita.setProcessoParteExpediente(processoParteExpediente);
					parteExpedienteVisita.setVisita(visita);
					getEntityManager().persist(parteExpedienteVisita);
					getEntityManager().flush();
					getEntityManager().clear();
					processoParteExpediente.setCheck(Boolean.FALSE);
				}
				newInstance();
				refreshGrid("visitaGrid");				
			} else {
				FacesMessages.instance().add(Severity.ERROR, "A data/hora da visita não pode ser superior à data/hora atual .");				
			}
			
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Escolha uma parte.");
		}
	}

	// public Boolean verificarDiligencias(){
	// Boolean resultado = Boolean.FALSE;
	//
	// List<Visita> visitaList = new ArrayList<Visita>(0);
	// visitaList = pegarListaVisita();
	//
	// for (Visita visita : visitaList) {
	// if (visita.getDiligencia() != null){
	// diligencia = visita.getDiligencia();
	// resultado = Boolean.TRUE;
	// }
	// }
	// return resultado;
	// }

	@SuppressWarnings("unchecked")
	public List<Visita> pegarListaVisita() {
		ProcessoExpedienteCentralMandado processoExpedienteCentralMandado = ProcessoExpedienteCentralMandadoHome
				.instance().getInstance();

		EntityManager em = EntityUtil.getEntityManager();
		StringBuilder sb = new StringBuilder();
		sb.append("select v from Visita v ");
		sb.append("inner join v.processoParteExpedienteVisitaList list ");
		sb.append("inner join list.processoParteExpediente ppe ");
		sb.append("where ppe in (select list from ProcessoExpedienteCentralMandado m ");
		sb.append("				   inner join m.processoExpediente pe ");
		sb.append("				   inner join pe.processoParteExpedienteList list ");
		sb.append("					where m = :processoExpedienteCentralMandado))");
		Query q = em.createQuery(sb.toString());
		q.setParameter("processoExpedienteCentralMandado", processoExpedienteCentralMandado);
		return q.getResultList();
	}

	// public void setPossuiDiligencia(Boolean possuiDiligencia) {
	// this.possuiDiligencia = possuiDiligencia;
	// }
	//
	//
	// public Boolean getPossuiDiligencia() {
	// return verificarDiligencias();
	// }

	public void setDiligencia(Diligencia diligencia) {
		this.diligencia = diligencia;
	}

	public Diligencia getDiligencia() {
		return diligencia;
	}

	public void setProcessoParteExpedienteList(List<ProcessoParteExpediente> processoParteExpedienteList) {
		this.processoParteExpedienteList = processoParteExpedienteList;
	}

	public List<ProcessoParteExpediente> getProcessoParteExpedienteList() {
		return processoParteExpedienteList;
	}
	
	/*
	 * PJE-JT: Ricardo Scholz : PJEII-3320 - 2012-10-11 Alteracoes feitas pela JT.
	 * Merge das alterações realizadas na issue PJE-1566 (Jira CSJT)
	 */
	public Authenticator getAuthenticator(){
		return ComponentUtil.getComponent("authenticator");
	}
	
	public Papel getPapelAtual(){
		return Authenticator.getPapelAtual();
	}
	/*
	 * PJE-JT: Fim.
	 */
}