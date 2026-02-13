/**
 * 
 */
package br.jus.cnj.pje.nucleo;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import br.jus.pje.nucleo.util.StringUtil;

/**
 * Classe destinada a permitir a geração de CPFs e CNPJs numericamente válidos,
 * ainda que eles não o sejam na base da Receita Federal.
 * 
 * @author cristof
 * 
 */
public class InscricaoMFUtil {

	public static class InscricaoMF {
		public String tipo;
		public String inscricao;

		public InscricaoMF(String tipo, String inscricao) {
			this.tipo = tipo;
			this.inscricao = inscricao;
		}
	}

	private static final String PADRAO_FORMATACAO_CNPJ = "$1.$2.$3/$4-$5";
	private static final String PADRAO_FORMATACAO_CNPJ_REDUZIDO = "$1.$2.$3";

	private static final Pattern CNPJ_NAO_FORMATADO = Pattern.compile("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})");
	private static final Pattern CNPJ_NAO_FORMATADO_REDUZIDO = Pattern.compile("(\\d{2})(\\d{3})(\\d{3})");

	private static int MAX_CPF = 999999999;
	private static int MAX_CNPJ = 9999999;
	public static final int TAMANHO_CPF = 11;
	public static final int TAMANHO_CNPJ = 14;
	public static final int TAMANHO_CNPJ_COM_MASCARA = 18;
	private static final int TAMANHO_CNPJ_REDUZIDO = 8;

	private static Random random = new Random();

	public static String geraCNPJ() {
		int baseInt = random.nextInt(MAX_CNPJ);
		String baseCNPJ = String.format("%08d", baseInt);
		return getCNPJBase(baseCNPJ);
	}

	public static String geraCPF() {
		int baseInt = random.nextInt(MAX_CPF);
		String baseCPF = String.format("%09d", baseInt);
		return getCPF(baseCPF);
	}

	public static boolean verificaCPF(String cpf) {
		String cpfSemMascara = retiraMascara(cpf);

		if (cpfSemMascara.length() != TAMANHO_CPF) {
			return false;
		}
		String dv = InscricaoMFUtil.obtemDVCPF(cpfSemMascara.substring(0, 9));
		Integer numeroVerificador = Integer.valueOf(dv);
		Integer numeroParadigma = Integer.valueOf(cpfSemMascara.substring(9));
		return numeroVerificador.equals(numeroParadigma);
	}

	public static boolean verificaCNPJ(String cnpj) {
		String cnpjSemMascara = retiraMascara(cnpj);

		if (cnpjSemMascara.length() != TAMANHO_CNPJ) {
			return false;
		}

		String dv = obtemDVCNPJ(cnpjSemMascara.substring(0, 12));
		Integer numeroVerificador = Integer.valueOf(dv);
		Integer numeroParadigma = Integer.valueOf(cnpjSemMascara.substring(12));
		return numeroVerificador.equals(numeroParadigma);
	}

	public static boolean validaCNPJ(String cnpj) {
		if (cnpj == null || cnpj.trim().length() > TAMANHO_CNPJ_COM_MASCARA) {
			return false;
		}

		String cnpjSemMascara = retiraMascara(cnpj);
		if (cnpjSemMascara.length() != TAMANHO_CNPJ) {
			return false;
		}

		String dv = obtemDVCNPJ(cnpjSemMascara.substring(0, 12));
		Integer numeroVerificador = Integer.valueOf(dv);
		Integer numeroParadigma = Integer.valueOf(cnpjSemMascara.substring(12));
		return numeroVerificador.equals(numeroParadigma);
	}

