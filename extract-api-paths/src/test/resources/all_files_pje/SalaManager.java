package br.com.jt.pje.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.core.manager.GenericManager;
import br.com.jt.pje.dao.SalaDAO;
import br.jus.pje.jt.entidades.PeriodoDeInatividadeDaSala;
import br.jus.pje.nucleo.entidades.BloqueioPauta;
import br.jus.pje.nucleo.entidades.DiaSemana;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Sala;
import br.jus.pje.nucleo.entidades.SalaHorario;
import br.jus.pje.nucleo.entidades.TipoAudiencia;
import br.jus.pje.nucleo.util.DateUtil;

@Name(SalaManager.NAME)
@Scope(ScopeType.EVENT)
@AutoCreate
public class SalaManager extends GenericManager{

	private static final long serialVersionUID = 8474732361291813090L;

	public static final String NAME = "salaManager";
	public static final int TODOS_OS_DIAS = 8;
	
	@In
	private SalaDAO salaDAO;
	
	public List<Sala> getSalaSessaoItems(){
		return salaDAO.getSalaSessaoItems();
	}
	
	public List<Sala> getSalaSessaoItemsByOrgaoJulgadorColegiado(OrgaoJulgadorColegiado ojc){
		return salaDAO.getSalaSessaoItemsByOrgaoJulgadorColegiado(ojc);
	}

	public List<Sala> getSalaByPeriodoAudienciaAndTipoAudiencia(Date dataInicial, Date dataFinal, TipoAudiencia tipoAudiencia, 
			OrgaoJulgador orgaoJulgador){
		return salaDAO.getSalaByPeriodoAudienciaAndTipoAudiencia(dataInicial, dataFinal, tipoAudiencia, orgaoJulgador);
	}

	/**  
	 * Encontra primeira data em que a sala esteja disponí­vel para agendamento automatico a partir da data de pesquisa. 
	 * Se a data de pesquisa nao estiver em periodo de indisponibilidade, retorna ela mesma.
	 *
	 * @param sala
	 * @param dataSala
	 * @return Calendar
	 */
	public Calendar getDataForaDeInatividades(Sala sala, Calendar dataSala) {
		List<SalaHorario> salaHorarioList = sala.getSalaHorarioList();	
		List<PeriodoDeInatividadeDaSala> periodoDeInatividadeList = sala.getPeriodoDeInatividadeList();

		if (salaHorarioList != null && salaHorarioList.size() > 0) {
			int salaHorarioIndex = inicializaDataNoDiaMaisProximoCompativelDaSala(salaHorarioList, dataSala);
			int dataSalaDia = dataSala.get(Calendar.DAY_OF_WEEK);
			int salaHorarioDia = salaHorarioList.get(salaHorarioIndex).getDiaSemana().getIdDiaSemana();

			dataSala.add(Calendar.DAY_OF_MONTH, diasASomarParaChegarEmUmProximoDiaDaSemana(dataSalaDia, salaHorarioDia));
			salaHorarioIndex++;

			if (salaHorarioIndex >= salaHorarioList.size()) {
				salaHorarioIndex = 0;
			}

			for (PeriodoDeInatividadeDaSala periodo : periodoDeInatividadeList) {
				if (periodo.getAtivo()) {
					if (periodo.getInicio().after(dataSala.getTime())) {
						break;
					}
					Date inicio = periodo.getInicio();
					Date termino = periodo.getTermino();
					if (DateUtil.isDataEntre(dataSala.getTime(), inicio, termino)) {
						dataSala.setTime(termino);
						dataSala.add(Calendar.DAY_OF_MONTH, 1);

						dataSalaDia = dataSala.get(Calendar.DAY_OF_WEEK);
						salaHorarioDia = salaHorarioList.get(salaHorarioIndex).getDiaSemana().getIdDiaSemana();

						dataSala.add(Calendar.DAY_OF_MONTH, diasASomarParaChegarEmUmProximoDiaDaSemana(dataSalaDia, salaHorarioDia));
						salaHorarioIndex++;

						if (salaHorarioIndex >= salaHorarioList.size()) {
							salaHorarioIndex = 0;
						}
					}
				}
			}
		} else {
			return null;
		}
		return dataSala;
	}

