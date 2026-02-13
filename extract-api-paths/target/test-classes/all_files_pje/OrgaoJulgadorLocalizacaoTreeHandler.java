package br.com.infox.cliente.component.tree;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.infox.component.tree.EntityNode;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name("orgaoJulgadorLocalizacaoTree")
@BypassInterceptors
public class OrgaoJulgadorLocalizacaoTreeHandler extends AbstractTreeHandler<Localizacao> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		StringBuilder sb = new StringBuilder();
		sb.append("select l ");
		sb.append("from Localizacao l ");
		sb.append("where not exists ");
		sb.append("(select o from OrgaoJulgador o ");
		sb.append(" where l = o.localizacao and o.idOrgaoJulgador != '");
		sb.append(getAbstractHome().getInstance().getIdOrgaoJulgador());
		sb.append("') and ");
		sb.append("l.localizacaoPai is null ");
		sb.append("order by l.localizacao");
		return sb.toString();
	}

	@Override
	protected String getQueryChildren() {
		StringBuilder sb = new StringBuilder();
		sb.append("select n from Localizacao n ");
		sb.append("where not exists ");
		sb.append("(select o1 from OrgaoJulgador o1 ");
		sb.append("where n = o1.localizacao or n.localizacaoPai = o1.localizacao) and ");
		sb.append("n.localizacaoPai = :");
		sb.append(EntityNode.PARENT_NODE);
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	protected AbstractHome<OrgaoJulgador> getAbstractHome() {
		return (AbstractHome<OrgaoJulgador>) Component.getInstance("orgaoJulgadorHome");
	}

	@Override
	protected Localizacao getEntityToIgnore() {
		return ComponentUtil.getInstance("localizacaoHome");
	}

}