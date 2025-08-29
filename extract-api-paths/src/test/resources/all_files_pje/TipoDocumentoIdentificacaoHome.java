package br.com.infox.cliente.home;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.component.AbstractHome;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;
import br.jus.pje.nucleo.enums.TipoPessoaEnum;

@Name("tipoDocumentoIdentificacaoHome")
@BypassInterceptors
public class TipoDocumentoIdentificacaoHome extends AbstractHome<TipoDocumentoIdentificacao> {

	private static final long serialVersionUID = 1L;
	public static final String TIPOPASSAPORTE = "PAS";
	public static final String TIPOOAB = "OAB";
	public static final String TIPOCPF = "CPF";
	public static final String tipoCPJ = "CPJ";
	public static final String TIPORJI = "RJI";

	private boolean lockCodTipo = false;

	public void setTipoDocumentoIdentificaoIdDocumentoIdentificacao(String id) {
		setId(id);
	}

	public String getTipoDocumentoIdentificaoIdDocumentoIdentificacao() {
		return (String) getId();
	}

	@Override
	protected TipoDocumentoIdentificacao createInstance() {
		TipoDocumentoIdentificacao tipoDocumentoIdentificacao = new TipoDocumentoIdentificacao();
		return tipoDocumentoIdentificacao;
	}

	public static TipoDocumentoIdentificacaoHome getHome() {
		return ComponentUtil.getComponent("tipoDocumentoIdentificacaoHome");
	}

	public TipoDocumentoIdentificacao getTipoDocumentoIdentificacao(String codTipo) {
		String sql = "select o from TipoDocumentoIdentificacao o where o.codTipo = :codTipo";
		Query q = EntityUtil.getEntityManager().createQuery(sql);
		q.setParameter("codTipo", codTipo);
		return (TipoDocumentoIdentificacao) q.getResultList().get(0);
	}

	@Override
	public String update() {
	    getInstance().setCodTipo(getTipoDocumentoIdentificaoIdDocumentoIdentificacao());
	    return super.update();
	}

	public TipoPessoaEnum[] getTipoPessoaValues() {
		return TipoPessoaEnum.values();
	}

	public void verificaCodigoDisabled() {
		if (getTab().equals("form")) {
			if (getId() == null) {
				lockCodTipo = false;
			} else {
				lockCodTipo = true;
			}
		}
	}

	public void setLockCodTipo(boolean lockCodTipo) {
		this.lockCodTipo = lockCodTipo;
	}

	public boolean isLockCodTipo() {
		return lockCodTipo;
	}

	@Override
	public boolean isEditable() {
		return ParametroUtil.instance().getPermitirCadastrosBasicos();
	}
}