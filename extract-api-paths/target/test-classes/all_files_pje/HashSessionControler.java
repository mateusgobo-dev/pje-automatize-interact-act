package br.com.infox.cliente.component.signfile;

import java.io.Serializable;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.util.RandomStringUtils;
import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.acesso.HashSession;

@Name(HashSessionControler.NAME)
public class HashSessionControler implements Serializable{

	private static final long serialVersionUID = 1L;

	public static final String NAME = "hashSessionControler";

	@In
	private EntityManager entityManager;

	public String createHashSession() throws Exception{

		Pessoa usuarioLogado = getPessoaLogada();
		String login = usuarioLogado.getLogin();

		HashSession hashSession = getHashSessionByLogin(login);

		if (hashSession != null){
			hashSession.resetExpirationDate();
			resetIp(hashSession);
			entityManager.merge(hashSession);
			entityManager.flush();
			return hashSession.getHash();
		}
		else{
			hashSession = new HashSession();
			hashSession.setPessoa(usuarioLogado);
			hashSession.setHash(generateHash());
			inicializar(hashSession);
			entityManager.persist(hashSession);
			entityManager.flush();
			return hashSession.getHash();
		}

	}

	private void inicializar(HashSession hashSession){
		hashSession.inicializar();
		resetIp(hashSession);
	}

	private void resetIp(HashSession hashSession){
		String ipRequest;
		try{
			ipRequest = LogUtil.getIpRequest();
			hashSession.setIp(ipRequest);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private Pessoa getPessoaLogada(){
		Pessoa resultado = Authenticator.getPessoaLogada();
		return entityManager.find(Pessoa.class, resultado.getIdUsuario());
	}

	private String generateHash(){
		return RandomStringUtils.randomAlphanumeric(40);
	}

	@Observer("org.jboss.seam.preDestroyContext.SESSION")
	@Transactional
	public void removeHashOnSessionEnd(){
		String login = Actor.instance().getId();
		if (login != null){
			HashSession hashSession = getHashSessionByLogin(login);
			if (hashSession != null){
				entityManager.remove(hashSession);
				entityManager.flush();
			}
		}
	}

	private HashSession getHashSessionByLogin(String login){
		String hql = "select o from HashSession o where o.pessoa.login = :login";
		Query query = entityManager.createQuery(hql);
		query.setParameter("login", login);
		return EntityUtil.getSingleResult(query);
	}

	public HashSession getHashSessionByHash(String hash){
		String hql = "select o from HashSession o where o.hash = :hash";
		Query query = entityManager.createQuery(hql);
		query.setParameter("hash", hash);
		return EntityUtil.getSingleResult(query);
	}

	public static HashSessionControler instance(){
		return ComponentUtil.getComponent(NAME);
	}

}
