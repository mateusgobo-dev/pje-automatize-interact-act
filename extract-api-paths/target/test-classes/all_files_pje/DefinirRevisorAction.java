package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.dao.OrgaoJulgadorColegiadoOrgaoJulgadorDAO;
import br.jus.cnj.fluxo.Validador;
import br.jus.cnj.fluxo.interfaces.TaskVariavelAction;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(DefinirRevisorAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class DefinirRevisorAction implements Serializable, TaskVariavelAction {
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "definirRevisorAction";
	
	@In(create = true)
	private ProcessoJudicialAction processoJudicialAction;
	
	@In
	private OrgaoJulgadorManager orgaoJulgadorManager;
	
	@In
	private ProcessoTrfManager processoTrfManager;
		
	private List<OrgaoJulgador> outrosOrgaosJulgadoresDoColegiado;

	private ProcessoTrf processoTrf;

	public ProcessoTrf getProcessoTrf() {	
		if (processoTrf == null) {
			processoTrf = processoJudicialAction.getProcessoJudicial();
		}
		return processoTrf;
	}
	
	public OrgaoJulgadorColegiadoOrgaoJulgadorDAO getOrgaoJulgadorColegiadoOrgaoJulgadorDAO() {
		return ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorDAO.NAME);
	}
	
	public void carregaOutrosOrgaosJulgadoresDoColegiadoSeNecessario() {
		
		if (outrosOrgaosJulgadoresDoColegiado == null) {		
			outrosOrgaosJulgadoresDoColegiado = orgaoJulgadorManager.recuperarOutrosOrgaosJulgadoresDoColegiado(getProcessoTrf().getOrgaoJulgadorColegiado(), getProcessoTrf().getOrgaoJulgador());		
		}		
	}

	public List<OrgaoJulgador> getOutrosOrgaosJulgadoresDoColegiado() {				
		return outrosOrgaosJulgadoresDoColegiado;
	}
	
	public void onClickExigeRevisor() {
		if (Boolean.TRUE == getProcessoTrf().getExigeRevisor()) {
			carregaOutrosOrgaosJulgadoresDoColegiadoSeNecessario();

			if (getProcessoTrf().getOrgaoJulgadorRevisor() == null) {
				OrgaoJulgadorColegiadoOrgaoJulgador ojcOrgaoJulgador = getOrgaoJulgadorColegiadoOrgaoJulgadorDAO().recuperarPorOrgaoJulgadorColegiadoEhOrgaoJulgador(getProcessoTrf().getOrgaoJulgadorColegiado(), getProcessoTrf().getOrgaoJulgador());
			
				if (ojcOrgaoJulgador != null) {
					getProcessoTrf().setOrgaoJulgadorRevisor(ojcOrgaoJulgador.getOrgaoJulgadorRevisor() != null ? ojcOrgaoJulgador.getOrgaoJulgadorRevisor().getOrgaoJulgador() : null);
				}
			}
		}
		else {
			getProcessoTrf().setOrgaoJulgadorRevisor(null);
		}
	}
	
	@Create
	public void inicializar() {		
		onClickExigeRevisor();
	}

	@Override
	public void validar(String transicaoSelecionada, Validador validador) {	
		validador.isNull(getProcessoTrf().getExigeRevisor(), "Por Favor, informe o se o processo exige Revisor");
		validador.isTrue(getProcessoTrf().getExigeRevisor() == Boolean.TRUE && getProcessoTrf().getOrgaoJulgadorRevisor() == null, "Por Favor, informe o Revisor do Processo");
	}

	@Override
	public void movimentar(String transicaoSelecionada) throws Exception {
		processoTrf = processoTrfManager.update(getProcessoTrf());		
	}
}