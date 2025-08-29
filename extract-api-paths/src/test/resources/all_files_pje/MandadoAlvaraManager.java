package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.jus.cnj.pje.business.dao.MandadoAlvaraDAO;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.pje.nucleo.entidades.AssuntoTrf;
import br.jus.pje.nucleo.entidades.DispositivoNorma;
import br.jus.pje.nucleo.entidades.Evento;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.MandadoAlvara;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.ProcessoEvento;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipificacaoDelito;
import br.jus.pje.nucleo.enums.SituacaoExpedienteCriminalEnum;

public abstract class MandadoAlvaraManager<T extends MandadoAlvara, D extends MandadoAlvaraDAO<T>> extends
		ProcessoExpedienteCriminalManager<T, D>{

	@Override
	public T persist(T entity) throws br.jus.cnj.pje.nucleo.PJeBusinessException{
		if (entity.getAssuntoPrincipal() == null){
			throw new PJeBusinessException("pje.mandadoAlvaraManager.error.assuntoPrincipalNaoInformado");
		}

		if (entity.getDataDelito() == null && entity.getInDataDelitoDesconhecida() == null){
			throw new PJeBusinessException("pje.mandadoAlvaraManager.error.dataDelitoNaoInformada");
		}

		// RN154
		if (entity.getInDataDelitoDesconhecida() == null){
			entity.setDataDelito(null);
		}

		// RN165
		if (entity.getProcessoProcedimentoOrigemList() == null || entity.getProcessoProcedimentoOrigemList().isEmpty()){
			throw new PJeBusinessException("pje.mandadoAlvaraManager.error.procedimentoOrigemNaoInformado");
		}

		// RN153		
		if (entity.getInDataDelitoDesconhecida() == null){
			TipificacaoDelito tip = recuperarTipificacaoMaiorPena(entity.getPessoa(),entity.getProcessoTrf());	
		
			if(tip.getDataDelito().equals(entity.getDataDelito())){
				throw new PJeBusinessException("pje.mandadoAlvaraManager.error.dataDelitoDiferente");
			}
		}

		return super.persist(entity);
	};

	public List<AssuntoTrf> recuperarAssuntosUltimaTipificacao(PessoaFisica pessoaFisica, ProcessoTrf processoTrf)
			throws PJeBusinessException{
		try{
			InformacaoCriminalRelevante icr = getDAO().recuperarUltimaIcrComDelitos(pessoaFisica, processoTrf);
			if(icr != null){
				List<AssuntoTrf> assuntos = new ArrayList<AssuntoTrf>(0);
				for(TipificacaoDelito tip : icr.getTipificacoes()){
					for(DispositivoNorma dis : tip.getDelito()){
						if(!assuntos.contains(dis.getAssuntoTrf())){
							assuntos.add(dis.getAssuntoTrf());
						}
					}
				}
				return assuntos;
			}
			
			return null;
		} catch (PJeDAOException e){
			throw new PJeBusinessException(e);
		}
	}

	public TipificacaoDelito recuperarTipificacaoMaiorPena(Pessoa pessoa, ProcessoTrf processoTrf){
		InformacaoCriminalRelevante icr = getDAO().recuperarUltimaIcrComDelitos(pessoa, processoTrf);
		if (icr != null){
			TipificacaoDelito tpMaior = null;
			int maior = 0;
			for (TipificacaoDelito aux : icr.getTipificacoes()){
				int pena = 0;
				for(DispositivoNorma delito : aux.getDelito()){
					pena += delito.getNrPenaMaximaAnos()
					+ (delito.getNrPenaMaximaMeses() / 12)
					+ (delito.getNrPenaMaximaDias() / 360);
				}
				if (pena > maior){
					maior = pena;
					tpMaior = aux;
				}
			}
	
			return tpMaior;
		}
		return null;
	}
	
	public DispositivoNorma recuperarDelitoMaiorPena(Pessoa pessoa, ProcessoTrf processoTrf){
		TipificacaoDelito tip = recuperarTipificacaoMaiorPena(pessoa, processoTrf);
		if(tip != null){
			DispositivoNorma disMaior = null;
			int maior = 0;
			int pena = 0;
			for(DispositivoNorma dis : tip.getDelito()){
				pena = 	dis.getNrPenaMaximaAnos()
						+ (dis.getNrPenaMaximaMeses() / 12)
						+ (dis.getNrPenaMaximaDias() / 360);
				if (pena > maior){
					maior = pena;
					disMaior = dis;
				}
				pena = 0;
			}
			return disMaior;
		}
		
		return null;
	}

	public T gravarCumprimento(T mandadoAlvara) throws PJeBusinessException{
		if (mandadoAlvara.getDataCumprimento() == null){
			throw new PJeBusinessException("pje.mandadoAlvaraManager.error.dataCumprimentoNaoInformada");
		}

		if (mandadoAlvara.getProcessoEventoList() == null || mandadoAlvara.getProcessoEventoList().isEmpty()){
			throw new PJeBusinessException("pje.mandadoAlvaraManager.error.movimentacoesNaoInformadas");
		}

		if (mandadoAlvara.getSituacaoExpedienteCriminal() == SituacaoExpedienteCriminalEnum.CP){
			throw new PJeBusinessException("pje.mandadoAlvaraManager.error.expedienteJaCumprido");
		}

		if (mandadoAlvara.getSituacaoExpedienteCriminal() != SituacaoExpedienteCriminalEnum.PC){
			throw new PJeBusinessException("pje.mandadoAlvaraManager.error.situacaoDiferentePendenteCumprimento",null,mandadoAlvara.getSituacaoExpedienteCriminal().getLabel());
		}

		setarSituacaoExpediente(mandadoAlvara);

		/*
		 * não se pode chamar o persit da classe,
		 * pois existem regras de negócio que
		 * não devem ser aplicadas no cumprimento
		 * ex: dt validade da prisão não pode ser menor
		 * que a dt atual
		 */
		return super.persist(mandadoAlvara);
	}

	public List<ProcessoEvento> getMovimentacoesNaoVinculadas(MandadoAlvara mandadoAlvara, Date dataInicio, Date dataFim,
			Evento movimentacaoSelecionada){
		return getDAO().getMovimentacoesNaoVinculadas(mandadoAlvara, dataInicio, dataFim, movimentacaoSelecionada);
	}
}
