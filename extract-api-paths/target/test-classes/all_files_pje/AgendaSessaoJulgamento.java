package br.com.infox.component.agenda;

import java.io.Serializable;
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

import br.com.infox.cliente.home.SessaoHome;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.Sessao;

public class AgendaSessaoJulgamento implements CalendarDataModel, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2987916418963801264L;

	private static final String SELECTED_DAY = "wi-agenda-selected";

	private static final String HOLIDAY_STYLE_CLASS = "rich-calendar-holly";

	private transient List<AgendaItem> holidays = new ArrayList<AgendaItem>();

	private List<String> thisYearHolidays = new ArrayList<String>();

	private static final String DATE_PATTERN = "%1$td/%1$tm";

	private Map<String, Date> holidaysMap = new HashMap<String, Date>();

	private transient AgendaItem[] items;

	private Date[] datas;

	private Date currentDate;

	private enum Holidays {
		CARNAVAL, SEXTA_SANTA, CORPUS_CHRISTI
	};

	@Override
	public CalendarDataModelItem[] getData(Date[] dates) {
		items = null;
		if (items != null && items[0] != null && items[0].getDate() != null
				&& getKey(items[0].getDate()).equals(getKey(dates[0]))) {
			return items;
		}
		datas = dates;
		items = new AgendaItem[dates.length];
		Calendar c = new GregorianCalendar();
		c.setTime(dates[0]);
		initYear(c.get(Calendar.YEAR));
		HashMap<String, List<Sessao>> sessaoAgendaMap = buildAgendaSessaoMap();
		for (int i = 0; i < dates.length; i++) {
			c.setTime(dates[i]);
			items[i] = getDayData(c, sessaoAgendaMap);
			items[i].setDate(dates[i]);
		}
		return items;
	}

	private AgendaItem getDayData(Calendar c, HashMap<String, List<Sessao>> sessaoAgendaMap) {
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
		if (sessaoAgendaMap.containsKey(getKey(c.getTime()))) {
			SessaoHome sa = SessaoHome.instance();
			Query q = sa.getEntityManager().createQuery("select o from Sessao o " + "where o.dataSessao = :dtSessao")
					.setParameter("dtSessao", c.getTime());

			if (q.getResultList().size() == 1) {
				data.put("tipoSessao", "Existe " + q.getResultList().size() + " sessão ");
			} else {
				data.put("tipoSessao", "Existem " + q.getResultList().size() + " sessões ");
			}
			if (!new Date().after(c.getTime())) {
				item.setEnabled(false);
			}
		} else {
			item.setEnabled(false);
		}
		item.setData(data);
		return item;
	}

	public String verificarSalasSessao(Sessao sessao) {

		return null;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, List<Sessao>> buildAgendaSessaoMap() {
		HashMap<String, List<Sessao>> sessaoAgendaMap = new HashMap<String, List<Sessao>>();
		SessaoHome sa = SessaoHome.instance();
		Query hql = sa
				.getEntityManager()
				.createQuery(
						"select o.sessao from SessaoComposicaoOrdem o " + "where o.orgaoJulgador = :orgaoJulgadorAtual")
				.setParameter("orgaoJulgadorAtual", Authenticator.getOrgaoJulgadorAtual());
		List<Sessao> sessaoAgendaList = hql.getResultList();
		for (Sessao sessaoAgenda : sessaoAgendaList) {
			String key = getKey(sessaoAgenda.getDataSessao());
			if (sessaoAgendaMap.containsKey(key)) {
				sessaoAgendaMap.get(key).add(sessaoAgenda);
			} else {
				List<Sessao> sessaoList = new ArrayList<Sessao>();
				sessaoList.add(sessaoAgenda);
				sessaoAgendaMap.put(key, sessaoList);
			}
		}
		return sessaoAgendaMap;
	}

	@Override
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

	public void selectDay(ValueChangeEvent event) {
		setCurrentDate((Date) event.getNewValue());
	}

	public void marcarDiaPlantao() {

		for (AgendaItem item : items) {
			if (getKey(getCurrentDate()).equals(getKey(item.getDate()))) {
				if (SELECTED_DAY.equals(item.getStyleClass())) {
					item.setStyleClass(null);
				} else {
					item.setStyleClass(SELECTED_DAY);
				}
			}
		}
	}

	private String getKey(Date data) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(data);
		String key = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/"
				+ cal.get(Calendar.YEAR);
		return key;
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