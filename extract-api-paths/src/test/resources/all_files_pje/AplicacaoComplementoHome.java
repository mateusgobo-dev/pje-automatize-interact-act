package br.com.infox.cliente.home;

import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.lancadormovimento.Aplicabilidade;
import br.jus.pje.nucleo.entidades.lancadormovimento.AplicacaoComplemento;

@Name(AplicacaoComplementoHome.NAME)
@BypassInterceptors
public class AplicacaoComplementoHome extends AbstractHome<AplicacaoComplemento>{

	private static final long serialVersionUID = 1L;
	public static final String NAME = "aplicacaoComplementoHome";

	public static AplicacaoComplementoHome instance(){
		return ComponentUtil.getComponent(AplicacaoComplementoHome.NAME);
	}

	@Override
	public String persist(){
		// getInstance().setEventoProcessual(EventoHome.instance().getInstance());
		String ret = super.persist();
		return ret;
	}

	@Override
	public void newInstance(){
		super.newInstance();
	}

	@Override
	public String update(){
		String ret = super.update();
		return ret;
	}

	@Override
	public String remove(AplicacaoComplemento obj){
		obj.getTipoComplemento().getAplicacaoComplementoList().remove(obj);
		obj.getAplicacaoMovimento().getAplicacaoComplementoList().remove(obj);
		obj.setAplicacaoMovimento(null);
		obj.setTipoComplemento(null);
		setInstance(obj);
		return super.remove(obj);
	}

	@Override
	protected boolean beforePersistOrUpdate(){
		// getInstance().setEventoProcessual(EventoHome.instance().getInstance());
		return super.beforePersistOrUpdate();
	}

	@SuppressWarnings("unchecked")
	public List<Aplicabilidade> getListAplicabilidadeAtivo(){
		Query q = getEntityManager().createQuery("from Aplicabilidade ss where ss.ativo = :ativo");
		q.setParameter("ativo", true);
		return (List<Aplicabilidade>) q.getResultList();
	}

}