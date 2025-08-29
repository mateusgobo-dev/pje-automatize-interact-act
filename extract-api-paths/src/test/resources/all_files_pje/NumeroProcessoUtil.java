package br.com.infox.cliente;


import java.math.BigInteger;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.utils.Constantes.GRAU_JURISDICAO;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.NumeradorProcesso;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.util.StringUtil;

/**
 * 
 * @author <a href="mailto:rodrigo@infox.com.br">Rodrigo Menezes da Conceição</a><br/>
 * <br/>
 *         Classe Util com metodos para numeração de processos seguindo normas no CNJ.
 * 
 */

public final class NumeroProcessoUtil{

	public static final int QTD_CARACTERES_NUMERO_UNICO_COM_MASCARA = 25;
	public static final int QTD_CARACTERES_NUMERO_UNICO_SEM_MASCARA = 20;

	private static final LogProvider log = Logging.getLogProvider(NumeroProcessoUtil.class);

	public static String completaZeros(long l, int tamanho){
		StringBuilder sb = new StringBuilder();
		String lSrt = Long.toString(l);
		for (int i = 0; i < tamanho - lSrt.length(); i++){
			sb.append('0');
		}
		sb.append(lSrt);
		return sb.toString();
	}

	/**
	 * <li><b>NNNNNNN</b> = Número sequencial do processo no ano</li> <li>
	 * <b>DD</b> = Dígito de verificação</li> <li><b>AAAA</b> = Ano</li> <li>
	 * <b>JTR</b> = Identificação do órgão da justiça</li> <li><b>OOOO</b> = Origem do processo</li> Para calcular os dígitos de verificação basta
	 * aplicar a seguinte fórmula:<br/>
	 * DD = 98 (NNNNNNN AAAA JTR OOOO 00 mod 97)<br/>
	 * O resultado da fórmula deve ser formatado em dois dígitos, incluindo o zero à esquerda se necessário. Os dígitos resultantes são os dígitos de
	 * verificação.
	 * 
	 * @param numeroSequencia = número seqüencial do processo no ano
	 * @param ano = ano do processo
	 * @param numeroVara = identificação do órgão da justiça
	 * @param numeroOrigemProcesso = origem do processo
	 * @return Digito verificador
	 */
	public static int calcDigitoVerificador(long numeroSequencia, long ano, long numeroVara, long numeroOrigemProcesso){
		String numeroCalc = completaZeros(numeroSequencia, 7) + completaZeros(ano, 4) + completaZeros(numeroVara, 3)
				+ completaZeros(numeroOrigemProcesso, 4) + "00";
		BigInteger nro = new BigInteger(numeroCalc);
		long digito = 98 - (nro.mod(new BigInteger("97")).longValue());
		return (int) digito;
	}

	/**
	 * Formata o numero do processo para exibição
	 * 
	 * @param numeroSequencia = número seqüencial do processo no ano (NNNNNNN)
	 * @param numeroDigitoVerificador = Número identificador (DD)
	 * @param ano = ano (AAAA)
	 * @param numeroOrgaoJustica = identificação do órgão da justiça (JTR)
	 * @param numeroOrigem = origem do processo (OOOO)
	 * @return Numero do processo formatado (NNNNNNN-DD.AAAA.J.TR.OOOO)
	 */
	public static String formatNumeroProcesso(Integer numeroSequencia, Integer numeroDigitoVerificador, Integer ano,
			Long numeroOrgaoJustica, Integer numeroOrigem){
		StringBuilder sb = new StringBuilder();
		sb.append(completaZeros(numeroSequencia, 7));
		sb.append('-').append(completaZeros(numeroDigitoVerificador, 2));
		sb.append('.').append(completaZeros(ano, 4));

		String nOrgJus = completaZeros(numeroOrgaoJustica.intValue(), 3);
		sb.append('.').append(nOrgJus.substring(0, 1));
		sb.append('.').append(nOrgJus.substring(1));

		sb.append('.').append(completaZeros(numeroOrigem, 4));
		return sb.toString();
	}

