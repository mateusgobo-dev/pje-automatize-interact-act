package br.jus.cnj.pje.util;


public class FlexaoNumeroGenero{
	private String singularMasculino;
	private String singularFeminino;
	private String pluralMasculino;
	private String pluralFeminino;
	
	
	public static void main(String[] args) {
		FlexaoNumeroGenero flexao = new FlexaoNumeroGenero("Ju{iz/íza/ízes/ízas} Substitut{o/a/os/as} em 2 grau");
		
		System.out.println(flexao.getFlexao(TipoFlexaoNumeroGeneroEnum.SM));
		System.out.println(flexao.getFlexao(TipoFlexaoNumeroGeneroEnum.SF));
		System.out.println(flexao.getFlexao(TipoFlexaoNumeroGeneroEnum.PM));
		System.out.println(flexao.getFlexao(TipoFlexaoNumeroGeneroEnum.PF));
	}
	
	
	public FlexaoNumeroGenero(String frase){
		flexionarFrase(frase, false);
	}
	
	public FlexaoNumeroGenero(String frase, boolean flexionarSomenteGenero){
		flexionarFrase(frase, flexionarSomenteGenero);
		
	}
	
	private void flexionarFrase(String frase, boolean flexionarSomenteGenero){
		String[] flexoes = flexionarOpcoesGenerosNumeros(frase, flexionarSomenteGenero);
		
		this.singularMasculino = flexoes[0];
		this.singularFeminino = flexoes[1];
		this.pluralMasculino = flexoes[2];
		this.pluralFeminino = flexoes[3];
	}
	
	private String[] flexionarOpcoesGenerosNumeros(String frase, boolean flexionarSomenteGenero){
		String[] frasesFlexionadas  = new String[]{"","","",""};
		
		int qtdFlexoes = flexionarSomenteGenero ? 2 : 4;
		
		int i=0;
		while (i < frase.length()){
			char charactere = frase.charAt(i);
			if (charactere == '{'){
				String bloco = frase.substring(i+1, frase.indexOf('}', i));
				String[] flexoes = bloco.split("/");
				for (int j=0; j < qtdFlexoes; j++){
					frasesFlexionadas[j] += flexoes[j];
				}
				i += bloco.length()+2;
			}
			else{
				for (int j=0; j<qtdFlexoes; j++){
					frasesFlexionadas[j] += charactere; 
				}
				i++;
			}
		}
		
		for (i = 0; i<frasesFlexionadas.length; i++){
			if (frasesFlexionadas[i].equals("")){
				frasesFlexionadas[i] = "Flexão indisponível no plural";
			}
		}
		
		return frasesFlexionadas;
	}
	
	
	
	public String getFlexao(TipoFlexaoNumeroGeneroEnum tipoFlexaoNumeroGenero){
		switch (tipoFlexaoNumeroGenero) {
		case SM:
			return singularMasculino;
		case SF:
			return singularFeminino;
		case PM:
			return pluralMasculino;
		case PF:
			return pluralFeminino;
		default:
			return "";
		}
	}
}
