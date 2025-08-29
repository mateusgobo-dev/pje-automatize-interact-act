package br.jus.csjt.pje.commons.model;

import java.util.Date;

public interface IPessoaFisicaCertificado {

	void setValidado(Boolean validado);

	Boolean getValidado();

	void setDataValidacao(Date dataValidacao);

	Date getDataValidacao();
}
