package br.com.infox.editor.interpretadorDocumento;

import java.util.LinkedList;
import java.util.List;

public class ValorLista extends Valor {
    List<Valor> valor = new LinkedList<Valor>();

    public ValorLista(List<Object> listaValor) throws TipoValorInvalidoException {
        this.valor.clear();
        if (listaValor != null) {
            for (int i=0; i<listaValor.size(); i++) {
                Valor objeto = TipoValor.converteValor(listaValor.get(i));
                if (objeto != null)
                    this.valor.add(objeto);
            }
        }
    }
    
    public List<Valor> getLista() {
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
        StringBuilder extenso = new StringBuilder();
        if (valor != null) {
            if (valor.size() > 1) {
                for (int i=0; i<valor.size()-1; i++) {
                    extenso.append(valor.get(i).getValorExtenso());
                    extenso.append(", ");
                }
                extenso = new StringBuilder(extenso.substring(0, extenso.length()-2));
                extenso.append(" e ");
                extenso.append(valor.get(valor.size()-1).getValorExtenso());
            }
            else if (!valor.isEmpty())
                extenso.append(valor.get(0).getValorExtenso());
        }
        return extenso.toString();
    }
        
}
