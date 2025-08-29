package br.jus.csjt.pje.view.action;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.util.ComponentUtil;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.pje.nucleo.entidades.ProcessoEvento;

/**
 * Componente Action usado para interface entre a tela de alteração de
 * visibilidade de complementos e seu respectivo service
 * 
 * @since 1.4.2
 * @category PJE-JT
 * @created 2011-08-25
 * @author David, Kelly
 */

@Name(AlteracaoVisibilidadeComplementoAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class AlteracaoVisibilidadeComplementoAction implements Serializable{

	private static final long serialVersionUID = 1948986449291218724L;
	public static final String NAME = "alteracaoVisibilidadeComplementoAction";
	private ProcessoEvento movimentoProcessoSelecionado;

	public ProcessoEvento getMovimentoProcessoSelecionado() {
		return movimentoProcessoSelecionado;
	}

	public void setMovimentoProcessoSelecionado(ProcessoEvento processoEvento) {
		this.movimentoProcessoSelecionado = processoEvento;
	}

	/**
	 * Método utilizado para alteração de visibilidade de complementos
	 */
	public void alterarVisibilidadeComplemento() {
		LancadorMovimentosService lancadorMovimentosService = ComponentUtil
				.getComponent(LancadorMovimentosService.NAME);
		lancadorMovimentosService.alteraVisibilidadeComplementoSegmentado(movimentoProcessoSelecionado);
		FacesMessages.instance().add(Severity.INFO, "Complementos alterados com sucesso!");
	}
}
