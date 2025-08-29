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
package br.com.infox.ibpm.component.tree;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.ManagedJbpmContext;
import org.jbpm.graph.def.ProcessDefinition;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Assunto;

@Name("iniciarFluxoTree")
@BypassInterceptors
public class IniciarFluxoTreeHandler extends AbstractTreeHandler<Assunto> {

	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(IniciarFluxoTreeHandler.class);

	@Override
	protected String getQueryRoots() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o ");
		sb.append("from Assunto o inner join o.fluxo flux ");
		sb.append("where o.ativo = true and ");
		sb.append("	  flux.publicado = true and ");
		sb.append("	  flux.ativo = true and ");
		sb.append("	  (cast(now() as date) between cast(flux.dataInicioPublicacao as date) and cast(flux.dataFimPublicacao as date) or ");
		sb.append("	  cast(now() as date) >= cast(flux.dataInicioPublicacao as date) and flux.dataFimPublicacao is null) ");
		sb.append("order by 1");
		return sb.toString();
	}

	/*
	 * Não esta pegando nenhum filho. Até o momento, esta tree esta funcionando
	 * de forma semelhante a um combo
	 */
	@Override
	protected String getQueryChildren() {
		StringBuilder sb = new StringBuilder();
		sb.append("select n from Assunto n where ");
		sb.append("n.fluxo is not null and ");
		sb.append("n.ativo = true and n.ativo = false and ");
		sb.append("n.assuntoPai = :");
		sb.append(EntityNode.PARENT_NODE);
		return sb.toString();
	}

	@Override
	protected String getEventSelected() {
		return "fluxoSelecionadoProcesso";
	}

	@Override
	public List<EntityNode<Assunto>> getRoots() {
		if (rootList == null) {
			List<EntityNode<Assunto>> list = super.getRoots();
			rootList = new ArrayList<EntityNode<Assunto>>();
			List<ProcessDefinition> processDefinitions = ManagedJbpmContext.instance().getGraphSession()
					.findAllProcessDefinitions();
			for (EntityNode<Assunto> node : list) {
				boolean found = false;
				String fluxo = node.getEntity().getFluxo().getFluxo();
				for (ProcessDefinition pd : processDefinitions) {
					if (pd.getName().equals(fluxo)) {
						rootList.add(node);
						found = true;
						break;
					}
				}
				if (!found) {
					log.warn("Fluxo [" + fluxo + "] não está publicado no jBPM");
				}
			}
		}
		return rootList;
	}

	@Override
	protected Assunto getEntityToIgnore() {
		return ComponentUtil.getInstance("assuntoHome");
	}

}
