package br.com.infox.cliente;

import java.util.Calendar;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Strings;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Rpv;

public class NumeroRpvUtil {

	private static final LogProvider LOG = Logging.getLogProvider(NumeroRpvUtil.class);

	private static Object lock = new Object();

	public static String completaZeros(long l, int tamanho) {
		StringBuilder sb = new StringBuilder();
		String lSrt = Long.toString(l);
		for (int i = 0; i < tamanho - lSrt.length(); i++) {
			sb.append('0');
		}
		sb.append(lSrt);
		return sb.toString();
	}

	/**
	 * Formata o numero do processo para exibição: O número do RPV que é
	 * composto por: ano, código da região federal, código da localidade
	 * federal, número da vara, número do sequencial gerado. Exemplo:
	 * 2011.84.00.007.300018, onde a sequência é agrupada por 2 011.84.00.007.
	 * Faixa do seqüencial que será utilizada pelo PJE: 200001 a 500000  
	 * 
	 * @param ano
	 *            = ano (AAAA)
	 * @param numeroOrigemProcesso
	 *            = origem do processo (OO.OO)
	 * @param numeroVara
	 *            = identificação do órgão da justiça (VARA)
	 * @param numeroSequencia
	 *            = número seqüencial do processo no ano (NNNNNN)
	 * @return Numero da Rpv formatada (AAAA.OO.OO.VARA.NNNNNN)
	 */
	public static String formatNumeroRpv(long ano, long numeroOrigemProcesso, long numeroVara, long numeroSequencia) {
		StringBuilder sb = new StringBuilder();

		sb.append(ano);

		String numOrigem = completaZeros(numeroOrigemProcesso, 4);
		sb.append('.').append(numOrigem.substring(0, 2));
		sb.append('.').append(numOrigem.substring(2));

		sb.append('.').append(completaZeros(numeroVara, 3));

		sb.append('.').append(completaZeros(numeroSequencia, 6));

		return sb.toString();
	}

	/**
	 * Formata o número da rpv para exibição
	 * 
	 * @param rpv
	 * @return número formatado Ex.: 2011.04.05.007.300018
	 */
	public static String formatNumeroRpv(Rpv rpv) {
		if (rpv == null) {
			return null;
		} else {
			return formatNumeroRpv(rpv.getAno(), rpv.getNumeroOrigemProcesso(), rpv.getNumeroVara(),
					rpv.getNumeroSequencia());
		}
	}

	/**
	 * Metodo que recupera o proximo numero sequencial para o rpv. Este método
	 * não é sincronizado então deverá ser feito syncronize onde ele for usado.
	 * 
	 * @param ano
	 * @param numeroVara
	 * @param numeroOrigemProcesso
	 * @return Numero sequencial
	 */
	public static int getProximoSequencial(int ano, int numeroVara, int numeroOrigemProcesso) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select max(o.numeroSequencia) from Rpv o ");
		sb.append(" where o.ano = :ano and o.numeroVara = :numeroVara ");
		sb.append(" and o.numeroOrigemProcesso = :numeroOrigemProcesso ");
		Query query = EntityUtil.getEntityManager().createQuery(sb.toString());
		query.setParameter("ano", ano);
		query.setParameter("numeroOrigemProcesso", numeroOrigemProcesso);
		query.setParameter("numeroVara", numeroVara);
		Integer result = (Integer) query.getSingleResult();
		return result != null ? ++result : getNumeroInicial();
	}

	private static int getNumeroInicial() {
		String numeroInicial = (String) Contexts.getApplicationContext().get("numeroInicialRpv");
		if (Strings.isEmpty(numeroInicial)) {
			return 1;
		} else {
			return Integer.parseInt(numeroInicial);
		}
	}

	/**
	 * Metodo que executa a numeração da Rpv de forma sincronizada
	 * 
	 * @param rpv
	 * @param numeroVara
	 * @param numeroOrigemProcesso
	 */
	public static void numerarRpv(Rpv rpv, int numeroVara, int numeroOrigemProcesso) {
		if (rpv == null) {
			throw new IllegalArgumentException("Rpv Inválida");
		} else if (rpv.isNumerado()) {
			LOG.warn(".numerarRpv() A Rpv já está numerada: " + rpv);
			return;
		}
		EntityManager em = EntityUtil.getEntityManager();
		int ano = Calendar.getInstance().get(Calendar.YEAR);

		synchronized (lock) {
			int numeroSequencia = getProximoSequencial(ano, numeroVara, numeroOrigemProcesso);
			rpv.setAno(ano);
			rpv.setNumeroSequencia(numeroSequencia);
			rpv.setNumeroOrigemProcesso(numeroOrigemProcesso);
			rpv.setNumeroVara(numeroVara);

			em.merge(rpv);
			String numeroRpv = formatNumeroRpv(rpv);
			rpv.setNumeroRpvPrecatorio(numeroRpv);
			em.merge(rpv);
			em.flush();
		}
	}

}