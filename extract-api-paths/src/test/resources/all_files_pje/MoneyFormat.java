/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.itx.util;

public class MoneyFormat {

	private static final String[] unidade = { "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove",
			"dez", "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove" };
	private static final String[] dezena = { "vinte", "trinta", "quarenta", "cinquenta", "sessenta", "setenta",
			"oitenta", "noventa" };
	private static final String[] centena = { "cento", "duzentos", "trezentos", "quatrocentos", "quinhentos",
			"seiscentos", "setecentos", "oitocentos", "novecentos" };
	private static final String[] casaSingular = { "mil", "milhão", "bilhão", "trilhão", "quatrilhão" };
	private static final String[] casaPlural = { "mil", "milhões", "bilhões", "trilhões", "quatrilhões" };

	/** Creates new NumberFormat */
	public MoneyFormat() {
	}

	public String execute(String[] args) {
		if (args.length < 1) {
			return "";
		}
		String parteInteira = "";
		String parteFracionaria = "";
		int posdot = args[0].indexOf('.');
		if (posdot != -1) {
			parteInteira = normalize(args[0].substring(0, posdot));
			parteFracionaria = normalize(args[0].substring(posdot + 1));
		} else {
			parteInteira = normalize(args[0]);
		}

		parteInteira = doExtenso(parteInteira);
		parteFracionaria = doExtenso(parteFracionaria);
		if (!parteInteira.equals("")) {
			if (parteInteira.equals("um")) {
				parteInteira += " real";
			} else {
				parteInteira += " reais";
			}
		}

		if (!parteFracionaria.equals("")) {
			if (parteFracionaria.equals("um")) {
				parteFracionaria += " centavo";
			} else {
				parteFracionaria += " centavos";
			}
			if (!parteInteira.equals("")) {
				parteFracionaria = " e " + parteFracionaria;
			}
		}

		return parteInteira + parteFracionaria;
	}

	private String doExtenso(String number) {
		int resto = normalize(number).length() % 3;
		int quociente = number.length() / 3;
		int numIteracoes = (resto == 0) ? quociente : quociente + 1;
		StringBuffer result = new StringBuffer();

		for (int i = 0; i < numIteracoes; i++) {
			String str = "";

			int tam = (resto != 0 && i == 0) ? resto : 3;
			str = number.substring(0, tam);
			number = number.substring(tam, number.length());
			if (str.length() == 1) {
				result.append(escreverUnidade(str));
			} else if (str.length() == 2) {
				result.append(escreverDezena(str));
			} else if (str.length() == 3) {
				result.append(escreverCentena(str));
			}
			if (i < numIteracoes - 1 && !str.equals("000")) {
				result.append(" ");
				int it = numIteracoes - 2 - i;
				if (Integer.parseInt(str) == 1)
					result.append(casaSingular[it]);
				else
					result.append(casaPlural[it]);
				if (Integer.parseInt(number) > 0) {
					result.append(" e ");
				}
			}
		}
		return result.toString().trim();
	}

	private String normalize(String number) {
		StringBuffer sb = new StringBuffer(number);
		while (sb.length() > 0 && sb.charAt(0) == '0') {
			sb.deleteCharAt(0);
		}
		return sb.toString();
	}

	private String escreverUnidade(String str) {
		return unidade[Integer.parseInt(str) - 1];
	}

	private String escreverDezena(String str) {
		StringBuffer sb = new StringBuffer();

		if (str.startsWith("1")) {
			sb.append(escreverUnidade(str));
		} else {
			if (!str.startsWith("0")) {
				int ind = Integer.parseInt(str.substring(0, 1));
				sb.append(dezena[ind - 2]);
			}
			if (str.charAt(1) != '0') {
				int ind = Integer.parseInt(str.substring(1, 2));
				if (!str.startsWith("0")) {
					sb.append(" e ");
				}
				sb.append(unidade[ind - 1]);
			}
		}

		return sb.toString();
	}

	private String escreverCentena(String str) {
		StringBuffer sb = new StringBuffer();
		if (str.equalsIgnoreCase("100")) {
			sb.append("cem");
		} else {
			if (!str.startsWith("0")) {
				int ind = Integer.parseInt(str.substring(0, 1));
				sb.append(centena[ind - 1]);
			}
			if (!str.endsWith("00")) {
				if (!str.startsWith("0")) {
					sb.append(" e ");
				}
				sb.append(escreverDezena(str.substring(1, 3)));
			}
		}
		return sb.toString();
	}

	public static void main(String args[]) {
		String numeros[] = { "123", "1", "1.01", "2.00", "123456789.99", "0.01" };
		MoneyFormat mf = new MoneyFormat();
		String numero[] = new String[1];
		for (int i = 0; i < numeros.length; i++) {
			numero[0] = numeros[i];
			System.out.println(mf.execute(numero));
		}
	}
}
