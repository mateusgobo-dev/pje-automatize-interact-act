package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.jus.pje.nucleo.entidades.AssuntoTrf;

@Name("assuntoTrfCriminalTree")
@BypassInterceptors
public class AssuntoTrfCriminalTreeHandler extends AbstractTreeHandler<AssuntoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return " select distinct a" + " from AssuntoTrf a, " + "      CompetenciaClasseAssunto cca, "
				+ "      ClasseJudicialAgrupamento cja " + " where a.idAssuntoTrf = cca.assuntoTrf.idAssuntoTrf "
				+ " and   cca.classeAplicacao.classeJudicial.idClasseJudicial = cja.classe.idClasseJudicial "
				+ " and   a.assuntoTrfSuperior is null " + " order by a.assuntoTrf";
	}

	@Override
	protected String getQueryChildren() {
		return "select n from AssuntoTrf n where assuntoTrfSuperior = :" + EntityNode.PARENT_NODE;
	}

	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Override public void selectListener(NodeSelectedEvent ev) { String hql =
	 * " select distinct a" + " from AssuntoTrf a, " +
	 * "      CompetenciaClasseAssunto cca, "+
	 * "      ClasseJudicialAgrupamento cja "+
	 * " where a.idAssuntoTrf = cca.assuntoTrf.idAssuntoTrf "+
	 * " and   cca.classeAplicacao.classeJudicial.idClasseJudicial = cja.classe.idClasseJudicial "
	 * + " and   cja.agrupamento.codAgrupamento = '"+Constantes.
	 * COD_AGRUPAMENTO_CRIMINAL+"'"+ " and   a.idAssuntoTrf = :idAssuntoTrf ";
	 * Query q = getEntityManager().createQuery(hql);
	 * q.setParameter("idAssuntoTrf",
	 * ((AssuntoTrf)ev.getSource()).getIdAssuntoTrf()); List<AssuntoTrf> result
	 * = q.getResultList();
	 * 
	 * //se o assunto selecionado for do agrupamento criminal, permite a seleção
	 * if(result != null && !result.isEmpty()){ super.selectListener(ev); }else{
	 * FacesMessages.instance().add(Severity.ERROR,
	 * "Selecione um assunto percentence ao agrupamento criminal"); } }
	 */

}
