package br.jus.cnj.pje.util.formatadorLista;

import java.util.Comparator;

public class PropertyComparator implements Comparator<Object> {

	private String[] property;
	private boolean[] asc;
	
	public PropertyComparator(String ordem) {
		if(ordem == null || ordem.isEmpty()) throw new IllegalArgumentException("A ordem deve ser passada como parâmetro.");
		
		String[] arr = ordem.split(",");
		this.property = new String[arr.length];
		this.asc      = new boolean[arr.length];
		
		for(int i=0 ; i<arr.length ; i++) {
			String[] arr2 = arr[i].trim().replaceAll("[ ]+", " ").split(" ");
			
			if(arr2.length > 2) throw new IllegalArgumentException("Ordem inválida para o atributo: '" + arr[i].trim().replaceAll("[ ]+", " ") + "'.");
			
			this.asc[i] = true;
			if(arr2.length == 2) {
				this.asc[i] = !arr2[1].toUpperCase().contains("DESC");
			}
			this.property[i] = arr2[0];
		}
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public int compare(Object o1, Object o2) {
		if(o1 == null && o2 == null) return 0;
		if(o1 == null) return 1;
		if(o2 == null) return -1;
		
		for(int i=0 ; i<property.length ; i++) {
			Object oo1 = PropUtils.getProperty(o1, property[i]);
			Object oo2 = PropUtils.getProperty(o2, property[i]);
			
			int res = 0;
			if(oo1 == null && oo2 == null) continue;
			if(oo1 == null) res = 1;
			if(oo2 == null) res = -1;
			
			if(res == 0) {
				if(oo1 instanceof Comparable && oo2 instanceof Comparable) {
					res = ((Comparable)oo1).compareTo(oo2);
				} else {
					res = oo1.toString().compareTo(oo2.toString());
				}
			}
			
			if(res != 0) {
				if(! this.asc[i]) {
					res = -res;
				}
				return res;
			}
		}
		return 0;
	}
	
}
