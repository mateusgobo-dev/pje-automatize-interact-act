package br.com.infox.cliente.home;

import java.util.List;
import javax.persistence.Query;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.CondicaoSuspensao;
import br.jus.pje.nucleo.entidades.TipoSuspensao;

@Name("condicaoSuspensaoHome")
@BypassInterceptors
public class CondicaoSuspencaoHome extends AbstractHome<CondicaoSuspensao>{

	private static final long serialVersionUID = 1L;

	public static CondicaoSuspencaoHome instance(){
		return ComponentUtil.getComponent("condicaoSuspensaoHome");
	}

	@Override
	public String remove(CondicaoSuspensao obj){
		return super.inactive(obj);
	}

	public List<CondicaoSuspensao> getCondicaoSuspensaoList(TipoSuspensao tipo){
		Query query = getEntityManager().createQuery("from CondicaoSuspensao o where o.tipoSuspensao = :tipo ");
		query.setParameter("tipo", tipo);
		return query.getResultList();
	}

	public List<CondicaoSuspensao> getCondicaoSuspensaoList(){
		if (TipoSuspensaoHome.instance().getInstance() != null){
			return (getCondicaoSuspensaoList(TipoSuspensaoHome.instance().getInstance()));
		}
		else{
			return getEntityManager().createQuery("from CondicaoSuspensao o ").getResultList();
		}
	}

	@Override
	protected boolean beforePersistOrUpdate(){
		instance.setAtivo(true);
		instance.setTipoSuspensao(TipoSuspensaoHome.instance().getInstance());
		List<CondicaoSuspensao> condicoes = getCondicaoSuspensaoList(getInstance().getTipoSuspensao());
		for (CondicaoSuspensao condicao : condicoes){
			if (condicao.equals(getInstance())){
				addFacesMessageFromResourceBundle("Condição já cadastrada");
				return false;
			}
		}
		return true;
	}

	public void adicionarCondicaoSuspensao(){
		getInstance().setTipoSuspensao(TipoSuspensaoHome.instance().getInstance());
		if (!beforePersistOrUpdate()){
			setInstance(new CondicaoSuspensao());
			return;
		}
		getEntityManager().persist(getInstance());
		getEntityManager().flush();
		setInstance(new CondicaoSuspensao());
		addFacesMessageFromResourceBundle("CondicaoSuspensao_created");
	}

	@Override
	protected String afterPersistOrUpdate(String ret){
		setInstance(null);
		return super.afterPersistOrUpdate(ret);
	}
}