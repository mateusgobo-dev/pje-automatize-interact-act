package br.com.infox.editor.interpretadorDocumento;

public class ValorTexto extends Valor {
    String valor = "";

    public ValorTexto(String valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        String texto = "";
        if (valor != null)
            texto = valor;
        return texto;
    }

    @Override
    public String getValorExtenso() {
        return getValor();
    }
       
}
