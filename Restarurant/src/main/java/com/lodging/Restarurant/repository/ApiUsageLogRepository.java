package com.lodging.Restarurant.repository;

import com.lodging.Restarurant.model.ApiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Map;

public interface ApiUsageLogRepository extends JpaRepository<ApiUsageLog, Long> {
    List<ApiUsageLog> findTop100ByOrderByCalledAtDesc();

    @Query("SELECT a.endpoint, COUNT(a) as cnt FROM ApiUsageLog a GROUP BY a.endpoint")
    List<Object[]> countByEndpoint();

    @Query("SELECT a.partnerName, COUNT(a) as cnt FROM ApiUsageLog a GROUP BY a.partnerName")
    List<Object[]> countByPartner();
}