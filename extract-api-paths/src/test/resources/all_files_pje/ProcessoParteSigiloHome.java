package br.com.infox.cliente.home;

import java.util.Date;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.ibpm.home.ProcessoHome;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteSigilo;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.SigiloStatusEnum;

@Name("processoParteSigiloHome")
@BypassInterceptors
public class ProcessoParteSigiloHome extends AbstractProcessoParteSigiloHome<ProcessoParteSigilo> {

	private static final long serialVersionUID = 1L;

	public static ProcessoParteSigiloHome instance() {
		return ComponentUtil.getComponent("processoParteSigiloHome");
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		ProcessoParte parte = ProcessoParteHome.instance().getInstance();
		Usuario usuario = ProcessoHome.instance().getUsuarioLogado();
		if (usuario instanceof PessoaFisica) {
			PessoaFisica pessoa = (PessoaFisica) usuario;
			getInstance().setUsuarioCadastro(pessoa);
			getInstance().setProcessoParte(parte);
			getInstance().setDataAlteracao(new Date());
			return super.beforePersistOrUpdate();
		} else {
			return false;
		}
	}

	@Override
	public String persist() {
		String ret = null;
		ret = super.persist();
		ProcessoParteHome ppsh = ProcessoParteHome.instance();
		if (ppsh.getParteSigilosaTran()) {
			instance.setStatus(SigiloStatusEnum.C);
		} else {
			instance.setStatus(SigiloStatusEnum.R);
		}
		if ("persisted".equals(ret)) {
			ProcessoParteHome.instance().atualizarSigilo();
			newInstance();
		}
		return ret;
	}
}