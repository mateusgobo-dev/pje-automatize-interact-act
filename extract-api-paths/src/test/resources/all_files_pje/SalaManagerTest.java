package br.com.infox.test.cliente.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.com.jt.pje.manager.SalaManager;
import br.jus.pje.jt.entidades.PeriodoDeInatividadeDaSala;
import br.jus.pje.nucleo.entidades.BloqueioPauta;
import br.jus.pje.nucleo.entidades.DiaSemana;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaHorario;

public class SalaManagerTest {

	private SalaManager salaManager;
	private Sala sala;
	
	@Before
	public void setup() {			
		salaManager = new SalaManager();
		
		sala = new Sala();
		
		// Configura as datas dos períodos de inatividade (de 15/06/2013 a 25/06/2013)		
		List<PeriodoDeInatividadeDaSala> inatividadeList = configuraInatividadesSala();
		sala.setPeriodoDeInatividadeList(inatividadeList);
		
		// Configura as datas de bloqueio de pauta (de 01/06/2013 a 10/06/2013)
		List<BloqueioPauta> bloqueioList = configuraBloqueiosPauta();
		sala.setBloqueioPautaList(bloqueioList);
		
		//Configura horários da sala (Segunda e Quarta de 14:00 às 16:00)
		List<SalaHorario> horarioList = configuraHorariosSala();
		sala.setSalaHorarioList(horarioList);		
			
	}			
	
	/**
	 * Método getDataForaDeInatividades()
	 * Usando a data 15/06/2013, deve retornar 26/06/2013
	 */
	@Test
	public void dataEmPeriodoDeInatividadeDeveRetornarPrimeiraDataAposInatividade() {
		// Data dentro da inatividade.
		Calendar data = Calendar.getInstance();
		data.set(2013, 5, 15); 
		
		// Data da primeira SEGUNDA FEIRA fora das inatividades.
		Calendar retornoEsperado = Calendar.getInstance();				
		retornoEsperado.set(2013, 5, 26); 
		
		Calendar retorno = salaManager.getDataForaDeInatividades(sala, data);
		
		assertEquals(retornoEsperado.get(Calendar.YEAR), retorno.get(Calendar.YEAR));
		assertEquals(retornoEsperado.get(Calendar.MONTH), retorno.get(Calendar.MONTH));
		assertEquals(retornoEsperado.get(Calendar.DAY_OF_MONTH), retorno.get(Calendar.DAY_OF_MONTH));
	}
	

	/**
	 * Método getDataForaDeInatividades()
	 * Usando a data 01/07/2013, deve retornar 01/07/2013
	 */
	@Test
	public void dataDepoisDePeriodoDeInatividadeDeveRetornarPrimeiraDataQuePossuaHorarioNaSala() {
		// Data fora de inatividades.
		Calendar data = Calendar.getInstance();
		data.set(2013, 6, 1);
		
		//
		Calendar retornoEsperado = Calendar.getInstance();				 
		retornoEsperado.set(2013, 6, 1);
		
		Calendar retorno = salaManager.getDataForaDeInatividades(sala, data);
		
		assertEquals(retornoEsperado.get(Calendar.YEAR), retorno.get(Calendar.YEAR));
		assertEquals(retornoEsperado.get(Calendar.MONTH), retorno.get(Calendar.MONTH));
		assertEquals(retornoEsperado.get(Calendar.DAY_OF_MONTH), retorno.get(Calendar.DAY_OF_MONTH));
	}
	

	/**
	 * Método getDataForaDeInatividades()
	 * Usando a data 11/06/2013, deve retornar 12/06/2013
	 */
	@Test
	public void dataAntesDePeriodoDeInatividadeDeveRetornarPrimeiraDataQueTemHorarioNaSala() {
		// data fora de inatividades.
		Calendar data = Calendar.getInstance();
		data.set(2013, 05, 11);
		
		Calendar retornoEsperado = Calendar.getInstance();			
		retornoEsperado.set(2013, 5, 12);
		
		Calendar retorno = salaManager.getDataForaDeInatividades(sala, data);
		
		assertEquals(retornoEsperado.get(Calendar.YEAR), retorno.get(Calendar.YEAR));
		assertEquals(retornoEsperado.get(Calendar.MONTH), retorno.get(Calendar.MONTH));
		assertEquals(retornoEsperado.get(Calendar.DAY_OF_MONTH), retorno.get(Calendar.DAY_OF_MONTH));
	}
	
