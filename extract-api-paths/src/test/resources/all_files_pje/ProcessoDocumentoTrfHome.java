package br.com.infox.cliente.home;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;

import br.com.infox.cliente.util.ParametroUtil;
import br.com.infox.ibpm.home.Authenticator;
import br.com.infox.pje.list.ProcessoDocumentoNaoLidoList;
import br.com.itx.util.ComponentUtil;
import br.com.itx.util.EntityUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoLido;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrf;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Scope(ScopeType.CONVERSATION)
@Name(ProcessoDocumentoTrfHome.NAME)
@Install(precedence = Install.APPLICATION)
@BypassInterceptors
public class ProcessoDocumentoTrfHome implements Serializable {
	public static final String NAME = "processoDocumentoTrfHome";
	private static final long serialVersionUID = 1L;

	private ProcessoDocumentoTrf instance = new ProcessoDocumentoTrf();
	private ProcessoTrf processoTrf;

	public ProcessoDocumentoTrf getInstance() {
		return instance;
	}

	public void setInstance(ProcessoDocumentoTrf instance) {
		this.instance = instance;
	}

	public boolean exibeMovimentar(ProcessoDocumentoTrf procDocTrf) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(sp) from SituacaoProcesso sp ");
		sb.append("where sp.processoTrf.idProcessoTrf = :idProcTrf ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("idProcTrf", procDocTrf.getProcessoTrf().getIdProcessoTrf());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * Verifica se exite um processoDocumentoLido com os mesmos pessoa e
	 * processoDocumento de um processoDocumentoLido.
	 * 
	 * @param pdl
	 *            ProcessoDocumentoLido esperado.
	 * @return Returna true caso seja encontrado um ProcessoDocumentoLido.
	 */
	private boolean exiteProcessoDocumentoLido(ProcessoDocumentoLido pdl) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(pdl) from ProcessoDocumentoLido pdl ");
		sb.append("where pdl.processoDocumento = :processoDocumento ");
		sb.append("and pdl.pessoa = :pessoa ");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoDocumento", pdl.getProcessoDocumento());
		q.setParameter("pessoa", pdl.getPessoa());
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	/**
	 * Insere a lista dos documentos que foram marcados na toogle Documentos não
	 * lidos na tabebla de documentos lidos
	 */
	public void inserirListaLidos() {
	    ProcessoDocumentoNaoLidoList processoDocumentoList = ComponentUtil.getComponent("processoDocumentoNaoLidoList");
	    List<ProcessoDocumento> listaProcessoDocumentoTrfLidos = ((ProcessoDocumentoNaoLidoList) processoDocumentoList).getSelectedItens();
	    Pessoa pessoa = getPessoaLogada();
	    Date dataLeitura = new Date();
	    for (ProcessoDocumento procDocTemp : listaProcessoDocumentoTrfLidos) {
	        gravarAlteracoesLeituraDocumento(procDocTemp, pessoa, dataLeitura);
	    }
	    EntityUtil.flush();
	    EntityUtil.getEntityManager().clear();
	    FacesMessages.instance().add("Documentos removidos.");
	    processoDocumentoList.refreshResultadoTotal();
	}
	 
	/**
	 * Atualiza um dado ProcessoDocumento como lido pela pessoa logada na data atual.
	 * 
	 * @param pd ProcessoDocumento que será atualizado como lido
	 */
	public void inserirDocumentoLido(ProcessoDocumento pd){
	    Pessoa pessoa = getPessoaLogada();
	    gravarAlteracoesLeituraDocumento(pd, pessoa, new Date());
	    EntityUtil.flush();
	    EntityUtil.getEntityManager().clear();      
	}
	 
	/**
	 * Atualiza um dado ProcessoDocumento como lido pela pessoa logada na data indicada.
	 * 
	 * @param processoDocumento Atualiza um dado ProcessoDocumento como lido pela pessoa logada na data atual.
	 * @param pessoaLogada objeto Pessoa que representa o usuário logado 
	 * @param dataLeitura a data em que o documento é marcado como lido
	 */
	public void gravarAlteracoesLeituraDocumento(ProcessoDocumento processoDocumento, Pessoa pessoaLogada, Date dataLeitura){	    
	    ProcessoDocumentoLido pdl = new ProcessoDocumentoLido();
	    processoDocumento.setLido(true);
	    pdl.setDataApreciacao(dataLeitura);
	    pdl.setPessoa(pessoaLogada);
	    pdl.setProcessoDocumento(processoDocumento);
	    if (!exiteProcessoDocumentoLido(pdl)) {
	        EntityUtil.getEntityManager().merge(pdl);
	        EntityUtil.getEntityManager().merge(processoDocumento);            
	    }               
	}

    private Pessoa getPessoaLogada() {
        Pessoa pessoa = EntityUtil.find(Pessoa.class, Authenticator.getUsuarioLogado().getIdUsuario());
        return pessoa;
    }

	public void setIdProcessoTrf(Integer idProcessoTrf) {
		this.processoTrf = getEntityManager().find(ProcessoTrf.class, idProcessoTrf);
	}

	public ProcessoTrf getProcessoTrf() {
		return processoTrf;
	}

	public void setProcessoTrf(ProcessoTrf processoTrf) {
		this.processoTrf = processoTrf;
	}

	public EntityManager getEntityManager() {
		return EntityUtil.getEntityManager();
	}

	public boolean isEditable() {
		return true;
	}

	public String getHomeName() {
		return NAME;
	}

	public Class<ProcessoDocumentoTrf> getEntityClass() {
		return ProcessoDocumentoTrf.class;
	}

	public static ProcessoDocumentoTrfHome instance() {
		return (ProcessoDocumentoTrfHome) Contexts.getConversationContext().get(NAME);
	}

	/**
	 * Método utilizado condicionar a exibição do ícone "Prazos Vencidos" na
	 * grid de processos com documentos não lidos
	 * 
	 * @param processoTrf
	 * @return
	 */
	public boolean exibePrazosVencidos(ProcessoTrf processoTrf) {
		if (Authenticator.getPapelAtual().equals(ParametroUtil.instance().getPapelDiretorSecretaria())
				|| Authenticator.getPapelAtual().equals(ParametroUtil.instance().getPapelAdministradorConhecimento())
				|| Authenticator.getPapelAtual().equals(ParametroUtil.instance().getPapelServidorConhecimento())) {
			if (ProcessoExpedienteHome.instance().verificaExpedientesVencidos(processoTrf)) {
				return true;
			}
		}
		return false;
	}

}