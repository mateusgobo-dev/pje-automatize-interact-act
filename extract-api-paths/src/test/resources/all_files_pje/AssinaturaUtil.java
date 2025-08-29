package br.com.itx.util;

public final class AssinaturaUtil {
	
	public static boolean isModoTeste(String assinatura){
		
		if (assinatura.equals("AAAAAAAAAAAAAAAAAAAAAAAAAA") || 
			assinatura.equals("assinaturadeteste") 			|| 
			assinatura.equals("QkJCQkJCQkJCQkJCQkJCQkJCQkI=") ||
			assinatura.equals("QVNTSU5BVFVSQV9NT0RPX1RFU1RF"))
			return true;
		
		return false;
		
	}
}
