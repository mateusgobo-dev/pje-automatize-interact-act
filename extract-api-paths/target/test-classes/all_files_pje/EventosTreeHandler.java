/* $Id: EventoTreeHandler.java 14765 2010-12-02 18:17:59Z laercio $ */

package br.com.infox.ibpm.component.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.ProcessInstance;
import org.jboss.seam.bpm.TaskInstance;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.com.infox.cliente.home.ProcessoDocumentoHome;
import br.com.infox.ibpm.home.ProcessoHome;
import br.com.infox.ibpm.jbpm.JbpmUtil;
import br.com.infox.pje.service.AssinaturaDocumentoService;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.csjt.pje.business.service.ParserTextoMovimento;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoEventoTemp;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;

@Name(AutomaticEventsTreeHandler.NAME)
@Install(precedence = Install.APPLICATION)
@BypassInterceptors
public class EventosTreeHandler extends AutomaticEventsTreeHandler {

	private static final long serialVersionUID = 1L;
	private static final LogProvider log = Logging.getLogProvider(EventosTreeHandler.class);

	@Override
	public void carregaEventos() {
		TipoProcessoDocumento tipoProcessoDocumento = ProcessoHome.instance().getTipoProcessoDocumento();
		ProcessoDocumento processoDocumento = ProcessoDocumentoHome.instance().getInstance();
		carregaEventos(processoDocumento, tipoProcessoDocumento);
	}

