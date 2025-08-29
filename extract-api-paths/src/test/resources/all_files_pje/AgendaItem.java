package br.com.infox.component.agenda;

import java.util.Date;

import org.richfaces.model.CalendarDataModelItem;

public class AgendaItem implements CalendarDataModelItem {

	private int day;

	private String toolTip;

	private String styleClass;

	private boolean enabled = true;

	private Date date;

	private String dayMonth;

	private Object data;

	@Override
	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	@Override
	public String getStyleClass() {
		return styleClass;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	@Override
	public Object getToolTip() {
		return toolTip;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}

	@Override
	public boolean hasToolTip() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getDayMonth() {
		return dayMonth;
	}

	public void setDayMonth(String dayMonth) {
		this.dayMonth = dayMonth;
	}

	@Override
	public String toString() {
		return toolTip + " - " + enabled + " - " + date;
	}

}