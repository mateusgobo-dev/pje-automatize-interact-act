package br.com.infox.cliente.home;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.CertidaoPessoa;

public abstract class AbstractCertidaoPessoaHome<T> extends AbstractHome<CertidaoPessoa> {

	private static final long serialVersionUID = 1L;

	public void setCertidaoPessoaIdCertidaoPessoa(Integer id) {
		setId(id);
	}

	public Integer getCertidaoPessoaIdCertidaoPessoa() {
		return (Integer) getId();
	}
}