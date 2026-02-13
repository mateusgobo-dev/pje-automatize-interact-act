package br.com.jt.pje.action;

import java.util.Date;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;
import br.com.jt.pje.list.RemanescentesList;
import br.com.jt.pje.manager.PautaSessaoManager;
import br.jus.pje.jt.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(AbaRemanescentesAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaRemanescentesAction extends AbstractPautaAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7691648412482498714L;

	public static final String NAME = "abaRemanescentesAction";
	
	@In
	private PautaSessaoManager pautaSessaoManager;

	public Date getDataUltimaSessao(ProcessoTrf row){
		return pautaSessaoManager.getDataUltimaSessaoByProcesso(row);
	}

	@Override
	protected TipoInclusaoEnum getTipoInclusaoEnum() {
		return TipoInclusaoEnum.RE;
	}

	@Override
	protected List<ProcessoTrf> getProcessoList() {
		RemanescentesList remanescentesList = ComponentUtil.getComponent("remanescentesList");
		return remanescentesList.getResultList();
	}
	
}
