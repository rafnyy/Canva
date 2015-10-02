package com.example;

import java.math.BigInteger;
import java.security.SecureRandom;

final class UniqueIdentifierGenerator {
    private final SecureRandom random = new SecureRandom();

    public String nextUniqueId() {
        return new BigInteger(130, random).toString(32);
    }
}
