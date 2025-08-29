/**
 *  pje-web
 *  Copyright (C) 2013 Conselho Nacional de Justiça
 *
 *  A propriedade intelectual deste programa, tanto quanto a seu código-fonte
 *  quanto a derivação compilada é propriedade da União Federal, dependendo
 *  o uso parcial ou total de autorização expressa do Conselho Nacional de Justiça.
 * 
 */
package br.jus.cnj.pje.entidades.listeners;

import javax.persistence.Query;

import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;

/**
 * Classe responsável por monitorar eventos JPA relevantes para a recuperação de {@link SessaoPautaProcessoTrf}.
 * 
 * A vinculação dessa classe com os eventos do ciclo de vida JPA deve ser feita por
 * meio do arquivo META-INF/orm.xml, que deverá conter a seguinte definição:
 * 
 * <pre>
 * {@code
 * 	<entity class="br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf">
 * 		<entity-listeners>
 * 			<entity-listener class="br.jus.cnj.pje.entidades.listeners.SessaoPautaProcessoTrfListener">
 * 				<post-load method-name="postLoad"/>
 *				<pre-update method-name="preUpdate"/>
 * 			</entity-listener>
 * 		</entity-listeners>
 * 	</entity>
 * }
 * </pre>
 * @author thiago.vieira
 * @since 1.6.0
 */
public class SessaoPautaProcessoTrfListener {
	
	
	public void postLoad(SessaoPautaProcessoTrf processoPauta){
	}
	
	public void postInsert(SessaoPautaProcessoTrf processoPauta){
		String q = "update tb_sessao_pauta_proc_trf set nr_ordem = " +
					 "(select case when max(nr_ordem) is null then 1 else max(nr_ordem) + 1 end from tb_sessao_pauta_proc_trf  where id_sessao = :sessao and dt_exclusao_processo is null) "+
				     " where id_sessao_pauta_processo_trf = :id";
		Query query = EntityUtil.createNativeQuery(q, "tb_sessao_pauta_proc_trf");
		query.setParameter("sessao", processoPauta.getSessao().getIdSessao());
		query.setParameter("id", processoPauta.getIdSessaoPautaProcessoTrf());
		query.executeUpdate();
				
	}
	
	public void postUpdate(SessaoPautaProcessoTrf processoPauta){
	}
}
