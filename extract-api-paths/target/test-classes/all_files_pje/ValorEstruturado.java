package br.com.infox.editor.interpretadorDocumento;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ValorEstruturado extends Valor {
    Map<String, Valor> valor = new TreeMap<String, Valor>();
    
    public ValorEstruturado(Map<String, Object> estrutura) throws TipoValorInvalidoException {
        if (estrutura != null) {
            Set<String> atributos = estrutura.keySet();
            for (String atributo : atributos) {
                Valor objeto = TipoValor.converteValor(estrutura.get(atributo));
                if (objeto != null) {
                    atributo = atributo.toLowerCase();
                    this.valor.put(atributo, objeto);
                }
            }
        }
    }
    
    public Map<String, Valor> getEstrutura() {
        return valor;
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
        StringBuilder extenso = new StringBuilder("(");
        if ((valor != null) && (!valor.isEmpty())) {
            Set<String> atributos = valor.keySet();
            for (String atributo : atributos) {
                extenso.append(atributo);
                extenso.append("=");
                extenso.append(valor.get(atributo).getValorExtenso());
                extenso.append(", ");
            }
            extenso = new StringBuilder(extenso.substring(0, extenso.length()-2));
        }
        extenso.append(")");
        return extenso.toString();
    }
    
}
