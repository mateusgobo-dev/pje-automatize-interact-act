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

import br.com.itx.component.Form;
import br.com.itx.component.FormField;
import br.com.itx.component.Template;

@Name("ajudaForm")
@BypassInterceptors
public class AjudaForm extends Form {

	private static final long serialVersionUID = 1L;

	public AjudaForm() {
		Template b = new Template();
		b.setId("ajuda");
		setButtons(b);
		setFormId("ajuda");
		List<FormField> f = new ArrayList<FormField>();
		FormField ff = new FormField();
		ff.setId("texto");
		ff.setType("textEdit");
		ff.setLabel("Texto");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("width", "660");
		m.put("height", "500");
		m.put("autoResize", "false");
		ff.setProperties(m);
		f.add(ff);
		setFields(f);
	}

}
