package br.jus.csjt.pje.business.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.home.ProcessoTrfHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.Variaveis;
import br.jus.cnj.pje.nucleo.manager.CompetenciaManager;
import br.jus.cnj.pje.nucleo.manager.HistoricoDeslocamentoOrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.manager.OrgaoJulgadorManager;
import br.jus.cnj.pje.nucleo.service.BaseService;
import br.jus.pje.jt.entidades.HistoricoDeslocamentoOrgaoJulgador;
import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.ClasseJudicial;
import br.jus.pje.nucleo.entidades.Competencia;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.AbrangenciaEnum;
import br.jus.pje.nucleo.util.DateUtil;

@Name(PlantaoJudicialService.NAME)
public class PlantaoJudicialService extends BaseService{

	public static final String NAME = "plantaoJudicialService";
	
	private static final LogProvider log = Logging.getLogProvider(PlantaoJudicialService.class);
	
	@In
	private HistoricoDeslocamentoOrgaoJulgadorManager historicoDeslocamentoOrgaoJulgadorManager;
	
	@In(create=true)
	private CalendarioEventoService calendarioEventoService;
	
	public static PlantaoJudicialService instance() {
		return ComponentUtil.getComponent(PlantaoJudicialService.class);
	}
	
	/**
	 * @return retorna true se o processo deve ir ao plantão judicial
	 */
	public Boolean processoDeveIrAoPlantao(ProcessoTrf processoTrf) throws PJeException {
		return ( historicoDeslocamentoOrgaoJulgadorManager.obterHistoricoSemDatasDefinidas(processoTrf) != null );
	}
	
	/**
	 * @return retorna true se o processo deve ir ao plantão judicial. Pega o ProcessoTrf que está no contexto.
	 */
	public Boolean processoDeveIrAoPlantao() throws PJeException {		
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		return ( this.processoDeveIrAoPlantao( processoTrf ) );
	}
	
	/**
	 * Registra se o processo deve ir ao plantão judicial
	 * @throws PJeBusinessException
	 */
	public void registraSeDeveIrParaPlantao() throws PJeBusinessException {
		ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
		HistoricoDeslocamentoOrgaoJulgador historicoDeslocamentoOrgaoJulgador = new HistoricoDeslocamentoOrgaoJulgador();
		historicoDeslocamentoOrgaoJulgador.setProcessoTrf(processoTrf);
		try{
			HistoricoDeslocamentoOrgaoJulgadorManager manager = ComponentUtil.getComponent("historicoDeslocamentoOrgaoJulgadorManager");
			manager.persist(historicoDeslocamentoOrgaoJulgador);
			log.info("[PLANTAO JUDICIAL - INFO] Inserido registro se deve ir para plantão do processo de id: "+processoTrf.getIdProcessoTrf());
			ComponentUtil.getTramitacaoProcessualService().acrescentarSituacao(processoTrf, Variaveis.PJE_ATENDIMENTO_PLANTAO);
		} catch (Exception e){
			log.error("[PLANTAO JUDICIAL - ERROR] - Não foi possível inserir registro se deve ir para plantão do processo de id: ", e);
			throw new PJeBusinessException("Não foi possível inserir registro se deve ir para plantão do processo de id: ",e);
		}
	}
	
	public boolean verificarPlantao() {
		return PlantaoJudicialService.instance().verificarPlantao(ProcessoTrfHome.instance().getInstance().getJurisdicao().getOrgaoJulgadorPlantao(),
				ProcessoTrfHome.instance().getInstance().getJurisdicao().getOrgaoJulgadorColegiadoPlantao(),
				ProcessoTrfHome.instance().getInstance().getJurisdicao(), calendarioEventoService.obterListaDeFeriados(),
				ParametroUtil.getParametro(Parametros.HORA_INICIO_PLANTAO), ParametroUtil.getParametro(Parametros.HORA_TERMINO_PLANTAO), new Date(),
				ParametroUtil.getParametro(Parametros.VALIDA_DIA_ANTERIOR_POSTERIOR), ProcessoTrfHome.instance().getInstance().getCompetencia(),
				ProcessoTrfHome.instance().getInstance().getClasseJudicial());
	}

