/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import it.bologna.ausl.masterchef.Chef;
import it.bologna.ausl.masterchef.errors.WorkerException;
import it.bologna.ausl.masterchef.workers.SendToParerWorker;
import java.io.File;
import java.util.Map;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author Salo
 */
public class TestaSendParerWorker {

    private static final Logger log = LogManager.getLogger(TestaSendParerWorker.class);
    private static Class<Chef> cClass;

    private static void testDue() throws WorkerException {

        log.info("testDue() ");
        SendToParerWorker c = new SendToParerWorker();

        JSONObject jo = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName", "letterafirmata.pdf");
        jsonObject.put("id", "1629702012629-GVzU60XlOc");
        jsonObject.put("mime", "application/pdf");
        jsonObject.put("formatType", "PDF");
        jsonObject.put("uuidMongo", "d58a62a4-9952-4915-9a8a-67665bef8db8");
        jsonObject.put("fileBase64", null);
        jsonObject.put("hash", "9871ea57c53fbb1deace880c693e006e");

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("fileName", "destinatari.pdf");
        jsonObject2.put("mime", "application/pdf");
        jsonObject2.put("id", "1629702012743-GF5p9eXzht");
        jsonObject2.put("formatType", "PDF");
        jsonObject2.put("uuidMongo", "3b5a2131-403e-41ac-8265-6600616bb74f");
        jsonObject2.put("fileBase64", null);
        jsonObject2.put("hash", "4355ef392f2b4eb02478f3eaad48b652");

        JSONObject jsonObject3 = new JSONObject();
        jsonObject3.put("fileName", "relata committente 1.pdf");
        jsonObject3.put("mime", "application/pdf");
        jsonObject3.put("id", "1629702012749-xdIWXvApHr");
        jsonObject3.put("formatType", "PDF");
        jsonObject3.put("uuidMongo", "");
        jsonObject3.put("fileBase64", null);
        jsonObject3.put("hash", "");

        JSONObject jsonObject4 = new JSONObject();
        jsonObject4.put("fileName", "frontespizio.pdf");
        jsonObject4.put("mime", "application/pdf");
        jsonObject4.put("id", "1629702012750-rExNjkRmXo");
        jsonObject4.put("formatType", "PDF");
        jsonObject4.put("uuidMongo", "b3a48008-982f-47be-91b3-6b8fb6d2081d");
        jsonObject4.put("fileBase64", null);
        jsonObject4.put("hash", "6a02578085ab0f1ed373cb209ecf8c1a");

        JSONObject jsonObject5 = new JSONObject();
        jsonObject5.put("fileName", "segnatura.xml");
        jsonObject5.put("mime", "text/xml");
        jsonObject5.put("id", "1629702012751-88ZTdtdTAT");
        jsonObject5.put("formatType", "XML");
        jsonObject5.put("uuidMongo", "8b1a8cbc-7044-4908-8b99-45c03d381519");
        jsonObject5.put("fileBase64", null);
        jsonObject5.put("hash", "d0f5e8bbae49bba6711f2fdc9b508bd0");

        jsonArray.add(jsonObject);
        jsonArray.add(jsonObject2);
        //jsonArray.add(jsonObject3);
        jsonArray.add(jsonObject4);
        jsonArray.add(jsonObject5);

        jo.put("identityFiles", jsonArray);
        jo.put("xmlDocument", "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>\n"
                + "<UnitaDocumentaria>\n"
                + "	<Intestazione>\n"
                + "		<Versione>1.4</Versione>\n"
                + "		<Versatore>\n"
                + "			<Ambiente>PARER_TEST</Ambiente>\n"
                + "			<Ente>AUSL_BO</Ente>\n"
                + "			<Struttura>ASL_BO</Struttura>\n"
                + "			<UserID>gedi_ausl_bo_pre</UserID>\n"
                + "		</Versatore>\n"
                + "		<Chiave>\n"
                + "			<Numero>55028326</Numero>\n"
                + "			<Anno>2021</Anno>\n"
                + "			<TipoRegistro>PG</TipoRegistro>\n"
                + "		</Chiave>\n"
                + "		<TipologiaUnitaDocumentaria>DOCUMENTO PROTOCOLLATO IN USCITA</TipologiaUnitaDocumentaria>\n"
                + "	</Intestazione>\n"
                + "	<Configurazione>\n"
                + "		<TipoConservazione>VERSAMENTO_ANTICIPATO</TipoConservazione>\n"
                + "		<ForzaAccettazione>false</ForzaAccettazione>\n"
                + "		<ForzaConservazione>false</ForzaConservazione>\n"
                + "		<ForzaCollegamento>false</ForzaCollegamento>\n"
                + "		<SimulaSalvataggioDatiInDB>false</SimulaSalvataggioDatiInDB>\n"
                + "	</Configurazione>\n"
                + "	<ProfiloArchivistico>\n"
                + "		<FascicoloPrincipale>\n"
                + "			<Classifica>02</Classifica>\n"
                + "			<Fascicolo>\n"
                + "				<Identificativo>2018/1225</Identificativo>\n"
                + "				<Oggetto>ls test</Oggetto>\n"
                + "			</Fascicolo>\n"
                + "		</FascicoloPrincipale>\n"
                + "	</ProfiloArchivistico>\n"
                + "	<ProfiloUnitaDocumentaria>\n"
                + "		<Oggetto>test errore glog 1</Oggetto>\n"
                + "		<Data>2021-08-20T11:28:00.000+02:00</Data>\n"
                + "		<Cartaceo>false</Cartaceo>\n"
                + "	</ProfiloUnitaDocumentaria>\n"
                + "	<DatiSpecifici>\n"
                + "		<VersioneDatiSpecifici>2.0</VersioneDatiSpecifici>\n"
                + "		<Destinatario>Vedi annesso elenco destinatari</Destinatario>\n"
                + "		<Movimento>OUT</Movimento>\n"
                + "		<ModalitaTrasmissione>BABEL</ModalitaTrasmissione>\n"
                + "		<ResponsabileDelProcedimento>De Marco Giuseppe (UO Sistemi Informatici (SS))</ResponsabileDelProcedimento>\n"
                + "		<Firmatario>&amp;lt;nominativo&amp;gt;Pedrazzi Gian Carla&amp;lt;/nominativo&amp;gt;</Firmatario>\n"
                + "		<DataFascicolazione>2021-08-20</DataFascicolazione>\n"
                + "		<IdentificazioneRepository>GEDI</IdentificazioneRepository>\n"
                + "		<Visibilita>LIBERA</Visibilita>\n"
                + "		<Consultabilita>NON PRECISATA</Consultabilita>\n"
                + "		<TipologiaAtto>Non rilevante ai fini dell'applicazione dell'art. 23 del D.Lgs. 33/2013</TipologiaAtto>\n"
                + "	</DatiSpecifici>\n"
                + "	<NumeroAnnessi>1</NumeroAnnessi>\n"
                + "	<NumeroAnnotazioni>2</NumeroAnnotazioni>\n"
                + "	<DocumentoPrincipale>\n"
                + "		<IDDocumento>babel_suite_52547A6D-70B5-3425-6EE7-6A6107A26D60</IDDocumento>\n"
                + "		<TipoDocumento>DOCUMENTO PROTOCOLLATO</TipoDocumento>\n"
                + "		<ProfiloDocumento>\n"
                + "			<Autore>&amp;lt;nominativo&amp;gt;Pedrazzi Gian Carla&amp;lt;/nominativo&amp;gt;</Autore>\n"
                + "		</ProfiloDocumento>\n"
                + "		<StrutturaOriginale>\n"
                + "			<TipoStruttura>DocumentoGenerico</TipoStruttura>\n"
                + "			<Componenti>\n"
                + "				<Componente>\n"
                + "					<ID>1629702012629-GVzU60XlOc</ID>\n"
                + "					<OrdinePresentazione>1</OrdinePresentazione>\n"
                + "					<TipoComponente>Contenuto</TipoComponente>\n"
                + "					<TipoSupportoComponente>FILE</TipoSupportoComponente>\n"
                + "					<NomeComponente>letterafirmata.pdf</NomeComponente>\n"
                + "					<HashVersato>9871ea57c53fbb1deace880c693e006e</HashVersato>\n"
                + "					<IDComponenteVersato>d58a62a4-9952-4915-9a8a-67665bef8db8</IDComponenteVersato>\n"
                + "					<UtilizzoDataFirmaPerRifTemp>false</UtilizzoDataFirmaPerRifTemp>\n"
                + "					<RiferimentoTemporale>2021-08-20T11:28:00.000+02:00</RiferimentoTemporale>\n"
                + "					<DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE</DescrizioneRiferimentoTemporale>\n"
                + "				</Componente>\n"
                + "			</Componenti>\n"
                + "		</StrutturaOriginale>\n"
                + "	</DocumentoPrincipale>\n"
                + "	<Annessi>\n"
                + "		<Annesso>\n"
                + "			<IDDocumento>3b5a2131-403e-41ac-8265-6600616bb74f</IDDocumento>\n"
                + "			<TipoDocumento>ELENCO DESTINATARI</TipoDocumento>\n"
                + "			<ProfiloDocumento>\n"
                + "				<Descrizione>Destinatari\n"
                + "</Descrizione>\n"
                + "			</ProfiloDocumento>\n"
                + "			<StrutturaOriginale>\n"
                + "				<TipoStruttura>DocumentoGenerico</TipoStruttura>\n"
                + "				<Componenti>\n"
                + "					<Componente>\n"
                + "						<ID>1629702012743-GF5p9eXzht</ID>\n"
                + "						<OrdinePresentazione>2</OrdinePresentazione>\n"
                + "						<TipoComponente>Contenuto</TipoComponente>\n"
                + "						<TipoSupportoComponente>FILE</TipoSupportoComponente>\n"
                + "						<NomeComponente>destinatari.pdf</NomeComponente>\n"
                + "						<HashVersato>4355ef392f2b4eb02478f3eaad48b652</HashVersato>\n"
                + "						<IDComponenteVersato>3b5a2131-403e-41ac-8265-6600616bb74f</IDComponenteVersato>\n"
                + "						<UtilizzoDataFirmaPerRifTemp>false</UtilizzoDataFirmaPerRifTemp>\n"
                + "						<RiferimentoTemporale>2021-08-20T11:28:00.000+02:00</RiferimentoTemporale>\n"
                + "						<DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE</DescrizioneRiferimentoTemporale>\n"
                + "					</Componente>\n"
                + "				</Componenti>\n"
                + "			</StrutturaOriginale>\n"
                + "		</Annesso>\n"
                + "	</Annessi>\n"
                + "	<Annotazioni>\n"
                + "		<Annotazione>\n"
                + "			<IDDocumento>b3a48008-982f-47be-91b3-6b8fb6d2081d</IDDocumento>\n"
                + "			<TipoDocumento>FRONTESPIZIO</TipoDocumento>\n"
                + "			<ProfiloDocumento>\n"
                + "				<Descrizione>Frontespizio\n"
                + "</Descrizione>\n"
                + "			</ProfiloDocumento>\n"
                + "			<StrutturaOriginale>\n"
                + "				<TipoStruttura>DocumentoGenerico</TipoStruttura>\n"
                + "				<Componenti>\n"
                + "					<Componente>\n"
                + "						<ID>1629702012750-rExNjkRmXo</ID>\n"
                + "						<OrdinePresentazione>4</OrdinePresentazione>\n"
                + "						<TipoComponente>Contenuto</TipoComponente>\n"
                + "						<TipoSupportoComponente>FILE</TipoSupportoComponente>\n"
                + "						<NomeComponente>frontespizio.pdf</NomeComponente>\n"
                + "						<HashVersato>6a02578085ab0f1ed373cb209ecf8c1a</HashVersato>\n"
                + "						<IDComponenteVersato>b3a48008-982f-47be-91b3-6b8fb6d2081d</IDComponenteVersato>\n"
                + "					</Componente>\n"
                + "				</Componenti>\n"
                + "			</StrutturaOriginale>\n"
                + "		</Annotazione>\n"
                + "		<Annotazione>\n"
                + "			<IDDocumento>8b1a8cbc-7044-4908-8b99-45c03d381519</IDDocumento>\n"
                + "			<TipoDocumento>SEGNATURA</TipoDocumento>\n"
                + "			<ProfiloDocumento>\n"
                + "				<Descrizione>segnatura.xml</Descrizione>\n"
                + "			</ProfiloDocumento>\n"
                + "			<StrutturaOriginale>\n"
                + "				<TipoStruttura>DocumentoGenerico</TipoStruttura>\n"
                + "				<Componenti>\n"
                + "					<Componente>\n"
                + "						<ID>1629702012751-88ZTdtdTAT</ID>\n"
                + "						<OrdinePresentazione>5</OrdinePresentazione>\n"
                + "						<TipoComponente>Contenuto</TipoComponente>\n"
                + "						<TipoSupportoComponente>FILE</TipoSupportoComponente>\n"
                + "						<NomeComponente>segnatura.xml</NomeComponente>\n"
                + "						<HashVersato>d0f5e8bbae49bba6711f2fdc9b508bd0</HashVersato>\n"
                + "						<IDComponenteVersato>8b1a8cbc-7044-4908-8b99-45c03d381519</IDComponenteVersato>\n"
                + "					</Componente>\n"
                + "				</Componenti>\n"
                + "			</StrutturaOriginale>\n"
                + "		</Annotazione>\n"
                + "	</Annotazioni>\n"
                + "</UnitaDocumentaria>");

        jo.put("command", "insert");

        c.doWork(jo, 0, new JSONArray(), null, null);
    }

