

package br.com.infox.cliente.home;

import java.util.List;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import br.com.infox.ibpm.home.EventoHome;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.csjt.pje.business.service.AplicabilidadeService;
import br.jus.pje.nucleo.entidades.lancadormovimento.Aplicabilidade;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoMovimento;

@Name(AplicacaoMovimentoHome.NAME)
@BypassInterceptors
public class AplicacaoMovimentoHome 
		extends 
		    AbstractHome<AplicacaoMovimento> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "aplicacaoMovimentoHome";
	
	public static AplicacaoMovimentoHome instance() {
		return ComponentUtil.getComponent(AplicacaoMovimentoHome.NAME);
	}
	
	@Override
	public String persist() {
		getInstance().setEventoProcessual(EventoHome.instance().getInstance());				
		String ret = super.persist();	
		return ret;
	}
	
	@Override
	public void newInstance() {
		super.newInstance();
	}
	
	@Override
	public String update() {
		String ret = super.update();
		return ret;
	}
	
	@Override
	public String remove(AplicacaoMovimento obj) {
		setInstance(obj);
		obj.setEventoProcessual(null);
		if (!obj.getAplicacaoComplementoList().isEmpty()){
			FacesMessages.instance().clear();
	 	 	FacesMessages.instance().add(Severity.ERROR, "Não é possível apagar essa aplicabilidade, pois existe(m) complemento(s) associado(s) a essa aplicabilidade!");
	 	 	return null;
	 	}
		return super.remove(obj);
	}
	
	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setEventoProcessual(EventoHome.instance().getInstance());
		return super.beforePersistOrUpdate();
	}
	
	public List<Aplicabilidade> getListAplicabilidadeAtivo() {
		return AplicabilidadeService.instance().getListAplicabilidadeAtivo();
	}
	
}
 