	public boolean verificarPlantao(OrgaoJulgador orgaoJulgadorPlantao, OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao, Jurisdicao jurisdicao,
			List<CalendarioEvento> listaFeriados, String tempoInicioPlantao, String tempoFimPlantao, Date data, String validaDiaAnteriorPosterior,
			Competencia competencia, ClasseJudicial classeJudicial) {

		boolean resultado = false;

		if (orgaoJulgadorPlantao != null && orgaoJulgadorPlantao.getAtivo() &&
				(ParametroUtil.instance().isPrimeiroGrau() || orgaoJulgadorColegiadoPlantao != null && orgaoJulgadorColegiadoPlantao.getAtivo())) {
			if (DateUtil.isFimDeSemana(data)) {
				resultado = true;
			} else {				
				if (verificarFeriado(jurisdicao, listaFeriados, data)) {
					resultado = true;
				} else {
					if ("true".equalsIgnoreCase(validaDiaAnteriorPosterior)) {
						if (verificarPlantaoDiaAnteriorPosterior(validaDiaAnteriorPosterior, jurisdicao, listaFeriados, 
								tempoInicioPlantao, tempoFimPlantao, data)) {
							
							resultado = true;
						}
					} else {
						if (verificarHoraInicioFimPlantao(tempoInicioPlantao, tempoFimPlantao, data)) {
							resultado = true;
						}
					}
				}
			}
			
			if (competencia != null && classeJudicial != null && !ComponentUtil.getComponent(CompetenciaManager.class).isClasseAtendimentoPlantao(competencia, classeJudicial)) {
				resultado = false;
			}
		}
		return resultado;
	}
	
	public OrgaoJulgador recuperarOrgaoJulgadorPlantao() {
		OrgaoJulgador orgaoJulgadorPlantao = null;
		String idOrgaoJulgadorPlantao = ParametroUtil.getParametro(Parametros.ID_OJ_PLANTAO);
		
		try {
			orgaoJulgadorPlantao = ComponentUtil.getComponent(OrgaoJulgadorManager.class).findById(Integer.parseInt(idOrgaoJulgadorPlantao));
		} catch (Exception e) {
			// Nada a fazer.
		}
		
		return orgaoJulgadorPlantao;
	}
	
	private boolean verificarFeriado(Jurisdicao jurisdicao, List<CalendarioEvento> listaFeriados, Date data) {
		boolean resultado = false;
		
		Estado estadoSedeJurisdicao = jurisdicao.getEstado();
		Municipio municipioSedeJurisdicao = jurisdicao.getMunicipioSede();
		
		for (CalendarioEvento calendarioEvento : listaFeriados) {
			if (calendarioEvento.getAtivo() && calendarioEvento.estaNesteEvento(data)) {
				if (calendarioEvento.getInJudiciario()) {
					resultado = true;
				} else {
					if (calendarioEvento.getInFeriado()) {
						if (calendarioEvento.getInAbrangencia().equals(AbrangenciaEnum.N)) {
							resultado = true;
						} else if (calendarioEvento.getInAbrangencia().equals(AbrangenciaEnum.E)) {
							if (calendarioEvento.getEstado().equals(estadoSedeJurisdicao)) {
								resultado = true;
							}
						} else if (calendarioEvento.getInAbrangencia().equals(AbrangenciaEnum.C)) {
							if (calendarioEvento.getEstado().equals(estadoSedeJurisdicao) && 
									calendarioEvento.getMunicipio().equals(municipioSedeJurisdicao)) {
								
								resultado = true;
							}
						}
					}
				}
			}
		}
		return resultado;
	}
	
	private boolean verificarPlantaoDiaAnteriorPosterior(String validaDiaAnteriorPosterior, Jurisdicao jurisdicao, 
			List<CalendarioEvento> listaFeriados, String tempoInicioPlantao, String tempoTerminoPlantao, Date data) {
		
		boolean resultado = false;
		
		if (DateUtil.validarRepresentacaoTempo(tempoInicioPlantao) && DateUtil.validarRepresentacaoTempo(tempoTerminoPlantao)) {
			String tempoAtual = DateUtil.dateToHour(data);
			Date dataAnterior = DateUtil.adicionarTempoData(data, Calendar.DAY_OF_MONTH, -1);
			Date dataSeguinte = DateUtil.adicionarTempoData(data, Calendar.DAY_OF_MONTH, 1);

			resultado = ((DateUtil.isFimDeSemana(dataAnterior) || verificarFeriado(jurisdicao, listaFeriados, dataAnterior)) && 
								tempoAtual.compareTo(tempoTerminoPlantao) <= 0) || 
						((DateUtil.isFimDeSemana(dataSeguinte) || verificarFeriado(jurisdicao, listaFeriados, dataSeguinte)) && 
								tempoAtual.compareTo(tempoInicioPlantao) >= 0);
		}
		return resultado;
	}
	
	private boolean verificarHoraInicioFimPlantao(String tempoInicioPlantao, String tempoTerminoPlantao, Date data) {
		boolean resultado = false;
		
		if (DateUtil.validarRepresentacaoTempo(tempoInicioPlantao) && DateUtil.validarRepresentacaoTempo(tempoTerminoPlantao)) {
			String tempoAtual = DateUtil.dateToHour(data);

			if (tempoInicioPlantao.compareTo(tempoTerminoPlantao) < 0) {
				if (tempoInicioPlantao.compareTo(tempoAtual) <= 0 && tempoAtual.compareTo(tempoTerminoPlantao) <= 0) {
					resultado = true;
				}
			} else if (tempoAtual.compareTo(tempoTerminoPlantao) <= 0 || tempoAtual.compareTo(tempoInicioPlantao) >= 0) {
					return true;
			}
		}
		return resultado;
	}
	
}

