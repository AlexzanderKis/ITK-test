package org.example.itktest.integration;

import org.example.itktest.WalletOperationType;
import org.example.itktest.dto.WalletOperationRequest;
import org.example.itktest.dto.WalletResponseDTO;
import org.example.itktest.entity.WalletEntity;
import org.example.itktest.exception.ErrorResponseDTO;
import org.example.itktest.repository.WalletRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class WalletControllerIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    WalletRepo walletRepo;

    private UUID walletId;

    @BeforeEach
    void setUp() {
        walletRepo.deleteAll();

        WalletEntity wallet = new WalletEntity();
        wallet.setWalletId(UUID.randomUUID());
        wallet.setBalance(new BigDecimal("1000.00"));
        wallet = walletRepo.save(wallet);
        walletId = wallet.getWalletId();
    }

// пополнение POST /api/v1/wallet -> DEPOSIT
    @Test
    void deposit_shouldIncreaseBalance_andReturn200() {
        WalletOperationRequest request = new WalletOperationRequest(
                walletId, WalletOperationType.DEPOSIT, new BigDecimal("250.50"));

        ResponseEntity<WalletResponseDTO> response = post(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().walletId()).isEqualTo(walletId);
        assertThat(response.getBody().balance()).isEqualByComparingTo("1250.50");

        WalletEntity fromDb = walletRepo.findById(walletId).orElseThrow();
        assertThat(fromDb.getBalance()).isEqualByComparingTo("1250.50");
    }

// снятие POST /api/v1/wallet -> WITHDRAW
    @Test
    void withdraw_shouldDecreaseBalance_andReturn200() {
        WalletOperationRequest request = new WalletOperationRequest(
                walletId, WalletOperationType.WITHDRAW, new BigDecimal("300.00"));

        ResponseEntity<WalletResponseDTO> response = post(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().balance()).isEqualByComparingTo("700.00");
    }

    @Test
    void withdraw_shouldReturn409_whenInsufficientFunds() {
        WalletOperationRequest request = new WalletOperationRequest(
                walletId, WalletOperationType.WITHDRAW, new BigDecimal("5000.00"));

        ResponseEntity<ErrorResponseDTO> response = postForError(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getMessage()).contains("Недостаточно");

        // Баланс не изменился
        WalletEntity fromDb = walletRepo.findById(walletId).orElseThrow();
        assertThat(fromDb.getBalance()).isEqualByComparingTo("1000.00");
    }

// POST /api/v1/wallet ошибки
    @Test
    void operation_shouldReturn404_whenWalletNotFound() {
        WalletOperationRequest request = new WalletOperationRequest(
                UUID.randomUUID(), WalletOperationType.DEPOSIT, new BigDecimal("100.00"));

        ResponseEntity<ErrorResponseDTO> response = postForError(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    void operation_shouldReturn400_whenAmountIsZero() {
        WalletOperationRequest request = new WalletOperationRequest(
                walletId, WalletOperationType.DEPOSIT, BigDecimal.ZERO);

        ResponseEntity<ErrorResponseDTO> response = postForError(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("amount");
    }

    @Test
    void operation_shouldReturn400_whenAmountIsNegative() {
        WalletOperationRequest request = new WalletOperationRequest(
                walletId, WalletOperationType.DEPOSIT, new BigDecimal("-50.00"));

        ResponseEntity<ErrorResponseDTO> response = postForError(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("amount");
    }

    @Test
    void operation_shouldReturn400_whenWalletIdIsNull() {
        WalletOperationRequest request = new WalletOperationRequest(
                null, WalletOperationType.DEPOSIT, new BigDecimal("100.00"));

        ResponseEntity<ErrorResponseDTO> response = postForError(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("walletId");
    }

    @Test
    void operation_shouldReturn400_whenOperationTypeIsNull() {
        WalletOperationRequest request = new WalletOperationRequest(
                walletId, null, new BigDecimal("100.00"));

        ResponseEntity<ErrorResponseDTO> response = postForError(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).contains("operationType");
    }

    @Test
    void operation_shouldReturn400_whenJsonIsMalformed() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>("{ not valid json", headers);

        ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
                "/api/v1/wallet", HttpMethod.POST, entity, ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

// GET /api/v1/wallets/{walletId}
    @Test
    void getBalance_shouldReturnCurrentBalance() {
        ResponseEntity<WalletResponseDTO> response = restTemplate.getForEntity(
                "/api/v1/wallets/" + walletId, WalletResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().walletId()).isEqualTo(walletId);
        assertThat(response.getBody().balance()).isEqualByComparingTo("1000.00");
    }

// GET /api/v1/wallets/{walletId} ошибки
    @Test
    void getBalance_shouldReturn404_whenWalletNotFound() {
        ResponseEntity<ErrorResponseDTO> response = restTemplate.getForEntity(
                "/api/v1/wallets/" + UUID.randomUUID(), ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getBalance_shouldReturn400_whenUuidIsInvalid() {
        ResponseEntity<ErrorResponseDTO> response = restTemplate.getForEntity(
                "/api/v1/wallets/not-a-uuid", ErrorResponseDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

// Конкурентность, проверка пессимистической блокировки
    @Test
    void concurrentWithdraw_shouldNotOverdraw() throws Exception {

        // баланс 1000
        // 2 параллельных снятия по 600
        // должно пройти только одно
        WalletOperationRequest request = new WalletOperationRequest(
                walletId, WalletOperationType.WITHDRAW, new BigDecimal("600.00"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<ResponseEntity<WalletResponseDTO>> task = () -> {
            start.await();
            return restTemplate.postForEntity("/api/v1/wallet", request, WalletResponseDTO.class);
        };

        Future<ResponseEntity<WalletResponseDTO>> f1 = executor.submit(task);
        Future<ResponseEntity<WalletResponseDTO>> f2 = executor.submit(task);

        start.countDown();

        ResponseEntity<WalletResponseDTO> r1 = f1.get(10, TimeUnit.SECONDS);
        ResponseEntity<WalletResponseDTO> r2 = f2.get(10, TimeUnit.SECONDS);

        executor.shutdown();

        long ok = Stream.of(r1, r2)
                .filter(r -> r.getStatusCode() == HttpStatus.OK).count();
        long conflict = Stream.of(r1, r2)
                .filter(r -> r.getStatusCode() == HttpStatus.CONFLICT).count();

        assertThat(ok).as("ровно один запрос должен пройти").isEqualTo(1);
        assertThat(conflict).as("второй должен получить 409").isEqualTo(1);

        WalletEntity finalWallet = walletRepo.findById(walletId).orElseThrow();
        assertThat(finalWallet.getBalance()).isEqualByComparingTo("400.00");
    }

// Вспомогательные методы
    private ResponseEntity<WalletResponseDTO> post(WalletOperationRequest request) {
        return restTemplate.postForEntity("/api/v1/wallet", request, WalletResponseDTO.class);
    }

    private ResponseEntity<ErrorResponseDTO> postForError(WalletOperationRequest request) {
        return restTemplate.postForEntity("/api/v1/wallet", request, ErrorResponseDTO.class);
    }
}
