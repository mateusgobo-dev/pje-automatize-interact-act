package br.com.infox.cliente.component.tree;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

@Name("competenciaClasseAssuntoTree")
@BypassInterceptors
public class CompetenciaClasseAssuntoTreeHandler extends AbstractTreeHandler<AssuntoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		String classeJudicial = "";
		String competencia = "";
		return "select n from AssuntoTrf n " + "inner join n.competenciaClasseAssuntoList cca "
				+ "where cca.classeAplicacao.classeJudicial.classeJudicial = '" + classeJudicial + "' and "
				+ "cca.competencia.competencia = '" + competencia + "' " + "order by n.assuntoTrf";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from AssuntoTrf n where assuntoTrfSuperior = :" + EntityNode.PARENT_NODE;
	}

	protected ProcessoTrfHome getHome() {
		return (ProcessoTrfHome) Component.getInstance("processoTrfHome");
	}

}