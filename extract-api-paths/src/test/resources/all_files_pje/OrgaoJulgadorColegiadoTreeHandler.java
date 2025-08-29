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
package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;

@Name(OrgaoJulgadorColegiadoTreeHandler.NAME)
@BypassInterceptors
public class OrgaoJulgadorColegiadoTreeHandler extends AbstractTreeHandler<OrgaoJulgadorColegiado> {

	public static final String NAME = "orgaoJulgadorColegiadoTree";
	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return "select o from OrgaoJulgadorColegiado o " + "where orgaoJulgadorColegiadoPai is null "
				+ "order by orgaoJulgadorColegiado";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from OrgaoJulgadorColegiado n where orgaoJulgadorColegiadoPai = :" + EntityNode.PARENT_NODE;
	}

	@Override
	protected OrgaoJulgadorColegiado getEntityToIgnore() {
		return ComponentUtil.getInstance("orgaoJulgadorColegiadoHome");
	}

}
