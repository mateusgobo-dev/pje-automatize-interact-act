package br.com.jt.pje.action;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.list.EmMesaList;
import br.jus.pje.jt.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(AbaEmMesaAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaEmMesaAction extends AbstractPautaAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5028641228022977943L;
	public static final String NAME = "abaEmMesaAction";

	@Override
	protected TipoInclusaoEnum getTipoInclusaoEnum() {
		return TipoInclusaoEnum.ME;
	}

	@Override
	protected List<ProcessoTrf> getProcessoList() {
		EmMesaList mesaList = ComponentUtil.getComponent("emMesaList");
		return mesaList.getResultList();
	}
	
	
}
