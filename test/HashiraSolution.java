import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class HashiraSolution {

    // Simple JSON parser for this specific format
    static class SimpleJSON {
        private String json;

        public SimpleJSON(String json) {
            this.json = json.replaceAll("\\s+", "");
        }

        public int getInt(String path) {
            String pattern = "\"" + path + "\":" + "(\\d+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
            return 0;
        }

        public String getString(String key, String subkey) {
            String pattern = "\"" + key + "\":\\{[^}]*\"" + subkey + "\":\"([^\"]+)\"";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
            return "";
        }

        public Set<String> getRootKeys() {
            Set<String> keys = new HashSet<>();
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"(\\d+)\":\\{\"base\":");
            java.util.regex.Matcher m = p.matcher(json);
            while (m.find()) {
                keys.add(m.group(1));
            }
            return keys;
        }
    }

    // Convert value from given base to decimal
    private static BigInteger decodeValue(String value, int base) {
        return new BigInteger(value, base);
    }

    // Optimized Lagrange interpolation to find constant term using BigInteger and fraction reduction
    private static BigInteger findConstantTerm(int[] x, BigInteger[] y, int k) {
        BigInteger resultNum = BigInteger.ZERO;
        BigInteger resultDen = BigInteger.ONE;

        for (int i = 0; i < k; i++) {
            BigInteger num = y[i];
            BigInteger den = BigInteger.ONE;
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    num = num.multiply(BigInteger.valueOf(-x[j]));
                    den = den.multiply(BigInteger.valueOf(x[i] - x[j]));
                    BigInteger g = num.gcd(den);
                    if (!g.equals(BigInteger.ONE)) {
                        num = num.divide(g);
                        den = den.divide(g);
                    }
                }
            }
            // Add to result fraction
            resultNum = resultNum.multiply(den).add(num.multiply(resultDen));
            resultDen = resultDen.multiply(den);
            BigInteger g = resultNum.gcd(resultDen);
            if (!g.equals(BigInteger.ONE)) {
                resultNum = resultNum.divide(g);
                resultDen = resultDen.divide(g);
            }
        }

        // Final division to get constant term
        return resultNum.divide(resultDen);
    }

    public static BigInteger solve(String jsonContent) {
        SimpleJSON json = new SimpleJSON(jsonContent);
        int k = json.getInt("k");

        Set<String> rootKeys = json.getRootKeys();
        List<String> sortedKeys = new ArrayList<>(rootKeys);
        sortedKeys.sort(Comparator.comparingInt(Integer::parseInt));

        int[] x = new int[k];
        BigInteger[] y = new BigInteger[k];

        for (int i = 0; i < k; i++) {
            String key = sortedKeys.get(i);
            x[i] = Integer.parseInt(key);

            String base = json.getString(key, "base");
            String value = json.getString(key, "value");

            y[i] = decodeValue(value, Integer.parseInt(base));
        }

        return findConstantTerm(x, y, k);
    }

    public static String readFile(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.err.println("Error reading file " + filename + ": " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Hashira Placements Assignment ===\n");

        // Test Case 1
        System.out.println("=== TEST CASE 1 ===");
        String testCase1Content = readFile("testcase1.json");
        if (testCase1Content != null) {
            BigInteger result1 = solve(testCase1Content);
            System.out.println("Secret (Constant Term): " + result1);
            System.out.println();
        } else {
            System.out.println("Could not read testcase1.json");
        }

        // Test Case 2
        System.out.println("=== TEST CASE 2 ===");
        String testCase2Content = readFile("testcase2.json");
        if (testCase2Content != null) {
            BigInteger result2 = solve(testCase2Content);
            System.out.println("Secret (Constant Term): " + result2);
            System.out.println();
        } else {
            System.out.println("Could not read testcase2.json");
        }
    }
}
