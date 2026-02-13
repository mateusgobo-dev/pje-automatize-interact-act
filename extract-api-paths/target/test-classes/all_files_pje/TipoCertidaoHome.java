package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.TipoCertidao;

@Name("tipoCertidaoHome")
@BypassInterceptors
public class TipoCertidaoHome extends AbstractTipoCertidaoHome<TipoCertidao> {

	private static final long serialVersionUID = 1L;

	public static TipoCertidaoHome instance() {
		return ComponentUtil.getComponent("tipoCertidaoHome");
	}

	@Override
	public void newInstance() {
		super.newInstance();
	}

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
			setTab("classeTipoCertidao");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return ret;
	}

	@Override
	public String update() {
		String ret = super.update();
		setTab("classeTipoCertidao");
		return ret;
	}

	@Override
	public String remove(TipoCertidao tipo) {
		tipo.setAtivo(Boolean.FALSE);
		setInstance(tipo);
		String ret = super.update();
		newInstance();
		refreshGrid("tipoCertidaoGrid");
		return ret;
	}

	public void setTab() {
		super.setTab("form");
	}

}