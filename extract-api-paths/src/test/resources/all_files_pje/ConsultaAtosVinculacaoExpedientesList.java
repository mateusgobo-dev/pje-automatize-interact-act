package br.com.infox.pje.list;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

import br.com.infox.DAO.EntityList;
import br.com.infox.DAO.SearchCriteria;
import br.com.infox.cliente.util.ParametroUtil;
import br.com.itx.util.ComponentUtil;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoDocumentoTrf;
import br.jus.pje.nucleo.entidades.TipoProcessoDocumento;
import br.jus.pje.nucleo.enums.SituacaoAtoEnum;

@Name(ConsultaAtosVinculacaoExpedientesList.NAME)
@BypassInterceptors
@Scope(ScopeType.PAGE)
public class ConsultaAtosVinculacaoExpedientesList extends EntityList<ProcessoDocumentoTrf> {

	private static final long serialVersionUID = 1L;
	public static final String NAME = "consultaAtosVinculacaoExpedientesList";

	private String numeroProcesso;
	private OrgaoJulgador orgaoJulgador;
	private String nomeParte;
	private TipoProcessoDocumento tipoDocumentoAtoMagistrado;
	private Boolean visibilidadeAto;
	private SituacaoAtoEnum situacaoAto;

	private static final String DEFAULT_EJBQL = "select o from ProcessoDocumentoTrf o ";
	private static final String DEFAULT_ORDER = "o";

	private static final String R1 = "o.processoTrf.processo.numeroProcesso like concat('%', #{consultaAtosVinculacaoExpedientesList.numeroProcesso}, '%')";
	private static final String R2 = "o.processoTrf.orgaoJulgador = #{consultaAtosVinculacaoExpedientesList.orgaoJulgador}";
	private static final String R3 = "exists(select ppe from ProcessoParte ppe where ppe.processoTrf = o.processoTrf and lower(ppe.pessoa.nome) like concat('%', lower(#{consultaAtosVinculacaoExpedientesList.nomeParte}), '%'))";
	private static final String R4 = "o.processoDocumento.tipoProcessoDocumento = #{consultaAtosVinculacaoExpedientesList.tipoDocumentoAtoMagistrado}";
	private static final String R5 = "o.processoTrf.orgaoJulgador = #{orgaoJulgadorAtual}";
	private static final String R6 = "o.processoTrf.orgaoJulgadorColegiado = #{orgaoJulgadorColegiadoAtual}";

	@Override
	protected void addSearchFields() {
		addSearchField("numeroProcesso", SearchCriteria.igual, R1);
		addSearchField("orgaoJulgador1", SearchCriteria.igual, R2);
		addSearchField("nomeParte", SearchCriteria.contendo, R3);
		addSearchField("tipoExpediente", SearchCriteria.igual, R4);
		addSearchField("orgaoJulgador", SearchCriteria.igual, R5);
		addSearchField("orgaoJulgadorColegiado", SearchCriteria.igual, R6);
	}

