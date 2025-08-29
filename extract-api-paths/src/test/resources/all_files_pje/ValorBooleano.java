package br.com.infox.editor.interpretadorDocumento;

public class ValorBooleano extends Valor {
    Boolean valor = false;

    public ValorBooleano(Boolean valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        String texto = "";
        if (valor != null)
            texto = valor.toString();
        return texto;
    }

    @Override
    public String getValorExtenso() {
        return getValor();
    }
       
}
