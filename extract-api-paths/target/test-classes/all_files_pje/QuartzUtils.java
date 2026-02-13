package br.com.infox.component.quartz;

import java.util.Date;
import java.util.Properties;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jbpm.util.ClassLoaderUtil;

import br.com.infox.timer.TimerUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Parametro;

@Name(QuartzUtils.NAME)
@Scope(ScopeType.EVENT)
public class QuartzUtils {

	public static final String NAME = "quartzUtils";

	private static final String ENABLED_PROPERTIE_NAME = "org.quartz.timer.enabled";
	private static final String SEAM_QUARTZ_PROPERTIES_FILE_NAME = "seam.quartz.properties";
	private static Properties quartzProperties = ClassLoaderUtil.getProperties(SEAM_QUARTZ_PROPERTIES_FILE_NAME);

	@In
	private EntityManager entityManager;

	public boolean isQuartzEnabled() {
		String enabled = quartzProperties.getProperty(ENABLED_PROPERTIE_NAME, "false");
		return Boolean.valueOf(enabled);
	}

	public boolean isJobAgendado(String idParametroIdentificacaiJob) {
		String idVerificadorPeriodicoTimer = null;
		try {
			idVerificadorPeriodicoTimer = TimerUtil.getParametro(idParametroIdentificacaiJob);
		} catch (IllegalArgumentException e) {
		}
		return idVerificadorPeriodicoTimer != null;
	}

	public void criarParametroIdentificadorJob(String idParametroIdentificacaiJob, String descricao, String triggerName) {
		Parametro p = new Parametro();
		p.setAtivo(true);
		p.setDescricaoVariavel(descricao);
		p.setDataAtualizacao(new Date());
		p.setNomeVariavel(idParametroIdentificacaiJob);
		p.setSistema(true);
		p.setValorVariavel(triggerName);
		entityManager.persist(p);
		entityManager.flush();
	}

	public static QuartzUtils instance() {
		return ComponentUtil.getComponent(NAME);
	}

}