	public static String formatNumeroProcesso(Integer numeroSequencia, Integer numeroDigitoVerificador, Integer ano,
			Integer numeroOrgaoJustica, Integer numeroOrigem){
		return formatNumeroProcesso(numeroSequencia, numeroDigitoVerificador, ano, numeroOrgaoJustica.longValue(),
				numeroOrigem);
	}

	/**
	 * Formata o numero do processo para exibição
	 * 
	 * @param processoTrf
	 * @return
	 */
	public static String formatNumeroProcesso(ProcessoTrf processoTrf){
		if (processoTrf == null){
			return null;
		}
		else{
			return formatNumeroProcesso(processoTrf.getNumeroSequencia(), processoTrf.getNumeroDigitoVerificador(),
					processoTrf.getAno(), processoTrf.getNumeroOrgaoJustica(), processoTrf.getNumeroOrigem());
		}
	}

	/**
	 * Metodo que recupera o proximo numero sequencial para o processo. Este meotodo não é sincronizado então deverá ser feito syncronize onde ele for
	 * usado.
	 * 
	 * @param ano
	 * @param numeroOrgaoJustica
	 * @param numeroOrigem
	 * @return Numero sequencial
	 */
	public static int getProximoSequencial(int ano, int numeroOrgaoJustica, int numeroOrigem){
		NumeradorProcesso np = (NumeradorProcesso) Component.getInstance("numeradorProcesso", ScopeType.APPLICATION);
		return np.getProximoNumero(numeroOrgaoJustica, numeroOrigem);
	}
	
	private static Boolean processoAtivo(ProcessoTrf processoTrf) {
		if (processoTrf == null){
			throw new IllegalArgumentException("Processo inválido");
		}else if (processoTrf.isNumerado()){
			log.warn("Chamada indevida para renumeração do processo judicial [" + processoTrf.getProcesso().getNumeroProcesso() + "].");
			return false;
		}
		return true;		
	}

	/**
	 * Metodo que executa a numeração do processo de maneira sincronizada
	 * 
	 * @param processoTrf
	 * @param numeroOrgaoJustica
	 * @param numeroOrigem
	 */
	public static void numerarProcesso(ProcessoTrf processoTrf, int numeroOrgaoJustica, int numeroOrigem){
		if (processoAtivo(processoTrf)) {
			NumeroProcesso numeroProcesso = gerarNumeroProcesso(numeroOrgaoJustica, numeroOrigem);
			processoTrf.setNumeroSequencia(numeroProcesso.getNumeroSequencia());
			processoTrf.setNumeroDigitoVerificador(numeroProcesso.getNumeroDigitoVerificador());
			processoTrf.setAno(numeroProcesso.getAno());
			processoTrf.setNumeroOrgaoJustica(numeroProcesso.getNumeroOrgaoJustica());
			processoTrf.setNumeroOrigem(numeroProcesso.getNumeroOrigem());
			persistirNumero(processoTrf);
		}
	}

	public static void numerarProcesso(ProcessoTrf processoTrf) {
		String numeroProcessoTemp = processoTrf.getProcesso().getNumeroProcessoTemp();
		if (StringUtils.isNotBlank(numeroProcessoTemp)) {
			numeroProcessoTemp = retiraMascaraNumeroProcesso(numeroProcessoTemp);
			if (numeroProcessoValido(numeroProcessoTemp)) {
				processoTrf.setNumeroSequencia(Integer.valueOf(numeroProcessoTemp.substring(0, 7)));
				processoTrf.setNumeroDigitoVerificador(Integer.valueOf(numeroProcessoTemp.substring(7, 9)));
				processoTrf.setAno(Integer.valueOf(numeroProcessoTemp.substring(9, 13)));
				processoTrf.setNumeroOrgaoJustica(Integer.valueOf(numeroProcessoTemp.substring(13, 16)));
				processoTrf.setNumeroOrigem(Integer.valueOf(numeroProcessoTemp.substring(16)));
				
				persistirNumero(processoTrf);
			}
		}
	}
	
