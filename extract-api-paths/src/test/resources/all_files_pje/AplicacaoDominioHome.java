

package br.com.infox.ibpm.home;

import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.FacesUtil;
import br.jus.csjt.pje.business.service.AplicabilidadeService;
import br.jus.pje.nucleo.entidades.AplicabilidadeView;
import br.jus.pje.nucleo.entidades.lancadormovimento.Aplicabilidade;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoDominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.Dominio;
import br.jus.pje.nucleo.entidades.lancadormovimento.TipoComplementoComDominio;

 
@Name(AplicacaoDominioHome.NAME)
@BypassInterceptors
public class AplicacaoDominioHome extends AbstractHome<AplicacaoDominio>{

	public static final String NAME = "aplicacaoDominioHome";
	private static final long serialVersionUID = 1L;
	
	@Override
	public String persist() {
		
		String ret = super.persist();
		
		TipoComplementoHome tipoComplementoHome = ComponentUtil.getComponent(TipoComplementoHome.NAME);
		
		if(tipoComplementoHome.getInstance() instanceof TipoComplementoComDominio){
			
			TipoComplementoComDominio tipoComplementoComDominio = (TipoComplementoComDominio) tipoComplementoHome.getInstance();
			
			if(tipoComplementoComDominio.getAplicacaoDominioList() == null)
				tipoComplementoComDominio.setAplicacaoDominioList(new ArrayList<AplicacaoDominio>());
			
			tipoComplementoComDominio.getAplicacaoDominioList().add(getInstance());
			
			tipoComplementoHome.update();
		}
		
		newInstance();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "aplicacaoDominio_created"));
		
		return ret;
	}
	
	@Override
	protected String afterPersistOrUpdate(String ret) {
		FacesMessages.instance().clear();
		if (ret.equals("updated"))
		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "aplicacaoDominio_updated"));
		if (ret.equals("persisted"))
		FacesMessages.instance().add(Severity.INFO, FacesUtil.getMessage("entity_messages", "aplicacaoDominio_created"));

		return ret;
	}
	
	@Override
	public String remove(AplicacaoDominio aplicacaoDominio) {
		TipoComplementoHome tipoComplementoHome = ComponentUtil.getComponent(TipoComplementoHome.NAME);
		
		if(tipoComplementoHome.getInstance() instanceof TipoComplementoComDominio){
			TipoComplementoComDominio tipoComplementoComDominio = (TipoComplementoComDominio) tipoComplementoHome.getInstance();
			tipoComplementoComDominio.getAplicacaoDominioList().remove(aplicacaoDominio);
		}
		
		aplicacaoDominio.setDominio(null);
		aplicacaoDominio.setAplicabilidade(null);
		
		String ret = super.remove(aplicacaoDominio);
		
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO,"Registro removido com Sucesso.");
		
		return ret;
	}
	
	
	public void setAplicacaoDominioId(Long id) {
		setId(id);
	}

	public Long getAplicacaoDominioId() {
		return (Long) getId();
	}
	
	public void setDominio(Dominio dominio) {
		getInstance().setDominio(dominio);
	}
	

	public Dominio getDominio() {
		return getInstance().getDominio();
	}
	
	public void setAplicabilidade(AplicabilidadeView aplicabilidade) {
		getInstance().setAplicabilidade(aplicabilidade);
	}
	
	public AplicabilidadeView getAplicabilidade() {
		return getInstance().getAplicabilidade();
	}
	
	
	public List<Aplicabilidade> obtemListaAplicabilidade(){
		return AplicabilidadeService.instance().getListAplicabilidadeAtivo();
	}
	
	public List<Dominio> obtemListaDominio(){
		return DominioHome.instance().getListDominioAtivo();
	}
}
