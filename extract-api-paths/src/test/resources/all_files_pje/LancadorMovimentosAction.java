package br.jus.csjt.pje.view.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.bpm.ProcessInstance;

import br.com.infox.ibpm.component.tree.ComplementoBean;
import br.com.infox.ibpm.component.tree.EventoBean;
import br.com.infox.ibpm.component.tree.EventsTreeHandler;
import br.com.infox.ibpm.component.tree.MovimentoBean;
import br.com.infox.ibpm.component.tree.ValorComplementoBean;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.HibernateUtil;
import br.jus.cnj.pje.servicos.ILancadorMovimentosAction;
import br.jus.csjt.pje.business.service.LancadorMovimentosService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.Processo;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.Tarefa;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoComplemento;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;
import br.jus.pje.nucleo.entidades.lancadormovimento.ComplementoSegmentado;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplemento;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoComDominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoDinamico;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoLivre;

/**
 * Componente Action usado para interface entre a View e o
 * LancadorMovimentosService.
 * 
 * @author David, Kelly
 */
@Name(ILancadorMovimentosAction.NAME)
@Scope(ScopeType.PAGE)
@BypassInterceptors
public class LancadorMovimentosAction implements ILancadorMovimentosAction, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3758522376343405778L;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public MovimentoBean getMovimentoBeanPreenchido(Evento evento) {
		MovimentoBean retorno = new MovimentoBean();

