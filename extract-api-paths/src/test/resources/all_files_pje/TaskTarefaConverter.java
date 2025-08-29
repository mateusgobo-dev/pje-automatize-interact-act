package br.jus.csjt.pje.view.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.Tarefa;

@org.jboss.seam.annotations.faces.Converter
@Name("taskTarefaConverter")
@BypassInterceptors
public class TaskTarefaConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value == null || "".equals(value) || "0".equals(value)) {
			return null;
		}

		Tarefa tarefa = new Tarefa();
		tarefa.setIdTarefa(Integer.parseInt(value));

		return tarefa;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return value == null ? null : value.toString();
	}

}
