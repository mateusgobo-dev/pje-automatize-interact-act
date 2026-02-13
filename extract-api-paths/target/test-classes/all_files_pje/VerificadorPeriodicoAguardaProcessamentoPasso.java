package br.jus.cnj.pje.controleprazos.verificadorperiodico;

import java.util.TimerTask;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.manager.VerificadorPeriodicoLoteManager;

@Scope(ScopeType.EVENT)
@Name(VerificadorPeriodicoAguardaProcessamentoPasso.NAME)
public class VerificadorPeriodicoAguardaProcessamentoPasso extends TimerTask {
	public static final String NAME = "verificadorPeriodicoAguardaProcessamentoPasso";

	private static final Logger log = Logger.getLogger(VerificadorPeriodicoAguardaProcessamentoPasso.class.getName());

	private static final class Lock {
	}

	private final Object lock = new Lock();

	private String passo;
	private UUID lote;
	private Integer tamanhoLote;
	private Integer expedientesProcessadosAnterior = 0;
	private Integer contadorSemMudancas = 0;
	private Integer contadorNulo = 0;
	private Integer minTentativasSemMudancas = 600;
	private Integer maxTentativasSemMudancas = 1800;
	private Integer maxTentativasNulo = 300;
	private boolean proibidoExecutar = true;

	VerificadorPeriodicoLoteManager manager = ComponentUtil.getComponent(VerificadorPeriodicoLoteManager.NAME);

	@Create
	public void init() {
		proibidoExecutar = true;
	}

	public VerificadorPeriodicoAguardaProcessamentoPasso() {
		proibidoExecutar = true;
	}

	public String getPasso() {
		return passo;
	}

	public void setPasso(String passo) {
		this.passo = passo;
	}

	public UUID getLote() {
		return lote;
	}

	public void setLote(UUID lote) {
		this.lote = lote;
	}

	public Integer getTamanhoLote() {
		return tamanhoLote;
	}

	public void setTamanhoLote(Integer tamanhoLote) {
		this.tamanhoLote = tamanhoLote;
	}

	public boolean getProibidoExecutar() {
		return proibidoExecutar;
	}

	public void setProibidoExecutar(boolean proibidoExecutar) {
		this.proibidoExecutar = proibidoExecutar;
	}

	public void limpaValores() {
		contadorSemMudancas = 0;
		contadorNulo = 0;
		expedientesProcessadosAnterior = 0;
	}

	public void run() {
		if (proibidoExecutar) {
			return;
		}

		proibidoExecutar = true;

		try {
			Integer jobsProcessados = manager.getJobsProcessadosPorLote(lote);

			if (jobsProcessados != null) {
				if (contadorNulo > 0) {
					contadorNulo = 0;
				}

				if (jobsProcessados.equals(tamanhoLote)) {
					String logMessage = "[" + passo + "] Finalizado o processamento do lote '" + lote + "'.";

					log.info(logMessage);

					notificaTermino();
					return;
				} else if (jobsProcessados > expedientesProcessadosAnterior) {
					if (contadorSemMudancas > 0) {
						contadorSemMudancas = 0;
					}
				} else if (jobsProcessados.equals(expedientesProcessadosAnterior)) {
					if (contadorSemMudancas.equals(maxTentativasSemMudancas)
							|| contadorSemMudancas > maxTentativasSemMudancas) {
						String logMessage = "[" + passo + "] Processamento do lote '" + lote
								+ "' finalizado, pois não houve atualização do total dos jobs processados do lote.";

						log.warn(logMessage);

						notificaTermino();
						return;
					} else {
						++contadorSemMudancas;

						if (contadorSemMudancas > minTentativasSemMudancas) {
							String logMessage = "[" + passo + "] Aguardando processamento do lote '" + lote
									+ "'. Total de '" + contadorSemMudancas + "/" + maxTentativasSemMudancas
									+ "' tentativas.";

							log.info(logMessage);
						}
					}
				}
			} else {
				if (contadorNulo < maxTentativasNulo) {
					++contadorNulo;

					String logMessage = "[" + passo
							+ "] Não foi possível obter o total de expedientes processados do lote '" + lote + "'."
							+ " Tentando obter o total dos jobs processados, tentativa '" + contadorNulo + "/"
							+ maxTentativasNulo + "'.";

					log.warn(logMessage);
				} else {
					String logMessage = "[" + passo + "] Processamento do lote '" + lote
							+ "' finalizado, mas não foi possível obter o total dos jobs processados no lote.";

					log.error(logMessage);

					notificaTermino();
					return;
				}
			}

			expedientesProcessadosAnterior = jobsProcessados;
		} catch (Exception e) {
			e.printStackTrace();

			String logMessage = "[" + passo + "] Erro durante a espera do processamento do lote '" + lote + "': "
					+ e.getMessage();

			log.error(logMessage);

			notificaTermino();

			return;
		}

		proibidoExecutar = false;
	}

	public void aguardaProcessamentoPassoTerminar() {
		synchronized (lock) {
			try {
				lock.wait();
			} catch (InterruptedException e) {
				log.error("[" + passo + "] Erro no wait durante a espera do processamento do lote '" + lote + "': "
						+ e.getLocalizedMessage());

				e.printStackTrace();
			}
		}
	}

	private void notificaTermino() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}
}