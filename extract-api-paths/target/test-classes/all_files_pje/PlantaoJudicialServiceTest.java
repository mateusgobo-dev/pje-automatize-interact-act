package br.jus.cnj.pje.servicos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import br.jus.csjt.pje.business.service.PlantaoJudicialService;
import br.jus.pje.nucleo.entidades.CalendarioEvento;
import br.jus.pje.nucleo.entidades.Estado;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.JurisdicaoMunicipio;
import br.jus.pje.nucleo.entidades.Municipio;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.enums.AbrangenciaEnum;

public class PlantaoJudicialServiceTest {

	PlantaoJudicialService plantaoJudicialService;

	@Before
	public void prepararTest() {
		plantaoJudicialService = new PlantaoJudicialService();
	}

	@Test
	public void verificarPlantao_orgaoJulgadorNaoDefinido() {
		OrgaoJulgador orgaoJulgadorPlantao = null;
		OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao = null;

		Calendar calendar = Calendar.getInstance();
		Date data = calendar.getTime();

		assertFalse(plantaoJudicialService.verificarPlantao(
				orgaoJulgadorPlantao, orgaoJulgadorColegiadoPlantao, getJurisdicao(), getFeriados(null, null, null, null, null, null), "19:00", "08:00", data, null, null, null));
	}

	@Test
	public void verificarPlantao_orgaoJulgadorInativo() {
		OrgaoJulgador orgaoJulgadorPlantao = new OrgaoJulgador();
		orgaoJulgadorPlantao.setAtivo(Boolean.FALSE);

		OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao = new OrgaoJulgadorColegiado();
		orgaoJulgadorColegiadoPlantao.setAtivo(Boolean.FALSE);

		Calendar calendar = Calendar.getInstance();
		Date data = calendar.getTime();

		assertFalse(plantaoJudicialService.verificarPlantao(
				orgaoJulgadorPlantao, orgaoJulgadorColegiadoPlantao, getJurisdicao(), getFeriados(null, null, null, null, null, null), "19:00", "08:00", data, null, null, null));
	}

	@Test
	public void verificarPlantao_protocoloFinalDeSemana() {
		OrgaoJulgador orgaoJulgadorPlantao = new OrgaoJulgador();
		orgaoJulgadorPlantao.setAtivo(Boolean.TRUE);

		OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao = new OrgaoJulgadorColegiado();
		orgaoJulgadorColegiadoPlantao.setAtivo(Boolean.FALSE);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
		Date data = calendar.getTime();

		assertTrue(plantaoJudicialService.verificarPlantao(
				orgaoJulgadorPlantao, orgaoJulgadorColegiadoPlantao, getJurisdicao(), getFeriados(null, null, null, null, null, null), "19:00", "08:00", data, null, null, null));
	}

	@Test
	public void verificarPlantao_protocoloDataFeriado() {
		OrgaoJulgador orgaoJulgadorPlantao = new OrgaoJulgador();
		orgaoJulgadorPlantao.setAtivo(Boolean.TRUE);

		OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao = new OrgaoJulgadorColegiado();
		orgaoJulgadorColegiadoPlantao.setAtivo(Boolean.TRUE);

		Calendar calendar = Calendar.getInstance();
		Date data = calendar.getTime();

		assertTrue(plantaoJudicialService.verificarPlantao(
				orgaoJulgadorPlantao, orgaoJulgadorColegiadoPlantao, getJurisdicao(), getFeriados(calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, null, null, null, null), "19:00", "08:00", data, null, null, null));
	}

