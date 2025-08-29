package br.jus.cnj.pje.nucleo.manager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.permissao.VisibilidadeValidador;
import br.jus.cnj.pje.permissao.VisibilidadeValidadorEnum;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;

/**
 * Manager responsável por avaliar se o usuário possui visibilidade.
 */
@Name("visibilidadeUsuarioManager")
public class VisibilidadeUsuarioManager {

	@Logger
	private Log log;

	@In
	private UsuarioLocalizacaoVisibilidadeManager usuarioLocalizacaoVisibilidadeManager;

	@In
	private UsuarioLocalizacaoMagistradoServidorManager usuarioLocalizacaoMagistradoServidorManager;

	public boolean isUsuarioLogadoComVisibilidade() {
		Boolean validadorValido = Boolean.FALSE;
		Boolean usuarioComVisibilidade = Boolean.FALSE;
		Usuario usuario = (Usuario) Contexts.getSessionContext().get(Authenticator.USUARIO_LOGADO);

		try {
			UsuarioLocalizacao localizacaoAtual = (UsuarioLocalizacao) Contexts.getSessionContext().get(Authenticator.USUARIO_LOCALIZACAO_ATUAL);

			if (localizacaoAtual != null) {
				UsuarioLocalizacaoMagistradoServidor magistrado = (UsuarioLocalizacaoMagistradoServidor) Contexts.getSessionContext().get(Authenticator.USUARIO_LOCALIZACAO_MAGISTRADO_SERVIDOR_ATUAL);

				usuarioComVisibilidade = usuarioLocalizacaoVisibilidadeManager.temVisibilidade(magistrado);

				if(usuarioComVisibilidade){
					return true;
				}

				for (VisibilidadeValidador validador : VisibilidadeValidadorEnum.obterValidadores(usuario)) {
					if (validador.isValido(usuario)) {
						validadorValido = Boolean.TRUE;
						break;
					}
				}
			}
		} catch (Exception e) {
			log.error("Erro ao buscar a localização do magistrado.", e);
		}

		return usuarioComVisibilidade || validadorValido;
	}
}
