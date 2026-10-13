package com.example.board;

import com.example.board.entity.Post;
import com.example.board.support.ConcurrencyTestSupport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestDeleteConcurrencyTest extends ConcurrencyTestSupport {

    @Test
    void testDelete_100개_병렬_요청() throws Exception {
        // Test 준비: 101개 글 생성
        for (int i = 0; i < REQUESTS + 1; i++) {
            Post p = new Post();
            p.setTitle("title" + i);
            p.setName("kim");
            p.setContent("test");
            repository.save(p);
        }
        // 워밍업으로 글 삭제 하나 해봄
        assertThat(post("/testDelete", null)).isEqualTo(200);
        long before = repository.count();

        // 100개를 1초 안에 동시에 삭제함
        List<Integer> statuses = fireInParallel(n -> post("/testDelete", null));

        // 200 이외의 응답이 하나라도 있으면 실패
        assertThat(count(statuses, 200)).as("200 응답 수").isEqualTo(REQUESTS);
        assertThat(repository.count()).isEqualTo(before - REQUESTS);
    }
}
