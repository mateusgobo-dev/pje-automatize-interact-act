package br.com.infox.cliente.component.suggest;

import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;

import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.component.suggest.AbstractSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.HibernateUtil;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("processoTrfAlertaSuggest")
@BypassInterceptors
public class ProcessoTrfAlertaSuggestBean extends AbstractSuggestBean<ProcessoTrf> {

	private static final long serialVersionUID = 1L;

	@Override
	public String getEjbql() {
		HibernateUtil.disableAllFilters();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("select o from ProcessoTrf o ");
		sb.append("where lower(o.processo.numeroProcesso) like lower(concat ('%', :");
		sb.append(INPUT_PARAMETER);
		sb.append(", '%')) ");
		if(!Authenticator.isPapelAdministrador()){

			Integer idOrgaoJulgadorAtual = Authenticator.getIdOrgaoJulgadorAtual();
			if(idOrgaoJulgadorAtual != null){
				sb.append(" and o.orgaoJulgador.idOrgaoJulgador = " + idOrgaoJulgadorAtual);
			}
			
			Integer idOrgaoJulgadorColegiadoAtual = Authenticator.getIdOrgaoJulgadorColegiadoAtual();
			if(idOrgaoJulgadorColegiadoAtual != null){
				sb.append(" and o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = " + idOrgaoJulgadorColegiadoAtual);
			}
			
		}
		sb.append(" order by 1");
		
		return sb.toString();
	}

	@Override
	public List<ProcessoTrf> suggestList(Object typed){
		Events.instance().raiseEvent(ControleFiltros.INICIALIZAR_FILTROS);
		
		return super.suggestList(typed);
	}
}
