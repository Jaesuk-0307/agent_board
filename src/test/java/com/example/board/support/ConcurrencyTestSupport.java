package com.example.board.support;

import com.example.board.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.servlet.client.RestTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.IntFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 동시성 테스트 공통 베이스.
 * 항상 REQUESTS(100)개 요청을 동시에 발사하고, LIMIT_MS(1초) 이내 완료를 검증한다.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
public abstract class ConcurrencyTestSupport {

    protected static final int REQUESTS = 100;
    protected static final long LIMIT_MS = 1000;

    @LocalServerPort
    protected int port;

    @Autowired
    protected PostRepository repository;

    protected RestTestClient client;

    @BeforeEach
    protected void setUpClient() {
        repository.deleteAll();
        client = RestTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    /** form 파라미터를 담아 POST 요청을 보내고 상태 코드를 반환한다. */
    protected int post(String uri, String formBody) {
        RestTestClient.RequestBodySpec spec = client.post().uri(uri);
        if (formBody != null) {
            spec.header("Content-Type", "application/x-www-form-urlencoded").body(formBody);
        }
        return spec.exchange().returnResult().getStatus().value();
    }

    /**
     * REQUESTS개 요청을 동시에 발사하고 각 요청의 상태 코드를 반환한다.
     * 1초(LIMIT_MS) 초과 시 실패 처리한다.
     *
     * @param request 요청 번호(0..99)를 받아 상태 코드를 돌려주는 함수
     */
    protected List<Integer> fireInParallel(IntFunction<Integer> request) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(REQUESTS);
        CountDownLatch ready = new CountDownLatch(REQUESTS);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Integer>> futures = new ArrayList<>();
        try {
            for (int i = 0; i < REQUESTS; i++) {
                final int n = i;
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    go.await();
                    return request.apply(n);
                }));
            }
            ready.await();
            long start = System.nanoTime();
            go.countDown();

            List<Integer> statuses = new ArrayList<>();
            for (Future<Integer> f : futures) {
                statuses.add(f.get());
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.println(REQUESTS + " requests finished in " + elapsedMs + " ms");
            assertThat(elapsedMs).as("1초 이내 완료").isLessThan(LIMIT_MS);
            return statuses;
        } finally {
            pool.shutdownNow();
        }
    }

    protected static long count(List<Integer> statuses, int status) {
        return statuses.stream().filter(s -> s == status).count();
    }
}
