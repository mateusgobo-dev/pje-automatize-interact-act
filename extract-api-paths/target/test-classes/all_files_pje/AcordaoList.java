package br.com.infox.pje.list;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.pje.nucleo.entidades.SessaoPautaProcessoTrf;
import br.jus.pje.nucleo.enums.AdiadoVistaEnum;

@Name(AcordaoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class AcordaoList extends EntityList<SessaoPautaProcessoTrf> {

	public static final String NAME = "acordaoList";

	private Date dataSessao;
	private String numeroProcesso;
	private String pendente = "T";

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_ORDER = "idSessaoPautaProcessoTrf";

	private static String R1 = "o.sessao.dataSessao = #{acordaoList.dataSessao}";
	private static String R2 = "o.processoTrf.processo.numeroProcesso like concat('%', #{acordaoList.numeroProcesso}, '%')";

	@Override
	protected void addSearchFields() {
		addSearchField("sessao.dataSessao", SearchCriteria.igual, R1);
		addSearchField("processoTrf.processo.numeroProcesso", SearchCriteria.igual, R2);
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append("select o from SessaoPautaProcessoTrf o ");
		sb.append("where o.processoTrf.processo.idProcesso not in (select spd.processoDocumento.processo.idProcesso from SessaoProcessoDocumento spd ");
		sb.append("inner join spd.processoDocumento.processoDocumentoBin as pdb ");
		sb.append("where ((spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoInteiroTeor} ");
		sb.append("or spd.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao}" +
				" ) and pdb.signatarios is not empty) ");
		sb.append("and spd.sessao.idSessao = o.sessao.idSessao ");
		sb.append("and spd.processoDocumento.processo.idProcesso = o.processoTrf.processo.idProcesso) ");
		sb.append("and o in(select max(sppt) from SessaoPautaProcessoTrf sppt where sppt.processoTrf = o.processoTrf) ");
		
		String condicaoOJA = Authenticator.getOrgaoJulgadorAtual() != null ? " = #{orgaoJulgadorAtual} " : " IS NULL ";
		String condicaoOJCA = Authenticator.getOrgaoJulgadorColegiadoAtual() != null ? " = #{orgaoJulgadorColegiadoAtual} " : " IS NULL ";
		String condicaoOJA2 = Authenticator.getOrgaoJulgadorAtual() != null ? " #{orgaoJulgadorAtual} " : " NULL ";
		
		if (Authenticator.isPapelAssessor() || Authenticator.isMagistrado()) {
			sb.append("and o.orgaoJulgadorVencedor " + condicaoOJA);
			sb.append("and o.sessao.orgaoJulgadorColegiado " + condicaoOJCA);
		}

		if (pendente != null && pendente.equals("R")) {
			sb.append("and o.processoTrf.orgaoJulgador " +condicaoOJA);
		} else if (pendente != null && pendente.equals("RA")) {
			sb.append("and o.processoTrf.orgaoJulgador != " +condicaoOJA2);
		}		
		
		sb.append("and (o.adiadoVista not in ('" + AdiadoVistaEnum.AD + "', '" + AdiadoVistaEnum.PV + "') or o.adiadoVista is null or o.adiadoVista = '') ");
		sb.append("and o.retiradaJulgamento = " + Boolean.FALSE);
		
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("numeroProcesso", "processoTrf.processo.numeroProcesso");
		map.put("orgaoJulgador", "processoTrf.orgaoJulgador");
		map.put("classeJudicial", "processoTrf.classeJudicial");
		map.put("dtSessao", "sessao.dataSessao");
		return map;
	}

	@Override
	public List<SessaoPautaProcessoTrf> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	public void setDataSessao(Date dataSessao) {
		this.dataSessao = dataSessao;
	}

	public Date getDataSessao() {
		return dataSessao;
	}

	public void setNumeroProcesso(String numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}

	public String getNumeroProcesso() {
		return numeroProcesso;
	}

	@Override
	public void newInstance() {
		setNumeroProcesso(null);
		setDataSessao(null);
		setPendente("T");
		super.newInstance();
	}

	public void setPendente(String pendente) {
		this.pendente = pendente;
	}

	public String getPendente() {
		return pendente;
	}

}
