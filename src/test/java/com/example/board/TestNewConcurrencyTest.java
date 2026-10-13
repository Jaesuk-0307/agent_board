package com.example.board;

import com.example.board.support.ConcurrencyTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestNewConcurrencyTest extends ConcurrencyTestSupport {

    @Test
    void testNew_100개_병렬_요청() throws Exception {
        // 워밍업으로 글 생성 하나 해봄
        post("/testNew", "title=warmup&name=kim&content=test");
        long before = repository.count();

        // 100개를 1초 안에 동시에 생성함
        List<Integer> statuses = fireInParallel(
                n -> post("/testNew", "title=hello" + n + "&name=kim&content=test"));

        assertThat(count(statuses, 200)).isEqualTo(REQUESTS);
        assertThat(repository.count() - before).isEqualTo(REQUESTS);
    }
}