	public static boolean validarCpfCnpj(String cpfOrCnpj) {
		if (cpfOrCnpj == null) {
			return false;
		}

		String n = retiraMascara(cpfOrCnpj);

		boolean isCnpj = n.length() == TAMANHO_CNPJ;
		boolean isCpf = n.length() == TAMANHO_CPF;

		if (!isCpf && !isCnpj) {
			return false;
		}

		int i;
		int j; // just count
		int digit; // A number digit
		int coeficient; // A coeficient
		int sum; // The sum of (Digit * Coeficient)
		int[] foundDv = { 0, 0 }; // The found Dv1 and Dv2
		int dv1 = Integer.parseInt(String.valueOf(n.charAt(n.length() - 2)));
		int dv2 = Integer.parseInt(String.valueOf(n.charAt(n.length() - 1)));
		for (j = 0; j < 2; j++) {
			sum = 0;
			coeficient = 2;
			for (i = n.length() - 3 + j; i >= 0; i--) {
				digit = Integer.parseInt(String.valueOf(n.charAt(i)));
				sum += digit * coeficient;
				coeficient++;
				if (coeficient > 9 && isCnpj)
					coeficient = 2;
			}
			foundDv[j] = 11 - sum % 11;
			if (foundDv[j] >= 10)
				foundDv[j] = 0;
		}
		return dv1 == foundDv[0] && dv2 == foundDv[1];
	}

	public static String obtemDVCPF(String raizCPF) {
		if (raizCPF.length() != 9) {
			throw new IllegalArgumentException("Raiz do CPF com número de dígitos diferente de 9.");
		}
		int soma = 0;
		int multiplicador = 10;
		for (int i = 0; i < 9; i++, --multiplicador) {
			soma += Character.getNumericValue(raizCPF.charAt(i)) * multiplicador;
		}
		int resto = ((soma % 11) < 2) ? 0 : 11 - (soma % 11);
		int digito1 = resto;
		soma = 0;
		multiplicador = 11;
		for (int i = 0; i < 9; i++, multiplicador--) {
			soma += Character.getNumericValue(raizCPF.charAt(i)) * multiplicador;
		}
		soma += digito1 * multiplicador;
		int digito2 = ((soma % 11) < 2) ? 0 : 11 - (soma % 11);
		StringBuilder sb = new StringBuilder();
		sb.append(digito1);
		sb.append(digito2);
		return sb.toString();
	}

	public static String obtemDVCNPJ(String cnpj) {
		int soma = 0;
		for (int i = 0; i < 4; i++) {
			soma = soma + Character.getNumericValue(cnpj.charAt(i)) * (5 - i);
		}
		for (int i = 4; i < 12; i++) {
			soma = soma + Character.getNumericValue(cnpj.charAt(i)) * (13 - i);
		}
		int dv1 = 11 - (soma % 11);
		if (dv1 >= 10) {
			dv1 = 0;
		}
		soma = 0;
		for (int i = 0; i < 5; i++) {
			soma = soma + Character.getNumericValue(cnpj.charAt(i)) * (6 - i);
		}
		for (int i = 5; i < 12; i++) {
			soma = soma + Character.getNumericValue(cnpj.charAt(i)) * (14 - i);
		}
		soma = soma + dv1 * 2;
		int dv2 = 11 - (soma % 11);
		if (dv2 >= 10) {
			dv2 = 0;
		}
		String ret = Integer.toString(dv1) + Integer.toString(dv2);
		return ret;
	}

	public static String retiraMascara(String cpfOrCnpj) {
		if (cpfOrCnpj != null) {
			return cpfOrCnpj.replaceAll("[^0-9]*", "");
		}
		return cpfOrCnpj;
	}

	public static String acrescentaMascaraMF(String inscricao) {
		if (inscricao == null || inscricao.trim().length() == 0) {
			throw new IllegalArgumentException("O número de inscrição informado é nulo.");
		}
		String inscricaoSemMascara = retiraMascara(inscricao);

		if (inscricaoSemMascara.length() == TAMANHO_CPF) {
			return acrescentaMascaraCPF(inscricaoSemMascara);
		} else if (inscricaoSemMascara.length() == TAMANHO_CNPJ_REDUZIDO
				|| inscricaoSemMascara.length() == TAMANHO_CNPJ) {
			return mascaraCnpj(inscricaoSemMascara);
		} else {
			throw new IllegalArgumentException(
					String.format("O número de inscrição informado não é de CPF nem de CNPJ."));
		}
	}

