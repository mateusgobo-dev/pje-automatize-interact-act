package br.jus.cnj.pje.nucleo.manager;

import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import br.jus.cnj.pje.business.dao.AbstractUsuarioDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.view.PjeUtil;
import br.jus.pje.nucleo.entidades.Usuario;
import br.jus.pje.nucleo.enums.StatusSenhaEnum;

public abstract class AbstractUsuarioManager<E extends Usuario, D extends AbstractUsuarioDAO<E>>
		extends BaseManager<E> {
	
	public static final int TAMANHO_CPF = 11;
	public static final int TAMANHO_CNPJ = 14;
	
	
	@SuppressWarnings({ "unchecked", "unused" })
	private Class<E> getEntityClass() {
		return (Class<E>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	/**
	 * Localiza um usuario pelo seu login
	 * @param login: login do usuario
	 * @return Usuario: usuario portador do login informado
	 */
	@SuppressWarnings("unchecked")
	public E findByLogin(String login){
		return ((D)getDAO()).findByLogin(login);
	}
	
	/**
	 * verifica se o login esta disponivel
	 * 
	 * @param login
	 *            a ser verificado
	 * @param verifica se login esta em uso por um usuario com ID diferente do informado
	 * @return true se login disponivel
	 */
	@SuppressWarnings("unchecked")
	public Boolean checkLogin(String login, Integer idUsuario){
		return ((D)getDAO()).checkLogin(login, idUsuario);
	}
	
	/**
	 * Localiza pessoas do tipo tratado pela classe pelo nome
	 * @param nome: nome ou parte do nome das pesoas que se quer localizar
	 * @return List<E>
	 */
	@SuppressWarnings("unchecked")
	public List<E> findByNome(String nome) {
	  	return ((D)getDAO()).findByNome(nome);
	}
	
	public boolean authenticate(Usuario u, String password){
		return u.getSenha() != null && BCrypt.checkpw(password, u.getSenha());
	}
	
	/**
	 * Gera a data de validade da senha. A senha deve ter validade de 45 dias.
	 * @return
	 */
	public static Date gerarDataValidadeSenha(){
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_YEAR, 180);// validade de 180 dias
		Date dt = calendar.getTime();			
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		String sdt = sdf.format(dt);
		try {
			return sdf.parse(sdt);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	@Override
	public E persist(E entity) throws PJeBusinessException {
		//regras para setar o login devem ser implantadas nas classes concretas
		if(entity.getLogin() == null){
			throw new PJeBusinessException("pje.manager.abstractUsarioManager.informeLoginUsuario"); 
		}
		
		if(entity.getNome() == null || entity.getNome().trim().isEmpty()){
			throw new PJeBusinessException("pje.manager.abstractUsarioManager.informeNomeUsuario");
		}
		
		Boolean loginDisponivel = checkLogin(entity.getLogin(), entity.getIdUsuario());
		if(!loginDisponivel){
			throw new PJeBusinessException("pje.manager.abstractUsarioManager.loginExistente");
		}
		
		if(entity.getIdUsuario() == null || entity.getSenha() == null){
			setaDadosUsuarioSenha(entity, entity.getLogin());
		}
		
		if(entity.getAtivo() == null){
			entity.setAtivo(true);
		}
		
		if(entity.getBloqueio() == null){
			entity.setBloqueio(false);
		}
		
		if(entity.getProvisorio() == null){
			entity.setProvisorio(false);
		}
		
		return super.persist(entity);
	}
	
	private void setaDadosUsuarioSenha(E entity, String login){
		entity.setAtivo(true);
		entity.setBloqueio(false);
		entity.setProvisorio(false);
		entity.setLogin(login);
		entity.setHashAtivacaoSenha(PjeUtil.instance().gerarHashAtivacao(entity.getLogin()));
		entity.setStatusSenha(StatusSenhaEnum.I);	
	}
}
