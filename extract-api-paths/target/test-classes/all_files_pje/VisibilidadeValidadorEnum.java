package br.jus.cnj.pje.permissao;

import java.util.ArrayList;
import java.util.List;

import br.jus.pje.nucleo.entidades.Usuario;

/**
 * Enum que registra os validadores de visibilidade dos usuários do sistema.
 * Ponto de partida para acesso as implementações da interface
 * {@link VisibilidadeValidador}.
 */
public enum VisibilidadeValidadorEnum {

	ADMINISTRADOR(new AdministradorVisibilidadeValidador()),
	MAGISTRADO(new MagistradoVisibilidadeValidador()),
	IGNORA(new IgnoraVisibilidadeValidador());

	private final VisibilidadeValidador validador;

	private VisibilidadeValidadorEnum(VisibilidadeValidador validador) {
		this.validador = validador;
	}

	/**
	 * Obtém os validadores que devem ser executados para o usuário.
	 * 
	 * @param usuario
	 * 
	 * @return Validadores que devem ser executados para validar a visibilidade.
	 */
	public static List<VisibilidadeValidador> obterValidadores(Usuario usuario) {
		List<VisibilidadeValidador> validadores = new ArrayList<VisibilidadeValidador>();

		for (VisibilidadeValidadorEnum visibilidade : VisibilidadeValidadorEnum.values()) {
			if (visibilidade.getValidador().isPossivelValidar(usuario)) {
				validadores.add(visibilidade.getValidador());
			}
		}

		if (validadores.isEmpty()) {
			validadores.add(new SemValidacaoVisibilidadeValidador());
		}

		return validadores;
	}

	public VisibilidadeValidador getValidador() {
		return validador;
	}

}
