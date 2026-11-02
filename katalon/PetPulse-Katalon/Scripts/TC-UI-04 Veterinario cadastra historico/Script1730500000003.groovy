// TC-UI-04 - Veterinário cadastra histórico clínico pelo painel (caso manual CT19)
// Pré-condição (criada pela API): pet "Bidu QA" sem histórico.
// Entrada: login Veterinário carlos.andrade@vetcare.com / 123456, busca "Bidu QA",
//          registro Consulta "Consulta de rotina QA" com retorno 10/01/2027.
// Saída esperada: diálogo "Registro cadastrado com sucesso!", registro listado no histórico do pet
//                 e gravado na API (GET /pets/{id}/health-summary).

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import internal.GlobalVariable
import petpulse.Helpers

Map dados = Helpers.prepararPet(false)
try {
	Helpers.abrirApp()

	Helpers.login('Veterinário', GlobalVariable.VET_EMAIL, GlobalVariable.SENHA)
	Mobile.waitForElementPresent(Helpers.campo('Buscar por pet ou tutor'), 120)
	Helpers.permitirNotificacoes(10)

	// Busca e abre o pet
	Mobile.setText(Helpers.campo('Buscar por pet ou tutor'), 'Bidu QA', 10)
	Mobile.hideKeyboard(FailureHandling.OPTIONAL)
	// TextView (o campo de busca também contém "Bidu QA")
	Mobile.tap(Helpers.xpath("//android.widget.TextView[@text='Bidu QA']"), 30)

	Mobile.verifyElementExist(Helpers.texto('HISTÓRICO CLÍNICO'), 30)
	Mobile.verifyElementExist(Helpers.texto('Nenhum registro encontrado.'), 30)

	// Botão "+" no cabeçalho, logo depois do nome do pet
	Mobile.tap(Helpers.xpath("(//android.widget.TextView[@text='Bidu QA'])[1]/following::android.view.ViewGroup[@clickable='true'][1]"), 10)

	// Formulário de novo registro
	Mobile.verifyElementExist(Helpers.texto('Novo Registro'), 10)
	Mobile.tap(Helpers.texto('Consulta'), 10)
	Mobile.setText(Helpers.campo('Descrição (ex: Vacina V10, Check-up geral)'), 'Consulta de rotina QA', 10)
	Mobile.setText(Helpers.campo('Data de retorno (opcional)'), '10012027', 10)
	Mobile.verifyElementText(Helpers.campo('Data de retorno (opcional)'), '10/01/2027')
	Mobile.hideKeyboard(FailureHandling.OPTIONAL)
	Mobile.scrollToText('Cadastrar Registro', FailureHandling.OPTIONAL)
	Mobile.tap(Helpers.texto('Cadastrar Registro'), 10)

	Mobile.waitForElementPresent(Helpers.dialogoMensagem(), 60)
	Mobile.verifyElementText(Helpers.dialogoMensagem(), 'Registro cadastrado com sucesso!')
	Mobile.tap(Helpers.dialogoOk(), 10)

	// Registro aparece no histórico do pet no app
	Mobile.verifyElementExist(Helpers.texto('Consulta de rotina QA'), 30)
	Mobile.verifyElementExist(Helpers.texto('Retorno: 10/01/2027'), 10)

	// E foi gravado na API
	List historicos = Helpers.historicosDoPet(dados.petId)
	assert historicos.size() == 1
	assert historicos[0].recordType == 'CONSULTA'
	assert historicos[0].description == 'Consulta de rotina QA'
	assert historicos[0].returnDate == '2027-01-10'

	Mobile.closeApplication()
} finally {
	Helpers.limparPet(dados)
}
