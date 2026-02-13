package br.jus.je.pje.manager;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.core.manager.GenericManager;
import br.jus.je.pje.persistence.dao.EleicaoDAO;
import br.jus.je.pje.persistence.dao.TipoEleicaoDao;
import br.jus.pje.je.entidades.Eleicao;
import br.jus.pje.je.entidades.TipoEleicao;

@Name(EleicaoManager.NAME)
@AutoCreate
public class EleicaoManager extends GenericManager implements Serializable {

	private static final long	serialVersionUID	= 1L;

	public static final String	NAME				= "eleicaoManager";

	@Logger
	private Log					logger;

	@In
	protected EntityManager		entityManager;

	@In
	private EleicaoDAO			eleicaoDAO;

	@In
	private TipoEleicaoDao		tipoEleicaoDao;

	public void inactive(Eleicao ae) {
		try {

			ae.setAtivo(false);
			update(ae);
		} catch (Exception exception) {
			if (exception instanceof SQLException) {
				while (exception != null) {
					logger.info(exception); // Log the exception
					// Get cause if present
					Throwable t = exception.getCause();
					while (t != null) {
						logger.info("Cause:" + t);
						t = t.getCause();
					}
					// procees to the next exception
					exception = ((SQLException) exception).getNextException();
				}
			}
		}
	}

	public Eleicao findPorAnoETipo(Integer ano, Integer codigoTipoEleicao) {
		return eleicaoDAO.findPorAnoTipo(ano, codigoTipoEleicao);
	}

	public Eleicao obtemOuCriaEleicao(Integer anoEleicao, Integer codigoTipoEleicao) {
		Eleicao eleicao = findPorAnoETipo(anoEleicao, codigoTipoEleicao);
		if (eleicao == null) {
			eleicao = criaEleicao(anoEleicao, codigoTipoEleicao);
		}
		return eleicao;
	}
	
	public List<Eleicao> findEleicoes(Boolean ativo){
		return this.eleicaoDAO.findEleicoes(ativo);
	}

	private Eleicao criaEleicao(Integer anoEleicao, Integer codigoTipoEleicao) {
		Eleicao eleicao = new Eleicao();
		eleicao.setAno(anoEleicao);
		eleicao.setTipoEleicao(tipoEleicaoDao.find(TipoEleicao.class, codigoTipoEleicao));
		eleicao.setDataInicioPeriodoEleitoral(new Date());
		eleicao.setDataFimPeriodoEleitoral(new Date());
		eleicao.setAtivo(true);
		eleicao.setCodCadastroEleitoral("0");
		eleicaoDAO.persist(eleicao);
		return eleicao;
	}
	

}