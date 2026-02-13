/**
 * pje-web
 * Copyright (C) 2013 Conselho Nacional de Justiça
 *
 * A propriedade intelectual deste programa, como código-fonte
 * e como sua derivação compilada, pertence à União Federal,
 * dependendo o uso parcial ou total de autorização expressa do
 * Conselho Nacional de Justiça.
 *
 **/
package br.jus.cnj.pje.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.jboss.seam.security.Identity;

import br.com.infox.ibpm.home.Authenticator;
import br.com.itx.util.EntityUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.manager.ProcessoDocumentoLidoManager;
import br.jus.cnj.pje.nucleo.manager.ProcessoParteExpedienteManager;
import br.jus.cnj.pje.nucleo.service.AtoComunicacaoService;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.RespostaExpediente;
import br.jus.pje.nucleo.util.StringUtil;
import br.jus.pje.search.Criteria;
import br.jus.pje.search.Search;

/**
 * Componente de controle de eventuais resposta a expedientes.
 * 
 * @author cristof
 *
 */
public class RespostaExpedienteBean {
	
	private ProcessoTrf processoJudicial;
	
	private List<Pessoa> destinatarios = new ArrayList<Pessoa>();
	
	private Identity identity;
	
	private AtoComunicacaoService atoComunicacaoService;
	
	private ProcessoDocumentoLidoManager processoDocumentoLidoManager;
	
	private ProcessoParteExpedienteManager processoParteExpedienteManager;
	
	private List<ProcessoParteExpediente> expedientes = new ArrayList<ProcessoParteExpediente>();
	
	private Map<ProcessoParteExpediente, Boolean> selecionados;
	
	/**
	 * Construtor padrão deste componente.
	 * 
	 * @param processoJudicial o processo judicial em relação ao qual haverá registro de eventuais ciências
	 * @param destinatario o potencial destinatário ou representante
	 * @param identity o conjunto de dados de identificação do usuário atual
	 */
	public RespostaExpedienteBean(ProcessoTrf processoJudicial, Pessoa destinatario, Identity identity){
		this.processoJudicial = processoJudicial;
		this.destinatarios.add(destinatario);
		this.identity = identity;
		init();
	}
	
	/**
	 * Construtor padrão deste componente.
	 * 
	 * @param processoJudicial o processo judicial em relação ao qual haverá registro de eventuais ciências
	 * @param destinatarios os potenciais destinatários ou representantes
	 * @param identity o conjunto de dados de identificação do usuário atual
	 */
	public RespostaExpedienteBean(ProcessoTrf processoJudicial, List<Pessoa> destinatarios, Identity identity){
		this.processoJudicial = processoJudicial;
		this.destinatarios = destinatarios;
		this.identity = identity;
		init();
	}
	
	/**
	 * Inicializa os serviços necessários para o tratamento dos expedientes.
	 * 
	 */
	private void init(){
		atoComunicacaoService = (AtoComunicacaoService) Component.getInstance("atoComunicacaoService");
		processoParteExpedienteManager = (ProcessoParteExpedienteManager) Component.getInstance("processoParteExpedienteManager");
		processoDocumentoLidoManager = (ProcessoDocumentoLidoManager) Component.getInstance("processoDocumentoLidoManager");
		selecionados = new HashMap<ProcessoParteExpediente, Boolean>(0);
		carregarExpedientes();
	}
	
