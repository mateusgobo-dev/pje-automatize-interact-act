package br.jus.cnj.pje.util.formatadorLista;

import java.util.List;

public class FiltroTeste {
	
	public static final int EQUALS     = 1;
	public static final int NOT_EQUALS = 2;
	public static final int NULL       = 3;
	public static final int NOT_NULL   = 4;
	public static final int EMPTY      = 5;
	public static final int NOT_EMPTY  = 6;
	
	private int operacao;
	private String property;
	private String value;
	
	public FiltroTeste(int operacao, String property, String value) {
		this(operacao, property);
		this.value = value;
	}
	
	public FiltroTeste(int operacao, String property) {
		this.operacao = operacao;
		this.property = property;
	}
	
	@SuppressWarnings("rawtypes")
	public boolean eval(Object obj) {
		Object value2 = PropUtils.getProperty(obj, property);
		
		switch (operacao) {
			case EQUALS:
				return (value2 != null && value2.toString().equals(value));
				
			case NOT_EQUALS:
				return (value2 != null && !value2.toString().equals(value));

			case NULL:
				return value2 == null;

			case NOT_NULL:
				return value2 != null;

			case EMPTY:
				if(value2 == null) return true;
				if(value2 instanceof List) return ((List)value2).isEmpty();
				return value2.toString().isEmpty();

			case NOT_EMPTY:
				if(value2 == null) return false;
				if(value2 instanceof List) return !((List)value2).isEmpty();
				return !value2.toString().isEmpty();
		}
		
		return true;
	}
	
	
}
