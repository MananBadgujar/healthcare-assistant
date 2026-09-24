package com.healthcare.ragservice.service;

import com.healthcare.ragservice.entity.KbChunk;
import com.healthcare.ragservice.entity.KbDocument;
import com.healthcare.ragservice.repo.KbChunkRepository;
import com.healthcare.ragservice.repo.KbDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RagService {
    static final int DIM = 64;
    private final KbDocumentRepository docs;
    private final KbChunkRepository chunks;

    public RagService(KbDocumentRepository docs, KbChunkRepository chunks) {
        this.docs = docs; this.chunks = chunks;
    }

    public static double[] embed(String text) {
        double[] v = new double[DIM];
        String[] toks = text.toLowerCase().split("[^a-z0-9]+");
        for (String t : toks) {
            if (t.isBlank()) continue;
            int h = Math.abs(t.hashCode());
            v[h % DIM] += 1.0;
        }
        double n = 0; for (double d : v) n += d * d;
        n = Math.sqrt(n);
        if (n > 0) for (int i = 0; i < DIM; i++) v[i] /= n;
        return v;
    }

    static String pack(double[] v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < v.length; i++) { if (i > 0) sb.append(','); sb.append(v[i]); }
        return sb.toString();
    }

    static double[] unpack(String s) {
        String[] p = s.split(",");
        double[] v = new double[p.length];
        for (int i = 0; i < p.length; i++) v[i] = Double.parseDouble(p[i]);
        return v;
    }

    @Transactional
    public KbDocument ingest(String title, String content) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title required");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content required");
        KbDocument d = new KbDocument();
        d.setTitle(title); d.setContent(content);
        KbDocument saved = docs.save(d);
        for (String para : content.split("\n\n")) {
            if (para.isBlank()) continue;
            KbChunk c = new KbChunk();
            c.setDocumentId(saved.getId());
            c.setText(para.length() > 4000 ? para.substring(0, 4000) : para);
            c.setEmbedding(pack(embed(para)));
            chunks.save(c);
        }
        return saved;
    }

    public List<Map<String, Object>> search(String q, int topK) {
        double[] qv = embed(q == null ? "" : q);
        List<Map<String, Object>> scored = new ArrayList<>();
        for (KbChunk c : chunks.findAll()) {
            double[] v = unpack(c.getEmbedding());
            double dot = 0; for (int i = 0; i < Math.min(qv.length, v.length); i++) dot += qv[i] * v[i];
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("documentId", c.getDocumentId());
            m.put("text", c.getText());
            m.put("score", dot);
            scored.add(m);
        }
        scored.sort((a, b) -> Double.compare((double) b.get("score"), (double) a.get("score")));
        return scored.subList(0, Math.min(topK, scored.size()));
    }
}