	@Test
	public void verificarPlantao_protocoloHorarioPlantao() {
		OrgaoJulgador orgaoJulgadorPlantao = new OrgaoJulgador();
		orgaoJulgadorPlantao.setAtivo(Boolean.TRUE);

		OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao = new OrgaoJulgadorColegiado();
		orgaoJulgadorColegiadoPlantao.setAtivo(Boolean.TRUE);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		calendar.set(Calendar.MINUTE, 45);
		Date data = calendar.getTime();

		assertTrue(plantaoJudicialService.verificarPlantao(
				orgaoJulgadorPlantao, orgaoJulgadorColegiadoPlantao, getJurisdicao(), getFeriados(19, 10, null, null, null, null), "19:00", "08:00", data, null, null, null));
	}

	@Test
	public void verificarPlantao_protocoloHorarioPlantaoValidaDiaAnteriorPosterior_Sexta() {
		OrgaoJulgador orgaoJulgadorPlantao = new OrgaoJulgador();
		orgaoJulgadorPlantao.setAtivo(Boolean.TRUE);

		OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao = new OrgaoJulgadorColegiado();
		orgaoJulgadorColegiadoPlantao.setAtivo(Boolean.TRUE);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		calendar.set(Calendar.MINUTE, 45);
		Date data = calendar.getTime();

		assertTrue(plantaoJudicialService.verificarPlantao(
				orgaoJulgadorPlantao, orgaoJulgadorColegiadoPlantao, getJurisdicao(), getFeriados(19, 10, null, null, null, null), "19:00", "08:00", data, "true", null, null));
	}

	@Test
	public void verificarPlantao_protocoloHorarioPlantaoValidaDiaAnteriorPosterior_Quarta() {
		OrgaoJulgador orgaoJulgadorPlantao = new OrgaoJulgador();
		orgaoJulgadorPlantao.setAtivo(Boolean.TRUE);

		OrgaoJulgadorColegiado orgaoJulgadorColegiadoPlantao = new OrgaoJulgadorColegiado();
		orgaoJulgadorColegiadoPlantao.setAtivo(Boolean.TRUE);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 20);
		calendar.set(Calendar.MINUTE, 45);
		Date data = calendar.getTime();

		assertFalse(plantaoJudicialService.verificarPlantao(
				orgaoJulgadorPlantao, orgaoJulgadorColegiadoPlantao, getJurisdicao(), getFeriados(19, 10, null, null, null, null), "19:00", "08:00", data, "true", null, null));
	}

	private List<CalendarioEvento> getFeriados(Integer diaInicio, Integer mesInicio, Integer anoInicio,
											   Integer diaFim, Integer mesFim, Integer anoFim) {

		List<CalendarioEvento> resultado = new ArrayList<>(0);

		CalendarioEvento calendarioEvento = new CalendarioEvento();
		calendarioEvento.setAtivo(Boolean.TRUE);
		calendarioEvento.setInFeriado(Boolean.TRUE);
		calendarioEvento.setInAbrangencia(AbrangenciaEnum.N);
		calendarioEvento.setDtDia(diaInicio);
		calendarioEvento.setDtMes(mesInicio);
		calendarioEvento.setDtAno(anoInicio);
		calendarioEvento.setDtDiaFinal(diaFim);
		calendarioEvento.setDtMesFinal(mesFim);
		calendarioEvento.setDtAnoFinal(anoFim);

		resultado.add(calendarioEvento);

		return resultado;
	}

	private Jurisdicao getJurisdicao() {
		Jurisdicao jurisdicao = new Jurisdicao();

		Estado estado = new Estado();
		estado.setIdEstado(7); // DF

		Municipio municipio = new Municipio();
		municipio.setIdMunicipio(805);  // Brasilia

		JurisdicaoMunicipio jurisdicaoMunicipio = new JurisdicaoMunicipio();
		jurisdicaoMunicipio.setJurisdicao(jurisdicao);
		jurisdicaoMunicipio.setMunicipio(municipio);
		jurisdicaoMunicipio.setSede(Boolean.TRUE);

		jurisdicao.setEstado(estado);
		jurisdicao.setMunicipioList(Arrays.asList(jurisdicaoMunicipio));

		return jurisdicao;
	}

}
