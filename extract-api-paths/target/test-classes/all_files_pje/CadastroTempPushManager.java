package br.jus.cnj.pje.nucleo.manager;

import java.util.Calendar;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import br.com.infox.cliente.util.PushUtil;
import br.jus.cnj.pje.business.dao.CadastroTempPushDAO;
import br.jus.pje.nucleo.entidades.CadastroTempPush;
import br.jus.pje.nucleo.entidades.TipoDocumentoIdentificacao;

@Name(CadastroTempPushManager.NAME)
public class CadastroTempPushManager extends BaseManager<CadastroTempPush> {
	public static final String NAME = "cadastroTempPushManager";
	private final int QTD_DIAS_EXPIRACAO = 5;
	
	@In
	private CadastroTempPushDAO cadastroTempPushDAO;
	
	@Override
	protected CadastroTempPushDAO getDAO() {
		return this.cadastroTempPushDAO;
	}
	
	/**
	 * Método responsável por recuperar o objeto {@link CadastroTempPush} pelo login.
	 * 
	 * @param login Login.
	 * @return {@link CadastroTempPush}.
	 */
	public CadastroTempPush recuperarCadastroTempPushByLogin(String login){
		return cadastroTempPushDAO.recuperarCadastroTempPushByLogin(login);
	}
	
	/**
	 * Método responsável por recuperar o objeto {@link CadastroTempPush} pelo código do hash.
	 * 
	 * @param cdHash Código do hash.
	 * @return {@link CadastroTempPush}.
	 */
	public CadastroTempPush recuperarCadastroTempPushByHash(String cdHash){
		return cadastroTempPushDAO.recuperarCadastroTempPushByHash(cdHash);
	}
	
	
	/**
	 * Método responsável por criar um objeto {@link CadastroTempPush} o qual contém as informações temporárias do usuário no cadastro do push.
	 * 
	 * @param email Email.
	 * @param nrDocumento Número do documento de identificação.
	 * @param tipoDocumentoIdentificacao Tipo do documento de identificação.
	 * @return Objeto {@link CadastroTempPush} o qual contém as informações temporárias do usuário no cadastro do push.
	 */
	public CadastroTempPush criarNovoCadastro(
			String email, String nrDocumento, TipoDocumentoIdentificacao tipoDocumentoIdentificacao) {
		
		CadastroTempPush cadastroTempPush = new CadastroTempPush();
		cadastroTempPush.setDsEmail(email);
		cadastroTempPush.setNrDocumento(nrDocumento);
		cadastroTempPush.setConfirmado(false);
		cadastroTempPush.setDtInclusao(Calendar.getInstance().getTime());
		cadastroTempPush.setTipoDocumentoIdentificacao(tipoDocumentoIdentificacao);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(cadastroTempPush.getDtInclusao());
		calendar.add(Calendar.DATE, QTD_DIAS_EXPIRACAO);
		
		cadastroTempPush.setDtExpiracao(calendar.getTime());
		cadastroTempPush.setCdHash(PushUtil.gerarHash(email + nrDocumento + calendar.getTime()));

		return cadastroTempPush;
	}
	
	/**
	 * Método responsável por atualizar o objeto que contém as informações temporárias do usuário no cadastro do push.
	 * 
	 * @param cadastroTempPush Objeto que contém as informações temporárias do usuário no cadastro do push.
	 * @param email Email.
	 * @return Objeto {@link CadastroTempPush} contendo informações temporárias do usuário no cadastro do push atualizadas.
	 */
	public CadastroTempPush atualizarCadastro(CadastroTempPush cadastroTempPush, String email) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, QTD_DIAS_EXPIRACAO);
		
		cadastroTempPush.setDtExpiracao(calendar.getTime());
		cadastroTempPush.setDsEmail(email);
		cadastroTempPush.setCdHash(PushUtil.gerarHash(email + cadastroTempPush.getNrDocumento() + calendar.getTime()));
		
		return cadastroTempPush;
	}

}
