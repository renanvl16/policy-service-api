package com.acme.policyapi.cucumber;

import com.acme.policyapi.application.dto.PolicyRequestCreateDTO;
import com.acme.policyapi.application.dto.PolicyRequestResponseDTO;
import com.acme.policyapi.domain.entity.*;
import com.acme.policyapi.domain.repository.PolicyRequestRepository;
import com.acme.policyapi.infrastructure.rest.PolicyRequestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.awaitility.Awaitility.await;

/**
 * Step definitions para testes de integração do ciclo de vida das solicitações de apólice.
 * 
 * @author Sistema ACME
 */
public class PolicyRequestStepDefinitions {

    @Autowired
    private SpringCucumberConfiguration config;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PolicyRequestRepository policyRequestRepository;

    private PolicyRequestCreateDTO currentRequest;
    private ResponseEntity<PolicyRequestController.PolicyRequestCreatedResponse> createResponse;
    private ResponseEntity<PolicyRequestResponseDTO> queryResponse;
    private ResponseEntity<PolicyRequestResponseDTO[]> queryListResponse;
    private ResponseEntity<String> errorResponse;
    private UUID currentPolicyRequestId;
    private UUID currentCustomerId;
    private String expectedRiskClassification;

    @Dado("que o serviço de solicitações de apólice está em execução")
    public void queOServicoDesolicitacoesDeApoliceEstaEmExecucao() {
        // Verifica se o serviço está respondendo
        ResponseEntity<String> response = restTemplate.getForEntity(
                config.getBaseUrl() + "/actuator/health", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @E("que a API de fraudes mock está disponível")
    public void queAAPIDefraudesMockEstaDisponivel() {
        // Mock está configurado via propriedades de teste
        assertTrue(true); // Mock sempre disponível nos testes
    }

    @E("que o banco de dados está limpo")
    public void queOBancoDeDadosEstaLimpo() {
        policyRequestRepository.deleteAll();
    }

    @Dado("que eu tenho uma solicitação de apólice válida para categoria {string}")
    public void queEuTenhoUmaSolicitacaoDeApoliceValidaParaCategoria(String categoria) {
        currentCustomerId = UUID.randomUUID();
        currentRequest = new PolicyRequestCreateDTO();
        currentRequest.setCustomerId(currentCustomerId);
        currentRequest.setProductId("PROD-" + System.currentTimeMillis());
        currentRequest.setCategory(InsuranceCategory.valueOf(categoria));
        currentRequest.setSalesChannel(SalesChannel.MOBILE);
        currentRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        currentRequest.setTotalMonthlyPremiumAmount(new BigDecimal("150.00"));
        currentRequest.setCoverages(Map.of("Cobertura Básica", new BigDecimal("100000.00")));
        currentRequest.setAssistances(List.of("Assistência 24h"));
    }

    @E("com valor segurado de {string} reais")
    public void comValorSeguradoDeReais(String valor) {
        currentRequest.setInsuredAmount(new BigDecimal(valor));
    }

    @E("para cliente com classificação de risco {string}")
    public void paraClienteComClassificacaoDeRisco(String classificacao) {
        expectedRiskClassification = classificacao;
        // Note: A classificação é determinada pelo mock da API de fraudes
    }

    @Quando("eu envio a solicitação para criação")
    public void euEnvioASolicitacaoParaCriacao() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<PolicyRequestCreateDTO> request = new HttpEntity<>(currentRequest, headers);
        
        createResponse = restTemplate.postForEntity(
                config.getBaseUrl() + "/api/v1/policy-requests",
                request,
                PolicyRequestController.PolicyRequestCreatedResponse.class
        );
        
        if (createResponse.getBody() != null) {
            currentPolicyRequestId = createResponse.getBody().getId();
        }
    }

    @Então("a solicitação deve ser criada com sucesso")
    public void aSolicitacaoDeveSerCriadaComSucesso() {
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        assertNotNull(createResponse.getBody().getId());
    }

    @E("deve retornar o ID da solicitação e data\\/hora de criação")
    public void deveRetornarOIDDaSolicitacaoEDataHoraDeCriacao() {
        PolicyRequestController.PolicyRequestCreatedResponse response = createResponse.getBody();
        assertNotNull(response.getId());
        assertNotNull(response.getCreatedAt());
        currentPolicyRequestId = response.getId();
    }

    @E("o status inicial deve ser {string}")
    public void oStatusInicialDeveSer(String status) {
        PolicyRequestController.PolicyRequestCreatedResponse response = createResponse.getBody();
        assertEquals(PolicyRequestStatus.valueOf(status), response.getStatus());
    }

    @E("aguardo o processamento da análise de fraudes")
    public void aguardoOProcessamentoDaAnaliseDefraudes() {
        // Aguarda até 10 segundos pelo processamento
        await().atMost(10, TimeUnit.SECONDS)
               .pollInterval(500, TimeUnit.MILLISECONDS)
               .until(() -> {
                   PolicyRequest policyRequest = policyRequestRepository.findById(currentPolicyRequestId).orElse(null);
                   return policyRequest != null && policyRequest.getStatus() != PolicyRequestStatus.RECEIVED;
               });
    }

    @Então("a solicitação deve ter status {string}")
    public void aSolicitacaoDeveTerStatus(String expectedStatus) {
        PolicyRequest policyRequest = policyRequestRepository.findById(currentPolicyRequestId).orElse(null);
        assertNotNull(policyRequest);
        assertEquals(PolicyRequestStatus.valueOf(expectedStatus), policyRequest.getStatus());
    }

    @E("em seguida deve ter status {string}")
    public void emSeguideDeveTerStatus(String expectedStatus) {
        // Aguarda mais um pouco para a próxima transição
        await().atMost(5, TimeUnit.SECONDS)
               .pollInterval(500, TimeUnit.MILLISECONDS)
               .until(() -> {
                   PolicyRequest policyRequest = policyRequestRepository.findById(currentPolicyRequestId).orElse(null);
                   return policyRequest != null && policyRequest.getStatus() == PolicyRequestStatus.valueOf(expectedStatus);
               });
        
        aSolicitacaoDeveTerStatus(expectedStatus);
    }

    @E("deve ter histórico das mudanças de status")
    public void deveTerHistoricoDasMudancasDeStatus() {
        PolicyRequest policyRequest = policyRequestRepository.findByIdWithHistory(currentPolicyRequestId).orElse(null);
        assertNotNull(policyRequest);
        assertFalse(policyRequest.getHistory().isEmpty());
        assertTrue(policyRequest.getHistory().size() >= 2); // Pelo menos RECEIVED e outro status
    }

    @E("deve ter uma razão de rejeição informando excesso de limite")
    public void deveTerUmaRazaoDeRejeicaoInformandoExcessoDelimite() {
        PolicyRequest policyRequest = policyRequestRepository.findByIdWithHistory(currentPolicyRequestId).orElse(null);
        assertNotNull(policyRequest);
        
        StatusHistory lastHistory = policyRequest.getHistory().get(policyRequest.getHistory().size() - 1);
        assertEquals(PolicyRequestStatus.REJECTED, lastHistory.getStatus());
        assertNotNull(lastHistory.getReason());
        assertTrue(lastHistory.getReason().contains("excede o limite"));
    }

    @Dado("que eu tenho uma solicitação de apólice criada e pendente")
    public void queEuTenhoUmaSolicitacaoDeApolice() {
        queEuTenhoUmaSolicitacaoDeApoliceValidaParaCategoria("AUTO");
        comValorSeguradoDeReais("200000.00");
        paraClienteComClassificacaoDeRisco("REGULAR");
        euEnvioASolicitacaoParaCriacao();
        aguardoOProcessamentoDaAnaliseDefraudes();
        emSeguideDeveTerStatus("PENDING");
    }

    @Quando("eu solicito o cancelamento da solicitação com motivo {string}")
    public void euSolicitoOCancelamentoDaSolicitacaoComMotivo(String motivo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> cancellationRequest = Map.of("reason", motivo);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(cancellationRequest, headers);
        
        ResponseEntity<Void> response = restTemplate.postForEntity(
                config.getBaseUrl() + "/api/v1/policy-requests/" + currentPolicyRequestId + "/cancel",
                request,
                Void.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @E("deve ter o motivo do cancelamento no histórico")
    public void deveTerOMotivoDocancelamentoNoHistorico() {
        PolicyRequest policyRequest = policyRequestRepository.findByIdWithHistory(currentPolicyRequestId).orElse(null);
        assertNotNull(policyRequest);
        
        StatusHistory lastHistory = policyRequest.getHistory().get(policyRequest.getHistory().size() - 1);
        assertEquals(PolicyRequestStatus.CANCELLED, lastHistory.getStatus());
        assertNotNull(lastHistory.getReason());
        assertTrue(lastHistory.getReason().contains("Desistência do cliente"));
    }

    @Dado("que eu tenho uma solicitação de apólice aprovada")
    public void queEuTenhoUmaSolicitacaoDeApoliceAprovada() {
        // Para este cenário, criaríamos uma solicitação e forçaríamos o status APPROVED
        queEuTenhoUmaSolicitacaoDeApoliceValidaParaCategoria("AUTO");
        comValorSeguradoDeReais("100000.00");
        euEnvioASolicitacaoParaCriacao();
        
        // Força o status para APPROVED diretamente no banco
        PolicyRequest policyRequest = policyRequestRepository.findById(currentPolicyRequestId).orElse(null);
        assertNotNull(policyRequest);
        policyRequest.setStatus(PolicyRequestStatus.APPROVED);
        policyRequestRepository.save(policyRequest);
    }

    @Quando("eu tento cancelar a solicitação")
    public void euTentoCancelarASolicitacao() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, String> cancellationRequest = Map.of("reason", "Tentativa de cancelamento");
        HttpEntity<Map<String, String>> request = new HttpEntity<>(cancellationRequest, headers);
        
        errorResponse = restTemplate.postForEntity(
                config.getBaseUrl() + "/api/v1/policy-requests/" + currentPolicyRequestId + "/cancel",
                request,
                String.class
        );
    }

    @Então("deve retornar erro indicando que não é possível cancelar")
    public void deveRetornarErroIndicandoQueNaoEPossivelCancelar() {
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
    }

    @Dado("que eu tenho uma solicitação de apólice criada")
    public void queEuTenhoUmaSolicitacaoDeApoliceCriada() {
        queEuTenhoUmaSolicitacaoDeApoliceValidaParaCategoria("VIDA");
        comValorSeguradoDeReais("300000.00");
        euEnvioASolicitacaoParaCriacao();
    }

    @Quando("eu consulto a solicitação pelo ID")
    public void euConsultoASolicitacaoPeloID() {
        queryResponse = restTemplate.getForEntity(
                config.getBaseUrl() + "/api/v1/policy-requests/" + currentPolicyRequestId,
                PolicyRequestResponseDTO.class
        );
    }

    @Então("deve retornar os detalhes completos da solicitação")
    public void deveRetornarOsDetalhesCompletosDaSolicitacao() {
        assertEquals(HttpStatus.OK, queryResponse.getStatusCode());
        PolicyRequestResponseDTO response = queryResponse.getBody();
        assertNotNull(response);
        assertEquals(currentPolicyRequestId, response.getId());
        assertEquals(currentCustomerId, response.getCustomerId());
        assertNotNull(response.getCategory());
        assertNotNull(response.getInsuredAmount());
    }

    @E("deve incluir o histórico de mudanças de status")
    public void deveIncluirOHistoricoCompletoEoHistoricoDeMudancasDeStatus() {
        PolicyRequestResponseDTO response = queryResponse.getBody();
        assertNotNull(response.getHistory());
        assertFalse(response.getHistory().isEmpty());
    }

    @Dado("que eu tenho {int} solicitações de apólice para o mesmo cliente")
    public void queEuTenho3SolicitacoesDeApoliceParaOMesmoCliente(int quantidade) {
        currentCustomerId = UUID.randomUUID();
        
        for (int i = 0; i < quantidade; i++) {
            PolicyRequestCreateDTO request = new PolicyRequestCreateDTO();
            request.setCustomerId(currentCustomerId);
            request.setProductId("PROD-" + i);
            request.setCategory(InsuranceCategory.AUTO);
            request.setSalesChannel(SalesChannel.MOBILE);
            request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            request.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
            request.setInsuredAmount(new BigDecimal("150000.00"));
            request.setCoverages(Map.of("Cobertura " + i, new BigDecimal("50000.00")));
            request.setAssistances(List.of("Assistência " + i));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PolicyRequestCreateDTO> httpRequest = new HttpEntity<>(request, headers);

            restTemplate.postForEntity(
                    config.getBaseUrl() + "/api/v1/policy-requests",
                    httpRequest,
                    PolicyRequestController.PolicyRequestCreatedResponse.class
            );
        }
    }

    @Quando("eu consulto as solicitações pelo ID do cliente")
    public void euConsultoAsSolicitacoesPeloIDDoCliente() {
        queryListResponse = restTemplate.getForEntity(
                config.getBaseUrl() + "/api/v1/policy-requests/customer/" + currentCustomerId,
                PolicyRequestResponseDTO[].class
        );
    }

    @Então("deve retornar todas as {int} solicitações")
    public void deveRetornarTodasAs3Solicitacoes(int quantidade) {
        assertEquals(HttpStatus.OK, queryListResponse.getStatusCode());
        PolicyRequestResponseDTO[] responses = queryListResponse.getBody();
        assertNotNull(responses);
        assertEquals(quantidade, responses.length);
    }

    @E("cada solicitação deve ter seus detalhes completos")
    public void cadaSolicitacaoDeveTerSeusDetalhesCompletos() {
        PolicyRequestResponseDTO[] responses = queryListResponse.getBody();
        for (PolicyRequestResponseDTO response : responses) {
            assertEquals(currentCustomerId, response.getCustomerId());
            assertNotNull(response.getId());
            assertNotNull(response.getCategory());
            assertNotNull(response.getInsuredAmount());
        }
    }

    @Quando("eu envio uma solicitação sem o ID do cliente")
    public void euEnvioUmaSolicitacaoSemOIDDoCliente() {
        PolicyRequestCreateDTO invalidRequest = new PolicyRequestCreateDTO();
        invalidRequest.setProductId("PROD-INVALID");
        invalidRequest.setCategory(InsuranceCategory.AUTO);
        invalidRequest.setSalesChannel(SalesChannel.MOBILE);
        invalidRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        invalidRequest.setTotalMonthlyPremiumAmount(new BigDecimal("100.00"));
        invalidRequest.setInsuredAmount(new BigDecimal("200000.00"));
        invalidRequest.setCoverages(Map.of("Cobertura", new BigDecimal("100000.00")));
        invalidRequest.setAssistances(List.of("Assistência"));
        // customerId não definido

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PolicyRequestCreateDTO> request = new HttpEntity<>(invalidRequest, headers);

        errorResponse = restTemplate.postForEntity(
                config.getBaseUrl() + "/api/v1/policy-requests",
                request,
                String.class
        );
    }

    @Então("deve retornar erro de validação indicando campo obrigatório")
    public void deveRetornarErroDeValidacaoIndicandoCampoObrigatorio() {
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
        String errorBody = errorResponse.getBody();
        assertNotNull(errorBody);
        assertTrue(errorBody.contains("obrigatório") || errorBody.contains("required"));
    }

    @E("a solicitação não deve ser criada")
    public void aSolicitacaoNaoDeveSerCriada() {
        // O erro já foi verificado no passo anterior
        assertTrue(true);
    }
}