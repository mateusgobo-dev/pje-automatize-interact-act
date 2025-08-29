package br.com.infox.component.agenda;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import org.jboss.seam.annotations.Name;
import org.richfaces.model.CalendarDataModel;
import org.richfaces.model.CalendarDataModelItem;

@Name(AgendaDiasPosteriores.NAME)
public class AgendaDiasPosteriores implements CalendarDataModel, Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "agendaDiasPosteriores";
	private transient AgendaItem[] items;

	@Override
	public CalendarDataModelItem[] getData(Date[] dates){

		items = new AgendaItem[dates.length];
		Calendar c = Calendar.getInstance();
		c.setTime(dates[0]);
		Calendar cAtual = Calendar.getInstance();
		cAtual.set(Calendar.HOUR, 0);
		cAtual.set(Calendar.MINUTE, 0);
		cAtual.set(Calendar.SECOND, 0);
		cAtual.set(Calendar.MILLISECOND, 0);

		for (int i = 0; i < dates.length; i++){
			items[i] = new AgendaItem();
			items[i].setDate(dates[i]);
			Calendar cTemp = Calendar.getInstance();
			cTemp.setTime(dates[i]);
			if (cTemp.compareTo(cAtual) < 0){
				items[i].setEnabled(true);
			}
		}
		return items;
	}

	@Override
	public Object getToolTip(Date date){
		return null;
	}

}