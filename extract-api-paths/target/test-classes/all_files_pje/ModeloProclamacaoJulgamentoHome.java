package br.com.infox.ibpm.home;

import java.util.Date;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.itx.component.AbstractHome;
import br.jus.pje.nucleo.entidades.ModeloProclamacaoJulgamento;

@Name(ModeloProclamacaoJulgamentoHome.NAME)
@BypassInterceptors
public class ModeloProclamacaoJulgamentoHome extends AbstractHome<ModeloProclamacaoJulgamento> {

	private static final long serialVersionUID = -1420934003346170364L;

	public static final String NAME = "modeloProclamacaoJulgamentoHome";
	
	@Override
	protected boolean beforePersistOrUpdate() {
		this.getInstance().setUsuario(Authenticator.getUsuarioLogado());
		this.getInstance().setDataAtualizacao(new Date());
		return true;
	}
}
