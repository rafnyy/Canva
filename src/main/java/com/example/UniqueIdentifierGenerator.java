package com.example;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class UniqueIdentifierGenerator {
    private SecureRandom random = new SecureRandom();

    public String nextUniqueId() {
        return new BigInteger(130, random).toString(32);
    }
}
