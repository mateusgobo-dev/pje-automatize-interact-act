package br.com.infox.cliente.home;

import java.util.Date;

import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;

import br.com.itx.component.grid.GridQuery;
import br.jus.csjt.pje.commons.util.ParametroJtUtil;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoDocumento;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoLido;

@Name("processoDocumentoLidoHome")
@BypassInterceptors
public class ProcessoDocumentoLidoHome extends AbstractProcessoDocumentoLidoHome<ProcessoDocumentoLido> {

	private static final long serialVersionUID = 1L;

	@Override
	public String persist() {
		GridQuery grid = (GridQuery) Component.getInstance(getIdGridQuery());
		Context session = Contexts.getSessionContext();
		Pessoa pessoaLogada = (Pessoa) session.get("usuarioLogado");
		String persist = null;
		for (int i = 0; i < grid.getSelectedRowsList().size(); i++) {
			ProcessoDocumento pd = (ProcessoDocumento) grid.getSelectedRowsList().get(i);
			if (!isDocumentoLido(pd.getIdProcessoDocumento())) {
				getInstance().setProcessoDocumento(pd);
				getInstance().setPessoa(pessoaLogada);
				getInstance().setDataApreciacao(new Date());
				pd.setLido(true);
				persist = super.persist();
				newInstance();
			}
		}
		FacesMessages.instance().clear();
		// [PJEII-1159] Mensagem alterada pois poderia passar a falsa impressao de ter sido apreciada por um magistrado
		FacesMessages.instance().add(Severity.INFO, "Documento(s) apreciado(s) com sucesso.");
		Contexts.removeFromAllContexts(getIdGridQuery());
		return persist;

	}

	public Boolean isDocumentoLido(Integer idProcessoDocumento) {
		String query = "select count(o) from ProcessoDocumentoLido o where " + "o.processoDocumento.idProcessoDocumento = :id";
		Query q = getEntityManager().createQuery(query);
		q.setParameter("id", idProcessoDocumento);
		try {
			Long retorno = (Long) q.getSingleResult();
			return retorno > 0;
		} catch (NoResultException no) {
			return Boolean.FALSE;
		}
	}

	public String getIdGridQuery() {
		return ParametroJtUtil.instance().justicaTrabalho() ? "documentoAnexoJTGrid" : "documentoAnexoGrid";
	}
}