package br.com.infox.editor.interpretadorDocumento;

public class ItemPilhaPARA {
    private Valor valorIdentificador = new ValorTexto("");
    private int indicePARA = 0;
    
    public ItemPilhaPARA(Valor valorIdentificador) {
        this.valorIdentificador = valorIdentificador;
    }

    public Valor getValorIdentificador() {
        return valorIdentificador;
    }

    public int getIndicePARA() {
        return indicePARA;
    }

    public void setIndicePARA(int indicePARA) {
        this.indicePARA = indicePARA;
    }
    
}
