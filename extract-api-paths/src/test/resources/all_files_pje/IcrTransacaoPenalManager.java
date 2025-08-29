package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.jboss.seam.annotations.Name;

import br.jus.pje.nucleo.entidades.AcompanhamentoCondicaoTransacaoPenal;
import br.jus.pje.nucleo.entidades.CondicaoIcrTransacaoPenal;
import br.jus.pje.nucleo.entidades.IcrTransacaoPenal;
import br.jus.pje.nucleo.entidades.TipoPena;
import br.jus.pje.nucleo.enums.GeneroPenaEnum;
import br.jus.pje.nucleo.enums.SituacaoAcompanhamentoIcrTransacaoPenalEnum;
import br.jus.pje.nucleo.enums.UnidadeMultaEnum;

@Name("icrTRPManager")
public class IcrTransacaoPenalManager extends InformacaoCriminalRelevanteManager<IcrTransacaoPenal> {
	@Override
	protected void prePersist(IcrTransacaoPenal entity) throws IcrValidationException {
		super.prePersist(entity);
		if (entity.getCondicaoIcrTransacaoList().isEmpty()) {
			throw new IcrValidationException("Adicione, no mínimo, uma condição");
		}
		if (entity.getId() != null && verificaEncerramento(entity)) {
			throw new IcrValidationException("icrTransacaoPenal.erroTransacaoEncerrada");
		}
		if (entity.getId() != null && verificaSuspensao(entity)) {
			throw new IcrValidationException("icrTransacaoPenal.erroTransacaoSuspensa");
		}
		if (entity.getId() == null && possuiTransacaoComAcompNaoCumpridos(entity)) {
			throw new IcrValidationException("icrTransacaoPenal.erroTransacaoAcompNaoCumprido");
		}
		for (CondicaoIcrTransacaoPenal condicao : entity.getCondicaoIcrTransacaoList()) {
			validarCondicao(condicao);
			if (condicao.getTipoPena() != null && !condicao.getTipoPena().getGeneroPena().equals(GeneroPenaEnum.MU)) {
				condicao.setUnidadeMulta(null);
			}
			condicao.setIcrTransacaoPenal(entity);
			Collections.sort(condicao.getAcompanhamentos());
			/*
			 * Se estiver inserindo uma condicao, as tarefas terao como numero
			 * de sequencia o proprio indice da lista
			 */
			if (condicao.getId() == null) {
				for (int i = 0; i < condicao.getAcompanhamentos().size(); i++) {
					if (condicao.getAcompanhamentos().get(i).getNumeroSequencia() == null) {
						condicao.getAcompanhamentos().get(i).setNumeroSequencia(i + 1);
					}
					// validando as tarefas
					validarAcompanhamento(condicao.getAcompanhamentos().get(i));
				}
			} else {
				/*
				 * senao, busca o proximo numero do banco de dados e aplica nas
				 * tarefas novas, incrementando-o a cada aplicacao
				 */
				if (condicao.getAcompanhamentos() != null && !condicao.getAcompanhamentos().isEmpty()) {
					Integer proximo = this.proximoNumeroTarefa(condicao);
					for (int i = 0; i < condicao.getAcompanhamentos().size(); i++) {
						if (condicao.getAcompanhamentos().get(i).getNumeroSequencia() == null) {
							condicao.getAcompanhamentos().get(i).setNumeroSequencia(proximo);
							proximo = proximo + 1;
						}
						// validando as tarefas
						validarAcompanhamento(condicao.getAcompanhamentos().get(i));
					}
				}
			}
		}
	}

