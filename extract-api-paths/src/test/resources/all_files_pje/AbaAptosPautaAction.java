package br.com.jt.pje.action;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.list.AbaAptosPautaJulgamentoList;
import br.jus.pje.jt.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(AbaAptosPautaAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaAptosPautaAction extends AbstractPautaAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8580602626074412101L;
	public static final String NAME = "abaAptosPautaAction";

	@Override
	protected TipoInclusaoEnum getTipoInclusaoEnum() {
		return TipoInclusaoEnum.PA;
	}

	@Override
	protected List<ProcessoTrf> getProcessoList() {
		AbaAptosPautaJulgamentoList aptosList = ComponentUtil.getComponent("abaAptosPautaJulgamentoList");
		return aptosList.getResultList();
	}
	
}
