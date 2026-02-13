package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.PessoaProcuradorProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;

@Name("pessoaProcuradorProcuradoriaHome")
@BypassInterceptors
public class PessoaProcuradorProcuradoriaHome extends
		AbstractPessoaProcuradorProcuradoriaHome<PessoaProcuradorProcuradoria> {

	private static final long serialVersionUID = 1L;
	private List<Object> listaObj = new ArrayList<Object>(0);
	private Boolean checkBox = Boolean.FALSE;

	public static PessoaProcuradorProcuradoriaHome instance() {
		return ComponentUtil.getComponent("pessoaProcuradorProcuradoriaHome");
	}

	@Override
	public String persist() {
		getInstance().setPessoaProcurador(PessoaProcuradorHome.instance().getInstance());
		String persist = super.persist();
		return persist;
	}

	public void inserir() {
		GridQuery gridQuery = getComponent("pessoaProcuradorProcuradoriaEntidadeGrid");
		listaObj = gridQuery.getSelectedRowsList();
		for (Object obj : listaObj) {
			PessoaProcuradorProcuradoria ppp = new PessoaProcuradorProcuradoria();
			ppp.setPessoaProcuradoriaEntidade((PessoaProcuradoriaEntidade) obj);
			ppp.setPessoaProcurador(PessoaProcuradorHome.instance().getInstance());
			getEntityManager().persist(ppp);
			getEntityManager().flush();
		}
		refreshGrid("pessoaProcuradorProcuradoriaEntidadeGrid");
		refreshGrid("pessoaProcuradorProcuradoriaGrid");
		listaObj.clear();
		setCheckBox(Boolean.FALSE);
		setTab("entidades");
	}

	@Override
	public String remove(PessoaProcuradorProcuradoria ppp) {
		setInstance(ppp);
		remove();
		return "";
	}

	@Override
	public String remove() {
		getInstance();
		String remove = super.remove();
		refreshGrid("pessoaProcuradorProcuradoriaEntidadeGrid");
		refreshGrid("pessoaProcuradorProcuradoriaGrid");
		listaObj.clear();
		FacesMessages.instance().clear();
		newInstance();
		return remove;
	}

	public void setCheckBox(Boolean checkBox) {
		this.checkBox = checkBox;
	}

	public Boolean getCheckBox() {
		return checkBox;
	}

	public void checkAll(String grid) {
		GridQuery gridQuery = getComponent("pessoaProcuradorProcuradoriaEntidadeGrid");
		List<Object> lista = gridQuery.getSelectedRowsList();
		List<Object> resultList = gridQuery.getResultList();

		lista.clear();
		if (getCheckBox()) {
			lista.addAll(resultList);
		}
	}
	
	/**
	 * Método que verifica se o Usuario pode vincular um procurador a uma entidade
	 */
	public boolean verificaVinculacaoProcuradorEntidade(){
		try {
			return !Boolean.valueOf(ParametroUtil.getParametro(Parametros.VAR_PROCURADOR_ENTIDADE));
		} catch (Exception ex) { return false; }
	}
	
}