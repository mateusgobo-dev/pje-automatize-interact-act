package br.com.infox.component.agenda;

import java.io.Serializable;
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
import org.jboss.seam.Component;
import org.jboss.seam.core.Expressions;
import org.richfaces.model.CalendarDataModel;
import org.richfaces.model.CalendarDataModelItem;
import br.com.infox.cliente.component.suggest.PessoaPlantaoSuggestBean;
import br.com.infox.cliente.home.PlantaoOficialJusticaHome;
import br.jus.pje.nucleo.entidades.Plantao;

public class Agenda implements CalendarDataModel, Serializable{

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

	private enum Holidays {
		CARNAVAL,
		SEXTA_SANTA,
		CORPUS_CHRISTI
	};

	@Override
	public CalendarDataModelItem[] getData(Date[] dates){
		if (items != null && items[0] != null && items[0].getDate() != null
				&& getKey(items[0].getDate()).equals(getKey(dates[0]))){
			return items;
		}
		datas = dates;
		items = new AgendaItem[dates.length];
		Calendar c = new GregorianCalendar();
		c.setTime(dates[0]);
		initYear(c.get(Calendar.YEAR));
		HashMap<String, Plantao> plantaoMap = buildPlantaoMap();
		for (int i = 0; i < dates.length; i++){
			c.setTime(dates[i]);
			items[i] = getDayData(c, plantaoMap);
			items[i].setDate(dates[i]);
		}
		return items;
	}

	private AgendaItem getDayData(Calendar c, HashMap<String, Plantao> plantaoMap){
		AgendaItem item = new AgendaItem();
		Map<String, Object> data = new HashMap<String, Object>();
		String date = String.format(DATE_PATTERN + "/%1tY", c.getTime());
		int i = thisYearHolidays.indexOf(date);
		if (i != -1){
			item = holidays.get(i);
			if (item.getStyleClass() == null){
				item.setStyleClass(HOLIDAY_STYLE_CLASS);
			}
		}
		item.setDay(c.get(Calendar.DAY_OF_MONTH));
		if (plantaoMap.containsKey(getKey(c.getTime()))){
			Plantao plantao = plantaoMap.get(getKey(c.getTime()));
			data.put("nomePessoa", plantao.getPessoa().getNome());
			if (plantao.getHoraInicial() != null){
				data.put("horarios", getHourKey(plantao));
			}
			else{
				data.put("horarios", "Dia todo");
			}
		}
		else{
			data.put("nomePessoa", "");
			data.put("horarios", "");
		}
		item.setData(data);
		return item;
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Plantao> buildPlantaoMap(){
		HashMap<String, Plantao> plantaoMap = new HashMap<String, Plantao>();
		PlantaoOficialJusticaHome ph = (PlantaoOficialJusticaHome) Component.getInstance("plantaoOficialJusticaHome");
		PessoaPlantaoSuggestBean ppsb = (PessoaPlantaoSuggestBean) Component.getInstance("pessoaPlantaoSuggest");
		ph.getInstance().setPessoa(ppsb.getInstance());
		if (ph.getInstance().getPessoa() != null){
			Query hql = ph.getEntityManager().createQuery("select o from Plantao o where " + "o.pessoa.idUsuario = ?1");
			hql.setParameter(1, ph.getInstance().getPessoa().getIdUsuario());
			List<Plantao> plantaoList = hql.getResultList();
			for (Plantao plantao : plantaoList){
				plantaoMap.put(getKey(plantao.getDtPlantao()), plantao);
			}
		}
		return plantaoMap;
	}

	@Override
	public Object getToolTip(Date date){
		return null;
	}

	public List<AgendaItem> getHolidays(){
		return holidays;
	}

	public void setHolidays(List<String> holidays){
		for (String s : holidays){
			this.holidays.add(toObject(s));
		}
	}

	private AgendaItem toObject(String value){
		AgendaItem item = new AgendaItem();
		String[] fields = value.split(",");
		item.setDayMonth(fields[0]);
		String toolTip = fields[1];
		toolTip = (String) Expressions.instance().createValueExpression(toolTip).getValue();
		item.setToolTip(toolTip);
		if (fields.length > 3){
			item.setStyleClass(fields[3]);
		}
		return item;
	}

	private void initYear(int year){
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
		for (AgendaItem dd : getHolidays()){
			String date = "";
			if (holidaysMap.containsKey(dd.getDayMonth())){
				Date d = holidaysMap.get(dd.getDayMonth());
				date = String.format(DATE_PATTERN, d);
			}
			else{
				date = dd.getDayMonth();
			}
			thisYearHolidays.add(date + "/" + year);
		}

	}

	public void selectDay(ValueChangeEvent event){
		setCurrentDate((Date) event.getNewValue());
	}

	public void marcarDiaPlantao(){

		for (AgendaItem item : items){
			if (getKey(getCurrentDate()).equals(getKey(item.getDate()))){
				if (SELECTED_DAY.equals(item.getStyleClass())){
					item.setStyleClass(null);
				}
				else{
					item.setStyleClass(SELECTED_DAY);
				}
			}
		}
	}

	private String getKey(Date data){
		Calendar cal = Calendar.getInstance();
		cal.setTime(data);
		String key = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/"
				+ cal.get(Calendar.YEAR);
		return key;
	}

	@SuppressWarnings("unchecked")
	private String getHourKey(Plantao plantao){
		StringBuilder diaPlantao = new StringBuilder();
		PlantaoOficialJusticaHome ph = (PlantaoOficialJusticaHome) Component.getInstance("plantaoOficialJusticaHome");
		Query hql = ph.getEntityManager().createQuery(
				"select o from Plantao o where "
						+ "o.pessoa.idUsuario = ?1 and o.dtPlantao = ?2 order by o.horaInicial");
		hql.setParameter(1, plantao.getPessoa().getIdUsuario());
		hql.setParameter(2, plantao.getDtPlantao());
		for (Plantao p : (List<Plantao>) hql.getResultList()){
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			diaPlantao.append(sdf.format(p.getHoraInicial()));
			diaPlantao.append(" - " + sdf.format(p.getHoraFinal()));
			diaPlantao.append(" (" + p.getLocalizacao().toString() + ")<br/>");
		}
		return diaPlantao.toString();
	}

	public void refreshAgenda(){
		items = null;
		getData(datas);
	}

	public void setItems(AgendaItem[] items){
		this.items = items;
	}

	public AgendaItem[] getItems(){
		return items;
	}

	public void setCurrentDate(Date currentDate){
		this.currentDate = currentDate;
	}

	public Date getCurrentDate(){
		return currentDate;
	}

}