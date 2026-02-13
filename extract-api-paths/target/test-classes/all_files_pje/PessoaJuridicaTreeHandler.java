package br.com.infox.cliente.component.tree;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.PessoaJuridica;

@Name("pessoaJuridicaTree")
@BypassInterceptors
public class PessoaJuridicaTreeHandler extends AbstractTreeHandler<PessoaJuridica> {

	private static final long serialVersionUID = 1L;

	@Override
	protected String getQueryRoots() {
		return "select n from PessoaJuridica n " + "where n.tipoPessoa.idTipoPessoa = '8' " + "order by nome";
	}

	@Override
	protected String getQueryChildren() {
		return null;
	}

	@Override
	protected PessoaJuridica getEntityToIgnore() {
		return ComponentUtil.getInstance("pessoaJuridicaHome");
	}

}
