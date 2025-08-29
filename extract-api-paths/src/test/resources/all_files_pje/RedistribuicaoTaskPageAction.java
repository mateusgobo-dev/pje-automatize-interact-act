package br.com.infox.bpm.taskPage.FGPJE;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.home.ProcessoTrfRedistribuicaoHome;

@Name(value = RedistribuicaoTaskPageAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class RedistribuicaoTaskPageAction extends TaskAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "redistribuicaoTaskPageAction";

	public void limparComboTipoRedistribuicao() {
		limparComboJurisdicao();
		limparComboOrgaoJulgador();
	}

	public void limparComboJurisdicao() {
		ProcessoTrfRedistribuicaoHome.instance().getInstance().setJurisdicao(null);
	}

	public void limparComboOrgaoJulgador() {
		ProcessoTrfHome.instance().setOrgaoJulgador(null);
		ProcessoTrfHome.instance().setOrgaoJulgadorColegiado(null);
	}

}
