package br.com.itx.util;

import java.util.List;

import org.jboss.seam.Component;

import br.jus.cnj.pje.nucleo.Papeis;
import br.jus.cnj.pje.nucleo.service.PapelService;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaFisicaEspecializada;
import br.jus.pje.nucleo.entidades.PessoaJuridica;
import br.jus.pje.nucleo.entidades.Procuradoria;
import br.jus.pje.nucleo.entidades.identidade.Papel;

public final class LocalizacaoUtil {
	
	public static final int TAMANHO_MAXIMO_PESSOA_FISICA = 83;
  	public static final int TAMANHO_MAXIMO_PESSOA_JURIDICA = 79;
  	public static final int TAMANHO_MAXIMO_PROCURADORIA = 85;
  	public static final int TAMANHO_MAXIMO_PROCURADOR = 70;
	
  	public static String formataLocalizacaoPessoa(Pessoa pessoa) {
  		if (pessoa instanceof PessoaFisica) {
  			PessoaFisica pessoaFisica = (PessoaFisica)pessoa;
  			if(pessoaFisica.getNome().length() >= TAMANHO_MAXIMO_PESSOA_FISICA) {
  				return pessoaFisica.getNome().substring(0,TAMANHO_MAXIMO_PESSOA_FISICA) + " (" + pessoaFisica.getNumeroCPF() + ")";
  			} else {
  				return pessoaFisica.getNome() + " (" + pessoaFisica.getNumeroCPF() + ")";
  			}
  		} else {
  			PessoaJuridica pessoaJuridica = (PessoaJuridica)pessoa;
  			if(pessoaJuridica.getNome().length() >= TAMANHO_MAXIMO_PESSOA_JURIDICA) {
  				return pessoaJuridica.getNome().substring(0,TAMANHO_MAXIMO_PESSOA_JURIDICA) + " (" + pessoaJuridica.getNumeroCNPJ() + ")";
  			} else {
  				return pessoaJuridica.getNome() + " (" + pessoaJuridica.getNumeroCNPJ() + ")";
  			}
  		}
	}
  	
  	public static String formataLocalizacaoPessoaFisica(PessoaFisica pessoaFisica){
		
		if(pessoaFisica.getNome().length() >= TAMANHO_MAXIMO_PESSOA_FISICA)
			return pessoaFisica.getNome().substring(0,TAMANHO_MAXIMO_PESSOA_FISICA) + " (" + pessoaFisica.getNumeroCPF() + ")";
		else
			return pessoaFisica.getNome() + " (" + pessoaFisica.getNumeroCPF() + ")";
	}
	
	public static String formataLocalizacaoPessoaJuridica(PessoaJuridica pessoaJuridica){
		
		if(pessoaJuridica.getNome().length() >= TAMANHO_MAXIMO_PESSOA_JURIDICA)
			return pessoaJuridica.getNome().substring(0,TAMANHO_MAXIMO_PESSOA_JURIDICA) + " (" + pessoaJuridica.getNumeroCNPJ() + ")";
		else
			return pessoaJuridica.getNome() + " (" + pessoaJuridica.getNumeroCNPJ() + ")";
	}
	
	public static String formataLocalizacaoProcuradoria(Procuradoria procuradoria){
		String tipoRepresentante = procuradoria.getTipo().getLabel();
		
		if(procuradoria.getNome().length() >= TAMANHO_MAXIMO_PROCURADORIA)
			return tipoRepresentante + " - " + procuradoria.getNome().substring(0,TAMANHO_MAXIMO_PROCURADORIA);
		else
			return tipoRepresentante + " - " + procuradoria.getNome();
	}
	
	public static String formataLocalizacaoProcurador(PessoaFisica pessoaFisica){
		PapelService papelService = (PapelService) Component.getInstance("papelService");
		Papel papel = papelService.findByCodeName(Papeis.REPRESENTANTE_PROCESSUAL);
		
		if(pessoaFisica.getNome().length() >= TAMANHO_MAXIMO_PROCURADOR)
			return papel.getNome()+" - " + pessoaFisica.getNome().substring(0,TAMANHO_MAXIMO_PROCURADOR) + " (" + pessoaFisica.getNumeroCPF() + ")";
		else
			return papel.getNome()+" - " + pessoaFisica.getNome() + " (" + pessoaFisica.getNumeroCPF() + ")";
	}

	public static String formataLocalizacaoProcurador(PessoaJuridica pessoaJuridica){
		PapelService papelService = (PapelService) Component.getInstance("papelService");
		Papel papel = papelService.findByCodeName(Papeis.REPRESENTANTE_PROCESSUAL);

		if(pessoaJuridica.getNome().length() >= TAMANHO_MAXIMO_PROCURADOR)
			return papel.getNome()+" - " + pessoaJuridica.getNome().substring(0,TAMANHO_MAXIMO_PROCURADOR) + " (" + pessoaJuridica.getNumeroCNPJ() + ")";
		else
			return papel.getNome()+" - " + pessoaJuridica.getNome() + " (" + pessoaJuridica.getNumeroCNPJ() + ")";
	}

	public static String formataLocalizacaoJusPostulandi(Pessoa pessoa){
		
		String numeroDocumento = " (" + pessoa.getDocumentoCpfCnpj() + ")";
		String jusPostulandi = "JusPostulandi - ";
		int maximoJusPostulandi = 100 - (numeroDocumento.length() + jusPostulandi.length());
		if(pessoa.getNome().length() >= maximoJusPostulandi)
			return jusPostulandi + pessoa.getNome().substring(0, maximoJusPostulandi) + numeroDocumento;
		else
			return "JusPostulandi - " + pessoa.getNome() + " (" + pessoa.getDocumentoCpfCnpj() + ")";
	}
	
	public static String formataLocalizacaoPessoaFisicaEspecializada(PessoaFisicaEspecializada pessoaFisicaEspecializada){
		
		if(pessoaFisicaEspecializada.getNome().length() >= TAMANHO_MAXIMO_PESSOA_FISICA)
			return pessoaFisicaEspecializada.getNome().substring(0,TAMANHO_MAXIMO_PESSOA_FISICA) + " (" + pessoaFisicaEspecializada.getNumeroCPF() + ")";
		else
			return pessoaFisicaEspecializada.getNome() + " (" + pessoaFisicaEspecializada.getNumeroCPF() + ")";
	}
	
	public static String converteLocalizacoesList(List<Localizacao> localizacoesList) {
		StringBuilder sb = new StringBuilder();

		if(CollectionUtilsPje.isNotEmpty(localizacoesList)) {
			for (Localizacao loc : localizacoesList){
				if (sb.length() > 0){
					sb.append(", ");
				}
				sb.append(loc.getIdLocalizacao());
			}
		}
		return sb.toString();
	}
}