		AplicacaoMovimento aplicacaoMovimento = LancadorMovimentosService.instance().getAplicacaoMovimentoByEvento(evento);
		if (aplicacaoMovimento != null) {
			for (AplicacaoComplemento aplicacaoComplemento : aplicacaoMovimento.getAplicacaoComplementoList()) {
				TipoComplemento tipoComplemento = aplicacaoComplemento.getTipoComplemento();

				if (!tipoComplemento.getAtivo()) {
					continue;
				}

				tipoComplemento = HibernateUtil.deproxy(tipoComplemento, TipoComplemento.class);

				// Preencher o ComplementoBean com valores comuns
				ComplementoBean complementoBean = new ComplementoBean();
				complementoBean.setMensagemErro(tipoComplemento.getMensagemErro());
				complementoBean.setValidacao(tipoComplemento.getValidacao());
				complementoBean.setLabel(tipoComplemento.getLabel());
				complementoBean.setMultiplo(aplicacaoComplemento.getMultivalorado());
				complementoBean.setGlossario(tipoComplemento.getDescricaoGlossario());
				complementoBean.setIdTipoComplemento(tipoComplemento.getIdTipoComplemento());

				// Adicionar o ValorComplementoBean que representará a escolha de
				// complemento do usuário
				ValorComplementoBean vcb = new ValorComplementoBean();
				complementoBean.getValorComplementoBeanList().add(vcb);

				// Preencher o ComplementoBean dependendo do tipo de complemento
				if (tipoComplemento instanceof TipoComplementoLivre) {
					// TipoComplementoLivre
					LancadorMovimentosService.instance().preencherTipoComplementoLivre(tipoComplemento, complementoBean);
				} else if (tipoComplemento instanceof TipoComplementoDinamico) {
					// TipoComplementoDinamico
					try {
						LancadorMovimentosService.instance().preencherTipoComplementoDinamico(tipoComplemento, complementoBean);
					} catch (Exception e) {
						throw new AplicationException("Erro na carga para o movimento " + evento.getEvento()
								+ ", EL do complemento inválida.");
					}
				} else if (tipoComplemento instanceof TipoComplementoComDominio) {
					// TipoComplementoComDominio
					try {
						LancadorMovimentosService.instance().preencherTipoComplementoComDominio(tipoComplemento, complementoBean);
					} catch (NonUniqueResultException e) {
						throw new AplicationException("Mais de um AplicacaoDominio encontrado para o evento "
								+ evento.getEvento() + ".");
					} catch (NoResultException e) {
						throw new AplicationException("Nenhum AplicacaoDominio encontrado para o evento "
								+ evento.getEvento() + ".");
					}
				}

				retorno.getComplementoBeanList().add(complementoBean);
			}
		}
		return retorno;
	}
	
	public void lancarMovimentosSemFluxo(List<EventoBean> eventoBeanList, ProcessoDocumento processoDocumento, Processo processo) {
		for (EventoBean eventoBean : eventoBeanList) {
			for (MovimentoBean movimentoBean : eventoBean.getMovimentoBeanList()) {
				lancarMovimentosSemFluxo(eventoBean.getCodEvento(), movimentoBean, processoDocumento, processo);
		
			}
		}
	}

	private void lancarMovimentosSemFluxo(String codigoEvento, MovimentoBean movimentoBean, ProcessoDocumento processoDocumento, Processo processo) {
		if (movimentoBean.getComplementoBeanList() == null || movimentoBean.getComplementoBeanList().isEmpty()) {
			MovimentoAutomaticoService.preencherMovimento()
					.deCodigo(codigoEvento)
					.associarAoDocumento(processoDocumento)
					.associarAoProcesso(processo)
					.lancarMovimento();
		} else {
			for (ComplementoBean complementoBean : movimentoBean.getComplementoBeanList()) {
				for (ValorComplementoBean valorComplementoBean : complementoBean.getValorComplementoBeanList()) {
					MovimentoAutomaticoService.preencherMovimento()
							.deCodigo(codigoEvento)
							.associarAoDocumento(processoDocumento)
							.comComplementoDeNome(complementoBean.getLabel()).preencherComTexto(valorComplementoBean.getValor())
							.associarAoProcesso(processo)
							.lancarMovimento();
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void lancarMovimentos(List<EventoBean> eventoBeanList, ProcessoDocumento processoDocumento,
			Processo processo, Long idJbpmTask, Long idProcessInstance, Tarefa tarefa, boolean lancadorPodeRegistrarMovimentosTemporarios, Integer agrupamentos) {

		LancadorMovimentosService lancadorMovimentosService = ComponentUtil
				.getComponent(LancadorMovimentosService.NAME);

		// lancadorPodeRegistrarMovimentosTemporarios é definido em cada lançador(Events*TreeHandler)
		// deveGravarTemporariamente é a condição definida no fluxo
		if (lancadorPodeRegistrarMovimentosTemporarios && deveGravarTemporariamente()) {
			lancadorMovimentosService.setAgrupamentoDeMovimentosTemporarios(ProcessInstance.instance(), agrupamentos);
			lancadorMovimentosService.setMovimentosTemporarios(ProcessInstance.instance(), eventoBeanList);
		} else {
			// Para cada EventoBean ... lançar os Movimentos (1 para cada MovimentoBean)
			for (EventoBean eventoBean : eventoBeanList) {
				Evento evento = getEventoById(eventoBean.getIdEvento());
				evento = HibernateUtil.deproxy(evento, Evento.class);
				
				for (MovimentoBean movimentoBean : eventoBean.getMovimentoBeanList()) {
					List<ComplementoSegmentado> complementoSegmentadoList = new ArrayList<ComplementoSegmentado>();
					
					preencherComplementoSegmentado(movimentoBean,
							complementoSegmentadoList);
					
					// Lançar o movimento
					lancadorMovimentosService.lancarMovimento((Evento) evento, complementoSegmentadoList,
							processoDocumento, processo, tarefa, idJbpmTask, idProcessInstance);
					
				}
			}
		}
		
	}
	
	private void preencherComplementoSegmentado(MovimentoBean movimentoBean,
			List<ComplementoSegmentado> complementoSegmentadoList) {
		// Preencher a lista de ComplementosSegmentados
		for (ComplementoBean complementoBean : movimentoBean.getComplementoBeanList()) {
			TipoComplemento tp = getTipoComplementoById(complementoBean.getIdTipoComplemento());
			for (int i = 0; i < complementoBean.getValorComplementoBeanList().size(); i++) {
				ValorComplementoBean vcb = complementoBean.getValorComplementoBeanList().get(i);
				ComplementoSegmentado complementoSegmentado = new ComplementoSegmentado();
				complementoSegmentado.setOrdem(i);
				complementoSegmentado.setTipoComplemento(tp);
				complementoSegmentado.setTexto(vcb.getCodigo());
				complementoSegmentado.setValorComplemento(vcb.getValor());
				complementoSegmentado.setMovimentoProcesso(null);
				complementoSegmentadoList.add(complementoSegmentado);
			}
		}
	}

	private Evento getEventoById(Integer idEvento) {
		return EventsTreeHandler.instance().getEventoById(idEvento);
	}

	private TipoComplemento getTipoComplementoById(Long idTipoComplemento) {
		return EntityUtil.getEntityManager().find(TipoComplemento.class, idTipoComplemento);
	}

	@Override
	public boolean deveGravarTemporariamente() {
		LancadorMovimentosService lancadorMovimentosService = ComponentUtil
				.getComponent(LancadorMovimentosService.NAME);
		return lancadorMovimentosService.deveGravarTemporariamente();
	}
	
	/**
	 * Método utilizado para lançamento definitivo de movimentos configurados em outro
	 * nó, através de lançamento temporário de movimentos.
	 * 
	 * @author David Vieira
	 */
	public void homologarMovimentosTemporarios(){
		LancadorMovimentosService lancadorMovimentosService = ComponentUtil.getComponent(LancadorMovimentosService.NAME);
		lancadorMovimentosService.homologarMovimentosTemporarios(ProcessInstance.instance());
	}
}
