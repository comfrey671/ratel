package org.nico.ratel.landlords.features;

import java.util.*;

public class Features {

    public final static String VERSION_1_4_0 = "v1.4.0";
    public final static String READY = "READY";
    private final static Map<String, List<String>> FEATURES = new HashMap<>();

    static{
        FEATURES.put(VERSION_1_4_0, Collections.singletonList(READY));
    }

    public static boolean supported(String clientVersion, String feature){
        List<String> features = FEATURES.get(clientVersion);
        if (Objects.isNull(features) || Objects.isNull(feature)){
            return false;
        }
        return features.contains(feature.toUpperCase(Locale.ROOT));
    }

}