	@Override
	protected void preInactive(IcrTransacaoPenal entity) throws IcrValidationException {
		if (verificaEncerramento(entity)) {
			throw new IcrValidationException("icrTransacaoPenal.erroTransacaoEncerrada");
		}
		if (verificaSuspensao(entity)) {
			throw new IcrValidationException("icrTransacaoPenal.erroTransacaoSuspensa");
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	private boolean possuiTransacaoComAcompNaoCumpridos(IcrTransacaoPenal entity) {
		List<IcrTransacaoPenal> listaTransacaoesPenais = (List<IcrTransacaoPenal>) getEntityManager()
				.createQuery(
						" SELECT tp FROM IcrTransacaoPenal tp WHERE " +
						" tp.ativo = true AND" +
						" tp.processoParte. processoTrf.idProcessoTrf = :processoTRF")
				.setParameter("processoTRF", InformacaoCriminalRelevanteHome.getHomeInstance().getProcessoTrf().getIdProcessoTrf())
				.getResultList();
		
		if(!listaTransacaoesPenais.isEmpty()) {
			for (IcrTransacaoPenal tp : listaTransacaoesPenais){
				for(CondicaoIcrTransacaoPenal condicao : tp.getCondicaoIcrTransacaoList()) {
					for(AcompanhamentoCondicaoTransacaoPenal acompanhamento : condicao.getAcompanhamentos()) {
						if(!acompanhamento.getCondicaoIcrTransacaoPenal().getSituacaoAcompanhamentoIcrTransacao()
								.equals(SituacaoAcompanhamentoIcrTransacaoPenalEnum.CUMPR)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private boolean verificaSuspensao(IcrTransacaoPenal entity) {
		return !getEntityManager()
				.createQuery(
						"select o from IcrSuspensaoTransacaoPenal o where o.ativo=true and o.transacaoPenal = :transacao")
				.setParameter("transacao", entity).getResultList().isEmpty();
	}

	private boolean verificaEncerramento(IcrTransacaoPenal entity) {
		return !getEntityManager()
				.createQuery(
						"select o from IcrEncerramentoDeTransacaoPenal o where o.ativo=true and o.transacaoPenal = :transacao")
				.setParameter("transacao", entity).getResultList().isEmpty();
	}

	@SuppressWarnings("unchecked")
	public List<TipoPena> recuperarTiposCondicoes(GeneroPenaEnum generoPena) {
		List<TipoPena> returnValue = new ArrayList<TipoPena>(0);
		if (generoPena != null) {
			String hql = " select o from TipoPena o " + " where o.generoPena = :generoPena " + " and o.ativo = true ";
			Query qry = getEntityManager().createQuery(hql);
			qry.setParameter("generoPena", generoPena);
			returnValue = qry.getResultList();
		}
		return returnValue;
	}
	
	public TipoPena getTipoPena(CondicaoIcrTransacaoPenal condicaoTransacaoPenal) {
		return (TipoPena) getEntityManager().createQuery("Select o.tipoPena From CondicaoIcrTransacaoPenal o Where o.id = " + condicaoTransacaoPenal.getId()).getSingleResult();
	}

	public void validarCondicao(CondicaoIcrTransacaoPenal condicaoTransacaoPenal) throws IcrValidationException {
		if (condicaoTransacaoPenal.getTipoPena() == null) condicaoTransacaoPenal.setTipoPena(getTipoPena(condicaoTransacaoPenal));
		if (condicaoTransacaoPenal.getTipoPena().getGeneroPena().equals(GeneroPenaEnum.MU)
				&& condicaoTransacaoPenal.getTipoPena().getInQuantidadeDiasMulta()
				&& condicaoTransacaoPenal.getUnidadeMulta().equals(UnidadeMultaEnum.DI)) {
			if (condicaoTransacaoPenal.getDiasMulta() == null) {
				throw new IcrValidationException("Informe a quantidade de dias da multa");
			} else if (condicaoTransacaoPenal.getValorFracaoDiaMultaSalarioMinimo() == null) {
				throw new IcrValidationException("Informe o valor do dia multa em frações do salário mínimo");
			} else if (condicaoTransacaoPenal.getMultiplicadorPena() == null) {
				throw new IcrValidationException("Informe o multiplicador");
			} else if (condicaoTransacaoPenal.getValorHistoricoPrevisto() == null) {
				throw new IcrValidationException("Informe o valor histórico previsto");
			}
		} else if (condicaoTransacaoPenal.getTipoPena().getGeneroPena().equals(GeneroPenaEnum.MU)
				&& condicaoTransacaoPenal.getTipoPena().getInValor()
				&& condicaoTransacaoPenal.getUnidadeMulta().equals(UnidadeMultaEnum.VA)) {
			if (condicaoTransacaoPenal.getValorMulta() == null) {
				throw new IcrValidationException("Informe o valor da penal");
			} else if (condicaoTransacaoPenal.getUnidadeMonetaria() == null) {
				throw new IcrValidationException("Informe a unidade monetária");
			}
		} else if (condicaoTransacaoPenal.getTipoPena().getInDescricaoBem()) {
			if (condicaoTransacaoPenal.getDescricaoBem() == null) {
				throw new IcrValidationException("Informe a descrição do bem");
			}
		} else if (condicaoTransacaoPenal.getTipoPena().getInDescricaoLocal()) {
			if (condicaoTransacaoPenal.getDescricaoLocal() == null) {
				throw new IcrValidationException("Informe a descrição do local");
			}
		} else if (condicaoTransacaoPenal.getTipoPena().getInTempoAno()) {
			if (condicaoTransacaoPenal.getQuantidadeAnoPena() == null) {
				throw new IcrValidationException("Informe a quantidade de anos");
			}
		} else if (condicaoTransacaoPenal.getTipoPena().getInTempoMes()) {
			if (condicaoTransacaoPenal.getQuantidadeMesPena() == null) {
				throw new IcrValidationException("Informe a quantidade de meses");
			}
		} else if (condicaoTransacaoPenal.getTipoPena().getInTempoDia()) {
			if (condicaoTransacaoPenal.getQuantidadeDiasPena() == null) {
				throw new IcrValidationException("Informe a quantidade de dias");
			}
		} else if (condicaoTransacaoPenal.getTipoPena().getInTempoHoras()) {
			if (condicaoTransacaoPenal.getQuantidadeHorasPena() == null) {
				throw new IcrValidationException("Informe a quantidade de horas");
			}
		}		
	}

	public boolean existeCondicaoMesmoTipoPena(IcrTransacaoPenal icr) {
		if (icr.getCondicaoIcrTransacaoList() != null) {
			for (CondicaoIcrTransacaoPenal aux : icr.getCondicaoIcrTransacaoList()) {
				for (CondicaoIcrTransacaoPenal aux2 : icr.getCondicaoIcrTransacaoList()) {
					if (!aux.equals(aux2)) {// nao comparar com ele mesmo
						return aux.getTipoPena().equals(aux2.getTipoPena());
					}
				}
			}
		}
		return false;
	}

	private void validarAcompanhamento(AcompanhamentoCondicaoTransacaoPenal acompanhamento)
			throws IcrValidationException {
		if (acompanhamento.getCondicaoIcrTransacaoPenal() == null) {
			throw new IcrValidationException("Informe a condição do acompanhamento");
		}
		if (acompanhamento.getDataPrevista() == null) {
			throw new IcrValidationException("Informe a data prevista para cumprimento do acompanhamento ");
		}
		if (acompanhamento.getNumeroSequencia() == null) {
			throw new IcrValidationException("Acompanhamento sem número de sequência da tarefa informado ");
		}
		// se a condição estiver cumprida, não permite a inserção de tarefas
		if (acompanhamento.getId() == null) {
			if (acompanhamento.getCondicaoIcrTransacaoPenal().getSituacaoAcompanhamentoIcrTransacao() != null
					&& acompanhamento.getCondicaoIcrTransacaoPenal().getSituacaoAcompanhamentoIcrTransacao()
							.equals(SituacaoAcompanhamentoIcrTransacaoPenalEnum.CUMPR)) {
				throw new IcrValidationException(
						"Não é permitido cadastrar novas tarefas quando a condição se encontra com situação cumprida.");
			}
		}
	}

	private Integer proximoNumeroTarefa(CondicaoIcrTransacaoPenal condicaoIcrTransacaoPenal) {
		String hql = " select max(o.numeroSequencia) from AcompanhamentoCondicaoTransacaoPenal o "
				+ " where o.condicaoIcrTransacaoPenal.id = ? ";
		Query qry = getEntityManager().createQuery(hql);
		qry.setParameter(1, condicaoIcrTransacaoPenal.getId());
		Integer max = (Integer) qry.getSingleResult();
		if (max != null) {
			return max + 1;
		}
		return 1;
	}

	@Override
	public Date getDtPublicacao(IcrTransacaoPenal entity) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean possuiDataPublicacao() {
		// TODO Auto-generated method stub
		return false;
	}
}
