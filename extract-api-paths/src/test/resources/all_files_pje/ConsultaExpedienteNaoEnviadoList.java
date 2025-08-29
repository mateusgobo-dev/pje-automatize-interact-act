package br.com.infox.pje.list;

import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.ExpedicaoExpedienteEnum;

@Name(ConsultaExpedienteNaoEnviadoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ConsultaExpedienteNaoEnviadoList extends EntityList<ProcessoExpediente> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "consultaExpedienteNaoEnviadoList";

	private String numeroProcesso;
	private TipoProcessoDocumento tipoDocumentoAtoMagistrado;
	private String nomeParte;
	private TipoProcessoDocumento tipoExpediente;
	private ExpedicaoExpedienteEnum meioExpedicao;

	private static final String DEFAULT_EJBQL = "select o from ProcessoExpediente o "
			+ "where not exists(select pde from ProcessoDocumentoExpediente pde"
			+ "             	  where pde.processoDocumento.processoDocumentoBin.certChain is not null "
			+ "                   and pde.processoDocumento.processoDocumentoBin.signature is not null "
			+ "                   and pde.anexo = false " + "               	and pde.processoExpediente = o) "
			+ "  and o.dtExclusao is null" + "  and o.inTemporario = false "
			+ "  and (o.meioExpedicaoExpediente != 'E' or "
			+ "         not exists(select ppa from ProcessoParteExpediente ppa "
			+ "			         where ppa.processoExpediente = o" + "				       and ppa.pendencia is not null))"
			+ "  and o.tipoProcessoDocumento != #{parametroUtil.tipoProcessoDocumentoIntimacaoPauta}";

	private static final String DEFAULT_ORDER = "o";

	private static final String R1 = "o.processoTrf.processo.numeroProcesso like concat('%', #{consultaExpedienteNaoEnviadoList.numeroProcesso}, '%')";
	private static final String R2 = "exists(select pd from ProcessoDocumentoExpediente pd where pd.processoExpediente = o and pd.processoDocumentoAto.tipoProcessoDocumento = #{consultaExpedienteNaoEnviadoList.tipoDocumentoAtoMagistrado})";
	private static final String R3 = "exists(select ppe from ProcessoParteExpediente ppe where ppe.processoExpediente = o and ppe.pessoaParte.nome like concat('%', #{consultaExpedienteNaoEnviadoList.nomeParte}, '%'))";
	private static final String R4 = "o.tipoProcessoDocumento = #{consultaExpedienteNaoEnviadoList.tipoExpediente}";
	private static final String R5 = "o.meioExpedicaoExpediente = #{consultaExpedienteNaoEnviadoList.meioExpedicao}";
	private static final String R6 = "o.processoTrf.orgaoJulgador = #{orgaoJulgadorAtual}";
	private static final String R7 = "o.processoTrf.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual}";

	@Override
	protected void addSearchFields() {
		addSearchField("numeroProcesso", SearchCriteria.contendo, R1);
		addSearchField("atoMagistrado", SearchCriteria.igual, R2);
		addSearchField("nomeParte", SearchCriteria.contendo, R3);
		addSearchField("tipoExpediente", SearchCriteria.igual, R4);
		addSearchField("meioExpedicao", SearchCriteria.igual, R5);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R6);
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R7);
	}

	public static ConsultaExpedienteNaoEnviadoList instance() {
		return ComponentUtil.getComponent(NAME);
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

	public void setTipoDocumentoAtoMagistrado(TipoProcessoDocumento tipoDocumentoAtoMagistrado) {
		this.tipoDocumentoAtoMagistrado = tipoDocumentoAtoMagistrado;
	}

	public TipoProcessoDocumento getTipoDocumentoAtoMagistrado() {
		return tipoDocumentoAtoMagistrado;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setNomeParte(String nomeParte) {
		this.nomeParte = nomeParte;
	}

	public String getNomeParte() {
		return nomeParte;
	}

	public void setTipoExpediente(TipoProcessoDocumento tipoExpediente) {
		this.tipoExpediente = tipoExpediente;
	}

	public TipoProcessoDocumento getTipoExpediente() {
		return tipoExpediente;
	}

	public void setMeioExpedicao(ExpedicaoExpedienteEnum meioExpedicao) {
		this.meioExpedicao = meioExpedicao;
	}

	public ExpedicaoExpedienteEnum getMeioExpedicao() {
		return meioExpedicao;
	}

}
