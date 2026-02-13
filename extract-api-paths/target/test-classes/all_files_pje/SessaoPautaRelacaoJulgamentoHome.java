package br.com.infox.cliente.home; 

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.SystemException;

import org.apache.commons.lang.BooleanUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.transaction.UserTransaction;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.SessaoPautaRelacaoJulgamentoList;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.service.ParametroService;
import br.jus.cnj.pje.nucleo.service.SessaoJulgamentoService;
import br.jus.csjt.pje.business.service.MovimentoAutomaticoService;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.ModeloDocumento;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.entidades.SessaoProcessoDocumento;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.TipoInclusaoEnum;
import br.jus.pje.nucleo.enums.TipoSituacaoPautaEnum;

@Name("sessaoPautaRelacaoJulgamentoHome")
@BypassInterceptors
public class SessaoPautaRelacaoJulgamentoHome extends AbstractHome<SessaoPautaProcessoTrf> {

	private static final long serialVersionUID = 1L;

	public void setSessaoPautaProcessoTrfIdSessaoPautaProcessoTrf(Integer id) {
		setId(id);
	}

	public Integer getSessaoPautaProcessoTrfIdSessaoPautaProcessoTrf() {
		return (Integer) getId();
	}

	@SuppressWarnings("rawtypes")
	public String getGabinete(SessaoPautaProcessoTrf obj) {
		String sql = "select a.orgaoJulgador from SessaoComposicaoOrdem a "
				+ "where a.sessao.idSessao = :idSessao and a.sessao.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado in "
				+ "(select o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado from "
				+ "OrgaoJulgadorColegiadoOrgaoJulgador o where o.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado = "
				+ "a.sessao.orgaoJulgadorColegiado.idOrgaoJulgadorColegiado)";
		EntityManager entityManager = EntityUtil.getEntityManager();
		Query query = entityManager.createQuery(sql);
		query.setParameter("idSessao", obj.getSessao().getIdSessao());
		query.setMaxResults(1);
		List resultList = query.getResultList();
		if (resultList.size() > 0) {
			return (String) resultList.get(0);
		} else {
			return null;
		}
	}

	public void setPref(SessaoPautaProcessoTrf s) {
		//setInstance(s);
		try {
			getEntityManager().merge(s);
			getEntityManager().flush();
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
		}
	}

	public void setSust(SessaoPautaProcessoTrf s) {
		//setInstance(s);
		try {
			getEntityManager().merge(s);
			getEntityManager().flush();
		} catch (Exception e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
		}
	}

	public Boolean verificaRevisor(SessaoPautaProcessoTrf s) {
		if (s == null) {
			return false;
		}
		OrgaoJulgador o = Authenticator.getOrgaoJulgadorAtual();
		OrgaoJulgador orgao = getRevisor(s.getProcessoTrf());
		if (s.getProcessoTrf().getOrgaoJulgador() == o) {
			return true;
		} else {
			return (o == orgao);
		}
	}

	@SuppressWarnings("rawtypes")
	public OrgaoJulgador getRevisor(ProcessoTrf obj) {
		StringBuilder sb = new StringBuilder("select o.orgaoJulgadorRevisor from SessaoComposicaoOrdem o ");
		sb.append("where o.orgaoJulgador.idOrgaoJulgador = :idOrgaoJulgador ");
		sb.append("and o.sessao.idSessao = :idSessao");

		Query query = getEntityManager().createQuery(sb.toString());
		query.setParameter("idOrgaoJulgador", obj.getOrgaoJulgador().getIdOrgaoJulgador());
		query.setParameter("idSessao", SessaoHome.instance().getInstance().getIdSessao());
		query.setMaxResults(1);
		List resultList = query.getResultList();
		if (resultList.size() > 0) {
			return (OrgaoJulgador) resultList.get(0);
		} else {
			return null;
		}
	}

