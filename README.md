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

`-Dspring.profiles.active=test`

b) Andes vastavad parameetrid ette testide käivitamisel (kirjeldus testide käivitamise punktis)

Parameetrite kirjeldus:

**sp** - Service Provider, teenus kes kasutab eIDAS autentimise võrgustikku. Praegusel juhul [eIDAS kliendi demo rakendus](https://github.com/e-gov/eIDAS-Client-demo).

**idp** - Identity Provider, teenus kes pakub autentimist. Praegusel juhul eIDAS sõlm (eIDAS Node).

| Parameeter | Vaikeväärtus | Vajalik korduvkasutatavatele testidele | Kirjeldus |
|------------|--------------|----------------------------------------|-----------|
| test.client.targetUrl | http://localhost:8889 | Jah | Testitava klientrakenduse Url ja port. SAML vastuses kasutatavad URLid loetakse metaandmetest. |
| test.client.spMetadataUrl | /metadata | Jah | Teenuse metaandmete otspunkt. |
| test.client.spStartUrl | /login | Ei | Teenuse autentimise alustamise otspunkt. |
| test.client.spReturnUrl | /returnUrl | Ei | Teenuse autentimise vastuse otspunkt. |
| test.client.spProviderName | EIDAS KLIENT DEMO | Jah | Teenuse nimi mida reklaamitakse metaandmetes. |
| test.client.acceptableTimeDiffMin | 5 | Ei | Vastuses tagastatava kehtivuse ajaperioodi pikkus. Peab olema sünkroonis kliendi seadistustega. |
| test.client.idpUrl | http://localhost:8080 |  Ei | eIDAS sõlme url ja port. |
| test.client.idpMetadataUrl | /EidasNode/ConnectorResponderMetadata |  Ei |eIDAS sõlme metateabe otspunkt. |
| test.client.idpStartUrl  | /EidasNode/ServiceProvider |  Ei | eIDAS sõlme autentimise alustamise otspunkt. |
| test.client.keystore | classpath:samlKeystore.jks | Ei | Võtmehoidla asukoht testides kasutatavate võtmete hoidmiseks. |
| test.client.keystorePass | changeit | Ei | Võtmehoidla parool. |
| test.client.responseSigningKeyId | test_sign | Ei | Võtmehoidlas oleva võtme alias mida kasutatakse SAML vastuse allkirjastamiseks. eIDAS sõlme vastuse simuleerimiseks. |
| test.client.responseSigningKeyPass | changeit | Ei | Võtme parool. |
| test.client.healthcheckUrl | http://localhost:8889/heartbeat | Ei | Elutukse otspuntki URL. |
| test.client.supportedCountriesUrl | http://localhost:8889/supportedCountries | Ei | Toetatud riikide otspuntki URL. |

4. Käivita testid:

a) eIDAS klient makettrakenduse testimiseks käivita kõik testid

`./mvnw clean test`

b) Enda eIDAS klient rakenduse testimiseks käivita ainult korduvkasutatavad testid ("common" prefiksiga testiklassid)

`./mvnw -Dtest=Common* clean test`

Testidele parameetrite ette andmine käivitamisel:

`./mvnw clean test -Dtest.client.targetUrl=http://localhost:1881`

5. Kontrolli testide tulemusi

a) Testid väljastavad raporti ja logi jooksvalt käivituskonsoolis

b) Surefire pistikprogramm väljastab tulemuste raporti ../target/surefire-reports kausta. Võimalik on genereerida ka html kujul koondraport. Selleks käivitada peale testide käivitamist käsk:

`./mvnw surefire-report:report-only`

Html raport on leitav ../target/site/ kaustast.
