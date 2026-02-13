package br.com.infox.editor.interpretadorDocumento;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class TipoValor {
    public static Valor converteValor(Object valor) throws TipoValorInvalidoException {
    	if (valor == null) {
    		throw new TipoValorInvalidoException(NullPointerException.class);
    	}
        Valor objeto = null;
        if (valor instanceof Boolean)
            objeto = new ValorBooleano((Boolean) valor);
        if (valor instanceof Date)
            objeto = new ValorData((Date) valor);
        else if (valor instanceof Integer)
            objeto = new ValorInteiro((Integer) valor);
        else if (valor instanceof Double)
            objeto = new ValorReal((Double) valor);
        else if (valor instanceof String)
            objeto = new ValorTexto((String) valor);
        else if (valor instanceof List)
            objeto = new ValorLista((List) valor);
        else if (valor instanceof Object[]) {
            Object[] auxValor = (Object[]) valor;
            objeto = new ValorLista(Arrays.asList(auxValor));
        }
        else if (valor instanceof Map) {
            objeto = new ValorEstruturado((Map) valor);
        } else if (valor instanceof Object) {
        	objeto = new ValorTexto(valor.toString());
        }
        if (objeto == null)
            throw new TipoValorInvalidoException(valor.getClass());
        return objeto;
    }
}
