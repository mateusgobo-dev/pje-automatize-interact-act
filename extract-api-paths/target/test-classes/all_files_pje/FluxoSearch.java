package br.com.infox.cliente.entity.search;

public class FluxoSearch {
	
	private String codFluxo;
	private String fluxo;
	private String tarefa;
	private String expressao;
	private Boolean ativo;
	
	public String getCodFluxo() {
		return codFluxo;
	}
	
	public void setCodFluxo(String codFluxo) {
		this.codFluxo = codFluxo;
	}
	
	public String getFluxo() {
		return fluxo;
	}
	
	public void setFluxo(String fluxo) {
		this.fluxo = fluxo;
	}

	public String getTarefa() {
		return tarefa;
	}

	public void setTarefa(String tarefa) {
		this.tarefa = tarefa;
	}

	public String getExpressao() {
		return expressao;
	}

	public void setExpressao(String expressao) {
		this.expressao = expressao;
	}

	public Boolean getAtivo() {
		return ativo;
	}
	
	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

}
