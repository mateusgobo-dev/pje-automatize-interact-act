package br.com.infox.cliente.home.icrrefactory;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import br.jus.pje.nucleo.entidades.DispositivoNorma;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.NormaPenal;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipificacaoDelito;
import br.jus.pje.nucleo.util.DateUtil;

public abstract class IcrAssociarTipificacaoDelitoManager<T extends InformacaoCriminalRelevante> extends
		IcrAssociarTransitoEmJulgadoManager<T> {

	public static final String TIPO_ITEM_PESQUISA_NORMA_PENAL_NORMA = "N";
	public static final String TIPO_ITEM_IDENTIFICADOR_DISPOSITIVO = "I";
	public static final String TIPO_ITEM_PESQUISA_NORMA_PENAL_TEXTO = "T";

	@Override
	protected void doPersist(T entity) throws IcrValidationException {
		validarPreenchimentoTipificacoes(entity);
		super.doPersist(entity);
	};

	public void validarPreenchimentoTipificacoes(T entity) throws IcrValidationException {
		if (exigeTipificacaoDelito(entity)) {
			for (TipificacaoDelito item : entity.getTipificacoes()) {
				validarTipificacaoDelito(item, entity);
			}
			if (entity.getTipificacoes().isEmpty()) {
				throw new IcrValidationException("tipificacaoDelito.tipificacoes_nao_informadas");
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<ProcessoParte> recuperarReusComTipificacaoCadastradaList(ProcessoTrf processoTrf) {
		Query query = getEntityManager().createQuery(
				"select distinct td.informacaoCriminalRelevante.processoParte " + "from TipificacaoDelito td "
						+ "where td.informacaoCriminalRelevante.processoParte.processoTrf.idProcessoTrf = ? "
						+ "and td.informacaoCriminalRelevante.ativo = true");

		query.setParameter(1, processoTrf.getIdProcessoTrf());
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<T> recuperarIcrCopiaList(ProcessoParte processoParte) {
		String queryString = "select distinct o.informacaoCriminalRelevante from TipificacaoDelito o where "
				+ " o.informacaoCriminalRelevante.processoParte.idProcessoParte = ? "
				+ " and o.informacaoCriminalRelevante.ativo = true order by o.informacaoCriminalRelevante.data desc";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter(1, processoParte.getIdProcessoParte());
		
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public T recuperarIcrUltimaTipificacao(ProcessoParte processoParte) {
		// última tipificacao cadastrada
		String queryString = "from TipificacaoDelito td "
				+ " where td.informacaoCriminalRelevante.processoParte.idProcessoParte = ? "
				+ " and td.informacaoCriminalRelevante.ativo = true order by td.id desc";

		Query query = getEntityManager().createQuery(queryString);
		query.setParameter(1, processoParte.getIdProcessoParte());

		List<TipificacaoDelito> tipificacoesCadastadas = query.getResultList();

		if (!tipificacoesCadastadas.isEmpty()) {
			return (T) tipificacoesCadastadas.get(0).getInformacaoCriminalRelevante();
		} else {
			return null;
		}
	}

	public void validarTipificacaoDelito(TipificacaoDelito tipificacaoDelito, T owner) throws IcrValidationException {
		try {
			if (tipificacaoDelito.getDelito() == null || tipificacaoDelito.getDelito().isEmpty()) {
				throw new IcrValidationException("tipificacaoDelito.delitos_nulo");
			}
			if (!tipificacaoDelito.getDataDesconhecida()
					&& tipificacaoDelito.getDataDelito().after(DateUtil.getDataAtual())) {
				throw new IcrValidationException("tipificacaoDelito.data_delito_superior_data_atual");
			}
			for (TipificacaoDelito item : owner.getTipificacoes()) {
				// item igual mas outra instancia
				if (item.equals(tipificacaoDelito) && item != tipificacaoDelito) {
					throw new IcrValidationException("tipificacaoDelito.delito_ja_cadastrado");
				}
			}

			
			Date dataInicioVigenciaNorma = tipificacaoDelito.getDelito().get(0).getNormaPenal().getDataInicioVigencia();
			Date dataFimVigenciaNorma = tipificacaoDelito.getDelito().get(0).getNormaPenal().getDataFimVigencia();
			Date dataDelito = tipificacaoDelito.getDataDelito();
			Calendar calendar = Calendar.getInstance();
			calendar.set(3000, 1, 1, 0, 0, 0);
			Date dataFutura = calendar.getTime();
			dataFimVigenciaNorma = dataFimVigenciaNorma == null ? dataFutura : dataFimVigenciaNorma;

			if (dataDelito != null
					&& !(DateUtil.isDataMaiorIgual(dataDelito, dataInicioVigenciaNorma) && DateUtil.isDataMenorIgual(
							dataDelito, dataFimVigenciaNorma))) {
				throw new IcrValidationException("tipificacaoDelito.data_do_delito_fora_do_periodo_de_vigencia_norma");
			}

		} catch (IcrValidationException e) {
			if (getEntityManager().contains(tipificacaoDelito)) {
				getEntityManager().refresh(tipificacaoDelito);
			}
			throw e;
		}
	}

	public void validarAdicaoNormaExtencao(DispositivoNorma normaCandidata, TipificacaoDelito tipificacaoDelito)
			throws IcrValidationException {

		for (DispositivoNorma combinacao : tipificacaoDelito.getCombinacoes()) {
			// tratar itens no mesmo nível hierarquico
			if ((normaCandidata.getDispositivoNormaPai() != null && combinacao.getDispositivoNormaPai().equals(
					normaCandidata.getDispositivoNormaPai()))
					|| (normaCandidata.getDispositivoNormaPai() == null && combinacao.getDispositivoNormaPai() == null)) {
				if (!normaCandidata.getPermitirAssociacaoMultipla() || !combinacao.getPermitirAssociacaoMultipla()) {
					throw new IcrValidationException("tipificacaoDelito.item_nao_permite_associacao_multipla");

				}
			}
		}
	}

	protected boolean exigeTipificacaoDelito(T entity) {
		return entity.getTipo().exigeTipificacaoDelito();
	}

	@SuppressWarnings("unchecked")
	public List<NormaPenal> recuperarNormaPenalTreeRoots() {
		String queryString = "select distinct dn.normaPenal from DispositivoNorma " + "dn where dn.ativo = true ";
		Query query = getEntityManager().createQuery(queryString);
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<DispositivoNorma> recuperarDispositivoNormaRoots(NormaPenal normaPenal) {

		String queryString = "from DispositivoNorma dn " + "where dn.normaPenal.idNormaPenal = ? "
				+ "and dn.dispositivoNormaPai is null " + " and dn.ativo = true order by dn.numeroOrdem";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter(1, normaPenal.getIdNormaPenal());

		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<DispositivoNorma> recuperarDispositivoNormaChildren(DispositivoNorma dispositivoNormaPai) {
		String queryString = "from DispositivoNorma dn " + "where dn.dispositivoNormaPai.idDispositivoNorma = ? "
				+ "and dn.ativo = true order by dn.numeroOrdem";
		Query query = getEntityManager().createQuery(queryString);
		query.setParameter(1, dispositivoNormaPai.getIdDispositivoNorma());
		return query.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<DispositivoNorma> pesquisarDispositivos(String tipoItem, String criterio) {
		String queryString = "from DispositivoNorma dn where dn.ativo = true ";
		if (tipoItem.equals(TIPO_ITEM_IDENTIFICADOR_DISPOSITIVO)) {
			queryString += " and dn.dsIdentificador = '" + criterio + "'";
		} else if (tipoItem.equals(TIPO_ITEM_PESQUISA_NORMA_PENAL_TEXTO)) {
			queryString += " and to_ascii(lower(dn.dsTextoDispositivo)) like " + "concat('%', to_ascii(lower('"
					+ criterio + "')), '%')";
		} else {
			return null;
		}
		Query query = getEntityManager().createQuery(queryString);

		return query.getResultList();
	}
}
