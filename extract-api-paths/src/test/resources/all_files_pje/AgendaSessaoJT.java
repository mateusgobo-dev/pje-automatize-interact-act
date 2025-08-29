package br.com.infox.component.agenda;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;
import javax.persistence.Query;

import org.jboss.seam.core.Expressions;
import org.richfaces.model.CalendarDataModel;
import org.richfaces.model.CalendarDataModelItem;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.pje.jt.entidades.SessaoJT;
import br.jus.pje.jt.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.util.StringUtil;

public class AgendaSessaoJT implements CalendarDataModel, Serializable{

	private static final long serialVersionUID = 1L;

	private static final String SELECTED_DAY = "wi-agenda-selected";
	
	private static final String HOLIDAY_STYLE_CLASS = "rich-calendar-holly";
	
	private transient List<AgendaItem> holidays = new ArrayList<AgendaItem>();
	
	private List<String> thisYearHolidays = new ArrayList<String>();

	private static final String DATE_PATTERN = "%1$td/%1$tm";
	
	private Map<String, Date> holidaysMap = new HashMap<String, Date>();
	
	private transient AgendaItem[] items;
	
	private Date[] datas;
	
	private Date currentDate;
	
	private DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
	private enum Holidays {CARNAVAL, SEXTA_SANTA, CORPUS_CHRISTI};
	
	public void limparSelecionado(){
		setCurrentDate(null);
	}
	
	public CalendarDataModelItem[] getData(Date[] dates) {
		items = null;
		if(items != null && items[0] != null && items[0].getDate() != null && 
								format.format(items[0].getDate()).equals(format.format(dates[0]))) {
			return items;
		}
		datas = dates;
		items = new AgendaItem[dates.length];		
		Calendar c = new GregorianCalendar();
		c.setTime(dates[0]);
		initYear(c.get(Calendar.YEAR));
		HashMap<String, List<SessaoJT>> sessaoAgendaMap = buildAgendaSessaoMap();
		for (int i = 0; i < dates.length; i++) {
			c.setTime(dates[i]);
			items[i] = getDayData(c, sessaoAgendaMap);
			items[i].setDate(dates[i]);
		}
		return items;
	}
	
	/**
	 * Retorna uma lista de sessões de acordo com a data, se está ativa e orgão julgador colegiado
	 * @param Calendar c
	 * @return List<Sessao>
	 */
	@SuppressWarnings("unchecked")
	private List<SessaoJT> listaSessoes(Calendar c){
		StringBuilder sql = new StringBuilder();
		sql.append("select o from SessaoJT o ");
	    sql.append("where cast(o.dataSessao as date) = cast(:dtSessao as date) ");
	    if(Authenticator.getOrgaoJulgadorAtual() != null){
	    	sql.append("and o in (select cs.sessao from ComposicaoSessao cs ");
	    	sql.append("		   where cs.orgaoJulgador = #{orgaoJulgadorAtual}) ");
		}
	    if(Authenticator.getOrgaoJulgadorColegiadoAtual() != null){
	    	sql.append("and o.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual} ");
	    }else{
	    	sql.append("and o.pessoaProcurador = #{usuarioLogado}");
	    }
		Query q = EntityUtil.createQuery(sql.toString());
		q.setParameter("dtSessao", c.getTime());
		
		return (List<SessaoJT>) q.getResultList();
	}
	
