package com.example.j2ee_fitness_nutrition_ecommerce.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Shop pagination: while you are on pages 1–8, shows 1…8 … last.
 * From page 9 onward, the numeric window slides with the current page (e.g. 1 … 7 8 9 10 11 … 23).
 * If total pages ≤ 9, shows every page.
 */
public final class PaginationHelper {

    /** How many leading pages (1-based: 1..8) stay visible before switching to sliding window. */
    private static final int FIRST_BLOCK_SIZE = 8;
    /** Pages on each side of the current page when in sliding mode. */
    private static final int NEIGHBORS = 2;

    private PaginationHelper() {
    }

    public record PageLink(boolean ellipsis, Integer pageIndex) {
    }

    /**
     * @param currentZeroBased current page index (0-based)
     * @param totalPages         total number of pages
     */
    public static List<PageLink> buildAdaptive(int currentZeroBased, int totalPages) {
        if (totalPages <= 1) {
            return List.of();
        }
        if (totalPages <= 9) {
            List<PageLink> out = new ArrayList<>(totalPages);
            for (int i = 0; i < totalPages; i++) {
                out.add(new PageLink(false, i));
            }
            return out;
        }

        int last = totalPages - 1;
        int c = Math.max(0, Math.min(currentZeroBased, last));

        TreeSet<Integer> indices = new TreeSet<>();
        indices.add(0);
        indices.add(last);

        if (c < FIRST_BLOCK_SIZE) {
            for (int i = 0; i < FIRST_BLOCK_SIZE; i++) {
                indices.add(i);
            }
        } else {
            int from = Math.max(0, c - NEIGHBORS);
            int to = Math.min(last, c + NEIGHBORS);
            for (int i = from; i <= to; i++) {
                indices.add(i);
            }
        }

        List<PageLink> out = new ArrayList<>();
        int prev = -1;
        for (int p : indices) {
            if (prev >= 0 && p - prev > 1) {
                out.add(new PageLink(true, null));
            }
            out.add(new PageLink(false, p));
            prev = p;
        }
        return out;
    }
}
