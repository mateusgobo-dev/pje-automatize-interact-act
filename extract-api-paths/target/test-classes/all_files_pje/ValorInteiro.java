package br.com.infox.editor.interpretadorDocumento;

public class ValorInteiro extends Valor {
    Integer valor = 0;

    public ValorInteiro(Integer valor) {
        this.valor = valor;
    }

     private Integer getNumero() {
        Integer numero = 0;
        if (valor != null)
            numero = valor;
        return numero;
    }

    @Override
    public String getValor() {
        return getNumero().toString();
    }

    @Override
    public String getValorExtenso() {
        JExtenso extenso = new JExtenso(getNumero().intValue());
        return extenso.toString();
    }
}
