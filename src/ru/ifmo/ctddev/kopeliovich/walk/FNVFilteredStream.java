package ru.ifmo.ctddev.kopeliovich.walk;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Kopeliovich Anna on 12.02.2017.
 */
public class FNVFilteredStream extends FilterInputStream{
    private static final int FNV_32_INIT = 0x811c9dc5;
    private static final int FNV_32_PRIME = 0x01000193;
    private int hash = FNV_32_INIT;

    protected FNVFilteredStream(InputStream in) {
        super(in);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    private void countHash(int val) {
        hash *= FNV_32_PRIME;
        hash ^= val & 255;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int cap = super.read(b, off, len);
        if (cap == -1)
            return cap;
        for (int i = off; i < off + cap; i++) {
            countHash(b[i]);
        }
        return cap;
    }

    @Override
    public int read() throws IOException {
        int cap = super.read();
        if (cap != -1) {
            countHash(cap);
        }
        return cap;
    }

    public int getHash() {
        return hash;
    }
}
