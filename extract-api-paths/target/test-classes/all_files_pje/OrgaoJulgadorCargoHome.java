package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.pje.list.OrgaoJulgadorCargoList;
import br.com.infox.pje.manager.CargoManager;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.FacesUtil;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.pje.nucleo.entidades.Cargo;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorCargo;

@Name(OrgaoJulgadorCargoHome.NAME)
public class OrgaoJulgadorCargoHome extends AbstractOrgaoJulgadorCargoHome<OrgaoJulgadorCargo> {

	private static final long serialVersionUID = 1L;
	
	public static final String NAME = "orgaoJulgadorCargoHome";
	
	@In(create = true)
	private OrgaoJulgadorCargoList orgaoJulgadorCargoList;

	@SuppressWarnings("unchecked")
	public List<OrgaoJulgadorCargo> getOrgaoJulgadorCargoList() {
		StringBuffer ejbql = new StringBuffer();
		ejbql.append("select o from OrgaoJulgadorCargo o ");
		ejbql.append("where o.recebeDistribuicao = true");
		Query q = getEntityManager().createQuery(ejbql.toString());

		return q.getResultList();
	}

	public List<Cargo> getCargoList(){
		return ComponentUtil.getComponent(CargoManager.class).cargoItems();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setOrgaoJulgador(OrgaoJulgadorHome.instance().getInstance());

		if (getInstance().getPesoDistribuicao() == null)
			getInstance().setPesoDistribuicao(0.0);

		if (getInstance().getAcumuladorDistribuicao() == null)
			getInstance().setAcumuladorDistribuicao(0.0);

		if (getInstance().getAcumuladorProcesso() == null)
			getInstance().setAcumuladorProcesso(0.0);

		if (getInstance().getValorPeso() == null) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "O valor do peso deve estar entre 1 e 200.");
			if (this.isManaged())
				getEntityManager().refresh(getInstance());
			return Boolean.FALSE;
		}
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		String ret = super.persist();
		if (ret == null) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Este órgão julgador já possui um cargo que recebe distribuição.");
		}
		refreshGrid("orgaoJulgadorCargoGrid");
		newInstance();
		return ret;
	}
	
	/**
	 * Método responsável por persistir um novo Cargo Judicial.
	 * 
	 * @param isMediaAcumulador Caso verdadeiro, o acumulador do novo Cargo Judicial será inicializado 
	 * 		  pela média dos acumuladores dos Cargos Judiciais associados ao respectivo Órgão julgador.
	 * 		  Caso falso, o acumulador do novo Cargo Judicial será inicializado com zero.
	 * 
	 * @return
	 */
	public String persist (Boolean isMediaAcumulador) {
		if (isMediaAcumulador) {
			getInstance().setAcumuladorDistribuicao(getMediaAcumuladorDistribuicao());
		}
		
		return persist();
	}

	/**
	 * Método responsável por calcular a média aritmética dos acumuladores dos Cargos Judiciais associados ao Órgão julgador.
	 * 
	 * @return Média aritmética dos acumuladores dos Cargos Judiciais associados ao Órgão julgador.
	 */
	private Double getMediaAcumuladorDistribuicao() {
		List<OrgaoJulgadorCargo> list = this.orgaoJulgadorCargoList.getResultList();
		
		double count = 0;
		if (list != null && !list.isEmpty()) {
			for (OrgaoJulgadorCargo orgaoJulgadorCargo : list) {
				count += orgaoJulgadorCargo.getAcumuladorDistribuicao();
			}
			count = count / list.size();
		}
		
		return count;
	}

	private Boolean verificaMagistradoLocalizacao(OrgaoJulgadorCargo ojc) {
		StringBuilder query = new StringBuilder("select count(o) from UsuarioLocalizacaoMagistradoServidor o ");
		query.append("where o.orgaoJulgadorCargo = :ojc ");
		query.append("and o.orgaoJulgador = :oj ");
		query.append("and o.usuarioLocalizacao.papel.identificador = :papelMagistrado");
		Query q1 = EntityUtil.createQuery(query.toString());
		q1.setParameter("ojc", ojc);
		q1.setParameter("oj", OrgaoJulgadorHome.instance().getInstance());
		q1.setParameter("papelMagistrado", Papeis.MAGISTRADO);
		try {
			Long retorno = (Long) q1.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	private Boolean verificaUsuarioLocalizacao(OrgaoJulgadorCargo ojc) {
		StringBuilder query = new StringBuilder("select count(o) from UsuarioLocalizacaoVisibilidade o ");
		query.append("where o.orgaoJulgadorCargo = :ojc ");
		Query q1 = EntityUtil.createQuery(query.toString());
		q1.setParameter("ojc", ojc);
		try {
			Long retorno = (Long) q1.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
		
	}

	private Boolean verificaPessoaLocalizacaoMagistradoLog(OrgaoJulgadorCargo ojc) {
		StringBuilder query = new StringBuilder("select count(o) from UsuarioLocalizacaoMagistradoServidor o ");
		query.append("where o.orgaoJulgadorCargo = :ojc ");
		query.append("and o.orgaoJulgador = :oj ");
		Query q1 = EntityUtil.createQuery(query.toString());
		q1.setParameter("ojc", ojc);
		q1.setParameter("oj", OrgaoJulgadorHome.instance().getInstance());
		try {
			Long retorno = (Long) q1.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	@Override
	public String remove(OrgaoJulgadorCargo ojc) {
		if (verificaMagistradoLocalizacao(ojc)) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO,
					"Antes de remover um cargo, remova os Magistrados associados a ele.");
		} else {
			if (verificaUsuarioLocalizacao(ojc)) {
				FacesMessages.instance().add(StatusMessage.Severity.INFO,
						"Antes de remover um cargo, remova as Localizações associados a ele.");
			} else {
				if (verificaPessoaLocalizacaoMagistradoLog(ojc)) {
					FacesMessages
							.instance()
							.add(StatusMessage.Severity.INFO,
									"Antes de remover um cargo, remover a associação entre a pessoa e sua loclização magistrado.");
				} else {
					try {
						if (!OrgaoJulgadorHome.instance().getInstance().getOrgaoJulgadorCargoList().isEmpty()) {
							OrgaoJulgadorHome.instance().getInstance().getOrgaoJulgadorCargoList().remove(ojc);
						}
						newInstance();
						super.remove(ojc);
						FacesMessages.instance().clear();
						FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "orgaoJulgadorColegiadoCargo_deleted"));
					} catch (Exception e) {
						FacesMessages.instance().add(StatusMessage.Severity.INFO,
								"Erro indefinido ao remover o cargo. Verifique esse erro junto ao suporte.");
						e.printStackTrace();
					}
				}
			}
		}
		refreshGrid("orgaoJulgadorCargoGrid");
		return "";
	}

	public static OrgaoJulgadorCargoHome instance() {
		return ComponentUtil.getComponent(OrgaoJulgadorCargoHome.NAME);
	}

}