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

import java.lang.reflect.Field;

import javax.persistence.Query;

import org.jboss.seam.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.extensao.servico.ParametroService;
import br.jus.cnj.pje.nucleo.Parametros;
import br.jus.cnj.pje.nucleo.manager.TipoParteManager;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoParte;
import br.jus.pje.nucleo.enums.ProcessoStatusEnum;

/**
 * Classe responsável por monitorar eventos JPA relevantes para a recuperação de {@link ProcessoTrf}.
 * 
 * Foi criada em razão da necessidade de retirar da entidade a dependência a classes e métodos que 
 * não fazem parte da natureza da entidade, em especial aqueles que dependiam da recuperação do tipo
 * de parte ({@link TipoParte}) advogado. 
 *  
 * A vinculação dessa classe com os eventos do ciclo de vida JPA deve ser feita por
 * meio do arquivo META-INF/orm.xml, que deverá conter a seguinte definição:
 * 
 * <pre>
 * {@code
 * 	<entity class="br.jus.pje.nucleo.entidades.ProcessoTrf">
 * 		<entity-listeners>
 * 			<entity-listener class="br.jus.cnj.pje.entidades.listeners.ProcessoTrfListener">
 * 				<post-load method-name="postLoad"/>
 *				<pre-update method-name="preUpdate"/>
 * 			</entity-listener>
 * 		</entity-listeners>
 * 	</entity>
 * }
 * </pre>
 * @author cristof
 * @since 1.6.0
 */
public class ProcessoTrfListener {
	
	private static final Logger logger = LoggerFactory.getLogger(ProcessoTrfListener.class);
	
	private static TipoParte tipoAdvogado;
	
	private static Field field;
	
	private void init(){
		ParametroService parametroService = (ParametroService) Component.getInstance("parametroService"); 
		TipoParteManager tipoParteManager = (TipoParteManager) Component.getInstance("tipoParteManager");
		try{
			Integer idTipoAdvogado = Integer.parseInt(parametroService.valueOf(Parametros.TIPOPARTEADVOGADO));
			tipoAdvogado = tipoParteManager.findById(idTipoAdvogado);
			field = ProcessoTrf.class.getDeclaredField("tipoParteAdvogado");
		}catch(Exception e){
			logger.error("Erro ao realizar a inicialização do listener de ProcessoTrf: " + e.getLocalizedMessage() + "].");
		}
	}

	public void preUpdate(ProcessoTrf processoTrf) {
		if(processoTrf != null && processoTrf.getProcessoStatus() == ProcessoStatusEnum.E) {
			if(processoDistribuido(processoTrf.getIdProcessoTrf())) {
				throw new RuntimeException("O processo já se encontra distribuído.");
			}
		}
	}
	
	public void postLoad(ProcessoTrf processo){
		if(tipoAdvogado == null){
			init();
		}
		if(tipoAdvogado == null || field == null){
			return;
		}
		try {
			field.setAccessible(true);
			field.set(processo, tipoAdvogado);
			field.setAccessible(false);
		} catch (SecurityException e) {
			logger.error("Erro de segurança ao atribuir o valor do tipo de parte advogado no ProcessoTrf: " + e.getLocalizedMessage() + "].");
		} catch (Exception e) {
			logger.error("Erro ao atribuir o valor do tipo de parte advogado no ProcessoTrf: " + e.getLocalizedMessage() + "].");
		}
	}

	private boolean processoDistribuido(int idProcesso) {
		String qlString = "SELECT p.idProcessoTrf FROM ProcessoTrf p WHERE idProcessoTrf = :idProcessoTrf AND processoStatus = :status";
		Query query = EntityUtil.getEntityManager().createQuery(qlString);

		query.setParameter("idProcessoTrf", idProcesso);
		query.setParameter("status", ProcessoStatusEnum.D);
		query.setMaxResults(1);
		
		return !query.getResultList().isEmpty();
	}
}
