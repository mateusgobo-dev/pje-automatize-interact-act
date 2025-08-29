package br.com.infox.pje.processor;

import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.annotations.async.IntervalCron;
import org.jboss.seam.async.QuartzTriggerHandle;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.service.LogService;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.CadastroTempPush;

@Name(ExcluirPreCadastroPushProcessor.NAME)
@AutoCreate
public class ExcluirPreCadastroPushProcessor {
	public static final String NAME = "excluirPreCadastroPushProcessor";

	@In
	private LogService logService;

	@Logger
	private Log log;
	
	public static ExcluirPreCadastroPushProcessor instance() {
		return (ExcluirPreCadastroPushProcessor) Component.getInstance(NAME);
	}
	
	@Asynchronous
	@Transactional
	public QuartzTriggerHandle excluirCadastro(@IntervalCron String cron) {
		// PJEII-4881  Tratamento de excecao para evitar que a aplicação nao inicie.
		try {
			excluirCadastro();
		} catch (Exception exception) {
			logService.enviarLogPorEmail(log, exception, this.getClass(), "excluirCadastro");
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private Object excluirCadastro() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from CadastroTempPush o ");
		sb.append("where o.confirmado=false ");
		sb.append("and cast(o.dtExpiracao as date) <= cast(current_date as date) ");
		Query q = EntityUtil.createQuery(sb.toString());
		List<CadastroTempPush> cadastroTempPushList = q.getResultList();
		for (CadastroTempPush cadastroTempPush : cadastroTempPushList) {
			EntityUtil.getEntityManager().remove(cadastroTempPush);
		}
		EntityUtil.flush();
		return null;
	}
}