	/**
	 * Método getDataForaDeBloqueios()
	 * Usando a data 03/06/2013, deve retornar 12/06/2013
	 */
	@Test
	public void dataEmUmBloqueioDeveRetornarPrimeiraDataForaDoBloqueioQueTemHorarioNaSala() {		
		Calendar dataNoBloqueio = Calendar.getInstance();
		dataNoBloqueio.set(2013, 5, 3);
		
		Calendar retornoEsperado = Calendar.getInstance();
		retornoEsperado.set(2013, 5, 12);
		
		Calendar retorno = salaManager.getDataForaDeBloqueios(sala, dataNoBloqueio);
		
		assertEquals(retornoEsperado.get(Calendar.YEAR), retorno.get(Calendar.YEAR));
		assertEquals(retornoEsperado.get(Calendar.MONTH), retorno.get(Calendar.MONTH));
		assertEquals(retornoEsperado.get(Calendar.DAY_OF_MONTH), retorno.get(Calendar.DAY_OF_MONTH));
		
	}
	
	/**
	 * Método dataForaDeBloqueioDeveRetornarADataMaisProximaQueTemHorarioNaSala()
	 * Usando a data 10/06/2013 15:01, deve retornar true
	 */
	@Test
	public void dataForaDeBloqueioDeveRetornarTrue(){		
		Calendar dataForaDoBloqueio = Calendar.getInstance();
		dataForaDoBloqueio.set(2013, 5, 10);
		dataForaDoBloqueio.set(Calendar.HOUR_OF_DAY, 15);
		dataForaDoBloqueio.set(Calendar.MINUTE, 1);
		dataForaDoBloqueio.set(Calendar.SECOND, 0);
		
		Boolean retornoEsperado = true;
		
		Boolean retorno = salaManager.isDataForaDeBloqueios(sala, dataForaDoBloqueio, 40);
		
		assertEquals(retornoEsperado, retorno);
	}
	
	/**
	 * Método dataForaDeBloqueioDeveRetornarADataMaisProximaQueTemHorarioNaSala()
	 * Usando a data 10/06/2013 14:59, deve retornar false
	 */
	@Test
	public void dataDentroDeBloqueioDeveRetornarFalse(){		
		Calendar dataForaDoBloqueio = Calendar.getInstance();
		dataForaDoBloqueio.set(2013, 5, 10);
		dataForaDoBloqueio.set(Calendar.HOUR_OF_DAY, 14);
		dataForaDoBloqueio.set(Calendar.MINUTE, 59);
		dataForaDoBloqueio.set(Calendar.SECOND, 0);
		
		Boolean retornoEsperado = false;
		
		Boolean retorno = salaManager.isDataForaDeBloqueios(sala, dataForaDoBloqueio, 40);
		
		assertEquals(retornoEsperado, retorno);
	}
	
	
	/**
	 * Método isSalaAberta()
	 * Usando uma TERÇA (04/06/2013), sendo que a sala só tem horários SEGUNDA e QUARTA.
	 */
	@Test
	public void salaFechadaNoDiaDaSemanaDeveRetornarFalse() {
		Calendar dataInicioSemHorarioNaSala = Calendar.getInstance();
		dataInicioSemHorarioNaSala.set(2013, 05, 04);
		
		Calendar dataFimSemHorarioNaSala = Calendar.getInstance();
		dataFimSemHorarioNaSala.set(2013, 05, 04);
		
		assertFalse("Não existe horário na sala nas terças, deveria retornar false.", salaManager.isSalaAberta(sala, dataInicioSemHorarioNaSala, dataFimSemHorarioNaSala));
	}
	
