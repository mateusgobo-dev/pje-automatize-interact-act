package br.com.jt.pje.action;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.jus.pje.jt.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(AbaVotacaoAntecipadaAction.NAME)
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class AbaVotacaoAntecipadaAction extends AbstractPautaAction{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3033289278398463143L;
	public static final String NAME = "abaVotacaoAntecipadaAction";

	@Override
	protected TipoInclusaoEnum getTipoInclusaoEnum() {
		return null;
	}

	@Override
	protected List<ProcessoTrf> getProcessoList() {
		return null;
	}
	
}