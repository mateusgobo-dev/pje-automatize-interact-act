package br.jus.je.pje.entity.vo;

import java.math.BigInteger;

public class CaixaAdvogadoProcuradorVO {
	
	private int id;
	private String nome;
	private String descricao;
	private boolean ativo;
	private boolean admin;
	private BigInteger contador;
	private int idJurisdicao;
	private String nomeJurisdicao;
	private JurisdicaoVO jurisdicao;
	
	// indica que esta é a caxia padrão da jurisdição - pode ser considerada como uma caixa de entrada
	private boolean padrao;

	public CaixaAdvogadoProcuradorVO() { }

	public CaixaAdvogadoProcuradorVO(int id, String nome, String descricao, 
			int idJurisdicao, String nomeJurisdicao, boolean admin, boolean ativo, BigInteger contador) {
		this(id, nome, descricao, idJurisdicao, nomeJurisdicao, admin, ativo, Boolean.FALSE, contador);
	}

	public CaixaAdvogadoProcuradorVO(int id, String nome, String descricao, 
			int idJurisdicao, String nomeJurisdicao, boolean admin, boolean ativo, boolean padrao, BigInteger contador) {
		
		this.id = id;
		this.nome = nome;
		this.descricao = descricao;
		this.idJurisdicao = idJurisdicao;
		this.nomeJurisdicao = nomeJurisdicao;
		this.admin = admin;
		this.ativo = ativo;
		this.contador = contador;
		this.padrao = padrao;
		this.jurisdicao = new JurisdicaoVO(idJurisdicao,nomeJurisdicao,admin, contador);
	}

	public int getId() {
		return id;
	}

	public String getNome() {
		return nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public boolean getAtivo() {
		return ativo;
	}

	public boolean getAdmin() {
		return admin;
	}

	public boolean getPadrao() {
		return padrao;
	}

	public BigInteger getContador() {
		return contador;
	}

	public int getIdJurisdicao() {
		return idJurisdicao;
	}

	public String getNomeJurisdicao() {
		return nomeJurisdicao;
	}

	public JurisdicaoVO getJurisdicao() {
		return jurisdicao;
	}
	
	public boolean equals(Object obj) {
	    if (obj == null) return false;
	    if (obj == this) return true;
	    if (!(obj instanceof CaixaAdvogadoProcuradorVO)) return false;
	    CaixaAdvogadoProcuradorVO o = (CaixaAdvogadoProcuradorVO) obj;
	    return o.getId() == this.getId();
	}
}