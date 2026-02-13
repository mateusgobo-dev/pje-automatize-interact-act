package br.jus.cnj.pje.auditoria;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.indexer.IndexVO;
import br.jus.cnj.pje.indexer.IndexerQueueSender;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.pje.indexacao.Indexador;
import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.entidades.log.EntityLogDetail;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name(PjeLogMQ.NAME)
@Scope(ScopeType.EVENT)
public class PjeLogMQ extends PjeLog {
	
	public static final String NAME = "pjeLogMQ";

	@In(value=Parametros.ELASTICSEARCHIDXURL, required=false)
	private String masterURL;
	
	@In(value=Parametros.ELASTICSEARCHIDXNAME, required=false)
	private String indexName;
	
	@Logger
	private Log logger;
	
	@Override
	public void log(Object instance, TipoOperacaoLogEnum tipoOperacao) {
		EntityLog log = LogUtil.createEntityLog(instance);
		log.setTipoOperacao(tipoOperacao);
		
		EntityLogDetail logDetail = new EntityLogDetail();
		logDetail.setEntityLog(log);
		logDetail.setValorAtual(LogUtil.toStringFields(instance));
		logDetail.setValorAnterior("");
	}
	
	protected void log(Class<?> clazz, 
			 Object id,
			 Object[] oldState, 
			 Object[] state, 
			 String[] nomes, 
			 Integer idUsuario, 
			 String ip, 
			 String url, 
			 TipoOperacaoLogEnum tipoOperacao){

		EntityLog log = LogUtil.createEntityLog(clazz, idUsuario, ip, url);
		log.setTipoOperacao(tipoOperacao);
		
		boolean markForIndexing = false;
		boolean canIndex = StringUtil.isSet(masterURL) && StringUtil.isSet(indexName);
		Indexador indexador = null;
		if (canIndex) {
			indexador = ComponentUtil.getComponent(Indexador.class);
			canIndex = indexador.isEnabled();
		}
		for(int i = 0; i < nomes.length; i++){
			try {
				String nome = nomes[i];
				EntityLogDetail detail = createDetails(log, clazz, id, tipoOperacao, nome, oldState != null ? oldState[i] : null, state != null ? state[i] : null);
				if (detail == null) {
					continue;
				}
				if (canIndex && !markForIndexing && indexador.isIndexable(clazz, nome)) {
					markForIndexing = true;
				}
			} catch (Exception e) {
				logger.error("Erro ao realizar o log da entidade.", e);
			}
		}
		if (markForIndexing) {
			ComponentUtil.getComponent(IndexerQueueSender.class).sendMessage(new IndexVO(id, clazz));
		}
	}
}