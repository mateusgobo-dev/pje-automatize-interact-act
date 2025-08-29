package br.jus.cnj.pje.nucleo.manager;

import java.util.Date;
import java.util.List;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.ibpm.entity.log.LogUtil;
import br.jus.cnj.pje.business.dao.LogAcessoDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.identidade.LogAcesso;
import br.jus.pje.nucleo.entidades.identidade.UsuarioLogin;

@Name(LogAcessoManager.NAME)
public class LogAcessoManager extends BaseManager<LogAcesso> {

	public static final String NAME = "logAcessoManager";

	@In
	private LogAcessoDAO logAcessoDAO;

	@Override
	protected LogAcessoDAO getDAO() {
		return logAcessoDAO;
	}
	
	public LogAcesso registrarTentativaLogon(UsuarioLogin usuarioLogin, Boolean bemSucedido) throws PJeBusinessException{
		return registrarTentativaLogon(usuarioLogin, bemSucedido, LogUtil.getIpRequest(100), false);
	}
	
	/**
	 * Grava no banco uma tentativa de logar no sistema
	 * 
	 * @param usuarioLogin usuario que esta tentando acessar o sistema
	 * @param bemSucedido  indica se o logon foi bem sucedido
	 * @return RegistroLogon criado e que corresponde a tentativa de logon
	 * @throws PJeBusinessException
	 */
	public LogAcesso registrarTentativaLogon(UsuarioLogin usuarioLogin, Boolean bemSucedido, String ip, boolean comCertificado) throws PJeBusinessException{		LogAcesso registroLogon = new LogAcesso();
		registroLogon.setDataEvento(new Date());
		registroLogon.setBemSucedido(bemSucedido);
		registroLogon.setUsuarioLogin(usuarioLogin);
		registroLogon.setServer(System.getProperty("jboss.server.name"));
		registroLogon.setComCertificado(comCertificado);
		registroLogon.setIp(ip);		
		persistAndFlush(registroLogon);
		return registroLogon;
	}

	/**
	 * Recupera o último login do usuário no sistema.
	 *
	 * @param idUsuario Identificador do usuário.
	 * @return Texto no formato HH:mm:ss dd/MM/YYYY.
	 */
	public String recuperarUltimoLogin(Integer idUsuario) {
		return getDAO().recuperarUltimoLogin(idUsuario);
	}

	/**
	 * metodo responsavel por recuperar todos os logs da pessoa
	 * 
	 * @param idPessoa
	 * @return
	 */
	public List<LogAcesso> recuperaLogsAcesso(Pessoa _pessoa) {
		return logAcessoDAO.recuperaTodosLogsAcesso(_pessoa);
	}
}