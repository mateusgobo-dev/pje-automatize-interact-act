package br.jus.cnj.pje.visao.beans;

import br.jus.cnj.pje.view.EntityDataModel;
import br.jus.pje.nucleo.entidades.Pessoa;

public class ConsultaPessoaBean {

	private boolean controleExibicao;
	private String textoPesquisa;
	private EntityDataModel<Pessoa> pessoas;

	public void inverterExibicao() {
		controleExibicao = !controleExibicao;
		textoPesquisa = null;
		pessoas = null;
	}
	
	public boolean isControleExibicao() {
		return controleExibicao;
	}
	public void setControleExibicao(boolean controleExibicao) {
		this.controleExibicao = controleExibicao;
	}
	public EntityDataModel<Pessoa> getPessoas() {
		return pessoas;
	}
	public void setPessoas(EntityDataModel<Pessoa> pessoas) {
		this.pessoas = pessoas;
	}
	public String getTextoPesquisa() {
		return textoPesquisa;
	}
	public void setTextoPesquisa(String textoPesquisa) {
		this.textoPesquisa = textoPesquisa;
	}
	
}
