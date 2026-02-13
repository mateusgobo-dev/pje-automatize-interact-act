package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiadoCargo;

@Name("orgaoJulgadorColegiadoCargoHome")
@BypassInterceptors
public class OrgaoJulgadorColegiadoCargoHome extends AbstractHome<OrgaoJulgadorColegiadoCargo> {

	private static final long serialVersionUID = 1L;

	private List<Cargo> cargos;
	private List<Cargo> cargosDisponiveis;

	public void setOrgaoJulgadorColegiadoCargoIdOrgaoJulgadorColegiadoCargo(Integer id) {
		setId(id);
	}

	public Integer getOrgaoJulgadorColegiadoCargoIdOrgaoJulgadorColegiadoCargo() {
		return (Integer) getId();
	}

	public List<Cargo> getCargos() {
		OrgaoJulgadorColegiado ojc = OrgaoJulgadorColegiadoHome.instance().getInstance();
		if (getInstance().getOrgaoJulgadorColegiado() != ojc) {
			OrgaoJulgadorColegiadoHome.instance().setInstance(ojc);
			cargos = null;
		}
		if (cargos == null) {
			List<OrgaoJulgadorColegiadoCargo> orgaoJulgadorColegiadoCargoList = ojc
					.getOrgaoJulgadorColegiadoCargoList();
			cargos = new ArrayList<Cargo>();
			for (OrgaoJulgadorColegiadoCargo ojcc : orgaoJulgadorColegiadoCargoList) {
				cargos.add(ojcc.getCargo());
			}
		}
		return cargos;
	}

	public void setCargos(List<String> cargos) {
		if (this.cargos == null) {
			this.cargos = new ArrayList<Cargo>();
		} else {
			this.cargos.clear();
		}
		for (String s : cargos) {
			for (Cargo c : cargosDisponiveis) {
				if (c.getCargo().equals(s)) {
					this.cargos.add(c);
					break;
				}
			}
		}
	}

	/**
	 * Método chamado para verificar se o orgão julgador colegiado cargo esta
	 * associado a algum orgão julgador colegiado orgão julgador
	 */
	private Boolean verificarOrgaoJulgador(OrgaoJulgadorColegiadoCargo obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from OrgaoJulgadorColegiadoOrgaoJulgador o ");
		sb.append("where o.orgaoJulgadorColegiadoCargo = :orgao");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgao", obj);
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
		
	}

	/**
	 * Método chamado no relacionamento pelo PickList para verificar as
	 * diferenças ocorridas na seleção, para então remover ou inserir.
	 */
	public String updateCargos() {
		String ret = "";
		Boolean aux = Boolean.TRUE;
		OrgaoJulgadorColegiado orgaoJulgadorColegiado = OrgaoJulgadorColegiadoHome.instance().getInstance();
		List<OrgaoJulgadorColegiadoCargo> orgaoJulgadorColegiadoCargoList = verificarCargos(orgaoJulgadorColegiado);
		List<Cargo> cargoList = new ArrayList<Cargo>();
		cargoList.addAll(cargos);
		for (OrgaoJulgadorColegiadoCargo ojcc : orgaoJulgadorColegiadoCargoList) {
			if (cargoList.contains(ojcc.getCargo())) {
				cargoList.remove(ojcc.getCargo());
			} else {
				if (!verificarOrgaoJulgador(ojcc)) {
					try {
						getEntityManager().remove(ojcc);
						getEntityManager().flush();
					} catch (Exception e) {
						FacesMessages.instance().clear();
						FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
					}
				} else {
					FacesMessages.instance().add(
							StatusMessage.Severity.ERROR,
							"Este cargo " + ojcc.getCargo()
									+ " está relacionado a um órgão julgador. Dissocie-os para prosseguir.");
					break;
				}
			}
		}
		if (aux) {
			for (Cargo c : cargoList) {
				OrgaoJulgadorColegiadoCargo ojcc = new OrgaoJulgadorColegiadoCargo();
				ojcc.setCargo(c);
				ojcc.setOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
				try {
					getEntityManager().persist(ojcc);
					getEntityManager().flush();
				} catch (Exception e) {
					FacesMessages.instance().add(StatusMessage.Severity.ERROR, e.getMessage());
				}
				ret = "persisted";
			}
		}
		return ret;
	}

	public void setCargosDisponiveis(List<Cargo> cargosDisponiveis) {
		this.cargosDisponiveis = cargosDisponiveis;
	}

	public List<Cargo> getCargosDisponiveis() {
		if (cargosDisponiveis == null) {
			cargosDisponiveis = EntityUtil.getEntityList(Cargo.class);
		}
		return cargosDisponiveis;
	}

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorColegiadoCargo> verificarCargos(OrgaoJulgadorColegiado obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from OrgaoJulgadorColegiadoCargo o ");
		sb.append("where o.orgaoJulgadorColegiado = :orgao");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("orgao", obj);
		return q.getResultList();
	}

	public void setOrgaoJulgadorColegiado(OrgaoJulgadorColegiado orgaoJulgadorColegiado) {
		newInstance();
		getInstance().setOrgaoJulgadorColegiado(orgaoJulgadorColegiado);
	}

}