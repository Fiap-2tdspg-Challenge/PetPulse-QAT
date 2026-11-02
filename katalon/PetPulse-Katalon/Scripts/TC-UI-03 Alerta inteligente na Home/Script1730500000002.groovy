// TC-UI-03 - Alerta inteligente exibido na Home (caso manual CT18)
// Pré-condição (criada pela API): pet "Bidu QA" com coleira e uma leitura de frequência cardíaca 195,
// que faz a API gerar o alerta FREQUENCIA_CARDIACA de risco ALTO.
// Entrada: login do tutor security.tutor@petpulse.com / 123456 e toque no sino da Home.
// Saída esperada: modal "Notificações" com "Frequência cardíaca fora da faixa esperada." e a recomendação.

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import petpulse.Helpers

Map dados = Helpers.prepararPet(true)
try {
	Helpers.abrirApp()

	Helpers.login('Tutor', GlobalVariable.TUTOR_EMAIL, GlobalVariable.SENHA)
	Mobile.waitForElementPresent(Helpers.textoContem('Olá,'), 120)
	Helpers.permitirNotificacoes(10)

	// O sino é o primeiro botão depois da saudação "Olá, ..." no cabeçalho
	String sino = "//android.widget.TextView[starts-with(@text,'Olá,')]/following::android.view.ViewGroup[@clickable='true'][1]"
	// Espera os alertas carregarem: o contador numérico aparece sobre o sino
	Mobile.waitForElementPresent(Helpers.xpath(sino + "//android.widget.TextView[string-length(@text) > 0]"), 60)
	Helpers.permitirNotificacoes(3)
	Mobile.tap(Helpers.xpath(sino), 10)
	if (!Mobile.waitForElementPresent(Helpers.texto('Notificações'), 5, FailureHandling.OPTIONAL)) {
		// O pedido de permissão pode ter aparecido por cima no momento do toque
		Helpers.permitirNotificacoes(3)
		Mobile.tap(Helpers.xpath(sino), 10)
	}

	Mobile.verifyElementExist(Helpers.texto('Notificações'), 10)
	Mobile.verifyElementExist(Helpers.texto('Frequência cardíaca fora da faixa esperada.'), 30)
	Mobile.verifyElementExist(Helpers.texto('Recomenda-se avaliação do estado do pet e acompanhamento profissional.'), 10)

	Mobile.closeApplication()
} finally {
	Helpers.limparPet(dados)
}
