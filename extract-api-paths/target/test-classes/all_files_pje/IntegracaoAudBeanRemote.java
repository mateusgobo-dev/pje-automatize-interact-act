package br.jus.csjt.pje.business.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Remote;

import br.jus.csjt.pje.commons.exception.IntegracaoAudException;
import br.jus.pje.jt.entidades.AudAdvogados;
import br.jus.pje.jt.entidades.AudAutor;
import br.jus.pje.jt.entidades.AudConf;
import br.jus.pje.jt.entidades.AudEspecie;
import br.jus.pje.jt.entidades.AudImportacao;
import br.jus.pje.jt.entidades.AudJuizes;
import br.jus.pje.jt.entidades.AudOrgaoMunicipio;
import br.jus.pje.jt.entidades.AudPauta;
import br.jus.pje.jt.entidades.AudPeritos;
import br.jus.pje.jt.entidades.AudReu;
import br.jus.pje.jt.entidades.AudTipoVerba;

@Remote
public interface IntegracaoAudBeanRemote {
	public List<AudAdvogados> listarAdvogados();
	public List<AudOrgaoMunicipio> listarOrgaoMunicipios();
	public List<AudPeritos> listarPeritos();
	public List<AudJuizes> listarJuizes();
	public List<AudEspecie> listarEspecie();
	public List<AudPauta> listarPauta(Date dataInicio, Date dataFim, int idOrgaoJulgador);
	public List<AudConf> listarAudConf();
	public List<AudConf> listarAudConf(int idMunicipio);
	public List<AudAutor> listarAutor(List<AudPauta> pautas);
	public List<AudReu> listarReu(List<AudPauta> pautas);
	public int getJustica(AudPauta audPauta);
	public int getRegional(AudPauta audPauta);
	public String getNumCNJ(AudPauta audPauta);
	public void setAudImportacao(AudImportacao ai) throws IntegracaoAudException;
	public List<AudTipoVerba> listarAudTipoVerba();                
	public List<AudConfiguracao> getAudConfiguracao();
}
