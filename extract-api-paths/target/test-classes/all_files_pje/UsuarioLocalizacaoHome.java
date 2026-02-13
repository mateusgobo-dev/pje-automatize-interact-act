/*
 IBPM - Ferramenta de produtividade Java
 Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda.

 Este programa é software livre; você pode redistribuí-lo e/ou modificá-lo 
 sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela 
 Free Software Foundation; versão 2 da Licença.
 Este programa é distribuído na expectativa de que seja útil, porém, SEM 
 NENHUMA GARANTIA; nem mesmo a garantia implícita de COMERCIABILIDADE OU 
 ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA.
 
 Consulte a GNU GPL para mais detalhes.
 Você deve ter recebido uma cópia da GNU GPL junto com este programa; se não, 
 veja em http://www.gnu.org/licenses/   
 */
package br.com.infox.ibpm.home;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.infox.cliente.component.tree.LocalizacaoNaoEstruturadaServidorTreeHandler;
import br.com.infox.component.tree.AbstractTreeHandler;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.identidade.Papel;

@Name("usuarioLocalizacaoHome")
@BypassInterceptors
public class UsuarioLocalizacaoHome extends AbstractUsuarioLocalizacaoHome<UsuarioLocalizacao> {

	public static final String AFTER_NEW_INSTANCE_EVENT = "usuarioLocalizacao.afterNewInstanceEvent";
	private static final long serialVersionUID = 1L;
	private Localizacao localizacaoFisica;
	private Localizacao localizacaoModelo;
	private Papel papel;
	private boolean responsavelLocalizacao = Boolean.FALSE;

	public Localizacao getLocalizacaoFisica() {
		return localizacaoFisica;
	}

	public void setLocalizacaoFisica(Localizacao localizacaoFisica) {
		this.localizacaoFisica = localizacaoFisica;
	}

	public static UsuarioLocalizacaoHome instance() {
		return ComponentUtil.getComponent("usuarioLocalizacaoHome");
	}

	@Override
	public void setId(Object id) {
		boolean changed = id != null && !id.equals(getId());
		super.setId(id);
		
		if (isManaged() && changed) {
			localizacaoFisica = getInstance().getLocalizacaoFisica();
			papel = getInstance().getPapel();			
			localizacaoModelo = getInstance().getLocalizacaoModelo();
			responsavelLocalizacao = getInstance().getResponsavelLocalizacao();
		}
	}

	@Override
	public void newInstance() {
		List<String> lockedFields = getLockedFields();

		if (lockedFields.size() > 0) {
			try {
				clearUnlocked();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			setId(null);
			clearForm();
			instance = createInstance();
		}

		localizacaoFisica = null;
		localizacaoModelo = null;
		papel = null;
		responsavelLocalizacao = Boolean.FALSE;
		Events.instance().raiseEvent(AFTER_NEW_INSTANCE_EVENT, getInstance());
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		getInstance().setLocalizacaoFisica(localizacaoFisica);
		getInstance().setLocalizacaoModelo(localizacaoModelo);
		getInstance().setPapel(papel);
		getInstance().setResponsavelLocalizacao(responsavelLocalizacao);
		return true;
	}

	@Override
	public String persist() {
		getInstance().setResponsavelLocalizacao(responsavelLocalizacao);
		if (checkPapelLocalizacao(instance)) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Localização e papel duplicados");
			refreshGrid("usuarioLocalizacaoGrid");
			return null;
		}
		Usuario usuario = EntityUtil.find(Usuario.class, getInstance().getUsuario().getIdUsuario());
		usuario.getUsuarioLocalizacaoList().add(getInstance());
		String msg = super.persist();
		getInstance().setUsuario(usuario);
		refreshGrid("usuarioLocalizacaoGrid");

		// TODO isso tem de sair daqui, usar um observer para o evento de
		// usuarioLocalizacao persist
		AbstractTreeHandler<?> tree = getComponent("localizacaoSetorPJETree");
		if (tree != null) {
			tree.clearTree();
		}
		tree = getComponent("papelUsuarioLocalizacaoPJETree");
		if (tree != null) {
			tree.clearTree();
		}
		tree = getComponent("localizacaoSetorTree");
		if (tree != null) {
			tree.clearTree();
		}
		tree = getComponent("uadLocalizacaoTree");
		if (tree != null) {
			tree.clearTree();
		}
		tree = getComponent("papelTree");
		if (tree != null) {
			tree.clearTree();
		}
		tree = getComponent("localizacaoEstruturaTree");
		if (tree != null) {
			tree.clearTree();
		}
		tree = getComponent("localizacaoNaoEstruturadaServidorTree");
		if (tree != null) {
			tree.clearTree();
		}		
		FacesMessages.instance().clear();
		FacesMessages.instance().add("Registro inserido com sucesso");
		return msg;
	}

