package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.ProcessoTrfConexaoHome;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("processoTrfConexaoTree")
@BypassInterceptors
public class ProcessoTrfConexaoTreeHandler extends AbstractTreeHandler<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		ProcessoTrfConexaoHome processoTrfConexaoHome = ProcessoTrfConexaoHome.instance();
		ProcessoTrf processoSelecionado = processoTrfConexaoHome.getProcessoSelecionado();
		if (processoSelecionado == null) {
			return "select n from ProcessoTrf n where n.idProcessoTrf = 0";
		}
		return "select n.processoTrfConexo from ProcessoTrfConexao n " + "where n.processoTrf.idProcessoTrf = "
				+ processoSelecionado.getIdProcessoTrf();
	}

	@Override
	protected String getQueryChildren() {
		return "select n.processoTrfConexo from ProcessoTrfConexao n where n.processoTrf = :" + EntityNode.PARENT_NODE;
	}

}