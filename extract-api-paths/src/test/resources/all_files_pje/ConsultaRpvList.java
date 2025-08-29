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
import br.com.infox.cliente.component.ControleFiltros;
import br.com.infox.cliente.component.NumeroProcesso;
import br.com.infox.cliente.component.NumeroRpv;
import br.com.infox.cliente.util.ParametroUtil;
import br.jus.pje.nucleo.entidades.RpvStatus;
import br.jus.pje.nucleo.enums.RpvPrecatorioEnum;
import br.jus.pje.nucleo.util.StringUtil;

@Name(ConsultaRpvList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ConsultaRpvList extends EntityList<Map<String, Object>>{


	public static final String NAME = "consultaRpvList";
	private static final long serialVersionUID = 1L;

	private NumeroProcesso numeroProcesso;
	private String nomeAutorCabeca;
	private String nomeBeneficiario;
	private String cpfBeneficiario;
	private String cnpjBeneficiario;
	private boolean cpf = false;
	private String nomeInventariante;
	private String inRpvPrecatorio;
	private NumeroRpv numeroRpv;
	private Date dataExpedicaoInicio;
	private Date dataExpedicaoFim;
	private List<RpvStatus> rpvStatusList;
	//TODO depois mudar para false
	private boolean mostrarGrid = true;
	
	private static final String RP1 = "o.processoTrf.numeroSequencia = #{consultaRpvList.numeroProcesso.numeroSequencia}";
	private static final String RP2 = "o.processoTrf.ano = #{consultaRpvList.numeroProcesso.ano}";
	private static final String RP3 = "o.processoTrf.numeroDigitoVerificador = #{consultaRpvList.numeroProcesso.numeroDigitoVerificador}";
	private static final String RP4 = "o.processoTrf.numeroOrgaoJustica = #{consultaRpvList.numeroProcesso.numeroOrgaoJustica}";
	private static final String RP5 = "o.processoTrf.numeroOrigem = #{consultaRpvList.numeroProcesso.numeroOrigem}";	
	
	private static final String R2 = "exists (select rv.processoTrf.idProcessoTrf from Rpv rv "+
 									 "where rv.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "+ 
 									 "and lower(to_ascii(rv.autorCabecaAcao.nome)) like "+
 									 "'%' || lower(to_ascii(#{consultaRpvList.nomeAutorCabeca})) || '%')";

	private static final String R3 = "exists (select rv2.processoTrf.idProcessoTrf from Rpv rv2 "+
									 "where rv2.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "+ 
									 "and lower(to_ascii(rv2.beneficiario.nome)) like "+
									 "'%' || lower(to_ascii(#{consultaRpvList.nomeBeneficiario})) || '%')";
	
	private static final String R4 = " exists (select rv3.processoTrf.idProcessoTrf from Rpv rv3 " +
									 " where rv3.processoTrf.idProcessoTrf = o.idProcessoTrf " +
									 " and rv3.beneficiario.idUsuario IN  " +
									 " (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi " +
									 " where pdi.tipoDocumento.codTipo = 'CPF' and " +
									 "pdi.numeroDocumento like '%' || #{consultaRpvList.cpfBeneficiario} || '%'))";

	private static final String R5 = "exists (select rv4.processoTrf.idProcessoTrf from Rpv rv4 " +
									 " where rv4.processoTrf.idProcessoTrf = o.idProcessoTrf " +
									 " and rv4.beneficiario.idUsuario IN  " +
									 " (select pdi.pessoa.idUsuario from PessoaDocumentoIdentificacao pdi " +
									 " where pdi.tipoDocumento.codTipo = 'CPJ' and " +
									 "pdi.numeroDocumento like '%' || #{consultaRpvList.cnpjBeneficiario} || '%'))";	

	//TODO O Herdeiro está sendo tratado como inventariante
	private static final String R6 = "exists (select rpp.rpv.processoTrf.idProcessoTrf from RpvPessoaParte rpp "+
									 "where rpp.rpv.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "+ 
									 "and lower(to_ascii(rpp.pessoa.nome)) like "+
									 "'%' || lower(to_ascii(#{consultaRpvList.nomeInventariante})) || '%' " +
									 " and rpp.tipoParte.idTipoParte = :idTipoParte)";
	
	private static final String R7 = "exists (select r.idRpv from Rpv r " +
									 "where r.processoTrf = o.processoTrf " +
									 "and r.inRpvPrecatorio = " +
									 "#{consultaRpvList.inRpvPrecatorio})";
	
	private static final String R8 = "exists (select rp.idRpv from Rpv rp " + 
	 								 " where rp.processoTrf.idProcessoTrf =  o.processoTrf.idProcessoTrf "+
	 								 " and cast(rp.dataCadastro as date) >= #{consultaRpvList.dataExpedicaoInicio})";
	
	private static final String R9 = "exists (select rp.idRpv from Rpv rp " + 
									 " where rp.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "+
									 " and cast(rp.dataCadastro as date) <= #{consultaRpvList.dataExpedicaoFim})";

	private static final String R10 = "exists (select rpv.idRpv from Rpv rpv " + 
									  "where rpv.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "+
									  " and rpv.rpvStatus in (#{consultaRpvList.rpvStatusList}))";	
	
	private static final String RPV1 = "exists (select rn.idRpv from Rpv rn " + 
									  "where rn.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "+
									  " and rn.ano = #{consultaRpvList.numeroRpv.ano})";	
	
	private static final String RPV2 = "exists (select rn.idRpv from Rpv rn " + 
									  "where rn.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "+
									  " and rn.numeroSequencia = #{consultaRpvList.numeroRpv.numeroSequencia})";	
	
	private static final String RPV3 = "exists (select rn.idRpv from Rpv rn " + 
									  "where rn.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "+
									  " and rn.numeroVara = #{consultaRpvList.numeroRpv.numeroVara})";		
	
	private static final String RPV4 = "exists (select rn.idRpv from Rpv rn " + 
									  "where rn.processoTrf.idProcessoTrf = o.processoTrf.idProcessoTrf "+
									  " and rn.numeroOrigemProcesso = #{consultaRpvList.numeroRpv.numeroOrigemProcesso})";			
	@Override
	protected void addSearchFields() {
		addSearchField("processo.numeroProcesso.numeroSequencia", SearchCriteria.igual, RP1);
		addSearchField("processo.numeroProcesso.ano", SearchCriteria.igual, RP2);
		addSearchField("processo.numeroProcesso.numeroDigitoVerificador", SearchCriteria.igual, RP3);
		addSearchField("processo.numeroProcesso.numeroOrgaoJustica", SearchCriteria.igual, RP4);
		addSearchField("processo.numeroProcesso.numeroOrigem", SearchCriteria.igual, RP5);
		addSearchField("processoTrf.nomeAutorCabeca", SearchCriteria.igual, R2);
		addSearchField("processoTrf.nomeBeneficiario", SearchCriteria.igual, R3);
		addSearchField("processoTrf.cpfBeneficiario", SearchCriteria.contendo, R4);
		addSearchField("processoTrf.cnpjBeneficiario", SearchCriteria.contendo, R5);
		addSearchField("processoTrf.nomeInventariante", SearchCriteria.igual, getR6());
		addSearchField("processoTrf.inRpvPrecatorio", SearchCriteria.contendo, R7);
		addSearchField("processoTrf.dataExpedicaoInicio", SearchCriteria.contendo, R8);
		addSearchField("processoTrf.dataExpedicaoFim", SearchCriteria.contendo, R9);
		addSearchField("processoTrf.rpvStatusList", SearchCriteria.contendo, R10);
		addSearchField("processo.numeroRpv.ano", SearchCriteria.igual, RPV1);
		addSearchField("processo.numeroRpv.numeroSequencia", SearchCriteria.igual, RPV2);
		addSearchField("processo.numeroRpv.numeroVara", SearchCriteria.igual, RPV3);
		addSearchField("processo.numeroRpv.numeroOrigemProcesso", SearchCriteria.igual, RPV4);
	}


	private String getR6() {
		return StringUtil.replace(R6, ":idTipoParte", String.valueOf(
				ParametroUtil.instance().getTipoParteHerdeiro().getIdTipoParte()));
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		return null;
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder hql = new StringBuilder(); 
		hql.append("select new map(o.processoTrf as processoTrf, ").
			append("o.dataAutuacao as dataAutuacao, " ).
			append("o.classeJudicial as classeJudicial) ").
			append("from ConsultaProcessoTrf o ").
			append("where o.dataDistribuicao is not null");
		return hql.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return null;
	}
	
	@Override
	public void newInstance() {
		this.numeroProcesso = null;
		this.numeroRpv = null;
		this.cnpjBeneficiario = null;
		this.cpfBeneficiario = null;
		entity = new HashMap<String, Object>();
		list();
	}

	@Override
	public List<Map<String, Object>> getResultList() {
		ControleFiltros.instance().iniciarFiltro();
		return super.getResultList();
	}
	
	public void setCpf(boolean cpf) {
		this.cpf = cpf;
	}

	public boolean isCpf() {
		return cpf;
	}

	public void limparCamposCPFCNPJ() {
		setCpfBeneficiario(null);
		setCnpjBeneficiario(null);		
	}
	
	public RpvPrecatorioEnum[] getRpvPrecatorioValues(){
		return RpvPrecatorioEnum.values();
	}
	
	public NumeroProcesso getNumeroProcesso() {
		if (numeroProcesso == null) {
			numeroProcesso = new NumeroProcesso();
		}
		return numeroProcesso;
	}

	public void setNumeroProcesso(NumeroProcesso numeroProcesso) {
		this.numeroProcesso = numeroProcesso;
	}
	
	public void setNomeAutorCabeca(String nomeAutorCabeca) {
		this.nomeAutorCabeca = nomeAutorCabeca;
	}
	
	public String getNomeAutorCabeca() {
		return nomeAutorCabeca;
	}
	
	
	public String getNomeBeneficiario() {
		return nomeBeneficiario;
	}

	public void setNomeBeneficiario(String nomeBeneficiario) {
		this.nomeBeneficiario = nomeBeneficiario;
	}

	public String getCpfBeneficiario() {
		return cpfBeneficiario;
	}

	public void setCpfBeneficiario(String cpfBeneficiario) {
		this.cpfBeneficiario = cpfBeneficiario;
	}

	public String getCnpjBeneficiario() {
		return cnpjBeneficiario;
	}

	public void setCnpjBeneficiario(String cnpjBeneficiario) {
		this.cnpjBeneficiario = cnpjBeneficiario;
	}

	public String getNomeInventariante() {
		return nomeInventariante;
	}

	public void setNomeInventariante(String nomeInventariante) {
		this.nomeInventariante = nomeInventariante;
	}

	public String getInRpvPrecatorio() {
		return inRpvPrecatorio;
	}
	
	public void setInRpvPrecatorio(String inRpvPrecatorio) {
		this.inRpvPrecatorio = inRpvPrecatorio;
	}

	public NumeroRpv getNumeroRpv() {
		if (numeroRpv == null) {
			numeroRpv = new NumeroRpv();
		}
		return numeroRpv;
	}
	
	public void setNumeroRpv(NumeroRpv numeroRpv) {
		this.numeroRpv = numeroRpv;
	}

	public Date getDataExpedicaoInicio() {
		return dataExpedicaoInicio;
	}

	public void setDataExpedicaoInicio(Date dataExpedicaoInicio) {
		this.dataExpedicaoInicio = dataExpedicaoInicio;
	}

	public Date getDataExpedicaoFim() {
		return dataExpedicaoFim;
	}

	public void setDataExpedicaoFim(Date dataExpedicaoFim) {
		this.dataExpedicaoFim = dataExpedicaoFim;
	}

	public List<RpvStatus> getRpvStatusList() {
		return rpvStatusList;
	}

	public void setRpvStatusList(List<RpvStatus> rpvStatusList) {
		this.rpvStatusList = rpvStatusList;
	}

	public void setMostrarGrid(boolean mostrarGrid) {
		this.mostrarGrid = mostrarGrid;
	}

	public boolean getMostrarGrid() {
		return mostrarGrid;
	}	
}
