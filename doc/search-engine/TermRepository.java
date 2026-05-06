package com.search.engine.repository;

import com.search.engine.model.Term;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermRepository extends JpaRepository<Term, Long> {

    /** 根据词语查找 */
    Optional<Term> findByWord(String word);

    /** 检查词语是否已存在 */
    boolean existsByWord(String word);
}
