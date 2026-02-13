package br.jus.cnj.pje.ws;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import br.jus.cnj.pje.nucleo.PJeException;
import br.jus.cnj.pje.nucleo.manager.AssuntoTrfManager;
import br.jus.cnj.pje.nucleo.manager.ClasseJudicialManager;
import br.jus.cnj.pje.nucleo.manager.JurisdicaoManager;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.TipoParteConfigClJudicial;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.util.StringUtil;

public class ConsultaPJeUtil {

	@SuppressWarnings("unchecked")
	/**
	 * Preenche uma lista com pojos do ws ConsultaPJe a partir de lista com entidades do PJe
	 * 
	 * @param pjeList
	 * @param pojoList
	 */
	public static <T, J> void fill(List<J> pojoList, List<T> pjeList) {
		try {
			Class<J> pojoClass = (Class<J>) pojoList.getClass()
					.getTypeParameters()[0].getClass();
			Class<T> pjeClass = (Class<T>) pjeList.getClass()
					.getTypeParameters()[0].getClass();

			// invoca o método estatico específico desta classe
			Method method = ConsultaPJeUtil.class.getMethod(
					"to" + pojoClass.getSimpleName(), pjeClass);
			if (pjeList != null && !pjeList.isEmpty()) {
				pojoList.clear();
				for (T objPJe : pjeList) {
					pojoList.add((J) method.invoke(null, objPJe));
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Falha ao converter lista de " + pjeList
					+ " para " + pojoList);
		}
	}

	public static Jurisdicao toJurisdicao(
			br.jus.pje.nucleo.entidades.Jurisdicao jurisdicaoPJe) {
		Jurisdicao jurisdicao = new Jurisdicao();
		jurisdicao.setId(jurisdicaoPJe.getNumeroOrigem());
		jurisdicao.setDescricao(jurisdicaoPJe.getJurisdicao());
		return jurisdicao;
	}

	public static OrgaoJulgador toOrgaoJulgador(
			br.jus.pje.nucleo.entidades.OrgaoJulgador orgaoJulgadorPJe) {
		OrgaoJulgador orgaoJulgador = new OrgaoJulgador();
		orgaoJulgador.setId(orgaoJulgadorPJe.getIdOrgaoJulgador());
		orgaoJulgador.setDescricao(orgaoJulgadorPJe.getOrgaoJulgador());
		for (br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo ojc : orgaoJulgadorPJe
				.getOrgaoJulgadorCargoList()) {
			if (ojc.getAtivo() && ojc.getCargo().getAtivo()) {
				orgaoJulgador.getCargosJudiciais().add(toCargoJudicial(ojc));
			}
		}

		return orgaoJulgador;
	}

	public static CargoJudicial toCargoJudicial(
			br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo orgaoJudicialPJe) {
		CargoJudicial cargoJudicial = new CargoJudicial();
		cargoJudicial.setId(orgaoJudicialPJe.getIdOrgaoJulgadorCargo());
		cargoJudicial.setDescricao(orgaoJudicialPJe.getCargo().getCargo());
		return cargoJudicial;
	}

	public static OrgaoJulgadorColegiado toOrgaoJulgadorColegiado(
			br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado orgaoJulgadorColegiadoPJe) {
		OrgaoJulgadorColegiado orgaoJulgadorColegiado = new OrgaoJulgadorColegiado();
		orgaoJulgadorColegiado.setId(orgaoJulgadorColegiadoPJe
				.getIdOrgaoJulgadorColegiado());
		orgaoJulgadorColegiado.setDescricao(orgaoJulgadorColegiadoPJe
				.getOrgaoJulgadorColegiado());
		return orgaoJulgadorColegiado;
	}

	public static ClasseJudicial toClasseJudicial(
			br.jus.pje.nucleo.entidades.ClasseJudicial classeJudicialPJe) {
		ClasseJudicial classeJudicial = new ClasseJudicial();
		classeJudicial.setCodigo(classeJudicialPJe.getCodClasseJudicial());
		classeJudicial.setDescricao(classeJudicialPJe.getClasseJudicial());
		classeJudicial.setRecursal(classeJudicialPJe.getRecursal());
		classeJudicial.setExigePoloPassivo(classeJudicialPJe.getReclamaPoloPassivo());
		List<TipoParteConfigClJudicial> tiposParteConfigClJudicial = classeJudicialPJe.getTipoParteConfigClJudicial();
		br.jus.pje.nucleo.entidades.TipoParte tipoParteAtivo = obterTipoParteAtivo(tiposParteConfigClJudicial);
		br.jus.pje.nucleo.entidades.TipoParte tipoPartePassivo = obterTipoPartePassivo(tiposParteConfigClJudicial);
		
		classeJudicial.setTipoPartePoloAtivo(toTipoParte(tipoParteAtivo));
		classeJudicial.setTipoPartePoloPassivo(toTipoParte(tipoPartePassivo));
		classeJudicial.setRemessaInstancia(classeJudicialPJe.getRemessaInstancia());

		return classeJudicial;
	}

	private static br.jus.pje.nucleo.entidades.TipoParte obterTipoPartePassivo(
			List<TipoParteConfigClJudicial> tiposParteConfigClJudicial) {
		br.jus.pje.nucleo.entidades.TipoParte tipoPartePassivo = new br.jus.pje.nucleo.entidades.TipoParte();
		for (TipoParteConfigClJudicial tipoConfig : tiposParteConfigClJudicial) {
			if (tipoConfig.getTipoParteConfiguracao().getTipoParte().getTipoPrincipal()) {
				if(tipoConfig.getTipoParteConfiguracao().getPoloPassivo() != null && tipoConfig.getTipoParteConfiguracao().getPoloPassivo()){
					tipoPartePassivo = tipoConfig.getTipoParteConfiguracao().getTipoParte();
					break;
				}
			}
		}
		return tipoPartePassivo;
	}

	private static br.jus.pje.nucleo.entidades.TipoParte obterTipoParteAtivo(
			List<TipoParteConfigClJudicial> tiposParteConfigClJudicial) {
		br.jus.pje.nucleo.entidades.TipoParte tipoParteAtivo = new br.jus.pje.nucleo.entidades.TipoParte();
		for (TipoParteConfigClJudicial tipoConfig : tiposParteConfigClJudicial) {
			if (tipoConfig.getTipoParteConfiguracao().getTipoParte().getTipoPrincipal()) {
				if(tipoConfig.getTipoParteConfiguracao().getPoloAtivo() != null && tipoConfig.getTipoParteConfiguracao().getPoloAtivo()){
					tipoParteAtivo = tipoConfig.getTipoParteConfiguracao().getTipoParte();
					break;
				}
			}
		}
		return tipoParteAtivo;
	}
	
	public static TipoParte toTipoParte(br.jus.pje.nucleo.entidades.TipoParte tipoParteEntidade){
		TipoParte tp = new TipoParte();
		tp.setDescTipoParte(tipoParteEntidade.getTipoParte());
		tp.setIdTipoParte(tipoParteEntidade.getIdTipoParte());
		return tp;
	}
	

	public static AssuntoJudicial toAssuntoJudicial(
			br.jus.pje.nucleo.entidades.AssuntoTrf assuntoJudicialPJe) {
		AssuntoJudicial assuntoJudicial = new AssuntoJudicial();
		assuntoJudicial.setCodigo(assuntoJudicialPJe.getCodAssuntoTrf());
		assuntoJudicial.setDescricao(assuntoJudicialPJe.getAssuntoTrf());
		assuntoJudicial.setComplementar(assuntoJudicialPJe.getComplementar());
		return assuntoJudicial;
	}

	public static Competencia toCompetencia(
			br.jus.pje.nucleo.entidades.Competencia competenciaPJe) {
		Competencia competencia = new Competencia();
		competencia.setId(competenciaPJe.getIdCompetencia());
		competencia.setDescricao(competenciaPJe.getCompetencia());
		return competencia;
	}

	public static TipoAudiencia toTipoAudiencia(
			br.jus.pje.nucleo.entidades.TipoAudiencia TipoAudienciaPJe) {
		TipoAudiencia tipoAudiencia = new TipoAudiencia();
		tipoAudiencia.setId(TipoAudienciaPJe.getIdTipoAudiencia());
		tipoAudiencia.setDescricao(TipoAudienciaPJe.getTipoAudiencia());
		return tipoAudiencia;
	}

	public static TipoDocumentoProcessual toTipoDocumentoProcessual(
			br.jus.pje.nucleo.entidades.TipoProcessoDocumento tipoDocumentoProcessualPJe) {
		Transformer conversor = novoConversorDeTipoProcessoDocumentoParaTipoDocumentoProcessual();
		return (TipoDocumentoProcessual) conversor.transform(tipoDocumentoProcessualPJe);
	}

	public static Fluxo getFluxo(
			br.jus.pje.nucleo.entidades.ClasseJudicial classeJudicialPJe) {
		Fluxo fluxo = new Fluxo();
		fluxo.setId(classeJudicialPJe.getFluxo().getIdFluxo());
		fluxo.setCodClasseJudicial(classeJudicialPJe.getCodClasseJudicial());
		fluxo.setXml(classeJudicialPJe.getFluxo().getXml());

		return fluxo;
	}

	public static SalaAudiencia toSalaAudiencia(Sala salaPJe,
			OrgaoJulgador orgaoJulgador) {

		SalaAudiencia sala = new SalaAudiencia();
		sala.setId(salaPJe.getIdSala());
		sala.setIgnoraFeriado(salaPJe.getIgnoraFeriado());
		sala.setOrgaoJulgador(orgaoJulgador);
		sala.setNome(salaPJe.getSala());
		sala.setTipoSala(SalaEnum.valueOf(salaPJe.getTipoSala().name()));

		for (br.jus.pje.nucleo.entidades.TipoAudiencia tipoAudienciaPJe : salaPJe
				.getTipoAudienciaList()) {
			TipoAudiencia tipoAudiencia = new TipoAudiencia();
			tipoAudiencia.setDescricao(tipoAudienciaPJe.getTipoAudiencia());
			tipoAudiencia.setId(tipoAudienciaPJe.getIdTipoAudiencia());
			sala.getTipoAudienciaList().add(tipoAudiencia);
		}

		for (br.jus.pje.nucleo.entidades.SalaHorario salaHorarioPJe : salaPJe
				.getSalaHorarioList()) {
			SalaHorario salaHorario = new SalaHorario();
			salaHorario.setDiaSemama(DiaSemanaEnum.values()[salaHorarioPJe
					.getDiaSemana().getIdDiaSemana() - 1]);
			salaHorario.setHoraInicial(salaHorarioPJe.getHoraInicial());
			salaHorario.setHoraFinal(salaHorarioPJe.getHoraFinal());
			sala.getSalaHorarioList().add(salaHorario);
		}

		return sala;
	}
	
	/**
	 * Converte uma PrioridadeProcesso do PJe para uma PrioridadeProcesso do MNI.
	 * 
	 * @param prioridade PrioridadeProcesso do PJe
	 * @return PrioridadeProcesso do MNI
	 */
	public static PrioridadeProcesso toPrioridadeProcesso(br.jus.pje.nucleo.entidades.PrioridadeProcesso prioridade) {
		PrioridadeProcesso resultado = null;
		
		if (prioridade != null) {
			resultado = new PrioridadeProcesso();
			resultado.setId(prioridade.getIdPrioridadeProcesso());
			resultado.setDescricao(prioridade.getPrioridade());
		}
		return resultado;
	}

	@SuppressWarnings("unchecked")
	public static List<TipoDocumentoProcessual> toTipoDocumentoProcessual(List<TipoProcessoDocumento> tipos) {
		return (List<TipoDocumentoProcessual>) CollectionUtils.collect(
				tipos, 
				novoConversorDeTipoProcessoDocumentoParaTipoDocumentoProcessual());
	}
	
	@SuppressWarnings("unchecked")
	public static List<Papel> toPapel(List<UsuarioLocalizacao> localizacoes) {
		return (List<Papel>) CollectionUtils.collect(
				localizacoes, 
				novoConversorDeUsuarioLocalizacaoParaPapel());
	}

	/**
	 * Cria um ProcessoTrf com os parâmetros passados por parâmetro.
	 * 
	 * @param jurisdicao Jurisdição.
	 * @param classe Classe.
	 * @param assuntos Lista de assuntos.
	 * @return ProcessoTrf
	 * @throws PJeException
	 */
	public static ProcessoTrf novoProcessoTrf(Jurisdicao jurisdicao, ClasseJudicial classe, List<AssuntoJudicial> assuntos) throws PJeException {
		Integer numeroOrigemJurisdicao = (jurisdicao != null ? jurisdicao.getId() : null);
		String codigoClasse = (classe != null ? classe.getCodigo() : null);
		
		br.jus.pje.nucleo.entidades.Jurisdicao pjeJurisdicao = JurisdicaoManager.instance().obterPorNumeroOrigem(StringUtil.toString(numeroOrigemJurisdicao));
		br.jus.pje.nucleo.entidades.ClasseJudicial pjeClasseJudicial = ClasseJudicialManager.instance().findByCodigo(codigoClasse);
		AssuntoTrfManager assuntoTrfManager = AssuntoTrfManager.instance();
		List<AssuntoTrf> pjeAssuntos = new ArrayList<AssuntoTrf>();
		for (AssuntoJudicial assunto : assuntos) {
			String codigoAssunto = (assunto != null ? assunto.getCodigo() : null);
			AssuntoTrf pjeAssunto = assuntoTrfManager.findByCodigo(codigoAssunto);
			if (pjeAssunto != null) {
				pjeAssuntos.add(pjeAssunto);
			}
		}
		
		ProcessoTrf processo = new ProcessoTrf();
		processo.setJurisdicao(pjeJurisdicao);
		processo.setClasseJudicial(pjeClasseJudicial);
		processo.setAssuntoTrfList(pjeAssuntos);
		return processo;
	}
	
	/**
	 * Retorna o conversor de TipoProcessoDocumento para TipoDocumentoProcessual.
	 * 
	 * @return Novo conversor.
	 */
	private static Transformer novoConversorDeTipoProcessoDocumentoParaTipoDocumentoProcessual() {
		return new Transformer() {

			@Override
			public Object transform(Object objeto) {
				TipoProcessoDocumento objetoEntrada = (TipoProcessoDocumento) objeto;
				
				TipoDocumentoProcessual objetoSaida = new TipoDocumentoProcessual();
				objetoSaida.setCodigo(objetoEntrada.getCodigoDocumento());
				objetoSaida.setDescricao(objetoEntrada.getTipoProcessoDocumento());
				
				return objetoSaida;
			}};
	}

	/**
	 * Retorna o conversor de UsuarioLocalizacao para Papel.
	 * 
	 * @return Novo conversor.
	 */
	private static Transformer novoConversorDeUsuarioLocalizacaoParaPapel() {
		return new Transformer() {
			
			@Override
			public Object transform(Object objeto) {
				UsuarioLocalizacao objetoEntrada = (UsuarioLocalizacao) objeto;
				
				Papel objetoSaida = new Papel();
				objetoSaida.setIdentificador(String.valueOf(objetoEntrada.getIdUsuarioLocalizacao()));
				objetoSaida.setNome(objetoEntrada.toString());
				
				return objetoSaida;
			}};
	}
}
