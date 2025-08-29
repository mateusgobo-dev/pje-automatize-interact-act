package br.jus.pje.nucleo.enums;

import java.util.ArrayList;
import java.util.List;

public enum ExigibilidadeAssinaturaEnum implements PJeEnum{
 	
 	/**
 	 * Define a assinatura de documentos como facultativo 
 	 */
 	F("FACULTATIVO"),
 	
 	/**
 	 * Define a assinatura de documentos como obrigatória 
 	 */
 	O("OBRIGATORIO"), 
 	
 	/**
 	 * Define a assinatura de documentos como suficiente 
 	 */
 	S("SUFICIENTE"), 
 	
 	/**
 	 * Define a assinatura de documentos como não necessária 
 	 */
 	N("SEM ASSINATURA");
 	
 	private String label;
 
 	ExigibilidadeAssinaturaEnum(String label) {
 		this.label = label;
 	}
 	
 	public boolean isSemAssinatura(){
 		return this.equals(N);
 	}
 	
 	public boolean isObrigatorio(){
 		return this.equals(O);
 	}
 	
 	public boolean isSuficiente(){
 		return this.equals(S);
 	}
 	
 	public boolean isFacultativo(){
 		return this.equals(F);
 	}
 	
 	public static List<ExigibilidadeAssinaturaEnum> getListaPermiteAssinar(){
 		List<ExigibilidadeAssinaturaEnum> lista = new ArrayList<ExigibilidadeAssinaturaEnum>();
 		lista.add(ExigibilidadeAssinaturaEnum.O);
 		lista.add(ExigibilidadeAssinaturaEnum.S);
 		lista.add(ExigibilidadeAssinaturaEnum.F);
 		
 		return lista;
 	}
 	
 	public static List<ExigibilidadeAssinaturaEnum> getListaNaoPermiteAssinar(){
 		List<ExigibilidadeAssinaturaEnum> lista = new ArrayList<ExigibilidadeAssinaturaEnum>();
 		lista.add(ExigibilidadeAssinaturaEnum.N);
 		
 		return lista;
 	}
 	
 	@Override
 	public String getLabel() {
 		return this.label;
 	}
 
 	
}