	private AgendaItem getDayData(Calendar c, HashMap<String, List<SessaoJT>> sessaoAgendaMap) {
		AgendaItem item = new AgendaItem();
		Map<String, Object> data = new HashMap<String, Object>();
		String date = String.format(DATE_PATTERN + "/%1tY", c.getTime());
		int i = thisYearHolidays.indexOf(date);
		if (i != -1) {
			item = holidays.get(i);
			if (item.getStyleClass() == null) {
				item.setStyleClass(HOLIDAY_STYLE_CLASS);
			}
		}
		item.setDay(c.get(Calendar.DAY_OF_MONTH));
		if(sessaoAgendaMap.containsKey(format.format(c.getTime()))) {
			boolean sessaoAndamento = false;
			for (SessaoJT sessaoAgenda: sessaoAgendaMap.get(format.format(c.getTime()))) {
				if (sessaoAgenda.getSituacaoSessao() != SituacaoSessaoEnum.E && sessaoAgenda.getSituacaoSessao() != SituacaoSessaoEnum.F) {
					sessaoAndamento = true;
					break;
				}
			}
				
			//criando lista de sessões de acordo com a data, se está ativa e orgão julgador colegiado
			List<SessaoJT> sessoes = (List<SessaoJT>) listaSessoes(c);
			
			//Mapeando os status das sessões
			Map<String, Integer> listaStatusQtd = new HashMap<String, Integer>();
			Integer count = null;
			for (SessaoJT sessao : sessoes) {
				count = listaStatusQtd.get(sessao.getSituacaoSessao().name());
				if(count == null){
					count = 0;
				}
				listaStatusQtd.put(sessao.getSituacaoSessao().name(), count++);
			}
			
			String status = StringUtil.concatList(new ArrayList<Object>(listaStatusQtd.keySet()), ", ");
			
			if(sessaoAndamento && sessoes.size() > 0) {
				Boolean flag = Boolean.TRUE;
				for (SessaoJT sessao : sessoes) {
					if(sessao.getSituacaoSessao() != SituacaoSessaoEnum.E && sessao.getSituacaoSessao() != SituacaoSessaoEnum.F){
						flag = Boolean.FALSE;
					}
				}
				if (sessoes.size() == 1) {
					data.put("tipoSessao", "Existe " + sessoes.size()+" sessão - " + status);
				}else if (sessoes.size() != 1 && flag) {
					data.put("tipoSessao", "Existem " + sessoes.size()+" sessões Finalizadas - " + status);
				}else{
					data.put("tipoSessao", "Existem " + sessoes.size()+" sessões - " + status);
				}
				
			}else if(sessoes.size() == 1){
				data.put("tipoSessao", "Sessão "+sessoes.get(0).getSituacaoSessao().getLabel()+".");
			}else{
				data.put("tipoSessao", "Existem " + sessoes.size()+" sessões - " + status);
			}
		} else {
			item.setEnabled(false);
		}
		item.setData(data);
		return item;
	}
	
	@SuppressWarnings("unchecked")
	private HashMap<String, List<SessaoJT>> buildAgendaSessaoMap() {
		HashMap<String, List<SessaoJT>> sessaoAgendaMap = new HashMap<String, List<SessaoJT>>();
		StringBuilder sb = new StringBuilder();
		sb.append("select s from SessaoJT s where ");
		if(Authenticator.getOrgaoJulgadorAtual() != null){
			sb.append("s in (select cs.sessao from ComposicaoSessao cs ");
			sb.append("			   where cs.orgaoJulgador = #{orgaoJulgadorAtual}) and ");
		}
		if(Authenticator.getOrgaoJulgadorColegiadoAtual() != null){
			sb.append("s.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual}");
		}else{
			sb.append("s.pessoaProcurador = #{usuarioLogado}");
		}
		Query hql = EntityUtil.getEntityManager().createQuery(sb.toString());
		List<SessaoJT> sessaoAgendaList = (List<SessaoJT>) hql.getResultList();
		for (SessaoJT sessaoAgenda: sessaoAgendaList) {
			String key = format.format(sessaoAgenda.getDataSessao());
			if (sessaoAgendaMap.containsKey(key)) {
				sessaoAgendaMap.get(key).add(sessaoAgenda);
			} else {
				List<SessaoJT> sessaoList = new ArrayList<SessaoJT>();
				sessaoList.add(sessaoAgenda);
				sessaoAgendaMap.put(key, sessaoList);
			}
		}
		return sessaoAgendaMap;
	}

	public Object getToolTip(Date date) {
		return null;
	}

	public List<AgendaItem> getHolidays() {
		return holidays;
	}
	
	public void setHolidays(List<String> holidays) {
		for (String s : holidays) {
			this.holidays.add(toObject(s));
		}
	}
	
