package br.com.infox.pje.list;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;

/**
 * Classe responsável pela aba 'Relação de julgamento'.
 * O conteudo dessa classe foi motivo para a classe AbstractSessaoPautaList pois o mesmo
 * código e idêntico ao utilizado em outras abas.
 * @see AbstractSessaoPautaList
 * @author lourival
 */
@Name(SessaoPautaRelacaoJulgamentoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoPautaRelacaoJulgamentoList extends AbstractSessaoPautaList<SessaoPautaProcessoTrf> {

	private static final long serialVersionUID = 3946701511228265186L;

	public static final String NAME = "sessaoPautaRelacaoJulgamentoList";

	public SessaoPautaRelacaoJulgamentoList() {
		super();
	}

	@Override
	protected String getComponentName() {
		return SessaoPautaRelacaoJulgamentoList.NAME;
	}

}
