package br.jus.cnj.pje.util.formatadorLista;

import java.util.List;

public class AtributoLista implements Atributo {
	
	private String key;
	private String property;
	private FormatadorLista formatador;
	
	public AtributoLista(String key, String property, FormatadorLista formatador) {
		this.key = key;
		this.property = property;
		this.formatador = formatador;
	}
	
	@Override
	public String getKey() {
		return this.key;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public String eval(Object obj) {
		formatador.setLista((List)PropUtils.getProperty(obj, property));
		return formatador.toString();
	}

}