	public static void persistirNumero(ProcessoTrf processoTrf) {
		EntityManager em = EntityUtil.getEntityManager();
		em.merge(processoTrf);
		String numeroProcesso = formatNumeroProcesso(processoTrf);
		processoTrf.getProcesso().setNumeroProcesso(numeroProcesso);
		em.merge(processoTrf.getProcesso());
		em.flush();
		log.trace("Atribuído o número [" + numeroProcesso + "] para o processo judicial com identificador interno [" +  processoTrf.getIdProcessoTrf() + "].");
	}

	public static String mascaraNumeroProcesso(String numeroProcesso){
		String numeroRetorno = numeroProcesso;
		if(!StringUtil.isEmpty(numeroProcesso)) {
			numeroProcesso = StringUtil.limparCaracteresEntreStrings(numeroProcesso);
			if (numeroProcesso.length() == QTD_CARACTERES_NUMERO_UNICO_SEM_MASCARA){
				StringBuilder sb = new StringBuilder();
				sb.append(numeroProcesso.substring(0, 7));
				sb.append('-');
				sb.append(numeroProcesso.substring(7, 9));
				sb.append('.');
				sb.append(numeroProcesso.substring(9, 13));
				sb.append('.');
				sb.append(numeroProcesso.substring(13, 14));
				sb.append('.');
				sb.append(numeroProcesso.substring(14, 16));
				sb.append('.');
				sb.append(numeroProcesso.substring(16, 20));
				numeroRetorno = sb.toString();
			}
		}
		return numeroRetorno;
	}
	
	public static String retiraMascaraNumeroProcesso(String numeroProcesso){
		return StringUtil.fullTrim(numeroProcesso.replaceAll("[_\\.\\-/]", ""));
	}
	
	/**
	 * Verifica se o Número do Processo está de acordo com as regras da norma 65 do CNJ.
	 * 
	 * @param processo
	 * @return
	 */
	public static boolean numeroProcessoValido(Processo processo){
		return numeroProcessoValido(processo.getNumeroProcessoTemp());
	}

	public static boolean numeroProcessoValido(String numeroProcesso){
		try{
			if(Strings.isEmpty(numeroProcesso)) {
				return false;
			}
			
			//considera inválido número de processo cujo tamanho seja diferente de 20 (sem máscara) e 25 (com máscara)
			if(numeroProcesso.length() != QTD_CARACTERES_NUMERO_UNICO_SEM_MASCARA && numeroProcesso.length() != QTD_CARACTERES_NUMERO_UNICO_COM_MASCARA) {
				return false;
			}
			
			//tamanho do número de processo sem máscara: atribui a máscara ao número do processo para viabilizar o cálculo do DV
			if(numeroProcesso.length() == QTD_CARACTERES_NUMERO_UNICO_SEM_MASCARA) {
				numeroProcesso = mascaraNumeroProcesso(numeroProcesso);
			}
			
			long numeroSequencia = Integer.parseInt(numeroProcesso.substring(0, 7));
			long numeroDigitoVerificador = Integer.parseInt(numeroProcesso.substring(8, 10));
			long ano = Integer.parseInt(numeroProcesso.substring(11, 15));
			long numeroVara = Integer.parseInt(numeroProcesso.substring(16, 17) + numeroProcesso.substring(18, 20));
			long numeroOrigemProcesso = Integer.parseInt(numeroProcesso.substring(21, 25));
			
			if(NumeroProcessoUtil.calcDigitoVerificador(numeroSequencia, ano, numeroVara, numeroOrigemProcesso) == numeroDigitoVerificador){
				return true;
			}
			
		}catch (Exception e){
			return false;
		}
		return false;
	}
	
