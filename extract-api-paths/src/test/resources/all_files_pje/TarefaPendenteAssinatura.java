package br.jus.cnj.pje.webservice.controller.painelUsuarioInterno.dto;

import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;

import java.io.Serializable;


public class TarefaPendenteAssinatura implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private TipoProcessoDocumento tipoDocumento;
    private int quantidade;

    public TipoProcessoDocumento getTipoDocumento() {
        return tipoDocumento;
    }
    public void setTipoDocumento(TipoProcessoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    public int getQuantidade() {
        return quantidade;
    }
    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public TarefaPendenteAssinatura(TipoProcessoDocumento tipoProcessoDocumento, int quantidade) {
        this.tipoDocumento = tipoProcessoDocumento;
        this.quantidade = quantidade;
    }
}