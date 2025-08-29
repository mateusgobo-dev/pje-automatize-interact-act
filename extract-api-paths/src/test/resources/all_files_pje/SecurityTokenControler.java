package br.com.infox.cliente.component.securitytoken;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.Expiration;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.RandomStringUtils;
import org.jboss.seam.util.Strings;

import br.com.infox.cliente.util.ProjetoUtil;
import br.com.infox.ibpm.service.LogService;
import br.com.infox.utils.Constantes;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.nucleo.entidades.acesso.Token;
import br.jus.pje.nucleo.entidades.acesso.TokenQuery;
import br.jus.pje.nucleo.util.Crypto;
import br.jus.pje.nucleo.util.DateUtil;

@Name(SecurityTokenControler.NAME)
@AutoCreate
public class SecurityTokenControler implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "securityTokenControler";

	private static final int TEMPO_EXPIRACAO = 1000 * 60 * 15;
	private static final int TOKEN_LENGTH = 30;

	@In
	private LogService logService;
	
	@In
	private EntityManager entityManager;
	
	@In
	private ParametroService parametroService;
	
	@In(required=false)
	private FacesContext facesContext;
	
	@Logger
	private Log log;
	
	private Crypto crypto;
	
	public SecurityTokenControler() {
		super();
		crypto = new Crypto(ProjetoUtil.getChaveCriptografica());
	}

	public String createToken(String ip){
		String tokenId = RandomStringUtils.randomAlphabetic(TOKEN_LENGTH);
		Token token = new Token(tokenId, ip);
		entityManager.persist(token);
		entityManager.flush();
		return tokenId;
	}

	public boolean isTokenValido(String tokenId, String ip){
		Token token = getToken(tokenId, ip);
		return token != null;
	}

	private Token getToken(String tokenId, String ip){
		Query query = entityManager.createNamedQuery(TokenQuery.OBTER_TOKEN);
		query.setParameter(TokenQuery.PARAM_ID_TOKEN, tokenId);
		query.setParameter(TokenQuery.PARAM_IP, ip);
		return EntityUtil.getSingleResult(query);
	}

	private void checkToken(Token token, Date data){
		long tempoDecorrido = data.getTime() - token.getDataCriacao().getTime();
		if (tempoDecorrido > TEMPO_EXPIRACAO){
			entityManager.remove(token);
			entityManager.flush();
		}
	}

	@Asynchronous
	@Transactional
	public QuartzTriggerHandle removeExpirados(@Expiration Date inicio, @IntervalCron String cron){
		// PJEII-4881  Tratamento de excecao para evitar que a aplicação nao inicie.
		try {
			removeExpirados();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "removeExpirados");
		}
		return null;
	}
	
	/**
	 * PJEII-4881 Evitar o erro na inicialização do PJe.
	 * @author Rafael Carvalho (CSJT)	
	 */
	private void removeExpirados() {
		Date date = new Date();
		Collection<Token> tokenList = EntityUtil.getEntityList(Token.class, entityManager);
		for (Token token : tokenList){
			checkToken(token, date);
		}
	}

	public static SecurityTokenControler instance(){
		return ComponentUtil.getComponent(NAME);
	}
	
	private String getChaveCriptografica(){
		String chaveCriptografica = parametroService.valueOf(Parametros.CHAVE_VOLATIL_CRIPTOGRAFIA);
		if (Strings.isEmpty(chaveCriptografica)){
			chaveCriptografica = Constantes.CHAVE_PADRAO_CRIPTOGRAFIA;
		}
		
		return chaveCriptografica;
	}
	
	public String gerarChaveAcessoProcesso(Integer id){
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		return gerarChaveAcessoProcesso(id, session);
		
	}
	
	public String gerarChaveAcessoGenerica(String message) {
		return gerarChaveAcessoGenerica(message,null);
	}
	
	public String gerarChaveAcessoGenerica(String message,Integer diasDuracao) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		if(diasDuracao != null) {
			cal.add(Calendar.DATE, diasDuracao);
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Crypto c = new Crypto(getChaveCriptografica());
		String chaveCodificada = c.encodeDES(message + "-"+ sdf.format(cal.getTime()));
		return chaveCodificada;
	}
	
	public String gerarChaveAcessoProcessoConsultaPublica(Integer id){
		return this.gerarChaveAcessoGenerica(String.valueOf(id));
	}
	
	public String gerarChaveAcessoProcesso(Integer id, HttpSession session){
		String chaveCodificada = crypto.encodeDES((id + ":" + session.getId()));
		return chaveCodificada;
	}
	
	public String gerarChaveAcessoProcesso(Integer id, String session){
		String chaveCodificada = crypto.encodeDES((id + ":" + session));
		return chaveCodificada;
	}	
	
	public String gerarChaveAcessoProtocolo(Integer id, String msg){
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		String chaveCodificada = crypto.encodeDES((id + "||" + msg + ":::" + session.getId()));
		return chaveCodificada;
		
	}
	
	public String verificaChaveAcessoProtocolo(){
		String chave = facesContext.getExternalContext().getRequestParameterMap().get("ca");
		String chaveDecodificada = crypto.decodeDES(chave);
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		String[] valores = chaveDecodificada.split(":::");
		if (valores.length == 2 && valores[1].contains(session.getId())){
			return valores[0];
		}
		return null;
	}
	
	public Integer verificaChaveAcesso(String ca){
		String chave;
		if(ca == null){
			chave = facesContext.getExternalContext().getRequestParameterMap().get("ca");
		} else {
			chave = ca;
		}
		if(chave != null && !chave.isEmpty()){
			String chaveDecodificada = crypto.decodeDES(chave);
			HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
	
			//[PJE-14983] testando a sessionId passada via url rewriting.  
			HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
			String sessionId = request.getRequestedSessionId() == null ? session.getId() : request.getRequestedSessionId();
			String[] valores = chaveDecodificada.split(":");
			if (valores.length == 2 && valores[1].contains(sessionId)){
				return Integer.parseInt(valores[0]);
			}
		}

		return null;
	}
	
	public Integer verificaChaveAcesso(){
		return verificaChaveAcesso(null);
	}
	
	public String validarChaveAcessoGenerica(String messageToDecode){
		String resultado = null;
		if(StringUtils.isNotBlank(messageToDecode)){
			Crypto c = new Crypto(getChaveCriptografica());
			String chaveDecodificada = c.decodeDES(messageToDecode);
			String[] valores = chaveDecodificada.split("-");
			if (valores.length == 2) {
				String tempo = valores[1];
				SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
				Calendar hoje = Calendar.getInstance();
				hoje.setTime(DateUtil.getDataSemHora(new Date()));
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(sdf.parse(tempo));
					if(!hoje.after(cal)){
						resultado = chaveDecodificada.split("-")[0];
					}
				} catch (ParseException e) {
					// Nothing to show.
				}
			}
		}
		return resultado;
	}
	
	public Integer verificaChaveAcessoConsultaPublica(){
		Integer resultado = 0;
		String chave = facesContext.getExternalContext().getRequestParameterMap().get("ca");
		if(StringUtils.isNotBlank(chave)){
			String decodedMessage = this.validarChaveAcessoGenerica(chave);
			if(StringUtils.isNotBlank(decodedMessage) && StringUtils.isNumeric(decodedMessage)) {
				resultado = Integer.parseInt(decodedMessage);
			}
		}
		
		return resultado;
	}
}
