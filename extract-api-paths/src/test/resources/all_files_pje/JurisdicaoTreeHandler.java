package br.com.infox.cliente.component.tree;

import java.util.List;
import java.util.Map;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.richfaces.event.NodeSelectedEvent;

import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.infox.pje.list.ProcessoTrfInicialAdvogadoList;
import br.com.itx.util.ComponentUtil;

@Name(JurisdicaoTreeHandler.NAME)
@BypassInterceptors
public class JurisdicaoTreeHandler extends AbstractTreeHandler<Map<String, Object>> {

	public static final String NAME = "jurisdicaoTree";
	public static final String EVENT_SELECTED = "jurisdicaoTreeEventSelected";
	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryChildren() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct new map(max(o.processoTrf.idProcessoTrf) as id, ");
		sb.append("count(o.processoTrf.idProcessoTrf) as qtd, ");
		sb.append("count(o.idProcessoCaixaAdvogadoProcurador) as qtdEmCaixa, ");
		sb.append("o.processoTrf.jurisdicao as jurisdicao, ");
		sb.append("'ProcessoCaixaAdvogadoProcurador' as type) ");
		sb.append("  from ProcessoCaixaAdvogadoProcurador o ");
		sb.append("where o.processoTrf.jurisdicao = :jurisdicao ");
		sb.append("and o.processoTrf.processoStatus = 'D' ");
		ProcessoTrfInicialAdvogadoList processoTrfInicialAdvogadoList = ProcessoTrfInicialAdvogadoList.instance();
		if (processoTrfInicialAdvogadoList.getCaixaPendentes()) {
			sb.append("and o.processoTrf.idProcessoTrf in (select ppe.processoJudicial.idProcessoTrf from ProcessoParteExpediente ppe ");
			sb.append("where ppe.dtCienciaParte is not null and ");
			sb.append("ppe.pendenteManifestacao = true and ");
			sb.append("ppe.pessoaParte in (#{pessoaAdvogadoHome.pessoaAdvogadoProcurador})) ");
		}

		sb.append("group by o.processoTrf.jurisdicao ");
		sb.append("order by o.processoTrf.jurisdicao ");
		return sb.toString();
	}

	@Override
	protected String getQueryRoots() {
		StringBuilder sb = new StringBuilder();
		sb.append("select new map(max(o.idProcessoTrf) as id, ");
		sb.append("count(o.processoTrf.idProcessoTrf) as qtd, ");
		sb.append("o.jurisdicao as jurisdicao, ");
		sb.append("'Jurisdicao' as type) ");
		sb.append("from ConsultaProcessoTrf o ");
		sb.append("where o.processoStatus = 'D' ");
		ProcessoTrfInicialAdvogadoList processoTrfInicialAdvogadoList = ProcessoTrfInicialAdvogadoList.instance();
		if (processoTrfInicialAdvogadoList.getCaixaPendentes()) {
			sb.append(processoTrfInicialAdvogadoList.getEjbqlFiltroPendentes());
		}
		sb.append("group by o.jurisdicao ");
		sb.append("order by o.jurisdicao ");
		return sb.toString();
	}

	@Override
	protected JurisdicaoEntityNode createNode() {
		return new JurisdicaoEntityNode(getQueryChildrenList());
	}

	@Override
	public List<EntityNode<Map<String, Object>>> getRoots() {
		Events.instance().raiseEvent(ControleFiltros.INICIALIZAR_FILTROS);
		return super.getRoots();
	}

	@Override
	protected String getEventSelected() {
		return EVENT_SELECTED;
	}

	public static JurisdicaoTreeHandler instance() {
		return ComponentUtil.getComponent(NAME);
	}

	@Override
	public void selectListener(NodeSelectedEvent ev) {
		ProcessoTrfInicialAdvogadoList.instance().limparCaixaPendentes();
		super.selectListener(ev);
	}

}