	/**
	 * Recupera os expedientes pendentes de manifestação como definido em
	 * {@link ProcessoParteExpedienteManager#listPendentesManifestacao(Search, br.jus.pje.nucleo.entidades.Usuario, List)}.
	 * 
	 */
	private void carregarExpedientes(){
		try {
			expedientes.clear();
			for (Pessoa destinatario: destinatarios) {
				Search s = new Search(ProcessoParteExpediente.class);
				s.setDistinct(true);
				s.addCriteria(Criteria.equals("processoJudicial.idProcessoTrf", processoJudicial.getIdProcessoTrf()));
				//s.addCriteria(Criteria.or(Criteria.equals("intimacaoPessoal", false), Criteria.equals("pessoaParte", Authenticator.getPessoaLogada())));
				expedientes.addAll(processoParteExpedienteManager.listPendentesManifestacao(s, destinatario));
			}
			
			EntityManager em = EntityUtil.getEntityManager();
			for (ProcessoParteExpediente expediente : expedientes) {
				em.refresh(expediente);
			}
		} catch (PJeBusinessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Recupera o mapa de expedientes passíveis de seleção.
	 * 
	 * @return o mapa de expedientes
	 */
	public Map<ProcessoParteExpediente, Boolean> getSelecionados() {
		return selecionados;
	}
	
	/**
	 * Inverte a indicação relativa a um dado expediente estar ou não
	 * selecionado como respondido.
	 * 
	 * @param atoComunicacao o ato a ser selecionado
	 */
	public void inverterSelecao(ProcessoParteExpediente atoComunicacao){
		if(selecionados.get(atoComunicacao) == null){
			selecionados.put(atoComunicacao, true);
		}else{
			selecionados.put(atoComunicacao, !selecionados.get(atoComunicacao));
		}
	}
	
	/**
	 * Recupera a lista de expedientes selecionados para resposta.
	 * 
	 * @return a lista de expedientes selecionados para resposta
	 */
	private ProcessoParteExpediente[] getRespondidos(){
		Set<ProcessoParteExpediente> ret = new HashSet<ProcessoParteExpediente>();
		for(java.util.Map.Entry<ProcessoParteExpediente, Boolean> e: selecionados.entrySet()){
			if(e.getValue()){
				ret.add(e.getKey());
			}
		}
		return ret.toArray(new ProcessoParteExpediente[ret.size()]);
	}
	
	/**
	 * Registra a resposta para os expedientes selecionados, indicando como ato de resposta
	 * o documento informado.
	 * 
	 * @param doc o documento que serve como resposta.
	 * @throws PJeBusinessException
	 */
	public void registrarResposta(ProcessoDocumento doc) throws PJeBusinessException {
		ProcessoParteExpediente[] respondidos = getRespondidos();
		if (respondidos.length == 0) {
			return;
		}
		RespostaExpediente resp = new RespostaExpediente();
		resp.setProcessoDocumento(doc);
		atoComunicacaoService.registraResposta(resp, respondidos);
		if (identity.hasRole(Papeis.PJE_ADVOGADO) || identity.hasRole(Papeis.PJE_REPRESENTANTE_PROCESSUAL)) {
			definirDocumentoComoLido(respondidos, Authenticator.getPessoaLogada());
		} else if (Identity.instance().hasRole(Papeis.SERVIDOR)) {
			OrgaoJulgador orgaoJulgador = processoJudicial.getOrgaoJulgador();
			OrgaoJulgadorColegiado orgaoJulgadorColegiado = processoJudicial.getOrgaoJulgadorColegiado();
			if ((orgaoJulgador != null && orgaoJulgador.equals(Authenticator.getOrgaoJulgadorAtual()))
					|| (orgaoJulgadorColegiado != null
							&& orgaoJulgadorColegiado.equals(Authenticator.getOrgaoJulgadorColegiadoAtual()))) {
				definirDocumentoComoLido(respondidos, Authenticator.getPessoaLogada());
			}
		}
		init();
	}

	private void definirDocumentoComoLido(ProcessoParteExpediente[] respondidos, Pessoa pessoa)
			throws PJeBusinessException {
		for (ProcessoParteExpediente respondido : respondidos) {
			if (respondido.getProcessoDocumento() != null) {
				processoDocumentoLidoManager.definirDocumentoComoLido(respondido.getProcessoDocumento(), pessoa);
				processoDocumentoLidoManager.flush();
				
			}
		}
	}

	/**
	 * Recupera a lista de expedientes selecionáveis.
	 * 
	 * @return a lista de expedientes
	 * @see #carregarExpedientes()
	 */
	public List<ProcessoParteExpediente> getExpedientes() {
		return expedientes;
	}
	
	/**
	 * Recupera o documento do expediente informado por parâmetro.
	 * 
	 * @param ppe ProcessoParteExpediente
	 * @return
	 */
    public String getDocumento(ProcessoParteExpediente ppe){
        if(ppe.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento() != null){
            return StringUtil.cleanData(ppe.getProcessoDocumento().getProcessoDocumentoBin().getModeloDocumento());
        }
        return StringUtils.EMPTY;
    }

}