	public static String mascararCpf(String cpf) {
		if (cpf == null || cpf.trim().length() == 0) {
			throw new IllegalArgumentException("CPF nulo.");
		}
		String numeroCpf = retiraMascara(cpf);

		if (numeroCpf.trim().length() != TAMANHO_CPF) {
			throw new IllegalArgumentException("Tamanho do CPF inválido.");
		}

		return acrescentaMascaraMF(numeroCpf);
	}

	/**
	 * Formata o CNPJ passado por parâmetro com complemento (se necessário).
	 * 
	 * @param cnpj CNPJ
	 * @return String do CNPJ formatado
	 */
	public static String formatarCNPJComComplemento(String cnpj) {
		String resultado = cnpj;

		if (StringUtils.length(cnpj) == TAMANHO_CNPJ_REDUZIDO) {
			String cnpjComRadical = cnpj + "0001";
			resultado = cnpjComRadical + obtemDVCNPJ(cnpjComRadical);
		}
		return resultado;
	}

	public static String mascararCnpj(String cnpj) {
		if (cnpj == null || cnpj.trim().length() == 0) {
			throw new IllegalArgumentException("CNPJ nulo.");
		}

		String numeroCnpj = retiraMascara(cnpj);

		if (numeroCnpj.trim().length() != TAMANHO_CNPJ) {
			throw new IllegalArgumentException("Tamanho do CNPJ inválido.");
		}
		return mascaraCnpj(numeroCnpj);
	}

	/**
	 * Método responsável por formatar CNPJs.
	 * 
	 * @param cnpj CNPJ a ser formatado
	 * @return Retorna o CNPJ formatado, conforme exemplos abaixo:
	 *         "06.182.058/0001-80" retorna "06.182.058/0001-80" "06182058000180"
	 *         retorna "06.182.058/0001-80" "6182058000180" retorna
	 *         "06.182.058/0001-80" (incluiu zero a esquerda) "158000180" retorna
	 *         "00.000.158/0001-80" (incluiu zeros a esquerda) "06.182.058" retorna
	 *         "06.182.058" (Se a qtd de digitos for 8, formata sem radical e DV)
	 *         "06182058" retorna "06.182.058" "6182058" retorna "06.182.058"
	 *         (incluiu zero a esquerda)
	 * 
	 * @see br.jus.cnj.pje.nucleo.InscricaoMFUtilTest
	 */
	public static String mascaraCnpj(String cnpj) {
		return mascaraCnpj(cnpj, false);
	}

	/**
	 * Se o radical e o DV estiverem ausentes, o método inclui esses valores e
	 * retorna o CNPJ formatado
	 * 
	 * @param cnpj CNPJ a ser formatado com solicitação de inclusão do radical e DV,
	 *             se necessário.
	 * @return Retorna o CNPJ formatado, conforme exemplos abaixo:
	 *         "06.182.058/0001-80" retorna "06.182.058/0001-80" "06182058000180"
	 *         retorna "06.182.058/0001-80" "6182058000180" retorna
	 *         "06.182.058/0001-80" "158000180" retorna "00.000.158/0001-80"
	 *         "06.182.058" retorna "06.182.058/0001-80" "06182058" retorna
	 *         "06.182.058/0001-80" "6182058" retorna "06.182.058/0001-80"
	 * 
	 * @see br.jus.cnj.pje.nucleo.InscricaoMFUtilTest
	 */
	public static String mascaraCnpjComComplemento(String cnpj) {
		return mascaraCnpj(cnpj, true);
	}

	/**
	 * Retorna true se os MF's passados por parâmetro são iguais.
	 * 
	 * @param mf0
	 * @param mf1
	 * @return booleano
	 */
	public static Boolean isIguais(String mf0, String mf1) {
		mf0 = retiraMascara(mf0);
		mf1 = retiraMascara(mf1);

		return (mf0 != null && mf1 != null && mf0.equals(mf1));
	}

