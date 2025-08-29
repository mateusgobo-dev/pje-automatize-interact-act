package br.com.infox.cliente.entity.search;

import java.util.Calendar;
import java.util.Date;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("relatorioDistribuicaoSearch")
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class RelatorioDistribuicaoSearch extends ProcessoTrf {

	private static final long serialVersionUID = 1L;
	private Date dataInicio;
	private Date dataFim;
	private String classeJudicialPes;
	private String orgaoJulgadorPes;

	public String getClasseJudicialPes() {
		return classeJudicialPes;
	}

	public void setClasseJudicialPes(String classeJudicialPes) {
		this.classeJudicialPes = classeJudicialPes;
	}

	public String getOrgaoJulgadorPes() {
		return orgaoJulgadorPes;
	}

	public void setOrgaoJulgadorPes(String orgaoJulgadorPes) {
		this.orgaoJulgadorPes = orgaoJulgadorPes;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public void verificaIntervalo() {
		if (dataInicio == null || dataFim == null) {
			return;
		}
		Calendar di = Calendar.getInstance();
		Calendar df = Calendar.getInstance();
		di.setTime(dataInicio);
		df.setTime(dataFim);
		di.add(Calendar.DAY_OF_MONTH, 9);
		if (di.compareTo(df) < 0) {
			dataFim = di.getTime();
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Data superior a 10 dias da data incial");
		}
	}

}