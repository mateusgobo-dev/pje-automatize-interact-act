package br.jus.csjt.pje.view.action;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.home.OrgaoJulgadorHome;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.AbstractHome;
import br.com.itx.exception.AplicationException;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.csjt.pje.commons.util.OrgaoJulgadorJtUtil;
import br.jus.pje.jt.entidades.OrgaoJulgadorJt;

/**
 * Classe utilizada para acomodar informacoes da JT que complementam o orgao
 * julgador do CNJ.
 * 
 * @author Rodrigo Cartaxo / Haroldo Arouca
 * @since versao 1.2.0
 * @see OrgaoJulgador, [PJE336]
 * @category PJE-JT
 */

@Name(OrgaoJulgadorJtHome.NAME)
@BypassInterceptors
public class OrgaoJulgadorJtHome extends AbstractHome<OrgaoJulgadorJt> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "orgaoJulgadorJtHome";

	public void setOrgaoJulgadorJtIdOrgaoJulgadorJt(Integer id) {
		setId(id);
	}

	public Integer getOrgaoJulgadorJtIdOrgaoJulgadorJt() {
		return (Integer) getId();
	}

	public void wire() {

		OrgaoJulgadorHome orgaoJulgadorHome = ComponentUtil.getComponent("orgaoJulgadorHome");

		if (orgaoJulgadorHome.isManaged()) {
			OrgaoJulgadorJt orgaoJulgadorJt = null;
			try {
				// orgaoJulgadorJt = ((OrgaoJulgadorJt)
				// getEntityManager().createQuery(
				// "FROM " + OrgaoJulgadorJt.class.getSimpleName()
				// + " WHERE orgaoJulgador = " + orgaoJulgadorHome
				// .getId()).getSingleResult());

				orgaoJulgadorJt = OrgaoJulgadorJtUtil.getOrgaoJulgadorJt(getEntityManager(), orgaoJulgadorHome.getId());

				if (orgaoJulgadorJt != null) {
					setOrgaoJulgadorJtIdOrgaoJulgadorJt(orgaoJulgadorJt.getIdOrgaoJulgadorJt());
				} else {
					/**
					 * [PJEII-4647] : Não havendo informações de intersticio para o orgão julgador corrente (carregado ou novo),
					 * gera uma nova instancia para evitar "retenção" dos dados de um órgão julgador carregado anteriormente.
					 * @author fernando.junior (04/04/2013)
					 */
					newInstance();
				}

			} catch (NoResultException e) {
				// pode estar editando um orgaoJulgador que nao possui
				// orgaoJulgadorJt associado
				getInstance().setIntersticio(0);
				throw new AplicationException("Editando um orgaoJulgador que nao possui orgaoJulgadorJt associado");
			} catch (NonUniqueResultException e) {
				// mais de um orgaoJulgadorJt para o orgaoJulgador gerenciado
				getInstance().setIntersticio(0);
				throw new AplicationException("Mais de um orgaoJulgadorJt para o orgaoJulgador gerenciado.");
			}
		}
	}

	@Override
	public String persist() {
		boolean isErro = false;
		FacesMessages.instance().clear();

		if (getInstance().getIntersticio() == null) {
			getInstance().setIntersticio(ParametroUtil.getTempoMinimoAudiencia());
		}

		if (getInstance().getIntersticio() < 5) {
			isErro = true;
			FacesMessages.instance().addFromResourceBundle(Severity.INFO,
					"orgaoJulgadorJt.erroIntersticioMenorQueCinco");
		}

		if (!isErro) {
			String message = "";
			OrgaoJulgadorHome orgaoJulgadorHome = (OrgaoJulgadorHome) Component.getInstance("orgaoJulgadorHome");
			this.instance.setOrgaoJulgador(orgaoJulgadorHome.getInstance());
			message = super.persist();
			return message;
		}

		return null;
	}

	@Override
	public String update() {
		boolean isErro = false;
		FacesMessages.instance().clear();

		if (getInstance().getIntersticio() == null) {
			// Conforme regra de negocio, quando for detectado um intersticio de
			// valor nulo, este será
			// considerado 5 (cinco) dias uteis.
			getInstance().setIntersticio(5);
		}

		if (getInstance().getIntersticio() < 0) {
			isErro = true;
			FacesMessages.instance()
					.addFromResourceBundle(Severity.INFO, "orgaoJulgadorJt.erroIntersticioMenorQueZero");
		}

		if (!isErro) {
			String message = super.update();
			return message;
		}

		return null;
	}
}
