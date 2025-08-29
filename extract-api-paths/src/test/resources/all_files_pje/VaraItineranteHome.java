package br.jus.csjt.pje.view.action;

import static org.jboss.seam.faces.FacesMessages.instance;

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.StatusMessage;

import br.com.infox.cliente.home.JurisdicaoHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.csjt.pje.persistence.dao.VaraItineranteList;
import br.jus.pje.jt.entidades.VaraItinerante;

/**
 * @author Rafael Barros / Sérgio Pacheco
 * @since 1.2.0
 * @see
 * @category PJE-JT
 * @class VaraItineranteHome
 * @description Classe responsável por gerenciar serviços para a entidade
 *              VaraItinerante
 */
@Name("varaItineranteHome")
@Scope(ScopeType.CONVERSATION)
public class VaraItineranteHome extends AbstractHome<VaraItinerante> {

	private static final long serialVersionUID = 1L;

	@In(create = true)
	JurisdicaoHome jurisdicaoHome;

	@Override
	public void newInstance() {
		Contexts.removeFromAllContexts("varaItineranteSuggest");
		super.newInstance();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {
		this.newInstance();
		return super.afterPersistOrUpdate(ret);
	}

	@Override
	public String persist() {
		VaraItineranteList varaItineranteList = ComponentUtil.getComponent(VaraItineranteList.NAME);
		List<VaraItinerante> listaVaraItinerante = varaItineranteList.list();
		
		for(VaraItinerante vi : listaVaraItinerante) {
			if(vi.getJurisdicaoMunicipio().equals(instance.getJurisdicaoMunicipio())) {
				instance().add(StatusMessage.Severity.ERROR, "Município da Jurisdição já incluído");
				this.instance.setJurisdicaoMunicipio(null);
				return null;
			}
		}
		
		if (this.instance.getJurisdicaoMunicipio() != null) {
			return super.persist();
		} else {
			instance().add(StatusMessage.Severity.ERROR, "Selecione da lista de sugestões um município da jurisdição");
			return null;
		}
	}

}
