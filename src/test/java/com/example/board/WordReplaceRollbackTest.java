package com.example.board;

import com.example.board.entity.Post;
import com.example.board.support.ConcurrencyTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WordReplaceRollbackTest extends ConcurrencyTestSupport {

    private Post save(String title, String content) {
        Post p = new Post();
        p.setTitle(title);
        p.setName("kim");
        p.setContent(content);
        return repository.save(p);
    }

    @Test
    void testReplace_중간_실패시_앞선_글도_롤백() {
        Post ok = save("kfc 맛집", "kfc 치킨");
        // 치환 후 제목이 100자를 넘어 검증 실패하는 글
        Post tooLong = save("kfc" + "가".repeat(95), "본문");
        Post after = save("kfc 후기", "kfc");

        String body = "ids=" + ok.getId() + "," + tooLong.getId() + "," + after.getId()
                + "&before=kfc&after=bhc-chicken";
        assertThat(post("/testReplace", body)).isEqualTo(500);

        // 하나라도 실패하면 첫 번째 글도 바뀌지 않아야 한다
        Post reloaded = repository.findById(ok.getId()).orElseThrow();
        assertThat(reloaded.getTitle()).isEqualTo("kfc 맛집");
        assertThat(reloaded.getContent()).isEqualTo("kfc 치킨");
        assertThat(repository.findById(after.getId()).orElseThrow().getTitle()).isEqualTo("kfc 후기");
    }

    @Test
    void testReplace_정상_치환() {
        Post a = save("kfc 맛집", "kfc 치킨");
        Post b = save("kfc 후기", "kfc");

        String body = "ids=" + a.getId() + "," + b.getId() + "&before=kfc&after=bhc";
        assertThat(post("/testReplace", body)).isEqualTo(200);

        assertThat(repository.findById(a.getId()).orElseThrow().getTitle()).isEqualTo("bhc 맛집");
        assertThat(repository.findById(b.getId()).orElseThrow().getContent()).isEqualTo("bhc");
    }
}