	private static String mascaraCnpj(String cnpj, boolean incluirDV) {
		if (cnpj == null || cnpj.trim().length() == 0) {
			throw new IllegalArgumentException("CNPJ nulo.");
		}

		String cnpjFormatado = retiraMascara(cnpj);

		if (cnpjFormatado.trim().length() <= TAMANHO_CNPJ_REDUZIDO) {
			if (incluirDV) {
				if (cnpjFormatado.length() < TAMANHO_CNPJ_REDUZIDO) {
					cnpjFormatado = preencheComZerosAEsquerda(cnpjFormatado, "00000000");
				}

				cnpjFormatado = formatarCNPJComComplemento(cnpjFormatado);
				cnpjFormatado = mascaraCnpjCompleto(cnpjFormatado);
			} else {
				cnpjFormatado = mascaraCnpjReduzido(cnpjFormatado);
			}
		} else {
			if (cnpjFormatado.trim().length() <= TAMANHO_CNPJ) {
				cnpjFormatado = mascaraCnpjCompleto(cnpjFormatado);
			} else {
				throw new IllegalAccessError("Tamanho do CNPJ inválido.");
			}
		}

		return cnpjFormatado;
	}

	private static String mascaraCnpjCompleto(String cnpj) {
		String cnpjFormatado = cnpj;

		if (cnpjFormatado.length() < TAMANHO_CNPJ) {
			cnpjFormatado = preencheComZerosAEsquerda(cnpjFormatado, "00000000000000");
		}

		Matcher matcher = CNPJ_NAO_FORMATADO.matcher(cnpjFormatado);
		cnpjFormatado = matchAndReplace(matcher, PADRAO_FORMATACAO_CNPJ);

		return cnpjFormatado;
	}

	private static String mascaraCnpjReduzido(String cnpj) {
		String cnpjFormatado = cnpj;

		if (cnpjFormatado.length() < TAMANHO_CNPJ_REDUZIDO) {
			cnpjFormatado = preencheComZerosAEsquerda(cnpjFormatado, "00000000");
		}

		Matcher matcher = CNPJ_NAO_FORMATADO_REDUZIDO.matcher(cnpjFormatado);
		cnpjFormatado = matchAndReplace(matcher, PADRAO_FORMATACAO_CNPJ_REDUZIDO);

		return cnpjFormatado;
	}

	public static String preencheComZerosAEsquerda(String valor, String mascaraDeZeros) {
		DecimalFormat df = new DecimalFormat(mascaraDeZeros);
		return df.format(Long.parseLong(valor));
	}

	public static String acrescentaMascaraCPF(String cpf) {
		StringBuilder sb = new StringBuilder();
		sb.append(cpf.substring(0, 3));
		sb.append(".");
		sb.append(cpf.substring(3, 6));
		sb.append(".");
		sb.append(cpf.substring(6, 9));
		sb.append("-");
		sb.append(cpf.substring(9));
		return sb.toString();
	}

	public static String getCNPJBase(final String cnpj) {
		if (cnpj.length() != 8) {
			throw new IllegalArgumentException("Raiz do CNPJ com número de dígitos diferente de 8.");
		}
		return formatarCNPJComComplemento(cnpj);
	}

	private static String getCPF(String raizCPF) {
		String dv = InscricaoMFUtil.obtemDVCPF(raizCPF);
		StringBuilder sb = new StringBuilder();
		sb.append(raizCPF);
		sb.append(dv);
		return sb.toString();
	}

	private static String matchAndReplace(Matcher matcher, String replacement) {
		String resultado = null;

		if (matcher.matches()) {
			resultado = matcher.replaceAll(replacement);
		} else {
			throw new IllegalArgumentException(
					"O valor nao foi apropriadamente desformatado para realizar a formatacao.");
		}
		return resultado;
	}

	/**
	 * Dado uma inscrição de cpf/cnpj, retorna um objeto com informações detalhadas
	 * dessa inscrição como tipo e inscricao formatada com máscara.
	 * 
	 * @param inscricao          inscrição a se criar
	 * @param tipoQuandoInvalido tipo a ser adotado por padrão caso a inscrição
	 *                           possua tamanho inválido
	 * @return
	 */
	public static InscricaoMF criarInscricaoMF(String inscricao, String tipoQuandoInvalido) {
		if (inscricao == null || inscricao.isEmpty()) {
			return null;
		}

		inscricao = StringUtil.removeNaoNumericos(inscricao);
		String tipoIMF = "";

		if (inscricao.length() == TAMANHO_CPF) {
			tipoIMF = "CPF";
		} else if (inscricao.length() == TAMANHO_CNPJ) {
			tipoIMF = "CPJ";
		} else if (inscricao.length() == InscricaoMFUtil.TAMANHO_CNPJ_REDUZIDO) {
			tipoIMF = "CPJ";
			inscricao = formatarCNPJComComplemento(inscricao);
		}

		try {
			inscricao = InscricaoMFUtil.acrescentaMascaraMF(inscricao);
		} catch (IllegalArgumentException e) {
			tipoIMF = tipoQuandoInvalido;
		}

		return new InscricaoMF(tipoIMF, inscricao);
	}

