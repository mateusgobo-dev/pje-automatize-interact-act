/**
 * pje-comum
 * Copyright (C) 2009-2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.pje.jt.enums;

public enum ValorPesoEnum {

		
	A1("1.0") {
		@Override
		public Double getValor() {
			return 1.0;
		}
	},
	A0("0") {
		@Override
		public Double getValor() {
			return 0.0;
		}
	} , 
	A05("0.5") {
		@Override
		public Double getValor() {
			return 0.5;
		}
	},
	A2("2.0") {
		@Override
		public Double getValor() {
			return 2.0;
		}
	},
	A3("3.0") {
		@Override
		public Double getValor() {
			return 3.0;
		}
	},
	A4("4.0") {
		@Override
		public Double getValor() {
			return 4.0;
		}
	},
	A5("5.0") {
		@Override
		public Double getValor() {
			return 5.0;
		}
	},
	A6("6.0") {
		@Override
		public Double getValor() {
			return 6.0;
		}
	},
	A7("7.0") {
		@Override
		public Double getValor() {
			return 7.0;
		}
	},
	A8("8.0") {
		@Override
		public Double getValor() {
			return 8.0;
		}
	},
	A9("9.0") {
		@Override
		public Double getValor() {
			return 9.0;
		}
	},
	A10("10.0") {
		@Override
		public Double getValor() {
			return 10.0;
		}
	};
	
	private String label;
	
	ValorPesoEnum(String label) {
		this.label = label;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public void setLabel(String label){
		this.label = label;
	}
	
	abstract public Double getValor(); 
	
	public static ValorPesoEnum getEnum(Double valor){
		for (ValorPesoEnum vpe : ValorPesoEnum.values()) {
			if (Double.parseDouble(vpe.label) == valor)
				return vpe;
		} 
		return null;
	}
}