	@SuppressWarnings("unchecked")
	/**
	 * Método que busca na tabela tb_processo_evento_temp os eventos ainda não lançados para o documento.
	 */
	public void carregaEventos(ProcessoDocumento processoDocumento, TipoProcessoDocumento tipoProcessoDocumento) {
		if (tipoProcessoDocumento == null) {
			return;
		}

		if (ProcessoDocumentoHome.instance().getInstance() != null) {
			String query = "select o from ProcessoEventoTemp o "
					+ "where o.processoDocumento.idProcessoDocumento = :pd "
					+ "and o.tipoProcessoDocumento.tipoProcessoDocumento.idTipoProcessoDocumento = :tpd";
			Query q = getEntityManager().createQuery(query);
			q.setParameter("pd", processoDocumento.getIdProcessoDocumento());
			q.setParameter("tpd", tipoProcessoDocumento.getIdTipoProcessoDocumento());

			if (super.getEventoBeanList().isEmpty()) {
				List<ProcessoEventoTemp> listTemp = q.getResultList();
				for (ProcessoEventoTemp processoEventoTemp : listTemp) {
					EventoBean eb = new EventoBean();
					eb.setIdEvento(processoEventoTemp.getEvento().getIdEvento());
					eb.setDescricaoMovimento(processoEventoTemp.getEvento().toString());
					eb.setExcluir(Boolean.TRUE);
					eb.setIdProcessoDocumento(processoEventoTemp.getProcessoDocumento().getIdProcessoDocumento());
					eb.setIdTipoProcessoDocumento(processoEventoTemp.getTipoProcessoDocumento()
							.getIdTipoProcessoDocumento());
					eb.setQuantidade(1);

					boolean eventoEncontrado = false;
					for (EventoBean eventoBean : super.getEventoBeanList()) {
						if (eventoBean.getIdEvento() != null && eventoBean.getIdEvento().equals(eb.getIdEvento())
								&& eventoBean.getIdProcessoDocumento().equals(eb.getIdProcessoDocumento())
								&& eventoBean.getIdTipoProcessoDocumento().equals(eb.getIdTipoProcessoDocumento())) {
							eventoBean.setQuantidade(eventoBean.getQuantidade() + 1);
							eventoEncontrado = true;
						}
					}
					if (!eventoEncontrado) {
						super.getEventoBeanList().add(eb);

					}
				}
				if (!super.getEventoBeanList().isEmpty()) {
					super.setAllRegistred(true);

				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private String listToString(Collection c) {
		return c.toString().replaceAll("\\[", "").replaceAll("\\]", "");
	}

	/**
	 * Grava na tabela tb_processo_evento_temp os eventos temporários para os
	 * documentos que ainda não foram assinados
	 */
	@Override
	public void registrarEventosJbpm(ProcessoDocumento pd) {
		Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
		Processo processo = null;
		Integer idProcesso = ProcessoHome.instance().getInstance().getIdProcesso();
		if (idProcesso != null) {
			processo = EntityUtil.find(Processo.class, idProcesso);
		} else {
			processo = JbpmUtil.getProcesso();
		}

		if (processo == null) {
			// Não há eventos para registrar
			return;
		}

		List<Integer> eventos = new ArrayList<Integer>();
		for (int i = 0; i < super.getEventoBeanList().size(); i++) {
			eventos.add(getEventoBeanList().get(i).getIdEvento());
		}
		StringBuilder sb = new StringBuilder();
		sb.append("delete from tb_processo_evento_temp pet ");
		sb.append("where pet.id_processo_documento = :pd ");
		sb.append("and pet.id_processo = :processo ");
		if (eventos.size() > 0) {
			sb.append("and pet.id_evento not in (" + listToString(eventos) + ")");
		}
		Query query = EntityUtil.createNativeQuery(sb, "tb_processo_evento_temp");
		query.setParameter("pd", pd.getIdProcessoDocumento());
		if (pd.getProcesso() == null) {
			pd.setProcesso(ProcessoHome.instance().getInstance());
		}
		query.setParameter("processo", pd.getProcesso().getIdProcesso());
		query.executeUpdate();
		for (EventoBean eventoBean : getEventoBeanList()) {
			String sql = "delete from tb_processo_evento_temp pet " + "where pet.id_processo_documento = :pd "
					+ "and pet.id_processo = :processo " + "and pet.id_evento = :evento";
			query = EntityUtil.createNativeQuery(sql, "tb_processo_evento_temp");
			query.setParameter("pd", pd.getIdProcessoDocumento());
			query.setParameter("processo", pd.getProcesso().getIdProcesso());
			query.setParameter("evento", eventoBean.getIdEvento());
			query.executeUpdate();
		}

		sb = new StringBuilder();
		sb.append("insert into tb_processo_evento_temp (id_processo, id_evento, id_usuario, " + "dt_insercao");
		org.jbpm.taskmgmt.exe.TaskInstance taskInstance = TaskInstance.instance();
		if (taskInstance != null) {
			sb.append(", id_jbpm_task");
		}
		if (pd != null) {
			sb.append(", id_processo_documento");
			sb.append(", id_tipo_processo_documento");
		}
		sb.append(") ").append("values (:processo, :evento, :usuario, :data");
		if (taskInstance != null) {
			sb.append(", :idJbpm");
		}
		if (pd != null) {
			sb.append(", :idProcessoDocumento");
			sb.append(", :idTipoProcessoDocumento");
		}
		sb.append(")");
		Query q = EntityUtil.createNativeQuery(sb, "tb_processo_evento_temp");
		for (EventoBean eb : getEventoBeanList()) {
			for (int i = 0; i < eb.getQuantidade(); i++) {
				q.setParameter("processo", processo.getIdProcesso());
				q.setParameter("evento", eb.getIdEvento());
				q.setParameter("usuario", usuario.getIdUsuario());
				q.setParameter("data", new Date());
				if (taskInstance != null) {
					q.setParameter("idJbpm", taskInstance.getId());
				}
				if (pd != null) {
					q.setParameter("idProcessoDocumento", pd.getIdProcessoDocumento());
					q.setParameter("idTipoProcessoDocumento", pd.getTipoProcessoDocumento()
							.getIdTipoProcessoDocumento());
				}
				q.executeUpdate();
			}
		}
	}

	/***
	 * Limpa todos os eventos do processoDocumento.
	 */
	private void limparEventoTemporario(ProcessoDocumento pd) {
		String query = "Select count(o) from ProcessoEventoTemp o " + "where o.processoDocumento.idProcessoDocumento = :pd";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("pd", pd.getIdProcessoDocumento());
		try {
			Long retorno = (Long) q.getSingleResult();
			
			if (retorno > 0) {
				String sql = "delete from tb_processo_evento_temp pet where pet.id_processo_documento = :pd";
				Query q2 = EntityUtil.createNativeQuery(sql, "tb_processo_evento_temp");
				q2.setParameter("pd", pd.getIdProcessoDocumento());
				q2.executeUpdate();
			}
		} catch (NoResultException no) {
		}		
	}

	/**
	 * Método que registra os eventos selecionados, este método pode ser
	 * invocado por vários lugares, incluindo a assinatura digital. Ele também
	 * limpa a tree e seta uma variável para verificar se existe ou não mais
	 * eventos a serem registrados nessa tarefa.
	 */
	@Override
	public void registraEventos() {
		ProcessoDocumento pd = EntityUtil.find(ProcessoDocumento.class, ProcessoHome.instance()
				.getIdProcessoDocumento());
		if (pd != null) {
			AssinaturaDocumentoService documentoService = ComponentUtil.getComponent(AssinaturaDocumentoService.NAME);
			if (documentoService.isDocumentoAssinado(pd.getIdProcessoDocumento())) {
				inserirEventosProcesso(pd);
				limparEventoTemporario(pd);
			} else {
				registrarEventosJbpm(pd);
			}
		} else {
			inserirEventosProcesso(null);
		}

	}

	@Override
	@Observer(REGISTRA_EVENTO_PD_EVENT)
	/**
	 * Recebe um processoDocumento e caso ele esteja assinado, os eventos são lançados. 
	 * Se não estiver, grava os eventos na tabela tb_processo_evento_temp para serem lançados
	 * somente quando o documento for assinado.
	 * @param ProcessoDocumento
	 */
	public void registraEventosProcessoDocumento(ProcessoDocumento pd) {
		if (pd != null) {
			AssinaturaDocumentoService documentoService = ComponentUtil.getComponent(AssinaturaDocumentoService.NAME);
			if (documentoService.isDocumentoAssinado(pd.getIdProcessoDocumento())) {
				inserirEventosProcesso(pd);
				limparEventoTemporario(pd);
			} else {
				registrarEventosJbpm(pd);
			}
		}
	}

	/**
	 * Lança os eventos do processoDocumento no processo.
	 * 
	 * @param ProcessoDocumento
	 */
	@SuppressWarnings("unchecked")
	private void inserirEventosProcesso(ProcessoDocumento pd) {
		if (getEventoBeanList().isEmpty() && pd == null) {
			return;
		}
		org.jbpm.taskmgmt.exe.TaskInstance taskInstance = TaskInstance.instance();
		org.jbpm.graph.exe.ProcessInstance processInstance = ProcessInstance.instance();
		try {
			Usuario usuario = (Usuario) Contexts.getSessionContext().get("usuarioLogado");
			Processo processo = null;

			Integer idProcesso = ProcessoHome.instance().getInstance().getIdProcesso();
			if (idProcesso != null && idProcesso != 0) {
				processo = EntityUtil.find(Processo.class, idProcesso);
			} else if (pd != null) {
				processo = EntityUtil.find(Processo.class, pd.getProcesso().getIdProcesso());
			} else {
				processo = JbpmUtil.getProcesso();
			}

			if (processo == null) {
				// Não há eventos para registrar
				return;
			}

			ProcessoEventoTemp processoEventoTemp;
			if (super.getEventoBeanList().isEmpty() && pd != null) {
				String query = "Select o from ProcessoEventoTemp o "
						+ "where o.processoDocumento.idProcessoDocumento = :pd "
						+ "and o.processo.idProcesso = :processo";
				Query q2 = EntityUtil.getEntityManager().createQuery(query);
				q2.setParameter("pd", pd.getIdProcessoDocumento());
				q2.setParameter("processo", processo.getIdProcesso());
				List<ProcessoEventoTemp> processoEventoTempList = q2.getResultList();
				if (processoEventoTempList != null && !processoEventoTempList.isEmpty()) {
					for (int i = 0; i < processoEventoTempList.size(); i++) {
						processoEventoTemp = (ProcessoEventoTemp) processoEventoTempList.get(i);
						EventoBean eb = new EventoBean();
						eb.setIdEvento(processoEventoTemp.getEvento().getIdEvento());
						eb.setDescricaoMovimento(processoEventoTemp.getEvento().toString());
						eb.setIdJbpmTask(processoEventoTemp.getIdJbpmTask());
						eb.setQuantidade(1);
						super.getEventoBeanList().add(eb);
					}
				}
			}
			for (EventoBean eb : super.getEventoBeanList()) {
				for (int i = 0; i < eb.getQuantidade(); i++) {
					ProcessoEvento pe = new ProcessoEvento();
					pe.setDataAtualizacao(new Date());
					pe.setProcesso(processo);
					Evento evento = EntityUtil.find(Evento.class,
							eb.getIdEvento());
					pe.setEvento(evento);
					
					AplicacaoMovimento aplicacaoMovimento  = evento.getAplicacaoMovimentoList().get(0);
					if(aplicacaoMovimento != null){
						pe.setTextoParametrizado(aplicacaoMovimento.getTextoParametrizado());
						ParserTextoMovimento parserTextoMovimento = new ParserTextoMovimento(aplicacaoMovimento);

						pe.setTextoFinalExterno(parserTextoMovimento.getTextoFinalExterno());
						pe.setTextoFinalInterno(parserTextoMovimento.getTextoFinalInterno());
					}
					
					pe.setUsuario(usuario);
					if (processInstance != null) {
						pe.setIdProcessInstance(processInstance.getId());
					}
					Tarefa t = null;
					if (taskInstance != null) {
						pe.setIdJbpmTask(taskInstance.getId());
						t = JbpmUtil
								.getTarefa(taskInstance.getName(), processInstance.getProcessDefinition().getName());
					} else {
						pe.setIdJbpmTask(eb.getIdJbpmTask());
					}
					pe.setTarefa(t);
					pe.setProcessoDocumento(pd);
					gravarProcessoEvento(pe);
					getEntityManager().flush();
				}
				if (pd != null) {
					limparEventoTemporario(pd);
				}
			}
			clearTree();
			super.setRootsSelectedMap(new HashMap<Evento, List<Evento>>());
			super.getEventoBeanList().clear();
			super.setRegistred(true);
			Events.instance().raiseEvent(AFTER_REGISTER_EVENT);
		} catch (Exception ex) {
			String action = "registrar os eventos do tipo tarefa: ";
			log.warn(action, ex);
			throw new AplicationException(AplicationException.createMessage(action + ex.getLocalizedMessage(),
					"registraEventos()", "EventosTreeHandler", "PJE"));
		}
	}

	private void gravarProcessoEvento(ProcessoEvento pe) {
		getEntityManager().persist(pe);
	}

	public Evento getEventoById(Integer idEvento) {
		return getEntityManager().find(Evento.class, idEvento);
	}
}
