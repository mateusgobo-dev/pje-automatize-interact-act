package br.jus.csjt.pje.view.action;

import java.io.Serializable;

import br.com.infox.cliente.util.ParametroUtil;

public class ConsultaDebitoTrabalhista implements Serializable {

	private static final long serialVersionUID = 1L;

	private String cpf;
	private String cnpj;
	private String nomeParte;
	private Boolean inPesquisa = false;
	// Inicio dos campos que compõe o número do processo
	private Integer numeroSequencia;
	private Integer numeroDigitoVerificador;
	private Integer ano;
	private Integer numeroOrigemProcesso;
    private Integer numeroOrgaoJustica;
    private String respectivoTribunal;

	// Fim dos campos que compõe o número do processo

	public Integer getNumeroSequencia() {
		return numeroSequencia;
	}

	public void setNumeroSequencia(Integer numeroSequencia) {
		this.numeroSequencia = numeroSequencia;
	}

	public Integer getNumeroDigitoVerificador() {
		return numeroDigitoVerificador;
	}

	public void setNumeroDigitoVerificador(Integer numeroDigitoVerificador) {
		this.numeroDigitoVerificador = numeroDigitoVerificador;
	}

	public Integer getAno() {
		return ano;
	}

	public void setAno(Integer ano) {
		this.ano = ano;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public Integer getNumeroOrigemProcesso() {
		return numeroOrigemProcesso;
	}

	public void setNumeroOrigemProcesso(Integer numeroOrigemProcesso) {
		this.numeroOrigemProcesso = numeroOrigemProcesso;
	}

	public Boolean getInPesquisa() {
		return inPesquisa;
	}

	public void setInPesquisa(Boolean inPesquisa) {
		this.inPesquisa = inPesquisa;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getCnpj() {
		return cnpj;
	}

	public void setCnpj(String cnpj) {
		this.cnpj = cnpj;
	}
	
	public String getRespectivoTribunal(){
		return respectivoTribunal;
	}
	
	public void setRespectivoTribunal(String respectivoTribunal) {       
        this.respectivoTribunal = respectivoTribunal;
        if(respectivoTribunal != null && !"".equals(respectivoTribunal)){
            String numeroOrgaoJusticaStr = ParametroUtil.getParametro("numeroOrgaoJustica").substring(0, 1) + respectivoTribunal;           
            setNumeroOrgaoJustica(Integer.parseInt(numeroOrgaoJusticaStr));
        } else{
            setNumeroOrgaoJustica(null);
        }
    }   
	   
    public void setNumeroOrgaoJustica(Integer numeroOrgaoJustica) {
        this.numeroOrgaoJustica = numeroOrgaoJustica;
    }

    public Integer getNumeroOrgaoJustica() {
        return numeroOrgaoJustica;
    }
}
