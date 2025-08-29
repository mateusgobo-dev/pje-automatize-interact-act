/**
 * 
 */
package br.jus.cnj.pje.nucleo.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.business.dao.PublicacaoDiarioEletronicoDAO;
import br.jus.cnj.pje.servicos.PrazosProcessuaisService;
import br.jus.cnj.pje.servicos.prazos.Calendario;
import br.jus.cnj.pje.util.CollectionUtilsPje;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.ProcessoExpediente;
import br.jus.pje.nucleo.entidades.ProcessoParteExpediente;
import br.jus.pje.nucleo.entidades.PublicacaoDiarioEletronico;
import br.jus.pje.nucleo.enums.ContagemPrazoEnum;
import br.jus.pje.nucleo.enums.SituacaoPublicacaoDiarioEnum;
import br.jus.pje.nucleo.enums.TipoPrazoEnum;

@Name(PublicacaoDiarioEletronicoManager.NAME)
public class PublicacaoDiarioEletronicoManager extends BaseManager<PublicacaoDiarioEletronico>{

	public static final String NAME = "publicacaoDiarioEletronicoManager";
	
	@Logger
	private Log logger;
	
	@In(create = true)
	private PublicacaoDiarioEletronicoDAO publicacaoDiarioEletronicoDAO;

	@In(create = true)
	private PrazosProcessuaisService prazosProcessuaisService;
    
	@Override
	protected PublicacaoDiarioEletronicoDAO getDAO(){
		return this.publicacaoDiarioEletronicoDAO;
	}
	
	public static PublicacaoDiarioEletronicoManager instance() {
		return (PublicacaoDiarioEletronicoManager)Component.getInstance(PublicacaoDiarioEletronicoManager.NAME);
	}
	
	public PublicacaoDiarioEletronico novo(Date dtCriacao, ProcessoParteExpediente ppe, Calendario calendario) {
		PublicacaoDiarioEletronico publicacaoDJE = new PublicacaoDiarioEletronico();
		publicacaoDJE.setProcessoParteExpediente(ppe);
		publicacaoDJE.setQtdVerificacoes(0);
		publicacaoDJE.setSituacao(SituacaoPublicacaoDiarioEnum.A);
		
		Date dtExpectativaPublicacaoDJE = prazosProcessuaisService.calculaPrazoProcessual(dtCriacao, 1, TipoPrazoEnum.D,
				calendario,
				ppe.getProcessoJudicial().getCompetencia().getCategoriaPrazoProcessual(), ContagemPrazoEnum.C);

		publicacaoDJE.setDtExpectativaPublicacao(dtExpectativaPublicacaoDJE);

		return publicacaoDJE;
	}
	
	public PublicacaoDiarioEletronico getPublicacao(ProcessoParteExpediente ppe) {
		List<PublicacaoDiarioEletronico> publicacoesDJE = ppe.getPublicacaoDiarioEletronicoList();
		PublicacaoDiarioEletronico publicacaoDJE = null;
		if(CollectionUtilsPje.isNotEmpty(publicacoesDJE)) {
			publicacaoDJE = publicacoesDJE.get(0);
		}
		if(publicacaoDJE == null) {
			OrgaoJulgador oj = ppe.getProcessoJudicial().getOrgaoJulgador();
			Calendario calendario = prazosProcessuaisService.obtemCalendario(oj);
			publicacaoDJE = this.novo(ppe.getProcessoExpediente().getDtCriacao(), ppe, calendario);
			ppe.getPublicacaoDiarioEletronicoList().add(publicacaoDJE);
		}
		return publicacaoDJE;
	}
	
	public void indicaVerificacaoPublicacoes(Calendar dtVerificacao) {
		this.getDAO().indicaVerificacaoPublicacoes(dtVerificacao);
	}
	
	public void indicaVerificacaoPublicacao(ProcessoExpediente expediente) {
		this.getDAO().indicaVerificacaoPublicacao(expediente);
	}

	public void indicaVerificacaoPublicacao(Integer idProcessoExpediente) {
		this.getDAO().indicaVerificacaoPublicacao(idProcessoExpediente);
	}

	public void sinalizaPendenciaPublicacao(Integer idProcessoExpediente) {
		this.getDAO().sinalizaPendenciaPublicacao(idProcessoExpediente);
	}

	public List<String> recuperaRecibosDJeAguardandoPublicacao(ProcessoExpediente expediente){
		List<String> codigosReciboDJe = new ArrayList<>();
		if(expediente != null && expediente.getProcessoParteExpedienteList() != null) {
			for (ProcessoParteExpediente ppe : expediente.getProcessoParteExpedienteList()) {
				if(!ppe.getFechado()) {
					PublicacaoDiarioEletronico publicacaoDJE = this.getPublicacao(ppe);
					if(publicacaoDJE.getSituacao() == SituacaoPublicacaoDiarioEnum.A && publicacaoDJE.getReciboPublicacaoDiarioEletronico() != null) {
						codigosReciboDJe.add(publicacaoDJE.getReciboPublicacaoDiarioEletronico());
					}
				}
			}
		}
		return codigosReciboDJe;
	}
}