	@Override
	public String update() {
		if (checkPapelLocalizacaoUpdate(instance)) {
			FacesMessages.instance().clear();
			FacesMessages.instance().add(Severity.ERROR, "Localização e papel duplicados");
			refreshGrid("usuarioLocalizacaoGrid");
			return null;
		}
		String update = super.update();
		refreshGrid("usuarioLocalizacaoGrid");
		return update;
	}

	@Override
	public String remove(UsuarioLocalizacao obj) {
		setInstance(obj);
		Usuario usuario = getInstance().getUsuario();
		String msg = super.remove(obj);
		getInstance().setUsuario(usuario);
		return msg;
	}

	public void setPapel(Papel papel) {
		this.papel = papel;
	}

	public Papel getPapel() {
		return papel;
	}
	
	@Observer("evtSelectLocalizacao")
	public void setLocalizacaoEstrutura(Localizacao localizacao) {
		int idLocalizacao = 0;
		
		if (localizacao != null && localizacao.getEstruturaFilho() != null) {
			idLocalizacao = localizacao.getEstruturaFilho().getIdLocalizacao();
		}
		
		LocalizacaoNaoEstruturadaServidorTreeHandler.instance().setIdLocalizacao(idLocalizacao);
		LocalizacaoNaoEstruturadaServidorTreeHandler.instance().clearTree();
		setLocalizacaoModelo(null);
	}
	
	@Observer("evtSelectLocalizacaoEstrutura")
	public void setLocalizacaoModelo(Localizacao localizacao, Localizacao localizacaoModelo) {
		setLocalizacaoModelo(localizacaoModelo);
	}

	public void setLocalizacaoModelo(Localizacao localizacaoModelo) {
		this.localizacaoModelo = localizacaoModelo;
	}

	public Localizacao getLocalizacaoModelo() {
		return localizacaoModelo;
	}

	public boolean isResponsavelLocalizacao() {
		return responsavelLocalizacao;
	}

	public void setResponsavelLocalizacao(boolean responsavelLocalizacao) {
		this.responsavelLocalizacao = responsavelLocalizacao;
	}

	public boolean checkPapelLocalizacao(UsuarioLocalizacao usuarioLocalizacao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from UsuarioLocalizacao o where ");
		sb.append(" o.usuario = :usuario and o.papel = :papel and ");
		sb.append("o.localizacaoFisica = :localizacaoFisica");
		if (localizacaoModelo != null) {
			sb.append(" and o.localizacaoModelo = :localizacaoModelo");
		}
		String sql = sb.toString();
		EntityManager em = getEntityManager();
		Query query = em.createQuery(sql)
				.setParameter("usuario", usuarioLocalizacao.getUsuario())
				.setParameter("papel", papel)
				.setParameter("localizacaoFisica", localizacaoFisica);
		if (localizacaoModelo != null) {
			query.setParameter("localizacaoModelo", localizacaoModelo);
		}
		Long u = (Long) query.getSingleResult();
		return u > 0;
	}

	public boolean checkPapelLocalizacaoUpdate(UsuarioLocalizacao usuarioLocalizacao) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(o) from UsuarioLocalizacao o where ");
		sb.append(" o.usuario = :usuario and o.papel = :papel and ");
		sb.append(" o.localizacaoFisica = :localizacaoFisica and o.idUsuarioLocalizacao != :idUsuarioLocalizacao");
		if (localizacaoModelo != null) {
			sb.append(" and o.localizacaoModelo = :localizacaoModelo");
		}
		String sql = sb.toString();
		EntityManager em = getEntityManager();
		Query query = em.createQuery(sql).setParameter("usuario", usuarioLocalizacao.getUsuario())
				.setParameter("papel", papel).setParameter("localizacaoFisica", localizacaoFisica)
				.setParameter("idUsuarioLocalizacao", usuarioLocalizacao.getIdUsuarioLocalizacao());
		if (localizacaoModelo != null) {
			query.setParameter("localizacaoModelo", localizacaoModelo);
		}
		Long u = (Long) query.getSingleResult();
		return u > 0;
	}
}