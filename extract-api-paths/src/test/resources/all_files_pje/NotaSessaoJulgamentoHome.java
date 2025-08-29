package br.com.infox.cliente.home;

import java.util.Date;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.NotaSessaoJulgamento;

@Name("notaSessaoJulgamentoHome")
@BypassInterceptors
public class NotaSessaoJulgamentoHome extends AbstractHome<NotaSessaoJulgamento> {

	private static final long serialVersionUID = 1L;

	public void setNotaSessaoJulgamentoIdNotaSessaoJulgamento(Integer id) {
		setId(id);
	}

	public Integer getNotaSessaoJulgamentoIdNotaSessaoJulgamento() {
		return (Integer) getId();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if(getInstance().getOrgaoJulgador() == null){
			getInstance().setOrgaoJulgador(Authenticator.getOrgaoJulgadorAtual());
		}
		getInstance().setUsuarioCadastro(Authenticator.getUsuarioLogado());
		getInstance().setDataCadastro(new Date());
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		getInstance().setProcessoTrf(SessaoPautaProcessoTrfHome.instance().getListaSCO().get(0).getProcessoTrf());
		getInstance().setSessao(SessaoHome.instance().getInstance());
		return super.persist();
	}

}