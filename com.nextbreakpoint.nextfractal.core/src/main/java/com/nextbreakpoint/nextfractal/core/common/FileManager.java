/*
 * NextFractal 2.3.2
 * https://github.com/nextbreakpoint/nextfractal
 *
 * Copyright 2015-2024 Andrea Medeghini
 *
 * This file is part of NextFractal.
 *
 * NextFractal is an application for creating fractals and other graphics artifacts.
 *
 * NextFractal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * NextFractal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with NextFractal.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.nextbreakpoint.nextfractal.core.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextbreakpoint.common.either.Either;
import com.nextbreakpoint.common.command.Command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.nextbreakpoint.nextfractal.core.common.Plugins.tryFindFactory;

public abstract class FileManager {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Either<Bundle> loadBundle(File file) {
        try (InputStream is = new FileInputStream(file)) {
            return loadBundle(is);
        } catch (Exception e) {
            return Either.failure(e);
        }
    }

    public static Either<Bundle> saveBundle(File file, Bundle bundle) {
        try (OutputStream os = new FileOutputStream(file)) {
            return saveBundle(os, bundle);
        } catch (Exception e) {
            return Either.failure(e);
        }
    }

    public static Either<Bundle> loadBundle(InputStream stream) {
        try (ZipInputStream is = new ZipInputStream(stream)) {
            return loadBundle(is).execute();
        } catch (Exception e) {
            return Either.failure(e);
        }
    }

    public static Either<Bundle> saveBundle(OutputStream stream, Bundle bundle) {
        try (ZipOutputStream os = new ZipOutputStream(stream)) {
            return saveBundle(os, bundle).execute();
        } catch (Exception e) {
            return Either.failure(e);
        }
    }

    public static Either<Bundle> decodeBundle(List<FileEntry> entries) {
        return tryDecodeBundle(entries).execute();
    }

    public static Either<List<FileEntry>> encodeBundle(Bundle bundle) {
        return tryEncodeBundle(bundle).execute();
    }

    private static Command<Bundle> tryDecodeBundle(List<FileEntry> entries) {
        return decodeManifest(entries)
                .flatMap(manifest -> decodeScript(entries)
                        .flatMap(script -> decodeMetadata(entries, getPluginId(manifest))
                                .flatMap(metadata -> createBundle(getPluginId(manifest), script, metadata)
                                        .flatMap(bundle -> decodeClips(entries)
                                                .flatMap(clips -> createBundle(bundle, clips))
                                        )
                                )
                        )
                );
    }

    private static Command<List<FileEntry>> tryEncodeBundle(Bundle bundle) {
        return encodeManifest(bundle)
                .flatMap(manifest -> encodeScript(bundle)
                        .flatMap(script -> encodeMetadata(bundle)
                                .flatMap(metadata -> encodeClips(bundle)
                                        .flatMap(clips -> createEntries(manifest, script, metadata, clips))
                                )
                        )
                );
    }

    private static Command<Bundle> loadBundle(ZipInputStream is) {
        return readEntries(is).flatMap(FileManager::tryDecodeBundle);
    }

    private static Command<Bundle> saveBundle(ZipOutputStream os, Bundle bundle) {
        return tryEncodeBundle(bundle).flatMap(entries -> writeEntries(os, entries)).map(x -> bundle);
    }

    private static Command<Bundle> createBundle(String pluginId, String script, Metadata metadata) {
        return Command.of(tryFindFactory(pluginId)).map(factory -> new Bundle(factory.createSession(script, metadata), List.of()));
    }

    private static Command<Bundle> createBundle(Bundle bundle, List<Clip> clips) {
        return Command.of(() -> new Bundle(bundle.getSession(), clips));
    }

    private static Command<List<FileEntry>> createEntries(FileEntry manifest, FileEntry script, FileEntry metadata, FileEntry clips) {
        return Command.value(List.of(manifest, script, metadata, clips));
    }

    private static Command<Map<String, String>> decodeManifest(List<FileEntry> entries) {
        return entries.stream()
                .filter(entry -> entry.getName().equals("manifest"))
                .findFirst()
                .map(entry -> decodeManifest(entry.getData()))
                .orElse(Command.error(new Exception("Manifest entry is required")));
    }

    private static Command<String> decodeScript(List<FileEntry> entries) {
        return entries.stream()
                .filter(entry -> entry.getName().equals("script"))
                .findFirst()
                .map(entry -> decodeScript(entry.getData()))
                .orElse(Command.error(new Exception("Script entry is required")));
    }

    private static Command<Metadata> decodeMetadata(List<FileEntry> entries, String pluginId) {
        return entries.stream()
                .filter(entry -> entry.getName().equals("metadata"))
                .findFirst()
                .map(entry -> decodeMetadata(pluginId, entry.getData()))
                .orElse(Command.error(new Exception("Metadata entry is required")));
    }

    private static Command<List<Clip>> decodeClips(List<FileEntry> entries) {
        return entries.stream()
                .filter(entry -> entry.getName().equals("clips"))
                .findFirst()
                .map(entry1 -> decodeClips(entry1.getData()))
                .orElse(Command.value(List.of()));
    }

    private static Command<Map<String, String>> decodeManifest(byte[] data) {
        return Command.of(() -> MAPPER.readValue(data, new TypeReference<>() {}));
    }

    private static Command<String> decodeScript(byte[] data) {
        return Command.value(new String(data));
    }

    private static Command<Metadata> decodeMetadata(String pluginId, byte[] data) {
        return decodeMetadata(pluginId, new String(data));
    }

    private static Command<List<Clip>> decodeClips(byte[] data) {
        return Command.of(() -> MAPPER.readTree(data)).flatMap(FileManager::decodeClips);
    }

    private static Command<List<Clip>> decodeClips(JsonNode clips) {
        final List<Either<Clip>> results = JsonUtils.getClips(clips)
                .map(FileManager::decodeClip)
                .map(Command::execute)
                .takeWhile(Either::isSuccess)
                .toList();
        final Optional<Either<Clip>> error = results.stream().filter(Either::isFailure).findFirst();
        return error.<Command<List<Clip>>>map(result -> createFailure("Can't decode clips", result))
                .orElseGet(() -> Command.value(results.stream().map(Either::get).toList()));
    }

    private static Command<Clip> decodeClip(JsonNode clip) {
        final Map<String, Object> stateMap = new HashMap<>();
        final List<Either<ClipEvent>> results = JsonUtils.getEvents(clip.get("events"))
                .map(clipEvent -> decodeClipEvent(stateMap, clipEvent))
                .map(Command::execute)
                .takeWhile(Either::isSuccess)
                .toList();
        final Optional<Either<ClipEvent>> error = results.stream().filter(Either::isFailure).findFirst();
        return error.<Command<Clip>>map(result -> createFailure("Can't decode clip", result))
                .orElseGet(() -> Command.value(new Clip(results.stream().map(Either::get).toList())));
    }

    private static Command<ClipEvent> decodeClipEvent(Map<String, Object> stateMap, JsonNode clipEvent) {
        try {
            final String pluginId = JsonUtils.getString(clipEvent, "pluginId");
            final String script = JsonUtils.getString(clipEvent, "script");
            final String data = JsonUtils.getString(clipEvent, "metadata");
            final Long date =JsonUtils. getLong(clipEvent, "date");

            if (pluginId != null) {
                stateMap.put("pluginId", pluginId);
            }

            if (script != null) {
                stateMap.put("script", script);
            }

            final Metadata metadata = data != null ? decodeMetadata((String) stateMap.get("pluginId"), new String(data.getBytes())).execute().orThrow().get() : null;

            if (metadata != null) {
                stateMap.put("metadata", metadata);
            }

            if (date != null) {
                stateMap.put("date", new Date(date));
            }

            return Command.value(new ClipEvent((Date) stateMap.get("date"), (String) stateMap.get("pluginId"), (String) stateMap.get("script"), (Metadata) stateMap.get("metadata")));
        } catch (Exception e) {
            return Command.error(e);
        }
    }

    private static Command<FileEntry> encodeManifest(Bundle bundle) {
        return encodeManifest(bundle.getSession()).map(entry -> new FileEntry("manifest", entry));
    }

    private static Command<FileEntry> encodeScript(Bundle bundle) {
        return encodeScript(bundle.getSession()).map(entry -> new FileEntry("script", entry));
    }

    private static Command<FileEntry> encodeMetadata(Bundle bundle) {
        return encodeMetadata(bundle.getSession()).map(entry -> new FileEntry("metadata", entry));
    }

    private static Command<FileEntry> encodeClips(Bundle bundle) {
        return encodeClips(bundle.getSession(), bundle.getClips()).map(entry -> new FileEntry("clips", entry));
    }

    private static Command<byte[]> encodeManifest(Session session) {
        return Command.of(() -> MAPPER.writeValueAsBytes(new FileManifest(session.getPluginId())));
    }

    private static Command<byte[]> encodeScript(Session session) {
        return Command.of(() -> session.getScript().getBytes());
    }

    private static Command<byte[]> encodeMetadata(Session session) {
        return encodeMetadata(session.getPluginId(), session.getMetadata()).map(String::getBytes);
    }

    private static Command<byte[]> encodeClips(Session session, List<Clip> clips) {
        return encodeClips(clips).flatMap(data -> Command.of(() -> MAPPER.writeValueAsBytes(data)));
    }

    private static Command<Object> encodeClips(List<Clip> clips) {
        final List<Either<Object>> results = clips.stream()
                .map(FileManager::encodeClip)
                .map(Command::execute)
                .takeWhile(Either::isSuccess)
                .toList();
        final Optional<Either<Object>> error = results.stream().filter(Either::isFailure).findFirst();
        return error.map(result -> createFailure("Can't encode clips", result))
                .orElseGet(() -> Command.value(results.stream().map(Either::get).toList()));
    }

    private static Command<Object> encodeClip(Clip clip) {
        final Map<String, Object> stateMap = new HashMap<>();
        final List<Either<Object>> results = clip.getEvents().stream()
                .map(clipEvent -> encodeClipEvent(stateMap, clipEvent))
                .map(Command::execute)
                .takeWhile(Either::isSuccess)
                .toList();
        final Optional<Either<Object>> error = results.stream().filter(Either::isFailure).findFirst();
        return error.map(result -> createFailure("Can't encode clip", result))
                .orElseGet(() -> Command.value(Map.of("events", results.stream().map(Either::get).toList())));
    }

    private static Command<Object> encodeClipEvent(Map<String, Object> stateMap, ClipEvent clipEvent) {
        try {
            final Map<String, Object> eventMap = new HashMap<>();

            if (!clipEvent.getPluginId().equals(stateMap.get("pluginId"))) {
                eventMap.put("pluginId", clipEvent.getPluginId());
            }

            if (!clipEvent.getScript().equals(stateMap.get("script"))) {
                eventMap.put("script", clipEvent.getScript());
            }

            final String metadata = encodeMetadata(clipEvent.getPluginId(), clipEvent.getMetadata()).execute().orThrow().get();

            if (!metadata.equals(stateMap.get("metadata"))) {
                eventMap.put("metadata", metadata);
            }

            final Long date = clipEvent.getDate().getTime();

            if (!date.equals(stateMap.get("date"))) {
                eventMap.put("date", date);
            }

            stateMap.putAll(eventMap);

            return Command.value(eventMap);
        } catch (Exception e) {
            return Command.error(e);
        }
    }

    private static Command<Metadata> decodeMetadata(String pluginId, String metadata) {
        return Command.of(tryFindFactory(pluginId)).flatMap(factory -> Command.of(() -> factory.createMetadataCodec().decodeMetadata(metadata)));
    }

    private static Command<String> encodeMetadata(String pluginId, Metadata metadata) {
        return Command.of(tryFindFactory(pluginId)).flatMap(factory -> Command.of(() -> factory.createMetadataCodec().encodeMetadata(metadata)));
    }

    private static Command<List<FileEntry>> readEntries(ZipInputStream is) {
        try {
            final List<FileEntry> entries = new LinkedList<>();
            for (ZipEntry entry = is.getNextEntry(); entry != null; entry = is.getNextEntry())
                entries.add(readEntry(is, entry));
            return Command.value(entries);
        } catch (Exception e) {
            return Command.error(e);
        }
    }

    private static FileEntry readEntry(ZipInputStream is, ZipEntry entry) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            IOUtils.copyBytes(is, baos);
            return new FileEntry(entry.getName(), baos.toByteArray());
        }
    }

    private static Command<Void> writeEntries(ZipOutputStream os, List<FileEntry> entries) {
        return entries.stream()
                .map(entry -> Command.of(() -> writeEntry(os, entry)))
                .map(Command::execute)
                .filter(Either::isFailure)
                .map(result -> Command.<Void>error(result.exception()))
                .findFirst()
                .orElseGet(() -> Command.value(null));
    }

    private static Void writeEntry(ZipOutputStream os, FileEntry entry) throws IOException {
        final ZipEntry zipEntry = new ZipEntry(entry.getName());
        os.putNextEntry(zipEntry);
        os.write(entry.getData());
        os.closeEntry();
        return null;
    }

    private static String getPluginId(Map<String, String> manifest) {
        return manifest.getOrDefault("pluginId", "");
    }

    private static <T> Command<T> createFailure(String message, Either<?> error) {
        return Command.error(new RuntimeException(message, error.exception()));
    }
}
