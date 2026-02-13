package br.jus.cnj.pje.nucleo.manager;

import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.jus.cnj.pje.business.dao.UsuarioLoginDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;

@Name(UsuarioLoginManager.NAME)
public class UsuarioLoginManager extends BaseManager<UsuarioLogin>{
	
	public static final String NAME = "usuarioLoginManager";
	
	@In
	private UsuarioLoginDAO usuarioLoginDAO;

	@Override
	protected UsuarioLoginDAO getDAO() {
		return usuarioLoginDAO;
	}

	/**
	 * metodo responsavel por encontrar o usuarioLogin da pessoa passada em parametro.
	 * @param pessoa
	 * @return
	 */
	public UsuarioLogin encontrarPorPessoa(Pessoa pessoa) {
		return encontrarPorID(pessoa.getIdPessoa());
	}
	
	public UsuarioLogin findByLogin(String login) {
		return usuarioLoginDAO.findByLogin(login);
	}
	
	/**
	 * metodo responsavel por encontrar o usuarioLogin com o id passado em parametro.
	 * @param id
	 * @return
	 */
	public UsuarioLogin encontrarPorID(Integer id) {
		return usuarioLoginDAO.find(id);
	}
	
	public void bloquearSenhaUsuario(UsuarioLogin usuario) throws PJeBusinessException{
		usuario.setStatusSenha(StatusSenhaEnum.B);
		persistAndFlush(usuario);
	}
	
	public void inicializaFalhasAutenticacao(UsuarioLogin usuario) throws PJeBusinessException {
		usuario.setFalhasSucessivas(0);
		persistAndFlush(usuario);
	}
	
	public void adicionaFalhasAutenticacao(UsuarioLogin usuario) throws PJeBusinessException {
		usuario.setFalhasSucessivas(usuario.getFalhasSucessivas() + 1);
		persistAndFlush(usuario);
	}
	
	public List<UsuarioLogin> findByName(String nomeInserido) {
		return usuarioLoginDAO.findByName(nomeInserido);
	}
	
	public void normalizaCadastroSSO(UsuarioLogin usuario) throws PJeBusinessException {
		usuario.setAtualizaSso(false);
		persistAndFlush(usuario);
	}
	
	public void atualizaCadastroSSO(UsuarioLogin usuario) throws PJeBusinessException {
		usuario.setAtualizaSso(true);
//		persistAndFlush(usuario);
	}

	public UsuarioLogin getReference(Integer idUsuario) {
		return usuarioLoginDAO.getReference(idUsuario);
	}

	public void registrarLogOnCertificado(UsuarioLogin usuario) throws PJeBusinessException {
		usuario.setTemCertificado(true);
		persistAndFlush(usuario);
	}
}