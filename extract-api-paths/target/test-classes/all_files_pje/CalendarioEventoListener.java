/**
 * CalendarioListener.java
 * 
 * Data: 01/02/2017
 */
package br.com.infox.listener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.controleprazos.AgendaServicosPeriodicos;
import br.jus.pje.nucleo.entidades.CalendarioEvento;

/**
 * Componente responsável pela monitoração dos objetos do tipo CalendarioEvento. Caso algum atributo 
 * importante seja alterado é necessário invocar o job AtualizarPrazosExpedientesAbertos.
 * 
 * @author Adriano Pamplona
 */
public class CalendarioEventoListener implements PostUpdateEventListener{
	private static final long serialVersionUID = 1L;
	
	private static Logger log = LoggerFactory.getLogger(CalendarioEventoListener.class);
	
	private static List<String> listaAtributosValidados;

	private AgendaServicosPeriodicos agendaServicosPeriodicos;
	
	@Override
	public void onPostUpdate(PostUpdateEvent event) {
		if (isEntityCalendarioEvento(event) && isCalendarioEventoAlterado(event)) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			
			String cron = String.format("0 0 0 %s %s ? %s", 
					calendar.get(Calendar.DAY_OF_MONTH) + 1, 
					calendar.get(Calendar.MONTH) + 1,
					calendar.get(Calendar.YEAR));
			
			try {
				getAgendaServicosPeriodicos().agendarServico(cron, "atualizarPrazosExpedientesAbertos", "execute", false);
				getAgendaServicosPeriodicos().agendarServico(cron, "atualizarPrazosExpedientesAbertosDomicilioEletronico", "execute", false);
			} catch (Exception e) {
				log.error("Erro ao tentar agendar o serviço de atualização de prazos dos expedientes abertos: {0}", e.getLocalizedMessage());
			}
		}
	}
	
	/**
	 * Retorna true se a entidade tratada é do tipo CalendarioEvento.
	 * 
	 * @param event
	 * @return
	 */
	protected boolean isEntityCalendarioEvento(PostUpdateEvent event) {
		return (event.getEntity() instanceof CalendarioEvento);
	}

	/**
	 * Retorna true se houve mudança em algum atributo importante.
	 * 
	 * @param event PostUpdateEvent
	 * @return Booleano
	 */
	protected Boolean isCalendarioEventoAlterado(PostUpdateEvent event) {
		String dadosAnteriores = obterDados(event, event.getOldState());
		String dadosAtuais = obterDados(event, event.getState());
		
		return (StringUtils.equals(dadosAnteriores, dadosAtuais) == Boolean.FALSE);
	}

	/**
	 * Retorna uma string com os dados que deverão ser verificados se houve validação.
	 * 
	 * @param event
	 * @param state
	 * @return String
	 */
	protected String obterDados(PostUpdateEvent event, Object[] state) {
		StringBuilder sb = new StringBuilder();
		
		if (state !=  null) {
			String[] atributos = event.getPersister().getClassMetadata().getPropertyNames();
			for(int indice = 0; indice < atributos.length; indice++) {
				if (getListaAtributosValidados().contains(atributos[indice])) {
					sb.append(state[indice]);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Retorna a lista de atributos que serão validados.
	 * 
	 * @return listaAtributosValidados.
	 */
	protected static List<String> getListaAtributosValidados() {
		if (listaAtributosValidados == null) {
			listaAtributosValidados = new ArrayList<String>();
			listaAtributosValidados.add("ativo");
			listaAtributosValidados.add("dtAno");
			listaAtributosValidados.add("dtDia");
			listaAtributosValidados.add("dtMes");
			listaAtributosValidados.add("inAbrangencia");
			listaAtributosValidados.add("inFeriado");
			listaAtributosValidados.add("inJudiciario");
			listaAtributosValidados.add("inSuspendePrazo");
			listaAtributosValidados.add("indisponibilidadeSistema");
		}
		return listaAtributosValidados;
	}


	/**
	 * @return agendaServicosPeriodicos.
	 */
	public AgendaServicosPeriodicos getAgendaServicosPeriodicos() {
		if (agendaServicosPeriodicos == null) {
			agendaServicosPeriodicos = ComponentUtil.getComponent(AgendaServicosPeriodicos.class);
		}
		return agendaServicosPeriodicos;
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		// TODO Auto-generated method stub
		return false;
	}
	
}