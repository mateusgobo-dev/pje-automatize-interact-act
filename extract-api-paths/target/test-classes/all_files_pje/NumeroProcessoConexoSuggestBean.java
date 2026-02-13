package br.com.infox.cliente.component.suggest;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.cliente.home.ProcessoTrfConexaoHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("numeroProcessoConexoSuggest")
@Scope(ScopeType.CONVERSATION)
public class NumeroProcessoConexoSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoTrf o where o.processoStatus = 'D'");
		sb.append(" and o.orgaoJulgador.idOrgaoJulgador = ");
		sb.append(ProcessoTrfConexaoHome.instance().getOrgaoJulgadorConexo().getIdOrgaoJulgador());
		sb.append(" and o.processo.numeroProcesso != '");
		sb.append(ProcessoTrfHome.instance().getInstance().getNumeroProcesso());
		sb.append("' and o.processo.numeroProcesso like concat(:");
		sb.append(INPUT_PARAMETER);
		sb.append(", '%')) order by o.processo.numeroProcesso");

		return sb.toString();
	}
}
