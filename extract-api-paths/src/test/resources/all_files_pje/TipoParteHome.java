package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.EntityExistsException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage;

import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoParteParticipacaoEnum;
import br.jus.pje.nucleo.enums.TipoParteEnum;

@Name("tipoParteHome")
@BypassInterceptors
public class TipoParteHome extends AbstractTipoParteHome<TipoParte> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		String ret = null;
		try {
			getInstance().setTipoParte(getInstance().getTipoParte().toUpperCase());
			ret = super.persist();
		} catch (EntityExistsException e) {
			FacesMessages.instance().add(StatusMessage.Severity.ERROR, "Registro já existe.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return ret;
	}

	@Override
	public String remove(TipoParte obj) {
		setInstance(obj);
		obj.setAtivo(Boolean.FALSE);
		super.update();
		newInstance();
		refreshGrid("tipoParteGrid");
		return "updated";
	}

	@SuppressWarnings("unchecked")
	public List<TipoParte> getTipoPartesRepresentantes(ProcessoParteParticipacaoEnum p) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoParte o ");
		sb.append("where o.inTipoPrincipal = false ");
		sb.append("and  o.ativo = true ");
		sb.append("order by o.tipoParte");
		Query q = EntityUtil.getEntityManager().createQuery(sb.toString());
		List<TipoParte> tipoPartes = q.getResultList();
		return tipoPartes;
	}

	public static TipoParteHome instance() {
		return ComponentUtil.getComponent("tipoParteHome");
	}
	
	public TipoParteEnum[] getTipoParteEnumValues() {
		return TipoParteEnum.values();
	}

	public TipoParteEnum[] getTipoParteEnumO() {
		TipoParteEnum p[] = { TipoParteEnum.TC };
		return p;
	}

}