	@Override
	protected Map<String, String> getCustomColumnsOrder() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("atoMagistrado", "o.processoDocumento.tipoProcessoDocumento");
		map.put("orgaoJulgador", "o.processoTrf.orgaoJulgador");
		map.put("criacaoDocumento", "o.processoDocumento.dataInclusao");
		return map;
	}

	@Override
	public List<ProcessoDocumentoTrf> getResultList() {
		setEjbql(getDefaultEjbql());
		return super.getResultList();
	}

	@Override
	protected String getDefaultEjbql() {
		StringBuilder sb = new StringBuilder();
		sb.append(DEFAULT_EJBQL);
		sb.append(getRestrictionAtoMagistrado());
		sb.append(getRestrictionVisibilidadeAto());
		sb.append(getRestrictionSituacaoAto());
		return sb.toString();
	}

	private String getRestrictionAtoMagistrado() {
		StringBuilder sb = new StringBuilder();
		sb.append("where o.processoDocumento.ativo = true ");
		sb.append("  and exists(select pdpa from ProcessoDocumentoBinPessoaAssinatura pdpa ");
		sb.append("                  where pdpa.processoDocumentoBin = o.processoDocumento.processoDocumentoBin) ");
		sb.append("  and (o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoDespacho} ");
		sb.append("       or o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoDecisao} ");
		sb.append("       or o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAtoOrdinatorio} ");
		if (ParametroUtil.instance().isPrimeiroGrau()) {
			sb.append("or o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoSentenca} ");
		} else {
			sb.append("or o.processoDocumento.tipoProcessoDocumento = #{parametroUtil.tipoProcessoDocumentoAcordao} ");
		}
		sb.append(") ");
		return sb.toString();
	}

	private String getRestrictionSituacaoAto() {
		StringBuilder sb = new StringBuilder();
		if (situacaoAto == SituacaoAtoEnum.EP) {
			sb.append("and exists(select pp from ProcessoParte pp "
					+ "where pp.processoTrf = o.processoTrf "
					+ "  and not exists(select pe from ProcessoExpediente pe "
					+ "					where exists(select pde from ProcessoDocumentoExpediente pde "
					+ "             	  			 where exists(select pdpa from ProcessoDocumentoBinPessoaAssinatura pdpa "
					+ "                  						   where pdpa.processoDocumentoBin = o.processoDocumento.processoDocumentoBin) "
					+ "                   			 and pde.anexo = false "
					+ "								 and pde.processoDocumentoAto = o.processoDocumento "
					+ "               	  			 and pde.processoExpediente = pe) "
					+ "					and not exists(select ppe from ProcessoParteExpediente ppe "
					+ "								   where ppe.processoExpediente = pe" + "								   and ppe.pessoaParte = pp.pessoa))) ");
		} else if (situacaoAto == SituacaoAtoEnum.NE) {
			sb.append("and not exists(select pde from ProcessoDocumentoExpediente pde "
					+ " 	  	      where exists(select pdpa from ProcessoDocumentoBinPessoaAssinatura pdpa "
					+ "                  			where pdpa.processoDocumentoBin = o.processoDocumento.processoDocumentoBin) "
					+ "                 and pde.processoDocumentoAto = o.processoDocumento) ");
		} else if (situacaoAto == SituacaoAtoEnum.SE) {
			sb.append("and o.processoTrf.segredoJustica = true "
					+ "and exists(select pvs from ProcessoVisibilidadeSegredo pvs "
					+ "           where pvs.processo.idProcesso = o.processoTrf.processo.idProcesso "
					+ "				and pvs.pessoa = #{usuarioLogado}) ");
		}
		return sb.toString();
	}

	private String getRestrictionVisibilidadeAto() {
		StringBuilder sb = new StringBuilder();
		if (visibilidadeAto != null) {
			sb.append("and ");
			if (!visibilidadeAto) {
				sb.append("not ");
			}
			sb.append("exists(select pdt from ProcessoDocumentoTrfLocal pdt where pdt.processoDocumento.idProcessoDocumento = o.processoDocumento.idProcessoDocumento and pdt.liberadoConsultaPublica = true) ");
		}
		return sb.toString();
	}

	@Override
	protected String getDefaultOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	public void newInstance() {
		setVisibilidadeAto(null);
		setOrgaoJulgador(null);
		super.newInstance();
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

	public void setOrgaoJulgador(OrgaoJulgador orgaoJulgador) {
		this.orgaoJulgador = orgaoJulgador;
	}

	public OrgaoJulgador getOrgaoJulgador() {
		return orgaoJulgador;
	}

	public void setVisibilidadeAto(Boolean visibilidadeAto) {
		this.visibilidadeAto = visibilidadeAto;
	}

	public Boolean getVisibilidadeAto() {
		return visibilidadeAto;
	}

	public void setSituacaoAto(SituacaoAtoEnum situacaoAto) {
		this.situacaoAto = situacaoAto;
	}

	public SituacaoAtoEnum getSituacaoAto() {
		return situacaoAto;
	}

	public void setTipoDocumentoAtoMagistrado(TipoProcessoDocumento tipoDocumentoAtoMagistrado) {
		this.tipoDocumentoAtoMagistrado = tipoDocumentoAtoMagistrado;
	}

	public TipoProcessoDocumento getTipoDocumentoAtoMagistrado() {
		return tipoDocumentoAtoMagistrado;
	}

	public static ConsultaAtosVinculacaoExpedientesList instance() {
		return ComponentUtil.getComponent(NAME);
	}
}
