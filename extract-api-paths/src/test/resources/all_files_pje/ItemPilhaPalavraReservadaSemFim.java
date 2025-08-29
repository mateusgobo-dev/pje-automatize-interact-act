package br.com.infox.editor.interpretadorDocumento;

public class ItemPilhaPalavraReservadaSemFim {
    private String palavra = "";
    private int linha = 0;
    
    public ItemPilhaPalavraReservadaSemFim(String palavra, int linha) {
        this.palavra = palavra;
        this.linha = linha;
    }

    public String getPalavra() {
        return palavra;
    }

    public int getLinha() {
        return linha;
    }

    
}
