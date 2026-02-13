package br.jus.pje.api.converters;

import java.util.ArrayList;
import java.util.List;

import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.Endereco;
import br.jus.cnj.pje.pjecommons.model.services.pjelegacy.ModalidadeUnidadeFederacao;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;

public class EnderecoConverter {
	
	public List<Endereco> convertFrom(List <ProcessoParteEndereco> ppeList) {
		List<Endereco> enderecoList = new ArrayList<>();
		
		for (ProcessoParteEndereco ppe : ppeList) {
			Endereco endereco = this.convertFrom(ppe.getEndereco());
			enderecoList.add(endereco);
		}
		
		return enderecoList;
	}
	
	public Endereco convertFrom(br.jus.pje.nucleo.entidades.Endereco enderecoEntity) {
		Endereco endereco = new Endereco();
		CidadeConverter cidadeConverter = new CidadeConverter();
		
		endereco.setBairro(enderecoEntity.getNomeBairro());
		endereco.setCep(enderecoEntity.getCep().getNumeroCep());
		
		if(enderecoEntity.getCep().getMunicipio() != null) {
			endereco.setCidade(cidadeConverter.convertFrom(enderecoEntity.getCep().getMunicipio()));
		}
		
		endereco.setComplemento(enderecoEntity.getComplemento());
		endereco.setLogradouro(enderecoEntity.getNomeLogradouro());
		endereco.setNumero(enderecoEntity.getNumeroEndereco());

		if(enderecoEntity.getCep() != null && 
				enderecoEntity.getCep().getMunicipio() != null && 
				enderecoEntity.getCep().getMunicipio().getEstado().getCodEstado() != null) {
			endereco.setUnidadeFederacao(ModalidadeUnidadeFederacao.fromValue(enderecoEntity.getCep().getMunicipio().getEstado().getCodEstado()));
		}
		
		return endereco;
	}
}
