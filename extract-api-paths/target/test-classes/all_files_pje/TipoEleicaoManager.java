package br.jus.je.pje.manager;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.core.manager.GenericManager;
import br.jus.je.pje.persistence.dao.TipoEleicaoDao;
import br.jus.pje.je.entidades.TipoEleicao;




@Name(TipoEleicaoManager.NAME)
@AutoCreate
public class TipoEleicaoManager extends GenericManager implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final String NAME = "tipoEleicaoManager";
	
	@Logger
	private Log logger;
	
	@In
	private TipoEleicaoDao tipoEleicaoDao;
	
	@In
	protected EntityManager entityManager;
	
	public List<TipoEleicao> listTipoEleicao(){
		return tipoEleicaoDao.tipoEleicaoList();
	}

}