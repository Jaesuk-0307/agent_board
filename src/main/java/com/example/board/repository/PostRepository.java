package com.example.board.repository;

import com.example.board.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findFirstByOrderByIdDesc();

    // 삭제된 행 수 반환 (이미 삭제되었으면 0)
    @Modifying
    @Transactional
    @Query("delete from Post p where p.id = :id")
    int deleteByPostId(@Param("id") Long id);
}
