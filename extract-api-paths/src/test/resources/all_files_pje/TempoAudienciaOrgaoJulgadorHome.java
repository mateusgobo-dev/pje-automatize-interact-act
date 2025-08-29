package br.com.infox.cliente.home;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.suggest.OrgaoJulgadorSuggestBean;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.TempoAudienciaOrgaoJulgador;
import br.jus.pje.nucleo.entidades.TipoAudiencia;

@Name("tempoAudienciaOrgaoJulgadorHome")
@BypassInterceptors
public class TempoAudienciaOrgaoJulgadorHome extends AbstractHome<TempoAudienciaOrgaoJulgador> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final int MINIMO = 10;
	private static final int MAXIMO = 600;
	
	public void setTempoAudienciaOrgaoJulgadorIdTempoAudienciaOrgaoJulgador(Integer id) {
		setId(id);
	}

	public Integer getTempoAudienciaOrgaoJulgadorIdTempoAudienciaOrgaoJulgador() {
		return (Integer) getId();
	}
	
	@PostConstruct
	public void init(){
		newInstance();
	}
	
	@Override
	public void newInstance() {
		super.newInstance();
		OrgaoJulgadorSuggestBean ojsb = getComponent("orgaoJulgadorSuggest");
		ojsb.setInstance(null);
		OrgaoJulgador orgaoJulgadorAtual = Authenticator.getOrgaoJulgadorAtual();

		if (orgaoJulgadorAtual != null) {
			getInstance().setOrgaoJulgador(orgaoJulgadorAtual);
		}
	}

	public boolean verificaOJExistente(TempoAudienciaOrgaoJulgador obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from TempoAudienciaOrgaoJulgador o ");
		
		sb.append("where o.orgaoJulgador = :oj ");
		// [PJEII-677] Adicionado para possibilitar a inclusão de tempos para tipos de audiencia diferentes em um mesmo orgao julgador
		sb.append("and o.tipoAudiencia.idTipoAudiencia = :tipoAudiencia ");
		if (obj.getIdTempoAudienciaOrgaoJulgador() != null) {
			sb.append("and o.idTempoAudienciaOrgaoJulgador != :id");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("oj", obj.getOrgaoJulgador());
		// [PJEII-677] Adicionado para possibilitar a inclusão de tempos para tipos de audiencia diferentes em um mesmo orgao julgador
		q.setParameter("tipoAudiencia", obj.getTipoAudiencia().getIdTipoAudiencia());
		if (obj.getIdTempoAudienciaOrgaoJulgador() != null) {
			q.setParameter("id", obj.getIdTempoAudienciaOrgaoJulgador());
		}
		Long result = (Long) q.getSingleResult();
		return result > 0;
	}
	
	@Override
	protected boolean beforePersistOrUpdate() {
		
		if(instance.getTempoAudiencia() >= MINIMO && instance.getTempoAudiencia() <= MAXIMO){
			if (verificaOJExistente(instance)) {
				// [PJEII-677] Modificado para melhorar a comunicação com o usuário
				FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "tempoAudienciaOrgaoJulgador.tempoJaCadastrado");
				return false;
			}
			return super.beforePersistOrUpdate();
		} else {
			FacesMessages.instance().addFromResourceBundle(Severity.ERROR, "tempoAudienciaOrgaoJulgador.erro.tempoEntreAudiencias");
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public List<TipoAudiencia> getTipoAudienciaItens() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from TipoAudiencia o ");
		sb.append("where o.ativo = true ");
		Query q = getEntityManager().createQuery(sb.toString());
		return q.getResultList();
	}

	public void setTipoAudiencia(TipoAudiencia tipoAudiencia) {
		getInstance().setTipoAudiencia(tipoAudiencia);
	}

	public TipoAudiencia getTipoAudiencia() {
		return getInstance().getTipoAudiencia();
	}

	public void limpar() {
		OrgaoJulgadorSuggestBean ojsb = ComponentUtil.getComponent("orgaoJulgadorSuggest");
		ojsb.setInstance(null);
		newInstance();
	}

}
