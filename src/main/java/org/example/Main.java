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

public class Main {
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
        JSONObject dependencies = (JSONObject) versionInfo.get("dependencies");
        TreeMap<String, String> dependenciesMap = new TreeMap<>(dependencies);

        return dependenciesMap;
    }

    public static void printGraph(TreeMap<String, String> dependencies) {

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

        System.out.printf("Зависимости пакета %s версии %s:\n", packageName, packageVersion);
        TreeMap<String, String> dependencies = getDependencies(versionsMap.get(packageVersion));

        for (Map.Entry<String, String> dep: dependencies.entrySet()) {
            System.out.println(dep.getKey() + ": " + dep.getValue().substring(1));
        }
    }
}