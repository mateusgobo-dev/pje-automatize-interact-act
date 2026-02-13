package br.com.infox.cliente.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Events;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.com.infox.pje.manager.PessoaProcuradoriaEntidadeManager;
import br.com.itx.component.Util;
import br.com.itx.component.grid.GridQuery;
import br.com.itx.util.ComponentUtil;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.manager.PessoaManager;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.pje.nucleo.Eventos;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaAutoridade;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.PessoaProcuradoriaEntidade;
import br.jus.pje.nucleo.entidades.Procuradoria;

@Name("pessoaProcuradoriaEntidadeHome")
@BypassInterceptors
public class PessoaProcuradoriaEntidadeHome extends AbstractPessoaProcuradoriaEntidadeHome<PessoaProcuradoriaEntidade> {
	
	private static final String ENTIDADE_AUTORIDADE_VIEW = "include/dadosEntidadeAutoridadeView.xml";
	private static final String ENTIDADE_JURIDICA_VIEW = "include/dadosEntidadeJuridicaView.xml";
	private static final String ENTIDADE_FISICA_VIEW = "include/dadosEntidadeFisicaView.xml";
	public static final String ID_RECURSO = "/pages/ConsultaPessoa/listView.seam";
	
	private static final long serialVersionUID = 1L;
	private List<Object> listaObj = new ArrayList<Object>(0);
	private Boolean checkBox = Boolean.FALSE;
	
	@Logger
	private Log logger;

	@Override
	public void newInstance() {
		super.newInstance();
	}

	public static PessoaProcuradoriaEntidadeHome instance() {
		return ComponentUtil.getComponent("pessoaProcuradoriaEntidadeHome");
	}

	@Override
	public String persist() {
		getInstance().setProcuradoria(ProcuradoriaHome.instance().getInstance());
		String persist = super.persist();
		refreshGrid("entidadeProcuradoriaGrid");
		newInstance();
		return persist;
	}

	@Override
	public String remove(PessoaProcuradoriaEntidade ppe) {
		setInstance(ppe);
		remove();
		return "";
	}

	@Override
	public String remove() {
		Pessoa pessoa = getInstance().getPessoa();
		Procuradoria procuradoria = getInstance().getProcuradoria();
		removerVinculacaoProcessos(pessoa,procuradoria);
		String remove = super.remove();
		this.lancarEventoAlteracaoProcuradoriaPessoa("REMOVE", pessoa, procuradoria);
		refreshGrid("pessoaProcuradoriaGrid");
		refreshGrid("pessoaProcuradoriaEntidadeGrid");
		listaObj.clear();
		newInstance();
		FacesMessages.instance().clear();
		FacesMessages.instance().add(Severity.INFO, "Registro excluído com sucesso.");
		
		
		return remove;
		
	}

	public void inserir() {
		PessoaService pessoaService = getComponent(PessoaService.NAME);
		PessoaProcuradoriaEntidadeManager pessoaProcuradoriaEntidadeManager = getComponent(PessoaProcuradoriaEntidadeManager.NAME);
		
		GridQuery gridQuery = getComponent("pessoaProcuradoriaGrid");
		listaObj = gridQuery.getSelectedRowsList();
		if (!listaObj.isEmpty()) {
			for (Object obj : listaObj) {
				Pessoa p = (Pessoa) obj;
				Procuradoria proc = ProcuradoriaHome.instance().getInstance();
				PessoaProcuradoriaEntidade ppe = new PessoaProcuradoriaEntidade();
				if(pessoaService.isFiscalDaLei(p)){
					List<PessoaProcuradoriaEntidade> pessoaProcuradoriaEntidadeList = pessoaProcuradoriaEntidadeManager.
							getListaPessoaProcuradoriaEntidade(p);
					if(pessoaProcuradoriaEntidadeList != null && !pessoaProcuradoriaEntidadeList.isEmpty()){
						Procuradoria procuradoria = pessoaProcuradoriaEntidadeList.get(0).getProcuradoria();
						FacesMessages.instance().add(Severity.ERROR, 
								String.format("Entidade selecionada já possui vinculo com %s.", procuradoria.getNome()));
						return;
					}
				}
				ppe.setPessoa(p);
				ppe.setIdPessoa(p.getIdPessoa());
				ppe.setProcuradoria(proc);
				getEntityManager().persist(ppe);
				atualizarProcessos(p,proc);
				atualizarExpedientes(p,proc);
				getEntityManager().flush();
				
				this.lancarEventoAlteracaoProcuradoriaPessoa("ADICIONA", p, proc);
			}
			refreshGrid("pessoaProcuradoriaGrid");
			refreshGrid("pessoaProcuradoriaEntidadeGrid");
			listaObj.clear();
			setCheckBox(Boolean.FALSE);
			FacesMessages.instance().add(Severity.INFO, "Registro inserido com sucesso.");
		} else {
			FacesMessages.instance().add(Severity.ERROR, "Selecione pelo menos uma entidade!");
		}
	}

