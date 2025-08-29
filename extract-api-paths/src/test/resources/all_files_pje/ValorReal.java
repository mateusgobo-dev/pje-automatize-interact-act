package br.com.infox.editor.interpretadorDocumento;

import java.text.DecimalFormat;

public class ValorReal extends Valor {
    Double valor = 0.0;

    public ValorReal(Double valor) {
        this.valor = valor;
    }
    
    public Double getNumero() {
        Double numero = 0.0;
        if (valor != null)
            numero = valor;
        return numero;
    }

    @Override
    public String getValor() {
        DecimalFormat df = new DecimalFormat("R$ ###,###,###,###,###,###,##0.00");
        return df.format(getNumero());
    }

    @Override
    public String getValorExtenso() {
        JExtenso extenso = new JExtenso(getNumero().doubleValue());
        return extenso.toString();
    }
}
