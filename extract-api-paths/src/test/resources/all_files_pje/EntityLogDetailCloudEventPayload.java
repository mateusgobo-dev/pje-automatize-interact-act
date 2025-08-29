package br.jus.cnj.pje.amqp.model.dto;

import br.jus.cnj.pje.pjecommons.model.amqp.CloudEventPayload;
import br.jus.cnj.pje.pjecommons.model.auditoria.TipoOperacaoLogEnum;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.entidades.log.EntityLogDetail;

public class EntityLogDetailCloudEventPayload extends EntityLogDetail implements CloudEventPayload<br.jus.cnj.pje.pjecommons.model.auditoria.EntityLogDetail, EntityLogDetail>{

	private static final long serialVersionUID = 1L;

	@Override
	public br.jus.cnj.pje.pjecommons.model.auditoria.EntityLogDetail convertEntityToPayload(EntityLogDetail entityLogDetail) {
		br.jus.cnj.pje.pjecommons.model.auditoria.EntityLogDetail payload = new br.jus.cnj.pje.pjecommons.model.auditoria.EntityLogDetail();
		
		payload.setEntityLog(this.convertEntityLog(entityLogDetail.getEntityLog()));
		payload.setNomeAtributo(entityLogDetail.getNomeAtributo());
		payload.setValorAnterior(entityLogDetail.getValorAnterior());
		payload.setValorAtual(entityLogDetail.getValorAtual());
		
		return payload;
	}
	
	private br.jus.cnj.pje.pjecommons.model.auditoria.EntityLog convertEntityLog(EntityLog entityLog) {
		br.jus.cnj.pje.pjecommons.model.auditoria.EntityLog payload = new br.jus.cnj.pje.pjecommons.model.auditoria.EntityLog();
		payload.setDataLog(entityLog.getDataLog());
		payload.setIdEntidade(entityLog.getIdEntidade());
		payload.setIdUsuario(entityLog.getIdUsuario());
		payload.setIp(entityLog.getIp());
		payload.setNomeEntidade(entityLog.getNomeEntidade());
		payload.setNomePackage(entityLog.getNomePackage());
		payload.setTipoOperacao(TipoOperacaoLogEnum.valueOf(entityLog.getTipoOperacao().name()));
		payload.setUrlRequisicao(entityLog.getUrlRequisicao());
		return payload;
	}

	@Override
	public Long getId(EntityLogDetail entity) {
		return (entity != null ? entity.getIdLogDetalhe() : null);
	}

}
