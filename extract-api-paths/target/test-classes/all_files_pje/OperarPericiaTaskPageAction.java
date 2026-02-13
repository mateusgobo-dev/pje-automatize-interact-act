package br.com.infox.bpm.taskPage.FGPJE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.bpm.action.TaskAction;
import br.com.infox.cliente.home.ProcessoExpedienteHome;
import br.com.infox.cliente.home.ProcessoPericiaHome;
import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.ibpm.jbpm.actions.RegistraEventoAction;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;

@Name(value = OperarPericiaTaskPageAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class OperarPericiaTaskPageAction extends TaskAction implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "operarPericiaTaskPageAction";

	private boolean paginaInicializada = false;
	
	public void inserirAtualizarDoc() throws Exception {
		if (ProcessoExpedienteHome.instance().inserirAtualizarDoc()) {
			RegistraEventoAction.instance().registraPorNome(
					DarCienciaPartesTaskPageAction.NOME_AGRUPAMENTO_EXPEDICAO_DE_DOCUMENTOS);
		}
	}

	public void initPage() {
		if (!paginaInicializada) {
			paginaInicializada = true;
			ProcessoTrfHome.instance().setarInstancia();
			Boolean possuiPericias = this.possuiPericia();
			if (!possuiPericias) {
				ProcessoPericiaHome.instance().limparInstance();
			}
			ProcessoPericiaHome.instance().fluxoDesignarPericia();
		}
	}

	public void controlePaginaDesignar() {
		ProcessoPericiaHome.instance().limparInstance();
		ProcessoPericiaHome.instance().fluxoDesignarPericia();
	}

	public Boolean possuiPericia() {
		String hql = "select o from ProcessoPericia o where o.processoTrf = :processoTrf";
		Query query = EntityUtil.createQuery(hql).setParameter("processoTrf", ProcessoTrfHome.instance().getInstance());
		return EntityUtil.getSingleResult(query) != null;
	}

	public List<Pessoa> getPartes() {
		List<Pessoa> listPartes = new ArrayList<Pessoa>();
		List<ProcessoParte> processoParteList = ProcessoTrfHome.instance().getInstance().getProcessoParteList();
		for (ProcessoParte processoParte : processoParteList) {
			listPartes.add(processoParte.getPessoa());
		}
		return listPartes;
	}

}
