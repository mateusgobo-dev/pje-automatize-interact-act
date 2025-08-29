package br.com.infox.editor.interpretadorDocumento;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ValorData extends Valor {
    Date valor = null;

    public ValorData(Date valor) {
        this.valor = valor;
    }

    @Override
    public String getValor() {
        String data = "";
        if (valor != null)
            data = valor.toString();
        return data;
    }
    
    public String getValorExtenso() {
        String data = "";
        if (valor != null) {
            String meses[] = {"janeiro", "fevereiro", "março", "abril",
                            "maio", "junho", "julho", "agosto", 
                            "setembro", "outubro", "novembro", "dezembro"};
            
            SimpleDateFormat sdfDia = new SimpleDateFormat("dd");
            SimpleDateFormat sdfMes = new SimpleDateFormat("MM");
            SimpleDateFormat sdfAno = new SimpleDateFormat("yyyy");
            
            data = sdfDia.format(valor) + " de " + 
                   meses[Integer.parseInt(sdfMes.format(valor))] + " de " + 
                   sdfAno.format(valor);
        }
        return data;
    }
    
}
