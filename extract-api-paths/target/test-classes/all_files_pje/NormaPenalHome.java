package br.com.infox.cliente.home;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.Messages;
import org.jboss.seam.international.StatusMessage;

import br.jus.pje.nucleo.entidades.NormaPenal;
import br.jus.pje.nucleo.entidades.TipoNormaPenal;

@Scope(ScopeType.CONVERSATION)
@Name("normaPenalHome")
@BypassInterceptors
public class NormaPenalHome extends AbstractNormaPenalHome<NormaPenal> {

	private static final long serialVersionUID = 1L;

	public List<SelectItem> listaTipoNormaPenal() {
		NormaPenalListaComboPesquisa listaCombo = new NormaPenalListaComboPesquisa(super.getEntityManager());
		listaCombo.setInstrucaoHql("select tnp from TipoNormaPenal tnp where tnp.inAtivo = true ");
		listaCombo.executar();
		return listaCombo.resultado();
	}

	@Override
	protected boolean beforePersistOrUpdate() {
		if (verificaDataVigenciaValida()) {
			NormaPenalVerificarPreCondicoes preCondicoes = criarVerificarPreCondicoes();

			preCondicoes.executar();
			preCondicoes.imprimirMensagemErroSePreciso();

			if (preCondicoes.resultadoValido()) {
				return super.beforePersistOrUpdate();
			} else {
				return false;
			}
		} else {
			String mensagem = Messages.instance().get("normaPenal.dataInvalida");
			FacesMessages.instance().add(StatusMessage.Severity.INFO, mensagem);

			return false;
		}
	}

	/**
	 * Metodo para validar a Data de Vigência Inicial em comparação com a Data
	 * de Vigência Final
	 * 
	 * PJE_UC001 - RN(não informado)
	 * 
	 * @return retorna true se a data for anterior e false se for posterior
	 *         (inválida)
	 */
	private boolean verificaDataVigenciaValida() {
		Date dataVigenciaInicial = getInstance().getDataInicioVigencia();

		if (getInstance().getDataFimVigencia() != null) {
			Date dataVigenciaFinal = getInstance().getDataFimVigencia();

			if (dataVigenciaInicial.compareTo(dataVigenciaFinal) <= 0) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}

	}

	protected NormaPenalVerificarPreCondicoes criarVerificarPreCondicoes() {
		EntityManager em = super.getEntityManager();
		NormaPenal np = super.getInstance();

		return new NormaPenalVerificarPreCondicoes(em, np);
	}

	public TipoNormaPenal recarregarTipoNormaPenal(Integer id) {
		StringBuilder sb = new StringBuilder();
		sb.append("select tnp from TipoNormaPenal tnp where tnp.id = :id ");

		Query q = getEntityManager().createQuery(sb.toString());
		q.setParameter("id", id);

		return (TipoNormaPenal) q.getSingleResult();
	}

	@Override
	protected String afterPersistOrUpdate(String ret) {

		refreshGrid("normaPenalGrid");
		String retorno = super.afterPersistOrUpdate(ret);
		return retorno;
	}

	class NormaPenalVerificarPreCondicoes {

		private NormaPenal normaPenal;

		private boolean haRegistroNaMesmaVigencia = true;
		private boolean semArtigoAssociado = true;

		private EntityManager entityManager;

		public NormaPenalVerificarPreCondicoes(EntityManager entityManager, NormaPenal normaPenal) {
			this.entityManager = entityManager;
			this.normaPenal = normaPenal;
		}

		public void executar() {
			haRegistroNaMesmaVigencia = haRegistroNaMesmaVigencia();
			semArtigoAssociado = verificarSeHaNoMinimoUmArtigo() == false;

		}

		public boolean resultadoValido() {
			return haRegistroNaMesmaVigencia == false && semArtigoAssociado == false;
		}

		public void imprimirMensagemErroSePreciso() {
			if (haRegistroNaMesmaVigencia) {
				String mensagem = Messages.instance().get("normaPenal.jaCadastradaNoPeriodo");
				imprimirMensagem(mensagem);
			}
		}

		public void imprimirMensagem(String mensagem) {
			FacesMessages.instance().add(StatusMessage.Severity.INFO, mensagem);
		}

		protected boolean haRegistroNaMesmaVigencia() {
			String sql = sqlBuscarRegistroDiferenteNaMesmaVigencia();
			Query q = this.entityManager.createQuery(sql);
			boolean haRegistroNaMesmaVigencia = q.getResultList().isEmpty() == false;

			return haRegistroNaMesmaVigencia;
		}

