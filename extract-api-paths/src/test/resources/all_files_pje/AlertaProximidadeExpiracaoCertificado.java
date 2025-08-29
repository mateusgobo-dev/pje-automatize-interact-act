

package br.com.infox.core.certificado;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.manager.LogAcessoManager;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.util.DateUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(AlertaProximidadeExpiracaoCertificado.NAME)
@Scope(ScopeType.SESSION)
public class AlertaProximidadeExpiracaoCertificado implements Serializable{
	private static final long serialVersionUID = -8861277928790662327L;
	public static final String NAME = "alertaProximidadeExpiracaoCertificado";
	private Boolean deveExibirAlerta;
	@In
	private LogAcessoManager logAcessoManager;

	public String obterDataDeValidadeDoCertificadoDigital() {
		Usuario usuario = Authenticator.getUsuarioLogado();
		Certificado certificado = getCertificadoDoUsuario(usuario);
		if (certificado == null) {
			return "";
		}
		return DateUtil.dateToString(certificado.getDataValidadeFim());
	}

	private Certificado getCertificadoDoUsuario(Usuario usuario) {
		try {
			if (usuario == null || StringUtil.isEmpty(usuario.getCertChain())) {
				return null;
			}
			Certificado certificado = new Certificado(usuario.getCertChain());
			return certificado;
		} catch (CertificadoException e) {
			throw new RuntimeException(e);
		}

	}

	public Boolean getDeveExibirAlerta() {
		return deveExibirAlerta;
	}

	public void exibirAlertaParaUsuario() {
		this.deveExibirAlerta = true;
	}

	public void definirAlertaComoVisualizado() {
		this.deveExibirAlerta = false;
	}

}

