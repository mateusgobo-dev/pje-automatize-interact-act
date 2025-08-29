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
package br.com.infox.ibpm.jbpm.actions;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Observer;

import br.com.infox.ibpm.jbpm.ActionTemplate;
import br.com.infox.ibpm.jbpm.handler.ActionTemplateHandler;
import br.com.itx.component.Util;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@SuppressWarnings("unchecked")
public abstract class AbstractEventoActionOrgaoJulgadorColegiado extends ActionTemplate {

	private static final long serialVersionUID = 1L;

	private List<OrgaoJulgador> naoRegistrados;
	private List<OrgaoJulgador> registrados;

	private String createdExpression;

	@Override
	public void extractParameters(String expression) {
		if (expression == null || expression.equals("")) {
			registrados = new ArrayList<OrgaoJulgador>();
			return;
		}
		String[] params = getExpressionParameters(expression);
		List<Integer> ids = new ArrayList<Integer>();
		for (String p : params) {
			ids.add(Integer.parseInt(p));
		}
		registrados = EntityUtil.getEntityManager()
				.createQuery("select o from OrgaoJulgador o where " + "o.idOrgaoJulgador in (:ids)")
				.setParameter("ids", Util.isEmpty(ids)?null:ids).getResultList();
	}

	public void createExpression() {
		if (registrados.isEmpty()) {
			createdExpression = "";
		} else {
			StringBuilder sb = new StringBuilder("#{");
			sb.append(getExpression()).append("(");
			for (OrgaoJulgador ae : registrados) {
				if (!sb.toString().endsWith("(")) {
					sb.append(",");
				}
				sb.append(ae.getIdOrgaoJulgador());
			}
			sb.append(")}");
			createdExpression = sb.toString();
		}
	}

	public String getCreatedExpression() {
		return createdExpression;
	}

	public void setOrgaoJulgador(List<OrgaoJulgador> naoRegistrados) {
		this.naoRegistrados = naoRegistrados;
	}

	public List<OrgaoJulgador> getNaoRegistrdos() {
		if (naoRegistrados == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct o from OrgaoJulgador o inner join ")
					.append("o.orgaoJulgadorColegiadoOrgaoJulgadorList ojc where ")
					.append("ojc.orgaoJulgadorColegiado.orgaoJulgadorColegiadoList.size = 0 and ");
			naoRegistrados = EntityUtil.getEntityManager().createQuery(sb.toString()).getResultList();
		}
		return naoRegistrados;
	}

	public void setRegistrados(List<String> registrados) {
		if (this.registrados == null) {
			this.registrados = new ArrayList<OrgaoJulgador>();
		} else {
			this.registrados.clear();
		}
		for (String s : registrados) {
			for (OrgaoJulgador ae : naoRegistrados) {
				if (ae.getOrgaoJulgador().equals(s)) {
					this.registrados.add(ae);
					break;
				}
			}
		}
	}

	@Observer(ActionTemplateHandler.SET_CURRENT_TEMPLATE_EVENT)
	public void clearListOnChangeNode() {
		registrados = null;
	}

	public List<OrgaoJulgador> getRegistrados() {
		return registrados;
	}

}