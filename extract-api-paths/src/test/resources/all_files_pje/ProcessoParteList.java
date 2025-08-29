package br.com.infox.pje.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.home.ProcessoParteExpedienteHome;
import br.jus.pje.nucleo.entidades.ProcessoParte;

@Name(ProcessoParteList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoParteList extends EntityList<ProcessoParte> {

	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_EJBQL = "select pp from ProcessoParte pp ";
	public static final String NAME = "processoParteList";

	private List<ProcessoParte> listaProcessoPasteSelecionados = new ArrayList<ProcessoParte>();

	public static final String R1 = "pp.processoTrf.idProcessoTrf = #{processoTrfHome.managed ? processoTrfHome.instance.idProcessoTrf : processoHome.instance.idProcesso}";

	@Override
	protected void addSearchFields() {
		addSearchField("idProcessoTrf", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return "pp.pessoa.nome";
	}

	@Override
	public List<ProcessoParte> list(int maxResult) {
		List<ProcessoParte> list = super.list(maxResult);
		for (ProcessoParte parteSelecionada : listaProcessoPasteSelecionados) {
			for (ProcessoParte processoParte : list) {
				if (parteSelecionada.getIdProcessoParte() == processoParte.getIdProcessoParte()) {
					processoParte.setCheckado(true);
				}
			}
		}
		ProcessoParteExpedienteHome.instance().setPartesList(listaProcessoPasteSelecionados);
		return list;
	}

	public void setSelecionado(ProcessoParte parte) {
		if (!listaProcessoPasteSelecionados.contains(parte)) {
			listaProcessoPasteSelecionados.add(parte);
		} else {
			listaProcessoPasteSelecionados.remove(parte);
		}
	}

	public void setListaProcessoPasteSelecionados(List<ProcessoParte> listaProcessoPasteSelecionados) {
		this.listaProcessoPasteSelecionados = listaProcessoPasteSelecionados;
	}

	public List<ProcessoParte> getListaProcessoPasteSelecionados() {
		return listaProcessoPasteSelecionados;
	}
}
