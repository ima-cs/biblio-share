
package com.biblioshare.catalogue.repository;

import com.biblioshare.catalogue.entity.Copy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CopyRepository extends JpaRepository<Copy, Long> {
}
