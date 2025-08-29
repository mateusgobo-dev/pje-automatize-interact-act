package br.com.infox.editor.service;

import java.util.Arrays;
import java.util.Date;

import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

import br.com.infox.ibpm.entity.log.LogUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.com.itx.util.ReflectionsUtil;
import br.jus.pje.nucleo.entidades.editor.Anotacao;
import br.jus.pje.nucleo.entidades.editor.HistoricoAnotacao;
import br.jus.pje.nucleo.entidades.editor.HistoricoProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.entidades.editor.ProcessoDocumentoEstruturadoTopico;
import br.jus.pje.nucleo.enums.editor.StatusAnotacao;
import br.jus.pje.nucleo.enums.editor.TipoOperacaoTopicoEnum;

public class EditorHibernateListner implements PreUpdateEventListener, PostUpdateEventListener {

	private static final long serialVersionUID = 1L;
	private static String[] fieldsAuditados = {"conteudo", "titulo", "sha1Titulo", "sha1Conteudo", "ativo"};
	private static String[] fieldsAuditadosAnotacao = {"conteudo", "destaque", "tipoAnotacao", "statusAnotacao", "statusAcolhidoAnotacao", 
		"statusCienciaAnotacao", "nivelVisibilidadeAnotacao"};
	
	static {
		Arrays.sort(fieldsAuditados);
		Arrays.sort(fieldsAuditadosAnotacao);
	}

	@Override
	public void onPostUpdate(PostUpdateEvent evt) {
		if (isEntity(evt.getEntity(), ProcessoDocumentoEstruturadoTopico.class)) {
			if (!houveMudanca(HistoricoProcessoDocumentoEstruturadoTopico.class, evt.getPersister().getPropertyNames(), evt.getOldState(), evt.getState())) {
				return;
			}
			HistoricoProcessoDocumentoEstruturadoTopico hist = new HistoricoProcessoDocumentoEstruturadoTopico();
			copiarCampos(hist, evt.getPersister().getPropertyNames(), evt.getOldState());
			ProcessoDocumentoEstruturadoTopico processoDocumentoEstruturadoTopico = (ProcessoDocumentoEstruturadoTopico) evt.getEntity();
			hist.setProcessoDocumentoEstruturadoTopico(processoDocumentoEstruturadoTopico);
			hist.setPessoa(Authenticator.getPessoaLogada());
			hist.setDataModificacao(processoDocumentoEstruturadoTopico.getDataModificacao());
			if (processoDocumentoEstruturadoTopico.isAtivo()) {
				hist.setTipoOperacaoTopico(TipoOperacaoTopicoEnum.A);
			} else {
				hist.setTipoOperacaoTopico(TipoOperacaoTopicoEnum.E);
			}
			EntityUtil.getEntityManager().persist(hist);
		} else if (isEntity(evt.getEntity(), Anotacao.class)) {
			if (!houveMudanca(HistoricoAnotacao.class, evt.getPersister().getPropertyNames(), evt.getOldState(), evt.getState())) {
				return;
			}

			HistoricoAnotacao hist = new HistoricoAnotacao();
			copiarCampos(hist, evt.getPersister().getPropertyNames(), evt.getOldState());
			
			Anotacao anotacao = (Anotacao) evt.getEntity();
			hist.setAnotacao(anotacao);
			hist.setUsuario(Authenticator.getUsuarioLogado());
			hist.setDataAlteracao(anotacao.getDataAlteracao());
			
			if (anotacao.getStatusAnotacao() != StatusAnotacao.E) {
				hist.setTipoOperacaoTopico(TipoOperacaoTopicoEnum.A);
			} else {
				hist.setTipoOperacaoTopico(TipoOperacaoTopicoEnum.E);
			}
			EntityUtil.getEntityManager().persist(hist);
		}
	}

	@Override
	public boolean onPreUpdate(PreUpdateEvent evt) {
		if (isEntity(evt.getEntity(), ProcessoDocumentoEstruturadoTopico.class)) {
			ProcessoDocumentoEstruturadoTopico topico = (ProcessoDocumentoEstruturadoTopico) evt.getEntity();
			topico.setDataModificacao(new Date());
			topico.setPessoa(Authenticator.getPessoaLogada());
		}
		return false;
	}
	
	private boolean isEntity(Object object, Class<?> klass) {
		Class<?> entityClass = EntityUtil.getEntityClass(object);
		//testando assim por causa do proxy do hibernate. Verificar melhor maneira depois
		return klass != null && klass.equals(entityClass);
	}
	
	private void copiarCampos(Object o, String[] nomes, Object[] oldState) {
		for (int i = 0; i < nomes.length; i++) {
			try {
				if (isCampoAuditavel(nomes[i], o.getClass())) {
					ReflectionsUtil.setValue(o, nomes[i], oldState[i]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}		
	}
	
	private boolean houveMudanca(Class<?> klass, String[] campos, Object[] oldState, Object[] newState) {
		for (int i = 0; i < oldState.length; i++) {
			Object oldValue = oldState[i];
			Object newValue = newState[i];
			String nomeCampo = campos[i];
			if (isCampoAuditavel(nomeCampo, klass) && !LogUtil.compareObj(oldValue, newValue)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isCampoAuditavel(String nomeCampo, Class<?> klass) {
		if (klass == HistoricoProcessoDocumentoEstruturadoTopico.class) {
			return Arrays.binarySearch(fieldsAuditados, nomeCampo) >= 0;
		} else if (klass == HistoricoAnotacao.class) {
			return Arrays.binarySearch(fieldsAuditadosAnotacao, nomeCampo) >= 0;
		} else {
			return false;
		}
	}

	@Override
	public boolean requiresPostCommitHanding(EntityPersister persister) {
		// TODO Auto-generated method stub
		return false;
	}
}
