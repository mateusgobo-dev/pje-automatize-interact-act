package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.core.certificado.ValidaDocumentoHome;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoBinPessoaAssinatura;

@Name(AssinaturaList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AssinaturaList extends EntityList<ProcessoDocumentoBinPessoaAssinatura> {

	public static final String NAME = "assinaturaList";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_EJBQL = "select o from ProcessoDocumentoBinPessoaAssinatura o";
	private static final String DEFAULT_ORDER = "dataAssinatura";

	private static final String R1 = "o.processoDocumentoBin.idProcessoDocumentoBin = #{processoDocumentoHome.instance.processoDocumentoBin.idProcessoDocumentoBin}";

	private ProcessoDocumentoBinPessoaAssinatura assinaturaSelecionada;

	@Override
	protected void addSearchFields() {
		addSearchField("idProcessoDocumentoBin", SearchCriteria.igual, R1);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		return DEFAULT_EJBQL;
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	public void setAssinaturaSelecionada(ProcessoDocumentoBinPessoaAssinatura assinaturaSelecionada) {
		this.assinaturaSelecionada = assinaturaSelecionada;
		ValidaDocumentoHome.instance().validaDocumento(assinaturaSelecionada.getProcessoDocumentoBin(),
				assinaturaSelecionada.getCertChain(), assinaturaSelecionada.getAssinatura());
	}

	public ProcessoDocumentoBinPessoaAssinatura getAssinaturaSelecionada() {
		return assinaturaSelecionada;
	}

}