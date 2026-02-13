package br.com.infox.cliente.home;

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogadoLocal;

/**
 * Classe para operações com "Pessoa Assistente Advogado Local"
 * 
 */

@Name("pessoaAssistenteAdvogadoLocalHome")
@BypassInterceptors
public class PessoaAssistenteAdvogadoLocalHome extends AbstractHome<PessoaAssistenteAdvogadoLocal> {

	private static final long serialVersionUID = 1L;

	private Boolean existeLocalizacao() {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from PessoaAssistenteAdvogadoLocal o where ");
		sb.append("o.usuario = :usuario ");
		sb.append("and o.localizacaoFisica = :localizacao ");
		if (isManaged()) {
			sb.append("and o.idUsuarioLocalizacao != :id");
		}
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("usuario", instance.getUsuario());
		q.setParameter("localizacao", instance.getLocalizacaoFisica());
		if (isManaged()) {
			q.setParameter("id", instance.getIdUsuarioLocalizacao());
		}
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
		
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (getInstance().getGestor()) {
			getInstance().setPapel(ParametroUtil.instance().getPapelAssistenteGestorAdvogado());
		} else {
			getInstance().setPapel(ParametroUtil.instance().getPapelAssistenteAdvogado());
		}
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		getInstance().setResponsavelLocalizacao(Boolean.FALSE);
		getInstance().setUsuario(PessoaAssistenteAdvogadoHome.instance().getInstance().getPessoa());
		if (existeLocalizacao()) {
			FacesMessages.instance().add(Severity.ERROR, "Localização já cadastrada");
			newInstance();
			return null;
		}
		return super.persist();
	}

	@Override
	public String update() {
		if (existeLocalizacao()) {
			FacesMessages.instance().add(Severity.ERROR, "Localização já cadastrada.");
			newInstance();
			return null;
		}
		return super.update();
	}

	@SuppressWarnings("unchecked")
	public List<Localizacao> localizacoesUsuario() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o.localizacaoFisica from UsuarioLocalizacao o ");
		sb.append("where o.usuario = :usuario ");
		sb.append("AND o.papel.idPapel = :idPapel ");
		sb.append("order by upper(to_ascii(o.localizacaoFisica.localizacao))");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("usuario", Authenticator.getUsuarioLogado());
		q.setParameter("idPapel", Authenticator.getIdPapelAtual());
		return q.getResultList();
	}

	@Override
	public String remove(PessoaAssistenteAdvogadoLocal obj) {
		String ret = super.remove(obj);
		newInstance();
		return ret;
	}

	public void setAssinaDigitalmente(Boolean assinaDigitalmente) {
		if(assinaDigitalmente != null) {
			getInstance().setAssinadoDigitalmente(assinaDigitalmente);
		}	
	}

	public Boolean getAssinaDigitalmente() {
		return getInstance().getAssinadoDigitalmente();
	}

}