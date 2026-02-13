package br.com.infox.cliente.home;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;

@Name("pessoaLocalizacaoHome")
@BypassInterceptors
public class PessoaLocalizacaoHome extends AbstractPessoaLocalizacaoHome<PessoaLocalizacao> {

	private static final long serialVersionUID = 1L;

	@Override
	protected boolean beforePersistOrUpdate() {
		PessoaJuridica pessoaJuridica = PessoaJuridicaHome.instance().getInstance();
		// TODO Verificar porque a comparacao pessoaJuridica != null nao esta
		// trazendo o valor correto.
		if (pessoaJuridica.getAtivo() != null) {
			getInstance().setPessoa(pessoaJuridica);
			refreshGrid("entidadeLocalizacaoGrid");
		}
		return super.beforePersistOrUpdate();
	}

	@Override
	public String remove(PessoaLocalizacao obj) {
		String remove = super.remove(obj);
		refreshGrid("entidadeLocalizacaoGrid");
		return remove;
	}

}