	public static boolean numeroProcessoValidoNaOrigem(String numeroProcesso) {
		//TODO Validar existência do processo na origem, via MNI. Por ora, apenas verifica se o número do processo é maior que 
		//o parâmetro de número inicial de processos.
		ParametroService parametroService = getParametroService();
		if(parametroService.valueOf("validaProcessoReferencia") != null && parametroService.valueOf("validaProcessoReferencia").equals("OR")) {
			long numeroVara = Integer.parseInt(numeroProcesso.substring(16, 17) + numeroProcesso.substring(18, 20));
			long numeroSequencia = Integer.parseInt(numeroProcesso.substring(0, 7));
			long ano = Integer.parseInt(numeroProcesso.substring(11, 15));
			long anoReferencia = parametroService.valueOf("anoInicialProcesso") == null ? 2014 : Long.parseLong(parametroService.valueOf("anoInicialProcesso"));
			long numeroInicialProcesso = parametroService.valueOf("numeroInicialProcesso") == null ? 0 : Long.parseLong(parametroService.valueOf("numeroInicialProcesso"));
			if(Integer.parseInt(parametroService.valueOf("numeroOrgaoJustica")) != numeroVara) {
				return false;
			}
			
			if(ano < anoReferencia){
				return false;
			}
			else if(numeroSequencia <= numeroInicialProcesso) {
				return false;
			}
			
		}
		return true;
	}
	
	/**
	 * Método responsável por gerar o número do processo de acordo com os parâmetros informados.
	 * 
	 * @param numeroOrgaoJustica Número do órgão de justiça.
	 * @param numeroOrigem Número de origem.
	 * @return O número do processo de acordo com os parâmetros informados.
	 */
	public static NumeroProcesso gerarNumeroProcesso(int numeroOrgaoJustica, int numeroOrigem) {
		NumeroProcesso numeroProcesso = new NumeroProcesso();
		
		int ano = GregorianCalendar.getInstance().get(Calendar.YEAR);
		int numeroSequencia = getProximoSequencial(ano, numeroOrgaoJustica, numeroOrigem);
		int numeroDigitoVerificador = calcDigitoVerificador(numeroSequencia, ano, numeroOrgaoJustica, numeroOrigem);
		
		numeroProcesso.setNumeroSequencia(numeroSequencia);
		numeroProcesso.setNumeroDigitoVerificador(numeroDigitoVerificador);
		numeroProcesso.setAno(ano);
		numeroProcesso.setNumeroOrgaoJustica(numeroOrgaoJustica);
		numeroProcesso.setNumeroOrigem(numeroOrigem);
		
		return numeroProcesso;
	}
	
	/**
	 * Método responsável por obter o número da jurisdição (número de origem) do processo.
	 * 
	 * @param isRequisicaoDePJE Indica se a requisição partiu de um PJe.
	 * @param numeroJurisdicaoOrigem Número da jurisdição de origem.
	 * @param numeroOrgaoJusticaOrigem Número do órgão de justiça da origem.
	 * @param instanciaProcesso Instância do processo.
	 * @return O número da jurisdição (número de origem) do processo.
	 */
	public static int obterNumeroJurisdicao(int numeroJurisdicaoOrigem, int numeroOrgaoJusticaOrigem, String instanciaProcesso, boolean isIncidental) {
		int numeroOrgaoJusticaAtual = Integer.parseInt(getParametroService().valueOf("numeroOrgaoJustica").substring(0, 1));
		if (instanciaProcesso.equals(GRAU_JURISDICAO.PRIMEIRO_GRAU) && 
				ParametroUtil.instance().isSegundoGrau() && 
					numeroOrgaoJusticaAtual != numeroOrgaoJusticaOrigem &&
						!isIncidental) {
			
			 return 9999;
			
		} else {			
			return numeroJurisdicaoOrigem;
		}
	}
	
	private static ParametroService getParametroService() {
		return ComponentUtil.getComponent(ParametroService.NAME);
	}

	public static String obterNumeroOrgaoJustica(String numeroProcesso) {
		if(!numeroProcessoValido(numeroProcesso)) {
			throw new IllegalArgumentException("Processo inválido");
		}
		return  retiraMascaraNumeroProcesso(numeroProcesso).substring(13,16);
	}
}