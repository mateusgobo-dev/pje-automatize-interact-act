package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;

public class TarefaPendente implements Serializable{

    private static final long serialVersionUID = 1L;
    private Long id;
    private String nome;
    private int quantidadePendente;


    public TarefaPendente() {
    	
    }

    public TarefaPendente(Long id,String nome, int quantidadePendente) {
        this.id = id;
        this.nome = nome;
        this.quantidadePendente = quantidadePendente;
    }


    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public int getQuantidadePendente() {
        return quantidadePendente;
    }
    public void setQuantidadePendente(int quantidadePendente) {
        this.quantidadePendente = quantidadePendente;
    }


    public Long getId() {
        return id;
    }
}