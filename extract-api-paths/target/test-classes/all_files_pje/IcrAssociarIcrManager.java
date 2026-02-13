package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.List;
import br.jus.pje.nucleo.entidades.InformacaoCriminalRelevante;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoTrf;
import br.jus.pje.nucleo.entidades.TipoInformacaoCriminalRelevante.TipoIcrEnum;
import br.jus.pje.nucleo.util.DateUtil;

public abstract class IcrAssociarIcrManager<T extends InformacaoCriminalRelevante> extends
		InformacaoCriminalRelevanteManager<T>{

	protected TipoIcrEnum[] tiposTiposDeIcrAceitos = {TipoIcrEnum.SAP, TipoIcrEnum.SAI, TipoIcrEnum.SEP,
			TipoIcrEnum.SCO, TipoIcrEnum.SAS, TipoIcrEnum.SPR, TipoIcrEnum.SEI};

	/**
	 * para aplicar filtros customizados. É adicionado um and ao final da query em recuperarIcrPortipo com o conteúdo da string. Álias para a icr:
	 * 'icr'. ex: filtro[0]="icr not in(...)";
	 * 
	 * @return
	 */
	protected String[] getFiltrosIcr(){
		return null;
	}

	/**
	 * Tipos de icr que serão consideradas em getListaReusNoProcesso() e getListaIcrPorParteETipo(). ver RN90
	 * 
	 * Serão recuperados todos os réus do processo que possuem ICR de sentenças em 1º grau. São elas: Sentença Absolutória (SAP), Sentença Absolutória
	 * Imprópria(SAI), Sentença de Extinção da Punibilidade(SEP), Sentença Condenatória(SCO), Sentença de Absolvição Sumária do Júri(SAS) e Sentença
	 * de Pronúncia(SPR), Sentença de Impronúncia(SEI). Não devem ser listadas as ICRs que constarem ICRs de anulação vinculadas a elas.
	 * 
	 * @return
	 */
	protected abstract TipoIcrEnum[] getTiposDeIcrAceitos();

	// para o futuro:
	// protected abstract Class<? extends InformacaoCriminalRelevante>[]
	// getIcrAceitos();
	protected abstract InformacaoCriminalRelevante getIcrAfetada(T entity);

	@Override
	protected void prePersist(T entity) throws IcrValidationException{
		super.prePersist(entity);
		if (getIcrAfetada(entity) == null){
			throw new IcrValidationException("icrAssociarSentenca.sentencaNaoInformada");
		}
		if (DateUtil.isDataMaior(getIcrAfetada(entity).getData(), entity.getData())){
			throw new IcrValidationException(getMensagemDataIcrMenorQueDataSentenca());
		}
	}

	protected String getMensagemDataIcrMenorQueDataSentenca(){
		return "icrAssociarSentenca.dataDaIcrMaiorQueADaSentenca";
	}

	@Override
	public void validate(T entity) throws IcrValidationException{
		super.validate(entity);
		// para validar a unicidade da icr antes de abrir a aba de tipificação
		if (entity.getTipo().getExigeTipificacaoDelito())
			ensureUniqueness(entity);
	}

	@SuppressWarnings("unchecked")
	public List<InformacaoCriminalRelevante> recuperarIcrPortipo(ProcessoTrf processo, TipoIcrEnum[] tiposDeIcrAceitos){
		if (processo == null)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append(" select icr from InformacaoCriminalRelevante icr ");
		sb.append(" where icr.ativo = true 	");
		sb.append(" 	 and icr.processoParte.processoTrf = :processoTrf ");
		sb.append(" and icr not in(select distinct(o.icrAfetada) from IcrDecisaoSuperiorAnulacaoDeSentenca o where o.ativo=true )");
		if (tiposDeIcrAceitos != null && tiposDeIcrAceitos.length > 0){
			StringBuilder codigos = new StringBuilder();
			for (TipoIcrEnum tipo : tiposDeIcrAceitos){
				codigos.append("'");
				codigos.append(tipo.name());
				codigos.append("',");
			}
			codigos = new StringBuilder(codigos.substring(0, codigos.lastIndexOf(",")));
			sb.append(" and icr.tipo.codigo in(");
			sb.append(codigos.toString());
			sb.append(")");
		}
		if (getFiltrosIcr() != null && (getFiltrosIcr().length > 0)){
			for (String filtro : getFiltrosIcr()){
				sb.append(" and(" + filtro + ") ");
			}
		}
		sb.append(" order by icr.data desc ");
		return getEntityManager().createQuery(sb.toString()).setParameter("processoTrf", processo).getResultList();
	}

	public List<ProcessoParte> recuperarReusNoProcesso(ProcessoTrf processoTrf){
		List<ProcessoParte> result = new ArrayList<ProcessoParte>();
		List<InformacaoCriminalRelevante> icrs = recuperarIcrPortipo(processoTrf, getTiposDeIcrAceitos());
		for (InformacaoCriminalRelevante icr : icrs){
			if (!result.contains(icr.getProcessoParte())){
				result.add(icr.getProcessoParte());
			}
		}
		return result;
	}

	public List<InformacaoCriminalRelevante> recuperarIcrPorParteEtipo(ProcessoParte pp){
		if (pp == null || pp.getProcessoTrf() == null)
			return null;
		List<InformacaoCriminalRelevante> result = new ArrayList<InformacaoCriminalRelevante>();
		List<InformacaoCriminalRelevante> temp = recuperarIcrPortipo(pp.getProcessoTrf(), getTiposDeIcrAceitos());
		for (InformacaoCriminalRelevante icr : temp){
			if (icr.getProcessoParte().equals(pp)){
				result.add(icr);
			}
		}
		return result;
	}

	protected List<InformacaoCriminalRelevante> filtraTiposAceitos(List<InformacaoCriminalRelevante> icrs,
			TipoIcrEnum[] tiposDeIcrAceitos){
		List<InformacaoCriminalRelevante> retorno = new ArrayList<InformacaoCriminalRelevante>(0);
		for (InformacaoCriminalRelevante icr : icrs){
			for (TipoIcrEnum tipo : tiposDeIcrAceitos){
				if (verificaTipo(icr, tipo)){
					if (!retorno.contains(icr))
						retorno.add(icr);
				}
			}
		}
		return retorno;
	}

	protected boolean verificaTipo(InformacaoCriminalRelevante icr, TipoIcrEnum tipo){
		return tipo.name().equals(icr.getTipo().getCodigo());
	}
}
