package br.jus.jt.estatistica.job;

import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.service.LogService;
import br.jus.pje.nucleo.entidades.Usuario;

/**
 * [PJEII-893] Boletim estatistico.
 * Componente Seam destinado à geracao periódica de boletim estatistico.
 * 
 * @author Rafael Carvalho
 * 
 */
@Name("geradorBoletimTrigger")
@Scope(ScopeType.EVENT)
@AutoCreate
public class GeradorBoletimTrigger implements Serializable {

	private static final long serialVersionUID = 1L;

	@In
	private LogService logService;

	@Logger
	private Log log;
	@In
	private EntityManager entityManager;

	@Asynchronous
	@Transactional
	public QuartzTriggerHandle execute(@IntervalCron String cron) {
		// PJEII-4881  Tratamento de excecao para evitar que a aplicação nao inicie.
		try {
			gerarBoletim();	
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "gerarBoletim");
		}
		return null;
	}

	public void gerarBoletim() {
		Usuario u = ParametroUtil.instance().getUsuarioSistema();
		if (u == null) {
			throw new RuntimeException(
					"Não foi possível localizar o usuário de sistema. Entre em contato com os administradores.");
		}

		// executar somente quando form primeiro dia da semana (Segunda).
		Calendar c = Calendar.getInstance();
		if (Calendar.MONDAY == c.get(Calendar.DAY_OF_WEEK)) {
			// Gerar boletim (Chamar funcao)
			try {
				entityManager
				.createNativeQuery(
						"SELECT jt.boletim_gerar_boletim_estatistico(" +
								c.get(Calendar.MONTH) + ", " +
								c.get(Calendar.YEAR) +", " +
								u.getIdUsuario() +
								")"
						)
						.getSingleResult();
				entityManager.flush();
				log.info("Boletim gerado com sucesso!");
			} catch (Exception e) {
				log.error("Erro ao gerar boletim", e);
			}
		}
	}

}

