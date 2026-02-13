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
package br.com.infox.ibpm.help;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.grid.GridColumn;
import br.com.itx.component.grid.GridQuery;

@Name("historicoAjudaGrid")
@SuppressWarnings("unchecked")
@BypassInterceptors
public class HistoricoAjudaGrid extends GridQuery {

	private static final long serialVersionUID = 1L;

	public HistoricoAjudaGrid() {
		setEjbql("select o from HistoricoAjuda o");
		setOrder("dataRegistro desc");
		setMaxResults(5);
		setGridId("historicoAjuda");
		setKey("idHistoricoAjuda");
		List<GridColumn> columns = new ArrayList<GridColumn>();
		GridColumn c = new GridColumn();
		c.setId("dataRegistro");
		Map<String, String> p = new HashMap<String, String>();
		p.put("header", "Data");
		c.setProperties(p);
		c.setValueType("date");
		columns.add(c);

		c = new GridColumn();
		c.setId("usuario");
		p = new HashMap<String, String>();
		p.put("header", "Usuário");
		c.setProperties(p);
		columns.add(c);

		c = new GridColumn();
		c.setId("texto");
		p = new HashMap<String, String>();
		p.put("header", " ");
		c.setProperties(p);
		c.setValueType("text");
		columns.add(c);

		setColumns(columns);

		List<String> r = new ArrayList<String>();
		r.add("pagina.url = #{ajudaHome.viewId}");
		setRestrictionExpressionStrings(r);

	}

}
