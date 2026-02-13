/**
 * 
 */
package br.jus.cnj.pje.nucleo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.FlushModeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.faces.Redirect;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.service.PessoaFisicaService;
import br.jus.cnj.pje.nucleo.service.PessoaService;
import br.jus.pje.nucleo.entidades.PessoaAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteAdvogado;
import br.jus.pje.nucleo.entidades.PessoaAssistenteProcuradoria;
import br.jus.pje.nucleo.entidades.PessoaFisica;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.PessoaOficialJustica;
import br.jus.pje.nucleo.entidades.PessoaPerito;
import br.jus.pje.nucleo.entidades.PessoaProcurador;
import br.jus.pje.nucleo.entidades.PessoaServidor;

/**
 * Classe destinada a permitir a criação de pessoas simples no PJe.
 * 
 * @author cristof
 * 
 */
@Name("criaPessoas")
@Scope(ScopeType.CONVERSATION)
public class CriaPessoas implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3669683155122655068L;

	@Logger
	private Log logger;

	@In(create = true)
	private transient PessoaFisicaService pessoaFisicaService;

	@In(create = true)
	private transient PessoaService pessoaService;
	
	@In
	private FacesMessages facesMessages;
	
	@In
	private Redirect redirect;

	private String cpf;
	
	private String tipoPessoa;
	
	public void criaPessoasFisicas(int n){
		InscricaoMFUtil gerador = new InscricaoMFUtil();
		Set<PessoaFisica> pessoas = new HashSet<PessoaFisica>(n);
		do{
			PessoaFisica pessoa = criaPessoa(gerador.geraCPF());
			pessoas.add(pessoa);
		} while (pessoas.size() < n);
		return;
	}

	private PessoaFisica criaPessoa(String cpf){
		PessoaFisica pessoa = null;
		try{

			pessoa = (PessoaFisica) pessoaService.findByInscricaoMF(cpf);

			logger.info("Pessoa [" + pessoa.getNome() + "] com CPF [" + pessoa.getNumeroCPF() + "] e mãe [" + pessoa.getNomeGenitora() + "].");
			pessoaFisicaService.persist(pessoa);

		} catch (PJeBusinessException e){
			e.printStackTrace();
		}

		return pessoa;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}
	
	public String getTipoPessoa() {
		return tipoPessoa;
	}

	public void setTipoPessoa(String tipoPessoa) {
		this.tipoPessoa = tipoPessoa;
	}

	@SuppressWarnings("unchecked")
	@Begin(flushMode=FlushModeType.AUTO)
	public void gato(){
		if(cpf != null){
			PessoaFisica p = new PessoaFisica();
			p.setNumeroCPF(cpf);
			String destino = null;
			try {
				if(tipoPessoa == null || tipoPessoa.isEmpty()){
					p = (PessoaFisica) pessoaService.especializa(p);
				}else if(tipoPessoa.equals("ADV")){
					p = (PessoaFisica) pessoaService.especializa(p, PessoaAdvogado.class);
				}else if(tipoPessoa.equals("ASA")){
					p = (PessoaFisica) pessoaService.especializa(p, PessoaAssistenteAdvogado.class);
					destino = "/PessoaAssistenteAdvogado/listView.seam";
				}else if(tipoPessoa.equals("APR")){
					p = (PessoaFisica) pessoaService.especializa(p, PessoaAssistenteProcuradoria.class);
					destino = "/PessoaAssistenteProcuradoria/listView.seam";
				}else if(tipoPessoa.equals("OFJ")){
					p = (PessoaFisica) pessoaService.especializa(p, PessoaOficialJustica.class);
					destino = "/PessoaOficialJustica/listView.seam";
				}else if(tipoPessoa.equals("MAG")){
					p = (PessoaFisica) pessoaService.especializa(p, PessoaMagistrado.class);
					destino = "/PessoaMagistrado/listView.seam";
				}else if(tipoPessoa.equals("PER")){
					p = (PessoaFisica) pessoaService.especializa(p, PessoaPerito.class);
					destino = "/PessoaPerito/listView.seam";
				}else if(tipoPessoa.equals("PRO")){
					p = (PessoaFisica) pessoaService.especializa(p, PessoaProcurador.class);
					destino = "/PessoaProcurador/listView.seam";
				}else if(tipoPessoa.equals("SRV")){
					p = (PessoaFisica) pessoaService.especializa(p, PessoaServidor.class);
					destino = "/PessoaServidor/listView.seam";
				}
				Conversation.instance().end();
				redirect.setViewId(destino);
				redirect.setParameter("id", p.getIdUsuario());
				redirect.setParameter("tab", "form");
				redirect.setConversationPropagationEnabled(false);
				redirect.execute();
			} catch (PJeBusinessException e) {
				facesMessages.add(Severity.ERROR, "Erro ao tentar criar a pessoa: {0}", e.getLocalizedMessage());
			}
		}
	}

}
