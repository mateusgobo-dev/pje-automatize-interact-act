package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.comparator.ProcessoParteComparator;
import br.com.itx.component.AbstractHome;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name("processoParteEnderecoHome")
@BypassInterceptors
public class ProcessoParteEnderecoHome extends AbstractHome<ProcessoParteEndereco> {

	private static final long serialVersionUID = 1L;

	private String urlOpenner;
	private List<Object> listaObj = new ArrayList<Object>(0);
	private List<Object> listaNaoMarcados = new ArrayList<Object>(0);

	public static ProcessoParteEnderecoHome instance() {
		return ComponentUtil.getComponent("processoParteEnderecoHome");
	}

	public void setProcessoParteAdvogadoIdProcessoParteAdvogado(Integer id) {
		setId(id);
	}

	public Integer getProcessoParteEnderecoIdProcessoParteEndereco() {
		return (Integer) getId();
	}

	@Override
	public void newInstance() {
		refreshGrid("enderecoParteGridTab");
		super.newInstance();
	}


	public ProcessoTrfHome getProcessoTrfHome() {
		return ComponentUtil.getComponent("processoTrfHome");
	}

	public List<ProcessoParte> getPartesProcesso(ProcessoTrf processo) {
		List<ProcessoParte> list = new ArrayList<ProcessoParte>(processo.getProcessoParteList());
		Collections.sort(list, new ProcessoParteComparator());
		return list;
	}

	public ProcessoTrf getProcessoTrfAtual() {
		return getProcessoTrfHome().getInstance();
	}



	@Override
	public String inactive(ProcessoParteEndereco obj) {
		//ProcessoTrf processoTrf = ProcessoTrfHome.instance().getDefinedInstance();
		// removerProcessoParteEnderecos(obj.getProcessoParte());
		//getEntityManager().merge(processoTrf);
		//LogUtil.removeEntity(obj);
		obj.getProcessoParte().getProcessoParteEnderecoList().remove(obj);
		EntityManager em = EntityUtil.getEntityManager();
		em.merge(obj.getProcessoParte());
		em.merge(obj);
		em.flush();
		refreshGrid("processoParteGrid");
		refreshGrid("pessoaParteProcessoGrid");
		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoAbaPartePoloAtivoGrid");
		refreshGrid("processoAbaPartePoloPassivoGrid");
		refreshGrid("processoAbaParteTerceiroGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoFiscalLeiGrid");
		refreshGrid("processoIncidentePoloAtivoGrid");
		refreshGrid("processoIncidentePoloPassivoGrid");
		return "deleted";
	}

	public void removeParte(ProcessoParte processoParte, String gridId) {
		ProcessoTrf processoTrf = getProcessoTrfAtual();
		processoTrf.getProcessoParteList().remove(processoParte);
		getEntityManager().merge(processoTrf);
		getEntityManager().remove(processoParte);
		getEntityManager().flush();
		refreshGrid(gridId);
		refreshGrid("processoParteGrid");
		refreshGrid("pessoaParteProcessoGrid");
		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoAbaPartePoloAtivoGrid");
		refreshGrid("processoAbaPartePoloPassivoGrid");
		refreshGrid("processoAbaParteTerceiroGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoFiscalLeiGrid");
	}

	public void addParteEndereco(ProcessoParte parte, String gridId) {
		ProcessoParteEndereco pp = new ProcessoParteEndereco();
		pp.setProcessoParte(parte);
		getEntityManager().persist(pp);
		getEntityManager().flush();
		refreshGrid(gridId);
		refreshGrid("processoParteGrid");
		refreshGrid("processoPoloAtivoGrid");
		refreshGrid("processoAbaPartePoloAtivoGrid");
		refreshGrid("processoAbaPartePoloPassivoGrid");
		refreshGrid("processoAbaParteTerceiroGrid");
		refreshGrid("pessoaParteProcessoGrid");
		refreshGrid("processoPoloPassivoGrid");
		refreshGrid("processoFiscalLeiGrid");
	}

	/**
	 * Associar Enderecos validos de Pessoa(s) a Processo
	 * 
	 * @author Rosfran
	 */
	@SuppressWarnings("unchecked")
	public void associarEnderecoParteProcesso() {
		GridQuery gridQuery = getComponent("processoParteVinculoPessoaEnderecoGrid");
		setListaObj(gridQuery.getSelectedRowsList());
		listaNaoMarcados = gridQuery.getResultList();
		listaNaoMarcados.removeAll(getListaObj());

		persistirEnderecosProcessoParte();
	}

	/**
	 * Persistir Enderecos validos de Pessoa(s) a Processo
	 * 
	 * @author Rosfran
	 */
	public void persistirEnderecosProcessoParte() {
		ProcessoParte pparte = ProcessoParteHome.instance().getInstance();
		if (pparte != null) {
			for (Object obj : getListaObj()) {
				Endereco en = (Endereco) obj;
				// Pessoa pessoa = parteEndereco.getProcessoParte().getPessoa();

				// persist na entidade PessoaParteEndereco
				ProcessoParteEndereco ppe = new ProcessoParteEndereco();
				ppe.setProcessoParte(ProcessoParteHome.instance().getInstance());
				ppe.setEndereco(en);
				pparte.getProcessoParteEnderecoList().add(ppe);

				getEntityManager().persist(ppe);
				getEntityManager().flush();
			} // for
		} // if
		refreshGrid("processoParteVinculoPessoaEnderecoGrid");

	}

	public void addRemoveRowList(Object row) {
		ProcessoParte pp = ProcessoParteHome.instance().getInstance();
		if (getListaObj().contains(row)) {
			getListaObj().remove(row);
			removeProcessoParteEndereco(pp, row);
		} else {
			getListaObj().add(row);
			addProcessoParteEndereco(pp, row);
		}
	}

	public void addProcessoParteEndereco(ProcessoParte pp, Object obj) {
		ProcessoParteEndereco ppe = new ProcessoParteEndereco();
		ppe.setProcessoParte(pp);
		ppe.setEndereco((Endereco) obj);

		pp.getProcessoParteEnderecoList().add(ppe);

		getEntityManager().persist(ppe);
		getEntityManager().flush();
	}

	public void removeProcessoParteEndereco(ProcessoParte pp, Object obj) {
		Endereco end = (Endereco) obj;
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoParteEndereco o ");
		sb.append("where o.processoParte = :pp ");
		sb.append("and o.endereco = :end");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("pp", pp);
		q.setParameter("end", end);
		ProcessoParteEndereco ppe;
		q.setMaxResults(1);
		try {
			ppe = (ProcessoParteEndereco) q.getSingleResult();
			if (ppe != null) {
				pp.getProcessoParteEnderecoList().remove(ppe);
				getEntityManager().remove(ppe);
				EntityUtil.flush();
			}
		} catch (NoResultException no) {
			
		}
	}

	public void setListaObj(List<Object> listaObj) {
		this.listaObj = listaObj;
	}

	public List<Object> getListaObj() {
		return listaObj;
	}

	public void setUrlOpenner(String urlOpenner) {
		this.urlOpenner = urlOpenner;
	}

	public String getUrlOpenner() {
		return urlOpenner;
	}

}