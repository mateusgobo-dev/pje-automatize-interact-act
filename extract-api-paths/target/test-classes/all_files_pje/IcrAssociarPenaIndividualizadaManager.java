package br.com.infox.cliente.home.icrrefactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Query;
import br.jus.pje.nucleo.entidades.IcrSentencaCondenatoria;
import br.jus.pje.nucleo.entidades.Pena;
import br.jus.pje.nucleo.entidades.PenaIndividualizada;
import br.jus.pje.nucleo.entidades.TipoPena;
import br.jus.pje.nucleo.entidades.UnidadeMonetaria;
import br.jus.pje.nucleo.enums.GeneroPenaEnum;

public class IcrAssociarPenaIndividualizadaManager<T extends IcrSentencaCondenatoria> extends
		InformacaoCriminalRelevanteManager<T>{

	@SuppressWarnings("unchecked")
	public List<TipoPena> recuperarTiposPena(GeneroPenaEnum generoPena){
		String hql = " select o from TipoPena o " + " where o.generoPena = ? " + " and o.ativo = true ";

		Query qry = getEntityManager().createQuery(hql);
		qry.setParameter(1, generoPena);

		return qry.getResultList();
	}

	public List<GeneroPenaEnum> recuperarGeneros(boolean penaSubstitutiva){
		List<GeneroPenaEnum> generos = Arrays.asList(GeneroPenaEnum.values());
		// se pena substitutiva, manter somente os GENEROS Restritiva de Direito
		// e Multa
		if (penaSubstitutiva){
			generos = new ArrayList<GeneroPenaEnum>();
			generos.add(GeneroPenaEnum.MU);
			generos.add(GeneroPenaEnum.RD);
		}
		return generos;
	}

	@Override
	protected void prePersist(T entity) throws IcrValidationException{
		super.prePersist(entity);
		validarPenasIndividualizadas(entity);

		for (Pena pena : entity.getPenas()){
			pena.setIcrSentencaCondenatoria(entity);
			for (Pena penaSubstitutiva : pena.getPenasSubstitutivas()){
				penaSubstitutiva.setIcrSentencaCondenatoria(entity);
			}
		}

	};

	public void validarPenasIndividualizadas(T entity) throws IcrValidationException{
		validarPenas(entity.getPenaIndividualizadaList());
	}

	public void validarPena(Pena pena) throws IcrValidationException{
		
		//valida as informações de tempo da pena
		if (pena.getTipoPena().getInTempoAno()
			|| pena.getTipoPena().getInTempoMes()
			|| pena.getTipoPena().getInTempoDia()
			|| pena.getTipoPena().getInTempoHoras()){

			if ((pena.getAnosPenaInicial() == null || pena.getAnosPenaInicial() == 0)
				&& (pena.getMesesPenaInicial() == null || pena.getMesesPenaInicial() == 0)
				&& (pena.getDiasPenaInicial() == null || pena.getDiasPenaInicial() == 0)
				&& (pena.getHorasPenaInicial() == null || pena.getHorasPenaInicial() == 0)){
				boolean throwErr = true;
				if (pena instanceof PenaIndividualizada){
					PenaIndividualizada penaIndividualizada = (PenaIndividualizada) pena;
					throwErr = (penaIndividualizada.getAnosPenaAcrescimo() == null || penaIndividualizada.getAnosPenaAcrescimo() == 0)
						&& (penaIndividualizada.getMesesPenaAcrescimo() == null || penaIndividualizada.getMesesPenaAcrescimo() == 0)
						&& (penaIndividualizada.getDiasPenaAcrescimo() == null || penaIndividualizada.getDiasPenaAcrescimo() == 0)
						&& (penaIndividualizada.getHorasPenaAcrescimo() == null || penaIndividualizada.getHorasPenaAcrescimo() == 0);
				}

				if (throwErr){
					throw new IcrValidationException("A Pena deve conter ao menos um item de informação de tempo preenchido!");
				}
			}
		}
	}

	protected void validarPenas(List<? extends Pena> penas) throws IcrValidationException{
		for (Pena pena : penas){

			if (pena.getTipoPena().getGeneroPena() == GeneroPenaEnum.PL){
				validarPena(pena);
				if (pena.getPenasSubstitutivas().size() > 2){
					throw new IcrValidationException("Não é possível cadastrar mais de duas Penas Substitutivas");
				}
				else if (pena.getPenasSubstitutivas().size() == 2){
					int qtdMultas = 0;
					TipoPena tipoPena = new TipoPena();

					for (Pena penaAlternativa : pena.getPenasSubstitutivas()){
						if (penaAlternativa.getTipoPena().getGeneroPena() == GeneroPenaEnum.MU){
							qtdMultas++;
						}
						if (penaAlternativa.getTipoPena().getGeneroPena() == GeneroPenaEnum.RD){
							if (penaAlternativa.getTipoPena().equals(tipoPena)){
								throw new IcrValidationException(
										"Não é possível cadastrar mais de uma Pena Substitutiva do Gênero "
											+ "Restritiva de Direito para a mesma Espécie da Pena ("
											+ penaAlternativa.getTipoPena().getDsTipoPena() + ")");
							}
							tipoPena = penaAlternativa.getTipoPena();
						}
					}

					if (qtdMultas > 1){
						throw new IcrValidationException(
								"Não é possível cadastrar mais de uma Pena Substitutiva do Gênero Multa");
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<UnidadeMonetaria> recuperarUnidadesMonetarias(){
		Query query = getEntityManager().createQuery("from UnidadeMonetaria o");
		return query.getResultList();
	}
}
