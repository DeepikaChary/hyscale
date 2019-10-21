package io.hyscale.ctl.generator.services.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class JsonTreeUtil {

    private static final String JSON_PATH_ARRAY_REGEX = "\\w+(\\[([0-9]*|\\*)?\\])+";
    private static final Pattern arrayRegexPattern = Pattern.compile(JSON_PATH_ARRAY_REGEX);

    private static boolean isEmptyArray(JsonNode jsonNode) {
        if (jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            return arrayNode.size() == 0;
        }
        return false;
    }

    public static String getParentKey(String field) {
        if (StringUtils.isBlank(field)) {
            return null;
        }
        if (!field.contains(".")) {
            return null;
        }
        String parentKey = field.substring(0, field.lastIndexOf("."));
        return parentKey;
    }

    public static String getKey(String field) {
        if (StringUtils.isBlank(field)) {
            return null;
        }
        if (!field.contains(".")) {
            return field;
        }
        String key = field.substring(field.lastIndexOf(".") + 1);
        return key;
    }

    public static String getSanitizedArrayPath(String field) {
        if (StringUtils.isBlank(field)) {
            return null;
        }

        String[] paths = field.split("\\.");
        String leafPath = paths[paths.length - 1];
        if (arrayRegexPattern.matcher(leafPath).matches()) {
            return field.substring(0, (field.length() - leafPath.length()) + leafPath.lastIndexOf("["));
        } else {
            return field;
        }
    }

    public static int getArrayIndex(String field) {
        if (StringUtils.isNotBlank(field)) {
            return -1;
        }
        String[] paths = field.split("\\.");
        String leafPath = paths[paths.length - 1];
        try {
            if (arrayRegexPattern.matcher(leafPath).matches()) {
                int start = leafPath.lastIndexOf("[");
                int end = leafPath.lastIndexOf("]");
                return Integer.parseInt(leafPath.substring(start + 1, end));
            }
        } catch (NumberFormatException e) {
            System.out.println(e);
        }
        return -1;
    }

    public static boolean isArrayPath(String path) {
        return arrayRegexPattern.matcher(path).matches();
    }
}
