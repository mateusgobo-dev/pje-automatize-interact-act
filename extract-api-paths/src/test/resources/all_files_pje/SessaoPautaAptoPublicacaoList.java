package br.com.infox.pje.list;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;

/**
 * Classe responsável pela aba 'Aptos para publicacao' da relação de julgamento.
 * @author lourival
 */
@Name(SessaoPautaAptoPublicacaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.EVENT)
public class SessaoPautaAptoPublicacaoList extends AbstractSessaoPautaList<SessaoPautaProcessoTrf> {

	private static final long serialVersionUID = 4354467512304016065L;
	public static final String NAME = "sessaoPautaAptoPublicacaoList";

	public SessaoPautaAptoPublicacaoList() {
		super();
	}
	
	@Override
	protected String getComponentName() {
		return SessaoPautaAptoPublicacaoList.NAME;
	}
	
}