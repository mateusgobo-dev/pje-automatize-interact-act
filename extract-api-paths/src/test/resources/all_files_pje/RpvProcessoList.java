package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.util.StringUtil;

@Name(RpvProcessoList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class RpvProcessoList extends EntityList<Map<String, Object>> {

	public static final String NAME = "rpvProcessoList";
	private static final long serialVersionUID = 1L;

	private static final String R1 = " lower(to_ascii(o.autorCabecaAcao.nome)) like "
			+ "'%' || lower(to_ascii(#{consultaRpvList.nomeAutorCabeca})) || '%'";

	private static final String R2 = " lower(to_ascii(o.beneficiario.nome)) like "
			+ "'%' || lower(to_ascii(#{consultaRpvList.nomeBeneficiario})) || '%'";

	private static final String R3 = " o.beneficiario.idUsuario IN  "
			+ " (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ " where pdi.tipoDocumento.codTipo = 'CPF' and "
			+ " pdi.numeroDocumento like '%' || #{consultaRpvList.cpfBeneficiario} || '%')";

	private static final String R4 = " and o.beneficiario.idUsuario IN  "
			+ " (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi "
			+ " where pdi.tipoDocumento.codTipo = 'CPJ' and "
			+ " pdi.numeroDocumento like '%' || #{consultaRpvList.cnpjBeneficiario} || '%')";

	// TODO O Herdeiro está sendo tratado como inventariante
	private static final String R5 = "exists (select rpp.rpv.idRpv from RpvPessoaParte rpp "
			+ "where rpp.rpv.idRpv = o.idRpv " + "and lower(to_ascii(rpp.pessoa.nome)) like "
			+ "'%' || lower(to_ascii(#{consultaRpvList.nomeInventariante})) || '%' "
			+ " and rpp.tipoParte.idTipoParte = :idTipoParte)";

	private static final String R6 = "exists (select rp.idRpv from Rpv rp " + " where rp.idRpv = o.idRpv "
			+ " and cast(rp.dataCadastro as date) >= #{consultaRpvList.dataExpedicaoInicio})";

	private static final String R7 = "exists (select rp.idRpv from Rpv rp " + " where rp.idRpv = o.idRpv "
			+ " and cast(rp.dataCadastro as date) <= #{consultaRpvList.dataExpedicaoFim})";

	private static final String R8 = "exists (select rpv.idRpv from Rpv rpv " + " where rpv.idRpv = o.idRpv "
			+ " and rpv.rpvStatus in (#{consultaRpvList.rpvStatusList}))";

	private static final String R9 = "exists (select r.idRpv from Rpv r " + "where r.idRpv = o.idRpv "
			+ "and r.inRpvPrecatorio = " + "#{consultaRpvList.inRpvPrecatorio})";

	private static final String RPV1 = "exists (select rn.idRpv from Rpv rn " + "where rn.idRpv = o.idRpv "
			+ " and rn.ano = #{consultaRpvList.numeroRpv.ano})";

	private static final String RPV2 = "exists (select rn.idRpv from Rpv rn " + "where rn.idRpv = o.idRpv "
			+ " and rn.numeroSequencia = #{consultaRpvList.numeroRpv.numeroSequencia})";

	private static final String RPV3 = "exists (select rn.idRpv from Rpv rn " + "where rn.idRpv = o.idRpv "
			+ " and rn.numeroVara = #{consultaRpvList.numeroRpv.numeroVara})";

	private static final String RPV4 = "exists (select rn.idRpv from Rpv rn " + "where rn.idRpv = o.idRpv "
			+ " and rn.numeroOrigemProcesso = #{consultaRpvList.numeroRpv.numeroOrigemProcesso})";

	@Override
	protected void addSearchFields() {
		addSearchField("nomeAutorCabeca", SearchCriteria.igual, R1);
		addSearchField("nomeBeneficiario", SearchCriteria.igual, R2);
		addSearchField("cpfBeneficiario", SearchCriteria.contendo, R3);
		addSearchField("cnpjBeneficiario", SearchCriteria.contendo, R4);
		addSearchField("nomeInventariante", SearchCriteria.igual, getR5());
		addSearchField("dataExpedicaoInicio", SearchCriteria.contendo, R6);
		addSearchField("dataExpedicaoFim", SearchCriteria.contendo, R7);
		addSearchField("rpvStatusList", SearchCriteria.contendo, R8);
		addSearchField("inRpvPrecatorio", SearchCriteria.contendo, R9);
		addSearchField("numeroRpv.ano", SearchCriteria.igual, RPV1);
		addSearchField("numeroRpv.numeroSequencia", SearchCriteria.igual, RPV2);
		addSearchField("numeroRpv.numeroVara", SearchCriteria.igual, RPV3);
		addSearchField("numeroRpv.numeroOrigemProcesso", SearchCriteria.igual, RPV4);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder hql = new StringBuilder();
		hql.append("select new map(o.inRpvPrecatorio as inRpvPrecatorio, ")
				.append("o.numeroRpvPrecatorio as numeroRpvPrecatorio, ")
				.append(" o.dataCadastro as dataCadastro, o.valorRequisitado as valorRequisitado, ")
				.append("o.rpvStatus as rpvStatus, o.idRpv as idRpv) ").append("from Rpv o ")
				.append("where o.processoTrf.idProcessoTrf = #{rpvAction.processoTrf.idProcessoTrf}");
		return hql.toString();
	}

	public String getR5() {
		return StringUtil.replace(R5, ":idTipoParte",
				String.valueOf(ParametroUtil.instance().getTipoParteHerdeiro().getIdTipoParte()));
	}

	@Override
	protected String getDefaultOrder() {
		return "o.numeroRpvPrecatorio";
	}

	@Override
	public void newInstance() {
		entity = new HashMap<String, Object>();
		list();
	}

}
