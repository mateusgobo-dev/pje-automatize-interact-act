package br.jus.cnj.pje.auditoria;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;

import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;

@Name("logLoadEvent")
@Scope(ScopeType.EVENT)
public class LogLoadEvent {
	
	public static final String INSERT_EVENT_NAME = "logEventOnPostInsert";
	public static final String UPDATE_EVENT_NAME = "logEventOnPostUpdate";
	public static final String DELETE_EVENT_NAME = "logEventOnPostDelete";
	public static final String SELECT_EVENT_NAME = "logLoadEventNow";
	
	@In(create=true, value="pjeLogInstance")
	private PjeLog pjeLog;
	
	@Observer(LogLoadEvent.SELECT_EVENT_NAME)
	public void logLoadEventCreate(Object instance) {
		if (ParametroUtil.instance().getRegistrarLogConsulta()) {
			pjeLog.log(instance, TipoOperacaoLogEnum.S);
		}
	}

	@Transactional
	@Observer(LogLoadEvent.UPDATE_EVENT_NAME)
	public void logEventOnPostUpdate(Class<?> clazz,
									 Object id, 
									 Object[] oldState,
			 						 Object[] state, 
			 						 String[] nomes, 
			 						 Integer idUsuario, 
			 						 String ip, 
			 						 String url) {
		log(clazz, id, oldState, state, nomes, idUsuario, ip, url, TipoOperacaoLogEnum.U);
	}

	@Transactional
	@Observer(LogLoadEvent.INSERT_EVENT_NAME)
	public void logEventOnPostInsert(Class<?> clazz,
									 Object id, 
									 Object[] state, 
									 String[] nomes, 
									 Integer idUsuario, 
									 String ip, 
									 String url) {
		log(clazz, id, null, state, nomes, idUsuario, ip, url, TipoOperacaoLogEnum.I);
	}
	

	@Transactional
	@Observer(LogLoadEvent.DELETE_EVENT_NAME)
	public void logEventOnPostDelete(Class<?> clazz, 
									 Object id,
									 Object[] deletedState,
			 						 String[] nomes, 
			 						 Integer idUsuario, 
			 						 String ip, 
			 						 String url) {
		log(clazz, id, deletedState, null, nomes, idUsuario, ip, url, TipoOperacaoLogEnum.D);
	}

	private void log(Class<?> clazz, 
			 Object id,
			 Object[] oldState, 
			 Object[] state, 
			 String[] nomes, 
			 Integer idUsuario, 
			 String ip, 
			 String url, 
			 TipoOperacaoLogEnum tipoOperacao){

		pjeLog.log(clazz, id, oldState, state, nomes, idUsuario, ip, url, tipoOperacao);
	}

}