package ru.exyon.telegrambot.repositories;

import org.springframework.data.jdbc.repository.query.Query;
import ru.exyon.telegrambot.models.DataInfo;

import java.util.Optional;
import java.util.UUID;

interface DataInfoRepository {

    /**
     * Объединение строк с данными за /1 и /2 шаги по указанному dialog_id
     *
     * @param dialogId идентификатор активного диалога
     * @return объект класса DataInfo c собранной информацией
     * <p>
     * <br>
     * EXPLAIN (ANALYZE) при 1000+ записях:
     * <p>
     * GroupAggregate  (cost=0.00..27.82 rows=2 width=80) (actual time=0.067..0.067 rows=1 loops=1)<br>
     * Group Key: dialog_id<br>
     * ->  Seq Scan on data  (cost=0.00..27.76 rows=3 width=36) (actual time=0.006..0.063 rows=3 loops=1)<br>
     * Filter: (dialog_id = 'd27b3a40-5d47-4930-8475-a4ff81fd1633'::uuid)<br>
     * Rows Removed by Filter: 1098<br>
     * Planning Time: 0.050 ms<br>
     * Execution Time: 0.081 ms<br>
     */
    @Query(value = """
            SELECT  max(
                        case step_id
                            when '/1' then data
                            end
                    ) as reason,
                    max(
                        case step_id
                            when '/2' then data
                            end
                    ) as date
            FROM data
            WHERE dialog_id = :dialogId
            GROUP BY dialog_id;
            """
    )
    Optional<DataInfo> findByDataContains(UUID dialogId);

    /**
     * Более медленный вариант<br>
     * Объединение строк с данными за /1 и /2 шаги по указанному dialog_id
     *
     * @param dialogId идентификатор активного диалога
     * @return объект класса DataInfo c собранной информацией
     * <p>
     * <br>
     * EXPLAIN (ANALYZE) при 1000+ записях:
     * <p>
     * Nested Loop  (cost=0.00..61.04 rows=1 width=34) (actual time=0.013..0.121 rows=1 loops=1)<br>
     * ->  Seq Scan on data  (cost=0.00..30.52 rows=1 width=33) (actual time=0.009..0.061 rows=1 loops=1)<br>
     * Filter: ((dialog_id = 'd27b3a40-5d47-4930-8475-a4ff81fd1633'::uuid) AND ((step_id)::text = '/1'::text))<br>
     * Rows Removed by Filter: 1100<br>
     * ->  Seq Scan on data d2  (cost=0.00..30.52 rows=1 width=33) (actual time=0.003..0.058 rows=1 loops=1)<br>
     * Filter: ((dialog_id = 'd27b3a40-5d47-4930-8475-a4ff81fd1633'::uuid) AND ((step_id)::text = '/2'::text))<br>
     * Rows Removed by Filter: 1100<br>
     * Planning Time: 0.057 ms<br>
     * Execution Time: 0.131 ms
     */
    @Deprecated
    @Query(value = """
            SELECT data.data as reason, d2.data as date
            FROM data
                INNER JOIN data as d2
                ON data.dialog_id = d2.dialog_id
                    AND d2.step_id = '/2'
            WHERE data.step_id = '/1'
                AND data.dialog_id = :dialogId
            """
    )
    Optional<DataInfo> findByDataContainsOldVariant(UUID dialogId);
}