	private AgendaItem toObject(String value) {		
		AgendaItem item = new AgendaItem();
		String[] fields = value.split(",");
		item.setDayMonth(fields[0]);
		String toolTip = fields[1];
		toolTip = (String) Expressions.instance().createValueExpression(toolTip).getValue();
		item.setToolTip(toolTip);
		if (fields.length > 3) {
			item.setStyleClass(fields[3]);
		}
		return item;
	}

	
	private void initYear(int year) {
		Calendar easter = Easter.findHolyDay(year);
		Date easterDay = easter.getTime();

		// Sexta feira santa
		easter.add(Calendar.DAY_OF_MONTH, -2);
		holidaysMap.put(Holidays.SEXTA_SANTA.name(), easter.getTime());

		// Carnaval
		easter.setTime(easterDay);
		easter.add(Calendar.DAY_OF_MONTH, -47);
		holidaysMap.put(Holidays.CARNAVAL.name(), easter.getTime());
		
		// Corpus Christi
		easter.setTime(easterDay);
		easter.add(Calendar.DAY_OF_MONTH, 60);
		holidaysMap.put(Holidays.CORPUS_CHRISTI.name(), easter.getTime());
		
		holidays.clear();
		
		for(CalendarioEvento ce : getFeriadosSimples(year)){
			if (ce.getDtMesFinal() == null && ce.getDtDiaFinal() == null) {
				if (ce.getOrgaoJulgador() == Authenticator.getOrgaoJulgadorAtual()) {
					insereFeriadoSimples(ce);
				}else if (ce.getOrgaoJulgador() == null) {
					insereFeriadoSimples(ce);
				} 
			}
			
			if (ce.getDtMes().equals(ce.getDtMesFinal())) {
				if (ce.getOrgaoJulgador() == Authenticator.getOrgaoJulgadorAtual()) {
					insereFeriadoPeriodicoMesmoMes(ce);
				}else if (ce.getOrgaoJulgador() == null) {
					insereFeriadoPeriodicoMesmoMes(ce);
 				}
 			}
			
			int mesInicial = ce.getDtMes() == null ? 0 : ce.getDtMes();
			int mesFinal = ce.getDtMesFinal() == null ? 0 : ce.getDtMesFinal();
			if (mesFinal != mesInicial) {
				if (ce.getOrgaoJulgador() == Authenticator.getOrgaoJulgadorAtual()) {
					insereFeriadoPeriodicoMesesDiferentes(ce, year);
				}else if (ce.getOrgaoJulgador() == null) {
					insereFeriadoPeriodicoMesesDiferentes(ce, year);
				}
			}
			
			thisYearHolidays.clear();
			for (AgendaItem dd : getHolidays()) {
				String date = "";
				if (holidaysMap.containsKey(dd.getDayMonth())) {
					Date d = holidaysMap.get(dd.getDayMonth());
					date = String.format(DATE_PATTERN, d);
				} else {
					date = dd.getDayMonth();		
				}	
				thisYearHolidays.add(date + "/" + year);
			}
		}	
	}
	
	private void insereFeriadoSimples(CalendarioEvento ce){
				int mesFinal = ce.getDtMesFinal() == null ? 0 : ce.getDtMesFinal();
				if (mesFinal == 0) {
					AgendaItem feriado = new AgendaItem();
					String day = ce.getDtDia() > 9 ? String.valueOf(ce.getDtDia()) : "0"+ce.getDtDia();
					String month = ce.getDtMes() > 9 ? String.valueOf(ce.getDtMes()) : "0"+ce.getDtMes();
					feriado.setDayMonth(day+"/"+month);
					feriado.setToolTip(ce.getDsEvento());
					holidays.add(feriado);
				}
			}
			
	private void insereFeriadoPeriodicoMesmoMes(CalendarioEvento ce){
		for (int i = ce.getDtDia(); i <= ce.getDtDiaFinal(); i++) {
			AgendaItem feriado = new AgendaItem();
			String day = (i) > 9 ? String.valueOf(i) : "0"+String.valueOf(i);
			String month = ce.getDtMesFinal() > 9 ? String.valueOf(ce.getDtMesFinal()) : "0"+ce.getDtMesFinal();
			feriado.setDayMonth(day+"/"+month);
			feriado.setToolTip(ce.getDsEvento());
			holidays.add(feriado);
		}
	}
			
