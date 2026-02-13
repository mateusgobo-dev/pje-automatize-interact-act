package br.com.infox.cliente.home;

import org.jboss.seam.Component;

import br.com.infox.ibpm.home.LocalizacaoHome;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.PessoaLocalizacao;

public abstract class AbstractPessoaLocalizacaoHome<T> extends AbstractHome<PessoaLocalizacao> {

	private static final long serialVersionUID = 1L;

	public void setPessoaLocalizacaoIdPessoaLocalizacao(Integer id) {
		setId(id);
	}

	public Integer getPessoaLocalizacaoIdPessoaLocalizacao() {
		return (Integer) getId();
	}

	@Override
	protected PessoaLocalizacao createInstance() {
		PessoaLocalizacao pessoaLocalizacao = new PessoaLocalizacao();

		PessoaAdvogadoHome pessoaHome = (PessoaAdvogadoHome) Component.getInstance("pessoaAdvogadoHome", false);
		if (pessoaHome != null) {
			pessoaLocalizacao.setPessoa(pessoaHome.getDefinedInstance().getPessoa());
		}

		LocalizacaoHome localizacaoHome = (LocalizacaoHome) Component.getInstance("localizacaoHome", false);
		if (localizacaoHome != null) {
			pessoaLocalizacao.setLocalizacao(localizacaoHome.getDefinedInstance());
		}

		return pessoaLocalizacao;
	}

	@Override
	public String remove(PessoaLocalizacao obj) {
		setInstance(obj);
		String ret = super.remove();
		newInstance();
		refreshGrid("pessoaLocalizacaoGrid");
		return ret;
	}

	@Override
	public String persist() {
		String action = super.persist();
		newInstance();
		return action;
	}

}