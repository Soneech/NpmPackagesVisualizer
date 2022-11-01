package org.example;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class NpmPackagesVisualizer {
    private static final String NPM_API_URL = "https://registry.npmjs.org/";

    public static JSONObject getJson(String packageName) throws IOException, ParseException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(NPM_API_URL + packageName);
        request.addHeader("accept", "application/json");

        HttpResponse response = httpClient.execute(request);

        String json = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(json);

        return jsonObject;
    }

    public static TreeMap<String, JSONObject> getVersions(JSONObject jsonObject) {
        JSONObject versions = (JSONObject) jsonObject.get("versions");
        TreeMap<String, JSONObject> versionsMap = new TreeMap<>(versions);

        return versionsMap;
    }

    public static TreeMap<String, String> getDependencies(JSONObject versionInfo) {
        TreeMap<String, String> dependenciesMap;
        try {
            JSONObject dependencies = (JSONObject) versionInfo.get("dependencies");
            dependenciesMap = new TreeMap<>(dependencies);
        } catch (NullPointerException exception) {
            dependenciesMap = null;
        }
        return dependenciesMap;
    }

    public static void printGraph(TreeMap<String, String> dependencies, String packageName, String packageVersion, int level) throws IOException, ParseException {
        StringBuilder tabs = new StringBuilder();
        for (int i = 0; i < level; i++) {
            tabs.append("   ");
        }
        if (dependencies != null) {
            for (Map.Entry<String, String> dep: dependencies.entrySet()) {
                System.out.println(
                        tabs + packageName + " " + packageVersion + " -> " + dep.getKey() + " " + dep.getValue());

                JSONObject jsonObject = getJson(dep.getKey());
                TreeMap<String, JSONObject> versionsMap = getVersions(jsonObject);
                JSONObject versionInfo = versionsMap.get(dep.getValue().substring(1));

                TreeMap<String, String> depend = getDependencies(versionInfo);

                printGraph(depend, dep.getKey(), dep.getValue().substring(1), level + 1);
            }
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
        Scanner scanner = new Scanner(System.in);

        String packageName, packageVersion;
        System.out.println("Введите имя пакета:");
        packageName = scanner.nextLine();

        JSONObject jsonObject = getJson(packageName);
        TreeMap<String, JSONObject> versionsMap = getVersions(jsonObject);
        System.out.println("Доступные версии:");

        for (String version: versionsMap.keySet()) {
            System.out.println(version);
        }

        System.out.println("Введите версию:");
        packageVersion = scanner.nextLine();

        TreeMap<String, String> dependencies = getDependencies(versionsMap.get(packageVersion));

        System.out.println("digraph DEPENDENCIES {");
        printGraph(dependencies, packageName, packageVersion, 1);
        System.out.println("}");
    }
}