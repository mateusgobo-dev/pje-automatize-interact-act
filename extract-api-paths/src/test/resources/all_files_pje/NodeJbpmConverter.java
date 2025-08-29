/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.jbpm.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.hibernate.Session;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jbpm.JbpmContext;
import org.jbpm.graph.def.Node;

@org.jboss.seam.annotations.faces.Converter
@Name("nodeJbpmConverter")
@BypassInterceptors
public class NodeJbpmConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		JbpmContext context = ManagedJbpmContext.instance();
		Session session = context.getSession();

		org.hibernate.Query q = session.createQuery("select a from org.jbpm.graph.def.Node a where a.id = :id");
		q.setParameter("id", Long.valueOf(arg2));
		Node n = (Node) q.uniqueResult();
		return n;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		Node node = (Node) arg2;
		return Long.toString(node.getId());
	}

}
