
package br.com.infox.ibpm.jbpm.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.hibernate.Session;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Transition;

@org.jboss.seam.annotations.faces.Converter
@Name("transitionJbpmConverter")
@BypassInterceptors
public class TransitionJbpmConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		JbpmContext context = ManagedJbpmContext.instance();
		Session session = context.getSession();
		Transition t = (Transition) session.load(Transition.class,  Long.valueOf(arg2));
		return t;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		Transition t = (Transition) arg2;
		return Long.toString(t.getId());
	}

}