	/**
	 * Retorna a primeira data fora de bloqueio em que existe horÃ¡rio para marcaÃ§Ã£o de audiÃªncia
	 * @param dataSala data a ser verificada.
	 * @param sala sala de audiÃªncia.
	 * @return Retorna a primeira data fora dos bloqueios.
	 * @Deprecated Utilizar o método {{@link #verificaBloqueio(Sala, Calendar, int)}
	 */
	@Deprecated
	public Calendar getDataForaDeBloqueios(Sala sala, Calendar dataSala){
		List<SalaHorario> salaHorarioList = sala.getSalaHorarioList();	
		List<BloqueioPauta> bloqueioPautaList = sala.getBloqueioPautaList();

		// TODO Caso a sala nÃ£o tenha horÃ¡rio nenhum deve retornar null ou lanÃ§ar exceÃ§Ã£o.
		if (salaHorarioList != null && salaHorarioList.size() > 0) {
			int horarioIndex = inicializaDataNoDiaMaisProximoCompativelDaSala(salaHorarioList, dataSala);

			int dataSalaDia = dataSala.get(Calendar.DAY_OF_WEEK);
			int salaHorarioDia = salaHorarioList.get(horarioIndex).getDiaSemana().getIdDiaSemana();

			//Coloca a data no dia mais prÃ³ximo que tem horÃ¡rio na sala.
			dataSala.add(Calendar.DAY_OF_MONTH, diasASomarParaChegarEmUmProximoDiaDaSemana(dataSalaDia, salaHorarioDia));
			horarioIndex++;

			// Retorna para o primeiro dia da semana da lista de horÃ¡rios (simula lista circular). Evita ArrayIndexOutOfBoundException.
			if (horarioIndex >= salaHorarioList.size()) {
				horarioIndex = 0;
			}

			for (BloqueioPauta bloqueio : bloqueioPautaList){
				
				if(bloqueio.getAtivo()){
					if (bloqueio.getDtInicial().after(dataSala.getTime())) {
						break;
					}

					//Caso a data esteja no perÃ­odo de bloqueio, avanÃ§a o calendÃ¡rio para o primeiro dia apÃ³s a data final do bloqueio. 
					if (DateUtil.isDataEntre(dataSala.getTime(), bloqueio.getDtInicial(), bloqueio.getDtFinal())){
						dataSala.setTime(bloqueio.getDtFinal());
						dataSala.add(Calendar.DAY_OF_MONTH, 1);

						dataSalaDia = dataSala.get(Calendar.DAY_OF_WEEK);
						salaHorarioDia = salaHorarioList.get(horarioIndex).getDiaSemana().getIdDiaSemana();

						//Coloca a data no dia mais prÃ³ximo que tem horÃ¡rio na sala.
						dataSala.add(Calendar.DAY_OF_MONTH, diasASomarParaChegarEmUmProximoDiaDaSemana(dataSalaDia, salaHorarioDia));
						horarioIndex++;

						// Retorna para o primeiro dia da semana da lista de horÃ¡rios (simula lista circular). Evita ArrayIndexOutOfBoundException.
						if (horarioIndex >= salaHorarioList.size()) {
							horarioIndex = 0;
						}
					}
				}
			}
			
		} else {
			return null;
		}

		return dataSala;
	}

