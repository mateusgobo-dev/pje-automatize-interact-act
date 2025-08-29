package br.jus.cnj.pje.view.fluxo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.pje.manager.ProcessoTrfManager;
import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.dao.OrgaoJulgadorColegiadoOrgaoJulgadorDAO;
import br.jus.cnj.fluxo.Validador;
import br.jus.cnj.fluxo.interfaces.TaskVariavelAction;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.ComposicaoJulgamentoEnum;

@Name(DefinirComposicaoJulgamentoAction.NAME)
@Scope(ScopeType.CONVERSATION)
public class DefinirComposicaoJulgamentoAction implements Serializable, TaskVariavelAction {
	
	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "definirComposicaoJulgamentoAction";
	
	@In(create = true)
	private ProcessoJudicialAction processoJudicialAction;
		
	@In
	private ProcessoTrfManager processoTrfManager;

	private ProcessoTrf processoTrf;

	private List<ComposicaoJulgamentoEnum> composicaoJulgamentoItens;
		
	public ProcessoTrf getProcessoTrf() {	
		if (processoTrf == null) {
			processoTrf = processoJudicialAction.getProcessoJudicial();
		}
		return processoTrf;
	}

	public OrgaoJulgadorColegiadoOrgaoJulgadorDAO getOrgaoJulgadorColegiadoOrgaoJulgadorDAO() {
		return ComponentUtil.getComponent(OrgaoJulgadorColegiadoOrgaoJulgadorDAO.NAME);
	}
	
	public List<ComposicaoJulgamentoEnum> getComposicaoJulgamentoItens() {
		
		if (composicaoJulgamentoItens == null) {
			composicaoJulgamentoItens = Arrays.asList(ComposicaoJulgamentoEnum.values());
		}
		
		return composicaoJulgamentoItens;
	}

	@Override
	public void validar(String transicaoSelecionada, Validador validador) {		
		validador.isNull(getProcessoTrf().getComposicaoJulgamento(), "Por Favor, informe a composição de julgamento do Processo!");
	}

	@Override
	public void movimentar(String transicaoSelecionada) throws Exception {
		processoTrf = processoTrfManager.update(getProcessoTrf());
	}
}