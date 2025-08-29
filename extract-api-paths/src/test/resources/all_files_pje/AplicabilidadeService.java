package br.jus.csjt.pje.business.service;

import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.lancadormovimento.Aplicabilidade;

@Name(AplicabilidadeService.NAME)
@BypassInterceptors
public class AplicabilidadeService
		extends AbstractHome<Aplicabilidade>{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "aplicabilidadeService";

	public void setAplicabilidadeId(Long id){
		setId(id);
	}

	public Long getAplicabilidadeId(){
		return (Long) getId();
	}
	
	public static AplicabilidadeService instance() {
		return ComponentUtil.getComponent(AplicabilidadeService.NAME);
	}
	
	@SuppressWarnings("unchecked")
	public List<Aplicabilidade> getListAplicabilidadeAtivo() {
		Query q = getEntityManager().createQuery("from AplicabilidadeView av order by av.codigoAplicacaoClasse, av.orgaoJustica, av.sujeitoAtivo");
		return (List<Aplicabilidade>) q.getResultList();
	}
}