	/**
	 * Retorna true se o CNPJ informado é um CNPJ de Matriz. Se o CNPJ informado for
	 * vazio então o retorno padrão será 'true'.
	 * 
	 * @param cnpj
	 * @return Booleano
	 */
	public static Boolean isCNPJMatriz(String cnpj) {
		cnpj = retiraMascara(cnpj);

		return StringUtils.isBlank(cnpj)
				|| (StringUtils.length(cnpj) == 14 && StringUtils.substring(cnpj, 8, 12).equals("0001"));
	}

	/**
	 * Valida se o CPF é válido (11 digitos e digito verificador)
	 * 
	 * @param cpf
	 * 
	 * @return {@code true} se cpf 11 digitos e digito verificador válido.
	 */
	public static boolean isCpfValido(final String cpf) {
		return cpf != null && cpf.length() == TAMANHO_CPF && verificaCPF(cpf);
	}

	/**
	 * Valida se o CNPJ é válido (14 digitos e digito verificador)
	 * 
	 * @param cnpj
	 * 
	 * @return {@code true} se cnpj 14 digitos e digito verificador válido.
	 */
	public static boolean isCnpjValido(final String cnpj) {
		return cnpj != null && cnpj.length() == TAMANHO_CNPJ && verificaCNPJ(cnpj);
	}

	// Não tem validação para fins de performance
	public static String obtemRaizDeCnpjSemValidacao(final String cnpj) {
		if (cnpj != null && cnpj.trim().length() == TAMANHO_CNPJ_COM_MASCARA) {
			// Exemplo: 18.328.118/0001-09 => 18.328.118
			return cnpj.trim().substring(0, 10);
		}
		if (cnpj != null && cnpj.trim().length() == TAMANHO_CNPJ) {
			// Exemplo: 18328118000109 => 18328118
			return cnpj.trim().substring(0, 8);
		}
		return null;
	}

	/**
	 * Formata a inscrição passada por parâmetro.
	 * 
	 * @param inscricao (CPF ou CNPJ).
	 * @return CPF ou CNPJ formatado.
	 */
	public static String formatar(String inscricao) {
		String resultado = null;

		if (StringUtils.isNotBlank(inscricao)) {
			String inscricaoSemMascara = retiraMascara(inscricao);

			if (isCpfValido(inscricaoSemMascara)) {
				return acrescentaMascaraCPF(inscricaoSemMascara);
			} else if (validaCNPJ(inscricaoSemMascara)) {
				return mascaraCnpj(inscricaoSemMascara);
			}
		}
		return resultado;
	}

	/**
	 * Retorna o tipo de documento do nmero passado por parmetro.
	 * 
	 * @param documento Nmero do documento.
	 * @return CNPJ ou CPF.
	 */
	public static String getTipoDocumentoCNPJouCPF(String documento) {
		String tipo = null;
		String documentoSemMascara = InscricaoMFUtil.retiraMascara(documento);

		if (StringUtils.isNotBlank(documentoSemMascara)) {
			Map<Integer, String> mapaTamanhoParaTipoDocumento = new HashMap<>();
			mapaTamanhoParaTipoDocumento.put(TAMANHO_CPF, "CPF");
			mapaTamanhoParaTipoDocumento.put(TAMANHO_CNPJ_REDUZIDO, "CNPJ");
			mapaTamanhoParaTipoDocumento.put(TAMANHO_CNPJ, "CNPJ");

			tipo = mapaTamanhoParaTipoDocumento.get(documentoSemMascara.length());
		}

		return tipo;
	}
}
