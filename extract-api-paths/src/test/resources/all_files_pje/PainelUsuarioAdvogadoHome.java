package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.Redirect;
import org.richfaces.event.DropEvent;

import br.com.infox.cliente.component.tree.JurisdicaoTreeHandler;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.manager.ProcessoCaixaAdvogadoProcuradorManager;
import br.jus.pje.nucleo.entidades.CaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ConsultaProcessoTrf;
import br.jus.pje.nucleo.entidades.Jurisdicao;
import br.jus.pje.nucleo.entidades.ProcessoCaixaAdvogadoProcurador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(PainelUsuarioAdvogadoHome.NAME)
@Scope(ScopeType.CONVERSATION)
@BypassInterceptors
public class PainelUsuarioAdvogadoHome implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "painelUsuarioAdvogadoHome";

	private Map<String, Object> selected = new HashMap<String, Object>();

	public static PainelUsuarioAdvogadoHome instance() {
		return ComponentUtil.getComponent(PainelUsuarioAdvogadoHome.NAME);
	}

	@SuppressWarnings("unchecked")
	@Observer(JurisdicaoTreeHandler.EVENT_SELECTED)
	public void onJurisdicaoSelected(Object obj) {
		selected = (Map<String, Object>) obj;
		GridQuery grid = ComponentUtil.getComponent("processoTrfInicialAdvogadoGrid");
		grid.setPage(1);
	}

	public Map<String, Object> getSelected() {
		return selected;
	}

	public Integer getIdOrgaoJulgador() {
		return (Integer) selected.get("idOrgaoJulgador");
	}

	public Jurisdicao getJurisdicao() {
		return (Jurisdicao) selected.get("jurisdicao");
	}

	public Integer getIdOrgaoJulgadorColegiado() {
		return (Integer) selected.get("idOrgaoJulgadorColegiado");
	}

	public Integer getIdCaixaAdvogadoProcurador() {
		return (Integer) selected.get("idCaixaAdvogadoProcurador");
	}

	@SuppressWarnings("unchecked")
	private void setProcessoCaixa(Object obj, CaixaAdvogadoProcurador caixa) {
		ProcessoCaixaAdvogadoProcurador pcap = new ProcessoCaixaAdvogadoProcurador();
		ProcessoCaixaAdvogadoProcuradorManager processoCaixaAdvogadoProcuradorManager =
				(ProcessoCaixaAdvogadoProcuradorManager) Component.getInstance(ProcessoCaixaAdvogadoProcuradorManager.class);
		if (obj instanceof List<?>) {
			for (ConsultaProcessoTrf processo : (List<ConsultaProcessoTrf>) obj) {
				ProcessoTrf processoTrf = processo.getProcessoTrf();
				pcap.setProcessoTrf(processoTrf);
				pcap.setCaixaAdvogadoProcurador(caixa);
				EntityUtil.getEntityManager().persist(processoTrf);
				EntityUtil.getEntityManager().merge(pcap);
			}
		} else {
			ProcessoTrf processoTrf = ((ConsultaProcessoTrf) obj).getProcessoTrf();
			pcap.setProcessoTrf(processoTrf);
			pcap.setCaixaAdvogadoProcurador(caixa);
			EntityUtil.getEntityManager().persist(processoTrf);
			EntityUtil.getEntityManager().merge(pcap);
		}
		EntityUtil.flush();
		refresh();
	}

	@SuppressWarnings("unchecked")
	public void addProcessosGridEmCaixa(CaixaAdvogadoProcurador caixa) {
		GridQuery grid = ComponentUtil.getComponent("processoTrfInicialAdvogadoGrid");
		List<ConsultaProcessoTrf> list = grid.getFullList();
		if (list.size() > 0) {
			setProcessoCaixa(list, caixa);
		}
	}

	public void refresh() {
		GridQuery grid = ComponentUtil.getComponent("processoTrfInicialAdvogadoGrid");
		grid.refresh();
		JurisdicaoTreeHandler tree = ComponentUtil.getComponent("jurisdicaoTree");
		tree.clearTree();
	}

	public void moverProcessoOrgaoJulgador(DropEvent evt) {
		setProcessoCaixa(evt.getDragValue(), null);
	}

	public void moverProcessoCaixa(DropEvent evt) {
		CaixaAdvogadoProcurador caixa = EntityUtil.find(CaixaAdvogadoProcurador.class, evt.getDropValue());
		for (ConsultaProcessoTrf cpt : getProcessoList(evt.getDragValue())) {
			if (isProcessoEmCaixa(cpt.getIdProcessoTrf())) {
				StringBuilder sb = new StringBuilder();
				sb.append("delete from tb_processo_caixa_adv_proc pcap ");
				sb.append("where pcap.id_processo_trf = :idProcessoTrf");
				EntityUtil.createNativeQuery(sb, "tb_processo_caixa_adv_proc")
						.setParameter("idProcessoTrf", cpt.getIdProcessoTrf()).executeUpdate();
			}
		}
		setProcessoCaixa(evt.getDragValue(), caixa);
	}

	@SuppressWarnings("unchecked")
	private List<ConsultaProcessoTrf> getProcessoList(Object o) {
		List<ConsultaProcessoTrf> list = new ArrayList<ConsultaProcessoTrf>();
		if (o instanceof ConsultaProcessoTrf) {
			list.add(((ConsultaProcessoTrf) o));
		} else if (o instanceof List) {
			for (ConsultaProcessoTrf cpt : (List<ConsultaProcessoTrf>) o) {
				list.add(cpt);
			}
		}
		return list;
	}

	public void removerProcessoCaixa(DropEvent evt) {
		for (ConsultaProcessoTrf cpt : getProcessoList(evt.getDragValue())) {
			StringBuilder sb = new StringBuilder();
			sb.append("delete from tb_processo_caixa_adv_proc pcap ");
			sb.append("where pcap.id_processo_trf = :idProcessoTrf");
			EntityUtil.createNativeQuery(sb, "tb_processo_caixa_adv_proc")
					.setParameter("idProcessoTrf", cpt.getIdProcessoTrf()).executeUpdate();
		}
		refresh();
	}

	/**
	 * Verifica se o processo já está em uma caixa com localização do usuário
	 * logado
	 * 
	 * @return
	 */
	private boolean isProcessoEmCaixa(Integer idProcesso) {
		StringBuilder sb = new StringBuilder();
		sb.append("Select count(o) from ProcessoCaixaAdvogadoProcurador o ");
		sb.append("where o.processoTrf.idProcessoTrf = :idProcessoTrf ");
		sb.append("and o.caixaAdvogadoProcurador.localizacao = :localizacao ");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcessoTrf", idProcesso);
		q.setParameter("localizacao", Authenticator.getLocalizacaoAtual());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public void editaCaixa() {
		Redirect r = new Redirect();
		r.setViewId("/CaixaAdvogadoProcurador/listView.xhtml");
		r.setParameter("tab", "form");
		r.setParameter("id", selected.get("idCaixaAdvogadoProcurador"));
		r.execute();
	}

}
