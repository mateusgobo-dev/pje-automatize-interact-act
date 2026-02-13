package br.jus.cnj.pje.util.formatadorLista;


public class AtributoExterno implements Atributo {
	
	private String key;
	private Object bean;
	private String methodName;
	private String property;
	private Object[] params;
	
	public AtributoExterno(String key, Object bean, String methodName, String property, Object[] params) {
		this.key = key;
		this.bean = bean;
		this.methodName = methodName;
		this.property = property;
		this.params = params;
	}
	
	@Override
	public String getKey() {
		return this.key;
	}
	
	@Override
	public String eval(Object obj) {
		try {
			Object[] paramList  = new Object[params.length];
			for(int i=0 ; i<params.length ; i++) {
				
				if(params[i] == null) {
					paramList[i] = null;
				} else if(params[i] instanceof String) {
					if(((String)params[i]).startsWith("@")) {
						paramList[i] = ((String)params[i]).substring(1);
					} else if(params[i].equals("null")) {
						paramList[i] = null;
					} else {
						paramList[i] = PropUtils.getProperty(obj, (String)params[i]);
					}
				} else {
					paramList[i]  = params[i];
				}
				
			}
			Object result = ReflectionUtils.invoke(bean, methodName, paramList);
			
			if(result == null) return "";
			
			if(property != null && property.length() > 0) {
				return PropUtils.getPropertyString(result, property); 
			} else {
				return result.toString();
			}
		} catch (Exception e) {
			throw new IllegalArgumentException(e);
		}
	}

}
