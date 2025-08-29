package br.jus.cnj.pje.util.formatadorLista;

public class AtributoSimples implements Atributo {
	
	private String key;
	private String property;
	
	public AtributoSimples(String key, String property) {
		this.key = key;
		this.property = property;
	}
	
	@Override
	public String getKey() {
		return this.key;
	}
	
	@Override
	public String eval(Object obj) {
		String result = null;
		if (obj != null){
			if ("".equals(property)){
				result = obj.toString();
			}
			else{
				result = PropUtils.getPropertyString(obj, property); 
			}
		}	
		return result;
	}

}
