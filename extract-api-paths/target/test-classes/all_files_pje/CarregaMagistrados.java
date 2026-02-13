/**
 * 
 */
package br.jus.cnj.pje.nucleo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

import br.jus.cnj.pje.nucleo.service.OrgaoJulgadorService;
import br.jus.cnj.pje.nucleo.service.PessoaMagistradoService;
import br.jus.cnj.pje.nucleo.service.UsuarioLocalizacaoMagistradoServidorService;
import br.jus.pje.nucleo.entidades.Localizacao;
import br.jus.pje.nucleo.entidades.OrgaoJulgador;
import br.jus.pje.nucleo.entidades.PessoaMagistrado;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacao;
import br.jus.pje.nucleo.entidades.UsuarioLocalizacaoMagistradoServidor;
import br.jus.pje.nucleo.enums.TitularidadeMagistradoEnum;

/**
 * @author cristof
 * 
 */
@Name("carregaMagistrados")
@Scope(ScopeType.CONVERSATION)
public class CarregaMagistrados implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7505712602352747974L;

	private static String[] cpfs = { "70831947420", "79740928315", "45750084353" };

	private static String[] cpfsAlunos = { "67865755015", "02348089424", "18027259894", "02348919800", "96209348491",
			"64170616987", "76577635900", "69551065468", "32792091886", "10158612884", "61951374304", "61446416100",
			"26354731888", "17611703846", "60132590468", "00779642414", "84676582820", "05961934918", "00922590818",
			"44093390363", "59667788415", "16337162870", "27164982880", "25900079813", "61476498504", "24751248812",
			"78385032649", "83718729415", "02614552658", "21366499816", "02271124409", "23935804172", "30400934876",
			"10534363865", "07299566877", "02728143420", "03112687817", "66252040568", "31775632172", "03080545630",
			"13258630500", "11029338809", "27753263587", "14049053837", "84828951334", "05527890469", "06627405806",
			"08705563801", "25607110877", "04035777803", "20352565268", "33911690100", "25555652801", "42829275187",
			"08482546864", "13016606801", "13425287869", "07713372806", "02931083828", "14726576836", "15545945881",
			"96387068487", "87444020400", "47000449349", "02381889493", "02521959901", "66714559453", "87956438934",
			"49745441449", "29331185804", "35588446372", "03695432462", "03714874470", "16126327420", "16271445870",
			"89089162534", "91425980449", "02425005447", "21730123805", "05746083461", "04361701407", "00932712479",
			"27524235860", "75934825372", "04357030480", "28583258805", "10596582838", "02415882437", "05198083470",
			"01874813426", "83999191449", "44060947415", "04970016674", "58779019668", "11097098893", "26901472897",
			"85288020434", "93973551634", "15356748817", "02384644408", "10850849810", "76994333353", "15066355120",
			"01697392873", "96374934504", "07785634807", "85291927453", "06968103877", "02053075406", "09147245840",
			"17706768805", "02884786490", "88104788434", "76308634115", "09510215830", "02586104412", "12512312844",
			"24676753861", "13077478846", "03382000458", "21997167832", "75698374953", "29698545468", "39356035415",
			"29113640828", "12639987801", "36194964472", "15256781839", "03944613473", "00803559518", "10672323893",
			"26815972852", "50362755515", "11350776149", "32092334620", "13645755829", "87580977320", "06813053809",
			"09177364864", "69576742404", "27305881830", "08962213850", "41050606434", "82854351991", "05688697881",
			"88303160834", "27239203808", "80947840168", "50828231400", "74200194368", "04568477824", "47210907300",
			"49333011900", "90397304404", "10875669832", "11437421504", "76874320882", "96247673515", "26931361805",
			"05255727830", "07153987843", "03880148490", "38285010449", "28937350459", "05568825807", "02676701410",
			"28192846806", "08375191876", "14645499823", "02700651642", "03939485446", "12515258816", "09285445837",
			"01273125444", "00965567494", "77592786400", "14848781833", "93815336872", "52398900487", "05803278840",
			"47535440100", "07195498417", "26108130822", "07648220806", "57176620334", "90503570672", "31651747172",
			"50127772120", "25301340816", "11664536850", "32695411391", "05709589896", "15324733890", "25265299807",
			"21573532835", "79010423468", "16682005890", "25022921820", "00990572803", "03509147456", "50780247434",
			"38664852120", "14047370819", "07690059856", "09549827895" };

	private static Random random = new Random();

	private int pagina = 0;

	@In(create = true)
	private transient OrgaoJulgadorService orgaoJulgadorService;

	@In(create = true)
	private transient PessoaMagistradoService pessoaMagistradoService;

	@In(create = true)
	private transient UsuarioLocalizacaoMagistradoServidorService usuarioLocalizacaoMagistradoServidorService;

	@Logger
	private Log logger;

	private OrgaoJulgador[] orgaosJulgadores;

	private List<String> cpfsMagistrados = new ArrayList<String>();

	public void carregaMagistradosAleatoriamente() {
		loadList();
		obtemOrgaosJulgadores();
		for (String cpf : cpfsMagistrados) {
			Localizacao localizacao = orgaosJulgadores[random.nextInt(orgaosJulgadores.length)].getLocalizacao();
			String matricula = String.format("%5d", random.nextInt(100000));
			TitularidadeMagistradoEnum tit = ((random.nextInt(100) % 2) == 0) ? TitularidadeMagistradoEnum.T
					: TitularidadeMagistradoEnum.S;
			PessoaMagistrado p = pessoaMagistradoService.create(cpf, matricula, localizacao);

			pessoaMagistradoService.persist(p);
			pessoaMagistradoService.refresh(p);
			logger.info("Persistido o magistrado [" + p.getNome() + " ] com id [" + p.getIdUsuario() + "].");
			UsuarioLocalizacao ul = p.getUsuarioLocalizacaoList().get(0);
			UsuarioLocalizacaoMagistradoServidor ulms = new UsuarioLocalizacaoMagistradoServidor();
			ulms.setDtInicio(new Date());
			// ulms.setTitularidade(tit);
			ulms.setUsuarioLocalizacao(ul);
			// ulms.setVisibilidadeProcesso(VisualizacaoProcessoEnum.A);
			ulms.setIdUsuarioLocalizacaoMagistradoServidor(ul.getIdUsuarioLocalizacao());
			usuarioLocalizacaoMagistradoServidorService.persist(ulms);
			logger.info("Criado magistrado [" + p.getNome() + "] na localização ["
					+ p.getLocalizacoes()[0].getLocalizacao() + "].");
		}
	}

	private void obtemOrgaosJulgadores() {
		orgaosJulgadores = this.orgaoJulgadorService.findAll().toArray(new OrgaoJulgador[] {});
	}

	@SuppressWarnings("unused")
	private void loadList0() {
		cpfsMagistrados.clear();
		for (String cpf : cpfs) {
			cpfsMagistrados.add(cpf);
		}
	}

	private void loadList() {
		cpfsMagistrados.clear();
		for (int i = pagina * 20; i < ((pagina + 1) * 20) && i < cpfsAlunos.length; i++) {
			cpfsMagistrados.add(cpfsAlunos[i]);
		}
	}

	public int getPagina() {
		return pagina;
	}

	public void setPagina(int pagina) {
		this.pagina = pagina;
	}

}
