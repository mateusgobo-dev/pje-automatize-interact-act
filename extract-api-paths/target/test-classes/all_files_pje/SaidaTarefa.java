package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import java.io.Serializable;

public class SaidaTarefa implements Serializable{

    private String nomeSaida;
    private Long idTarefa;

    public String getNomeSaida() {
        return nomeSaida;
    }

    public void setNomeSaida(String nomeSaida) {
        this.nomeSaida = nomeSaida;
    }

    public Long getIdTarefa() {
        return idTarefa;
    }

    public void setIdTarefa(Long idTarefa) {
        this.idTarefa = idTarefa;
    }
}
