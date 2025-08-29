package br.com.infox.cliente.home;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.service.LocalizacaoService;
import br.jus.pje.nucleo.entidades.GrupoOficialJustica;

@Name("grupoOficialJusticaHome")
@BypassInterceptors
public class GrupoOficialJusticaHome extends AbstractGrupoOficialJusticaHome<GrupoOficialJustica> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		String ret = null;
		try {
			ret = super.persist();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	public void atualizarGrupo() {
		Boolean ativo = getInstance().getAtivo();
		GrupoOficialJustica grupoOficialJustica = getInstance().getGrupoOficialInstancia();
		setInstance(grupoOficialJustica);

		getInstance().setAtivo(ativo);
		getInstance().setCentralMandado(CentralMandadoHome.instance().getInstance());
		super.update();
		refreshGrid("grupoOficialJusticaCentralMandadoGrid");
		UIComponent form = ComponentUtil.getUIComponent("grupoOficialJusticaCentralMandadoForm");
		ComponentUtil.clearChildren(form);
		newInstance();
		FacesMessages.instance().clear();
	}

	public void removeCentral(GrupoOficialJustica obj) {
		setInstance(obj);
		getInstance().setCentralMandado(null);
		super.update();
		newInstance();
		refreshGrid("grupoOficialJusticaCentralMandadoGrid");
		UIComponent form = ComponentUtil.getUIComponent("grupoOficialJusticaCentralMandadoForm");
		ComponentUtil.clearChildren(form);
	}

	@Override
	public String update() {
		String ret = null;
		try {
			getEntityManager().merge(getInstance());
			getEntityManager().flush();
			ret = getUpdatedMessage().getValue().toString();
			FacesMessages.instance().add(StatusMessage.Severity.INFO, "Registro alterado com sucesso");
		} catch (Exception e) {
			Throwable cause = e.getCause();
			if (cause instanceof ConstraintViolationException) {
				FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já cadastrado!");
			}
		}
		return ret;
	}

	@SuppressWarnings("unchecked")
	public List<GrupoOficialJustica> getGrupoOficialJusticaList() {
		String query = "select o from GrupoOficialJustica o where o.centralMandado = :centralMandado";

		Query q = getEntityManager().createQuery(query);
		q.setParameter("centralMandado", CentralMandadoHome.instance().getInstance());
		
		return q.getResultList();
	}

	public static GrupoOficialJusticaHome instance() {
		return ComponentUtil.getComponent("grupoOficialJusticaHome");
	}
	
	public String selectPorPerfil() {
		if (!Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("admin") || 
				!Authenticator.getPapelAtual().getIdentificador().equalsIgnoreCase("administrador")) {
	
			LocalizacaoService localizacaoService = ComponentUtil.getComponent("localizacaoService");
			
			String subQueryIdsCentralMandado = 
				String.format("select cml.centralMandado.idCentralMandado from CentralMandadoLocalizacao cml where cml.localizacao.idLocalizacao in %s", 
				localizacaoService.getTreeIds(Authenticator.getUsuarioLocalizacaoAtual().getLocalizacaoFisica()));
			
			return " where o.centralMandado.idCentralMandado in ( " + subQueryIdsCentralMandado + " ) ";
		}		
		return StringUtils.EMPTY;
	}
}