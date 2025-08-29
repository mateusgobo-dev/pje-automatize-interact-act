package br.com.infox.editor.interpretadorDocumento;

public abstract class Valor {
    
    public abstract String getValor();
    public abstract String getValorExtenso();

    @Override
    public String toString() {
        return getValor();
    }
}
