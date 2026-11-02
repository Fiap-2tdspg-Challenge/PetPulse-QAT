// TC-UI-01 - Login com senha inválida (caso manual CT17, cenário negativo)
// Entrada: perfil Tutor, security.tutor@petpulse.com / 000000
// Saída esperada: diálogo "Erro" com "E-mail ou senha incorretos." e o app continua na tela de login.

import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import internal.GlobalVariable
import petpulse.Helpers

Helpers.abrirApp()

Helpers.login('Tutor', GlobalVariable.TUTOR_EMAIL, '000000')

Mobile.waitForElementPresent(Helpers.dialogoMensagem(), 120)
Mobile.verifyElementText(Helpers.dialogoTitulo(), 'Erro')
Mobile.verifyElementText(Helpers.dialogoMensagem(), 'E-mail ou senha incorretos.')
Mobile.tap(Helpers.dialogoOk(), 10)

Mobile.verifyElementExist(Helpers.texto('Entre na sua conta'), 10)
Mobile.verifyElementNotExist(Helpers.textoContem('Olá,'), 3)

Mobile.closeApplication()
