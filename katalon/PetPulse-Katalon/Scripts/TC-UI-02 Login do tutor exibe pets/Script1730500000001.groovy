// TC-UI-02 - Login do tutor e exibição dos pets (caso manual CT17)
// Pré-condição (criada pela API): pet "Bidu QA", CACHORRO / Golden Retriever, nascido em 2023.
// Entrada: perfil Tutor, security.tutor@petpulse.com / 123456
// Saída esperada: Home com "Olá, Tutor!", seção "Seus pets" e o pet "Bidu QA" com a raça Golden Retriever.

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import petpulse.Helpers

Map dados = Helpers.prepararPet(false)
try {
	Helpers.abrirApp()

	Helpers.login('Tutor', GlobalVariable.TUTOR_EMAIL, GlobalVariable.SENHA)

	Mobile.waitForElementPresent(Helpers.textoContem('Olá,'), 120)
	Helpers.permitirNotificacoes(10)
	Mobile.verifyElementText(Helpers.textoContem('Olá,'), 'Olá, Tutor!')
	Mobile.verifyElementExist(Helpers.texto('Seus pets'), 10)

	Mobile.scrollToText('Bidu QA', FailureHandling.OPTIONAL)
	Mobile.verifyElementExist(Helpers.texto('Bidu QA'), 30)
	Mobile.verifyElementExist(Helpers.textoContem('Golden Retriever'), 10)

	// Abre o perfil do pet e confere os dados vindos da API
	Mobile.tap(Helpers.texto('Bidu QA'), 10)
	Mobile.verifyElementExist(Helpers.texto('Bidu QA'), 30)
	Mobile.verifyElementExist(Helpers.textoContem('Golden Retriever'), 10)

	Mobile.closeApplication()
} finally {
	Helpers.limparPet(dados)
}