	/**
	 * Método isSalaAberta()
	 * Usando uma SEGUNDA (03/06/2013 12:00-14:00), sendo que a sala só tem horários SEGUNDA e QUARTA 14:00-16:00.
	 */
	@Test
	public void salaFechadaNoHorarioDeveRetornarFalse() {
		Calendar dataInicioSemHorarioNaSala = Calendar.getInstance();
		dataInicioSemHorarioNaSala.set(2013, 05, 03, 12, 0, 0);
		
		Calendar dataFimSemHorarioNaSala = Calendar.getInstance();
		dataFimSemHorarioNaSala.set(2013, 05, 03, 14, 0, 0);
		
		assertFalse("Não existe horário na sala na segunda de 12:00 às 14:00, deveria retornar false.", salaManager.isSalaAberta(sala, dataInicioSemHorarioNaSala, dataFimSemHorarioNaSala));
	}
	
	/**
	 * Método isSalaAberta()
	 * Usando uma SEGUNDA (03/06/2013 14:00-15:59), sendo que a sala só tem horários SEGUNDA e QUARTA 14:00-16:00.
	 */
	@Test
	public void salaAbertaNoHorarioDeveRetornarTrue() {
		Calendar dataInicioComHorarioNaSala = Calendar.getInstance();
		dataInicioComHorarioNaSala.set(2013, 05, 03, 14, 0, 0);
		
		Calendar dataFimComHorarioNaSala = Calendar.getInstance();
		dataFimComHorarioNaSala.set(2013, 05, 03, 15, 59, 59);
		
		assertTrue("Existe horário na sala na segunda de 14:00 às 16:00, deveria retornar true.", salaManager.isSalaAberta(sala, dataInicioComHorarioNaSala, dataFimComHorarioNaSala));
	}
	
	
	/**
	 * Método getDataForaDeBloqueios()
	 * Usando a data 03/06/2013, deve retornar 12/06/2013
	 */
	@Test
	public void dataEmUmBloqueioDeveRetornarOProximoHorarioDisponivel() {		
		Calendar dataNoBloqueio = Calendar.getInstance();
		dataNoBloqueio.set(2013, 5, 3);
		
		Calendar retornoEsperado = Calendar.getInstance();
		retornoEsperado.set(2013, 5, 12);
		
		Calendar retorno = salaManager.getDataForaDeBloqueios(sala, dataNoBloqueio);
		
		assertEquals(retornoEsperado.get(Calendar.YEAR), retorno.get(Calendar.YEAR));
		assertEquals(retornoEsperado.get(Calendar.MONTH), retorno.get(Calendar.MONTH));
		assertEquals(retornoEsperado.get(Calendar.DAY_OF_MONTH), retorno.get(Calendar.DAY_OF_MONTH));
		
	}
	
	
	/********************************** CONFIGURAÇÕES *****************************************/	
	// Configura as datas dos períodos de inatividade (de 15/06/2013 a 25/06/2013)
	private List<PeriodoDeInatividadeDaSala> configuraInatividadesSala() {
		List<PeriodoDeInatividadeDaSala> listaInatividades = new ArrayList<PeriodoDeInatividadeDaSala>();
		
		//Sala inativa de 15/06/2013 a 20/06/2013 
		Calendar inicioInatividade1 = Calendar.getInstance();
		Calendar fimInatividade1 = Calendar.getInstance();
		inicioInatividade1.set(2013, 05, 15);
		fimInatividade1.set(2013, 05, 20);
		
		PeriodoDeInatividadeDaSala inatividade1 = new PeriodoDeInatividadeDaSala();
		inatividade1.setAtivo(true);
		inatividade1.setInicio(inicioInatividade1.getTime());
		inatividade1.setTermino(fimInatividade1.getTime());
		
		listaInatividades.add(inatividade1);
		
		//Sala inativa de 21/06/2013 a 25/06/2013
		Calendar inicioInatividade2 = Calendar.getInstance();
		Calendar fimInatividade2 = Calendar.getInstance();
		inicioInatividade2.set(2013, 05, 21);
		fimInatividade2.set(2013, 05, 25);				
		
		PeriodoDeInatividadeDaSala inatividade2 = new PeriodoDeInatividadeDaSala();						
		inatividade2.setAtivo(true);
		inatividade2.setInicio(inicioInatividade2.getTime());
		inatividade2.setTermino(fimInatividade2.getTime());				
		
		listaInatividades.add(inatividade2);
		
		return listaInatividades;
	}

