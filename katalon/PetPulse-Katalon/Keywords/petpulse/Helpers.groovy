package petpulse

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testobject.ConditionType
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.util.KeywordUtil
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import internal.GlobalVariable

/**
 * Funções compartilhadas pelos testes de interface do PetPulse-mobile:
 * localizadores das telas (por texto/placeholder, sem depender do Object Repository),
 * login no app e preparação/limpeza dos dados de teste pela API.
 */
class Helpers {

	// ------------------------------------------------------------------
	// Localizadores
	// ------------------------------------------------------------------

	static TestObject xpath(String expressao) {
		TestObject objeto = new TestObject(expressao)
		objeto.addProperty('xpath', ConditionType.EQUALS, expressao)
		return objeto
	}

	/** Elemento cujo texto é exatamente o informado (botões, títulos, nomes). */
	static TestObject texto(String valor) {
		return xpath("//*[@text='${valor}']")
	}

	/** Elemento cujo texto contém o valor informado. */
	static TestObject textoContem(String valor) {
		return xpath("//*[contains(@text,'${valor}')]")
	}

	/** Campo de texto identificado pelo placeholder. */
	static TestObject campo(String placeholder) {
		return xpath("//android.widget.EditText[@hint='${placeholder}' or @text='${placeholder}']")
	}

	/** Título, mensagem e botão OK das caixas de diálogo nativas (Alert.alert do React Native). */
	// O app usa o diálogo do AppCompat (id alert_title); alertTitle é o do diálogo nativo
	static TestObject dialogoTitulo() { return xpath("//*[contains(@resource-id,'alert_title') or contains(@resource-id,'alertTitle')]") }
	static TestObject dialogoMensagem() { return xpath("//*[@resource-id='android:id/message']") }
	static TestObject dialogoOk() { return xpath("//*[@resource-id='android:id/button1']") }

	// ------------------------------------------------------------------
	// Ações no app
	// ------------------------------------------------------------------

	/** Abre o app instalado do zero (sem sessão salva) e espera a tela de login. */
	@Keyword
	static void abrirApp() {
		Mobile.startApplication(GlobalVariable.APK_PATH, true)
		permitirNotificacoes()
		Mobile.waitForElementPresent(texto('Entre na sua conta'), 60)
	}

	/** Aceita o pedido de permissão de notificações do Android 13+, se aparecer em até `segundos`. */
	@Keyword
	static void permitirNotificacoes(int segundos = 3) {
		TestObject permitir = xpath("//*[@resource-id='com.android.permissioncontroller:id/permission_allow_button']")
		if (Mobile.waitForElementPresent(permitir, segundos, FailureHandling.OPTIONAL)) {
			Mobile.tap(permitir, 5)
		}
	}

	/** Preenche a tela de login. perfil = 'Tutor' ou 'Veterinário'. */
	@Keyword
	static void login(String perfil, String email, String senha) {
		Mobile.tap(texto(perfil), 10)
		Mobile.setText(campo('Insira seu email'), email, 10)
		Mobile.setText(campo('Senha'), senha, 10)
		Mobile.hideKeyboard(FailureHandling.OPTIONAL)
		Mobile.tap(texto('Login'), 10)
	}

	// ------------------------------------------------------------------
	// Dados de teste pela API (pré-condições controladas)
	// ------------------------------------------------------------------

	static Object api(String metodo, String caminho, Object corpo = null, String token = null) {
		HttpURLConnection conexao = (HttpURLConnection) new URL(GlobalVariable.API_URL + caminho).openConnection()
		conexao.setRequestMethod(metodo)
		// A API está no plano gratuito do Render: a primeira chamada pode levar minutos
		conexao.setConnectTimeout(300000)
		conexao.setReadTimeout(300000)
		conexao.setRequestProperty('Content-Type', 'application/json')
		if (token) {
			conexao.setRequestProperty('Authorization', 'Bearer ' + token)
		}
		if (corpo != null) {
			conexao.setDoOutput(true)
			conexao.getOutputStream().withWriter('UTF-8') { it << JsonOutput.toJson(corpo) }
		}
		int status = conexao.getResponseCode()
		InputStream resposta = status < 400 ? conexao.getInputStream() : conexao.getErrorStream()
		String texto = resposta?.getText('UTF-8')
		if (status >= 400) {
			throw new RuntimeException("${metodo} ${caminho} -> HTTP ${status}: ${texto}")
		}
		return texto ? new JsonSlurper().parseText(texto) : null
	}

	static String token(String email) {
		return api('POST', '/login', [email: email, password: GlobalVariable.SENHA]).token
	}

	/**
	 * Cria o pet "Bidu QA" do tutor de teste com uma coleira IoT.
	 * Com comAlerta = true envia uma leitura com frequência cardíaca 195,
	 * o que faz a API gerar o alerta FREQUENCIA_CARDIACA de risco ALTO.
	 */
	@Keyword
	static Map prepararPet(boolean comAlerta) {
		String tokenTutor = token(GlobalVariable.TUTOR_EMAIL)
		def pet = api('POST', '/pets', [
			name: 'Bidu QA', birthDate: '2023-03-15', weight: 12.5, sex: 'M', neutered: true,
			tutorId: GlobalVariable.TUTOR_ID, speciesId: 1, breedId: 1, petSizeId: 2
		], tokenTutor)
		def coleira = api('POST', '/iot-devices', [
			petId: pet.id, collectionIntervalMinutes: 5, heartRate: 90, activityLevel: 5, pressure: 12, status: 'ATIVO'
		], tokenTutor)
		if (comAlerta) {
			api('POST', '/iot-readings', [deviceId: coleira.id, heartRate: 195, activityLevel: 4, pressure: 12], tokenTutor)
		}
		KeywordUtil.logInfo("Dados de teste criados: pet ${pet.id}, coleira ${coleira.id}")
		return [petId: pet.id, deviceId: coleira.id]
	}

	/** Remove o pet de teste e tudo o que foi gerado para ele (alertas, leituras, histórico, coleira). */
	@Keyword
	static void limparPet(Map dados) {
		if (!dados) return
		String tokenTutor = token(GlobalVariable.TUTOR_EMAIL)
		def resumo = api('GET', "/pets/${dados.petId}/health-summary", null, tokenTutor)
		resumo.alerts.each { api('DELETE', "/smart-alerts/${it.id}", null, tokenTutor) }
		resumo.latestIoTReadings.each { api('DELETE', "/iot-readings/${it.id}", null, tokenTutor) }
		resumo.clinicalHistories.each { api('DELETE', "/clinical-histories/${it.id}", null, tokenTutor) }
		api('DELETE', "/iot-devices/${dados.deviceId}", null, tokenTutor)
		api('DELETE', "/pets/${dados.petId}", null, tokenTutor)
		KeywordUtil.logInfo("Dados de teste removidos: pet ${dados.petId}")
	}

	/** Históricos clínicos do pet, consultados direto na API (validação de ponta a ponta). */
	@Keyword
	static List historicosDoPet(long petId) {
		return api('GET', "/pets/${petId}/health-summary", null, token(GlobalVariable.TUTOR_EMAIL)).clinicalHistories
	}
}