    private static void testUno() throws WorkerException {
        log.info("testUno() ");
        SendToParerWorker c = new SendToParerWorker();

        JSONObject jo = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("fileName", "letterafirmata.pdf");
        jsonObject.put("mime", "application/pdf");
        jsonObject.put("id", "1643121601041-arJ1EUNhDQ");
        jsonObject.put("formatType", "PDF");
        jsonObject.put("uuidMongo", "e917da3d-dd78-4bec-8716-d4d5d727a0ee");
        jsonObject.put("fileBase64", null);
        jsonObject.put("hash", "1e736360b1535d1c1f84e42991f3c7b9");

        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("fileName", "destinatari.pdf");
        jsonObject2.put("mime", "application/pdf");
        jsonObject2.put("id", "1643121602222-A2bEJ1MQ8S");
        jsonObject2.put("formatType", "PDF");
        jsonObject2.put("uuidMongo", "702446a1-09a3-43a8-bfea-a018cf97219c");
        jsonObject2.put("fileBase64", null);
        jsonObject2.put("hash", "a46e48fdd280ef41545eb6427296e376");

        JSONObject jsonObject3 = new JSONObject();
        jsonObject3.put("fileName", "relata committente 1.pdf");
        jsonObject3.put("mime", "application/pdf");
        jsonObject3.put("id", "1643121602465-29dSatcMzN");
        jsonObject3.put("formatType", "PDF");
        jsonObject3.put("uuidMongo", "702446a1-09a3-43a8-bfea-a018cf97219c");
        jsonObject3.put("fileBase64", null);
        jsonObject3.put("hash", "");

        JSONObject jsonObject4 = new JSONObject();
        jsonObject4.put("fileName", "frontespizio.pdf");
        jsonObject4.put("mime", "application/pdf");
        jsonObject4.put("id", "1643121602742-kF4oNs9d63");
        jsonObject4.put("formatType", "PDF");
        jsonObject4.put("uuidMongo", "9396c66d-301f-4ff2-b9ea-131461231c40");
        jsonObject4.put("fileBase64", null);
        jsonObject4.put("hash", "6195653494df641fb92ea74b5ed9224a");

        JSONObject jsonObject5 = new JSONObject();
        jsonObject5.put("fileName", "segnatura.xml");
        jsonObject5.put("mime", "text/xml");
        jsonObject5.put("id", "1643121602796-sdEzigZ3Bw");
        jsonObject5.put("formatType", "XML");
        jsonObject5.put("uuidMongo", "20d93c82-8547-44d4-82b2-de96cafc688f");
        jsonObject5.put("fileBase64", null);
        jsonObject5.put("hash", "64caa01c08cb87d1efe8424363904167");

        jsonArray.add(jsonObject);
        jsonArray.add(jsonObject2);
        //jsonArray.add(jsonObject3);
        jsonArray.add(jsonObject4);
        jsonArray.add(jsonObject5);

        jo.put("identityFiles", jsonArray);
        jo.put("xmlDocument", "<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\" standalone=\\\"yes\\\"?>\n"
                + "<UnitaDocumentaria>\n"
                + "	<Intestazione>\n"
                + "		<Versione>1.4</Versione>\n"
                + "		<Versatore>\n"
                + "			<Ambiente>PARER_TEST</Ambiente>\n"
                + "			<Ente>AUSL_BO</Ente>\n"
                + "			<Struttura>ASL_BO</Struttura>\n"
                + "			<UserID>gedi_ausl_bo_pre</UserID>\n"
                + "		</Versatore>\n"
                + "		<Chiave>\n"
                + "			<Numero>33618411</Numero>\n"
                + "			<Anno>2022</Anno>\n"
                + "			<TipoRegistro>PG</TipoRegistro>\n"
                + "		</Chiave>\n"
                + "		<TipologiaUnitaDocumentaria>DOCUMENTO PROTOCOLLATO IN USCITA</TipologiaUnitaDocumentaria>\n"
                + "	</Intestazione>\n"
                + "	<Configurazione>\n"
                + "		<TipoConservazione>VERSAMENTO_ANTICIPATO</TipoConservazione>\n"
                + "		<ForzaAccettazione>false</ForzaAccettazione>\n"
                + "		<ForzaConservazione>false</ForzaConservazione>\n"
                + "		<ForzaCollegamento>false</ForzaCollegamento>\n"
                + "		<SimulaSalvataggioDatiInDB>false</SimulaSalvataggioDatiInDB>\n"
                + "	</Configurazione>\n"
                + "	<ProfiloArchivistico>\n"
                + "		<FascicoloPrincipale>\n"
                + "			<Classifica>02.02.05</Classifica>\n"
                + "			<Fascicolo>\n"
                + "				<Identificativo>2022/2</Identificativo>\n"
                + "				<Oggetto>Primo Fascicolo 2022</Oggetto>\n"
                + "			</Fascicolo>\n"
                + "		</FascicoloPrincipale>\n"
                + "	</ProfiloArchivistico>\n"
                + "	<ProfiloUnitaDocumentaria>\n"
                + "		<Oggetto>test 4</Oggetto>\n"
                + "		<Data>2022-01-10T15:32:00.000+01:00</Data>\n"
                + "		<Cartaceo>false</Cartaceo>\n"
                + "	</ProfiloUnitaDocumentaria>\n"
                + "	<DatiSpecifici>\n"
                + "		<VersioneDatiSpecifici>2.0</VersioneDatiSpecifici>\n"
                + "		<Destinatario>Vedi annesso elenco destinatari</Destinatario>\n"
                + "		<Movimento>OUT</Movimento>\n"
                + "		<ModalitaTrasmissione>BABEL</ModalitaTrasmissione>\n"
                + "		<ResponsabileDelProcedimento>Campa Rosanna (UO Servizio Acquisti  Metropolitano (SC))</ResponsabileDelProcedimento>\n"
                + "		<Firmatario>&amp;lt;nominativo&amp;gt;Campa Rosanna&amp;lt;/nominativo&amp;gt;</Firmatario>\n"
                + "		<DataFascicolazione>2022-01-10</DataFascicolazione>\n"
                + "		<IdentificazioneRepository>GEDI</IdentificazioneRepository>\n"
                + "		<Visibilita>LIBERA</Visibilita>\n"
                + "		<Consultabilita>ON PRECISATA</Consultabilita>\n"
                + "		<TipologiaAtto>on rilevante ai fini dell'applicazione dell'art. 23 del D.Lgs. 33/2013</TipologiaAtto>\n"
                + "	</DatiSpecifici>\n"
                + "	<NumeroAnnessi>2</NumeroAnnessi>\n"
                + "	<NumeroAnnotazioni>2</NumeroAnnotazioni>\n"
                + "	<DocumentoPrincipale>\n"
                + "		<IDDocumento>babel_suite_DE4ADE4D-D5DC-DFB8-014A-A637F18E0F5D</IDDocumento>\n"
                + "		<TipoDocumento>DOCUMENTO PROTOCOLLATO</TipoDocumento>\n"
                + "		<ProfiloDocumento>\n"
                + "			<Autore>&amp;lt;nominativo&amp;gt;Campa Rosanna&amp;lt;/nominativo&amp;gt;</Autore>\n"
                + "		</ProfiloDocumento>\n"
                + "		<StrutturaOriginale>\n"
                + "			<TipoStruttura>DocumentoGenerico</TipoStruttura>\n"
                + "			<Componenti>\n"
                + "				<Componente>\n"
                + "					<ID>1643121601041-arJ1EUNhDQ</ID>\n"
                + "					<OrdinePresentazione>1</OrdinePresentazione>\n"
                + "					<TipoComponente>Contenuto</TipoComponente>\n"
                + "					<TipoSupportoComponente>FILE</TipoSupportoComponente>\n"
                + "					<NomeComponente>letterafirmata.pdf</NomeComponente>\n"
                + "					<HashVersato>1e736360b1535d1c1f84e42991f3c7b9</HashVersato>\n"
                + "					<IDComponenteVersato>e917da3d-dd78-4bec-8716-d4d5d727a0ee</IDComponenteVersato>\n"
                + "					<UtilizzoDataFirmaPerRifTemp>false</UtilizzoDataFirmaPerRifTemp>\n"
                + "					<RiferimentoTemporale>2022-01-10T15:32:00.000+01:00</RiferimentoTemporale>\n"
                + "					<DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE</DescrizioneRiferimentoTemporale>\n"
                + "				</Componente>\n"
                + "			</Componenti>\n"
                + "		</StrutturaOriginale>\n"
                + "	</DocumentoPrincipale>\n"
                + "	<Annessi>\n"
                + "		<Annesso>\n"
                + "			<IDDocumento>702446a1-09a3-43a8-bfea-a018cf97219c</IDDocumento>\n"
                + "			<TipoDocumento>ELENCO DESTINATARI</TipoDocumento>\n"
                + "			<ProfiloDocumento>\n"
                + "				<Descrizione>Destinatarin</Descrizione>\n"
                + "			</ProfiloDocumento>\n"
                + "			<StrutturaOriginale>\n"
                + "				<TipoStruttura>DocumentoGenerico</TipoStruttura>\n"
                + "				<Componenti>\n"
                + "					<Componente>\n"
                + "						<ID>1643121602222-A2bEJ1MQ8S</ID>\n"
                + "						<OrdinePresentazione>2</OrdinePresentazione>\n"
                + "						<TipoComponente>Contenuto</TipoComponente>\n"
                + "						<TipoSupportoComponente>FILE</TipoSupportoComponente>\n"
                + "						<NomeComponente>destinatari.pdf</NomeComponente>\n"
                + "						<HashVersato>a46e48fdd280ef41545eb6427296e376</HashVersato>\n"
                + "						<IDComponenteVersato>702446a1-09a3-43a8-bfea-a018cf97219c</IDComponenteVersato>\n"
                + "						<UtilizzoDataFirmaPerRifTemp>false</UtilizzoDataFirmaPerRifTemp>\n"
                + "						<RiferimentoTemporale>2022-01-10T15:32:00.000+01:00</RiferimentoTemporale>\n"
                + "						<DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE</DescrizioneRiferimentoTemporale>\n"
                + "					</Componente>\n"
                + "				</Componenti>\n"
                + "			</StrutturaOriginale>\n"
                + "		</Annesso>\n"
                + "	</Annessi>\n"
                + "	<Annotazioni>\n"
                + "		<Annotazione>\n"
                + "			<IDDocumento>9396c66d-301f-4ff2-b9ea-131461231c40</IDDocumento>\n"
                + "			<TipoDocumento>FRONTESPIZIO</TipoDocumento>\n"
                + "			<ProfiloDocumento>\n"
                + "				<Descrizione>Frontespizion</Descrizione>\n"
                + "			</ProfiloDocumento>\n"
                + "			<StrutturaOriginale>\n"
                + "				<TipoStruttura>DocumentoGenerico</TipoStruttura>\n"
                + "				<Componenti>\n"
                + "					<Componente>\n"
                + "						<ID>1643121602742-kF4oNs9d63</ID>\n"
                + "						<OrdinePresentazione>4</OrdinePresentazione>\n"
                + "						<TipoComponente>Contenuto</TipoComponente>\n"
                + "						<TipoSupportoComponente>FILE</TipoSupportoComponente>\n"
                + "						<NomeComponente>frontespizio.pdf</NomeComponente>\n"
                + "						<HashVersato>6195653494df641fb92ea74b5ed9224a</HashVersato>\n"
                + "						<IDComponenteVersato>9396c66d-301f-4ff2-b9ea-131461231c40</IDComponenteVersato>\n"
                + "					</Componente>\n"
                + "				</Componenti>\n"
                + "			</StrutturaOriginale>\n"
                + "		</Annotazione>\n"
                + "		<Annotazione>\n"
                + "			<IDDocumento>20d93c82-8547-44d4-82b2-de96cafc688f</IDDocumento>\n"
                + "			<TipoDocumento>SEGNATURA</TipoDocumento>\n"
                + "			<ProfiloDocumento>\n"
                + "				<Descrizione>segnatura.xml</Descrizione>\n"
                + "			</ProfiloDocumento>\n"
                + "			<StrutturaOriginale>\n"
                + "				<TipoStruttura>DocumentoGenerico</TipoStruttura>\n"
                + "				<Componenti>\n"
                + "					<Componente>\n"
                + "						<ID>1643121602796-sdEzigZ3Bw</ID>\n"
                + "						<OrdinePresentazione>5</OrdinePresentazione>\n"
                + "						<TipoComponente>Contenuto</TipoComponente>\n"
                + "						<TipoSupportoComponente>FILE</TipoSupportoComponente>\n"
                + "						<NomeComponente>segnatura.xml</NomeComponente>\n"
                + "						<HashVersato>64caa01c08cb87d1efe8424363904167</HashVersato>\n"
                + "						<IDComponenteVersato>20d93c82-8547-44d4-82b2-de96cafc688f</IDComponenteVersato>\n"
                + "					</Componente>\n"
                + "				</Componenti>\n"
                + "			</StrutturaOriginale>\n"
                + "		</Annotazione>\n"
                + "	</Annotazioni>\n"
                + "</UnitaDocumentaria>");
        jo.put("command", "insert");

        c.doWork(jo, 0, new JSONArray(), null, null);
    }

