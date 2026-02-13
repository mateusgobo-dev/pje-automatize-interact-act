package br.jus.cnj.pje.nucleo.service;

import java.util.Date;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import br.com.infox.ibpm.home.Authenticator;
import br.jus.cnj.pje.nucleo.PJeBusinessException;
import br.jus.cnj.pje.nucleo.PJeDAOException;
import br.jus.cnj.pje.nucleo.manager.CepManager;
import br.jus.cnj.pje.nucleo.manager.EnderecoManager;
import br.jus.pje.nucleo.entidades.Cep;
import br.jus.pje.nucleo.entidades.Endereco;
import br.jus.pje.nucleo.entidades.Pessoa;
import br.jus.pje.nucleo.entidades.ProcessoParte;
import br.jus.pje.nucleo.entidades.ProcessoParteEndereco;

@Name(EnderecoService.NAME)
public class EnderecoService{

	public static final String NAME = "enderecoService";

	@In
	private CepManager cepManager;

	@In
	private EnderecoManager enderecoManager;

	@Logger
	private Log logger;

	public Endereco getEndereco(){
		Date d = new Date();
		Endereco end = new Endereco();
		end.setDataAlteracao(d);
		end.setUsuarioCadastrador(Authenticator.getUsuarioLogado());
		
		Cep cep = new Cep();
		end.setCep(cep);
		
		return end;
	}

	public Endereco getEndereco(Integer id){
		Endereco ret = null;
		try{

			ret = this.enderecoManager.findById(id);

		} catch (PJeBusinessException e){
			logger.debug("Não foi possível recuperar o endereço com id {0}. {1}", id, e.getLocalizedMessage());
		}
		return ret;
	}

	/**
	 * Obtém um objeto endereço a partir de um CEP dado.
	 * 
	 * @param cep o cep do endereço que se pretende criar, sem separadores
	 * @return um endereço novo, com o cep dado, ou null, se o CEP não existir.
	 */
	public Endereco getEndereco(String cep){
		Endereco end = null;
		Cep c = cepManager.findByCep(cep);
		if (c != null){
			end = this.getEndereco();
			end.setCep(c);
			end.setNomeEstado(c.getMunicipio().getEstado().getEstado());
			end.setNomeCidade(c.getMunicipio().getMunicipio());
			end.setNomeBairro(c.getNomeBairro());
			end.setNomeLogradouro(c.getNomeLogradouro());
			end.setComplemento(c.getComplemento());
		}
		return end;
	}

	/**
	 * Grava um dado endereço, vinculando-o a uma pessoa.
	 * 
	 * @param e o endereço a ser gravado
	 * @param p a pessoa a quem será vinculado o endereço.
	 * 
	 * @throws PJeBusinessException se houver falha na gravação do endereço por desrespeito a alguma regra de negócio
	 * @throws PJeDAOException se houver falha na gravação do endereço por falha quando da gravação
	 */
	public Endereco gravaEndereco(Endereco e, Pessoa p) throws PJeBusinessException, PJeDAOException{
		if (!respeitaCep(e)){
			throw new PJeBusinessException(
					"O endereço que se pretendia gravar contém dados conflitantes em relação aos dados constantes em seu código postal.");
		}
		e.setUsuario(p);
		e = this.enderecoManager.persist(e);
		this.enderecoManager.flush();
		if (e != null){
			p.getEnderecoList().add(e);
		}
		return e;
	}

	public Endereco gravaEndereco(Endereco e, ProcessoParte p) throws PJeBusinessException, PJeDAOException{
		e = this.gravaEndereco(e, p.getPessoa());
		ProcessoParteEndereco ppe = new ProcessoParteEndereco();
		ppe.setEndereco(e);
		ppe.setProcessoParte(p);
		p.getProcessoParteEnderecoList().add(ppe);
		return e;
	}
	
	/**
	 * Recupera o endereço mais recentemente cadastrado para uma dada pessoa.
	 * 
	 * @param p a pessoa cujo endereço se pretende recuperar
	 * @return o endereço mais recente localizado, ou null se não houver endereço cadastrado.
	 */
	public Endereco recuperaEnderecoRecente(Pessoa p){
		return enderecoManager.recuperaEnderecoRecente(p);
	}

	/**
	 * Verifica se o endereço dado encerra alterações indevidas em campos já preenchidos do cep.
	 * 
	 * @param e endereço a ser analisado
	 * @return true, se o endereço não encerra dados conflitantes com seu respectivo CEP
	 */
	private boolean respeitaCep(Endereco e){
		boolean ret = true;
		Cep cep = e.getCep();
		if (!cep.getMunicipio().getEstado().getEstado().equalsIgnoreCase(e.getNomeEstado())){
			ret = false;
			logger.error("O Estado indicado no endereço é diverso daquele a que pertence o CEP.");
		}
		if (!cep.getMunicipio().getMunicipio().equalsIgnoreCase(e.getNomeCidade())){
			ret = false;
			logger.error("O município indicado no endereço é diverso daquele a que pertence o CEP.");
		}
		if (cep.getNomeBairro() != null && !cep.getNomeBairro().equalsIgnoreCase(e.getNomeBairro())){
			ret = false;
			logger.error("O bairro indicado no endereço é diverso daquele a que pertence o CEP.");
		}
		return ret;
	}

}