		public String sqlBuscarRegistroDiferenteNaMesmaVigencia() {
			StringBuilder sql = new StringBuilder();
			sql.append(" select np from NormaPenal np ");
			sql.append(" where  ");
			sql.append("       np.ativo = true ");
			sql.append(condicaoParaNaoCompararComProprioRegistro());
			sql.append("   and np.nrNorma = '").append(normaPenal.getNrNorma()).append("' ");
			sql.append("   and np.tipoNormaPenal.id = ").append(normaPenal.getTipoNormaPenal().getId()).append(" ");
			sql.append(condicaoParaCompararVigencia());

			return sql.toString();
		}

		private String condicaoParaNaoCompararComProprioRegistro() {
			Integer idNormaPenal = normaPenal.getIdNormaPenal();
			return "   and np.idNormaPenal <> " + idNormaPenal;
		}

		private String condicaoParaCompararVigencia() {
			StringBuilder sql = new StringBuilder();
			String dataInicioVigenciaNoNovoRegistro = getDataInicioVigenciaNoNovoRegistro();
			String dataFimVigenciaNoNovoRegistro = getDataFimVigenciaNoNovoRegistro();
			sql.append("   and ( ");
			sql.append("   	  np.dataFimVigencia is null  "); // data final da
																// vigência
																// preenchida
			sql.append("   	  or  ");
			sql.append("      np.dataFimVigencia >= to_date('");
			sql.append(dataInicioVigenciaNoNovoRegistro).append("','dd/mm/yyyy') "); // data
																						// de
																						// início
																						// da
																						// vigência
																						// não
																						// pode
																						// ser
																						// superior
																						// à
																						// data
																						// do
																						// fim

			// Ajuste para segunda lista de correção

			if ((!dataFimVigenciaNoNovoRegistro.equals("") ) || (dataFimVigenciaNoNovoRegistro != null)) {
				sql.append("   	  or  ");
				sql.append("      np.dataFimVigencia >= to_date('");
				sql.append(dataInicioVigenciaNoNovoRegistro).append("','dd/mm/yyyy') ");
				sql.append("   	  )  ");
			}

			return sql.toString();
		}

		private String getDataFimVigenciaNoNovoRegistro() {
			Format formatter = new SimpleDateFormat("dd/MM/yyyy");
			if (normaPenal.getDataFimVigencia() != null) {
				Date dataInicioVigencia = normaPenal.getDataFimVigencia();
				String dataInicioVigenciaAsString = formatter.format(dataInicioVigencia);
				return dataInicioVigenciaAsString;
			}
			return "";
		}

		protected String getDataInicioVigenciaNoNovoRegistro() {
			Format formatter = new SimpleDateFormat("dd/MM/yyyy");
			Date dataInicioVigencia = normaPenal.getDataInicioVigencia();
			String dataInicioVigenciaAsString = formatter.format(dataInicioVigencia);
			return dataInicioVigenciaAsString;
		}

		private boolean verificarSeHaNoMinimoUmArtigo() {
			return true; // ANTES INTEGRAÇÃO COM UC 002 - DISPOSITIVO NORMA
		}
	}

	class NormaPenalListaComboPesquisa {

		List<SelectItem> itensParaExibicao;
		List<TipoNormaPenal> itensDaBase;
		EntityManager entityManager;
		String hql = "";

		public NormaPenalListaComboPesquisa(EntityManager entityManager) {
			this.entityManager = entityManager;
			inicializar();
		}

		private void inicializar() {
			itensParaExibicao = new ArrayList<SelectItem>();
			itensParaExibicao.add(new SelectItem(0, "Selecione"));
		}

		public void setInstrucaoHql(String hql) {
			this.hql = hql;
		}

		public void executar() {
			if (this.hql.isEmpty() == false) {
				lerRegistrosDaBase();
				preencherExibicao();
			}
		}

		public List<SelectItem> resultado() {
			return getItensParaExibicao();
		}

		@SuppressWarnings("unchecked")
		private void lerRegistrosDaBase() {
			StringBuilder sb = new StringBuilder();
			sb.append(hql);
			Query q = this.entityManager.createQuery(sb.toString());
			itensDaBase = q.getResultList();
		}

		private void preencherExibicao() {
			for (TipoNormaPenal t : itensDaBase) {
				adicioneItemNaExibicao(t);
			}
		}

		private void adicioneItemNaExibicao(TipoNormaPenal t) {
			long id = t.getId();
			String descricao = t.getDescricao();
			// SelectItem novoItemCombo = new SelectItem(id, id + " - " +
			// descricao);
			SelectItem novoItemCombo = new SelectItem(id, descricao);
			itensParaExibicao.add(novoItemCombo);
		}

		private List<SelectItem> getItensParaExibicao() {
			return itensParaExibicao;
		}
	}

}