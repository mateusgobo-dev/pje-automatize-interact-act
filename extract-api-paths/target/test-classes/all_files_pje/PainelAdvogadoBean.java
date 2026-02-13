package br.com.infox.cliente.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Caixa;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;

@Name(PainelAdvogadoBean.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class PainelAdvogadoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "painelAdvogadoBean";

	private transient List<GrupoFiltroProcessoAdvogado> grupoFiltroProcessoAdvogadoList = new ArrayList<GrupoFiltroProcessoAdvogado>(
			0);

	public void gravar() {
		for (GrupoFiltroProcessoAdvogado grupoFiltroProcessoAdvogado : grupoFiltroProcessoAdvogadoList) {
			for (ConsultaOrgaoJulgadorCaixa c : grupoFiltroProcessoAdvogado.getOrgaoJulgadorCaixaList()) {
				if (c.getOrgaoJulgador() != null) {
					System.out.print(c.getOrgaoJulgador().getOrgaoJulgador());
					System.out.println(c.getCaixa() != null ? " - " + c.getCaixa().getNomeCaixa() : "");
				}
			}
			for (br.com.infox.cliente.component.NumeroProcesso numeroProcesso : grupoFiltroProcessoAdvogado
					.getNumeroProcessoList()) {
				System.out.println(numeroProcesso.getNumeroProcesso());
			}
			// System.out.println(grupoFiltroProcessoAdvogado.isAndOperator());
		}
	}

	public void addGrupoFiltroProcessoAdvogadoList() {
		grupoFiltroProcessoAdvogadoList.add(new GrupoFiltroProcessoAdvogado());
	}

	public void removeFiltroProcessoAdvogadoList(GrupoFiltroProcessoAdvogado grupoFiltroProcessoAdvogado) {
		grupoFiltroProcessoAdvogadoList.remove(grupoFiltroProcessoAdvogado);
	}

	public List<GrupoFiltroProcessoAdvogado> getGrupoFiltroProcessoAdvogadoList() {
		if (grupoFiltroProcessoAdvogadoList.size() == 0) {
			addGrupoFiltroProcessoAdvogadoList();
		}
		return grupoFiltroProcessoAdvogadoList;
	}

	@SuppressWarnings("unchecked")
	public List<Caixa> caixaOrgaoJulgadorItems(OrgaoJulgador orgaoJulgador) {
		String hql = "select o.caixa from CaixaFiltro o " + "where o.orgaoJulgador = :orgaoJulgador "
				+ "order by o.caixa.nomeCaixa";
		return EntityUtil.createQuery(hql).setParameter("orgaoJulgador", orgaoJulgador).getResultList();
	}
}
