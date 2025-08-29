package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoSituacaoJulgamento;
import br.jus.pje.nucleo.enums.ContextoEnum;

@Name("tipoSituacaoJulgamentoHome")
@BypassInterceptors
public class TipoSituacaoJulgamentoHome extends AbstractHome<TipoSituacaoJulgamento> {

	private static final long serialVersionUID = 1L;

	public void setTipoSituacaoJulgamentoIdTipoSituacaoJulgamento(Integer id) {
		setId(id);
	}

	public Integer getTipoSituacaoJulgamentoIdTipoSituacaoJulgamento() {
		return (Integer) getId();
	}

	public static TipoSituacaoJulgamentoHome instance() {
		return ComponentUtil.getComponent("tipoSituacaoJulgamentoHome");
	}

	public ContextoEnum[] getContextoEnumValues() {
		return ContextoEnum.values();
	}

}