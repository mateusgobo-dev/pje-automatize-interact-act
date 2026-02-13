package br.com.infox.pje.list;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.OrgaoJulgadorColegiado;
import br.jus.pje.nucleo.entidades.Sessao;
import br.jus.pje.nucleo.enums.SituacaoSessaoEnum;
import br.jus.pje.nucleo.enums.StatusSessaoEnum;

@Name(SessaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class SessaoList extends EntityList<Sessao> {

	public static final String NAME = "sessaoList";
	private String sala;
	private Date dataInicio;
	private Date dataFim;
	private StatusSessaoEnum status;
	private SituacaoSessaoEnum situacaoSessao;
	private OrgaoJulgadorColegiado ojcPesquisa = pegarOjcPesquisa();

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "o.dataSessao DESC, o.tipoSessao ASC";

	private static final String R0 = "o.orgaoJulgadorColegiado = #{sessaoList.ojcPesquisa} ";
	private static final String R1 = "lower(to_ascii(o.orgaoJulgadorColegiadoSalaHorario.sala.sala)) like concat('%', lower(to_ascii(#{sessaoList.sala})),'%')";
	private static final String R2 = "cast(o.dataSessao as date) >= #{sessaoList.dataInicio}";
	private static final String R3 = "cast(o.dataSessao as date) <= #{sessaoList.dataFim}";

	@Override
	protected void addSearchFields() {
		addSearchField("tipoSessao", SearchCriteria.igual);
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R0);
		addSearchField("idSessao", SearchCriteria.igual, R1);
		addSearchField("usuarioExclusao", SearchCriteria.igual, R2);
		addSearchField("dataExclusao", SearchCriteria.igual, R3);

	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from Sessao o where ");

		if (Authenticator.getOrgaoJulgadorAtual() != null && Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
			sb.append("(o.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()} and ");
			sb.append("(o in (select s.sessao from SessaoComposicaoOrdem s where ");
			sb.append("	      s.orgaoJulgador = #{authenticator.getOrgaoJulgadorAtual()} and ");
			sb.append("	 	  o.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()}) ");
			sb.append("		  or (not exists (select sco from SessaoComposicaoOrdem sco where sco.sessao = o))))");
			return sb.toString();
		} else {
			if (Authenticator.getOrgaoJulgadorAtual() != null) {
				sb.append("(o in (select s.sessao from SessaoComposicaoOrdem s where ");
				sb.append("		  s.orgaoJulgador = #{authenticator.getOrgaoJulgadorAtual()})) ");
			} else if (Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
				sb.append("o.orgaoJulgadorColegiado = #{authenticator.getOrgaoJulgadorColegiadoAtual()} ");
			} else if (Authenticator.getOrgaoJulgadorAtual() == null
					&& Authenticator.getOrgaoJulgadorColegiadoAtual() == null) {
				sb.append("o not in (select s.sessao from SessaoComposicaoOrdem s) ");
			}
		}
		return sb.toString();
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("orgaoJulgadorColegiado", "o.orgaoJulgadorColegiado");
		map.put("dataSessao", "o.dataSessao");
		map.put("orgaoJulgadorColegiadoSalaHorario", "o.orgaoJulgadorColegiadoSalaHorario");
		map.put("tipoSessao", "o.tipoSessao");
		map.put("dataFechamentoSessao", "o.dataFechamentoSessao");
		map.put("sala.horaInicial", "o.orgaoJulgadorColegiadoSalaHorario.horaInicial");
		map.put("sala.horaFinal", "o.orgaoJulgadorColegiadoSalaHorario.horaFinal");
		return map;
	}

	@Override
	protected String getDefaultOrder() {
		if (Authenticator.getOrgaoJulgadorAtual() != null && Authenticator.getOrgaoJulgadorColegiadoAtual() != null) {
			return ("o.idSessao");
		}
		return DEFAULT_ORDER;
	}

	@Override
	public List<Sessao> list(int maxResult) {
		setEjbql(getDefaultEjbql().concat(obterQueryStatus()).concat(obterQuerySituacao()));
		return super.list(maxResult);
	}
	
	private String obterQueryStatus() {
		String resultado = null;
		if (StatusSessaoEnum.EASP.equals(status)) {
			resultado = " and o.dataAberturaSessao is null and o.dataFechamentoPauta is null ";
		} else if (StatusSessaoEnum.EACP.equals(status)) {
			resultado = " and o.dataAberturaSessao is null and o.dataFechamentoPauta is not null ";
		} else if (StatusSessaoEnum.A.equals(status)) {
			resultado = " and o.dataAberturaSessao is not null and o.dataRealizacaoSessao is null ";
		} else if (StatusSessaoEnum.R.equals(status)) {
			resultado = " and o.dataRealizacaoSessao is not null and o.dataRegistroEvento is null ";
		} else if (StatusSessaoEnum.RE.equals(status)) {
			resultado = " and o.dataRegistroEvento is not null and o.dataFechamentoSessao is null ";
		} else if (StatusSessaoEnum.F.equals(status)) {
			resultado = " and o.dataFechamentoSessao is not null ";
		} else {
			resultado = StringUtils.EMPTY;
		}
		return resultado;
	}
	
	private String obterQuerySituacao() {
		String resultado = null;
		if (SituacaoSessaoEnum.ATIVA.equals(this.situacaoSessao)) {
			resultado = " and o.dataExclusao is null ";
		} else if (SituacaoSessaoEnum.INATIVA.equals(this.situacaoSessao)) {
			resultado = " and o.dataExclusao is not null ";
		} else {
			resultado = StringUtils.EMPTY;
		}
		return resultado;
	}

	private OrgaoJulgadorColegiado pegarOjcPesquisa() {
		if (null != Authenticator.getOrgaoJulgadorColegiadoAtual()) {
			return Authenticator.getOrgaoJulgadorColegiadoAtual();
		}
		return null;
	}

	/*
	 * Caso haja mais algum atributo dessa classe, deverá ser incluido aqui para
	 * limpá-lo
	 */
	@Override
	public void newInstance() {
		sala = null;
		dataInicio = null;
		dataFim = null;
		status = null;
		situacaoSessao=null;
		if (Authenticator.getOrgaoJulgadorColegiadoAtual() == null) {
			ojcPesquisa = null;
		}
		super.newInstance();
	}

	public void setSala(String sala) {
		this.sala = sala;
	}

	public String getSala() {
		return sala;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setStatus(StatusSessaoEnum status) {
		this.status = status;
	}

	public StatusSessaoEnum getStatus() {
		return status;
	}

	public SituacaoSessaoEnum getSituacaoSessao() {
		return situacaoSessao;
	}

	public void setSituacaoSessao(SituacaoSessaoEnum situacaoSessao) {
		this.situacaoSessao = situacaoSessao;
	}

	public void setOjcPesquisa(OrgaoJulgadorColegiado ojcPesquisa) {
		this.ojcPesquisa = ojcPesquisa;
	}

	public OrgaoJulgadorColegiado getOjcPesquisa() {
		return ojcPesquisa;
	}
}