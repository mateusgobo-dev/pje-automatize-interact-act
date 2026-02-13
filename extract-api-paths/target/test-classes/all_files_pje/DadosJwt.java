package br.jus.cnj.pje.webservice.mobile;

public class DadosJwt {
	private String codigoPareamento;
	
	public DadosJwt(String codigoPareamento){
		this.codigoPareamento = codigoPareamento;
	}
	
	public String getCodigoPareamento() {
		return codigoPareamento;
	}
	public void setCodigoPareamento(String codigoPareamento) {
		this.codigoPareamento = codigoPareamento;
	}

}
