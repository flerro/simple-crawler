package flerro.scraper.support;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByKey;

public class MetricsRegistry {

    private final ConcurrentMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    private LocalDateTime lastReset = LocalDateTime.now();

    public void increment(String label) {
        delta(label, 1);
    }

    public void delta(String label, long delta) {
        counters.putIfAbsent(label, new AtomicLong(0));
        counters.get(label).addAndGet(delta);
    }

    public long peek(String label) {
        return counters.getOrDefault(label, new AtomicLong(0)).longValue();
    }

    public void reset() {
        counters.replaceAll((k,v) -> new AtomicLong(0));
        lastReset = LocalDateTime.now();
    }

    public Map<String, Long> collect() {
        return counters.entrySet().stream()
                .sorted(comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().longValue(),
                        (e1, e2) -> e2,
                        LinkedHashMap::new
                ));
    }

    public long getUptime() {
        return ChronoUnit.HOURS.between(lastReset, LocalDateTime.now());
    }

}
