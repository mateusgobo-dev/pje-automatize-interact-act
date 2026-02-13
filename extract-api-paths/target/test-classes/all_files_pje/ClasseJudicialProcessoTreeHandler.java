package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Jurisdicao;

@Name("classeJudicialProcessoTree")
@BypassInterceptors
public class ClasseJudicialProcessoTreeHandler extends AbstractTreeHandler<ClasseJudicial> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		Jurisdicao jurisdicao = ProcessoTrfHome.instance().getInstance().getJurisdicao();
		int id = jurisdicao != null ? jurisdicao.getIdJurisdicao() : -1;
		String classeJudicialFiltro = ProcessoTrfHome.instance().getClasseJudicialFiltro();
		if (classeJudicialFiltro == null)
			classeJudicialFiltro = "";
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(o) from ClasseJudicial o ");
		sb.append("inner join o.classeAplicacaoList cat ");
		sb.append("inner join cat.competenciaClasseAssuntoList cca ");
		sb.append("where o.ativo = true and cat.ativo = true ");
		sb.append("and (o.recursal = true or o.incidental = true) ");
		sb.append("and cca.competencia.ativo = true ");
		if (!Strings.isEmpty(classeJudicialFiltro)) {
			sb.append("and lower(o.classeJudicial) like concat('%',lower('" + classeJudicialFiltro + "'),'%') ");
		}

		sb.append("and exists (select ojc.competencia ");
		sb.append("			   from OrgaoJulgadorCompetencia ojc ");
		sb.append("			   inner join ojc.orgaoJulgador oj ");
		sb.append("            where oj.jurisdicao.idJurisdicao = " + id);
		sb.append("			     and oj.ativo = true ");
		sb.append("			     and oj.aplicacaoClasse = cat.aplicacaoClasse ");
		sb.append("              and ojc.competencia = cca.competencia ");
		sb.append("              and ojc.dataInicio <= current_date ");
		sb.append("              and (ojc.dataFim >= current_date or ojc.dataFim is null) ");
		sb.append(") ");
		sb.append("order by o.classeJudicial");
		return sb.toString();
	}

	@Override
	protected String getQueryChildren() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ClasseJudicial o where classeJudicialPai = :");
		sb.append(EntityNode.PARENT_NODE);
		sb.append(" and (o.fluxo is not null ");
		sb.append("      OR exists (select c.idClasseJudicial from ClasseJudicial ");
		sb.append("      c where c.classeJudicialPai = o))");
		return sb.toString();
	}

	@Override
	protected ClasseJudicial getEntityToIgnore() {
		return ComponentUtil.getInstance("classeJudicialHome");
	}

}