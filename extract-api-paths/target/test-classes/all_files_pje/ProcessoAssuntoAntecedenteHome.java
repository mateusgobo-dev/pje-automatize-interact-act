package br.com.infox.cliente.home;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ProcessoAssunto;
import br.jus.pje.nucleo.entidades.ProcessoAssuntoAntecedente;

@Name("processoAssuntoAntecedenteHome")
@BypassInterceptors
public class ProcessoAssuntoAntecedenteHome extends
		AbstractHome<ProcessoAssuntoAntecedente> {
	
	private Integer  rowCount;
	
	private static final int MAX_RESULTS = 10;
	
	public static final String COD_AGRUPAMENTO_CRIME_ANTECEDENTE = "ACA";

	private static final long serialVersionUID = 1L;

	private ProcessoAssunto processoAssuntoSelecionado = new ProcessoAssunto();
	
	public ProcessoAssunto getProcessoAssuntoSelecionado() {
		return processoAssuntoSelecionado;
	}

	private List<AssuntoTrf> listaRight = new ArrayList<AssuntoTrf>(0);
	private List<AssuntoTrf> listaLeft = new ArrayList<AssuntoTrf>(0);
	
	private int page = 1;
	private long count;
	
	 private Boolean existeAgrupadorAssuntoAntecedente = Boolean.TRUE;
  	
  	 public Boolean getExisteAgrupadorAssuntoAntecedente() {
  		 return existeAgrupadorAssuntoAntecedente;
  	 }
  	
  	 public void setExisteAgrupadorAssuntoAntecedente(Boolean existeAgrupadorAssuntoAntecedente) {
  		 this.existeAgrupadorAssuntoAntecedente = existeAgrupadorAssuntoAntecedente;
  	 }
  	 
	
	public int getPage() {
		return page;
	}
	
	public void setPage(int page) {
		this.page = page;
	}
	
	public int getRowCount(){
		return rowCount.intValue();
	}


	public long getCount() {
		return count; 
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	
	public int getPageCount(){
		if(count > 0){
			MathContext mc = new MathContext(1, RoundingMode.UP);
			return new BigDecimal(new Double(count)/new Double(MAX_RESULTS)).round(mc).intValue();
		}
		else{
			return 0;
		}
	}
	

	@SuppressWarnings("unchecked")
	private List<AssuntoTrf> getAssuntoTrfList() {
		StringBuilder sqlCount = new StringBuilder();
		
		sqlCount.append(" select count(o.assunto) from AssuntoAgrupamento o where o.agrupamento.codAgrupamento = 'ACA'");
		sqlCount.append(" AND o.assunto.idAssuntoTrf not in (select ant.assuntoTrf.idAssuntoTrf from ProcessoAssuntoAntecedente ant where ant.processoAssunto.idProcessoAssunto = ");
		sqlCount.append(getProcessoAssuntoSelecionado().getIdProcessoAssunto());
		sqlCount.append(")");
		sqlCount.append(" AND o.assunto.idAssuntoTrf != ");
		sqlCount.append(getProcessoAssuntoSelecionado().getAssuntoTrf()
				.getIdAssuntoTrf());
		sqlCount.append(")");
		
		setCount(((Long)getEntityManager().createQuery(sqlCount.toString()).getSingleResult()));	
		
		StringBuilder sql = new StringBuilder();
		sql.append(" select o.assunto from AssuntoAgrupamento o where o.agrupamento.codAgrupamento = 'ACA'");
		sql.append(" AND o.assunto.idAssuntoTrf not in (select ant.assuntoTrf.idAssuntoTrf from ProcessoAssuntoAntecedente ant where ant.processoAssunto.idProcessoAssunto = ");
		sql.append(getProcessoAssuntoSelecionado().getIdProcessoAssunto());
		sql.append(")");
		sql.append(" AND o.assunto.idAssuntoTrf != ");
		sql.append(getProcessoAssuntoSelecionado().getAssuntoTrf()
				.getIdAssuntoTrf());
		sql.append(")");
		sql.append(" ORDER BY o.assunto");
		Query query = getEntityManager().createQuery(sql.toString());
		
		query.setFirstResult((page - 1) * MAX_RESULTS);
		query.setMaxResults(MAX_RESULTS);
		
		List<AssuntoTrf> result = query.getResultList();
		if(!listaLeft.isEmpty()){
			result.removeAll(listaLeft);
		}
		return result;
	}
   
	public List<AssuntoTrf> getListaRight() {
		return listaRight;
	}
	
	public List<AssuntoTrf> getListaLeft() {
		return listaLeft;
	}
	
	public void popularListaRight(){
		if (isProcessoAssuntoSelecionadoPreenchido()) {
				listaRight.clear();
				listaRight.addAll(getAssuntoTrfList());
		}
	}
	
	private void popularListaLeft(){
		if (isProcessoAssuntoSelecionadoPreenchido()) {
			listaLeft.clear();
			for (ProcessoAssuntoAntecedente processoAssuntoAntecedente : processoAssuntoSelecionado
					.getProcessoAssuntoAntecedenteList()) {
				listaLeft.add(processoAssuntoAntecedente.getAssuntoTrf());
			}
		}
	}
	
	private boolean isProcessoAssuntoSelecionadoPreenchido() {
		return processoAssuntoSelecionado != null
				&& processoAssuntoSelecionado.getAssuntoTrf() != null
				&& processoAssuntoSelecionado.getProcessoTrf() != null;

	}

	public void setarProcessoAssuntoSelecionado(ProcessoAssunto pa) {
		this.processoAssuntoSelecionado = pa;
		popularListaLeft();
		popularListaRight();
	}

	// Método que recupera a lista de processos antececentes
	public List<ProcessoAssuntoAntecedente> getProcessoAssuntoAntecedenteList() {
		return getInstance().getProcessoAssunto()
				.getProcessoAssuntoAntecedenteList();
	}

	public void addProcessoAssuntoAntecedente(AssuntoTrf assuntoTrf) {
		ProcessoAssuntoAntecedente processoAssuntoAntecedente = new ProcessoAssuntoAntecedente();
		processoAssuntoAntecedente
				.setProcessoAssunto(getProcessoAssuntoSelecionado());
		processoAssuntoAntecedente.setAssuntoTrf(assuntoTrf);
		if (getProcessoAssuntoSelecionado().getProcessoAssuntoAntecedenteList().isEmpty() || !getProcessoAssuntoSelecionado().getProcessoAssuntoAntecedenteList().contains(processoAssuntoAntecedente)) {
			getProcessoAssuntoSelecionado().getProcessoAssuntoAntecedenteList()
					.add(processoAssuntoAntecedente);
			getEntityManager().merge(getProcessoAssuntoSelecionado());
			getEntityManager().flush();
		}
		refreshGrid("processoAssuntoAntecedenteInferiorGrid");
		refreshGrid("processoAssuntoAntecedenteSuperiorGrid");
	}

	@Override
	public String remove(ProcessoAssuntoAntecedente obj) {
		if (!getProcessoAssuntoSelecionado().getProcessoAssuntoAntecedenteList().isEmpty() && getProcessoAssuntoSelecionado().getProcessoAssuntoAntecedenteList().contains(obj)) {
			getProcessoAssuntoSelecionado().getProcessoAssuntoAntecedenteList().remove(obj);
		}
		String retorno = super.remove(obj);
		refreshGrid("processoAssuntoAntecedenteSuperiorGrid");
		refreshGrid("processoAssuntoAntecedenteInferiorGrid");
		return retorno;
	}
	
	//Método que move a lista da tabela da esquerda para direita
	public void moverParaDireita(AssuntoTrf assuntoTrf) {
		listaRight.add(assuntoTrf);
		listaLeft.remove(assuntoTrf);
	}

	//Método que move a lista da tabela da direita para esquerda
	public void moverParaEsquerda(AssuntoTrf assuntoTrf) {
		listaLeft.add(assuntoTrf);
		listaRight.remove(assuntoTrf);
	}

	// Salva e fecha as alterações feitas no PopUp Assunto Antecedente
	public void salvar() {
		getProcessoAssuntoSelecionado().getProcessoAssuntoAntecedenteList().clear();
		for(AssuntoTrf assuntoSelecionado : getListaLeft()){
			ProcessoAssuntoAntecedente processoAssuntoAntecedente = new ProcessoAssuntoAntecedente();
			processoAssuntoAntecedente.setAssuntoTrf(assuntoSelecionado);
			processoAssuntoAntecedente.setProcessoAssunto(processoAssuntoSelecionado);
			getProcessoAssuntoSelecionado().getProcessoAssuntoAntecedenteList().add(processoAssuntoAntecedente);
		}
	}
	
	/**
  	 * Método responsável por verificar se existe um agrupador cadastrado para os assuntos antecedentes
  	 */
  	public void verificaAgrupador() {
  	
  		StringBuilder sb = new StringBuilder();
  		sb.append("SELECT o.agrupamento FROM AssuntoAgrupamento o ");
  		sb.append("WHERE o.agrupamento.codAgrupamento = " + "'" + COD_AGRUPAMENTO_CRIME_ANTECEDENTE + "'");
  	
  		if(this.getEntityManager().createQuery(sb.toString()).getResultList().isEmpty()){
  	
  			FacesMessages.instance().add(Severity.ERROR, "Não existe agrupador para os assuntos antecedentes, favor entrar em contato com o administrador do sistema.");
  	
  			this.setExisteAgrupadorAssuntoAntecedente(Boolean.FALSE);
  	
  		} else {
  	
  			this.setExisteAgrupadorAssuntoAntecedente(Boolean.TRUE);
  		}
  	 }

	// Fecha o PopUp Assunto Antecedente sem salvar as modificaçoes feitas
	public void fechar() {
		getListaLeft().clear();
		getListaRight().clear();
	}

	public void setListaRight(List<AssuntoTrf> listaRight) {
		this.listaRight = listaRight;
	}
	
	public void setListaLeft(List<AssuntoTrf> listaLeft) {
		this.listaLeft = listaLeft;
	}
	
}