	private void insereFeriadoPeriodicoMesesDiferentes(CalendarioEvento ce, int year){
		for(CalendarioEvento cePeriodo : getFeriadosSimples(year)){
			int mesFinal = cePeriodo.getDtMesFinal() == null ? 0 : cePeriodo.getDtMesFinal();
			
			//insere no 2o mes do periodo
			if (mesFinal != 0 && ce == cePeriodo) {
				for (int i = 0; i < cePeriodo.getDtDiaFinal(); i++) {
					AgendaItem feriado = new AgendaItem();
					String day = (i+1) > 9 ? String.valueOf(i+1) : "0"+String.valueOf(i+1);
					String month = cePeriodo.getDtMesFinal() > 9 ? String.valueOf(cePeriodo.getDtMesFinal()) : "0"+cePeriodo.getDtMesFinal();
					feriado.setDayMonth(day+"/"+month);
					feriado.setToolTip(cePeriodo.getDsEvento());
					holidays.add(feriado);
				}			
			}
			
			//insere no 1o mes do periodo
			if (cePeriodo.getDtDiaFinal() != null && ce == cePeriodo) {
				for (int i = 0; i < (datas.length - cePeriodo.getDtDia()+1); i++) {
					AgendaItem feriado = new AgendaItem();
					String day = cePeriodo.getDtDia()+i > 9 ? String.valueOf(i+cePeriodo.getDtDia()) : "0"+String.valueOf(i+cePeriodo.getDtDia());
					String month = cePeriodo.getDtMes()+i > 9 ? String.valueOf(cePeriodo.getDtMes()) : "0"+cePeriodo.getDtMes();
					feriado.setDayMonth(day+"/"+month);
					feriado.setToolTip(cePeriodo.getDsEvento());
					holidays.add(feriado);
				}
			}
		}
	}
	//caso seja inserido um periodo no mes de dezembro e janeiro, faz a inserção no novo ano
	public void insereAnoNovo(CalendarioEvento ceNovoAno, int year){
		for(CalendarioEvento cePeriodo : getFeriadosSimples(year-1)){
			if (ceNovoAno == cePeriodo && cePeriodo.getDtMesFinal() != null) {
				for (int i = 1; i <= ceNovoAno.getDtDiaFinal(); i++) {
					AgendaItem feriado = new AgendaItem();
					String day = (i) > 9 ? String.valueOf(i) : "0"+String.valueOf(i);
					String month = ceNovoAno.getDtMesFinal() > 9 ? String.valueOf(ceNovoAno.getDtMesFinal()) : "0"+ceNovoAno.getDtMesFinal();
					feriado.setDayMonth(day+"/"+month);
					feriado.setToolTip(ceNovoAno.getDsEvento());
					holidays.add(feriado);
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	private List<CalendarioEvento> getFeriadosSimples(int year){
		StringBuilder sb = new StringBuilder();
		sb.append("select o from CalendarioEvento o where ");
		sb.append("o.inFeriado = true and ");
		sb.append("(o.dtAno is null or o.dtAno = :ano) ");
		sb.append("and (o.dtMes = :mes or o.dtMesFinal = :mes)");
		Query q = EntityUtil.createQuery(sb.toString());
		q.setParameter("ano", year);
		q.setParameter("mes", datas[0].getMonth() + 1);
		List<CalendarioEvento> resultList = q.getResultList();
		if(resultList.size() > 0) {
			return resultList;
		} else {
			return new ArrayList<CalendarioEvento>(0);
		}
	}
	
	public void selectDay(ValueChangeEvent event) {
		setCurrentDate((Date)event.getNewValue());
	}
	
	public void marcarDiaPlantao() {
		
		for (AgendaItem item : items) {
			if(format.format(getCurrentDate()).equals(format.format(item.getDate()))) {
				if(SELECTED_DAY.equals(item.getStyleClass())) {
					item.setStyleClass(null);
				} else {
					item.setStyleClass(SELECTED_DAY);
				}
			}
		}
	}

	public void refreshAgenda() {
		items = null;
		getData(datas);
	}
	
	public void setItems(AgendaItem[] items) {
		this.items = items;
	}

	public AgendaItem[] getItems() {
		return items;
	}

	public void setCurrentDate(Date currentDate) {
		this.currentDate = currentDate;
	}

	public Date getCurrentDate() {
		return currentDate;
	}
	
}
