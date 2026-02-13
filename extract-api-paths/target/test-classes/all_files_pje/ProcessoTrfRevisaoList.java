package br.com.infox.pje.list;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.component.NumeroProcesso;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoTrf;

@Name(ProcessoTrfRevisaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ProcessoTrfRevisaoList extends EntityList<ProcessoTrf> {

	public static final String NAME = "processoTrfRevisaoList";

	private static final long serialVersionUID = 1L;

	private NumeroProcesso numeroProcesso = new NumeroProcesso();
	private OrgaoJulgador orgaoJulgador;
	private Date dataSugestaoSessao;

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();

		SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd");
		String data = null;
		try {
			data = fm.format(Calendar.getInstance().getTime());
		} catch (Exception e) {
			e.printStackTrace();
		}
		sb.append("select o from ProcessoTrfConsultaSemFiltros o ");
		sb.append("where  o.revisado = false and o.processoStatus = 'D' and o.prontoRevisao = true and o.selecionadoJulgamento = false ");
		sb.append("and (o.orgaoJulgador in (select sco.orgaoJulgador from SessaoComposicaoOrdem sco ");
		sb.append("where sco.sessao.dataSessao >= '" + data + "' ");
		sb.append("and sco.orgaoJulgadorRevisor = #{authenticator.getOrgaoJulgadorAtual()})) ");
		return sb.toString();
	}

	private static final String R1 = "o.numeroSequencia = #{processoTrfRevisaoList.numeroProcesso.numeroSequencia}";
	private static final String R2 = "o.numeroDigitoVerificador = #{processoTrfRevisaoList.numeroProcesso.numeroDigitoVerificador}";
	private static final String R3 = "o.ano = #{processoTrfRevisaoList.numeroProcesso.ano}";
	private static final String R4 = "o.numeroOrigem = #{processoTrfRevisaoList.numeroProcesso.numeroOrigem}";
	private static final String R5 = "o.numeroOrgaoJustica = #{processoTrfRevisaoList.numeroProcesso.numeroOrgaoJustica}";
	private static final String R6 = "o.orgaoJulgador = #{processoTrfRevisaoList.orgaoJulgador}";
	private static final String R7 = "cast(o.dataSugestaoSessao as date) = #{processoTrfRevisaoList.dataSugestaoSessao}";

	private static final String DEFAULT_ORDER = "idProcessoTrf";

	@Override
	protected void addSearchFields() {
		addSearchField("numeroSequencia", SearchCriteria.contendo, R1);
		addSearchField("numeroDigitoVerificador", SearchCriteria.contendo, R2);
		addSearchField("ano", SearchCriteria.contendo, R3);
		addSearchField("numeroOrigem", SearchCriteria.contendo, R4);
		addSearchField("numeroOrgaoJustica", SearchCriteria.igual, R5);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R6);
		addSearchField("dataSugestaoSessao", SearchCriteria.igual, R7);
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("numeroProcesso", "processoTrf.processo.numeroProcesso");
		return null;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public NumeroProcesso getNumeroProcesso() {
		return numeroProcesso;
	}

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public Date getDataSugestaoSessao() {
		return dataSugestaoSessao;
	}

	public void setDataSugestaoSessao(Date dataSugestaoSessao) {
		this.dataSugestaoSessao = dataSugestaoSessao;
	}

	@Override
	public void newInstance() {
		setOrgaoJulgador(null);
		setNumeroProcesso(new NumeroProcesso());
		setDataSugestaoSessao(null);
		super.newInstance();
	}
}