	// Configura as datas de bloqueio de pauta (de 01/06/2013 a 10/06/2013)
	public List<BloqueioPauta> configuraBloqueiosPauta() {
		List<BloqueioPauta> bloqueioList = new ArrayList<BloqueioPauta>();		
		
		Calendar dtBloqueioInicial = Calendar.getInstance();
		Calendar dtBloqueioFinal = Calendar.getInstance();
		dtBloqueioInicial.set(2013, 5, 1);
		dtBloqueioInicial.set(Calendar.HOUR_OF_DAY, 14);		
		dtBloqueioInicial.set(Calendar.MINUTE, 0);
		dtBloqueioInicial.set(Calendar.SECOND, 0);
		
		dtBloqueioFinal.set(2013, 5, 10);
		dtBloqueioFinal.set(Calendar.HOUR_OF_DAY, 15);
		dtBloqueioFinal.set(Calendar.MINUTE, 0);
		dtBloqueioFinal.set(Calendar.SECOND, 0);
		
		BloqueioPauta bp = new BloqueioPauta();
		bp.setAtivo(true);
		bp.setDtInicial(dtBloqueioInicial.getTime());
		bp.setDtFinal(dtBloqueioFinal.getTime());
		bloqueioList.add(bp);		
		
		return bloqueioList;
	}		
	
	
	//Configura horários da sala (Segunda e Quarta de 14:00 às 16:00)
	public List<SalaHorario> configuraHorariosSala() {			
		List<SalaHorario> horarioList = new ArrayList<SalaHorario>();

		// Configura horários da sala
		SalaHorario sh1 = new SalaHorario();
		Calendar horaInicial1 = Calendar.getInstance();
		Calendar horaFinal1 = Calendar.getInstance();

		// Configura perí­odo SEGUNDA 14:00 às 16:00
		horaInicial1.set(Calendar.HOUR_OF_DAY, 14);
		horaInicial1.set(Calendar.MINUTE, 0);
		horaInicial1.set(Calendar.SECOND, 0);
		horaFinal1.set(Calendar.HOUR_OF_DAY, 16);
		horaFinal1.set(Calendar.MINUTE, 0);
		horaFinal1.set(Calendar.SECOND, 0);

		sh1.setAtivo(true);
		DiaSemana ds = new DiaSemana();
		ds.setIdDiaSemana(2);
		sh1.setDiaSemana(ds);
		sh1.setHoraInicial(horaInicial1.getTime());
		sh1.setHoraFinal(horaFinal1.getTime());

		horarioList.add(sh1);

		SalaHorario sh2 = new SalaHorario();
		Calendar horaInicial2 = Calendar.getInstance();
		Calendar horaFinal2 = Calendar.getInstance();

		// Configura perí­odo QUARTA 14:00 às 16:00
		horaInicial2.set(Calendar.HOUR_OF_DAY, 14);
		horaInicial2.set(Calendar.MINUTE, 0);
		horaInicial2.set(Calendar.SECOND, 0);
		horaFinal2.set(Calendar.HOUR_OF_DAY, 16);
		horaFinal2.set(Calendar.MINUTE, 0);
		horaFinal2.set(Calendar.SECOND, 0);

		sh2.setAtivo(true);
		DiaSemana ds2 = new DiaSemana();
		ds2.setIdDiaSemana(4);
		sh2.setDiaSemana(ds2);
		sh2.setHoraInicial(horaInicial2.getTime());
		sh2.setHoraFinal(horaFinal2.getTime());		

		horarioList.add(sh2);

		return horarioList;
	}
}
