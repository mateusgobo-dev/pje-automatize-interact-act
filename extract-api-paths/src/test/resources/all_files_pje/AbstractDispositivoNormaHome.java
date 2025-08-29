package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.DispositivoNorma;

public abstract class AbstractDispositivoNormaHome<T> extends AbstractHome<DispositivoNorma> {

	private static final long serialVersionUID = 1L;

	public void setDispositivoNormaId(Integer id) {
		setId(id);
	}

	public Integer getDispositivoNormaId() {
		return (Integer) getId();
	}

	public static DispositivoNormaHome instance() {
		return ComponentUtil.getComponent("dispositivoNormaHome");
	}

	@Override
	public void newInstance() {
		limparTrees();
		Contexts.removeFromAllContexts("dispositivoNormaSearch");
		// força a criação do componente
		getComponent("dispositivoNormaSearch");
		super.newInstance();
		NormaPenalHome normaPenalHome = (NormaPenalHome) Component.getInstance("normaPenalHome");
		getInstance().setDtInicioVigencia(normaPenalHome.getInstance().getDataInicioVigencia());
		getInstance().setAtivo(true);
	}

	@Override
	public String remove(DispositivoNorma obj) {
		int ordenacao = obj.getNumeroOrdem();
		List<DispositivoNorma> irmaosMaisNovos = recuperarIrmaosMaisNovos(obj);

		setInstance(obj);
		String ret = super.remove();

		reordenarAoExcluir(ordenacao, irmaosMaisNovos);
		getEntityManager().flush();

		newInstance();
		GridQuery grid = (GridQuery) Component.getInstance("dispositivoNormaGrid");
		grid.refresh();
		return ret;
	}

	@Override
	public String persist() {
		NormaPenalHome normaPenalHome = (NormaPenalHome) Component.getInstance("normaPenalHome");
		instance.setNormaPenal(normaPenalHome.getInstance());
		// TODO se está sendo capturada aqui, porque também em 'verificar'
		// (beforeUpda...)
		// instance é getInstance() ... ?

		String action = super.persist();
		return action;
	}

	public abstract void limparTrees();

	@Override
	protected DispositivoNorma createInstance() {
		DispositivoNorma dispositivoNorma = new DispositivoNorma();
		return dispositivoNorma;
	}

	public void imprimirMensagem(String mensagem) {
		FacesMessages.instance().add(StatusMessage.Severity.ERROR, mensagem);
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		newInstance();
		clearForm();
		limparTrees();
		return super.afterPersistOrUpdate(ret);
	}

	public void reordenarAoExcluir(int ordenacaoItemExcluido, List<DispositivoNorma> irmaosMaisNovos) {
		if (irmaosMaisNovos != null) {
			Collections.sort(irmaosMaisNovos);
			for (DispositivoNorma aux : irmaosMaisNovos) {
				aux.setNumeroOrdem(aux.getNumeroOrdem() - ordenacaoItemExcluido);
				getEntityManager().persist(aux);
			}
		}
	}

	public List<DispositivoNorma> recuperarIrmaosMaisNovos(DispositivoNorma dispositivoNorma) {
		List<DispositivoNorma> irmaosMaisNovos = null;
		if (dispositivoNorma.getDispositivoNormaPai() != null) {
			irmaosMaisNovos = new ArrayList<DispositivoNorma>(0);
			for (DispositivoNorma aux : dispositivoNorma.getDispositivoNormaPai().getDispositivoNormaList()) {
				if (aux.getNumeroOrdem() > dispositivoNorma.getNumeroOrdem()) {
					irmaosMaisNovos.add(aux);
				}
			}
		} else {
			String hql = " select o from DispositivoNorma o " + " where o.dispositivoNormaPai is null "
					+ " and o.numeroOrdem > :nrOrdem ";
			Query qry = getEntityManager().createQuery(hql);
			qry.setParameter("nrOrdem", dispositivoNorma.getNumeroOrdem());
			irmaosMaisNovos = qry.getResultList();
		}

		return irmaosMaisNovos;
	}
}
