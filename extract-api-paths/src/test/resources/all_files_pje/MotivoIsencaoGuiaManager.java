package br.jus.cnj.pje.nucleo.manager;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.com.infox.core.manager.GenericManager;
import br.jus.cnj.pje.business.dao.MotivoIsencaoGuiaDAO;
import br.jus.pje.nucleo.entidades.MotivoIsencaoGuia;

@Name(MotivoIsencaoGuiaManager.NAME)
@AutoCreate
public class MotivoIsencaoGuiaManager extends GenericManager implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String NAME = "motivoIsencaoManager";

	@Logger
	private Log logger;

	@In
	protected EntityManager entityManager;

	@In(create = true, required = false, value = "motivoIsencaoDAO")
	private MotivoIsencaoGuiaDAO motivoIsencaoDAO;

	public void inactive(MotivoIsencaoGuia motivo) {
		try {
			motivo.setAtivo(false);
			update(motivo);
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

	public MotivoIsencaoGuia obtemOuCriaMotivoIsencaoGuia(String dsMotivoIsencao) {
		MotivoIsencaoGuia motivoIsencao = motivoIsencaoDAO.findMotivo(dsMotivoIsencao);
		if (motivoIsencao == null) {
			motivoIsencao = criaMotivoIsencaoGuia(dsMotivoIsencao);
		}
		return motivoIsencao;
	}

	public List<MotivoIsencaoGuia> findMotivos(Boolean ativo) {
		return this.motivoIsencaoDAO.findMotivos(ativo);
	}

	private MotivoIsencaoGuia criaMotivoIsencaoGuia(String dsMotivoIsencao) {
		MotivoIsencaoGuia motivoIsencao = new MotivoIsencaoGuia();
		motivoIsencao.setDsMotivoIsencao(dsMotivoIsencao);
		motivoIsencao.setInControlaIsencao(false);
		motivoIsencao.setAtivo(true);
		motivoIsencaoDAO.persist(motivoIsencao);
		return motivoIsencao;
	}
}