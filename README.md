# eIDAS kliendi integratsioonitestid

[eIDAS klient makettrakenduse](https://github.com/e-gov/eIDAS-Client) raames on loodud automaatsete testide komplekt mida on võimalik kasutada eIDAS sõlmega liidestumise testimiseks. Testid on mõeldud eIDAS klient makettrakenduse testimiseks, kuid on osaliselt korduvkasutatavad (näiteks metaandmete publitseerimise kontrollimiseks). Samuti saab neid teste kasutada enda klientrakenduse testimiseks ideede allikana või enda klientrakenduse jaoks kohandada. 

**NB! Antud testid on arenduses ning  muutuvad projekti edenedes.**

## Testide seadistamine ja käivitamine

Vajalik on Java VM eelnev installatsioon. Arenduseks on kasutatud Oracle Java jdk 1.8.0_161 versiooni.

1. Hangi eIDAS kliendi lähtekood ning käivita eIDAS klient (vajalik eIDAS kliendi makettrakendus testimiseks). eIDAS kliendi makettrakenduse koodi ja käivitamise juhendid leiab [GitHubist](https://github.com/e-gov/eIDAS-Client). NB! eIDAS klient vajab käivitamisel ka juurdepääsu eIDAS sõlmele (eIDAS Node). Kui kasutate teste enda klient lahenduse testimiseks peab klientrakendus töötama ning olema kättesaadav.
2. Hangi eIDAS kliendi testid:

 `git clone https://github.com/e-gov/eIDAS-Client-Test.git`

3. Seadista testid vastavaks testitava klient rakenduse otspunktidele. Selleks on kaks võimalust:

a) Võimalik on ette anda kahe erineva "profiili" properties faile "dev" ja "test" - vastavad properties failid [application-dev.properties](https://github.com/e-gov/eIDAS-Client-Test/blob/master/src/test/resources/application-dev.properties) ja [application-test.properties](https://github.com/e-gov/eIDAS-Client-Test/blob/master/src/test/resources/application-test.properties). Vaikeväärtusena on kasutusel profiil "dev", kuid seda on võimalik käivitamisel muuta parameetriga. Vaikeväärtused on seadistatud [application.properties](https://github.com/e-gov/eIDAS-Client-Test/blob/master/src/test/resources/application.properties) failis.

b) Andes vastavad parameetrid ette testide käivitamisel (kirjeldus testide käivitamise punktis)

Parameetrite kirjeldus:

| Parameeter | Vaikeväärtus | Vajalik korduvkasutatavatele testidele | Kirjeldus |
|------------|--------------|----------------------------------------|-----------|
| test.client.url | http://localhost:8889 | Jah | Testitava klientrakenduse Url ja port. |
| test.client.demoUrl | http://localhost:8889 | Ei | Demorakenduse Url ja port. Sama url mis kajastub metateabe otspunktis. |
| test.client.spMetadataUrl | /metadata | Jah | eIDAS kliendi metaandmete otspunkt. |
| test.client.spStartUrl | /login | Ei | eIDAS kliendi autentimise alustamise otspunkt. |
| test.client.spReturnUrl | /returnUrl | Ei | eIDAS kliendi autentimise vastuse otspunkt. |
| test.client.spProviderName | EIDAS KLIENT DEMO | Jah | eIDAS kliendi nimi mida reklaamitakse metaandmetes. |
| test.client.acceptableTimeDiffMin | 5 | Ei | Vastuses tagastatava kehtivuse ajaperioodi pikkus. Peab olema sünkroonis kliendi seadistustega. |
| test.node.idpUrl | http://localhost:8080 |  Ei |eIDAS nodei url ja port. |
| test.node.idpMetadataUrl | /EidasNode/ConnectorResponderMetadata |  Ei |eIDAS nodei metateabe otspunkt. |
| test.node.idpStartUrl | /EidasNode/ServiceProvider |  Ei |eIDAS nodei otspunkt kuhu klient päringu saadab. |
| test.keystore | classpath:samlKeystore.jks | Ei | Võtmehoidla asukoht testides kasutatavate võtmete hoidmiseks. |
| test.keystorePass | changeit | Ei | Võtmehoidla parool. |
| test.node.responseSigningKeyId | test_sign | Ei | Võtmehoidlas oleva võtme alias mida kasutatakse SAML vastuse allkirjastamiseks. eIDAS sõlme vastuse simuleerimiseks. |
| test.node.responseSigningKeyPass | changeit | Ei | Võtme parool. |

4. Käivita testid:

a) eIDAS klient makettrakenduse testimiseks käivita kõik testid

`./mvnw clean test`

b) Enda eIDAS klient rakenduse testimiseks käivita ainult korduvkasutatavad testid ("common" prefiksiga testiklassid)

`./mvnw -Dtest=Common* clean test`

Testidele parameetrite ette andmine käivitamisel:

`./mvnw clean test -Dtarget.url=http://localhost:1881`

5. Kontrolli testide tulemusi

a) Testid väljastavad raporti ja logi jooksvalt käivituskonsoolis

b) Surefire pistikprogramm väljastab tulemuste raporti ../target/surefire-reports kausta. Võimalik on genereerida ka html kujul koondraport. Selleks käivitada peale testide käivitamist käsk:

`./mvnw surefire-report:report-only`

Html raport on leitav ../target/site/ kaustast.