	public Boolean verificaProcessoExigeRevisao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		if (Authenticator.getOrgaoJulgadorAtual() != null) {
			if (sessaoPautaProcessoTrf.getProcessoTrf().getOrgaoJulgador() == Authenticator.getOrgaoJulgadorAtual()
					&& BooleanUtils.isTrue(sessaoPautaProcessoTrf.getProcessoTrf().getExigeRevisor())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	@SuppressWarnings("unused")
	private void inativarDocumentosSessao(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		List<SessaoProcessoDocumento> docSessao = SessaoProcessoDocumentoHome.instance().getDocumentosSessao(
				sessaoPautaProcessoTrf);
		for (SessaoProcessoDocumento sessaoProcessoDocumento : docSessao) {
			ProcessoDocumentoHome.instance().setInstance(sessaoProcessoDocumento.getProcessoDocumento());
			ProcessoDocumentoHome.instance().inactive();
		}
		if (!docSessao.isEmpty()) {
			ProcessoDocumentoHome.instance().newInstance();
		}
	}

	private void removeSessaoPautaProcessoTrf(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		sessaoPautaProcessoTrf.setDataExclusaoProcessoTrf(new Date());
		sessaoPautaProcessoTrf.setUsuarioExclusao(Authenticator.getUsuarioLogado());
		realocaDocumentosSessao(sessaoPautaProcessoTrf.getProcessoTrf(), sessaoPautaProcessoTrf.getSessao(), null);
		getEntityManager().merge(sessaoPautaProcessoTrf);
	}
	
	@Transactional
	public String removeSessaoPauta(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		Date dataAtual = new Date();
		Sessao sessao = sessaoPautaProcessoTrf.getSessao();
		if (!sessaoTipoPautaJulgamentoComPautaFechadaHojeOuAntes(sessaoPautaProcessoTrf, dataAtual)) {
			removeSessaoPautaProcessoTrf(sessaoPautaProcessoTrf);
		} else if (sessaoTipoPautaJulgamentoComPautaFechadaHojeOuAntes(sessaoPautaProcessoTrf, dataAtual)) {
			lancarEventoRetiradoDePauta(sessaoPautaProcessoTrf);

			sessaoPautaProcessoTrf.setDataExclusaoProcessoTrf(new Date());
			sessaoPautaProcessoTrf.setUsuarioExclusao(Authenticator.getUsuarioLogado());
			sessaoPautaProcessoTrf.setSituacaoJulgamento(TipoSituacaoPautaEnum.CP);
			realocaDocumentosSessao(sessaoPautaProcessoTrf.getProcessoTrf(), sessaoPautaProcessoTrf.getSessao(), null);

			// Limpando histórico ao remover. Solicitação de Gisele
			ProcessoTrf processoTrf = sessaoPautaProcessoTrf.getProcessoTrf();
			processoTrf.setPessoaMarcouPauta((Pessoa) null);
			processoTrf.setSelecionadoPauta(false);
			
			if (!intimarPartesProcessoPautaCancelada(sessaoPautaProcessoTrf)) {
				return null;
			}

			processoTrf.setPessoaMarcouPauta((Pessoa) null);
			processoTrf.setSessaoSugerida(null);

			// verifica se o processo exige revisor
			if (BooleanUtils.isTrue(processoTrf.getExigeRevisor())) {
				// seta o in_revisado como false caso a classe exija revisão
				processoTrf.setRevisado(false);
			}

			processoTrf.setSelecionadoPauta(Boolean.FALSE);
			getEntityManager().merge(processoTrf);
			getEntityManager().merge(sessaoPautaProcessoTrf);
		} else if (sessaoPautaProcessoTrf.getTipoInclusao() == TipoInclusaoEnum.AD) {
			if (sessaoPautaProcessoTrf.getSessao().getOrgaoJulgadorColegiado().getDiaRetiradaAdiada() != null) {
				sessaoPautaProcessoTrf.setDataExclusaoProcessoTrf(new Date());
				sessaoPautaProcessoTrf.setUsuarioExclusao(Authenticator.getUsuarioLogado());
				getEntityManager().merge(sessaoPautaProcessoTrf);

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(sessaoPautaProcessoTrf.getSessao().getDataSessao());
				calendar.add(Calendar.DATE, sessaoPautaProcessoTrf.getSessao().getOrgaoJulgadorColegiado().getDiaRetiradaAdiada());

				Sessao novaSessao = getSessaoProxima(calendar.getTime());
				SessaoPautaProcessoTrf novaSessaoPauta = new SessaoPautaProcessoTrf();

				novaSessaoPauta.setSessao(novaSessao);
				novaSessaoPauta.setProcessoTrf(sessaoPautaProcessoTrf.getProcessoTrf());
				novaSessaoPauta.setDataInclusaoProcessoTrf(new Date());
				novaSessaoPauta.setUsuarioInclusao(sessaoPautaProcessoTrf.getUsuarioExclusao());
				novaSessaoPauta.setTipoInclusao(TipoInclusaoEnum.AD);
				getEntityManager().persist(novaSessaoPauta);
				realocaDocumentosSessao(sessaoPautaProcessoTrf.getProcessoTrf(), sessaoPautaProcessoTrf.getSessao(), novaSessaoPauta.getSessao());
			} else {
				sessaoPautaProcessoTrf.setDataExclusaoProcessoTrf(new Date());
				sessaoPautaProcessoTrf.setUsuarioExclusao(Authenticator.getUsuarioLogado());
				realocaDocumentosSessao(sessaoPautaProcessoTrf.getProcessoTrf(), sessaoPautaProcessoTrf.getSessao(), null);
				getEntityManager().merge(sessaoPautaProcessoTrf);
			}
		} else if (sessaoPautaProcessoTrf.getTipoInclusao() == TipoInclusaoEnum.PV || sessaoPautaProcessoTrf.getTipoInclusao() == TipoInclusaoEnum.ME) {
			removeSessaoPautaProcessoTrf(sessaoPautaProcessoTrf);
		}
		getEntityManager().flush();
		SessaoProcessoDocumentoHome.instance().newInstance();
		refreshSessaoPautaRelacaoJulgamentoList();
		reordernarListaDeProcessosNaSessao(sessao);
		return "removed";
	}

	private boolean intimarPartesProcessoPautaCancelada(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		Integer idTipoDocumento = Integer.parseInt(ComponentUtil.getParametroService().valueOf(Parametros.ID_TIPO_DOCUMENTO_INTIMACAO_PAUTA));
		Integer idModeloDocumento = Integer.parseInt(ComponentUtil.getParametroService().valueOf(Parametros.ID_MODELO_DOCUMENTO_CANCELAMENTO_PAUTA));
		try {
			ModeloDocumento modelo = ComponentUtil.getModeloDocumentoManager().findById(idModeloDocumento);
			TipoProcessoDocumento tipoDocumento = ComponentUtil.getTipoProcessoDocumentoManager().findById(idTipoDocumento);
			String siglaFluxo = ComponentUtil.getComponent(ParametroService.class).valueOf("pje:fluxo:prepararIntimacaoPauta");
			if (sessaoPautaProcessoTrf.getSessao().getOrgaoJulgadorColegiado().getIntimacaoAutomatica() 
					&& (sessaoPautaProcessoTrf.getTipoInclusao() == TipoInclusaoEnum.PA 
							|| sessaoPautaProcessoTrf.getTipoInclusao() == TipoInclusaoEnum.AD 
							|| (sessaoPautaProcessoTrf.getIntimavel() != null && sessaoPautaProcessoTrf.getIntimavel()))) {
				ComponentUtil.getComponent(SessaoJulgamentoService.class).intimarPartesProcessoPautaCancelada(sessaoPautaProcessoTrf, sessaoPautaProcessoTrf.getSessao(), modelo, tipoDocumento, siglaFluxo);
			}
		} catch (PJeBusinessException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Não foi possível intimar os participantes do julgamento para informá-los do cancelamento da pauta. " + e.getMessage());
			return false;
		}
		return true;
	}
	
	private void lancarEventoRetiradoDePauta(SessaoPautaProcessoTrf sessaoPautaProcessoTrf) {
		Evento eventoRetiradoPauta = ParametroUtil.instance().getEventoRetiradoPauta();
		if (eventoRetiradoPauta != null) {
			MovimentoAutomaticoService
				.preencherMovimento()
				.deCodigo(eventoRetiradoPauta.getCodEvento())
				.associarAoProcesso(sessaoPautaProcessoTrf.getProcessoTrf().getProcesso())
				.associarAoUsuario(Authenticator.getUsuarioLogado())
				.lancarMovimento();
		}
	}

	private boolean sessaoTipoPautaJulgamentoComPautaFechadaHojeOuAntes(SessaoPautaProcessoTrf sessaoPautaProcessoTrf, Date dataAtual) {
		return sessaoPautaProcessoTrf.getSessao().getDataFechamentoPauta() != null
				&& sessaoPautaProcessoTrf.getTipoInclusao() == TipoInclusaoEnum.PA
				&& (dataAtual.after(sessaoPautaProcessoTrf.getSessao().getDataFechamentoPauta()) || dataAtual == sessaoPautaProcessoTrf
						.getSessao().getDataFechamentoPauta());
	}

	private void reordernarListaDeProcessosNaSessao(Sessao sessao) {
		if (sessao.getDataFechamentoPauta() == null) {
			List<SessaoPautaProcessoTrf> listaPauta = new ArrayList<>(ComponentUtil.getSessaoPautaProcessoTrfManager().recuperarPautaSessaoOrdenada(sessao));
			int numeroOrdem = 0;
			List<Integer> ordens = new ArrayList<>();
			for (SessaoPautaProcessoTrf processoPauta: listaPauta) {
				processoPauta.setNumeroOrdem(++numeroOrdem);
				getEntityManager().merge(processoPauta);
				ordens.add(numeroOrdem);
			}
			SessaoPautaRelacaoJulgamentoList sessaoPautaRelacaoJulgamentoList = (SessaoPautaRelacaoJulgamentoList) Component.getInstance(SessaoPautaRelacaoJulgamentoList.class);
			sessaoPautaRelacaoJulgamentoList.getOrdens().clear();
			sessaoPautaRelacaoJulgamentoList.setOrdens(ordens);			
			EntityUtil.flush();
		}
	}

	private void refreshSessaoPautaRelacaoJulgamentoList() {
		final UserTransaction transaction = Transaction.instance();
		SessaoPautaRelacaoJulgamentoList list = (SessaoPautaRelacaoJulgamentoList) Component.getInstance(SessaoPautaRelacaoJulgamentoList.class);
		for(SessaoPautaProcessoTrf processoPauta : list.list()){
			getEntityManager().refresh(processoPauta);
		}
		
		try {
			if (!transaction.isActive() || transaction.isRolledBackOrMarkedRollback()) {
				throw new IllegalStateException("Transação abortada!");
			}
			if (transaction.isActive() && !transaction.isRolledBackOrMarkedRollback()) {
				EntityUtil.flush();
			}
		} catch (SystemException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private void realocaDocumentosSessao(ProcessoTrf processoJudicial, Sessao sessaoOrigem, Sessao sessaoDestino){
	  Set<SessaoProcessoDocumento> procDocs = new HashSet<SessaoProcessoDocumento>();
		String query = "SELECT spd FROM SessaoProcessoDocumento AS spd "
				+ "	WHERE spd.sessao = :sessaoOrigem "
				+ "		AND spd.processoDocumento.ativo = true "
				+ "		AND spd.processoDocumento.processo = :processo";
		Query q = EntityUtil.getEntityManager().createQuery(query);
		q.setParameter("sessaoOrigem", sessaoOrigem);
		q.setParameter("processo", processoJudicial.getProcesso());
		procDocs.addAll(q.getResultList());

		query = "SELECT spd FROM SessaoProcessoDocumentoVoto AS spd "
				+ "	WHERE spd.sessao = :sessaoOrigem "
				+ "		AND spd.processoTrf = :processo";
		Query q1 = EntityUtil.getEntityManager().createQuery(query);
		q1.setParameter("sessaoOrigem", sessaoOrigem);
		q1.setParameter("processo", processoJudicial);
		procDocs.addAll(q1.getResultList());
		for (SessaoProcessoDocumento sp : procDocs) {
			sp.setSessao(sessaoDestino);
			getEntityManager().persist(sp);
		}
	}

	public Sessao getSessaoProxima(Date data) {
		String q = "select o from Sessao o where o.dataSessao in (select min(s.dataSessao) from Sessao s where s.dataSessao > :data)";
		Query query = getEntityManager().createQuery(q);
		query.setParameter("data", data);
		return (Sessao) query.getSingleResult();
	}

}