	public boolean isSalaAberta(Sala sala, Calendar dataInicio, Calendar dataFim) {
		int diaSemana = dataInicio.get(Calendar.DAY_OF_WEEK);
		
		for (SalaHorario salaHorario : sala.getSalaHorarioList()){
			if(salaHorario.getAtivo()){
				if (diaSemana == salaHorario.getDiaSemana().getIdDiaSemana()){

					dataInicio.set(0, 0, 0);

					Calendar aberturaSala = Calendar.getInstance();
					aberturaSala.setTime(salaHorario.getHoraInicial());
					aberturaSala.set(0, 0, 0);

					dataFim.set(0, 0, 0);

					Calendar fechamentoSala = Calendar.getInstance();
					fechamentoSala.setTime(salaHorario.getHoraFinal());
					fechamentoSala.set(0, 0, 0);

					//PerÃ­odo da audiÃªncia estÃ¡ contido no horÃ¡rio da sala.
					if (!dataInicio.before(aberturaSala) && !dataFim.after(fechamentoSala)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	

	/**
	 * Define o próximo dia da semana (sucedente ou igual ao dia de dtMarcacao) em que a sala tem horário compatí­vel com o tempo da audiência.
	 * 
	 * @param dtMarcacao
	 * @param horarioList
	 * @param tempoAudiencia
	 * @return true caso tenha conseguido achar um dia compatí­vel com o tempo de audiência. false caso em nenhum horário caiba a audiência.
	 */
	private int inicializaDataNoDiaMaisProximoCompativelDaSala(List<SalaHorario> salaHorarioList, Calendar dtMarcacao) {
		int diaMarcacao = dtMarcacao.get(Calendar.DAY_OF_WEEK);
		int somaFinal = 99;
		int indice = 0;
		int indiceDesejado = 0;
		for (SalaHorario salaHorario : salaHorarioList) {			
			DiaSemana diaSemanaSala = salaHorario.getDiaSemana();
			int diaSalaHorario = diaSemanaSala.getIdDiaSemana();
			int aux = diasASomarParaChegarEmUmProximoDiaDaSemana(diaMarcacao, diaSalaHorario);
			if (aux < somaFinal) {
				somaFinal = aux;
				indiceDesejado = indice;
			}
			indice++;
		}
		return indiceDesejado;
	}

	private int diasASomarParaChegarEmUmProximoDiaDaSemana(int anterior, int posterior) {
		int diferenca = posterior - anterior;
		int diasASomar = 0;
		if (diferenca >= 0) {
			diasASomar = diferenca;
		} else {
			diasASomar = anterior + diferenca + (7 - anterior);
		}
		return diasASomar;
	}
	
    public List<Sala> filtrarSalasPorOrgaoJulgador(List<String> salas, OrgaoJulgador orgaoJulgador) {
        List<Sala> salasOJ = orgaoJulgador.getSalaList();
        List<Sala> listaRetorno = new ArrayList<Sala>();
        for (String salaIndicada : salas) {
            for (Sala salaOrgaoJulgador : salasOJ) {
                if (salaIndicada.equals(salaOrgaoJulgador.getSala())) {
                    listaRetorno.add(salaOrgaoJulgador);
                }
            }
        }        
        return listaRetorno;
    }
    
	/**
	 * Retorna a primeira data fora dos bloqueios (1 minuto a mais).
	 * 
	 * @param sala
	 * @param horarioInicio
	 * @param tempoAudiencia
	 * @return Retorna a primeira data fora dos bloqueios.
	 */
	public Calendar verificaBloqueio(Sala sala, Calendar horarioInicio, int tempoAudiencia) {
		List<SalaHorario> salaHorarioList = sala.getSalaHorarioList();
		List<BloqueioPauta> bloqueioPautaList = sala.getBloqueioPautaList();
		Calendar dataForaDeBloqueio = null;
		if (sala != null && horarioInicio != null && salaHorarioList != null && salaHorarioList.size() > 0) {
			dataForaDeBloqueio = (Calendar) horarioInicio.clone();
			Calendar horarioFim = (Calendar) horarioInicio.clone();
			horarioFim.add(Calendar.MINUTE, tempoAudiencia);

			for (BloqueioPauta bloqueio : bloqueioPautaList) {
				if (bloqueio.getAtivo()) {
					if (DateUtil.isDataComHoraEntre(horarioInicio.getTime(), bloqueio.getDtInicial(), bloqueio.getDtFinal())
							|| DateUtil.isDataComHoraEntre(horarioFim.getTime(), bloqueio.getDtInicial(), bloqueio.getDtFinal())
							|| DateUtil.isDataComHoraEntre(bloqueio.getDtInicial(), horarioInicio.getTime(), horarioFim.getTime())
							|| DateUtil.isDataComHoraEntre(bloqueio.getDtFinal(), horarioInicio.getTime(), horarioFim.getTime())) {
						
						dataForaDeBloqueio = (Calendar) horarioInicio.clone();
						dataForaDeBloqueio.setTime(bloqueio.getDtFinal());
						
						horarioInicio = (Calendar) dataForaDeBloqueio.clone();
						horarioFim = (Calendar) horarioInicio.clone();
						horarioFim.add(Calendar.MINUTE, tempoAudiencia);
					}
				}
			}
		}
		return dataForaDeBloqueio;
	}

	/**
	 * Verifica se a data passada está em um período de bloqueio de pauta.
	 * @param sala
	 * @param horarioInicio
	 * @param tempoAudiencia
	 * @return isDataForaDeBloqueios
	 */
	public boolean isDataForaDeBloqueios(Sala sala, Calendar horarioInicio, int tempoAudiencia){
		Calendar dataForaDeBloqueios = verificaBloqueio(sala, horarioInicio, tempoAudiencia);
		
		if (dataForaDeBloqueios == null) {
			return true;
		}
		
		return horarioInicio.equals(dataForaDeBloqueios);
	}
	
	public boolean verificarSalaCadastrada(Sala sala, OrgaoJulgador orgaoJulgador) {
		return this.salaDAO.getSalaListByOrgaoJulgador(orgaoJulgador).contains(sala);
	}
	
	public List<Sala> recuperar(OrgaoJulgador orgaoJulgador, TipoAudiencia tipoAudiencia) {
		return this.salaDAO.recuperar(orgaoJulgador, tipoAudiencia);
	}
	
	public List<Sala> recuperarSalasAudienciaAtivas(Integer idTipoAudiencia, List<Integer> idsOrgaoJulgador) {
		return this.salaDAO.recuperarSalasAudienciaAtivas(idTipoAudiencia, new HashSet<Integer>(idsOrgaoJulgador));
	}
	
	/**
	 * Retorna a lista de horários de uma sala para um determinado dia da semana. 
	 */
	public List<SalaHorario> getSalaHorarioDiaList(Sala sala, int idDiaSemana) {
		List<SalaHorario> horariosDoDia = new ArrayList<SalaHorario>();
		for (SalaHorario salaHorario : sala.getSalaHorarioList()) {
			if (salaHorario.getAtivo() && 
					(salaHorario.getDiaSemana().getIdDiaSemana() == idDiaSemana || 
						salaHorario.getDiaSemana().getIdDiaSemana() == TODOS_OS_DIAS)) {
				
				horariosDoDia.add(salaHorario);				
			}
		}		
		return horariosDoDia;
	}

}