	/**
	 * Lanca evento de alteração do relacionamento de pessoa com procuradoria, por padrao com os identificadores: pessoa / procuradoria / procuradoriaPadrao / pessoa
	 * 
	 * @param tipoAlteracao ('ADICIONA', 'REMOVE')
	 * @param pessoa
	 * @param procuradoria
	 */
	private void lancarEventoAlteracaoProcuradoriaPessoa(String tipoAlteracao, Pessoa pessoa, Procuradoria procuradoria){
		// TODO - aplicar esta lõgica na alteração do relacionamento da procuradoria e pessoa para processos / expedientes / sigilo de documentos
		Map<String, Object> payloadEvento = new HashMap<String, Object>();
		payloadEvento.put("tipoAlteracao", tipoAlteracao);
		int idPessoa = 0;
		if(pessoa.getIdPessoa() != null){
			idPessoa = pessoa.getIdPessoa();
		}
		payloadEvento.put("idPessoa", idPessoa);
		int idProcuradoria = 0;
		if(procuradoria != null && procuradoria.getIdProcuradoria() != 0){
			idProcuradoria = procuradoria.getIdProcuradoria();
		}
		payloadEvento.put("idProcuradoria", idProcuradoria);
		PessoaProcuradoriaEntidadeManager pessoaProcuradoriaEntidadeManager = getComponent(PessoaProcuradoriaEntidadeManager.NAME);
		int idProcuradoriaPadrao = 0;
		if(pessoaProcuradoriaEntidadeManager.getProcuradoriaPadraoPessoa(pessoa) != null){
			idProcuradoriaPadrao = pessoaProcuradoriaEntidadeManager.getProcuradoriaPadraoPessoa(pessoa).getIdProcuradoria();
		}
		payloadEvento.put("idProcuradoriaPadrao", idProcuradoriaPadrao);
		Events.instance().raiseEvent(Eventos.ALTERACAO_PROCURADORIA_PESSOA, payloadEvento);
		
		logger.debug(Severity.INFO, "EVENTO - Alteracao no relacionamento de procuradoria e entidade.");
	}
	
	private void atualizarProcessos(Pessoa p,Procuradoria proc){
		String hql = "update ProcessoParte set procuradoria = :procuradoria where pessoa = :pessoa and procuradoria is null";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("procuradoria", proc);
		q.setParameter("pessoa", p);
		q.executeUpdate();
		
	}
	
	private void atualizarExpedientes(Pessoa p,Procuradoria proc){
		String hql = "update ProcessoParteExpediente set procuradoria = :procuradoria where pessoaParte = :pessoa and procuradoria is null";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("procuradoria", proc);
		q.setParameter("pessoa", p);
		q.executeUpdate();
	}
	
	private void removerVinculacaoProcessos(Pessoa p,Procuradoria proc){
		String hql = "update ProcessoParte set procuradoria = null where pessoa = :pessoa and procuradoria = :procuradoria";
		Query q = getEntityManager().createQuery(hql);
		q.setParameter("procuradoria", proc);
		q.setParameter("pessoa", p);
		q.executeUpdate();
		
	}

	public void setCheckBox(Boolean checkBox) {
		this.checkBox = checkBox;
	}

	public Boolean getCheckBox() {
		return checkBox;
	}

	public void checkAll(String grid) {
		GridQuery gridQuery = getComponent("pessoaProcuradoriaGrid");
		List<Object> lista = gridQuery.getSelectedRowsList();
		List<Object> resultList = gridQuery.getResultList();

		lista.clear();
		if (getCheckBox()) {
			lista.addAll(resultList);
		}
	}

	public String getIdRecurso() {
		return ID_RECURSO;
	}
	
	/**
	 * 
	 * Método responsável por designar qual tela será mostrada nos detalhes das
	 * Entidades.
	 * 
	 * @param id
	 *            da {@link Pessoa} a ser consultada os dados.
	 * @return <code>String</code>, caminho da view a ser renderizada.
	 * 
	 * @see <a href="http://www.cnj.jus.br/jira/browse/PJEII-18468">PJEII-18468</a>
	 */
	public String consultarDadosEntidadeView(Integer id) {
		StringBuilder sb = new StringBuilder();
		sb.append(Util.instance().getViewIdDirectory());
		try {
			Pessoa pessoa = ComponentUtil.<PessoaManager>getComponent("pessoaManager").findById(id);
			if (pessoa instanceof PessoaFisica) {
				sb.append(ENTIDADE_FISICA_VIEW);
				return sb.toString();
			} else if (pessoa instanceof PessoaJuridica) {
				sb.append(ENTIDADE_JURIDICA_VIEW);
				return sb.toString();
			} else if (pessoa instanceof PessoaAutoridade) {
				sb.append(ENTIDADE_AUTORIDADE_VIEW);
				return sb.toString();
			}
		} catch (PJeBusinessException e) {
			logger.error(Severity.FATAL, "Erro ao consultar entidade: {0}.",
					e.getLocalizedMessage());
			e.printStackTrace();
		}
		return null;
	}

}
