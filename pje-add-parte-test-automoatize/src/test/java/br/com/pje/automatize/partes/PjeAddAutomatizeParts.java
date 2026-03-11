package br.com.pje.automatize.partes;

import br.com.pje.automatize.PjeAutomatizeTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class PjeAddAutomatizeParts extends PjeAutomatizeTest implements Serializable {
    private static final long serialVersionUID = 1L;
    private  static ChromeDriver driver;
    private static Stream<String> documentos (){
        return Stream.of("00077468732", "00077525710", "00081743009", "00092019714", "00100077765", "00105485136", "00106362771", "00107818701", "00108536777", "00111612721", "00115236708", "00127610014", "00129482765", "00149662718", "00152127771", "00154487546", "00158906608", "00159366780", "00161534724", "00199399735", "00201029766", "00201417774", "00205295703", "00212297759", "00213976005", "00214484777", "00234426039", "00238264793", "00251971856", "00254260799", "00262807602", "00275015742", "00276937830", "00279128762", "00287747794", "00301868700", "00303089687", "00304495700", "00305233700", "00306752190", "00309248710", "00315947195", "00320025721", "00320164764", "00328883760", "00332653722", "00338352767", "00341503312", "00342150774", "00347343767", "00352343770", "00355936798", "00359296777", "00380817764", "00383242703", "00398634769", "00409316172", "00412144719", "00413398781", "00420092781", "00424467755", "00432259775", "00434593745", "00435024744", "00435969757", "00461846780", "00463560701", "00466663706", "00466951760", "00468643770", "00479310785", "00481125779", "00485538750", "00500336709", "00505317001", "00508107741", "00513518037", "00524699640", "00537307397", "00552994740", "00554926733", "00555769747", "00559071728", "00585063737", "00589873555", "00594757738", "00598564756", "00608290793", "00611642751", "00617485798", "00621209724", "00621806145", "00625705602", "00627481736", "00628135793", "00641270100", "00641833784", "00644700670", "00648538702", "00662354729", "00667911707", "00670293709", "00673272303", "00680634746", "00691088543", "00694424790", "00704933756", "00705066711", "00708889735", "00710685700", "00710859716", "00713009705", "00714391700", "00714723711", "00715321706", "00716732700", "00717822737", "00724101918", "00724154795", "00728501759", "00729237176", "00731913701", "00747741239", "00748454110", "00752913476", "00754349705", "00762294752", "00762734159", "00811589714", "00826261701", "00826607780", "00832059773", "00835189910", "00838563740", "00839283750", "00847025160", "00850279100", "00853647704", "00866164715", "00872641708", "00877750343", "00878318720", "00892345101", "00900951389", "00904288706", "00904432688", "00905865731", "00909150621", "00909618704", "00917476786", "00925090700", "00932417710", "00932600247", "00942854705", "00949053716", "00961889446", "01000842738", "01001231716", "01001828798", "01002092701", "01002956730", "01003524729", "01027623921", "01029963185", "01030556733", "01041026919", "01045365785", "01053151764", "01054584818", "01066902720", "01067557733", "01068443774", "01073946770", "01074918770", "01075419557", "01084319594", "01094786730", "01095101005", "01098609778", "01100346783", "01100509755", "01118098609", "01119675383", "01127746790", "01132214874", "01135579300", "01136701761", "01136817042", "01137145994", "01138268771", "01147716773", "01149868783", "01160913714", "01160980748", "01161362789", "01165042002", "01170504744", "01173115706", "01180218779", "01181600790", "01192184700", "01198100001", "01199017736", "01202403786", "01205274804", "01212109791", "01213073790", "01214990738", "01220960616", "01224276108", "01231562706", "01250407761", "01259957411", "01262008743", "01263776779", "01265203300", "01267179740", "01267188731", "01273368770", "01274195438", "01276238509", "01280792078", "01281096709", "01286724716", "01291733639", "01295127610", "01324181770", "01331464722", "01331549710", "01332111777", "01334269718", "01353936554", "01359615563", "01361311746", "01364274647", "01368034667", "01373910054", "01375588729", "01380215706", "01383012636", "01383863741", "01383870799", "01384454780", "01393797709", "01398427721", "01404468773", "01405844710", "01406231169", "01413610773", "01430810718", "01439145610", "01440618569", "01458042758", "01461636728", "01464245738", "01464570566", "01467119717", "01470276755", "01473669502", "01473897637", "01478911328", "01491072342", "01496157796", "01500045756", "01500048771", "01510621610", "01511249730", "01512989630", "01516691393", "01519011717", "01560368519", "01560967730", "01566555787", "01572299711", "01573049735", "01589599730", "01606235702", "01606673726", "01608219755", "01613903669", "01615304789", "01621801799", "01625449100", "01626158762", "01641151960", "01644180758", "01646134710", "01647411785", "01661968783", "01662030762", "01674203721", "01677254750", "01677823712", "01679157701", "01681193744", "01686644701", "01711758914", "01718062125", "01734876964", "01743483988", "01744184771", "01745126783", "01750947722", "01753523729", "01761865730", "01763097722", "01765677718", "01768064741", "01772161748", "01776112784", "01778605702", "01781672741", "01782848746", "01800562128", "01806569965", "01835209718", "01838183507", "01848838735", "01859654908", "01861443773", "01876932775", "01886871140", "01887186727", "01889132705", "01891042793", "01898271739", "01913001792", "01917837739", "01921637790", "01931720703", "01933951770", "01943017700", "01947666754", "01949443795", "01965902723", "01967676798", "01969213760", "01971274798", "01971466000", "01978961111", "01994188960", "02004813717", "02020669595", "02038019703", "02038291748", "02045584763", "02051067902", "02057365727", "02060495954", "02066684732", "02080096710", "02080286170", "02088611819", "02092183630", "02102990532", "02105174760", "02107889767", "02110848758", "02117543740", "02125891727", "02129498370", "02130314775", "02131884737", "02139315758", "02145015744", "02146360542", "02150962748", "02156218919", "02162739780", "02166530532", "02172022500", "02178017738", "02181260265", "02185390708", "02193018758", "02200874740", "02211507735", "02214456767", "02215472316", "02216433462", "02219268799", "02220756785", "02221007700", "02222037735", "02224848706", "02233221486", "02235473792", "02236475780", "02237322732", "02244094251", "02248213778", "02249653445", "02252665718", "02260412718", "02305883013", "02317016751", "02319138773", "02330758790", "02338808797", "02343163731", "02358218707", "02364698782", "02370091703", "02373117711", "02377475701", "02388785707", "02393228519", "02400376182", "02403344770", "02405295709", "02416736744", "02420530764", "02424110450", "02428811738", "02439547786", "02453282700", "02455759016", "02474528741", "02474638768", "02491871165", "02494216745", "02501693558", "02504371713", "02504429738", "02505077908", "02507473754", "02507782706", "02510656775", "02531568743", "02542789738", "02543451749", "02546958763", "02553942761", "02561870707", "02562886585", "02570037702", "02573307719", "02573412776", "02588194052", "02594948799", "02616947749", "02620347785", "02623233624", "02625901900", "02631654773", "02634911700", "02635040770", "02637357703", "02639646710", "02641201755", "02642837750", "02650534478", "02653353709", "02657571425", "02670936750", "02671109792", "02679821785", "02688772708", "02691002705", "02696958777", "02703304706", "02704891770", "02710127733", "02713676797", "02724382781", "02728249768", "02730233490", "02731926716", "02732488712", "02734341441", "02745398709", "02749689678", "02772022641", "02776982437", "02788021763", "02796820009", "02804010783", "02804540316", "02806724384", "02809920761", "02823919759", "02824928794", "02826831941", "02829299752", "02829314735", "02850000639", "02850418781");
    }

    @BeforeEach
    public void setUp() throws Exception {
        driver = this.setupDriver();
        driver.get("https://stg-03.tjrj.pje.jus.br/1g/login.seam");
    }

    @AfterEach
    public void tearDown() throws Exception {
        try {
            driver.quit();
        }catch (Exception e) {
            throw e;
        }
    }

    @ParameterizedTest()
    @MethodSource("documentos")
    public void adicionarPartesProcesso(String documento) throws Exception {
        try {
            this.autenticarPje(driver);
            this.pesquisarProcesso(driver);
            ChromeDriver retificarAutuacao =  this.autosDigitaisRetificarAutuacao(driver);
            changeToPartes(retificarAutuacao, documento);
        }catch (Exception e) {
            Logger.getLogger(PjeAutomatizeTest.class.getName()).severe(e.getMessage());
        }
        driver.close();
    }

    public void changeToPartes(ChromeDriver chromeDriver, String documento) throws Exception {
        //Switch tab
        ChromeDriver tabPartes = (ChromeDriver) chromeDriver.switchTo().window(chromeDriver.getWindowHandles().toArray()[2].toString());
        tabPartes.findElement(By.xpath("(//td[@id='tabPartes_lbl'])[1]")).click();
        sleepTimerTwoSeconds.get();

        tabPartes.findElement(By.xpath("(//i)[9]")).click();
        Select select = new Select(tabPartes.findElement(By.tagName("select")));
        select.selectByIndex(1);
        sleepTimerTwoSeconds.get();

        tabPartes.findElement(By.xpath("(//input[@id='preCadastroPessoaFisicaForm:preCadastroPessoaFisica_nrCPFDecoration:preCadastroPessoaFisica_nrCPF'])[1]")).sendKeys(documento);
        tabPartes.findElement(By.xpath("(//input[@id='preCadastroPessoaFisicaForm:pesquisarDocumentoPrincipal'])[1]")).click();
        sleepTimerInSeconds.apply(5l);

        tabPartes.findElement(By.xpath("(//input[@id='preCadastroPessoaFisicaForm:btnConfirmarCadastro'])[1]")).click();
        tabPartes.findElement(By.xpath("(//input[@id='formInserirParteProcesso:btnComplementarDadosParte'])[1]")).click();
        sleepTimerTwoSeconds.get();

        wait.apply(tabPartes).until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@id='formInserirParteProcesso:btnInserirParteProcesso'])[1]"))).click();
        sleepTimerInSeconds.apply(5l);

        WebElement mainDialog =  tabPartes.findElement(By.xpath("(//div[@id='mpAssociarParteProcessoCursorDiv'])[1]"));
        sleepTimerTwoSeconds.get();
        if(mainDialog.isDisplayed()) {
            WebElement mensagemParteCadastrada = tabPartes.findElement(By.xpath("(//dl[@id='formInserirParteProcesso:msgEndereco'])[1]"));
            if(mensagemParteCadastrada.isDisplayed() && !mensagemParteCadastrada.getText().contains("Parte já cadastrada")) {
                WebElement mensagemEndereco = tabPartes.findElement(By.xpath("(//span[@class='rich-messages-label'][contains(text(),\"Selecione ao menos um endereço para utilizar no pr\")])[2]"));
                if (mensagemEndereco.isDisplayed()) {
                    sleepTimerTwoSeconds.get();
                    tabPartes.findElement(By.xpath("(//td[@id='formInserirParteProcesso:enderecoUsuario_lbl'])[1]")).click();
                    tabPartes.findElement(By.xpath("(//input[@id='formInserirParteProcesso:cadastroPartePessoaEnderecochbkxIsEnderecoDesconhecido'])[1]")).click();
                    wait.apply(tabPartes).until(ExpectedConditions.elementToBeClickable(By.xpath("//input[starts-with(@id,'formInserirParteProcesso') and contains(@id,'simButton')]"))).click();

                    sleepTimerTwoSeconds.get();
                    wait.apply(tabPartes).until(ExpectedConditions.elementToBeClickable(By.xpath("(//input[@id='formInserirParteProcesso:btnInserirParteProcesso'])[1]"))).click();
                    sleepTimerInSeconds.apply(3l);
                }
            }
        }
        System.out.println(String.format("Parte vinculada %s", documento));
    }
}