    private static void testTre() throws WorkerException, ParseException {
        String obj = "{\"identityFiles\":[{\"fileName\":\"allegato principale\",\"mime\":\"image\\/png\",\"id\":\"1646058318722-fN9LgwxROM\",\"formatType\":\"PNG\",\"uuidMongo\":\"246750f1-4bbb-4a9f-ae89-27ae54dc39cb\",\"fileBase64\":null,\"hash\":\"76a456043546b42b586dea2c7c1213cc\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058318886-z757ux8APL\",\"formatType\":\"PDF\",\"uuidMongo\":\"431991ed-2175-4adc-b306-8e3f584993f6\",\"fileBase64\":null,\"hash\":\"32c501020b4efe3e963606692db0ddaa\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058318927-pU5TkPmbrF\",\"formatType\":\"PDF\",\"uuidMongo\":\"0a938d78-9c96-43fe-940b-394e1f872c84\",\"fileBase64\":null,\"hash\":\"ffc24c385ff5c717facbbaa71a3aa431\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058318972-SdDLKj1F9q\",\"formatType\":\"PDF\",\"uuidMongo\":\"0739808c-6e2f-4e01-9a92-5c9114284aa6\",\"fileBase64\":null,\"hash\":\"cbbb9fbf34999756e1bdee9037f04351\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319008-OwkUm7vGjG\",\"formatType\":\"PDF\",\"uuidMongo\":\"8ac6e0c7-4b9a-43ef-ac8c-4c3ce740b678\",\"fileBase64\":null,\"hash\":\"0844be2f49e107dfe3147f8c984a720e\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319043-lVfny9Y6Gd\",\"formatType\":\"PDF\",\"uuidMongo\":\"d1fe5424-fdcc-45d9-b7dc-ee3e4b2aff6b\",\"fileBase64\":null,\"hash\":\"7a0ab1b03721e9321f540f128c76d1cd\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319080-8JRxodD5Uf\",\"formatType\":\"PDF\",\"uuidMongo\":\"71be489b-9b81-4695-a2ad-3d835fa81872\",\"fileBase64\":null,\"hash\":\"e807e268b02bf6b96578baf27ed45235\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319115-kv0aOy34uw\",\"formatType\":\"PDF\",\"uuidMongo\":\"f55edb46-26c5-47f1-9230-71131714850a\",\"fileBase64\":null,\"hash\":\"d310b658030611a787d948bf9b6294be\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319156-OgcuROhSV3\",\"formatType\":\"PDF\",\"uuidMongo\":\"7a7fa3cf-8463-471d-8786-348ab2d49d2b\",\"fileBase64\":null,\"hash\":\"b3d189a4b0ba1767994eb5d30b4ec4ab\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319194-w00cxsRGLl\",\"formatType\":\"PDF\",\"uuidMongo\":\"79ddf80a-9f5a-43fd-9a4f-394bd5f5b872\",\"fileBase64\":null,\"hash\":\"6e716274f80dbe05c243b41be009eb78\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319229-fIHHVMyf5o\",\"formatType\":\"PDF\",\"uuidMongo\":\"ee949aa6-1e68-45cf-80c1-88c966099ece\",\"fileBase64\":null,\"hash\":\"7febc2b95ec59f6f9c780db7a3545043\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319267-pIIPe21wW6\",\"formatType\":\"PDF\",\"uuidMongo\":\"9766fc45-d09a-4b3c-93ca-f2be81bf9cb7\",\"fileBase64\":null,\"hash\":\"5d0557382be3778da00074d03fb88be3\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319303-1GUFggLme7\",\"formatType\":\"PDF\",\"uuidMongo\":\"42c8ffab-123f-440e-844d-6e830a5a3503\",\"fileBase64\":null,\"hash\":\"80a4a4b9a08250be80896809b9ca033a\"},{\"fileName\":\"allegato_pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319339-JAImEwxKn6\",\"formatType\":\"PDF\",\"uuidMongo\":\"25069371-6aab-4a64-9c25-c0aef1f09cbe\",\"fileBase64\":null,\"hash\":\"274f102345d43200d352fe6f7e6638fd\"},{\"fileName\":\"frontespizio.pdf\",\"mime\":\"application\\/pdf\",\"id\":\"1646058319343-Voxw0MrF8I\",\"formatType\":\"PDF\",\"uuidMongo\":\"c7f64c2c-c9e6-47e7-b6d2-536eaa370426\",\"fileBase64\":null,\"hash\":\"c3cd511f33e87669cf1eb02e1e6b2920\"},{\"fileName\":\"segnatura.xml\",\"mime\":\"text\\/xml\",\"id\":\"1646058319344-Uv7SuaBlXI\",\"formatType\":\"XML\",\"uuidMongo\":\"40af43b5-657b-441b-b33b-67344fb961c4\",\"fileBase64\":null,\"hash\":\"421b57a485a2a967b131301244948c24\"}],\"xmlDocument\":\"<?xml version=\\\"1.0\\\" encoding=\\\"ISO-8859-1\\\" standalone=\\\"yes\\\"?>\\n<UnitaDocumentaria>\\n    <Intestazione>\\n        <Versione>1.4<\\/Versione>\\n        <Versatore>\\n            <Ambiente>PARER_TEST<\\/Ambiente>\\n            <Ente>AUSL_BO<\\/Ente>\\n            <Struttura>ASL_BO<\\/Struttura>\\n            <UserID>gedi_ausl_bo_pre<\\/UserID>\\n        <\\/Versatore>\\n        <Chiave>\\n            <Numero>63450136<\\/Numero>\\n            <Anno>2022<\\/Anno>\\n            <TipoRegistro>PG<\\/TipoRegistro>\\n        <\\/Chiave>\\n        <TipologiaUnitaDocumentaria>DOCUMENTO PROTOCOLLATO IN ENTRATA<\\/TipologiaUnitaDocumentaria>\\n    <\\/Intestazione>\\n    <Configurazione>\\n        <TipoConservazione>VERSAMENTO_ANTICIPATO<\\/TipoConservazione>\\n        <ForzaAccettazione>true<\\/ForzaAccettazione>\\n        <ForzaConservazione>true<\\/ForzaConservazione>\\n        <ForzaCollegamento>false<\\/ForzaCollegamento>\\n        <SimulaSalvataggioDatiInDB>false<\\/SimulaSalvataggioDatiInDB>\\n    <\\/Configurazione>\\n    <ProfiloArchivistico>\\n        <FascicoloPrincipale>\\n            <Classifica>07.02.01<\\/Classifica>\\n            <Fascicolo>\\n                <Identificativo>2019\\/250<\\/Identificativo>\\n                <Oggetto>LS Fascicolo Da Sanare con Sottofascicoli<\\/Oggetto>\\n            <\\/Fascicolo>\\n            <SottoFascicolo>\\n                <Identificativo>2019\\/250\\/2022<\\/Identificativo>\\n                <Oggetto>SELLOUM - JANNET - 10\\/08\\/2016<\\/Oggetto>\\n            <\\/SottoFascicolo>\\n        <\\/FascicoloPrincipale>\\n    <\\/ProfiloArchivistico>\\n    <ProfiloUnitaDocumentaria>\\n        <Oggetto>test super kool<\\/Oggetto>\\n        <Data>2022-02-21T15:04:00.000+01:00<\\/Data>\\n        <Cartaceo>false<\\/Cartaceo>\\n    <\\/ProfiloUnitaDocumentaria>\\n    <DatiSpecifici>\\n        <VersioneDatiSpecifici>2.0<\\/VersioneDatiSpecifici>\\n        <Mittente>l.salomone@nsi.it<\\/Mittente>\\n        <Movimento>IN<\\/Movimento>\\n        <ModalitaTrasmissione>BABEL<\\/ModalitaTrasmissione>\\n        <OperatoreDiProtocollo>l.salomone<\\/OperatoreDiProtocollo>\\n        <DataFascicolazione>2022-02-21<\\/DataFascicolazione>\\n        <IdentificazioneRepository>GEDI<\\/IdentificazioneRepository>\\n        <Visibilita>LIBERA<\\/Visibilita>\\n        <Consultabilita>NON PRECISATA<\\/Consultabilita>\\n    <\\/DatiSpecifici>\\n    <NumeroAllegati>13<\\/NumeroAllegati>\\n    <NumeroAnnotazioni>2<\\/NumeroAnnotazioni>\\n    <DocumentoPrincipale>\\n        <IDDocumento>babel_suite_33EDE14B-396E-31A9-C58D-428FC2811468<\\/IDDocumento>\\n        <TipoDocumento>DOCUMENTO PROTOCOLLATO<\\/TipoDocumento>\\n        <ProfiloDocumento\\/>\\n        <StrutturaOriginale>\\n            <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n            <Componenti>\\n                <Componente>\\n                    <ID>1646058318722-fN9LgwxROM<\\/ID>\\n                    <OrdinePresentazione>1<\\/OrdinePresentazione>\\n                    <TipoComponente>Contenuto<\\/TipoComponente>\\n                    <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                    <NomeComponente>allegato principale<\\/NomeComponente>\\n                    <HashVersato>76a456043546b42b586dea2c7c1213cc<\\/HashVersato>\\n                    <IDComponenteVersato>246750f1-4bbb-4a9f-ae89-27ae54dc39cb<\\/IDComponenteVersato>\\n                    <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                    <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                    <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                <\\/Componente>\\n            <\\/Componenti>\\n        <\\/StrutturaOriginale>\\n    <\\/DocumentoPrincipale>\\n    <Allegati>\\n        <Allegato>\\n            <IDDocumento>431991ed-2175-4adc-b306-8e3f584993f6<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_dete-deli senza cruscotto.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058318886-z757ux8APL<\\/ID>\\n                        <OrdinePresentazione>2<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>32c501020b4efe3e963606692db0ddaa<\\/HashVersato>\\n                        <IDComponenteVersato>431991ed-2175-4adc-b306-8e3f584993f6<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>0a938d78-9c96-43fe-940b-394e1f872c84<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_Petraccaro NOn &amp;egrave; osservatore.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058318927-pU5TkPmbrF<\\/ID>\\n                        <OrdinePresentazione>3<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>ffc24c385ff5c717facbbaa71a3aa431<\\/HashVersato>\\n                        <IDComponenteVersato>0a938d78-9c96-43fe-940b-394e1f872c84<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>0739808c-6e2f-4e01-9a92-5c9114284aa6<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_forse son tornati.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058318972-SdDLKj1F9q<\\/ID>\\n                        <OrdinePresentazione>4<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>cbbb9fbf34999756e1bdee9037f04351<\\/HashVersato>\\n                        <IDComponenteVersato>0739808c-6e2f-4e01-9a92-5c9114284aa6<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>8ac6e0c7-4b9a-43ef-ac8c-4c3ce740b678<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_problemi di affidabilit&amp;agrave; del\\ncertificato.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319008-OwkUm7vGjG<\\/ID>\\n                        <OrdinePresentazione>5<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>0844be2f49e107dfe3147f8c984a720e<\\/HashVersato>\\n                        <IDComponenteVersato>8ac6e0c7-4b9a-43ef-ac8c-4c3ce740b678<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>d1fe5424-fdcc-45d9-b7dc-ee3e4b2aff6b<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_firme presenti.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319043-lVfny9Y6Gd<\\/ID>\\n                        <OrdinePresentazione>6<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>7a0ab1b03721e9321f540f128c76d1cd<\\/HashVersato>\\n                        <IDComponenteVersato>d1fe5424-fdcc-45d9-b7dc-ee3e4b2aff6b<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>71be489b-9b81-4695-a2ad-3d835fa81872<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_problemi firma ubertini.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319080-8JRxodD5Uf<\\/ID>\\n                        <OrdinePresentazione>7<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>e807e268b02bf6b96578baf27ed45235<\\/HashVersato>\\n                        <IDComponenteVersato>71be489b-9b81-4695-a2ad-3d835fa81872<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>f55edb46-26c5-47f1-9230-71131714850a<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_problemi firma messori.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319115-kv0aOy34uw<\\/ID>\\n                        <OrdinePresentazione>8<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>d310b658030611a787d948bf9b6294be<\\/HashVersato>\\n                        <IDComponenteVersato>f55edb46-26c5-47f1-9230-71131714850a<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>7a7fa3cf-8463-471d-8786-348ab2d49d2b<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_applicazione in fase di avvio.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319156-OgcuROhSV3<\\/ID>\\n                        <OrdinePresentazione>9<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>b3d189a4b0ba1767994eb5d30b4ec4ab<\\/HashVersato>\\n                        <IDComponenteVersato>7a7fa3cf-8463-471d-8786-348ab2d49d2b<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>79ddf80a-9f5a-43fd-9a4f-394bd5f5b872<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_iter creato.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319194-w00cxsRGLl<\\/ID>\\n                        <OrdinePresentazione>10<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>6e716274f80dbe05c243b41be009eb78<\\/HashVersato>\\n                        <IDComponenteVersato>79ddf80a-9f5a-43fd-9a4f-394bd5f5b872<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>ee949aa6-1e68-45cf-80c1-88c966099ece<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_casella non attiva.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319229-fIHHVMyf5o<\\/ID>\\n                        <OrdinePresentazione>11<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>7febc2b95ec59f6f9c780db7a3545043<\\/HashVersato>\\n                        <IDComponenteVersato>ee949aa6-1e68-45cf-80c1-88c966099ece<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>9766fc45-d09a-4b3c-93ca-f2be81bf9cb7<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_10 fascicoli importati.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319267-pIIPe21wW6<\\/ID>\\n                        <OrdinePresentazione>12<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>5d0557382be3778da00074d03fb88be3<\\/HashVersato>\\n                        <IDComponenteVersato>9766fc45-d09a-4b3c-93ca-f2be81bf9cb7<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>42c8ffab-123f-440e-844d-6e830a5a3503<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_errore in esempio.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319303-1GUFggLme7<\\/ID>\\n                        <OrdinePresentazione>13<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>80a4a4b9a08250be80896809b9ca033a<\\/HashVersato>\\n                        <IDComponenteVersato>42c8ffab-123f-440e-844d-6e830a5a3503<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n        <Allegato>\\n            <IDDocumento>25069371-6aab-4a64-9c25-c0aef1f09cbe<\\/IDDocumento>\\n            <TipoDocumento>GENERICO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>PG0000055_2022_components.png_pdf\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319339-JAImEwxKn6<\\/ID>\\n                        <OrdinePresentazione>14<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>allegato_pdf<\\/NomeComponente>\\n                        <HashVersato>274f102345d43200d352fe6f7e6638fd<\\/HashVersato>\\n                        <IDComponenteVersato>25069371-6aab-4a64-9c25-c0aef1f09cbe<\\/IDComponenteVersato>\\n                        <UtilizzoDataFirmaPerRifTemp>false<\\/UtilizzoDataFirmaPerRifTemp>\\n                        <RiferimentoTemporale>2022-02-21T15:04:00.000+01:00<\\/RiferimentoTemporale>\\n                        <DescrizioneRiferimentoTemporale>DATA_DI_PROTOCOLLAZIONE<\\/DescrizioneRiferimentoTemporale>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Allegato>\\n    <\\/Allegati>\\n    <Annotazioni>\\n        <Annotazione>\\n            <IDDocumento>c7f64c2c-c9e6-47e7-b6d2-536eaa370426<\\/IDDocumento>\\n            <TipoDocumento>FRONTESPIZIO<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>Frontespizio\\n<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319343-Voxw0MrF8I<\\/ID>\\n                        <OrdinePresentazione>15<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>frontespizio.pdf<\\/NomeComponente>\\n                        <HashVersato>c3cd511f33e87669cf1eb02e1e6b2920<\\/HashVersato>\\n                        <IDComponenteVersato>c7f64c2c-c9e6-47e7-b6d2-536eaa370426<\\/IDComponenteVersato>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Annotazione>\\n        <Annotazione>\\n            <IDDocumento>40af43b5-657b-441b-b33b-67344fb961c4<\\/IDDocumento>\\n            <TipoDocumento>SEGNATURA<\\/TipoDocumento>\\n            <ProfiloDocumento>\\n                <Descrizione>segnatura.xml<\\/Descrizione>\\n            <\\/ProfiloDocumento>\\n            <StrutturaOriginale>\\n                <TipoStruttura>DocumentoGenerico<\\/TipoStruttura>\\n                <Componenti>\\n                    <Componente>\\n                        <ID>1646058319344-Uv7SuaBlXI<\\/ID>\\n                        <OrdinePresentazione>16<\\/OrdinePresentazione>\\n                        <TipoComponente>Contenuto<\\/TipoComponente>\\n                        <TipoSupportoComponente>FILE<\\/TipoSupportoComponente>\\n                        <NomeComponente>segnatura.xml<\\/NomeComponente>\\n                        <HashVersato>421b57a485a2a967b131301244948c24<\\/HashVersato>\\n                        <IDComponenteVersato>40af43b5-657b-441b-b33b-67344fb961c4<\\/IDComponenteVersato>\\n                    <\\/Componente>\\n                <\\/Componenti>\\n            <\\/StrutturaOriginale>\\n        <\\/Annotazione>\\n    <\\/Annotazioni>\\n<\\/UnitaDocumentaria>\\n\",\"command\":\"insert\"}";
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(obj);
        SendToParerWorker c = new SendToParerWorker();
        c.doWork(jsonObject, 0, new JSONArray(), null, null);
    }

    public static void main(String[] args) throws ClassNotFoundException, WorkerException, ParseException {
        try {
            log.info("Nel main");
            //testUno();
            //testDue();
            testTre();
        } catch (Throwable t) {
            log.error("Errore durante il test", t);
            t.printStackTrace();
        }
    }

}
