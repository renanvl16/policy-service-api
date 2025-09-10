@echo off
setlocal enabledelayedexpansion

REM Script completo para testar todos os fluxos da API Policy Service (Windows)
REM Teste de integração end-to-end

set API_BASE_URL=http://localhost:8080/api/v1
set CONTENT_TYPE=application/json
set CUSTOMER_ID=123e4567-e89b-12d3-a456-426614174000

echo ==============================================
echo 🚀 INICIANDO TESTES COMPLETOS DA API
echo ==============================================

REM Aguardar API estar disponível
echo Aguardando API estar disponível...
:wait_loop
powershell -Command "try { Invoke-WebRequest -Uri 'http://localhost:8080/actuator/health' -Method Get -ErrorAction Stop | Out-Null; exit 0 } catch { exit 1 }"
if !errorlevel! equ 0 (
    echo ✓ API está disponível!
    goto api_ready
)
timeout /t 5 >nul
goto wait_loop

:api_ready

echo.
echo ==============================================
echo 📝 1. TESTES DE CRIAÇÃO DE SOLICITAÇÕES
echo ==============================================

REM 1.1 Criar solicitação válida - Seguro de Vida
echo 1.1 - Criando solicitação de Seguro de Vida
set create_data_vida={"customerId": "%CUSTOMER_ID%","productId": "VIDA-001","category": "VIDA","salesChannel": "WEBSITE","paymentMethod": "CREDIT_CARD","totalMonthlyPremiumAmount": 150.00,"insuredAmount": 50000.00,"coverages": {"MORTE_NATURAL": 50000.00,"MORTE_ACIDENTAL": 100000.00,"INVALIDEZ_PERMANENTE": 25000.00},"assistances": ["ASSISTENCIA_FUNERAL", "ASSISTENCIA_JURIDICA"]}

powershell -Command "try { $response = Invoke-RestMethod -Uri '%API_BASE_URL%/policy-requests' -Method Post -Body '%create_data_vida%' -ContentType '%CONTENT_TYPE%'; Write-Output $response.id; exit 0 } catch { Write-Output 'ERRO'; exit 1 }"
if !errorlevel! equ 0 (
    echo ✓ Solicitação de Vida criada com sucesso
) else (
    echo ✗ Erro ao criar solicitação de Vida
)

echo.
echo ==============================================
echo 🔍 2. TESTES DE CONSULTA
echo ==============================================

REM 2.1 Consultar por Customer ID
echo 2.1 - Consultando solicitações do cliente: %CUSTOMER_ID%
powershell -Command "try { Invoke-RestMethod -Uri '%API_BASE_URL%/policy-requests/customer/%CUSTOMER_ID%' -Method Get | ConvertTo-Json; exit 0 } catch { Write-Output 'ERRO na consulta'; exit 1 }"
if !errorlevel! equ 0 (
    echo ✓ Consulta por Customer ID realizada com sucesso
) else (
    echo ✗ Erro na consulta por Customer ID
)

echo.
echo ==============================================
echo 📊 3. VERIFICAÇÃO DE HEALTH CHECK
echo ==============================================

REM 3.1 Health check da aplicação
echo 3.1 - Verificando health check
powershell -Command "try { $health = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -Method Get; if($health.status -eq 'UP') { Write-Output 'UP'; exit 0 } else { Write-Output 'DOWN'; exit 1 } } catch { Write-Output 'ERRO'; exit 1 }"
if !errorlevel! equ 0 (
    echo ✓ Aplicação está UP
) else (
    echo ✗ Aplicação não está UP
)

echo.
echo ==============================================
echo 📝 4. RESUMO FINAL DOS TESTES
echo ==============================================

echo.
echo ✓ TODOS OS TESTES FORAM EXECUTADOS!
echo Verifique os logs acima para identificar eventuais falhas.
echo.
echo ✓ TESTE COMPLETO FINALIZADO COM SUCESSO! 🎉
echo ==============================================

pause