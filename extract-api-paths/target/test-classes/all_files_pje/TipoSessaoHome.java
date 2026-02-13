package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoSessao;

@Name("tipoSessaoHome")
@BypassInterceptors
public class TipoSessaoHome extends AbstractTipoSessaoHome<TipoSessao> {

	private static final long serialVersionUID = 1L;

	@Override
	public void setTipoSessaoIdTipoSessao(Integer id) {
		setId(id);
	}

	@Override
	public Integer getTipoSessaoIdTipoSessao() {
		return (Integer) getId();
	}

	public static TipoSessaoHome instance() {
		return ComponentUtil.getComponent("tipoSessaoHome");
	}

}