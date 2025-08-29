package br.com.infox.cliente.home;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;

import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoSegredo;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.enums.SigiloStatusEnum;

@Name("processoDocumentoSegredoHome")
@BypassInterceptors
public class ProcessoDocumentoSegredoHome extends AbstractProcessoDocumentoSegredoHome<ProcessoDocumentoSegredo> {

	private static final long serialVersionUID = 1L;
	private Boolean segredoJustica;
	private Boolean documentoSigiloso = Boolean.FALSE;
	private String nomePessoa;
	private ProcessoDocumento processoDocumento;
	private Boolean exibeModal = Boolean.FALSE;

	// @Override
	// public void newInstance() {
	// super.newInstance();
	// }

	public void setarProcessoDocumentoSegredo(ProcessoDocumento obj) {
		processoDocumento = obj;
		setDocumentoSigiloso(obj.getDocumentoSigiloso());
		obj.setDocumentoSigiloso(!getDocumentoSigiloso());
	}

	// public void setarProcessoSegredo(ProcessoTrf processo){
	// ProcessoTrfHome ptrf = ComponentUtil.getComponent("processoTrfHome");
	// ptrf.updatePermissaoSegredoJustica();
	// }

	public void inserirMotivo() {
		processoDocumento.setDocumentoSigiloso(getDocumentoSigiloso());
		getEntityManager().merge(processoDocumento);
		getEntityManager().flush();

		getInstance().setProcessoDocumento(processoDocumento);
		if (documentoSigiloso) {
			instance.setStatus(SigiloStatusEnum.C);
		} else {
			instance.setStatus(SigiloStatusEnum.R);
		}
		persist();
	}
	
	public void inserirMotivos() {
		//Obtém a descrição do texto do motivo
		String motivo = getInstance().getMotivo();
		List<ProcessoDocumento> listaSigilo = ProcessoDocumentoHome.instance().getListaSigilo();
		for (ProcessoDocumento processoDocumento : listaSigilo){
			processoDocumento.setDocumentoSigiloso(!processoDocumento.getDocumentoSigiloso());
			getEntityManager().merge(processoDocumento);
			getEntityManager().flush();
			
			getInstance().setProcessoDocumento(processoDocumento);
			//Seta o mesmo texto do motivo do primeiro documento para os demais documentos
			getInstance().setMotivo(motivo);
			
			if (processoDocumento.getDocumentoSigiloso()) {
				getInstance().setStatus(SigiloStatusEnum.C);
			} else {
				getInstance().setStatus(SigiloStatusEnum.R);
			}
			persist();
		}
		exibeModal = false;
		ProcessoDocumentoHome.instance().setCheckAllSigilo(Boolean.FALSE);
		ProcessoDocumentoHome.instance().checkAll("processoTrfDocumentoMagistradoGrid", ProcessoDocumentoHome.instance().getListaSigilo(), false);
		
	}
	
	/*
	 * [PJEII-2168] PJE-JT: Ronny Paterson : PJE-1.4.4
	 * Criação de método que verifica se o documento tem registro de concessão/revogação 
	 * de seu sigilo.  
	 */
	public boolean haConcessaoRevogacaoSigiloDocumento(ProcessoDocumento processoDocumento) {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from ProcessoDocumentoSegredo o where ");
		sb.append("o.processoDocumento = :processoDocumento");
		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("processoDocumento", processoDocumento);
		if(q.getResultList() == null || q.getResultList().size() <= 0){
			return false;
		}
		return true;
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		Context session = Contexts.getSessionContext();
		Pessoa pessoa = (Pessoa) session.get("usuarioLogado");
		getInstance().setPessoa(pessoa);
		getInstance().setDtAlteracao(new Date());
		return super.beforePersistOrUpdate();
	}

	@Override
	public String persist() {
		String ret = super.persist();
		if ("persisted".equals(ret)) {
			ProcessoTrf processoTrf = ProcessoTrfHome.instance().getInstance();
			segredoJustica = processoTrf.getSegredoJustica();
			processoTrf.setSegredoJustica(segredoJustica);
			getEntityManager().merge(processoTrf);
		}
		return ret;
	}

	public static ProcessoDocumentoSegredoHome instance() {
		return ComponentUtil.getComponent("processoDocumentoSegredoHome");
	}

	public void setSegredoJustica(Boolean segredoJustica) {
		this.segredoJustica = segredoJustica;
	}

	public Boolean getSegredoJustica() {
		if (segredoJustica == null) {
			segredoJustica = ProcessoTrfHome.instance().getInstance().getSegredoJustica();
		}
		return segredoJustica;
	}

	public String getNomePessoa() {
		return nomePessoa;
	}

	public void setNomePessoa(String nomePessoa) {
		this.nomePessoa = nomePessoa;
	}

	public void setDocumentoSigiloso(Boolean documentoSigiloso) {
		this.documentoSigiloso = documentoSigiloso;
	}

	public Boolean getDocumentoSigiloso() {
		return documentoSigiloso;
	}

	public Boolean getExibeModal() {
		return exibeModal;
	}

	public void setExibeModal(Boolean exibeModal) {
		this.exibeModal = exibeModal;
	}

}
