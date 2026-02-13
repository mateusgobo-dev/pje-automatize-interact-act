/*
 * IBPM - Ferramenta de produtividade Java Copyright (c) 1986-2009 Infox Tecnologia da Informação Ltda. Este programa é software livre; você pode
 * redistribuí-lo e/ou modificá-lo sob os termos da GNU GENERAL PUBLIC LICENSE (GPL) conforme publicada pela Free Software Foundation; versão 2 da
 * Licença. Este programa é distribuído na expectativa de que seja útil, porém, SEM NENHUMA GARANTIA; nem mesmo a garantia implícita de
 * COMERCIABILIDADE OU ADEQUAÇÃO A UMA FINALIDADE ESPECÍFICA. Consulte a GNU GPL para mais detalhes. Você deve ter recebido uma cópia da GNU GPL junto
 * com este programa; se não, veja em http://www.gnu.org/licenses/
 */
package br.com.infox.ibpm.entity.log;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.hibernate.AssertionFailure;
import org.hibernate.persister.entity.EntityPersister;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import br.jus.pje.nucleo.entidades.log.EntityLog;
import br.jus.pje.nucleo.entidades.log.EntityLogDetail;
import br.jus.pje.nucleo.enums.TipoOperacaoLogEnum;
import br.jus.pje.nucleo.util.ArrayUtil;

/**
 * 
 * @author Rodrigo Menezes
 * 
 */
public class ExecuteLog implements Serializable{

	private static final long serialVersionUID = 1L;

	private static final LogProvider log = Logging.getLogProvider(ExecuteLog.class);
	private Object[] oldState;
	private Object[] state;
	private EntityPersister persister;
	private Object entidade;
	private TipoOperacaoLogEnum tipoOperacao;
	private EntityManager em;

	public ExecuteLog(){
		em = (EntityManager) Component.getInstance("entityManagerLog");
	}

	public Object[] getOldState(){
		return ArrayUtil.copyOf(oldState);
	}

	public void setOldState(Object[] oldState){
		this.oldState = ArrayUtil.copyOf(oldState);
	}

	public void setState(Object[] state){
		this.state = ArrayUtil.copyOf(state);
	}

	public EntityPersister getPersister(){
		return persister;
	}

	public void setPersister(EntityPersister persister){
		this.persister = persister;
	}

	public Object getEntidade(){
		return entidade;
	}

	public void setEntidade(Object entidade){
		this.entidade = entidade;
	}

	public TipoOperacaoLogEnum getTipoOperacao(){
		return tipoOperacao;
	}

	public void setTipoOperacao(TipoOperacaoLogEnum tipoOperacao){
		this.tipoOperacao = tipoOperacao;
	}

	private void init(){
		if (tipoOperacao.equals(TipoOperacaoLogEnum.I)){
			oldState = new Object[state.length];
		}
		else if (tipoOperacao.equals(TipoOperacaoLogEnum.D)){
			state = new Object[oldState.length];
		}
	}

	public EntityManager getEm(){
		return em;
	}

	public void setEm(EntityManager em){
		this.em = em;
	}

	public void execute(){
		if (!Contexts.isSessionContextActive() || LogUtil.isRequisicaoIntercomunicacaoRest()){
			return;
		}

		init();

		String[] nomes = persister.getClassMetadata().getPropertyNames();
		EntityLog logEnt = LogUtil.createEntityLog(entidade);
		logEnt.setTipoOperacao(tipoOperacao);

		em.persist(logEnt);

		for (int i = 0; i < nomes.length; i++){
			try{
				if (LogUtil.isValidForLog(entidade.getClass(), nomes[i])
						&& !LogUtil.compareObj(oldState[i], state[i])){
					EntityLogDetail detail = new EntityLogDetail();
					detail.setEntityLog(logEnt);
					detail.setNomeAtributo(nomes[i]);
					detail.setValorAtual(LogUtil.toStringForLog(state[i]));
					detail.setValorAnterior(LogUtil.toStringForLog(oldState[i]));
					em.persist(detail);
					logEnt.getLogDetalheList().add(detail);
				}
			} catch (Exception e){
				log.error("Erro ao logar", e);
				e.printStackTrace();
			}
		}
		try{
			em.flush();
		} catch (AssertionFailure e){ /* Bug do hibernate: HHH-2763 */
		}
	}

}